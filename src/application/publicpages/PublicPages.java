package application.publicpages; // public landing, login, and donation views

import application.Donation; // donation model
import application.User; // user model
import application.shared.AppState; // shared state and assets
import javafx.geometry.Insets; // layout padding
import javafx.geometry.Pos; // container alignment
import javafx.scene.Scene; // scene wrapper
import javafx.scene.control.Button; // button control
import javafx.scene.control.Label; // label control
import javafx.scene.control.PasswordField; // password input
import javafx.scene.control.Separator; // divider line
import javafx.scene.control.TextField; // text input
import javafx.scene.control.ToggleButton; // preset amount chip
import javafx.scene.control.ToggleGroup; // toggle group for preset chips
import javafx.scene.image.Image; // image container
import javafx.scene.image.ImageView; // image view node
import javafx.scene.layout.BorderPane; // root page layout
import javafx.scene.layout.HBox; // horizontal layout container
import javafx.scene.layout.Pane; // background shape layer
import javafx.scene.layout.Priority; // flex growth priority
import javafx.scene.layout.Region; // flexible spacer
import javafx.scene.layout.StackPane; // layered container
import javafx.scene.layout.VBox; // vertical layout container
import javafx.scene.paint.Color; // color definitions
import javafx.scene.shape.Circle; // decorative background circle
import javafx.scene.shape.Rectangle; // rounded clip mask
import javafx.scene.text.Text; // styled text span
import javafx.scene.text.TextFlow; // multi-color hero heading
import javafx.stage.Modality; // modal window mode
import javafx.stage.Stage; // stage window
import javafx.stage.StageStyle; // transparent modal window style

import java.net.URL; // resource url lookup
import java.util.function.Consumer; // login success callback

// handles the public landing screen, sign-in screen, and donation dialog
public class PublicPages { // declare publicpages class
	private final Stage mainStage; // primary window reference
	private final AppState state; // shared application state
	private final Consumer<User> onAuthenticated; // callback invoked after successful login

	// constructor for public screens controller
	public PublicPages(Stage stage, AppState state, Consumer<User> onAuthenticated) { // define publicpages method
		this.mainStage = stage; // store primary stage
		this.state = state; // store shared state
		this.onAuthenticated = onAuthenticated; // store login callback
	}

	// load a logo image from classpath and configure smooth scaling
	private ImageView loadLogo(String resourcePath, double size) { // define loadlogo helper
		URL resource = PublicPages.class.getResource(resourcePath); // find image resource
		ImageView view = new ImageView(); // create image view wrapper
		view.setFitWidth(size); // set target width
		view.setFitHeight(size); // set target height
		view.setPreserveRatio(true); // keep original proportions
		view.setSmooth(true); // smooth image rendering

		if (resource != null) { // apply image if resource exists
			view.setImage(new Image(resource.toExternalForm())); // load png graphic
		} else { // fallback branch
			System.err.println("Missing logo resource: " + resourcePath); // log missing path
		}
		return view; // return configured image view
	}

