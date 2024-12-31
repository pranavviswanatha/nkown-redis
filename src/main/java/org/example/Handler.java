package org.example;

import java.io.*;
import java.net.Socket;

public class Handler implements Runnable {
    private final Socket clientSocket;

    public Handler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run(){
        try {
            Resp resp = new Resp(clientSocket.getInputStream());
            Value value = resp.read();
            System.out.println("Parsed RESP object:");
            System.out.println(resp.toString());
            clientSocket.getOutputStream().write(value.marshall());
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
