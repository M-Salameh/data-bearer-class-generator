package com.AutoGenClass;

import com.AutoGenClass.example.User;
import com.AutoGenClass.example.EnhancedUser;
import com.AutoGenClass.example.UserProfile;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

/**
 * Main class demonstrating the use of auto-generated DTOs
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("Auto-Generated Classes Demo");
        System.out.println("==========================");
        
        // Create a User entity
        User user = new User(1L, "john_doe", "john@example.com", "John", "Doe", "password123", "2024-01-01");
        
        // Create an EnhancedUser entity with collections and entities
        List<String> roles = Arrays.asList("ADMIN", "USER", "MODERATOR");
        Set<String> preferences = new HashSet<>(Arrays.asList("dark_mode", "notifications", "email_alerts"));
        Map<String, String> addresses = new HashMap<>();
        addresses.put("home", "123 Main Street, City, Country");
        addresses.put("work", "456 Business Avenue, Downtown, Country");
        addresses.put("billing", "789 Finance Road, Business District, Country");
        
        UserProfile userProfile = new UserProfile(1L, "John Doe", "Senior Software Developer", 
            Arrays.asList("Java", "Spring", "Microservices", "Docker"));
        List<UserProfile> relatedProfiles = Arrays.asList(
            new UserProfile(2L, "Jane Smith", "Product Manager", Arrays.asList("Agile", "Scrum", "Product Strategy")),
            new UserProfile(3L, "Bob Johnson", "DevOps Engineer", Arrays.asList("Kubernetes", "AWS", "CI/CD"))
        );
        
        Map<String, UserProfile> profileMap = new HashMap<>();
        profileMap.put("primary", userProfile);
        profileMap.put("secondary", relatedProfiles.get(0));
        
        EnhancedUser enhancedUser = new EnhancedUser(
            1L, "john_doe", "john@example.com", "John", "Doe", 
            "password123", "2024-01-01", "profile_data_here",
            roles, preferences, addresses, userProfile, relatedProfiles, profileMap
        );
        
    }
}