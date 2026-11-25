package application.shared; // shared state and ui helpers package

import application.Donation; // donation entity model
import application.Product; // seed inventory entity model
import application.SeedRequest; // seed request entity model
import application.SeedReturn; // seed return entity model
import application.User; // user account entity model
import javafx.collections.FXCollections; // observable list factory
import javafx.collections.ListChangeListener; // listener for automatic metric refresh
import javafx.collections.ObservableList; // reactive list backing tables
import javafx.geometry.Insets; // padding helper
import javafx.geometry.Pos; // alignment helper
import javafx.scene.Node; // base javafx scene node
import javafx.scene.Scene; // modal scene container
import javafx.scene.control.Button; // button control
import javafx.scene.control.Label; // text label control
import javafx.scene.control.TableCell; // custom table cell for status pills
import javafx.scene.control.TableColumn; // table column definition
import javafx.scene.control.TableView; // table view reference
import javafx.scene.image.Image; // image loader
import javafx.scene.image.ImageView; // image renderer
import javafx.scene.layout.HBox; // horizontal box container
import javafx.scene.layout.VBox; // vertical box container
import javafx.stage.Modality; // modal window behavior
import javafx.stage.Stage; // stage window
import javafx.stage.StageStyle; // stage border style
import javafx.util.Callback; // cell factory callback

import java.io.IOException; // file io exception
import java.io.PrintWriter; // text file writer
import java.net.URL; // resource url resolver
import java.nio.file.Files; // file existence and reading helpers
import java.nio.file.Path; // file path abstraction
import java.nio.file.Paths; // path constructor
import java.time.LocalDate; // date utilities for due dates
import java.util.ArrayList; // resizable list for serialization
import java.util.List; // list interface

// holds shared application data, persistence methods, and reusable ui helpers
public class AppState { // declare appstate class

	public static final double MAX_LOAN_VALUE_PHP = 10000.0; // max php seed loan per farm
	public static final String LOGO_ICON_PATH = "/application/assets/seed-spreaders-icon.png"; // green icon for light headers
	public static final String LOGO_HERO_PATH = "/application/assets/seed-spreaders-hero.png"; // white mark for dark backgrounds

	public static final String FOREST_DARK = "#0c3026"; // dark forest sidebar and hero background
	public static final String PRIMARY_GREEN = "#17684d"; // primary brand button green
	public static final String SAGE_ACCENT = "#77a589"; // muted sage subtitle accent
	public static final String BG_PAGE = "#f6f8f3"; // warm off-white page background
	public static final String BORDER_COLOR = "#dce4de"; // subtle border gray-green
	public static final String TEXT_DARK = "#18241e"; // primary dark text color
	public static final String TEXT_MUTED = "#5a6e64"; // secondary muted text color

	public Stage mainStage; // reference to primary application stage
	public User currentUser; // currently logged-in user or null when logged out

	public ObservableList<User> users = FXCollections.observableArrayList(); // all registered admin and farmer accounts
	public ObservableList<Product> productData = FXCollections.observableArrayList(); // all seed lots in inventory
	public ObservableList<SeedRequest> requestData = FXCollections.observableArrayList(); // all seed loan requests
	public ObservableList<SeedReturn> returnData = FXCollections.observableArrayList(); // all harvest seed returns
	public ObservableList<Donation> donationData = FXCollections.observableArrayList(); // all community donations

	public final Path userPath = Paths.get("users.txt"); // flat file storing user accounts
	public final Path productPath = Paths.get("products.txt"); // flat file storing seed inventory
	public final Path requestPath = Paths.get("requests.txt"); // flat file storing seed requests
	public final Path returnPath = Paths.get("returns.txt"); // flat file storing seed returns
	public final Path donationPath = Paths.get("donations.txt"); // flat file storing donations

	public TableView<Product> inventoryTable; // active inventory table reference for live refresh
	public int nextRequestId = 1; // auto-increment counter for request ids
	public int nextReturnId = 1; // auto-increment counter for return ids

