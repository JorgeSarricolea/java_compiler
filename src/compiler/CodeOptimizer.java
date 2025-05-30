package src.compiler;

import java.util.*;
import java.util.regex.*;

public class CodeOptimizer {

    public String optimize(String originalCode) {
        String[] lines = originalCode.split("\\n");
        Map<String, String> exprToVar = new LinkedHashMap<>();
        List<String> optimizedLines = new ArrayList<>();

        Pattern assignmentPattern = Pattern.compile("^\\s*(JSJ[a-z][0-9]+)\\s*=\\s*(.+);$");

        // Paso 1: almacenar expresiones constantes
        for (String line : lines) {
            Matcher matcher = assignmentPattern.matcher(line.trim());
            if (matcher.matches()) {
                String var = matcher.group(1).trim();
                String expr = matcher.group(2).trim();
                if (isPureConstant(expr)) {
                    String normalized = normalize(expr);
                    exprToVar.putIfAbsent(normalized, var);
                }
            }
        }

        // Paso 2: optimizar reemplazando subexpresiones respetando jerarquía
        for (String line : lines) {
            Matcher matcher = assignmentPattern.matcher(line.trim());

            if (!matcher.matches()) {
                optimizedLines.add(line);
                continue;
            }

            String leftVar = matcher.group(1).trim();
            String rightExpr = matcher.group(2).trim();
            String normalizedRight = normalize(rightExpr);

            if (exprToVar.getOrDefault(normalizedRight, "").equals(leftVar)) {
                optimizedLines.add(line);
                continue;
            }

            boolean replaced = false;
            for (Map.Entry<String, String> entry : exprToVar.entrySet()) {
                String exprNorm = entry.getKey();
                String exprVar = entry.getValue();

                if (leftVar.equals(exprVar)) continue;

                if (containsExactNormalizedSubexpr(normalizedRight, exprNorm) && isSafeToReplace(rightExpr, exprNorm)) {
                    String newExpr = replaceExactSubExpr(rightExpr, exprNorm, exprVar);
                    optimizedLines.add(leftVar + " = " + newExpr + ";");
                    replaced = true;
                    break;
                }
            }

            if (!replaced) optimizedLines.add(line);
        }

        return String.join("\n", optimizedLines);
    }

    private boolean isPureConstant(String expr) {
        return expr.matches("[0-9\\s+\\-*/()]+");
    }

    private String normalize(String expr) {
        return expr.replaceAll("\\s+", "");
    }

    private boolean containsExactNormalizedSubexpr(String normalizedExpr, String subexpr) {
        int idx = normalizedExpr.indexOf(subexpr);
        if (idx == -1) return false;

        char before = idx > 0 ? normalizedExpr.charAt(idx - 1) : ' ';
        char after = idx + subexpr.length() < normalizedExpr.length() ? normalizedExpr.charAt(idx + subexpr.length()) : ' ';

        return !(Character.isLetterOrDigit(before) || Character.isLetterOrDigit(after));
    }

    private boolean isSafeToReplace(String expr, String rawExpr) {
        String noSpaces = expr.replaceAll("\\s+", "");
        String normalizedRaw = rawExpr.replaceAll("\\s+", "");
        int idx = noSpaces.indexOf(normalizedRaw);
        if (idx == -1) return false;

        if (noSpaces.equals(normalizedRaw)) return true;

        // Verificar que no esté afectado por * o / justo afuera de la subexpresión
        int beforeIdx = idx - 1;
        int afterIdx = idx + normalizedRaw.length();

        if (beforeIdx >= 0) {
            char before = noSpaces.charAt(beforeIdx);
            if (before == '*' || before == '/') return false;
        }
        if (afterIdx < noSpaces.length()) {
            char after = noSpaces.charAt(afterIdx);
            if (after == '*' || after == '/') return false;
        }

        return true;
    }

    private List<String> tokenize(String expr) {
        List<String> tokens = new ArrayList<>();
        Matcher matcher = Pattern.compile("JSJ[a-z][0-9]+|\\d+|[+\\-*/()]|\\s+").matcher(expr);
        while (matcher.find()) {
            tokens.add(matcher.group());
        }
        return tokens;
    }

    private String replaceExactSubExpr(String expr, String targetNormalized, String replacementVar) {
        List<String> tokens = tokenize(expr);
        List<String> normalized = new ArrayList<>();
        for (String t : tokens) {
            if (!t.matches("\\s+")) normalized.add(t);
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < normalized.size(); ) {
            boolean matched = false;
            for (int len = normalized.size() - i; len > 0; len--) {
                String segment = String.join("", normalized.subList(i, i + len));
                if (segment.equals(targetNormalized)) {
                    result.append(replacementVar);
                    i += len;
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                result.append(normalized.get(i));
                i++;
            }
        }
        return result.toString();
    }
}
