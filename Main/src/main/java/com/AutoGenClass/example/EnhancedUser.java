package com.AutoGenClass.example;

import com.AutoGenClass.generator.AutoGen;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.util.List;
import java.util.Set;
import java.util.Map;
import com.AutoGenClass.example.UserProfile;

/**
 * Enhanced User entity demonstrating collection types and entity dependencies
 */
@AutoGen(
    simpleFields = {"id", "username", "email", "firstName", "lastName", "roles", "preferences", "addresses"},
    serializedFields = {"password", "createdAt", "userProfile"},
    serializers = {"com.fasterxml.jackson.databind.ser.std.StdSerializer", "com.fasterxml.jackson.databind.ser.std.StdSerializer", "com.fasterxml.jackson.databind.ser.std.StdSerializer"},
    name = "EnhancedUserDTO"
)
public class EnhancedUser {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String password;
    private String createdAt;
    private String profile;
    
    // Collection fields
    private List<String> roles;
    private Set<String> preferences;
    private Map<String, String> addresses;
    
    // Entity dependencies (simulating external entities from JARs)
    private UserProfile userProfile;
    private List<UserProfile> relatedProfiles;
    private Map<String, UserProfile> profileMap;

    public EnhancedUser() {}

    public EnhancedUser(Long id, String username, String email, String firstName, String lastName, 
                       String password, String createdAt, String profile, List<String> roles, 
                       Set<String> preferences, Map<String, String> addresses, UserProfile userProfile,
                       List<UserProfile> relatedProfiles, Map<String, UserProfile> profileMap) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.createdAt = createdAt;
        this.profile = profile;
        this.roles = roles;
        this.preferences = preferences;
        this.addresses = addresses;
        this.userProfile = userProfile;
        this.relatedProfiles = relatedProfiles;
        this.profileMap = profileMap;
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

    public String getProfile() { return profile; }
    public void setProfile(String profile) { this.profile = profile; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }

    public Set<String> getPreferences() { return preferences; }
    public void setPreferences(Set<String> preferences) { this.preferences = preferences; }

    public Map<String, String> getAddresses() { return addresses; }
    public void setAddresses(Map<String, String> addresses) { this.addresses = addresses; }

    public UserProfile getUserProfile() { return userProfile; }
    public void setUserProfile(UserProfile userProfile) { this.userProfile = userProfile; }

    public List<UserProfile> getRelatedProfiles() { return relatedProfiles; }
    public void setRelatedProfiles(List<UserProfile> relatedProfiles) { this.relatedProfiles = relatedProfiles; }

    public Map<String, UserProfile> getProfileMap() { return profileMap; }
    public void setProfileMap(Map<String, UserProfile> profileMap) { this.profileMap = profileMap; }
}

 