package application.admin; // admin views and management workflows

import application.Donation; // donation model
import application.Product; // seed inventory model
import application.SeedRequest; // seed request model
import application.SeedReturn; // seed return model
import application.User; // user account model
import application.shared.AppState; // shared data state and ui helpers
import javafx.beans.property.SimpleStringProperty; // computed string property for table columns
import javafx.collections.transformation.FilteredList; // live search filter wrapper
import javafx.geometry.Insets; // layout padding
import javafx.geometry.Pos; // container alignment
import javafx.scene.Node; // scene graph node
import javafx.scene.Scene; // application scene
import javafx.scene.control.Button; // button control
import javafx.scene.control.ComboBox; // dropdown selector
import javafx.scene.control.Label; // text label
import javafx.scene.control.PasswordField; // password input
import javafx.scene.control.ScrollPane; // scrollable viewport
import javafx.scene.control.Separator; // visual divider line
import javafx.scene.control.TableCell; // custom table cell
import javafx.scene.control.TableColumn; // table column
import javafx.scene.control.TableView; // table view
import javafx.scene.control.TextField; // text input
import javafx.scene.control.cell.PropertyValueFactory; // model property binding
import javafx.scene.layout.BorderPane; // main shell layout
import javafx.scene.layout.ColumnConstraints; // grid column sizing
import javafx.scene.layout.GridPane; // form grid layout
import javafx.scene.layout.HBox; // horizontal container
import javafx.scene.layout.Priority; // flex growth priority
import javafx.scene.layout.Region; // flexible spacer
import javafx.scene.layout.VBox; // vertical container
import javafx.stage.Modality; // modal dialog modality
import javafx.stage.Stage; // stage window
import javafx.stage.StageStyle; // stage border style

import java.time.LocalDate; // current date and due date calculation
import java.util.List; // list collection
import java.util.stream.Collectors; // stream filtering helper

// handles all admin dashboard tabs, user management, inventory, and transactions
public class AdminPages { // declare adminpages class
	private final Stage mainStage; // primary application stage
	private final AppState state; // shared application state
	private final Runnable onLogout; // callback returning to landing page

	private String activeTab = "Overview"; // currently selected sidebar route
	private BorderPane shellRoot; // root shell container for swapping tab content
	private Label lblPageTitle; // top bar page heading

	// constructor for admin screens controller
	public AdminPages(Stage stage, AppState state, Runnable onLogout) { // define adminpages method
		this.mainStage = stage; // store primary stage
		this.state = state; // store shared state
		this.onLogout = onLogout; // store logout callback
	}

	// display the admin shell with persistent sidebar, header, and active tab
	public void showAdminShell() { // define showadminshell method
		shellRoot = new BorderPane(); // initialize root border pane
		shellRoot.setStyle("-fx-background-color: " + AppState.BG_PAGE + ";"); // set warm off-white background

		shellRoot.setLeft(buildSidebar()); // attach left forest-green navigation sidebar
		shellRoot.setTop(buildTopHeader()); // attach top bar with title and help button
		switchTab(activeTab); // load active route content into center

		Scene scene = new Scene(shellRoot, 1220, 760); // create admin scene
		mainStage.setScene(scene); // display admin scene
		mainStage.setTitle("Seed Spreaders - Admin Dashboard"); // set window title
	}

	// build the left navigation sidebar with logo, route buttons, and logout
	private VBox buildSidebar() { // define buildsidebar helper
		VBox sidebar = new VBox(8); // vertical sidebar stack
		sidebar.setPrefWidth(245); // fixed sidebar width
		sidebar.setMinWidth(245); // prevent shrinking
		sidebar.setPadding(new Insets(24, 16, 22, 16)); // inner sidebar padding
		sidebar.setStyle("-fx-background-color: " + AppState.FOREST_DARK + ";"); // dark forest green fill

		Node logo = state.buildLogo(0.92, true); // build white logo lockup for dark sidebar
		VBox logoBox = new VBox(logo); // wrapper for bottom margin
		logoBox.setPadding(new Insets(0, 6, 24, 6)); // space below logo

		Button btnOverview = createNavButton("Overview", "Overview"); // dashboard overview route
		Button btnInventory = createNavButton("Inventory", "Inventory"); // seed inventory route
		Button btnUsers = createNavButton("User Management", "Users"); // user management route
		Button btnTransactions = createNavButton("Transactions", "Transactions"); // requests & returns route
		Button btnDonations = createNavButton("Donations", "Donations"); // community donations route

		Region spacer = new Region(); // push user card and logout to bottom
		VBox.setVgrow(spacer, Priority.ALWAYS); // fill remaining vertical space

		Label lblRole = new Label("ADMINISTRATOR"); // role badge text
		lblRole.setStyle("-fx-text-fill: #8ec9a4; -fx-font-size: 10px; -fx-font-weight: bold;"); // sage green text

		Label lblName = new Label(state.currentUser != null ? state.currentUser.getFullName() : "Admin"); // active admin name
		lblName.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px; -fx-font-weight: bold;"); // white bold text

		VBox userCard = new VBox(3, lblRole, lblName); // session info box
		userCard.setPadding(new Insets(12)); // inner padding
		userCard.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10;"); // translucent dark card

		Button btnLogout = new Button("Log Out"); // logout button
		btnLogout.setMaxWidth(Double.MAX_VALUE); // full sidebar width
		btnLogout.setStyle("-fx-background-color: rgba(255,255,255,0.12); -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 12px; -fx-background-radius: 8; -fx-padding: 10 14 10 14; -fx-cursor: hand;"); // subtle light button on dark green
		btnLogout.setOnAction(e -> { // attach click listener
			state.saveUsers(); // save user updates before logout
			state.saveProducts(); // save inventory updates before logout
			state.saveRequests(); // save request updates before logout
			state.saveReturns(); // save return updates before logout
			state.currentUser = null; // clear active session
			onLogout.run(); // navigate back to landing page
		});

		sidebar.getChildren().addAll(logoBox, btnOverview, btnInventory, btnUsers, btnTransactions, btnDonations, spacer, userCard, btnLogout); // assemble sidebar items
		return sidebar; // return sidebar container
	}

	// build a styled sidebar navigation button
	private Button createNavButton(String label, String routeKey) { // define createnavbutton helper
		Button btn = new Button(label); // create route button
		btn.setMaxWidth(Double.MAX_VALUE); // stretch across sidebar
		btn.setAlignment(Pos.CENTER_LEFT); // align text left
		boolean selected = activeTab.equals(routeKey); // check if currently active route
		if (selected) { // active tab styling
			btn.setStyle("-fx-background-color: #1d6b50; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 9; -fx-padding: 11 16 11 16; -fx-cursor: hand;"); // highlighted green pill
		} else { // inactive tab styling
			btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #cfe0d7; -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 9; -fx-padding: 11 16 11 16; -fx-cursor: hand;"); // muted sage text
		}
		btn.setOnAction(e -> switchTab(routeKey)); // switch tab when clicked
		return btn; // return styled nav button
	}

	// build the top header bar with page title and per-tab help button (no bell icon)
	private HBox buildTopHeader() { // define buildtopheader helper
		lblPageTitle = new Label("Admin Overview"); // dynamic page heading
		lblPageTitle.setStyle("-fx-font-size: 21px; -fx-font-weight: bold; -fx-text-fill: " + AppState.TEXT_DARK + ";"); // dark heading text

		Region spacer = new Region(); // push help button right
		HBox.setHgrow(spacer, Priority.ALWAYS); // expand spacer horizontally

		Button btnHelp = new Button("? Help"); // per-tab manual help button
		btnHelp.setStyle(state.outlineButtonStyle()); // outline button style
		btnHelp.setOnAction(e -> openTabHelp()); // open feature overview for current tab

		HBox header = new HBox(12, lblPageTitle, spacer, btnHelp); // header bar container
		header.setAlignment(Pos.CENTER_LEFT); // align vertically
		header.setPadding(new Insets(18, 28, 18, 28)); // outer header padding
		header.setStyle("-fx-background-color: #ffffff; -fx-border-color: transparent transparent #e3ebe6 transparent;"); // white bar with bottom border
		return header; // return header bar
	}

