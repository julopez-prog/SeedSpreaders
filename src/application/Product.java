package application; // declare package application

public class Product { // define product class
	private String name; // store name field
	private double price; // store price field
	private double amount; // store amount field
	private int lotID; // store lotid field
	private String location; // store location field
	private String variety; // store variety field
	private String description; // store description field
	private String unit; // store unit field
	private double minThreshold; // store minthreshold field
	private String expiryDate; // store expirydate field

	public Product(String name, double price, double amount, int lotID, String location, String variety, String description) { // initialize product instance
		this(name, price, amount, lotID, location, variety, description, "kg", 20.0, "2027-12-31"); // execute statement
	}

	public Product(String name, double price, double amount, int lotID, String location, // continue statement
			String variety, String description, String unit, double minThreshold, String expiryDate) { // open code block
		this.name = name; // assign name value
		this.price = price; // assign price value
		this.amount = amount; // assign amount value
		this.lotID = lotID; // assign lotid value
		this.location = location; // assign location value
		this.variety = variety; // assign variety value
		this.description = description; // assign description value
		this.unit = unit; // assign unit value
		this.minThreshold = minThreshold; // assign minthreshold value
		this.expiryDate = expiryDate; // assign expirydate value
	}

	public String getName() { // handle getname operation
		return name; // return name
	}

	public void setName(String name) { // handle setname operation
		this.name = name; // assign name value
	}

	public double getPrice() { // handle getprice operation
		return price; // return price
	}

	public void setPrice(double price) { // handle setprice operation
		this.price = price; // assign price value
	}

	public double getAmount() { // handle getamount operation
		return amount; // return amount
	}

	public void setAmount(double amount) { // handle setamount operation
		this.amount = amount; // assign amount value
	}

	public int getLotID() { // handle getlotid operation
		return lotID; // return lotid
	}

	public void setLotID(int lotID) { // handle setlotid operation
		this.lotID = lotID; // assign lotid value
	}

	public String getLotCode() { // handle getlotcode operation
		return String.format("#%03d", lotID); // return string.format("#%03d", lotid)
	}

	public String getLocation() { // handle getlocation operation
		return location; // return location
	}

	public void setLocation(String location) { // handle setlocation operation
		this.location = location; // assign location value
	}

	public String getVariety() { // handle getvariety operation
		return variety; // return variety
	}

	public void setVariety(String variety) { // handle setvariety operation
		this.variety = variety; // assign variety value
	}

	public String getDescription() { // handle getdescription operation
		return description; // return description
	}

	public void setDescription(String description) { // handle setdescription operation
		this.description = description; // assign description value
	}

	public String getUnit() { // handle getunit operation
		return unit; // return unit
	}

	public void setUnit(String unit) { // handle setunit operation
		this.unit = unit; // assign unit value
	}

	public double getMinThreshold() { // handle getminthreshold operation
		return minThreshold; // return minthreshold
	}

	public void setMinThreshold(double minThreshold) { // handle setminthreshold operation
		this.minThreshold = minThreshold; // assign minthreshold value
	}

	public String getExpiryDate() { // handle getexpirydate operation
		return expiryDate; // return expirydate
	}

	public void setExpiryDate(String expiryDate) { // handle setexpirydate operation
		this.expiryDate = expiryDate; // assign expirydate value
	}

	public String getQuantityLabel() { // handle getquantitylabel operation
		return amount + " " + (unit == null || unit.isEmpty() ? "kg" : unit); // return computed result
	}

	public String getStockStatus() { // handle getstockstatus operation
		if (amount <= 0) { // check condition before proceeding
			return "Out of Stock"; // return "out of stock"
		} else if (amount <= minThreshold) { // check fallback condition
			return "Low Stock"; // return "low stock"
		}
		return "Available"; // return "available"
	}
}
