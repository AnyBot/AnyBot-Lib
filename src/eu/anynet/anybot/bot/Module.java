/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.anynet.anybot.bot;

import eu.anynet.java.util.Properties;
import eu.anynet.java.util.Regex;

/**
 *
 * @author sim
 */
public abstract class Module
{

   private Properties properties;
   private boolean isenabled;
   private Bot bot;

   public void onConnect(final ChatEvent msg) {  }

   public void onDisconnect(final ChatEvent msg) {  }

   public void onJoin(final ChatMessage msg) {  }

   public void onPart(final ChatMessage msg) {  }

   public void onKick(final ChatMessage msg) {  }

   public void onMessage(final ChatMessage msg) {  }

   public void onInvite(final ChatMessage msg) {  }

   public boolean isEnabled()
   {
      return this.isenabled;
   }

   public Properties getProperties()
   {
      if(this.properties==null)
      {
         this.properties = new Properties();
      }
      return this.properties;
   }

   public void mergeProperties(Properties props)
   {
      for(String key : props.getKeySet())
      {
         this.getProperties().set(key, props.get(key));
      }
   }

   public void launch() {  }

   public void setBot(final Bot b)
   {
      if(this.bot!=null)
      {
         throw new IllegalArgumentException("Bot object already set");
      }
      this.bot = b;
   }

   protected Bot getBot()
   {
      if(this.bot==null)
      {
         throw new IllegalArgumentException("Bot object not set");
      }
      return this.bot;
   }

   public String buildRegex(String regex)
   {
      String qnick = Regex.quote(this.getBot().getNick());
      String newrgx = "^"+qnick+"[:,]\\s+"+regex;
      return newrgx;
   }

}
