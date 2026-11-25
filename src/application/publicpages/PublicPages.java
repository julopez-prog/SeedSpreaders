package application.publicpages; // declare package application.publicpages

import application.Donation; // import donation class
import application.User; // import user class
import application.shared.AppState; // import appstate class
import javafx.geometry.Insets; // import insets class
import javafx.geometry.Pos; // import pos class
import javafx.scene.Node; // import node class
import javafx.scene.Scene; // import scene class
import javafx.scene.control.Button; // import button class
import javafx.scene.control.Label; // import label class
import javafx.scene.control.PasswordField; // import passwordfield class
import javafx.scene.control.ScrollPane; // import scrollpane class
import javafx.scene.control.TextArea; // import textarea class
import javafx.scene.control.TextField; // import textfield class
import javafx.scene.layout.BorderPane; // import borderpane class
import javafx.scene.layout.HBox; // import hbox class
import javafx.scene.layout.Priority; // import priority class
import javafx.scene.layout.Region; // import region class
import javafx.scene.layout.StackPane; // import stackpane class
import javafx.scene.layout.VBox; // import vbox class
import javafx.scene.text.Font; // import font class
import javafx.scene.text.FontWeight; // import fontweight class
import javafx.stage.Modality; // import modality class
import javafx.stage.Stage; // import stage class

public class PublicPages { // define publicpages class

	private final AppState state; // store state field
	private final Runnable onAdminLogin; // store onadminlogin field
	private final Runnable onFarmerLogin; // store onfarmerlogin field

	public PublicPages(AppState state, Runnable onAdminLogin, Runnable onFarmerLogin) { // initialize publicpages instance
		this.state = state; // assign state value
		this.onAdminLogin = onAdminLogin; // assign onadminlogin value
		this.onFarmerLogin = onFarmerLogin; // assign onfarmerlogin value
	}

