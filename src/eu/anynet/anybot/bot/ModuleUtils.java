/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.anynet.anybot.bot;

import java.io.File;
import java.io.FileFilter;

/**
 *
 * @author sim
 */
public class ModuleUtils
{

   private static String modulefolder;
   private static String settingsfolder;

   public static void setModuleFolder(String modfolder)
   {
      modulefolder = modfolder;
   }

   public static void setSettingsFolder(String settfolder)
   {
      settingsfolder = settfolder;
   }

   public static File[] getModuleFiles()
   {
      File[] jars = new File(modulefolder).listFiles(new FileFilter()
      {
         @Override
         public boolean accept(File pathname) {
            return pathname.getName().endsWith(".jar") && pathname.getName().startsWith("AnyBot-Module-");
         }
      });
      return jars==null ? new File[] {} : jars;
   }

   public static int getModuleCount()
   {
      return getModuleFiles().length;
   }

   public static ModuleInfo[] getModules()
   {
      File[] files = getModuleFiles();
      ModuleInfo[] mods = new ModuleInfo[files.length];

      for(int i=0; i<files.length; i++)
      {
         mods[i] = new ModuleInfo(files[i], new File(settingsfolder));
      }

      return mods;
   }

   public static String[] getModuleNames()
   {
      ModuleInfo[] infos = getModules();
      String[] names = new String[infos.length];

      for(int i=0; i<infos.length; i++)
      {
         names[i] = infos[i].getName();
      }
      return names;
   }


}
