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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsServer;



public class Server implements HttpHandler {
    private List<String> messages = new ArrayList<>(); // Store messages
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
        InputStream inputStream = t.getRequestBody();
        String message = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines().collect(Collectors.joining("\n"));
        messages.add(message);
    
        System.out.println("Received message: " + message); // Confirmation!
    
        t.sendResponseHeaders(200, 0); // Or send a more informative response
        t.getResponseBody().close();
    }

    private void handleGet(HttpExchange t) throws IOException {
        String responseString = messages.isEmpty() ? "No messages" : String.join("\n", messages);
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
        UserAuthenticator authenticator = new UserAuthenticator("datarecord");
        HttpContext context = server.createContext("/datarecord", new Server()); // 2. Get context
        context.setAuthenticator(authenticator); // 3. Set authenticator
        RegistrationHandler registrationHandler = new RegistrationHandler(authenticator); // For /registration
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