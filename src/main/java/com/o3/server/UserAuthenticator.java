package com.o3.server;


import java.util.Hashtable;
import java.util.Map;

import com.sun.net.httpserver.BasicAuthenticator;

public class UserAuthenticator extends BasicAuthenticator {

    private Map<String, String> users = new Hashtable<>(); // Initialize 

    public UserAuthenticator(String realm) {
        super(realm);
        
        users.put("dummy", "passwd"); // Add  dummy user
    }

    @Override
    public boolean checkCredentials(String user, String password) {
        String storedPassword = users.get(user);
        return storedPassword != null && storedPassword.equals(password);
    }

    public boolean addUser(String userName, String password) {
        if (users.containsKey(userName)) {
            return false; // User already exists
        }
        users.put(userName, password);
        return true; // User added successfully
    }
}