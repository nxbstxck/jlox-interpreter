package com.nxbstxck.interpreters.jlox;

import java.util.ArrayList;
import java.util.List;

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
        switch (c) {
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;
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
                }
                else {
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

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
        }

        // Advance to the closing "
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
