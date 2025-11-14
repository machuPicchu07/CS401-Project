package parkingGarage;

import java.util.Random;

public class CreditCard {
	private String cardNum;

	public CreditCard() {
		this.cardNum = generateCardNum();
	}

	private String generateCardNum() {
		String str = "";
		Random random = new Random();

		// pretend to have an invalid credit card number;
		if (random.nextInt(6) % 5 == 0) {
			return "0000";
		}

		// else get 16 digit card number;
		for (int i = 1; i < 17; i++) {
			str += Integer.toString(random.nextInt(10));
			if (i % 4 == 0) {
				str += " ";
			}
		}

		return str;
	}

	public String getCardNum() {
		return cardNum;
	}
}
