import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and", TokenType.AND);
        keywords.put("class", TokenType.CLASS);
        keywords.put("else", TokenType.ELSE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("for", TokenType.FOR);
        keywords.put("fun", TokenType.FUN);
        keywords.put("if", TokenType.IF);
        keywords.put("nil", TokenType.NIL);
        keywords.put("or", TokenType.OR);
        keywords.put("print", TokenType.PRINT);
        keywords.put("return", TokenType.RETURN);
        keywords.put("super", TokenType.SUPER);
        keywords.put("this", TokenType.THIS);
        keywords.put("true", TokenType.TRUE);
        keywords.put("var", TokenType.VAR);
        keywords.put("while", TokenType.WHILE);
    }

    Scanner(String source) {
        this.source = source;
    }

    public List<Token> scanTokens() {
        while (!this.isAtEnd()) {
            this.start = current;

            this.scanToken();
        }

        this.tokens.add(new Token(TokenType.EOF, "", null, this.line));

        return this.tokens;
    }

    private char advance() {
        return this.source.charAt(this.current++);
    }

    private void addToken(TokenType type) {
        this.addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = this.source.substring(this.start, this.current);

        this.tokens.add(new Token(type, text, literal, this.line));
    }

    private void scanToken() {
        char c = this.advance();

        switch (c) {
            case '(': this.addToken(TokenType.LEFT_PAREN); break;
            case ')': this.addToken(TokenType.RIGHT_PAREN); break;
            case '{': this.addToken(TokenType.LEFT_BRACE); break;
            case '}': this.addToken(TokenType.RIGHT_BRACE); break;
            case ',': this.addToken(TokenType.COMMA); break;
            case '.': this.addToken(TokenType.DOT); break;
            case '-': this.addToken(TokenType.MINUS); break;
            case '+': this.addToken(TokenType.PLUS); break;
            case ';': this.addToken(TokenType.SEMICOLON); break;
            case '*': this.addToken(TokenType.STAR); break;
            case '!':
                addToken(this.match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
                break;
            case '=':
                addToken(this.match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
                break;
            case '<':
                addToken(this.match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
                break;
            case '>':
                addToken(this.match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
                break;
            case '/':
                if (this.match('/')) {
                    // A comment goes until the end of the line.
                    while (this.peek() != '\n' && !this.isAtEnd()) this.advance();
                } else if (this.match('*')) {
                    this.blockComment();
                } else {
                    this.addToken(TokenType.SLASH);
                }
                break;
            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespace.
                break;
            case '\n':
                line++;
                break;
            case '"': this.string(); break;
            default:
                if (this.isDigit(c)) {
                    this.number();
                } else if (this.isAlpha(c)) {
                    this.identifier();
                } else {
                    Lox.error(line, "Unexpected character.");
                }
                break;
        }
    }

    private void identifier() {
        while (this.isAlphaNumeric(peek())) advance();

        String text = this.source.substring(this.start, this.current);
        TokenType type = keywords.get(text);

        if (type == null) type = TokenType.IDENTIFIER;

        this.addToken(type);
    }

    private void string() {
        while (this.peek() != '"' && !this.isAtEnd()) {
            if (this.peek() == '\n') line++;

            this.advance();
        }

        if (this.isAtEnd()) {
            Lox.error(line, "Unterminated string.");

            return;
        }

        // The closing ".
        this.advance();

        // Trim the surrounding quotes.
        String value = this.source.substring(start + 1, current - 1);

        this.addToken(TokenType.STRING, value);
    }

    private boolean match(char expected) {
        if (this.isAtEnd() || this.source.charAt(this.current) != expected) return false;

        current++;

        return true;
    }

    private char peek() {
        return this.isAtEnd() ? '\0' : this.source.charAt(current);
    }

    private char peekNext() {
        if (this.current + 1 >= this.source.length()) return '\0';

        return this.source.charAt(this.current + 1);
    }

    private void blockComment() {
        while(!this.isBlockCommentEnd() && !this.isAtEnd()) {
            if (peek() == '\n') this.line++;

            this.advance();
        }

        if (this.isAtEnd()) {
            Lox.error(this.line, "Unterminated block comment.");
        }

        if (!this.match('/')) {
            this.advance();
        }

        this.advance();
    }

    private boolean isBlockCommentEnd() {
        return this.peek() == '*' && this.peekNext() == '/';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return this.isAlpha(c) || this.isDigit(c);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private void number() {
        while (this.isDigit(peek())) this.advance();

        // Look for a fractional part.
        if (this.peek() == '.' && this.isDigit(this.peekNext())) {
            // Consume the "."
            this.advance();

            while (this.isDigit(this.peek())) this.advance();
        }

        this.addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private boolean isAtEnd() {
        return this.current >= source.length();
    }
}
