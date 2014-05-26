/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.anynet.java.util;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.reflections.Reflections;

/**
 *
 * @author perry
 */
public abstract class CommandLineModuleBase implements CommandLineModule
{

   private CommandLineParser parser;
   private ArrayList<CommandLineListener> loadedCommands;
   private Object[] additionalobjects;
   protected final SaveBoolean isEnabled;

   public CommandLineModuleBase(CommandLineParser parser, Object[] additionalobjects)
   {
      this.parser = parser;
      this.additionalobjects = additionalobjects;
      this.loadedCommands = new ArrayList<>();
      this.isEnabled = new SaveBoolean(false);
   }

   public Object getObjectAt(int i)
   {
      if(this.additionalobjects.length>i)
      {
         return this.additionalobjects[i];
      }
      else
      {
         return null;
      }
   }

   public static void loadAll(CommandLineParser parser, Object[] additionalobjects)
   {
      Reflections reflections = new Reflections("eu.anynet.anybot.commands");
      Set<Class<? extends CommandLineModuleBase>> modules = reflections.getSubTypesOf(CommandLineModuleBase.class);

      for (Class<? extends CommandLineModuleBase> item : modules)
      {
         try {
            CommandLineModuleBase module = item.getConstructor(CommandLineParser.class, Object[].class).newInstance(parser, additionalobjects);
            if(module.isEnabled.isTrue())
            {
               module.load();
            }
         }
         catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(CommandLineModuleBase.class.getName()).log(Level.SEVERE, null, ex);
         }
      }
   }

   abstract public ArrayList<CommandLineListener> getCommands();

   @Override
   public void load()
   {
      for(CommandLineListener listener : this.getCommands())
      {
         this.parser.addCommandLineListener(listener);
         this.loadedCommands.add(listener);
      }
   }

   @Override
   public void unload()
   {
      for(CommandLineListener listener : this.getCommands())
      {
         this.parser.removeCommandLineListener(listener);
         this.loadedCommands.remove(listener);
      }
   }

}
