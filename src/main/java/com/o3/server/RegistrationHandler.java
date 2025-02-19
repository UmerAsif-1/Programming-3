package com.o3.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class RegistrationHandler implements HttpHandler {
// private static final Map<String, User> registeredUsers = new HashMap<>();
    private UserAuthenticator authenticator;
    private MessageDatabase db; 

    public RegistrationHandler(UserAuthenticator authenticator) {
        this.authenticator = authenticator;
    }
    public RegistrationHandler(UserAuthenticator authenticator, MessageDatabase db) { // Add db to constructor
        this.authenticator = authenticator;
        this.db = db;
    }

    @Override
    public void handle(HttpExchange t) throws IOException {
        if (t.getRequestMethod().equalsIgnoreCase("POST")) {
            handlePost(t);
        } else if (t.getRequestMethod().equalsIgnoreCase("GET")) {
            handleGet(t);
        } else {
            handleUnsupported(t);
        }
    }

    private void handlePost(HttpExchange t) throws IOException {
        String contentType = t.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.equals("application/json")) {
            String response = "Content-Type must be application/json";
            t.sendResponseHeaders(400, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
            return;
        }

        InputStreamReader reader = new InputStreamReader(t.getRequestBody(), StandardCharsets.UTF_8);
        String requestBody = new BufferedReader(reader).lines().collect(Collectors.joining("\n"));

        // String[] parts = requestBody.split(":");
        // if (parts.length != 2) {
        //     String response = "Invalid registration data. Format must be username:password";
        //     t.sendResponseHeaders(400, response.length());
        //     OutputStream os = t.getResponseBody();
        //     os.write(response.getBytes());
        //     os.close();
        //     return;
        // }

        // String username = parts[0];
        // String password = parts[1];

        // if (authenticator.addUser(username, password)) {
        //     String response = "User registered successfully"; 
        //     t.sendResponseHeaders(201, response.length());
        //     OutputStream os = t.getResponseBody();
        //     os.write(response.getBytes());
        //     os.close();
        // } else {
        //     String response = "User already registered";
        //     t.sendResponseHeaders(403, response.length());
        //     OutputStream os = t.getResponseBody();
        //     os.write(response.getBytes());
        //     os.close();
        // }
        try {
            JSONObject json = new JSONObject(requestBody);
            
            if (!json.has("username") || !json.has("password") || !json.has("email")) {
                throw new JSONException("Missing required fields");
            }
            
            String username = json.getString("username");
            String password = json.getString("password");
            String email = json.getString("email");

            if (db.addUser(username, password, email)) {
                String response = "User registered successfully";
                t.sendResponseHeaders(201, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                String response = "User already registered";
                t.sendResponseHeaders(403, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }  catch (SQLException e) {
                String response = "Database error: " + e.getMessage();
                t.sendResponseHeaders(500, response.length()); // Internal Server Error
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
                e.printStackTrace(); // Log the error
            }

        } catch (JSONException e) {
            String response = "Invalid JSON format: " + e.getMessage();
            t.sendResponseHeaders(400, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
    }

    private void handleGet(HttpExchange t) throws IOException {
        String response = "Not supported";
        t.sendResponseHeaders(400, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
    private void handleUnsupported(HttpExchange t) throws IOException {
        String response = "Not supported";
        t.sendResponseHeaders(400, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}