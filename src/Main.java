import java.sql.Connection;
import java.sql.DriverManager;

public class Main {
    public static void main(String[] args) {
        try {
            // Replace with your database details
            Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/testdb", "root", "password"
            );
            System.out.println("✅ Connected!");
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
