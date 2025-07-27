# Auto-Generated Classes
it is a start-up idea on how we generate class in compile time based on custom annotation.
the annotation must declare the fields, then we will try to write classes
(generate) them , it must be super useful at creating DTOs and other custom
data bearer or for simplifications DTOs classes.

# Super Important
for now , it is uncompleted and untested , just compiles fine and 
the processing needs working a little more.
once it is tested and working this section shall be deleted

## Overview

This project provides a compile-time solution for generating classes from base classes. 
Instead of manually creating DTOs, you simply annotate your entity class with `@AutoGen` and specify which fields should be included in the DTO.

## Features

- **Compile-time generation**: DTOs are generated during compilation, no runtime overhead
- **Flexible field selection**: Choose which fields to include in the DTO
- **Custom serialization**: Support for custom Jackson serializers for specific fields
- **Complete DTO generation**: Includes constructors, getters, setters, equals, hashCode, and toString methods
- **JSON annotations**: Automatically adds Jackson annotations for JSON serialization

## Usage

### 1. Annotate Your Entity Class

```java
import com.AutoGenClass.generator.AutoGen;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

@AutoGen(
    simpleFields = {"id", "username", "email", "firstName", "lastName"},
    serializedFields = {"password", "createdAt"},
    serializers = {StdSerializer.class, StdSerializer.class},
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

### 2. Compile the Project

```bash
mvn compile
```

### 3. Use the Generated DTO

The generated `UserDTO` class will be available in `dto/generated-dto/` and can be used like any other Java class:

```java
UserDTO userDTO = new UserDTO();
userDTO.setId(user.getId());
userDTO.setUsername(user.getUsername());
userDTO.setEmail(user.getEmail());
userDTO.setFirstName(user.getFirstName());
userDTO.setLastName(user.getLastName());

