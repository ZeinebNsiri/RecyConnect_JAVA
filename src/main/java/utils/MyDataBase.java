package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {

    private final String URL = "jdbc:mysql://localhost:3306/recyconnect5";
    private final String USERNAME = "root";
    private final String PASSWORD = "";
    private Connection conx ;
    public static MyDataBase instance;

    private MyDataBase() {
        try {
            conx = DriverManager.getConnection(URL,USERNAME,PASSWORD);
            System.out.println("connexion etablit");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    public Connection getConx() {
        return conx;
    }

    public static MyDataBase getInstance() {
        if (instance == null)
            instance = new MyDataBase();
        return instance;


    }


}