	// landing page
	public void showLandingPage() { // handle showlandingpage operation
		// top white navbar
		HBox navbar = new HBox(14); // execute statement
		navbar.setAlignment(Pos.CENTER_LEFT); // set container alignment
		navbar.setPadding(new Insets(14, 36, 14, 36)); // set inner padding spacing
		navbar.setStyle("-fx-background-color: " + state.bgHeaderBar() + "; -fx-border-color: transparent transparent " + state.borderColor() + " transparent;"); // apply inline css styling

		Node logo = state.buildLogo(0.95, false); // execute statement

		Region spacer = new Region(); // execute statement
		HBox.setHgrow(spacer, Priority.ALWAYS); // allow horizontal growth inside hbox

		Button btnLogin = new Button("Login  →"); // execute statement
		btnLogin.setStyle(state.primaryButtonStyle() + "-fx-background-radius: 12; -fx-padding: 10 24 10 24;"); // apply inline css styling
		btnLogin.setOnAction(e -> showLoginPage()); // attach button click event handler

		navbar.getChildren().addAll(logo, spacer, btnLogin); // add child nodes to container

		// hero card (#163a2c rounded 20px)
		StackPane heroWrapper = new StackPane(); // execute statement
		heroWrapper.setPadding(new Insets(36, 64, 48, 64)); // set inner padding spacing
		heroWrapper.setStyle("-fx-background-color: " + state.bgPage() + ";"); // apply inline css styling

		HBox heroCard = new HBox(30); // execute statement
		heroCard.setAlignment(Pos.CENTER_LEFT); // set container alignment
		heroCard.setPadding(new Insets(56, 64, 56, 64)); // set inner padding spacing
		heroCard.setMaxWidth(1040); // configure node width constraint
		heroCard.setMaxHeight(540); // configure node height constraint
		heroCard.setStyle("-fx-background-color: " + state.HERO_CARD_BG + "; -fx-background-radius: 22;"); // apply inline css styling

		// left hero text
		VBox heroLeft = new VBox(18); // execute statement
		heroLeft.setAlignment(Pos.CENTER_LEFT); // set container alignment
		HBox.setHgrow(heroLeft, Priority.ALWAYS); // allow horizontal growth inside hbox

		HBox line1 = new HBox(8); // execute statement
		Label lblEvery = new Label(state.t("Every", "Bawat")); // execute statement
		lblEvery.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 44px; -fx-font-weight: bold;"); // apply inline css styling
		Label lblSeed = new Label(state.t("seed", "binhi")); // execute statement
		lblSeed.setStyle("-fx-text-fill: " + state.SAGE_ACCENT + "; -fx-font-size: 44px; -fx-font-weight: bold;"); // apply inline css styling
		Label lblHas = new Label(state.t("has", "ay may")); // execute statement
		lblHas.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 44px; -fx-font-weight: bold;"); // apply inline css styling
		line1.getChildren().addAll(lblEvery, lblSeed, lblHas); // add child nodes to container

		Label lblFuture = new Label(state.t("a future.", "kinabukasan.")); // execute statement
		lblFuture.setStyle("-fx-text-fill: " + state.LIME_ACCENT + "; -fx-font-size: 44px; -fx-font-weight: bold;"); // apply inline css styling

		VBox headlineBox = new VBox(-6, line1, lblFuture); // execute statement

		Label lblSub = new Label(state.t("Supporting Filipino farmers with access to quality\nseeds, so more farms can take root, grow stronger, and\ngive back.", "Tinutulungan ang mga magsasakang Pinoy na magkaroon ng de-kalidad na binhi para mas lumago ang ani at makapagbalik sa komunidad.")); // execute statement
		lblSub.setStyle("-fx-text-fill: #d8e4de; -fx-font-size: 14px; -fx-line-spacing: 3;"); // apply inline css styling

		Button btnDonate = new Button(state.t("Donate", "Mag-donate")); // execute statement
		btnDonate.setStyle("-fx-background-color: " + state.PRIMARY_GREEN + "; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 15px; -fx-background-radius: 14; -fx-padding: 14 48 14 48; -fx-cursor: hand;"); // apply inline css styling
		btnDonate.setOnAction(e -> showDonationDialog()); // attach button click event handler

		HBox ctaRow = new HBox(14, btnDonate); // execute statement
		ctaRow.setPadding(new Insets(12, 0, 0, 0)); // set inner padding spacing

		heroLeft.getChildren().addAll(headlineBox, lblSub, ctaRow); // add child nodes to container

		// right large white s-logo graphic (brand logo graphic)
		Node bigIcon = state.buildHeroLogoMark(265); // execute statement

		heroCard.getChildren().addAll(heroLeft, bigIcon); // add child nodes to container
		heroWrapper.getChildren().add(heroCard); // append child node to container

		BorderPane root = new BorderPane(); // execute statement
		root.setTop(navbar); // execute statement
		root.setCenter(heroWrapper); // execute statement

		state.applyScene(new Scene(root, 1140, 740), "Seed Spreaders - Seed Bank System"); // execute statement
	}

