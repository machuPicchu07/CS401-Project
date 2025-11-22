package parkingGarage;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class OperatorGUI implements Runnable {

	// private boolean isConnected = false;
	private boolean isLoggedIn = false;

	// Operator data
	// private Operator operator;
	private int garageID;
	OperatorGUILoginCB operatorLoginCallback;
	OperatorGUIgetReportCB operatorGetReportCallback;
	OperatorGUISearchTicketCB operatorGUISearchTicketCallback;
	// GUI components
	private JFrame mainFrame;
	private JPanel loginPanel;
	private JPanel dashboardPanel;

	// Login components
	private JTextField usernameField;
	private JPasswordField passwordField;
	private JButton loginButton;
	private JLabel statusLabel;

	// Dashboard components
	private JButton logoutButton;
	private JButton getReportButton;
	private JButton searchButton;
	private JTextField searchField;
	private JTextArea displayArea;

	public OperatorGUI(int garageID, OperatorGUILoginCB operatorLoginCallback,
			OperatorGUIgetReportCB operatorGetReportCallback,
			OperatorGUISearchTicketCB operatorGUISearchTicketCallback) {

		this.garageID = garageID;
		this.operatorLoginCallback = operatorLoginCallback;
		this.operatorGetReportCallback = operatorGetReportCallback;
		this.operatorGUISearchTicketCallback = operatorGUISearchTicketCallback;
	}

	@Override
	public void run() {
		SwingUtilities.invokeLater(this::createWindow);

	}

	// Creates and displays the main GUI window
	private void createWindow() {
		mainFrame = new JFrame("Parking Garage Operator GUI");
		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainFrame.setSize(1200, 600);
		mainFrame.setLocationRelativeTo(null);

		// Create both panels
		createLoginPanel();
		createDashboardPanel();

		mainFrame.setContentPane(loginPanel);
		mainFrame.setVisible(true);

	}

	private void createLoginPanel() {
		loginPanel = new JPanel(new GridBagLayout());
		loginPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(10, 10, 10, 10);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// Username label
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.EAST;
		loginPanel.add(new JLabel("Username:"), gbc);

		// Username field
		usernameField = new JTextField(20);
		usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
		gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.WEST;
		loginPanel.add(usernameField, gbc);

		// Password label
		gbc.gridy = 1;
		gbc.gridx = 0;
		gbc.anchor = GridBagConstraints.EAST;
		loginPanel.add(new JLabel("Password:"), gbc);

		// Password field
		passwordField = new JPasswordField(20);
		passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
		gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.WEST;
		loginPanel.add(passwordField, gbc);

		// Login button
		loginButton = new JButton("login");
		loginButton.setFont(new Font("Arial", Font.BOLD, 14));
		loginButton.setPreferredSize(new Dimension(100, 30));
		loginButton.addActionListener(e -> login());
		gbc.gridy = 2;
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.CENTER;
		loginPanel.add(loginButton, gbc);

		// Status label
		statusLabel = new JLabel("");
		statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
		gbc.gridy = 3;
		loginPanel.add(statusLabel, gbc);

	}

	// Creates the operator dashboard panel
	// Button: GetReport
	// License Plate:
	// Button: Search

	private void createDashboardPanel() {
		dashboardPanel = new JPanel(new BorderLayout(10, 10));
		dashboardPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		// Top panel - Controls
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
		controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		// GetReport button
		JPanel reportButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		getReportButton = new JButton("GetReport");
		getReportButton.setFont(new Font("Arial", Font.BOLD, 14));
		getReportButton.addActionListener(e -> getReport());
		reportButtonPanel.add(getReportButton);

		// Logout Button
		logoutButton = new JButton("logout");
		logoutButton.setFont(new Font("Arial", Font.BOLD, 14));
		reportButtonPanel.add(logoutButton);
		logoutButton.addActionListener(e -> logout());

		controlPanel.add(reportButtonPanel);

		// Add some spacing
		controlPanel.add(Box.createVerticalStrut(15));

		// Search panel
		JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		searchPanel.add(new JLabel("License Plate:"));
		searchField = new JTextField(15);
		searchField.setFont(new Font("Arial", Font.PLAIN, 14));
//		searchField.addActionListener(e -> handleSearch());
		searchPanel.add(searchField);

		searchButton = new JButton("Search");
		searchButton.setFont(new Font("Arial", Font.BOLD, 14));
		searchButton.addActionListener(e -> searchTicket());
		searchPanel.add(searchButton);
		controlPanel.add(searchPanel);

		dashboardPanel.add(controlPanel, BorderLayout.NORTH);

		// Center - Display area for results
		displayArea = new JTextArea();
		displayArea.setEditable(false);
		displayArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
		displayArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		JScrollPane scrollPane = new JScrollPane(displayArea);
		dashboardPanel.add(scrollPane, BorderLayout.CENTER);
	}

	private void login() {
		// get operator log in username/pw from GUI
		String username = usernameField.getText().trim();
		String pw = new String(passwordField.getPassword());
		statusLabel.setText("");
		// call the callback function from ParkingGarage
		operatorLoginCallback.run(username, pw);

	}

	private void logout() {
		SwingUtilities.invokeLater(() -> {
			displayArea.setText("");
			searchField.setText("");
			mainFrame.setContentPane(loginPanel);
			mainFrame.revalidate();
			mainFrame.repaint();
		});
	}

	public void loggedInSuccess() {
		SwingUtilities.invokeLater(() -> {
			isLoggedIn = true;
			passwordField.setText("");
			usernameField.setText("");
			mainFrame.setContentPane(dashboardPanel);
			mainFrame.revalidate();
			mainFrame.repaint();
		});

	}

	public void loggedInFail() {
		SwingUtilities.invokeLater(() -> {
			statusLabel.setText("Invalid username or password");
			passwordField.setText("");
			usernameField.setText("");
		});
	}

	private void getReport() {
		operatorGetReportCallback.run();
	}

	public void displayReport(Report report) {
		if (report != null) {
			StringBuilder sb = new StringBuilder();
			sb.append("Report for garage #").append(report.getGarageId()).append('\n');
			sb.append("----------------------------------------------------\n");
			sb.append("Average stay time: ").append(report.getAvgStayTIme()).append(" mins \n");
			sb.append("Total fee: $").append(report.getTotalFee()).append("\n");
			// sb.append(report.getTicketStrings());
			List<Ticket> tickets = report.getTickets();
			for (Ticket t : tickets) {
				sb.append(formatTicket(t));
			}
			SwingUtilities.invokeLater(() -> {
				displayArea.setText(sb.toString());
				displayArea.setCaretPosition(0);
			});
		}

	}

	private void searchTicket() {
		SwingUtilities.invokeLater(() -> {
			String licensePlate = searchField.getText().trim().toUpperCase();
			if (!licensePlate.equals("")) {
				operatorGUISearchTicketCallback.run(licensePlate);
			}
		});
	}

	public void displayTicket(Ticket ticket) {

		SwingUtilities.invokeLater(() -> {
			StringBuilder sb = new StringBuilder();
			if (ticket.getEntryTime() == null) {
				// that means this ticket not found;
				sb.append(ticket.getLicensePlate() + " is not found");
				displayArea.setText(sb.toString());
				displayArea.setCaretPosition(0);
			} else {
				sb.append(formatTicket(ticket));
				displayArea.setText(sb.toString());
				displayArea.setCaretPosition(0);
			}
		});

	}

	private String formatTicket(Ticket ticket) {

		String entryTime = (ticket.getEntryTime() != null)
				? ticket.getEntryTime().format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"))
				: "null";
		String exitTime = (ticket.getExitTime() != null)
				? ticket.getExitTime().format(DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss"))
				: "null";
		String isPaid = (ticket.isTicketPaid() == true) ? "true" : "false";
		StringBuilder sb = new StringBuilder();
		String duration = (ticket.getDurationOfStay() == null) ? "null"
				: String.valueOf(ticket.getDurationOfStay().getSeconds());

		sb.append("\nLicense Plate: " + ticket.getLicensePlate());
		sb.append("  Garage #: " + ticket.getGarageID());
		sb.append("  Paid? : " + isPaid);
		sb.append("  Entry Time: " + entryTime);
		sb.append("  Exit Time: " + exitTime);
		sb.append("  Duration of Stay:  " + duration);
		sb.append("  Fee: $" + ticket.getFee());

		return sb.toString();
	}
//	private void handleGetReport() {

//	}

}