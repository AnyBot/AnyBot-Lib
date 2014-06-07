/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.anynet.anybot.bot;

/**
 *
 * @author sim
 */
public class ChatEvent {

   private Bot bot;
   private Network networksettings;

   public ChatEvent(Bot bot, Network networksettings) {
      this.bot = bot;
      this.networksettings = networksettings;
   }

   public Bot getBot()
   {
      return this.bot;
   }

   public Network getNetworkSettings()
   {
      return this.networksettings;
   }

   public void sendMessage(String channelornick, String message, boolean action)
   {
      if(action)
      {
         this.getBot().sendAction(channelornick, message);
      }
      else
      {
         this.getBot().sendMessage(channelornick, message);
      }
   }

   public void sendMessage(String channelornick, String message)
   {
      this.sendMessage(channelornick, message, false);
   }

}
