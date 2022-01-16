package com.nxbstxck.interpreters.jlox;

import java.util.*;

import static com.nxbstxck.interpreters.jlox.TokenType.*;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();

    // Used for indexing the lexeme.
    private int start = 0; // First character of the lexeme
    private int current = 0; // Current character being considered in the lexeme

    // What line we're currently producing tokens from
    private int line = 1;

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    boolean isAtEnd() {
        return current >= source.length();
    }

    private void scanToken() {

        char c = advance();

        if (Character.isDigit(c)) {
            number();
        }
        else {
            switch (c) {
                case '(':
                    addToken(LEFT_PAREN);
                    break;
                case ')':
                    addToken(RIGHT_PAREN);
                    break;
                case '{':
                    addToken(LEFT_BRACE);
                    break;
                case '}':
                    addToken(RIGHT_BRACE);
                    break;
                case ',':
                    addToken(COMMA);
                    break;
                case '.':
                    addToken(DOT);
                    break;
                case '-':
                    addToken(MINUS);
                    break;
                case '+':
                    addToken(PLUS);
                    break;
                case ';':
                    addToken(SEMICOLON);
                    break;
                case '*':
                    addToken(STAR);
                    break;
                case '!':
                    addToken(matchNext('=') ? BANG_EQUAL : BANG);
                    break;
                case '=':
                    addToken(matchNext('=') ? EQUAL_EQUAL : EQUAL);
                    break;
                case '<':
                    addToken(matchNext('=') ? LESS_EQUAL : LESS);
                    break;
                case '>':
                    addToken(matchNext('=') ? GREATER_EQUAL : GREATER);
                    break;
                case '/':
                    if (matchNext('/')) {
                        // We have a comment.
                        // Move to the end of the line
                        while (peek() != '\n') {
                            current++;
                        }
                    } else {
                        addToken(SLASH);
                    }
                    break;
                case ' ':
                case '\r':
                case '\t':
                    // Ignore whitespace
                    break;

                case '\n':
                    line++;
                    break;

                case '"':
                    // Start processing a string literal
                    string();
                    break;

                default:
                    Lox.error(line, "Unexpected character.");
                    break;
            }
        }
    }

    private void number() {
        final Set<Character> whitespaceChars = new HashSet<>(Arrays.asList(' ', '\t', '\r', '\n'));
        boolean hasDecimal = false;
        boolean postDecimalDigits = false;
        boolean hasIllegalChar = false;

        while (!whitespaceChars.contains(peek()) && !isAtEnd()) {
            char currentChar = advance();
            // Validate the digit/decimal
            if (currentChar != '.' && !Character.isDigit(currentChar)) {
                Lox.error(line,"Unexpected character: '" + currentChar + "' found in number.");
                hasIllegalChar = true;
            }
            // Prevent double decimals
            else if (hasDecimal && currentChar == '.') {
                Lox.error(line, "Unexpected character: '" + currentChar + "' found in number.");
                hasIllegalChar = true;
            }

            // take note of the existence of a decimal
            if (currentChar == '.') {
                hasDecimal = true;
            }

            // take note of post-decimal digits
            if (Character.isDigit(currentChar) && hasDecimal) {
                postDecimalDigits = true;
            }
        }

        if (hasDecimal && !postDecimalDigits) {
            Lox.error(line, "Expected at least one digit after decimal number.");
            return;
        }

        if (!hasIllegalChar){
            String numberLexeme = source.substring(start, current);
            tokens.add(new Token(NUMBER, numberLexeme, Float.parseFloat(numberLexeme), line));
        }
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
        }

        // Advance to the closing quote (")
        advance();

        // Get the string from the source, and create a token for it
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    private char peek() {
        return source.charAt(current);
    }

    private boolean matchNext(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        tokens.add(new Token(type, source.substring(start, current), literal, line));
    }
}
