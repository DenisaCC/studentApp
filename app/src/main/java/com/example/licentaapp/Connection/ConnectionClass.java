package com.example.licentaapp.Connection;

import android.annotation.SuppressLint;
import android.os.StrictMode;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionClass {
    public static String ip =  "192.168.61.44";
    public static String un = "sa";
    public static String pass = "1234";
    public static String db = "BDUniversitate";
    public static String port = "1433";

    public static Connection connect() {
        Connection conn = null;
        String connURL = null;
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            connURL = "jdbc:jtds:sqlserver://" + ip + ":" + port + ";databaseName=" + db + ";user=" + un + ";password=" + pass + ";";
            conn = DriverManager.getConnection(connURL);
        } catch (SQLException se) {
            Log.e("SQL Exception", se.getMessage());
        } catch (ClassNotFoundException e) {
            Log.e("Class Not Found", e.getMessage());
        }
        return conn;
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                Log.e("SQL Exception", e.getMessage());
            }
        }
    }
}