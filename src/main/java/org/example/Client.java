package org.example;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final String HOST = "127.0.0.1"; // Redis server address
    private static final int PORT = 6379;          // Redis default port

    public static void main(String[] args) {
        try (Socket socket = new Socket(HOST, PORT);
             OutputStream outputStream = socket.getOutputStream();
             InputStream inputStream = socket.getInputStream();
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Connected to Redis server at " + HOST + ":" + PORT);
            System.out.println("Type commands to interact with Redis or 'exit' to quit.");

            while (true) {
                // Read user input
                System.out.print("> ");
                String command = scanner.nextLine().trim();
                if (command.equalsIgnoreCase("exit")) {
                    System.out.println("Exiting Redis CLI. Goodbye!");
                    break;
                }

                // Convert command to RESP format and send
                byte[] request = buildRESPCommand(command);
                outputStream.write(request);
                outputStream.flush();

                // Read and print the response
                String response = readResponse(inputStream);
                System.out.println(response);
            }

        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    /**
     * Converts a Redis command into RESP format.
     *
     * @param command The command string, e.g., "SET key value"
     * @return Byte array representing the RESP command
     */
    private static byte[] buildRESPCommand(String command) {
        String[] parts = command.split("\\s+");
        StringBuilder builder = new StringBuilder();

        // Start the RESP array
        builder.append('*').append(parts.length).append("\r\n");

        // Add each part of the command as a bulk string
        for (String part : parts) {
            builder.append('$').append(part.length()).append("\r\n");
            builder.append(part).append("\r\n");
        }
//        System.out.println(builder.toString());
        return builder.toString().getBytes();
    }

    /**
     * Reads the response from the Redis server.
     *
     * @param inputStream The input stream from the server
     * @return The response as a string
     */
    private static String readResponse(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder response = new StringBuilder();
        String line;

        // Read lines until the end of the response
        while ((line = reader.readLine()) != null) {
            response.append(line).append(System.lineSeparator());}
//            if (line.isEmpty() || line.charAt(0) == '+' || line.charAt(0) == '-' || line.charAt(0) == ':' || line.charAt(0) == '*') {
//                break; // End of simple response
//            }
//            if (line.startsWith("$")) {
//                int bulkLength = Integer.parseInt(line.substring(1));
//                if (bulkLength > 0) {
//                    char[] bulkData = new char[bulkLength];
//                    reader.read(bulkData, 0, bulkLength);
//                    response.append(new String(bulkData)).append(System.lineSeparator());
//                    reader.readLine(); // Consume the trailing CRLF
//                }
//                break;
//            }
//            if (line.startsWith("*")) {
//                // Handle array responses here if needed
//                break;
//            }
//        }

        return response.toString();
    }
}
