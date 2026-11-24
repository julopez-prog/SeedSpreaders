package application;

public class Product {
	private String name;
	private double price;
	private double amount;
	private int lotID;
	private String location;
	private String variety;
	private String description;
	private String unit;
	private double minThreshold;
	private String expiryDate;

	public Product(String name, double price, double amount, int lotID, String location, String variety, String description) {
		this(name, price, amount, lotID, location, variety, description, "kg", 20.0, "2027-12-31");
	}

	public Product(String name, double price, double amount, int lotID, String location,
			String variety, String description, String unit, double minThreshold, String expiryDate) {
		this.name = name;
		this.price = price;
		this.amount = amount;
		this.lotID = lotID;
		this.location = location;
		this.variety = variety;
		this.description = description;
		this.unit = unit;
		this.minThreshold = minThreshold;
		this.expiryDate = expiryDate;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public int getLotID() {
		return lotID;
	}

	public void setLotID(int lotID) {
		this.lotID = lotID;
	}

	public String getLotCode() {
		return String.format("#%03d", lotID);
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getVariety() {
		return variety;
	}

	public void setVariety(String variety) {
		this.variety = variety;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public double getMinThreshold() {
		return minThreshold;
	}

	public void setMinThreshold(double minThreshold) {
		this.minThreshold = minThreshold;
	}

	public String getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(String expiryDate) {
		this.expiryDate = expiryDate;
	}

	public String getQuantityLabel() {
		return amount + " " + (unit == null || unit.isEmpty() ? "kg" : unit);
	}

	public String getStockStatus() {
		if (amount <= 0) {
			return "Out of Stock";
		} else if (amount <= minThreshold) {
			return "Low Stock";
		}
		return "Available";
	}
}
