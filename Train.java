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
     
     System.err.println("Nu går vi in i setSwitch");
     try {
       this.sim.setSwitch(switches[s].x, switches[s].y, direction);
     } catch (CommandException e) {
       e.printStackTrace();
     }
   }
   
   // Chooses between two possible directions depending on which is available. Always goes left first. Changes switch accordingly.
   private void chooseBetween(int left, int right, int s) {
     int direction = this.semaphores[left].tryAcquire() ? left : right;
     acquire(direction);
     this.previous = semaphores[direction];
     setSwitch(s, direction == left ? TSimInterface.SWITCH_LEFT : TSimInterface.SWITCH_RIGHT);
     start();
     this.previous.release();
     this.state = direction;
   }
   
   private void getSensorAndReact(){
     System.err.println("Nu går vi in i getSensorAndReact");
     SensorEvent event;
     try {
       event = this.sim.getSensor(this.id);
     } catch (Exception exc) {
       System.err.println(exc.getMessage());
     }
     
     // Start the main loop
     while (true) {
       // Set speed to zero in order to stop if the semaphore isn't acquired
       stop();
     
       // Logic for trains travelling downwards
       if (this.goingDown) {
         if (state == 0) {
           acquire(2);
           setSwitch(0, TSimInterface.SWITCH_RIGHT);
           start();
           // Release the preceding track
           this.previous.release();
           this.state = 2;
         }
         if (state == 2) {
           chooseBetween(3,4,1);
         }
         if (state == 6) {
           chooseBetween(6,7,3);
         }
       } // end if going down
      
       if (!this.goingDown) {
         if (state == 6) {
           acquire(5);
           setSwitch(3, TSimInterface.SWITCH_LEFT);
           start();
           this.previous.release();
           this.state = 5;
         }
       } // end if not going down
       
       // Start looking for new signals
       try {
         this.sim.getSensor(this.id);
       } catch (Exception e) {
         e.printStackTrace();
       }
     } // end while
   }
   
   public void run() {
     SensorEvent e;
     // Set the initial speed
     try {
       sim.setSpeed(this.id, this.speed);
     } catch (CommandException exc1) {
       System.err.println(exc1.getMessage());
     }
     
     // Wait for the sensor and record the initial direction of the train
     try {
       e = sim.getSensor(this.id);
       System.err.println("Y-position: " + e.getYpos());
       this.goingDown = e.getYpos() == 7;
       this.state = this.goingDown ? 0 : 6;
       acquire(state);
       this.previous = this.semaphores[state];
       setSwitch(goingDown ? 0 : 3, goingDown ? TSimInterface.SWITCH_RIGHT : TSimInterface.SWITCH_LEFT);
       System.err.println("Nu är jag initierad. Min state är: " + state + " min direction är " + goingDown);
     } catch (Exception exc2) {
       System.err.println(exc2.getMessage());
       System.exit(1);
     }
     
     getSensorAndReact();
     
   }
 }