	// switch the active tab content and refresh sidebar highlight
	private void switchTab(String routeKey) { // define switchtab helper
		this.activeTab = routeKey; // record active route
		shellRoot.setLeft(buildSidebar()); // rebuild sidebar to update active highlight

		Node content; // target tab view node
		switch (routeKey) { // continue statement
			case "Inventory": // continue statement
				lblPageTitle.setText("Seed Inventory"); // update header title
				content = createInventoryPane(); // build inventory view
				break; // exit switch block
			case "Users": // continue statement
				lblPageTitle.setText("User Management"); // update header title
				content = createUserManagementPane(); // build user management view
				break; // exit switch block
			case "Transactions": // continue statement
				lblPageTitle.setText("Seed Requests & Returns"); // update header title
				content = createAdminTransactionsPane(); // build transactions view
				break; // exit switch block
			case "Donations": // continue statement
				lblPageTitle.setText("Community Donations"); // update header title
				content = createAdminDonationsPane(); // build donations view
				break; // exit switch block
			default: // default route case
				lblPageTitle.setText("Admin Overview"); // update header title
				content = createOverviewPane(); // build overview dashboard
				break; // exit switch block
		}

		ScrollPane scroller = new ScrollPane(content); // wrap tab content in scroll pane
		scroller.setFitToWidth(true); // stretch content to viewport width
		scroller.setStyle("-fx-background: " + AppState.BG_PAGE + "; -fx-background-color: " + AppState.BG_PAGE + ";"); // match page background
		shellRoot.setCenter(scroller); // set center content
	}

	// show feature guide popup tailored to the currently active admin tab
	private void openTabHelp() { // define opentabhelp helper
		switch (activeTab) { // continue statement
			case "Inventory": // continue statement
				state.showTabHelpDialog("Seed Inventory", // pass value
						"\u2022 Monitor total plant varieties, available seed stock (kg), and storage lots.\n" // continue statement
								+ "\u2022 Click '+ Add Seed Lot' to register new seed batches in the bank.\n" // continue statement
								+ "\u2022 Click 'View Details' on any row to edit prices, quantities, or delete a lot."); // inventory help text
				break; // exit switch block
			case "Users": // continue statement
				state.showTabHelpDialog("User Management", // pass value
						"\u2022 Click '+ Create Account' to register new Farmer or Admin accounts directly (pre-verified).\n" // continue statement
								+ "\u2022 Click 'View Details' on any account to inspect Farmer Info, Farm Info, and Pending Seed Requests/Returns.\n" // continue statement
								+ "\u2022 Inside View Details, you can Assign/Revoke Admin privileges or Suspend/Unsuspend users with a reason."); // user management help text
				break; // exit switch block
			case "Transactions": // continue statement
				state.showTabHelpDialog("Seed Requests & Returns", // pass value
						"\u2022 Review pending farmer seed requests in the top table and Approve or Reject them.\n" // continue statement
								+ "\u2022 Approving a request deducts seed stock and assigns a 30-day harvest repayment due date.\n" // continue statement
								+ "\u2022 Accepting a harvest seed return replenishes stock and clears any overdue loan status."); // transactions help text
				break; // exit switch block
			case "Donations": // continue statement
				state.showTabHelpDialog("Community Donations", // pass value
						"\u2022 View all monetary contributions submitted from the public landing page.\n" // continue statement
								+ "\u2022 Tracks donor names, emails, amounts in PHP, and messages of support."); // donations help text
				break; // exit switch block
			default: // default route case
				state.showTabHelpDialog("Admin Overview", // pass value
						"\u2022 View live counts for Pending Seed Requests, Pending Seed Returns, Missing Returns (Late Deadlines), and Inventory Info.\n" // continue statement
								+ "\u2022 Inspect late/overdue seed loans directly in the Missing Returns table below."); // overview help text
				break; // exit switch block
		}
	}

	// =========================================================================
	// 1. OVERVIEW TAB (Pending Requests, Pending Returns, Missing Returns, Inventory Info)
	// =========================================================================
	private VBox createOverviewPane() { // define createoverviewpane helper
		long pendingReqCount = state.requestData.stream().filter(r -> "Pending".equalsIgnoreCase(r.getStatus())).count(); // count waiting requests
		long pendingRetCount = state.returnData.stream().filter(r -> "Pending".equalsIgnoreCase(r.getStatus())).count(); // count waiting returns
		List<SeedRequest> lateRequests = state.requestData.stream().filter(SeedRequest::isOverdue).collect(Collectors.toList()); // filter overdue active loans
		double totalStockKg = state.productData.stream().mapToDouble(Product::getAmount).sum(); // sum available stock kg

		VBox cardReq = buildMetricCard("Pending Seed Requests", String.valueOf(pendingReqCount), "Awaiting admin review", "#9a6700"); // metric card 1
		VBox cardRet = buildMetricCard("Pending Seed Returns", String.valueOf(pendingRetCount), "Awaiting quality inspection", "#17684d"); // metric card 2
		VBox cardLate = buildMetricCard("Missing Returns (Late)", String.valueOf(lateRequests.size()), "Past return deadline", "#b42318"); // metric card 3
		VBox cardInv = buildMetricCard("Inventory Info", String.format("%.1f kg", totalStockKg), state.productData.size() + " active storage lots", AppState.FOREST_DARK); // metric card 4

		HBox metricsRow = new HBox(16, cardReq, cardRet, cardLate, cardInv); // horizontal KPI strip
		for (Node n : metricsRow.getChildren()) { // give all 4 cards equal width
			HBox.setHgrow(n, Priority.ALWAYS); // expand evenly
		}

		// Missing Returns (Late Deadline) Table Card
		Label lblLateTitle = new Label("Missing Returns (Late Deadline)"); // section title
		lblLateTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + AppState.TEXT_DARK + ";"); // bold heading

		Button btnGoTx = new Button("Review in Transactions \u2192"); // shortcut button to transactions tab
		btnGoTx.setStyle(state.outlineButtonStyle()); // outline style
		btnGoTx.setOnAction(e -> switchTab("Transactions")); // open transactions view

		Region spacer = new Region(); // spacer between title and shortcut button
		HBox.setHgrow(spacer, Priority.ALWAYS); // expand spacer
		HBox lateHeader = new HBox(10, lblLateTitle, spacer, btnGoTx); // header row
		lateHeader.setAlignment(Pos.CENTER_LEFT); // center vertically

		TableView<SeedRequest> lateTable = new TableView<>(); // table of overdue loans
		lateTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // stretch columns evenly
		lateTable.setPrefHeight(220); // table height
		lateTable.setPlaceholder(new Label("No missing returns! All active seed loans are within their return deadlines.")); // empty message

		TableColumn<SeedRequest, Integer> colId = new TableColumn<>("Request ID"); // id column
		colId.setCellValueFactory(new PropertyValueFactory<>("requestId")); // bind requestId

		TableColumn<SeedRequest, String> colFarmer = new TableColumn<>("Farmer"); // farmer column
		colFarmer.setCellValueFactory(new PropertyValueFactory<>("farmer")); // bind farmer username

		TableColumn<SeedRequest, String> colSeed = new TableColumn<>("Seed Variety"); // seed column
		colSeed.setCellValueFactory(new PropertyValueFactory<>("seedLabel")); // bind seedLabel

		TableColumn<SeedRequest, Double> colQty = new TableColumn<>("Borrowed (kg)"); // weight column
		colQty.setCellValueFactory(new PropertyValueFactory<>("amount")); // bind amount

		TableColumn<SeedRequest, String> colDue = new TableColumn<>("Due Date"); // deadline column
		colDue.setCellValueFactory(new PropertyValueFactory<>("dueDate")); // bind dueDate

		TableColumn<SeedRequest, String> colAlert = new TableColumn<>("Status"); // overdue badge column
		colAlert.setCellValueFactory(cell -> new SimpleStringProperty("OVERDUE")); // show OVERDUE pill
		colAlert.setCellFactory(state.createStatusBadgeCell()); // apply red badge renderer

		lateTable.getColumns().addAll(colId, colFarmer, colSeed, colQty, colDue, colAlert); // attach columns
		lateTable.getItems().setAll(lateRequests); // populate overdue items

		VBox lateCard = new VBox(12, lateHeader, lateTable); // wrap table in white card
		lateCard.setStyle(state.cardStyle()); // apply card css

