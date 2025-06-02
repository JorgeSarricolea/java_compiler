package src.compiler;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Stack;

public class AssemblyGenerator {

    private StringBuilder assemblyCode;
    private int tempCount = 1;
    private int labelCounter = 1;
    private Stack<String> tempStack = new Stack<>();
    private String finalTarget = null;

    public AssemblyGenerator() {
        this.assemblyCode = new StringBuilder();
    }

    public void generateAssembly(List<String> optimizedCode) {
        initializeVariables(optimizedCode);

        for (String line : optimizedCode) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("IntegerType")) continue;

            if (line.startsWith("while")) {
                generateWhile(line);
            } else if (line.equals("}")) {
                assemblyCode.append("    JMP LOOP_START").append(labelCounter - 1).append("\n");
                assemblyCode.append("END_LOOP").append(labelCounter - 1).append(":\n");
            } else if (line.contains("=")) {
                processAssignment(line);
            }
        }

        assemblyCode.append("END:\n");
    }

    private void initializeVariables(List<String> code) {
        for (String line : code) {
            if (line.startsWith("IntegerType")) {
                String[] vars = line.replace("IntegerType", "").replace(";", "").split(",");
                for (String var : vars) {
                    String cleanVar = var.trim();
                    assemblyCode.append("    MOV AX, 0\n");
                    assemblyCode.append("    MOV ").append(cleanVar).append(", AX\n");
                }
            }
        }
    }

    private void processAssignment(String line) {
        String[] parts = line.replace(";", "").split("=");
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
    
        // Detección anticipada de cuál parte es multiplicación/división
        int multIndex = -1;
        String[] evaluatedTerms = new String[addSubParts.length];
    
        for (int i = 0; i < addSubParts.length; i++) {
            String part = addSubParts[i].trim();
            if (part.startsWith("+") || part.startsWith("-")) {
                part = part.substring(1).trim();
            }
    
            String eval = evaluateMulDiv(part);
            evaluatedTerms[i] = eval;
    
            if ("AX_RESULT".equals(eval)) {
                multIndex = i;
            }
        }
    
        for (int i = 0; i < addSubParts.length; i++) {
            String part = addSubParts[i].trim();
            char op = '+';
    
            if (part.charAt(0) == '+' || part.charAt(0) == '-') {
                op = part.charAt(0);
                part = part.substring(1).trim();
            }
    
            String term = evaluatedTerms[i];
    
            if (i == multIndex) {
                // Ya se generó el código dentro de evaluateMulDiv
                continue;
            }
    
            if (multIndex == -1 && i == 0) {
                assemblyCode.append("    MOV AX, ").append(term).append("\n");
            } else {
                if (op == '+') {
                    assemblyCode.append("    ADD AX, ").append(term).append("\n");
                } else {
                    assemblyCode.append("    SUB AX, ").append(term).append("\n");
                }
            }
        }
    
        if (finalTarget != null && !"AX".equals(finalTarget)) {
            assemblyCode.append("    MOV ").append(finalTarget).append(", AX\n");
        }
    }
    

    private String evaluateMulDiv(String expr) {
        if (!expr.contains("*") && !expr.contains("/")) return expr;

        String[] factors = expr.split("[*/]");
        char[] ops = expr.replaceAll("[^*/]", "").toCharArray();

        String left = factors[0].trim();
        boolean usedAX = false;

        for (int i = 0; i < ops.length; i++) {
            String right = factors[i + 1].trim();
            char op = ops[i];

            if (op == '*') {
                if (!"AL".equals(left)) {
                    assemblyCode.append("    MOV AL, ").append(left).append("\n");
                }
                assemblyCode.append("    MOV BL, ").append(right).append("\n");
                assemblyCode.append("    MUL BL\n");
                usedAX = true;
                left = "AX";
            } else {
                if (!"AX".equals(left)) {
                    assemblyCode.append("    MOV AX, ").append(left).append("\n");
                }
                assemblyCode.append("    MOV BL, ").append(right).append("\n");
                assemblyCode.append("    DIV BL\n");
                usedAX = true;
                left = "AX";
            }
        }

        return usedAX ? "AX_RESULT" : left;
    }

    private void generateWhile(String line) {
        String loopLabel = "LOOP_START" + labelCounter;
        String endLabel = "END_LOOP" + labelCounter;
        labelCounter++;
    
        assemblyCode.append(loopLabel).append(":\n");
    
        line = line.substring(line.indexOf('(') + 1, line.lastIndexOf(')'));
        String[] conditions = line.split("&&");
    
        for (String cond : conditions) {
            cond = cond.trim();
            String[] parts;
            String jump = "";
    
            if (cond.contains("<")) {
                parts = cond.split("<");
                jump = "LT"; // CORRECTO: salir si NO se cumple <
            } else if (cond.contains(">")) {
                parts = cond.split(">");
                jump = "GT"; // CORRECTO: salir si NO se cumple >
            } else {
                continue;
            }
    
            String left = parts[0].trim();
            String right = parts[1].trim();
    
            assemblyCode.append("    MOV AX, ").append(left).append("\n");
            assemblyCode.append("    CMP AX, ").append(right).append("\n");
            assemblyCode.append("    ").append(jump).append(" ").append(endLabel).append("\n");
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
