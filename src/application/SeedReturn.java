package application; // root application package

// represents a harvest seed return submitted by a farmer
public class SeedReturn { // declare seedreturn class
	private int returnId; // unique return id number
	private int requestId; // linked seed request id
	private String farmer; // farmer username who submitted the return
	private int lotId; // destination seed lot id
	private String seedLabel; // crop and variety label
	private double amount; // weight of seeds returned in kg
	private String status; // Pending, Accepted, or Rejected
	private String returnDate; // date return was submitted (yyyy-mm-dd)

	// full constructor including return date
	public SeedReturn(int returnId, int requestId, String farmer, int lotId, // continue statement
			String seedLabel, double amount, String status, String returnDate) { // declare parameter
		this.returnId = returnId; // assign return id
		this.requestId = requestId; // assign linked request id
		this.farmer = farmer; // assign farmer username
		this.lotId = lotId; // assign lot id
		this.seedLabel = seedLabel; // assign seed label
		this.amount = amount; // assign returned weight
		this.status = status; // assign return status
		this.returnDate = returnDate == null ? "" : returnDate; // assign return date
	}

	// backward-compatible constructor
	public SeedReturn(int returnId, int requestId, String farmer, int lotId, // continue statement
			String seedLabel, double amount, String status) { // declare parameter
		this(returnId, requestId, farmer, lotId, seedLabel, amount, status, ""); // delegate to full constructor
	}

	public int getReturnId() { // return id getter
		return returnId; // integer return id
	}

	public int getRequestId() { // linked request id getter
		return requestId; // integer request id
	}

	public String getFarmer() { // farmer username getter
		return farmer; // farmer username
	}

	public int getLotId() { // seed lot id getter
		return lotId; // integer lot id
	}

	public String getSeedLabel() { // seed label getter
		return seedLabel; // crop and variety text
	}

	public double getAmount() { // returned amount getter
		return amount; // weight in kg
	}

	public String getStatus() { // status getter
		return status; // Pending, Accepted, or Rejected
	}

	public void setStatus(String status) { // update return status
		this.status = status; // store new status
	}

	public String getReturnDate() { // return date getter
		return returnDate; // yyyy-mm-dd string
	}

	public void setReturnDate(String returnDate) { // update return date
		this.returnDate = returnDate; // store date string
	}
}
