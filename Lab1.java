
import TSim.CommandException;
import TSim.SensorEvent;
import TSim.TSimInterface;

public class Lab1 {
	private static String [] tspeeds;
	Thread t1, t2;

	public static void main(String[] a) {
		tspeeds = a;
		new Lab1();
	}

	public Lab1() {
		//t1.start();
		//t2.start();
		
		TSimInterface inter = TSimInterface.getInstance();
		
		try {
			inter.setSpeed(1,Integer.valueOf(tspeeds[0]));
			inter.setSpeed(2,Integer.valueOf(tspeeds[1]));
		}
		catch (CommandException e) {
			e.printStackTrace();    // or only e.getMessage() for the error
			System.exit(1);
		}
		while(true){
			SensorEvent t1 = null;
			try {
				t1 = inter.getSensor(1);
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
	
			while (true) {
				try {
					t1 = inter.getSensor(1);
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
				if( t1.getXpos() == 13 && t1.getYpos() == 7){
					try {
						inter.setSwitch(17,7, TSimInterface.SWITCH_RIGHT);
						break;
					} 
					catch (Exception e1) {
						e1.printStackTrace();
					}

				}//if
				
			}//while sensor 3 from above
			
			while (true) {
				try {
					t1 = inter.getSensor(1);
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
				if( t1.getXpos() == 19 && t1.getYpos() == 9){
					try {
						inter.setSwitch(15,9, TSimInterface.SWITCH_RIGHT);
						break;
					} 
					catch (Exception e1) {
						e1.printStackTrace();
					}

				}//if
				
			}//while sensor 4 from above
			
			while (true) {
				try {
					t1 = inter.getSensor(1);
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
				if( t1.getXpos() == 10 && t1.getYpos() == 9){
					try {
						inter.setSwitch(4,9, TSimInterface.SWITCH_LEFT);
						break;
					} 
					catch (Exception e1) {
						e1.printStackTrace();
					}

				}//if
				
			}//while sensor 4

			while (true) {

				try {
					t1 = inter.getSensor(1);
				} 
				catch (Exception e) {
					e.printStackTrace();
				}

				if( t1.getXpos() == 1 && t1.getYpos() == 10){
					try {
						inter.setSwitch(3,11, TSimInterface.SWITCH_RIGHT);
					} 
					catch (Exception e1) {
						e1.printStackTrace();
					}
					try {
						inter.setSpeed(1,0);
						Thread.sleep(5000);
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					try {
						inter.setSpeed(1,-(Integer.valueOf(tspeeds[0])));
					} catch (Exception e) {
						e.printStackTrace();
					} 
					break;
				}//if
				
			}//while switch 5	

		}//train while


	}//lab1
}

