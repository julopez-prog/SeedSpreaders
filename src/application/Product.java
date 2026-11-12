package application;

public class Product {
	private String name;
	private double price;
	private double amount;
	private int lotID;
	private String location;
	private String variety;
	private String description;

	public Product(String name, double price, double amount, int lotID, String location, String variety, String description) {
		this.name = name;
		this.price = price;
		this.amount = amount;
		this.lotID = lotID;
		this.location = location;
		this.variety = variety;
		this.description = description;
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
<<<<<<< HEAD
}
=======
}
>>>>>>> d7029ea0ad33fafcea9f150859c82f21e4d90bc4
