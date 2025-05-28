# Java Compiler Project

A simple Java compiler implementation that handles lexical analysis, symbol table management, and semantic error detection.

## Features

### Lexical Analysis

- Custom Java-like language processing
- Token recognition and classification
- Support for multiple variable declarations
- Delimiter and operator handling

### Symbol Table Management

- Efficient symbol tracking using HashMap
- Variable scope management
- Type checking and validation
- Support for three data types:
  - IntegerType (JSJ[a-z][0-9]+)
  - FloatType (JSJ[a-z][0-9]+)
  - StringType (JSJ[a-z][0-9]+)

### Semantic Analysis

- Type compatibility checking
- Variable declaration validation
- Undefined variable detection
- Operation type validation

### GUI Interface

- Dark theme modern interface
- Real-time code analysis
- Symbol table visualization
- Error reporting with detailed messages

## Project Structure

```
└── 📁src
    └── 📁compiler
        └── Compiler.java
        └── MainWindow.java
        └── TripletGenerator.java
    └── 📁errors
        └── ErrorHandler.java
        └── ErrorType.java
    └── 📁tables
        └── BaseTable.java
        └── ErrorTable.java
        └── SymbolTable.java
    └── 📁theme
        └── DarkThemeColors.java
    └── 📁tokens
        └── TokenType.java
    └── 📁validators
        └── RegExPattern.java
```

## Getting Started

> [!IMPORTANT]
> This Java compiler was developed using Java 8.

### Prerequisites

- Java 8 JDK
- Java IDE (recommended: VS Code, IntelliJ IDEA, or Eclipse)
- Git

### Installation

1. Clone the repository:

```bash
git clone https://github.com/JorgeSarricolea/java_compiler
```

2. Navigate to project directory:

```bash
cd java_compiler
```

3. Compile and run:

```bash
javac src/**/*.java && java src.compiler.Compiler

```

## Usage Example (Just Compile)

```java
// Variable declarations
IntegerType JSJa1, JSJb2, JSJc3;
StringType JSJs1;

// Valid assignments
JSJa1 = 10;
JSJb2 = JSJa1;
JSJs1 = "Hello";

// This will generate a type mismatch error
JSJc3 = JSJs1;
```

## More Code Examples

### Basic While Loop

```java
IntegerType JSJa1;

JSJa1 = 10;
while (JSJa1 < 20) {
    JSJa1 = JSJa1 + 1;
}
```

#### Generated triplet:

| Line | Data Object | Data Source | Operator |
| ---- | ----------- | ----------- | -------- |
| 1    | T1          | 10          | =        |
| 2    | JSJa1       | T1          | =        |
| 3    | T1          | 20          | =        |
| 4    | T2          | JSJa1       | =        |
| 5    | T2          | T1          | <        |
| 6    | TR1         | true        | 8        |
| 7    | TR1         | false       | 12       |
| 8    | T1          | JSJa1       | =        |
| 9    | T1          | 1           | +        |
| 10   | JSJa1       | T1          | =        |
| 11   |             | JMP         | 3        |
| 12   |             | end         |          |

### While Loop with AND operator

```java
IntegerType JSJa1;

JSJa1 = 10;
while (JSJa1 < 20 && JSJa1 != 15) {
    JSJa1 = JSJa1 + 1;
}
```

#### Generated triplet:

| Line | Data Object | Data Source | Operator |
| ---- | ----------- | ----------- | -------- |
| 1    | T1          | 10          | =        |
| 2    | JSJa1       | T1          | =        |
| 3    | T1          | 20          | =        |
| 4    | T2          | JSJa1       | =        |
| 5    | T2          | T1          | <        |
| 6    | TR1         | true        | 8        |
| 7    | TR1         | false       | 17       |
| 8    | T3          | 15          | =        |
| 9    | T4          | JSJa1       | =        |
| 10   | T4          | T3          | !=       |
| 11   | TR1         | true        | 13       |
| 12   | TR1         | false       | 17       |
| 13   | T1          | JSJa1       | =        |
| 14   | T1          | 1           | +        |
| 15   | JSJa1       | T1          | =        |
| 16   |             | JMP         | 3        |
| 17   |             | end         |          |

#### Generated Assembly:

```asm
        ; Inicialización de JSJa1
        MOV AX, 10
        MOV [JSJa1], AX

START0:
        MOV AX, [JSJa1]
        MOV BX, 20
        CMP AX, BX
        JGE END2          ; Si JSJa1 >= 20, salir

        MOV AX, [JSJa1]
        MOV BX, 15
        CMP AX, BX
        JE END2           ; Si JSJa1 == 15, salir

BODY1:
        MOV AX, [JSJa1]
        MOV BX, 1
        ADD AX, BX
        MOV [JSJa1], AX

        JMP START0

END2:
        ; Fin del programa
```

### While Loop with OR operator

```java
IntegerType JSJa1;

JSJa1 = 10;
while (JSJa1 > 5 || JSJa1 < 15) {
    JSJa1 = JSJa1 + 2;
}
```

#### Generated triplet:

| Line | Data Object | Data Source | Operator |
| ---- | ----------- | ----------- | -------- |
| 1    | T1          | 10          | =        |
| 2    | JSJa1       | T1          | =        |
| 3    | T1          | 5           | =        |
| 4    | T2          | JSJa1       | =        |
| 5    | T2          | T1          | >        |
| 6    | TR1         | true        | 13       |
| 7    | TR1         | false       | 8        |
| 8    | T3          | 15          | =        |
| 9    | T4          | JSJa1       | =        |
| 10   | T4          | T3          | <        |
| 11   | TR1         | true        | 13       |
| 12   | TR1         | false       | 17       |
| 13   | T1          | JSJa1       | =        |
| 14   | T1          | 2           | +        |
| 15   | JSJa1       | T1          | =        |
| 16   |             | JMP         | 3        |
| 17   |             | end         |          |

