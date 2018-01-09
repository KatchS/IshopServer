package RegisterInfo;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static infrastructure.Global.*;

@WebServlet(name = "Product", urlPatterns = "/register_prod")
public class Product extends HttpServlet {

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
        String prodID = request.getParameter("prod_id");
        String picLink = request.getParameter("picture");
        String name = request.getParameter("name");
        float price = Float.parseFloat(request.getParameter("price"));

        boolean succeeded = insertNewProdect(prodID,name,price,picLink);

        response.getWriter().write(succeeded ? "true" : "false");

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    /**
     * insert the data to the db
     *
     * @param prod_id the barcode of the product
     * @param name the name of the product
     * @param price the price of the product
     * @param picture the absulute path of the file that saves the picture
     *
     * @return true if succeeded to upload the data, false if not
     */
    private boolean insertNewProdect(String prod_id, String name, float price, String picture) {
        String query = "INSERT INTO " + PRODUCT_TABLE + " (prod_id, name, price, picture) VALUES (?,?,?,?)";
        try(
                Connection connection = DriverManager.getConnection(SQL_URL + SQL_DATABASE, SQL_USERNAME,SQL_PASSWORD);
                PreparedStatement preparedStatement = connection.prepareStatement(query)
        ){
            preparedStatement.setString(1,prod_id);
            preparedStatement.setString(2,name);
            preparedStatement.setFloat(3,price);
            preparedStatement.setString(4,picture);
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
