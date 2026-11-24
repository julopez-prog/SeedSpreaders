package application;

/**
 * Represents a system user - either an Admin or a Farmer.
 * Admins create farmer accounts directly; new farmers are active (Verified)
 * right away. Suspended accounts store a reason for the suspension.
 */
public class User {
	private String userId;
	private String username;
	private String password;
	private String role; // "Admin" or "Farmer"
	private String phoneNumber;
	private String email;
	private String farmName;
	private String farmAddress;
	private String rsbsaNumber;
	private String farmArea;
	private String status; // "Verified", "Pending", "Suspended"
	private String suspensionReason;

	public User(String userId, String username, String password, String role,
			String phoneNumber, String email, String farmName, String farmAddress,
			String rsbsaNumber, String farmArea, String status) {
		this(userId, username, password, role, phoneNumber, email, farmName,
				farmAddress, rsbsaNumber, farmArea, status, "");
	}

	public User(String userId, String username, String password, String role,
			String phoneNumber, String email, String farmName, String farmAddress,
			String rsbsaNumber, String farmArea, String status, String suspensionReason) {
		this.userId = userId;
		this.username = username;
		this.password = password;
		this.role = role;
		this.phoneNumber = phoneNumber;
		this.email = email;
		this.farmName = farmName;
		this.farmAddress = farmAddress;
		this.rsbsaNumber = rsbsaNumber;
		this.farmArea = farmArea;
		this.status = status;
		this.suspensionReason = suspensionReason == null ? "" : suspensionReason;
	}

	public User(String username, String password, String role) {
		this("USR-000", username, password, role, "", "", username + " Farm", "",
				"N/A", "1.0 ha", "Verified", "");
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFarmName() {
		return farmName;
	}

	public void setFarmName(String farmName) {
		this.farmName = farmName;
	}

	public String getFarmAddress() {
		return farmAddress;
	}

	public void setFarmAddress(String farmAddress) {
		this.farmAddress = farmAddress;
	}

	public String getRsbsaNumber() {
		return rsbsaNumber;
	}

	public void setRsbsaNumber(String rsbsaNumber) {
		this.rsbsaNumber = rsbsaNumber;
	}

	public String getFarmArea() {
		return farmArea;
	}

	public void setFarmArea(String farmArea) {
		this.farmArea = farmArea;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getSuspensionReason() {
		return suspensionReason == null ? "" : suspensionReason;
	}

	public void setSuspensionReason(String suspensionReason) {
		this.suspensionReason = suspensionReason == null ? "" : suspensionReason;
	}

	public boolean isVerified() {
		return "Verified".equalsIgnoreCase(status);
	}

	public boolean isSuspended() {
		return "Suspended".equalsIgnoreCase(status);
	}
}
