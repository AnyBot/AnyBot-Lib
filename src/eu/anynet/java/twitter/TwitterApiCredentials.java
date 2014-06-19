/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.anynet.java.twitter;

import eu.anynet.java.util.HttpClient;
import eu.anynet.java.util.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.fluent.Form;
import org.apache.http.client.fluent.Request;
import org.json.simple.JSONObject;

/**
 *
 * @author sim
 */
@XmlRootElement(name = "TwitterApiCredentials")
@XmlAccessorType(XmlAccessType.FIELD)
public class TwitterApiCredentials extends Serializable<TwitterApiCredentials>
{

   private String applicationApiKey;
   private String applicationApiSecret;
   private String bearerToken;

   public TwitterApiCredentials()
   {
      this(null, null);
   }

   public TwitterApiCredentials(String key, String secret)
   {
      this.initSerializer(this, TwitterApiCredentials.class);
      this.applicationApiKey = key;
      this.applicationApiSecret = secret;
      this.bearerToken = null;
   }

   public boolean isCredentialsAvailable()
   {
      return (this.applicationApiKey!=null && this.applicationApiKey.length()>0 &&
              this.applicationApiSecret!=null && this.applicationApiSecret.length()>0);
   }

   public String getBasicToken()
   {
      String cred = this.applicationApiKey+":"+this.applicationApiSecret;
      return Base64.encodeBase64String(cred.getBytes());
   }

   public boolean isBearerTokenAvailable()
   {
      return (this.bearerToken!=null && this.bearerToken.length()>0);
   }

   public boolean generateBearerToken()
   {
      if(!this.isBearerTokenAvailable())
      {
         try
         {

            Request request = HttpClient.Post("https://api.twitter.com/oauth2/token", Form.form().add("grant_type", "client_credentials"))
               .addHeader("Authorization", "Basic "+this.getBasicToken());

            JSONObject bearer_json = HttpClient.toJsonObject(request);
            String access_token = bearer_json.get("access_token").toString();

            if(access_token!=null && access_token.length()>0)
            {
               this.setBearerToken(access_token);
               System.out.println("Created twitter access token");
               this.serialize();
               return true;
            }
         } catch(Exception ex) {
            ex.printStackTrace();
         }
      }
      return false;
   }

   public boolean invalidateBearerToken()
   {
      if(this.isBearerTokenAvailable())
      {
         try
         {

            Request request = HttpClient.Post("https://api.twitter.com/oauth2/invalidate_token", Form.form().add("access_token", this.getBearerToken()))
               .addHeader("Authorization", "Basic "+this.getBasicToken());

            JSONObject bearer_json = HttpClient.toJsonObject(request);
            String access_token = bearer_json.get("access_token").toString();

            if(access_token!=null && access_token.length()>0)
            {
               this.setBearerToken(null);
               this.serialize();
               return true;
            }
         } catch(Exception ex) {
            ex.printStackTrace();
         }
      }
      return false;
   }

   public void setBearerToken(String bearertoken)
   {
      this.bearerToken = bearertoken;
   }

   public String getBearerToken()
   {
      return this.bearerToken;
   }

   @Override
   public String getSerializerFileName() {
      return "twittercredentials.xml";
   }

}
