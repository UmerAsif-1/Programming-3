package com.o3.server;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement; // Import PreparedStatement
import java.util.ArrayList;
import java.util.List;

public class MessageDatabase {
    private Connection connection;

    public void open(String dbName) throws SQLException {
        File dbFile = new File(dbName);
        boolean dbExists = dbFile.exists() &&!dbFile.isDirectory();

        String connectionAddress = "jdbc:sqlite:" + dbName;
        connection = DriverManager.getConnection(connectionAddress);

        if (!dbExists) {
            initializeDatabase();
        }
    }

    private void initializeDatabase() throws SQLException {
        if (null!= connection) {
            String createUserTableSQL = "CREATE TABLE users (" +
                    "username TEXT PRIMARY KEY NOT NULL," +
                    "password TEXT NOT NULL," +
                    "email TEXT NOT NULL" +
                    ")";

            String createMessageTableSQL = "CREATE TABLE messages (" +
                    "recordIdentifier TEXT NOT NULL," +
                    "originalPostingTime INTEGER NOT NULL," + // Unix time
                    "recordDescription TEXT," +
                    "recordPayload TEXT," +
                    "recordRightAscension TEXT," +
                    "recordDeclination TEXT" +
                    ")";

            Statement createStatement = connection.createStatement();
            createStatement.executeUpdate(createUserTableSQL);
            createStatement.executeUpdate(createMessageTableSQL);
            createStatement.close();
        }
    }

    // 4. Add user registration
    public boolean addUser(String username, String password, String email) throws SQLException {
    if (isUserRegistered(username)) {
        return false; // User already exists
    }

    String insertUserSQL = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
    try (PreparedStatement preparedStatement = connection.prepareStatement(insertUserSQL)) {
        preparedStatement.setString(1, username);
        preparedStatement.setString(2, password);
        preparedStatement.setString(3, email);
        preparedStatement.executeUpdate();
        return true;
    }
}

    // Helper method to check if a user is registered
    private boolean isUserRegistered(String username) throws SQLException {
        String checkUserSQL = "SELECT COUNT(*) FROM users WHERE username = '" + username + "'";
        Statement checkStatement = connection.createStatement();
        ResultSet resultSet = checkStatement.executeQuery(checkUserSQL);
        int count = resultSet.getInt(1);
        resultSet.close();
        checkStatement.close();
        return count > 0;
    }

    // 5. Validate users
    public boolean validateUser(String username, String password) throws SQLException {
        String checkUserSQL = "SELECT password FROM users WHERE username = '" + username + "'";
        Statement checkStatement = connection.createStatement();
        ResultSet resultSet = checkStatement.executeQuery(checkUserSQL);

        if (resultSet.next()) {
            String storedPassword = resultSet.getString("password");
            resultSet.close();
            checkStatement.close();
            return storedPassword.equals(password);
        } else {
            resultSet.close();
            checkStatement.close();
            return false;
        }
    }

    // 6. Inserting new messages
    public void insertMessage(ObservationRecord record) throws SQLException {
    String insertMessageSQL = "INSERT INTO messages (recordIdentifier, originalPostingTime, recordDescription, recordPayload, recordRightAscension, recordDeclination) VALUES (?, ?, ?, ?, ?, ?)";
    try (PreparedStatement preparedStatement = connection.prepareStatement(insertMessageSQL)) {
        preparedStatement.setString(1, record.getRecordIdentifier());
        preparedStatement.setLong(2, record.dateAsInt());
        preparedStatement.setString(3, record.getRecordDescription());
        preparedStatement.setString(4, record.getRecordPayload());
        preparedStatement.setString(5, record.getRecordRightAscension());
        preparedStatement.setString(6, record.getRecordDeclination());
        preparedStatement.executeUpdate();
    }
}

    // 7. Reading messages (example - you'll need to adapt this)
    public List<ObservationRecord> getMessages() throws SQLException {
    List<ObservationRecord> messages = new ArrayList<>();
    String selectMessagesSQL = "SELECT * FROM messages";
    try (Statement selectStatement = connection.createStatement();
         ResultSet resultSet = selectStatement.executeQuery(selectMessagesSQL)) {

        while (resultSet.next()) {
            ObservationRecord record = new ObservationRecord(
                    resultSet.getString("recordIdentifier"),
                    resultSet.getString("recordDescription"),
                    resultSet.getString("recordPayload"),
                    resultSet.getString("recordRightAscension"),
                    resultSet.getString("recordDeclination")
            );
            record.setSent(resultSet.getLong("originalPostingTime")); // Convert Unix time
            messages.add(record);
        }
    }
    return messages;
}

}