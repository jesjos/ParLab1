import java.util.concurrent.Semaphore;
import java.awt.Point;
import TSim.*;

/**
 * A train thread.
 * Assumptions:
 * A train either starts at the top-most track or at the second-to-last track, i.e. state 0 or 7
 * @author Jesper Josefsson
 * @author Anmar Khazal
 */
 
 public class Train implements Runnable {
   public static Point[] switches = {new Point(17,7), new Point(15,9), new Point(4,9), new Point(3,11)};
   
   public Semaphore previous;
   
   // Denotes whether the train in question is travelling down or up the track
   private boolean goingDown;
   
   // The speed with which the train travels
   private int speed;
   
   // The id of the train
   private int id;
   
   // The current state which the train is in, i.e. where it is located in the sequence of tracks and stops
   private int state;
   
   // The simulator controller
   private TSimInterface sim;
   
   private Semaphore[] semaphores;
   
   public Train(int id, int speed, Semaphore[] semaphores, TSimInterface sim) {
     this.id = id;
     this.speed = speed;
     this.semaphores = semaphores;
     this.sim = sim;
   }
   
   private void start() {
     try {
       this.sim.setSpeed(this.id, this.speed);
     } catch (CommandException e) {
       e.printStackTrace();
     }
   }
   
   private void stop() {
     try {
       this.sim.setSpeed(this.id, 0);
     } catch (CommandException e) {
       e.printStackTrace();
     }
   }
   
   private void acquire(int semaphore) {
     try {
       this.semaphores[semaphore].acquire();
     } catch (InterruptedException e) {
       e.printStackTrace();
     }
   }
   
   private void setSwitch(int s, int direction) {
     try {
       this.sim.setSwitch(switches[s].x, switches[s].y, direction);
     } catch (CommandException e) {
       e.printStackTrace();
     }
   }
   
   private void getSensorAndReact(){
     SensorEvent event;
     try {
       event = this.sim.getSensor(this.id);
     } catch (Exception exc) {
       System.err.println(exc.getMessage());
     }
     this.previous.release();
     
     // Set speed to zero in order to stop if the semaphore isn't acquired
     stop();
     
     if (this.goingDown) {
       if (state == 2) {
         if (this.semaphores[3].tryAcquire()) {
           acquire(3);
           setSwitch(1, TSimInterface.SWITCH_RIGHT);
           start();
           this.state = 3;
         } else {
           acquire(4);
           setSwitch(1, TSimInterface.SWITCH_LEFT);
           start();
           this.state = 4;
         }
       }
       
     }
   }
   
   public void run() {
     SensorEvent e;
     // Set the initial speed
     try {
       sim.setSpeed(this.id, this.speed);
     } catch (CommandException exc1) {
       System.err.println(exc1.getMessage());
     }
     
     // Wait for the sensor and record the direction of the train
     try {
       e = sim.getSensor(this.id);
       this.goingDown = e.getYpos() == 3;
       this.state = this.goingDown ? 0 : 7;
       this.previous = this.semaphores[state];
     } catch (Exception exc2) {
       System.err.println(exc2.getMessage());
     }
     
     
   }
 }