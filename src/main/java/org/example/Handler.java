package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Handler implements Runnable {
    private final Socket clientSocket;

    public Handler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run(){
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String inputLine;

            // Read and respond to client messages
            while ((inputLine = reader.readLine()) != null) {
                // If the client closes the connection (EOF), exit the loop
                if (inputLine.equalsIgnoreCase("exit")) {
                    break;
                }

                // Print received message and send a response
                System.out.println("Received: " + inputLine);
                writer.println("+OK\r\n");  // Respond with "+OK"
            }
        } catch (IOException e) {
            System.out.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Error closing client connection: " + e.getMessage());
            }
        }
    }
}
