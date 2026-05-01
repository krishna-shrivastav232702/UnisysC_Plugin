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
package org.sonar.c.checks;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class CheckList {

  public static final String REPOSITORY_KEY = "unisys_c";

  public static final String SONAR_WAY_PROFILE = "Sonar way";

  private CheckList() {
  }

  public static List<Class<?>> getChecks() {
    return Collections.unmodifiableList(Arrays.asList(
        CommentRegularExpressionCheck.class,
        LineLengthCheck.class,
        ControlFlowStmtDepthCheck.class,
        XPathCheck.class,
        OneStatementPerLineCheck.class,
        CommentedCodeCheck.class,
        SwitchWithoutDefaultCheck.class,
        NonEmptyCaseWithoutBreakCheck.class,
        FunctionSinglePointOfExitCheck.class,
        FunctionWithTooManyParametersCheck.class,
        EmptyStatementCheck.class,
        TooManyReturnCheck.class,
        CollapsibleIfStatementCheck.class,
        LabelNameCheck.class,
        TooManyLinesInCaseCheck.class,
        OctalValueCheck.class,
        LocalVarAndParameterNameCheck.class,
        EmptyNestedBlockCheck.class,
        FunctionNameCheck.class,
        LocalVarShadowsFieldCheck.class,
        UnusedLocalVariableCheck.class,
        TooManyLinesInFunctionCheck.class,
        NestedSwitchCheck.class,
        EmptyMethodCheck.class,
        UnusedFunctionParametersCheck.class,
        FileHeaderCheck.class,
        VariantStopConditionInForLoopCheck.class,
        DuplicateBranchImplementationCheck.class,
        DefaultCasePositionCheck.class,
        AllBranchesIdenticalCheck.class,
        ParsingErrorCheck.class,
        VolatileLocalVariableCheck.class,
        PointerIndirectionLevelCheck.class,
        MultipleVariableDeclarationCheck.class,
        IfElseIfHasElseCheck.class,
        AllVariablesInitializedCheck.class,
        CognitiveComplexityCheck.class,
        CyclomaticComplexityCheck.class,
        CastAwayQualifiersCheck.class,
        GotoStatementCheck.class,
        AsmDeclarationCheck.class,
        NoUnaryPlusCheck.class,
        NoUnaryMinusOnUnsignedCheck.class,
        NoGlobalVariablesCheck.class,
        NoCastPointerToIntegralCheck.class,
        OctalConstantCheck.class,
        UnsignedLiteralSuffixCheck.class,
        SignedBitFieldLengthCheck.class,
        TooManyLinesInFileCheck.class,
        NoPointerSubtractionCheck.class,
        NoReturnLocalAddressCheck.class,
        NoProgramTerminatingFunctionsCheck.class,
        NoFloatForLoopCounterCheck.class,
        FileEndsWithNewlineCheck.class,
        PreferIfOverSwitchCheck.class,
        LocalStaticVariableCheck.class,
        NoLowercaseLSuffixCheck.class,
        NoBitFieldsCheck.class,
        ConsistentDeclarationTypeCheck.class,
        NonVoidFunctionMustReturnCheck.class,
        NoReturnFunctionShouldBeNoreturnCheck.class,
        NoNestedSwitchLabelCheck.class,
        ForLoopExpressionCheck.class,
        NoPostfixOperatorCheck.class,
        NonPortableIncludeCheck.class,
        EscapeCharacterCheck.class,
        TooManyFieldsInStructCheck.class,
        ArrayBracketPositionCheck.class,
        SwitchNonCaseLabelCheck.class,
        RedundantPointerOperatorCheck.class,
        TooManyCaseCheck.class,
        ExplicitZeroComparisonCheck.class,
        ConstPointerLocalVarCheck.class,
        SingleLineBodyIndentationCheck.class,
        ConditionalsStartOnNewLineCheck.class,
        RepeatedUnaryPrefixOperatorCheck.class,
        IdentifierLengthCheck.class,
        XorNotExponentiationCheck.class,
        IncludeHeaderNameCheck.class,
        EscapeSequenceTerminationCheck.class,
        ElseMatchedWithIfCheck.class,
        EnumBitFieldConsistencyCheck.class,
        FieldNamingConventionCheck.class,
        UnusedFunctionCheck.class,
        NoBitwiseInBooleanContextCheck.class
      ));
  }
}