	public Label lblVarieties = new Label("0"); // live variety count label
	public Label lblStocks = new Label("0.0 kg"); // live total stock weight label
	public Label lblLots = new Label("0"); // live lot count label
	public Label lblDonationTotal = new Label("Total Donations: \u20b10.00"); // live donation total label

	private Image cachedIconImage; // cached green seedling icon
	private Image cachedHeroImage; // cached white hero logo mark

	// initialize state, load files, and attach live listeners
	public void initialize(Stage stage) { // define initialize method
		this.mainStage = stage; // store primary stage reference
		users = loadUsers(userPath); // load accounts from users.txt
		if (users.isEmpty()) { // seed default accounts if first run
			seedDefaultUsers(); // populate default admin and farmers
			saveUsers(); // persist default accounts to disk
		}

		productData = loadProducts(productPath); // load inventory from products.txt
		if (productData.isEmpty()) { // seed laguna crop catalog if first run
			seedDefaultProducts(); // populate okra, tomato, squash, pineapple, and rice seeds
			saveProducts(); // persist default products to disk
		}

		requestData = loadRequests(requestPath); // load seed requests from requests.txt
		returnData = loadReturns(returnPath); // load seed returns from returns.txt
		if (requestData.isEmpty() && returnData.isEmpty()) { // seed sample transactions if first run
			seedDefaultTransactions(); // create sample active, overdue, and pending records
			saveRequests(); // persist sample requests
			saveReturns(); // persist sample returns
		}

		for (SeedRequest r : requestData) { // compute next available request id
			if (r.getRequestId() >= nextRequestId) { // check highest existing id
				nextRequestId = r.getRequestId() + 1; // advance counter past existing id
			}
		}
		for (SeedReturn r : returnData) { // compute next available return id
			if (r.getReturnId() >= nextReturnId) { // check highest existing id
				nextReturnId = r.getReturnId() + 1; // advance counter past existing id
			}
		}

		donationData = loadDonations(donationPath); // load donations from donations.txt
		if (donationData.isEmpty()) { // seed sample community donation if empty
			donationData.add(new Donation("Elena Santos", "elena.santos@email.ph", 1500.00, "Para sa mga magsasaka ng Laguna!")); // sample donation entry
			saveDonations(); // persist sample donation
		}

		productData.addListener((ListChangeListener.Change<? extends Product> c) -> updateSummaryMetrics()); // refresh metrics when inventory changes
		donationData.addListener((ListChangeListener.Change<? extends Donation> c) -> updateDonationTotal()); // refresh total when donations change
		updateSummaryMetrics(); // compute initial inventory totals
		updateDonationTotal(); // compute initial donation total
	}

	// populate initial admin and farmer accounts
	private void seedDefaultUsers() { // define seeddefaultusers helper
		users.add(new User("USR-001", "admin", "admin123", "Admin", "Seed Bank Administrator", "09171234567", "admin@seedspreaders.ph", "Laguna Central Seed Vault", "Los Banos, Laguna", "RSBSA-04-34-0001", 0.0, true, false, "")); // default admin account
		users.add(new User("USR-002", "juan", "farmer123", "Farmer", "Juan Dela Cruz", "09182345678", "juan.delacruz@farm.ph", "Dela Cruz Family Farm", "Brgy. San Antonio, Los Banos, Laguna", "RSBSA-04-34-1042", 2.5, true, false, "")); // verified farmer account
		users.add(new User("USR-003", "maria", "farmer123", "Farmer", "Maria Clara Reyes", "09193456789", "maria.reyes@farm.ph", "Reyes Upland Harvest", "Brgy. Timugan, Los Banos, Laguna", "RSBSA-04-34-1088", 1.8, true, false, "")); // verified farmer account
		users.add(new User("USR-004", "pedro", "farmer123", "Farmer", "Pedro Penduko", "09204567890", "pedro.penduko@farm.ph", "Penduko Agri Lot", "Brgy. Mayondon, Los Banos, Laguna", "RSBSA-04-34-1105", 1.2, true, true, "Unsettled seed loan from previous harvest cycle.")); // suspended farmer example
	}

