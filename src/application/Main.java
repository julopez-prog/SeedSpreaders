/*
- @author 
- Paolo Jay A. Labindalawa
- Axel Andre B. Lao 
- Jared U. Lopez
- @date 11/07/26 23:30
- 
- SeedSpreaders Milestone 2
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
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main extends Application {
	private ObservableList<Product> productData;
	private final Path filePath = Paths.get("products.txt");

	// Summary labels for the top portion
	private Label lblVarieties = new Label("Total Plant Varieties: 0");
	private Label lblStocks = new Label("Total Seed Stocks: 0.0");
	private Label lblLots = new Label("Total Storage Lots: 0");

	@Override
	public void start(Stage stage) {
		// Table
		TableView<Product> table = new TableView<>();
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		
		TableColumn<Product, Integer> colLotID       = new TableColumn<>("Lot ID");
		TableColumn<Product, String> colVariety     = new TableColumn<>("Variety");
		TableColumn<Product, String> colLocation    = new TableColumn<>("Location");
		TableColumn<Product, Double> colAmount      = new TableColumn<>("Amount");
		TableColumn<Product, String> colName        = new TableColumn<>("Name");
		TableColumn<Product, Double> colPrice       = new TableColumn<>("Price");
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
			colLotID, colVariety, colLocation, colAmount, colName, colPrice, colDescription, colAction
		);

		// Load data from file
		productData = loadProducts(filePath);
		table.setItems(productData);

		// Listen for changes in productData to dynamically update summary metrics
		productData.addListener((javafx.collections.ListChangeListener.Change<? extends Product> c) -> {
			updateSummaryMetrics();
		});
		updateSummaryMetrics(); // Initial calculation
		
		// Buttons (only Add Inventory and Save & Quit on top bar)
		Button btnProduct = new Button("Create New Product");
		Button btnSaveQuit = new Button("Save and Quit");

		// Button actions
		btnProduct.setOnAction(e -> {
			createNewProduct();
		});
		
		btnSaveQuit.setOnAction(e -> {
			saveProducts(filePath, productData);
			stage.close();
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
		root.setCenter(table);
		
		// Set Stage
		Scene scene = new Scene(root, 1020, 360);
		stage.setScene(scene);
		stage.setTitle("Product Inventory Management");
		stage.show();
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

	// Load products from a file
	private ObservableList<Product> loadProducts(Path path) {
		ObservableList<Product> list = FXCollections.observableArrayList();
		
		try {
			if (Files.exists(path)) { // Check if file exists
				List<String> records = Files.readAllLines(path); // Iteratively reads file contents and stores in a list
				for (String line : records) {
					String[] parts = line.split(",");
					list.add(new Product(
						parts[0].trim(),
						Double.parseDouble(parts[1].trim()),
						Double.parseDouble(parts[2].trim()),
						Integer.parseInt(parts[3].trim()),
						parts[4].trim(),
						parts[5].trim(),
						parts[6].trim()
					));
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
				lines.add(product.getName() + "," + product.getPrice() + "," + product.getAmount() + "," + product.getLotID() + "," + product.getLocation() + "," + product.getVariety() + "," + product.getDescription());
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
		VBox vbox = new VBox(10, hBoxName, hBoxPrice, hBoxAmount, hBoxLotID, hBoxLocation, hBoxVariety, hBoxDescription, labelWarn, btnCreate);
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
			if (name.isEmpty() || priceStr.isEmpty() || amountStr.isEmpty() || lotIDStr.isEmpty() || location.isEmpty() || variety.isEmpty() || description.isEmpty()) {
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

		VBox vbox = new VBox(10, labelName, labelPrice, labelAmount, labelLotID, labelLocation, labelVariety, labelDescription, hBoxButtons);
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

		VBox vbox = new VBox(10, hBoxName, hBoxPrice, hBoxAmount, hBoxLotID, hBoxLocation, hBoxVariety, hBoxDescription, labelWarn, btnSave);
		vbox.setStyle("-fx-padding: 15;");

		btnSave.setOnAction(e -> {
			String name = textName.getText().trim();
			String priceStr = textPrice.getText().trim();
			String amountStr = textAmount.getText().trim();
			String lotIDStr = textLotID.getText().trim();
			String location = textLocation.getText().trim();
			String variety = textVariety.getText().trim();
			String description = textDescription.getText().trim();

			if (name.isEmpty() || priceStr.isEmpty() || amountStr.isEmpty() || lotIDStr.isEmpty() || location.isEmpty() || variety.isEmpty() || description.isEmpty()) {
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