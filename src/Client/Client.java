/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import com.sun.media.jfxmedia.logging.Logger;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Properties;
import java.util.logging.Level;

/**
 *
 * @author mercyhoush
 */
public class Client {

  private static boolean connected = true;
    private static String host;
    private static Integer port;
    private static Socket socket;
    private static BufferedReader inStream;
    private static PrintWriter outStream;
    
    public static void startClient() {
        loadConfiguration();
        establishServerConnection();
        
        System.out.println("Welcome!");
        
        try(BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))) {
            while(connected) {
                try {
                    if(inStream.ready()) {
                        String response = inStream.readLine();
                        
                        if(!response.equals("CONNECTION_TERMINATED")) {
                            System.out.println(response);
                            System.out.println("> ");
                        }
                        else connected = false;
                    }
                    
                    if(userInput.ready()) {
                        outStream.println(userInput.readLine());
                        String response = inStream.readLine();
                        System.out.println("> ");
                        if(response == null) {
                            System.out.println("[ERROR]: The connection to the server has been interrupted.");
                            break;
                        }
                        else if(!response.equals("ACK")) {
                            System.out.println(response);
                        }
                    }
                } catch(IOException e) {
                    System.out.println("[ERROR]: The connection to the server has been interrupted.");
                }
            }
            closeServerConnection();
            System.out.println("Server connection closed");
            System.exit(0);
        } catch(IOException ioe) {
            Logger.logMsg(Level.WARNING.intValue(), ioe.getMessage());
        }
    }
    
    private static void loadConfiguration() {
        Properties config = new Properties();
        
        try (FileInputStream is = new FileInputStream(System.getProperty("user.dir") + "/config.properties")) {
            config.load(is);
            
            host = config.getProperty("SERVER_HOST");
            port = Integer.parseInt(config.getProperty("SERVER_PORT"));
        } catch (Exception e) {
            System.out.println("[ChatRooms]: There's an error in your client configuration. Please update.");
            Logger.logMsg(Level.SEVERE.intValue(), e.getMessage());
            System.exit(1);
        }
    }
    
    private static void establishServerConnection() {
        try {
            socket = new Socket(host, port);
            inStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outStream = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.out.println("[ChatRooms]: A connection couldn't be made to the server. Please check to see if it's on.");
            closeServerConnection();
            System.exit(1);
        }
    }
    
    private static void closeServerConnection() {
        try {
            if(socket != null) {
                socket.close();
            }
            if(inStream != null) {
                inStream.close();
            }
            if(outStream != null) {
                outStream.close();
            }
        } catch(IOException ioe) {
            Logger.logMsg(Level.WARNING.intValue(), ioe.getMessage());
        }
    }
}
