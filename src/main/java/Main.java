import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.*;
import org.htmlparser.tags.Div;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

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
   
   Main mainObject = new Main();
   
   attributes.put("message", mainObject.getFirstLinkFromPage(req.queryParams("start")));
   
   new Main().insertURLIntoTable(1, req.queryParams("start"));

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
       ResultSet rs = stmt.executeQuery("SELECT MAX(id) from step");
       int maxId = 0;
       int maxSequence = 0;
       
       if  (rs.next()) 
       {
          maxId = rs.getInt(1);
       }

       rs = stmt.executeQuery("SELECT MAX(sequence_number) from step where start_id = " + startId);

       if  (rs.next()) 
       {
          maxSequence = rs.getInt(1);
       }
       
       stmt.executeUpdate("INSERT INTO step VALUES (" + (maxId + 1) + ", '" + URL + "', " + startId + ", " + (maxSequence + 1) + ")");
     } 
     catch (Exception e) 
     {
        // e.printStackTrace();
     } 
     finally 
     {
       if (connection != null) try{connection.close();} catch(SQLException e){}
     }
  }
  
  public String getFirstLinkFromPage(String URL)
  {
     Parser parser;
     
     StringBuilder strBuilder = new StringBuilder();
     
     System.out.println("*** URL:[" + URL +"]");

     try
     {
        parser = new Parser (URL);
        
        NodeList list = parser.parse (new HasAttributeFilter ("id"));
        NodeFilter filter =
           new AndFilter (
              new TagNameFilter ("DIV"),
              new HasChildFilter (
                  new TagNameFilter ("A")));

        for (NodeIterator e = parser.elements (); e.hasMoreNodes (); )
        {
            Div divNode = (Div)e.nextNode ();
            
            String divId = divNode.getAttribute("id");
            
            strBuilder.append("*** Div ID:[" + divId +"]<BR>\n");
            
            System.out.println("*** Div ID:[" + divId +"]");
        }
     } 
     catch (ParserException e1)
     {
        // TODO Auto-generated catch block
        e1.printStackTrace();
     }

     return strBuilder.toString();
  }

}
