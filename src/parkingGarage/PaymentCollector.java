package parkingGarage;

public class PaymentCollector {

	private CreditCard card;

	public PaymentCollector(CreditCard card) {
		this.card = card;
	};

	public boolean validatePayment() {
		// if card number is 16 digits, we will pretend its valid card
		return card.getCardNum().length() == 20;
	}
}