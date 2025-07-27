# AutoGen - Configuration-Based DTO Generator

A powerful Java annotation processor that automatically generates DTO (Data Transfer Object) classes with full Jackson serialization support. The generator is now **fully configurable** and works with any project structure.

## 🚀 Features

- ✅ **Configuration-Based**: Fully configurable via properties file
- ✅ **Universal Compatibility**: Works with any project structure and module naming
- ✅ **Jackson Integration**: Complete Jackson annotation support with custom serializers
- ✅ **Collection Support**: Handles Lists, Sets, Maps with proper generics
- ✅ **Type Safety**: Generates type-safe DTOs with proper imports
- ✅ **Clean Code**: Generates complete classes with constructors, getters, setters, and utility methods

## 📋 Configuration

The annotation processor reads configuration from `autogen-config.properties` file. This file should be placed in your project's resources directory.

### Configuration File Location

For **single-module projects**:
```
src/main/resources/autogen-config.properties
```

For **multi-module projects** (place in the module that uses the annotation):
```
YourModule/src/main/resources/autogen-config.properties
```

### Configuration Options

```properties
# The module name where source classes are located
# Examples: "Main", "app", "core", "api", "service", "web"
# Leave empty to auto-detect based on annotated class location
source.module=Main

# The source directory pattern relative to the module root
# Common patterns: "src/main/java", "src/test/java", "app/src/main/java"
source.directory=src/main/java

# The subpackage name where DTOs will be generated
# DTOs will be generated in: {source.package}.{dto.subpackage}
dto.subpackage=autogendto

# Optional: Override the source directory completely
# If specified, this will be used instead of auto-detection
# Format: absolute path or relative path from project root
# source.directory.override=Main/src/main/java
```

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

### 2. Create Configuration File

Create `autogen-config.properties` in your module's resources directory with your desired settings.

### 3. Annotate Your Classes

```java
package com.example.model;

import com.AutoGenClass.generator.AutoGen;

@AutoGen(
    simpleFields = {"id", "username", "email", "firstName", "lastName"},
    serializedFields = {"password", "createdAt"},
    serializers = {"com.fasterxml.jackson.databind.ser.std.StdSerializer", "com.fasterxml.jackson.databind.ser.std.StdSerializer"},
    name = "UserDTO"
)
public class User {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String password;
    private LocalDateTime createdAt;
    
    // ... constructors, getters, setters
}
```

### 4. Compile

Run `mvn compile` and the DTO will be automatically generated in the configured location.

## 📁 Generated File Structure

Based on the configuration, DTOs will be generated in:

```
{source.module}/{source.directory}/{source.package}/{dto.subpackage}/
```

### Example Output

For a class in `com.example.model.User` with default configuration:

**Generated Location**: `Main/src/main/java/com/example/model/autogendto/UserDTO.java`

**Generated Package**: `com.example.model.autogendto`

## 🔧 Advanced Configuration

### Auto-Detection Mode

Leave `source.module` empty to enable auto-detection:

```properties
source.module=
source.directory=src/main/java
dto.subpackage=autogendto
```

The processor will automatically detect the correct module by analyzing the annotated class location.

### Custom Serializers

You can use custom serializers by providing their full class names:

```java
@AutoGen(
    simpleFields = {"id", "name"},
    serializedFields = {"sensitiveData"},
    serializers = {"com.example.serializer.CustomSerializer"},
    name = "SecureDTO"
)
```

### Collection Support

The processor automatically handles collections with proper generics:

```java
@AutoGen(
    simpleFields = {"id", "name", "roles", "preferences"},
    serializedFields = {"password"},
    serializers = {"com.fasterxml.jackson.databind.ser.std.StdSerializer"},
    name = "UserDTO"
)
public class User {
    private Long id;
    private String name;
    private List<String> roles;           // → List<String> in DTO
    private Map<String, Object> preferences; // → Map<String, Object> in DTO
    private String password;
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

### Multi-Module (Any Names)
```
project/
├── Main/src/main/java/
│   └── com/example/
│       ├── User.java
│       └── autogendto/
│           └── UserDTO.java
├── app/src/main/java/
├── core/src/main/java/
├── api/src/main/java/
└── service/src/main/java/
```

### Custom Structures
```
project/
├── backend/src/main/java/
├── frontend/src/main/java/
└── shared/src/main/java/
```

## 🎯 Benefits

- **🔧 Configurable**: Easy to customize for any project structure
- **🌍 Universal**: Works with any module naming convention
- **📦 Portable**: Can be published and used by other developers
- **🔄 Maintainable**: Configuration-driven approach makes it easy to modify
- **⚡ Efficient**: Generates optimized, production-ready DTOs
- **🛡️ Type-Safe**: Full type safety with proper generics support

## 🚀 Getting Started

1. **Clone/Download** the annotation processor
2. **Install** it to your local Maven repository: `mvn install`
3. **Add** the dependency to your project
4. **Create** the configuration file
5. **Annotate** your classes
6. **Compile** and enjoy auto-generated DTOs!

## 📝 License

This project is open source and available under the MIT License.