	// render the landing page with login and donate actions
	public void showLandingPage() { // define showlandingpage method
		ImageView headerIcon = loadLogo(AppState.LOGO_ICON_PATH, 58); // load green seedling icon for header
		VBox brandText = new VBox(1); // stack brand name and subtitle

		Label brandName = new Label("Seed Spreaders."); // main title
		brandName.getStyleClass().add("landing-brand-name"); // apply css class

		Label brandTagline = new Label("SEED BANK SYSTEM"); // uppercase subtitle
		brandTagline.getStyleClass().add("landing-brand-tagline"); // apply css class

		brandText.getChildren().addAll(brandName, brandTagline); // add labels to stack
		brandText.setAlignment(Pos.CENTER_LEFT); // align left

		HBox brand = new HBox(11, headerIcon, brandText); // combine icon and text lockup
		brand.setAlignment(Pos.CENTER_LEFT); // center vertically

		Button loginButton = new Button("Log In   \u2192"); // top-right login button
		loginButton.getStyleClass().add("landing-login-button"); // apply green pill css
		loginButton.setOnAction(event -> showLoginPage()); // navigate to login screen
		loginButton.setMinWidth(132); // keep comfortable button width

		Region headerSpacer = new Region(); // spacer pushing login button right
		HBox.setHgrow(headerSpacer, Priority.ALWAYS); // expand spacer horizontally

		HBox header = new HBox(brand, headerSpacer, loginButton); // top navigation bar
		header.setAlignment(Pos.CENTER_LEFT); // vertically center items
		header.setPadding(new Insets(19, 72, 18, 72)); // outer navbar padding
		header.getStyleClass().add("landing-header"); // apply header css

		Text every = new Text("Every "); // white first word
		every.getStyleClass().add("landing-title-white"); // white hero text style

		Text seed = new Text("seed"); // sage green keyword
		seed.getStyleClass().add("landing-title-green"); // green hero text style

		Text secondLine = new Text(" has\na future."); // lime-tinted second line
		secondLine.getStyleClass().add("landing-title-lime"); // lime hero text style

		TextFlow heading = new TextFlow(every, seed, secondLine); // multi-colored headline
		heading.getStyleClass().add("landing-title"); // headline css
		heading.setPrefWidth(415); // preferred wrap width
		heading.setMaxWidth(415); // max wrap width
		heading.setLineSpacing(3); // line spacing

		Label description = new Label( // continue statement
				"Supporting Filipino farmers with access to quality seeds, " // continue statement
						+ "so more farms can take root, grow stronger, and give back."); // mission summary paragraph
		description.getStyleClass().add("landing-description"); // description css
		description.setWrapText(true); // wrap paragraph lines
		description.setMaxWidth(368); // match headline column width

		Button donateButton = new Button("Donate"); // primary cta button
		donateButton.getStyleClass().add("landing-donate-button"); // cta css
		donateButton.setPrefWidth(175); // fixed cta width
		donateButton.setOnAction(event -> showDonationDialog()); // open donation modal
		VBox.setMargin(donateButton, new Insets(20, 0, 0, 0)); // space above donate button

		VBox copy = new VBox(20, heading, description, donateButton); // left hero text column
		copy.setAlignment(Pos.CENTER_LEFT); // align left
		copy.setMinWidth(390); // minimum width
		copy.setPrefWidth(440); // preferred width
		HBox.setHgrow(copy, Priority.ALWAYS); // allow column growth

		ImageView heroLogo = loadLogo(AppState.LOGO_HERO_PATH, 425); // large white logo mark on right
		heroLogo.setFitWidth(425); // hero mark width
		heroLogo.setFitHeight(425); // hero mark height
		heroLogo.setPreserveRatio(true); // preserve proportions

		HBox heroContents = new HBox(16, copy, heroLogo); // hero interior row
		heroContents.setAlignment(Pos.CENTER); // center vertically
		heroContents.setPadding(new Insets(32, 55, 32, 55)); // inner hero padding
		heroContents.setMaxWidth(Double.MAX_VALUE); // fill hero card width
		heroContents.setMinHeight(490); // minimum hero height

		Pane decoration = new Pane(); // translucent circle overlay layer
		decoration.setMouseTransparent(true); // ignore pointer events

		Circle upperCircle = new Circle(148, Color.rgb(255, 255, 255, .045)); // top-left subtle circle
		upperCircle.setCenterX(105); // x offset
		upperCircle.setCenterY(5); // y offset

		Circle lowerCircle = new Circle(120, Color.rgb(255, 255, 255, .045)); // bottom-right subtle circle
		lowerCircle.centerXProperty().bind(decoration.widthProperty().subtract(72)); // bind to right edge
		lowerCircle.centerYProperty().bind(decoration.heightProperty().add(52)); // bind to bottom edge

		decoration.getChildren().addAll(upperCircle, lowerCircle); // add circles to overlay

		StackPane hero = new StackPane(decoration, heroContents); // dark green rounded hero banner
		hero.getStyleClass().add("landing-hero"); // hero background css
		hero.setMaxWidth(1080); // max card width
		hero.setPrefHeight(490); // preferred height
		hero.setMinHeight(490); // minimum height

		Rectangle heroClip = new Rectangle(); // rounded corner clip shape
		heroClip.widthProperty().bind(hero.widthProperty()); // match card width
		heroClip.heightProperty().bind(hero.heightProperty()); // match card height
		heroClip.setArcWidth(32); // corner horizontal arc
		heroClip.setArcHeight(32); // corner vertical arc
		hero.setClip(heroClip); // apply rounded clip

		StackPane content = new StackPane(hero); // outer centered container
		content.getStyleClass().add("landing-content"); // background css
		content.setPadding(new Insets(45, 68, 66, 68)); // outer page margins

		BorderPane root = new BorderPane(); // main page layout
		root.getStyleClass().add("landing-root"); // root css
		root.setTop(header); // attach navbar
		root.setCenter(content); // attach hero card

		Scene scene = new Scene(root, 1180, 745); // create window scene
		URL css = PublicPages.class.getResource("/application/style.css"); // load shared stylesheet
		if (css != null) { // attach css if found
			scene.getStylesheets().add(css.toExternalForm()); // apply stylesheet
		}

		mainStage.setScene(scene); // display landing scene
		mainStage.setTitle("Seed Spreaders - Welcome"); // set window title
	}

