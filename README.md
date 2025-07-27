# AutoGen - Annotation-Based DTO Generator

A powerful Java annotation processor that automatically generates DTO (Data Transfer Object) classes with full Jackson serialization support. The generator supports multi-module projects and provides flexible configuration options.

## 🚀 Features

- ✅ **Annotation-Based**: Simple `@AutoGen` annotation for DTO generation
- ✅ **Multi-Module Support**: Generate DTOs in specific modules or auto-detect
- ✅ **Jackson Integration**: Complete Jackson annotation support with custom serializers
- ✅ **Collection Support**: Handles Lists, Sets, Maps with proper generics
- ✅ **Type Safety**: Generates type-safe DTOs with proper imports
- ✅ **Clean Code**: Generates complete classes with constructors, getters, setters, and utility methods
- ✅ **Entity Dependencies**: Supports external entity types from JAR dependencies

## 🛠️ Usage

### 1. Add Dependency

Add the annotation processor to your project:

```xml
<dependency>
    <groupId>com.AutoGenClass</groupId>
    <artifactId>class-generator</artifactId>
    <version>1.0.0</version>
    <scope>provided</scope>
</dependency>
```

### 2. Configure Annotation Processor

Add the annotation processor to your Maven compiler plugin:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.11.0</version>
    <configuration>
        <source>21</source>
        <target>21</target>
        <annotationProcessorPaths>
            <path>
                <groupId>com.AutoGenClass</groupId>
                <artifactId>class-generator</artifactId>
                <version>1.0.0</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

### 3. Annotate Your Classes

```java
package com.example.model;

import com.AutoGenClass.generator.AutoGen;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

@AutoGen(
    simpleFields = {"id", "username", "email", "firstName", "lastName"},
    serializedFields = {"password", "createdAt"},
    serializers = {"com.fasterxml.jackson.databind.ser.std.StdSerializer", "com.fasterxml.jackson.databind.ser.std.StdSerializer"},
    name = "UserDTO",
    module = "Main"
)
public class User {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String password;
    private String createdAt;
    
    // ... constructors, getters, setters
}
```

### 4. Compile

Run `mvn compile` and the DTO will be automatically generated in the specified location.

## 📁 Generated File Structure

DTOs are generated in the following structure:

```
{module}/src/main/java/{package}/autogendto/{ClassName}DTO.java
```

### Example Output

For a class in `com.AutoGenClass.example.User` with `module = "Main"`:

**Generated Location**: `Main/src/main/java/com/AutoGenClass/example/autogendto/UserDTO.java`

**Generated Package**: `com.AutoGenClass.example.autogendto`

## 🔧 Annotation Parameters

### Required Parameters

- **`simpleFields`**: Array of field names that don't require special serialization
- **`serializedFields`**: Array of field names that require custom serialization
- **`serializers`**: Array of serializer class names (must match `serializedFields` order)
- **`name`**: Name of the generated DTO class

### Optional Parameters

- **`module`**: Target module name where the DTO should be created (default: same module as source class)

## 🎯 Advanced Examples

### 1. Basic DTO Generation

```java
@AutoGen(
    simpleFields = {"id", "username", "email"},
    serializedFields = {"password"},
    serializers = {"com.fasterxml.jackson.databind.ser.std.StdSerializer"},
    name = "UserDTO", module = "Main"
)
public class User {
    // ... fields
}
```

### 2. Multi-Module DTO Generation

```java
@AutoGen(
    simpleFields = {"id", "name", "description", "price"},
    serializedFields = {"metadata"},
    serializers = {"com.fasterxml.jackson.databind.ser.std.StdSerializer"},
    name = "ProductDTO",
        , module = "Main"  // Creates DTO in 'Main' module
)
public class Product {
    // ... fields
}
```

### 3. Collection Support

```java
@AutoGen(
    simpleFields = {"id", "username", "roles", "preferences", "addresses"},
    serializedFields = {"password", "userProfile"},
    serializers = {"com.fasterxml.jackson.databind.ser.std.StdSerializer", "com.fasterxml.jackson.databind.ser.std.StdSerializer"},
    name = "EnhancedUserDTO", module = "Main"
)
public class EnhancedUser {
    private Long id;
    private String username;
    private String password;
    private List<String> roles;           // → List<String> in DTO
    private Set<String> preferences;      // → Set<String> in DTO
    private Map<String, String> addresses; // → Map<String, String> in DTO
    private UserProfile userProfile;      // → UserProfile in DTO
}
```

### 4. Entity Dependencies

The processor automatically handles external entity types:

```java
@AutoGen(
    simpleFields = {"id", "userProfile", "relatedProfiles"},
    serializedFields = {"password"},
    serializers = {"com.fasterxml.jackson.databind.ser.std.StdSerializer"},
    name = "UserWithProfileDTO", module = "Main"
)
public class UserWithProfile {
    private Long id;
    private String password;
    private UserProfile userProfile;           // External entity
    private List<UserProfile> relatedProfiles; // Collection of external entities
}
```

## 🏗️ Supported Project Structures

The annotation processor works with any project structure:

### Single Module
```
project/
└── src/main/java/
    └── com/example/
        ├── User.java
        └── autogendto/
            └── UserDTO.java
```

### Multi-Module
```
project/
├── Main/src/main/java/
│   └── com/example/
│       ├── User.java
│       └── autogendto/
│           └── UserDTO.java
├── api/src/main/java/
│   └── com/example/autogendto/
│       └── ProductDTO.java
└── service/src/main/java/
    └── com/example/autogendto/
        └── OrderDTO.java
```

## 🎯 Benefits

- **🔧 Simple**: Just add an annotation to generate complete DTOs
- **🌍 Flexible**: Works with any module structure
- **📦 Portable**: Can be used in any Java project
- **🔄 Maintainable**: Generated code is clean and follows best practices
- **⚡ Efficient**: Generates optimized, production-ready DTOs
- **🛡️ Type-Safe**: Full type safety with proper generics support
- **🎨 Customizable**: Support for custom serializers and module targeting

## 🚀 Getting Started

1. **Clone** the repository: `git clone https://github.com/M-Salameh/AutoGeneratedClasses.git`
2. **Install** the annotation processor: `mvn install`
3. **Add** the dependency to your project
4. **Configure** the annotation processor in your Maven compiler plugin
5. **Annotate** your classes with `@AutoGen`
6. **Compile** and enjoy auto-generated DTOs!

## 📝 Examples

Check out the examples in the `Main/src/main/java/com/AutoGenClass/example/` directory:

- **User.java**: Basic DTO generation
- **EnhancedUser.java**: Collection types and entity dependencies
- **UserProfile.java**: External entity example

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License

This project is open source and available under the MIT License.

## 👨‍💻 Author

**Mohammed SALAMEH**
- Email: mohammedsalameh37693@gmail.com
- Role: Back-End, RPA, Software Engineer
- GitHub: [M-Salameh](https://github.com/M-Salameh)