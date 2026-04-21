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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;
import org.sonar.api.SonarEdition;
import org.sonar.api.SonarQubeSide;
import org.sonar.api.SonarRuntime;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder;
import org.sonar.api.batch.rule.internal.NewActiveRule;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.FileLinesContext;
import org.sonar.api.measures.FileLinesContextFactory;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.testfixtures.log.LogTesterJUnit5;
import org.sonar.api.utils.Version;
import org.sonar.api.utils.log.LoggerLevel;
import org.sonar.plugins.c.core.C;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CSquidSensorTest {

  private static final File TEST_DIR = new File("src/test/resources/org/sonar/plugins/c/squid");
  private static final SonarRuntime SONARQUBE_89 = SonarRuntimeImpl.forSonarQube(Version.create(8, 9),
      SonarQubeSide.SCANNER, SonarEdition.DEVELOPER);

  private CSquidSensor sensor;
  private SensorContextTester tester;

  @RegisterExtension
  public LogTesterJUnit5 logTester = new LogTesterJUnit5();

  @BeforeEach
  public void setUp() {
    createSensor(SONARQUBE_89);
    tester = SensorContextTester.create(TEST_DIR);
  }

  private CSquidSensor createSensor(SonarRuntime sonarRuntime) {
    ActiveRulesBuilder activeRulesBuilder = new ActiveRulesBuilder();
    activeRulesBuilder
        .addRule(new NewActiveRule.Builder().setRuleKey(RuleKey.of("c", "S1314")).setSeverity("BLOCKER").build());
    ActiveRules activeRules = activeRulesBuilder.build();
    CheckFactory checkFactory = new CheckFactory(activeRules);
    FileLinesContextFactory fileLinesContextFactory = mock(FileLinesContextFactory.class);
    FileLinesContext fileLinesContext = mock(FileLinesContext.class);
    when(fileLinesContextFactory.createFor(Mockito.any(InputFile.class))).thenReturn(fileLinesContext);
    sensor = new CSquidSensor(sonarRuntime, checkFactory, fileLinesContextFactory);
    return sensor;
  }

  @Test
  public void analyse() throws IOException {
    DefaultFileSystem fs = new DefaultFileSystem(TEST_DIR);
    fs.setEncoding(StandardCharsets.UTF_8);
    tester.setFileSystem(fs);
    fs.add(inputFile("SmallFile.ccc_m"));

    sensor.execute(tester);

    String componentKey = "key:SmallFile.ccc_m";
    assertThat(tester.measure(componentKey, CoreMetrics.NCLOC).value()).isEqualTo(15);
    assertThat(tester.measure(componentKey, CoreMetrics.COMMENT_LINES).value()).isEqualTo(2);
    assertThat(tester.measure(componentKey, CoreMetrics.STATEMENTS).value()).isEqualTo(4);
    assertThat(tester.measure(componentKey, CoreMetrics.FUNCTIONS).value()).isEqualTo(3);
    assertThat(tester.measure(componentKey, CoreMetrics.COMPLEXITY).value()).isEqualTo(3);
    assertThat(tester.measure(componentKey, CoreMetrics.EXECUTABLE_LINES_DATA).value()).isEqualTo("4=1;9=1;10=1;16=1;");

    assertThat(tester.cpdTokens(componentKey)).hasSize(15);
  }

  private DefaultInputFile inputFile(String fileName) throws IOException {
    File file = new File(TEST_DIR, fileName);
    String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);

    return TestInputFileBuilder.create("key", fileName)
        .setModuleBaseDir(Paths.get(TEST_DIR.getAbsolutePath()))
        .setType(InputFile.Type.MAIN)
        .setLanguage(C.KEY)
        .setCharset(StandardCharsets.UTF_8)
        .initMetadata(content)
        .build();
  }

  @Test
  public void parse_error() throws IOException {
    DefaultFileSystem fs = new DefaultFileSystem(TEST_DIR);
    fs.setEncoding(StandardCharsets.UTF_8);
    tester.setFileSystem(fs);
    DefaultInputFile inputFile = inputFile("parse_error.ccc_m");
    fs.add(inputFile);
    sensor.execute(tester);
    assertThat(tester.measure(inputFile.key(), CoreMetrics.NCLOC)).isNull();
    assertThat(logTester.logs(LoggerLevel.ERROR).stream()
        .filter(log -> log.startsWith("Unable to parse file: ") && log.endsWith("parse_error.ccc_m"))).isNotEmpty();
  }

  @Test
  public void testDescriptor() {
    DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();
    sensor.describe(descriptor);
    assertThat(descriptor.name()).isEqualTo("C");
    assertThat(descriptor.languages()).containsOnly("unisys_c");
  }

  @Test
  public void test_descriptor_sonarlint() {
    DefaultSensorDescriptor descriptor = new DefaultSensorDescriptor();
    createSensor(SonarRuntimeImpl.forSonarLint(Version.create(6, 5))).describe(descriptor);
    assertThat(descriptor.name()).isEqualTo("C");
    assertThat(descriptor.languages()).containsOnly("unisys_c");
  }

  @Test
  public void test_descriptor_sonarqube_9_3() {
    DefaultSensorDescriptor descriptor = spy(new DefaultSensorDescriptor());
    when(descriptor.processesFilesIndependently()).thenReturn(descriptor);
    createSensor(SonarRuntimeImpl.forSonarQube(Version.create(9, 3), SonarQubeSide.SCANNER, SonarEdition.DEVELOPER))
        .describe(descriptor);
    assertThat(descriptor.name()).isEqualTo("C");
    assertThat(descriptor.languages()).containsOnly("unisys_c");
    verify(descriptor).processesFilesIndependently();
  }

}
