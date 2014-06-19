/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.anynet.java.uax;

import eu.anynet.java.util.HttpClient;
import static eu.anynet.java.util.Properties.properties;
import eu.anynet.java.util.Serializable;
import eu.anynet.java.util.Serializer;
import java.io.File;
import java.io.IOException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.http.client.fluent.Form;
import org.json.simple.JSONObject;

/**
 *
 * @author sim
 */
@XmlRootElement(name = "UaxApiCredentials")
@XmlAccessorType(XmlAccessType.FIELD)
public class UaxApi extends Serializable<UaxApi>
{

   private String apikey;

   public UaxApi()
   {
      this(null);
   }

   public static UaxApi initialize()
   {
      UaxApi uax = new UaxApi("fillmeout");
      String fs = properties.get("fs.settings");
      File tsettings = new File(fs+"uaxcredentials.xml");
      Serializer<UaxApi> serializer = uax.createSerializer(tsettings);
      if(serializer.isReadyForUnserialize())
      {
         uax = serializer.unserialize();
      }
      else
      {
         uax.serialize();
         uax = null;
      }
      return uax;
   }

   public UaxApi(String apikey)
   {
      this.apikey = apikey;
      this.initSerializer(this, UaxApi.class);
   }

   @Override
   public String getSerializerFileName()
   {
      return "uaxcredentials.xml";
   }

   public String shortUrl(String url)
   {
      if(this.apikey==null || this.apikey.equals("fillmeout"))
      {
         this.apikey="fillmeout";
         this.serialize();
         return null;
      }

      try
      {
         JSONObject result = HttpClient.toJsonObject(HttpClient.Post("http://krz.link/~api/add", Form.form().add("api_key", this.apikey).add("url", url)));
         boolean success = result.get("success").toString().equals("true");
         if(success)
         {
            JSONObject data = ((JSONObject)result.get("result"));
            return data.get("shortlink").toString();
         }
      }
      catch(IOException ex)
      {
         ex.printStackTrace();
      }
      return null;
   }

}
