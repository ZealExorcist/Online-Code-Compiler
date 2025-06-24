# Error Highlighting Test Cases

## Python Test Cases

### Syntax Errors
```python
# Missing colon and indentation error
if x == 5
    print("Hello")
    
# Unmatched parentheses
print("Hello"
print(("Missing closing parenthesis")

# Invalid variable names
123variable = "test"
def 456function():
    pass
```

### Valid Code
```python
def hello_world():
    x = 5
    if x == 5:
        print("Hello, World!")
    return x
```

## JavaScript Test Cases

### Syntax Errors
```javascript
// Missing semicolon
let x = 5
let y = 10
console.log(x + y)

// Unmatched brackets
function test() {
    let arr = [1, 2, 3;
    return arr;

// Undefined function call
someUndefinedFunction();
```

### Valid Code
```javascript
function hello() {
    let message = "Hello, World!";
    console.log(message);
    return message;
}
```

## Java Test Cases

### Syntax Errors
```java
// Missing semicolon
public class Test {
    public static void main(String[] args) {
        System.out.println("Hello")
        int x = 5
    }
}

// Unmatched braces
public class Test2 {
    public void method() {
        if (true) {
            System.out.println("test");
        // Missing closing brace
    }
```

### Valid Code
```java
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}
```

## Features

- **Real-time Error Detection**: Errors are highlighted as you type
- **Multiple Error Types**: Supports syntax errors, warnings, and info messages
- **Language-Specific**: Different error rules for different programming languages
- **Visual Indicators**: 
  - Red underlines for syntax errors
  - Orange underlines for warnings
  - Blue underlines for info messages
  - Gutter markers showing error locations
  - Hover tooltips with error descriptions
- **Customizable**: Can be enabled/disabled in settings
- **Performance**: Debounced checking (300ms delay) for smooth editing

## Settings

You can toggle error highlighting in **Settings > Editor Settings > Enable Error Highlighting**

The feature is enabled by default but can be disabled if you prefer a cleaner editing experience.
