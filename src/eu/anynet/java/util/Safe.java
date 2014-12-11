/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.anynet.java.util;

/**
 *
 * @author sim
 */
public class Safe<T>
{

   private T value;

   public T get()
   {
      return this.value;
   }

   public void set(T val)
   {
      this.value = val;
   }

}
