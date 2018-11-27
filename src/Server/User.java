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
import java.io.*;
import java.util.ArrayList;

public class User implements Serializable {
    private String userId;
    private String password;
    private static ArrayList<User> users;
    private static String userFile = System.getProperty("user.dir") + "/users.ring";

    private User(String userId, String password) {
        this.userId = userId;
        this.password = password;
    }

    public static boolean register(String userId, String password) {
        if (users == null) {
            loadUsers();
        }

        // Create user and append to user list
        User user =  new User(userId, password);

        // Verify user is not already registered
        for (User existingUser : users) {
            if (existingUser.getUserId() == userId) {
                return false;
            }
        }
        users.add(user);
        return saveUsers();
    }

    public static boolean loadUsers() {
        FileInputStream fis;
        ObjectInputStream ois;
        users = new ArrayList<>();

        // Get user file, create if it doesn't exist
        File file = new File(userFile);
        try {
            file.createNewFile();

            fis = new FileInputStream(file);
            ois = new ObjectInputStream(fis);

            User user;
            while ((user = (User) ois.readObject()) != null) {
                users.add(user);
            }

            ois.close();
            fis.close();
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public static boolean saveUsers() {

        // Create new user file if it doesn't exist
        File file = new File(userFile);
        try {
            file.createNewFile();
            // File will be overridden if it exists
            FileOutputStream fos = new FileOutputStream(file, false);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            // Write every user to file
            for (User user : users) {
                oos.writeObject(user);
            }

            oos.close();
            fos.close();
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public static ArrayList<User> getUsers() {
        if (users == null) {
            loadUsers();
        }

        return users;
    }

    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }
}
