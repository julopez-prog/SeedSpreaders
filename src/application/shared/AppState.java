package application.shared; // declare package application.shared

import application.Donation; // import donation class
import application.Product; // import product class
import application.SeedRequest; // import seedrequest class
import application.SeedReturn; // import seedreturn class
import application.User; // import user class
import javafx.collections.FXCollections; // import fxcollections class
import javafx.collections.ListChangeListener; // import listchangelistener class
import javafx.collections.ObservableList; // import observablelist class
import javafx.geometry.Insets; // import insets class
import javafx.geometry.Pos; // import pos class
import javafx.scene.Node; // import node class
import javafx.scene.Scene; // import scene class
import javafx.scene.control.Button; // import button class
import javafx.scene.control.Label; // import label class
import javafx.scene.control.TableCell; // import tablecell class
import javafx.scene.control.TextField; // import textfield class
import javafx.scene.image.Image; // import image class
import javafx.scene.image.ImageView; // import imageview class
import javafx.scene.layout.HBox; // import hbox class
import javafx.scene.layout.Priority; // import priority class
import javafx.scene.layout.Region; // import region class
import javafx.scene.layout.StackPane; // import stackpane class
import javafx.scene.layout.VBox; // import vbox class
import javafx.scene.paint.Color; // import color class
import javafx.scene.shape.Circle; // import circle class
import javafx.scene.text.Font; // import font class
import javafx.scene.text.FontWeight; // import fontweight class
import javafx.stage.Modality; // import modality class
import javafx.stage.Stage; // import stage class
import javafx.stage.StageStyle; // import stagestyle class

import java.io.File; // import file class
import java.io.IOException; // import ioexception class
import java.io.PrintWriter; // import printwriter class
import java.net.URL; // import url class
import java.nio.file.Files; // import files class
import java.nio.file.Path; // import path class
import java.nio.file.Paths; // import paths class
import java.time.LocalDate; // import localdate class
import java.time.format.DateTimeFormatter; // import datetimeformatter class
import java.util.ArrayList; // import arraylist class
import java.util.List; // import list class

public class AppState { // define appstate class

	// brand logo graphics (add png images in eclipse & paste path/link here)
	// option a (recommended in eclipse): drag & drop your png files directly into
	// src/application/ and keep "/application/<filename>.png" below.
	// option b (local file link): paste a full file path or file:/// uri, e.g.:
	// "c:/users/yourname/pictures/logo_white.png"
	public static final String LOGO_ICON_PATH = "/application/assets/seed-spreaders-icon.png"; // store logo_icon_path field
	public static final String LOGO_HERO_PATH = "/application/assets/seed-spreaders-hero.png"; // store logo_hero_path field

	public Stage mainStage; // store mainstage field
	public User currentUser; // store currentuser field

	public ObservableList<Product> productData; // store productdata field
	public ObservableList<SeedRequest> requestData = FXCollections.observableArrayList(); // store requestdata field
	public ObservableList<SeedReturn> returnData = FXCollections.observableArrayList(); // store returndata field
	public ObservableList<Donation> donationData = FXCollections.observableArrayList(); // store donationdata field
	public ObservableList<User> users = FXCollections.observableArrayList(); // store users field

	public final Path productPath = Paths.get("products.txt"); // store productpath field
	public final Path donationPath = Paths.get("donations.txt"); // store donationpath field
	public final Path userPath = Paths.get("users.txt"); // store userpath field

	public int nextRequestId = 1; // store nextrequestid field
	public int nextReturnId = 1; // store nextreturnid field
	public int nextUserId = 1; // store nextuserid field

	public boolean darkMode = false; // store darkmode field
	public boolean tagalog = false; // store tagalog field
	public String currentRoute = "Dashboard"; // store currentroute field

	public static final String FOREST_DARK = "#0c3026"; // store forest_dark field
	public static final String HERO_CARD_BG = "#163a2c"; // store hero_card_bg field
	public static final String PRIMARY_GREEN = "#1e6349"; // store primary_green field
	public static final String PRIMARY_GREEN_HOVER = "#164e39"; // store primary_green_hover field
	public static final String LIME_ACCENT = "#c8e27c"; // store lime_accent field
	public static final String SAGE_ACCENT = "#5b9e6c"; // store sage_accent field

	public static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMMM d, yyyy"); // store date_fmt field

	private Image cachedIconImg; // store cachediconimg field
	private Image cachedHeroImg; // store cachedheroimg field

	public void initialize(Stage stage) { // handle initialize operation
		this.mainStage = stage; // assign mainstage value

		users = loadUsers(userPath); // execute statement
		productData = loadProducts(productPath); // execute statement
		donationData = loadDonations(donationPath); // execute statement

		if (productData.isEmpty()) { // check condition before proceeding
			productData.addAll( // continue statement
					new Product("Pineapple", 120.0, 200.0, 1, "Storage A", "Pineapple", // create seed product entry
							"Sweet Queen pineapple crowns for upland Laguna farms.", "kg", 30.0, "2027-08-15"), // continue statement
					new Product("Rice", 55.0, 350.0, 2, "Storage B", "NSIC Rc222", // create seed product entry
							"High-yielding irrigated lowland rice variety.", "kg", 50.0, "2027-05-20"), // continue statement
					new Product("Corn", 48.0, 180.0, 3, "Storage A", "IPB Var 6", // create seed product entry
							"Open-pollinated white flint corn developed at UPLB.", "kg", 25.0, "2027-06-30")); // execute statement
			saveProducts(productPath, productData); // execute statement
		}

		seedInitialTransactionsIfEmpty(); // execute statement

		productData.addListener((ListChangeListener.Change<? extends Product> c) -> { // open code block
			saveProducts(productPath, productData); // execute statement
		}); // execute statement
		donationData.addListener((ListChangeListener.Change<? extends Donation> c) -> { // open code block
			saveDonations(donationPath, donationData); // execute statement
		}); // execute statement
	}

