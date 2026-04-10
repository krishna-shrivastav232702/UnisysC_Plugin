/*
 * SonarQube Flex Plugin
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
package org.sonar.c;

import com.sonar.sslr.api.GenericTokenType;
import org.sonar.sslr.grammar.GrammarRuleKey;
import org.sonar.sslr.grammar.LexerlessGrammarBuilder;
import org.sonar.sslr.parser.LexerlessGrammar;

import static org.sonar.c.CKeyword.AS;
import static org.sonar.c.CKeyword.AUTO;
import static org.sonar.c.CKeyword.BREAK;
import static org.sonar.c.CKeyword.CASE;
import static org.sonar.c.CKeyword.CATCH;
import static org.sonar.c.CKeyword.CLASS;
import static org.sonar.c.CKeyword.CONST;
import static org.sonar.c.CKeyword.CONTINUE;
import static org.sonar.c.CKeyword.DEFAULT;
import static org.sonar.c.CKeyword.DELETE;
import static org.sonar.c.CKeyword.DO;
import static org.sonar.c.CKeyword.DYNAMIC;
import static org.sonar.c.CKeyword.EACH;
import static org.sonar.c.CKeyword.ELSE;
import static org.sonar.c.CKeyword.EXTENDS;
import static org.sonar.c.CKeyword.FALSE;
import static org.sonar.c.CKeyword.FINALLY;
import static org.sonar.c.CKeyword.FOR;
import static org.sonar.c.CKeyword.FUNCTION;
import static org.sonar.c.CKeyword.GET;
import static org.sonar.c.CKeyword.GOTO;
import static org.sonar.c.CKeyword.IF;
import static org.sonar.c.CKeyword.IMPLEMENTS;
import static org.sonar.c.CKeyword.IMPORT;
import static org.sonar.c.CKeyword.IN;
import static org.sonar.c.CKeyword.INCLUDE;
import static org.sonar.c.CKeyword.INLINE;
import static org.sonar.c.CKeyword.INSTANCEOF;
import static org.sonar.c.CKeyword.INTERFACE;
import static org.sonar.c.CKeyword.INTERNAL;
import static org.sonar.c.CKeyword.IS;
import static org.sonar.c.CKeyword.NAMESPACE;
import static org.sonar.c.CKeyword.NEW;
import static org.sonar.c.CKeyword.NULL;
import static org.sonar.c.CKeyword.PACKAGE;
import static org.sonar.c.CKeyword.PRIVATE;
import static org.sonar.c.CKeyword.PROTECTED;
import static org.sonar.c.CKeyword.PUBLIC;
import static org.sonar.c.CKeyword.REGISTER;
import static org.sonar.c.CKeyword.RETURN;
import static org.sonar.c.CKeyword.SET;
import static org.sonar.c.CKeyword.STATIC;
import static org.sonar.c.CKeyword.SUPER;
import static org.sonar.c.CKeyword.SWITCH;
import static org.sonar.c.CKeyword.THIS;
import static org.sonar.c.CKeyword.THROW;
import static org.sonar.c.CKeyword.TRUE;
import static org.sonar.c.CKeyword.TRY;
import static org.sonar.c.CKeyword.TYPEDEF;
import static org.sonar.c.CKeyword.TYPEOF;
import static org.sonar.c.CKeyword.USE;
import static org.sonar.c.CKeyword.VAR;
import static org.sonar.c.CKeyword.VOID;
import static org.sonar.c.CKeyword.VOLATILE;
import static org.sonar.c.CKeyword.WHILE;
import static org.sonar.c.CKeyword.WITH;
import static org.sonar.c.CKeyword.XML;
import static org.sonar.c.CKeyword.CHAR;
import static org.sonar.c.CKeyword.INT;
import static org.sonar.c.CKeyword.FLOAT;
import static org.sonar.c.CKeyword.DOUBLE;
import static org.sonar.c.CKeyword.SIGNED;
import static org.sonar.c.CKeyword.SIZEOF;
import static org.sonar.c.CKeyword.UNSIGNED;
import static org.sonar.c.CKeyword.LONG;
import static org.sonar.c.CKeyword.SHORT;
import static org.sonar.c.CKeyword.EXTERN;
import static org.sonar.c.CPunctuator.AND;
import static org.sonar.c.CPunctuator.ANDAND;
import static org.sonar.c.CPunctuator.ANDAND_EQU;
import static org.sonar.c.CPunctuator.AND_EQU;
import static org.sonar.c.CPunctuator.AT_SIGN;
import static org.sonar.c.CPunctuator.COLON;
import static org.sonar.c.CPunctuator.COMMA;
import static org.sonar.c.CPunctuator.DIV;
import static org.sonar.c.CPunctuator.DIV_EQU;
import static org.sonar.c.CPunctuator.DOT;
import static org.sonar.c.CPunctuator.DOUBLE_COLON;
import static org.sonar.c.CPunctuator.DOUBLE_DOT;
import static org.sonar.c.CPunctuator.DOUBLE_MINUS;
import static org.sonar.c.CPunctuator.DOUBLE_PLUS;
import static org.sonar.c.CPunctuator.EQUAL1;
import static org.sonar.c.CPunctuator.EQUAL2;
import static org.sonar.c.CPunctuator.EQUAL3;
import static org.sonar.c.CPunctuator.GE;
import static org.sonar.c.CPunctuator.GT;
import static org.sonar.c.CPunctuator.HASH;
import static org.sonar.c.CPunctuator.LBRAKET;
import static org.sonar.c.CPunctuator.LCURLYBRACE;
import static org.sonar.c.CPunctuator.LE;
import static org.sonar.c.CPunctuator.LPARENTHESIS;
import static org.sonar.c.CPunctuator.LT;
import static org.sonar.c.CPunctuator.MINUS;
import static org.sonar.c.CPunctuator.MINUS_EQU;
import static org.sonar.c.CPunctuator.MOD;
import static org.sonar.c.CPunctuator.MOD_EQU;
import static org.sonar.c.CPunctuator.NOT;
import static org.sonar.c.CPunctuator.NOTEQUAL1;
import static org.sonar.c.CPunctuator.NOTEQUAL2;
import static org.sonar.c.CPunctuator.OR;
import static org.sonar.c.CPunctuator.OROR;
import static org.sonar.c.CPunctuator.OROR_EQU;
import static org.sonar.c.CPunctuator.OR_EQU;
import static org.sonar.c.CPunctuator.PLUS;
import static org.sonar.c.CPunctuator.PLUS_EQU;
import static org.sonar.c.CPunctuator.QUERY;
import static org.sonar.c.CPunctuator.RBRAKET;
import static org.sonar.c.CPunctuator.RCURLYBRACE;
import static org.sonar.c.CPunctuator.RPARENTHESIS;
import static org.sonar.c.CPunctuator.SEMICOLON;
import static org.sonar.c.CPunctuator.SL;
import static org.sonar.c.CPunctuator.SL_EQU;
import static org.sonar.c.CPunctuator.SR;
import static org.sonar.c.CPunctuator.SR2;
import static org.sonar.c.CPunctuator.SR_EQU;
import static org.sonar.c.CPunctuator.SR_EQU2;
import static org.sonar.c.CPunctuator.STAR;
import static org.sonar.c.CPunctuator.STAR_EQU;
import static org.sonar.c.CPunctuator.TILD;
import static org.sonar.c.CPunctuator.TRIPLE_DOTS;
import static org.sonar.c.CPunctuator.XOR;
import static org.sonar.c.CPunctuator.XORXOR_EQU;
import static org.sonar.c.CPunctuator.XOR_EQU;

import java.util.List;

public enum CGrammar implements GrammarRuleKey {
    // for C - 

    ABSTRACT_DECLARATOR,
    DIRECT_ABSTRACT_DECLARATOR,
    ARRAY_SUFFIX,
    ARRAY_ABSTRACT_SUFFIX,
    FOR_INIT,
    FUNCTION_SUFFIX,
    LABEL,
    LABEL_NAME,
    COMPOUND_STATEMENT,
    CAST_EXPRESSION,
    CONSTANT_EXPRESSION,
    BLOCK_ITEM_LIST,
    BLOCK_ITEM,
    DECLARATOR,
    DECLARATION,
    DECLARATION_LIST,
    DECLARATION_SPECIFIERS,
    DESIGNATION,
    DESIGNATOR,
    DESIGNATOR_LIST,
    DIRECT_DECLARATOR,
    FUNCTION_ABSTRACT_SUFFIX,
    IDENTIFIER_LIST,
    INIT_DECLARATOR_LIST,
    INIT_DECLARATOR,
    INITIALIZER,
    INITIALIZER_LIST,
    ITERATION_STATEMENT,
    JUMP_STATEMENT,
    CONTROL_STATEMENT,
    SPECIFIER_QUALIFIER_LIST,
    // STATIC_ASSERT_DECLARATION,
    STORAGE_CLASS_SPECIFIER,
    FUNCTION_SPECIFIER,
    POINTER,
    PARAMETER_DECLARATION,
    PARAMETER_LIST,
    PARAMETER_TYPE_LIST,
    TYPE_NAME,
    TYPE_QUALIFIER,
    TYPE_QUALIFIER_LIST,
    TYPE_SPECIFIER,
    UNARY_OPERATOR,

    // existing flex -
    WHITESPACE,
    SPACING,
    SPACING_NO_LB,
    NEXT_NOT_LB,

    EOS,
    EOS_NO_LB,

    STRING,
    NUMBER,
    DECIMAL,
    CONSTANT,
    HEXADECIMAL,
    OCTAL,
    I_CONSTANT,
    F_CONSTANT,
    ENUMERATION_CONSTANT,

    /**
     * EXPRESSIONS
     */
    // <editor-fold defaultstate="collapsed" desc="Expression">
    PRIMARY_EXPR,
    RESERVED_NAMESPACE,
    PARENTHESIZED_EXPR,
    PARENTHESIZED_LIST_EXPR,
    FUNCTION_EXPR,
    OBJECT_INITIALISER,
    FIELD_NAME,
    LITERAL_FIELD,
    ARRAY_INITIALISER,
    ELEMENT_LIST,
    LITERAL_ELEMENT,
    CONDITIONAL_EXPR,
    CONDITIONAL_EXPR_NO_IN,
    POSTFIX_EXPR,
    COMPOUND_ASSIGNMENT,
    LOGICAL_ASSIGNMENT,
    SUPER_EXPR,
    GENERIC_SELECTION,
    GENERIC_ASSOC_LIST,
    GENERIC_ASSOCIATION,
    // Identifiers
    PROPERTY_IDENTIFIER,
    QUALIFIER,
    SIMPLE_QUALIFIED_IDENTIFIER,
    EXPR_QUALIFIED_IDENTIFIER,
    NON_ATTRIBUTE_QUALIFIED_IDENTIFIER,
    QUALIFIED_IDENTIFIER,
    BRACKETS,
    IDENTIFIER,
    IDENTIFIER_PART,
    // New expressions
    FULL_NEW_EXPR,
    FULL_NEW_SUB_EXPR,
    SHORT_NEW_EXPR,
    SHORT_NEW_SUB_EXPR,
    PROPERTY_OPERATOR,
    QUERY_OPERATOR,
    // Call expression
    ARGUMENTS,
    ARGUMENT_EXPRESSION_LIST,
    // Unary expression
    UNARY_EXPR,
    // Binary expression
    MULTIPLICATIVE_EXPR,
    ADDITIVE_EXPR,
    ADDITIVE_OPERATOR,
    SHIFT_EXPR,
    RELATIONAL_EXPR,
    RELATIONAL_EXPR_NO_IN,
    RELATIONAL_OPERATOR,
    RELATIONAL_OPERATOR_NO_IN,
    EQUALITY_EXPR,
    EQUALITY_EXPR_NO_IN,
    EQUALITY_OPERATOR,
    BITEWISE_AND_EXPR,
    BITEWISE_AND_EXPR_NO_IN,
    BITEWISE_XOR_EXPR,
    BITEWISE_XOR_EXPR_NO_IN,
    BITEWISE_OR_EXPR,
    BITEWISE_OR_EXPR_NO_IN,
    LOGICAL_AND_EXPR,
    LOGICAL_AND_EXPR_NO_IN,
    LOGICAL_AND_OPERATOR,
    LOGICAL_OR_EXPR,
    LOGICAL_OR_EXPR_NO_IN,
    LOGICAL_OR_OPERATOR,
    // Assignment expression
    ASSIGNMENT_EXPR,
    ASSIGNMENT_EXPR_NO_IN,
    ASSIGNMENT_OPERATOR,
    // List expression
    LIST_EXPRESSION,
    LIST_EXPRESSION_NO_IN,
    // Non assignment expression
    NON_ASSIGNMENT_EXPR,
    NON_ASSIGNMENT_EXPR_NO_IN,
    // Type expression
    TYPE_EXPR,
    TYPE_EXPR_NO_IN,
    TYPE_APPLICATION,
    VECTOR_LITERAL_EXPRESSION,
    // XML Initialiser
    XML_INITIALISER,
    XML_MARKUP,
    XML_ELEMENT,
    XML_TAG_CONTENT,
    XML_WHITESPACE,
    XML_TAG_NAME,
    XML_ATTRIBUTE,
    XML_ATTRIBUTES,
    XML_ATTRIBUTE_VALUE,
    XML_NAME,
    XML_ELEMENT_CONTENT,
    XML_TEXT,
    XML_COMMENT,
    XML_CDATA,
    XML_PI,
    KEYWORDS,
    REGULAR_EXPRESSION,

    STDIO_FUNCTION_NAME,
    MATH_FUNCTION_NAME,
    // </editor-fold>

    /**
     * DEFINITIONS
     */
    // <editor-fold defaultstate="collapsed" desc="Definitions">
    // Variable
    VARIABLE_DEF,
    VARIABLE_DEF_NO_IN,
    VARIABLE_DEF_KIND,
    VARIABLE_BINDING_LIST,
    VARIABLE_BINDING_LIST_NO_IN,
    VARIABLE_BINDING,
    VARIABLE_BINDING_NO_IN,
    VARIABLE_INITIALISATION,
    VARIABLE_INITIALISATION_NO_IN,
    TYPED_IDENTIFIER,
    TYPED_IDENTIFIER_NO_IN,
    VARIABLE_INITIALISER,
    VARIABLE_INITIALISER_NO_IN,
    // Function
    FUNCTION_DEF,
    FUNCTION_NAME,
    FUNCTION_COMMON,
    FUNCTION_SIGNATURE,
    RESULT_TYPE,
    PARAMETERS,
    PARAMETER,
    REST_PARAMETERS,
    // Class
    CLASS_DEF,
    CLASS_NAME,
    INHERITENCE,
    CLASS_IDENTIFIERS,
    TYPE_EXPRESSION_LIST,
    // Interface
    INTERFACE_DEF,
    EXTENDS_LIST,
    // Package
    PACKAGE_DEF,
    PACKAGE_NAME,
    // Namespace
    NAMESPACE_DEF,
    NAMESPACE_BINDING,
    NAMESPACE_INITIALISATION,
    // Program
    PROGRAM,
    // </editor-fold>

    /**
     * STATEMENTS
     */
    // <editor-fold defaultstate="collapsed" desc="Statements">
    STATEMENT,
    SUPER_STATEMENT,
    SWITCH_STATEMENT,
    IF_STATEMENT,
    DO_STATEMENT,
    WHILE_STATEMENT,
    FOR_STATEMENT,
    WITH_STATEMENT,
    CONTINUE_STATEMENT,
    BREAK_STATEMENT,
    RETURN_STATEMENT,
    THROW_STATEMENT,
    TRY_STATEMENT,
    EXPRESSION_STATEMENT,
    EXPRESSION,
    LABELED_STATEMENT,
    METADATA_STATEMENT,
    DEFAULT_XML_NAMESPACE_DIRECTIVE,
    SUB_STATEMENT,
    EMPTY_STATEMENT,
    VARIABLE_DECLARATION_STATEMENT,
    BLOCK,
    CASE_ELEMENT,
    CASE_LABEL,
    FOR_INITIALISER,
    FOR_IN_BINDING,
    CATCH_CLAUSE,
    CATCH_CLAUSES,
    DECIMAL_DIGITS,
    EXPONENT_PART,
    DECIMAL_INTEGER,
    // </editor-fold>

    /**
     * DIRECTIVES
     */
    // <editor-fold defaultstate="collapsed" desc="Directives">
    DIRECTIVES,
    DIRECTIVE,
    CONFIG_CONDITION,
    ANNOTABLE_DIRECTIVE,
    USE_DIRECTIVE,
    IMPORT_DIRECTIVE,
    INCLUDE_DIRECTIVE,
    ATTRIBUTES,
    ATTRIBUTE,
    ATTRIBUTE_COMBINATION,
    ATTRIBUTE_EXPR;
    // </editor-fold>

    private static final String UNICODE_LETTER = "\\p{Lu}\\p{Ll}\\p{Lt}\\p{Lm}\\p{Lo}\\p{Nl}";
    private static final String UNICODE_DIGIT = "\\p{Nd}";
    private static final String UNICODE_COMBINING_MARK = "\\p{Mn}\\p{Mc}";
    private static final String UNICODE_CONNECTOR_PUNCTUATION = "\\p{Pc}";

    private static final String UNICODE_ESCAPE_SEQUENCE_REGEXP = "u[0-9a-fA-F]{4,4}";
    private static final String IDENTIFIER_START_REGEXP = "(?:[$_" + UNICODE_LETTER + "]|\\\\"
            + UNICODE_ESCAPE_SEQUENCE_REGEXP + ")";
    private static final String IDENTIFIER_PART_REGEXP = "(?:" +
            IDENTIFIER_START_REGEXP + "|[" + UNICODE_COMBINING_MARK + UNICODE_DIGIT + UNICODE_CONNECTOR_PUNCTUATION
            + "])";

    private static final String EXPONENT_PART_REGEXP = "([eE][-+]?[0-9]++)?";
    private static final String FLOAT_SUFFIX_REGEXP = "[fFlL]?";
    private static final String INTEGER_SUFFIX_REGEXP = "(?:[uU](?:ll|LL|l|L)?|(?:ll|LL|l|L)[uU]?)?";
    private static final String DECIMAL_INTEGER_REGEXP = "(0|([1-9][0-9]*+))";
    private static final String DECIMAL_DIGITS_REGEXP = "([0-9]*+)";
    private static final String DECIMAL_REGEXP = DECIMAL_INTEGER_REGEXP + "\\." + DECIMAL_DIGITS_REGEXP + "?"
            + EXPONENT_PART_REGEXP +
            "|\\." + DECIMAL_DIGITS_REGEXP + EXPONENT_PART_REGEXP +
            "|" + DECIMAL_INTEGER_REGEXP + EXPONENT_PART_REGEXP;

    private static final String SINGLE_LINE_COMMENT_REGEXP = "//[^\\n\\r]*+";

    private static final String MULTI_LINE_COMMENT_REGEXP = "/\\*[\\s\\S]*?\\*/";
    private static final String MULTI_LINE_COMMENT_NO_LB_REGEXP = "/\\*[^\\n\\r]*?\\*/";

    /**
     * LF, CR, LS, PS
     */
    private static final String LINE_TERMINATOR_REGEXP = "\\n\\r\\p{Zl}\\p{Zp}";

    /**
     * tab, vertical tab, form feed, space, no-break space, Byte Order Mark, any
     * other Unicode "space character"
     */
    private static final String WHITESPACE_REGEXP = "\\t\\v\\f\\u0020\\u00A0\\uFEFF\\p{Zs}";

    public static final String STRING_REGEXP = "(?:\"([^\"\\\\]*+(\\\\[\\s\\S])?+)*+\"|\'([^\'\\\\]*+(\\\\[\\s\\S])?+)*+\')";

    private static final String NEWLINE_REGEXP = "(?:\\n|\\r\\n|\\r)";

    public static LexerlessGrammar createGrammar() {
        LexerlessGrammarBuilder b = LexerlessGrammarBuilder.create();

        b.rule(WHITESPACE).is(b.regexp("[" + LINE_TERMINATOR_REGEXP + WHITESPACE_REGEXP + "]*+"));

        b.rule(SPACING).is(
                b.skippedTrivia(WHITESPACE),
                b.zeroOrMore(
                        b.commentTrivia(
                                b.regexp("(?:" + SINGLE_LINE_COMMENT_REGEXP + "|" + MULTI_LINE_COMMENT_REGEXP + ")")),
                        b.skippedTrivia(WHITESPACE)))
                .skip();

        b.rule(SPACING_NO_LB).is(b.zeroOrMore(b.firstOf(
                b.skippedTrivia(b.regexp("[\\s&&[^\n\r]]++")),
                b.commentTrivia(
                        b.regexp("(?:" + SINGLE_LINE_COMMENT_REGEXP + "|" + MULTI_LINE_COMMENT_NO_LB_REGEXP + ")")))))
                .skip();
        b.rule(NEXT_NOT_LB).is(b.nextNot(b.regexp("(?:" + "[\n\r]" + "|" + MULTI_LINE_COMMENT_REGEXP + ")"))).skip();

        b.rule(EOS).is(b.firstOf(
                b.sequence(SPACING, ";"),
                b.sequence(SPACING_NO_LB, b.regexp(NEWLINE_REGEXP)),
                b.sequence(SPACING_NO_LB, b.next("}")),
                b.sequence(SPACING, b.endOfInput())));
        b.rule(EOS_NO_LB).is(b.firstOf(
                b.sequence(SPACING_NO_LB, ";"),
                b.sequence(SPACING_NO_LB, b.regexp(NEWLINE_REGEXP)),
                b.sequence(SPACING_NO_LB, b.next("}")),
                b.sequence(SPACING_NO_LB, b.endOfInput())));

        punctuators(b);
        keywords(b);
        literals(b);
        expressions(b);
        statements(b);
        directives(b);
        definitions(b);
        xml(b);

        b.setRootRule(PROGRAM);

        return b.build();
    }

    private static void literals(LexerlessGrammarBuilder b) {
        b.rule(STRING).is(SPACING, b.regexp(STRING_REGEXP));

        b.rule(I_CONSTANT).is(SPACING, b.regexp(
                "(?:0[xX][0-9a-fA-F]++|0[0-7]++|" + DECIMAL_INTEGER_REGEXP + ")" + INTEGER_SUFFIX_REGEXP));
        b.rule(F_CONSTANT).is(SPACING, b.regexp(
                "(?:" + DECIMAL_INTEGER_REGEXP + "\\." + DECIMAL_DIGITS_REGEXP + "|\\." + DECIMAL_DIGITS_REGEXP
                        + "|" + DECIMAL_INTEGER_REGEXP + "[eE][-+]?[0-9]++)" + FLOAT_SUFFIX_REGEXP));
        b.rule(ENUMERATION_CONSTANT).is(IDENTIFIER);

        b.rule(HEXADECIMAL).is(SPACING, b.regexp("0[xX][0-9a-fA-F]++"));
        b.rule(OCTAL).is(SPACING, b.regexp("0[0-7]++"));
        b.rule(DECIMAL).is(SPACING, b.regexp(DECIMAL_REGEXP));
        b.rule(NUMBER).is(b.firstOf(OCTAL, DECIMAL, HEXADECIMAL));

        b.rule(CONSTANT).is(b.firstOf(
                I_CONSTANT,
                F_CONSTANT,
                ENUMERATION_CONSTANT));

        // Regular expression according to ECMA 262
        b.rule(REGULAR_EXPRESSION).is(SPACING, b.regexp(
                "/"
                        // Regular expression first char
                        + "([^\\n\\r\\*\\\\/]|(\\\\[^\\n\\r]))"
                        // Regular expression chars
                        + "([^\\n\\r\\\\/]|(\\\\[^\\n\\r]))*"
                        + "/"
                        // Regular expression flags
                        + IDENTIFIER_PART_REGEXP + "*+"));
    }

    private static void expressions(LexerlessGrammarBuilder b) {
        //  TODO - change identifiers for C, refer - https://www.quut.com/c/ANSI-C-grammar-l-2011.html#IDENTIFIER
        // Identifiers
        b.rule(IDENTIFIER).is(b.firstOf(
                DYNAMIC,
                EACH,
                GET,
                INCLUDE,
                NAMESPACE,
                SET,
                STATIC,
                b.sequence(SPACING, b.nextNot(KEYWORDS),
                        b.regexp(IDENTIFIER_START_REGEXP + IDENTIFIER_PART_REGEXP + "*+"))));
        b.rule(IDENTIFIER_PART).is(b.regexp(IDENTIFIER_PART_REGEXP));

        b.rule(PROPERTY_IDENTIFIER).is(b.firstOf(
                IDENTIFIER,
                STAR));

        b.rule(QUALIFIER).is(b.firstOf(
                PROPERTY_IDENTIFIER,
                RESERVED_NAMESPACE));

        b.rule(SIMPLE_QUALIFIED_IDENTIFIER).is(b.firstOf(
                b.sequence(QUALIFIER, DOUBLE_COLON, PROPERTY_IDENTIFIER),
                b.sequence(QUALIFIER, DOUBLE_COLON, BRACKETS),
                PROPERTY_IDENTIFIER));

        b.rule(GENERIC_SELECTION).is(
                word(b, "_Generic"),
                LPARENTHESIS,
                ASSIGNMENT_EXPR,
                COMMA,
                GENERIC_ASSOC_LIST,
                RPARENTHESIS);
        b.rule(GENERIC_ASSOC_LIST).is(GENERIC_ASSOCIATION, b.zeroOrMore(COMMA, GENERIC_ASSOCIATION));
        b.rule(GENERIC_ASSOCIATION).is(b.firstOf(
                b.sequence(TYPE_NAME, COLON, ASSIGNMENT_EXPR),
                b.sequence(DEFAULT, COLON, ASSIGNMENT_EXPR)));

        b.rule(EXPR_QUALIFIED_IDENTIFIER).is(b.firstOf(
                b.sequence(PARENTHESIZED_EXPR, DOUBLE_COLON, PROPERTY_IDENTIFIER),
                b.sequence(PARENTHESIZED_EXPR, BRACKETS)));

        b.rule(NON_ATTRIBUTE_QUALIFIED_IDENTIFIER).is(b.firstOf(
                SIMPLE_QUALIFIED_IDENTIFIER,
                EXPR_QUALIFIED_IDENTIFIER));

        b.rule(QUALIFIED_IDENTIFIER).is(b.firstOf(
                b.sequence(AT_SIGN, BRACKETS),
                b.sequence(AT_SIGN, NON_ATTRIBUTE_QUALIFIED_IDENTIFIER),
                NON_ATTRIBUTE_QUALIFIED_IDENTIFIER));

        b.rule(PRIMARY_EXPR).is(b.firstOf(
                NULL,
                TRUE,
                FALSE,
                GENERIC_SELECTION,
                HEXADECIMAL,
                NUMBER,
                STRING,
                THIS,
                REGULAR_EXPRESSION,
                XML_INITIALISER,
                STDIO_FUNCTION_NAME,
                MATH_FUNCTION_NAME,
                QUALIFIED_IDENTIFIER,
                RESERVED_NAMESPACE,
                PARENTHESIZED_EXPR,
                ARRAY_INITIALISER,
                OBJECT_INITIALISER,
                FUNCTION_EXPR));

        b.rule(RESERVED_NAMESPACE).is(b.firstOf(PUBLIC, PRIVATE, PROTECTED, INTERNAL));

        b.rule(PARENTHESIZED_EXPR).is(LPARENTHESIS, ASSIGNMENT_EXPR, RPARENTHESIS);
        b.rule(PARENTHESIZED_LIST_EXPR).is(LPARENTHESIS, LIST_EXPRESSION, RPARENTHESIS);

        b.rule(FUNCTION_EXPR).is(b.firstOf(
                b.sequence(FUNCTION, FUNCTION_COMMON),
                b.sequence(FUNCTION, IDENTIFIER, FUNCTION_COMMON)));

        b.rule(OBJECT_INITIALISER).is(LCURLYBRACE, b.optional(LITERAL_FIELD, b.zeroOrMore(COMMA, LITERAL_FIELD)),
                RCURLYBRACE);
        b.rule(LITERAL_FIELD).is(FIELD_NAME, COLON, ASSIGNMENT_EXPR);
        b.rule(FIELD_NAME).is(b.firstOf(
                NON_ATTRIBUTE_QUALIFIED_IDENTIFIER,
                STRING,
                NUMBER));

        // Array initialiser
        b.rule(ARRAY_INITIALISER).is(LBRAKET, b.optional(ELEMENT_LIST), RBRAKET);
        b.rule(ELEMENT_LIST).is(b.optional(COMMA), LITERAL_ELEMENT, b.zeroOrMore(COMMA, LITERAL_ELEMENT),
                b.optional(COMMA));
        b.rule(LITERAL_ELEMENT).is(ASSIGNMENT_EXPR);

        // Assignement expressions
        b.rule(ASSIGNMENT_EXPR).is(b.firstOf(
                b.sequence(UNARY_EXPR, ASSIGNMENT_OPERATOR, ASSIGNMENT_EXPR),
                CONDITIONAL_EXPR));
        b.rule(ASSIGNMENT_EXPR_NO_IN).is(b.firstOf(
                b.sequence(POSTFIX_EXPR, ASSIGNMENT_OPERATOR, ASSIGNMENT_EXPR_NO_IN),
                CONDITIONAL_EXPR));
        b.rule(ASSIGNMENT_OPERATOR).is(
                b.firstOf(
                        EQUAL1,
                        STAR_EQU,
                        DIV_EQU,
                        MOD_EQU,
                        PLUS_EQU,
                        MINUS_EQU,
                        SL_EQU,
                        SR_EQU,
                        AND_EQU,
                        XOR_EQU,
                        OR_EQU
                ));
        b.rule(COMPOUND_ASSIGNMENT).is(b.firstOf(STAR_EQU, DIV_EQU, MOD_EQU, PLUS_EQU, MINUS_EQU, SL_EQU, SR_EQU,
                SR_EQU2, AND_EQU, XOR_EQU, OR_EQU));
        b.rule(LOGICAL_ASSIGNMENT).is(b.firstOf(ANDAND_EQU, XORXOR_EQU, OROR_EQU));

        // Super expression
        b.rule(SUPER_EXPR).is(b.firstOf(
                b.sequence(SUPER, ARGUMENTS),
                SUPER));

        b.rule(POSTFIX_EXPR).is(
                b.firstOf(
                        b.sequence(LPARENTHESIS, TYPE_NAME, RPARENTHESIS, LCURLYBRACE, INITIALIZER_LIST, COMMA, RCURLYBRACE),
                        b.sequence(LPARENTHESIS, TYPE_NAME, RPARENTHESIS, LCURLYBRACE, INITIALIZER_LIST, RCURLYBRACE),
                        PRIMARY_EXPR),b.zeroOrMore(b.firstOf(
                                b.sequence(LBRAKET, LIST_EXPRESSION, RBRAKET),
                                ARGUMENTS,
                                b.sequence(DOT, IDENTIFIER),
                                b.sequence(SPACING_NO_LB, NEXT_NOT_LB, DOUBLE_PLUS),
                                b.sequence(SPACING_NO_LB, NEXT_NOT_LB, DOUBLE_MINUS))));

        // New expressions
        b.rule(FULL_NEW_EXPR).is(NEW, b.firstOf(FULL_NEW_SUB_EXPR, VECTOR_LITERAL_EXPRESSION), ARGUMENTS);
        b.rule(FULL_NEW_SUB_EXPR).is(b.firstOf(
                PRIMARY_EXPR,
                b.sequence(FULL_NEW_EXPR, PROPERTY_OPERATOR),
                FULL_NEW_EXPR,
                b.sequence(SUPER_EXPR, PROPERTY_OPERATOR)));

        b.rule(SHORT_NEW_EXPR).is(NEW, b.firstOf(SHORT_NEW_SUB_EXPR, VECTOR_LITERAL_EXPRESSION));
        b.rule(SHORT_NEW_SUB_EXPR).is(b.firstOf(
                FULL_NEW_SUB_EXPR,
                SHORT_NEW_EXPR));

        // Property accessors
        b.rule(PROPERTY_OPERATOR).is(b.firstOf(
                b.sequence(DOT, QUALIFIED_IDENTIFIER),
                // not in specs:
                TYPE_APPLICATION,
                BRACKETS));
        b.rule(BRACKETS).is(LBRAKET, LIST_EXPRESSION, RBRAKET);

        // Query operators
        b.rule(QUERY_OPERATOR).is(b.firstOf(
                b.sequence(DOUBLE_DOT, QUALIFIED_IDENTIFIER),
                b.sequence(DOT, LPARENTHESIS, LIST_EXPRESSION, RPARENTHESIS)));

        // Call expresions
        b.rule(ARGUMENTS).is(LPARENTHESIS, b.optional(LIST_EXPRESSION), RPARENTHESIS);
        b.rule(ARGUMENT_EXPRESSION_LIST).is(ASSIGNMENT_EXPR, b.zeroOrMore(COMMA, ASSIGNMENT_EXPR));

        // Unary expression
        // b.rule(UNARY_EXPR).is(b.firstOf(
        //         b.sequence(PLUS, UNARY_EXPR),
        //         b.sequence(MINUS, UNARY_EXPR),
        //         b.sequence(UNARY_OPERATOR, CAST_EXPRESSION),
        //         b.sequence(SIZEOF, UNARY_EXPR),
        //         b.sequence(SIZEOF, LPARENTHESIS, TYPE_NAME, RPARENTHESIS),
        //         POSTFIX_EXPR)).skipIfOneChild();
        b.rule(UNARY_EXPR).is(b.firstOf(
                b.sequence(DOUBLE_PLUS, UNARY_EXPR),
                b.sequence(DOUBLE_MINUS, UNARY_EXPR),
                b.sequence(UNARY_OPERATOR, CAST_EXPRESSION),
                b.sequence(SIZEOF, UNARY_EXPR),
                b.sequence(SIZEOF, LPARENTHESIS, TYPE_NAME, RPARENTHESIS),
                b.sequence(word(b, "_Alignof"), LPARENTHESIS, TYPE_NAME, RPARENTHESIS),
                POSTFIX_EXPR)).skipIfOneChild();

        b.rule(UNARY_OPERATOR).is(
                b.firstOf(
                        AND,
                        STAR,
                        PLUS,
                        MINUS,
                        TILD,
                        NOT
                )
        );

        b.rule(TYPE_NAME).is(SPECIFIER_QUALIFIER_LIST, b.optional(ABSTRACT_DECLARATOR));

        b.rule(SPECIFIER_QUALIFIER_LIST).is(b.oneOrMore(b.firstOf(TYPE_SPECIFIER, TYPE_QUALIFIER)));

        b.rule(ABSTRACT_DECLARATOR).is(b.firstOf(b.sequence(POINTER, b.optional(DIRECT_ABSTRACT_DECLARATOR)),DIRECT_ABSTRACT_DECLARATOR));

        b.rule(DIRECT_ABSTRACT_DECLARATOR).is(
        b.firstOf(
                b.sequence(LPARENTHESIS, ABSTRACT_DECLARATOR, RPARENTHESIS),
                b.firstOf(ARRAY_ABSTRACT_SUFFIX, FUNCTION_ABSTRACT_SUFFIX)),
        b.zeroOrMore(b.firstOf(ARRAY_ABSTRACT_SUFFIX, FUNCTION_ABSTRACT_SUFFIX)));

        b.rule(ARRAY_ABSTRACT_SUFFIX).is(LBRAKET, b.optional(b.firstOf(STAR,
        b.sequence(
            b.optional(STATIC), 
            b.optional(TYPE_QUALIFIER_LIST), 
            b.optional(ASSIGNMENT_EXPR)
        ),
        b.sequence(
            TYPE_QUALIFIER_LIST, 
            STATIC, 
            ASSIGNMENT_EXPR
        ))), RBRAKET);

        b.rule(FUNCTION_ABSTRACT_SUFFIX).is(LPARENTHESIS, b.optional(PARAMETER_TYPE_LIST), RPARENTHESIS);

        b.rule(PARAMETER_TYPE_LIST).is(PARAMETER_LIST, b.optional(b.sequence(COMMA, TRIPLE_DOTS)));

        b.rule(PARAMETER_LIST).is(PARAMETER_DECLARATION, b.zeroOrMore(b.sequence(COMMA, PARAMETER_DECLARATION)));

        b.rule(PARAMETER_DECLARATION).is(DECLARATION_SPECIFIERS, b.optional(b.firstOf(
            DECLARATOR,
            ABSTRACT_DECLARATOR)));

        b.rule(CAST_EXPRESSION).is(b.firstOf(b.sequence(LPARENTHESIS, TYPE_NAME, RPARENTHESIS, CAST_EXPRESSION), UNARY_EXPR));

        b.rule(CONSTANT_EXPRESSION).is(CONDITIONAL_EXPR);

        // Binary expressions
        b.rule(MULTIPLICATIVE_EXPR).is(CAST_EXPRESSION, b.zeroOrMore(b.firstOf(
                b.sequence(STAR, CAST_EXPRESSION),
                b.sequence(DIV, CAST_EXPRESSION),
                b.sequence(MOD, CAST_EXPRESSION)))).skipIfOneChild();

        b.rule(ADDITIVE_EXPR).is(MULTIPLICATIVE_EXPR, b.zeroOrMore(b.firstOf(
                b.sequence(PLUS, MULTIPLICATIVE_EXPR),
                b.sequence(MINUS, MULTIPLICATIVE_EXPR)))).skipIfOneChild();

        b.rule(ADDITIVE_OPERATOR).is(b.firstOf(PLUS, MINUS, /* Action Script 2: */ word(b, "add")));
        b.rule(SHIFT_EXPR).is(ADDITIVE_EXPR, b.zeroOrMore(b.firstOf(
                b.sequence(SL, ADDITIVE_EXPR),
                b.sequence(SR, ADDITIVE_EXPR),
                b.sequence(SR2, ADDITIVE_EXPR)))).skipIfOneChild();

        b.rule(RELATIONAL_EXPR).is(SHIFT_EXPR, b.zeroOrMore(b.firstOf(
                b.sequence(LT, SHIFT_EXPR),
                b.sequence(GT, SHIFT_EXPR),
                b.sequence(LE, SHIFT_EXPR),
                b.sequence(GE, SHIFT_EXPR)))).skipIfOneChild();

        b.rule(RELATIONAL_EXPR_NO_IN).is(SHIFT_EXPR, b.zeroOrMore(RELATIONAL_OPERATOR_NO_IN, SHIFT_EXPR))
                .skipIfOneChild();
        b.rule(RELATIONAL_OPERATOR).is(b.firstOf(LE, GE, LT, GT, IN, INSTANCEOF, IS, AS,
                /* Action Script 2: */ word(b, "le"), word(b, "ge"), word(b, "lt"), word(b, "gt")));
        b.rule(RELATIONAL_OPERATOR_NO_IN).is(b.firstOf(LE, GE, LT, GT, INSTANCEOF, IS, AS,
                /* Action Script 2: */ word(b, "le"), word(b, "ge"), word(b, "lt"), word(b, "gt")));

        b.rule(EQUALITY_EXPR).is(RELATIONAL_EXPR, b.zeroOrMore(b.firstOf(
                b.sequence(EQUAL2, RELATIONAL_EXPR),
                b.sequence(NOTEQUAL1, RELATIONAL_EXPR)))).skipIfOneChild();
        
        b.rule(EQUALITY_EXPR_NO_IN).is(RELATIONAL_EXPR_NO_IN, b.zeroOrMore(EQUALITY_OPERATOR, RELATIONAL_EXPR_NO_IN))
                .skipIfOneChild();
        b.rule(EQUALITY_OPERATOR).is(b.firstOf(
                NOTEQUAL2,
                EQUAL3,
                EQUAL2,
                NOTEQUAL1,
                /* ActionScript 2: */
                b.sequence(SPACING, "<>"),
                word(b, "eq"),
                word(b, "ne")));

        b.rule(BITEWISE_AND_EXPR).is(EQUALITY_EXPR, b.zeroOrMore(AND, EQUALITY_EXPR)).skipIfOneChild();
        b.rule(BITEWISE_AND_EXPR_NO_IN).is(EQUALITY_EXPR_NO_IN, b.zeroOrMore(AND, EQUALITY_EXPR_NO_IN))
                .skipIfOneChild();

        b.rule(BITEWISE_XOR_EXPR).is(BITEWISE_AND_EXPR, b.zeroOrMore(XOR, BITEWISE_AND_EXPR)).skipIfOneChild();
        b.rule(BITEWISE_XOR_EXPR_NO_IN).is(BITEWISE_AND_EXPR_NO_IN, b.zeroOrMore(XOR, BITEWISE_AND_EXPR_NO_IN))
                .skipIfOneChild();

        b.rule(BITEWISE_OR_EXPR).is(BITEWISE_XOR_EXPR, b.zeroOrMore(OR, BITEWISE_XOR_EXPR)).skipIfOneChild();
        b.rule(BITEWISE_OR_EXPR_NO_IN).is(BITEWISE_XOR_EXPR_NO_IN, b.zeroOrMore(OR, BITEWISE_XOR_EXPR_NO_IN))
                .skipIfOneChild();

        b.rule(LOGICAL_AND_EXPR).is(BITEWISE_OR_EXPR, b.zeroOrMore(LOGICAL_AND_OPERATOR, BITEWISE_OR_EXPR)).skipIfOneChild();
       
        b.rule(LOGICAL_AND_EXPR_NO_IN)
                .is(BITEWISE_OR_EXPR_NO_IN, b.zeroOrMore(LOGICAL_AND_OPERATOR, BITEWISE_XOR_EXPR_NO_IN))
                .skipIfOneChild();
        
        b.rule(LOGICAL_AND_OPERATOR).is(b.firstOf(
                ANDAND,
                /* ActionScript 2: */
                b.sequence(SPACING, "and", b.nextNot(IDENTIFIER_PART))));

        b.rule(LOGICAL_OR_EXPR).is(LOGICAL_AND_EXPR, b.zeroOrMore(LOGICAL_OR_OPERATOR, LOGICAL_AND_EXPR))
                .skipIfOneChild();
        b.rule(LOGICAL_OR_EXPR_NO_IN)
                .is(LOGICAL_AND_EXPR_NO_IN, b.zeroOrMore(LOGICAL_OR_OPERATOR, LOGICAL_AND_EXPR_NO_IN)).skipIfOneChild();
        b.rule(LOGICAL_OR_OPERATOR).is(b.firstOf(
                OROR,
                /* ActionScript 2: */
                b.sequence(SPACING, "or", b.nextNot(IDENTIFIER_PART))));

        // Conditional expression
        b.rule(CONDITIONAL_EXPR).is(LOGICAL_OR_EXPR, b.optional(QUERY, EXPRESSION, COLON, CONDITIONAL_EXPR))
                .skipIfOneChild();
        b.rule(CONDITIONAL_EXPR_NO_IN)
                .is(LOGICAL_OR_EXPR_NO_IN, b.optional(QUERY, ASSIGNMENT_EXPR_NO_IN, COLON, ASSIGNMENT_EXPR_NO_IN))
                .skipIfOneChild();

        // Non assignment expression
        b.rule(NON_ASSIGNMENT_EXPR)
                .is(LOGICAL_OR_EXPR, b.optional(QUERY, NON_ASSIGNMENT_EXPR, COLON, NON_ASSIGNMENT_EXPR))
                .skipIfOneChild();
        b.rule(NON_ASSIGNMENT_EXPR_NO_IN)
                .is(LOGICAL_OR_EXPR_NO_IN,
                        b.optional(QUERY, NON_ASSIGNMENT_EXPR_NO_IN, COLON, NON_ASSIGNMENT_EXPR_NO_IN))
                .skipIfOneChild();

        b.rule(LIST_EXPRESSION).is(ASSIGNMENT_EXPR, b.zeroOrMore(b.sequence(COMMA, ASSIGNMENT_EXPR)));
        b.rule(LIST_EXPRESSION_NO_IN).is(ASSIGNMENT_EXPR_NO_IN, b.zeroOrMore(b.sequence(COMMA, ASSIGNMENT_EXPR_NO_IN)));

        b.rule(TYPE_EXPR).is(b.firstOf(
                STAR,
                b.sequence(/* Godin: not sure about QUALIFIED_IDENTIFIER, but it works: */QUALIFIED_IDENTIFIER,
                        b.zeroOrMore(DOT, QUALIFIED_IDENTIFIER), b.optional(TYPE_APPLICATION))));
        b.rule(TYPE_APPLICATION).is(DOT, LT, TYPE_EXPRESSION_LIST, GT);
        b.rule(TYPE_EXPR_NO_IN).is(TYPE_EXPR);

        b.rule(VECTOR_LITERAL_EXPRESSION).is(LT, TYPE_EXPR, GT, BRACKETS);
        b.rule(STDIO_FUNCTION_NAME).is(SPACING, b.firstOf(
                b.sequence("fopen",  b.nextNot(IDENTIFIER_PART)),
                b.sequence("fread",  b.nextNot(IDENTIFIER_PART)),
                b.sequence("fwrite", b.nextNot(IDENTIFIER_PART)),
                b.sequence("fclose", b.nextNot(IDENTIFIER_PART)),
                b.sequence("printf", b.nextNot(IDENTIFIER_PART)),
                b.sequence("scanf",  b.nextNot(IDENTIFIER_PART))
        ));

        // -------------------------------------------------------------------------
        // math.h   predefined function names  (full C99 / POSIX set)
        // -------------------------------------------------------------------------
        b.rule(MATH_FUNCTION_NAME).is(SPACING, b.firstOf(
        // Trigonometric
                b.sequence("acos",       b.nextNot(IDENTIFIER_PART)),
                b.sequence("acosh",      b.nextNot(IDENTIFIER_PART)),
                b.sequence("asin",       b.nextNot(IDENTIFIER_PART)),
                b.sequence("asinh",      b.nextNot(IDENTIFIER_PART)),
                b.sequence("atan2",      b.nextNot(IDENTIFIER_PART)),
                b.sequence("atan",       b.nextNot(IDENTIFIER_PART)),
                b.sequence("atanh",      b.nextNot(IDENTIFIER_PART)),
                b.sequence("cos",        b.nextNot(IDENTIFIER_PART)),
                b.sequence("cosh",       b.nextNot(IDENTIFIER_PART)),
                b.sequence("sin",        b.nextNot(IDENTIFIER_PART)),
                b.sequence("sinh",       b.nextNot(IDENTIFIER_PART)),
                b.sequence("tan",        b.nextNot(IDENTIFIER_PART)),
                b.sequence("tanh",       b.nextNot(IDENTIFIER_PART)),
                // Exponential & logarithmic
                b.sequence("exp2",       b.nextNot(IDENTIFIER_PART)),
                b.sequence("exp",        b.nextNot(IDENTIFIER_PART)),
                b.sequence("expm1",      b.nextNot(IDENTIFIER_PART)),
                b.sequence("frexp",      b.nextNot(IDENTIFIER_PART)),
                b.sequence("ilogb",      b.nextNot(IDENTIFIER_PART)),
                b.sequence("ldexp",      b.nextNot(IDENTIFIER_PART)),
                b.sequence("lgamma",     b.nextNot(IDENTIFIER_PART)),
                b.sequence("log10",      b.nextNot(IDENTIFIER_PART)),
                b.sequence("log1p",      b.nextNot(IDENTIFIER_PART)),
                b.sequence("log2",       b.nextNot(IDENTIFIER_PART)),
                b.sequence("logb",       b.nextNot(IDENTIFIER_PART)),
                b.sequence("log",        b.nextNot(IDENTIFIER_PART)),
                b.sequence("modf",       b.nextNot(IDENTIFIER_PART)),
                b.sequence("scalbn",     b.nextNot(IDENTIFIER_PART)),
                b.sequence("scalbln",    b.nextNot(IDENTIFIER_PART)),
                b.sequence("tgamma",     b.nextNot(IDENTIFIER_PART)),
                // Power & absolute value
                b.sequence("cbrt",       b.nextNot(IDENTIFIER_PART)),
                b.sequence("fabs",       b.nextNot(IDENTIFIER_PART)),
                b.sequence("hypot",      b.nextNot(IDENTIFIER_PART)),
                b.sequence("pow",        b.nextNot(IDENTIFIER_PART)),
                b.sequence("sqrt",       b.nextNot(IDENTIFIER_PART)),
                // Integer variants (keep longer alternatives first to avoid partial match)
                b.sequence("llrint",     b.nextNot(IDENTIFIER_PART)),
                b.sequence("llround",    b.nextNot(IDENTIFIER_PART)),
                b.sequence("lrint",      b.nextNot(IDENTIFIER_PART)),
                b.sequence("lround",     b.nextNot(IDENTIFIER_PART)),
                // Rounding
                b.sequence("ceil",       b.nextNot(IDENTIFIER_PART)),
                b.sequence("floor",      b.nextNot(IDENTIFIER_PART)),
                b.sequence("nearbyint",  b.nextNot(IDENTIFIER_PART)),
                b.sequence("nextafter",  b.nextNot(IDENTIFIER_PART)),
                b.sequence("nexttoward", b.nextNot(IDENTIFIER_PART)),
                b.sequence("rint",       b.nextNot(IDENTIFIER_PART)),
                b.sequence("round",      b.nextNot(IDENTIFIER_PART)),
                b.sequence("trunc",      b.nextNot(IDENTIFIER_PART)),
                // Floating-point manipulation
                b.sequence("copysign",   b.nextNot(IDENTIFIER_PART)),
                b.sequence("erf",        b.nextNot(IDENTIFIER_PART)),
                b.sequence("erfc",       b.nextNot(IDENTIFIER_PART)),
                b.sequence("fdim",       b.nextNot(IDENTIFIER_PART)),
                b.sequence("fma",        b.nextNot(IDENTIFIER_PART)),
                b.sequence("fmax",       b.nextNot(IDENTIFIER_PART)),
                b.sequence("fmin",       b.nextNot(IDENTIFIER_PART)),
                b.sequence("fmod",       b.nextNot(IDENTIFIER_PART)),
                b.sequence("nan",        b.nextNot(IDENTIFIER_PART)),
                b.sequence("remainder",  b.nextNot(IDENTIFIER_PART)),
                b.sequence("remquo",     b.nextNot(IDENTIFIER_PART)),
                // abs is last: short name, must not steal 'acos', 'asin' etc. (those listed above first)
                b.sequence("abs",        b.nextNot(IDENTIFIER_PART))
        ));
    }

    private static void statements(LexerlessGrammarBuilder b) {
        // new rules added for C - 
        b.rule(COMPOUND_STATEMENT).is(
                LCURLYBRACE,
                b.optional(BLOCK_ITEM_LIST),
                RCURLYBRACE
        );

        b.rule(BLOCK_ITEM_LIST).is(b.oneOrMore(BLOCK_ITEM));

        b.rule(BLOCK_ITEM).is(b.firstOf(DECLARATION, STATEMENT));

        // existing rules in flex - 
        b.rule(STATEMENT).is(b.firstOf(
                LABELED_STATEMENT,
                COMPOUND_STATEMENT,
                EXPRESSION_STATEMENT,
                CONTROL_STATEMENT,
                ITERATION_STATEMENT,
                JUMP_STATEMENT
        ));

        b.rule(CONTROL_STATEMENT).is(b.firstOf(
                b.sequence(IF, LPARENTHESIS, EXPRESSION, RPARENTHESIS, STATEMENT, b.optional(b.sequence(ELSE, STATEMENT))),
                b.sequence(SWITCH, LPARENTHESIS, EXPRESSION, RPARENTHESIS, STATEMENT)));

        b.rule(ITERATION_STATEMENT).is(
                b.firstOf(
                        b.sequence(WHILE, LPARENTHESIS, EXPRESSION, RPARENTHESIS, STATEMENT),
                        b.sequence(DO, STATEMENT, WHILE, LPARENTHESIS, EXPRESSION, RPARENTHESIS, SEMICOLON),
                        b.sequence(FOR, LPARENTHESIS, FOR_INIT, EXPRESSION_STATEMENT, b.optional(EXPRESSION),RPARENTHESIS, STATEMENT)));

        b.rule(FOR_INIT).is(b.firstOf(DECLARATION, EXPRESSION_STATEMENT));

        b.rule(JUMP_STATEMENT).is(b.firstOf(
                b.sequence(GOTO, IDENTIFIER, SEMICOLON),
                b.sequence(CONTINUE, SEMICOLON),
                b.sequence(BREAK, SEMICOLON),
                b.sequence(RETURN, b.optional(EXPRESSION), SEMICOLON))).skipIfOneChild();

        b.rule(SUB_STATEMENT).is(b.firstOf(
                EMPTY_STATEMENT,
                STATEMENT,
                VARIABLE_DECLARATION_STATEMENT));

        b.rule(EXPRESSION_STATEMENT).is(b.firstOf(SEMICOLON, b.sequence(EXPRESSION, SEMICOLON)));

        // Not in spec:
        b.rule(METADATA_STATEMENT).is(LBRAKET, ASSIGNMENT_EXPR, RBRAKET);

        b.rule(VARIABLE_DECLARATION_STATEMENT).is(VARIABLE_DEF, EOS);

        b.rule(EMPTY_STATEMENT).is(SEMICOLON);

        b.rule(SUPER_STATEMENT).is(SUPER, ARGUMENTS, EOS);

        b.rule(BLOCK).is(LCURLYBRACE, DIRECTIVES, RCURLYBRACE);

        b.rule(LABELED_STATEMENT).is(b.firstOf(
        b.sequence(IDENTIFIER, COLON, STATEMENT),
        b.sequence(CASE, CONSTANT_EXPRESSION, COLON, STATEMENT),
        b.sequence(DEFAULT, COLON, STATEMENT)));
        
        // the following if, switch, do, while and for statements are replaced by control and iteration statements
        b.rule(IF_STATEMENT).is(IF, PARENTHESIZED_LIST_EXPR, SUB_STATEMENT, b.optional(ELSE, SUB_STATEMENT));

        b.rule(SWITCH_STATEMENT).is(SWITCH, PARENTHESIZED_LIST_EXPR, LCURLYBRACE, b.zeroOrMore(CASE_ELEMENT),
                RCURLYBRACE);
        b.rule(CASE_ELEMENT).is(b.oneOrMore(CASE_LABEL), b.zeroOrMore(DIRECTIVE));
        b.rule(CASE_LABEL).is(b.firstOf(DEFAULT, b.sequence(CASE, LIST_EXPRESSION)), COLON);

        b.rule(DO_STATEMENT).is(DO, SUB_STATEMENT, WHILE, PARENTHESIZED_LIST_EXPR, EOS);

        b.rule(WHILE_STATEMENT).is(WHILE, PARENTHESIZED_LIST_EXPR, SUB_STATEMENT);

        b.rule(FOR_STATEMENT).is(FOR, LPARENTHESIS, b.optional(FOR_INITIALISER), SEMICOLON, b.optional(LIST_EXPRESSION), SEMICOLON, b.optional(LIST_EXPRESSION), RPARENTHESIS, SUB_STATEMENT);
        b.rule(FOR_INITIALISER).is(b.firstOf(LIST_EXPRESSION, VARIABLE_DEF_NO_IN));
        b.rule(FOR_IN_BINDING).is(b.firstOf(
                b.sequence(VARIABLE_DEF_KIND, VARIABLE_BINDING_NO_IN),
                POSTFIX_EXPR));

        b.rule(CONTINUE_STATEMENT).is(CONTINUE, b.firstOf(
                b.sequence(/* No line break */ SPACING_NO_LB, NEXT_NOT_LB, IDENTIFIER, EOS),
                EOS_NO_LB));

        b.rule(BREAK_STATEMENT).is(BREAK, b.firstOf(
                b.sequence(/* No line break */ SPACING_NO_LB, NEXT_NOT_LB, IDENTIFIER, EOS),
                EOS_NO_LB));

        b.rule(WITH_STATEMENT).is(WITH, PARENTHESIZED_LIST_EXPR, SUB_STATEMENT);

        b.rule(RETURN_STATEMENT).is(RETURN, b.firstOf(
                b.sequence(/* No line break */ SPACING_NO_LB, NEXT_NOT_LB, LIST_EXPRESSION, EOS),
                EOS_NO_LB));

        b.rule(THROW_STATEMENT).is(THROW, b.firstOf(
                b.sequence(/* No line break */ SPACING_NO_LB, NEXT_NOT_LB, LIST_EXPRESSION, EOS),
                EOS_NO_LB));

        b.rule(TRY_STATEMENT).is(TRY, BLOCK, b.firstOf(
                b.sequence(CATCH_CLAUSES, b.optional(FINALLY, BLOCK)),
                b.sequence(FINALLY, BLOCK)));
        b.rule(CATCH_CLAUSES).is(CATCH_CLAUSE, b.zeroOrMore(CATCH_CLAUSE));
        b.rule(CATCH_CLAUSE).is(CATCH, LPARENTHESIS, PARAMETER, RPARENTHESIS, BLOCK);

        b.rule(EXPRESSION).is(ASSIGNMENT_EXPR, b.zeroOrMore(COMMA, ASSIGNMENT_EXPR));
    }

    private static void directives(LexerlessGrammarBuilder b) {
        b.rule(DIRECTIVE).is(b.firstOf(
                CONFIG_CONDITION,
                EMPTY_STATEMENT,
                ANNOTABLE_DIRECTIVE,
                STATEMENT,
                DEFAULT_XML_NAMESPACE_DIRECTIVE,
                b.sequence(ATTRIBUTES, /* No line break */ SPACING_NO_LB, NEXT_NOT_LB, ANNOTABLE_DIRECTIVE),
                b.sequence(INCLUDE_DIRECTIVE, /* No line break */ EOS_NO_LB),
                b.sequence(IMPORT_DIRECTIVE, /* No line break */ EOS_NO_LB),
                b.sequence(USE_DIRECTIVE, /* No line break */ EOS_NO_LB)));

        b.rule(CONFIG_CONDITION).is(IDENTIFIER, DOUBLE_COLON, IDENTIFIER, LCURLYBRACE, DIRECTIVES, RCURLYBRACE);

        b.rule(ANNOTABLE_DIRECTIVE).is(b.firstOf(
                VARIABLE_DECLARATION_STATEMENT,
                FUNCTION_DEF,
                CLASS_DEF,
                INTERFACE_DEF,
                NAMESPACE_DEF));

        b.rule(DIRECTIVES).is(b.zeroOrMore(DIRECTIVE));

        b.rule(ATTRIBUTES).is(b.oneOrMore(ATTRIBUTE));
        b.rule(ATTRIBUTE_COMBINATION).is(ATTRIBUTE, /* No line break */ SPACING_NO_LB, NEXT_NOT_LB, ATTRIBUTES);
        b.rule(ATTRIBUTE).is(b.firstOf(
                b.sequence(/* hack: */b.nextNot(NAMESPACE), ATTRIBUTE_EXPR),
                RESERVED_NAMESPACE,
                b.sequence(LBRAKET, ASSIGNMENT_EXPR, RBRAKET)));
        b.rule(ATTRIBUTE_EXPR).is(IDENTIFIER, b.zeroOrMore(PROPERTY_OPERATOR));

        b.rule(IMPORT_DIRECTIVE).is(IMPORT, LABEL, b.optional(DOT, STAR));

        b.rule(INCLUDE_DIRECTIVE).is(HASH, INCLUDE, SPACING_NO_LB, NEXT_NOT_LB, b.firstOf(STRING, b.sequence(LT, b.regexp("[^>\\r\\n]++"), GT)));

        b.rule(USE_DIRECTIVE).is(USE, NAMESPACE, LIST_EXPRESSION);

        b.rule(DEFAULT_XML_NAMESPACE_DIRECTIVE).is(DEFAULT, /* No line break */ SPACING_NO_LB, NEXT_NOT_LB, XML,
                /* No line break */ SPACING_NO_LB, NEXT_NOT_LB, NAMESPACE, EQUAL1, NON_ASSIGNMENT_EXPR, EOS);
    }

    private static void definitions(LexerlessGrammarBuilder b) {
        // for C -
        b.rule(DECLARATION).is(b.sequence(DECLARATION_SPECIFIERS, b.optional(INIT_DECLARATOR_LIST), SEMICOLON));
        // STATIC_ASSERT_DECLARATION may or may not be added to above rule

        b.rule(DECLARATION_SPECIFIERS).is(b.oneOrMore(
                b.firstOf(
                        STORAGE_CLASS_SPECIFIER,
                        TYPE_SPECIFIER,
                        TYPE_QUALIFIER,
                        FUNCTION_SPECIFIER)));

        b.rule(STORAGE_CLASS_SPECIFIER).is(b.firstOf(TYPEDEF, EXTERN, STATIC, AUTO, REGISTER));

        b.rule(FUNCTION_SPECIFIER).is(INLINE);

        b.rule(INIT_DECLARATOR_LIST).is(INIT_DECLARATOR, b.zeroOrMore(b.sequence(COMMA, INIT_DECLARATOR)));
        
        b.rule(INIT_DECLARATOR).is(DECLARATOR, b.optional(EQUAL1, INITIALIZER));

        b.rule(INITIALIZER).is(b.firstOf(
                b.sequence(LCURLYBRACE, INITIALIZER_LIST, b.optional(COMMA), RCURLYBRACE),
        ASSIGNMENT_EXPR));

        b.rule(INITIALIZER_LIST).is(b.sequence(b.optional(DESIGNATION), INITIALIZER), 
        b.zeroOrMore(b.sequence(COMMA, b.optional(DESIGNATION), INITIALIZER)));

        b.rule(DESIGNATION).is(DESIGNATOR_LIST, EQUAL1);

        b.rule(DESIGNATOR_LIST).is(b.oneOrMore(DESIGNATOR));

        b.rule(DESIGNATOR).is(
                b.firstOf(
                        b.sequence(LBRAKET, CONSTANT_EXPRESSION, RBRAKET),
                        b.sequence(DOT, IDENTIFIER)));

        b.rule(DECLARATOR).is(b.optional(POINTER), DIRECT_DECLARATOR);

        b.rule(POINTER).is(b.oneOrMore(b.sequence(STAR, b.optional(TYPE_QUALIFIER_LIST))));

        b.rule(TYPE_QUALIFIER_LIST).is(b.oneOrMore(TYPE_QUALIFIER));

        b.rule(DIRECT_DECLARATOR).is(
                b.firstOf(IDENTIFIER, b.sequence(LPARENTHESIS, DECLARATOR, RPARENTHESIS)),
                b.zeroOrMore(b.firstOf(ARRAY_SUFFIX, FUNCTION_SUFFIX)));

        b.rule(ARRAY_SUFFIX).is(LBRAKET, b.optional(b.firstOf(STAR, b.sequence(
            b.optional(STATIC), 
            b.optional(TYPE_QUALIFIER_LIST), 
            b.optional(ASSIGNMENT_EXPR)),
            b.sequence(TYPE_QUALIFIER_LIST, STATIC, ASSIGNMENT_EXPR))), RBRAKET);

        b.rule(FUNCTION_SUFFIX).is(LPARENTHESIS, b.optional(b.firstOf(PARAMETER_TYPE_LIST, IDENTIFIER_LIST)), RPARENTHESIS);

        b.rule(IDENTIFIER_LIST).is(IDENTIFIER, b.zeroOrMore(b.sequence(COMMA, IDENTIFIER)));

        // existing in flex - 
        b.rule(VARIABLE_DEF).is(b.optional(TYPE_QUALIFIER), TYPE_SPECIFIER, VARIABLE_BINDING_LIST, EOS);
        b.rule(VARIABLE_DEF_NO_IN).is(VARIABLE_DEF_KIND, VARIABLE_BINDING_LIST_NO_IN);

        b.rule(VARIABLE_DEF_KIND).is(b.firstOf(VAR, CONST));

        b.rule(VARIABLE_BINDING_LIST).is(VARIABLE_BINDING, b.zeroOrMore(COMMA, VARIABLE_BINDING));
        b.rule(VARIABLE_BINDING_LIST_NO_IN).is(VARIABLE_BINDING_NO_IN, b.zeroOrMore(COMMA, VARIABLE_BINDING_NO_IN));

        b.rule(VARIABLE_BINDING).is(TYPED_IDENTIFIER, b.optional(VARIABLE_INITIALISATION));
        b.rule(VARIABLE_BINDING_NO_IN).is(TYPED_IDENTIFIER_NO_IN, b.optional(VARIABLE_INITIALISATION_NO_IN));

        b.rule(VARIABLE_INITIALISATION).is(EQUAL1, VARIABLE_INITIALISER);
        b.rule(VARIABLE_INITIALISATION_NO_IN).is(EQUAL1, VARIABLE_INITIALISER_NO_IN);

        b.rule(VARIABLE_INITIALISER).is(b.firstOf(
                ASSIGNMENT_EXPR,
                ATTRIBUTE_COMBINATION));
        b.rule(VARIABLE_INITIALISER_NO_IN).is(b.firstOf(
                ASSIGNMENT_EXPR_NO_IN,
                ATTRIBUTE_COMBINATION));

        b.rule(TYPED_IDENTIFIER).is(b.firstOf(
                b.sequence(IDENTIFIER, COLON, TYPE_EXPR),
                IDENTIFIER));
        b.rule(TYPED_IDENTIFIER_NO_IN).is(b.firstOf(
                b.sequence(IDENTIFIER, COLON, TYPE_EXPR_NO_IN),
                IDENTIFIER));

        b.rule(FUNCTION_DEF).is(DECLARATION_SPECIFIERS, DECLARATOR, b.optional(DECLARATION_LIST), COMPOUND_STATEMENT);

        b.rule(DECLARATION_LIST).is(b.oneOrMore(DECLARATION));

        b.rule(TYPE_SPECIFIER).is(b.firstOf(
                VOID,
                CHAR,
                SHORT,
                INT,
                LONG,
                FLOAT,
                DOUBLE,
                SIGNED,
                UNSIGNED
        ));
        b.rule(FUNCTION_NAME).is(IDENTIFIER);
        b.rule(TYPE_QUALIFIER).is(CONST, VOLATILE);

        b.rule(FUNCTION_COMMON).is(b.firstOf(
                b.sequence(FUNCTION_SIGNATURE, BLOCK),
                b.sequence(FUNCTION_SIGNATURE, EOS)));

        b.rule(FUNCTION_SIGNATURE)
                .is(b.sequence(LPARENTHESIS, b.optional(PARAMETERS), RPARENTHESIS, b.optional(RESULT_TYPE)));

        b.rule(PARAMETERS).is(b.firstOf(
                b.sequence(PARAMETER, b.zeroOrMore(COMMA, PARAMETER), b.optional(COMMA, REST_PARAMETERS)),
                REST_PARAMETERS));

        b.rule(PARAMETER).is(b.firstOf(
                b.sequence(TYPED_IDENTIFIER, EQUAL1, ASSIGNMENT_EXPR),
                TYPED_IDENTIFIER));

        b.rule(REST_PARAMETERS).is(b.firstOf(
                b.sequence(TRIPLE_DOTS, TYPED_IDENTIFIER),
                TRIPLE_DOTS));

        b.rule(RESULT_TYPE).is(COLON, b.firstOf(VOID, TYPE_EXPR));

        b.rule(CLASS_DEF).is(CLASS, CLASS_NAME, b.optional(INHERITENCE), BLOCK);
        b.rule(CLASS_NAME).is(CLASS_IDENTIFIERS);
        b.rule(CLASS_IDENTIFIERS).is(IDENTIFIER, b.zeroOrMore(b.sequence(DOT, IDENTIFIER)));
        b.rule(INHERITENCE).is(b.firstOf(
                b.sequence(IMPLEMENTS, TYPE_EXPRESSION_LIST),
                b.sequence(EXTENDS, TYPE_EXPR, IMPLEMENTS, TYPE_EXPRESSION_LIST),
                b.sequence(EXTENDS, TYPE_EXPR)));

        b.rule(TYPE_EXPRESSION_LIST).is(TYPE_EXPR, b.zeroOrMore(b.sequence(COMMA, TYPE_EXPR)));

        b.rule(INTERFACE_DEF).is(INTERFACE, CLASS_NAME, b.optional(EXTENDS_LIST), BLOCK);
        b.rule(EXTENDS_LIST).is(EXTENDS, TYPE_EXPRESSION_LIST);

        b.rule(LABEL).is(LABEL_NAME, COLON, STATEMENT);
        b.rule(LABEL_NAME).is(IDENTIFIER);

        b.rule(NAMESPACE_DEF).is(NAMESPACE, NAMESPACE_BINDING, EOS);
        b.rule(NAMESPACE_BINDING).is(IDENTIFIER, b.optional(NAMESPACE_INITIALISATION));
        b.rule(NAMESPACE_INITIALISATION).is(EQUAL1, ASSIGNMENT_EXPR);

        /*
         * b.rule(PROGRAM).is(
         * b.firstOf(
         * b.sequence(PACKAGE_DEF, PROGRAM),
         * DIRECTIVES),
         * SPACING,
         * b.token(GenericTokenType.EOF, b.endOfInput()));
         */

b.rule(PROGRAM).is(
      b.zeroOrMore(INCLUDE_DIRECTIVE),
      b.zeroOrMore(FUNCTION_DEF),
      SPACING,
      b.token(GenericTokenType.EOF, b.endOfInput())
    );
    }

    private static void xml(LexerlessGrammarBuilder b) {
        b.rule(XML_INITIALISER).is(b.firstOf(
                XML_MARKUP,
                XML_ELEMENT,
                b.sequence(LT, GT, XML_ELEMENT_CONTENT, LT, DIV, GT)));

        b.rule(XML_ELEMENT).is(b.firstOf(
                b.sequence(LT, XML_TAG_CONTENT, b.optional(XML_WHITESPACE), DIV, GT),
                b.sequence(LT, XML_TAG_CONTENT, b.optional(XML_WHITESPACE), XML_ELEMENT_CONTENT, LT, DIV, XML_TAG_NAME,
                        b.optional(XML_WHITESPACE), GT)));

        b.rule(XML_TAG_CONTENT).is(XML_TAG_NAME, XML_ATTRIBUTES);

        b.rule(XML_TAG_NAME).is(b.firstOf(
                b.sequence(LCURLYBRACE, EXPRESSION, RCURLYBRACE),
                XML_NAME));

        b.rule(XML_ATTRIBUTES).is(b.optional(b.firstOf(
                b.sequence(XML_ATTRIBUTE, XML_ATTRIBUTES),
                b.sequence(XML_WHITESPACE, LCURLYBRACE, EXPRESSION, RCURLYBRACE))));

        b.rule(XML_ATTRIBUTE).is(b.firstOf(
                b.sequence(b.zeroOrMore(XML_WHITESPACE), XML_NAME,
                        b.zeroOrMore(XML_WHITESPACE), EQUAL1, b.zeroOrMore(XML_WHITESPACE),
                        LCURLYBRACE, EXPRESSION, RCURLYBRACE),
                b.sequence(b.zeroOrMore(XML_WHITESPACE), XML_NAME,
                        b.zeroOrMore(XML_WHITESPACE), EQUAL1, b.zeroOrMore(XML_WHITESPACE),
                        XML_ATTRIBUTE_VALUE)));

        b.rule(XML_ELEMENT_CONTENT).is(b.optional(
                b.firstOf(
                        b.sequence(LCURLYBRACE, EXPRESSION, RCURLYBRACE, XML_ELEMENT_CONTENT),
                        b.sequence(XML_MARKUP, XML_ELEMENT_CONTENT),
                        b.sequence(XML_TEXT, XML_ELEMENT_CONTENT),
                        b.sequence(XML_ELEMENT, XML_ELEMENT_CONTENT))));

        b.rule(XML_MARKUP).is(b.firstOf(
                XML_COMMENT,
                XML_CDATA,
                XML_PI));

        b.rule(XML_COMMENT).is(SPACING, b.regexp("<!--(?:(?!--)[\\s\\S])*?-->"));
        b.rule(XML_CDATA).is(SPACING, b.regexp("<!\\[CDATA\\[(?:(?!]])[\\s\\S])*?]]>"));
        b.rule(XML_PI).is(SPACING, b.regexp("<\\?(?:(?!\\?>)[\\s\\S])*?\\?>"));
        b.rule(XML_TEXT).is(SPACING, b.regexp("[^{<]++"));
        b.rule(XML_NAME).is(SPACING,
                b.regexp("[" + UNICODE_LETTER + "_:" + "]" + "[" + UNICODE_LETTER + UNICODE_DIGIT + "\\.\\-_:" + "]*"));
        b.rule(XML_ATTRIBUTE_VALUE).is(b.regexp("(\"([^\"]*[//s//S]*)\")|(\'([^\']*[//s//S]*)\')"));
        b.rule(XML_WHITESPACE).is(b.regexp("[ \\t\\r\\n]+"));
    }

    private static void keywords(LexerlessGrammarBuilder b) {
        for (CKeyword k : CKeyword.values()) {
            b.rule(k).is(SPACING, k.getValue(), b.nextNot(IDENTIFIER_PART));
        }

        List<CKeyword> keywords = CKeyword.keywords();
        Object[] rest = new Object[keywords.size() - 2];
        for (int i = 2; i < keywords.size(); i++) {
            rest[i - 2] = keywords.get(i);
        }
        b.rule(KEYWORDS).is(b.firstOf(keywords.get(0), keywords.get(1), rest));
    }

    private static void punctuators(LexerlessGrammarBuilder b) {
        for (CPunctuator p : CPunctuator.values()) {
            b.rule(p).is(SPACING, p.getValue());
        }
    }

    private static Object word(LexerlessGrammarBuilder b, String word) {
        return b.sequence(SPACING, word, b.nextNot(IDENTIFIER_PART));
    }

}
