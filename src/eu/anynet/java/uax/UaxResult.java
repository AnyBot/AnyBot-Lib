/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.anynet.java.uax;

import org.json.simple.JSONObject;

/**
 *
 * @author sim
 */
public class UaxResult
{

   private JSONObject res;

   public UaxResult(JSONObject result)
   {
      this.res=result;

   }

   public boolean isSuccess()
   {
      return this.res.get("success").toString().equals("true");
   }

   public JSONObject getResult()
   {
      return ((JSONObject)this.res.get("result"));
   }

   public String getShortLink()
   {
      return this.getResult().get("shortlink").toString();
   }

   public String getUserdefLink()
   {
      Object o = this.getResult().get("userdeflink");
      if(o==null)
      {
         return null;
      }
      return o.toString();
   }

}
