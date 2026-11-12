/*
- @author
- Paolo Jay A. Labindalawa
- Axel Andre B. Lao
- Jared U. Lopez
- @date 11/07/26 23:30
-
- SeedSpreaders Milestone 3
-
-
-
- The Seed Spreaders Inventory Management System helps organize and monitor the seed bank’s inventory.
- It allows users to add, view, edit, and delete seed products while tracking their quantities, prices,
- and storage locations. The system also saves inventory records and updates stock summaries to keep
- seed management simple and efficient.
*/
package application;

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


public class Main extends Application {
	private Stage mainStage;
	private User currentUser;

	private ObservableList<Product> productData;
	private ObservableList<SeedRequest> requestData = FXCollections.observableArrayList();
	private ObservableList<SeedReturn> returnData = FXCollections.observableArrayList();
	private ObservableList<Donation> donationData = FXCollections.observableArrayList();

	private final Path productPath = Paths.get("products.txt");
	private final Path donationPath = Paths.get("donations.txt");

	private TableView<Product> inventoryTable;

	private int nextRequestId = 1;
	private int nextReturnId = 1;

	private final List<User> users = List.of(
			new User("admin", "admin123", "Admin"),
			new User("juan", "farmer123", "Farmer"),
			new User("maria", "farmer123", "Farmer"));

	// Summary labels for the top portion
	private Label lblVarieties = new Label("Total Plant Varieties: 0");
	private Label lblStocks = new Label("Total Seed Stocks: 0.0");
	private Label lblLots = new Label("Total Storage Lots: 0");
	private Label lblDonationTotal = new Label("Total Donations: 0.00");

	@Override
	public void start(Stage stage) {
		mainStage = stage;

		// Load data from files
		productData = loadProducts(productPath);
		donationData = loadDonations(donationPath);

		// Listen for changes in productData to dynamically update summary metrics
		productData.addListener((ListChangeListener.Change<? extends Product> c) -> {
			updateSummaryMetrics();
		});
		donationData.addListener((ListChangeListener.Change<? extends Donation> c) -> {
			updateDonationTotal();
		});

		updateSummaryMetrics();
		updateDonationTotal();

		showLandingPage();
		stage.show();
	}

	// Shows the first page with the donate and login buttons
	private void showLandingPage() {
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

	// Shows the login page for admins and farmers
	private void showLoginPage() {
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

			currentUser = user;
			if (user.getRole().equals("Admin")) {
				showAdminDashboard();
			} else {
				showFarmerDashboard();
			}
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
		for (User user : users) {
			if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
				return user;
			}
		}
		return null;
	}

	// Shows the admin dashboard with inventory and transactions
	private void showAdminDashboard() {
		Label lblUser = new Label("Logged in as: " + currentUser.getUsername() + " (Admin)");
		Button btnLogout = new Button("Logout");
		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		HBox topBar = new HBox(10, lblUser, spacer, btnLogout);
		topBar.setStyle("-fx-padding: 10;");

		Tab inventoryTab = new Tab("Inventory", createInventoryPane());
		Tab transactionTab = new Tab("Transactions", createAdminTransactionsPane());
		Tab donationTab = new Tab("Donations", createAdminDonationsPane());
		inventoryTab.setClosable(false);
		transactionTab.setClosable(false);
		donationTab.setClosable(false);

		TabPane tabs = new TabPane(inventoryTab, transactionTab, donationTab);

		btnLogout.setOnAction(e -> {
			saveProducts(productPath, productData);
			currentUser = null;
			showLandingPage();
		});

		BorderPane root = new BorderPane();
		root.setTop(topBar);
		root.setCenter(tabs);

		mainStage.setScene(new Scene(root, 1100, 650));
		mainStage.setTitle("Seed Spreaders - Admin");
	}

