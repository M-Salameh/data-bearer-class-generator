package com.AutoGenClass.example;

import java.util.List;

/**
 * Simulated external entity from a JAR dependency
 */
public class UserProfile {
    private Long id;
    private String name;
    private String bio;
    private List<String> interests;

    public UserProfile() {}

    public UserProfile(Long id, String name, String bio, List<String> interests) {
        this.id = id;
        this.name = name;
        this.bio = bio;
        this.interests = interests;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public List<String> getInterests() { return interests; }
    public void setInterests(List<String> interests) { this.interests = interests; }
} 