	// populate initial seed lots modeled after laguna agricultural commodities
	private void seedDefaultProducts() { // define seeddefaultproducts helper
		productData.add(new Product("Okra Seeds", 320.00, 45.0, 101, "Vault A - Bin 1", "Abelmoschus esculentus (Smooth Green)", "Top-ranked Laguna vegetable crop; fast-maturing edible pods rich in fiber and vitamins.")); // okra lot
		productData.add(new Product("Tomato Seeds", 450.00, 38.0, 102, "Vault A - Bin 2", "Solanum lycopersicum (Diamante Max)", "High-demand Laguna commercial tomato variety suitable for fresh markets and sauces.")); // tomato lot
		productData.add(new Product("Squash Seeds", 280.00, 52.0, 103, "Vault A - Bin 3", "Cucurbita moschata (Suprema Kalabasa)", "Nutrient-dense Laguna squash variety rich in beta-carotene for household and market sales.")); // squash lot
		productData.add(new Product("Pineapple Planting Slips", 150.00, 120.0, 104, "Nursery Bay B1", "Ananas comosus (Queen Formosa)", "Vegetative slips and crowns cultivated across Laguna for fresh fruit and processing.")); // pineapple lot
		productData.add(new Product("Rice Seeds", 65.00, 250.0, 105, "Silo C - Bay 1", "Oryza sativa (NSIC Rc 222)", "Certified inbred palay seeds supporting Laguna staple rice production.")); // rice lot
	}

	// populate sample requests and returns so dashboards show realistic states
	private void seedDefaultTransactions() { // define seeddefaulttransactions helper
		String today = LocalDate.now().toString(); // current date string
		String pastDue = LocalDate.now().minusDays(6).toString(); // 6 days overdue date
		String upcomingDue = LocalDate.now().plusDays(10).toString(); // upcoming return deadline
		requestData.add(new SeedRequest(1, "juan", 101, "Okra Seeds", "Abelmoschus esculentus (Smooth Green)", 5.0, 1600.00, "Dry season backyard plot planting", "Approved", LocalDate.now().minusDays(40).toString(), pastDue)); // overdue active loan for juan
		requestData.add(new SeedRequest(2, "maria", 105, "Rice Seeds", "Oryza sativa (NSIC Rc 222)", 40.0, 2600.00, "Main wet-season paddy replanting", "Approved", LocalDate.now().minusDays(20).toString(), upcomingDue)); // active loan for maria
		requestData.add(new SeedRequest(3, "juan", 102, "Tomato Seeds", "Solanum lycopersicum (Diamante Max)", 3.0, 1350.00, "Market garden expansion", "Pending", today, "")); // pending request for admin review
		returnData.add(new SeedReturn(1, 2, "maria", 105, "Rice Seeds - Oryza sativa (NSIC Rc 222)", 42.0, "Pending", today)); // pending harvest return with seed interest
	}

	// load an image resource from classpath safely
	public Image loadImage(String resourcePath) { // define loadimage method
		try { // attempt risky block
			URL url = AppState.class.getResource(resourcePath); // locate image inside classpath
			if (url != null) { // check that resource exists
				return new Image(url.toExternalForm()); // load javafx image
			}
		} catch (Exception e) { // handle exception
			System.err.println("Could not load image: " + resourcePath); // log missing image path
		}
		return null; // return null if resource was missing
	}

