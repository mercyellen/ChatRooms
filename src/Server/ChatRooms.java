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
public class ChatRooms {
    public static void main(String[] args) {
		try {
		    // Start chat server
			Server.start();
		} catch (Exception ex) {
			System.out.println("[ERROR]: Server could not be started. Please make sure config.properties exists and the specified port is unbound.");
		} finally {
		    Server.stop();
        }
	}
}