	public void seedInitialTransactionsIfEmpty() { // handle seedinitialtransactionsifempty operation
		if (requestData.isEmpty()) { // check condition before proceeding
			requestData.add(new SeedRequest(nextRequestId++, "juan", "Dela Cruz Farm", 2, // continue statement
					"Rice", "NSIC Rc222", 100.0, 55.0, "Wet season planting", "Approved", // continue statement
					LocalDate.now().minusDays(20), LocalDate.now().plusDays(14))); // execute statement
			requestData.add(new SeedRequest(nextRequestId++, "maria", "Santos Agri-Farm", 3, // continue statement
					"Corn", "IPB Var 6", 60.0, 48.0, "Intercropping trial", "Approved", // continue statement
					LocalDate.now().minusDays(105), LocalDate.now().minusDays(5))); // execute statement
			requestData.add(new SeedRequest(nextRequestId++, "juan", "Dela Cruz Farm", 1, // continue statement
					"Pineapple", "Pineapple", 50.0, 120.0, "Orchard expansion", "Pending", // continue statement
					LocalDate.now(), LocalDate.now().plusDays(90))); // execute statement
		}
		if (returnData.isEmpty()) { // check condition before proceeding
			returnData.add(new SeedReturn(nextReturnId++, 1, "juan", 2, // continue statement
					"Rice - NSIC Rc222", 50.0, "Pending", LocalDate.now())); // execute statement
		}
	}

	// logo image loader & placeholder builder
	public Image loadLogoImage(String pathOrLink) { // handle loadlogoimage operation
		if (pathOrLink == null || pathOrLink.trim().isEmpty()) return null; // check condition before proceeding
		String trimmed = pathOrLink.trim(); // execute statement
		try { // start protected execution block
			URL resUrl = AppState.class.getResource(trimmed.startsWith("/") ? trimmed : "/application/" + trimmed); // execute statement
			if (resUrl != null) { // check condition before proceeding
				Image img = new Image(resUrl.toExternalForm()); // execute statement
				if (!img.isError()) return img; // check condition before proceeding
			}
		} catch (Exception ignored) { // handle caught exception
		}
		try { // start protected execution block
			if (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("file:")) {
				Image img = new Image(trimmed); // execute statement
				if (!img.isError()) return img; // check condition before proceeding
			}
			File localFile = new File(trimmed); // execute statement
			if (localFile.exists()) { // check condition before proceeding
				Image img = new Image(localFile.toURI().toString()); // execute statement
				if (!img.isError()) return img; // check condition before proceeding
			}
		} catch (Exception ignored) { // handle caught exception
		}
		return null; // return null
	}

	public Node buildLogo(double scale, boolean darkBackground) { // handle buildlogo operation
		Image img = darkBackground ? getHeroImage() : getIconImage(); // execute statement
		HBox lockup = new HBox(10 * scale); // execute statement
		lockup.setAlignment(Pos.CENTER_LEFT); // set container alignment

		if (img != null) { // check condition before proceeding
			ImageView view = new ImageView(img); // execute statement
			view.setFitHeight(46 * scale); // set image display dimensions
			view.setPreserveRatio(true); // preserve image aspect ratio
			view.setSmooth(true); // enable smooth image scaling
			view.setCache(true); // cache rendered node bitmap
			lockup.getChildren().add(view); // append child node to container
		}

		Label lblBrand = new Label("Seed Spreaders."); // execute statement
		lblBrand.setFont(Font.font("System", FontWeight.BOLD, 18 * scale)); // apply font family and weight
		lblBrand.setStyle("-fx-text-fill: " + (darkBackground ? "#ffffff" : FOREST_DARK) + ";"); // apply inline css styling

		Label lblSub = new Label("SEED BANK SYSTEM"); // execute statement
		lblSub.setFont(Font.font("System", FontWeight.BOLD, 8.5 * scale)); // apply font family and weight
		lblSub.setStyle("-fx-text-fill: " + (darkBackground ? "#8ec9a4" : SAGE_ACCENT) + ";"); // apply inline css styling

		VBox words = new VBox(1 * scale, lblBrand, lblSub); // execute statement
		words.setAlignment(Pos.CENTER_LEFT); // set container alignment
		lockup.getChildren().add(words); // append child node to container
		return lockup; // return lockup
	}

