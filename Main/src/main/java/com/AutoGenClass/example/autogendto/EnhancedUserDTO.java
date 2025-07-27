package com.AutoGenClass.example.autogendto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.Serializable;
import java.util.Objects;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.util.Set;
import com.AutoGenClass.example.UserProfile;

/**
 * Auto-generated DTO class for com.AutoGenClass.example.EnhancedUser
 */
public class EnhancedUserDTO implements Serializable {
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

    @JsonProperty("roles")
    @JsonSerialize(contentUsing = StdSerializer.class)
    private List<String> roles;

    @JsonProperty("preferences")
    @JsonSerialize(contentUsing = StdSerializer.class)
    private Set<String> preferences;

    @JsonProperty("addresses")
    @JsonSerialize(contentUsing = StdSerializer.class)
    private Map<String, String> addresses;

    @JsonProperty("password")
    @JsonSerialize(using = StdSerializer.class)
    private String password;

    @JsonProperty("createdAt")
    @JsonSerialize(using = StdSerializer.class)
    private String createdAt;

    @JsonProperty("userProfile")
    @JsonSerialize(using = StdSerializer.class)
    private UserProfile userProfile;

    public EnhancedUserDTO() {
    }

    public EnhancedUserDTO(
        Long id,
        String username,
        String email,
        String firstName,
        String lastName,
        List<String> roles,
        Set<String> preferences,
        Map<String, String> addresses,
        String password,
        String createdAt,
        UserProfile userProfile
    ) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.roles = roles;
        this.preferences = preferences;
        this.addresses = addresses;
        this.password = password;
        this.createdAt = createdAt;
        this.userProfile = userProfile;
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

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public Set<String> getPreferences() {
        return preferences;
    }

    public void setPreferences(Set<String> preferences) {
        this.preferences = preferences;
    }

    public Map<String, String> getAddresses() {
        return addresses;
    }

    public void setAddresses(Map<String, String> addresses) {
        this.addresses = addresses;
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

    public UserProfile getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        EnhancedUserDTO that = (EnhancedUserDTO) obj;
        return 
                Objects.equals(id, that.id) &&
                Objects.equals(username, that.username) &&
                Objects.equals(email, that.email) &&
                Objects.equals(firstName, that.firstName) &&
                Objects.equals(lastName, that.lastName) &&
                Objects.equals(roles, that.roles) &&
                Objects.equals(preferences, that.preferences) &&
                Objects.equals(addresses, that.addresses) &&
                Objects.equals(password, that.password) &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(userProfile, that.userProfile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            username,
            email,
            firstName,
            lastName,
            roles,
            preferences,
            addresses,
            password,
            createdAt,
            userProfile
        );
    }

    @Override
    public String toString() {
        return "EnhancedUserDTO{" +
                "id=" + id + "," +
                "username=" + username + "," +
                "email=" + email + "," +
                "firstName=" + firstName + "," +
                "lastName=" + lastName + "," +
                "roles=" + roles + "," +
                "preferences=" + preferences + "," +
                "addresses=" + addresses + "," +
                "password=" + password + "," +
                "createdAt=" + createdAt + "," +
                "userProfile=" + userProfile +
                '}';
    }
}
