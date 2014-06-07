/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.anynet.java.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author perry
 */
public class Properties
{

   public static final Properties properties = new Properties();

   private HashMap<String,String> propvalues;

   public Properties()
   {
      this.propvalues = new HashMap<>();
   }

   public ArrayList<String> getKeySet()
   {
      ArrayList<String> result = new ArrayList<>();
      result.addAll(this.propvalues.keySet());
      return result;
   }

   public void set(String key, String value)
   {
      this.propvalues.put(key, value);
   }

   public String get(String key)
   {
      return this.propvalues.get(key);
   }

   public int getInt(String key)
   {
      return Integer.parseInt(this.get(key));
   }

   public double getDouble(String key)
   {
      return Double.parseDouble(this.get(key));
   }

   public long getLong(String key)
   {
      return Long.parseLong(this.get(key));
   }

   public File getFile(String key)
   {
      return new File(this.get(key));
   }

   public boolean getBoolean(String key)
   {
      return (this.get(key).equals("true") || this.get(key).equals("1") || this.get(key).equals("yes"));
   }


}
