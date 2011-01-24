import TSim.*;
import java.util.concurrent.Semaphore;

/**
 * Concurrent programming - Labb 1
 * @author Jesper Josefsson
 * @author Anmar Khazal
 */
 
public class Lab1 {
  private TSimInterface sim;
  
  private Thread one;
  private Thread two;
  
  Semaphore[] semaphores;
  
  public Lab1() {
    sim = TSimInterface.getInstance();
    one = new Thread (new Train(1,20,this.sim));
    two = new Thread (new Train(1,20,this.sim));
    
    for (int i = 0; i < 5; i++) {
     semaphores[i] = new Semaphore(1, true);
    }
  }
  
  public void start() {
    one.start();
    two.start();
  }
  
  public static void main(String[] args) {
    Lab1 lab = new Lab1();
    lab.start();
  }
}
