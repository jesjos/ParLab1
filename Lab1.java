import TSim.*;

/**
 * Concurrent programming - Labb 1
 * @author Jesper Josefsson
 * @author Anmar Khazal
 */
 
public class Lab1 {
  private static TSimInterface sim = TSimInterface.getInstance();
  
  private Thread one = new Thread (new Train());
  
  public static void main(String[] args) {
    try {
      sim.setSpeed(1,100);
      SensorEvent e = sim.getSensor(1);
      sim.setSwitch(17,7, TSim.TSimInterface.SWITCH_RIGHT);
      sim.setSwitch(4,9,TSim.TSimInterface.SWITCH_RIGHT);
      sim.setSwitch(3,11,TSim.TSimInterface.SWITCH_RIGHT);
    } catch (Exception e) {
      System.err.println("Oh fuckness!");
    }
  }
}