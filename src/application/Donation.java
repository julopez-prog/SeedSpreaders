package application;

public class Donation {
	private String donorName;
	private String email;
	private double amount;
	private String message;

	public Donation(String donorName, String email, double amount, String message) {
		this.donorName = donorName;
		this.email = email;
		this.amount = amount;
		this.message = message;
	}

	public String getDonorName() {
		return donorName;
	}

	public String getEmail() {
		return email;
	}

	public double getAmount() {
		return amount;
	}

	public String getMessage() {
		return message;
	}
}
