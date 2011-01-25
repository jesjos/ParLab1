import java.util.concurrent.Semaphore;
import java.awt.Point;
import TSim.*;

/**
 * A train thread.
 * Assumptions:
 * A train either starts at the top-most track or at the second-to-last track, i.e. state 0 or 7
 * We use this information to intitialize the logic.
 * @author Jesper Josefsson
 * @author Anmar Khazal
 */
 
 public class Train implements Runnable {
   public static Point[] switches = {new Point(17,7), new Point(15,9), new Point(4,9), new Point(3,11)};
   
   private Semaphore previous;
   
   // Denotes whether the train in question is travelling down or up the track
   private boolean goingDown;
   
   private int nextState;
   
   // The speed with which the train travels
   private int speed;
   
   // The id of the train
   private int id;
   
   // The current state which the train is in, i.e. where it is located in the sequence of tracks and stops
   private int state;
   
   // The current SensorEvent being handled
   private SensorEvent event;
   
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
       System.err.println("Train #" + this.id + " acquired " + semaphore);
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
   
   // Chooses between two possible directions depending on which is available. Always goes left first. Changes switch accordingly.
   private void chooseBetween(int left, int right, int s) {
     System.err.println("Choose between: left " + left + " right " + right);
     // Determines which direction is available and acquires it
     int direction;
     if (this.semaphores[left].tryAcquire()) {
       direction = left;
       System.err.println("Train #" + this.id + " acquired " + direction);
     } else {
       acquire(right);
       direction = right;
     }
     // Sets the desired switch
     setSwitch(s, direction == left ? TSimInterface.SWITCH_LEFT : TSimInterface.SWITCH_RIGHT);
     start();
     this.nextState = direction;
   }
   
   private void getSensor() {
     try {
       event = this.sim.getSensor(this.id);
       if (event.getStatus() == SensorEvent.INACTIVE) {
         event = this.sim.getSensor(this.id);
       }
       this.semaphores[state].release();
       System.err.println("Train # " + this.id + " has released " + state);
       this.state = nextState;
       System.err.println("Train # " + this.id + " has next state " + nextState);
     } catch (Exception e) {
       e.printStackTrace();
     }
   }
   
   private void getSensorAndReact(){
     SensorEvent event;
     
     // Start the main loop
     while (true) {
       // Set speed to zero in order to stop if the semaphore isn't acquired
       stop();
     
       // Logic for trains travelling downwards
       if (this.goingDown) {
         if (state == 0 || state == 1) {
           acquire(2);
           setSwitch(0, TSimInterface.SWITCH_RIGHT);
           start();
           this.nextState = 2;
         }
         else if (state == 2) {
           chooseBetween(4,3,1);
         }
         else if (state == 5) {
           chooseBetween(6,7,3);
         }
         else if (state == 3 || state == 4) {
           acquire(5);
           setSwitch(2, state == 3 ? TSimInterface.SWITCH_LEFT : TSimInterface.SWITCH_RIGHT);
           start();
           this.nextState = 5;
         }
         else if (state == 6 || state == 7) {
           try {
             this.sim.setSpeed(this.id, this.speed/2);
           } catch (CommandException exc) {
             exc.printStackTrace();
           }
           nap(Math.abs(this.speed *100));
           stop();
           nap(2000);
           this.speed = this.speed*(-1);
           this.goingDown = false;
           this.nextState = 5;
           start();
           getSensor();
         }
       } // end if going down
      
       else if (!this.goingDown) {
         if (state == 6 || state == 7) {
           acquire(5);
           setSwitch(3, state == 6 ? TSimInterface.SWITCH_LEFT : TSimInterface.SWITCH_RIGHT);
           start();
           this.nextState = 5;
         }
         else if (state == 5) {
           chooseBetween(3,4,3);
         }
         else if (state == 3 || state == 4) {
           acquire(2);
           setSwitch(1, state == 3 ? TSimInterface.SWITCH_RIGHT : TSimInterface.SWITCH_LEFT);
           start();
           this.nextState = 2;
         }
         else if (state == 2) {
           chooseBetween(1,0,0);
         }
         else if (state == 0 || state == 1) {
           try {
             this.sim.setSpeed(this.id, this.speed/2);
           } catch (CommandException exc) {
             exc.printStackTrace();
           }
           nap(Math.abs(this.speed *100));
           stop();
           nap(2000);
           this.speed = this.speed*(-1);
           this.goingDown = true;
           this.nextState = 2;
           start();
           getSensor();
         }
       } // end if not going down
       
       getSensor();
       
     } // end while
   }
   
   public void run() {
     SensorEvent e;
     // Set the initial speed
     start();
     
     // Wait for the sensor and record the initial direction of the train
     try {
       e = sim.getSensor(this.id);
       if (e.getStatus() == SensorEvent.INACTIVE)
         e = sim.getSensor(this.id);
       System.err.println(e);
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
   private static void nap(int millisecs) {
     try {
       Thread.sleep(millisecs);
     } catch (InterruptedException e) {
       System.err.println(e.getMessage());
     }
   }
 }
