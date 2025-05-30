package src.compiler;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Stack;

public class AssemblyGenerator {

    private StringBuilder assemblyCode;
    private int tempCount = 1;
    private Stack<String> tempStack = new Stack<>();
    private String finalTarget = null;

    public AssemblyGenerator() {
        this.assemblyCode = new StringBuilder();
    }

    public void generateAssembly(List<String> optimizedCode) {
        int labelCounter = 1;

        for (String line : optimizedCode) {
            line = line.trim();
            if (line.isEmpty()) continue;

            if (line.contains("=") && !line.contains("while")) {
                processAssignment(line);
            } else if (line.startsWith("while")) {
                String labelStart = "LOOP_START" + labelCounter;
                String labelEnd = "LOOP_END" + labelCounter;
                labelCounter++;

                assemblyCode.append(labelStart).append(":\n");
                processCondition(line, labelEnd);
            } else if (line.equals("}")) {
                assemblyCode.append("    JMP LOOP_START" + (labelCounter - 1) + "\n");
                assemblyCode.append("LOOP_END" + (labelCounter - 1) + ":\n");
            }
        }

        assemblyCode.append("    ; End of program\nEND:\n");
    }

    private void processAssignment(String line) {
        line = line.replace(";", "");
        String[] parts = line.split("=");
        if (parts.length != 2) return;

        String target = parts[0].trim();
        String expr = parts[1].trim();

        finalTarget = target;
        tempStack.clear();
        generateExpr(expr);
        finalTarget = null;
    }

    private void generateExpr(String expr) {
        String[] addSubParts = expr.split("(?=[+-])");
        for (int i = 0; i < addSubParts.length; i++) {
            String part = addSubParts[i].trim();
            if (part.isEmpty()) continue;

            char op = '+';
            if (part.charAt(0) == '+' || part.charAt(0) == '-') {
                op = part.charAt(0);
                part = part.substring(1).trim();
            }

            String term = evaluateMulDiv(part);

            if (i == 0 && op == '+') {
                tempStack.push(term);
            } else {
                String left = tempStack.pop();
                assemblyCode.append("    MOV AL, ").append(left).append("\n");
                if (op == '+') {
                    assemblyCode.append("    ADD AL, ").append(term).append("\n");
                } else {
                    assemblyCode.append("    SUB AL, ").append(term).append("\n");
                }

                boolean isLast = (i == addSubParts.length - 1);
                if (isLast && finalTarget != null) {
                    assemblyCode.append("    MOV ").append(finalTarget).append(", AL\n");
                } else {
                    String tempResult = getTemp();
                    assemblyCode.append("    MOV ").append(tempResult).append(", AL\n");
                    tempStack.push(tempResult);
                }
            }
        }

        if (addSubParts.length == 1) {
            String onlyTerm = tempStack.pop();
            if (finalTarget != null) {
                assemblyCode.append("    MOV ").append(finalTarget).append(", ").append(onlyTerm).append("\n");
            }
        }
    }

    private String evaluateMulDiv(String expr) {
        if (!expr.contains("*") && !expr.contains("/")) return expr;

        String[] factors = expr.split("[*/]");
        char[] ops = expr.replaceAll("[^*/]", "").toCharArray();

        String left = factors[0].trim();
        for (int i = 0; i < ops.length; i++) {
            String right = factors[i + 1].trim();
            char op = ops[i];
            boolean isLast = (i == ops.length - 1);

            if (op == '*') {
                assemblyCode.append("    MOV AL, ").append(left).append("\n");
                assemblyCode.append("    MOV BL, ").append(right).append("\n");
                assemblyCode.append("    MUL BL\n");
                if (isLast && finalTarget != null) {
                    assemblyCode.append("    MOV ").append(finalTarget).append(", AX\n");
                    return finalTarget;
                } else {
                    String result = getTemp();
                    assemblyCode.append("    MOV ").append(result).append(", AX\n");
                    left = result;
                }
            } else {
                assemblyCode.append("    MOV AX, ").append(left).append("\n");
                assemblyCode.append("    MOV BL, ").append(right).append("\n");
                assemblyCode.append("    DIV BL\n");
                if (isLast && finalTarget != null) {
                    assemblyCode.append("    MOV ").append(finalTarget).append(", AL\n");
                    return finalTarget;
                } else {
                    String result = getTemp();
                    assemblyCode.append("    MOV ").append(result).append(", AL\n");
                    left = result;
                }
            }
        }

        return left;
    }

    private void processCondition(String line, String labelEnd) {
        line = line.substring(line.indexOf('(') + 1, line.lastIndexOf(')'));
        String[] conditions = line.split("&&");

        for (String cond : conditions) {
            cond = cond.trim();
            String[] ops = null;
            String op = null;
            if (cond.contains("<")) {
                ops = cond.split("<");
                op = "JL";
            } else if (cond.contains(">")) {
                ops = cond.split(">");
                op = "JG";
            }

            if (ops != null && op != null) {
                String left = ops[0].trim();
                String right = ops[1].trim();

                assemblyCode.append("    MOV AX, ").append(left).append("\n");
                assemblyCode.append("    CMP AX, ").append(right).append("\n");
                assemblyCode.append("    JN").append(op.substring(1)).append(" ").append(labelEnd).append("\n");
            }
        }
    }

    private String getTemp() {
        return "TMP" + (tempCount++);
    }

    public void saveToFile(String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(assemblyCode.toString());
        }
    }
}