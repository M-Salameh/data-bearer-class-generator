# AutoGen DTO Generator - Complete Documentation

## Table of Contents

1. [Overview](#overview)
2. [Installation](#installation)
3. [Basic Usage](#basic-usage)
4. [Annotation Parameters](#annotation-parameters)
5. [Advanced Features](#advanced-features)
6. [Project Structure](#project-structure)
7. [Examples](#examples)
8. [Troubleshooting](#troubleshooting)
9. [API Reference](#api-reference)

## Overview

AutoGen is a Java annotation processor that automatically generates DTO (Data Transfer Object) classes at compile time. It provides a simple, annotation-based approach to create type-safe DTOs with full Jackson serialization support.

### Key Features

- **Annotation-Based**: Simple `@AutoGen` annotation for DTO generation
- **Multi-Module Support**: Generate DTOs in specific modules or auto-detect
- **Jackson Integration**: Complete Jackson annotation support with custom serializers
- **Collection Support**: Handles Lists, Sets, Maps with proper generics
- **Type Safety**: Generates type-safe DTOs with proper imports
- **Entity Dependencies**: Supports external entity types from JAR dependencies
- **Clean Code**: Generates complete classes with constructors, getters, setters, and utility methods

## Installation

### 1. Add Dependency

Add the annotation processor to your project's `pom.xml`:

```xml
<dependency>
    <groupId>com.AutoGenClass</groupId>
    <artifactId>class-generator</artifactId>
    <version>1.0.0</version>
    <scope>provided</scope>
</dependency>
```

### 2. Configure Annotation Processor

Configure the Maven compiler plugin to use the annotation processor:

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

## Basic Usage

### Step 1: Annotate Your Class

```java
package com.example.model;

import com.AutoGenClass.generator.AutoGen;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

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
    private String createdAt;
    
    // ... constructors, getters, setters
}
```

### Step 2: Compile

Run `mvn compile` and the DTO will be automatically generated.

### Step 3: Use the Generated DTO

The generated DTO will be available at:
```
src/main/java/com/example/model/autogendto/UserDTO.java
```

## Annotation Parameters

### Required Parameters

| Parameter | Type | Description | Example |
|-----------|------|-------------|---------|
| `simpleFields` | `String[]` | Field names that don't require special serialization | `{"id", "name", "email"}` |
| `serializedFields` | `String[]` | Field names that require custom serialization | `{"password", "sensitiveData"}` |
| `serializers` | `String[]` | Serializer class names (must match `serializedFields` order) | `{"com.example.CustomSerializer"}` |
| `name` | `String` | Name of the generated DTO class | `"UserDTO"` |

### Optional Parameters

| Parameter | Type | Default | Description | Example |
|-----------|------|---------|-------------|---------|
| `module` | `String` | `""` | Target module name where the DTO should be created | `"api"` |

## Advanced Features

### 1. Multi-Module Support

Specify a target module for DTO generation:

```java
@AutoGen(
    simpleFields = {"id", "name", "description"},
    serializedFields = {"metadata"},
    serializers = {"com.fasterxml.jackson.databind.ser.std.StdSerializer"},
    name = "ProductDTO",
    module = "api"  // Creates DTO in 'api' module
)
public class Product {
    // ... fields
}
```

**Generated Location**: `api/src/main/java/com/example/autogendto/ProductDTO.java`

### 2. Collection Support

The processor automatically handles collections with proper generics:

```java
@AutoGen(
    simpleFields = {"id", "username", "roles", "preferences", "addresses"},
    serializedFields = {"password"},
    serializers = {"com.fasterxml.jackson.databind.ser.std.StdSerializer"},
    name = "EnhancedUserDTO"
)
public class EnhancedUser {
    private Long id;
    private String username;
    private String password;
    private List<String> roles;           // → List<String> in DTO
    private Set<String> preferences;      // → Set<String> in DTO
    private Map<String, String> addresses; // → Map<String, String> in DTO
}
```

### 3. Entity Dependencies

Support for external entity types from JAR dependencies:

```java
@AutoGen(
    simpleFields = {"id", "userProfile", "relatedProfiles"},
    serializedFields = {"password"},
    serializers = {"com.fasterxml.jackson.databind.ser.std.StdSerializer"},
    name = "UserWithProfileDTO"
)
public class UserWithProfile {
    private Long id;
    private String password;
    private UserProfile userProfile;           // External entity
    private List<UserProfile> relatedProfiles; // Collection of external entities
}
```

### 4. Custom Serializers

Use custom serializers for specific fields:

```java
@AutoGen(
    simpleFields = {"id", "name"},
    serializedFields = {"sensitiveData", "encryptedField"},
    serializers = {"com.example.serializer.SensitiveDataSerializer", "com.example.serializer.EncryptionSerializer"},
    name = "SecureDTO"
)
public class SecureEntity {
    private Long id;
    private String name;
    private String sensitiveData;
    private String encryptedField;
}
```

## Project Structure

### Single Module Project

```
project/
└── src/main/java/
    └── com/example/
        ├── User.java
        └── autogendto/
            └── UserDTO.java
```

### Multi-Module Project

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

## Examples

### Example 1: Basic User DTO

**Source Class**:
```java
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
    private String createdAt;
}
```

**Generated DTO**:
```java
package com.example.autogendto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.Serializable;
import java.util.Objects;

public class UserDTO implements Serializable {
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("username")
    private String username;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("firstName")
    private String firstName;
    
    @JsonProperty("lastName")
    private String lastName;
    
    @JsonProperty("password")
    @JsonSerialize(using = StdSerializer.class)
    private String password;
    
    @JsonProperty("createdAt")
    @JsonSerialize(using = StdSerializer.class)
    private String createdAt;
    
    // Constructors, getters, setters, equals, hashCode, toString
}
```

### Example 2: Collection Support

**Source Class**:
```java
@AutoGen(
    simpleFields = {"id", "username", "roles", "preferences"},
    serializedFields = {"password"},
    serializers = {"com.fasterxml.jackson.databind.ser.std.StdSerializer"},
    name = "EnhancedUserDTO"
)
public class EnhancedUser {
    private Long id;
    private String username;
    private String password;
    private List<String> roles;
    private Set<String> preferences;
}
```

**Generated DTO**:
```java
public class EnhancedUserDTO implements Serializable {
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("username")
    private String username;
    
    @JsonProperty("roles")
    @JsonSerialize(contentUsing = StdSerializer.class)
    private List<String> roles;
    
    @JsonProperty("preferences")
    @JsonSerialize(contentUsing = StdSerializer.class)
    private Set<String> preferences;
    
    @JsonProperty("password")
    @JsonSerialize(using = StdSerializer.class)
    private String password;
    
    // Constructors, getters, setters, equals, hashCode, toString
}
```

## Troubleshooting

### Common Issues

#### 1. DTO Not Generated

**Problem**: DTO is not generated after compilation.

**Solutions**:
- Check that the annotation processor is properly configured in `pom.xml`
- Verify that the `@AutoGen` annotation is correctly applied
- Ensure all required parameters are provided
- Check compilation logs for error messages

#### 2. Module Not Found

**Problem**: Specified module doesn't exist.

**Solution**: The processor will log a warning and fall back to the default behavior (same module as source class).

#### 3. Import Issues

**Problem**: Generated DTO has missing imports.

**Solution**: The processor automatically generates imports based on field types. Ensure all referenced types are available in the classpath.

#### 4. Serializer Not Found

**Problem**: Custom serializer class not found.

**Solution**: Ensure the serializer class is available in the classpath and the full class name is correct.

### Debug Information

The processor logs information during compilation:

```
[INFO] processing class : com.example.User
[INFO] Using specified module: api at /path/to/api/src/main/java
[INFO] Generated source file: /path/to/api/src/main/java/com/example/autogendto/UserDTO.java
[INFO] Generated DTO class: com.example.UserDTO
```

## API Reference

### AutoGen Annotation

```java
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface AutoGen {
    String[] simpleFields();
    String[] serializedFields();
    String[] serializers();
    String name();
    String module() default "";
}
```

### Generated DTO Features

Each generated DTO includes:

- **Package Declaration**: `{source.package}.autogendto`
- **Imports**: Automatically generated based on field types
- **Class Declaration**: Implements `Serializable`
- **Fields**: With Jackson annotations
- **Constructors**: Default and all-args constructors
- **Getters/Setters**: For all fields
- **Utility Methods**: `equals()`, `hashCode()`, `toString()`

### Supported Types

- **Primitive Types**: `int`, `long`, `double`, `float`, `boolean`, `char`, `byte`, `short`
- **Wrapper Types**: `Integer`, `Long`, `Double`, `Float`, `Boolean`, `Character`, `Byte`, `Short`, `String`, `Object`
- **Collections**: `List<T>`, `Set<T>`, `Map<K,V>`, `Collection<T>`
- **Arrays**: `T[]`
- **Custom Types**: Any class available in the classpath

### Jackson Annotations

The processor automatically adds Jackson annotations:

- `@JsonProperty`: For all fields
- `@JsonSerialize`: For serialized fields and collection content
- `@JsonSerialize(contentUsing = StdSerializer.class)`: For collection elements

## Best Practices

1. **Naming Convention**: Use descriptive names for DTOs (e.g., `UserDTO`, `ProductResponseDTO`)
2. **Module Organization**: Use the `module` parameter to organize DTOs by functionality
3. **Serializer Selection**: Choose appropriate serializers for sensitive or complex data
4. **Field Selection**: Only include fields that are needed in the DTO
5. **Type Safety**: Use proper generic types for collections

## Version History

- **v1.0.0**: Initial release with basic DTO generation
- **v2.0**: Added multi-module support and enhanced collection handling 