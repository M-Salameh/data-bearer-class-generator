package com.AutoGenClass.example.autogendto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.Serializable;
import java.util.Objects;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * Auto-generated DTO class for com.AutoGenClass.example.User
 */
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

    public UserDTO() {
    }

    public UserDTO(
        Long id,
        String username,
        String email,
        String firstName,
        String lastName,
        String password,
        String createdAt
    ) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        UserDTO that = (UserDTO) obj;
        return 
                Objects.equals(id, that.id) &&
                Objects.equals(username, that.username) &&
                Objects.equals(email, that.email) &&
                Objects.equals(firstName, that.firstName) &&
                Objects.equals(lastName, that.lastName) &&
                Objects.equals(password, that.password) &&
                Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            username,
            email,
            firstName,
            lastName,
            password,
            createdAt
        );
    }

    @Override
    public String toString() {
        return "UserDTO{" +
                "id=" + id + "," +
                "username=" + username + "," +
                "email=" + email + "," +
                "firstName=" + firstName + "," +
                "lastName=" + lastName + "," +
                "password=" + password + "," +
                "createdAt=" + createdAt +
                '}';
    }
}