	public Node buildHeroLogoMark(double height) { // handle buildherologomark operation
		Image img = getHeroImage(); // execute statement
		if (img != null) { // check condition before proceeding
			ImageView view = new ImageView(img); // execute statement
			view.setFitHeight(height); // set image display dimensions
			view.setPreserveRatio(true); // preserve image aspect ratio
			view.setSmooth(true); // enable smooth image scaling
			view.setCache(true); // cache rendered node bitmap
			return view; // return view
		}

		StackPane heroPlaceholder = new StackPane(); // execute statement
		heroPlaceholder.setPrefSize(height, height); // execute statement
		heroPlaceholder.setStyle("-fx-border-color: rgba(255,255,255,0.35); -fx-border-style: dashed; -fx-border-radius: 24;"); // apply inline css styling
		Label lblHint = new Label("Seed Spreaders."); // execute statement
		lblHint.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 16px;"); // apply inline css styling
		heroPlaceholder.getChildren().add(lblHint); // append child node to container
		return heroPlaceholder; // return heroplaceholder
	}

	private Image getIconImage() { // handle geticonimage operation
		if (cachedIconImg == null) cachedIconImg = loadLogoImage(LOGO_ICON_PATH); // check condition before proceeding
		return cachedIconImg; // return cachediconimg
	}

	private Image getHeroImage() { // handle getheroimage operation
		if (cachedHeroImg == null) cachedHeroImg = loadLogoImage(LOGO_HERO_PATH); // check condition before proceeding
		return cachedHeroImg; // return cachedheroimg
	}

	// theme & bilingual helpers
	public String bgPage() { // handle bgpage operation
		return darkMode ? "#111916" : "#f5f7f2"; // return darkmode ? "#111916" : "#f5f7f2"
	}

	public String bgCard() { // handle bgcard operation
		return darkMode ? "#1a2621" : "#ffffff"; // return darkmode ? "#1a2621" : "#ffffff"
	}

	public String bgHeaderBar() { // handle bgheaderbar operation
		return darkMode ? "#15201c" : "#ffffff"; // return darkmode ? "#15201c" : "#ffffff"
	}

	public String bgTableHeader() { // handle bgtableheader operation
		return darkMode ? "#22312b" : "#f0f2ec"; // return darkmode ? "#22312b" : "#f0f2ec"
	}

	public String borderColor() { // handle bordercolor operation
		return darkMode ? "#2c3f37" : "#e2e6dc"; // return darkmode ? "#2c3f37" : "#e2e6dc"
	}

	public String textPrimary() { // handle textprimary operation
		return darkMode ? "#eef3ef" : "#111815"; // return darkmode ? "#eef3ef" : "#111815"
	}

	public String textMuted() { // handle textmuted operation
		return darkMode ? "#9cb0a6" : "#66736c"; // return darkmode ? "#9cb0a6" : "#66736c"
	}

	public String inputStyle() { // handle inputstyle operation
		return "-fx-background-color: " + (darkMode ? "#131d19" : "#ffffff") + ";" // return computed result
				+ "-fx-text-fill: " + textPrimary() + ";" // continue statement
				+ "-fx-prompt-text-fill: " + textMuted() + ";" // continue statement
				+ "-fx-border-color: " + borderColor() + ";" // continue statement
				+ "-fx-border-radius: 8;" // continue statement
				+ "-fx-background-radius: 8;" // continue statement
				+ "-fx-padding: 9 12 9 12;" // continue statement
				+ "-fx-font-size: 13px;"; // execute statement
	}

	public String primaryButtonStyle() { // handle primarybuttonstyle operation
		return "-fx-background-color: " + PRIMARY_GREEN + ";" // return computed result
				+ "-fx-text-fill: #ffffff;" // continue statement
				+ "-fx-font-weight: bold;" // continue statement
				+ "-fx-font-size: 13px;" // continue statement
				+ "-fx-background-radius: 10;" // continue statement
				+ "-fx-padding: 10 20 10 20;" // continue statement
				+ "-fx-cursor: hand;"; // execute statement
	}

	public String outlineButtonStyle() { // handle outlinebuttonstyle operation
		return "-fx-background-color: " + bgCard() + ";" // return computed result
				+ "-fx-text-fill: " + (darkMode ? "#cde3d8" : PRIMARY_GREEN) + ";" // continue statement
				+ "-fx-border-color: " + borderColor() + ";" // continue statement
				+ "-fx-border-radius: 10;" // continue statement
				+ "-fx-background-radius: 10;" // continue statement
				+ "-fx-font-weight: bold;" // continue statement
				+ "-fx-font-size: 13px;" // continue statement
				+ "-fx-padding: 9 18 9 18;" // continue statement
				+ "-fx-cursor: hand;"; // execute statement
	}

	public String dangerOutlineButtonStyle() { // handle dangeroutlinebuttonstyle operation
		return "-fx-background-color: " + bgCard() + ";" // return computed result
				+ "-fx-text-fill: #dc3545;" // continue statement
				+ "-fx-border-color: " + borderColor() + ";" // continue statement
				+ "-fx-border-radius: 10;" // continue statement
				+ "-fx-background-radius: 10;" // continue statement
				+ "-fx-font-weight: bold;" // continue statement
				+ "-fx-font-size: 13px;" // continue statement
				+ "-fx-padding: 9 18 9 18;" // continue statement
				+ "-fx-cursor: hand;"; // execute statement
	}

