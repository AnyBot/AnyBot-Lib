/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.anynet.java.util;

/**
 *
 * @author sim
 */
abstract public class TimerTask {

   private boolean isstarted;
   private boolean skipfirst;
   private boolean single;
   private Thread worker;
   private final long millis;

   public TimerTask(final long millis)
   {
      this(millis, false, false);
   }

   public TimerTask(final long millis, boolean skipfirst)
   {
      this(millis, false, false);
   }

   public TimerTask(final long millis, boolean skipfirst, boolean single)
   {
      this.millis = millis;
      this.skipfirst = skipfirst;
      this.single = single;
      this.isstarted = false;
      this.worker = null;
   }

   public boolean isRunning()
   {
      return this.worker!=null && this.worker.isAlive() && !this.worker.isInterrupted();
   }

   public void start()
   {
      if(this.isstarted || this.worker!=null)
      {
         throw new IllegalStateException("Thread was already started");
      }

      final TimerTask me = this;
      this.worker = new Thread()
      {
         @Override
         public void run()
         {
            try
            {
               while(true)
               {
                  if(me.skipfirst==false)
                  {
                     Thread.sleep(millis);
                  }
                  else
                  {
                     me.skipfirst = false;
                  }

                  me.doWork();

                  if(me.single)
                  {
                     break;
                  }
               }
            }
            catch(InterruptedException ex)
            {
               // Work done!
            }
         }
      };

      this.isstarted = true;
      this.worker.start();
   }

   public void stop()
   {
      if(this.isstarted==true && this.worker!=null &&
         this.worker.isAlive() && !this.worker.isInterrupted())
      {
         this.worker.interrupt();
         this.worker=null;
         this.isstarted=false;
      }
   }

   public void doWork()
   {

   }

}
