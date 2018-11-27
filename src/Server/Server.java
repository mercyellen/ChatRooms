/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import java.io.FileInputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.Properties;

/**
 *
 * @author mercyhoush
 */
public class Server {
    private static InetAddress host;
    private static int port;
    private static int timeout;
    private static int maxClients;
    private static boolean serverOnline = false;
    private static ServerSocket serverSocket;
    private static ArrayList<Client> clients;
    
    public static void start() throws Exception {
        System.out.println("Connecting to server...");
        loadConfiguration();
        serverSocket = new ServerSocket(port, timeout, host);
        serverOnline = true;
        clients = new ArrayList<>();
        while(serverOnline) {
            Client client = new Client(serverSocket.accept());            
            if(clients.size() < maxClients) {
                clients.add(client);
                new Thread(client).start();
                client.notify("Connection established; please login.");
            } else {
                // Server is full, stop them right here
                client.notify("The chat server is currently full. Please try again later.");
                client.stop();
            }
        }
    }

    public static String displayMenu() {
        return "options:\n\tregister <UserID> <Password>\tcreate a new account with the chat server\n\tlogin <UserID> <Password>\tlog in to the chat server\n\tsend all <message>\t\tsend message to every online user\n\tsend <UserID> <message>\t\tsend message to an online user\n\twho\t\t\t\tdisplay a list of online users\n\tlogout\t\t\t\tlog out of the server\n\n";
    }

    public static void processInput(Client client, String input) {

        // Acknowledge message
        client.notify("ACK");

        // Split input string into args
        String[] args = input.split("\\s");

        // Parse input and process request
        switch (args[0]) {
            case "newuser": {
                // Verify client is not already logged in
                if (client.getUser() != null) {
                    client.notify("[ERROR]: You are already logged in");
                    break;
                }

                // Get username, pass and login
                String userId = args[1];
                String password = args[2];

								if (userId.length() > 31 || password.length() < 4 || password.length() > 8) {
										client.notify("[ERROR]: Username must be less than 32 characters and password must be between 4-8 characters.");
										break;
								}

                if (User.register(userId, password)) {
                    client.notify("Registration successful. Please login.");
                } else {
                    client.notify("[ERROR]: Username is already in use");
                }
                break;
            }
            case "login": {
                try {
                    // Verify client is not already logged in
                    if (client.getUser() != null) {
                        client.notify("[ERROR]: You are already logged in");
                        break;
                    }

                    // Get username, pass and login
                    String username = args[1];
                    String password = args[2];

                    // Attempt login
                    User user = login(username, password);

                    // CAUTION: returns null if login failed.
                    if (user == null) {
                        client.notify("[ERROR]: Username or password incorrect.");
                    } else {
                        client.setUser(user);
                        client.notify("Welcome, " + user.getUserId() + "! You have been successfully logged in.");
                    }
                } catch (Exception e) {
                    client.notify("[ERROR]: Invalid input. Please try again.");
                }
                break;
            }
            case "send": {
                try {
                    // Verify client is logged in
                    if (client.getUser() == null) {
                        client.notify("[ERROR]: You must be logged in to send messages.");
                        break;
                    } else {
                        StringBuilder message = new StringBuilder();
                        String senderUserId = client.getUser().getUserId();

                        // Get message from input
                        for (int i = 2; i < args.length; i++) {
                            message.append(" ");
                            message.append(args[i]);
                        }

                        // Determine if the message is for another user or everyone
                        if (args[1].equals("all")) {

                            // Broadcast message
                            Server.broadcastMessage(senderUserId,"[" + senderUserId + "]:" + message.toString());
                        } else {
                            // Get username to send message to
                            String receiverUserId = args[1];

                            // Send message to user
                            if (!Server.sendMessage(senderUserId, receiverUserId, message.toString())) {
                                client.notify("[ERROR]: " + receiverUserId + " is not online");
                            }
                        }
                    }
                } catch (Exception e) {
                    client.notify("[ERROR]: Invalid input. Please try again.");
                }
                break;
            }
            case "who":
                if (client.getUser() != null) {
                    // Get users online
                    ArrayList<String> users = Server.getOnlineUsers();

                    // Create response containing online users to send to client
                    StringBuilder response = new StringBuilder("Online users: ");
                    response.append(users.toString());

                    // Display online users
                    client.notify(response.toString());
                } else {
                    client.notify("[ERROR]: You must be logged in to see a list of online users.");
                }
                break;
            case "logout":
                if (client.getUser() == null) {
                    client.notify("[ERROR]: You are not logged in.");
                    break;
                }

                // Chat server removes client
                logout(client);

                // Stop the thread
                client.stop();
                break;
            default:
                client.notify("[ERROR]: Invalid input. Please try again.");
                break;
        }
    }

    public static void stop() {
        serverOnline = false;

        try {
            // Safely stop remaining client threads
            for (Client client : clients) {
                client.stop();
            }

            // Unbind socket
            serverSocket.close();
        } catch (Exception e) {
            System.out.println("Server did not shutdown successfully.");
        }
    }

    private static void loadConfiguration() throws Exception {
        Properties config = new Properties();

        // Load server config file
        FileInputStream is = new FileInputStream(System.getProperty("user.dir") + "/config.properties");
        config.load(is);

        host = InetAddress.getByName(config.getProperty("SERVER_HOST"));
        port = Integer.parseInt(config.getProperty("SERVER_PORT"));
        timeout = Integer.parseInt(config.getProperty("SERVER_TIMEOUT"));
        maxClients = Integer.parseInt(config.getProperty("MAX_CLIENTS"));

        is.close();
    }

    private static User login(String userId, String password) {

        // Verify user information is correct
        for (User user : User.getUsers()) {
            if (user.getUserId().equals(userId) && user.getPassword().equals(password)) {
                broadcastMessage(userId, userId + " joined"); // Broadcast that user has joined chat room
                return user; // User is added to Client
            }
        }
        return null; // CAUTION: returns null if login failed
    }

	private static void broadcastMessage(String sender, String message) {

	    // Send message to every client except sender
	    for (Client client : clients) {
	        if (client.getUser() != null && !client.getUser().getUserId().equals(sender)) {
                client.notify(message);
            }
        }

        // Log in server console
        System.out.println("> " + message);
    }

    private static boolean sendMessage(String sender, String userId, String message) {

        // Check for userId in list of clients
        for (Client client : clients) {
            if (client.getUser().getUserId().equals(userId)) {

                // Send message to user if they are online
                client.notify("[" + sender + "]:" + message);

                // Log the action in the server
                System.out.println("> [" + sender + "] (to " + userId + "): " + message);
                return true;
            }
        }
        return false;
    }

    private static ArrayList<String> getOnlineUsers() {
	    ArrayList<String> onlineUsers = new ArrayList<>();

	    for (Client client : clients) {
	        onlineUsers.add(client.getUser().getUserId());
        }

        return onlineUsers;
    }

    private static void logout(Client client) {
	    // Remove client from active clients list
	    clients.remove(client);

	    // Log action
        String userId = client.getUser().getUserId();
        broadcastMessage(userId, userId + " left");
    }
}
