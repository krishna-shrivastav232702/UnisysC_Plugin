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

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;
import com.sonar.sslr.api.Token;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

import org.sonar.c.CCheck;
import org.sonar.c.CGrammar;
import org.sonar.check.Rule;

@Rule(key = "M23_034")
public class EscapeCharacterCheck extends CCheck {

    private static final String MESSAGE = "Within character literals and non raw-string literals, \"\\\\\" shall only be used to form a defined escape sequence or universal character name";

    private final Set<Integer> reportedLines = new HashSet<>();

    @Override
    public List<AstNodeType> subscribedTo() {
        return Arrays.asList(CGrammar.STRING_CONSTANT, CGrammar.CHARACTER_CONSTANT, CGrammar.STRING);
    }

    @Override
    public void visitFile(@Nullable AstNode node) {
        reportedLines.clear();

        // Fall back to source scanning when the grammar cannot build an AST for a file
        // containing invalid escapes or universal character names not modeled in
        // ESCAPE_SEQUENCE.
        if (getContext().rootTree() == null) {
            scanSource(getContext().fileContent());
        }
    }

    @Override
    public void visitNode(AstNode node) {
        String literal = fullText(node);
        if (hasInvalidEscape(extractContent(literal))) {
            report(node.getTokenLine());
        }
    }

    private void scanSource(String source) {
        int index = 0;
        int line = 1;

        while (index < source.length()) {
            char current = source.charAt(index);

            if (current == '\r') {
                if (index + 1 < source.length() && source.charAt(index + 1) == '\n') {
                    index++;
                }
                line++;
                index++;
                continue;
            }

            if (current == '\n') {
                line++;
                index++;
                continue;
            }

            if (startsLineComment(source, index)) {
                index += 2;
                while (index < source.length() && source.charAt(index) != '\r' && source.charAt(index) != '\n') {
                    index++;
                }
                continue;
            }

            if (startsBlockComment(source, index)) {
                index += 2;
                while (index < source.length()) {
                    char blockChar = source.charAt(index);
                    if (blockChar == '\r') {
                        if (index + 1 < source.length() && source.charAt(index + 1) == '\n') {
                            index++;
                        }
                        line++;
                        index++;
                        continue;
                    }
                    if (blockChar == '\n') {
                        line++;
                        index++;
                        continue;
                    }
                    if (blockChar == '*' && index + 1 < source.length() && source.charAt(index + 1) == '/') {
                        index += 2;
                        break;
                    }
                    index++;
                }
                continue;
            }

            Literal literal = readLiteral(source, index);
            if (literal != null) {
                if (hasInvalidEscape(literal.content)) {
                    report(line);
                }
                index = literal.endIndex;
                continue;
            }

            index++;
        }
    }

    private void report(int line) {
        if (reportedLines.add(line)) {
            addIssueAtLine(MESSAGE, line);
        }
    }

    private static String fullText(AstNode node) {
        StringBuilder builder = new StringBuilder();
        for (Token token : node.getTokens()) {
            builder.append(token.getValue());
        }
        return builder.toString();
    }

    private static String extractContent(String literal) {
        if (literal == null || literal.isEmpty()) {
            return "";
        }

        int start = 0;
        if (literal.startsWith("L\"") || literal.startsWith("L'")) {
            start = 2;
        } else if (literal.charAt(0) == '"' || literal.charAt(0) == '\'') {
            start = 1;
        }

        int end = literal.length();
        if (end > start && (literal.charAt(end - 1) == '"' || literal.charAt(end - 1) == '\'')) {
            end--;
        }

        return literal.substring(start, end);
    }

    private static boolean hasInvalidEscape(String content) {
        int index = 0;
        while (index < content.length()) {
            if (content.charAt(index) != '\\') {
                index++;
                continue;
            }

            int length = escapeLength(content, index);
            if (length == 0) {
                return true;
            }
            index += length;
        }
        return false;
    }

