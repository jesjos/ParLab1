import java.util.concurrent.Semaphore;
import java.awt.Point;
import TSim.*;

/**
 * A train thread
 * @author Jesper Josefsson
 * @author Anmar Khazal
 */
 
 public class Train implements Runnable {
   public static Point[] switches = {new Point(17,7), new Point(15,9), new Point(4,9), new Point(3,11)};
   
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
   
   public void run() {
     sim.setSpeed(this.id, this.speed);
     SensorEvent e = sim.getSensor(this.id);
     this.goingDown = e.getYpos() == 3;
     
   }
 }