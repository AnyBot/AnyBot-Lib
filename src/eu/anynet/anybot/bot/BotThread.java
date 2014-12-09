/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.anynet.anybot.bot;

import eu.anynet.anybot.pircbotxextensions.MessageEventEx;
import eu.anynet.java.uax.UaxApi;
import eu.anynet.java.util.CommandLineEvent;
import eu.anynet.java.util.CommandLineListener;
import eu.anynet.java.util.CommandLineParser;
import static eu.anynet.java.util.Properties.properties;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.pircbotx.Configuration;
import org.pircbotx.User;
import org.pircbotx.exception.IrcException;
import org.pircbotx.hooks.events.ConnectEvent;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.KickEvent;
import org.pircbotx.hooks.events.PartEvent;
import org.pircbotx.hooks.events.UserListEvent;

/**
 *
 * @author sim
 */
public class BotThread extends Thread
{

   // http://en.wikipedia.org/wiki/List_of_Internet_Relay_Chat_commands

   private Thread botthread;
   private Bot bot;
   private final ThreadPipes pipes;
   private final Network network;

   public BotThread(Network network) throws IOException
   {
      this.setName("network-worker-"+network.getKey());
      this.pipes = new ThreadPipes();
      this.network = network;
   }

   public ThreadPipeEndpoint getPipeEndpoint()
   {
      return this.pipes.getOutsideEndpoint();
   }

   private String readPipeLine() throws IOException, InterruptedIOException
   {
      return this.pipes.getInsideEndpoint().receive();
   }

   private void writePipeLine(String message)
   {
      if(this.bot!=null && this.bot.isConnected() && this.network.isDebugChannelSet())
      {
         this.bot.sendIRC().message(this.network.getDebugChannel(), message);
      }

      try {
         this.pipes.getInsideEndpoint().send(message+"\n");
      } catch (IOException ex) {
         Logger.getLogger(BotThread.class.getName()).log(Level.SEVERE, null, ex);
      }
   }

