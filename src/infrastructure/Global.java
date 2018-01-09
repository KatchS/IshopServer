package infrastructure;

import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;

public final class Global {

    private Global(){}

    // the Strings that represent the needed info to connect to MySQL
    public static final String SQL_USERNAME = ""; // the user name after creating the mysql server
    public static final String SQL_PASSWORD = ""; // the password after creating the mysql server
    public static final String SQL_URL = "jdbc:mysql:///"; // jdbc:mysql://{the computer ip, or localhost for this computer}:3306
    public static final String SQL_DATABASE = "";

    // the Strings that represent the columns inside the db
    public static final String USER_TABLE = "user";
    public static final String PRODUCT_TABLE = "product";

    /**
     * represent status
     */
    public static final String STATUS = "status";



    /**
     * create a Json object out of a received http request
     *
     * @param request the data received from client
     * @return a Json object that contains all the data from the client
     */
    public static JSONObject makeJsonObject(HttpServletRequest request){
        try {
            InputStream in = request.getInputStream();
            byte[] buffer = new byte[256];
            StringBuilder sb = new StringBuilder();
            int actuallyRead;
            while((actuallyRead = in.read(buffer)) != -1){
                sb.append(new String(buffer,0,actuallyRead));
            }
            JSONObject data = new JSONObject(sb.toString());
            return data;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
