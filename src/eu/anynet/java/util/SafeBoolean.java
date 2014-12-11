/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.anynet.java.util;

/**
 *
 * @author sim
 */
public class SafeBoolean extends Safe<Boolean>
{

   public SafeBoolean(boolean b)
   {
      this.set(b);
   }

   public void setTrue()
   {
      this.set(true);
   }

   public boolean isTrue()
   {
      return (this.get()==true);
   }

   public void setFalse()
   {
      this.set(false);
   }

   public boolean isFalse()
   {
      return (this.get()==false);
   }

}