    private static int escapeLength(String content, int index) {
        if (index + 1 >= content.length()) {
            return 0;
        }

        char next = content.charAt(index + 1);
        if (isSimpleEscape(next)) {
            return 2;
        }

        if (next == 'x') {
            int cursor = index + 2;
            while (cursor < content.length() && isHexDigit(content.charAt(cursor))) {
                cursor++;
            }
            return cursor > index + 2 ? cursor - index : 0;
        }

        if (isOctalDigit(next)) {
            int cursor = index + 2;
            int digits = 1;
            while (cursor < content.length() && digits < 3 && isOctalDigit(content.charAt(cursor))) {
                cursor++;
                digits++;
            }
            return cursor - index;
        }

        if (next == 'u') {
            return hasExactHexDigits(content, index + 2, 4) ? 6 : 0;
        }

        if (next == 'U') {
            return hasExactHexDigits(content, index + 2, 8) ? 10 : 0;
        }

        return 0;
    }

    private static Literal readLiteral(String source, int index) {
        if (index >= source.length()) {
            return null;
        }

        int quoteIndex = -1;
        if (source.charAt(index) == '"' || source.charAt(index) == '\'') {
            quoteIndex = index;
        } else if (matchesPrefix(source, index, "L") && hasQuoteAt(source, index + 1)
                && isLiteralPrefixBoundary(source, index)) {
            quoteIndex = index + 1;
        }

        if (quoteIndex < 0) {
            return null;
        }

        char quote = source.charAt(quoteIndex);
        int cursor = quoteIndex + 1;
        StringBuilder content = new StringBuilder();

        while (cursor < source.length()) {
            char current = source.charAt(cursor);
            if (current == quote) {
                return new Literal(content.toString(), cursor + 1);
            }
            if (current == '\r' || current == '\n') {
                return new Literal(content.toString(), cursor);
            }
            if (current == '\\' && cursor + 1 < source.length()) {
                content.append(current);
                content.append(source.charAt(cursor + 1));
                cursor += 2;
                continue;
            }
            content.append(current);
            cursor++;
        }

        return new Literal(content.toString(), cursor);
    }

    private static boolean startsLineComment(String source, int index) {
        return index + 1 < source.length() && source.charAt(index) == '/' && source.charAt(index + 1) == '/';
    }

    private static boolean startsBlockComment(String source, int index) {
        return index + 1 < source.length() && source.charAt(index) == '/' && source.charAt(index + 1) == '*';
    }

    private static boolean matchesPrefix(String source, int index, String prefix) {
        return source.regionMatches(index, prefix, 0, prefix.length());
    }

    private static boolean hasQuoteAt(String source, int index) {
        return index < source.length() && (source.charAt(index) == '"' || source.charAt(index) == '\'');
    }

    private static boolean isLiteralPrefixBoundary(String source, int index) {
        return index == 0 || !isIdentifierPart(source.charAt(index - 1));
    }

    private static boolean isIdentifierPart(char character) {
        return Character.isLetterOrDigit(character) || character == '_';
    }

    private static boolean isSimpleEscape(char character) {
        return "'\"?\\\\abfnrtv".indexOf(character) >= 0;
    }

    private static boolean isOctalDigit(char character) {
        return character >= '0' && character <= '7';
    }

    private static boolean isHexDigit(char character) {
        return (character >= '0' && character <= '9')
                || (character >= 'a' && character <= 'f')
                || (character >= 'A' && character <= 'F');
    }

    private static boolean hasExactHexDigits(String content, int index, int count) {
        if (index + count > content.length()) {
            return false;
        }

        for (int i = 0; i < count; i++) {
            if (!isHexDigit(content.charAt(index + i))) {
                return false;
            }
        }
        return true;
    }

    private static final class Literal {
        private final String content;
        private final int endIndex;

        private Literal(String content, int endIndex) {
            this.content = content;
            this.endIndex = endIndex;
        }
    }
}