	// build the brand logo lockup using the real png assets
	public Node buildLogo(double scale, boolean darkBackground) { // define buildlogo method
		if (cachedIconImage == null) { // lazy-load green seedling icon once
			cachedIconImage = loadImage(LOGO_ICON_PATH); // load green icon png
		}
		if (cachedHeroImage == null) { // lazy-load white logo mark once
			cachedHeroImage = loadImage(LOGO_HERO_PATH); // load white mark png
		}

		Image chosen = darkBackground ? cachedHeroImage : cachedIconImage; // choose white mark on dark green or green icon on light surface
		HBox lockup = new HBox(10 * scale); // horizontal container for icon and brand text
		lockup.setAlignment(Pos.CENTER_LEFT); // vertically center icon and text

		if (chosen != null) { // attach image view when png loaded cleanly
			ImageView view = new ImageView(chosen); // create image view
			view.setFitHeight(44 * scale); // scale logo height proportionally
			view.setPreserveRatio(true); // keep original aspect ratio
			view.setSmooth(true); // enable smooth scaling
			lockup.getChildren().add(view); // add logo graphic to lockup
		}

		Label lblBrand = new Label("Seed Spreaders."); // main organization title
		lblBrand.setStyle("-fx-font-size: " + Math.round(18 * scale) + "px; -fx-font-weight: bold; -fx-text-fill: " + (darkBackground ? "#ffffff" : FOREST_DARK) + ";"); // white on dark background, forest green on light

		Label lblSub = new Label("SEED BANK SYSTEM"); // uppercase system subtitle
		lblSub.setStyle("-fx-font-size: " + Math.max(8, Math.round(9 * scale)) + "px; -fx-font-weight: bold; -fx-text-fill: " + (darkBackground ? "#8ec9a4" : "#6d8379") + ";"); // sage green subtitle

		VBox textStack = new VBox(1 * scale, lblBrand, lblSub); // stack title and subtitle
		textStack.setAlignment(Pos.CENTER_LEFT); // align text left
		lockup.getChildren().add(textStack); // add text stack beside icon
		return lockup; // return complete logo lockup
	}

	// find a seed product lot by its numeric lot id
	public Product findProduct(int lotId) { // define findproduct method
		for (Product product : productData) { // loop through inventory lots
			if (product.getLotID() == lotId) { // check matching lot id
				return product; // return matching lot
			}
		}
		return null; // return null if lot id not found
	}

	// find a user account by username (case-insensitive)
	public User findUserByUsername(String username) { // define finduserbyusername method
		for (User u : users) { // loop through registered users
			if (u.getUsername().equalsIgnoreCase(username)) { // compare username ignoring case
				return u; // return matching user
			}
		}
		return null; // return null if not found
	}

	// generate the next sequential user id such as USR-005
	public String generateNextUserId() { // define generatenextuserid method
		int max = 0; // track highest numeric suffix
		for (User u : users) { // scan existing user ids
			String id = u.getUserId(); // read user id string
			if (id != null && id.startsWith("USR-")) { // parse standard USR-XXX format
				try { // attempt risky block
					int num = Integer.parseInt(id.substring(4)); // extract numeric portion
					if (num > max) { // update max if higher
						max = num; // store new max
					}
				} catch (NumberFormatException ignored) { // handle exception
				}
			}
		}
		return String.format("USR-%03d", max + 1); // format next 3-digit user id
	}

	// round a decimal number to two decimal places
	public double round2(double value) { // define round2 method
		return Math.round(value * 100.0) / 100.0; // standard 2-decimal rounding
	}

	// compute total active loan value in php for a given farmer
	public double getFarmerActiveLoanValue(String username) { // define getfarmeractiveloanvalue method
		double sum = 0.0; // accumulate active loan php value
		for (SeedRequest r : requestData) { // inspect all seed requests
			if (r.getFarmer().equalsIgnoreCase(username) && ("Approved".equalsIgnoreCase(r.getStatus()) || "Pending".equalsIgnoreCase(r.getStatus()))) { // include pending and active loans
				double val = r.getTotalValue(); // read stored php value
				if (val <= 0) { // fallback calculation from current product price
					Product p = findProduct(r.getLotId()); // look up product lot
					if (p != null) { // multiply weight by unit price
						val = round2(r.getAmount() * p.getPrice()); // compute php value
					}
				}
				sum += val; // add to farmer total
			}
		}
		return round2(sum); // return rounded php total
	}

	// recompute inventory summary labels
	public void updateSummaryMetrics() { // define updatesummarymetrics method
		int totalVarieties = (int) productData.stream().map(Product::getVariety).distinct().count(); // count unique seed varieties
		double totalStocks = productData.stream().mapToDouble(Product::getAmount).sum(); // sum available kilograms across all lots
		int totalLots = productData.size(); // count total inventory lots

		lblVarieties.setText(String.valueOf(totalVarieties)); // update varieties label
		lblStocks.setText(String.format("%.1f kg", totalStocks)); // update stock weight label
		lblLots.setText(String.valueOf(totalLots)); // update lot count label
	}

