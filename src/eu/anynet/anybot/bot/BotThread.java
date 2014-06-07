/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.anynet.anybot.bot;

import eu.anynet.java.util.CommandLineEvent;
import eu.anynet.java.util.CommandLineListener;
import eu.anynet.java.util.CommandLineParser;
import static eu.anynet.java.util.Properties.properties;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jibble.pircbot.IrcException;

/**
 *
 * @author sim
 */
public class BotThread extends Thread
{

   // http://en.wikipedia.org/wiki/List_of_Internet_Relay_Chat_commands

   private Bot bot;
   private final ThreadPipes pipes;
   private final Network network;

   public BotThread(Network network) throws IOException
   {
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
      if(this.network.isDebugChannelSet())
      {
         this.bot.sendMessage(this.network.getDebugChannel(), message);
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
         this.bot = new Bot(this.network);
         this.bot.setDebugChannel(this.network.getDebugChannel());

         // http://www.informatik-forum.at/showthread.php?66277-Java-Plugin-System-mit-jar-Dateien
         ModuleInfo[] mods = ModuleUtils.getModules();

         if(mods!=null && mods.length>0)
         {
            for(ModuleInfo mod : mods)
            {
               try {
                  // Init classloader
                  URLClassLoader loader = URLClassLoader.newInstance(mod.getJarURLs());

                  // Find main class
                  ResourceBundle props = ResourceBundle.getBundle("anybotmodule", Locale.getDefault(), loader);
                  final String isubClassName = props.getString("anbot.module.module");

                  // Create instance and set data
                  Module sub = (Module) loader.loadClass(isubClassName).newInstance();
                  sub.mergeProperties(properties);
                  sub.setModuleinfo(mod);

                  // Add to thread and launch!
                  this.bot.addModule(sub);
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
         this.bot.addModule(new Module()
         {

            @Override
            public void onConnect(ChatEvent ev) {
               me.writePipeLine("Connected!");
               me.bot.changeNick(me.network.getBotNickname());

               // Startup Commands
               if(me.network.getAfterConnectCommands().length>0)
               {
                  for(IRCCommand cmd : me.network.getAfterConnectCommands())
                  {
                     String cmdstr = cmd.buildRawCommand();
                     me.bot.sendRawLine(cmdstr);
                  }
               }

               // Join Debug Channel
               if(me.network.isDebugChannelSet())
               {
                  me.bot.joinChannel(me.network.getDebugChannel());
               }

               // Join stored channels
               String[] joinedchannels = me.network.getJoinedChannels();
               if(joinedchannels!=null && joinedchannels.length>0)
               {
                  for(String channel : joinedchannels)
                  {
                     me.writePipeLine("Join "+channel);
                     if(!Arrays.asList(ev.getBot().getChannels()).contains(channel))
                     {
                        ev.getBot().joinChannel(channel);
                     }
                  }
               }

            }

            @Override
            public void onInvite(ChatMessage msg) {
               if(msg.getBot().getNick().equals(msg.getMessage()))
               {
                  String chan = msg.getChannel();
                  //String source = msg.getNick();

                  if(msg.getHost().equals("sim4000.off.users.iZ-smart.net") || (chan!=null && me.network.getDebugChannel()!=null && chan.equals(me.network.getDebugChannel())))
                  {
                     msg.getBot().joinChannel(chan);
                  }
                  else
                  {
                     msg.respondNotice("Access denied!");
                  }
               }
            }
            @Override
            public void onJoin(ChatMessage msg) {
               if(msg.getNick().equals(msg.getBot().getNick())) {
                  me.network.addJoinedChannel(msg.getChannel());
                  me.network.serialize();
               }
            }
            @Override
            public void onPart(ChatMessage msg) {
               if(msg.getNick().equals(msg.getBot().getNick())) {
                  me.network.removeJoinedChannel(msg.getChannel());
                  me.network.serialize();
               }
            }
            @Override
            public void onKick(ChatMessage msg) {
               if(msg.getRecipient().equals(msg.getBot().getNick()))
               {
                  me.network.removeJoinedChannel(msg.getChannel());
                  me.network.serialize();
               }
            }
         });

         this.bot.addModule(new Module() {
            @Override
            public void onMessage(ChatMessage msg) {
               if(msg.isBotAsked() && msg.count()>1 && msg.get(1).equalsIgnoreCase("version"))
               {
                  msg.respond(properties.get("versionstring"));
               }
            }
         });

         this.bot.enableAutoReconnect();
         this.bot.connect(this.network.getHost());

         CommandLineParser parser = new CommandLineParser();

         parser.addCommandLineListener(new CommandLineListener("^join") {
            @Override
            public void handleCommand(CommandLineEvent e) {
               String chan = e.get(1);
               me.writePipeLine("Join "+chan);
               bot.joinChannel(chan);
            }
         });

         parser.addCommandLineListener(new CommandLineListener("^part") {
            @Override
            public void handleCommand(CommandLineEvent e) {
               String chan = e.get(1);
               bot.partChannel(chan);
            }
         });

         parser.addCommandLineListener(new CommandLineListener("^quit") {
            @Override
            public void handleCommand(CommandLineEvent e) {
               bot.disableAutoReconnect();
               bot.quitServer("The bot is shutting down!");
            }
         });

         parser.addCommandLineListener(new CommandLineListener("^msg") {
            @Override
            public void handleCommand(CommandLineEvent e) {
               bot.sendMessage(e.get(1), e.get(2, -1, " "));
            }
         });

         parser.addCommandLineListener(new CommandLineListener("^raw") {
            @Override
            public void handleCommand(CommandLineEvent e) {
               bot.sendRawLineViaQueue(e.get(1, -1, " "));
            }
         });

         while(true)
         {
            parser.handleCommandLine(this.readPipeLine());
         }

      }
      catch(InterruptedIOException ex)
      {

      }
      catch (IOException | IrcException ex)
      {
         this.writePipeLine("Error: "+ex.getMessage());
      }
   }

   @Override
   public void interrupt()
   {
      if(this.bot.isConnected())
      {
         this.writePipeLine("Thread exited.");
         this.bot.quitServer("Bot is shutting down!");
         this.bot.dispose();
      }
      super.interrupt();
   }


}
