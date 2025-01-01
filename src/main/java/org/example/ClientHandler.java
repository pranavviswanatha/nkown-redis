package org.example;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run(){
        try {
            while(clientSocket.isConnected()){
                Resp resp = new Resp(clientSocket.getInputStream());
                Value request = null, response = null;
                try {
                    request = resp.read();
                    response = RequestHandler.handleRequest(request);
                } catch (IOException e) {
                    response = RequestHandler.errorMessage(e);
                }
                clientSocket.getOutputStream().write(response.marshall());
            }
        } catch (IOException e) {
            System.out.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                System.out.println("Client disconnected " + clientSocket.getInetAddress());
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("Error closing client connection: " + e.getMessage());
            }
        }
    }
}
