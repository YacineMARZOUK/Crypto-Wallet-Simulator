package repository;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() {
        try {
            Properties props = new Properties();
            InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties");
            if (input == null) {
                throw new IOException("Fichier config.properties introuvable !");
            }
            props.load(input);

            // Lire les propriétés
            String url = props.getProperty("db.url");
            String user = props.getProperty("db.user");
            String password = props.getProperty("db.password");

            Class.forName("org.postgresql.Driver");

            this.connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connexion faite avec succès ✅");

        } catch (IOException e) {
            System.err.println("Erreur de chargement du fichier de configuration !");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("Driver PostgreSQL introuvable !");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Erreur de connexion à la base de données !");
            e.printStackTrace();
        }
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}
