package _01_SimpleLexer;

import java.io.CharArrayReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple Lexer implementation to tokenize input scripts.
 * It identifies tokens such as keywords, identifiers, operators, literals, and delimiters.
 */
public class SimpleLexer {

    public static void main(String[] args) {
        SimpleLexer lexer = new SimpleLexer();

        // Test script 1: Valid script
        String script = "int age = 45;";
        System.out.println("parse:\t" + script);
        SimpleTokenReader tokenReader = lexer.tokenize(script);
        dump(tokenReader);

        // Test script 2: Invalid identifier
        script = "inta age = 45;";
        System.out.println("\nparse:\t" + script);
        tokenReader = lexer.tokenize(script);
        dump(tokenReader);

        // Test script 3: Relational operator '>='
        script = "age >= 45;";
        System.out.println("\nparse:\t" + script);
        tokenReader = lexer.tokenize(script);
        dump(tokenReader);

        // Test script 4: Relational operator '>'
        script = "age > 45;";
        System.out.println("\nparse:\t" + script);
        tokenReader = lexer.tokenize(script);
        dump(tokenReader);
    }

    // Temporary storage for token being processed
    private StringBuffer tokenText = null;
    // List to store identified tokens
    private List<Token> tokens = null;
    // Current token being constructed
    private SimpleToken token = null;

    // Check if the character is alphabetic
    private boolean isAlpha(int ch) {
        return ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z';
    }

    // Check if the character is blank (space, tab, newline)
    private boolean isBlank(int ch) {
        return ch == ' ' || ch == '\t' || ch == '\n';
    }

    // Check if the character is numeric
    private boolean isDigit(int ch) {
        return ch >= '0' && ch <= '9';
    }

    /**
     * Inner class representing a token.
     */
    private final class SimpleToken implements Token {
        private TokenType type = null; // Token type (e.g., Identifier, IntLiteral, etc.)
        private String text = null;   // Text content of the token

        @Override
        public TokenType getType() {
            return type;
        }

        @Override
        public String getText() {
            return text;
        }
    }

    /**
     * Inner class representing a token reader to iterate over tokens.
     */
    private class SimpleTokenReader implements TokenReader {
        List<Token> tokens = null; // List of tokens
        int pos = 0;              // Current position in token list

        public SimpleTokenReader(List<Token> tokens) {
            this.tokens = tokens;
        }

        @Override
        public Token read() {
            if (pos < tokens.size()) {
                return tokens.get(pos++);
            }
            return null;
        }

        @Override
        public Token peek() {
            if (pos < tokens.size()) {
                return tokens.get(pos);
            }
            return null;
        }

        @Override
        public void unread() {
            if (pos > 0) {
                pos--;
            }
        }

        @Override
        public int getPosition() {
            return pos;
        }

        @Override
        public void setPosition(int position) {
            if (position >= 0 && position < tokens.size()) {
                pos = position;
            }
        }
    }

    /**
     * Prints tokens to the console.
     */
    public static void dump(SimpleTokenReader tokenReader) {
        System.out.println("text\ttype");
        Token token = null;
        while ((token = tokenReader.read()) != null) {
            System.out.println(token.getText() + "\t\t" + token.getType());
        }
    }

    /**
     * DFA states for the lexer.
     */
    private enum DfaState {
        Initial,          // Initial state
        If, Id_if1, Id_if2,
        Else, Id_else1, Id_else2, Id_else3, Id_else4,
        Int, Id_int1, Id_int2, Id_int3, Id,
        GT, GE,           // Relational operators '>' and '>='
        Assignment,       // '=' operator
        Plus, Minus, Star, Slash, // Arithmetic operators
        SemiColon,        // ';' delimiter
        LeftParen,        // '('
        RightParen,       // ')'
        IntLiteral        // Integer literal
    }

