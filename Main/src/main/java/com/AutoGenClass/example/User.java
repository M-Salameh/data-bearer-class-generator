package com.AutoGenClass.example;

import com.AutoGenClass.generator.AutoGen;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * Example entity class that demonstrates the use of @AutoGen annotation
 * to automatically generate a DTO class at compile time.
 * This example does not specify a module, so the DTO will be created
 * in the same module as this class (Main module).
 */
@AutoGen(
    simpleFields = {"id", "username", "email", "firstName", "lastName"},
    serializedFields = {"password", "createdAt"},
    serializers = {"com.fasterxml.jackson.databind.ser.std.StdSerializer", "com.fasterxml.jackson.databind.ser.std.StdSerializer"},
    name = "UserDTO", module = "Main"
)
public class User {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String password;
    private String createdAt;

    // Constructors
    public User() {}

    public User(Long id, String username, String email, String firstName, String lastName, String password, String createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
} 