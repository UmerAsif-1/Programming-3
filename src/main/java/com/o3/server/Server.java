package com.o3.server;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.sql.SQLException;


public class Server implements HttpHandler {
    private MessageDatabase db; 

    public Server(MessageDatabase db) { 
        this.db = db;
    }
    @Override
    public void handle(HttpExchange t) throws IOException {
       try{
        if (t.getRequestMethod().equalsIgnoreCase("POST")) {
            handlePost(t);
        } else if (t.getRequestMethod().equalsIgnoreCase("GET")) {
            handleGet(t);
        } else {
            handleUnsupported(t);
        }
    }catch (Exception e) {  // Catch block for ALL exceptions
        System.err.println("Error in handle(): " + e.getMessage()); // Log the error!
        e.printStackTrace(); // Print the stack trace for debugging
        String errorMessage = "An error occurred: " + e.getMessage(); // Error message for the client
        sendResponse(t, 500, errorMessage); // Send 500 Internal Server Error
    } 
}
    private void handlePost(HttpExchange t) throws IOException {
        String contentType = t.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.equals("application/json")) {
            sendResponse(t, 400, "Content-Type must be application/json");
            return;
        }

        try (InputStreamReader reader = new InputStreamReader(t.getRequestBody(), StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(reader)) {

            String requestBody = br.lines().collect(Collectors.joining("\n"));

            try {
                JSONObject json = new JSONObject(requestBody);

                String recordIdentifier = json.getString("recordIdentifier");
                String recordDescription = json.getString("recordDescription");
                String recordPayload = json.getString("recordPayload");
                String recordRightAscension = json.getString("recordRightAscension");
                String recordDeclination = json.getString("recordDeclination");

                ObservationRecord record = new ObservationRecord(recordIdentifier, recordDescription, recordPayload, recordRightAscension, recordDeclination);
                db.insertMessage(record); // Store in database!

                System.out.println("Received message: " + json.toString()); // Log the JSON

                sendResponse(t, 200, ""); // 200 OK, empty body

            } catch (JSONException | IllegalArgumentException | SQLException e) { 
                sendResponse(t, 400, "Invalid JSON message data: " + e.getMessage());
            }

        }
    }

    private void handleGet(HttpExchange t) throws IOException {
       
        JSONArray jsonArray = new JSONArray();
        try {
            List<ObservationRecord> records = this.db.getMessages();
            for (ObservationRecord record : records) {
                JSONObject json = new JSONObject();
                json.put("recordIdentifier", record.getRecordIdentifier());
                json.put("recordDescription", record.getRecordDescription());
                json.put("recordPayload", record.getRecordPayload());
                json.put("recordRightAscension", record.getRecordRightAscension());
                json.put("recordDeclination", record.getRecordDeclination());
    
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
                String recordTimeReceived = record.getSent().withZoneSameInstant(ZoneOffset.UTC).format(formatter);
                json.put("recordTimeRecieved", recordTimeReceived);
    
                jsonArray.put(json);
            }
        } catch (SQLException e) {
            System.err.println("Database error retrieving messages: " + e.getMessage());
            e.printStackTrace();
        } catch (JSONException e) {
            System.err.println("Error constructing JSON: " + e.getMessage());
            e.printStackTrace();
        }
    
        String responseString = jsonArray.toString();
        byte[] responseBytes = responseString.getBytes(StandardCharsets.UTF_8);
    
        t.sendResponseHeaders(200, responseBytes.length);
        OutputStream os = t.getResponseBody();
        os.write(responseBytes);
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

    private void sendResponse(HttpExchange t, int statusCode, String response) throws IOException {
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        t.sendResponseHeaders(statusCode, responseBytes.length);
        OutputStream os = t.getResponseBody();
        os.write(responseBytes);
        os.close();
    }

    private static SSLContext myServerSSLContext(String keystorePath, String keystorePassword) throws Exception {
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(keystorePath), keystorePassword.toCharArray()); // Use arguments!

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, keystorePassword.toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        SSLContext ssl = SSLContext.getInstance("TLS");
        ssl.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        return ssl;
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: java Server <keystore_path> <keystore_password>");
            System.exit(1); // Exit with error code if arguments are missing
        }

        String keystorePath = args[0];
        String keystorePassword = args[1];
        

        try {
            SSLContext sslContext = myServerSSLContext(keystorePath, keystorePassword);

        HttpsServer server = HttpsServer.create(new InetSocketAddress(8001), 0);
        server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
            @Override
            public void configure(HttpsParameters params) {
                SSLParameters sslparams = sslContext.getDefaultSSLParameters();
                params.setSSLParameters(sslparams);
            }
        });
        MessageDatabase db = new MessageDatabase();
        db.open("MessageDB.db");
        UserAuthenticator authenticator = new UserAuthenticator("datarecord" , db); // 1. Create authenticator
        HttpContext context = server.createContext("/datarecord", new Server(db)); // 2. Get context
        context.setAuthenticator(authenticator); // 3. Set authenticator
        RegistrationHandler registrationHandler = new RegistrationHandler(authenticator, db); // For /registration
        server.createContext("/registration", registrationHandler); // No authenticator for /registration

        server.setExecutor(null);
        server.start();

        System.out.println("Server running at https://127.0.0.1:8001");

    } catch (IOException e) { // More specific catch for I/O errors
        System.err.println("Error starting server (I/O): " + e.getMessage());
        e.printStackTrace(); // Print the stack trace for debugging
        System.exit(1); 
    } catch (Exception e) { // General exception catch 
        System.err.println("Error starting server: " + e.getMessage());
        e.printStackTrace(); // Print the stack trace for debugging
        System.exit(1); 
    }
}
    

}