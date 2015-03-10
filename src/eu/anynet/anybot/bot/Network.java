/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.anynet.anybot.bot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author sim
 */
@XmlRootElement(name = "NetworkSettings")
@XmlAccessorType(XmlAccessType.FIELD)
public class Network
{

   private String key;
   private boolean autostart;
   private String host;
   private int port;
   private boolean ssl;
   private String botNickname;
   private String botIdent;
   private String botRealname;
   private String debugChannel;

   @XmlTransient
   private BotThread botthread;

   @XmlTransient
   private Thread output;

   @XmlTransient
   private NetworkSettingsStore networkstore;

   @XmlElementWrapper(name = "AfterConnectCommands")
   @XmlElement(name = "IRCCommand")
   private final ArrayList<IRCCommand> afterConnectCommands;

   @XmlElementWrapper(name = "BeforeDisconnectCommands")
   @XmlElement(name = "IRCCommand")
   private final ArrayList<IRCCommand> beforeDisconnectCommands;

   @XmlElementWrapper(name = "JoinedChannels")
   @XmlElement(name = "JoinedChannel")
   private final ArrayList<String> joinedChannels;

   @XmlElementWrapper(name = "EnabledModules")
   @XmlElement(name = "Module")
   private final ArrayList<String> enabledModules;


   public Network()
   {
      this.autostart = false;
      this.afterConnectCommands = new ArrayList<>();
      this.beforeDisconnectCommands = new ArrayList<>();
      this.joinedChannels = new ArrayList<>();
      this.enabledModules = new ArrayList<>();
      this.botthread = null;
      this.output = null;
   }

   public void setNetworkStore(NetworkSettingsStore store)
   {
      this.networkstore = store;
   }

   public BotThread getBotThread() throws IOException
   {
      if(this.botthread==null || this.botthread.isInterrupted())
      {
         this.botthread = new BotThread(this);
         final Network net = this;

         if(this.output!=null && (this.output.isAlive() || !this.output.isInterrupted()))
         {
            this.output.interrupt();
            this.output = null;
         }

         this.output = new Thread("network-output-"+net.getKey()) {
            @Override
            public void run()
            {
               while(true)
               {
                  try
                  {
                        String msg = net.botthread.getPipeEndpoint().receive();
                        System.out.println("["+net.getKey()+"] "+msg);
                  }
                  catch (Exception ex)
                  {
                     Logger.getLogger(BotThread.class.getName()).log(Level.SEVERE, null, ex);
                     try {
                        Thread.sleep(5000);
                     } catch (InterruptedException ex1) {
                        break;
                     }
                  }
               }
            }
         };

         this.output.start();
      }
      return this.botthread;
   }

   public boolean isRunning()
   {
      return (this.botthread!=null && this.botthread.isAlive() && !this.botthread.isInterrupted());
   }

   public void stop()
   {
      if(this.botthread.isAlive() || !this.botthread.isInterrupted())
      {
         this.botthread.interrupt();
         this.botthread = null;
      }
      if(this.output.isAlive() || this.output.isInterrupted())
      {
         this.output.interrupt();
         this.output = null;
      }
   }

   public void start() throws IOException
   {
      if(this.botthread==null && this.output==null)
      {
         this.getBotThread().start();
      }
   }

   public void setAutostart(boolean b)
   {
      this.autostart = b;
   }

   public boolean isAutostartEnabled()
   {
      return this.autostart;
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

   public String getKey() {
      return key;
   }

   public void setKey(String key) {
      this.key = key;
   }

   public String getHost() {
      return host;
   }

   public void setHost(String host) {
      this.host = host;
   }

   public int getPort() {
      return port;
   }

   public void setPort(int port) {
      this.port = port;
   }

   public boolean isSsl() {
      return ssl;
   }

   public void setSsl(boolean ssl) {
      this.ssl = ssl;
   }

   public String getBotNickname() {
      return botNickname;
   }

   public void setBotNickname(String botNickname) {
      this.botNickname = botNickname;
   }

   public String getBotIdent() {
      return botIdent;
   }

   public void setBotIdent(String botIdent) {
      this.botIdent = botIdent;
   }

   public String getBotRealname() {
      return botRealname;
   }

   public void setBotRealname(String botRealname) {
      this.botRealname = botRealname;
   }

   public void addAfterConnectCommand(IRCCommand cmd)
   {
      this.afterConnectCommands.add(cmd);
   }

   public void addBeforeDisconnectCommand(IRCCommand cmd)
   {
      this.beforeDisconnectCommands.add(cmd);
   }

   public void addJoinedChannel(String channel)
   {
      if(!this.joinedChannels.contains(channel))
      {
         this.joinedChannels.add(channel);
      }
   }

   public void removeJoinedChannel(String channel)
   {
      if(this.joinedChannels.contains(channel))
      {
         this.joinedChannels.remove(channel);
      }
   }

   public String[] getJoinedChannels()
   {
      return this.joinedChannels.toArray(new String[] {  });
   }

   public IRCCommand[] getAfterConnectCommands()
   {
      return this.afterConnectCommands.toArray(new IRCCommand[] {  });
   }

   public String[] getEnabledModules()
   {
      return this.enabledModules.toArray(new String[] {  });
   }

   public boolean isModuleEnabled(String name)
   {
      return this.enabledModules.contains(name);
   }

   public void serialize()
   {
      if(this.networkstore!=null)
      {
         this.networkstore.serialize();
      }
   }


}
