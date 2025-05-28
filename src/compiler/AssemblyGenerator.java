package src.compiler;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AssemblyGenerator {
    private List<String> assemblyCode;
    private int labelCounter;
    
    public AssemblyGenerator() {
        this.assemblyCode = new ArrayList<>();
        this.labelCounter = 0;
    }
    
    public void generateAssembly(List<String> optimizedCode) {
        assemblyCode.clear();
        labelCounter = 0;
        
        // Primero, identificar todas las variables declaradas
        for (String line : optimizedCode) {
            if (line.matches("(IntegerType|FloatType|StringType)\\s+.*")) {
                String varName = extractVariableName(line);
                // Inicializar la variable con un valor por defecto
                assemblyCode.add("        ; Inicialización de " + varName);
                assemblyCode.add("        MOV AX, 0");
                assemblyCode.add("        MOV [" + varName + "], AX");
            }
        }
        
        // Luego procesar el resto del código
        for (int i = 0; i < optimizedCode.size(); i++) {
            String line = optimizedCode.get(i).trim();
            if (line.isEmpty()) continue;
            
            // Procesar declaraciones de variables
            if (line.matches("(IntegerType|FloatType|StringType)\\s+.*")) {
                continue; // Ya procesamos las declaraciones arriba
            }
            
            // Procesar asignaciones
            if (line.contains("=")) {
                processAssignment(line);
            }
            
            // Procesar while
            if (line.startsWith("while")) {
                processWhile(line, optimizedCode, i);
                // Saltar el bloque del while
                while (i < optimizedCode.size() && !optimizedCode.get(i).trim().equals("}")) {
                    i++;
                }
            }
        }
    }
    
    private void processAssignment(String line) {
        String[] parts = line.split("=");
        String target = parts[0].trim();
        String value = parts[1].trim().replace(";", ""); // Eliminar punto y coma
        
        // Si es un número (valor inmediato)
        if (value.matches("\\d+")) {
            assemblyCode.add("        MOV AX, " + value);
            assemblyCode.add("        MOV [" + target + "], AX");
        }
        // Si es una variable
        else if (value.matches("\\w+")) {
            assemblyCode.add("        MOV AX, [" + value + "]");
            assemblyCode.add("        MOV [" + target + "], AX");
        }
        // Si es una operación aritmética
        else if (value.contains("+") || value.contains("-")) {
            String[] operands = value.split("[+-]");
            String operator = value.contains("+") ? "+" : "-";
            
            // Procesar primer operando
            String op1 = operands[0].trim();
            if (op1.matches("\\d+")) {
                assemblyCode.add("        MOV AX, " + op1);
            } else {
                assemblyCode.add("        MOV AX, [" + op1 + "]");
            }
            
            // Procesar segundo operando
            String op2 = operands[1].trim();
            if (op2.matches("\\d+")) {
                assemblyCode.add("        MOV BX, " + op2);
            } else {
                assemblyCode.add("        MOV BX, [" + op2 + "]");
            }
            
            if (operator.equals("+")) {
                assemblyCode.add("        ADD AX, BX");
            } else {
                assemblyCode.add("        SUB AX, BX");
            }
            assemblyCode.add("        MOV [" + target + "], AX");
        }
    }
    
    private void processWhile(String line, List<String> code, int startIndex) {
        String condition = extractCondition(line);
        String labelStart = "START" + labelCounter++;
        String labelBody = "BODY" + labelCounter++;
        String labelEnd = "END" + labelCounter++;
        
        assemblyCode.add(labelStart + ":");
        
        // Procesar condición
        if (condition.contains("&&")) {
            processAndCondition(condition, labelBody, labelEnd);
        } else if (condition.contains("||")) {
            processOrCondition(condition, labelBody, labelEnd);
        } else {
            processSimpleCondition(condition, labelBody, labelEnd);
        }
        
        assemblyCode.add(labelBody + ":");
        
        // Procesar cuerpo del while
        int i = startIndex + 1;
        while (i < code.size() && !code.get(i).trim().equals("}")) {
            String bodyLine = code.get(i).trim();
            if (!bodyLine.isEmpty()) {
                processAssignment(bodyLine);
            }
            i++;
        }
        
        assemblyCode.add("        JMP " + labelStart);
        assemblyCode.add(labelEnd + ":");
    }
    
    private void processAndCondition(String condition, String labelBody, String labelEnd) {
        String[] parts = condition.split("&&");
        
        // Procesar cada condición individualmente
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim();
            
            // Extraer los operandos y el operador
            Pattern pattern = Pattern.compile("(\\w+)\\s*([<>=!]+)\\s*(\\w+)");
            Matcher matcher = pattern.matcher(part);
            
            if (matcher.find()) {
                String left = matcher.group(1);
                String operator = matcher.group(2);
                String right = matcher.group(3);
                
                // Cargar el valor de la variable izquierda
                assemblyCode.add("        MOV AX, [" + left + "]");
                
                // Cargar el valor derecho (inmediato o variable)
                if (right.matches("\\d+")) {
                    assemblyCode.add("        MOV BX, " + right);
                } else {
                    assemblyCode.add("        MOV BX, [" + right + "]");
                }
                
                assemblyCode.add("        CMP AX, BX");
                
                // Generar el salto condicional apropiado
                switch (operator) {
                    case ">":
                        assemblyCode.add("        JLE " + labelEnd);  // Si no es mayor, salir
                        break;
                    case "<":
                        assemblyCode.add("        JGE " + labelEnd);  // Si no es menor, salir
                        break;
                    case "==":
                        assemblyCode.add("        JNE " + labelEnd);  // Si no es igual, salir
                        break;
                    case "!=":
                        assemblyCode.add("        JE " + labelEnd);   // Si es igual, salir
                        break;
                }
            }
        }
        
        // Si todas las condiciones se cumplen, continuar con el cuerpo
        assemblyCode.add("        JMP " + labelBody);
    }
    
    private void processOrCondition(String condition, String labelBody, String labelEnd) {
        String[] parts = condition.split("\\|\\|");
        
        // Procesar cada condición individualmente
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim();
            
            // Extraer los operandos y el operador
            Pattern pattern = Pattern.compile("(\\w+)\\s*([<>=!]+)\\s*(\\w+)");
            Matcher matcher = pattern.matcher(part);
            
            if (matcher.find()) {
                String left = matcher.group(1);
                String operator = matcher.group(2);
                String right = matcher.group(3);
                
                // Cargar el valor de la variable izquierda
                assemblyCode.add("        MOV AX, [" + left + "]");
                
                // Cargar el valor derecho (inmediato o variable)
                if (right.matches("\\d+")) {
                    assemblyCode.add("        MOV BX, " + right);
                } else {
                    assemblyCode.add("        MOV BX, [" + right + "]");
                }
                
                assemblyCode.add("        CMP AX, BX");
                
                // Generar el salto condicional apropiado
                switch (operator) {
                    case ">":
                        assemblyCode.add("        JG " + labelBody);  // Si es mayor, ir al cuerpo
                        break;
                    case "<":
                        assemblyCode.add("        JL " + labelBody);  // Si es menor, ir al cuerpo
                        break;
                    case "==":
                        assemblyCode.add("        JE " + labelBody);  // Si es igual, ir al cuerpo
                        break;
                    case "!=":
                        assemblyCode.add("        JNE " + labelBody); // Si no es igual, ir al cuerpo
                        break;
                }
            }
        }
        
        // Si ninguna condición se cumplió, salir
        assemblyCode.add("        JMP " + labelEnd);
    }
    
    private void processSimpleCondition(String condition, String labelBody, String labelEnd) {
        Pattern pattern = Pattern.compile("(\\w+)\\s*([<>=!]+)\\s*(\\w+)");
        Matcher matcher = pattern.matcher(condition);
        
        if (matcher.find()) {
            String left = matcher.group(1);
            String operator = matcher.group(2);
            String right = matcher.group(3);
            
            assemblyCode.add("        MOV AX, [" + left + "]");
            if (right.matches("\\d+")) {
                assemblyCode.add("        MOV BX, " + right);
            } else {
                assemblyCode.add("        MOV BX, [" + right + "]");
            }
            assemblyCode.add("        CMP AX, BX");
            
            switch (operator) {
                case ">":
                    assemblyCode.add("        JG " + labelBody);
                    break;
                case "<":
                    assemblyCode.add("        JL " + labelBody);
                    break;
                case "==":
                    assemblyCode.add("        JE " + labelBody);
                    break;
                case "!=":
                    assemblyCode.add("        JNE " + labelBody);
                    break;
            }
        }
    }
    
    private String extractVariableName(String declaration) {
        Pattern pattern = Pattern.compile("(IntegerType|FloatType|StringType)\\s+(\\w+)");
        Matcher matcher = pattern.matcher(declaration);
        if (matcher.find()) {
            return matcher.group(2);
        }
        return "";
    }
    
    private String extractCondition(String line) {
        Pattern pattern = Pattern.compile("\\((.*?)\\)");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "";
    }
    
    public void saveToFile(String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            for (String line : assemblyCode) {
                writer.write(line + "\n");
            }
        }
    }
} 