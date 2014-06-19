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

   private static Request defaults(Request r)
   {
      return r
         .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.114 Safari/537.36")
         .connectTimeout(5000)
         .socketTimeout(5000)
         .version(HttpVersion.HTTP_1_1);
   }

   public static Request Get(String url)
   {
      return defaults(Request.Get(url));
   }

   public static Request Post(String url, Form form)
   {
      return defaults(Request.Post(url)).bodyForm(form.build());
   }

   public static String toString(Request request, int maxinkbyte) throws IOException
   {
      return toString(request.execute(), maxinkbyte);
   }

   public static String toString(Request request) throws IOException
   {
      return toString(request.execute(), -1);
   }

   public static String toString(Response response, int maxinkbyte) throws IOException
   {
      Content content = response.returnContent();
      return toString(content.asStream(), maxinkbyte);
   }

   public static String toString(InputStream stream, int maxinkbyte) throws IOException
   {
      BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
      Writer resultbuffer = new StringWriter();

      char buffer[] = new char[1024];
      int totalReadSize = 0, currentRead=0;

      while ((currentRead = reader.read(buffer,0,buffer.length)) != -1)
      {
         totalReadSize += currentRead;
         resultbuffer.write(buffer, 0, currentRead);
         if(maxinkbyte>0 && totalReadSize>=(maxinkbyte*1024))
         {
            break;
         }
      }

      reader.close();
      return resultbuffer.toString();
   }

   public static String toString(Response response) throws IOException
   {
      return toString(response, -1);
   }

   public static JSONObject toJsonObject(Request request) throws IOException
   {
      String text = toString(request, DEFAULT_MAXDOWNLOADINKBYTE);
      JSONObject json = (JSONObject)JSONValue.parse(text);
      return json;
   }

   public static JSONArray toJsonArray(Request request) throws IOException
   {
      String text = toString(request, DEFAULT_MAXDOWNLOADINKBYTE);
      JSONArray json = (JSONArray)JSONValue.parse(text);
      return json;
   }

}
