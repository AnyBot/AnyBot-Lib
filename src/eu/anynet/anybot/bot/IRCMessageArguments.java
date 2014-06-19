/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.anynet.anybot.bot;

import eu.anynet.java.util.Arguments;
import eu.anynet.java.util.Regex;
import java.util.ArrayList;

/**
 *
 * @author sim
 */
public class IRCMessageArguments extends Arguments
{

   private String botnickname;

   public IRCMessageArguments(String argstring, String botnick)
   {
      super(argstring);
      this.botnickname = null;

      try {
         ArrayList<String> groups = Regex.findAllGroupsByRegex("^("+Regex.quote(botnick)+")[,:] (.*)$", argstring);

         if(groups.size()>1)
         {
            this.botnickname = groups.get(0);
            String msg = groups.get(1);
            this.setArgString(msg);
         }
         else
         {
            this.setArgString(argstring);
         }
      }
      catch(java.lang.IllegalStateException ex)
      {
         this.setArgString(argstring);
      }
   }

   public boolean isBotAsked()
   {
      return (this.botnickname!=null && this.botnickname.length()>0);
   }

}
