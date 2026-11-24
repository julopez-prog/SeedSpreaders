package application.publicpages;

import application.*;
import application.shared.AppState;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;



import java.util.function.Consumer;

public class PublicPages {
	private final Stage mainStage;
	private final AppState state;
	private final Consumer<User> onAuthenticated;

	public PublicPages(Stage stage, AppState state, Consumer<User> onAuthenticated) {
		this.mainStage = stage;
		this.state = state;
		this.onAuthenticated = onAuthenticated;
	}
	

	private ImageView loadLogo(String resourcePath, double size) {
	
		java.net.URL resource =
			PublicPages.class.getResource(resourcePath);
	
		ImageView view = new ImageView();
	
		view.setFitWidth(size);
		view.setFitHeight(size);
		view.setPreserveRatio(true);
		view.setSmooth(true);
	
		if (resource != null) {
			view.setImage(
				new Image(resource.toExternalForm())
			);
		} else {
			System.err.println(
				"Missing logo resource: " + resourcePath
			);
		}
	
		return view;
	}

	public void showLandingPage() {

		// HEADER LOGO
		ImageView headerIcon = loadLogo(
			"/application/assets/seed-spreaders-icon.png", 58
		);

		VBox brandText = new VBox(1);

		Label brandName = new Label("Seed Spreaders.");
		brandName.getStyleClass().add("landing-brand-name");

		Label brandTagline = new Label("SEED BANK SYSTEM");
		brandTagline.getStyleClass().add("landing-brand-tagline");

		brandText.getChildren().addAll(brandName, brandTagline);
		brandText.setAlignment(Pos.CENTER_LEFT);

		HBox brand = new HBox(11, headerIcon, brandText);
		brand.setAlignment(Pos.CENTER_LEFT);


		// LOGIN BUTTON
		Button loginButton = new Button("Log In   →");
		loginButton.getStyleClass().add("landing-login-button");
		loginButton.setOnAction(event -> showLoginPage());
		loginButton.setMinWidth(132);

		Region headerSpacer = new Region();
		HBox.setHgrow(headerSpacer, Priority.ALWAYS);

		HBox header = new HBox(brand, headerSpacer, loginButton);
		header.setAlignment(Pos.CENTER_LEFT);
		header.setPadding(new Insets(19, 72, 18, 72));
		header.getStyleClass().add("landing-header");


		// MAIN HEADING
		Text every = new Text("Every ");
		every.getStyleClass().add("landing-title-white");

		Text seed = new Text("seed");
		seed.getStyleClass().add("landing-title-green");

		Text secondLine = new Text(" has\na future.");
		secondLine.getStyleClass().add("landing-title-lime");

		TextFlow heading = new TextFlow(every, seed, secondLine);
		heading.getStyleClass().add("landing-title");
		heading.setPrefWidth(415);
		heading.setMaxWidth(415);
		heading.setLineSpacing(3);


		// DESCRIPTION
		Label description = new Label(
			"Supporting Filipino farmers with access to quality seeds, " +
			"so more farms can take root, grow stronger, and give back."
		);

		description.getStyleClass().add("landing-description");
		description.setWrapText(true);
		description.setMaxWidth(368);


		// DONATE BUTTON
		Button donateButton = new Button("Donate");
		donateButton.getStyleClass().add("landing-donate-button");
		donateButton.setPrefWidth(175);

		donateButton.setOnAction(
			event -> showDonationDialog()
		);

		VBox.setMargin(
			donateButton,
			new Insets(20, 0, 0, 0)
		);


		// LEFT CONTENT
		VBox copy = new VBox(
			20,
			heading,
			description,
			donateButton
		);

		copy.setAlignment(Pos.CENTER_LEFT);
		copy.setMinWidth(390);
		copy.setPrefWidth(440);

		HBox.setHgrow(copy, Priority.ALWAYS);


		// MAIN LOGO IMAGE
		ImageView heroLogo = loadLogo(
			"/application/assets/seed-spreaders-hero.png", 425
		);

		heroLogo.setFitWidth(425);
		heroLogo.setFitHeight(425);
		heroLogo.setPreserveRatio(true);


		// HERO LAYOUT
		HBox heroContents = new HBox(
			16,
			copy,
			heroLogo
		);

		heroContents.setAlignment(Pos.CENTER);
		heroContents.setPadding(
			new Insets(32, 55, 32, 55)
		);

		heroContents.setMaxWidth(Double.MAX_VALUE);
		heroContents.setMinHeight(490);


		// DECORATIVE CIRCLES
		Pane decoration = new Pane();
		decoration.setMouseTransparent(true);

		Circle upperCircle = new Circle(
			148,
			Color.rgb(255, 255, 255, .045)
		);

		upperCircle.setCenterX(105);
		upperCircle.setCenterY(5);

		Circle lowerCircle = new Circle(
			120,
			Color.rgb(255, 255, 255, .045)
		);

		lowerCircle.centerXProperty().bind(
			decoration.widthProperty().subtract(72)
		);

		lowerCircle.centerYProperty().bind(
			decoration.heightProperty().add(52)
		);

		decoration.getChildren().addAll(
			upperCircle,
			lowerCircle
		);


		// HERO CONTAINER
		StackPane hero = new StackPane(
			decoration,
			heroContents
		);

		hero.getStyleClass().add("landing-hero");
		hero.setMaxWidth(1080);
		hero.setPrefHeight(490);
		hero.setMinHeight(490);


		// ROUNDED HERO CLIPPING
		Rectangle heroClip = new Rectangle();

		heroClip.widthProperty().bind(
			hero.widthProperty()
		);

		heroClip.heightProperty().bind(
			hero.heightProperty()
		);

		heroClip.setArcWidth(32);
		heroClip.setArcHeight(32);

		hero.setClip(heroClip);


		// PAGE CONTENT
		StackPane content = new StackPane(hero);
		content.getStyleClass().add("landing-content");
		content.setPadding(
			new Insets(45, 68, 66, 68)
		);


		// ROOT LAYOUT
		BorderPane root = new BorderPane();

		root.getStyleClass().add("landing-root");
		root.setTop(header);
		root.setCenter(content);


		// LOAD SHARED CSS
		Scene scene = new Scene(root, 1180, 745);

		java.net.URL css = PublicPages.class.getResource(
			"/application/style.css"
		);

		if (css != null) {
			scene.getStylesheets().add(
				css.toExternalForm()
			);
		}

		mainStage.setScene(scene);
		mainStage.setTitle("Seed Spreaders - Welcome");
	}


