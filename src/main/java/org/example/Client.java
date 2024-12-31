package org.example;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private static void sendCommand(String host, int port, String command) {
        try (Socket socket = new Socket(host, port);
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("Connected to server at " + host + ":" + port);

            // Send the command
            System.out.println("Sending command: " + command);
            writer.print(command);
            writer.flush();

            // Read the response
            while (true) {
                String response = reader.readLine();
                if (response==null) break;
                System.out.println("Server response: " + response);
            }

        } catch (IOException e) {
            System.err.println("Error communicating with server: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // Define the server address and port
        String serverHost = "localhost";
        int serverPort = 6379;

        // Example RESP commands to test
        String bulkCommand = "$5\r\nHello\r\n";
        String arrayCommand = "*2\r\n$5\r\nHello\r\n$5\r\nWorld\r\n";

        // Send commands to the server
        sendCommand(serverHost,serverPort,bulkCommand);
        sendCommand(serverHost,serverPort,arrayCommand);
    }
}

