/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

/**
 *
 * @author mercyhoush
 */
import com.sun.media.jfxmedia.logging.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;

public class Client implements Runnable {
    private boolean connected = true;
    private BufferedReader inStream;
    private PrintWriter outStream;
    private User user;

    public Client(Socket clientSocket) throws IOException {
        this.inStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.outStream = new PrintWriter(clientSocket.getOutputStream(), true);
    }

    public void notify(String message) {
        outStream.println(message);
    }

    public void run() {
        String input;

        try {
            while (connected) {
                // Get input from the client
                if ((input = inStream.readLine()) != null) {

                    // Process client input
                    Server.processInput(this, input);
                }
            }
        } catch (IOException e) {
            outStream.println("[Error]: Invalid input. Please try again.");
        }
    }

    public void stop() {
        // Thread will end safely
        connected = false;

        // Close client connection
        closeConnection();
    }

    private void closeConnection() {
        try {
            if (outStream != null) {
                outStream.print("CONNECTION_TERMINATED");
                outStream.close();
            }
            if (inStream != null) {
                inStream.close();
            }
        } catch (Exception e) {
            Logger.logMsg(Level.WARNING.intValue(), e.getMessage());
        }

    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}