ObjectMapper mapper = new ObjectMapper();
String json = mapper.writeValueAsString(userDTO);
```

## Annotation Parameters

### `simpleFields`
Array of field names that don't require special serialization. These fields will be serialized using Jackson's default serialization.

### `serializedFields`
Array of field names that require custom serialization. These fields will use the serializers specified in the `serializers` parameter.

### `serializers`
Array of Jackson serializer classes. Must be in the same order as `serializedFields`. Each serializer will be applied to its corresponding field.

### `name`
The name of the generated DTO class.

## Generated DTO Features

The generated DTO class includes:

- **Fields**: All specified fields with appropriate Jackson annotations
- **Constructors**: Default constructor and all-args constructor
- **Getters and Setters**: For all fields
- **equals() and hashCode()**: Based on all fields
- **toString()**: Human-readable string representation
- **Serializable**: Implements `Serializable` interface
- **Jackson Annotations**: `@JsonProperty` for all fields, `@JsonSerialize` for custom serialized fields

## Example Generated DTO

For the User entity above, the generated `UserDTO` would look like:

```java
public class UserDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private Object id;

    @JsonProperty("username")
    private Object username;

    @JsonProperty("email")
    private Object email;

    @JsonProperty("firstName")
    private Object firstName;

    @JsonProperty("lastName")
    private Object lastName;

    @JsonProperty("password")
    @JsonSerialize(using = com.fasterxml.jackson.databind.ser.std.StdSerializer.class)
    private Object password;

    @JsonProperty("createdAt")
    @JsonSerialize(using = com.fasterxml.jackson.databind.ser.std.StdSerializer.class)
    private Object createdAt;

    // Constructors, getters, setters, equals, hashCode, toString
}
```

## Building and Running

1. Clone the repository
2. Run `mvn compile` to compile the project
3. Run `mvn exec:java -Dexec.mainClass="com.AutoGenClass.Main"` to see the demo

## Current Status

This project demonstrates the concept of auto-generating DTO classes using annotation processing. The implementation includes:

- **@AutoGen annotation**: Defines which fields to include in the DTO and how to serialize them
- **ClassAutoGenerator**: Annotation processor that reads the annotation and generates DTO classes
- **Field type matching**: Uses the annotation processing API to match annotation fields with actual class fields
- **Complete DTO generation**: Includes constructors, getters, setters, equals, hashCode, and toString methods
- **Jackson integration**: Automatically adds JSON annotations for serialization

## Demo Output

The demo shows:
- **Basic User Example**: Original User entity with simple fields
- **Enhanced User Example**: Complex entity with collections and entity dependencies
- **Collection Fields**: List<String> roles, Set<String> preferences, Map<String, String> addresses
- **Entity Fields**: UserProfile objects with nested relationships
- **Generated DTO Features**: Type-safe generics, Jackson annotations, import generation
- **Usage Examples**: Step-by-step DTO creation and usage patterns
- **Real-world Scenarios**: REST API, database operations, microservice communication

## Main Class Methods

The `Main` class includes 4 demonstration methods:

### 1. `createUserDTO(User user)`
- **Purpose**: Demonstrates the structure that would be generated for a basic User DTO
- **Shows**: Field types, Jackson annotations, serialization configuration
- **Output**: Simulated generated class structure with all annotations

### 2. `handleUserDTO(User user)`
- **Purpose**: Demonstrates how to use the generated User DTO after compilation
- **Shows**: DTO instantiation, field population, JSON serialization, API usage
- **Output**: Step-by-step usage examples and expected results

### 3. `createEnhancedUserDTO()`
- **Purpose**: Demonstrates the enhanced structure with collections and entities
- **Shows**: Collection types, entity dependencies, content serialization, imports
- **Output**: Advanced DTO structure with List, Set, Map, and entity fields

### 4. `handleEnhancedUserDTO()`
- **Purpose**: Demonstrates how to use the enhanced DTO with complex data structures
- **Shows**: Collection population, entity relationships, advanced serialization
- **Output**: Real-world usage scenarios for complex DTOs

## Technical Implementation

The enhanced annotation processor:
1. Reads the @AutoGen annotation from the source class
2. Uses the annotation processing API to inspect the actual class fields
3. Matches annotation field names with actual class field names
4. Analyzes field types including:
   - Primitive types (int, long, double, etc.)
   - Wrapper types (Integer, Long, String, etc.)
   - Collection types (List, Set, Map, arrays)
   - Entity types from external JARs
5. Generates appropriate imports based on field types
6. Adds content serialization for collections
7. Generates a complete DTO class with proper types and annotations

## Code Documentation

The `ClassAutoGenerator` class is comprehensively documented with:

### Class-Level Documentation
- **JavaDoc**: Complete class documentation with features, author, version, and since tags
- **Purpose**: Clear explanation of the annotation processor's role
- **Key Features**: Bulleted list of main capabilities

### Method Documentation
- **Public Methods**: Full JavaDoc for `init()` and `process()` methods
- **Private Methods**: Comprehensive documentation for all utility methods
- **Parameters**: Detailed parameter descriptions with types
- **Return Values**: Clear return value documentation
- **Exceptions**: Documented exceptions where applicable

### Inline Comments
- **Code Sections**: Comments explaining major code blocks
- **Logic Flow**: Step-by-step explanations of complex operations
- **Type Analysis**: Comments explaining type detection logic
- **Code Generation**: Comments describing what each generation method produces

### Field Documentation
- **FieldInfo Class**: Complete documentation for the internal data structure
- **Field Properties**: Detailed explanations of each field's purpose
- **Constructor**: Documented constructor with all parameters

### Examples and Usage
- **Code Comments**: Examples of generated code structure
- **Type Handling**: Comments explaining how different types are processed
- **Serialization**: Documentation of Jackson annotation generation

## Enhanced Features

### Collection Support
- **List types**: `List<String>`, `List<UserProfile>`, etc.
- **Set types**: `Set<String>`, `HashSet<User>`, etc.
- **Map types**: `Map<String, String>`, `HashMap<String, User>`, etc.
- **Arrays**: `String[]`, `User[]`, etc.
- **Content serialization**: Automatically adds `@JsonSerialize(contentUsing = ...)` for collection elements

### Import Generation
- **Automatic imports**: Generates imports for all non-java.lang types
- **Collection imports**: Adds `import java.util.List`, `import java.util.Set`, etc.
- **Entity imports**: Adds imports for external entity types from JARs
- **Smart import detection**: Only imports types that are actually used

### Type Analysis
- **Field type detection**: Uses annotation processing API to determine exact types
- **Generic type extraction**: Extracts element types from collections (e.g., `List<String>` → `String`)
- **Entity recognition**: Identifies external entity types that need imports
- **Type safety**: Ensures generated DTOs have correct types matching source classes

## Requirements

- Java 21+
- Maven 3.6+
- Jackson 2.19.1+

## License

This project is open source and available under the MIT License.

## Author
- name : Mohammed SALAMEH
- github : github.com/M-Salameh
- email : mohammedsalameh37693@gmail.com