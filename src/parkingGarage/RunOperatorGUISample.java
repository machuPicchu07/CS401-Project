package parkingGarage;

public class RunOperatorGUISample {

	public static void main(String[] args) {
		OperatorGUI gui = new OperatorGUI(0);
		new Thread(gui).run();
	}

}