	// login page
	public void showLoginPage() { // handle showloginpage operation
		HBox split = new HBox(); // execute statement

		// left forest green panel
		VBox leftBrand = new VBox(20); // container for the brand logo on the login screen
		leftBrand.setAlignment(Pos.CENTER); // center logo vertically and horizontally inside the panel
		leftBrand.setPadding(new Insets(24, 24, 24, 72)); // add left padding so the logo sits further right without clipping
		leftBrand.setPrefWidth(540); // set preferred width of the left panel
		HBox.setHgrow(leftBrand, Priority.ALWAYS); // allow left panel to expand evenly with the right panel
		leftBrand.setStyle("-fx-background-color: " + state.FOREST_DARK + ";"); // apply dark forest green background
		Node loginLogo = state.buildLogo(1.75, true); // build the white brand logo lockup
		VBox.setMargin(loginLogo, new Insets(0, 0, 0, 24)); // shift the logo slightly further to the right
		leftBrand.getChildren().add(loginLogo); // attach the logo lockup to the left panel

		// right form panel
		VBox rightFormContainer = new VBox(); // execute statement
		rightFormContainer.setAlignment(Pos.CENTER); // set container alignment
		rightFormContainer.setPrefWidth(600); // configure node width constraint
		HBox.setHgrow(rightFormContainer, Priority.ALWAYS); // allow horizontal growth inside hbox
		rightFormContainer.setStyle("-fx-background-color: " + state.bgPage() + ";"); // apply inline css styling

		VBox formBox = new VBox(14); // execute statement
		formBox.setMaxWidth(380); // configure node width constraint
		formBox.setAlignment(Pos.CENTER_LEFT); // set container alignment

		Label lblTitle = new Label(state.t("Sign in", "Mag-log In")); // execute statement
		lblTitle.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: " + state.textPrimary() + ";"); // apply inline css styling

		Label lblUser = new Label(state.t("User ID / Username", "User ID o Username")); // execute statement
		lblUser.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: " + state.textPrimary() + ";"); // apply inline css styling
		TextField txtUsername = new TextField(); // execute statement
		txtUsername.setPromptText(state.t("Enter username or User ID", "Ilagay ang username o User ID")); // set input placeholder text
		txtUsername.setStyle(state.inputStyle()); // apply inline css styling

		Label lblPass = new Label(state.t("Password", "Password")); // execute statement
		lblPass.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: " + state.textPrimary() + ";"); // apply inline css styling
		PasswordField txtPassword = new PasswordField(); // execute statement
		txtPassword.setPromptText("\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022"); // set input placeholder text
		txtPassword.setStyle(state.inputStyle()); // apply inline css styling

		Label lblWarn = new Label(); // execute statement
		lblWarn.setWrapText(true); // enable text wrapping
		lblWarn.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 12px; -fx-font-weight: bold;"); // apply inline css styling

		Button btnSignIn = new Button(state.t("Sign in to dashboard  \u2192", "Pumasok sa Dashboard  \u2192")); // execute statement
		btnSignIn.setMaxWidth(Double.MAX_VALUE); // configure node width constraint
		btnSignIn.setStyle(state.primaryButtonStyle() + "-fx-padding: 13 20 13 20; -fx-background-radius: 10;"); // apply inline css styling

		btnSignIn.setOnAction(e -> { // attach button click event handler
			String idOrUser = txtUsername.getText().trim(); // execute statement
			String pass = txtPassword.getText(); // execute statement

			User found = null; // execute statement
			for (User u : state.users) { // iterate through collection items
				boolean idMatch = u.getUsername().equalsIgnoreCase(idOrUser) // continue statement
						|| u.getUserId().equalsIgnoreCase(idOrUser); // execute statement
				if (idMatch && u.getPassword().equals(pass)) { // check condition before proceeding
					found = u; // execute statement
					break; // exit current loop
				}
			}

			if (found == null) { // check condition before proceeding
				lblWarn.setText("Invalid username or password. Please try again."); // update displayed label text
				return; // execute statement
			}
			if (found.isSuspended()) { // check condition before proceeding
				String reason = found.getSuspensionReason(); // execute statement
				if (reason == null || reason.isEmpty()) { // check condition before proceeding
					reason = "Please contact a Seed Bank administrator."; // execute statement
				}
				lblWarn.setText("Account suspended: " + reason); // update displayed label text
				return; // execute statement
			}

			state.currentUser = found; // execute statement
			state.currentRoute = "Dashboard"; // execute statement
			if ("Admin".equalsIgnoreCase(found.getRole())) { // check condition before proceeding
				onAdminLogin.run(); // execute statement
			} else { // handle alternative branch
				onFarmerLogin.run(); // execute statement
			}
		}); // execute statement

		HBox linksRow = new HBox(12); // execute statement
		linksRow.setAlignment(Pos.CENTER_LEFT); // set container alignment
		Button btnBack = new Button("← Home"); // execute statement
		btnBack.setStyle(state.outlineButtonStyle()); // apply inline css styling
		btnBack.setOnAction(e -> showLandingPage()); // attach button click event handler
		linksRow.getChildren().add(btnBack); // append child node to container

		formBox.getChildren().addAll(lblTitle, lblUser, txtUsername, // add form heading and username controls
				lblPass, txtPassword, lblWarn, btnSignIn, linksRow); // add password, warning, submit button, and back link without demo text
		rightFormContainer.getChildren().add(formBox); // append child node to container

		split.getChildren().addAll(leftBrand, rightFormContainer); // add child nodes to container
		state.applyScene(new Scene(split, 1140, 740), "Seed Spreaders - Sign In"); // execute statement
	}

