package application.shared;

import application.*;
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

public class AppState {

    public User currentUser;
    public ObservableList<Product> productData;
    public ObservableList<SeedRequest> requestData = FXCollections.observableArrayList();
    public ObservableList<SeedReturn> returnData = FXCollections.observableArrayList();
    public ObservableList<Donation> donationData = FXCollections.observableArrayList();
    public final Path productPath = Paths.get("products.txt");
    public final Path donationPath = Paths.get("donations.txt");
    public TableView<Product> inventoryTable;
    public int nextRequestId = 1;
    public int nextReturnId = 1;
    public final List<User> users = List.of(
            new User("admin", "admin123", "Admin"),
            new User("juan", "farmer123", "Farmer"),
            new User("maria", "farmer123", "Farmer"));

    public Label lblVarieties = new Label("Total Plant Varieties: 0");
    public Label lblStocks = new Label("Total Seed Stocks: 0.0");
    public Label lblLots = new Label("Total Storage Lots: 0");
    public Label lblDonationTotal = new Label("Total Donations: 0.00");

    public void initialize() {
        productData = loadProducts(productPath);
        donationData = loadDonations(donationPath);
        productData.addListener((ListChangeListener.Change<? extends Product> c) -> updateSummaryMetrics());
        donationData.addListener((ListChangeListener.Change<? extends Donation> c) -> updateDonationTotal());
        updateSummaryMetrics();
        updateDonationTotal();
    }


	public Product findProduct(int lotId) {
		for (Product product : productData) {
			if (product.getLotID() == lotId) {
				return product;
			}
		}
		return null;
	}

	public double round2(double value) {
		return Math.round(value * 100.0) / 100.0;
	}

	public void updateSummaryMetrics() {
		int totalVarieties = (int) productData.stream().map(Product::getVariety).distinct().count();
		double totalStocks = productData.stream().mapToDouble(Product::getAmount).sum();
		int totalLots = productData.size();

		lblVarieties.setText("Total Plant Varieties: " + totalVarieties);
		lblStocks.setText("Total Seed Stocks: " + totalStocks);
		lblLots.setText("Total Storage Lots: " + totalLots);
	}

	public void updateDonationTotal() {
		double total = donationData.stream().mapToDouble(Donation::getAmount).sum();
		lblDonationTotal.setText("Total Donations: " + String.format("%,.2f", total));
	}

	public ObservableList<Product> loadProducts(Path path) {
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

	public void saveProducts(Path path, ObservableList<Product> data) {
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

	public ObservableList<Donation> loadDonations(Path path) {
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

	public void saveDonations(Path path, ObservableList<Donation> data) {
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
}
