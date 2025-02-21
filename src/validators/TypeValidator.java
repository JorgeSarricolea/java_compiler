package src.validators;

import java.util.HashMap;

public class TypeValidator {
    private static final HashMap<String, String> TYPE_PATTERNS = new HashMap<String, String>() {{
        put("IntegerType", "^-?\\d+$");
        put("FloatType", "^-?\\d*\\.?\\d+$");
        put("StringType", "^\".*\"$");
    }};

    private static final HashMap<String, String[]> COMPATIBLE_TYPES = new HashMap<String, String[]>() {{
        put("IntegerType", new String[]{"IntegerType", "FloatType"});
        put("FloatType", new String[]{"IntegerType", "FloatType"});
        put("StringType", new String[]{"StringType"});
    }};

    public static boolean isValidType(String type) {
        return TYPE_PATTERNS.containsKey(type);
    }

    public static boolean isValidValueForType(String type, String value) {
        String pattern = TYPE_PATTERNS.get(type);
        return pattern != null && value.matches(pattern);
    }

    public static boolean areTypesCompatible(String type1, String type2) {
        String[] compatibleTypes = COMPATIBLE_TYPES.get(type1);
        if (compatibleTypes == null) return false;

        for (String compatibleType : compatibleTypes) {
            if (compatibleType.equals(type2)) return true;
        }
        return false;
    }
}