	public void showLoginPage() {
	
		// LEFT PANEL - LOGO
		ImageView whiteLogo = loadLogo(
			"/application/assets/seed-spreaders-hero.png", 120
		);
	
		whiteLogo.setFitWidth(120);
		whiteLogo.setFitHeight(120);
	
		Label leftBrandName = new Label("Seed\nSpreaders.");
		leftBrandName.getStyleClass().add("login-brand-name");
	
		Label leftBrandTagline = new Label("SEED BANK SYSTEM");
		leftBrandTagline.getStyleClass().add("login-brand-tagline");
	
		VBox brandWords = new VBox(
			4, leftBrandName, leftBrandTagline
		);
		brandWords.setAlignment(Pos.CENTER_LEFT);
	
		HBox brandLockup = new HBox(
			10, whiteLogo, brandWords
		);
		brandLockup.setAlignment(Pos.CENTER);
	
		StackPane leftPanel = new StackPane(brandLockup);
		leftPanel.getStyleClass().add("login-left-panel");
	
	
		// RIGHT PANEL - LOGIN FORM
		Label title = new Label("Sign in");
		title.getStyleClass().add("login-title");
	
	
		// USERNAME
		Label usernameLabel = new Label("User ID");
		usernameLabel.getStyleClass().add("login-field-label");
	
		TextField usernameField = new TextField();
		usernameField.setPromptText("Enter username");
		usernameField.getStyleClass().add("login-input");
	
	
		// EMAIL (OPTIONAL - VISUAL ONLY)
		Label emailLabel = new Label("Email (optional)");
		emailLabel.getStyleClass().add("login-field-label");
	
		TextField emailField = new TextField();
		emailField.setPromptText("Enter email");
		emailField.getStyleClass().add("login-input");
	
	
		// PASSWORD
		Label passwordLabel = new Label("Password");
		passwordLabel.getStyleClass().add("login-field-label");
	
		PasswordField passwordField = new PasswordField();
		passwordField.setPromptText("Enter password");
		passwordField.getStyleClass().add("login-input");
	
	
		// ERROR MESSAGE
		Label warning = new Label();
		warning.getStyleClass().add("login-warning");
		warning.setWrapText(true);
		warning.setMinHeight(18);
	
	
		// LOGIN BUTTON
		Button signIn = new Button("Sign in to dashboard  →");
		signIn.getStyleClass().add("login-submit");
		signIn.setMaxWidth(Double.MAX_VALUE);
	
		signIn.setOnAction(event -> {
	
			User user = findUser(
				usernameField.getText().trim(),
				passwordField.getText()
			);
	
			if (user == null) {
				warning.setText("Invalid username or password.");
				return;
			}
	
			state.currentUser = user;
			onAuthenticated.accept(user);
		});
	
		// Pressing Enter in password field also logs in
		passwordField.setOnAction(event -> signIn.fire());
	
	
		// BACK BUTTON
		Button backButton = new Button("← Back to welcome");
		backButton.getStyleClass().add("login-back-link");
	
		backButton.setOnAction(event -> showLandingPage());
	
	
		// ORGANIZE INPUT FIELDS
		VBox fields = new VBox(
			7,
			usernameLabel, usernameField,
			emailLabel, emailField,
			passwordLabel, passwordField
		);
	
		VBox.setMargin(
			emailLabel,
			new Insets(7, 0, 0, 0)
		);
	
		VBox.setMargin(
			passwordLabel,
			new Insets(7, 0, 0, 0)
		);
	
	
		// FORM CONTAINER
		VBox form = new VBox(
			21,
			title,
			fields,
			warning,
			signIn,
			backButton
		);
	
		form.setAlignment(Pos.CENTER_LEFT);
		form.setMaxWidth(340);
		form.setPrefWidth(340);
	
	
		// RIGHT PANEL
		StackPane rightPanel = new StackPane(form);
		rightPanel.getStyleClass().add("login-right-panel");
		rightPanel.setPadding(new Insets(45));
	
	
		// COMBINE LEFT AND RIGHT PANELS
		HBox root = new HBox(leftPanel, rightPanel);
		root.getStyleClass().add("login-root");
	
		leftPanel.prefWidthProperty().bind(
			root.widthProperty().multiply(.50)
		);
	
		rightPanel.prefWidthProperty().bind(
			root.widthProperty().multiply(.50)
		);
	
		HBox.setHgrow(leftPanel, Priority.ALWAYS);
		HBox.setHgrow(rightPanel, Priority.ALWAYS);
	
	
		// CREATE SCENE
		Scene scene = new Scene(root, 1180, 745);
	
	
		// APPLY SHARED CSS
		java.net.URL stylesheet =
			PublicPages.class.getResource("/application/style.css");
	
		if (stylesheet == null) {
			System.err.println("Missing CSS: /application/style.css");
		} else {
			scene.getStylesheets().add(
				stylesheet.toExternalForm()
			);
		}
	
		mainStage.setScene(scene);
		mainStage.setTitle("Seed Spreaders - Sign in");
	}


