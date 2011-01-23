/**
 * A train thread
 * @author Jesper Josefsson
 * @author Anmar Khazal
 */
 
 public class Train implements Runnable {
   
   // Denotes whether the train in question is traveling down or up the track
   private boolean goingDown;
   
   // The speed with which the train travels
   private int speed;
   
   // The id of the train
   private int id;
   
   public Train(int id, int speed) {
     this.id = id;
     this.speed = speed;
     
   }
   
   public void run() {
     
   }
 }