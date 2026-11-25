package application; // declare package application

import java.time.LocalDate; // import localdate class

public class SeedRequest { // define seedrequest class
	private int requestId; // store requestid field
	private String farmer; // store farmer field
	private String farmName; // store farmname field
	private int lotId; // store lotid field
	private String name; // store name field
	private String variety; // store variety field
	private double amount; // store amount field
	private double unitPrice; // store unitprice field
	private String purpose; // store purpose field
	private String status; // store status field
	private LocalDate requestDate; // store requestdate field
	private LocalDate dueDate; // store duedate field

	public SeedRequest(int requestId, String farmer, int lotId, String name, String variety, // continue statement
			double amount, String purpose, String status) { // open code block
		this(requestId, farmer, farmer + " Farm", lotId, name, variety, amount, 0.0, // continue statement
				purpose, status, LocalDate.now(), LocalDate.now().plusDays(90)); // execute statement
	}

	public SeedRequest(int requestId, String farmer, String farmName, int lotId, String name, String variety, // continue statement
			double amount, double unitPrice, String purpose, String status, LocalDate requestDate, LocalDate dueDate) { // open code block
		this.requestId = requestId; // assign requestid value
		this.farmer = farmer; // assign farmer value
		this.farmName = farmName; // assign farmname value
		this.lotId = lotId; // assign lotid value
		this.name = name; // assign name value
		this.variety = variety; // assign variety value
		this.amount = amount; // assign amount value
		this.unitPrice = unitPrice; // assign unitprice value
		this.purpose = purpose; // assign purpose value
		this.status = status; // assign status value
		this.requestDate = requestDate; // assign requestdate value
		this.dueDate = dueDate; // assign duedate value
	}

	public int getRequestId() { // handle getrequestid operation
		return requestId; // return requestid
	}

	public String getTransactionCode() { // handle gettransactioncode operation
		return String.format("TRS-%03d", requestId); // return computed result
	}

	public String getFarmer() { // handle getfarmer operation
		return farmer; // return farmer
	}

	public String getFarmName() { // handle getfarmname operation
		return farmName; // return farmname
	}

	public void setFarmName(String farmName) { // handle setfarmname operation
		this.farmName = farmName; // assign farmname value
	}

	public int getLotId() { // handle getlotid operation
		return lotId; // return lotid
	}

	public String getName() { // handle getname operation
		return name; // return name
	}

	public String getVariety() { // handle getvariety operation
		return variety; // return variety
	}

	public String getSeedLabel() { // handle getseedlabel operation
		return name + " - " + variety; // return name + " - " + variety
	}

	public double getAmount() { // handle getamount operation
		return amount; // return amount
	}

	public String getQuantityLabel() { // handle getquantitylabel operation
		return amount + " kg"; // return amount + " kg"
	}

	public double getUnitPrice() { // handle getunitprice operation
		return unitPrice; // return unitprice
	}

	public double getTotalValue() { // handle gettotalvalue operation
		return Math.round(amount * unitPrice * 100.0) / 100.0; // return computed result
	}

	public String getPurpose() { // handle getpurpose operation
		return purpose; // return purpose
	}

	public String getStatus() { // handle getstatus operation
		return status; // return status
	}

	public void setStatus(String status) { // handle setstatus operation
		this.status = status; // assign status value
	}

	public LocalDate getRequestDate() { // handle getrequestdate operation
		return requestDate; // return requestdate
	}

	public void setRequestDate(LocalDate requestDate) { // handle setrequestdate operation
		this.requestDate = requestDate; // assign requestdate value
	}

	public LocalDate getDueDate() { // handle getduedate operation
		return dueDate; // return duedate
	}

	public void setDueDate(LocalDate dueDate) { // handle setduedate operation
		this.dueDate = dueDate; // assign duedate value
	}

	public boolean isOverdue() { // handle isoverdue operation
		return "Approved".equalsIgnoreCase(status) && dueDate != null && dueDate.isBefore(LocalDate.now()); // return computed result
	}
}