   @Override
   public void run()
   {
      try
      {
         final BotThread me = this;
         Configuration config = Bot.createConfigFromNetworkSettings(this.network);

         this.bot = new Bot(config, me.network);

         // http://www.informatik-forum.at/showthread.php?66277-Java-Plugin-System-mit-jar-Dateien
         ModuleInfo[] mods = ModuleUtils.getModules();

         if(mods!=null && mods.length>0)
         {
            for(ModuleInfo mod : mods)
            {
               try {
                  // Init classloader
                  URL[] jars = mod.getJarURLs();
                  URLClassLoader loader = URLClassLoader.newInstance(jars);

                  // Find main class
                  ResourceBundle props = ResourceBundle.getBundle("anybotmodule", Locale.getDefault(), loader);
                  final String isubClassName = props.getString("anybot.module.module");

                  // Create instance and set data
                  Module sub = (Module) loader.loadClass(isubClassName).newInstance();
                  sub.mergeProperties(properties);
                  sub.setModuleinfo(mod);
                  sub.setNetworksettings(me.network);

                  // Add to thread and launch!
                  this.bot.addListener(sub);
                  //config.getListenerManager().addListener(sub);
                  sub.launch();

                  me.writePipeLine("Load module successfull: "+mod.getName());
               }
               catch(ClassNotFoundException | InstantiationException | IllegalAccessException ex)
               {
                  me.writePipeLine("Load of module "+mod.getName()+" failed: "+ex.getMessage());
               }
            }
         }
         else
         {
            me.writePipeLine("No modules found.");
         }


         // TODO: Put this in a module class
         this.bot.addListener(new Module()
         {

            @Override
            public void onConnect(ConnectEvent<Bot> event) throws Exception
            {
               me.writePipeLine("Connected");

               // Startup Commands
               if(me.network.getAfterConnectCommands().length>0)
               {
                  for(IRCCommand cmd : me.network.getAfterConnectCommands())
                  {
                     String cmdstr = cmd.buildRawCommand();
                     event.getBot().sendRaw().rawLineNow(cmdstr);
                  }
               }

               me.writePipeLine("Wait...");
               Thread.sleep(3000);
               me.writePipeLine("Wait done.");

               // Join Debug Channel
               if(me.network.isDebugChannelSet())
               {
                  event.getBot().sendIRC().joinChannel(me.network.getDebugChannel());
               }

               // Join stored channels
               String[] joinedchannels = me.network.getJoinedChannels();
               if(joinedchannels!=null && joinedchannels.length>0)
               {
                  for(String channel : joinedchannels)
                  {
                     me.writePipeLine("Startup-Join "+channel);
                     if(!event.getBot().isBotInChannel(channel))
                     {
                        event.getBot().sendIRC().joinChannel(channel);
                     }
                  }
               }
            }

            @Override
            public void onUserList(UserListEvent<Bot> event) throws Exception
            {
               for(User user : event.getUsers().asList())
               {
                  String name = user.getNick();
               }
            }



            /*
            @Override
            public void onInvite(InviteEvent<Bot> event) throws Exception
            {
            if(event.getBot().getNick().equals(event.getUser()))
            {
            String chan = event.getChannel();
            if(msg.getHost().equals("sim4000.off.users.iZ-smart.net") || (chan!=null && me.network.getDebugChannel()!=null && chan.equals(me.network.getDebugChannel())))
            {
            msg.getBot().sendIRC().joinChannel(chan);
            }
            else
            {
            msg.respondNotice("Access denied!");
            }
            }
            }
             */

            @Override
            public void onJoin(JoinEvent<Bot> event) throws Exception
            {
               if(event.getUser().getNick().equals(event.getBot().getNick())) {
                  me.network.addJoinedChannel(event.getChannel().getName());
                  me.network.serialize();
               }
            }

            @Override
            public void onPart(PartEvent<Bot> event) throws Exception
            {
               if(event.getUser().getNick().equals(event.getBot().getNick())) {
                  me.network.removeJoinedChannel(event.getChannel().getName());
                  me.network.serialize();
               }
            }

            @Override
            public void onKick(KickEvent<Bot> event) throws Exception
            {
               if(event.getRecipient().getNick().equals(event.getBot().getNick()))
               {
                  me.network.removeJoinedChannel(event.getChannel().getName());
                  me.network.serialize();
               }
            }

         });

         this.bot.addListener(new Module()
         {

            @Override
            public void onMessage(MessageEventEx event) throws Exception
            {
               if(event.args().isBotAsked() && event.args().count()>0 && event.args().get(0).equalsIgnoreCase("version"))
               {
                  event.respond(properties.get("versionstring"));
               }
               else if(event.args().isBotAsked() && event.args().count()>1 && event.args().get(0).equalsIgnoreCase("short"))
               {
                  UaxApi uax = UaxApi.initialize();
                  if(uax==null)
                  {
                     event.respond("No api key defined");
                  }
                  else
                  {
                     String shortlink = uax.shortUrl(event.args().get(1));
                     if(shortlink!=null)
                     {
                        event.respond(shortlink);
                     }
                     else
                     {
                        event.respond("Could not short link.");
                     }
                  }

               }
            }

         });

         this.startPircBotX();

         CommandLineParser parser = new CommandLineParser();

         parser.addCommandLineListener(new CommandLineListener("^join") {
            @Override
            public void handleCommand(CommandLineEvent e) {
               String chan = e.get(1);
               me.writePipeLine("Trying to join "+chan);
               bot.sendIRC().joinChannel(chan);
            }
         });

         parser.addCommandLineListener(new CommandLineListener("^part") {
            @Override
            public void handleCommand(CommandLineEvent e) {
               String chan = e.get(1);
               //bot.partChannel(chan);
               me.writePipeLine("Trying to part "+chan);
               bot.sendRaw().rawLineNow("PART "+chan);
            }
         });

         parser.addCommandLineListener(new CommandLineListener("^quit") {
            @Override
            public void handleCommand(CommandLineEvent e) {
               bot.stopBotReconnect();
               bot.sendIRC().quitServer("The bot is shutting down!");
            }
         });

         parser.addCommandLineListener(new CommandLineListener("^msg") {
            @Override
            public void handleCommand(CommandLineEvent e) {
               bot.sendIRC().message(e.get(1), e.get(2, -1, " "));
            }
         });

         parser.addCommandLineListener(new CommandLineListener("^raw") {
            @Override
            public void handleCommand(CommandLineEvent e) {
               bot.sendRaw().rawLineNow(e.get(1, -1, " "));
            }
         });

         while(true)
         {
            String l = this.readPipeLine();
            bot.sendDebug("[CONSOLE] "+l);
            parser.handleCommandLine(l);
         }

      }
      catch(InterruptedIOException ex)
      {
         Logger.getLogger(BotThread.class.getName()).log(Level.SEVERE, null, ex);
      }
      catch (IOException ex)
      {
         this.writePipeLine("Error: "+ex.getMessage());
      }
      catch (IrcException ex)
      {
         Logger.getLogger(BotThread.class.getName()).log(Level.SEVERE, null, ex);
      }
   }

   @Override
   public void interrupt()
   {
      this.stopPircBotX();
      super.interrupt();
   }


   private void startPircBotX()
   {
      final Bot b = this.bot;
      this.botthread = new Thread("network-pircbotx-"+this.network.getKey()) {
         @Override
         public void run()
         {
            try {
               b.startBot();
            } catch (IOException | IrcException ex) {
               Logger.getLogger(BotThread.class.getName()).log(Level.SEVERE, null, ex);
            }
         }
      };

      this.botthread.start();
   }


   private void stopPircBotX()
   {
      if(this.bot.isConnected())
      {
         this.writePipeLine("Thread exited.");
         this.bot.stopBotReconnect();
         this.bot.sendIRC().quitServer("Bot is shutting down!");
      }

      if(this.botthread!=null && (this.botthread.isAlive() || !this.botthread.isInterrupted()))
      {
         this.botthread.interrupt();
         this.botthread=null;
      }

   }


}
