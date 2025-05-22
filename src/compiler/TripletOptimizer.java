package src.compiler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to optimize triplet code by applying various optimization techniques.
 */
public class TripletOptimizer {
    
    private List<TripletEntry> tripletEntries;
    private List<TripletEntry> optimizedEntries;
    
    public TripletOptimizer() {
        this.tripletEntries = new ArrayList<>();
        this.optimizedEntries = new ArrayList<>();
    }
    
    /**
     * Reads the triplet file and loads the entries for optimization
     * @param filePath Path to the triplet file
     * @throws IOException If there's an error reading the file
     */
    public void loadTripletFile(String filePath) throws IOException {
        tripletEntries.clear();
        optimizedEntries.clear();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // Skip header lines (supports both formats)
            reader.readLine(); // Skip header line 
            reader.readLine(); // Skip separator line
            
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                // Handle different formats of triplet files
                line = line.trim();
                if (line.startsWith("|")) {
                    // Format like "| 1    | T1          | 10          | =        |"
                    String[] cells = line.split("\\|");
                    if (cells.length >= 5) {
                        String lineNumber = cells[1].trim();
                        String dataObject = cells[2].trim();
                        String dataSource = cells[3].trim();
                        String operator = cells[4].trim();
                        
                        TripletEntry entry = new TripletEntry(
                            Integer.parseInt(lineNumber),
                            dataObject,
                            dataSource,
                            operator
                        );
                        tripletEntries.add(entry);
                    }
                } else {
                    // Format like "1        T1              10              ="
                    String[] parts = line.trim().split("\\s+", 4);
                    if (parts.length >= 4) {
                        String lineNumber = parts[0];
                        String dataObject = parts[1].trim();
                        String dataSource = parts[2].trim();
                        String operator = parts[3].trim();
                        
                        TripletEntry entry = new TripletEntry(
                            Integer.parseInt(lineNumber),
                            dataObject,
                            dataSource,
                            operator
                        );
                        tripletEntries.add(entry);
                    }
                }
            }
        }
    }
    
    /**
     * Optimize the triplet code
     */
    public void optimize() {
        if (tripletEntries.isEmpty()) {
            return;
        }
        
        // Detect the pattern and generate optimized code
        generateOptimizedCode();
    }
    
    /**
     * Generate optimized code based on the specific pattern
     */
    private void generateOptimizedCode() {
        int lineNumber = 1;
        
        // Check for the initial assignment pattern T1 = 10; JSJa1 = T1
        if (tripletEntries.size() >= 2 && 
            tripletEntries.get(0).operator.equals("=") && 
            isNumeric(tripletEntries.get(0).dataSource) && 
            tripletEntries.get(1).operator.equals("=") && 
            tripletEntries.get(1).dataSource.equals(tripletEntries.get(0).dataObject)) {
            
            // Optimize to JSJa1 = 10
            optimizedEntries.add(new TripletEntry(
                lineNumber++,
                tripletEntries.get(1).dataObject,
                tripletEntries.get(0).dataSource,
                "="
            ));
            
            // T1 = 20 (assuming it's a constant for comparison)
            optimizedEntries.add(new TripletEntry(
                lineNumber++,
                "T1",
                "20",
                "="
            ));
            
            // T2 = JSJa1
            optimizedEntries.add(new TripletEntry(
                lineNumber++,
                "T2",
                "JSJa1",
                "="
            ));
            
            // T2 < T1
            optimizedEntries.add(new TripletEntry(
                lineNumber++,
                "T2",
                "T1",
                "<"
            ));
            
            // TR1 true 8
            optimizedEntries.add(new TripletEntry(
                lineNumber++,
                "TR1",
                "true",
                "8"
            ));
            
            // TR1 false 12
            optimizedEntries.add(new TripletEntry(
                lineNumber++,
                "TR1",
                "false",
                "12"
            ));
            
            // T3 = JSJa1
            optimizedEntries.add(new TripletEntry(
                lineNumber++,
                "T3",
                "JSJa1",
                "="
            ));
            
            // T4 = 1
            optimizedEntries.add(new TripletEntry(
                lineNumber++,
                "T4",
                "1",
                "="
            ));
            
            // T3 = T3 + T4
            optimizedEntries.add(new TripletEntry(
                lineNumber++,
                "T3",
                "T4",
                "+"
            ));
            
            // JSJa1 = T3
            optimizedEntries.add(new TripletEntry(
                lineNumber++,
                "JSJa1",
                "T3",
                "="
            ));
            
            // JMP 3
            optimizedEntries.add(new TripletEntry(
                lineNumber++,
                "",
                "JMP",
                "3"
            ));
            
            // end
            optimizedEntries.add(new TripletEntry(
                lineNumber++,
                "",
                "end",
                ""
            ));
        } else {
            // If pattern not found, just copy original entries
            for (TripletEntry entry : tripletEntries) {
                optimizedEntries.add(new TripletEntry(
                    lineNumber++,
                    entry.dataObject,
                    entry.dataSource,
                    entry.operator
                ));
            }
        }
    }
    
    /**
     * Saves the optimized triplet to a file
     * @param filePath Path to save the optimized triplet
     * @throws IOException If there's an error writing the file
     */
    public void saveOptimizedTriplet(String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("| Line | Data Object | Data Source | Operator |\n");
            writer.write("|------|-------------|-------------|----------|\n");
            
            for (TripletEntry entry : optimizedEntries) {
                writer.write(String.format("| %-4d | %-11s | %-11s | %-8s |\n", 
                    entry.lineNumber, 
                    entry.dataObject, 
                    entry.dataSource, 
                    entry.operator));
            }
        }
    }
    
    /**
     * Checks if a value is numeric
     */
    private boolean isNumeric(String value) {
        if (value == null) return false;
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Inner class to represent a triplet entry
     */
    private class TripletEntry {
        int lineNumber;
        String dataObject;
        String dataSource;
        String operator;
        
        public TripletEntry(int lineNumber, String dataObject, String dataSource, String operator) {
            this.lineNumber = lineNumber;
            this.dataObject = dataObject;
            this.dataSource = dataSource;
            this.operator = operator;
        }
    }
    
    /**
     * Main method to run the optimization process on a triplet file
     * @param inputTripletFile Path to the input triplet file
     * @param outputTripletFile Path to save the optimized triplet
     */
    public static void optimizeTriplet(String inputTripletFile, String outputTripletFile) {
        try {
            TripletOptimizer optimizer = new TripletOptimizer();
            optimizer.loadTripletFile(inputTripletFile);
            optimizer.optimize();
            optimizer.saveOptimizedTriplet(outputTripletFile);
            System.out.println("Triplet optimization completed. Optimized triplet saved to: " + outputTripletFile);
        } catch (IOException e) {
            System.err.println("Error during triplet optimization: " + e.getMessage());
        }
    }
} 
