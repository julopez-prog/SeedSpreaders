package application; // declare package application

public class Donation { // define donation class
	private String donorName; // store donorname field
	private String email; // store email field
	private double amount; // store amount field
	private String message; // store message field

	public Donation(String donorName, String email, double amount, String message) { // initialize donation instance
		this.donorName = donorName; // assign donorname value
		this.email = email; // assign email value
		this.amount = amount; // assign amount value
		this.message = message; // assign message value
	}

	public String getDonorName() { // handle getdonorname operation
		return donorName; // return donorname
	}

	public String getEmail() { // handle getemail operation
		return email; // return email
	}

	public double getAmount() { // handle getamount operation
		return amount; // return amount
	}

	public String getMessage() { // handle getmessage operation
		return message; // return message
	}
}
