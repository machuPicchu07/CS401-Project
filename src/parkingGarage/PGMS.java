package parkingGarage;

import java.io.BufferedReader;
//java.io.*
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
//java.net.*
import java.net.ServerSocket;
import java.net.Socket;
//java.util.*
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

//Parking Garage Management System (PGMS)
public class PGMS {

	// Private Variables
	// Two Two-Dimensional Array Lists of Ticket Objects
	// Garage ID as the Index, PAIDTICKETS or UNPAIDTICKETS object as each element
	private static final List<List<Ticket>> PAIDTICKETS = new ArrayList<>();
	private static final List<List<Ticket>> UNPAIDTICKETS = new ArrayList<>();

	private static int garageCount = 0;
	static String numberOfGarageFileName = "numberOfGarage.txt";

	// Program Main Section
	public static void main(String[] args) {

		// Lazy Instantiation of ServerSocket
		ServerSocket server = null;

		try {
			checkGarages();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			server = new ServerSocket(7777); // Run server on Socket 7777
			server.setReuseAddress(true);

			while (true) { // Run server perpetually
				// Create a socket object 'client' equal to a connecting socket, wait (block)
				// until a socket has connected
				Socket client = server.accept();

				// ClientHandler object created using the connected socket
				ClientHandler clientSock = new ClientHandler(client);

				// A thread is started using the ClientHandler object, handling the client
				// separately
				new Thread(clientSock).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (server != null) {
				try {
					server.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static void checkGarages() throws IOException {

		// check if the server is running the first time.
		// if its running the first time, create the file and save it.
		// otherwise load the total of garage from file to garageCount;
		File file = new File(numberOfGarageFileName);
		if (file.exists()) {
			String garageNumber;
			try (Scanner scanner = new Scanner(file)) {
				garageNumber = scanner.nextLine().trim();
				garageCount = Integer.parseInt(garageNumber);
			}
		} else {
			try (FileWriter writer = new FileWriter(numberOfGarageFileName, true)) {
				writer.write(String.valueOf(garageCount));
			}
		}
	}

	private static class ClientHandler implements Runnable {

		// Private Variables
		private final Socket clientSocket;
		private int garageID;
		boolean loggedIn = false;
		private MsgTypes msgType;
		private final Object fileLockHandler = new Object();

		// Constructor
		public ClientHandler(Socket socket) {
			this.clientSocket = socket;
		}

		public void run() {
			try {
				// Create ObjectOutputStream from the OutPutStream
				ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());

				// Create ObjectInputStream from the InputStream
				ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());

				// Create Message Object
				Message inMsg;
				Message outMsg;
				// While the receiving Message is not null
				while ((inMsg = (Message) in.readObject()) != null) {

					// Attain Message Type
					msgType = inMsg.getMsgType();

					if (!loggedIn) {
						if (msgType == MsgTypes.NEWGARAGE) { // If not logged in and MsgType == NEWGARAGE
							garageID = garageCount++; // Increase Garage Count and Assign that to Garage ID
							createNewGarage(garageID); // Run Function createNewGarage with Current Garage ID

						} else if (msgType == MsgTypes.GARAGELOGIN) { // If garage existed
							garageID = inMsg.getGarageID();
							loadGarage(garageID); // get the garageID and load tickets for garage
						}
						loggedIn = true; // Set this Garage to logged in
						// Create new Message object with MsgType
						outMsg = new Message(MsgTypes.SUCCESS, garageID);
						out.writeObject(outMsg); // Send response to Client

					} else {
						switch (msgType) { // If the garage is Logged In, Check MsgType

						// MsgType NEWTICKET adds Ticket to File & Array UNPAIDTICKETS
						// Creates new ticket with RECEIVED MsgType w/ Garage ID to send to Client as
						// response
						case NEWTICKET: {
							addNewTicketToFile(inMsg);

							// Response to Client
							outMsg = new Message(MsgTypes.RECEIVED, garageID);
							out.writeObject(outMsg);
							out.flush();
							break;
						}

						// MsgType LOOKUPTICKET creates a new Ticket object from the client's request
						// If ticket is found from the two dimensional array, send ticket 'reply' to
						// client
						case LOOKUPUNPAIDTICKET: {
							Ticket ticket = lookUpUnpaidTicket(garageID, inMsg);
							if (ticket != null) {
								outMsg = new Message(MsgTypes.LOOKUPUNPAIDTICKET, garageID);
								outMsg.setTicket(ticket);
								out.writeObject(outMsg);
								out.flush();
							}
							break;
						}
						case TICKETPAID: {
//							String str = inMsg.getTicket().toString();
//							System.out.println(str);
							ticketIsPaid(inMsg);
							break;
						}
						case OPERATORLOGIN: {
							if (isOperatorAuthenticated(inMsg)) {
								outMsg = new Message(MsgTypes.OPERATORSUCCESS, garageID);
								System.out.println("correct pw");
								out.writeObject(outMsg);
								out.flush();
							} else {
								outMsg = new Message(MsgTypes.OPERATORFAILURE, garageID);
								System.out.println("wrong pw");
								out.writeObject(outMsg);
								out.flush();
							}
							break;

						}
						case GETREPORT: {
							outMsg = new Message(MsgTypes.GETREPORT, garageID);
							List<Ticket> source = PAIDTICKETS.get(garageID);
							List<Ticket> copy;
							synchronized (source) {
								copy = new ArrayList<>(source); // shallow copy of the list
							}

							Report report = new Report(garageID, copy);

							Operator operator = new Operator();
							operator.setReport(report);
							outMsg.setOperator(operator);
							out.writeObject(outMsg);
							out.flush();
							System.out.println("sent report");
							// Operator(String username, String password, Report report, int garageID)
							break;
						}
						case SEARCHTICKET: {
							Ticket ticket = searchTicket(inMsg);
							outMsg = new Message(MsgTypes.SEARCHTICKET, garageID);
							outMsg.setTicket(ticket);
							out.writeObject(outMsg);
							out.flush();
							break;
						}
						default:
							throw new IllegalArgumentException("Unexpected value: " + msgType);
						}

					}
				}

			} catch (EOFException eof) {

			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			} finally {
				try {
					clientSocket.close();
				} catch (IOException ignore) {
				}
			}
		}

		// Function to Create a new Garage with a garage ID
		private void createNewGarage(int garageID) throws IOException {

			// Both UNPAIDTICKETS & PAIDTICKETS are instantiated and pushed into their
			// respective Two-Dimensional array

//			List<Ticket> unPaidList = new ArrayList<Ticket>();
//			UNPAIDTICKETS.add(unPaidList); // first garage is on UNPAIDTICKETS[0];
//			List<Ticket> paidList = new ArrayList<Ticket>();
//			PAIDTICKETS.add(paidList);

			// make sure the list exist
			while (UNPAIDTICKETS.size() <= garageID)
				UNPAIDTICKETS.add(null);
			while (PAIDTICKETS.size() <= garageID)
				PAIDTICKETS.add(null);
			if (UNPAIDTICKETS.get(garageID) == null)
				UNPAIDTICKETS.set(garageID, new ArrayList<>());
			if (PAIDTICKETS.get(garageID) == null)
				PAIDTICKETS.set(garageID, new ArrayList<>());
			// Format text file name
			String fileNamePaid = "garage#" + Integer.toString(garageID) + "_paid.txt";
			String fileNameUnpaid = "garage#" + Integer.toString(garageID) + "_unpaid.txt";

			// Creates both text files
			try (FileWriter writerPaid = new FileWriter(fileNamePaid, true);
					FileWriter writerUnpaid = new FileWriter(fileNameUnpaid, true)) {
				System.out.println("Created New Garage # " + garageID);
			}

			// save total number of garages on server
			try (FileWriter writer = new FileWriter(numberOfGarageFileName)) {
				writer.write(String.valueOf(garageCount));
			}
		}

		// Adds new Ticket to file
		private void addNewTicketToFile(Message inMsg) throws IOException {
			synchronized (fileLockHandler) {
				UNPAIDTICKETS.get(garageID).add(inMsg.getTicket()); // Add ticket to UNPAIDTICKETS 2d array
				String fileNameUnpaid = "garage#" + garageID + "_unpaid.txt"; // Find appropriate file name
				try (FileWriter writer = new FileWriter(fileNameUnpaid, true)) { // Opens file
					writer.write(inMsg.getTicket().toString()); // Writes to file Ticket information
				}
			}

		}

		// Lookup Unpaid Ticket
		private Ticket lookUpUnpaidTicket(int garageID, Message inMsg) throws IOException {
			List<Ticket> tickets = UNPAIDTICKETS.get(garageID); // Create ticket list using UNPAIDTICKETS 2d array key
																// (garageID)
			Ticket ticket = null; // Create a ticket object
			Ticket copy = null; // create a copy of the ticket, so two DriverGUI don't show the same ticket
			if (tickets != null && !tickets.isEmpty()) { // If the UNPAIDTICKETS 2d array is not null and is not empty
				Random random = new Random(); // Create a random object
				int index = random.nextInt(tickets.size());
				ticket = tickets.get(index); // Grab random ticket to send to client
				// tickets.remove(index);

				copy = new Ticket();
				copy.setGuiID(inMsg.getTicket().getGuiID());
				copy.setLicensePlate(ticket.getLicensePlate());
				copy.setEntryTime(ticket.getEntryTime());
			}

			return copy; // return random ticket to client
		}

		private void ticketIsPaid(Message inMsg) throws IOException {

			Ticket ticket = inMsg.getTicket();
			PAIDTICKETS.get(garageID).add(ticket); // Add ticket to PAIDTICKETS 2d array
			String fileNamePaid = "garage#" + garageID + "_paid.txt"; // Find appropriate file name

			synchronized (fileLockHandler) {
				try (FileWriter writer = new FileWriter(fileNamePaid, true)) { // Opens file
					writer.write(ticket.toString()); // Writes to file Ticket information
				}
			}

			// remove the ticket from unpaid ticket
			if (ticket != null) {
				List<Ticket> tickets = UNPAIDTICKETS.get(garageID);
				for (int i = 0; i < tickets.size(); i++) {
					if (tickets.get(i).getLicensePlate().equals(ticket.getLicensePlate())) {
						tickets.remove(i);
					}
				}
				String fileNameUnpaid = "garage#" + garageID + "_unpaid.txt";
				synchronized (fileLockHandler) {
					StringBuilder fileInfo = new StringBuilder();

					try (BufferedReader reader = new BufferedReader(new FileReader(fileNameUnpaid))) {
						String line;
						while ((line = reader.readLine()) != null) {
							Ticket fileTicket = new Ticket(line);
							if (!fileTicket.getLicensePlate().equals(ticket.getLicensePlate())) {
								// i need to remove the "line" from the txt file.
								fileInfo.append(line).append(System.lineSeparator());
							}
						}
					}
					try (FileWriter writer = new FileWriter(fileNameUnpaid)) { // Opens file
						writer.write(fileInfo.toString()); // Writes to file Ticket information
					}
				}
			}
		}

		private void loadGarage(int garageID) throws FileNotFoundException {
			// load existing ticket from file to UNPAIDTICKETS and PAIDTICKETS for garage

			// Format text file name
			String fileNamePaid = "garage#" + Integer.toString(garageID) + "_paid.txt";
			String fileNameUnpaid = "garage#" + Integer.toString(garageID) + "_unpaid.txt";

			List<Ticket> paidList = new ArrayList<Ticket>();
			List<Ticket> unPaidList = new ArrayList<Ticket>();

			// read ticket from file and add it list;
			File file = new File(fileNamePaid);
			try (Scanner scanner = new Scanner(file)) {
				while (scanner.hasNextLine()) {
					String line = scanner.nextLine().trim();
					Ticket ticket = new Ticket(line);
					paidList.add(ticket);
				}
			}

			file = new File(fileNameUnpaid);
			try (Scanner scanner = new Scanner(file)) {
				while (scanner.hasNextLine()) {
					String line = scanner.nextLine().trim();
					Ticket ticket = new Ticket(line);
					unPaidList.add(ticket);
				}
			}
			// set it to null in case its empty;
			while (UNPAIDTICKETS.size() <= garageID)
				UNPAIDTICKETS.add(null);
			while (PAIDTICKETS.size() <= garageID)
				PAIDTICKETS.add(null);
			// add the list of tickets to list
			UNPAIDTICKETS.set(garageID, unPaidList);
			PAIDTICKETS.set(garageID, paidList);

//			System.out.println("garage# " + garageID + " unpaid tickets:");
//			for (Ticket t : unPaidList)
//				System.out.println(t);
//
//			System.out.println("garage# " + garageID + " paid tickets:");
//			for (Ticket t : paidList)
//				System.out.println(t);
		}

		private boolean isOperatorAuthenticated(Message inMsg) throws FileNotFoundException, IOException {
			String operatorUsername = inMsg.getOperator().getUsername();
			String operatorPw = inMsg.getOperator().getPassword();
			try (BufferedReader reader = new BufferedReader(new FileReader("username_pw.txt"))) {
				String line;
				while ((line = reader.readLine()) != null) {
					String[] parts = line.split(",");
					if (operatorUsername.equals(parts[0]) && operatorPw.equals(parts[1])) {
						return true;
					}

				}
			}
			return false;
		}

		private Ticket searchTicket(Message inMsg) {
			Ticket ticket = inMsg.getTicket();
			List<Ticket> tickets = UNPAIDTICKETS.get(garageID);
			for (Ticket t : tickets) {
				if (t.getLicensePlate().equals(ticket.getLicensePlate())) {
					return copyTicket(t);
				}
			}
			tickets = PAIDTICKETS.get(garageID);
			for (Ticket t : tickets) {
				if (t.getLicensePlate().equals(ticket.getLicensePlate())) {
					return copyTicket(t);
				}
			}
			return ticket;
		}

		private Ticket copyTicket(Ticket original) {
			Ticket copy = new Ticket();
			copy.setGarageID(original.getGarageID());
			copy.setLicensePlate(original.getLicensePlate());
			copy.setEntryTime(original.getEntryTime());
			copy.setExitTime(original.getExitTime());
			copy.setTicketPaid(original.isTicketPaid());
			copy.setGuiID(original.getGuiID());
			copy.setFee(original.getFee());
			copy.setDurationOfStay(original.getDurationOfStay());
			// copy.calculateFee(original.getFee());
			// etc — copy all needed fields
			return copy;
		}
	}

}
