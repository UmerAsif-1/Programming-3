package com.o3.server;

import java.util.Hashtable;
import java.util.Map;

import com.sun.net.httpserver.BasicAuthenticator;

public class UserAuthenticator extends BasicAuthenticator {

    private Map<String, User> users = null; // Changed to Map<String, User>

    public UserAuthenticator(String realm) {
        super(realm);
        users = new Hashtable<>();
        users.put("dummy", new User("dummy", "passwd", "dummy@example.com")); // Add dummy user as User object
    }

    @Override
    public boolean checkCredentials(String user, String password) {
        if (users == null) {
            return false; // No users defined
        }

        User storedUser = users.get(user); // Get the User object
        if (storedUser != null && storedUser.getPassword().equals(password)) { // Check password
            return true; // Authentication successful
        } else {
            return false; // Authentication failed
        }
    }

    public boolean addUser(String userName, String password, String email) { // Add email parameter
        if (users.containsKey(userName)) {
            return false; // User already exists
        }
        users.put(userName, new User(userName, password, email)); // Store User object
        return true; // User added successfully
    }
}