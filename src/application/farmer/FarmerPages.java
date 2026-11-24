package application.farmer;

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

public class FarmerPages {
    private final Stage mainStage;
    private final AppState state;
    private final Runnable onLogout;

    public FarmerPages(Stage stage, AppState state, Runnable onLogout) {
        this.mainStage = stage;
        this.state = state;
        this.onLogout = onLogout;
    }

	public void showFarmerDashboard() {
		Label lblUser = new Label("Logged in as: " + state.currentUser.getUsername() + " (Farmer)");
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
			state.currentUser = null;
			onLogout.run();
		});

		BorderPane root = new BorderPane();
		root.setTop(topBar);
		root.setCenter(tabs);

		mainStage.setScene(new Scene(root, 1000, 650));
		mainStage.setTitle("Seed Spreaders - Farmer");
	}

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
		table.setItems(state.productData);

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

				state.requestData.add(new SeedRequest(state.nextRequestId++, state.currentUser.getUsername(),
						product.getLotID(), product.getName(), product.getVariety(),
						state.round2(amount), purpose, "Pending"));

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

	private VBox createFarmerTransactionsPane() {
		FilteredList<SeedRequest> myRequests = new FilteredList<>(state.requestData,
				request -> request.getFarmer().equals(state.currentUser.getUsername()));

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

		FilteredList<SeedReturn> myReturns = new FilteredList<>(state.returnData,
				ret -> ret.getFarmer().equals(state.currentUser.getUsername()));

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

				state.returnData.add(new SeedReturn(state.nextReturnId++, request.getRequestId(),
						state.currentUser.getUsername(), request.getLotId(), request.getSeedLabel(),
						state.round2(amount), "Pending"));

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

	private void loadReturnableRequests(ObservableList<SeedRequest> list) {
		list.clear();
		for (SeedRequest request : state.requestData) {
			if (!request.getFarmer().equals(state.currentUser.getUsername())) {
				continue;
			}
			if (!request.getStatus().equals("Approved")) {
				continue;
			}

			boolean alreadyReturned = false;
			for (SeedReturn ret : state.returnData) {
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
}