	private User findUser(String username, String password) {
		for (User user : state.users) {
			if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
				return user;
			}
		}
		return null;
	}

	private void showDonationDialog() {
		Stage dialogStage = new Stage();
		dialogStage.initOwner(mainStage);
		dialogStage.initModality(Modality.APPLICATION_MODAL);
		dialogStage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
		dialogStage.setResizable(false);
		dialogStage.setTitle("Donation Details");

		Label title = new Label("Donation Details");
		title.getStyleClass().add("donate-title");
		HBox titleRow = new HBox(title);
		titleRow.getStyleClass().add("donate-title-row");
		javafx.scene.control.Separator titleDivider = new javafx.scene.control.Separator();
		titleDivider.getStyleClass().add("donate-divider");

		Label nameLabel = new Label("Full Name");
		nameLabel.getStyleClass().add("donate-field-label");
		TextField textName = new TextField();
		textName.setPromptText("Enter your name");
		textName.getStyleClass().add("donate-input");

		Label emailLabel = new Label("Email");
		emailLabel.getStyleClass().add("donate-field-label");
		TextField textEmail = new TextField();
		textEmail.setPromptText("name@example.com");
		textEmail.getStyleClass().add("donate-input");

		Label amountLabel = new Label("Amount (PHP)");
		amountLabel.getStyleClass().add("donate-field-label");
		TextField textAmount = new TextField("250");
		textAmount.getStyleClass().add("donate-input");

		Label messageLabel = new Label("Message (optional)");
		messageLabel.getStyleClass().add("donate-field-label");
		TextField textMessage = new TextField();
		textMessage.setPromptText("Leave a short message");
		textMessage.getStyleClass().add("donate-input");

		VBox nameField = new VBox(6, nameLabel, textName);
		VBox emailField = new VBox(6, emailLabel, textEmail);
		VBox amountField = new VBox(6, amountLabel, textAmount);
		VBox messageField = new VBox(6, messageLabel, textMessage);
		for (VBox field : new VBox[]{nameField, emailField, amountField, messageField}) {
			field.setMaxWidth(Double.MAX_VALUE);
			HBox.setHgrow(field, Priority.ALWAYS);
		}
		HBox donorRow = new HBox(20, nameField, emailField);
		HBox detailRow = new HBox(20, amountField, messageField);

		Label presetsLabel = new Label("Quick amount");
		presetsLabel.getStyleClass().add("donate-field-label");
		HBox presetRow = new HBox(9);
		ToggleGroup amounts = new ToggleGroup();
		int[] presets = {100, 250, 500, 1000};
		for (int preset : presets) {
			ToggleButton chip = new ToggleButton("₱" + String.format("%,d", preset));
			chip.getStyleClass().add("donate-amount-chip");
			chip.setToggleGroup(amounts);
			chip.setMaxWidth(Double.MAX_VALUE);
			HBox.setHgrow(chip, Priority.ALWAYS);
			chip.setOnAction(e -> textAmount.setText(Integer.toString(preset)));
			if (preset == 250) chip.setSelected(true);
			presetRow.getChildren().add(chip);
		}
		textAmount.textProperty().addListener((obs, before, after) -> {
			try {
				double typed = Double.parseDouble(after.trim().replace(",", ""));
				for (int i = 0; i < presets.length; i++) {
					((ToggleButton) presetRow.getChildren().get(i)).setSelected(typed == presets[i]);
				}
			} catch (NumberFormatException ex) {
				amounts.selectToggle(null);
			}
		});
		VBox presetsArea = new VBox(7, presetsLabel, presetRow);

		Label feedback = new Label();
		feedback.getStyleClass().add("donate-feedback");
		feedback.setWrapText(true);
		feedback.setMinHeight(18);

		Button cancel = new Button("Cancel");
		cancel.getStyleClass().add("donate-cancel-button");
		cancel.setOnAction(e -> dialogStage.close());
		Button donate = new Button("Donate");
		donate.getStyleClass().add("donate-save-button");
		donate.setDefaultButton(true);
		HBox actions = new HBox(12, cancel, donate);
		actions.setAlignment(Pos.CENTER_RIGHT);
		actions.getStyleClass().add("donate-actions");

		donate.setOnAction(e -> {
			String name = textName.getText().trim();
			String email = textEmail.getText().trim();
			String amountStr = textAmount.getText().trim().replace(",", "");
			String message = textMessage.getText().trim();
			feedback.getStyleClass().removeAll("donate-feedback-success", "donate-feedback-error");
			if (name.isEmpty() || email.isEmpty() || amountStr.isEmpty()) {
				feedback.setText("Please enter your name, email, and donation amount.");
				feedback.getStyleClass().add("donate-feedback-error");
				return;
			}
			if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
			    feedback.setText("Please enter a valid email address.");
			    feedback.getStyleClass().add("donate-feedback-error");
			    return;
			}
			try {
				double amount = Double.parseDouble(amountStr);
				if (!Double.isFinite(amount) || amount <= 0) {
					feedback.setText("Please enter an amount greater than zero.");
					feedback.getStyleClass().add("donate-feedback-error");
					return;
				}
				state.donationData.add(new Donation(name, email, state.round2(amount), message));
				state.saveDonations(state.donationPath, state.donationData);
				state.updateDonationTotal();
				feedback.setText("Donation record saved. No payment processed.");
				feedback.getStyleClass().add("donate-feedback-success");

				// Hide Cancel after successful donation
				cancel.setVisible(false);
				cancel.setManaged(false);

				// Change Donate into Close
				donate.setText("Close");
				donate.setOnAction(event -> dialogStage.close());
				donate.requestFocus();
			} catch (NumberFormatException ex) {
				feedback.setText("Please enter a valid numeric amount.");
				feedback.getStyleClass().add("donate-feedback-error");
			}
		});

		VBox body = new VBox(16, donorRow, detailRow, presetsArea, feedback);
		body.getStyleClass().add("donate-body");
		javafx.scene.control.Separator footerDivider = new javafx.scene.control.Separator();
		footerDivider.getStyleClass().add("donate-divider");
		VBox card = new VBox(titleRow, titleDivider, body, footerDivider, actions);
		card.getStyleClass().add("donate-card");
		StackPane root = new StackPane(card);
		root.getStyleClass().add("donate-root");
		root.setPadding(new Insets(18));

		Scene scene = new Scene(root, 560, 488, Color.TRANSPARENT);
		java.net.URL css = PublicPages.class.getResource("/application/style.css");
		if (css != null) scene.getStylesheets().add(css.toExternalForm());
		else System.err.println("Missing CSS: /application/style.css");
		dialogStage.setScene(scene);
		dialogStage.showAndWait();
	}
}
