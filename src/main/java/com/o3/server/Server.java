package com.o3.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

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
        
        t.sendResponseHeaders(200, 0);
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

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8001), 0);
        server.createContext("/datarecord", new Server());
        server.setExecutor(null);
        server.start();
        System.out.println("Server running at http://127.0.0.1:8001/datarecord");
    }
}