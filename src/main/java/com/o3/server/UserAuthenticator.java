package com.o3.server;


import java.util.Hashtable;
import java.util.Map;

import com.sun.net.httpserver.BasicAuthenticator;

public class UserAuthenticator extends BasicAuthenticator {

    private Map<String, String> users = null; // Initialize 

    public UserAuthenticator(String realm) {
        super(realm);
        users = new Hashtable<>();
        users.put("dummy", "passwd"); // Add  dummy user
    }

    @Override
    public boolean checkCredentials(String user, String password) {
            if (users == null) {
                return false; // No users defined 
            }
    
            String storedPassword = users.get(user); // Get stored password for the given user
    
            if (storedPassword != null && storedPassword.equals(password)) {  // Check if passwords match
                return true; // Authentication successful
            } else {
                return false; // Authentication failed
            }
    }
    public boolean addUser(String userName, String password) {
        if (users.containsKey(userName)) {
            return false; // User already exists
        }
        users.put(userName, password);
        return true; // User added successfully
    }
}