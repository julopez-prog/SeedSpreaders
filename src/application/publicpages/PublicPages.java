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

	public void showLandingPage() {
		Label title = new Label("Seed Spreaders");
		title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
		Label subtitle = new Label("A simple seed lending prototype for Laguna farmers.");

		Button btnDonate = new Button("Donate");
		Button btnLogin = new Button("Login");

		btnDonate.setOnAction(e -> showDonationDialog());
		btnLogin.setOnAction(e -> showLoginPage());

		VBox root = new VBox(12, title, subtitle, btnDonate, btnLogin);
		root.setStyle("-fx-padding: 25;");

		mainStage.setScene(new Scene(root, 520, 320));
		mainStage.setTitle("Seed Spreaders Prototype");
	}

	public void showLoginPage() {
		Label title = new Label("Login");
		title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

		Label labelRole = new Label("Login as:");
		ComboBox<String> roleBox = new ComboBox<>();
		roleBox.getItems().addAll("Admin", "Farmer");
		roleBox.setValue("Farmer");

		Label labelUsername = new Label("Username:");
		TextField textUsername = new TextField();

		Label labelPassword = new Label("Password:");
		PasswordField textPassword = new PasswordField();

		Button btnLogin = new Button("Login");
		Button btnBack = new Button("Back");
		Label lblWarn = new Label();
		Label lblHint = new Label("Demo accounts: admin / admin123, juan / farmer123, maria / farmer123");

		btnLogin.setOnAction(e -> {
			User user = findUser(textUsername.getText().trim(), textPassword.getText());

			if (user == null) {
				lblWarn.setText("Error: Invalid username or password.");
				return;
			}

			if (!user.getRole().equals(roleBox.getValue())) {
				lblWarn.setText("Error: This account is not registered as " + roleBox.getValue() + ".");
				return;
			}

			state.currentUser = user;
			onAuthenticated.accept(user);
		});
		btnBack.setOnAction(e -> showLandingPage());

		HBox buttons = new HBox(10, btnLogin, btnBack);

		VBox root = new VBox(8, title, labelRole, roleBox, labelUsername, textUsername,
				labelPassword, textPassword, buttons, lblWarn, lblHint);
		root.setStyle("-fx-padding: 20;");

		mainStage.setScene(new Scene(root, 500, 400));
		mainStage.setTitle("Seed Spreaders - Login");
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
		dialogStage.setTitle("Donate");

		TextField textName = new TextField();
		TextField textEmail = new TextField();
		TextField textAmount = new TextField();
		TextArea textMessage = new TextArea();
		textMessage.setPrefRowCount(3);

		Label labelName = new Label("Name:");
		labelName.setMinWidth(100);
		HBox hBoxName = new HBox(10, labelName, textName);

		Label labelEmail = new Label("Email:");
		labelEmail.setMinWidth(100);
		HBox hBoxEmail = new HBox(10, labelEmail, textEmail);

		Label labelAmount = new Label("Amount:");
		labelAmount.setMinWidth(100);
		HBox hBoxAmount = new HBox(10, labelAmount, textAmount);

		Label labelMessage = new Label("Message:");
		labelMessage.setMinWidth(100);
		HBox hBoxMessage = new HBox(10, labelMessage, textMessage);

		Button btnDonate = new Button("Donate");
		Button btnClose = new Button("Close");
		Label lblWarn = new Label();

		btnDonate.setOnAction(e -> {
			String name = textName.getText().trim();
			String email = textEmail.getText().trim();
			String amountStr = textAmount.getText().trim();
			String message = textMessage.getText().trim();

			if (name.isEmpty() || email.isEmpty() || amountStr.isEmpty()) {
				lblWarn.setText("Error: Name, email, and amount are required!");
				return;
			}
			if (!email.contains("@")) {
				lblWarn.setText("Error: Please enter a valid email address.");
				return;
			}

			try {
				double amount = Double.parseDouble(amountStr);
				if (amount <= 0) {
					lblWarn.setText("Error: Donation amount must be greater than zero.");
					return;
				}

				state.donationData.add(new Donation(name, email, state.round2(amount), message));
				state.saveDonations(state.donationPath, state.donationData);
				state.updateDonationTotal();

				lblWarn.setText("Thank you! Your donation was recorded.");
				textName.clear();
				textEmail.clear();
				textAmount.clear();
				textMessage.clear();
			} catch (NumberFormatException ex) {
				lblWarn.setText("Error: Donation amount must be a valid number!");
			}
		});
		btnClose.setOnAction(e -> dialogStage.close());

		HBox buttons = new HBox(10, btnDonate, btnClose);
		VBox root = new VBox(10, hBoxName, hBoxEmail, hBoxAmount, hBoxMessage, buttons, lblWarn);
		root.setStyle("-fx-padding: 15;");

		dialogStage.setScene(new Scene(root, 460, 380));
		dialogStage.showAndWait();
	}
}
