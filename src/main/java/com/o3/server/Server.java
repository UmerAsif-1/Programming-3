package com.o3.server;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

public class Server implements HttpHandler {
    private Map<String, User> users = new HashMap<>();
    private List<String> messages = new ArrayList<>(); // Store messages
    private List<ObservationRecord> observationRecords = new ArrayList<>(); // Store observation records

    @Override
    public void handle(HttpExchange t) throws IOException {
        String path = t.getRequestURI().getPath();

        if (t.getRequestMethod().equalsIgnoreCase("POST")) {
            if (path.equals("/registration")) {
                handleUserRegistration(t); // Handle user registration
            } else if (path.equals("/observationrecord")) {
                handlePostObservationRecord(t); // Handle observation record posting
            } else {
                handleUnsupported(t);
            }
        } else if (t.getRequestMethod().equalsIgnoreCase("GET")) {
            if (path.equals("/observationrecord")) {
                handleGetObservationRecords(t); // Handle retrieving observation records
            } else {
                handleUnsupported(t);
            }
        } else {
            handleUnsupported(t);
        }
    }

    // Handle user registration (POST /registration)
    private void handleUserRegistration(HttpExchange exchange) throws IOException {
        InputStream inputStream = exchange.getRequestBody();
        String json = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining("\n"));

