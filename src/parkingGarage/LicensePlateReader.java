package parkingGarage;

import java.util.Random;
import java.util.concurrent.BlockingQueue;

public class LicensePlateReader implements Runnable {
	//
	private static int count = 0;
	private int id;
	private Random random = new Random();
	private boolean running = true;
	private Location LPRLocation;
	private int garageID;

	private Gate gate;
	private BlockingQueue<String> sharedQueue;

	public LicensePlateReader() {
		this.id = count++;
		this.LPRLocation = Location.Exit;
		this.gate = new Gate();
	}

	public LicensePlateReader(int garageID, Location location, BlockingQueue<String> sharedQueue) {
		this.id = count++;
		this.LPRLocation = location;
		this.garageID = garageID;
		this.sharedQueue = sharedQueue;
		this.gate = new Gate(garageID, location);
	}

	public int getGarageID() {
		return garageID;
	}

	public int getLPRid() {
		return id;
	}

	@Override
	public void run() {
		String plate = "";

		while (running) {
			try {
				Thread.sleep(5000); // Pretend Cars come in every 5seconds
				if (LPRLocation == Location.Entry) { // Entrance License Plate Reader
					// create Message class with a new ticket with the license plate and send to
					// server;
					plate = randomPlate();
					System.out.println(plate);
					sharedQueue.put(plate);
					new Thread(gate).start(); // open the gate and auto close

					while (!gate.isGateOpen()) {
						Thread.sleep(2000); // sleep 2 second, so gate can set up sensors
					}
					while (gate.isGateOpen()) { // while the gate is open.
						Thread.sleep(3000); // do nothing, keep sleeping
					}
					// gate will be close at this point, that means car entered;
					// start the loop again to pretend car comes in;

				} else if (LPRLocation == Location.Exit) { // Exit License Plate Reader
					// need to find a way to randomly select a unpaid ticket to pay.

				}
			} catch (Exception e) {
				stop();
				System.out.println(e);
			}

		}
	}

	public void stop() {
		running = false;
	}

	public String randomPlate() {// Generate a random string as license plate and return it;
		String str = "";
		for (int i = 0; i < 3; i++) {
			str += (char) ('A' + random.nextInt(26)); // get a letter
			str += Integer.toString(random.nextInt(10)); // get a number
		}
		return str;
	}
}
