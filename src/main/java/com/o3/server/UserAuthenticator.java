package com.o3.server;

import java.sql.SQLException;

import com.sun.net.httpserver.BasicAuthenticator;

public class UserAuthenticator extends BasicAuthenticator {
    private MessageDatabase db;

    public UserAuthenticator(String realm, MessageDatabase db) { // Constructor with db
        super(realm);
        this.db = db;
    }

    @Override
    public boolean checkCredentials(String user, String password) {
        try {
            return db.validateUser(user, password); // Use database validation
        } catch (SQLException e) {
            System.err.println("Database error during authentication: " + e.getMessage());
            e.printStackTrace();
            return false; // Authentication failed due to database error
        }
    }

    public boolean addUser(String userName, String password, String email) {
        try {
            return db.addUser(userName, password, email); // Use database add user
        } catch (SQLException e) {
            System.err.println("Database error during user registration: " + e.getMessage());
            e.printStackTrace();
            return false; // User registration failed due to database error
        }
    }
}