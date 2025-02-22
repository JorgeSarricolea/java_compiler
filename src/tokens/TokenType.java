package src.tokens;

import java.util.HashMap;
import java.util.regex.Pattern;

public class TokenType {
    private final String type;
    private final Pattern valuePattern;

    public static final TokenType RESERVED_WORD = new TokenType("Reserved Word", null);
    public static final TokenType DELIMITER = new TokenType("Delimiter", null);
    public static final TokenType ASSIGNMENT_OPERATOR = new TokenType("Assignment Operator", null);
    public static final TokenType ARITHMETIC_OPERATOR = new TokenType("Arithmetic Operator", null);
    public static final TokenType RELATIONAL_OPERATOR = new TokenType("Relational Operator", null);

    public static final TokenType INTEGER_TYPE = new TokenType("IntegerType", Pattern.compile("^-?\\d+$"));
    public static final TokenType FLOAT_TYPE = new TokenType("FloatType", Pattern.compile("^-?\\d*\\.?\\d+$"));
    public static final TokenType STRING_TYPE = new TokenType("StringType", Pattern.compile("^\".*\"$"));

    private static final HashMap<String, TokenType> SYMBOL_TYPES = new HashMap<String, TokenType>() {{
        // Types (Reserved Words)
        put("IntegerType", INTEGER_TYPE);
        put("FloatType", FLOAT_TYPE);
        put("StringType", STRING_TYPE);

        // Delimiters
        put(",", DELIMITER);
        put(";", DELIMITER);

        // Operators
        put("=", ASSIGNMENT_OPERATOR);
        put("+", ARITHMETIC_OPERATOR);
        put("-", ARITHMETIC_OPERATOR);
        put("*", ARITHMETIC_OPERATOR);
        put("/", ARITHMETIC_OPERATOR);
        put("<", RELATIONAL_OPERATOR);
        put(">", RELATIONAL_OPERATOR);
        put("<=", RELATIONAL_OPERATOR);
        put(">=", RELATIONAL_OPERATOR);
        put("==", RELATIONAL_OPERATOR);
        put("!=", RELATIONAL_OPERATOR);
    }};

    private TokenType(String type, Pattern valuePattern) {
        this.type = type;
        this.valuePattern = valuePattern;
    }

    public static TokenType getType(String symbol) {
        return SYMBOL_TYPES.get(symbol);
    }

    public boolean isValidValue(String value) {
        return valuePattern != null && valuePattern.matcher(value).matches();
    }

    @Override
    public String toString() {
        return type;
    }
}