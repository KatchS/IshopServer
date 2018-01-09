package sign_In;

import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.concurrent.ThreadLocalRandom;


import static infrastructure.Global.*;

@WebServlet(name = "Login", urlPatterns = "/login")
public class Login extends HttpServlet {

    public final String USER_ID = "user_id";
    private final String ACTION = "action";

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            DriverManager.registerDriver(new com.mysql.jdbc.Driver()); // create the object that will start the link
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JSONObject input = makeJsonObject(request);
        JSONObject output = new JSONObject();
        try {
            int action = input.getInt(ACTION);
            switch (action){
                case 1:
                    output = getUserID(input);
                    break;
                case 2:
                    output = checkExternalId(input);
                    break;
                case 3:
                    output = registerUser(input);
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        response.setContentType("application/json");
        response.getWriter().write(output.toString());
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    private JSONObject checkExternalId(JSONObject input) throws JSONException {
        JSONObject output = new JSONObject();
        Long userId = input.getLong(USER_ID);
        if(!isIdExist(userId)) {
            if (!insertId(userId)) {
                output.put(STATUS, false);
                output.put(ACTION,2);
                return output;
            }
        }
        output.put(USER_ID, userId);
        output.put(STATUS,true);
        return output;
    }

    /**
     * insert a specific user ID to the db (that was given by the user from facebook/google)
     *
     * @param userId the given Id from
     * @return
     */
    private boolean insertId(Long userId) {
        String query = "INSERT INTO " + USER_TABLE + " (user_id) VALUES (?)";
        try(
                Connection connection = DriverManager.getConnection(SQL_URL + SQL_DATABASE, SQL_USERNAME,SQL_PASSWORD);
                PreparedStatement preparedStatement = connection.prepareStatement(query)
        ){
            preparedStatement.setLong(1, userId);
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private JSONObject registerUser(JSONObject input) throws JSONException {
        JSONObject output = new JSONObject();
        boolean registerSucc = true;
        long userID = 0;
        if(!isEmailExist(input.getString("email"))){
            userID = insertUser(input);
            if(userID == -1)
                registerSucc = false;
        } else{
            registerSucc = false;
        }
        if(registerSucc){
            output.put(STATUS,true);
            output.put(USER_ID,userID);
        }else{
            output.put(STATUS,false);
            output.put(ACTION,3);
        }
        return output;
    }

    /**
     * checks that the given email is not already in the db
     *
     * @param email the email of the user
     * @return *true* if the email exist, false if not
     */
    private boolean isEmailExist(String email) {
        String query = "SELECT user_id FROM user WHERE email='" + email + "'";
        return searchInDb(query);
    }

    /**
     * insert a user to the db
     *
     * @param input the Json object containing the data of the user
     * @return a long that represent the user ID, -1 if the insertion was unsuccessful
     * @throws JSONException in case data could not be extracted from the given Json object)
     */
    private long insertUser(JSONObject input) throws JSONException {
        long userID = -1;
        String query = "INSERT INTO " + USER_TABLE + " (email, password, user_id) VALUES (?,?,?)";
        try(
                Connection connection = DriverManager.getConnection(SQL_URL + SQL_DATABASE, SQL_USERNAME,SQL_PASSWORD);
                PreparedStatement preparedStatement = connection.prepareStatement(query)
        ){
            preparedStatement.setString(1,input.getString("email"));
            preparedStatement.setString(2,input.getString("password"));
            userID = generateUserId();
            preparedStatement.setLong(3, userID);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userID;
    }

    /**
     * generate an ID for the user
     *
     * checks that there is no ID that match the one being created (with isIdExist())
     * @return the newly generated ID
     */
    private Long generateUserId() {
        Long userID = ThreadLocalRandom.current().nextLong(999999999);
        while(isIdExist(userID))
            userID = ThreadLocalRandom.current().nextLong(999999999);
        return userID;
    }

    /**
     * checks that the given ID isn't already exist in the db
     *
     * @param userID the user ID to be checked
     * @return *true* if exist, false in not
     */
    private boolean isIdExist(Long userID){
        String query = "SELECT user_id FROM user WHERE user_id=" + userID;
        return searchInDb(query);
    }

    /**
     * search the db for the given query
     *
     * @param query the data to search
     * @return true if the query find a result in the db, false if not
     */
    private boolean searchInDb(String query){
        try(
                Connection connection = DriverManager.getConnection(SQL_URL + SQL_DATABASE, SQL_USERNAME,SQL_PASSWORD);
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query)
        ){
            if(resultSet.next()){
                return true;
            }else
                return false;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private JSONObject getUserID(JSONObject input) throws JSONException {
        String emailAdd = input.getString("email");
        String password = input.getString("password");
        String query = "SELECT user_id FROM " + USER_TABLE + " WHERE email='" + emailAdd + "' AND password='" + password+"'";
        try(
                Connection connection = DriverManager.getConnection(SQL_URL + SQL_DATABASE, SQL_USERNAME,SQL_PASSWORD);
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query)
        ){
            JSONObject output = new JSONObject();
            if(resultSet.next()){
                output.put(USER_ID, resultSet.getLong(USER_ID));
                output.put(STATUS,true);
            }else {
                output.put(STATUS, false);
                output.put(ACTION,1);
            }
            return output;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