### While Loop with AND and OR operators

```java
IntegerType JSJa1;

JSJa1 = 10;
while (JSJa1 > 5 || JSJa1 < 15 && JSJa1 < 1) {
    JSJa1 = JSJa1 + 2;
}
```

#### Generated triplet:

| Line | Data Object | Data Source | Operator |
| ---- | ----------- | ----------- | -------- |
| 1    | T1          | 10          | =        |
| 2    | JSJa1       | T1          | =        |
| 3    | T1          | 5           | =        |
| 4    | T2          | JSJa1       | =        |
| 5    | T2          | T1          | >        |
| 6    | TR1         | true        | 18       |
| 7    | TR1         | false       | 8        |
| 8    | T3          | 15          | =        |
| 9    | T4          | JSJa1       | =        |
| 10   | T4          | T3          | <        |
| 11   | TR1         | true        | 13       |
| 12   | TR1         | false       | 22       |
| 13   | T5          | 1           | =        |
| 14   | T6          | JSJa1       | =        |
| 15   | T6          | T5          | <        |
| 16   | TR3         | true        | 18       |
| 17   | TR3         | false       | 22       |
| 18   | T1          | JSJa1       | =        |
| 19   | T1          | 2           | +        |
| 20   | JSJa1       | T1          | =        |
| 21   |             | JMP         | 3        |
| 22   |             | end         |          |

### While Loop nested

```java
IntegerType JSJa1;

JSJa1 = 10;
while (JSJa1 > 5 || JSJa1 < 15 && JSJa1 < 1) {
    while (JSJa1 > 5) {
        JSJa1 = JSJa1 + 2;
    }
    JSJa1 = JSJa1 + 2;
}
```

#### Generated triplet:

| Line | Data Object | Data Source | Operator |
| ---- | ----------- | ----------- | -------- |
| 1    | T1          | 10          | =        |
| 2    | JSJa1       | T1          | =        |
| 3    | T1          | 5           | =        |
| 4    | T2          | JSJa1       | =        |
| 5    | T2          | T1          | >        |
| 6    | TR1         | true        | 18       |
| 7    | TR1         | false       | 8        |
| 8    | T3          | 15          | =        |
| 9    | T4          | JSJa1       | =        |
| 10   | T4          | T3          | <        |
| 11   | TR2         | true        | 13       |
| 12   | TR2         | false       | 27       |
| 13   | T5          | 1           | =        |
| 14   | T6          | JSJa1       | =        |
| 15   | T6          | T5          | <        |
| 16   | TR3         | true        | 18       |
| 17   | TR3         | false       | 27       |
| 18   | T1          | 5           | =        |
| 19   | T2          | JSJa1       | =        |
| 20   | T2          | T1          | >        |
| 21   | TR1         | true        | 23       |
| 22   | TR1         | false       | 27       |
| 23   | T1          | JSJa1       | =        |
| 24   | T1          | 2           | +        |
| 25   | JSJa1       | T1          | =        |
| 26   |             | JMP         | 18       |
| 27   | T1          | JSJa1       | =        |
| 28   | T1          | 2           | +        |
| 29   | JSJa1       | T1          | =        |
| 30   |             | JMP         | 3        |
| 31   |             | end         |          |

### Optimized While Loop with Multiple Conditions

Original code

```java
IntegerType JSJa1;
IntegerType JSJb1;
IntegerType JSJc1;
IntegerType JSJy1;

JSJa1 = 10;
JSJb1 = 20;
JSJc1 = 15;
JSJy1 = 10;

while (JSJa1 < JSJb1 && JSJa1 != JSJc1) {
    JSJa1 = JSJa1 + 1;
}
```

Optimized code

```java
IntegerType JSJa1;
IntegerType JSJb1;
IntegerType JSJc1;

JSJa1 = 10;
JSJb1 = 20;
JSJc1 = 15;

while (JSJa1 < JSJb1 && JSJa1 != JSJc1) {
    JSJa1 = JSJa1 + 1;
}
```

#### Generated triplet:

| Line | Data Object | Data Source | Operator |
| ---- | ----------- | ----------- | -------- |
| 1    | T1          | 10          | =        |
| 2    | JSJa1       | T1          | =        |
| 3    | T1          | 20          | =        |
| 4    | JSJb1       | T1          | =        |
| 5    | T1          | 15          | =        |
| 6    | JSJc1       | T1          | =        |
| 7    | T1          | JSJb1       | =        |
| 8    | T2          | JSJa1       | =        |
| 9    | T2          | T1          | <        |
| 10   | TR1         | true        | 12       |
| 11   | TR1         | false       | 21       |
| 12   | T3          | JSJc1       | =        |
| 13   | T4          | JSJa1       | =        |
| 14   | T4          | T3          | !=       |
| 15   | TR1         | true        | 17       |
| 16   | TR1         | false       | 21       |
| 17   | T1          | JSJa1       | =        |
| 18   | T1          | 1           | +        |
| 19   | JSJa1       | T1          | =        |
| 20   |             | JMP         | 7        |
| 21   |             | end         |          |

## Code Style

- Variables must follow the pattern: JSJ[a-z][0-9]+
- Each statement must end with a semicolon
- Multiple declarations allowed with comma separation
- Type checking is strict between operations

## Contributing

Feel free to fork this project and submit pull requests with improvements.
