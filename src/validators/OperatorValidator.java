package src.validators;

import java.util.HashMap;

public class OperatorValidator {
    private static final HashMap<String, String> OPERATORS = new HashMap<String, String>() {{
        // Assignment Operator
        put("=", "Assignment Operator");

        // Arithmetic Operators
        put("+", "Arithmetic Operator");
        put("-", "Arithmetic Operator");
        put("*", "Arithmetic Operator");
        put("/", "Arithmetic Operator");

        // Relational Operators
        put("<", "Relational Operator");
        put(">", "Relational Operator");
        put("<=", "Relational Operator");
        put(">=", "Relational Operator");
        put("==", "Relational Operator");
        put("!=", "Relational Operator");
    }};

    public static boolean isOperator(String token) {
        return OPERATORS.containsKey(token);
    }

    public static String getOperatorType(String token) {
        return OPERATORS.get(token);
    }
}