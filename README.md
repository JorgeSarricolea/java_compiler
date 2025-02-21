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
    └── 📁errors
        └── ErrorHandler.java         # Centralized error message management
    └── 📁tables
        └── ErrorTable.java          # Semantic error tracking and display
        └── SymbolTable.java         # Symbol management and type checking
    └── 📁theme
        └── DarkThemeColors.java     # UI theme configuration
    └── 📁validators
        └── IdentifierValidator.java  # Variable name validation
        └── TypeValidator.java       # Data type validation
        └── Validator.java           # Validation interface
    └── Compiler.java               # Main compiler implementation
    └── MainWindow.java             # GUI implementation
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

3. Compile the source files:

```bash
javac src/*.java
```

4. Run the compiler:

```bash
java src.Compiler
```

## Usage Example

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

## Code Style

- Variables must follow the pattern: JSJ[a-z][0-9]+
- Each statement must end with a semicolon
- Multiple declarations allowed with comma separation
- Type checking is strict between operations

## Contributing

Feel free to fork this project and submit pull requests with improvements.
