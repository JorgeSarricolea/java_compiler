package src.errors;

import java.util.HashMap;

public class ErrorHandler {
    private static final HashMap<String, String> ERROR_MESSAGES = new HashMap<String, String>() {{
        put("INVALID_TYPE", "Type must be IntegerType, FloatType, or StringType");
        put("INVALID_IDENTIFIER", "Identifier must match pattern JSJ[a-z][0-9]+");
        put("DUPLICATE_DECLARATION", "Variable already declared");
        put("UNDECLARED_VARIABLE", "Variable must be declared before use");
        put("TYPE_MISMATCH", "Cannot perform operation between %s and %s");
        put("INVALID_ASSIGNMENT", "Cannot assign %s value to %s variable");
    }};

    public static String getErrorMessage(String errorCode, Object... args) {
        String message = ERROR_MESSAGES.get(errorCode);
        return args.length > 0 ? String.format(message, args) : message;
    }
}