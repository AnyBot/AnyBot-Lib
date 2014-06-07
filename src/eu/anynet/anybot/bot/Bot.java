/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.anynet.anybot.bot;

import java.io.IOException;
import java.util.ArrayList;
import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;

/**
 *
 * @author sim
 */
public class Bot extends PircBot
{

   private final ArrayList<Module> modules = new ArrayList<>();
   private boolean autoreconnect=false;
   private String debugChannel;
   private final Network networksettings;

   public Bot(Network networksettings)
   {
      this.networksettings = networksettings;
      this.setLogin(this.networksettings.getBotIdent());
      this.setVersion(this.networksettings.getBotRealname());
      this.setFinger("AnyBot 1.0");
   }


   public Network getNetworkSettings()
   {
      return this.networksettings;
   }

   public void setDebugChannel(String channel)
   {
      if(channel==null || channel.trim().length()<1)
      {
         this.debugChannel=null;
      }
      else if(channel.startsWith("#") && channel.length()>1)
      {
         this.debugChannel = channel;
      }
   }

   public String getDebugChannel()
   {
      return this.debugChannel;
   }

   public boolean isDebugChannelSet()
   {
      return (this.debugChannel!=null && this.debugChannel.startsWith("#") && this.debugChannel.length()>1);
   }

   public void sendDebug(String message)
   {
      if(this.isDebugChannelSet())
      {
         this.sendMessage(this.getDebugChannel(), message);
      }
   }

   public void addModule(Module newmod)
   {
      if(!this.modules.contains(newmod))
      {
         newmod.setBot(this);
         this.modules.add(newmod);
      }
   }

   private synchronized ArrayList<Module> cloneModuleList()
   {
      return (ArrayList<Module>) this.modules.clone();
   }

   public void enableAutoReconnect(boolean b)
   {
      this.autoreconnect = b;
   }

   public void enableAutoReconnect()
   {
      this.enableAutoReconnect(true);
   }

   public void disableAutoReconnect()
   {
      this.enableAutoReconnect(false);
   }

   public boolean isAutoReconnectEnabled()
   {
      return this.autoreconnect;
   }

   public boolean isChannelOperator(String channel, String nick)
   {
      User[] users = this.getUsers(channel);
      for(User user : users)
      {
         if(user.getNick().equals(nick))
         {
            if(user.isOp())
            {
               return true;
            }
            break;
         }
      }
      return false;
   }

   @Override
   public void onMessage(String channel, String sender, String login, String hostname, String message)
   {
      if(!(channel!=null && this.getDebugChannel()!=null && channel.equals(this.getDebugChannel())))
      {
         ChatMessage newmsg = new ChatMessage(this, this.networksettings);
         newmsg.setChannel(channel);
         newmsg.setNick(sender);
         newmsg.setIdent(login);
         newmsg.setHost(hostname);
         newmsg.setMessage(message);

         ArrayList<Module> locallist = this.cloneModuleList();
         for (Module listener : locallist) {
            listener.onMessage(newmsg);
         }
      }
   }

   @Override
   public void onConnect()
   {
      //System.out.println("Connected!");
      ArrayList<Module> locallist = this.cloneModuleList();
      for (Module listener : locallist) {
         listener.onConnect(new ChatEvent(this, this.networksettings));
      }
   }

   @Override
   public void onDisconnect()
   {
      if (this.isAutoReconnectEnabled())
      {
         try {
            while(true)
            {
               try {
                  this.reconnect();
                  break;
               }
               catch(IrcException|IOException ex)
               {
                  Thread.sleep(60000);
               }
            }
         } catch (InterruptedException ex) {  }
         return;
      }

      ArrayList<Module> locallist = this.cloneModuleList();
      for (Module listener : locallist) {
         listener.onDisconnect(new ChatEvent(this, this.networksettings));
      }
   }

   @Override
   public void onInvite(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String channel)
   {
      ChatMessage newmsg = new ChatMessage(this, this.networksettings);
      newmsg.setChannel(channel);
      newmsg.setNick(sourceNick);
      newmsg.setIdent(sourceLogin);
      newmsg.setHost(sourceHostname);
      newmsg.setMessage(targetNick);

      ArrayList<Module> locallist = this.cloneModuleList();
      for (Module listener : locallist) {
         listener.onInvite(newmsg);
      }
   }

   @Override
   protected void onJoin(String channel, String sender, String login, String hostname)
   {
      ChatMessage newmsg = new ChatMessage(this, this.networksettings);
      newmsg.setChannel(channel);
      newmsg.setNick(sender);
      newmsg.setIdent(login);
      newmsg.setHost(hostname);
      newmsg.setMessage("join");

      ArrayList<Module> locallist = this.cloneModuleList();
      for (Module listener : locallist) {
         listener.onJoin(newmsg);
      }
   }

   @Override
   protected void onPart(String channel, String sender, String login, String hostname)
   {
      ChatMessage newmsg = new ChatMessage(this, this.networksettings);
      newmsg.setChannel(channel);
      newmsg.setNick(sender);
      newmsg.setIdent(login);
      newmsg.setHost(hostname);
      newmsg.setMessage("part");

      ArrayList<Module> locallist = this.cloneModuleList();
      for (Module listener : locallist) {
         listener.onPart(newmsg);
      }
   }

   @Override
   protected void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason)
   {
      ChatMessage newmsg = new ChatMessage(this, this.networksettings);
      newmsg.setChannel(channel);
      newmsg.setNick(kickerNick);
      newmsg.setIdent(kickerLogin);
      newmsg.setHost(kickerHostname);
      newmsg.setRecipient(recipientNick);
      newmsg.setMessage(reason);

      ArrayList<Module> locallist = this.cloneModuleList();
      for (Module listener : locallist) {
         listener.onKick(newmsg);
      }
   }

   /*
   @Override
   protected void handleLine(String line)
   {
      super.handleLine(line);
      System.out.println("[DEBUG] ("+this.getServer()+") "+line);
   }
   */

}