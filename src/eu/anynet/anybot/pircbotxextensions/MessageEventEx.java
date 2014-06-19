/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.anynet.anybot.pircbotxextensions;

import eu.anynet.anybot.bot.Bot;
import eu.anynet.anybot.bot.IRCMessageArguments;
import org.pircbotx.Channel;
import org.pircbotx.User;
import org.pircbotx.hooks.events.MessageEvent;

/**
 *
 * @author sim
 */
public class MessageEventEx extends MessageEvent<Bot>
{

   private IRCMessageArguments args;

   public MessageEventEx(Bot bot, Channel channel, User user, String message) {
      super(bot, channel, user, message);
      this.args = new IRCMessageArguments(message, bot.getNick());
   }

   public IRCMessageArguments args()
   {
      return this.args;
   }

   public String getResponseTarget()
   {
      if(this.getChannel()!=null)
      {
         return this.getChannel().getName();
      }
      else if(this.getUser()!=null)
      {
         return this.getChannel().getName();
      }
      else
      {
         return null;
      }
   }

   public boolean isChannelMessage()
   {
      return this.getChannel()!=null;
   }

   public boolean isChannelAdmin()
   {
      return (this.getUser()!=null && this.getChannel()!=null &&
              (this.getChannel().getOps().contains(this.getUser()) ||
              this.getChannel().getOwners().contains(this.getUser())));
   }

   public boolean isBotAsked()
   {
      return this.args().isBotAsked();
   }

   public void respondNoHighlight(String message)
   {
      this.getBot().sendIRC().message(this.getResponseTarget(), message);
   }


}
