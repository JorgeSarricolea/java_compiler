package src.compiler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Optimizer {
    private List<String> optimizedCode;
    private Set<String> usedVariables;
    private Set<String> declaredVariables;
    private Set<String> temporaryVariables;
    private Set<String> assignedVariables;
    
    public Optimizer() {
        this.optimizedCode = new ArrayList<>();
        this.usedVariables = new HashSet<>();
        this.declaredVariables = new HashSet<>();
        this.temporaryVariables = new HashSet<>();
        this.assignedVariables = new HashSet<>();
    }
    
    /**
     * Optimizes the given code by reorganizing dependent instructions
     * @param code The code to optimize
     * @return The optimized code as a list of strings
     */
    public List<String> optimize(String code) {
        optimizedCode.clear();
        usedVariables.clear();
        declaredVariables.clear();
        temporaryVariables.clear();
        assignedVariables.clear();
        
        String[] lines = code.split("\n");
        List<String> declarations = new ArrayList<>();
        List<String> otherLines = new ArrayList<>();
        
        // Primera pasada: identificar variables declaradas y no inicializadas
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            
            if (line.matches("(IntegerType|FloatType|StringType)\\s+.*")) {
                String varName = extractVariableName(line);
                if (!varName.isEmpty()) {
                    declaredVariables.add(varName);
                }
            }
        }
        
        // Segunda pasada: analizar uso de variables y detectar inicializaciones
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;
            
            // Si es una asignación, verificar si la variable se usa después
            if (line.contains("=")) {
                String targetVar = extractTargetVariable(line);
                if (declaredVariables.contains(targetVar)) {
                    assignedVariables.add(targetVar);
                    // Buscar si la variable se usa en alguna línea después de su asignación
                    boolean isUsed = false;
                    for (int j = i + 1; j < lines.length; j++) {
                        String nextLine = lines[j].trim();
                        if (nextLine.isEmpty()) continue;
                        
                        // Si la línea contiene la variable y no es su propia asignación
                        if (nextLine.contains(targetVar) && !nextLine.startsWith(targetVar + " =")) {
                            isUsed = true;
                            break;
                        }
                    }
                    
                    if (isUsed) {
                        usedVariables.add(targetVar);
                    }
                }
            }
            
            // Analizar uso en condiciones de while y su cuerpo
            if (line.startsWith("while")) {
                String condition = extractCondition(line);
                // Marcar todas las variables en la condición como usadas
                String[] conditionParts = condition.split("&&|\\|\\|");
                for (String part : conditionParts) {
                    part = part.trim();
                    // Extraer variables de cada parte de la condición
                    Pattern varPattern = Pattern.compile("\\b\\w+\\b");
                    Matcher varMatcher = varPattern.matcher(part);
                    while (varMatcher.find()) {
                        String var = varMatcher.group();
                        if (declaredVariables.contains(var)) {
                            usedVariables.add(var);
                        }
                    }
                }
                
                // Analizar el cuerpo del while
                int j = i + 1;
                while (j < lines.length && !lines[j].trim().equals("}")) {
                    String bodyLine = lines[j].trim();
                    if (!bodyLine.isEmpty()) {
                        Pattern varPattern = Pattern.compile("\\b\\w+\\b");
                        Matcher varMatcher = varPattern.matcher(bodyLine);
                        while (varMatcher.find()) {
                            String var = varMatcher.group();
                            if (declaredVariables.contains(var)) {
                                usedVariables.add(var);
                            }
                        }
                    }
                    j++;
                }
            }
        }
        
        // Tercera pasada: procesar el código optimizado
        boolean addedPrecomputed = false;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;
            
            if (line.matches("(IntegerType|FloatType|StringType)\\s+.*")) {
                String varName = extractVariableName(line);
                // Mantener solo las declaraciones de variables que se usan
                if (usedVariables.contains(varName) || assignedVariables.contains(varName)) {
                    declarations.add(getIndentation(lines[i]) + line);
                }
            } else {
                // Si es un while, procesar su bloque
                if (line.startsWith("while")) {
                    // Optimizar la condición del while
                    String optimizedCondition = optimizeWhileCondition(lines, i);
                    otherLines.add(getIndentation(lines[i]) + "while (" + optimizedCondition + ") {");
                    i++; // Saltar la llave de apertura
                    
                    // Procesar el bloque del while
                    List<String> blockLines = new ArrayList<>();
                    while (i < lines.length && !lines[i].trim().equals("}")) {
                        blockLines.add(lines[i]);
                        i++;
                    }
                    
                    // Optimizar el bloque
                    List<String> optimizedBlock = optimizeBlock(blockLines);
                    otherLines.addAll(optimizedBlock);
                    otherLines.add(getIndentation(lines[i]) + "}");
                } else {
                    // Detectar y precomputar expresiones constantes
                    Pattern constExprPattern = Pattern.compile("\\b(\\d+\\s*[+\\-*/]\\s*\\d+)+\\b");
                    Matcher matcher = constExprPattern.matcher(line);
                    while (matcher.find()) {
                        String constExpr = matcher.group();
                        try {
                            int result = evaluateExpression(constExpr);
                            String varName = "JSJa" + (declarations.size() + 1);
                            declarations.add("IntegerType " + varName + ";");
                            otherLines.add(varName + " = " + result + ";");
                            line = line.replace(constExpr, varName);
                        } catch (Exception e) {
                            // Si no se puede evaluar, continuar
                        }
                    }
                    otherLines.add(getIndentation(lines[i]) + line);
                }
            }
        }
        
        // Combinar declaraciones y líneas optimizadas
        optimizedCode.addAll(declarations);
        optimizedCode.addAll(otherLines);
        
        // Verificar si el código optimizado es diferente del original
        List<String> originalLines = new ArrayList<>();
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                originalLines.add(line);
            }
        }
        
        // Si no hay cambios significativos, devolver el código original
        if (optimizedCode.size() == originalLines.size()) {
            boolean hasChanges = false;
            for (int i = 0; i < optimizedCode.size(); i++) {
                if (!optimizedCode.get(i).trim().equals(originalLines.get(i).trim())) {
                    hasChanges = true;
                    break;
                }
            }
            if (!hasChanges) {
                return originalLines;
            }
        }
        
        return optimizedCode;
    }
    
    /**
     * Optimiza la condición de un while
     * @param lines Todas las líneas de código
     * @param whileIndex Índice de la línea del while
     * @return Condición optimizada
     */
    private String optimizeWhileCondition(String[] lines, int whileIndex) {
        // Buscar asignaciones de variables booleanas antes del while
        List<String> booleanAssignments = new ArrayList<>();
        for (int i = whileIndex - 1; i >= 0; i--) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;
            
            if (line.contains("=")) {
                String[] parts = line.split("=");
                String expression = parts[1].trim();
                
                // Si la expresión es una condición booleana
                if (expression.contains("<") || expression.contains(">") || 
                    expression.contains("==") || expression.contains("!=")) {
                    booleanAssignments.add(0, line);
                }
            }
        }
        
        // Extraer la condición original del while
        String originalCondition = extractCondition(lines[whileIndex]);
        
        // Si hay asignaciones booleanas, reemplazar las variables con sus expresiones
        for (String assignment : booleanAssignments) {
            String[] parts = assignment.split("=");
            String targetVar = parts[0].trim();
            String expression = parts[1].trim();
            
            // Reemplazar la variable en la condición
            originalCondition = originalCondition.replace(targetVar, expression);
        }
        
        return originalCondition;
    }
    
    /**
     * Extrae el nombre de la variable de una declaración
     * @param declaration Línea de declaración
     * @return Nombre de la variable
     */
    private String extractVariableName(String declaration) {
        Pattern pattern = Pattern.compile("(IntegerType|FloatType|StringType)\\s+(\\w+)");
        Matcher matcher = pattern.matcher(declaration);
        if (matcher.find()) {
            return matcher.group(2);
        }
        return "";
    }
    
    /**
     * Extrae la condición de un while
     * @param line Línea con el while
     * @return Condición del while
     */
    private String extractCondition(String line) {
        Pattern pattern = Pattern.compile("\\((.*?)\\)");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "";
    }
    
    /**
     * Obtiene la indentación de una línea
     * @param line Línea de código
     * @return String con la indentación
     */
    private String getIndentation(String line) {
        int i = 0;
        while (i < line.length() && Character.isWhitespace(line.charAt(i))) {
            i++;
        }
        return line.substring(0, i);
    }
    
    /**
     * Optimiza un bloque de código
     * @param blockLines Lista de líneas del bloque
     * @return Lista de líneas optimizadas
     */
    private List<String> optimizeBlock(List<String> blockLines) {
        List<String> optimized = new ArrayList<>();
        
        for (int i = 0; i < blockLines.size(); i++) {
            String line = blockLines.get(i);
            String indentation = getIndentation(line);
            line = line.trim();
            
            // Si hay una siguiente línea
            if (i + 1 < blockLines.size()) {
                String nextLine = blockLines.get(i + 1);
                nextLine = nextLine.trim();
                
                // Verificar si son instrucciones dependientes
                if (isDependentInstructions(line, nextLine)) {
                    // Optimizar las instrucciones dependientes
                    String optimizedLine = optimizeDependentInstructions(line, nextLine);
                    optimized.add(indentation + optimizedLine);
                    i++; // Saltar la siguiente línea ya que la hemos optimizado
                } else {
                    optimized.add(indentation + line);
                }
            } else {
                optimized.add(indentation + line);
            }
        }
        
        return optimized;
    }
    
    /**
     * Checks if two lines contain dependent instructions that can be optimized
     * @param line1 First line of code
     * @param line2 Second line of code
     * @return true if the lines contain dependent instructions that can be optimized
     */
    private boolean isDependentInstructions(String line1, String line2) {
        // Pattern for variable assignment
        Pattern assignPattern = Pattern.compile("(\\w+)\\s*=\\s*(.+);?");
        Matcher matcher1 = assignPattern.matcher(line1);
        Matcher matcher2 = assignPattern.matcher(line2);
        
        if (matcher1.find() && matcher2.find()) {
            String targetVar1 = matcher1.group(1);
            String expression2 = matcher2.group(2);
            
            // Verificar si la segunda línea usa la variable de la primera
            return expression2.contains(targetVar1);
        }
        
        return false;
    }
    
    /**
     * Optimizes dependent instructions by combining them into a single operation
     * @param line1 First line of code
     * @param line2 Second line of code
     * @return The optimized line of code
     */
    private String optimizeDependentInstructions(String line1, String line2) {
        // Pattern for variable assignment
        Pattern assignPattern = Pattern.compile("(\\w+)\\s*=\\s*(.+);?");
        Matcher matcher1 = assignPattern.matcher(line1);
        Matcher matcher2 = assignPattern.matcher(line2);
        
        if (matcher1.find() && matcher2.find()) {
            String targetVar1 = matcher1.group(1);
            String expression1 = matcher1.group(2);
            String targetVar2 = matcher2.group(1);
            String expression2 = matcher2.group(2);
            
            // Reemplazar la variable temporal en expression2 con expression1
            String optimizedExpression = expression2.replace(targetVar1, expression1);
            
            // Retornar la asignación optimizada con un solo punto y coma
            return targetVar2 + " = " + optimizedExpression + ";";
        }
        
        return line1;
    }
    
    /**
     * Extrae la variable objetivo de una asignación
     * @param line Línea de código
     * @return Nombre de la variable objetivo
     */
    private String extractTargetVariable(String line) {
        Pattern pattern = Pattern.compile("(\\w+)\\s*=");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }
    
    /**
     * Evaluates a simple arithmetic expression
     * @param expression The expression to evaluate
     * @return The result of the evaluation
     */
    private int evaluateExpression(String expression) throws Exception {
        // Simple evaluation logic for demonstration purposes
        // This should be replaced with a proper expression parser
        return (int) new javax.script.ScriptEngineManager().getEngineByName("JavaScript").eval(expression);
    }
    
    public static void main(String[] args) {
        Optimizer optimizer = new Optimizer();
        String code = "IntegerType JSJa1, JSJa2;\n" +
                      "JSJa1 = 10;\n" +
                      "JSJa2 = 5;\n" +
                      "\n" +
                      "while (JSJa1 < 20 && JSJa1 > 0) {\n" +
                      "    JSJa1 = JSJa1 + 2 * 3 - 1;\n" +
                      "}";
        List<String> optimizedCode = optimizer.optimize(code);
        for (String line : optimizedCode) {
            System.out.println(line);
        }
    }
} 