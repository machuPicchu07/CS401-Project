package parkingGarage;

public class PaymentCollector {

	private CreditCard card;

	public PaymentCollector(CreditCard card) {
		this.card = card;
	};

	public boolean validatePayment() {
		//If card number is between 13 and 19 digits, we will pretend its a valid card
		return card.getCardNum().length() < 20 && card.getCardNum().length() > 13;
	}
}