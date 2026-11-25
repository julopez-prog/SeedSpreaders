package application; // root application package

import java.time.LocalDate; // used for checking overdue return deadlines

// represents a farmer's seed loan request
public class SeedRequest { // declare seedrequest class
	private int requestId; // unique request id number
	private String farmer; // username of the requesting farmer
	private int lotId; // target seed lot id
	private String name; // requested crop name
	private String variety; // requested seed variety
	private double amount; // requested weight in kilograms
	private double totalValue; // php value of the borrowed seeds
	private String purpose; // farming or planting purpose
	private String status; // Pending, Approved, Rejected, or Returned
	private String requestDate; // date request was submitted (yyyy-mm-dd)
	private String dueDate; // harvest repayment due date (yyyy-mm-dd)

	// full constructor with value and date tracking
	public SeedRequest(int requestId, String farmer, int lotId, String name, String variety, // continue statement
			double amount, double totalValue, String purpose, String status, // declare parameter
			String requestDate, String dueDate) { // declare parameter
		this.requestId = requestId; // assign request id
		this.farmer = farmer; // assign farmer username
		this.lotId = lotId; // assign lot id
		this.name = name; // assign crop name
		this.variety = variety; // assign variety
		this.amount = amount; // assign requested weight
		this.totalValue = totalValue; // assign php loan value
		this.purpose = purpose; // assign planting purpose
		this.status = status; // assign request status
		this.requestDate = requestDate == null ? "" : requestDate; // assign request date
		this.dueDate = dueDate == null ? "" : dueDate; // assign due date
	}

	// backward-compatible constructor
	public SeedRequest(int requestId, String farmer, int lotId, String name, String variety, // continue statement
			double amount, String purpose, String status) { // declare parameter
		this(requestId, farmer, lotId, name, variety, amount, 0.0, purpose, status, LocalDate.now().toString(), ""); // delegate to full constructor
	}

	public int getRequestId() { // return request id
		return requestId; // integer request id
	}

	public String getFarmer() { // return farmer username
		return farmer; // farmer username
	}

	public int getLotId() { // return seed lot id
		return lotId; // lot id
	}

	public String getName() { // return crop name
		return name; // crop name
	}

	public String getVariety() { // return variety name
		return variety; // variety name
	}

	public String getSeedLabel() { // combined crop and variety label for tables
		return name + " - " + variety; // formatted seed label
	}

	public double getAmount() { // return requested weight in kg
		return amount; // weight in kg
	}

	public double getTotalValue() { // return php value of loan
		return totalValue; // php amount
	}

	public void setTotalValue(double totalValue) { // update php value of loan
		this.totalValue = totalValue; // store php amount
	}

	public String getPurpose() { // return planting purpose
		return purpose; // purpose string
	}

	public String getStatus() { // return current status
		return status; // Pending, Approved, Rejected, or Returned
	}

	public void setStatus(String status) { // update request status
		this.status = status; // store new status
	}

	public String getRequestDate() { // return date requested
		return requestDate; // yyyy-mm-dd string
	}

	public void setRequestDate(String requestDate) { // update date requested
		this.requestDate = requestDate; // store date string
	}

	public String getDueDate() { // return harvest repayment due date
		return dueDate; // yyyy-mm-dd string or empty
	}

	public void setDueDate(String dueDate) { // update repayment due date
		this.dueDate = dueDate; // store due date string
	}

	// check if this loan is still active (approved) and past its due date
	public boolean isOverdue() { // define isoverdue method
		if (!"Approved".equalsIgnoreCase(status) || dueDate == null || dueDate.isBlank()) { // only active approved loans with a due date can be overdue
			return false; // not overdue once returned or if not approved
		}
		try { // attempt risky block
			return LocalDate.parse(dueDate.trim()).isBefore(LocalDate.now()); // true if due date is earlier than today
		} catch (Exception e) { // handle exception
			return false; // ignore unparseable dates
		}
	}
}
