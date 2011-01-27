import java.util.Random;
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

	private int i = 0;

	// The time that we nap at the station.
	public static int NAP_TIME = 100;

	private static Random rand = new Random();

	public static int getNap() {
		return rand.nextInt(1200);
	}

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

	// The previous sensor that the train reacted to
	private Point previousSensor;

	// The simulator controller
	private TSimInterface sim;
	// A field of semaphores
	private Semaphore[] semaphores;

	/** 
	 * Constructs a train
	 * @param id The id of the train
	 * @param speed The speed of the train
	 * @param semaphores A global list of semaphores
	 * @param sim A reference to the simulator interface
	 * */

	public Train(int id, int speed, Semaphore[] semaphores, TSimInterface sim) {
		this.id = id;
		this.speed = speed;
		this.semaphores = semaphores;
		this.sim = sim;
	}
	// Starts the train catches exceptions
	private void start() {
		try {
			this.sim.setSpeed(this.id, this.speed);
		} catch (CommandException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	// Sets the speed of the train
	private void setSpeed(int speed) {
		try {
			this.sim.setSpeed(this.id, speed);
		} catch (CommandException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	// Stops the train
	private void stop() {
		try {
			this.sim.setSpeed(this.id, 0);
		} catch (CommandException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	// Acquires a given semaphore
	private void acquire(int semaphore) {
		try {
			System.err.println("Train #" + this.id + " tries to acquire semaphore: " + semaphore + " which has permits " + this.semaphores[semaphore].availablePermits());
			this.semaphores[semaphore].acquire();
			System.err.println("Train #" + this.id + " acquired " + semaphore);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	// Sets the switch to the given direction
	private void setSwitch(int s, int direction) {
		try {
			this.sim.setSwitch(switches[s].x, switches[s].y, direction);
		} catch (CommandException e) {
			e.printStackTrace();
		}
	}

	// Chooses between two possible directions depending on which is available. Always goes left first. Changes switch accordingly.
	private int chooseBetween(int left, int right, int s) {
		int direction;
		// Determines which direction is available and acquires it
		if (this.semaphores[left].tryAcquire()) {
			direction = left;
			System.err.println("Train #" + this.id + " acquired " + direction);
		} else {
			acquire(right);
			direction = right;
		}
		// Sets the needed switch
		setSwitch(s, direction == left ? TSimInterface.SWITCH_LEFT : TSimInterface.SWITCH_RIGHT);
		return direction;
	}

	// Blocks the current thread and looks for the next sensor. In order to minimize false triggers, discards
	// an event if the origin is the same as the previous event.
	private void getSensor() {
		try {
			System.err.println("Train #" + this.id + " has previous sensor " + previousSensor.x + " " + previousSensor.y);
			event = this.sim.getSensor(this.id);
			System.err.println("Train #" + this.id + " found sensor " + event.getXpos() + " " + event.getYpos());
			if (new Point(event.getXpos(),event.getYpos()).equals(previousSensor)) {
				event = this.sim.getSensor(this.id);
				System.err.println("Train #" + this.id + " found another sensor " + event.getXpos() + " " + event.getYpos());
			}
			this.previousSensor = new Point(event.getXpos(), event.getYpos());
			//       if (event.getStatus() == SensorEvent.INACTIVE) {
			//         event = this.sim.getSensor(this.id);
			//       }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void release(int semaphore) {
		System.err.println("Train #" + this.id + " releases: " + semaphore);
		this.semaphores[semaphore].release();
	}

	private void releaseAndUpdate() {
		System.err.println("Train #" + this.id + " releases: " + state + " assumes state #" + nextState);
		release(this.state);
		System.err.println("Semaphore " + state + "has permits " + this.semaphores[state].availablePermits());
		this.state = this.nextState;     
	}

	private void crossRoads(){
		getSensor();
		stop();
		acquire(8);
		start();
	}

	private void actAndGetSensor() {
		while (true) {
			if (this.goingDown) {
				if (state == nextState) {
					release(8);
					System.err.println("Train #" + this.id + " has state " + this.state + " nextState " + this.nextState);
					this.nextState = 2;
					acquire(this.nextState);
					setSwitch(0, state == 0 ? TSimInterface.SWITCH_RIGHT : TSimInterface.SWITCH_LEFT);
					start();
				}
				else if (this.nextState == 2) {
					releaseAndUpdate();
					nextState = chooseBetween(4,3,1);
					start();
				}
				else if (this.nextState == 3 || this.nextState == 4) {
					releaseAndUpdate();
					nextState = 5;
					start();
					getSensor();
					stop();
					acquire(this.nextState);
					setSwitch(2, state == 3 ? TSimInterface.SWITCH_LEFT : TSimInterface.SWITCH_RIGHT);
					start();
				}
				else if (this.nextState == 5) {
					releaseAndUpdate();
					nextState = chooseBetween(6,7,3);
					start();
				}
				else if (this.nextState == 6 || this.nextState == 7) {
					releaseAndUpdate();
					stationProtocol();
				}
			} // end going down
			else if (!this.goingDown) {
				if (this.state == this.nextState) {
					System.err.println("Train #" + this.id + " has state " + this.state + " nextState " + this.nextState);
					this.nextState = 5;
					acquire(this.nextState);
					setSwitch(3, state == 6 ? TSimInterface.SWITCH_LEFT : TSimInterface.SWITCH_RIGHT);
					start();
				}
				else if (this.nextState == 5) {
					releaseAndUpdate();
					this.nextState = chooseBetween(3,4,2);
					start();
				}
				else if (this.nextState == 3 || this.nextState == 4) {
					releaseAndUpdate();
					this.nextState = 2;
					start();
					getSensor();
					stop();
					acquire(this.nextState);
					setSwitch(1, state == 3 ? TSimInterface.SWITCH_RIGHT : TSimInterface.SWITCH_LEFT);
					start();
				}
				else if (this.nextState == 2) {
					releaseAndUpdate();
					this.nextState = chooseBetween(1,0,0);
					start();
				}
				else if (this.nextState == 0 || this.nextState == 1) {
					releaseAndUpdate();
					stationProtocol();
				}
			} // end not going down

			getSensor();
			stop();
		} // end while
	}

	private void stationProtocol() {
		System.err.println("Station protocol");
		if (this.state == 0 || this.state == 1) {
			acquire(8);
			start();
			getSensor();
			release(8);
		} else {
			start();
		}
		// Slow down in order not to barge into the station
		if (Math.abs(this.speed) > 20) {
			setSpeed(this.speed > 0 ? 20 : -20);
		}
		// Wait for next sensor and stop, trigger on INACTIVE event in order to minimize trouble after turning around
		try {
			SensorEvent event = this.sim.getSensor(this.id);
			if (event.getStatus() == SensorEvent.INACTIVE) {
				event = this.sim.getSensor(this.id);
				if (event.getStatus() == SensorEvent.ACTIVE) {
					event = this.sim.getSensor(this.id);
				}
			}
			this.previousSensor = new Point(event.getXpos(), event.getYpos());
		} catch (Exception e) {
			System.err.println("OH THE HORROR!!!!!");
			e.printStackTrace();
			System.exit(1);
		}
		stop();
		// Nap to compensate time before stop
		nap(this.speed > 20 ? 200 : this.speed * 20);
		// Nap at station
		nap(getNap());
		// Negate train speed
		this.speed = this.speed * (-1);
		// Set going down
		this.goingDown = !this.goingDown;
		start();
		// We want to disregard the first sensor
		getSensor();
		System.err.println("Train #" + this.id + " disregarded a sensor after starting again");
		if (this.state == 0 || this.state == 1)
			crossRoads();
	}

	public void run() {
		SensorEvent e;
		// Set the initial speed
		start();

		// Wait for the sensor and record the initial direction of the train
		try {
			e = sim.getSensor(this.id);
			if (e.getStatus() == SensorEvent.ACTIVE)
				e = sim.getSensor(this.id);
			this.previousSensor = new Point(e.getXpos(), e.getYpos());
			System.err.println("Y-position: " + e.getYpos());
			this.goingDown = e.getYpos() == 3;
			this.state = this.goingDown ? 0 : 6;
			this.nextState = this.state;
			acquire(this.state);
			if (this.state == 0) {
				getSensor();
				stop();
				acquire(8);
				start();
			}
			System.err.println("Nu är jag initierad. Min state är: " + state + " min direction är " + goingDown);
		} catch (Exception exc2) {
			System.err.println(exc2.getMessage());
			System.exit(1);
		}

		getSensor();
		stop();
		actAndGetSensor();

	}

	// Naps a given number of milliseconds. Makes sure nap times are always positive
	private static void nap(int millisecs) {
		try {
			Thread.sleep(Math.abs(millisecs));
		} catch (InterruptedException e) {
			System.err.println(e.getMessage());
		}
	}
}
