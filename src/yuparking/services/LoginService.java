package yuparking.services;
import yuparking.database.Database;
import yuparking.models.User;
import yuparking.factory.UserFactory;

import java.util.ArrayList;
import java.util.List;
public class LoginService {
    private Database db;
    private List<User> users;

    public LoginService() {
        db = new Database();
        users = loadUsers();
    }

    private List<User> loadUsers() {
        List<User> userList = new ArrayList<>();
        List<String[]> userRecords = db.retrieveData("users");

        for (int i = 1; i < userRecords.size(); i++) {
            String[] row = userRecords.get(i);
            int id = Integer.parseInt(row[0]);
            String email = row[1];
            String password = row[2];
            String userType = row[3];
            User user = UserFactory.createUser(id, email, password, userType);
            userList.add(user);
        }
        return userList;
    }

    public List<User> getAllUsers() {
        return users;
    }

    public User login(String email, String password) {
        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(email) && user.getPassword().equals(password)) {
                System.out.println("Login successful: " + user.getEmail() + " (" + user.getUserType() + ")");
                return user;
            }
        }
        System.out.println("Login failed: Incorrect email or password.");
        return null;
    }

    //this will update csv after verification
    public void updateVerificationInCSV(int userId) {
        List<String[]> users = db.retrieveData("users");
        for (int i = 1; i < users.size(); i++) { 
            String[] row = users.get(i);
            if (Integer.parseInt(row[0]) == userId) {
                row[4] = "true";
                db.confirmUpdate("users", users);
                System.out.println("User verification updated in users.csv for user ID: " + userId);
                return;
            }
        }
        System.out.println("User ID not found for verification update.");
    }
    
    
    
}