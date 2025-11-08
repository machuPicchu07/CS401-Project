package parkingGarage;

import java.util.Random;

class GateSensor implements Runnable {
	private boolean running;
	private boolean carDetected;
	private boolean carExited;

	public GateSensor() {
		running = true;
		carDetected = false;
		carExited = false;
	}

	@Override
	public void run() {
		reset();
		running = true;
		carDetected = false;
		Random random = new Random();
		int randomNumber;
		while (running) {
			try {
				Thread.sleep(3000); // check it every 3 second
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (!carDetected) { // if car havent pass the sensor do the do the following
				randomNumber = random.nextInt(6);
				if (randomNumber % 3 == 0) { // pretend the car is in the lane now
					carDetected = true;
				}
			} else { // if car have passed or in the land. do the following
				randomNumber = random.nextInt(6);
				if (randomNumber % 3 == 0) { // pretend the car left the land
					carDetected = false;
					carExited = true;
					running = false; // exit the loop
					return;
				}
			}

		}
	}

	public void reset() { // reset sensor states
		running = true;
		carDetected = false;
		carExited = false;
	}

	public boolean getCarDetected() {
		return carDetected;
	}

	public boolean isCarExited() {
		return carExited;
	}
}