	// render the split-screen sign-in view (admin-created accounts only, suspended reasons shown)
	public void showLoginPage() { // define showloginpage method
		ImageView whiteLogo = loadLogo(AppState.LOGO_HERO_PATH, 120); // white brand logo on left panel
		whiteLogo.setFitWidth(120); // icon width
		whiteLogo.setFitHeight(120); // icon height

		Label leftBrandName = new Label("Seed\nSpreaders."); // stacked brand title
		leftBrandName.getStyleClass().add("login-brand-name"); // css class

		Label leftBrandTagline = new Label("SEED BANK SYSTEM"); // subtitle
		leftBrandTagline.getStyleClass().add("login-brand-tagline"); // css class

		VBox brandWords = new VBox(4, leftBrandName, leftBrandTagline); // stack text beside logo
		brandWords.setAlignment(Pos.CENTER_LEFT); // align left

		HBox brandLockup = new HBox(10, whiteLogo, brandWords); // centered logo lockup
		brandLockup.setAlignment(Pos.CENTER); // center horizontally and vertically

		StackPane leftPanel = new StackPane(brandLockup); // dark green left half
		leftPanel.getStyleClass().add("login-left-panel"); // apply dark forest css

		Label title = new Label("Sign in"); // form title
		title.getStyleClass().add("login-title"); // heading css

		Label usernameLabel = new Label("Username / User ID"); // username label (no email login)
		usernameLabel.getStyleClass().add("login-field-label"); // label css

		TextField usernameField = new TextField(); // username input field
		usernameField.setPromptText("Enter Username or User ID"); // placeholder hint
		usernameField.getStyleClass().add("login-input"); // input css

		Label passwordLabel = new Label("Password"); // password label
		passwordLabel.getStyleClass().add("login-field-label"); // label css

		PasswordField passwordField = new PasswordField(); // masked password field
		passwordField.setPromptText("Enter Password"); // placeholder hint
		passwordField.getStyleClass().add("login-input"); // input css

		Label warning = new Label(); // validation and suspension feedback label
		warning.getStyleClass().add("login-warning"); // red warning text css
		warning.setWrapText(true); // wrap multi-line suspension reasons
		warning.setMinHeight(24); // reserve vertical space for messages

		Button signIn = new Button("Sign in to dashboard  \u2192"); // submit login button
		signIn.getStyleClass().add("login-submit"); // primary button css
		signIn.setMaxWidth(Double.MAX_VALUE); // stretch to form width

		signIn.setOnAction(event -> { // handle submit click
			String enteredId = usernameField.getText().trim(); // read entered username or user id
			String enteredPw = passwordField.getText(); // read entered password

			User user = findUser(enteredId, enteredPw); // look up matching account
			if (user == null) { // handle wrong username or password
				warning.setText("Invalid username or password."); // display credential error
				return; // stop login flow
			}

			if (user.isSuspended()) { // block suspended users and display the admin's reason
				String reason = user.getSuspensionReason().isBlank() // declare parameter
						? "No specific reason provided by the administrator." // continue statement
						: user.getSuspensionReason(); // fallback if reason string is empty
				warning.setText("Account Suspended: " + reason); // show suspension notice with reason
				return; // prevent access to dashboard
			}

			state.currentUser = user; // store active session user
			onAuthenticated.accept(user); // route to admin or farmer dashboard
		});

		passwordField.setOnAction(event -> signIn.fire()); // allow pressing enter in password field

		Button backButton = new Button("\u2190 Back to welcome"); // link back to landing page
		backButton.getStyleClass().add("login-back-link"); // link css
		backButton.setOnAction(event -> showLandingPage()); // return to welcome page

		Label noteAdminCreated = new Label("Accounts are issued by Seed Bank Administrators."); // helper note replacing self-signup
		noteAdminCreated.setStyle("-fx-text-fill: #6d8379; -fx-font-size: 11px;"); // muted helper text style

		VBox fields = new VBox(7, usernameLabel, usernameField, passwordLabel, passwordField); // stack input controls
		VBox.setMargin(passwordLabel, new Insets(7, 0, 0, 0)); // space between username and password

		VBox form = new VBox(18, title, fields, warning, signIn, noteAdminCreated, backButton); // centered form column
		form.setAlignment(Pos.CENTER_LEFT); // left-align form controls
		form.setMaxWidth(340); // fixed form width
		form.setPrefWidth(340); // preferred form width

		StackPane rightPanel = new StackPane(form); // light right half
		rightPanel.getStyleClass().add("login-right-panel"); // off-white background css
		rightPanel.setPadding(new Insets(45)); // outer padding

		HBox root = new HBox(leftPanel, rightPanel); // split-screen container
		root.getStyleClass().add("login-root"); // root css
		leftPanel.prefWidthProperty().bind(root.widthProperty().multiply(.50)); // 50% width left panel
		rightPanel.prefWidthProperty().bind(root.widthProperty().multiply(.50)); // 50% width right panel
		HBox.setHgrow(leftPanel, Priority.ALWAYS); // grow evenly
		HBox.setHgrow(rightPanel, Priority.ALWAYS); // grow evenly

		Scene scene = new Scene(root, 1180, 745); // create login scene
		URL stylesheet = PublicPages.class.getResource("/application/style.css"); // load shared stylesheet
		if (stylesheet != null) { // attach css if found
			scene.getStylesheets().add(stylesheet.toExternalForm()); // apply stylesheet
		}

		mainStage.setScene(scene); // display login scene
		mainStage.setTitle("Seed Spreaders - Sign in"); // update window title
	}

