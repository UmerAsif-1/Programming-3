package com.o3.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class RegistrationHandler implements HttpHandler {

    private UserAuthenticator authenticator;

    public RegistrationHandler(UserAuthenticator authenticator) {
        this.authenticator = authenticator;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            handlePost(exchange);
        } else if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            handleGet(exchange);
        } else {
            handleUnsupported(exchange);
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.equals("text/plain")) {
            sendResponse(exchange, 400, "Invalid Content-Type. Expected 'text/plain'");
            return;
        }

        InputStreamReader reader = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(reader);
        String requestBody = br.readLine();

        if (requestBody == null) {
            sendResponse(exchange, 400, "Empty request body");
            return;
        }

        String[] userInfo = requestBody.split(":");
        if (userInfo.length != 2) {
            sendResponse(exchange, 400, "Invalid user information format. Expected 'username:password'");
            return;
        }

        String username = userInfo[0];
        String password = userInfo[1];

        if (authenticator.addUser(username, password)) {
            sendResponse(exchange, 201, "User registered successfully"); // 201 Created is more appropriate
        } else {
            sendResponse(exchange, 403, "User already registered"); // 403 Forbidden
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        sendResponse(exchange, 400, "Not supported");
    }

    private void handleUnsupported(HttpExchange exchange) throws IOException {
        String response = "Not supported";
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(400, responseBytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(responseBytes);
        os.close();
    }


    private void sendResponse(HttpExchange exchange, int code, String message) throws IOException {
        byte[] response = message.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(code, response.length);
        OutputStream os = exchange.getResponseBody();
        os.write(response);
        os.close();
    }
}