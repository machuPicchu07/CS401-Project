package parkingGarage;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

public class Report implements Serializable {

	// Private variables
	private int garageID;
	private int avgStayTime;
	private double totalFee;
	private LocalDate creationDate;
	private List<Ticket> tickets;

	// Default Constructor
	public Report() {
		this.garageID = 0;
		this.avgStayTime = 0;
		this.totalFee = 0;
		this.creationDate = LocalDate.now();
	}

	// Parameterized Constructor
	public Report(int garageID, List<Ticket> tickets) {
		this.garageID = garageID;
		this.tickets = tickets; // PAIDTICKETS
		this.creationDate = LocalDate.now();
		this.avgStayTime = 0;
		this.totalFee = 0;
		calculateAvgStayTime();
		calculateTotalFee();
	}

	// Function to calculate average stay time
	private void calculateAvgStayTime() {
		if (tickets == null || tickets.isEmpty()) {
			avgStayTime = 0;
			return;
		}
		Duration duration = Duration.ofSeconds(0);
		for (Ticket t : tickets) {
			// Add amount of every ticket duration of stay into 'duration'
			duration = duration.plus(t.getDurationOfStay());
		}
		avgStayTime = (int) duration.getSeconds() / 60 / tickets.size(); // Seconds divided by 60 divided by
																			// PAIDTICKETS

	}

	// Function to calculate total amount of fees by adding all fees
	private void calculateTotalFee() {
		if (tickets == null)
			return;
		for (Ticket t : tickets) {
			totalFee += t.getFee();
		}
	}

	public int getGarageId() {
		return garageID;
	}

	public int getAvgStayTIme() {
		return avgStayTime;
	}

	public double getTotalFee() {
		return totalFee;
	}

	public LocalDate creationDate() {
		return creationDate;
	}

	public List<Ticket> getTickets() {
		return tickets;
	}

	public String getReportInfo() {
		StringBuilder sb = new StringBuilder();
		sb.append(garageID).append(",").append(avgStayTime).append(",").append(totalFee).append(",")
				.append(creationDate).append("\n");
		return sb.toString();
	}

	public String getTicketStrings() {
		StringBuilder sb = new StringBuilder();
		// Append all tickets, one per line
		for (Ticket t : tickets) {
			sb.append(t.toString());
		}

		return sb.toString();
	}

	@Override
	public String toString() {
		// Example line in txt file: 1,45,123.75,2025-11-01

		// StringBuilder object to append all parts of this report into a string
		StringBuilder sb = new StringBuilder();
		sb.append(garageID).append(",").append(avgStayTime).append(",").append(totalFee).append(",")
				.append(creationDate).append("\n");

		// Append all tickets, one per line
		for (Ticket t : tickets) {
			sb.append(t.toString());
		}

		return sb.toString();

	}
}