	// find a matching user by username or user id and password
	private User findUser(String identifier, String password) { // define finduser helper
		for (User user : state.users) { // check all registered users
			boolean idMatch = user.getUsername().equalsIgnoreCase(identifier) // declare parameter
					|| user.getUserId().equalsIgnoreCase(identifier); // match either username or USR-XXX id
			if (idMatch && user.getPassword().equals(password)) { // verify password match
				return user; // return matched account
			}
		}
		return null; // return null when credentials do not match
	}

	// open the modal dialog for submitting a community donation
	private void showDonationDialog() { // define showdonationdialog helper
		Stage dialogStage = new Stage(); // create modal stage
		dialogStage.initOwner(mainStage); // set owner stage
		dialogStage.initModality(Modality.APPLICATION_MODAL); // block parent window
		dialogStage.initStyle(StageStyle.TRANSPARENT); // transparent outer frame for rounded card shadow
		dialogStage.setResizable(false); // fixed modal size
		dialogStage.setTitle("Donation Details"); // modal title

		Label title = new Label("Donation Details"); // card title
		title.getStyleClass().add("donate-title"); // css class
		HBox titleRow = new HBox(title); // title header row
		titleRow.getStyleClass().add("donate-title-row"); // padding css
		Separator titleDivider = new Separator(); // horizontal rule below title
		titleDivider.getStyleClass().add("donate-divider"); // divider css

		Label nameLabel = new Label("Full Name"); // donor name label
		nameLabel.getStyleClass().add("donate-field-label"); // label css
		TextField textName = new TextField(); // donor name input
		textName.setPromptText("Enter your name"); // placeholder
		textName.getStyleClass().add("donate-input"); // input css

		Label emailLabel = new Label("Email"); // donor email label
		emailLabel.getStyleClass().add("donate-field-label"); // label css
		TextField textEmail = new TextField(); // donor email input
		textEmail.setPromptText("name@example.com"); // placeholder
		textEmail.getStyleClass().add("donate-input"); // input css

		Label amountLabel = new Label("Amount (PHP)"); // amount label
		amountLabel.getStyleClass().add("donate-field-label"); // label css
		TextField textAmount = new TextField("250"); // default preset amount
		textAmount.getStyleClass().add("donate-input"); // input css

		Label messageLabel = new Label("Message (optional)"); // optional note label
		messageLabel.getStyleClass().add("donate-field-label"); // label css
		TextField textMessage = new TextField(); // optional message input
		textMessage.setPromptText("Leave a short message"); // placeholder
		textMessage.getStyleClass().add("donate-input"); // input css

		VBox nameField = new VBox(6, nameLabel, textName); // name column
		VBox emailField = new VBox(6, emailLabel, textEmail); // email column
		VBox amountField = new VBox(6, amountLabel, textAmount); // amount column
		VBox messageField = new VBox(6, messageLabel, textMessage); // message column
		for (VBox field : new VBox[]{nameField, emailField, amountField, messageField}) { // allow equal column expansion
			field.setMaxWidth(Double.MAX_VALUE); // fill available width
			HBox.setHgrow(field, Priority.ALWAYS); // equal horizontal grow
		}
		HBox donorRow = new HBox(20, nameField, emailField); // top row of fields
		HBox detailRow = new HBox(20, amountField, messageField); // second row of fields

		Label presetsLabel = new Label("Quick amount"); // quick-select chips label
		presetsLabel.getStyleClass().add("donate-field-label"); // label css
		HBox presetRow = new HBox(9); // horizontal row of amount toggle chips
		ToggleGroup amounts = new ToggleGroup(); // single-selection toggle group
		int[] presets = {100, 250, 500, 1000}; // preset peso amounts
		for (int preset : presets) { // create a chip button for each preset
			ToggleButton chip = new ToggleButton("\u20b1" + String.format("%,d", preset)); // format peso chip text
			chip.getStyleClass().add("donate-amount-chip"); // chip css
			chip.setToggleGroup(amounts); // attach to toggle group
			chip.setMaxWidth(Double.MAX_VALUE); // stretch evenly across row
			HBox.setHgrow(chip, Priority.ALWAYS); // equal width distribution
			chip.setOnAction(e -> textAmount.setText(Integer.toString(preset))); // sync input field when clicked
			if (preset == 250) { // pre-select 250 preset
				chip.setSelected(true); // highlight default chip
			}
			presetRow.getChildren().add(chip); // add chip to row
		}
		textAmount.textProperty().addListener((obs, before, after) -> { // sync toggle chip selection when user types manually
			try { // attempt risky block
				double typed = Double.parseDouble(after.trim().replace(",", "")); // parse typed amount
				for (int i = 0; i < presets.length; i++) { // match against preset values
					((ToggleButton) presetRow.getChildren().get(i)).setSelected(typed == presets[i]); // highlight matching chip
				}
			} catch (NumberFormatException ex) { // handle exception
				amounts.selectToggle(null); // clear chip selection on custom or non-numeric input
			}
		});
		VBox presetsArea = new VBox(7, presetsLabel, presetRow); // group preset label and chips

		Label feedback = new Label(); // status or validation label
		feedback.getStyleClass().add("donate-feedback"); // feedback css
		feedback.setWrapText(true); // wrap feedback text
		feedback.setMinHeight(18); // reserve message height

		Button cancel = new Button("Cancel"); // cancel button
		cancel.getStyleClass().add("donate-cancel-button"); // cancel css
		cancel.setOnAction(e -> dialogStage.close()); // close modal without saving

		Button donate = new Button("Donate"); // confirm donation button
		donate.getStyleClass().add("donate-save-button"); // save css
		donate.setDefaultButton(true); // trigger on enter key

		HBox actions = new HBox(12, cancel, donate); // footer button bar
		actions.setAlignment(Pos.CENTER_RIGHT); // align buttons right
		actions.getStyleClass().add("donate-actions"); // footer padding css

		donate.setOnAction(e -> { // handle submit click
			String name = textName.getText().trim(); // read donor name
			String email = textEmail.getText().trim(); // read donor email
			String amountStr = textAmount.getText().trim().replace(",", ""); // read numeric amount string
			String message = textMessage.getText().trim(); // read optional message
			feedback.getStyleClass().removeAll("donate-feedback-success", "donate-feedback-error"); // reset feedback state

			if (name.isEmpty() || email.isEmpty() || amountStr.isEmpty()) { // check required fields
				feedback.setText("Please enter your name, email, and donation amount."); // show missing fields error
				feedback.getStyleClass().add("donate-feedback-error"); // apply red text css
				return; // abort submission
			}
			if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) { // validate email format
				feedback.setText("Please enter a valid email address."); // show invalid email error
				feedback.getStyleClass().add("donate-feedback-error"); // apply red text css
				return; // abort submission
			}
			try { // attempt risky block
				double amount = Double.parseDouble(amountStr); // parse donation amount
				if (!Double.isFinite(amount) || amount <= 0) { // ensure positive amount
					feedback.setText("Please enter an amount greater than zero."); // show positive amount error
					feedback.getStyleClass().add("donate-feedback-error"); // apply red text css
					return; // abort submission
				}
				state.donationData.add(new Donation(name, email, state.round2(amount), message)); // append donation record
				state.saveDonations(); // save updated donations to donations.txt
				state.updateDonationTotal(); // recalculate total donations metric
				feedback.setText("Donation record saved. Thank you for supporting Filipino farmers!"); // show confirmation message
				feedback.getStyleClass().add("donate-feedback-success"); // apply green success css

				cancel.setVisible(false); // hide cancel button after recording
				cancel.setManaged(false); // collapse cancel button space
				donate.setText("Close"); // convert primary button to close
				donate.setOnAction(event -> dialogStage.close()); // close modal on click
				donate.requestFocus(); // focus close button
			} catch (NumberFormatException ex) { // handle exception
				feedback.setText("Please enter a valid numeric amount."); // show numeric parse error
				feedback.getStyleClass().add("donate-feedback-error"); // apply red text css
			}
		});

		VBox body = new VBox(16, donorRow, detailRow, presetsArea, feedback); // modal form body
		body.getStyleClass().add("donate-body"); // body padding css
		Separator footerDivider = new Separator(); // divider above footer buttons
		footerDivider.getStyleClass().add("donate-divider"); // divider css

		VBox card = new VBox(titleRow, titleDivider, body, footerDivider, actions); // white rounded modal card
		card.getStyleClass().add("donate-card"); // card shadow and border css
		StackPane root = new StackPane(card); // outer transparent wrapper
		root.getStyleClass().add("donate-root"); // root css
		root.setPadding(new Insets(18)); // shadow margin

		Scene scene = new Scene(root, 560, 488, Color.TRANSPARENT); // transparent scene for rounded drop shadow
		URL css = PublicPages.class.getResource("/application/style.css"); // load stylesheet
		if (css != null) { // attach css if found
			scene.getStylesheets().add(css.toExternalForm()); // apply stylesheet
		}
		dialogStage.setScene(scene); // set modal scene
		dialogStage.showAndWait(); // display modal dialog
	}
}