        try {
            JSONObject jsonObject = new JSONObject(json);
            // Validate required fields
            if (!jsonObject.has("username") || !jsonObject.has("password") || !jsonObject.has("email")) {
                sendResponse(exchange, 400, "Missing fields: username, password, and email are required");
                return;
            }

            String username = jsonObject.getString("username");
            String password = jsonObject.getString("password");
            String email = jsonObject.getString("email");

            // Check if the user already exists
            if (users.containsKey(username)) {
                sendResponse(exchange, 400, "User already exists");
                return;
            }

            // Create new user and store in the users map
            users.put(username, new User(username, password, email));
            sendResponse(exchange, 200, "User registered successfully");
        } catch (JSONException e) {
            sendResponse(exchange, 400, "Invalid JSON format or missing fields");
        }
    }

    // Handle posting an observation record (POST /observationrecord)
    private void handlePostObservationRecord(HttpExchange exchange) throws IOException {
        InputStream inputStream = exchange.getRequestBody();
        String json = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining("\n"));
    
        try {
            JSONObject jsonObject = new JSONObject(json);
    
            // More specific missing field checks:
            if (!jsonObject.has("recordIdentifier")) {
                sendResponse(exchange, 400, "Missing required field: recordIdentifier");
                return;
            }
            if (!jsonObject.has("recordDescription")) {
                sendResponse(exchange, 400, "Missing required field: recordDescription");
                return;
            }
            if (!jsonObject.has("recordPayload")) {
                sendResponse(exchange, 400, "Missing required field: recordPayload");
                return;
            }
            if (!jsonObject.has("recordRightAscension")) {
                sendResponse(exchange, 400, "Missing required field: recordRightAscension");
                return;
            }
            if (!jsonObject.has("recordDeclination")) {
                sendResponse(exchange, 400, "Missing required field: recordDeclination");
                return;
            }
    
    
            String recordIdentifier = jsonObject.getString("recordIdentifier");
            String recordDescription = jsonObject.getString("recordDescription");
            String recordPayload = jsonObject.getString("recordPayload");
            String recordRightAscension = jsonObject.getString("recordRightAscension");
            String recordDeclination = jsonObject.getString("recordDeclination");
    
            ObservationRecord observationRecord = new ObservationRecord(recordIdentifier, recordDescription,
                    recordPayload, recordRightAscension, recordDeclination);
    
            observationRecords.add(observationRecord);
    
            sendResponse(exchange, 200, "Observation record posted successfully");
    
        } catch (JSONException e) {
            sendResponse(exchange, 400, "Invalid JSON format: " + e.getMessage()); // Include exception details
        }
    }

    // Handle retrieving all observation records (GET /observationrecord)
    private void handleGetObservationRecords(HttpExchange exchange) throws IOException {
        try {
            if (observationRecords.isEmpty()) {
                exchange.sendResponseHeaders(204, -1); // No Content
                return; // Important: Exit the handler after sending the 204
            }
    
            JSONArray jsonRecords = new JSONArray();
            for (ObservationRecord record : observationRecords) {
                JSONObject jsonRecord = new JSONObject();
                jsonRecord.put("recordIdentifier", record.getRecordIdentifier());
                jsonRecord.put("recordDescription", record.getRecordDescription());
                jsonRecord.put("recordPayload", record.getRecordPayload());
                jsonRecord.put("recordRightAscension", record.getRecordRightAscension());
                jsonRecord.put("recordDeclination", record.getRecordDeclination());
                jsonRecords.put(jsonRecord);
            }
    
            String responseString = jsonRecords.toString();
            byte[] responseBytes = responseString.getBytes(StandardCharsets.UTF_8);
    
            exchange.sendResponseHeaders(200, responseBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(responseBytes);
            os.close();
    
        } catch (JSONException | IOException e) {
            String errorMessage = "Error handling GET request: " + e.getMessage(); // More specific message
            byte[] errorBytes = errorMessage.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(500, errorBytes.length); // Internal Server Error
            OutputStream os = exchange.getResponseBody();
            os.write(errorBytes);
            os.close();
            e.printStackTrace(); // Log the error on the server-side
        }
    }
    // Utility method to send responses
    private void sendResponse(HttpExchange exchange, int code, String message) throws IOException {
        byte[] response = message.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(code, response.length);
        OutputStream os = exchange.getResponseBody();
        os.write(response);
        os.close();
    }

    private void handleUnsupported(HttpExchange t) throws IOException {
        String response = "Not supported";
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        t.sendResponseHeaders(400, responseBytes.length);
        OutputStream os = t.getResponseBody();
        os.write(responseBytes);
        os.close();
    }
    private static SSLContext myServerSSLContext(String keystorePath, String keystorePassword) { // Correct placement
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS"); // Or PKCS12
            try (FileInputStream keystoreInputStream = new FileInputStream(keystorePath)) {
                keyStore.load(keystoreInputStream, keystorePassword.toCharArray());
            }

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, keystorePassword.toCharArray());

            SSLContext sslContext = SSLContext.getInstance("TLS"); // Or TLSv1.2
            sslContext.init(keyManagerFactory.getKeyManagers(), null, new SecureRandom());
            return sslContext;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error initializing SSLContext", e);
        }
    }
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: java Server <keystore_path> <keystore_password>");
            System.exit(1);
        }
    
        String keystorePath = args[0];
        String keystorePassword = args[1];
    
        try {
            SSLContext sslContext = myServerSSLContext(keystorePath, keystorePassword);
    
            // Use HttpsServer instead of HttpServer
            HttpsServer server = HttpsServer.create(new InetSocketAddress(8001), 0);
    
            // setting up the HTTPS configurator
            server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                @Override
                public void configure(HttpsParameters params) {
                    params.setSSLParameters(sslContext.getDefaultSSLParameters());
                }
            });

            UserAuthenticator authenticator = new UserAuthenticator("observationrecord"); // Realm name
    
            // Register endpoints

            Server observationHandler = new Server(); // For /observationrecord (with authentication)
            RegistrationHandler registrationHandler = new RegistrationHandler(authenticator); // For /registration

            com.sun.net.httpserver.HttpContext observationContext = server.createContext("/observationrecord", observationHandler);
            observationContext.setAuthenticator(authenticator); // Authentication for /observationrecord

            com.sun.net.httpserver.HttpContext registrationContext = server.createContext("/registration", registrationHandler);
            

            server.setExecutor(null);
            server.start();
    
            System.out.println("Server running at https://127.0.0.1:8001"); // Use HTTPS
        } catch (IOException e) { // Catch IOExceptions
            System.err.println("Error starting server (I/O): " + e.getMessage());
            e.printStackTrace(); // Print stack trace for debugging
            System.exit(1); // Exit with error code
        } catch (Exception e) { // Catch other exceptions (SSL setup, etc.)
            System.err.println("Error starting server: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for debugging
            System.exit(1); // Exit with error code
        }
    }
    

}