	// legacy self-registration page (kept for compatibility; primary flow is admin-created accounts).
	public void showSignUpPage() { // handle showsignuppage operation
		HBox split = new HBox(); // execute statement

		VBox leftBrand = new VBox(16); // execute statement
		leftBrand.setAlignment(Pos.CENTER); // set container alignment
		leftBrand.setPrefWidth(480); // configure node width constraint
		leftBrand.setStyle("-fx-background-color: " + state.FOREST_DARK + ";"); // apply inline css styling
		Label lblNote = new Label(state.t("New farmer accounts start with limited\nview-only access until verified by an Admin.", "Limited access muna ang bagong farmer account hanggang ma-verify ng Admin.")); // execute statement
		lblNote.setStyle("-fx-text-fill: #c8e27c; -fx-font-size: 13px; -fx-text-alignment: center;"); // apply inline css styling
		leftBrand.getChildren().addAll(state.buildLogo(1.55, true), lblNote); // add child nodes to container

		VBox rightContainer = new VBox(); // execute statement
		rightContainer.setAlignment(Pos.CENTER); // set container alignment
		HBox.setHgrow(rightContainer, Priority.ALWAYS); // allow horizontal growth inside hbox
		rightContainer.setStyle("-fx-background-color: " + state.bgPage() + ";"); // apply inline css styling

		VBox formBox = new VBox(10); // execute statement
		formBox.setMaxWidth(420); // configure node width constraint

		Label lblTitle = new Label(state.t("Farmer Registration", "Mag-sign Up Bilang Farmer")); // execute statement
		lblTitle.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + state.textPrimary() + ";"); // apply inline css styling

		TextField txtUsername = new TextField(); // execute statement
		txtUsername.setPromptText(state.t("Username *", "Username *")); // set input placeholder text
		txtUsername.setStyle(state.inputStyle()); // apply inline css styling

		PasswordField txtPassword = new PasswordField(); // execute statement
		txtPassword.setPromptText(state.t("Password *", "Password *")); // set input placeholder text
		txtPassword.setStyle(state.inputStyle()); // apply inline css styling

		TextField txtPhone = new TextField(); // execute statement
		txtPhone.setPromptText(state.t("Phone Number (e.g. 09171234567) *", "Cellphone Number (hal. 09171234567) *")); // set input placeholder text
		txtPhone.setStyle(state.inputStyle()); // apply inline css styling

		TextField txtEmail = new TextField(); // execute statement
		txtEmail.setPromptText(state.t("Email Address *", "Email Address *")); // set input placeholder text
		txtEmail.setStyle(state.inputStyle()); // apply inline css styling

		TextField txtFarmName = new TextField(); // execute statement
		txtFarmName.setPromptText(state.t("Farm Name (Optional)", "Pangalan ng Sakahan (Optional)")); // set input placeholder text
		txtFarmName.setStyle(state.inputStyle()); // apply inline css styling

		TextField txtFarmAddress = new TextField(); // execute statement
		txtFarmAddress.setPromptText(state.t("Farm Address (Barangay, Municipality, Laguna) *", "Address ng Sakahan (Barangay, Bayan, Laguna) *")); // set input placeholder text
		txtFarmAddress.setStyle(state.inputStyle()); // apply inline css styling

		Label lblStatus = new Label(); // execute statement
		lblStatus.setWrapText(true); // enable text wrapping
		lblStatus.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;"); // apply inline css styling

		Button btnRegister = new Button(state.t("Create Account  \u2192", "Gumawa ng Account  \u2192")); // execute statement
		btnRegister.setMaxWidth(Double.MAX_VALUE); // configure node width constraint
		btnRegister.setStyle(state.primaryButtonStyle()); // apply inline css styling

