package src.compiler;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TripletGenerator {
    private List<TriploEntry> triploEntries;
    private Stack<Integer> conditionPositions; // Para almacenar posiciones de inicio de condición
    private Stack<Integer> pendingJumps;      // Para almacenar posiciones de saltos que necesitan actualizarse

    public TripletGenerator() {
        this.triploEntries = new ArrayList<>();
        this.conditionPositions = new Stack<>();
        this.pendingJumps = new Stack<>();
    }

    /**
     * Genera el triplo para un código dado
     * @param code Código fuente a procesar
     */
    public void generateTriplo(String code) {
        // Reiniciar variables
        triploEntries.clear();
        conditionPositions.clear();
        pendingJumps.clear();
        
        // Analizar el código línea por línea
        String[] lines = code.split("\n");
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;
            
            // Omitir las declaraciones de variables sin inicialización
            if (line.matches("(IntegerType|FloatType|StringType)\\s+.*") && !line.contains("=")) {
                continue;
            }
            
            // Procesar asignaciones
            if (line.contains("=") && !isConditional(line)) {
                line = line.replace(";", ""); // Eliminar punto y coma
                processAssignment(line);
            }
            // Procesar estructuras de control - while
            else if (line.startsWith("while")) {
                // Extraer la condición del while
                String condition = extractCondition(line);
                
                // Guardar la posición actual para el JMP de retorno
                int conditionStartPos = triploEntries.size() + 1;
                conditionPositions.push(conditionStartPos);
                
                // Procesar condición
                if (hasAndOr(condition)) {
                    processMixedConditions(condition);
                } else if (condition.contains("||")) {
                    String[] parts = condition.split("\\|\\|");
                    processOrConditions(parts);
                } else if (condition.contains("&&")) {
                    String[] parts = condition.split("&&");
                    processAndConditions(parts);
                } else {
                    processSimpleCondition(condition);
                }
            }
            // Procesar cierre de bloques
            else if (line.equals("}")) {
                if (!conditionPositions.isEmpty()) {
                    int conditionStartPos = conditionPositions.pop();
                    
                    // Añadir JMP para volver a la evaluación de la condición
                    triploEntries.add(new TriploEntry("", "JMP", String.valueOf(conditionStartPos)));
                    
                    // La posición después del JMP
                    int afterJmpPos = triploEntries.size() + 1;
                    
                    // Actualizar todos los saltos pendientes para que apunten a esta posición
                    while (!pendingJumps.isEmpty()) {
                        int pendingJumpPos = pendingJumps.pop();
                        TriploEntry entry = triploEntries.get(pendingJumpPos - 1);
                        entry.operador = String.valueOf(afterJmpPos);
                    }
                }
            }
        }
        
        // Añadir la marca de fin
        triploEntries.add(new TriploEntry("", "end", ""));
    }
    
    private boolean isConditional(String line) {
        return line.contains("<") || line.contains(">") || 
               line.contains("==") || line.contains("!=") || 
               line.contains("<=") || line.contains(">=");
    }
    
    private String extractCondition(String line) {
        Pattern pattern = Pattern.compile("\\((.*?)\\)");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "";
    }
    
    private void processAssignment(String line) {
        String[] parts = line.split("=", 2);
        
        if (parts.length != 2) return;
        
        String target = parts[0].trim();
        String expression = parts[1].trim();
        
        // Expresión simple (un valor literal o una variable)
        if (!expression.contains("+") && !expression.contains("-") && 
            !expression.contains("*") && !expression.contains("/")) {
            
            // Crear variable temporal T1 para el valor
            triploEntries.add(new TriploEntry("T1", expression, "="));
            
            // Asignar variable temporal al objetivo
            triploEntries.add(new TriploEntry(target, "T1", "="));
            
            return;
        }
        
        // Procesar expresión aritmética con múltiples operadores y respetando jerarquía
        processArithmeticExpression(target, expression);
    }
    
    /**
     * Procesa una expresión aritmética respetando la jerarquía de operaciones
     * @param target Variable objetivo donde se asignará el resultado
     * @param expression Expresión aritmética a procesar
     */
    private void processArithmeticExpression(String target, String expression) {
        // Primero procesamos multiplicación y división
        // Buscar términos separados por + o -
        String[] sumTerms = expression.split("(?=[+-])");
        
        // Contador para variables temporales
        int tempVarCount = 1;
        String[] processedTerms = new String[sumTerms.length];
        
        // Procesar cada término (puede contener * o /)
        for (int i = 0; i < sumTerms.length; i++) {
            String term = sumTerms[i].trim();
            
            // Si el término comienza con + o -, extraerlo
            char operator = '+';
            if (term.startsWith("+") || term.startsWith("-")) {
                operator = term.charAt(0);
                term = term.substring(1).trim();
            }
            
            // Verificar si el término contiene multiplicación o división
            if (term.contains("*") || term.contains("/")) {
                // Determinar el operador
                String mulOperator = term.contains("*") ? "*" : "/";
                String[] factors = term.split("\\" + mulOperator);
                
                if (factors.length == 2) {
                    String left = factors[0].trim();
                    String right = factors[1].trim();
                    
                    // Generar triplo para la multiplicación/división
                    String resultVar = "T" + tempVarCount++;
                    triploEntries.add(new TriploEntry(resultVar, left, "="));
                    triploEntries.add(new TriploEntry(resultVar, right, mulOperator));
                    
                    // Guardar la variable temporal como resultado de este término
                    processedTerms[i] = (operator == '-' ? "-" : "") + resultVar;
                } else {
                    // Si no se puede dividir correctamente, usar el término original
                    processedTerms[i] = (operator == '-' ? "-" : "") + term;
                }
            } else {
                // Término simple, guardar como está
                processedTerms[i] = (operator == '-' ? "-" : "") + term;
            }
        }
        
        // Ahora procesamos las sumas y restas
        if (processedTerms.length == 1) {
            // Si solo hay un término después de procesar * y /, asignarlo directamente
            String term = processedTerms[0];
            if (term.startsWith("-")) {
                // Manejar el caso de negativo
                triploEntries.add(new TriploEntry("T" + tempVarCount, "0", "="));
                triploEntries.add(new TriploEntry("T" + tempVarCount, term.substring(1), "-"));
                triploEntries.add(new TriploEntry(target, "T" + tempVarCount, "="));
            } else {
                // Asignar directamente si no es negativo
                if (term.startsWith("+")) {
                    term = term.substring(1);
                }
                triploEntries.add(new TriploEntry("T" + tempVarCount, term, "="));
                triploEntries.add(new TriploEntry(target, "T" + tempVarCount, "="));
            }
        } else {
            // Procesar múltiples términos con sumas y restas
            String resultVar = "T" + tempVarCount++;
            
            // El primer término
            String firstTerm = processedTerms[0];
            if (firstTerm.startsWith("-")) {
                // Si el primer término es negativo, comenzar con 0 y restar
                triploEntries.add(new TriploEntry(resultVar, "0", "="));
                triploEntries.add(new TriploEntry(resultVar, firstTerm.substring(1), "-"));
            } else {
                // Si es positivo, asignar directamente
                if (firstTerm.startsWith("+")) {
                    firstTerm = firstTerm.substring(1);
                }
                triploEntries.add(new TriploEntry(resultVar, firstTerm, "="));
            }
            
            // Procesar términos restantes
            for (int i = 1; i < processedTerms.length; i++) {
                String term = processedTerms[i];
                char operator = '+';
                
                if (term.startsWith("-")) {
                    operator = '-';
                    term = term.substring(1);
                } else if (term.startsWith("+")) {
                    term = term.substring(1);
                }
                
                triploEntries.add(new TriploEntry(resultVar, term, String.valueOf(operator)));
            }
            
            // Asignar el resultado final
            triploEntries.add(new TriploEntry(target, resultVar, "="));
        }
    }
    
    private void processSimpleCondition(String condition) {
        // Determinar el operador relacional
        String operator = "";
        String[] operands = null;
        
        if (condition.contains("!=")) {
            operator = "!=";
            operands = condition.split("!=");
        } else if (condition.contains("==")) {
            operator = "==";
            operands = condition.split("==");
        } else if (condition.contains("<=")) {
            operator = "<=";
            operands = condition.split("<=");
        } else if (condition.contains(">=")) {
            operator = ">=";
            operands = condition.split(">=");
        } else if (condition.contains("<")) {
            operator = "<";
            operands = condition.split("<");
        } else if (condition.contains(">")) {
            operator = ">";
            operands = condition.split(">");
        }
        
        if (operator.isEmpty() || operands == null || operands.length != 2) return;
        
        // Procesar operandos
        String left = operands[0].trim();
        String right = operands[1].trim();
        
        // Generar el triplo para cargar valores
        triploEntries.add(new TriploEntry("T1", right, "="));
        triploEntries.add(new TriploEntry("T2", left, "="));
        triploEntries.add(new TriploEntry("T2", "T1", operator));
        
        // Calcular posición del cuerpo del bloque (luego del salto condicional)
        int bodyStartPos = triploEntries.size() + 3;
        
        // Añadir los saltos condicionales
        triploEntries.add(new TriploEntry("TR1", "true", String.valueOf(bodyStartPos)));
        
        // Registro del salto en falso para actualizar cuando se cierre el bloque
        int falseJumpPos = triploEntries.size() + 1;
        triploEntries.add(new TriploEntry("TR1", "false", "?"));
        pendingJumps.push(falseJumpPos);
    }
    
    private void processOrConditions(String[] conditions) {
        // Para OR: si cualquiera es verdadera, se ejecuta el cuerpo
        
        // Procesar primera condición
        String firstCondition = conditions[0].trim();
        
        // Analizar el tipo de operador
        String operator = "";
        String[] operands = null;
        
        if (firstCondition.contains(">")) {
            operator = ">";
            operands = firstCondition.split(">");
        } else if (firstCondition.contains("<")) {
            operator = "<";
            operands = firstCondition.split("<");
        } else if (firstCondition.contains("!=")) {
            operator = "!=";
            operands = firstCondition.split("!=");
        } else if (firstCondition.contains("==")) {
            operator = "==";
            operands = firstCondition.split("==");
        }
        
        if (operator.isEmpty() || operands == null || operands.length != 2) return;
        
        // Evaluar primera condición
        String left = operands[0].trim();
        String right = operands[1].trim();
        
        triploEntries.add(new TriploEntry("T1", right, "="));
        triploEntries.add(new TriploEntry("T2", left, "="));
        triploEntries.add(new TriploEntry("T2", "T1", operator));
        
        // Si la primera condición es verdadera, saltamos al cuerpo
        // Guardamos esta posición para actualizar después
        int bodyStartPos = -1; // Se calculará más adelante
        int trueJumpPos = triploEntries.size() + 1;
        triploEntries.add(new TriploEntry("TR1", "true", "?")); // Se actualizará después
        
        // Si es falsa, verificamos la segunda condición
        int falseJumpPos = triploEntries.size() + 1;
        triploEntries.add(new TriploEntry("TR1", "false", String.valueOf(falseJumpPos + 1)));
        
        // Evaluar segunda condición
        String secondCondition = conditions[1].trim();
        
        operator = "";
        operands = null;
        
        if (secondCondition.contains("<")) {
            operator = "<";
            operands = secondCondition.split("<");
        } else if (secondCondition.contains(">")) {
            operator = ">";
            operands = secondCondition.split(">");
        } else if (secondCondition.contains("!=")) {
            operator = "!=";
            operands = secondCondition.split("!=");
        } else if (secondCondition.contains("==")) {
            operator = "==";
            operands = secondCondition.split("==");
        }
        
        if (!operator.isEmpty() && operands != null && operands.length == 2) {
            left = operands[0].trim();
            right = operands[1].trim();
            
            triploEntries.add(new TriploEntry("T3", right, "="));
            triploEntries.add(new TriploEntry("T4", left, "="));
            triploEntries.add(new TriploEntry("T4", "T3", operator));
            
            // La posición donde comienza el cuerpo del bloque
            bodyStartPos = triploEntries.size() + 3;
            
            // Actualizar el salto verdadero de la primera condición
            TriploEntry trueJumpEntry = triploEntries.get(trueJumpPos - 1);
            trueJumpEntry.operador = String.valueOf(bodyStartPos);
            
            // Añadir saltos para la segunda condición
            triploEntries.add(new TriploEntry("TR1", "true", String.valueOf(bodyStartPos)));
            
            // Si la segunda condición también es falsa, saltar fuera del bloque
            int secondFalseJumpPos = triploEntries.size() + 1;
            triploEntries.add(new TriploEntry("TR1", "false", "?"));
            pendingJumps.push(secondFalseJumpPos);
        }
    }
    
    private void processAndConditions(String[] conditions) {
        // Para AND: ambas condiciones deben ser verdaderas
        
        // Procesar primera condición
        String firstCondition = conditions[0].trim();
        
        // Analizar el tipo de operador
        String operator = "";
        String[] operands = null;
        
        if (firstCondition.contains(">")) {
            operator = ">";
            operands = firstCondition.split(">");
        } else if (firstCondition.contains("<")) {
            operator = "<";
            operands = firstCondition.split("<");
        } else if (firstCondition.contains("!=")) {
            operator = "!=";
            operands = firstCondition.split("!=");
        } else if (firstCondition.contains("==")) {
            operator = "==";
            operands = firstCondition.split("==");
        }
        
        if (operator.isEmpty() || operands == null || operands.length != 2) return;
        
        // Evaluar primera condición
        String left = operands[0].trim();
        String right = operands[1].trim();
        
        triploEntries.add(new TriploEntry("T1", right, "="));
        triploEntries.add(new TriploEntry("T2", left, "="));
        triploEntries.add(new TriploEntry("T2", "T1", operator));
        
        // Si la primera condición es falsa, saltar fuera del bloque
        int falseJumpOutPos = triploEntries.size() + 1;
        triploEntries.add(new TriploEntry("TR1", "true", String.valueOf(falseJumpOutPos + 2)));
        triploEntries.add(new TriploEntry("TR1", "false", "?")); // Se actualizará cuando cerremos el bloque
        pendingJumps.push(falseJumpOutPos + 1);
        
        // Evaluar segunda condición
        String secondCondition = conditions[1].trim();
        
        operator = "";
        operands = null;
        
        if (secondCondition.contains("<")) {
            operator = "<";
            operands = secondCondition.split("<");
        } else if (secondCondition.contains(">")) {
            operator = ">";
            operands = secondCondition.split(">");
        } else if (secondCondition.contains("!=")) {
            operator = "!=";
            operands = secondCondition.split("!=");
        } else if (secondCondition.contains("==")) {
            operator = "==";
            operands = secondCondition.split("==");
        }
        
        if (!operator.isEmpty() && operands != null && operands.length == 2) {
            left = operands[0].trim();
            right = operands[1].trim();
            
            triploEntries.add(new TriploEntry("T3", right, "="));
            triploEntries.add(new TriploEntry("T4", left, "="));
            triploEntries.add(new TriploEntry("T4", "T3", operator));
            
            // Calcular posición del cuerpo del bloque
            int bodyStartPos = triploEntries.size() + 3;
            
            // Añadir saltos para la segunda condición
            triploEntries.add(new TriploEntry("TR1", "true", String.valueOf(bodyStartPos)));
            
            // Si la segunda condición es falsa, saltar fuera del bloque
            int secondFalseJumpPos = triploEntries.size() + 1;
            triploEntries.add(new TriploEntry("TR1", "false", "?"));
            pendingJumps.push(secondFalseJumpPos);
        }
    }
    
    private boolean hasAndOr(String condition) {
        return condition.contains("&&") && condition.contains("||");
    }
    
    private void processMixedConditions(String condition) {
        // Primero procesamos basándonos en la precedencia de OR (dividiéndolo primero)
        String[] orParts = condition.split("\\|\\|");
        
        // Evaluar la primera parte (puede contener AND)
        String firstPart = orParts[0].trim();
        int firstPartJumpPos = -1;
        
        if (firstPart.contains("&&")) {
            // Si contiene AND, procesar como condición compuesta AND
            String[] andParts = firstPart.split("&&");
            // Guardar la posición para salto si es verdadero
            firstPartJumpPos = processAndSubcondition(andParts);
        } else {
            // Si es simple, procesar como condición simple
            firstPartJumpPos = processConditionPart(firstPart, true, false);
        }
        
        // Posición para actualizar posteriormente
        int bodyStartPos = -1;
        
        // Procesar la segunda parte (puede contener AND)
        String secondPart = orParts[1].trim();
        
        if (secondPart.contains("&&")) {
            // Si contiene AND, procesar como condición compuesta AND
            String[] andParts = secondPart.split("&&");
            bodyStartPos = processAndSubcondition(andParts);
        } else {
            // Si es simple, procesar como condición simple
            bodyStartPos = processConditionPart(secondPart, false, true);
        }
        
        // Actualizar el salto de la primera parte si es verdadera
        if (firstPartJumpPos > 0) {
            TriploEntry entry = triploEntries.get(firstPartJumpPos - 1);
            entry.operador = String.valueOf(bodyStartPos);
        }
    }
    
    private int processAndSubcondition(String[] conditions) {
        // Procesar primera condición
        String firstCondition = conditions[0].trim();
        processConditionPart(firstCondition, false, false);
        
        // Procesar segunda condición
        String secondCondition = conditions[1].trim();
        return processConditionPart(secondCondition, false, true);
    }
    
    private int processConditionPart(String condition, boolean isFirstOrPart, boolean isLastPart) {
        // Determinar el operador relacional
        String operator = "";
        String[] operands = null;
        
        if (condition.contains("!=")) {
            operator = "!=";
            operands = condition.split("!=");
        } else if (condition.contains("==")) {
            operator = "==";
            operands = condition.split("==");
        } else if (condition.contains("<=")) {
            operator = "<=";
            operands = condition.split("<=");
        } else if (condition.contains(">=")) {
            operator = ">=";
            operands = condition.split(">=");
        } else if (condition.contains("<")) {
            operator = "<";
            operands = condition.split("<");
        } else if (condition.contains(">")) {
            operator = ">";
            operands = condition.split(">");
        }
        
        if (operator.isEmpty() || operands == null || operands.length != 2) return -1;
        
        // Procesar operandos
        String left = operands[0].trim();
        String right = operands[1].trim();
        
        // Variables temporales diferentes según la posición
        String tempVar1 = isFirstOrPart ? "T1" : (isLastPart ? "T5" : "T3");
        String tempVar2 = isFirstOrPart ? "T2" : (isLastPart ? "T6" : "T4");
        String resultVar = isFirstOrPart ? "TR1" : (isLastPart ? "TR3" : "TR2");
        
        // Generar triplo para valores
        triploEntries.add(new TriploEntry(tempVar1, right, "="));
        triploEntries.add(new TriploEntry(tempVar2, left, "="));
        triploEntries.add(new TriploEntry(tempVar2, tempVar1, operator));
        
        // Calcular posición del cuerpo o siguiente condición
        int nextPos = triploEntries.size() + 3;
        
        if (isFirstOrPart) {
            // Si es la primera parte del OR y es verdadera, saltar al cuerpo
            int trueJumpPos = triploEntries.size() + 1;
            triploEntries.add(new TriploEntry(resultVar, "true", "?")); // Se actualizará después
            
            // Si es falsa, evaluar siguiente condición
            triploEntries.add(new TriploEntry(resultVar, "false", String.valueOf(nextPos)));
            
            return trueJumpPos;
        } else if (isLastPart) {
            // Si es la última parte y es verdadera, ejecutar cuerpo
            int bodyStartPos = triploEntries.size() + 3;
            triploEntries.add(new TriploEntry(resultVar, "true", String.valueOf(bodyStartPos)));
            
            // Si es falsa, saltar fuera del bloque
            int falseJumpPos = triploEntries.size() + 1;
            triploEntries.add(new TriploEntry(resultVar, "false", "?"));
            pendingJumps.push(falseJumpPos);
            
            return bodyStartPos;
        } else {
            // Si no es ni primera ni última, es una condición intermedia
            // Si es verdadera, continuar con la siguiente
            triploEntries.add(new TriploEntry(resultVar, "true", String.valueOf(nextPos)));
            
            // Si es falsa, saltar fuera del bloque
            int falseJumpPos = triploEntries.size() + 1;
            triploEntries.add(new TriploEntry(resultVar, "false", "?"));
            pendingJumps.push(falseJumpPos);
            
            return -1;
        }
    }
    
    /**
     * Genera un archivo de texto con el triplo generado
     * @param filePath Ruta del archivo a generar
     * @throws IOException Si hay un error al escribir el archivo
     */
    public void saveToFile(String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("Line  Data Object     Data Source     Operator  \n");
            writer.write("--------------------------------------------------\n");
            
            int rowNumber = 1;
            for (TriploEntry entry : triploEntries) {
                writer.write(String.format("%-8d %-15s %-15s %-10s\n", 
                    rowNumber++, 
                    entry.datoObjeto, 
                    entry.datoFuente, 
                    entry.operador));
            }
        }
    }
    
    /**
     * Obtiene el triplo como un string formateado
     * @return String con el triplo formateado
     */
    public String getTriploAsString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Renglón  Dato Objeto     Dato Fuente     Operador  \n");
        sb.append("--------------------------------------------------\n");
        
        int rowNumber = 1;
        for (TriploEntry entry : triploEntries) {
            sb.append(String.format("%-8d %-15s %-15s %-10s\n", 
                rowNumber++, 
                entry.datoObjeto, 
                entry.datoFuente, 
                entry.operador));
        }
        
        return sb.toString();
    }
    
    /**
     * Clase interna para representar una entrada en el triplo
     */
    private class TriploEntry {
        String datoObjeto;
        String datoFuente;
        String operador;
        
        public TriploEntry(String datoObjeto, String datoFuente, String operador) {
            this.datoObjeto = datoObjeto;
            this.datoFuente = datoFuente;
            this.operador = operador;
        }
    }
}