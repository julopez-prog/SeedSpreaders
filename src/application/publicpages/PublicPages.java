package application.publicpages;

import application.Donation;
import application.User;
import application.shared.AppState;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class PublicPages {

	private final AppState state;
	private final Runnable onAdminLogin;
	private final Runnable onFarmerLogin;

	public PublicPages(AppState state, Runnable onAdminLogin, Runnable onFarmerLogin) {
		this.state = state;
		this.onAdminLogin = onAdminLogin;
		this.onFarmerLogin = onFarmerLogin;
	}

	// =========================================================================
	// LANDING PAGE (MATCHING SLIDE 1 - Screenshot 211722)
	// =========================================================================
	public void showLandingPage() {
		// Top White Navbar
		HBox navbar = new HBox(14);
		navbar.setAlignment(Pos.CENTER_LEFT);
		navbar.setPadding(new Insets(14, 36, 14, 36));
		navbar.setStyle("-fx-background-color: " + state.bgHeaderBar() + "; -fx-border-color: transparent transparent " + state.borderColor() + " transparent;");

		Node logo = state.buildLogo(0.95, false);

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		Button btnLogin = new Button("Login  →");
		btnLogin.setStyle(state.primaryButtonStyle() + "-fx-background-radius: 12; -fx-padding: 10 24 10 24;");
		btnLogin.setOnAction(e -> showLoginPage());

		navbar.getChildren().addAll(logo, spacer, btnLogin);

		// Hero Card (#163a2c rounded 20px)
		StackPane heroWrapper = new StackPane();
		heroWrapper.setPadding(new Insets(36, 64, 48, 64));
		heroWrapper.setStyle("-fx-background-color: " + state.bgPage() + ";");

		HBox heroCard = new HBox(30);
		heroCard.setAlignment(Pos.CENTER_LEFT);
		heroCard.setPadding(new Insets(56, 64, 56, 64));
		heroCard.setMaxWidth(1040);
		heroCard.setMaxHeight(540);
		heroCard.setStyle("-fx-background-color: " + state.HERO_CARD_BG + "; -fx-background-radius: 22;");

		// Left Hero Text
		VBox heroLeft = new VBox(18);
		heroLeft.setAlignment(Pos.CENTER_LEFT);
		HBox.setHgrow(heroLeft, Priority.ALWAYS);

		HBox line1 = new HBox(8);
		Label lblEvery = new Label(state.t("Every", "Bawat"));
		lblEvery.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 44px; -fx-font-weight: bold;");
		Label lblSeed = new Label(state.t("seed", "binhi"));
		lblSeed.setStyle("-fx-text-fill: " + state.SAGE_ACCENT + "; -fx-font-size: 44px; -fx-font-weight: bold;");
		Label lblHas = new Label(state.t("has", "ay may"));
		lblHas.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 44px; -fx-font-weight: bold;");
		line1.getChildren().addAll(lblEvery, lblSeed, lblHas);

		Label lblFuture = new Label(state.t("a future.", "kinabukasan."));
		lblFuture.setStyle("-fx-text-fill: " + state.LIME_ACCENT + "; -fx-font-size: 44px; -fx-font-weight: bold;");

		VBox headlineBox = new VBox(-6, line1, lblFuture);

		Label lblSub = new Label(state.t("Supporting Filipino farmers with access to quality\nseeds, so more farms can take root, grow stronger, and\ngive back.", "Tinutulungan ang mga magsasakang Pinoy na magkaroon ng de-kalidad na binhi para mas lumago ang ani at makapagbalik sa komunidad."));
		lblSub.setStyle("-fx-text-fill: #d8e4de; -fx-font-size: 14px; -fx-line-spacing: 3;");

		Button btnDonate = new Button(state.t("Donate", "Mag-donate"));
		btnDonate.setStyle("-fx-background-color: " + state.PRIMARY_GREEN + "; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 15px; -fx-background-radius: 14; -fx-padding: 14 48 14 48; -fx-cursor: hand;");
		btnDonate.setOnAction(e -> showDonationDialog());

		HBox ctaRow = new HBox(14, btnDonate);
		ctaRow.setPadding(new Insets(12, 0, 0, 0));

		heroLeft.getChildren().addAll(headlineBox, lblSub, ctaRow);

		// Right Large White S-Logo Graphic (Logo Image Placeholder)
		Node bigIcon = state.buildHeroLogoMark(265);

		heroCard.getChildren().addAll(heroLeft, bigIcon);
		heroWrapper.getChildren().add(heroCard);

		BorderPane root = new BorderPane();
		root.setTop(navbar);
		root.setCenter(heroWrapper);

		state.applyScene(new Scene(root, 1140, 740), "Seed Spreaders - Seed Bank System");
	}

	// =========================================================================
	// LOGIN PAGE (MATCHING SLIDE 2 - Screenshot 211753: Split Screen)
	// =========================================================================
	public void showLoginPage() {
		HBox split = new HBox();

		// Left Forest Green Panel
		VBox leftBrand = new VBox(20);
		leftBrand.setAlignment(Pos.CENTER);
		leftBrand.setPrefWidth(540);
		HBox.setHgrow(leftBrand, Priority.ALWAYS);
		leftBrand.setStyle("-fx-background-color: " + state.FOREST_DARK + ";");
		leftBrand.getChildren().add(state.buildLogo(1.75, true));

		// Right Form Panel
		VBox rightFormContainer = new VBox();
		rightFormContainer.setAlignment(Pos.CENTER);
		rightFormContainer.setPrefWidth(600);
		HBox.setHgrow(rightFormContainer, Priority.ALWAYS);
		rightFormContainer.setStyle("-fx-background-color: " + state.bgPage() + ";");

		VBox formBox = new VBox(14);
		formBox.setMaxWidth(380);
		formBox.setAlignment(Pos.CENTER_LEFT);

		Label lblTitle = new Label(state.t("Sign in", "Mag-log In"));
		lblTitle.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: " + state.textPrimary() + ";");

		Label lblUser = new Label(state.t("User ID / Username", "User ID o Username"));
		lblUser.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: " + state.textPrimary() + ";");
		TextField txtUsername = new TextField();
		txtUsername.setPromptText(state.t("Enter username or User ID", "Ilagay ang username o User ID"));
		txtUsername.setStyle(state.inputStyle());

		Label lblPass = new Label(state.t("Password", "Password"));
		lblPass.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: " + state.textPrimary() + ";");
		PasswordField txtPassword = new PasswordField();
		txtPassword.setPromptText("\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022");
		txtPassword.setStyle(state.inputStyle());

		Label lblWarn = new Label();
		lblWarn.setWrapText(true);
		lblWarn.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 12px; -fx-font-weight: bold;");

		Button btnSignIn = new Button(state.t("Sign in to dashboard  \u2192", "Pumasok sa Dashboard  \u2192"));
		btnSignIn.setMaxWidth(Double.MAX_VALUE);
		btnSignIn.setStyle(state.primaryButtonStyle() + "-fx-padding: 13 20 13 20; -fx-background-radius: 10;");

		btnSignIn.setOnAction(e -> {
			String idOrUser = txtUsername.getText().trim();
			String pass = txtPassword.getText();

			User found = null;
			for (User u : state.users) {
				boolean idMatch = u.getUsername().equalsIgnoreCase(idOrUser)
						|| u.getUserId().equalsIgnoreCase(idOrUser);
				if (idMatch && u.getPassword().equals(pass)) {
					found = u;
					break;
				}
			}

			if (found == null) {
				lblWarn.setText("Invalid username or password. Please try again.");
				return;
			}
			if (found.isSuspended()) {
				String reason = found.getSuspensionReason();
				if (reason == null || reason.isEmpty()) {
					reason = "Please contact a Seed Bank administrator.";
				}
				lblWarn.setText("Account suspended: " + reason);
				return;
			}

			state.currentUser = found;
			state.currentRoute = "Dashboard";
			if ("Admin".equalsIgnoreCase(found.getRole())) {
				onAdminLogin.run();
			} else {
				onFarmerLogin.run();
			}
		});

		HBox linksRow = new HBox(12);
		linksRow.setAlignment(Pos.CENTER_LEFT);
		Button btnBack = new Button("← Home");
		btnBack.setStyle(state.outlineButtonStyle());
		btnBack.setOnAction(e -> showLandingPage());
		linksRow.getChildren().add(btnBack);

		Label lblDemo = new Label("Demo: admin / admin123  |  juan / farmer123  |  maria / farmer123");
		lblDemo.setStyle("-fx-text-fill: " + state.textMuted() + "; -fx-font-size: 11px;");

		formBox.getChildren().addAll(lblTitle, lblUser, txtUsername,
				lblPass, txtPassword, lblWarn, btnSignIn, linksRow, lblDemo);
		rightFormContainer.getChildren().add(formBox);

		split.getChildren().addAll(leftBrand, rightFormContainer);
		state.applyScene(new Scene(split, 1140, 740), "Seed Spreaders - Sign In");
	}

	// =========================================================================
	// Legacy self-registration page (kept for compatibility; primary flow is admin-created accounts).
	// =========================================================================
	public void showSignUpPage() {
		HBox split = new HBox();

		VBox leftBrand = new VBox(16);
		leftBrand.setAlignment(Pos.CENTER);
		leftBrand.setPrefWidth(480);
		leftBrand.setStyle("-fx-background-color: " + state.FOREST_DARK + ";");
		Label lblNote = new Label(state.t("New farmer accounts start with limited\nview-only access until verified by an Admin.", "Limited access muna ang bagong farmer account hanggang ma-verify ng Admin."));
		lblNote.setStyle("-fx-text-fill: #c8e27c; -fx-font-size: 13px; -fx-text-alignment: center;");
		leftBrand.getChildren().addAll(state.buildLogo(1.55, true), lblNote);

		VBox rightContainer = new VBox();
		rightContainer.setAlignment(Pos.CENTER);
		HBox.setHgrow(rightContainer, Priority.ALWAYS);
		rightContainer.setStyle("-fx-background-color: " + state.bgPage() + ";");

		VBox formBox = new VBox(10);
		formBox.setMaxWidth(420);

		Label lblTitle = new Label(state.t("Farmer Registration", "Mag-sign Up Bilang Farmer"));
		lblTitle.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + state.textPrimary() + ";");

		TextField txtUsername = new TextField();
		txtUsername.setPromptText(state.t("Username *", "Username *"));
		txtUsername.setStyle(state.inputStyle());

		PasswordField txtPassword = new PasswordField();
		txtPassword.setPromptText(state.t("Password *", "Password *"));
		txtPassword.setStyle(state.inputStyle());

		TextField txtPhone = new TextField();
		txtPhone.setPromptText(state.t("Phone Number (e.g. 09171234567) *", "Cellphone Number (hal. 09171234567) *"));
		txtPhone.setStyle(state.inputStyle());

		TextField txtEmail = new TextField();
		txtEmail.setPromptText(state.t("Email Address *", "Email Address *"));
		txtEmail.setStyle(state.inputStyle());

		TextField txtFarmName = new TextField();
		txtFarmName.setPromptText(state.t("Farm Name (Optional)", "Pangalan ng Sakahan (Optional)"));
		txtFarmName.setStyle(state.inputStyle());

		TextField txtFarmAddress = new TextField();
		txtFarmAddress.setPromptText(state.t("Farm Address (Barangay, Municipality, Laguna) *", "Address ng Sakahan (Barangay, Bayan, Laguna) *"));
		txtFarmAddress.setStyle(state.inputStyle());

		Label lblStatus = new Label();
		lblStatus.setWrapText(true);
		lblStatus.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

		Button btnRegister = new Button(state.t("Create Account  \u2192", "Gumawa ng Account  \u2192"));
		btnRegister.setMaxWidth(Double.MAX_VALUE);
		btnRegister.setStyle(state.primaryButtonStyle());

		btnRegister.setOnAction(e -> {
			String uname = txtUsername.getText().trim();
			String pass = txtPassword.getText();
			String phone = txtPhone.getText().trim();
			String email = txtEmail.getText().trim();
			String farmAddr = txtFarmAddress.getText().trim();
			String farmName = txtFarmName.getText().trim().isEmpty() ? uname + " Farm" : txtFarmName.getText().trim();

			if (uname.isEmpty() || pass.isEmpty() || phone.isEmpty() || email.isEmpty() || farmAddr.isEmpty()) {
				lblStatus.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
				lblStatus.setText(state.t("Error: All required fields (*) must be filled.", "Oops! Paki-fill up lahat ng required fields (*)."));
				return;
			}
			if (!email.contains("@")) {
				lblStatus.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
				lblStatus.setText(state.t("Error: Enter a valid email address.", "Oops! Maglagay ng valid na email address."));
				return;
			}
			for (User existing : state.users) {
				if (existing.getUsername().equalsIgnoreCase(uname)) {
					lblStatus.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
					lblStatus.setText(state.t("Error: Username already taken.", "May gumagamit na ng username na 'yan."));
					return;
				}
			}

			String uid = String.format("FRM-%03d", state.nextUserId++);
			User newFarmer = new User(uid, uname, pass, "Farmer", phone, email,
					farmName, farmAddr, "Pending RSBSA", "1.0 ha", "Pending");
			state.users.add(newFarmer);
			state.saveUsers(state.userPath, state.users);

			lblStatus.setStyle("-fx-text-fill: " + state.PRIMARY_GREEN + "; -fx-font-weight: bold;");
			lblStatus.setText(state.t("Account created (ID: " + uid + ")! Status: Pending Admin Verification. You may sign in now with limited access.",
					"Nagawa na ang account (" + uid + ")! Status: Naghihintay ng beripikasyon ng Admin."));
			txtUsername.clear();
			txtPassword.clear();
			txtPhone.clear();
			txtEmail.clear();
			txtFarmName.clear();
			txtFarmAddress.clear();
		});

		Button btnBackLogin = new Button(state.t("\u2190 Back to Sign In", "\u2190 Balik sa Log In"));
		btnBackLogin.setStyle(state.outlineButtonStyle());
		btnBackLogin.setOnAction(e -> showLoginPage());

		formBox.getChildren().addAll(lblTitle, txtUsername, txtPassword, txtPhone, txtEmail,
				txtFarmName, txtFarmAddress, lblStatus, btnRegister, btnBackLogin);
		rightContainer.getChildren().add(formBox);

		split.getChildren().addAll(leftBrand, rightContainer);
		state.applyScene(new Scene(split, 1140, 740), "Seed Spreaders - Farmer Registration");
	}


	// =========================================================================
	// DONATION MODAL FROM LANDING PAGE
	// =========================================================================
	private void showDonationDialog() {
		Stage dialogStage = new Stage();
		dialogStage.initOwner(state.mainStage);
		dialogStage.initModality(Modality.APPLICATION_MODAL);
		dialogStage.setTitle(state.t("Donate to Seed Spreaders", "Mag-donate sa Seed Spreaders"));

		TextField textName = new TextField();
		textName.setPromptText(state.t("Full Name", "Buong Pangalan"));
		textName.setStyle(state.inputStyle());

		TextField textEmail = new TextField();
		textEmail.setPromptText("donor@example.com");
		textEmail.setStyle(state.inputStyle());

		TextField textAmount = new TextField();
		textAmount.setPromptText("e.g. 500.00");
		textAmount.setStyle(state.inputStyle());

		TextArea textMessage = new TextArea();
		textMessage.setPrefRowCount(3);
		textMessage.setPromptText(state.t("Message of support for Filipino farmers...", "Mensahe ng suporta para sa mga magsasakang Pinoy..."));

		Label lblWarn = new Label();
		lblWarn.setStyle("-fx-font-weight: bold;");

		Button btnDonate = new Button(state.t("Confirm Donation", "I-confirm ang Donasyon"));
		btnDonate.setStyle(state.primaryButtonStyle());
		btnDonate.setOnAction(e -> {
			String name = textName.getText().trim();
			String email = textEmail.getText().trim();
			String amountStr = textAmount.getText().trim();
			String message = textMessage.getText().trim();

			if (name.isEmpty() || email.isEmpty() || amountStr.isEmpty() || !email.contains("@")) {
				lblWarn.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
				lblWarn.setText(state.t("Error: Valid name, email, and amount are required.", "Paki-lagay ang tamang pangalan, email, at halaga."));
				return;
			}
			try {
				double amount = Double.parseDouble(amountStr);
				if (amount <= 0) {
					lblWarn.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
					lblWarn.setText("Error: Amount must be greater than 0.");
					return;
				}
				state.donationData.add(new Donation(name, email, state.round2(amount), message));
				state.saveDonations(state.donationPath, state.donationData);
				lblWarn.setStyle("-fx-text-fill: " + state.PRIMARY_GREEN + "; -fx-font-weight: bold;");
				lblWarn.setText(state.t("Thank you! Your donation was recorded.", "Maraming salamat! Naitala na ang donasyon mo."));
				textName.clear();
				textEmail.clear();
				textAmount.clear();
				textMessage.clear();
			} catch (NumberFormatException ex) {
				lblWarn.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
				lblWarn.setText("Error: Invalid amount.");
			}
		});

		Button btnClose = new Button(state.t("Close", "Isara"));
		btnClose.setStyle(state.outlineButtonStyle());
		btnClose.setOnAction(e -> dialogStage.close());

		HBox buttons = new HBox(10, btnClose, btnDonate);
		buttons.setAlignment(Pos.CENTER_RIGHT);

		VBox root = new VBox(12,
				state.wrapField(state.t("Donor Name", "Pangalan ng Donor"), textName),
				state.wrapField("Email", textEmail),
				state.wrapField(state.t("Amount (\u20b1)", "Halaga (\u20b1)"), textAmount),
				state.wrapField(state.t("Message", "Mensahe"), textMessage),
				lblWarn, buttons);
		root.setPadding(new Insets(22));
		root.setStyle("-fx-background-color: " + state.bgCard() + ";");

		dialogStage.setScene(new Scene(root, 460, 420));
		dialogStage.showAndWait();
	}
}
