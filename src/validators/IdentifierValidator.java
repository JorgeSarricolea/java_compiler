package src.validators;

import java.util.regex.Pattern;

public class IdentifierValidator {
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("JSJ[a-z][0-9]+");

    public static boolean isValidIdentifier(String identifier) {
        return IDENTIFIER_PATTERN.matcher(identifier).matches();
    }
}