    /**
     * Initializes a new token and sets the DFA state based on the input character.
     */
    private DfaState initToken(char ch) {
        if (tokenText.length() > 0) {
            token.text = tokenText.toString();
            tokens.add(token);
            tokenText = new StringBuffer();
            token = new SimpleToken();
        }
        DfaState newState = DfaState.Initial;

        if (isAlpha(ch)) {
            if (ch == 'i') {
                newState = DfaState.Id_int1; // Possible 'int' keyword
            } else {
                newState = DfaState.Id;
            }
            token.type = TokenType.Identifier;
            tokenText.append(ch);
        } else if (isDigit(ch)) {
            newState = DfaState.IntLiteral;
            token.type = TokenType.IntLiteral;
            tokenText.append(ch);
        } else if (ch == '>') {
            newState = DfaState.GT;
            token.type = TokenType.GT;
            tokenText.append(ch);
        } else if (ch == '+') {
            newState = DfaState.Plus;
            token.type = TokenType.Plus;
            tokenText.append(ch);
        } else if (ch == '-') {
            newState = DfaState.Minus;
            token.type = TokenType.Minus;
            tokenText.append(ch);
        } else if (ch == '*') {
            newState = DfaState.Star;
            token.type = TokenType.Star;
            tokenText.append(ch);
        } else if (ch == '/') {
            newState = DfaState.Slash;
            token.type = TokenType.Slash;
            tokenText.append(ch);
        } else if (ch == ';') {
            newState = DfaState.SemiColon;
            token.type = TokenType.SemiColon;
            tokenText.append(ch);
        } else if (ch == '(') {
            newState = DfaState.LeftParen;
            token.type = TokenType.LeftParen;
            tokenText.append(ch);
        } else if (ch == ')') {
            newState = DfaState.RightParen;
            token.type = TokenType.RightParen;
            tokenText.append(ch);
        } else if (ch == '=') {
            newState = DfaState.Assignment;
            token.type = TokenType.Assignment;
            tokenText.append(ch);
        } else {
            newState = DfaState.Initial; // Ignore unrecognized characters
        }
        return newState;
    }

    /**
     * Tokenizes the input code into a list of tokens.
     */
    public SimpleTokenReader tokenize(String code) {
        // Initializes the list to store tokens that will be generated.
        tokens = new ArrayList<>();
        // Converts the input code string into a character array and initializes a CharArrayReader to iterate through the characters.
        CharArrayReader reader = new CharArrayReader(code.toCharArray());
        // Initializes a buffer to construct the text of the current token being processed.
        tokenText = new StringBuffer();
        // Initializes a new SimpleToken object to store the current token's type and text.
        token = new SimpleToken();

        int ich = 0;
        char ch = 0;
        // Sets the initial DFA state to 'Initial' to start tokenization.
        DfaState state = DfaState.Initial;

        try {
            // Reads characters from the input stream one by one until the end is reached.
            while ((ich = reader.read()) != -1) {
                // Casts the integer 'ich' to a character for processing.
                ch = (char) ich;
                // Determines the next action based on the current DFA state.
                switch (state) {
                    case Initial:
                        // Handles the 'Initial' state by attempting to transition to a new state based on the character.
                        state = initToken(ch);
                        break;
                    case Id:
                        // Processes identifiers by appending alphabetic or numeric characters to the token text.
                        if (isAlpha(ch) || isDigit(ch)) {
                            tokenText.append(ch);
                        } else {
                            state = initToken(ch);
                        }
                        break;
                    case GT:
                        // Handles the '>' operator and checks for the '>=' operator by reading the next character.
                        if (ch == '=') {
                            token.type = TokenType.GE;
                            state = DfaState.GE;
                            tokenText.append(ch);
                        } else {
                            state = initToken(ch);
                        }
                        break;
                    case GE:
                    case Assignment:
                    case Plus:
                    case Minus:
                    case Star:
                    case Slash:
                    case SemiColon:
                    case LeftParen:
                    case RightParen:
                        // Finalizes the token and resets the state for these specific token types.
                        state = initToken(ch);
                        break;
                    case IntLiteral:
                        // Processes numeric literals by appending digits to the token text.
                        if (isDigit(ch)) {
                            tokenText.append(ch);
                        } else {
                            state = initToken(ch);
                        }
                        break;
                    case Id_int1:
                        // Handles the potential start of the 'int' keyword by checking for the next character.
                        if (ch == 'n') {
                            state = DfaState.Id_int2;
                            tokenText.append(ch);
                        } else if (isAlpha(ch) || isDigit(ch)) {
                            state = DfaState.Id;
                            tokenText.append(ch);
                        } else {
                            state = initToken(ch);
                        }
                        break;
                    case Id_int2:
                        // Processes the second character of the 'int' keyword or transitions back to 'Id' for other identifiers.
                        if (ch == 't') {
                            state = DfaState.Id_int3;
                            tokenText.append(ch);
                        } else if (isAlpha(ch) || isDigit(ch)) {
                            state = DfaState.Id;
                            tokenText.append(ch);
                        } else {
                            state = initToken(ch);
                        }
                        break;
                    case Id_int3:
                        // Finalizes the 'int' keyword if followed by a blank character or continues as an identifier.
                        if (isBlank(ch)) {
                            token.type = TokenType.Int; // Finalize 'int' keyword
                            state = initToken(ch);
                        } else {
                            state = DfaState.Id;
                            tokenText.append(ch);
                        }
                        break;
                    default:
                        break;
                }
            }

            // Finalize any remaining token
            if (tokenText.length() > 0) {
                initToken(ch);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Returns a SimpleTokenReader initialized with the list of generated tokens.
        return new SimpleTokenReader(tokens);
    }
}
