package org.example;

import org.example.utils.MyDataBase;
import java.sql.Connection;

/**
 * Simple diagnostic test to verify database connection works
 */
public class TestDatabaseConnection {
    public static void main(String[] args) {
        System.out.println("========== DATABASE CONNECTION TEST ==========");
        System.out.println("Attempting to connect to: jdbc:mysql://127.0.0.1:3306/mindgrow");
        System.out.println("User: root");
        System.out.println("Password: (empty)");
        System.out.println("=============================================\n");
        
        try {
            Connection conn = MyDataBase.getConnection();
            
            if (conn == null) {
                System.err.println("✗ FAILED: Connection is NULL");
                System.exit(1);
            }
            
            if (conn.isClosed()) {
                System.err.println("✗ FAILED: Connection is CLOSED");
                System.exit(1);
            }
            
            System.out.println("✓ SUCCESS: Database connection established!");
            System.out.println("✓ Connection object: " + conn);
            System.out.println("✓ Ready to execute queries\n");
            
            System.exit(0);
        } catch (Exception e) {
            System.err.println("✗ EXCEPTION: " + e.getClass().getName());
            System.err.println("✗ Message: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