	// recompute total donations label
	public void updateDonationTotal() { // define updatedonationtotal method
		double total = donationData.stream().mapToDouble(Donation::getAmount).sum(); // sum all donation amounts
		lblDonationTotal.setText("Total Donations: \u20b1" + String.format("%,.2f", total)); // format peso total
	}

	// shared css style for primary forest-green action buttons
	public String primaryButtonStyle() { // define primarybuttonstyle method
		return "-fx-background-color: #17684d; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 12px; -fx-background-radius: 8; -fx-padding: 9 16 9 16; -fx-cursor: hand;"; // primary button css
	}

	// shared css style for secondary outline buttons
	public String outlineButtonStyle() { // define outlinebuttonstyle method
		return "-fx-background-color: #ffffff; -fx-text-fill: #17684d; -fx-font-weight: bold; -fx-font-size: 12px; -fx-border-color: #cbd8d0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 14 8 14; -fx-cursor: hand;"; // outline button css
	}

	// shared css style for destructive or suspend buttons
	public String dangerButtonStyle() { // define dangerbuttonstyle method
		return "-fx-background-color: #fff5f5; -fx-text-fill: #c0392b; -fx-font-weight: bold; -fx-font-size: 12px; -fx-border-color: #f1c0bb; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 14 8 14; -fx-cursor: hand;"; // danger button css
	}

	// shared css style for text inputs and combo boxes
	public String inputStyle() { // define inputstyle method
		return "-fx-background-color: #ffffff; -fx-border-color: #d6e0da; -fx-border-radius: 7; -fx-background-radius: 7; -fx-padding: 7 11 7 11; -fx-font-size: 12px; -fx-text-fill: #18241e;"; // standard field css
	}

	// shared css style for white dashboard cards
	public String cardStyle() { // define cardstyle method
		return "-fx-background-color: #ffffff; -fx-border-color: #e3ebe6; -fx-border-radius: 12; -fx-background-radius: 12; -fx-padding: 18;"; // white card css
	}

