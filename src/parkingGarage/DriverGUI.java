package parkingGarage;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class DriverGUI implements Runnable {

	private static int count = 0;
	private Gate gate;
	private LicensePlateReader LPR;
	private int garageID;
	private int GuiID;
	private Ticket ticket;
	private GUIgetUnpaidTicket getUnpaidCallback;
	private GUIpaidTicket paidTicketCallback;

//	private JFrame frame;
	private JButton leaveButton;
	private JLabel durationLabel, plateLabel, feeLabel, welcomeText;
	private JButton payButton;
	// this is used to store license plate from exit license plate reader
	BlockingQueue<String> queue = new LinkedBlockingQueue<String>();

	public DriverGUI(int garageID, GUIgetUnpaidTicket getUnpaidCallback, GUIpaidTicket paidTicketCallback) {
		this.gate = new Gate(garageID, Location.Exit);
		this.garageID = garageID;
		this.GuiID = ++count;
		this.LPR = new LicensePlateReader(garageID, Location.Exit, queue);
		this.ticket = null;
		this.getUnpaidCallback = getUnpaidCallback;
		this.paidTicketCallback = paidTicketCallback;
	}

	public int getGarageID() {
		return garageID;
	}

	public int getGuiID() {
		return GuiID;
	}

	@Override
	public void run() {
		SwingUtilities.invokeLater(this::createWindow);
		// createWindow();
	}

	private void createWindow() {
		String name = "Garage ID #" + garageID + ", GUI #" + GuiID;
		JFrame frame = new JFrame(name);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		createUI(frame);
		frame.setSize(1000, 300);
		frame.setLocationRelativeTo(null); // Center on screen
		frame.setVisible(true); // make visible
	}

	private void createUI(final JFrame frame) {
		JPanel panel = new JPanel();
		LayoutManager layout = new FlowLayout(FlowLayout.LEFT, 10, 10);
		panel.setLayout(layout);

		welcomeText = new JLabel("Thanks for staying. Are you leaving?");
		leaveButton = new JButton("Yes (Get Unpaid Ticket)");
		payButton = new JButton("pay");
		payButton.setEnabled(false);
		// result section
		JPanel result = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
		result.add(new JLabel("Duration of Stay:"));
		durationLabel = new JLabel("-");
		result.add(durationLabel);

		result.add(new JLabel("License Plate:"));
		plateLabel = new JLabel("-");
		result.add(plateLabel);

		result.add(new JLabel("Amount Due:"));
		feeLabel = new JLabel("-");
		result.add(feeLabel);
//		logArea = new JTextArea(7, 48);
//		logArea.setEditable(false);

		payButton.addActionListener((ActionEvent e) -> {
			payButtonClicked();
		});

		leaveButton.addActionListener((ActionEvent e) -> {
			getUnpaidCallback.run(GuiID); // triggers client to send GET_UNPAID
		});

		panel.add(welcomeText);
		panel.add(leaveButton);
		panel.add(result);
		panel.add(payButton);

//		result.add(durationLabel);
//		result.add(plateLabel);
//		result.add(feeLabel);
		frame.getContentPane().setLayout(new BorderLayout(8, 8));
		frame.getContentPane().add(panel, BorderLayout.NORTH);
		// frame.getContentPane().add(new JScrollPane(logArea), BorderLayout.CENTER);
	}

	/** Safe to call from ANY thread. */
	public void showUnpaidTicket(Ticket t) {
		SwingUtilities.invokeLater(() -> {
			this.ticket = t;

			if (t == null) {
				durationLabel.setText("None");
				plateLabel.setText("-");
				feeLabel.setText("-");
				// appendLine("No unpaid ticket found.");

			} else {
				durationLabel.setText(String.valueOf(t.getDurationOfStay().getSeconds()) + " seconds");
				plateLabel.setText(t.getLicensePlate());
				feeLabel.setText(String.format("$%.2f", t.getFee()));
				// appendLine("Received unpaid ticket: " + t);
				payButton.setEnabled(true);
			}

		});
	}

	public void payButtonClicked() {
		CreditCard creditCard = new CreditCard();
		PaymentCollector paymentCollector = new PaymentCollector(creditCard);

		if (paymentCollector.validatePayment()) {
			welcomeText.setText("Gate is Open, Thank you");
			payButton.setEnabled(false);
			Thread thread = new Thread(() -> {
				gate.openGate();
				SwingUtilities.invokeLater(() -> {
					resetGUI();
				});
			});
			thread.start();
			paidTicketCallback.run(GuiID, ticket);
		} else {
			welcomeText.setText("Invalid credit card, Please Pay again.");
			payButton.setEnabled(true);
		}

	}

	public void resetGUI() {
		durationLabel.setText("-");
		plateLabel.setText("-");
		feeLabel.setText("-");
		welcomeText.setText("Thanks for staying. Are you leaving?");
		// appendLine("Received unpaid ticket: " + t);
		payButton.setEnabled(false);
	}

}
