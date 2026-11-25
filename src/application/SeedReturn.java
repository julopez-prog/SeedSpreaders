package application; // declare package application

import java.time.LocalDate; // import localdate class

public class SeedReturn { // define seedreturn class
	private int returnId; // store returnid field
	private int requestId; // store requestid field
	private String farmer; // store farmer field
	private int lotId; // store lotid field
	private String seedLabel; // store seedlabel field
	private double amount; // store amount field
	private String status; // store status field
	private LocalDate returnDate; // store returndate field

	public SeedReturn(int returnId, int requestId, String farmer, int lotId, // continue statement
			String seedLabel, double amount, String status) { // open code block
		this(returnId, requestId, farmer, lotId, seedLabel, amount, status, LocalDate.now()); // execute statement
	}

	public SeedReturn(int returnId, int requestId, String farmer, int lotId, // continue statement
			String seedLabel, double amount, String status, LocalDate returnDate) { // open code block
		this.returnId = returnId; // assign returnid value
		this.requestId = requestId; // assign requestid value
		this.farmer = farmer; // assign farmer value
		this.lotId = lotId; // assign lotid value
		this.seedLabel = seedLabel; // assign seedlabel value
		this.amount = amount; // assign amount value
		this.status = status; // assign status value
		this.returnDate = returnDate; // assign returndate value
	}

	public int getReturnId() { // handle getreturnid operation
		return returnId; // return returnid
	}

	public String getReturnCode() { // handle getreturncode operation
		return String.format("RET-%03d", returnId); // return string.format("ret-%03d", returnid)
	}

	public int getRequestId() { // handle getrequestid operation
		return requestId; // return requestid
	}

	public String getFarmer() { // handle getfarmer operation
		return farmer; // return farmer
	}

	public int getLotId() { // handle getlotid operation
		return lotId; // return lotid
	}

	public String getSeedLabel() { // handle getseedlabel operation
		return seedLabel; // return seedlabel
	}

	public double getAmount() { // handle getamount operation
		return amount; // return amount
	}

	public String getQuantityLabel() { // handle getquantitylabel operation
		return amount + " kg"; // return amount + " kg"
	}

	public String getStatus() { // handle getstatus operation
		return status; // return status
	}

	public void setStatus(String status) { // handle setstatus operation
		this.status = status; // assign status value
	}

	public LocalDate getReturnDate() { // handle getreturndate operation
		return returnDate; // return returndate
	}

	public void setReturnDate(LocalDate returnDate) { // handle setreturndate operation
		this.returnDate = returnDate; // assign returndate value
	}
}