		btnRegister.setOnAction(e -> { // attach button click event handler
			String uname = txtUsername.getText().trim(); // execute statement
			String pass = txtPassword.getText(); // execute statement
			String phone = txtPhone.getText().trim(); // execute statement
			String email = txtEmail.getText().trim(); // execute statement
			String farmAddr = txtFarmAddress.getText().trim(); // execute statement
			String farmName = txtFarmName.getText().trim().isEmpty() ? uname + " Farm" : txtFarmName.getText().trim(); // execute statement

			if (uname.isEmpty() || pass.isEmpty() || phone.isEmpty() || email.isEmpty() || farmAddr.isEmpty()) { // check condition before proceeding
				lblStatus.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;"); // apply inline css styling
				lblStatus.setText(state.t("Error: All required fields (*) must be filled.", "Oops! Paki-fill up lahat ng required fields (*).")); // update displayed label text
				return; // execute statement
			}
			if (!email.contains("@")) { // check condition before proceeding
				lblStatus.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;"); // apply inline css styling
				lblStatus.setText(state.t("Error: Enter a valid email address.", "Oops! Maglagay ng valid na email address.")); // update displayed label text
				return; // execute statement
			}
			for (User existing : state.users) { // iterate through collection items
				if (existing.getUsername().equalsIgnoreCase(uname)) { // check condition before proceeding
					lblStatus.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;"); // apply inline css styling
					lblStatus.setText(state.t("Error: Username already taken.", "May gumagamit na ng username na 'yan.")); // update displayed label text
					return; // execute statement
				}
			}

			String uid = String.format("FRM-%03d", state.nextUserId++); // execute statement
			User newFarmer = new User(uid, uname, pass, "Farmer", phone, email, // continue statement
					farmName, farmAddr, "Pending RSBSA", "1.0 ha", "Pending"); // execute statement
			state.users.add(newFarmer); // execute statement
			state.saveUsers(state.userPath, state.users); // execute statement

			lblStatus.setStyle("-fx-text-fill: " + state.PRIMARY_GREEN + "; -fx-font-weight: bold;"); // apply inline css styling
			lblStatus.setText(state.t("Account created (ID: " + uid + ")! Status: Pending Admin Verification. You may sign in now with limited access.", // update displayed label text
					"Nagawa na ang account (" + uid + ")! Status: Naghihintay ng beripikasyon ng Admin.")); // execute statement
			txtUsername.clear(); // execute statement
			txtPassword.clear(); // execute statement
			txtPhone.clear(); // execute statement
			txtEmail.clear(); // execute statement
			txtFarmName.clear(); // execute statement
			txtFarmAddress.clear(); // execute statement
		}); // execute statement

		Button btnBackLogin = new Button(state.t("\u2190 Back to Sign In", "\u2190 Balik sa Log In")); // execute statement
		btnBackLogin.setStyle(state.outlineButtonStyle()); // apply inline css styling
		btnBackLogin.setOnAction(e -> showLoginPage()); // attach button click event handler

		formBox.getChildren().addAll(lblTitle, txtUsername, txtPassword, txtPhone, txtEmail, // add child nodes to container
				txtFarmName, txtFarmAddress, lblStatus, btnRegister, btnBackLogin); // execute statement
		rightContainer.getChildren().add(formBox); // append child node to container

