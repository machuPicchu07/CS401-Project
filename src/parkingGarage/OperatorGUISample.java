//
//package parkingGarage;
//
//import java.awt.BorderLayout;
//import java.awt.Color;
//import java.awt.Dimension;
//import java.awt.FlowLayout;
//import java.awt.Font;
//import java.awt.GridBagConstraints;
//import java.awt.GridBagLayout;
//import java.awt.Insets;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.ObjectInputStream;
//import java.io.ObjectOutputStream;
//import java.io.OutputStream;
//import java.net.Socket;
//
//import javax.swing.BorderFactory;
//import javax.swing.Box;
//import javax.swing.BoxLayout;
//import javax.swing.JButton;
//import javax.swing.JFrame;
//import javax.swing.JLabel;
//import javax.swing.JOptionPane;
//import javax.swing.JPanel;
//import javax.swing.JPasswordField;
//import javax.swing.JScrollPane;
//import javax.swing.JTextArea;
//import javax.swing.JTextField;
//import javax.swing.SwingUtilities;
//
//public class OperatorGUISample implements Runnable {
//	private Socket socket;
//	private ObjectOutputStream out;
//	private ObjectInputStream in;
//	private boolean isConnected = false;
//	private boolean isLoggedIn = false;
//
//	// Operator data
//	private Operator operator;
//	private int garageID;
//
//	// GUI components
//	private JFrame mainFrame;
//	private JPanel loginPanel;
//	private JPanel dashboardPanel;
//
//	// Login components
//	private JTextField usernameField;
//	private JPasswordField passwordField;
//	private JButton loginButton;
//	private JLabel statusLabel;
//
//	// Dashboard components
//	private JButton getReportButton;
//	private JButton searchButton;
//	private JTextField searchField;
//	private JTextArea displayArea;
//
//	// Server connection details
//	private static final String SERVER_HOST = "localhost";
//	private static final int SERVER_PORT = 7777;
//
//	public OperatorGUISample() {
//		this.operator = null;
//		this.garageID = -1;
//	}
//
//	@Override
//	public void run() {
//		SwingUtilities.invokeLater(this::createAndShowGUI);
//
//	}
//
//	// Creates and displays the main GUI window
//	private void createAndShowGUI() {
//		mainFrame = new JFrame("Parking Garage Operatore");
//		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		mainFrame.setSize(700, 500);
//		mainFrame.setLocationRelativeTo(null);
//
//		// Create both panels
//		createLoginPanel();
//		createDashboardPanel();
//
//		// Show login panel initially
//		mainFrame.setContentPane(loginPanel);
//		mainFrame.setVisible(true);
//
//		connectToServer();
//	}
//
//	// Establishes connection to the parking garage server
//	private void connectToServer() {
//		try {
//			socket = new Socket(SERVER_HOST, SERVER_PORT);
//
//			// Create output stream first
//			OutputStream outputStream = socket.getOutputStream();
//			out = new ObjectOutputStream(outputStream);
//			out.flush();
//
//			// Create input stream
//			InputStream inputStream = socket.getInputStream();
//			in = new ObjectInputStream(inputStream);
//
//			isConnected = true;
//			updateStatus("Connected to server", Color.GREEN);
//
//			// Start listening for server messages
//			startServerListener();
//
//		} catch (IOException e) {
//			isConnected = false;
//			updateStatus("Failed to connect to server: " + e.getMessage(), Color.BLUE);
//			JOptionPane.showMessageDialog(mainFrame, "Couldn't connect to server.", "Connection Error",
//					JOptionPane.ERROR_MESSAGE);
//
//		}
//
//	}
//
//	// Starts a background thread to listen for server messages
//
//	private void startServerListener() {
//		Thread listenerThread = new Thread(() -> {
//			try {
//				while (isConnected && !socket.isClosed()) {
//					Message msg = (Message) in.readObject();
//					handleServerMessage(msg);
//				}
//			} catch (IOException | ClassNotFoundException e) {
//				if (isConnected) {
//					SwingUtilities.invokeLater(() -> {
//						JOptionPane.showMessageDialog(mainFrame, "Connection to server lost.", "Connection Error",
//								JOptionPane.ERROR_MESSAGE);
//					});
//				}
//				isConnected = false;
//			}
//		});
//		listenerThread.setDaemon(true);
//		listenerThread.start();
//	}
//
//	// Handles messages received from the server
//
//	private void handleServerMessage(Message msg) {
//		SwingUtilities.invokeLater(() -> {
//			switch (msg.getMsgType()) {
//			case SUCCESS:
//				handleLoginSuccess(msg);
//				break;
//			case GETREPORT:
//				handleReportReceived(msg);
//				break;
//			case LOOKUPPAIDTICKET:
//				handleTicketLookupResult(msg);
//				break;
//			default:
//				System.out.println("Received message: " + msg.getMsgType());
//				break;
//			}
//		});
//	}
//
//	// Creates the login panel
//	// Username:
//	// Password:
//	// Button: login
//
//	private void createLoginPanel() {
//		loginPanel = new JPanel(new GridBagLayout());
//		loginPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
//		GridBagConstraints gbc = new GridBagConstraints();
//		gbc.insets = new Insets(10, 10, 10, 10);
//		gbc.fill = GridBagConstraints.HORIZONTAL;
//
//		// Username label
//		gbc.gridx = 0;
//		gbc.gridy = 0;
//		gbc.anchor = GridBagConstraints.EAST;
//		loginPanel.add(new JLabel("Username:"), gbc);
//
//		// Username field
//		usernameField = new JTextField(20);
//		usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
//		gbc.gridx = 1;
//		gbc.anchor = GridBagConstraints.WEST;
//		loginPanel.add(usernameField, gbc);
//
//		// Password label
//		gbc.gridy = 1;
//		gbc.gridx = 0;
//		gbc.anchor = GridBagConstraints.EAST;
//		loginPanel.add(new JLabel("Password:"), gbc);
//
//		// Password field
//		passwordField = new JPasswordField(20);
//		passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
//		gbc.gridx = 1;
//		gbc.anchor = GridBagConstraints.WEST;
//		loginPanel.add(passwordField, gbc);
//
//		// Login button
//		loginButton = new JButton("login");
//		loginButton.setFont(new Font("Arial", Font.BOLD, 14));
//		loginButton.setPreferredSize(new Dimension(100, 30));
//		loginButton.addActionListener(e -> handleLogin());
//		gbc.gridy = 2;
//		gbc.gridx = 0;
//		gbc.gridwidth = 2;
//		gbc.anchor = GridBagConstraints.CENTER;
//		loginPanel.add(loginButton, gbc);
//
//		// Status label
//		statusLabel = new JLabel(" ");
//		statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
//		gbc.gridy = 3;
//		loginPanel.add(statusLabel, gbc);
//
//		// Add enter key listener to password field
//		passwordField.addActionListener(e -> handleLogin());
//	}
//
//	// Creates the operator dashboard panel
//	// Button: GetReport
//	// License Plate:
//	// Button: Search
//
//	private void createDashboardPanel() {
//		dashboardPanel = new JPanel(new BorderLayout(10, 10));
//		dashboardPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
//
//		// Top panel - Controls
//		JPanel controlPanel = new JPanel();
//		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
//		controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
//
//		// GetReport button
//		JPanel reportButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
//		getReportButton = new JButton("GetReport");
//		getReportButton.setFont(new Font("Arial", Font.BOLD, 14));
//		getReportButton.addActionListener(e -> handleGetReport());
//		reportButtonPanel.add(getReportButton);
//		controlPanel.add(reportButtonPanel);
//
//		// Add some spacing
//		controlPanel.add(Box.createVerticalStrut(15));
//
//		// Search panel
//		JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
//		searchPanel.add(new JLabel("License Plate:"));
//		searchField = new JTextField(15);
//		searchField.setFont(new Font("Arial", Font.PLAIN, 14));
//		searchField.addActionListener(e -> handleSearch());
//		searchPanel.add(searchField);
//
//		searchButton = new JButton("Search");
//		searchButton.setFont(new Font("Arial", Font.BOLD, 14));
//		searchButton.addActionListener(e -> handleSearch());
//		searchPanel.add(searchButton);
//		controlPanel.add(searchPanel);
//
//		dashboardPanel.add(controlPanel, BorderLayout.NORTH);
//
//		// Center - Display area for results
//		displayArea = new JTextArea();
//		displayArea.setEditable(false);
//		displayArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
//		displayArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));
//		JScrollPane scrollPane = new JScrollPane(displayArea);
//		dashboardPanel.add(scrollPane, BorderLayout.CENTER);
//	}
//
//	// Handles login button click
//	// Client sends Message with Operator(username/password/garageId) to server
//	// Server verifies operator and sends "SUCCESS" message type back if operator
//	// exists
//
//	private void handleLogin() {
//		if (!isConnected) {
//			updateStatus("Not connected to server", Color.RED);
//			return;
//		}
//
//		String username = usernameField.getText().trim();
//		String password = new String(passwordField.getPassword());
//
//		if (username.isEmpty() || password.isEmpty()) {
//			updateStatus("Please enter username and password", Color.RED);
//			return;
//		}
//
//		// Create operator object and send login request
//		try {
//			operator = new Operator(username, password, garageID);
//			Message loginMsg = new Message(MsgTypes.LOGIN, garageID);
//			loginMsg.setOperator(operator);
//
//			out.writeObject(loginMsg);
//			out.flush();
//
//			updateStatus("Authenticating...", Color.BLUE);
//			loginButton.setEnabled(false);
//
//		} catch (IOException e) {
//			updateStatus("Login failed: " + e.getMessage(), Color.RED);
//			loginButton.setEnabled(true);
//		}
//	}
//
//}