	// create a colored status badge cell factory for table columns
	public <T> Callback<TableColumn<T, String>, TableCell<T, String>> createStatusBadgeCell() { // define createstatusbadgecell method
		return col -> new TableCell<>() { // pass value
			@Override
			protected void updateItem(String item, boolean empty) { // render pill badge per cell value
				super.updateItem(item, empty); // call superclass update
				if (empty || item == null || item.isBlank()) { // clear empty cells
					setGraphic(null); // remove graphic
					setText(null); // remove text
					return; // exit early
				}
				Label badge = new Label(item); // create pill label
				String upper = item.toUpperCase(); // normalize status text for color matching
				String bg = "#edf2ef"; // default neutral gray background
				String fg = "#3c4f46"; // default neutral text
				if (upper.contains("APPROVED") || upper.contains("VERIFIED") || upper.contains("ACCEPTED") || upper.contains("RETURNED")) { // positive statuses
					bg = "#e4f5ec"; // soft mint background
					fg = "#146c43"; // deep green text
				} else if (upper.contains("PENDING")) { // waiting statuses
					bg = "#fff4df"; // soft amber background
					fg = "#9a6700"; // warm brown-amber text
				} else if (upper.contains("REJECTED") || upper.contains("SUSPENDED") || upper.contains("OVERDUE")) { // negative or overdue statuses
					bg = "#fdeaea"; // soft red background
					fg = "#b42318"; // crimson text
				}
				badge.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg + "; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 3 10 3 10; -fx-background-radius: 12;"); // apply pill css
				setGraphic(badge); // set badge as cell graphic
				setText(null); // hide raw text
			}
		};
	}

	// display a per-tab feature guide dialog when the user clicks the help button
	public void showTabHelpDialog(String tabTitle, String overviewText) { // define showtabhelpdialog method
		Stage dialog = new Stage(); // create modal help window
		dialog.initOwner(mainStage); // attach to main application stage
		dialog.initModality(Modality.APPLICATION_MODAL); // block background clicks while open
		dialog.initStyle(StageStyle.UTILITY); // clean utility dialog border
		dialog.setTitle("Help - " + tabTitle); // set window title

		Label lblHeading = new Label("How to Use: " + tabTitle); // dialog heading
		lblHeading.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: " + FOREST_DARK + ";"); // heading style

		Label lblBody = new Label(overviewText); // tab feature instructions
		lblBody.setWrapText(true); // wrap multi-line bullet points
		lblBody.setStyle("-fx-font-size: 13px; -fx-text-fill: " + TEXT_DARK + "; -fx-line-spacing: 4;"); // body text style

		Button btnClose = new Button("Got it"); // close button
		btnClose.setStyle(primaryButtonStyle()); // apply primary green button style
		btnClose.setOnAction(e -> dialog.close()); // close dialog when clicked

		HBox footer = new HBox(btnClose); // bottom button bar
		footer.setAlignment(Pos.CENTER_RIGHT); // align close button right

		VBox layout = new VBox(16, lblHeading, lblBody, footer); // vertical dialog container
		layout.setPadding(new Insets(22)); // outer dialog padding
		layout.setStyle("-fx-background-color: #ffffff;"); // clean white surface

		dialog.setScene(new Scene(layout, 460, 250)); // set dialog dimensions
		dialog.showAndWait(); // display modal dialog
	}

	// sanitize pipe delimiters before writing text records
	private String cleanField(String text) { // define cleanfield helper
		if (text == null) { // handle null strings safely
			return ""; // return empty string
		}
		return text.replace("|", "/").replace("\n", " ").replace("\r", " ").trim(); // strip pipe and newlines
	}

	// load user accounts from users.txt
	public ObservableList<User> loadUsers(Path path) { // define loadusers method
		ObservableList<User> list = FXCollections.observableArrayList(); // result list
		try { // attempt risky block
			if (Files.exists(path)) { // read only if file exists
				for (String line : Files.readAllLines(path)) { // iterate over each line
					if (line.trim().isEmpty()) { // skip blank lines
						continue; // move to next line
					}
					String[] p = line.split("\\|", -1); // split pipe-delimited fields
					if (p.length >= 13) { // parse full 14-column user record
						String reason = p.length >= 14 ? p[13].trim() : ""; // read optional suspension reason
						list.add(new User(p[0].trim(), p[1].trim(), p[2].trim(), p[3].trim(), // continue statement
								p[4].trim(), p[5].trim(), p[6].trim(), p[7].trim(), // continue statement
								p[8].trim(), p[9].trim(), Double.parseDouble(p[10].trim()), // continue statement
								Boolean.parseBoolean(p[11].trim()), Boolean.parseBoolean(p[12].trim()), reason)); // add parsed user
					}
				}
			}
		} catch (Exception e) { // handle exception
			e.printStackTrace(); // log file read errors
		}
		return list; // return loaded users
	}

	// save all user accounts to users.txt
	public void saveUsers() { // define saveusers method
		try (PrintWriter writer = new PrintWriter(userPath.toFile())) { // open writer to users.txt
			for (User u : users) { // write one line per user
				writer.println(cleanField(u.getUserId()) + "|" + cleanField(u.getUsername()) + "|" // continue statement
						+ cleanField(u.getPassword()) + "|" + cleanField(u.getRole()) + "|" // continue statement
						+ cleanField(u.getFullName()) + "|" + cleanField(u.getPhone()) + "|" // continue statement
						+ cleanField(u.getEmail()) + "|" + cleanField(u.getFarmName()) + "|" // continue statement
						+ cleanField(u.getFarmAddress()) + "|" + cleanField(u.getRsbsaNumber()) + "|" // continue statement
						+ u.getFarmAreaHa() + "|" + u.isVerified() + "|" + u.isSuspended() + "|" // continue statement
						+ cleanField(u.getSuspensionReason())); // write pipe-separated user fields
			}
		} catch (IOException e) { // handle exception
			e.printStackTrace(); // log write errors
		}
	}

	// load seed inventory from products.txt
	public ObservableList<Product> loadProducts(Path path) { // define loadproducts method
		ObservableList<Product> list = FXCollections.observableArrayList(); // result list
		try { // attempt risky block
			if (Files.exists(path)) { // check if products.txt exists
				for (String line : Files.readAllLines(path)) { // read each line
					if (line.trim().isEmpty()) { // skip blank lines
						continue; // move to next line
					}
					String[] parts = line.split(","); // split comma-separated product fields
					if (parts.length >= 7) { // ensure all 7 columns exist
						list.add(new Product( // continue statement
								parts[0].trim(), // crop name
								Double.parseDouble(parts[1].trim()), // price per kg
								Double.parseDouble(parts[2].trim()), // stock amount in kg
								Integer.parseInt(parts[3].trim()), // lot id
								parts[4].trim(), // storage location
								parts[5].trim(), // variety
								parts[6].trim())); // description
					}
				}
			}
		} catch (IOException | NumberFormatException e) { // handle exception
			e.printStackTrace(); // log parsing errors
		}
		return list; // return loaded products
	}

	// save seed inventory to products.txt
	public void saveProducts() { // define saveproducts method
		saveProducts(productPath, productData); // delegate to parameterized overload
	}

	// save seed inventory to a target file path
	public void saveProducts(Path path, ObservableList<Product> data) { // define saveproducts method
		try { // attempt risky block
			List<String> lines = new ArrayList<>(); // buffer formatted lines
			for (Product product : data) { // serialize each product lot
				lines.add(product.getName().replace(",", " ") + "," + product.getPrice() + "," + product.getAmount() + "," // continue statement
						+ product.getLotID() + "," + product.getLocation().replace(",", " ") + "," // continue statement
						+ product.getVariety().replace(",", " ") + "," + product.getDescription().replace(",", " ")); // comma-separated product line
			}
			Files.write(path, lines); // write all lines to disk
		} catch (IOException e) { // handle exception
			e.printStackTrace(); // log write errors
		}
	}

	// load seed requests from requests.txt
	public ObservableList<SeedRequest> loadRequests(Path path) { // define loadrequests method
		ObservableList<SeedRequest> list = FXCollections.observableArrayList(); // result list
		try { // attempt risky block
			if (Files.exists(path)) { // check if file exists
				for (String line : Files.readAllLines(path)) { // read each line
					if (line.trim().isEmpty()) { // skip blank lines
						continue; // move to next line
					}
					String[] p = line.split("\\|", -1); // split pipe-delimited columns
					if (p.length >= 11) { // parse 11-column request record
						list.add(new SeedRequest( // continue statement
								Integer.parseInt(p[0].trim()), // request id
								p[1].trim(), // farmer username
								Integer.parseInt(p[2].trim()), // lot id
								p[3].trim(), // crop name
								p[4].trim(), // variety
								Double.parseDouble(p[5].trim()), // amount in kg
								Double.parseDouble(p[6].trim()), // total php value
								p[7].trim(), // purpose
								p[8].trim(), // status
								p[9].trim(), // request date
								p[10].trim())); // due date
					}
				}
			}
		} catch (Exception e) { // handle exception
			e.printStackTrace(); // log read errors
		}
		return list; // return loaded requests
	}

	// save seed requests to requests.txt
	public void saveRequests() { // define saverequests method
		try (PrintWriter writer = new PrintWriter(requestPath.toFile())) { // open writer to requests.txt
			for (SeedRequest r : requestData) { // write one line per request
				writer.println(r.getRequestId() + "|" + cleanField(r.getFarmer()) + "|" + r.getLotId() + "|" // continue statement
						+ cleanField(r.getName()) + "|" + cleanField(r.getVariety()) + "|" + r.getAmount() + "|" // continue statement
						+ r.getTotalValue() + "|" + cleanField(r.getPurpose()) + "|" + cleanField(r.getStatus()) + "|" // continue statement
						+ cleanField(r.getRequestDate()) + "|" + cleanField(r.getDueDate())); // write pipe-separated request line
			}
		} catch (IOException e) { // handle exception
			e.printStackTrace(); // log write errors
		}
	}

	// load seed returns from returns.txt
	public ObservableList<SeedReturn> loadReturns(Path path) { // define loadreturns method
		ObservableList<SeedReturn> list = FXCollections.observableArrayList(); // result list
		try { // attempt risky block
			if (Files.exists(path)) { // check if returns.txt exists
				for (String line : Files.readAllLines(path)) { // read each line
					if (line.trim().isEmpty()) { // skip blank lines
						continue; // move to next line
					}
					String[] p = line.split("\\|", -1); // split pipe-delimited columns
					if (p.length >= 8) { // parse 8-column return record
						list.add(new SeedReturn( // continue statement
								Integer.parseInt(p[0].trim()), // return id
								Integer.parseInt(p[1].trim()), // linked request id
								p[2].trim(), // farmer username
								Integer.parseInt(p[3].trim()), // lot id
								p[4].trim(), // seed label
								Double.parseDouble(p[5].trim()), // returned amount in kg
								p[6].trim(), // status
								p[7].trim())); // return date
					}
				}
			}
		} catch (Exception e) { // handle exception
			e.printStackTrace(); // log read errors
		}
		return list; // return loaded returns
	}

	// save seed returns to returns.txt
	public void saveReturns() { // define savereturns method
		try (PrintWriter writer = new PrintWriter(returnPath.toFile())) { // open writer to returns.txt
			for (SeedReturn r : returnData) { // write one line per return
				writer.println(r.getReturnId() + "|" + r.getRequestId() + "|" + cleanField(r.getFarmer()) + "|" // continue statement
						+ r.getLotId() + "|" + cleanField(r.getSeedLabel()) + "|" + r.getAmount() + "|" // continue statement
						+ cleanField(r.getStatus()) + "|" + cleanField(r.getReturnDate())); // write pipe-separated return line
			}
		} catch (IOException e) { // handle exception
			e.printStackTrace(); // log write errors
		}
	}

	// load donations from donations.txt
	public ObservableList<Donation> loadDonations(Path path) { // define loaddonations method
		ObservableList<Donation> list = FXCollections.observableArrayList(); // result list
		try { // attempt risky block
			if (Files.exists(path)) { // check if donations.txt exists
				for (String line : Files.readAllLines(path)) { // read each line
					if (line.trim().isEmpty()) { // skip blank lines
						continue; // move to next line
					}
					String[] parts = line.split(",", 4); // split into 4 fields max so messages can keep commas
					if (parts.length >= 3) { // require at least name, email, and amount
						list.add(new Donation( // continue statement
								parts[0].trim(), // donor name
								parts[1].trim(), // donor email
								Double.parseDouble(parts[2].trim()), // donation amount
								parts.length > 3 ? parts[3].trim() : "")); // optional message
					}
				}
			}
		} catch (IOException | NumberFormatException e) { // handle exception
			e.printStackTrace(); // log read errors
		}
		return list; // return loaded donations
	}

	// save donations to donations.txt
	public void saveDonations() { // define savedonations method
		saveDonations(donationPath, donationData); // delegate to parameterized overload
	}

	// save donations to a target file path
	public void saveDonations(Path path, ObservableList<Donation> data) { // define savedonations method
		try { // attempt risky block
			List<String> lines = new ArrayList<>(); // buffer formatted lines
			for (Donation donation : data) { // serialize each donation
				String message = donation.getMessage().replace("\n", " ").replace("\r", " "); // flatten line breaks
				lines.add(donation.getDonorName().replace(",", " ") + "," + donation.getEmail().replace(",", " ") + "," // continue statement
						+ donation.getAmount() + "," + message); // format comma-separated donation line
			}
			Files.write(path, lines); // write all donation lines to disk
		} catch (IOException e) { // handle exception
			e.printStackTrace(); // log write errors
		}
	}
}
