package Repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLOutput;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;

    private static final String URL = "jdbc:postgresql://localhost:5432/crypto_wallet_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "0000";

    private DatabaseConnection(){
        try {
            Class.forName("org.postgresql.Driver");
            this.connection = DriverManager.getConnection(URL,USER,PASSWORD);
            Systeme.out.println("connexion faite avec succee");
        }catch(ClassNotFoundException){
            System.out.println("Driver postgreSQL not found");
        }catch (SQLException){
            System.out.println("Erreur de connection");
        }
    }

    private static DatabaseConnection getInstance(){
        if(instance == null){
            synchronized (DatabaseConnection.class){
                if (instance == null){
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    public Connection getConnection(){
        return connection;
    }
}