	// Builds the inventory page from the original product database example
	private BorderPane createInventoryPane() {
		// Table
		TableView<Product> table = new TableView<>();
		inventoryTable = table;
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		TableColumn<Product, Integer> colLotID = new TableColumn<>("Lot ID");
		TableColumn<Product, String> colVariety = new TableColumn<>("Variety");
		TableColumn<Product, String> colLocation = new TableColumn<>("Location");
		TableColumn<Product, Double> colAmount = new TableColumn<>("Amount");
		TableColumn<Product, String> colName = new TableColumn<>("Name");
		TableColumn<Product, Double> colPrice = new TableColumn<>("Price");
		TableColumn<Product, String> colDescription = new TableColumn<>("Description");

		colLotID.setCellValueFactory(new PropertyValueFactory<>("lotID"));
		colVariety.setCellValueFactory(new PropertyValueFactory<>("variety"));
		colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));
		colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
		colName.setCellValueFactory(new PropertyValueFactory<>("name"));
		colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
		colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));

		// Column with a button to view product details
		TableColumn<Product, Void> colAction = new TableColumn<>("Actions");
		colAction.setCellFactory(param -> new TableCell<>() {
			private final Button btnView = new Button("View Details");

			{
				btnView.setOnAction(e -> {
					Product product = getTableView().getItems().get(getIndex());
					openProductDetailDialog(product);
				});
			}

			@Override
			protected void updateItem(Void item, boolean empty) {
				super.updateItem(item, empty);
				if (empty) {
					setGraphic(null);
				} else {
					setGraphic(btnView);
				}
			}
		});

		table.getColumns().addAll(
				colLotID, colVariety, colLocation, colAmount, colName, colPrice, colDescription, colAction);
		table.setItems(productData);

		// Buttons (only Add Inventory and Save & Quit on top bar)
		Button btnProduct = new Button("Create New Product");
		Button btnSaveQuit = new Button("Save and Quit");

		btnProduct.setOnAction(e -> {
			createNewProduct();
		});

		btnSaveQuit.setOnAction(e -> {
			saveProducts(productPath, productData);
			currentUser = null;
			showLandingPage();
		});

		// Summary bar layout container
		HBox summaryBar = new HBox(20, lblVarieties, lblStocks, lblLots);
		summaryBar.setStyle("-fx-padding: 10 10 0 10; -fx-font-weight: bold;");

		// Toolbar layout container
		HBox toolbar = new HBox(10, btnProduct, btnSaveQuit);
		toolbar.setStyle("-fx-padding: 10;");

		// Combine summary and toolbar in the top portion
		VBox topContainer = new VBox(5, summaryBar, toolbar);

		BorderPane root = new BorderPane(table);
		root.setTop(topContainer);
		return root;
	}

	// Builds the admin side of the transaction workflow
	private VBox createAdminTransactionsPane() {
		Label lblRequests = new Label("Seed Requests");
		lblRequests.setStyle("-fx-font-weight: bold;");

		TableView<SeedRequest> requestTable = new TableView<>();
		requestTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		requestTable.setPrefHeight(200);

		TableColumn<SeedRequest, Integer> colRequestID = new TableColumn<>("Request ID");
		TableColumn<SeedRequest, String> colFarmer = new TableColumn<>("Farmer");
		TableColumn<SeedRequest, String> colSeed = new TableColumn<>("Seed");
		TableColumn<SeedRequest, Double> colAmount = new TableColumn<>("Amount (kg)");
		TableColumn<SeedRequest, String> colPurpose = new TableColumn<>("Purpose");
		TableColumn<SeedRequest, String> colStatus = new TableColumn<>("Status");

		colRequestID.setCellValueFactory(new PropertyValueFactory<>("requestId"));
		colFarmer.setCellValueFactory(new PropertyValueFactory<>("farmer"));
		colSeed.setCellValueFactory(new PropertyValueFactory<>("seedLabel"));
		colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
		colPurpose.setCellValueFactory(new PropertyValueFactory<>("purpose"));
		colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

		requestTable.getColumns().addAll(
				colRequestID, colFarmer, colSeed, colAmount, colPurpose, colStatus);
		requestTable.setItems(requestData);
		requestTable.setPlaceholder(new Label("No seed requests yet."));

		Button btnApproveRequest = new Button("Approve Request");
		Button btnRejectRequest = new Button("Reject Request");
		HBox requestButtons = new HBox(10, btnApproveRequest, btnRejectRequest);

		Label lblReturns = new Label("Seed Returns");
		lblReturns.setStyle("-fx-font-weight: bold;");

		TableView<SeedReturn> returnTable = new TableView<>();
		returnTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		returnTable.setPrefHeight(200);

		TableColumn<SeedReturn, Integer> colReturnID = new TableColumn<>("Return ID");
		TableColumn<SeedReturn, String> colReturnFarmer = new TableColumn<>("Farmer");
		TableColumn<SeedReturn, String> colReturnSeed = new TableColumn<>("Seed");
		TableColumn<SeedReturn, Double> colReturnAmount = new TableColumn<>("Amount (kg)");
		TableColumn<SeedReturn, String> colReturnStatus = new TableColumn<>("Status");

		colReturnID.setCellValueFactory(new PropertyValueFactory<>("returnId"));
		colReturnFarmer.setCellValueFactory(new PropertyValueFactory<>("farmer"));
		colReturnSeed.setCellValueFactory(new PropertyValueFactory<>("seedLabel"));
		colReturnAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
		colReturnStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

		returnTable.getColumns().addAll(
				colReturnID, colReturnFarmer, colReturnSeed, colReturnAmount, colReturnStatus);
		returnTable.setItems(returnData);
		returnTable.setPlaceholder(new Label("No seed returns yet."));

		Button btnAcceptReturn = new Button("Accept Return");
		Button btnRejectReturn = new Button("Reject Return");
		HBox returnButtons = new HBox(10, btnAcceptReturn, btnRejectReturn);

		Label lblWarn = new Label();

		btnApproveRequest.setOnAction(e -> approveRequest(requestTable, lblWarn));
		btnRejectRequest.setOnAction(e -> rejectRequest(requestTable, lblWarn));
		btnAcceptReturn.setOnAction(e -> acceptReturn(returnTable, lblWarn));
		btnRejectReturn.setOnAction(e -> rejectReturn(returnTable, lblWarn));

		VBox root = new VBox(10, lblRequests, requestTable, requestButtons,
				lblReturns, returnTable, returnButtons, lblWarn);
		root.setStyle("-fx-padding: 10;");
		return root;
	}

	// Builds the donation list for the admin
	private VBox createAdminDonationsPane() {
		TableView<Donation> table = new TableView<>();
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		TableColumn<Donation, String> colDonor = new TableColumn<>("Donor");
		TableColumn<Donation, String> colEmail = new TableColumn<>("Email");
		TableColumn<Donation, Double> colAmount = new TableColumn<>("Amount");
		TableColumn<Donation, String> colMessage = new TableColumn<>("Message");

		colDonor.setCellValueFactory(new PropertyValueFactory<>("donorName"));
		colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
		colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
		colMessage.setCellValueFactory(new PropertyValueFactory<>("message"));

		table.getColumns().addAll(colDonor, colEmail, colAmount, colMessage);
		table.setItems(donationData);
		table.setPlaceholder(new Label("No donations yet."));

		updateDonationTotal();

		VBox root = new VBox(10, table, lblDonationTotal);
		root.setStyle("-fx-padding: 10;");
		return root;
	}

	// Shows the farmer dashboard with seed requests and returns
	private void showFarmerDashboard() {
		Label lblUser = new Label("Logged in as: " + currentUser.getUsername() + " (Farmer)");
		Button btnLogout = new Button("Logout");
		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		HBox topBar = new HBox(10, lblUser, spacer, btnLogout);
		topBar.setStyle("-fx-padding: 10;");

		Tab requestTab = new Tab("Request Seeds", createFarmerRequestPane());
		Tab transactionTab = new Tab("My Transactions", createFarmerTransactionsPane());
		Tab returnTab = new Tab("Return Seeds", createFarmerReturnPane());
		requestTab.setClosable(false);
		transactionTab.setClosable(false);
		returnTab.setClosable(false);

		TabPane tabs = new TabPane(requestTab, transactionTab, returnTab);

		btnLogout.setOnAction(e -> {
			currentUser = null;
			showLandingPage();
		});

		BorderPane root = new BorderPane();
		root.setTop(topBar);
		root.setCenter(tabs);

		mainStage.setScene(new Scene(root, 1000, 650));
		mainStage.setTitle("Seed Spreaders - Farmer");
	}

	// Builds the farmer seed request page
	private VBox createFarmerRequestPane() {
		TableView<Product> table = new TableView<>();
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		table.setPrefHeight(300);

		TableColumn<Product, Integer> colLotID = new TableColumn<>("Lot ID");
		TableColumn<Product, String> colName = new TableColumn<>("Name");
		TableColumn<Product, String> colVariety = new TableColumn<>("Variety");
		TableColumn<Product, Double> colAmount = new TableColumn<>("Available (kg)");
		TableColumn<Product, Double> colPrice = new TableColumn<>("Price per kg");
		TableColumn<Product, String> colLocation = new TableColumn<>("Location");

		colLotID.setCellValueFactory(new PropertyValueFactory<>("lotID"));
		colName.setCellValueFactory(new PropertyValueFactory<>("name"));
		colVariety.setCellValueFactory(new PropertyValueFactory<>("variety"));
		colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
		colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
		colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));

		table.getColumns().addAll(colLotID, colName, colVariety, colAmount, colPrice, colLocation);
		table.setItems(productData);

		Label labelAmount = new Label("Amount (kg):");
		labelAmount.setMinWidth(100);
		TextField textAmount = new TextField();
		HBox hBoxAmount = new HBox(10, labelAmount, textAmount);

		Label labelPurpose = new Label("Purpose:");
		labelPurpose.setMinWidth(100);
		TextField textPurpose = new TextField();
		HBox hBoxPurpose = new HBox(10, labelPurpose, textPurpose);

		Button btnSubmit = new Button("Submit Request");
		Button btnRefresh = new Button("Refresh");
		Label lblWarn = new Label();

		btnRefresh.setOnAction(e -> table.refresh());

		btnSubmit.setOnAction(e -> {
			Product product = table.getSelectionModel().getSelectedItem();
			if (product == null) {
				lblWarn.setText("Error: Please select a seed lot first.");
				return;
			}

			String amountStr = textAmount.getText().trim();
			String purpose = textPurpose.getText().trim();

			if (amountStr.isEmpty() || purpose.isEmpty()) {
				lblWarn.setText("Error: Amount and purpose are required!");
				return;
			}

			try {
				double amount = Double.parseDouble(amountStr);
				if (amount <= 0) {
					lblWarn.setText("Error: Amount must be greater than zero.");
					return;
				}
				if (amount > product.getAmount()) {
					lblWarn.setText("Error: Not enough stock available.");
					return;
				}

				requestData.add(new SeedRequest(nextRequestId++, currentUser.getUsername(),
						product.getLotID(), product.getName(), product.getVariety(),
						round2(amount), purpose, "Pending"));

				lblWarn.setText("Request submitted. Please wait for admin approval.");
				textAmount.clear();
				textPurpose.clear();
			} catch (NumberFormatException ex) {
				lblWarn.setText("Error: Amount must be a valid number!");
			}
		});

		HBox buttons = new HBox(10, btnSubmit, btnRefresh);

		VBox root = new VBox(10, table, hBoxAmount, hBoxPurpose, buttons, lblWarn);
		root.setStyle("-fx-padding: 10;");
		return root;
	}

	// Builds the farmer transaction history page
	private VBox createFarmerTransactionsPane() {
		FilteredList<SeedRequest> myRequests = new FilteredList<>(requestData,
				request -> request.getFarmer().equals(currentUser.getUsername()));

		TableView<SeedRequest> requestTable = new TableView<>();
		requestTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		requestTable.setPrefHeight(220);

		TableColumn<SeedRequest, Integer> colRequestID = new TableColumn<>("Request ID");
		TableColumn<SeedRequest, String> colSeed = new TableColumn<>("Seed");
		TableColumn<SeedRequest, Double> colAmount = new TableColumn<>("Amount (kg)");
		TableColumn<SeedRequest, String> colPurpose = new TableColumn<>("Purpose");
		TableColumn<SeedRequest, String> colStatus = new TableColumn<>("Status");

		colRequestID.setCellValueFactory(new PropertyValueFactory<>("requestId"));
		colSeed.setCellValueFactory(new PropertyValueFactory<>("seedLabel"));
		colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
		colPurpose.setCellValueFactory(new PropertyValueFactory<>("purpose"));
		colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

		requestTable.getColumns().addAll(colRequestID, colSeed, colAmount, colPurpose, colStatus);
		requestTable.setItems(myRequests);
		requestTable.setPlaceholder(new Label("You have no seed requests yet."));

		FilteredList<SeedReturn> myReturns = new FilteredList<>(returnData,
				ret -> ret.getFarmer().equals(currentUser.getUsername()));

		TableView<SeedReturn> returnTable = new TableView<>();
		returnTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		returnTable.setPrefHeight(180);

		TableColumn<SeedReturn, Integer> colReturnID = new TableColumn<>("Return ID");
		TableColumn<SeedReturn, String> colReturnSeed = new TableColumn<>("Seed");
		TableColumn<SeedReturn, Double> colReturnAmount = new TableColumn<>("Amount (kg)");
		TableColumn<SeedReturn, String> colReturnStatus = new TableColumn<>("Status");

		colReturnID.setCellValueFactory(new PropertyValueFactory<>("returnId"));
		colReturnSeed.setCellValueFactory(new PropertyValueFactory<>("seedLabel"));
		colReturnAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
		colReturnStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

		returnTable.getColumns().addAll(colReturnID, colReturnSeed, colReturnAmount, colReturnStatus);
		returnTable.setItems(myReturns);
		returnTable.setPlaceholder(new Label("You have no seed returns yet."));

		Label lblRequests = new Label("My Seed Requests");
		lblRequests.setStyle("-fx-font-weight: bold;");
		Label lblReturns = new Label("My Seed Returns");
		lblReturns.setStyle("-fx-font-weight: bold;");

		VBox root = new VBox(10, lblRequests, requestTable, lblReturns, returnTable);
		root.setStyle("-fx-padding: 10;");
		return root;
	}

	// Builds the farmer seed return page
	private VBox createFarmerReturnPane() {
		ObservableList<SeedRequest> returnable = FXCollections.observableArrayList();
		loadReturnableRequests(returnable);

		TableView<SeedRequest> table = new TableView<>();
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		table.setPrefHeight(280);

		TableColumn<SeedRequest, Integer> colRequestID = new TableColumn<>("Request ID");
		TableColumn<SeedRequest, String> colSeed = new TableColumn<>("Seed");
		TableColumn<SeedRequest, Double> colAmount = new TableColumn<>("Approved (kg)");
		TableColumn<SeedRequest, String> colPurpose = new TableColumn<>("Purpose");

		colRequestID.setCellValueFactory(new PropertyValueFactory<>("requestId"));
		colSeed.setCellValueFactory(new PropertyValueFactory<>("seedLabel"));
		colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
		colPurpose.setCellValueFactory(new PropertyValueFactory<>("purpose"));

		table.getColumns().addAll(colRequestID, colSeed, colAmount, colPurpose);
		table.setItems(returnable);
		table.setPlaceholder(new Label("No approved requests are ready for return."));

		Label labelAmount = new Label("Amount (kg):");
		labelAmount.setMinWidth(100);
		TextField textAmount = new TextField();
		HBox hBoxAmount = new HBox(10, labelAmount, textAmount);

		Button btnReturn = new Button("Submit Return");
		Label lblWarn = new Label();

		btnReturn.setOnAction(e -> {
			SeedRequest request = table.getSelectionModel().getSelectedItem();
			if (request == null) {
				lblWarn.setText("Error: Please select an approved request first.");
				return;
			}

			String amountStr = textAmount.getText().trim();
			if (amountStr.isEmpty()) {
				lblWarn.setText("Error: Amount is required!");
				return;
			}

			try {
				double amount = Double.parseDouble(amountStr);
				if (amount <= 0) {
					lblWarn.setText("Error: Amount must be greater than zero.");
					return;
				}
				if (amount > request.getAmount()) {
					lblWarn.setText("Error: You can only return up to " + request.getAmount() + " kg.");
					return;
				}

				returnData.add(new SeedReturn(nextReturnId++, request.getRequestId(),
						currentUser.getUsername(), request.getLotId(), request.getSeedLabel(),
						round2(amount), "Pending"));

				returnable.remove(request);
				textAmount.clear();
				lblWarn.setText("Return submitted. Please wait for inspection.");
			} catch (NumberFormatException ex) {
				lblWarn.setText("Error: Amount must be a valid number!");
			}
		});

		Label lblInfo = new Label("Only approved seed requests can be returned here.");

		VBox root = new VBox(10, lblInfo, table, hBoxAmount, btnReturn, lblWarn);
		root.setStyle("-fx-padding: 10;");
		return root;
	}

	// Loads approved requests that do not have a pending or accepted return yet
	private void loadReturnableRequests(ObservableList<SeedRequest> list) {
		list.clear();
		for (SeedRequest request : requestData) {
			if (!request.getFarmer().equals(currentUser.getUsername())) {
				continue;
			}
			if (!request.getStatus().equals("Approved")) {
				continue;
			}

			boolean alreadyReturned = false;
			for (SeedReturn ret : returnData) {
				if (ret.getRequestId() == request.getRequestId()
						&& !ret.getStatus().equals("Rejected")) {
					alreadyReturned = true;
					break;
				}
			}

			if (!alreadyReturned) {
				list.add(request);
			}
		}
	}

	// Admin approves a request and deducts the seeds from stock
	private void approveRequest(TableView<SeedRequest> table, Label lblWarn) {
		SeedRequest request = table.getSelectionModel().getSelectedItem();
		if (request == null) {
			lblWarn.setText("Error: Please select a request first.");
			return;
		}
		if (!request.getStatus().equals("Pending")) {
			lblWarn.setText("Error: This request is already " + request.getStatus() + ".");
			return;
		}

		Product product = findProduct(request.getLotId());
		if (product == null) {
			lblWarn.setText("Error: The seed lot no longer exists.");
			return;
		}
		if (product.getAmount() < request.getAmount()) {
			lblWarn.setText("Error: Not enough stock for this request.");
			return;
		}

		product.setAmount(round2(product.getAmount() - request.getAmount()));
		request.setStatus("Approved");
		updateSummaryMetrics();
		table.refresh();
		if (inventoryTable != null) {
			inventoryTable.refresh();
		}
		lblWarn.setText("Request #" + request.getRequestId() + " approved. Stock updated.");
	}

	// Admin rejects a pending request
	private void rejectRequest(TableView<SeedRequest> table, Label lblWarn) {
		SeedRequest request = table.getSelectionModel().getSelectedItem();
		if (request == null) {
			lblWarn.setText("Error: Please select a request first.");
			return;
		}
		if (!request.getStatus().equals("Pending")) {
			lblWarn.setText("Error: This request is already " + request.getStatus() + ".");
			return;
		}

		request.setStatus("Rejected");
		table.refresh();
		lblWarn.setText("Request #" + request.getRequestId() + " rejected.");
	}

	// Admin accepts a return and adds the seeds back to stock
	private void acceptReturn(TableView<SeedReturn> table, Label lblWarn) {
		SeedReturn ret = table.getSelectionModel().getSelectedItem();
		if (ret == null) {
			lblWarn.setText("Error: Please select a return first.");
			return;
		}
		if (!ret.getStatus().equals("Pending")) {
			lblWarn.setText("Error: This return is already " + ret.getStatus() + ".");
			return;
		}

		Product product = findProduct(ret.getLotId());
		if (product == null) {
			lblWarn.setText("Error: The seed lot no longer exists.");
			return;
		}

		product.setAmount(round2(product.getAmount() + ret.getAmount()));
		ret.setStatus("Accepted");
		updateSummaryMetrics();
		table.refresh();
		if (inventoryTable != null) {
			inventoryTable.refresh();
		}
		lblWarn.setText("Return #" + ret.getReturnId() + " accepted. Stock replenished.");
	}

	// Admin rejects a return
	private void rejectReturn(TableView<SeedReturn> table, Label lblWarn) {
		SeedReturn ret = table.getSelectionModel().getSelectedItem();
		if (ret == null) {
			lblWarn.setText("Error: Please select a return first.");
			return;
		}
		if (!ret.getStatus().equals("Pending")) {
			lblWarn.setText("Error: This return is already " + ret.getStatus() + ".");
			return;
		}

		ret.setStatus("Rejected");
		table.refresh();
		lblWarn.setText("Return #" + ret.getReturnId() + " rejected.");
	}

	private Product findProduct(int lotId) {
		for (Product product : productData) {
			if (product.getLotID() == lotId) {
				return product;
			}
		}
		return null;
	}

	private double round2(double value) {
		return Math.round(value * 100.0) / 100.0;
	}

	// Shows the donation form from the landing page
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

				donationData.add(new Donation(name, email, round2(amount), message));
				saveDonations(donationPath, donationData);
				updateDonationTotal();

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

	// Helper method to update summary metrics dynamically
	private void updateSummaryMetrics() {
		int totalVarieties = (int) productData.stream().map(Product::getVariety).distinct().count();
		double totalStocks = productData.stream().mapToDouble(Product::getAmount).sum();
		int totalLots = productData.size();

		lblVarieties.setText("Total Plant Varieties: " + totalVarieties);
		lblStocks.setText("Total Seed Stocks: " + totalStocks);
		lblLots.setText("Total Storage Lots: " + totalLots);
	}

	private void updateDonationTotal() {
		double total = donationData.stream().mapToDouble(Donation::getAmount).sum();
		lblDonationTotal.setText("Total Donations: " + String.format("%,.2f", total));
	}

	// Load products from a file
	private ObservableList<Product> loadProducts(Path path) {
		ObservableList<Product> list = FXCollections.observableArrayList();

		try {
			if (Files.exists(path)) { // Check if file exists
				List<String> records = Files.readAllLines(path); // Iteratively reads file contents and stores in a list
				for (String line : records) {
					if (line.trim().isEmpty()) {
						continue;
					}
					String[] parts = line.split(",");
					list.add(new Product(
							parts[0].trim(),
							Double.parseDouble(parts[1].trim()),
							Double.parseDouble(parts[2].trim()),
							Integer.parseInt(parts[3].trim()),
							parts[4].trim(),
							parts[5].trim(),
							parts[6].trim()));
				}
			}
		} catch (IOException | NumberFormatException e) {
			e.printStackTrace();
		}

		return list;
	}

	// Saves products into a file
	private void saveProducts(Path path, ObservableList<Product> data) {
		try {
			List<String> lines = new ArrayList<>();
			for (Product product : data) { // Iteratively adds products into a list of strings
				lines.add(product.getName() + "," + product.getPrice() + "," + product.getAmount() + ","
						+ product.getLotID() + "," + product.getLocation() + ","
						+ product.getVariety() + "," + product.getDescription());
			}

			// Add to file
			if (Files.exists(path)) {
				try (PrintWriter writer = new PrintWriter(path.toFile())) {
					for (String line : lines) {
						writer.println(line);
					}
				}
			} else {
				Files.write(path, lines); // File does not exist
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Load donations from a file
	private ObservableList<Donation> loadDonations(Path path) {
		ObservableList<Donation> list = FXCollections.observableArrayList();

		try {
			if (Files.exists(path)) {
				List<String> records = Files.readAllLines(path);
				for (String line : records) {
					if (line.trim().isEmpty()) {
						continue;
					}
					String[] parts = line.split(",", 4);
					if (parts.length < 3) {
						continue;
					}
					list.add(new Donation(
							parts[0].trim(),
							parts[1].trim(),
							Double.parseDouble(parts[2].trim()),
							parts.length > 3 ? parts[3].trim() : ""));
				}
			}
		} catch (IOException | NumberFormatException e) {
			e.printStackTrace();
		}

		return list;
	}

	// Saves donations into a file
	private void saveDonations(Path path, ObservableList<Donation> data) {
		try {
			List<String> lines = new ArrayList<>();
			for (Donation donation : data) {
				String message = donation.getMessage().replace("\n", " ").replace("\r", " ");
				lines.add(donation.getDonorName() + "," + donation.getEmail() + ","
						+ donation.getAmount() + "," + message);
			}

			if (Files.exists(path)) {
				try (PrintWriter writer = new PrintWriter(path.toFile())) {
					for (String line : lines) {
						writer.println(line);
					}
				}
			} else {
				Files.write(path, lines);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Adds a new product
	private void createNewProduct() {
		Stage dialogStage = new Stage();
		dialogStage.setTitle("Create New Product");

		// Text Fields
		TextField textName = new TextField();
		TextField textPrice = new TextField();
		TextField textAmount = new TextField();
		TextField textLotID = new TextField();
		TextField textLocation = new TextField();
		TextField textVariety = new TextField();
		TextField textDescription = new TextField();

		// Sets up labels and fits them neatly in Hboxes
		Label labelName = new Label("Name:");
		labelName.setMinWidth(100);
		HBox hBoxName = new HBox(10, labelName, textName);

		Label labelPrice = new Label("Price:");
		labelPrice.setMinWidth(100);
		HBox hBoxPrice = new HBox(10, labelPrice, textPrice);

		Label labelAmount = new Label("Amount:");
		labelAmount.setMinWidth(100);
		HBox hBoxAmount = new HBox(10, labelAmount, textAmount);

		Label labelLotID = new Label("Lot ID:");
		labelLotID.setMinWidth(100);
		HBox hBoxLotID = new HBox(10, labelLotID, textLotID);

		Label labelLocation = new Label("Location:");
		labelLocation.setMinWidth(100);
		HBox hBoxLocation = new HBox(10, labelLocation, textLocation);

		Label labelVariety = new Label("Variety:");
		labelVariety.setMinWidth(100);
		HBox hBoxVariety = new HBox(10, labelVariety, textVariety);

		Label labelDescription = new Label("Description:");
		labelDescription.setMinWidth(100);
		HBox hBoxDescription = new HBox(10, labelDescription, textDescription);

		Button btnCreate = new Button("Create Product");
		Label labelWarn = new Label();

		// Arrange
		VBox vbox = new VBox(10, hBoxName, hBoxPrice, hBoxAmount, hBoxLotID, hBoxLocation,
				hBoxVariety, hBoxDescription, labelWarn, btnCreate);
		vbox.setStyle("-fx-padding: 15;");

		// Button for creating products
		btnCreate.setOnAction(e -> {
			String name = textName.getText().trim();
			String priceStr = textPrice.getText().trim();
			String amountStr = textAmount.getText().trim();
			String lotIDStr = textLotID.getText().trim();
			String location = textLocation.getText().trim();
			String variety = textVariety.getText().trim();
			String description = textDescription.getText().trim();

			// Make sure required text fields are full
			if (name.isEmpty() || priceStr.isEmpty() || amountStr.isEmpty() || lotIDStr.isEmpty()
					|| location.isEmpty() || variety.isEmpty() || description.isEmpty()) {
				labelWarn.setText("Error: All fields are required!");
				return;
			}

			try {
				double price = Double.parseDouble(priceStr);
				double amount = Double.parseDouble(amountStr);
				int lotID = Integer.parseInt(lotIDStr);

				// Add data
				productData.add(new Product(name, price, amount, lotID, location, variety, description));
				dialogStage.close();
			} catch (NumberFormatException ex) {
				labelWarn.setText("Error: Price, Amount, and Lot ID must be valid numbers!");
			}
		});

		// Sets up scene
		Scene scene = new Scene(vbox, 400, 450);
		dialogStage.setScene(scene);
		dialogStage.showAndWait();
	}

	// Opens product details window with Edit and Delete buttons
	private void openProductDetailDialog(Product product) {
		Stage detailStage = new Stage();
		detailStage.setTitle("Product Details");

		Label labelName = new Label("Name: " + product.getName());
		Label labelPrice = new Label("Price: " + product.getPrice());
		Label labelAmount = new Label("Amount: " + product.getAmount());
		Label labelLotID = new Label("Lot ID: " + product.getLotID());
		Label labelLocation = new Label("Location: " + product.getLocation());
		Label labelVariety = new Label("Variety: " + product.getVariety());
		Label labelDescription = new Label("Description: " + product.getDescription());

		Button btnEdit = new Button("Edit");
		btnEdit.setOnAction(e -> {
			openEditProductDialog(product, detailStage);
		});

		Button btnDelete = new Button("Delete");
		btnDelete.setOnAction(e -> {
			productData.remove(product);
			detailStage.close();
		});

		HBox hBoxButtons = new HBox(10, btnEdit, btnDelete);

		VBox vbox = new VBox(10, labelName, labelPrice, labelAmount, labelLotID,
				labelLocation, labelVariety, labelDescription, hBoxButtons);
		vbox.setStyle("-fx-padding: 15;");

		Scene scene = new Scene(vbox, 350, 300);
		detailStage.setScene(scene);
		detailStage.showAndWait();
	}

	// Opens a fully functional edit form pre-populated with existing product data
	private void openEditProductDialog(Product product, Stage detailStage) {
		Stage editStage = new Stage();
		editStage.setTitle("Edit Product");

		TextField textName = new TextField(product.getName());
		TextField textPrice = new TextField(String.valueOf(product.getPrice()));
		TextField textAmount = new TextField(String.valueOf(product.getAmount()));
		TextField textLotID = new TextField(String.valueOf(product.getLotID()));
		TextField textLocation = new TextField(product.getLocation());
		TextField textVariety = new TextField(product.getVariety());
		TextField textDescription = new TextField(product.getDescription());

		Label labelName = new Label("Name:");
		labelName.setMinWidth(100);
		HBox hBoxName = new HBox(10, labelName, textName);

		Label labelPrice = new Label("Price:");
		labelPrice.setMinWidth(100);
		HBox hBoxPrice = new HBox(10, labelPrice, textPrice);

		Label labelAmount = new Label("Amount:");
		labelAmount.setMinWidth(100);
		HBox hBoxAmount = new HBox(10, labelAmount, textAmount);

		Label labelLotID = new Label("Lot ID:");
		labelLotID.setMinWidth(100);
		HBox hBoxLotID = new HBox(10, labelLotID, textLotID);

		Label labelLocation = new Label("Location:");
		labelLocation.setMinWidth(100);
		HBox hBoxLocation = new HBox(10, labelLocation, textLocation);

		Label labelVariety = new Label("Variety:");
		labelVariety.setMinWidth(100);
		HBox hBoxVariety = new HBox(10, labelVariety, textVariety);

		Label labelDescription = new Label("Description:");
		labelDescription.setMinWidth(100);
		HBox hBoxDescription = new HBox(10, labelDescription, textDescription);

		Button btnSave = new Button("Save Changes");
		Label labelWarn = new Label();

		VBox vbox = new VBox(10, hBoxName, hBoxPrice, hBoxAmount, hBoxLotID, hBoxLocation,
				hBoxVariety, hBoxDescription, labelWarn, btnSave);
		vbox.setStyle("-fx-padding: 15;");

		btnSave.setOnAction(e -> {
			String name = textName.getText().trim();
			String priceStr = textPrice.getText().trim();
			String amountStr = textAmount.getText().trim();
			String lotIDStr = textLotID.getText().trim();
			String location = textLocation.getText().trim();
			String variety = textVariety.getText().trim();
			String description = textDescription.getText().trim();

			if (name.isEmpty() || priceStr.isEmpty() || amountStr.isEmpty() || lotIDStr.isEmpty()
					|| location.isEmpty() || variety.isEmpty() || description.isEmpty()) {
				labelWarn.setText("Error: All fields are required!");
				return;
			}

			try {
				double price = Double.parseDouble(priceStr);
				double amount = Double.parseDouble(amountStr);
				int lotID = Integer.parseInt(lotIDStr);

				// Update product object attributes
				product.setName(name);
				product.setPrice(price);
				product.setAmount(amount);
				product.setLotID(lotID);
				product.setLocation(location);
				product.setVariety(variety);
				product.setDescription(description);

				// Refresh table data and metrics
				int index = productData.indexOf(product);
				if (index >= 0) {
					productData.set(index, product);
				}

				editStage.close();
				detailStage.close();
			} catch (NumberFormatException ex) {
				labelWarn.setText("Error: Price, Amount, and Lot ID must be valid numbers!");
			}
		});

		Scene scene = new Scene(vbox, 400, 450);
		editStage.setScene(scene);
		editStage.showAndWait();
	}

	public static void main(String[] args) {
		launch();
	}
}
