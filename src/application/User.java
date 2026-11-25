package application; // declare package application

/**
 * represents a system user - either an admin or a farmer.
 * admins create farmer accounts directly; new farmers are active (verified)
 * right away. suspended accounts store a reason for the suspension.
 */
public class User { // define user class
	private String userId; // store userid field
	private String username; // store username field
	private String password; // store password field
	private String role; // "admin" or "farmer"
	private String phoneNumber; // store phonenumber field
	private String email; // store email field
	private String farmName; // store farmname field
	private String farmAddress; // store farmaddress field
	private String rsbsaNumber; // store rsbsanumber field
	private String farmArea; // store farmarea field
	private String status; // "verified", "pending", "suspended"
	private String suspensionReason; // store suspensionreason field

	public User(String userId, String username, String password, String role, // continue statement
			String phoneNumber, String email, String farmName, String farmAddress, // continue statement
			String rsbsaNumber, String farmArea, String status) { // open code block
		this(userId, username, password, role, phoneNumber, email, farmName, // continue statement
				farmAddress, rsbsaNumber, farmArea, status, ""); // execute statement
	}

	public User(String userId, String username, String password, String role, // continue statement
			String phoneNumber, String email, String farmName, String farmAddress, // continue statement
			String rsbsaNumber, String farmArea, String status, String suspensionReason) { // open code block
		this.userId = userId; // assign userid value
		this.username = username; // assign username value
		this.password = password; // assign password value
		this.role = role; // assign role value
		this.phoneNumber = phoneNumber; // assign phonenumber value
		this.email = email; // assign email value
		this.farmName = farmName; // assign farmname value
		this.farmAddress = farmAddress; // assign farmaddress value
		this.rsbsaNumber = rsbsaNumber; // assign rsbsanumber value
		this.farmArea = farmArea; // assign farmarea value
		this.status = status; // assign status value
		this.suspensionReason = suspensionReason == null ? "" : suspensionReason; // assign suspensionreason value
	}

	public User(String username, String password, String role) { // initialize user instance
		this("USR-000", username, password, role, "", "", username + " Farm", "", // continue statement
				"N/A", "1.0 ha", "Verified", ""); // execute statement
	}

	public String getUserId() { // handle getuserid operation
		return userId; // return userid
	}

	public void setUserId(String userId) { // handle setuserid operation
		this.userId = userId; // assign userid value
	}

	public String getUsername() { // handle getusername operation
		return username; // return username
	}

	public void setUsername(String username) { // handle setusername operation
		this.username = username; // assign username value
	}

	public String getPassword() { // handle getpassword operation
		return password; // return password
	}

	public void setPassword(String password) { // handle setpassword operation
		this.password = password; // assign password value
	}

	public String getRole() { // handle getrole operation
		return role; // return role
	}

	public void setRole(String role) { // handle setrole operation
		this.role = role; // assign role value
	}

	public String getPhoneNumber() { // handle getphonenumber operation
		return phoneNumber; // return phonenumber
	}

	public void setPhoneNumber(String phoneNumber) { // handle setphonenumber operation
		this.phoneNumber = phoneNumber; // assign phonenumber value
	}

	public String getEmail() { // handle getemail operation
		return email; // return email
	}

	public void setEmail(String email) { // handle setemail operation
		this.email = email; // assign email value
	}

	public String getFarmName() { // handle getfarmname operation
		return farmName; // return farmname
	}

	public void setFarmName(String farmName) { // handle setfarmname operation
		this.farmName = farmName; // assign farmname value
	}

	public String getFarmAddress() { // handle getfarmaddress operation
		return farmAddress; // return farmaddress
	}

	public void setFarmAddress(String farmAddress) { // handle setfarmaddress operation
		this.farmAddress = farmAddress; // assign farmaddress value
	}

	public String getRsbsaNumber() { // handle getrsbsanumber operation
		return rsbsaNumber; // return rsbsanumber
	}

	public void setRsbsaNumber(String rsbsaNumber) { // handle setrsbsanumber operation
		this.rsbsaNumber = rsbsaNumber; // assign rsbsanumber value
	}

	public String getFarmArea() { // handle getfarmarea operation
		return farmArea; // return farmarea
	}

	public void setFarmArea(String farmArea) { // handle setfarmarea operation
		this.farmArea = farmArea; // assign farmarea value
	}

	public String getStatus() { // handle getstatus operation
		return status; // return status
	}

	public void setStatus(String status) { // handle setstatus operation
		this.status = status; // assign status value
	}

	public String getSuspensionReason() { // handle getsuspensionreason operation
		return suspensionReason == null ? "" : suspensionReason; // return computed result
	}

	public void setSuspensionReason(String suspensionReason) { // handle setsuspensionreason operation
		this.suspensionReason = suspensionReason == null ? "" : suspensionReason; // assign suspensionreason value
	}

	public boolean isVerified() { // handle isverified operation
		return "Verified".equalsIgnoreCase(status); // return "verified".equalsignorecase(status)
	}

	public boolean isSuspended() { // handle issuspended operation
		return "Suspended".equalsIgnoreCase(status); // return computed result
	}
}
