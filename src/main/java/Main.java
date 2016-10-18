import java.sql.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;

import java.net.URI;
import java.net.URISyntaxException;

import static spark.Spark.*;
import spark.template.freemarker.FreeMarkerEngine;
import spark.ModelAndView;
import static spark.Spark.get;

import com.heroku.sdk.jdbc.DatabaseUrl;

public class Main {

  public static void main(String[] args) 
  {

    port(Integer.valueOf(System.getenv("PORT")));
    staticFileLocation("/public");

    get("/hello", (req, res) -> "Hello World");

    get("/", (request, response) -> {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("message", "Hello World!");

            return new ModelAndView(attributes, "index.ftl");
        }, new FreeMarkerEngine());

    get("/db", (req, res) -> {
      Connection connection = null;
      Map<String, Object> attributes = new HashMap<>();
      try {
        connection = DatabaseUrl.extract().getConnection();

        Statement stmt = connection.createStatement();
        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ticks (tick timestamp)");
        stmt.executeUpdate("INSERT INTO ticks VALUES (now())");
        ResultSet rs = stmt.executeQuery("SELECT tick FROM ticks");

        ArrayList<String> output = new ArrayList<String>();
        while (rs.next()) {
          output.add( "Read from DB: " + rs.getTimestamp("tick"));
        }

        attributes.put("results", output);
        return new ModelAndView(attributes, "db.ftl");
      } catch (Exception e) {
        attributes.put("message", "There was an error: " + e);
        return new ModelAndView(attributes, "error.ftl");
      } finally {
        if (connection != null) try{connection.close();} catch(SQLException e){}
      }
    }, new FreeMarkerEngine());

    get("/", (request, response) -> {
       Map<String, Object> attributes = new HashMap<>();
       attributes.put("message", "Hello World!");

       return new ModelAndView(attributes, "index.ftl");
   }, new FreeMarkerEngine());

get("/findPath", (req, res) -> {
   Map<String, Object> attributes = new HashMap<>();
   attributes.put("message", req.queryParams("start"));

   return new ModelAndView(attributes, "error.ftl");
}, new FreeMarkerEngine());

 }
  
  
  public void insertURLIntoTable(int startId, String URL)
  {
     Connection connection = null;
     Map<String, Object> attributes = new HashMap<>();
     
     try {
       connection = DatabaseUrl.extract().getConnection();

       Statement stmt = connection.createStatement();
       ResultSet rs = stmt.executeQuery("SELECT MAX(id) from set");
       int maxIdi = 0;
       int maxSequence = 0;
       
       if  (rs.next()) 
       {
          maxId = rs.getInt(1);
       }

       rs = stmt.executeQuery("SELECT MAX(sequence_number) from set where startId = " + startId);

       if  (rs.next()) 
       {
          maxSequence = rs.getInt(1);
       }
       
       stmt.executeUpdate("INSERT INTO step VALUES (" + (maxId + 1) + ", " + URL + ", " + startId + ", " + (maxSequence + 1) + ")");
     } 
     catch (Exception e) 
     {
     } 
     finally 
     {
       if (connection != null) try{connection.close();} catch(SQLException e){}
     }
  }
  

}
