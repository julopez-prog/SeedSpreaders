package application; // root application package

// represents a seed inventory lot stored in the seed bank
public class Product { // declare product class
	private String name; // crop name like Okra Seeds or Rice Seeds
	private double price; // monetary value per kilogram in php
	private double amount; // available stock in kilograms
	private int lotID; // unique lot number
	private String location; // storage bin or warehouse bay
	private String variety; // botanical or local variety name
	private String description; // notes on crop yield and origin

	// constructor for a seed product lot
	public Product(String name, double price, double amount, int lotID, String location, String variety, String description) { // define product method
		this.name = name; // assign crop name
		this.price = price; // assign unit price per kg
		this.amount = amount; // assign stock quantity
		this.lotID = lotID; // assign lot id
		this.location = location; // assign storage location
		this.variety = variety; // assign seed variety
		this.description = description; // assign crop description
	}

	public String getName() { // return crop name
		return name; // crop name string
	}

	public void setName(String name) { // update crop name
		this.name = name; // store crop name
	}

	public double getPrice() { // return price per kg
		return price; // price value
	}

	public void setPrice(double price) { // update price per kg
		this.price = price; // store price value
	}

	public double getAmount() { // return available stock in kg
		return amount; // stock weight
	}

	public void setAmount(double amount) { // update available stock
		this.amount = amount; // store new stock weight
	}

	public int getLotID() { // return lot id
		return lotID; // integer lot id
	}

	public void setLotID(int lotID) { // update lot id
		this.lotID = lotID; // store new lot id
	}

	public String getLocation() { // return storage location
		return location; // storage location string
	}

	public void setLocation(String location) { // update storage location
		this.location = location; // store storage location
	}

	public String getVariety() { // return variety name
		return variety; // variety string
	}

	public void setVariety(String variety) { // update variety name
		this.variety = variety; // store variety string
	}

	public String getDescription() { // return description
		return description; // description text
	}

	public void setDescription(String description) { // update description
		this.description = description; // store description text
	}

	public double getTotalValue() { // compute total php value of this lot
		return Math.round(price * amount * 100.0) / 100.0; // price times weight rounded to 2 decimals
	}
}
