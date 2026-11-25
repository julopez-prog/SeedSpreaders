package application; // root application package

// represents a monetary donation submitted from the landing page
public class Donation { // declare donation class
	private String donorName; // name of the donor
	private String email; // contact email of the donor
	private double amount; // donation amount in php
	private String message; // optional message of support

	// constructor for a donation record
	public Donation(String donorName, String email, double amount, String message) { // define donation method
		this.donorName = donorName; // assign donor name
		this.email = email; // assign donor email
		this.amount = amount; // assign donation amount
		this.message = message; // assign donor message
	}

	public String getDonorName() { // return donor name
		return donorName; // donor name string
	}

	public String getEmail() { // return donor email
		return email; // email string
	}

	public double getAmount() { // return donation amount
		return amount; // php amount
	}

	public String getMessage() { // return donor message
		return message; // message string
	}
}
