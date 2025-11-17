package parkingGarage;

//java.io.*
import java.io.EOFException;
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

//Parking Garage Management System (PGMS)
public class PGMS {

	// Private Variables
	// Two Two-Dimensional Array Lists of Ticket Objects
	// Garage ID as the Index, PAIDTICKETS or UNPAIDTICKETS object as each element
	private static final List<List<Ticket>> PAIDTICKETS = new ArrayList<>();
	private static final List<List<Ticket>> UNPAIDTICKETS = new ArrayList<>();

	private static int garageCount = 0;

	// Program Main Section
	public static void main(String[] args) {

		// Lazy Instantiation of ServerSocket
		ServerSocket server = null;

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

	private static class ClientHandler implements Runnable {

		// Private Variables
		private final Socket clientSocket;
		private int garageID;
		boolean loggedIn = false;
		private MsgTypes msgType;

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

				// While the receiving Message is not null
				while ((inMsg = (Message) in.readObject()) != null) {

					// Attain Message Type
					msgType = inMsg.getMsgType();

					if (!loggedIn) {
						if (msgType == MsgTypes.NEWGARAGE) { // If not logged in and MsgType == NEWGARAGE
							garageID = garageCount++; // Increase Garage Count and Assign that to Garage ID
							createNewGarage(garageID); // Run Function createNewGarage with Current Garage ID
							loggedIn = true; // Set this Garage to logged in
							inMsg = new Message(MsgTypes.SUCCESS, garageID);// Create new Message object with MsgType
																			// SUCCESS, and current Garage ID
							out.writeObject(inMsg); // Send response to Client
						}
					} else {
						switch (msgType) { // If the garage is Logged In, Check MsgType

						// MsgType NEWTICKET adds Ticket to File & Array UNPAIDTICKETS
						// Creates new ticket with RECEIVED MsgType w/ Garage ID to send to Client as
						// response
						case NEWTICKET: {
							addNewTicketToFile(inMsg);

							// Response to Client
							inMsg = new Message(MsgTypes.RECEIVED, garageID);
							out.writeObject(inMsg);
							out.flush();
							break;
						}

						// MsgType LOOKUPTICKET creates a new Ticket object from the client's request
						// If ticket is found from the two dimensional array, send ticket 'reply' to
						// client
						case LOOKUPTICKET: {
							Ticket ticket = lookUpUnpaidTicket(garageID, inMsg);
							if (ticket != null) {
								Message reply = new Message(MsgTypes.LOOKUPTICKET, garageID);
								reply.setTicket(ticket);
								out.writeObject(reply);
								out.flush();
							}

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
		private void createNewGarage(int garageID) {

			// Both UNPAIDTICKETS & PAIDTICKETS are instantiated and pushed into their
			// respective Two-Dimensional array
			List<Ticket> unPaidList = new ArrayList<Ticket>();
			UNPAIDTICKETS.add(unPaidList); // first garage is on UNPAIDTICKETS[0];
			List<Ticket> paidList = new ArrayList<Ticket>();
			PAIDTICKETS.add(paidList);

			// Format text file name
			String fileNamePaid = "garage#" + Integer.toString(garageID) + "_paid.txt";
			String fileNameUnpaid = "garage#" + Integer.toString(garageID) + "_unpaid.txt";

			// Creates both text files
			try (FileWriter writerPaid = new FileWriter(fileNamePaid, true);
					FileWriter writerUnpaid = new FileWriter(fileNameUnpaid, true)) {
				System.out.println("Appended to file!");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// Adds new Ticket to file
		private void addNewTicketToFile(Message inMsg) throws IOException {
			UNPAIDTICKETS.get(garageID).add(inMsg.getTicket()); // Add ticket to UNPAIDTICKETS 2d array
			String fileNameUnpaid = "garage#" + garageID + "_unpaid.txt"; // Find appropriate file name
			try (FileWriter writer = new FileWriter(fileNameUnpaid, true)) { // Opens file
				writer.write(inMsg.getTicket().toString()); // Writes to file Ticket information
			}
		}

		// Lookup Unpaid Ticket
		private Ticket lookUpUnpaidTicket(int garageID, Message inMsg) {
			List<Ticket> tickets = UNPAIDTICKETS.get(garageID); // Create ticket list using UNPAIDTICKETS 2d array key
																// (garageID)
			Ticket ticket = null; // Create a ticket object
			Ticket copy = null;
			if (tickets != null && !tickets.isEmpty()) { // If the UNPAIDTICKETS 2d array is not null and is not empty
				Random random = new Random(); // Create a random object
				ticket = tickets.get(random.nextInt(tickets.size())); // Grab random ticket to send to client
				copy = new Ticket();
				copy.setGuiID(inMsg.getTicket().getGuiID());
				copy.setLicensePlate(ticket.getLicensePlate());
				copy.setEntryTime(ticket.getEntryTime());

			}
			return copy; // return random ticket to client
		}

	}

}
