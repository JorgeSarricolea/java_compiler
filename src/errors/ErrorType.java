package src.errors;

import java.util.HashMap;

public class ErrorType {
    private final String token;
    private final String messageTemplate;

    private static final HashMap<String, String> ERROR_MESSAGES = new HashMap<String, String>() {{
        put("INVALID_TYPE", "Type must be IntegerType, FloatType, or StringType");
        put("INVALID_IDENTIFIER", "Identifier must match pattern JSJ[a-z][0-9]+");
        put("DUPLICATE_DECLARATION", "Variable already declared");
        put("UNDECLARED_VARIABLE", "Variable must be declared before use");
        put("TYPE_MISMATCH", "Cannot assign %s value to %s variable");
        put("SYNTAX_ERROR", "Missing value after assignment operator");
    }};

    public static final ErrorType INVALID_TYPE = new ErrorType("Invalid_Type", "INVALID_TYPE");
    public static final ErrorType INVALID_IDENTIFIER = new ErrorType("Invalid_Identifier", "INVALID_IDENTIFIER");
    public static final ErrorType DUPLICATE_DECLARATION = new ErrorType("Duplicate_Declaration", "DUPLICATE_DECLARATION");
    public static final ErrorType UNDECLARED_VARIABLE = new ErrorType("Undeclared_Variable", "UNDECLARED_VARIABLE");
    public static final ErrorType TYPE_MISMATCH = new ErrorType("Type_Mismatch", "TYPE_MISMATCH");
    public static final ErrorType SYNTAX_ERROR = new ErrorType("Syntax_Error", "SYNTAX_ERROR");

    private ErrorType(String token, String messageKey) {
        this.token = token;
        this.messageTemplate = ERROR_MESSAGES.get(messageKey);
    }

    public String getToken() {
        return token;
    }

    public String getMessage(Object... args) {
        return args.length > 0 ? String.format(messageTemplate, args) : messageTemplate;
    }
}