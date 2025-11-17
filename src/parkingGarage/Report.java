package parkingGarage;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;

public class Report implements Serializable {
	
	//Private variables
	private int garageID;
	private int avgStayTime;
	private double totalFee;
	private LocalDate creationDate;
	private ArrayList<Ticket> tickets;

	//Default Constructor
	public Report() {
		this.garageID = 0;
		this.avgStayTime = 0;
		this.totalFee = 0;
		this.creationDate = LocalDate.now();
	}

	//Parameterized Constructor
	public Report(int garageID, ArrayList<Ticket> tickets) {
		this.garageID = garageID;
		this.tickets = tickets;				//PAIDTICKETS
		this.creationDate = LocalDate.now();
		this.avgStayTime = 0;
		this.totalFee = 0;
		calculateAvgStayTime();
		calculateTotalFee();
	}

	//Function to calculate average stay time
	private void calculateAvgStayTime() {
		Duration duration = Duration.ofSeconds(0);
		for (Ticket t : tickets) {
			//Add amount of every ticket duration of stay into 'duration'
			duration = duration.plus(t.getDurationOfStay()); 
		}
		avgStayTime = (int) duration.getSeconds() / 60 / tickets.size(); //Seconds divided by 60 divided by PAIDTICKETS size
	}

	//Function to calculate total amount of fees by adding all fees
	private void calculateTotalFee() {
		for (Ticket t : tickets) {
			totalFee += t.getFee();
		}
	}

	@Override
	public String toString() {
		//Example line in txt file: 1,45,123.75,2025-11-01
		
		//StringBuilder object to append all parts of this report into a string
		StringBuilder sb = new StringBuilder();	
		sb.append(garageID).append(",").append(avgStayTime).append(",").append(totalFee).append(",")
				.append(creationDate).append("\n");

		//Append all tickets, one per line
		for (Ticket t : tickets) {
			sb.append(t.toString());
		}

		return sb.toString();
		
		//Example Report from toString();
		/*1,45,123.75,2025-11-01
		 *1,XY658R,80,80,EntryTime,ExitTime,DurationOfStay
		 *1,A4358Z,80,80,EntryTime,ExitTime,DurationOfStay
		 *...rest of tickets
		 */
	}
}
