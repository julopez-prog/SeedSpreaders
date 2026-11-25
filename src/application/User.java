package application; // root application package

// represents a system user account (admin or farmer)
public class User { // declare user class
	private String userId; // unique user id like USR-001
	private String username; // login username
	private String password; // login password
	private String role; // Admin or Farmer
	private String fullName; // farmer or admin full name
	private String phone; // contact phone number
	private String email; // contact email address
	private String farmName; // registered farm name
	private String farmAddress; // farm barangay/town address
	private String rsbsaNumber; // rsbsa registration code
	private double farmAreaHa; // farm area in hectares
	private boolean verified; // true when verified (option 2 accounts default to true)
	private boolean suspended; // true when suspended by an admin
	private String suspensionReason; // reason shown if suspended

	// constructor used when creating or loading a user record
	public User(String userId, String username, String password, String role, // continue statement
			String fullName, String phone, String email, String farmName, // declare parameter
			String farmAddress, String rsbsaNumber, double farmAreaHa, // declare parameter
			boolean verified, boolean suspended, String suspensionReason) { // declare parameter
		this.userId = userId; // assign user id
		this.username = username; // assign username
		this.password = password; // assign password
		this.role = role; // assign role
		this.fullName = fullName; // assign full name
		this.phone = phone; // assign phone number
		this.email = email; // assign email address
		this.farmName = farmName; // assign farm name
		this.farmAddress = farmAddress; // assign farm address
		this.rsbsaNumber = rsbsaNumber; // assign rsbsa number
		this.farmAreaHa = farmAreaHa; // assign farm area
		this.verified = verified; // assign verified flag
		this.suspended = suspended; // assign suspended flag
		this.suspensionReason = suspensionReason == null ? "" : suspensionReason; // store suspension reason
	}

	// backward-compatible 3-arg constructor
	public User(String username, String password, String role) { // define user method
		this("USR-000", username, password, role, username, "", "", "", "", "", 0.0, true, false, ""); // delegate to main constructor
	}

	public String getUserId() { // return user id
		return userId; // user id value
	}

	public void setUserId(String userId) { // update user id
		this.userId = userId; // store new user id
	}

	public String getUsername() { // return username
		return username; // username string
	}

	public void setUsername(String username) { // update username
		this.username = username; // store new username
	}

	public String getPassword() { // return password
		return password; // password string
	}

	public void setPassword(String password) { // update password
		this.password = password; // store new password
	}

	public String getRole() { // return role
		return role; // Admin or Farmer
	}

	public void setRole(String role) { // update role
		this.role = role; // store new role
	}

	public String getFullName() { // return full name
		return fullName; // full name value
	}

	public void setFullName(String fullName) { // update full name
		this.fullName = fullName; // store full name
	}

	public String getPhone() { // return phone number
		return phone; // phone number string
	}

	public void setPhone(String phone) { // update phone number
		this.phone = phone; // store phone number
	}

	public String getEmail() { // return email address
		return email; // email string
	}

	public void setEmail(String email) { // update email address
		this.email = email; // store email address
	}

	public String getFarmName() { // return farm name
		return farmName; // farm name string
	}

	public void setFarmName(String farmName) { // update farm name
		this.farmName = farmName; // store farm name
	}

	public String getFarmAddress() { // return farm address
		return farmAddress; // farm address string
	}

	public void setFarmAddress(String farmAddress) { // update farm address
		this.farmAddress = farmAddress; // store farm address
	}

	public String getRsbsaNumber() { // return rsbsa code
		return rsbsaNumber; // rsbsa string
	}

	public void setRsbsaNumber(String rsbsaNumber) { // update rsbsa code
		this.rsbsaNumber = rsbsaNumber; // store rsbsa string
	}

	public double getFarmAreaHa() { // return farm area in hectares
		return farmAreaHa; // farm area value
	}

	public void setFarmAreaHa(double farmAreaHa) { // update farm area
		this.farmAreaHa = farmAreaHa; // store farm area
	}

	public boolean isVerified() { // check if account is verified
		return verified; // verified status
	}

	public void setVerified(boolean verified) { // update verified status
		this.verified = verified; // store verified flag
	}

	public boolean isSuspended() { // check if account is suspended
		return suspended; // suspended status
	}

	public void setSuspended(boolean suspended) { // update suspended status
		this.suspended = suspended; // store suspended flag
	}

	public String getSuspensionReason() { // get suspension reason text
		return suspensionReason; // reason string
	}

	public void setSuspensionReason(String suspensionReason) { // update suspension reason
		this.suspensionReason = suspensionReason == null ? "" : suspensionReason; // store non-null reason
	}

	public String getStatusLabel() { // helper label for table display
		if (suspended) { // suspended takes priority
			return "SUSPENDED"; // show suspended badge text
		}
		return verified ? "VERIFIED" : "PENDING"; // show verified or pending text
	}
}
