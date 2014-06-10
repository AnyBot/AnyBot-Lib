/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.anynet.anybot.bot;

import eu.anynet.anybot.pircbotxextensions.MessageEventEx;
import eu.anynet.java.util.Properties;
import eu.anynet.java.util.Regex;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

/**
 *
 * @author sim
 */
public abstract class Module extends ListenerAdapter<Bot>
{

   private Properties properties;
   private boolean isenabled;
   private Bot bot;
   private ModuleInfo moduleinfo;
   private Network networksettings;


   public ModuleInfo getModuleinfo()
   {
      return moduleinfo;
   }

   public void setModuleinfo(ModuleInfo moduleinfo)
   {
      this.moduleinfo = moduleinfo;
   }

   public Network getNetworksettings() {
      return networksettings;
   }

   public void setNetworksettings(Network networksettings) {
      this.networksettings = networksettings;
   }

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
   public void dispose() {  }

   public void setBot(final Bot b)
   {
      if(this.bot!=null)
      {
         throw new IllegalArgumentException("Bot object already set");
      }
      this.bot = b;
   }

   public Bot getBot()
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

   @Override
   public void onMessage(MessageEvent<Bot> event) throws Exception
   {
      MessageEventEx ex = new MessageEventEx(event.getBot(), event.getChannel(), event.getUser(), event.getMessage());
      this.onMessage(ex);
   }

   public void onMessage(MessageEventEx event) throws Exception {  }

}
