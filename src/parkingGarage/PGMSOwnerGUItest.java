package parkingGarage;

public class PGMSOwnerGUItest {

	public static void main(String[] args) {
		PGMSOwnerGUISetRateCB cb = null;
		GUISearchTicketCB cb1 = null;
		GUIgetReportCB cb2 = null;
		PGMSOwnerGUI gui = new PGMSOwnerGUI(3, cb, cb1, cb2);
		gui.run();

	}

}
