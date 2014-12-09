/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.anynet.java.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.Writer;
import org.apache.http.HttpVersion;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 *
 * @author sim
 */
public class HttpClient
{

   private static final int DEFAULT_MAXDOWNLOADINKBYTE = 2048;


   /**
    * Add default properties to the fluent object
    * @param r The request
    * @return The modified request
    */
   private static Request defaults(Request r)
   {
      return r
         .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.114 Safari/537.36")
         .connectTimeout(5000)
         .socketTimeout(5000)
         .version(HttpVersion.HTTP_1_1);
   }


   /**
    * Create GET request
    * @param url The URL
    * @return The request
    */
   public static Request Get(String url)
   {
      return defaults(Request.Get(url));
   }


   /**
    * Create POST request
    * @param url The URL
    * @param form Formdata
    * @return The request
    */
   public static Request Post(String url, Form form)
   {
      return defaults(Request.Post(url)).bodyForm(form.build());
   }


   /**
    * Request to string
    * @param request The request object
    * @param maxbyte Max bytes to fetch
    * @return The response string
    * @throws IOException
    */
   public static String toString(Request request, int maxbyte) throws IOException
   {
      return toString(request.execute(), maxbyte);
   }


   /**
    * Request to string
    * @param request The request object
    * @return The response string
    * @throws IOException
    */
   public static String toString(Request request) throws IOException
   {
      return toString(request.execute(), -1);
   }


   /**
    * Response to String
    * @param response -The response object
    * @param maxbyte Max bytes to fetch
    * @return The string
    * @throws IOException
    */
   public static String toString(Response response, int maxbyte) throws IOException
   {
      Content content = response.returnContent();
      return toString(content.asStream(), maxbyte);
   }


   /**
    * Read from inputstream and get the result as string
    * @param stream The stream
    * @param maxlength Max bytes to fetch, set &lt;1 to disable max length
    * @return The string
    * @throws IOException
    */
   public static String toString(InputStream stream, int maxlength) throws IOException
   {
      BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
      Writer resultbuffer = new StringWriter();

      char buffer[] = new char[1024];
      int totalReadSize = 0, currentRead=0, writelength=0;

      while ((currentRead = reader.read(buffer,0,buffer.length)) != -1)
      {
         totalReadSize += currentRead;
         writelength = currentRead;
         if(maxlength>0 && totalReadSize>maxlength)
         {
            writelength = maxlength-totalReadSize;
         }

         if(writelength>0)
         {
            resultbuffer.write(buffer, 0, writelength);
         }
         else
         {
            break;
         }
      }

      reader.close();
      return resultbuffer.toString();
   }


   /**
    * Response to string
    * @param response The response object
    * @return The string
    * @throws IOException
    */
   public static String toString(Response response) throws IOException
   {
      return toString(response, -1);
   }


   /**
    * Request to JSONObject (max 2048 KB)
    * @param request The request object
    * @return The JSON Object
    * @throws IOException
    */
   public static JSONObject toJsonObject(Request request) throws IOException
   {
      String text = toString(request, DEFAULT_MAXDOWNLOADINKBYTE*1024);
      JSONObject json = (JSONObject)JSONValue.parse(text);
      return json;
   }


   /**
    * Request to JSONArray (max 2048 KB)
    * @param request The request object
    * @return The JSON Array
    * @throws IOException
    */
   public static JSONArray toJsonArray(Request request) throws IOException
   {
      String text = toString(request, DEFAULT_MAXDOWNLOADINKBYTE*1024);
      JSONArray json = (JSONArray)JSONValue.parse(text);
      return json;
   }

}
