/*
 * SonarQube Unisys C Plugin
 * Copyright (C) 2010-2025 SonarSource Sàrl
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the Sonar Source-Available License Version 1, as published by SonarSource SA.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Sonar Source-Available License for more details.
 *
 * You should have received a copy of the Sonar Source-Available License
 * along with this program; if not, see https://sonarsource.com/license/ssal/
 */
package org.sonar.plugins.c;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.RecognitionException;
import com.sonar.sslr.impl.Parser;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.sonar.api.SonarProduct;
import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.measures.Metric;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.Version;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.c.CCheck;
import org.sonar.c.CVisitorContext;
import org.sonar.c.Issue;
import org.sonar.c.checks.CheckList;
import org.sonar.c.lexer.CLexer;
import org.sonar.c.metrics.CyclomaticComplexityVisitor;
import org.sonar.c.metrics.FileMetrics;
import org.sonar.c.parser.CParser;
import org.sonar.plugins.c.core.C;
import org.sonar.sslr.parser.LexerlessGrammar;
import org.sonarsource.analyzer.commons.ProgressReport;

public class CSquidSensor implements Sensor {

  private static final Logger LOG = Loggers.get(CSquidSensor.class);

  private final SonarRuntime sonarRuntime;
  private final Checks<CCheck> checks;
  private final FileLinesContextFactory fileLinesContextFactory;

  public CSquidSensor(SonarRuntime sonarRuntime, CheckFactory checkFactory,
      FileLinesContextFactory fileLinesContextFactory) {
    this.sonarRuntime = sonarRuntime;
    this.checks = checkFactory
        .<CCheck>create(CheckList.REPOSITORY_KEY)
        .addAnnotatedChecks(CheckList.getChecks());
    this.fileLinesContextFactory = fileLinesContextFactory;
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
        .name("C")
        .onlyOnFileType(InputFile.Type.MAIN)
        .onlyOnLanguage(C.KEY);
    processesFilesIndependently(descriptor);
  }

  private void processesFilesIndependently(SensorDescriptor descriptor) {
    if ((sonarRuntime.getProduct() == SonarProduct.SONARLINT)
        || !sonarRuntime.getApiVersion().isGreaterThanOrEqual(Version.create(9, 3))) {
      return;
    }
    try {
      Method method = descriptor.getClass().getMethod("processesFilesIndependently");
      method.invoke(descriptor);
    } catch (ReflectiveOperationException e) {
      LOG.warn("Could not call SensorDescriptor.processesFilesIndependently() method", e);
    }
  }

  @Override
  public void execute(SensorContext context) {
    FileSystem fileSystem = context.fileSystem();
    FilePredicates predicates = fileSystem.predicates();

    FilePredicate filePredicate = predicates.and(
        predicates.hasType(InputFile.Type.MAIN),
        predicates.hasLanguage(C.KEY),
        inputFile -> !inputFile.uri().getPath().endsWith("mxml"));

    List<InputFile> inputFiles = new ArrayList<>();
    fileSystem.inputFiles(filePredicate).forEach(inputFiles::add);

    ProgressReport progressReport = new ProgressReport("Report about progress of the Unisys C analyzer",
        TimeUnit.SECONDS.toMillis(10));
    List<String> filenames = inputFiles.stream().map(InputFile::toString).collect(Collectors.toList());
    progressReport.start(filenames);

    for (InputFile inputFile : inputFiles) {
      analyseFile(context, inputFile.charset(), inputFile);
      progressReport.nextFile();
    }

    progressReport.stop();
  }

  private void analyseFile(SensorContext context, Charset charset, InputFile inputFile) {
    String fileContent;
    try {
      fileContent = inputFile.contents();
    } catch (IOException e) {
      throw new IllegalStateException("Cannot read " + inputFile, e);
    }

    Parser<LexerlessGrammar> parser = CParser.create(charset);
    CVisitorContext visitorContext;
    try {
      visitorContext = new CVisitorContext(fileContent, parser.parse(fileContent));
      saveMeasures(context, inputFile, visitorContext);
    } catch (RecognitionException e) {
      visitorContext = new CVisitorContext(fileContent, e);
      LOG.error("Unable to parse file: {}", inputFile);
      LOG.error(e.getMessage());
    }

    for (CCheck check : checks.all()) {
      saveIssues(context, check, check.scanFileForIssues(visitorContext), inputFile);
    }

    new CTokensVisitor(context, CLexer.create(charset), inputFile).scanFile(visitorContext);
  }

  private void saveIssues(SensorContext context, CCheck check, List<Issue> issues, InputFile inputFile) {
    for (Issue cIssue : issues) {
      RuleKey ruleKey = checks.ruleKey(check);
      NewIssue issue = context.newIssue();
      NewIssueLocation location = issue.newLocation()
          .on(inputFile)
          .message(cIssue.message());
      Integer line = cIssue.line();
      if (line != null) {
        location.at(inputFile.selectLine(line));
      }
      Double cost = cIssue.cost();
      if (cost != null) {
        issue.gap(cost);
      }
      issue.at(location).forRule(Objects.requireNonNull(ruleKey)).save();
    }
  }

  private void saveMeasures(SensorContext context, InputFile inputFile, CVisitorContext visitorContext) {
    FileMetrics metrics = new FileMetrics(visitorContext);
    saveMeasure(context, inputFile, CoreMetrics.NCLOC, metrics.linesOfCode().size());
    saveMeasure(context, inputFile, CoreMetrics.COMMENT_LINES, metrics.commentLines().size());
    saveMeasure(context, inputFile, CoreMetrics.CLASSES, metrics.numberOfClasses());
    saveMeasure(context, inputFile, CoreMetrics.FUNCTIONS, metrics.numberOfFunctions());
    saveMeasure(context, inputFile, CoreMetrics.STATEMENTS, metrics.numberOfStatements());
    context.<String>newMeasure().on(inputFile).forMetric(CoreMetrics.EXECUTABLE_LINES_DATA)
        .withValue(metrics.executableLines()).save();

    FileLinesContext fileLinesContext = fileLinesContextFactory.createFor(inputFile);
    metrics.linesOfCode().forEach(line -> fileLinesContext.setIntValue(CoreMetrics.NCLOC_DATA_KEY, line, 1));
    fileLinesContext.save();

    AstNode root = visitorContext.rootTree();
    int fileComplexity = CyclomaticComplexityVisitor.complexity(Objects.requireNonNull(root));
    saveMeasure(context, inputFile, CoreMetrics.COMPLEXITY, fileComplexity);
  }

  private static void saveMeasure(SensorContext context, InputFile inputFile, Metric<Integer> metric, int value) {
    context.<Integer>newMeasure()
        .on(inputFile)
        .forMetric(metric)
        .withValue(value)
        .save();
  }

}