		split.getChildren().addAll(leftBrand, rightContainer); // add child nodes to container
		state.applyScene(new Scene(split, 1140, 740), "Seed Spreaders - Farmer Registration"); // execute statement
	}


	// donation modal from landing page
	private void showDonationDialog() { // handle showdonationdialog operation
		Stage dialogStage = new Stage(); // execute statement
		dialogStage.initOwner(state.mainStage); // execute statement
		dialogStage.initModality(Modality.APPLICATION_MODAL); // execute statement
		dialogStage.setTitle(state.t("Donate to Seed Spreaders", "Mag-donate sa Seed Spreaders")); // execute statement

		TextField textName = new TextField(); // execute statement
		textName.setPromptText(state.t("Full Name", "Buong Pangalan")); // set input placeholder text
		textName.setStyle(state.inputStyle()); // apply inline css styling

		TextField textEmail = new TextField(); // execute statement
		textEmail.setPromptText("donor@example.com"); // set input placeholder text
		textEmail.setStyle(state.inputStyle()); // apply inline css styling

		TextField textAmount = new TextField(); // execute statement
		textAmount.setPromptText("e.g. 500.00"); // set input placeholder text
		textAmount.setStyle(state.inputStyle()); // apply inline css styling

		TextArea textMessage = new TextArea(); // execute statement
		textMessage.setPrefRowCount(3); // execute statement
		textMessage.setPromptText(state.t("Message of support for Filipino farmers...", "Mensahe ng suporta para sa mga magsasakang Pinoy...")); // set input placeholder text

		Label lblWarn = new Label(); // execute statement
		lblWarn.setStyle("-fx-font-weight: bold;"); // apply inline css styling

		Button btnDonate = new Button(state.t("Confirm Donation", "I-confirm ang Donasyon")); // execute statement
		btnDonate.setStyle(state.primaryButtonStyle()); // apply inline css styling
		btnDonate.setOnAction(e -> { // attach button click event handler
			String name = textName.getText().trim(); // execute statement
			String email = textEmail.getText().trim(); // execute statement
			String amountStr = textAmount.getText().trim(); // execute statement
			String message = textMessage.getText().trim(); // execute statement

			if (name.isEmpty() || email.isEmpty() || amountStr.isEmpty() || !email.contains("@")) { // check condition before proceeding
				lblWarn.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;"); // apply inline css styling
				lblWarn.setText(state.t("Error: Valid name, email, and amount are required.", "Paki-lagay ang tamang pangalan, email, at halaga.")); // update displayed label text
				return; // execute statement
			}
			try { // start protected execution block
				double amount = Double.parseDouble(amountStr); // execute statement
				if (amount <= 0) { // check condition before proceeding
					lblWarn.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;"); // apply inline css styling
					lblWarn.setText("Error: Amount must be greater than 0."); // update displayed label text
					return; // execute statement
				}
				state.donationData.add(new Donation(name, email, state.round2(amount), message)); // execute statement
				state.saveDonations(state.donationPath, state.donationData); // execute statement
				lblWarn.setStyle("-fx-text-fill: " + state.PRIMARY_GREEN + "; -fx-font-weight: bold;"); // apply inline css styling
				lblWarn.setText(state.t("Thank you! Your donation was recorded.", "Maraming salamat! Naitala na ang donasyon mo.")); // update displayed label text
				textName.clear(); // execute statement
				textEmail.clear(); // execute statement
				textAmount.clear(); // execute statement
				textMessage.clear(); // execute statement
			} catch (NumberFormatException ex) { // handle caught exception
				lblWarn.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;"); // apply inline css styling
				lblWarn.setText("Error: Invalid amount."); // update displayed label text
			}
		}); // execute statement

		Button btnClose = new Button(state.t("Close", "Isara")); // execute statement
		btnClose.setStyle(state.outlineButtonStyle()); // apply inline css styling
		btnClose.setOnAction(e -> dialogStage.close()); // attach button click event handler

		HBox buttons = new HBox(10, btnClose, btnDonate); // execute statement
		buttons.setAlignment(Pos.CENTER_RIGHT); // set container alignment

		VBox root = new VBox(12, // continue statement
				state.wrapField(state.t("Donor Name", "Pangalan ng Donor"), textName), // continue statement
				state.wrapField("Email", textEmail), // continue statement
				state.wrapField(state.t("Amount (\u20b1)", "Halaga (\u20b1)"), textAmount), // continue statement
				state.wrapField(state.t("Message", "Mensahe"), textMessage), // continue statement
				lblWarn, buttons); // execute statement
		root.setPadding(new Insets(22)); // set inner padding spacing
		root.setStyle("-fx-background-color: " + state.bgCard() + ";"); // apply inline css styling

		dialogStage.setScene(new Scene(root, 460, 420)); // execute statement
		dialogStage.showAndWait(); // execute statement
	}
}