	public String cardStyle() { // handle cardstyle operation
		return "-fx-background-color: " + bgCard() + ";" // return computed result
				+ "-fx-border-color: " + borderColor() + ";" // continue statement
				+ "-fx-border-radius: 16;" // continue statement
				+ "-fx-background-radius: 16;" // continue statement
				+ "-fx-padding: 20;"; // execute statement
	}

	public String t(String en, String tl) { // handle t operation
		return tagalog ? tl : en; // return tagalog ? tl : en
	}

	public void applyScene(Scene scene, String title) { // handle applyscene operation
		scene.getStylesheets().clear(); // execute statement
		String tableCss = "data:text/css," // continue statement
				+ ".table-view { -fx-background-color: transparent; -fx-border-color: transparent; }" // continue statement
				+ ".table-view .column-header-background { -fx-background-color: " + bgTableHeader() + "; -fx-background-radius: 8; }" // continue statement
				+ ".table-view .column-header { -fx-background-color: transparent; -fx-size: 40px; }" // continue statement
				+ ".table-view .column-header .label { -fx-text-fill: " + textPrimary() + "; -fx-font-weight: bold; -fx-font-size: 12px; }" // continue statement
				+ ".table-view .table-row-cell { -fx-background-color: " + bgCard() + "; -fx-border-color: transparent transparent " + borderColor() + " transparent; -fx-cell-size: 46px; }" // continue statement
				+ ".table-view .table-row-cell:selected { -fx-background-color: " + (darkMode ? "#253b32" : "#e8f2ec") + "; }" // continue statement
				+ ".table-view .table-cell { -fx-text-fill: " + textPrimary() + "; -fx-alignment: CENTER-LEFT; -fx-font-size: 13px; }" // continue statement
				+ ".scroll-pane { -fx-background-color: transparent; -fx-background: transparent; }" // continue statement
				+ ".scroll-pane > .viewport { -fx-background-color: transparent; }"; // execute statement
		scene.getStylesheets().add(tableCss); // execute statement
		mainStage.setScene(scene); // execute statement
		mainStage.setTitle(title); // execute statement
		mainStage.setResizable(true); // execute statement
		mainStage.setMinWidth(960); // configure node width constraint
		mainStage.setMinHeight(640); // configure node height constraint
	}

	// shared shell navigation & top header helpers
	public Button createNavButton(Runnable refreshShell, String label, String routeKey, boolean isAdmin) { // handle createnavbutton operation
		boolean active = currentRoute.equals(routeKey); // execute statement
		Button btn = new Button("  " + getNavIcon(routeKey) + "   " + label); // execute statement
		btn.setMaxWidth(Double.MAX_VALUE); // configure node width constraint
		btn.setAlignment(Pos.CENTER_LEFT); // set container alignment
		btn.setStyle("-fx-background-color: " + (active ? PRIMARY_GREEN : "transparent") + ";" // apply inline css styling
				+ "-fx-text-fill: #ffffff;" // continue statement
				+ "-fx-font-size: 14px;" // continue statement
				+ "-fx-font-weight: " + (active ? "bold" : "normal") + ";" // continue statement
				+ "-fx-background-radius: 12;" // continue statement
				+ "-fx-padding: 13 16 13 16;" // continue statement
				+ "-fx-cursor: hand;"); // execute statement
		btn.setOnAction(e -> { // attach button click event handler
			currentRoute = routeKey; // execute statement
			refreshShell.run(); // execute statement
		}); // execute statement
		return btn; // return btn
	}

	public String getNavIcon(String route) { // handle getnavicon operation
		switch (route) { // open code block
			case "Dashboard": // continue statement
				return "\u229e"; // return "\u229e"
			case "Inventory": // continue statement
				return "\u2b21"; // return "\u2b21"
			case "Farmers": // continue statement
				return "\u2687"; // return "\u2687"
			case "Requests": // continue statement
				return "\ud83c\udf31"; // return "\ud83c\udf31"
			case "Transactions": // continue statement
				return "\u25a4"; // return "\u25a4"
			case "Donations": // continue statement
				return "\u2665"; // return "\u2665"
			default: // continue statement
				return "\u2022"; // return "\u2022"
		}
	}

	public String getAdminPageTitle() { // handle getadminpagetitle operation
		switch (currentRoute) { // open code block
			case "Inventory": // continue statement
				return t("Inventory", "Imbentaryo"); // return t("inventory", "imbentaryo")
			case "Farmers": // continue statement
				return t("User Management", "Pamamahala ng Users"); // return computed result
			case "Transactions": // continue statement
				return t("Transactions", "Mga Transaksyon"); // return computed result
			case "Donations": // continue statement
				return t("Donations", "Mga Donasyon"); // return t("donations", "mga donasyon")
			default: // continue statement
				return t("Dashboard", "Dashboard"); // return t("dashboard", "dashboard")
		}
	}

	public String getFarmerPageTitle() { // handle getfarmerpagetitle operation
		switch (currentRoute) { // open code block
			case "Requests": // continue statement
				return t("Requests", "Humiram ng Binhi"); // return t("requests", "humiram ng binhi")
			case "Transactions": // continue statement
				return t("Transactions", "Mga Transaksyon"); // return computed result
			default: // continue statement
				return t("Dashboard", "Dashboard"); // return t("dashboard", "dashboard")
		}
	}

