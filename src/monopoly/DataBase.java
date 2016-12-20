package monopoly;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

public class DataBase {

    Connection connection;
    List<String> nameTable;
    ResultSet resultSet;
    ResultSet keys;
    ResultSet executeQuery;
    String tablespace;
    static final String TABLE_USERS = "USERS";
    static final String TABLE_FRIENDS = "FRIENDS";

    public DataBase(String name, String pass, String tab, String localhost) throws ClassNotFoundException, SQLException {
        Class.forName("oracle.jdbc.OracleDriver");
        Locale.setDefault(Locale.ENGLISH);
        connection = DriverManager.getConnection("jdbc:oracle:thin:@" + localhost + ":1521:xe", name, pass);
        resultSet = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE).executeQuery("SELECT * FROM user_tables");
        tablespace = tab;
    }
    
    public void insertNewUser(String login, String password, String email) throws SQLException{
        System.out.println("INSERT INTO \"" + TABLE_USERS + "\" VALUES(\'" + login+"\', \'"+ password+ "\')");
        connection.createStatement().executeQuery("INSERT INTO \"" + TABLE_USERS + "\" VALUES(\'" + login+"\', \'"+ password+ "\', \'" + email + "\')");
    }
    
    
    
   public String getPassword(String login) throws SQLException{
       
       ResultSet pass = connection.createStatement().executeQuery("SELECT PASSWORD FROM \"" + TABLE_USERS + "\"WHERE LOGIN = \'" +login+ "\'");
       pass.next();
       String s = pass.getString(1);
       return s;
   }
   public String getEmail(String login) throws SQLException{
       
       ResultSet email = connection.createStatement().executeQuery("SELECT EMAIL FROM \"" + TABLE_USERS + "\"WHERE LOGIN = \'" +login+ "\'");
       email.next();
       String s = email.getString(1);
       return s;
   }
}