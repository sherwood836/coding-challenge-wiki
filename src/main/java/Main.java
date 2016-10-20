import java.sql.*;
import java.util.*;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.*;
import org.htmlparser.tags.Div;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.ParagraphTag;
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
   
   attributes.put("message", mainObject.traverseLinksToPhilosophy(req.queryParams("start")));
   
   new Main().insertURLIntoTable(1, req.queryParams("start"));

   return new ModelAndView(attributes, "error.ftl");
}, new FreeMarkerEngine());

 }
  
  public String traverseLinksToPhilosophy(String URL)
  {
     StringBuilder strBuilder = new StringBuilder();

     List<String> URLList = new ArrayList<String>();

     String nextURL = URL;
     
     int startId = insertURLIntoStartTable(URL);
     
     strBuilder.append("*** link tag text:[" + nextURL +"]<BR>\n");
     
     for (int maxJumps = 0; 
          maxJumps < 100  && !nextURL.equals("https://en.wikipedia.org/wiki/Philosophy") && !URLList.contains(nextURL); 
          maxJumps++)
     {
        URLList.add(nextURL);

        nextURL = getFirstLinkFromPage(nextURL);

        strBuilder.append("*** link tag text:[" + nextURL +"]<BR>\n");
     }
     
     insertURLIntoTable(startId, URLList);
     
     return strBuilder.toString();
  }  
  
  public void insertURLIntoTable(int startId, List<String> URLList)
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
       
       for (String URL : URLList)
       {
          stmt.executeUpdate("INSERT INTO step VALUES (" + (maxId + 1) + ", '" + URL + "', " + startId + ", " + (maxSequence + 1) + ")");
       } 
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
  
  public int insertURLIntoStartTable(String URL)
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

       rs = stmt.executeQuery("SELECT MAX(id) from start");

       if  (rs.next()) 
       {
          maxSequence = rs.getInt(1);
       }
       
       stmt.executeUpdate("INSERT INTO start VALUES (" + (maxId + 1) + ", '" + URL + "')");
       
       return maxId + 1;
     } 
     catch (Exception e) 
     {
        // e.printStackTrace();
     } 
     finally 
     {
       if (connection != null) try{connection.close();} catch(SQLException e){}
     }
     
     return -1;
  }
  
  public String getFirstLinkFromPage(String URL)
  {
     Parser parser;
     
     StringBuilder strBuilder = new StringBuilder();
     
     System.out.println("*** URL:[" + URL +"]");

     try
     {
        parser = new Parser (URL);
        
        NodeFilter filter =
           new TagNameFilter ("DIV");

        NodeList list2 = parser.parse(filter);

        for (NodeIterator e = list2.elements (); e.hasMoreNodes (); )
        {
            Div divNode = (Div)e.nextNode();
            
            String divId = divNode.getAttribute("id");
            
            if ("mw-content-text".equals(divId))
            {
               for (NodeIterator children = divNode.elements (); children.hasMoreNodes (); )
               {
                  Node child = children.nextNode();

                  if (child instanceof ParagraphTag)
                  {
                     ParagraphTag paragraphNode = (ParagraphTag)child;
                     
                     for (NodeIterator paragraph = paragraphNode.elements (); paragraph.hasMoreNodes (); )
                     {
                        Node paraChild = paragraph.nextNode();   
                        
                        if (paraChild instanceof LinkTag)
                        {
                           LinkTag link = (LinkTag)paraChild;
                           
                           String tagText = link.getLinkText();

                           if (tagText.length() > 0)
                              if (tagText.charAt(0) >= 'a' && tagText.charAt(0) <= 'z')
                              {
                                 strBuilder.append("*** link tag text:[" + link.getLinkText() +"]<BR>\n");
                                 strBuilder.append("*** link tag text:[" + link.extractLink() +"]<BR>\n");
                                 
                                 return link.extractLink();
                              }
                        }
                     }
                  }
               }
            }
        }
     } 
     catch (ParserException e1)
     {
        // TODO Auto-generated catch block
        e1.printStackTrace();
     }

     return null;
  }

}
