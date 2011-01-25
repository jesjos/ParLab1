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
    sim.setDebug(true);
    semaphores = new Semaphore[8];
    for (int i = 0; i < 7; i++) {
     semaphores[i] = new Semaphore(1, true);
    }
    one = new Thread (new Train(1,20,this.semaphores,this.sim));
    //two = new Thread (new Train(1,20,this.semaphores,this.sim));
  }
  
  public void start() {
    one.start();
    //two.start();
  }
  
  public static void main(String[] args) {
    Lab1 lab = new Lab1();
    lab.start();
  }
}
