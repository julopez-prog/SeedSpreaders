package application.admin;

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



public class AdminPages {
    private final Stage mainStage;
    private final AppState state;
    private final Runnable onLogout;

    public AdminPages(Stage stage, AppState state, Runnable onLogout) {
        this.mainStage = stage;
        this.state = state;
        this.onLogout = onLogout;
    }

	public void showAdminDashboard() {
		Label lblUser = new Label("Logged in as: " + state.currentUser.getUsername() + " (Admin)");
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
			state.saveProducts(state.productPath, state.productData);
			state.currentUser = null;
			onLogout.run();
		});

		BorderPane root = new BorderPane();
		root.setTop(topBar);
		root.setCenter(tabs);

		mainStage.setScene(new Scene(root, 1100, 650));
		mainStage.setTitle("Seed Spreaders - Admin");
	}

	private BorderPane createInventoryPane() {
		// Table
		TableView<Product> table = new TableView<>();
		state.inventoryTable = table;
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
		table.setItems(state.productData);

		// Buttons (only Add Inventory and Save & Quit on top bar)
		Button btnProduct = new Button("Create New Product");
		Button btnSaveQuit = new Button("Save and Quit");

		btnProduct.setOnAction(e -> {
			createNewProduct();
		});

		btnSaveQuit.setOnAction(e -> {
			state.saveProducts(state.productPath, state.productData);
			state.currentUser = null;
			onLogout.run();
		});

		// Summary bar layout container
		HBox summaryBar = new HBox(20, state.lblVarieties, state.lblStocks, state.lblLots);
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
		requestTable.setItems(state.requestData);
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
		returnTable.setItems(state.returnData);
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
		table.setItems(state.donationData);
		table.setPlaceholder(new Label("No donations yet."));

		state.updateDonationTotal();

		VBox root = new VBox(10, table, state.lblDonationTotal);
		root.setStyle("-fx-padding: 10;");
		return root;
	}

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

		Product product = state.findProduct(request.getLotId());
		if (product == null) {
			lblWarn.setText("Error: The seed lot no longer exists.");
			return;
		}
		if (product.getAmount() < request.getAmount()) {
			lblWarn.setText("Error: Not enough stock for this request.");
			return;
		}

		product.setAmount(state.round2(product.getAmount() - request.getAmount()));
		request.setStatus("Approved");
		state.updateSummaryMetrics();
		table.refresh();
		if (state.inventoryTable != null) {
			state.inventoryTable.refresh();
		}
		lblWarn.setText("Request #" + request.getRequestId() + " approved. Stock updated.");
	}

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

		Product product = state.findProduct(ret.getLotId());
		if (product == null) {
			lblWarn.setText("Error: The seed lot no longer exists.");
			return;
		}

		product.setAmount(state.round2(product.getAmount() + ret.getAmount()));
		ret.setStatus("Accepted");
		state.updateSummaryMetrics();
		table.refresh();
		if (state.inventoryTable != null) {
			state.inventoryTable.refresh();
		}
		lblWarn.setText("Return #" + ret.getReturnId() + " accepted. Stock replenished.");
	}

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
				state.productData.add(new Product(name, price, amount, lotID, location, variety, description));
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
			state.productData.remove(product);
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
				int index = state.productData.indexOf(product);
				if (index >= 0) {
					state.productData.set(index, product);
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
}
