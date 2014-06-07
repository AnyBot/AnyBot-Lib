/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.anynet.anybot.bot;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sim
 */
public class ModuleInfo
{

   private final File modulefile;
   private final File settingsfolder;

   public ModuleInfo(File modulefile, File settingsfolder)
   {
      this.modulefile = modulefile;
      this.settingsfolder = settingsfolder;
   }

   public String getAbsoluteFilename()
   {
      return this.modulefile.getAbsolutePath();
   }

   public String getFilename()
   {
      return this.modulefile.getName();
   }

   public String getName()
   {
      return this.modulefile.getName().substring(0, this.modulefile.getName().lastIndexOf("."));
   }

   public File getSettingsFolder()
   {
      String modulename = this.getName();
      File modsettingsfolder = new File(this.settingsfolder.getAbsolutePath()+File.separator+modulename+File.separator);
      modsettingsfolder.mkdirs();
      return modsettingsfolder;
   }

   /*
   public boolean createSettingsFolder()
   {
      File folder = this.getSettingsFolder();
      if(folder.exists())
      {
         return true;
      }
      else
      {
         return folder.mkdirs();
      }
   }

   public boolean isSettingsFolderExist()
   {
      return this.getSettingsFolder().exists() && this.getSettingsFolder().isDirectory();
   }
   */

   public String getResourceProperty(String key)
   {
      URLClassLoader loader = URLClassLoader.newInstance(new URL[] { this.toURL() });
      ResourceBundle props = ResourceBundle.getBundle("anybotmodule", Locale.getDefault(), loader);
      if(props.containsKey(key))
      {
         return props.getString(key);
      }
      else
      {
         return null;
      }
   }

   public String[] getResourcePropertyArray(String key)
   {
      String prop = this.getResourceProperty(key);
      if(prop!=null)
      {
         String[] props = prop.split(File.pathSeparator);
         return props;
      }
      else
      {
         return null;
      }
   }

   public URL toURL()
   {
      try {
         return this.modulefile.toURI().toURL();
      } catch (MalformedURLException ex) {
         return null;
      }
   }

   public URL[] getJarURLs()
   {
      String[] dependencies = this.getResourcePropertyArray("anbot.module.dependencies");
      int depcount=0;
      if(dependencies!=null)
      {
         depcount=dependencies.length;
      }

      URL[] urls = new URL[depcount+1];
      urls[0] = this.toURL();

      if(depcount>0)
      {
         int i=1;
         for(String dependency : dependencies)
         {
            try {
               urls[i] = (new File(dependency)).toURI().toURL();
            } catch (MalformedURLException ex) {
               urls[i] = null;
            }
            i++;
         }
      }

      return urls;
   }

}
