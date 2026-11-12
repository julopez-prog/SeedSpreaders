package application;

public class SeedReturn {
	private int returnId;
	private int requestId;
	private String farmer;
	private int lotId;
	private String seedLabel;
	private double amount;
	private String status;

	public SeedReturn(int returnId, int requestId, String farmer, int lotId,
			String seedLabel, double amount, String status) {
		this.returnId = returnId;
		this.requestId = requestId;
		this.farmer = farmer;
		this.lotId = lotId;
		this.seedLabel = seedLabel;
		this.amount = amount;
		this.status = status;
	}

	public int getReturnId() {
		return returnId;
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

	public String getSeedLabel() {
		return seedLabel;
	}

	public double getAmount() {
		return amount;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