		// Inventory Info Summary Table Card
		Label lblInvTitle = new Label("Inventory Info by Seed Lot"); // inventory summary heading
		lblInvTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + AppState.TEXT_DARK + ";"); // bold heading

		Button btnGoInv = new Button("Manage Inventory \u2192"); // shortcut to inventory tab
		btnGoInv.setStyle(state.outlineButtonStyle()); // outline button style
		btnGoInv.setOnAction(e -> switchTab("Inventory")); // open inventory view

		Region invSpacer = new Region(); // spacer
		HBox.setHgrow(invSpacer, Priority.ALWAYS); // expand spacer
		HBox invHeader = new HBox(10, lblInvTitle, invSpacer, btnGoInv); // header row
		invHeader.setAlignment(Pos.CENTER_LEFT); // align vertically

		TableView<Product> invPreview = new TableView<>(); // quick inventory table
		invPreview.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // stretch columns
		invPreview.setPrefHeight(220); // table height

		TableColumn<Product, Integer> colLot = new TableColumn<>("Lot ID"); // lot id column
		colLot.setCellValueFactory(new PropertyValueFactory<>("lotID")); // bind lotID

		TableColumn<Product, String> colCrop = new TableColumn<>("Seed Product"); // crop name column
		colCrop.setCellValueFactory(new PropertyValueFactory<>("name")); // bind name

		TableColumn<Product, String> colVar = new TableColumn<>("Variety"); // variety column
		colVar.setCellValueFactory(new PropertyValueFactory<>("variety")); // bind variety

		TableColumn<Product, Double> colStock = new TableColumn<>("Available (kg)"); // stock column
		colStock.setCellValueFactory(new PropertyValueFactory<>("amount")); // bind amount

		TableColumn<Product, Double> colVal = new TableColumn<>("Value / kg (\u20b1)"); // price column
		colVal.setCellValueFactory(new PropertyValueFactory<>("price")); // bind price

		TableColumn<Product, String> colLoc = new TableColumn<>("Storage Location"); // location column
		colLoc.setCellValueFactory(new PropertyValueFactory<>("location")); // bind location

		invPreview.getColumns().addAll(colLot, colCrop, colVar, colStock, colVal, colLoc); // attach columns
		invPreview.setItems(state.productData); // bind observable inventory list

		VBox invCard = new VBox(12, invHeader, invPreview); // wrap in white card
		invCard.setStyle(state.cardStyle()); // apply card css

		VBox root = new VBox(20, metricsRow, lateCard, invCard); // main overview container
		root.setPadding(new Insets(24, 28, 28, 28)); // outer margins
		return root; // return overview view
	}

	// helper to build a KPI stat card
	private VBox buildMetricCard(String title, String value, String subtitle, String accentColor) { // define buildmetriccard helper
		Label lblTitle = new Label(title); // card label
		lblTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + AppState.TEXT_MUTED + ";"); // muted title

		Label lblValue = new Label(value); // main numeric metric
		lblValue.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: " + accentColor + ";"); // colored number

		Label lblSub = new Label(subtitle); // supporting note
		lblSub.setStyle("-fx-font-size: 11px; -fx-text-fill: " + AppState.TEXT_MUTED + ";"); // muted caption

		VBox card = new VBox(6, lblTitle, lblValue, lblSub); // stack labels
		card.setStyle(state.cardStyle()); // apply white card style
		return card; // return stat card
	}

	// =========================================================================
	// 2. INVENTORY TAB (Without Save and Quit button)
	// =========================================================================
	private VBox createInventoryPane() { // define createinventorypane helper
		state.updateSummaryMetrics(); // ensure labels match current inventory

		VBox cardVar = new VBox(4, new Label("Plant Varieties"), state.lblVarieties); // varieties metric card
		cardVar.setStyle(state.cardStyle()); // card style
		state.lblVarieties.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + AppState.FOREST_DARK + ";"); // value style

		VBox cardStk = new VBox(4, new Label("Total Seed Stock"), state.lblStocks); // stock metric card
		cardStk.setStyle(state.cardStyle()); // card style
		state.lblStocks.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + AppState.PRIMARY_GREEN + ";"); // value style

		VBox cardLot = new VBox(4, new Label("Storage Lots"), state.lblLots); // lots metric card
		cardLot.setStyle(state.cardStyle()); // card style
		state.lblLots.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + AppState.TEXT_DARK + ";"); // value style

		HBox summaryRow = new HBox(16, cardVar, cardStk, cardLot); // metric row
		for (Node n : summaryRow.getChildren()) { // equal card widths
			HBox.setHgrow(n, Priority.ALWAYS); // expand evenly
		}

		TextField searchField = new TextField(); // inventory search input
		searchField.setPromptText("Search seed name, variety, or location..."); // placeholder hint
		searchField.setStyle(state.inputStyle()); // input css
		searchField.setPrefWidth(300); // search box width

		Button btnAdd = new Button("+  Add Seed Lot"); // button to add new lot (Save & Quit removed)
		btnAdd.setStyle(state.primaryButtonStyle()); // primary button css
		btnAdd.setOnAction(e -> createNewProduct()); // open add product modal

		Region spacer = new Region(); // push add button right
		HBox.setHgrow(spacer, Priority.ALWAYS); // expand spacer
		HBox toolbar = new HBox(12, searchField, spacer, btnAdd); // toolbar row
		toolbar.setAlignment(Pos.CENTER_LEFT); // vertical center

		FilteredList<Product> filteredProducts = new FilteredList<>(state.productData, p -> true); // live filter wrapper
		searchField.textProperty().addListener((obs, oldVal, newVal) -> { // filter rows as admin types
			String q = newVal == null ? "" : newVal.trim().toLowerCase(); // normalize query
			filteredProducts.setPredicate(p -> q.isEmpty() // continue statement
					|| p.getName().toLowerCase().contains(q) // continue statement
					|| p.getVariety().toLowerCase().contains(q) // continue statement
					|| p.getLocation().toLowerCase().contains(q) // continue statement
					|| String.valueOf(p.getLotID()).contains(q)); // match name, variety, location, or lot id
		});

		TableView<Product> table = new TableView<>(); // main inventory table
		state.inventoryTable = table; // save reference for cross-tab refresh
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // stretch columns
		table.setPrefHeight(430); // table height
		table.setItems(filteredProducts); // bind filtered list

		TableColumn<Product, Integer> colLotID = new TableColumn<>("Lot ID"); // lot id column
		colLotID.setCellValueFactory(new PropertyValueFactory<>("lotID")); // bind lotID

		TableColumn<Product, String> colName = new TableColumn<>("Product Name"); // crop name column
		colName.setCellValueFactory(new PropertyValueFactory<>("name")); // bind name

		TableColumn<Product, String> colVariety = new TableColumn<>("Variety"); // variety column
		colVariety.setCellValueFactory(new PropertyValueFactory<>("variety")); // bind variety

		TableColumn<Product, String> colLocation = new TableColumn<>("Location"); // storage location column
		colLocation.setCellValueFactory(new PropertyValueFactory<>("location")); // bind location

		TableColumn<Product, Double> colAmount = new TableColumn<>("Stock (kg)"); // available stock column
		colAmount.setCellValueFactory(new PropertyValueFactory<>("amount")); // bind amount

		TableColumn<Product, Double> colPrice = new TableColumn<>("Value / kg (\u20b1)"); // unit value column
		colPrice.setCellValueFactory(new PropertyValueFactory<>("price")); // bind price

		TableColumn<Product, Void> colAction = new TableColumn<>("Actions"); // row action column
		colAction.setCellFactory(param -> new TableCell<>() { // continue statement
			private final Button btnView = new Button("View Details"); // view/edit/delete button
			{
				btnView.setStyle(state.outlineButtonStyle()); // outline style
				btnView.setOnAction(e -> { // attach click listener
					Product product = getTableView().getItems().get(getIndex()); // get selected row item
					openProductDetailDialog(product); // open product details dialog
				});
			}
			@Override
			protected void updateItem(Void item, boolean empty) { // show button on non-empty rows
				super.updateItem(item, empty); // call superclass
				setGraphic(empty ? null : btnView); // attach or clear button
			}
		});

		table.getColumns().addAll(colLotID, colName, colVariety, colLocation, colAmount, colPrice, colAction); // add all columns

		VBox tableCard = new VBox(14, toolbar, table); // wrap toolbar and table in white card
		tableCard.setStyle(state.cardStyle()); // card style

		VBox root = new VBox(20, summaryRow, tableCard); // main inventory layout
		root.setPadding(new Insets(24, 28, 28, 28)); // outer margins
		return root; // return inventory pane
	}

	// =========================================================================
	// 3. USER MANAGEMENT TAB (Option 2 Account Creation, Assign Admin, Suspend with Reason, View Details)
	// =========================================================================
	private VBox createUserManagementPane() { // define createusermanagementpane helper
		TextField searchField = new TextField(); // user search input
		searchField.setPromptText("Search by user ID, username, name, or farm..."); // placeholder hint
		searchField.setStyle(state.inputStyle()); // input css
		searchField.setPrefWidth(320); // search box width

		Button btnCreateUser = new Button("+  Create Account"); // option 2 account creation button
		btnCreateUser.setStyle(state.primaryButtonStyle()); // primary button css
		btnCreateUser.setOnAction(e -> openCreateAccountDialog()); // open admin account creation modal

		Region spacer = new Region(); // push create button right
		HBox.setHgrow(spacer, Priority.ALWAYS); // expand spacer
		HBox toolbar = new HBox(12, searchField, spacer, btnCreateUser); // top toolbar
		toolbar.setAlignment(Pos.CENTER_LEFT); // center vertically

		FilteredList<User> filteredUsers = new FilteredList<>(state.users, u -> true); // live user filter
		searchField.textProperty().addListener((obs, oldVal, newVal) -> { // filter users as admin types
			String q = newVal == null ? "" : newVal.trim().toLowerCase(); // normalize query
			filteredUsers.setPredicate(u -> q.isEmpty() // continue statement
					|| u.getUserId().toLowerCase().contains(q) // continue statement
					|| u.getUsername().toLowerCase().contains(q) // continue statement
					|| u.getFullName().toLowerCase().contains(q) // continue statement
					|| u.getFarmName().toLowerCase().contains(q) // continue statement
					|| u.getFarmAddress().toLowerCase().contains(q)); // match id, username, name, or farm
		});

		TableView<User> userTable = new TableView<>(); // user accounts table
		userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // stretch columns
		userTable.setPrefHeight(470); // table height
		userTable.setItems(filteredUsers); // bind filtered list

		TableColumn<User, String> colId = new TableColumn<>("User ID"); // user id column
		colId.setCellValueFactory(new PropertyValueFactory<>("userId")); // bind userId

		TableColumn<User, String> colUsername = new TableColumn<>("Username"); // username column
		colUsername.setCellValueFactory(new PropertyValueFactory<>("username")); // bind username

		TableColumn<User, String> colName = new TableColumn<>("Full Name"); // full name column
		colName.setCellValueFactory(new PropertyValueFactory<>("fullName")); // bind fullName

		TableColumn<User, String> colFarm = new TableColumn<>("Farm / Address"); // farm summary column
		colFarm.setCellValueFactory(cell -> { // bind column value
			User u = cell.getValue(); // read row user
			String label = u.getFarmName().isBlank() ? u.getFarmAddress() : u.getFarmName() + " (" + u.getFarmAddress() + ")"; // combine farm name and address
			return new SimpleStringProperty(label.isBlank() ? "-" : label); // fallback dash if empty
		});

		TableColumn<User, String> colRole = new TableColumn<>("Role"); // role column
		colRole.setCellValueFactory(new PropertyValueFactory<>("role")); // bind role

		TableColumn<User, String> colStatus = new TableColumn<>("Status"); // status badge column
		colStatus.setCellValueFactory(new PropertyValueFactory<>("statusLabel")); // bind statusLabel
		colStatus.setCellFactory(state.createStatusBadgeCell()); // render colored pill badge

		TableColumn<User, Void> colActions = new TableColumn<>("Actions"); // details button column
		colActions.setCellFactory(param -> new TableCell<>() { // continue statement
			private final Button btnDetails = new Button("View Details"); // open user details modal
			{
				btnDetails.setStyle(state.outlineButtonStyle()); // outline style
				btnDetails.setOnAction(e -> { // attach click listener
					User selected = getTableView().getItems().get(getIndex()); // get clicked user
					openUserDetailDialog(selected, userTable); // open detail & management dialog
				});
			}
			@Override
			protected void updateItem(Void item, boolean empty) { // render button on populated rows
				super.updateItem(item, empty); // call superclass
				setGraphic(empty ? null : btnDetails); // attach or clear button
			}
		});

		userTable.getColumns().addAll(colId, colUsername, colName, colFarm, colRole, colStatus, colActions); // add all columns

		VBox card = new VBox(14, toolbar, userTable); // white card wrapper
		card.setStyle(state.cardStyle()); // card style

		VBox root = new VBox(20, card); // main user management container
		root.setPadding(new Insets(24, 28, 28, 28)); // outer margins
		return root; // return user management pane
	}

	// option 2: admin creates a new farmer or admin account directly (no verification gate needed)
	private void openCreateAccountDialog() { // define opencreateaccountdialog helper
		Stage dialog = new Stage(); // create modal stage
		dialog.initOwner(mainStage); // set owner stage
		dialog.initModality(Modality.APPLICATION_MODAL); // block parent clicks
		dialog.initStyle(StageStyle.UTILITY); // clean dialog frame
		dialog.setTitle("Create User Account"); // window title

		Label heading = new Label("Create New Account (Pre-Verified)"); // modal heading
		heading.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: " + AppState.FOREST_DARK + ";"); // heading style

		GridPane grid = new GridPane(); // two-column form grid
		grid.setHgap(14); // horizontal spacing
		grid.setVgap(10); // vertical spacing
		ColumnConstraints c1 = new ColumnConstraints(145); // label column width
		ColumnConstraints c2 = new ColumnConstraints(280); // input column width
		grid.getColumnConstraints().addAll(c1, c2); // apply column widths

		TextField tfUsername = new TextField(); // username input
		tfUsername.setStyle(state.inputStyle()); // input css
		PasswordField pfPassword = new PasswordField(); // password input
		pfPassword.setStyle(state.inputStyle()); // input css
		TextField tfFullName = new TextField(); // full name input
		tfFullName.setStyle(state.inputStyle()); // input css
		TextField tfPhone = new TextField(); // phone number input
		tfPhone.setStyle(state.inputStyle()); // input css
		TextField tfEmail = new TextField(); // email address input
		tfEmail.setStyle(state.inputStyle()); // input css
		TextField tfFarmName = new TextField(); // farm name input
		tfFarmName.setStyle(state.inputStyle()); // input css
		TextField tfFarmAddress = new TextField(); // farm address input
		tfFarmAddress.setStyle(state.inputStyle()); // input css
		TextField tfRsbsa = new TextField(); // rsbsa number input
		tfRsbsa.setPromptText("e.g. RSBSA-04-34-1200"); // placeholder
		tfRsbsa.setStyle(state.inputStyle()); // input css

		ComboBox<String> cbRole = new ComboBox<>(); // role selector (Admin or Farmer)
		cbRole.getItems().addAll("Farmer", "Admin"); // populate roles
		cbRole.setValue("Farmer"); // default to Farmer
		cbRole.setStyle(state.inputStyle()); // input css
		cbRole.setMaxWidth(Double.MAX_VALUE); // match field width

		grid.addRow(0, new Label("Username *:"), tfUsername); // row 0
		grid.addRow(1, new Label("Password *:"), pfPassword); // row 1
		grid.addRow(2, new Label("Full Name *:"), tfFullName); // row 2
		grid.addRow(3, new Label("Phone Number *:"), tfPhone); // row 3
		grid.addRow(4, new Label("Email Address *:"), tfEmail); // row 4
		grid.addRow(5, new Label("Farm Name:"), tfFarmName); // row 5
		grid.addRow(6, new Label("Farm Address *:"), tfFarmAddress); // row 6
		grid.addRow(7, new Label("RSBSA Number:"), tfRsbsa); // row 7
		grid.addRow(8, new Label("Account Role *:"), cbRole); // row 8

		Label lblWarn = new Label(); // validation error label
		lblWarn.setStyle("-fx-text-fill: #b42318; -fx-font-size: 12px;"); // red text
		lblWarn.setWrapText(true); // wrap long errors

		Button btnCancel = new Button("Cancel"); // cancel button
		btnCancel.setStyle(state.outlineButtonStyle()); // outline style
		btnCancel.setOnAction(e -> dialog.close()); // close modal

		Button btnSave = new Button("Create Account"); // submit account button
		btnSave.setStyle(state.primaryButtonStyle()); // primary style
		btnSave.setOnAction(e -> { // attach click listener
			String username = tfUsername.getText().trim(); // read username
			String password = pfPassword.getText().trim(); // read password
			String fullName = tfFullName.getText().trim(); // read full name
			String phone = tfPhone.getText().trim(); // read phone
			String email = tfEmail.getText().trim(); // read email
			String farmName = tfFarmName.getText().trim(); // read farm name
			String farmAddr = tfFarmAddress.getText().trim(); // read farm address
			String rsbsa = tfRsbsa.getText().trim(); // read rsbsa
			String role = cbRole.getValue(); // read selected role

			if (username.isEmpty() || password.isEmpty() || fullName.isEmpty() // continue statement
					|| phone.isEmpty() || email.isEmpty() || farmAddr.isEmpty()) { // validate required fields
				lblWarn.setText("Error: Please complete all required fields (*)."); // show missing fields message
				return; // stop creation
			}
			if (state.findUserByUsername(username) != null) { // prevent duplicate usernames
				lblWarn.setText("Error: Username '" + username + "' is already taken."); // show duplicate error
				return; // stop creation
			}

			User created = new User( // continue statement
					state.generateNextUserId(), // next USR-XXX id
					username, // username
					password, // password
					role, // Farmer or Admin
					fullName, // full name
					phone, // phone number
					email, // email address
					farmName.isEmpty() ? fullName + " Farm" : farmName, // default farm name if left blank
					farmAddr, // farm address
					rsbsa.isEmpty() ? "RSBSA-PENDING" : rsbsa, // rsbsa code
					1.0, // default 1.0 ha
					true, // option 2 requires no separate verification step
					false, // active (not suspended)
					""); // empty suspension reason

			state.users.add(created); // add to observable user list
			state.saveUsers(); // save immediately to users.txt
			dialog.close(); // close modal
		});

		HBox footer = new HBox(10, btnCancel, btnSave); // bottom action buttons
		footer.setAlignment(Pos.CENTER_RIGHT); // align right

		VBox layout = new VBox(14, heading, grid, lblWarn, footer); // modal container
		layout.setPadding(new Insets(22)); // outer padding
		layout.setStyle("-fx-background-color: #ffffff;"); // white background

		dialog.setScene(new Scene(layout, 485, 480)); // set modal scene size
		dialog.showAndWait(); // display modal
	}

	// view full user details: farmer info, farm info, pending seed requests & returns, assign admin, suspend with reason
	private void openUserDetailDialog(User user, TableView<User> userTable) { // define openuserdetaildialog helper
		Stage dialog = new Stage(); // create modal stage
		dialog.initOwner(mainStage); // set owner stage
		dialog.initModality(Modality.APPLICATION_MODAL); // block background clicks
		dialog.initStyle(StageStyle.UTILITY); // clean dialog border
		dialog.setTitle("User Details - " + user.getUsername()); // window title

		Label heading = new Label(user.getFullName() + " (" + user.getUserId() + ")"); // user heading
		heading.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + AppState.FOREST_DARK + ";"); // heading css

		// Farmer Information Section
		Label lblFarmerSec = new Label("Farmer Information"); // section 1 title
		lblFarmerSec.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + AppState.PRIMARY_GREEN + ";"); // green section label
		Label lblFarmerInfo = new Label( // continue statement
				"Username: " + user.getUsername() + "   |   Role: " + user.getRole() + "   |   Status: " + user.getStatusLabel() + "\n" // continue statement
						+ "Phone: " + (user.getPhone().isBlank() ? "-" : user.getPhone()) // continue statement
						+ "   |   Email: " + (user.getEmail().isBlank() ? "-" : user.getEmail())); // farmer details text
		lblFarmerInfo.setStyle("-fx-font-size: 12px; -fx-text-fill: " + AppState.TEXT_DARK + ";"); // dark text

		// Farm Information Section
		Label lblFarmSec = new Label("Farm Information"); // section 2 title
		lblFarmSec.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + AppState.PRIMARY_GREEN + ";"); // green section label
		Label lblFarmInfo = new Label( // continue statement
				"Farm Name: " + (user.getFarmName().isBlank() ? "-" : user.getFarmName()) // continue statement
						+ "   |   RSBSA: " + (user.getRsbsaNumber().isBlank() ? "-" : user.getRsbsaNumber()) + "\n" // continue statement
						+ "Farm Address: " + (user.getFarmAddress().isBlank() ? "-" : user.getFarmAddress()) // continue statement
						+ "   |   Active Loan Value: \u20b1" + String.format("%,.2f", state.getFarmerActiveLoanValue(user.getUsername()))); // farm details text
		lblFarmInfo.setStyle("-fx-font-size: 12px; -fx-text-fill: " + AppState.TEXT_DARK + ";"); // dark text

		// Pending Seed Requests and Pending Seed Returns Section
		Label lblPendingSec = new Label("Pending Seed Requests & Pending Seed Returns"); // section 3 title
		lblPendingSec.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + AppState.PRIMARY_GREEN + ";"); // green section label

		List<SeedRequest> pendingReqs = state.requestData.stream() // continue statement
				.filter(r -> r.getFarmer().equalsIgnoreCase(user.getUsername()) && "Pending".equalsIgnoreCase(r.getStatus())) // continue statement
				.collect(Collectors.toList()); // filter user's pending seed requests
		List<SeedReturn> pendingRets = state.returnData.stream() // continue statement
				.filter(r -> r.getFarmer().equalsIgnoreCase(user.getUsername()) && "Pending".equalsIgnoreCase(r.getStatus())) // continue statement
				.collect(Collectors.toList()); // filter user's pending seed returns

		StringBuilder sb = new StringBuilder(); // format pending items list
		for (SeedRequest r : pendingReqs) { // append each pending request
			sb.append("\u2022 [Request #").append(r.getRequestId()).append("] ").append(r.getSeedLabel()) // pass value
					.append(" - ").append(r.getAmount()).append(" kg (").append(r.getPurpose()).append(")\n"); // request bullet
		}
		for (SeedReturn r : pendingRets) { // append each pending return
			sb.append("\u2022 [Return #").append(r.getReturnId()).append("] ").append(r.getSeedLabel()) // pass value
					.append(" - ").append(r.getAmount()).append(" kg (Awaiting Inspection)\n"); // return bullet
		}
		if (sb.length() == 0) { // message when user has no pending transactions
			sb.append("No pending seed requests or seed returns for this account."); // empty state message
		}

		Label lblPendingList = new Label(sb.toString().trim()); // display formatted pending items
		lblPendingList.setWrapText(true); // wrap lines
		lblPendingList.setStyle("-fx-font-size: 12px; -fx-text-fill: " + AppState.TEXT_DARK + "; -fx-background-color: #f6f8f3; -fx-padding: 10; -fx-background-radius: 8;"); // soft box background

		// Suspension Reason Input & Action Buttons
		Label lblReason = new Label("Suspension Reason (shown to user if suspended):"); // suspension reason prompt
		lblReason.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + AppState.TEXT_DARK + ";"); // label style
		TextField tfReason = new TextField(user.getSuspensionReason()); // prefill existing reason if any
		tfReason.setPromptText("Enter reason before suspending (e.g., Unsettled overdue seed return)"); // placeholder hint
		tfReason.setStyle(state.inputStyle()); // input css

		Label lblFeedback = new Label(); // status feedback label
		lblFeedback.setStyle("-fx-font-size: 12px; -fx-text-fill: #b42318;"); // red text

		Button btnToggleAdmin = new Button("Admin".equalsIgnoreCase(user.getRole()) ? "Revoke Admin (Set Farmer)" : "Assign Admin"); // toggle role button
		btnToggleAdmin.setStyle(state.outlineButtonStyle()); // outline style
		btnToggleAdmin.setOnAction(e -> { // attach click listener
			if (state.currentUser != null && state.currentUser.getUsername().equalsIgnoreCase(user.getUsername())) { // prevent removing own admin role
				lblFeedback.setText("You cannot revoke your own Admin role while logged in."); // warning text
				return; // abort
			}
			boolean makeAdmin = !"Admin".equalsIgnoreCase(user.getRole()); // flip role
			user.setRole(makeAdmin ? "Admin" : "Farmer"); // update user role
			state.saveUsers(); // save to users.txt
			userTable.refresh(); // refresh table row
			dialog.close(); // close modal
		});

		Button btnSuspend = new Button(user.isSuspended() ? "Unsuspend User" : "Suspend User"); // suspend/unsuspend button
		btnSuspend.setStyle(user.isSuspended() ? state.primaryButtonStyle() : state.dangerButtonStyle()); // green to unsuspend, red to suspend
		btnSuspend.setOnAction(e -> { // attach click listener
			if (state.currentUser != null && state.currentUser.getUsername().equalsIgnoreCase(user.getUsername())) { // prevent self-suspension
				lblFeedback.setText("You cannot suspend your own active Admin account."); // warning text
				return; // abort
			}
			if (!user.isSuspended()) { // suspending an active user requires a reason
				String reason = tfReason.getText().trim(); // read reason input
				if (reason.isEmpty()) { // enforce entering a suspension reason
					lblFeedback.setText("Please enter a suspension reason above before suspending this user."); // error text
					return; // abort
				}
				user.setSuspended(true); // mark suspended
				user.setSuspensionReason(reason); // store reason shown at login
			} else { // unsuspending user
				user.setSuspended(false); // clear suspended flag
				user.setSuspensionReason(""); // clear suspension reason
			}
			state.saveUsers(); // persist to users.txt
			userTable.refresh(); // refresh table row
			dialog.close(); // close modal
		});

		Button btnClose = new Button("Close"); // close dialog button
		btnClose.setStyle(state.outlineButtonStyle()); // outline style
		btnClose.setOnAction(e -> dialog.close()); // close modal

		Region spacer = new Region(); // push close button right
		HBox.setHgrow(spacer, Priority.ALWAYS); // expand spacer
		HBox actions = new HBox(10, btnToggleAdmin, btnSuspend, spacer, btnClose); // bottom button row
		actions.setAlignment(Pos.CENTER_LEFT); // align vertically

		VBox layout = new VBox(12, // continue statement
				heading, new Separator(), // continue statement
				lblFarmerSec, lblFarmerInfo, // pass value
				lblFarmSec, lblFarmInfo, // pass value
				lblPendingSec, lblPendingList, // pass value
				lblReason, tfReason, lblFeedback, actions); // assemble modal content
		layout.setPadding(new Insets(22)); // outer padding
		layout.setStyle("-fx-background-color: #ffffff;"); // white background

		dialog.setScene(new Scene(layout, 580, 520)); // set modal dimensions
		dialog.showAndWait(); // display modal
	}

	// =========================================================================
	// 4. TRANSACTIONS TAB (Approve/Reject Requests & Accept/Reject Seed Returns)
	// =========================================================================
	private VBox createAdminTransactionsPane() { // define createadmintransactionspane helper
		Label lblRequests = new Label("Seed Requests"); // top section title
		lblRequests.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + AppState.TEXT_DARK + ";"); // bold heading

		TableView<SeedRequest> requestTable = new TableView<>(); // seed requests table
		requestTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // stretch columns
		requestTable.setPrefHeight(225); // table height
		requestTable.setPlaceholder(new Label("No seed requests yet.")); // empty message

		TableColumn<SeedRequest, Integer> colRequestID = new TableColumn<>("Request ID"); // id column
		colRequestID.setCellValueFactory(new PropertyValueFactory<>("requestId")); // bind requestId

		TableColumn<SeedRequest, String> colFarmer = new TableColumn<>("Farmer"); // farmer column
		colFarmer.setCellValueFactory(new PropertyValueFactory<>("farmer")); // bind farmer

		TableColumn<SeedRequest, String> colSeed = new TableColumn<>("Seed"); // seed column
		colSeed.setCellValueFactory(new PropertyValueFactory<>("seedLabel")); // bind seedLabel

		TableColumn<SeedRequest, Double> colAmount = new TableColumn<>("Amount (kg)"); // weight column
		colAmount.setCellValueFactory(new PropertyValueFactory<>("amount")); // bind amount

		TableColumn<SeedRequest, String> colDue = new TableColumn<>("Due Date"); // deadline column
		colDue.setCellValueFactory(cell -> { // bind column value
			SeedRequest r = cell.getValue(); // read row request
			if (r.getDueDate() == null || r.getDueDate().isBlank()) { // no due date yet
				return new SimpleStringProperty("-"); // display dash
			}
			return new SimpleStringProperty(r.isOverdue() ? r.getDueDate() + " (OVERDUE)" : r.getDueDate()); // highlight overdue dates
		});

		TableColumn<SeedRequest, String> colPurpose = new TableColumn<>("Purpose"); // purpose column
		colPurpose.setCellValueFactory(new PropertyValueFactory<>("purpose")); // bind purpose

		TableColumn<SeedRequest, String> colStatus = new TableColumn<>("Status"); // status badge column
		colStatus.setCellValueFactory(new PropertyValueFactory<>("status")); // bind status
		colStatus.setCellFactory(state.createStatusBadgeCell()); // render status pill

		requestTable.getColumns().addAll(colRequestID, colFarmer, colSeed, colAmount, colDue, colPurpose, colStatus); // add columns
		requestTable.setItems(state.requestData); // bind request list

		Button btnApproveRequest = new Button("Approve Request"); // approve button
		btnApproveRequest.setStyle(state.primaryButtonStyle()); // primary style
		Button btnRejectRequest = new Button("Reject Request"); // reject button
		btnRejectRequest.setStyle(state.dangerButtonStyle()); // danger style
		HBox requestButtons = new HBox(10, btnApproveRequest, btnRejectRequest); // button row

		Label lblReturns = new Label("Seed Returns (Harvest Repayments)"); // bottom section title
		lblReturns.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + AppState.TEXT_DARK + ";"); // bold heading

		TableView<SeedReturn> returnTable = new TableView<>(); // seed returns table
		returnTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // stretch columns
		returnTable.setPrefHeight(225); // table height
		returnTable.setPlaceholder(new Label("No seed returns yet.")); // empty message

		TableColumn<SeedReturn, Integer> colReturnID = new TableColumn<>("Return ID"); // return id column
		colReturnID.setCellValueFactory(new PropertyValueFactory<>("returnId")); // bind returnId

		TableColumn<SeedReturn, Integer> colLinkedReq = new TableColumn<>("Request ID"); // linked request id column
		colLinkedReq.setCellValueFactory(new PropertyValueFactory<>("requestId")); // bind requestId

		TableColumn<SeedReturn, String> colReturnFarmer = new TableColumn<>("Farmer"); // farmer column
		colReturnFarmer.setCellValueFactory(new PropertyValueFactory<>("farmer")); // bind farmer

		TableColumn<SeedReturn, String> colReturnSeed = new TableColumn<>("Seed"); // seed column
		colReturnSeed.setCellValueFactory(new PropertyValueFactory<>("seedLabel")); // bind seedLabel

		TableColumn<SeedReturn, Double> colReturnAmount = new TableColumn<>("Returned (kg)"); // returned weight column
		colReturnAmount.setCellValueFactory(new PropertyValueFactory<>("amount")); // bind amount

		TableColumn<SeedReturn, String> colReturnStatus = new TableColumn<>("Status"); // status badge column
		colReturnStatus.setCellValueFactory(new PropertyValueFactory<>("status")); // bind status
		colReturnStatus.setCellFactory(state.createStatusBadgeCell()); // render status pill

		returnTable.getColumns().addAll(colReturnID, colLinkedReq, colReturnFarmer, colReturnSeed, colReturnAmount, colReturnStatus); // add columns
		returnTable.setItems(state.returnData); // bind return list

		Button btnAcceptReturn = new Button("Accept Return"); // accept return button
		btnAcceptReturn.setStyle(state.primaryButtonStyle()); // primary style
		Button btnRejectReturn = new Button("Reject Return"); // reject return button
		btnRejectReturn.setStyle(state.dangerButtonStyle()); // danger style
		HBox returnButtons = new HBox(10, btnAcceptReturn, btnRejectReturn); // button row

		Label lblWarn = new Label(); // action feedback label
		lblWarn.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + AppState.PRIMARY_GREEN + ";"); // green status text

		btnApproveRequest.setOnAction(e -> approveRequest(requestTable, lblWarn)); // approve selected request
		btnRejectRequest.setOnAction(e -> rejectRequest(requestTable, lblWarn)); // reject selected request
		btnAcceptReturn.setOnAction(e -> acceptReturn(returnTable, requestTable, lblWarn)); // accept selected return and clear overdue status
		btnRejectReturn.setOnAction(e -> rejectReturn(returnTable, lblWarn)); // reject selected return

		VBox reqCard = new VBox(12, lblRequests, requestTable, requestButtons); // requests white card
		reqCard.setStyle(state.cardStyle()); // card style

		VBox retCard = new VBox(12, lblReturns, returnTable, returnButtons, lblWarn); // returns white card
		retCard.setStyle(state.cardStyle()); // card style

		VBox root = new VBox(20, reqCard, retCard); // main transactions container
		root.setPadding(new Insets(24, 28, 28, 28)); // outer margins
		return root; // return transactions pane
	}

	// approve a pending seed request, deduct inventory, and assign a 30-day return due date
	private void approveRequest(TableView<SeedRequest> table, Label lblWarn) { // define approverequest helper
		SeedRequest request = table.getSelectionModel().getSelectedItem(); // get selected request
		if (request == null) { // check selection
			lblWarn.setText("Error: Please select a request first."); // error text
			return; // abort
		}
		if (!"Pending".equalsIgnoreCase(request.getStatus())) { // ensure status is Pending
			lblWarn.setText("Error: This request is already " + request.getStatus() + "."); // error text
			return; // abort
		}

		Product product = state.findProduct(request.getLotId()); // look up seed lot
		if (product == null) { // ensure lot exists
			lblWarn.setText("Error: The seed lot no longer exists."); // error text
			return; // abort
		}
		if (product.getAmount() < request.getAmount()) { // ensure sufficient inventory
			lblWarn.setText("Error: Not enough stock for this request."); // error text
			return; // abort
		}

		product.setAmount(state.round2(product.getAmount() - request.getAmount())); // deduct approved weight from stock
		request.setStatus("Approved"); // mark request approved
		if (request.getDueDate() == null || request.getDueDate().isBlank()) { // assign 30-day return deadline if not set
			request.setDueDate(LocalDate.now().plusDays(30).toString()); // set due date 30 days ahead
		}

		state.updateSummaryMetrics(); // update inventory totals
		state.saveProducts(); // save products.txt
		state.saveRequests(); // save requests.txt
		table.refresh(); // refresh requests table
		if (state.inventoryTable != null) { // refresh inventory table if initialized
			state.inventoryTable.refresh(); // redraw rows
		}
		lblWarn.setText("Request #" + request.getRequestId() + " approved. Stock updated and due date set to " + request.getDueDate() + "."); // success notice
	}

	// reject a pending seed request
	private void rejectRequest(TableView<SeedRequest> table, Label lblWarn) { // define rejectrequest helper
		SeedRequest request = table.getSelectionModel().getSelectedItem(); // get selected request
		if (request == null) { // check selection
			lblWarn.setText("Error: Please select a request first."); // error text
			return; // abort
		}
		if (!"Pending".equalsIgnoreCase(request.getStatus())) { // ensure Pending status
			lblWarn.setText("Error: This request is already " + request.getStatus() + "."); // error text
			return; // abort
		}

		request.setStatus("Rejected"); // mark request rejected
		state.saveRequests(); // persist to requests.txt
		table.refresh(); // refresh table
		lblWarn.setText("Request #" + request.getRequestId() + " rejected."); // status message
	}

	// accept a seed return, replenish inventory stock, and clear overdue status on the linked request
	private void acceptReturn(TableView<SeedReturn> returnTable, TableView<SeedRequest> requestTable, Label lblWarn) { // define acceptreturn helper
		SeedReturn ret = returnTable.getSelectionModel().getSelectedItem(); // get selected return
		if (ret == null) { // check selection
			lblWarn.setText("Error: Please select a return first."); // error text
			return; // abort
		}
		if (!"Pending".equalsIgnoreCase(ret.getStatus())) { // ensure Pending status
			lblWarn.setText("Error: This return is already " + ret.getStatus() + "."); // error text
			return; // abort
		}

		Product product = state.findProduct(ret.getLotId()); // look up target seed lot
		if (product == null) { // ensure lot exists
			lblWarn.setText("Error: The seed lot no longer exists."); // error text
			return; // abort
		}

		product.setAmount(state.round2(product.getAmount() + ret.getAmount())); // add returned seeds back into inventory
		ret.setStatus("Accepted"); // mark return accepted

		// mark the linked loan request as Returned so any overdue flag is cleared
		for (SeedRequest req : state.requestData) { // find matching request by id
			if (req.getRequestId() == ret.getRequestId()) { // match request id
				req.setStatus("Returned"); // clear active/overdue loan state
				break; // exit loop once updated
			}
		}

		state.updateSummaryMetrics(); // refresh inventory totals
		state.saveProducts(); // persist updated stock
		state.saveReturns(); // persist return status
		state.saveRequests(); // persist cleared overdue status
		returnTable.refresh(); // refresh returns table
		requestTable.refresh(); // refresh requests table so OVERDUE badge disappears
		if (state.inventoryTable != null) { // refresh inventory table if initialized
			state.inventoryTable.refresh(); // redraw rows
		}
		lblWarn.setText("Return #" + ret.getReturnId() + " accepted. Stock replenished and loan overdue status cleared."); // success notice
	}

	// reject a pending seed return
	private void rejectReturn(TableView<SeedReturn> table, Label lblWarn) { // define rejectreturn helper
		SeedReturn ret = table.getSelectionModel().getSelectedItem(); // get selected return
		if (ret == null) { // check selection
			lblWarn.setText("Error: Please select a return first."); // error text
			return; // abort
		}
		if (!"Pending".equalsIgnoreCase(ret.getStatus())) { // ensure Pending status
			lblWarn.setText("Error: This return is already " + ret.getStatus() + "."); // error text
			return; // abort
		}

		ret.setStatus("Rejected"); // mark return rejected
		state.saveReturns(); // persist to returns.txt
		table.refresh(); // refresh table
		lblWarn.setText("Return #" + ret.getReturnId() + " rejected."); // status notice
	}

	// =========================================================================
	// 5. DONATIONS TAB
	// =========================================================================
	private VBox createAdminDonationsPane() { // define createadmindonationspane helper
		state.updateDonationTotal(); // refresh total donations label
		state.lblDonationTotal.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + AppState.PRIMARY_GREEN + ";"); // green bold total

		TableView<Donation> table = new TableView<>(); // donations table
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // stretch columns
		table.setPrefHeight(450); // table height

		TableColumn<Donation, String> colDonor = new TableColumn<>("Donor"); // donor column
		colDonor.setCellValueFactory(new PropertyValueFactory<>("donorName")); // bind donorName

		TableColumn<Donation, String> colEmail = new TableColumn<>("Email"); // email column
		colEmail.setCellValueFactory(new PropertyValueFactory<>("email")); // bind email

		TableColumn<Donation, Double> colAmount = new TableColumn<>("Amount (\u20b1)"); // amount column
		colAmount.setCellValueFactory(new PropertyValueFactory<>("amount")); // bind amount

		TableColumn<Donation, String> colMessage = new TableColumn<>("Message"); // message column
		colMessage.setCellValueFactory(new PropertyValueFactory<>("message")); // bind message

		table.getColumns().addAll(colDonor, colEmail, colAmount, colMessage); // add columns
		table.setItems(state.donationData); // bind donation list
		table.setPlaceholder(new Label("No donations yet.")); // empty message

		VBox card = new VBox(14, state.lblDonationTotal, table); // wrap in white card
		card.setStyle(state.cardStyle()); // card style

		VBox root = new VBox(20, card); // main donations container
		root.setPadding(new Insets(24, 28, 28, 28)); // outer margins
		return root; // return donations pane
	}

	// dialog for adding a new seed product lot to inventory
	private void createNewProduct() { // define createnewproduct helper
		Stage dialogStage = new Stage(); // create modal stage
		dialogStage.initOwner(mainStage); // set owner stage
		dialogStage.initModality(Modality.APPLICATION_MODAL); // block parent window
		dialogStage.setTitle("Add Seed Lot"); // dialog title

		TextField textName = new TextField(); // crop name input
		textName.setStyle(state.inputStyle()); // input css
		TextField textPrice = new TextField(); // price per kg input
		textPrice.setStyle(state.inputStyle()); // input css
		TextField textAmount = new TextField(); // stock weight input
		textAmount.setStyle(state.inputStyle()); // input css
		TextField textLotID = new TextField(); // lot id input
		textLotID.setStyle(state.inputStyle()); // input css
		TextField textLocation = new TextField(); // storage location input
		textLocation.setStyle(state.inputStyle()); // input css
		TextField textVariety = new TextField(); // variety input
		textVariety.setStyle(state.inputStyle()); // input css
		TextField textDescription = new TextField(); // description input
		textDescription.setStyle(state.inputStyle()); // input css

		GridPane grid = new GridPane(); // two-column form grid
		grid.setHgap(12); // horizontal gap
		grid.setVgap(10); // vertical gap
		grid.addRow(0, new Label("Crop Name:"), textName); // row 0
		grid.addRow(1, new Label("Price / kg (\u20b1):"), textPrice); // row 1
		grid.addRow(2, new Label("Stock (kg):"), textAmount); // row 2
		grid.addRow(3, new Label("Lot ID:"), textLotID); // row 3
		grid.addRow(4, new Label("Location:"), textLocation); // row 4
		grid.addRow(5, new Label("Variety:"), textVariety); // row 5
		grid.addRow(6, new Label("Description:"), textDescription); // row 6

		Button btnCreate = new Button("Save Seed Lot"); // submit button
		btnCreate.setStyle(state.primaryButtonStyle()); // primary style
		Label labelWarn = new Label(); // validation error label
		labelWarn.setStyle("-fx-text-fill: #b42318; -fx-font-size: 12px;"); // red text

		btnCreate.setOnAction(e -> { // attach click listener
			String name = textName.getText().trim(); // read crop name
			String priceStr = textPrice.getText().trim(); // read price
			String amountStr = textAmount.getText().trim(); // read amount
			String lotIDStr = textLotID.getText().trim(); // read lot id
			String location = textLocation.getText().trim(); // read location
			String variety = textVariety.getText().trim(); // read variety
			String description = textDescription.getText().trim(); // read description

			if (name.isEmpty() || priceStr.isEmpty() || amountStr.isEmpty() || lotIDStr.isEmpty() // continue statement
					|| location.isEmpty() || variety.isEmpty() || description.isEmpty()) { // check all fields filled
				labelWarn.setText("Error: All fields are required!"); // show missing fields message
				return; // abort
			}

			try { // attempt risky block
				double price = Double.parseDouble(priceStr); // parse numeric price
				double amount = Double.parseDouble(amountStr); // parse numeric stock
				int lotID = Integer.parseInt(lotIDStr); // parse integer lot id
				if (state.findProduct(lotID) != null) { // ensure unique lot id
					labelWarn.setText("Error: Lot ID " + lotID + " already exists!"); // show duplicate lot id error
					return; // abort
				}

				state.productData.add(new Product(name, price, amount, lotID, location, variety, description)); // add new lot
				state.saveProducts(); // save to products.txt
				dialogStage.close(); // close modal
			} catch (NumberFormatException ex) { // handle exception
				labelWarn.setText("Error: Price, Stock, and Lot ID must be valid numbers!"); // show numeric parse error
			}
		});

		VBox vbox = new VBox(14, grid, labelWarn, btnCreate); // dialog container
		vbox.setPadding(new Insets(20)); // outer padding
		vbox.setStyle("-fx-background-color: #ffffff;"); // white background
		dialogStage.setScene(new Scene(vbox, 420, 390)); // set scene size
		dialogStage.showAndWait(); // display modal
	}

	// dialog showing full details for a selected seed lot with edit and delete options
	private void openProductDetailDialog(Product product) { // define openproductdetaildialog helper
		Stage detailStage = new Stage(); // create modal stage
		detailStage.initOwner(mainStage); // set owner stage
		detailStage.initModality(Modality.APPLICATION_MODAL); // block background clicks
		detailStage.setTitle("Seed Lot Details"); // dialog title

		Label labelName = new Label("Name: " + product.getName()); // crop name
		Label labelPrice = new Label("Price / kg: \u20b1" + product.getPrice()); // unit price
		Label labelAmount = new Label("Stock: " + product.getAmount() + " kg"); // stock weight
		Label labelLotID = new Label("Lot ID: " + product.getLotID()); // lot id
		Label labelLocation = new Label("Location: " + product.getLocation()); // location
		Label labelVariety = new Label("Variety: " + product.getVariety()); // variety
		Label labelDescription = new Label("Description: " + product.getDescription()); // description
		labelDescription.setWrapText(true); // wrap description text

		Button btnEdit = new Button("Edit"); // edit button
		btnEdit.setStyle(state.primaryButtonStyle()); // primary style
		btnEdit.setOnAction(e -> openEditProductDialog(product, detailStage)); // open edit form

		Button btnDelete = new Button("Delete"); // delete button
		btnDelete.setStyle(state.dangerButtonStyle()); // danger style
		btnDelete.setOnAction(e -> { // attach click listener
			state.productData.remove(product); // remove lot from inventory
			state.saveProducts(); // save to products.txt
			detailStage.close(); // close modal
		});

		HBox hBoxButtons = new HBox(10, btnEdit, btnDelete); // action button bar

		VBox vbox = new VBox(10, labelName, labelPrice, labelAmount, labelLotID, // continue statement
				labelLocation, labelVariety, labelDescription, hBoxButtons); // dialog layout
		vbox.setPadding(new Insets(20)); // outer padding
		vbox.setStyle("-fx-background-color: #ffffff;"); // white background

		detailStage.setScene(new Scene(vbox, 400, 320)); // set scene size
		detailStage.showAndWait(); // display modal
	}

	// dialog for editing an existing seed lot's fields
	private void openEditProductDialog(Product product, Stage detailStage) { // define openeditproductdialog helper
		Stage editStage = new Stage(); // create edit stage
		editStage.initOwner(detailStage); // set owner stage
		editStage.initModality(Modality.APPLICATION_MODAL); // block parent clicks
		editStage.setTitle("Edit Seed Lot"); // dialog title

		TextField textName = new TextField(product.getName()); // prefill name
		textName.setStyle(state.inputStyle()); // input css
		TextField textPrice = new TextField(String.valueOf(product.getPrice())); // prefill price
		textPrice.setStyle(state.inputStyle()); // input css
		TextField textAmount = new TextField(String.valueOf(product.getAmount())); // prefill amount
		textAmount.setStyle(state.inputStyle()); // input css
		TextField textLotID = new TextField(String.valueOf(product.getLotID())); // prefill lot id
		textLotID.setStyle(state.inputStyle()); // input css
		TextField textLocation = new TextField(product.getLocation()); // prefill location
		textLocation.setStyle(state.inputStyle()); // input css
		TextField textVariety = new TextField(product.getVariety()); // prefill variety
		textVariety.setStyle(state.inputStyle()); // input css
		TextField textDescription = new TextField(product.getDescription()); // prefill description
		textDescription.setStyle(state.inputStyle()); // input css

		GridPane grid = new GridPane(); // two-column form grid
		grid.setHgap(12); // horizontal gap
		grid.setVgap(10); // vertical gap
		grid.addRow(0, new Label("Crop Name:"), textName); // row 0
		grid.addRow(1, new Label("Price / kg (\u20b1):"), textPrice); // row 1
		grid.addRow(2, new Label("Stock (kg):"), textAmount); // row 2
		grid.addRow(3, new Label("Lot ID:"), textLotID); // row 3
		grid.addRow(4, new Label("Location:"), textLocation); // row 4
		grid.addRow(5, new Label("Variety:"), textVariety); // row 5
		grid.addRow(6, new Label("Description:"), textDescription); // row 6

		Button btnSave = new Button("Save Changes"); // save changes button
		btnSave.setStyle(state.primaryButtonStyle()); // primary style
		Label labelWarn = new Label(); // validation error label
		labelWarn.setStyle("-fx-text-fill: #b42318; -fx-font-size: 12px;"); // red text

		btnSave.setOnAction(e -> { // attach click listener
			String name = textName.getText().trim(); // read name
			String priceStr = textPrice.getText().trim(); // read price
			String amountStr = textAmount.getText().trim(); // read amount
			String lotIDStr = textLotID.getText().trim(); // read lot id
			String location = textLocation.getText().trim(); // read location
			String variety = textVariety.getText().trim(); // read variety
			String description = textDescription.getText().trim(); // read description

			if (name.isEmpty() || priceStr.isEmpty() || amountStr.isEmpty() || lotIDStr.isEmpty() // continue statement
					|| location.isEmpty() || variety.isEmpty() || description.isEmpty()) { // validate non-empty fields
				labelWarn.setText("Error: All fields are required!"); // show missing fields message
				return; // abort
			}

			try { // attempt risky block
				double price = Double.parseDouble(priceStr); // parse price
				double amount = Double.parseDouble(amountStr); // parse stock weight
				int lotID = Integer.parseInt(lotIDStr); // parse lot id

				product.setName(name); // update name
				product.setPrice(price); // update price
				product.setAmount(amount); // update amount
				product.setLotID(lotID); // update lot id
				product.setLocation(location); // update location
				product.setVariety(variety); // update variety
				product.setDescription(description); // update description

				int index = state.productData.indexOf(product); // locate item in observable list
				if (index >= 0) { // trigger table row refresh
					state.productData.set(index, product); // replace in-place to fire change event
				}
				state.saveProducts(); // save to products.txt
				editStage.close(); // close edit modal
				detailStage.close(); // close detail modal
			} catch (NumberFormatException ex) { // handle exception
				labelWarn.setText("Error: Price, Stock, and Lot ID must be valid numbers!"); // show numeric parse error
			}
		});

		VBox vbox = new VBox(14, grid, labelWarn, btnSave); // dialog container
		vbox.setPadding(new Insets(20)); // outer padding
		vbox.setStyle("-fx-background-color: #ffffff;"); // white background
		editStage.setScene(new Scene(vbox, 420, 390)); // set scene size
		editStage.showAndWait(); // display modal
	}
}
