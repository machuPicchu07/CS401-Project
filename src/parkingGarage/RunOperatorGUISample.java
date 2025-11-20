package parkingGarage;

public class RunOperatorGUISample {

	public static void main(String[] args) {
		OperatorGUISample gui = new OperatorGUISample();
		new Thread(gui).run();
	}

}
