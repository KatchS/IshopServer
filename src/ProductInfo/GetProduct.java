package ProductInfo;

import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;

import static infrastructure.Global.*;

@WebServlet(name = "GetProduct", urlPatterns = "/product")
public class GetProduct extends HttpServlet {

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
            output = getProduct(input.getString("prod_id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        response.setContentType("application/json");
        response.getWriter().write(output.toString());
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    private JSONObject getProduct(String product_id) throws JSONException {
        String query = "SELECT * FROM " + PRODUCT_TABLE + " WHERE prod_id='" + product_id + "'";
        try(
                Connection connection = DriverManager.getConnection(SQL_URL + SQL_DATABASE, SQL_USERNAME,SQL_PASSWORD);
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query)
        ){
            JSONObject output = new JSONObject();
            if(resultSet.next()){
                output.put("name", resultSet.getString("name"));
                output.put("price", resultSet.getFloat("price"));
                output.put("prod_id", resultSet.getString("prod_id"));
                output.put("picture", resultSet.getString("picture"));
                output.put(STATUS,true);
            }else
                output.put(STATUS, false);
            return output;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
