package application;

public class SeedRequest {
	private int requestId;
	private String farmer;
	private int lotId;
	private String name;
	private String variety;
	private double amount;
	private String purpose;
	private String status;

	public SeedRequest(int requestId, String farmer, int lotId, String name, String variety,
			double amount, String purpose, String status) {
		this.requestId = requestId;
		this.farmer = farmer;
		this.lotId = lotId;
		this.name = name;
		this.variety = variety;
		this.amount = amount;
		this.purpose = purpose;
		this.status = status;
	}

	public int getRequestId() {
		return requestId;
	}

	public String getFarmer() {
		return farmer;
	}

	public int getLotId() {
		return lotId;
	}

	public String getName() {
		return name;
	}

	public String getVariety() {
		return variety;
	}

	public String getSeedLabel() {
		return name + " - " + variety;
	}

	public double getAmount() {
		return amount;
	}

	public String getPurpose() {
		return purpose;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