	public HBox buildTopHeader(String pageTitle, boolean isAdmin, Runnable refreshShell) { // handle buildtopheader operation
		HBox header = new HBox(12); // execute statement
		header.setAlignment(Pos.CENTER_LEFT); // set container alignment

		Label lblTitle = new Label(pageTitle); // execute statement
		lblTitle.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + textPrimary() + ";"); // apply inline css styling

		Region spacer = new Region(); // execute statement
		HBox.setHgrow(spacer, Priority.ALWAYS); // allow horizontal growth inside hbox

		// contextual help for the current tab
		Button btnHelp = new Button("? Help"); // execute statement
		btnHelp.setStyle(outlineButtonStyle()); // apply inline css styling
		btnHelp.setOnAction(e -> showTabHelpDialog(currentRoute, isAdmin)); // attach button click event handler

		// user avatar with initials
		StackPane avatar = new StackPane(); // execute statement
		Circle circle = new Circle(20, Color.web("#bc9d9d")); // execute statement
		String initials = currentUser.getUsername().substring(0, Math.min(2, currentUser.getUsername().length())).toUpperCase(); // execute statement
		Label lblInit = new Label(initials); // execute statement
		lblInit.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 12px;"); // apply inline css styling
		avatar.getChildren().addAll(circle, lblInit); // add child nodes to container

