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

import com.heroku.sdk.jdbc.DatabaseUrl;


public class WikipediaTraverser
{
   public String traverseLinksToPhilosophy(String URL)
   {
      StringBuilder strBuilder = new StringBuilder();

      List<String> URLList = new ArrayList<String>();

      String nextURL = URL;
      
      int startId = insertURLIntoStartTable(URL);
      
      strBuilder.append("*** Starting URL:[" + nextURL +"]<BR>\n");
      
      int maxJumps = 0;
      
      while (maxJumps < 100
             && null != nextURL
             && !"https://en.wikipedia.org/wiki/Philosophy".equals(nextURL) 
             && !URLList.contains(nextURL))
      {
         URLList.add(nextURL);

         nextURL = getFirstLinkFromPage(nextURL);

         strBuilder.append("*** Next URL:[" + nextURL +"]<BR>\n");
         
         maxJumps++;
      }
      
      if ("https://en.wikipedia.org/wiki/Philosophy".equals(nextURL))
         URLList.add("https://en.wikipedia.org/wiki/Philosophy");
      
      strBuilder.append("*** Number of jumps:[" + maxJumps +"]<BR>\n");
      
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
           stmt.executeUpdate("INSERT INTO step VALUES (" + (++maxId) + ", '" + URL + "', " + startId + ", " + (++maxSequence) + ")");
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
        ResultSet rs = stmt.executeQuery("SELECT MAX(id) from start");
        int maxId = 0;
        int maxSequence = 0;
        
        if  (rs.next()) 
        {
           maxId = rs.getInt(1);
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
