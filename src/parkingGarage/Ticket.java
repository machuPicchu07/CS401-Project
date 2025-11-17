package parkingGarage;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;

public class Ticket implements Serializable {
	
	//Private variables
	private int garageID;
	private String licensePlate;
	private double fee;
	private boolean paid;
	private LocalDateTime entryTime;
	private LocalDateTime exitTime;
	private Duration durationOfStay;
	private int GuiID;

	//Default Constructor
	public Ticket() {
	}

	//Parameterized Constructor using licensePlate and garageID (Programmer Defined)
	public Ticket(String licensePlate, int garageID) {
		this.licensePlate = licensePlate;
		this.entryTime = LocalDateTime.now();
		this.paid = false;
		this.garageID = garageID;
		this.fee = 0;
		this.exitTime = null;
	}

	//Parameterized Constructor using string from file read
	public Ticket(String stringFromTxtFile) {
		String[] parts = stringFromTxtFile.split(",");
		garageID = Integer.parseInt(parts[0]);
		licensePlate = parts[1];
		fee = Double.parseDouble(parts[2]);
		paid = parts[3].equals("1") ? true : false;
		entryTime = LocalDateTime.parse(parts[4]);
		exitTime = LocalDateTime.parse(parts[5]);
		durationOfStay = Duration.parse(parts[6]);
	}

	//Function to set exit time
	private void setExitTime() {
		this.exitTime = LocalDateTime.now();
		durationOfStay = Duration.between(entryTime, exitTime);
	}

	//Function to calculate fee, rate passed in by the client
	public void calculateFee(double ratePerS) {
		setExitTime();
		int s = (int) durationOfStay.getSeconds();
		fee = s * ratePerS;
	}

	public double getFee() {
		return fee;
	}

	public void setGuiID(int id) {
		GuiID = id;
	}

	public int getGuiID() {
		return GuiID;
	}

	public void setGarageID(int id) {
		GuiID = id;
	}

	public int getGarageID() {
		return GuiID;
	}

	public Duration getDurationOfStay() {
		return durationOfStay;
	}

	public boolean isTicketPaid() {
		return paid;
	}

	public void setTicketPaid() {
		paid = true;
	}

	public void setLicensePlate(String licensePlate) {
		this.licensePlate = licensePlate;

	}

	public String getLicensePlate() {
		return licensePlate;
	}

	public void setEntryTime(LocalDateTime entryTime) {
		this.entryTime = entryTime;
	}

	public LocalDateTime getEntryTime() {
		return entryTime;
	}

	//Create a String from the information from the Ticket variables, using ',' as a separator
	@Override
	public String toString() {
		return garageID + "," + licensePlate + "," + fee + "," + paid + "," + entryTime + "," + exitTime + ","
				+ durationOfStay + "\n";
	}

}