		header.getChildren().addAll(lblTitle, spacer, btnHelp, avatar); // add child nodes to container
		return header; // return header
	}

	public VBox createStatCard(String title, String value, String subtitle, String accentColor) { // handle createstatcard operation
		VBox card = new VBox(8); // execute statement
		card.setStyle(cardStyle()); // apply inline css styling
		HBox.setHgrow(card, Priority.ALWAYS); // allow horizontal growth inside hbox
		card.setPrefWidth(220); // configure node width constraint

		Label lblTitle = new Label(title); // execute statement
		lblTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + textPrimary() + ";"); // apply inline css styling

		Label lblVal = new Label(value); // execute statement
		lblVal.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + accentColor + ";"); // apply inline css styling

		Label lblSub = new Label(subtitle); // execute statement
		lblSub.setStyle("-fx-font-size: 11px; -fx-text-fill: " + textMuted() + ";"); // apply inline css styling

		card.getChildren().addAll(lblTitle, lblVal, lblSub); // add child nodes to container
		return card; // return card
	}

	public HBox createAlertRow(String badgeText, String detail, String bgHex, String fgHex) { // handle createalertrow operation
		HBox row = new HBox(10); // execute statement
		row.setAlignment(Pos.CENTER_LEFT); // set container alignment
		row.setPadding(new Insets(10, 12, 10, 12)); // set inner padding spacing
		row.setStyle("-fx-background-color: " + bgPage() + "; -fx-background-radius: 10;"); // apply inline css styling

		Label badge = new Label(badgeText); // execute statement
		badge.setStyle("-fx-background-color: " + bgHex + "; -fx-text-fill: " + fgHex + "; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 3 8 3 8; -fx-background-radius: 6;"); // apply inline css styling

		Label lbl = new Label(detail); // execute statement
		lbl.setStyle("-fx-text-fill: " + textPrimary() + "; -fx-font-size: 12px;"); // apply inline css styling

		row.getChildren().addAll(badge, lbl); // add child nodes to container
		return row; // return row
	}

	public Label makeDetailLine(String key, String val) { // handle makedetailline operation
		Label l = new Label(key + "  " + val); // execute statement
		l.setStyle("-fx-text-fill: " + textPrimary() + "; -fx-font-size: 12px;"); // apply inline css styling
		return l; // return l
	}


	public VBox wrapField(String labelText, Node control) { // handle wrapfield operation
		Label lbl = new Label(labelText); // execute statement
		lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: " + textPrimary() + ";"); // apply inline css styling
		return new VBox(5, lbl, control); // return new vbox(5, lbl, control)
	}

	public VBox makeReadOnlyBox(String label, String val) { // handle makereadonlybox operation
		Label lbl = new Label(label); // execute statement
		lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: " + textPrimary() + ";"); // apply inline css styling
		TextField tf = new TextField(val); // execute statement
		tf.setEditable(false); // execute statement
		tf.setStyle(inputStyle()); // apply inline css styling
		return new VBox(5, lbl, tf); // return new vbox(5, lbl, tf)
	}

	public <T> TableCell<T, String> createStatusBadgeCell() { // open code block
		return new TableCell<T, String>() { // return new tablecell<t, string>()
			@Override // override parent method
			protected void updateItem(String item, boolean empty) { // handle updateitem operation
				super.updateItem(item, empty); // execute statement
				if (empty || item == null) { // check condition before proceeding
					setGraphic(null); // execute statement
				} else { // handle alternative branch
					Label badge = new Label(item); // execute statement
					String bg = "#fef3c7"; // execute statement
					String fg = "#92400e"; // execute statement
					if ("Approved".equalsIgnoreCase(item) || "Accepted".equalsIgnoreCase(item)) { // check condition before proceeding
						bg = "#dcfce7"; // execute statement
						fg = "#166534"; // execute statement
					} else if ("Rejected".equalsIgnoreCase(item)) { // check fallback condition
						bg = "#fde8e8"; // execute statement
						fg = "#9b1c1c"; // execute statement
					}
					badge.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg + ";" // apply inline css styling
							+ "-fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 4 10 4 10; -fx-background-radius: 8;"); // execute statement
					setGraphic(badge); // execute statement
				}
			}
		}; // execute statement
	}

	// per-tab help manual modal (low priority: help button per tab)
	public void showTabHelpDialog(String route, boolean isAdmin) { // handle showtabhelpdialog operation
		Stage helpStage = new Stage(); // execute statement
		helpStage.initOwner(mainStage); // execute statement
		helpStage.initModality(Modality.APPLICATION_MODAL); // execute statement
		helpStage.initStyle(StageStyle.UTILITY); // execute statement
		helpStage.setTitle(t("User Manual \u2014 ", "Gabay sa Paggamit \u2014 ") + route); // execute statement

		VBox card = new VBox(14); // execute statement
		card.setPadding(new Insets(24)); // set inner padding spacing
		card.setStyle("-fx-background-color: " + bgCard() + ";"); // apply inline css styling

		Label lblTitle = new Label(t("Help & Feature Overview: ", "Tulong at Guide sa Tab na Ito: ") + route); // execute statement
		lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + PRIMARY_GREEN + ";"); // apply inline css styling

		String guideText; // execute statement
		if (isAdmin) { // check condition before proceeding
			switch (route) { // open code block
				case "Inventory": // continue statement
					guideText = t( // continue statement
							"\u2022 View live metrics for Plant Varieties, Seed Stock (kg), and Storage Lots.\n" // continue statement
									+ "\u2022 Click '+ Add Inventory Slot' to register new seed lots with unit, minimum threshold, and expiry.\n" // continue statement
									+ "\u2022 Click 'View' on any lot row to inspect, edit, or delete that seed product.\n" // continue statement
									+ "\u2022 Changes automatically save to products.txt.", // continue statement
							"\u2022 Kita agad dito kung ilang variety, ilang kilo ang stock, at ilang seed lot ang meron.\n" // continue statement
									+ "\u2022 I-click ang '+ Magdagdag ng Seed Lot' para magpasok ng bagong binhi.\n" // continue statement
									+ "\u2022 I-click ang 'Tingnan' sa kahit anong row para i-edit o i-delete ang binhi.\n" // continue statement
									+ "\u2022 Automatic nang nase-save sa products.txt ang lahat ng changes."); // execute statement
					break; // exit current loop
				case "Farmers": // continue statement
					guideText = t( // continue statement
							"\u2022 Manage all registered Farmers and Admins in the system.\n" // continue statement
									+ "\u2022 Click '+ Add Farm / User Account' to create a new Farmer or Admin account directly.\n" // continue statement
									+ "\u2022 Click 'View' on any account to inspect Farmer Info, Farm Info, and Pending Seed Requests/Returns.\n" // continue statement
									+ "\u2022 Inside View Details, you can Verify pending farmers, Assign/Revoke Admin privileges, or Suspend/Unsuspend users.", // continue statement
							"\u2022 Dito mo ma-manage lahat ng registered Farmers at Admins.\n" // continue statement
									+ "\u2022 I-click ang '+ Magdagdag ng Account' para gumawa ng bagong Farmer o Admin account.\n" // continue statement
									+ "\u2022 I-click ang 'Tingnan' para makita ang Farmer Info, Farm Info, at Pending Requests/Returns.\n" // continue statement
									+ "\u2022 Sa loob ng View Details, pwede mong i-Verify ang farmer, Gawing Admin, o I-suspend ang user."); // execute statement
					break; // exit current loop
				case "Transactions": // continue statement
					guideText = t( // continue statement
							"\u2022 Review incoming farmer seed requests in the top table and click 'View' to Approve or Reject.\n" // continue statement
									+ "\u2022 Approving a request automatically deducts seed stock and sets a return deadline.\n" // continue statement
									+ "\u2022 Inspect returned seeds in the bottom table and click 'Accept' to replenish inventory stock.", // continue statement
							"\u2022 Tingnan ang mga hiling na binhi sa taas at i-click ang 'Tingnan' para i-Approve o i-Reject.\n" // continue statement
									+ "\u2022 Pag na-approve, automatic na mababawas sa stock at magkakaroon ng return due date.\n" // continue statement
									+ "\u2022 Sa baba naman, i-click ang 'Tanggapin' sa mga ibinalik na binhi para madagdag ulit sa stock."); // execute statement
					break; // exit current loop
				case "Donations": // continue statement
					guideText = t( // continue statement
							"\u2022 View all monetary donations contributed from the public landing page.\n" // continue statement
									+ "\u2022 Tracks donor names, emails, amounts, and messages.", // continue statement
							"\u2022 Kita dito lahat ng donasyong pumasok galing sa public landing page.\n" // continue statement
									+ "\u2022 Naka-list dito ang pangalan ng donor, email, halaga, at message nila."); // execute statement
					break; // exit current loop
				default: // continue statement
					guideText = t( // continue statement
							"\u2022 Overview of Pending Seed Requests, Pending Seed Returns, Missing Returns (Late Deadlines), and Inventory Stock.\n" // continue statement
									+ "\u2022 Quickly identify overdue loans highlighted in red and jump straight to User Management.", // continue statement
							"\u2022 Quick summary ng Pending Seed Requests, Pending Seed Returns, Late Returns, at lagay ng Inventory.\n" // continue statement
									+ "\u2022 Kita agad ang mga overdue na hiram (naka-red badge) at pwede kang dumiretso sa User Management."); // execute statement
					break; // exit current loop
			}
		} else { // handle alternative branch
			switch (route) { // open code block
				case "Requests": // continue statement
					guideText = t( // continue statement
							"\u2022 Browse available seed lots in the Seed Bank table on the left.\n" // continue statement
									+ "\u2022 Select a seed lot, enter the requested weight (kg) and planting purpose on the right, and submit.\n" // continue statement
									+ "\u2022 Note: Unverified farmer accounts have limited access until verified by an Admin.", // continue statement
							"\u2022 Pumili ng available na binhi sa table sa kaliwa.\n" // continue statement
									+ "\u2022 Ilagay sa kanan kung ilang kilo (kg) ang kailangan at kung para saan itatanim, tapos i-submit.\n" // continue statement
									+ "\u2022 Paalala: Kailangan munang ma-verify ng Admin ang account mo bago makahiram."); // execute statement
					break; // exit current loop
				case "Transactions": // continue statement
					guideText = t( // continue statement
							"\u2022 Track the approval status and return due dates of your seed requests.\n" // continue statement
									+ "\u2022 Click '\u21a9 Submit Seed Return' to return harvested seeds from an approved loan.", // continue statement
							"\u2022 I-check dito kung na-approve na ang request mo at kailan ang due date ng pagbabalik.\n" // continue statement
									+ "\u2022 I-click ang '\u21a9 Magbalik ng Binhi' para mag-submit ng ibabalik na binhi pagka-ani."); // execute statement
					break; // exit current loop
				default: // continue statement
					guideText = t( // continue statement
							"\u2022 View your Pending Seed Requests, Pending Seed Returns, and Return Deadlines at a glance.\n" // continue statement
									+ "\u2022 Update your Phone Number, Email, Farm Name, and Farm Address directly in the Basic Information card.", // continue statement
							"\u2022 Kita agad dito ang Pending Seed Requests, Pending Seed Returns, at mga Due Date mo.\n" // continue statement
									+ "\u2022 Pwede mo ring i-edit dito mismo ang Cellphone Number, Email, Farm Name, at Farm Address mo."); // execute statement
					break; // exit current loop
			}
		}

		Label lblBody = new Label(guideText); // execute statement
		lblBody.setWrapText(true); // enable text wrapping
		lblBody.setStyle("-fx-text-fill: " + textPrimary() + "; -fx-font-size: 13px; -fx-line-spacing: 4;"); // apply inline css styling

		Button btnOk = new Button(t("Got it", "Sige, gets!")); // execute statement
		btnOk.setStyle(primaryButtonStyle()); // apply inline css styling
		btnOk.setOnAction(e -> helpStage.close()); // attach button click event handler

		HBox footer = new HBox(btnOk); // execute statement
		footer.setAlignment(Pos.CENTER_RIGHT); // set container alignment

		card.getChildren().addAll(lblTitle, lblBody, footer); // add child nodes to container
		helpStage.setScene(new Scene(card, 480, 260)); // execute statement
		helpStage.showAndWait(); // execute statement
	}

	// file persistence (users, products, donations)
	public ObservableList<User> loadUsers(java.nio.file.Path path) { // handle loadusers operation
		ObservableList<User> list = FXCollections.observableArrayList(); // execute statement
		try { // start protected execution block
			if (Files.exists(path)) { // check condition before proceeding
				for (String line : Files.readAllLines(path)) { // iterate through collection items
					if (line.trim().isEmpty()) continue; // check condition before proceeding
					String[] p = line.split("\\|", -1); // execute statement
					if (p.length >= 12) { // check condition before proceeding
						list.add(new User(p[0], p[1], p[2], p[3], p[4], p[5], p[6], p[7], p[8], p[9], p[10], p[11])); // execute statement
					} else if (p.length >= 11) { // check fallback condition
						list.add(new User(p[0], p[1], p[2], p[3], p[4], p[5], p[6], p[7], p[8], p[9], p[10], "")); // execute statement
					}
				}
			}
		} catch (IOException e) { // handle caught exception
			e.printStackTrace(); // execute statement
		}
		if (list.isEmpty()) { // check condition before proceeding
			list.addAll( // continue statement
					new User("ADM-001", "admin", "admin123", "Admin", "09171000001", "admin@seedspreaders.ph", // create user account entry
							"UPLB Central Seed Bank", "Los Ba\u00f1os, Laguna", "ADMIN-01", "5.0 ha", "Verified"), // continue statement
					new User("FRM-001", "juan", "farmer123", "Farmer", "09172223333", "juan@lagunafarms.ph", // create user account entry
							"Dela Cruz Farm", "Bay, Laguna", "RSBSA-04-034-0012", "2.5 ha", "Verified"), // continue statement
					new User("FRM-002", "maria", "farmer123", "Farmer", "09174445555", "maria@lagunafarms.ph", // create user account entry
							"Santos Agri-Farm", "Calauan, Laguna", "RSBSA-04-034-0089", "3.0 ha", "Verified")); // execute statement
			saveUsers(path, list); // execute statement
		}
		nextUserId = list.size() + 1; // execute statement
		return list; // return list
	}

	public void saveUsers(java.nio.file.Path path, ObservableList<User> data) { // handle saveusers operation
		try { // start protected execution block
			List<String> lines = new ArrayList<>(); // execute statement
			for (User u : data) { // iterate through collection items
				lines.add(String.join("|", // continue statement
						safe(u.getUserId()), safe(u.getUsername()), safe(u.getPassword()), safe(u.getRole()), // continue statement
						safe(u.getPhoneNumber()), safe(u.getEmail()), safe(u.getFarmName()), safe(u.getFarmAddress()), // continue statement
						safe(u.getRsbsaNumber()), safe(u.getFarmArea()), safe(u.getStatus()), // continue statement
						safe(u.getSuspensionReason()))); // execute statement
			}
			Files.write(path, lines); // execute statement
		} catch (IOException e) { // handle caught exception
			e.printStackTrace(); // execute statement
		}
	}

	public String safe(String s) { // handle safe operation
		return s == null ? "" : s.replace("|", "/").replace("\n", " "); // return computed result
	}

	public ObservableList<Product> loadProducts(java.nio.file.Path path) { // handle loadproducts operation
		ObservableList<Product> list = FXCollections.observableArrayList(); // execute statement
		try { // start protected execution block
			if (Files.exists(path)) { // check condition before proceeding
				for (String line : Files.readAllLines(path)) { // iterate through collection items
					if (line.trim().isEmpty()) continue; // check condition before proceeding
					String[] parts = line.split(","); // execute statement
					if (parts.length >= 10) { // check condition before proceeding
						list.add(new Product(parts[0].trim(), Double.parseDouble(parts[1].trim()), // continue statement
								Double.parseDouble(parts[2].trim()), Integer.parseInt(parts[3].trim()), // continue statement
								parts[4].trim(), parts[5].trim(), parts[6].trim(), // continue statement
								parts[7].trim(), Double.parseDouble(parts[8].trim()), parts[9].trim())); // execute statement
					} else if (parts.length >= 7) { // check fallback condition
						list.add(new Product(parts[0].trim(), Double.parseDouble(parts[1].trim()), // continue statement
								Double.parseDouble(parts[2].trim()), Integer.parseInt(parts[3].trim()), // continue statement
								parts[4].trim(), parts[5].trim(), parts[6].trim())); // execute statement
					}
				}
			}
		} catch (IOException | NumberFormatException e) { // handle caught exception
			e.printStackTrace(); // execute statement
		}
		return list; // return list
	}

	public void saveProducts(java.nio.file.Path path, ObservableList<Product> data) { // handle saveproducts operation
		try { // start protected execution block
			List<String> lines = new ArrayList<>(); // execute statement
			for (Product p : data) { // iterate through collection items
				lines.add(p.getName() + "," + p.getPrice() + "," + p.getAmount() + "," // continue statement
						+ p.getLotID() + "," + p.getLocation() + "," + p.getVariety() + "," // continue statement
						+ p.getDescription().replace(",", ";") + "," + p.getUnit() + "," // continue statement
						+ p.getMinThreshold() + "," + p.getExpiryDate()); // execute statement
			}
			Files.write(path, lines); // execute statement
		} catch (IOException e) { // handle caught exception
			e.printStackTrace(); // execute statement
		}
	}

	public ObservableList<Donation> loadDonations(java.nio.file.Path path) { // handle loaddonations operation
		ObservableList<Donation> list = FXCollections.observableArrayList(); // execute statement
		try { // start protected execution block
			if (Files.exists(path)) { // check condition before proceeding
				for (String line : Files.readAllLines(path)) { // iterate through collection items
					if (line.trim().isEmpty()) continue; // check condition before proceeding
					String[] parts = line.split(",", 4); // execute statement
					if (parts.length < 3) continue; // check condition before proceeding
					list.add(new Donation(parts[0].trim(), parts[1].trim(), // continue statement
							Double.parseDouble(parts[2].trim()), parts.length > 3 ? parts[3].trim() : "")); // execute statement
				}
			}
		} catch (IOException | NumberFormatException e) { // handle caught exception
			e.printStackTrace(); // execute statement
		}
		return list; // return list
	}

	public void saveDonations(java.nio.file.Path path, ObservableList<Donation> data) { // handle savedonations operation
		try { // start protected execution block
			List<String> lines = new ArrayList<>(); // execute statement
			for (Donation d : data) { // iterate through collection items
				String msg = d.getMessage().replace("\n", " ").replace("\r", " "); // execute statement
				lines.add(d.getDonorName() + "," + d.getEmail() + "," + d.getAmount() + "," + msg); // execute statement
			}
			try (PrintWriter writer = new PrintWriter(path.toFile())) { // open code block
				for (String line : lines) writer.println(line); // iterate through collection items
			}
		} catch (IOException e) { // handle caught exception
			e.printStackTrace(); // execute statement
		}
	}

	public Product findProduct(int lotId) { // handle findproduct operation
		for (Product p : productData) { // iterate through collection items
			if (p.getLotID() == lotId) return p; // check condition before proceeding
		}
		return null; // return null
	}

	public double round2(double value) { // handle round2 operation
		return Math.round(value * 100.0) / 100.0; // return math.round(value * 100.0) / 100.0
	}
}
