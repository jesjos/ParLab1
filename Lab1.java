import TSim.*;
import java.util.concurrent.Semaphore;

/**
 * Concurrent programming - Labb 1
 * Reads from input, initializes Trains, simulator and semaphores
 * @author Jesper Josefsson
 * @author Anmar Khazal
 */
 
public class Lab1 {
  private TSimInterface sim;
  
  private Thread one;
  private Thread two;
  
  Semaphore[] semaphores;
  
  public Lab1(int speed1, int speed2) {
    sim = TSimInterface.getInstance();
    sim.setDebug(false);
    semaphores = new Semaphore[9];
    for (int i = 0; i < 9; i++) {
     semaphores[i] = new Semaphore(1, true);
    }
    System.err.println("Semaphores number: " + semaphores.length);
    one = new Thread (new Train(1,speed1,this.semaphores,this.sim));
    two = new Thread (new Train(2,speed2,this.semaphores,this.sim));
  }
  
  public void start() {
    one.start();
    two.start();
  }
  
  public static void main(String[] args) {
    Lab1 lab = new Lab1(Integer.valueOf(args[0]), Integer.valueOf(args[1]));
    lab.start();
  }
}
