package application.shared;

import application.Donation;
import application.Product;
import application.SeedRequest;
import application.SeedReturn;
import application.User;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AppState {

	// =========================================================================
	// LOGO IMAGE PLACEHOLDERS (ADD PNG IMAGES IN ECLIPSE & PASTE PATH/LINK HERE)
	// =========================================================================
	// Option A (Recommended in Eclipse): Drag & drop your PNG files directly into
	//   src/application/ and keep "/application/<filename>.png" below.
	// Option B (Local file link): Paste a full file path or file:/// URI, e.g.:
	//   "C:/Users/YourName/Pictures/logo_white.png"
	public static final String LOGO_ICON_PATH = "/application/assets/seed-spreaders-icon.png";
	public static final String LOGO_HERO_PATH = "/application/assets/seed-spreaders-hero.png";

	public Stage mainStage;
	public User currentUser;

	public ObservableList<Product> productData;
	public ObservableList<SeedRequest> requestData = FXCollections.observableArrayList();
	public ObservableList<SeedReturn> returnData = FXCollections.observableArrayList();
	public ObservableList<Donation> donationData = FXCollections.observableArrayList();
	public ObservableList<User> users = FXCollections.observableArrayList();

	public final Path productPath = Paths.get("products.txt");
	public final Path donationPath = Paths.get("donations.txt");
	public final Path userPath = Paths.get("users.txt");

	public int nextRequestId = 1;
	public int nextReturnId = 1;
	public int nextUserId = 1;

	public boolean darkMode = false;
	public boolean tagalog = false;
	public String currentRoute = "Dashboard";

	public static final String FOREST_DARK = "#0c3026";
	public static final String HERO_CARD_BG = "#163a2c";
	public static final String PRIMARY_GREEN = "#1e6349";
	public static final String PRIMARY_GREEN_HOVER = "#164e39";
	public static final String LIME_ACCENT = "#c8e27c";
	public static final String SAGE_ACCENT = "#5b9e6c";

	public static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMMM d, yyyy");

	private Image cachedIconImg;
	private Image cachedHeroImg;

	public void initialize(Stage stage) {
		this.mainStage = stage;

		users = loadUsers(userPath);
		productData = loadProducts(productPath);
		donationData = loadDonations(donationPath);

		if (productData.isEmpty()) {
			productData.addAll(
					new Product("Pineapple", 120.0, 200.0, 1, "Storage A", "Pineapple",
							"Sweet Queen pineapple crowns for upland Laguna farms.", "kg", 30.0, "2027-08-15"),
					new Product("Rice", 55.0, 350.0, 2, "Storage B", "NSIC Rc222",
							"High-yielding irrigated lowland rice variety.", "kg", 50.0, "2027-05-20"),
					new Product("Corn", 48.0, 180.0, 3, "Storage A", "IPB Var 6",
							"Open-pollinated white flint corn developed at UPLB.", "kg", 25.0, "2027-06-30"));
			saveProducts(productPath, productData);
		}

		seedInitialTransactionsIfEmpty();

		productData.addListener((ListChangeListener.Change<? extends Product> c) -> {
			saveProducts(productPath, productData);
		});
		donationData.addListener((ListChangeListener.Change<? extends Donation> c) -> {
			saveDonations(donationPath, donationData);
		});
	}

	public void seedInitialTransactionsIfEmpty() {
		if (requestData.isEmpty()) {
			requestData.add(new SeedRequest(nextRequestId++, "juan", "Dela Cruz Farm", 2,
					"Rice", "NSIC Rc222", 100.0, 55.0, "Wet season planting", "Approved",
					LocalDate.now().minusDays(20), LocalDate.now().plusDays(14)));
			requestData.add(new SeedRequest(nextRequestId++, "maria", "Santos Agri-Farm", 3,
					"Corn", "IPB Var 6", 60.0, 48.0, "Intercropping trial", "Approved",
					LocalDate.now().minusDays(105), LocalDate.now().minusDays(5)));
			requestData.add(new SeedRequest(nextRequestId++, "juan", "Dela Cruz Farm", 1,
					"Pineapple", "Pineapple", 50.0, 120.0, "Orchard expansion", "Pending",
					LocalDate.now(), LocalDate.now().plusDays(90)));
		}
		if (returnData.isEmpty()) {
			returnData.add(new SeedReturn(nextReturnId++, 1, "juan", 2,
					"Rice - NSIC Rc222", 50.0, "Pending", LocalDate.now()));
		}
	}

	// =========================================================================
	// LOGO IMAGE LOADER & PLACEHOLDER BUILDER
	// =========================================================================
	public Image loadLogoImage(String pathOrLink) {
		if (pathOrLink == null || pathOrLink.trim().isEmpty()) return null;
		String trimmed = pathOrLink.trim();
		try {
			URL resUrl = AppState.class.getResource(trimmed.startsWith("/") ? trimmed : "/application/" + trimmed);
			if (resUrl != null) {
				Image img = new Image(resUrl.toExternalForm());
				if (!img.isError()) return img;
			}
		} catch (Exception ignored) {
		}
		try {
			if (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("file:")) {
				Image img = new Image(trimmed);
				if (!img.isError()) return img;
			}
			File localFile = new File(trimmed);
			if (localFile.exists()) {
				Image img = new Image(localFile.toURI().toString());
				if (!img.isError()) return img;
			}
		} catch (Exception ignored) {
		}
		return null;
	}

	public Node buildLogo(double scale, boolean darkBackground) {
		Image img = darkBackground ? getHeroImage() : getIconImage();
		HBox lockup = new HBox(10 * scale);
		lockup.setAlignment(Pos.CENTER_LEFT);

		if (img != null) {
			ImageView view = new ImageView(img);
			view.setFitHeight(46 * scale);
			view.setPreserveRatio(true);
			view.setSmooth(true);
			view.setCache(true);
			lockup.getChildren().add(view);
		}

		Label lblBrand = new Label("Seed Spreaders.");
		lblBrand.setFont(Font.font("System", FontWeight.BOLD, 18 * scale));
		lblBrand.setStyle("-fx-text-fill: " + (darkBackground ? "#ffffff" : FOREST_DARK) + ";");

		Label lblSub = new Label("SEED BANK SYSTEM");
		lblSub.setFont(Font.font("System", FontWeight.BOLD, 8.5 * scale));
		lblSub.setStyle("-fx-text-fill: " + (darkBackground ? "#8ec9a4" : SAGE_ACCENT) + ";");

		VBox words = new VBox(1 * scale, lblBrand, lblSub);
		words.setAlignment(Pos.CENTER_LEFT);
		lockup.getChildren().add(words);
		return lockup;
	}

	public Node buildHeroLogoMark(double height) {
		Image img = getHeroImage();
		if (img != null) {
			ImageView view = new ImageView(img);
			view.setFitHeight(height);
			view.setPreserveRatio(true);
			view.setSmooth(true);
			view.setCache(true);
			return view;
		}

		StackPane heroPlaceholder = new StackPane();
		heroPlaceholder.setPrefSize(height, height);
		heroPlaceholder.setStyle("-fx-border-color: rgba(255,255,255,0.35); -fx-border-style: dashed; -fx-border-radius: 24;");
		Label lblHint = new Label("Seed Spreaders.");
		lblHint.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 16px;");
		heroPlaceholder.getChildren().add(lblHint);
		return heroPlaceholder;
	}

	private Image getIconImage() {
		if (cachedIconImg == null) cachedIconImg = loadLogoImage(LOGO_ICON_PATH);
		return cachedIconImg;
	}

	private Image getHeroImage() {
		if (cachedHeroImg == null) cachedHeroImg = loadLogoImage(LOGO_HERO_PATH);
		return cachedHeroImg;
	}

	// =========================================================================
	// THEME & BILINGUAL HELPERS
	// =========================================================================
	public String bgPage() {
		return darkMode ? "#111916" : "#f5f7f2";
	}

	public String bgCard() {
		return darkMode ? "#1a2621" : "#ffffff";
	}

	public String bgHeaderBar() {
		return darkMode ? "#15201c" : "#ffffff";
	}

	public String bgTableHeader() {
		return darkMode ? "#22312b" : "#f0f2ec";
	}

	public String borderColor() {
		return darkMode ? "#2c3f37" : "#e2e6dc";
	}

	public String textPrimary() {
		return darkMode ? "#eef3ef" : "#111815";
	}

	public String textMuted() {
		return darkMode ? "#9cb0a6" : "#66736c";
	}

	public String inputStyle() {
		return "-fx-background-color: " + (darkMode ? "#131d19" : "#ffffff") + ";"
				+ "-fx-text-fill: " + textPrimary() + ";"
				+ "-fx-prompt-text-fill: " + textMuted() + ";"
				+ "-fx-border-color: " + borderColor() + ";"
				+ "-fx-border-radius: 8;"
				+ "-fx-background-radius: 8;"
				+ "-fx-padding: 9 12 9 12;"
				+ "-fx-font-size: 13px;";
	}

	public String primaryButtonStyle() {
		return "-fx-background-color: " + PRIMARY_GREEN + ";"
				+ "-fx-text-fill: #ffffff;"
				+ "-fx-font-weight: bold;"
				+ "-fx-font-size: 13px;"
				+ "-fx-background-radius: 10;"
				+ "-fx-padding: 10 20 10 20;"
				+ "-fx-cursor: hand;";
	}

	public String outlineButtonStyle() {
		return "-fx-background-color: " + bgCard() + ";"
				+ "-fx-text-fill: " + (darkMode ? "#cde3d8" : PRIMARY_GREEN) + ";"
				+ "-fx-border-color: " + borderColor() + ";"
				+ "-fx-border-radius: 10;"
				+ "-fx-background-radius: 10;"
				+ "-fx-font-weight: bold;"
				+ "-fx-font-size: 13px;"
				+ "-fx-padding: 9 18 9 18;"
				+ "-fx-cursor: hand;";
	}

	public String dangerOutlineButtonStyle() {
		return "-fx-background-color: " + bgCard() + ";"
				+ "-fx-text-fill: #dc3545;"
				+ "-fx-border-color: " + borderColor() + ";"
				+ "-fx-border-radius: 10;"
				+ "-fx-background-radius: 10;"
				+ "-fx-font-weight: bold;"
				+ "-fx-font-size: 13px;"
				+ "-fx-padding: 9 18 9 18;"
				+ "-fx-cursor: hand;";
	}

	public String cardStyle() {
		return "-fx-background-color: " + bgCard() + ";"
				+ "-fx-border-color: " + borderColor() + ";"
				+ "-fx-border-radius: 16;"
				+ "-fx-background-radius: 16;"
				+ "-fx-padding: 20;";
	}

	public String t(String en, String tl) {
		return tagalog ? tl : en;
	}

	public void applyScene(Scene scene, String title) {
		scene.getStylesheets().clear();
		String tableCss = "data:text/css,"
				+ ".table-view { -fx-background-color: transparent; -fx-border-color: transparent; }"
				+ ".table-view .column-header-background { -fx-background-color: " + bgTableHeader() + "; -fx-background-radius: 8; }"
				+ ".table-view .column-header { -fx-background-color: transparent; -fx-size: 40px; }"
				+ ".table-view .column-header .label { -fx-text-fill: " + textPrimary() + "; -fx-font-weight: bold; -fx-font-size: 12px; }"
				+ ".table-view .table-row-cell { -fx-background-color: " + bgCard() + "; -fx-border-color: transparent transparent " + borderColor() + " transparent; -fx-cell-size: 46px; }"
				+ ".table-view .table-row-cell:selected { -fx-background-color: " + (darkMode ? "#253b32" : "#e8f2ec") + "; }"
				+ ".table-view .table-cell { -fx-text-fill: " + textPrimary() + "; -fx-alignment: CENTER-LEFT; -fx-font-size: 13px; }"
				+ ".scroll-pane { -fx-background-color: transparent; -fx-background: transparent; }"
				+ ".scroll-pane > .viewport { -fx-background-color: transparent; }";
		scene.getStylesheets().add(tableCss);
		mainStage.setScene(scene);
		mainStage.setTitle(title);
		mainStage.setResizable(true);
		mainStage.setMinWidth(960);
		mainStage.setMinHeight(640);
	}

	// =========================================================================
	// SHARED SHELL NAVIGATION & TOP HEADER HELPERS
	// =========================================================================
	public Button createNavButton(Runnable refreshShell, String label, String routeKey, boolean isAdmin) {
		boolean active = currentRoute.equals(routeKey);
		Button btn = new Button("  " + getNavIcon(routeKey) + "   " + label);
		btn.setMaxWidth(Double.MAX_VALUE);
		btn.setAlignment(Pos.CENTER_LEFT);
		btn.setStyle("-fx-background-color: " + (active ? PRIMARY_GREEN : "transparent") + ";"
				+ "-fx-text-fill: #ffffff;"
				+ "-fx-font-size: 14px;"
				+ "-fx-font-weight: " + (active ? "bold" : "normal") + ";"
				+ "-fx-background-radius: 12;"
				+ "-fx-padding: 13 16 13 16;"
				+ "-fx-cursor: hand;");
		btn.setOnAction(e -> {
			currentRoute = routeKey;
			refreshShell.run();
		});
		return btn;
	}

	public String getNavIcon(String route) {
		switch (route) {
			case "Dashboard":
				return "\u229e";
			case "Inventory":
				return "\u2b21";
			case "Farmers":
				return "\u2687";
			case "Requests":
				return "\ud83c\udf31";
			case "Transactions":
				return "\u25a4";
			case "Donations":
				return "\u2665";
			default:
				return "\u2022";
		}
	}

	public String getAdminPageTitle() {
		switch (currentRoute) {
			case "Inventory":
				return t("Inventory", "Imbentaryo");
			case "Farmers":
				return t("User Management", "Pamamahala ng Users");
			case "Transactions":
				return t("Transactions", "Mga Transaksyon");
			case "Donations":
				return t("Donations", "Mga Donasyon");
			default:
				return t("Dashboard", "Dashboard");
		}
	}

	public String getFarmerPageTitle() {
		switch (currentRoute) {
			case "Requests":
				return t("Requests", "Humiram ng Binhi");
			case "Transactions":
				return t("Transactions", "Mga Transaksyon");
			default:
				return t("Dashboard", "Dashboard");
		}
	}

	public HBox buildTopHeader(String pageTitle, boolean isAdmin, Runnable refreshShell) {
		HBox header = new HBox(12);
		header.setAlignment(Pos.CENTER_LEFT);

		Label lblTitle = new Label(pageTitle);
		lblTitle.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + textPrimary() + ";");

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		// Contextual help for the current tab
		Button btnHelp = new Button("? Help");
		btnHelp.setStyle(outlineButtonStyle());
		btnHelp.setOnAction(e -> showTabHelpDialog(currentRoute, isAdmin));

		// User avatar with initials
		StackPane avatar = new StackPane();
		Circle circle = new Circle(20, Color.web("#bc9d9d"));
		String initials = currentUser.getUsername().substring(0, Math.min(2, currentUser.getUsername().length())).toUpperCase();
		Label lblInit = new Label(initials);
		lblInit.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 12px;");
		avatar.getChildren().addAll(circle, lblInit);

		header.getChildren().addAll(lblTitle, spacer, btnHelp, avatar);
		return header;
	}

	public VBox createStatCard(String title, String value, String subtitle, String accentColor) {
		VBox card = new VBox(8);
		card.setStyle(cardStyle());
		HBox.setHgrow(card, Priority.ALWAYS);
		card.setPrefWidth(220);

		Label lblTitle = new Label(title);
		lblTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + textPrimary() + ";");

		Label lblVal = new Label(value);
		lblVal.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + accentColor + ";");

		Label lblSub = new Label(subtitle);
		lblSub.setStyle("-fx-font-size: 11px; -fx-text-fill: " + textMuted() + ";");

		card.getChildren().addAll(lblTitle, lblVal, lblSub);
		return card;
	}

	public HBox createAlertRow(String badgeText, String detail, String bgHex, String fgHex) {
		HBox row = new HBox(10);
		row.setAlignment(Pos.CENTER_LEFT);
		row.setPadding(new Insets(10, 12, 10, 12));
		row.setStyle("-fx-background-color: " + bgPage() + "; -fx-background-radius: 10;");

		Label badge = new Label(badgeText);
		badge.setStyle("-fx-background-color: " + bgHex + "; -fx-text-fill: " + fgHex + "; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 3 8 3 8; -fx-background-radius: 6;");

		Label lbl = new Label(detail);
		lbl.setStyle("-fx-text-fill: " + textPrimary() + "; -fx-font-size: 12px;");

		row.getChildren().addAll(badge, lbl);
		return row;
	}

	public Label makeDetailLine(String key, String val) {
		Label l = new Label(key + "  " + val);
		l.setStyle("-fx-text-fill: " + textPrimary() + "; -fx-font-size: 12px;");
		return l;
	}


	public VBox wrapField(String labelText, Node control) {
		Label lbl = new Label(labelText);
		lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: " + textPrimary() + ";");
		return new VBox(5, lbl, control);
	}

	public VBox makeReadOnlyBox(String label, String val) {
		Label lbl = new Label(label);
		lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: " + textPrimary() + ";");
		TextField tf = new TextField(val);
		tf.setEditable(false);
		tf.setStyle(inputStyle());
		return new VBox(5, lbl, tf);
	}

	public <T> TableCell<T, String> createStatusBadgeCell() {
		return new TableCell<T, String>() {
			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setGraphic(null);
				} else {
					Label badge = new Label(item);
					String bg = "#fef3c7";
					String fg = "#92400e";
					if ("Approved".equalsIgnoreCase(item) || "Accepted".equalsIgnoreCase(item)) {
						bg = "#dcfce7";
						fg = "#166534";
					} else if ("Rejected".equalsIgnoreCase(item)) {
						bg = "#fde8e8";
						fg = "#9b1c1c";
					}
					badge.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg + ";"
							+ "-fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 4 10 4 10; -fx-background-radius: 8;");
					setGraphic(badge);
				}
			}
		};
	}

	// =========================================================================
	// PER-TAB HELP MANUAL MODAL (LOW PRIORITY: HELP BUTTON PER TAB)
	// =========================================================================
	public void showTabHelpDialog(String route, boolean isAdmin) {
		Stage helpStage = new Stage();
		helpStage.initOwner(mainStage);
		helpStage.initModality(Modality.APPLICATION_MODAL);
		helpStage.initStyle(StageStyle.UTILITY);
		helpStage.setTitle(t("User Manual \u2014 ", "Gabay sa Paggamit \u2014 ") + route);

		VBox card = new VBox(14);
		card.setPadding(new Insets(24));
		card.setStyle("-fx-background-color: " + bgCard() + ";");

		Label lblTitle = new Label(t("Help & Feature Overview: ", "Tulong at Guide sa Tab na Ito: ") + route);
		lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + PRIMARY_GREEN + ";");

		String guideText;
		if (isAdmin) {
			switch (route) {
				case "Inventory":
					guideText = t(
							"\u2022 View live metrics for Plant Varieties, Seed Stock (kg), and Storage Lots.\n"
									+ "\u2022 Click '+ Add Inventory Slot' to register new seed lots with unit, minimum threshold, and expiry.\n"
									+ "\u2022 Click 'View' on any lot row to inspect, edit, or delete that seed product.\n"
									+ "\u2022 Changes automatically save to products.txt.",
							"\u2022 Kita agad dito kung ilang variety, ilang kilo ang stock, at ilang seed lot ang meron.\n"
									+ "\u2022 I-click ang '+ Magdagdag ng Seed Lot' para magpasok ng bagong binhi.\n"
									+ "\u2022 I-click ang 'Tingnan' sa kahit anong row para i-edit o i-delete ang binhi.\n"
									+ "\u2022 Automatic nang nase-save sa products.txt ang lahat ng changes.");
					break;
				case "Farmers":
					guideText = t(
							"\u2022 Manage all registered Farmers and Admins in the system.\n"
									+ "\u2022 Click '+ Add Farm / User Account' to create a new Farmer or Admin account directly.\n"
									+ "\u2022 Click 'View' on any account to inspect Farmer Info, Farm Info, and Pending Seed Requests/Returns.\n"
									+ "\u2022 Inside View Details, you can Verify pending farmers, Assign/Revoke Admin privileges, or Suspend/Unsuspend users.",
							"\u2022 Dito mo ma-manage lahat ng registered Farmers at Admins.\n"
									+ "\u2022 I-click ang '+ Magdagdag ng Account' para gumawa ng bagong Farmer o Admin account.\n"
									+ "\u2022 I-click ang 'Tingnan' para makita ang Farmer Info, Farm Info, at Pending Requests/Returns.\n"
									+ "\u2022 Sa loob ng View Details, pwede mong i-Verify ang farmer, Gawing Admin, o I-suspend ang user.");
					break;
				case "Transactions":
					guideText = t(
							"\u2022 Review incoming farmer seed requests in the top table and click 'View' to Approve or Reject.\n"
									+ "\u2022 Approving a request automatically deducts seed stock and sets a return deadline.\n"
									+ "\u2022 Inspect returned seeds in the bottom table and click 'Accept' to replenish inventory stock.",
							"\u2022 Tingnan ang mga hiling na binhi sa taas at i-click ang 'Tingnan' para i-Approve o i-Reject.\n"
									+ "\u2022 Pag na-approve, automatic na mababawas sa stock at magkakaroon ng return due date.\n"
									+ "\u2022 Sa baba naman, i-click ang 'Tanggapin' sa mga ibinalik na binhi para madagdag ulit sa stock.");
					break;
				case "Donations":
					guideText = t(
							"\u2022 View all monetary donations contributed from the public landing page.\n"
									+ "\u2022 Tracks donor names, emails, amounts, and messages.",
							"\u2022 Kita dito lahat ng donasyong pumasok galing sa public landing page.\n"
									+ "\u2022 Naka-list dito ang pangalan ng donor, email, halaga, at message nila.");
					break;
				default:
					guideText = t(
							"\u2022 Overview of Pending Seed Requests, Pending Seed Returns, Missing Returns (Late Deadlines), and Inventory Stock.\n"
									+ "\u2022 Quickly identify overdue loans highlighted in red and jump straight to User Management.",
							"\u2022 Quick summary ng Pending Seed Requests, Pending Seed Returns, Late Returns, at lagay ng Inventory.\n"
									+ "\u2022 Kita agad ang mga overdue na hiram (naka-red badge) at pwede kang dumiretso sa User Management.");
					break;
			}
		} else {
			switch (route) {
				case "Requests":
					guideText = t(
							"\u2022 Browse available seed lots in the Seed Bank table on the left.\n"
									+ "\u2022 Select a seed lot, enter the requested weight (kg) and planting purpose on the right, and submit.\n"
									+ "\u2022 Note: Unverified farmer accounts have limited access until verified by an Admin.",
							"\u2022 Pumili ng available na binhi sa table sa kaliwa.\n"
									+ "\u2022 Ilagay sa kanan kung ilang kilo (kg) ang kailangan at kung para saan itatanim, tapos i-submit.\n"
									+ "\u2022 Paalala: Kailangan munang ma-verify ng Admin ang account mo bago makahiram.");
					break;
				case "Transactions":
					guideText = t(
							"\u2022 Track the approval status and return due dates of your seed requests.\n"
									+ "\u2022 Click '\u21a9 Submit Seed Return' to return harvested seeds from an approved loan.",
							"\u2022 I-check dito kung na-approve na ang request mo at kailan ang due date ng pagbabalik.\n"
									+ "\u2022 I-click ang '\u21a9 Magbalik ng Binhi' para mag-submit ng ibabalik na binhi pagka-ani.");
					break;
				default:
					guideText = t(
							"\u2022 View your Pending Seed Requests, Pending Seed Returns, and Return Deadlines at a glance.\n"
									+ "\u2022 Update your Phone Number, Email, Farm Name, and Farm Address directly in the Basic Information card.",
							"\u2022 Kita agad dito ang Pending Seed Requests, Pending Seed Returns, at mga Due Date mo.\n"
									+ "\u2022 Pwede mo ring i-edit dito mismo ang Cellphone Number, Email, Farm Name, at Farm Address mo.");
					break;
			}
		}

		Label lblBody = new Label(guideText);
		lblBody.setWrapText(true);
		lblBody.setStyle("-fx-text-fill: " + textPrimary() + "; -fx-font-size: 13px; -fx-line-spacing: 4;");

		Button btnOk = new Button(t("Got it", "Sige, gets!"));
		btnOk.setStyle(primaryButtonStyle());
		btnOk.setOnAction(e -> helpStage.close());

		HBox footer = new HBox(btnOk);
		footer.setAlignment(Pos.CENTER_RIGHT);

		card.getChildren().addAll(lblTitle, lblBody, footer);
		helpStage.setScene(new Scene(card, 480, 260));
		helpStage.showAndWait();
	}

	// =========================================================================
	// FILE PERSISTENCE (USERS, PRODUCTS, DONATIONS)
	// =========================================================================
	public ObservableList<User> loadUsers(java.nio.file.Path path) {
		ObservableList<User> list = FXCollections.observableArrayList();
		try {
			if (Files.exists(path)) {
				for (String line : Files.readAllLines(path)) {
					if (line.trim().isEmpty()) continue;
					String[] p = line.split("\\|", -1);
					if (p.length >= 12) {
						list.add(new User(p[0], p[1], p[2], p[3], p[4], p[5], p[6], p[7], p[8], p[9], p[10], p[11]));
					} else if (p.length >= 11) {
						list.add(new User(p[0], p[1], p[2], p[3], p[4], p[5], p[6], p[7], p[8], p[9], p[10], ""));
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (list.isEmpty()) {
			list.addAll(
					new User("ADM-001", "admin", "admin123", "Admin", "09171000001", "admin@seedspreaders.ph",
							"UPLB Central Seed Bank", "Los Ba\u00f1os, Laguna", "ADMIN-01", "5.0 ha", "Verified"),
					new User("FRM-001", "juan", "farmer123", "Farmer", "09172223333", "juan@lagunafarms.ph",
							"Dela Cruz Farm", "Bay, Laguna", "RSBSA-04-034-0012", "2.5 ha", "Verified"),
					new User("FRM-002", "maria", "farmer123", "Farmer", "09174445555", "maria@lagunafarms.ph",
							"Santos Agri-Farm", "Calauan, Laguna", "RSBSA-04-034-0089", "3.0 ha", "Verified"));
			saveUsers(path, list);
		}
		nextUserId = list.size() + 1;
		return list;
	}

	public void saveUsers(java.nio.file.Path path, ObservableList<User> data) {
		try {
			List<String> lines = new ArrayList<>();
			for (User u : data) {
				lines.add(String.join("|",
						safe(u.getUserId()), safe(u.getUsername()), safe(u.getPassword()), safe(u.getRole()),
						safe(u.getPhoneNumber()), safe(u.getEmail()), safe(u.getFarmName()), safe(u.getFarmAddress()),
						safe(u.getRsbsaNumber()), safe(u.getFarmArea()), safe(u.getStatus()),
						safe(u.getSuspensionReason())));
			}
			Files.write(path, lines);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String safe(String s) {
		return s == null ? "" : s.replace("|", "/").replace("\n", " ");
	}

	public ObservableList<Product> loadProducts(java.nio.file.Path path) {
		ObservableList<Product> list = FXCollections.observableArrayList();
		try {
			if (Files.exists(path)) {
				for (String line : Files.readAllLines(path)) {
					if (line.trim().isEmpty()) continue;
					String[] parts = line.split(",");
					if (parts.length >= 10) {
						list.add(new Product(parts[0].trim(), Double.parseDouble(parts[1].trim()),
								Double.parseDouble(parts[2].trim()), Integer.parseInt(parts[3].trim()),
								parts[4].trim(), parts[5].trim(), parts[6].trim(),
								parts[7].trim(), Double.parseDouble(parts[8].trim()), parts[9].trim()));
					} else if (parts.length >= 7) {
						list.add(new Product(parts[0].trim(), Double.parseDouble(parts[1].trim()),
								Double.parseDouble(parts[2].trim()), Integer.parseInt(parts[3].trim()),
								parts[4].trim(), parts[5].trim(), parts[6].trim()));
					}
				}
			}
		} catch (IOException | NumberFormatException e) {
			e.printStackTrace();
		}
		return list;
	}

	public void saveProducts(java.nio.file.Path path, ObservableList<Product> data) {
		try {
			List<String> lines = new ArrayList<>();
			for (Product p : data) {
				lines.add(p.getName() + "," + p.getPrice() + "," + p.getAmount() + ","
						+ p.getLotID() + "," + p.getLocation() + "," + p.getVariety() + ","
						+ p.getDescription().replace(",", ";") + "," + p.getUnit() + ","
						+ p.getMinThreshold() + "," + p.getExpiryDate());
			}
			Files.write(path, lines);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ObservableList<Donation> loadDonations(java.nio.file.Path path) {
		ObservableList<Donation> list = FXCollections.observableArrayList();
		try {
			if (Files.exists(path)) {
				for (String line : Files.readAllLines(path)) {
					if (line.trim().isEmpty()) continue;
					String[] parts = line.split(",", 4);
					if (parts.length < 3) continue;
					list.add(new Donation(parts[0].trim(), parts[1].trim(),
							Double.parseDouble(parts[2].trim()), parts.length > 3 ? parts[3].trim() : ""));
				}
			}
		} catch (IOException | NumberFormatException e) {
			e.printStackTrace();
		}
		return list;
	}

	public void saveDonations(java.nio.file.Path path, ObservableList<Donation> data) {
		try {
			List<String> lines = new ArrayList<>();
			for (Donation d : data) {
				String msg = d.getMessage().replace("\n", " ").replace("\r", " ");
				lines.add(d.getDonorName() + "," + d.getEmail() + "," + d.getAmount() + "," + msg);
			}
			try (PrintWriter writer = new PrintWriter(path.toFile())) {
				for (String line : lines) writer.println(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Product findProduct(int lotId) {
		for (Product p : productData) {
			if (p.getLotID() == lotId) return p;
		}
		return null;
	}

	public double round2(double value) {
		return Math.round(value * 100.0) / 100.0;
	}
}
