package application;

import java.time.LocalDate;

public class SeedRequest {
	private int requestId;
	private String farmer;
	private String farmName;
	private int lotId;
	private String name;
	private String variety;
	private double amount;
	private double unitPrice;
	private String purpose;
	private String status;
	private LocalDate requestDate;
	private LocalDate dueDate;

	public SeedRequest(int requestId, String farmer, int lotId, String name, String variety,
			double amount, String purpose, String status) {
		this(requestId, farmer, farmer + " Farm", lotId, name, variety, amount, 0.0,
				purpose, status, LocalDate.now(), LocalDate.now().plusDays(90));
	}

	public SeedRequest(int requestId, String farmer, String farmName, int lotId, String name, String variety,
			double amount, double unitPrice, String purpose, String status, LocalDate requestDate, LocalDate dueDate) {
		this.requestId = requestId;
		this.farmer = farmer;
		this.farmName = farmName;
		this.lotId = lotId;
		this.name = name;
		this.variety = variety;
		this.amount = amount;
		this.unitPrice = unitPrice;
		this.purpose = purpose;
		this.status = status;
		this.requestDate = requestDate;
		this.dueDate = dueDate;
	}

	public int getRequestId() {
		return requestId;
	}

	public String getTransactionCode() {
		return String.format("TRS-%03d", requestId);
	}

	public String getFarmer() {
		return farmer;
	}

	public String getFarmName() {
		return farmName;
	}

	public void setFarmName(String farmName) {
		this.farmName = farmName;
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

	public String getQuantityLabel() {
		return amount + " kg";
	}

	public double getUnitPrice() {
		return unitPrice;
	}

	public double getTotalValue() {
		return Math.round(amount * unitPrice * 100.0) / 100.0;
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

	public LocalDate getRequestDate() {
		return requestDate;
	}

	public void setRequestDate(LocalDate requestDate) {
		this.requestDate = requestDate;
	}

	public LocalDate getDueDate() {
		return dueDate;
	}

	public void setDueDate(LocalDate dueDate) {
		this.dueDate = dueDate;
	}

	public boolean isOverdue() {
		return "Approved".equalsIgnoreCase(status) && dueDate != null && dueDate.isBefore(LocalDate.now());
	}
}
