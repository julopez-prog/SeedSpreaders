package application.admin; // declare package application.admin

import application.Donation; // import donation class
import application.Product; // import product class
import application.SeedRequest; // import seedrequest class
import application.SeedReturn; // import seedreturn class
import application.User; // import user class
import application.shared.AppState; // import appstate class
import javafx.beans.property.SimpleStringProperty; // import simplestringproperty class
import javafx.collections.FXCollections; // import fxcollections class
import javafx.collections.transformation.FilteredList; // import filteredlist class
import javafx.geometry.Insets; // import insets class
import javafx.geometry.Pos; // import pos class
import javafx.scene.Node; // import node class
import javafx.scene.Scene; // import scene class
import javafx.scene.control.Button; // import button class
import javafx.scene.control.ComboBox; // import combobox class
import javafx.scene.control.Label; // import label class
import javafx.scene.control.PasswordField; // import passwordfield class
import javafx.scene.control.ScrollPane; // import scrollpane class
import javafx.scene.control.Separator; // import separator class
import javafx.scene.control.TableCell; // import tablecell class
import javafx.scene.control.TableColumn; // import tablecolumn class
import javafx.scene.control.TableView; // import tableview class
import javafx.scene.control.TextArea; // import textarea class
import javafx.scene.control.TextField; // import textfield class
import javafx.scene.control.cell.PropertyValueFactory; // import propertyvaluefactory class
import javafx.scene.layout.BorderPane; // import borderpane class
import javafx.scene.layout.ColumnConstraints; // import columnconstraints class
import javafx.scene.layout.GridPane; // import gridpane class
import javafx.scene.layout.HBox; // import hbox class
import javafx.scene.layout.Priority; // import priority class
import javafx.scene.layout.Region; // import region class
import javafx.scene.layout.VBox; // import vbox class
import javafx.stage.Modality; // import modality class
import javafx.stage.Stage; // import stage class
import javafx.stage.StageStyle; // import stagestyle class

import java.time.LocalDate; // import localdate class
import java.util.Comparator; // import comparator class
import java.util.List; // import list class
import java.util.stream.Collectors; // import collectors class

public class AdminPages { // define adminpages class

	private final AppState state; // store state field
	private final Runnable onLogout; // store onlogout field

	public AdminPages(AppState state, Runnable onLogout) { // initialize adminpages instance
		this.state = state; // assign state value
		this.onLogout = onLogout; // assign onlogout value
	}

	// persistent sidebar + header shell (matching figma dashboard screens)
	public void showAdminShell() { // handle showadminshell operation
		BorderPane shell = new BorderPane(); // execute statement
		shell.setStyle("-fx-background-color: " + state.bgPage() + ";"); // apply inline css styling

		// left dark forest sidebar (#0c3026)
		VBox sidebar = new VBox(8); // execute statement
		sidebar.setPrefWidth(250); // configure node width constraint
		sidebar.setMinWidth(250); // configure node width constraint
		sidebar.setPadding(new Insets(26, 16, 24, 16)); // set inner padding spacing
		sidebar.setStyle("-fx-background-color: " + state.FOREST_DARK + ";"); // apply inline css styling

		Node logo = state.buildLogo(0.95, true); // execute statement
		VBox logoWrap = new VBox(logo); // execute statement
		logoWrap.setPadding(new Insets(0, 8, 28, 8)); // set inner padding spacing

		Button navDashboard = state.createNavButton(this::showAdminShell, state.t("Dashboard", "Dashboard"), "Dashboard", true); // execute statement
		Button navInventory = state.createNavButton(this::showAdminShell, state.t("Inventory", "Imbentaryo"), "Inventory", true); // execute statement
		Button navFarmers = state.createNavButton(this::showAdminShell, state.t("Farmers & Users", "Mga Farmer at User"), "Farmers", true); // execute statement
		Button navTransactions = state.createNavButton(this::showAdminShell, state.t("Transactions", "Mga Transaksyon"), "Transactions", true); // execute statement
		Button navDonations = state.createNavButton(this::showAdminShell, state.t("Donations", "Mga Donasyon"), "Donations", true); // execute statement

		Region spacer = new Region(); // execute statement
		VBox.setVgrow(spacer, Priority.ALWAYS); // allow vertical growth inside vbox

		Label lblRoleBadge = new Label("ADMIN \u2022 " + state.currentUser.getUsername()); // execute statement
		lblRoleBadge.setStyle("-fx-text-fill: #9cb8ad; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 0 0 6 12;"); // apply inline css styling

		Button btnLogout = new Button(state.t("Logout", "Mag-log Out")); // execute statement
		btnLogout.setMaxWidth(Double.MAX_VALUE); // configure node width constraint
		btnLogout.setStyle("-fx-background-color: rgba(255,255,255,0.10); -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 10 16 10 16; -fx-cursor: hand;"); // apply inline css styling
		btnLogout.setOnAction(e -> { // attach button click event handler
			state.saveProducts(state.productPath, state.productData); // execute statement
			state.saveUsers(state.userPath, state.users); // execute statement
			state.currentUser = null; // execute statement
			onLogout.run(); // execute statement
		}); // execute statement

		sidebar.getChildren().addAll(logoWrap, navDashboard, navInventory, navFarmers, navTransactions, navDonations, spacer, lblRoleBadge, btnLogout); // add child nodes to container

		// main content area
		VBox centerWrapper = new VBox(18); // execute statement
		centerWrapper.setPadding(new Insets(24, 32, 24, 32)); // set inner padding spacing
		centerWrapper.setStyle("-fx-background-color: " + state.bgPage() + ";"); // apply inline css styling

		HBox topHeader = state.buildTopHeader(state.getAdminPageTitle(), true, this::showAdminShell); // execute statement

		Node pageBody; // execute statement
		switch (state.currentRoute) { // open code block
			case "Inventory": // continue statement
				pageBody = buildAdminInventoryView(); // execute statement
				break; // exit current loop
			case "Farmers": // continue statement
				pageBody = buildAdminUserManagementView(); // execute statement
				break; // exit current loop
			case "Transactions": // continue statement
				pageBody = buildAdminTransactionsView(); // execute statement
				break; // exit current loop
			case "Donations": // continue statement
				pageBody = buildAdminDonationsView(); // execute statement
				break; // exit current loop
			default: // continue statement
				pageBody = buildAdminDashboardOverview(); // execute statement
				break; // exit current loop
		}
		VBox.setVgrow(pageBody, Priority.ALWAYS); // allow vertical growth inside vbox

		centerWrapper.getChildren().addAll(topHeader, pageBody); // add child nodes to container

		shell.setLeft(sidebar); // execute statement
		shell.setCenter(centerWrapper); // execute statement

		state.applyScene(new Scene(shell, 1200, 780), "Seed Spreaders - Admin (" + state.currentRoute + ")"); // execute statement
	}

	// admin overview dashboard (slide 3 + medium priority specs)
	private ScrollPane buildAdminDashboardOverview() { // handle buildadmindashboardoverview operation
		VBox container = new VBox(18); // execute statement
		container.setPadding(new Insets(2, 4, 16, 2)); // set inner padding spacing

		long pendingReqCount = state.requestData.stream().filter(r -> "Pending".equalsIgnoreCase(r.getStatus())).count(); // execute statement
		long pendingRetCount = state.returnData.stream().filter(r -> "Pending".equalsIgnoreCase(r.getStatus())).count(); // execute statement
		long missingRetCount = state.requestData.stream().filter(SeedRequest::isOverdue).count(); // execute statement
		double totalStockKg = state.productData.stream().mapToDouble(Product::getAmount).sum(); // execute statement
		int totalVarieties = (int) state.productData.stream().map(Product::getVariety).distinct().count(); // execute statement

		// top 4 metric cards (matching figma card proportions + medium priority specs)
		HBox topCards = new HBox(16); // execute statement
		topCards.getChildren().addAll( // add child nodes to container
				state.createStatCard(state.t("Pending Seed Requests", "Mga Pending na Request"), // continue statement
						String.valueOf(pendingReqCount), // continue statement
						state.t("Awaiting admin approval", "Nag-aantay ng approval ng admin"), "#d97706"), // continue statement
				state.createStatCard(state.t("Pending Seed Returns", "Mga Pending na Return"), // continue statement
						String.valueOf(pendingRetCount), // continue statement
						state.t("Awaiting quality inspection", "Ie-inspect pa ng admin"), state.PRIMARY_GREEN), // continue statement
				state.createStatCard(state.t("Missing Returns (Late)", "Mga Late na Balik-Binhi"), // continue statement
						String.valueOf(missingRetCount), // continue statement
						state.t("Past return deadline", "Lampas na sa due date"), "#dc3545"), // continue statement
				state.createStatCard(state.t("Inventory Info", "Lagay ng Imbentaryo"), // continue statement
						String.format("%.1f kg", totalStockKg), // continue statement
						totalVarieties + state.t(" varieties across ", " varieties sa ") + state.productData.size() + state.t(" lots", " lots"), state.PRIMARY_GREEN)); // execute statement

		// middle row: tasks / alerts card (left) + stock per crop card (right) matching slide 3
		HBox midRow = new HBox(16); // execute statement
		VBox.setVgrow(midRow, Priority.ALWAYS); // allow vertical growth inside vbox

		// left card: pending requests, pending returns & overdue missing returns
		VBox tasksCard = new VBox(12); // execute statement
		tasksCard.setStyle(state.cardStyle()); // apply inline css styling
		HBox.setHgrow(tasksCard, Priority.ALWAYS); // allow horizontal growth inside hbox
		tasksCard.setPrefWidth(620); // configure node width constraint

		Label lblTasksTitle = new Label(state.t("Action Items & Missing Returns", "Mga Kailangang Aksyon at Late na Returns")); // execute statement
		lblTasksTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + state.textPrimary() + ";"); // apply inline css styling

		VBox taskList = new VBox(10); // execute statement
		for (SeedRequest r : state.requestData) { // iterate through collection items
			if (r.isOverdue()) { // check condition before proceeding
				taskList.getChildren().add(state.createAlertRow( // append child node to container
						"OVERDUE \u2022 " + r.getTransactionCode(), // continue statement
						r.getFarmer() + " (" + r.getSeedLabel() + " - " + r.getAmount() + " kg) \u2014 Due: " + r.getDueDate(), // continue statement
						"#fde8e8", "#9b1c1c")); // execute statement
			}
		}
		for (SeedRequest r : state.requestData) { // iterate through collection items
			if ("Pending".equalsIgnoreCase(r.getStatus())) { // check condition before proceeding
				taskList.getChildren().add(state.createAlertRow( // append child node to container
						"REQUEST \u2022 " + r.getTransactionCode(), // continue statement
						r.getFarmer() + " requested " + r.getAmount() + " kg of " + r.getSeedLabel(), // continue statement
						"#fef3c7", "#92400e")); // execute statement
			}
		}
		for (SeedReturn ret : state.returnData) { // iterate through collection items
			if ("Pending".equalsIgnoreCase(ret.getStatus())) { // check condition before proceeding
				taskList.getChildren().add(state.createAlertRow( // append child node to container
						"RETURN \u2022 " + ret.getReturnCode(), // continue statement
						ret.getFarmer() + " returned " + ret.getAmount() + " kg of " + ret.getSeedLabel(), // continue statement
						"#e0f2fe", "#075985")); // execute statement
			}
		}
		if (taskList.getChildren().isEmpty()) { // check condition before proceeding
			Label emptyLbl = new Label(state.t("All seed requests, returns, and deadlines are up to date.", "Goods lahat! Walang pending o late na returns ngayon.")); // execute statement
			emptyLbl.setStyle("-fx-text-fill: " + state.textMuted() + ";"); // apply inline css styling
			taskList.getChildren().add(emptyLbl); // append child node to container
		}
		tasksCard.getChildren().addAll(lblTasksTitle, taskList); // add child nodes to container

		// right card: stock per crop / variety
		VBox stockCard = new VBox(12); // execute statement
		stockCard.setStyle(state.cardStyle()); // apply inline css styling
		stockCard.setPrefWidth(340); // configure node width constraint

		Label lblStockTitle = new Label(state.t("Stock per Crop", "Stock Bawat Pananim")); // execute statement
		lblStockTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + state.textPrimary() + ";"); // apply inline css styling

		VBox stockList = new VBox(10); // execute statement
		for (Product p : state.productData) { // iterate through collection items
			HBox row = new HBox(10); // execute statement
			row.setAlignment(Pos.CENTER_LEFT); // set container alignment
			row.setPadding(new Insets(8, 12, 8, 12)); // set inner padding spacing
			row.setStyle("-fx-background-color: " + state.bgPage() + "; -fx-background-radius: 10;"); // apply inline css styling
			Label lblCrop = new Label(p.getName() + " (" + p.getVariety() + ")"); // execute statement
			lblCrop.setStyle("-fx-font-weight: bold; -fx-text-fill: " + state.textPrimary() + ";"); // apply inline css styling
			Region sp = new Region(); // execute statement
			HBox.setHgrow(sp, Priority.ALWAYS); // allow horizontal growth inside hbox
			Label lblQty = new Label(p.getQuantityLabel()); // execute statement
			lblQty.setStyle("-fx-font-weight: bold; -fx-text-fill: " + state.PRIMARY_GREEN + ";"); // apply inline css styling
			row.getChildren().addAll(lblCrop, sp, lblQty); // add child nodes to container
			stockList.getChildren().add(row); // append child node to container
		}
		stockCard.getChildren().addAll(lblStockTitle, stockList); // add child nodes to container

		midRow.getChildren().addAll(tasksCard, stockCard); // add child nodes to container

		// bottom wide card: unverified farmer accounts & quick summary
		VBox bottomCard = new VBox(10); // execute statement
		bottomCard.setStyle(state.cardStyle()); // apply inline css styling
		long unverifiedCount = state.users.stream().filter(u -> "Farmer".equalsIgnoreCase(u.getRole()) && !u.isVerified()).count(); // execute statement
		double totalDonations = state.donationData.stream().mapToDouble(Donation::getAmount).sum(); // execute statement

		HBox bottomRow = new HBox(20); // execute statement
		bottomRow.setAlignment(Pos.CENTER_LEFT); // set container alignment
		Label lblBottom = new Label(state.t( // continue statement
				"Seed Bank Summary: " + unverifiedCount + " farmer account(s) awaiting verification  \u2022  Total Community Donations: \u20b1" + String.format("%,.2f", totalDonations), // continue statement
				"Buod ng Seed Bank: " + unverifiedCount + " account ng magsasaka ang naghihintay ng beripikasyon  \u2022  Kabuuang Donasyon: \u20b1" + String.format("%,.2f", totalDonations))); // execute statement
		lblBottom.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + state.textPrimary() + ";"); // apply inline css styling

		Region spBottom = new Region(); // execute statement
		HBox.setHgrow(spBottom, Priority.ALWAYS); // allow horizontal growth inside hbox

		Button btnVerifyShortcut = new Button(state.t("Manage Users  \u2192", "I-manage ang Users  \u2192")); // execute statement
		btnVerifyShortcut.setStyle(state.primaryButtonStyle()); // apply inline css styling
		btnVerifyShortcut.setOnAction(e -> { // attach button click event handler
			state.currentRoute = "Farmers"; // execute statement
			showAdminShell(); // execute statement
		}); // execute statement
		bottomRow.getChildren().addAll(lblBottom, spBottom, btnVerifyShortcut); // add child nodes to container
		bottomCard.getChildren().add(bottomRow); // append child node to container

		container.getChildren().addAll(topCards, midRow, bottomCard); // add child nodes to container

		ScrollPane scroll = new ScrollPane(container); // execute statement
		scroll.setFitToWidth(true); // execute statement
		return scroll; // return scroll
	}





	// admin inventory view (slide 4 & slide 5 - no "save and quit" button)
	private VBox buildAdminInventoryView() { // handle buildadmininventoryview operation
		VBox container = new VBox(18); // execute statement

		int varieties = (int) state.productData.stream().map(Product::getVariety).distinct().count(); // execute statement
		double totalStocks = state.productData.stream().mapToDouble(Product::getAmount).sum(); // execute statement
		int storageLots = state.productData.size(); // execute statement

		HBox topCards = new HBox(16); // execute statement
		topCards.getChildren().addAll( // add child nodes to container
				state.createStatCard(state.t("Plant Variety", "Variety ng Pananim"), String.valueOf(varieties), state.t("Distinct seed varieties", "Iba't ibang variety ng binhi"), state.PRIMARY_GREEN), // continue statement
				state.createStatCard(state.t("Seed Stock", "Stock ng Binhi"), String.format("%.1f kg", totalStocks), state.t("Total available weight", "Kabuuang timbang na available"), state.PRIMARY_GREEN), // continue statement
				state.createStatCard(state.t("Storage Lots", "Mga Seed Lot"), String.valueOf(storageLots), state.t("Active inventory slots", "Mga active na seed lot"), state.PRIMARY_GREEN)); // execute statement

		VBox tableCard = new VBox(14); // execute statement
		tableCard.setStyle(state.cardStyle()); // apply inline css styling
		VBox.setVgrow(tableCard, Priority.ALWAYS); // allow vertical growth inside vbox

		HBox bar = new HBox(12); // execute statement
		bar.setAlignment(Pos.CENTER_LEFT); // set container alignment

		TextField txtSearch = new TextField(); // execute statement
		txtSearch.setPromptText(state.t("\ud83d\udd0d  Search inventory lots...", "\ud83d\udd0d  Maghanap ng seed lot...")); // set input placeholder text
		txtSearch.setPrefWidth(360); // configure node width constraint
		txtSearch.setStyle(state.inputStyle()); // apply inline css styling

		Region spacer = new Region(); // execute statement
		HBox.setHgrow(spacer, Priority.ALWAYS); // allow horizontal growth inside hbox

		Button btnAddSlot = new Button(state.t("+  Add Inventory Slot", "+  Magdagdag ng Seed Lot")); // execute statement
		btnAddSlot.setStyle(state.primaryButtonStyle()); // apply inline css styling
		btnAddSlot.setOnAction(e -> openAddInventorySlotModal()); // attach button click event handler

		// inventory is saved automatically when stock changes.
		bar.getChildren().addAll(txtSearch, spacer, btnAddSlot); // add child nodes to container

		FilteredList<Product> filtered = new FilteredList<>(state.productData, p -> true); // execute statement
		txtSearch.textProperty().addListener((obs, oldV, newV) -> { // open code block
			String q = newV == null ? "" : newV.trim().toLowerCase(); // execute statement
			filtered.setPredicate(p -> q.isEmpty() // continue statement
					|| p.getName().toLowerCase().contains(q) // continue statement
					|| p.getVariety().toLowerCase().contains(q) // continue statement
					|| p.getLocation().toLowerCase().contains(q) // continue statement
					|| p.getLotCode().toLowerCase().contains(q)); // execute statement
		}); // execute statement

		TableView<Product> table = new TableView<>(filtered); // execute statement
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // execute statement
		VBox.setVgrow(table, Priority.ALWAYS); // allow vertical growth inside vbox

		TableColumn<Product, String> colLot = new TableColumn<>(state.t("Lot ID", "Lot ID")); // execute statement
		colLot.setCellValueFactory(new PropertyValueFactory<>("lotCode")); // execute statement

		TableColumn<Product, String> colVariety = new TableColumn<>(state.t("Variety", "Variety")); // execute statement
		colVariety.setCellValueFactory(new PropertyValueFactory<>("variety")); // execute statement

		TableColumn<Product, String> colLocation = new TableColumn<>(state.t("Location", "Lokasyon")); // execute statement
		colLocation.setCellValueFactory(new PropertyValueFactory<>("location")); // execute statement

		TableColumn<Product, String> colQty = new TableColumn<>(state.t("Quantity", "Dami")); // execute statement
		colQty.setCellValueFactory(new PropertyValueFactory<>("quantityLabel")); // execute statement

		TableColumn<Product, String> colStatus = new TableColumn<>(state.t("Status", "Status")); // execute statement
		colStatus.setCellValueFactory(new PropertyValueFactory<>("stockStatus")); // execute statement
		colStatus.setCellFactory(col -> new TableCell<Product, String>() { // open code block
			@Override // override parent method
			protected void updateItem(String item, boolean empty) { // handle updateitem operation
				super.updateItem(item, empty); // execute statement
				if (empty || item == null) { // check condition before proceeding
					setGraphic(null); // execute statement
				} else { // handle alternative branch
					Label badge = new Label(item); // execute statement
					boolean low = !"Available".equalsIgnoreCase(item); // execute statement
					badge.setStyle("-fx-background-color: " + (low ? "#fef3c7" : "#dcfce7") + ";" // apply inline css styling
							+ "-fx-text-fill: " + (low ? "#92400e" : "#166534") + ";" // continue statement
							+ "-fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 4 10 4 10; -fx-background-radius: 8;"); // execute statement
					setGraphic(badge); // execute statement
				}
			}
		}); // execute statement

		TableColumn<Product, Void> colAction = new TableColumn<>(""); // execute statement
		colAction.setCellFactory(col -> new TableCell<Product, Void>() { // open code block
			private final Button btnView = new Button(state.t("View", "Tingnan")); // store btnview field
			{
				btnView.setStyle("-fx-background-color: " + state.PRIMARY_GREEN + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 5 16 5 16; -fx-cursor: hand;"); // apply inline css styling
				btnView.setOnAction(e -> { // attach button click event handler
					Product p = getTableView().getItems().get(getIndex()); // execute statement
					openProductDetailDialog(p); // execute statement
				}); // execute statement
			}

			@Override // override parent method
			protected void updateItem(Void item, boolean empty) { // handle updateitem operation
				super.updateItem(item, empty); // execute statement
				setGraphic(empty ? null : btnView); // execute statement
			}
		}); // execute statement

		table.getColumns().addAll(colLot, colVariety, colLocation, colQty, colStatus, colAction); // execute statement
		tableCard.getChildren().addAll(bar, table); // add child nodes to container

		container.getChildren().addAll(topCards, tableCard); // add child nodes to container
		return container; // return container
	}

	// admin user management view (slide 6 & slide 7 + high priority specs)
	private VBox buildAdminUserManagementView() { // handle buildadminusermanagementview operation
		VBox container = new VBox(18); // execute statement

		long registeredFarms = state.users.stream().filter(u -> "Farmer".equalsIgnoreCase(u.getRole())).count(); // execute statement
		long verifiedFarms = state.users.stream().filter(u -> "Farmer".equalsIgnoreCase(u.getRole()) && u.isVerified()).count(); // execute statement
		long pendingReview = state.users.stream().filter(u -> "Farmer".equalsIgnoreCase(u.getRole()) && "Pending".equalsIgnoreCase(u.getStatus())).count(); // execute statement

		HBox topCards = new HBox(16); // execute statement
		topCards.getChildren().addAll( // add child nodes to container
				state.createStatCard(state.t("Registered Farms", "Mga Rehistradong Sakahan"), String.valueOf(registeredFarms), state.t("Total farmer accounts", "Bilang ng mga farmer"), state.PRIMARY_GREEN), // continue statement
				state.createStatCard(state.t("Verified Farms", "Mga Verified na Sakahan"), String.valueOf(verifiedFarms), state.t("Full seed request access", "May full access sa paghiram"), "#166534"), // continue statement
				state.createStatCard(state.t("Pending Review", "Nire-review pa"), String.valueOf(pendingReview), state.t("Awaiting admin verification", "Nag-aantay ma-verify ng admin"), "#d97706")); // execute statement

		VBox tableCard = new VBox(14); // execute statement
		tableCard.setStyle(state.cardStyle()); // apply inline css styling
		VBox.setVgrow(tableCard, Priority.ALWAYS); // allow vertical growth inside vbox

		HBox bar = new HBox(12); // execute statement
		bar.setAlignment(Pos.CENTER_LEFT); // set container alignment

		TextField txtSearch = new TextField(); // execute statement
		txtSearch.setPromptText(state.t("\ud83d\udd0d  Search users, farms, or locations...", "\ud83d\udd0d  Maghanap ng user, sakahan, o lugar...")); // set input placeholder text
		txtSearch.setPrefWidth(360); // configure node width constraint
		txtSearch.setStyle(state.inputStyle()); // apply inline css styling

		Region spacer = new Region(); // execute statement
		HBox.setHgrow(spacer, Priority.ALWAYS); // allow horizontal growth inside hbox

		// admins can create farmer or admin accounts from this tab.
		Button btnAddAccount = new Button(state.t("+  Add Farm / User Account", "+  Magdagdag ng Account")); // execute statement
		btnAddAccount.setStyle(state.primaryButtonStyle()); // apply inline css styling
		btnAddAccount.setOnAction(e -> openAdminCreateAccountModal()); // attach button click event handler

		bar.getChildren().addAll(txtSearch, spacer, btnAddAccount); // add child nodes to container

		FilteredList<User> filteredUsers = new FilteredList<>(state.users, u -> true); // execute statement
		txtSearch.textProperty().addListener((obs, oldV, newV) -> { // open code block
			String q = newV == null ? "" : newV.trim().toLowerCase(); // execute statement
			filteredUsers.setPredicate(u -> q.isEmpty() // continue statement
					|| u.getUsername().toLowerCase().contains(q) // continue statement
					|| u.getUserId().toLowerCase().contains(q) // continue statement
					|| u.getFarmName().toLowerCase().contains(q) // continue statement
					|| u.getFarmAddress().toLowerCase().contains(q) // continue statement
					|| u.getRole().toLowerCase().contains(q)); // execute statement
		}); // execute statement

		TableView<User> table = new TableView<>(filteredUsers); // execute statement
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // execute statement
		VBox.setVgrow(table, Priority.ALWAYS); // allow vertical growth inside vbox

		TableColumn<User, String> colId = new TableColumn<>(state.t("User ID", "User ID")); // execute statement
		colId.setCellValueFactory(new PropertyValueFactory<>("userId")); // execute statement

		TableColumn<User, String> colFarm = new TableColumn<>(state.t("User / Farm", "User / Sakahan")); // execute statement
		colFarm.setCellValueFactory(cell -> new SimpleStringProperty( // continue statement
				cell.getValue().getUsername() + " (" + cell.getValue().getFarmName() + ")")); // execute statement

		TableColumn<User, String> colLocation = new TableColumn<>(state.t("Location", "Lokasyon")); // execute statement
		colLocation.setCellValueFactory(new PropertyValueFactory<>("farmAddress")); // execute statement

		TableColumn<User, String> colRole = new TableColumn<>(state.t("Role", "Role")); // execute statement
		colRole.setCellValueFactory(new PropertyValueFactory<>("role")); // execute statement

		TableColumn<User, String> colStatus = new TableColumn<>(state.t("Status", "Status")); // execute statement
		colStatus.setCellValueFactory(new PropertyValueFactory<>("status")); // execute statement
		colStatus.setCellFactory(col -> new TableCell<User, String>() { // open code block
			@Override // override parent method
			protected void updateItem(String item, boolean empty) { // handle updateitem operation
				super.updateItem(item, empty); // execute statement
				if (empty || item == null) { // check condition before proceeding
					setGraphic(null); // execute statement
				} else { // handle alternative branch
					Label badge = new Label(item); // execute statement
					String bg = "#fef3c7"; // execute statement
					String fg = "#92400e"; // execute statement
					if ("Verified".equalsIgnoreCase(item)) { // check condition before proceeding
						bg = "#dcfce7"; // execute statement
						fg = "#166534"; // execute statement
					} else if ("Suspended".equalsIgnoreCase(item)) { // check fallback condition
						bg = "#fde8e8"; // execute statement
						fg = "#9b1c1c"; // execute statement
					}
					badge.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg + ";" // apply inline css styling
							+ "-fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 4 10 4 10; -fx-background-radius: 8;"); // execute statement
					setGraphic(badge); // execute statement
				}
			}
		}); // execute statement

		TableColumn<User, Void> colAction = new TableColumn<>(""); // execute statement
		colAction.setCellFactory(col -> new TableCell<User, Void>() { // open code block
			private final Button btnView = new Button(state.t("View", "Tingnan")); // store btnview field
			{
				btnView.setStyle("-fx-background-color: " + state.PRIMARY_GREEN + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 5 16 5 16; -fx-cursor: hand;"); // apply inline css styling
				btnView.setOnAction(e -> { // attach button click event handler
					User u = getTableView().getItems().get(getIndex()); // execute statement
					openUserDetailManagementModal(u); // execute statement
				}); // execute statement
			}

			@Override // override parent method
			protected void updateItem(Void item, boolean empty) { // handle updateitem operation
				super.updateItem(item, empty); // execute statement
				setGraphic(empty ? null : btnView); // execute statement
			}
		}); // execute statement

		table.getColumns().addAll(colId, colFarm, colLocation, colRole, colStatus, colAction); // execute statement
		tableCard.getChildren().addAll(bar, table); // add child nodes to container

		container.getChildren().addAll(topCards, tableCard); // add child nodes to container
		return container; // return container
	}

	// user management -> view details modal (farm info, farmer info, pending returns & requests, verify, assign admin, suspend user)
	private void openUserDetailManagementModal(User targetUser) { // handle openuserdetailmanagementmodal operation
		Stage modal = new Stage(); // execute statement
		modal.initOwner(state.mainStage); // execute statement
		modal.initModality(Modality.APPLICATION_MODAL); // execute statement
		modal.initStyle(StageStyle.UTILITY); // execute statement
		modal.setTitle(state.t("User & Farm Details", "Detalye ng User at Sakahan")); // execute statement

		VBox card = new VBox(16); // execute statement
		card.setPadding(new Insets(24)); // set inner padding spacing
		card.setStyle("-fx-background-color: " + state.bgCard() + ";"); // apply inline css styling

		Label lblHeader = new Label(state.t("Account Details \u2014 ", "Detalye ng Account \u2014 ") + targetUser.getUserId()); // execute statement
		lblHeader.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + state.textPrimary() + ";"); // apply inline css styling

		// 2-column info grid: farmer info & farm info
		GridPane grid = new GridPane(); // execute statement
		grid.setHgap(18); // execute statement
		grid.setVgap(10); // execute statement
		ColumnConstraints cc1 = new ColumnConstraints(); // execute statement
		cc1.setPercentWidth(50); // execute statement
		ColumnConstraints cc2 = new ColumnConstraints(); // execute statement
		cc2.setPercentWidth(50); // execute statement
		grid.getColumnConstraints().addAll(cc1, cc2); // execute statement

		VBox farmerInfoBox = new VBox(6); // execute statement
		farmerInfoBox.setStyle("-fx-background-color: " + state.bgPage() + "; -fx-padding: 14; -fx-background-radius: 12;"); // apply inline css styling
		Label lblFarmerSec = new Label(state.t("Farmer Information", "Impormasyon ng Farmer")); // execute statement
		lblFarmerSec.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: " + state.PRIMARY_GREEN + ";"); // apply inline css styling
		farmerInfoBox.getChildren().addAll( // add child nodes to container
				lblFarmerSec, // continue statement
				state.makeDetailLine(state.t("Username:", "Username:"), targetUser.getUsername()), // continue statement
				state.makeDetailLine(state.t("Role:", "Role:"), targetUser.getRole()), // continue statement
				state.makeDetailLine("Status:", targetUser.getStatus() // continue statement
						+ (targetUser.isSuspended() && !targetUser.getSuspensionReason().isEmpty() // continue statement
							? " — " + targetUser.getSuspensionReason() : "")), // continue statement
				state.makeDetailLine(state.t("Phone Number:", "Cellphone Number:"), targetUser.getPhoneNumber().isEmpty() ? "N/A" : targetUser.getPhoneNumber()), // continue statement
				state.makeDetailLine(state.t("Email Address:", "Email Address:"), targetUser.getEmail().isEmpty() ? "N/A" : targetUser.getEmail())); // execute statement

		VBox farmInfoBox = new VBox(6); // execute statement
		farmInfoBox.setStyle("-fx-background-color: " + state.bgPage() + "; -fx-padding: 14; -fx-background-radius: 12;"); // apply inline css styling
		Label lblFarmSec = new Label(state.t("Farm Information", "Impormasyon ng Sakahan")); // execute statement
		lblFarmSec.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: " + state.PRIMARY_GREEN + ";"); // apply inline css styling
		farmInfoBox.getChildren().addAll( // add child nodes to container
				lblFarmSec, // continue statement
				state.makeDetailLine(state.t("Farm Name:", "Pangalan ng Sakahan:"), targetUser.getFarmName()), // continue statement
				state.makeDetailLine(state.t("Farm Address:", "Address ng Sakahan:"), targetUser.getFarmAddress().isEmpty() ? "N/A" : targetUser.getFarmAddress()), // continue statement
				state.makeDetailLine(state.t("RSBSA Number:", "RSBSA Number:"), targetUser.getRsbsaNumber()), // continue statement
				state.makeDetailLine(state.t("Farm Area:", "Laki ng Sakahan:"), targetUser.getFarmArea())); // execute statement

		grid.add(farmerInfoBox, 0, 0); // execute statement
		grid.add(farmInfoBox, 1, 0); // execute statement

		// pending seed requests & pending seed returns for this user
		VBox activityBox = new VBox(8); // execute statement
		activityBox.setStyle("-fx-background-color: " + state.bgPage() + "; -fx-padding: 14; -fx-background-radius: 12;"); // apply inline css styling
		Label lblActTitle = new Label(state.t("Pending Seed Requests & Pending Seed Returns", "Mga Pending na Seed Request at Seed Return")); // execute statement
		lblActTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: " + state.textPrimary() + ";"); // apply inline css styling

		List<SeedRequest> userPendingReqs = state.requestData.stream() // continue statement
				.filter(r -> r.getFarmer().equalsIgnoreCase(targetUser.getUsername()) && "Pending".equalsIgnoreCase(r.getStatus())) // continue statement
				.collect(Collectors.toList()); // execute statement
		List<SeedReturn> userPendingRets = state.returnData.stream() // continue statement
				.filter(r -> r.getFarmer().equalsIgnoreCase(targetUser.getUsername()) && "Pending".equalsIgnoreCase(r.getStatus())) // continue statement
				.collect(Collectors.toList()); // execute statement

		VBox actItems = new VBox(4); // execute statement
		if (userPendingReqs.isEmpty() && userPendingRets.isEmpty()) { // check condition before proceeding
			Label none = new Label(state.t("No pending seed requests or returns for this user.", "Walang pending na request o return ang user na ito.")); // execute statement
			none.setStyle("-fx-text-fill: " + state.textMuted() + "; -fx-font-size: 12px;"); // apply inline css styling
			actItems.getChildren().add(none); // append child node to container
		} else { // handle alternative branch
			for (SeedRequest r : userPendingReqs) { // iterate through collection items
				Label l = new Label("\u2022 [Request " + r.getTransactionCode() + "] " + r.getSeedLabel() + " (" + r.getAmount() + " kg) \u2014 " + r.getPurpose()); // execute statement
				l.setStyle("-fx-text-fill: " + state.textPrimary() + "; -fx-font-size: 12px;"); // apply inline css styling
				actItems.getChildren().add(l); // append child node to container
			}
			for (SeedReturn ret : userPendingRets) { // iterate through collection items
				Label l = new Label("\u2022 [Return " + ret.getReturnCode() + "] " + ret.getSeedLabel() + " (" + ret.getAmount() + " kg)"); // execute statement
				l.setStyle("-fx-text-fill: " + state.textPrimary() + "; -fx-font-size: 12px;"); // apply inline css styling
				actItems.getChildren().add(l); // append child node to container
			}
		}
		activityBox.getChildren().addAll(lblActTitle, actItems); // add child nodes to container

		Label lblFeedback = new Label(); // execute statement
		lblFeedback.setStyle("-fx-text-fill: " + state.PRIMARY_GREEN + "; -fx-font-weight: bold; -fx-font-size: 12px;"); // apply inline css styling

		// action controls: verify user, assign admin, suspend / reactivate user
		HBox actionBar = new HBox(10); // execute statement
		actionBar.setAlignment(Pos.CENTER_RIGHT); // set container alignment

		Button btnVerify = new Button(targetUser.isVerified() ? state.t("Verified \u2713", "Verified Na \u2713") : state.t("Verify User", "I-verify ang User")); // execute statement
		btnVerify.setStyle(state.primaryButtonStyle()); // apply inline css styling
		btnVerify.setDisable(targetUser.isVerified()); // execute statement
		btnVerify.setOnAction(e -> { // attach button click event handler
			targetUser.setStatus("Verified"); // execute statement
			state.saveUsers(state.userPath, state.users); // execute statement
			lblFeedback.setText(state.t("User verified! Full farmer access granted.", "Verified na ang farmer! May full access na siya.")); // update displayed label text
			modal.close(); // execute statement
			showAdminShell(); // execute statement
		}); // execute statement

		boolean isAlreadyAdmin = "Admin".equalsIgnoreCase(targetUser.getRole()); // execute statement
		Button btnAssignAdmin = new Button(isAlreadyAdmin ? state.t("Revoke Admin (Set Farmer)", "Alisin Bilang Admin (Gawing Farmer)") : state.t("Assign Admin", "Gawing Admin")); // execute statement
		btnAssignAdmin.setStyle(state.outlineButtonStyle()); // apply inline css styling
		btnAssignAdmin.setDisable(targetUser.getUsername().equalsIgnoreCase(state.currentUser.getUsername())); // execute statement
		btnAssignAdmin.setOnAction(e -> { // attach button click event handler
			if (isAlreadyAdmin) { // check condition before proceeding
				targetUser.setRole("Farmer"); // execute statement
			} else { // handle alternative branch
				targetUser.setRole("Admin"); // execute statement
				targetUser.setStatus("Verified"); // execute statement
			}
			state.saveUsers(state.userPath, state.users); // execute statement
			modal.close(); // execute statement
			showAdminShell(); // execute statement
		}); // execute statement

		boolean isSuspended = targetUser.isSuspended(); // execute statement
		Button btnSuspend = new Button(isSuspended ? "Unsuspend User" : "Suspend User"); // execute statement
		btnSuspend.setStyle(state.dangerOutlineButtonStyle()); // apply inline css styling
		btnSuspend.setDisable(targetUser.getUsername().equalsIgnoreCase(state.currentUser.getUsername())); // execute statement
		btnSuspend.setOnAction(e -> { // attach button click event handler
			if (isSuspended) { // check condition before proceeding
				targetUser.setStatus("Verified"); // execute statement
				targetUser.setSuspensionReason(""); // execute statement
				state.saveUsers(state.userPath, state.users); // execute statement
				modal.close(); // execute statement
				showAdminShell(); // execute statement
			} else { // handle alternative branch
				// ask admin for a suspension reason
				Stage reasonStage = new Stage(); // execute statement
				reasonStage.initOwner(modal); // execute statement
				reasonStage.initModality(Modality.APPLICATION_MODAL); // execute statement
				reasonStage.initStyle(StageStyle.UTILITY); // execute statement
				reasonStage.setTitle("Suspension Reason"); // execute statement

				VBox reasonBox = new VBox(12); // execute statement
				reasonBox.setPadding(new Insets(20)); // set inner padding spacing
				reasonBox.setStyle("-fx-background-color: " + state.bgCard() + ";"); // apply inline css styling

				Label lblR = new Label("Please provide a reason for suspending this account:"); // execute statement
				lblR.setStyle("-fx-font-size: 13px; -fx-text-fill: " + state.textPrimary() + ";"); // apply inline css styling

				TextArea txtReason = new TextArea(); // execute statement
				txtReason.setPromptText("e.g. Repeated overdue seed returns, policy violation..."); // set input placeholder text
				txtReason.setPrefRowCount(3); // execute statement
				txtReason.setWrapText(true); // enable text wrapping
				txtReason.setStyle(state.inputStyle()); // apply inline css styling

				Label reasonWarn = new Label(); // execute statement
				reasonWarn.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 12px;"); // apply inline css styling

				Button btnConfirm = new Button("Confirm Suspension"); // execute statement
				btnConfirm.setStyle(state.dangerOutlineButtonStyle()); // apply inline css styling
				btnConfirm.setOnAction(ev -> { // attach button click event handler
					String reason = txtReason.getText().trim(); // execute statement
					if (reason.isEmpty()) { // check condition before proceeding
						reasonWarn.setText("A reason is required."); // update displayed label text
						return; // execute statement
					}
					targetUser.setStatus("Suspended"); // execute statement
					targetUser.setSuspensionReason(reason); // execute statement
					state.saveUsers(state.userPath, state.users); // execute statement
					reasonStage.close(); // execute statement
					modal.close(); // execute statement
					showAdminShell(); // execute statement
				}); // execute statement

				Button btnCancel = new Button("Cancel"); // execute statement
				btnCancel.setStyle(state.outlineButtonStyle()); // apply inline css styling
				btnCancel.setOnAction(ev -> reasonStage.close()); // attach button click event handler

				HBox rBtns = new HBox(10, btnCancel, btnConfirm); // execute statement
				rBtns.setAlignment(Pos.CENTER_RIGHT); // set container alignment
				reasonBox.getChildren().addAll(lblR, txtReason, reasonWarn, rBtns); // add child nodes to container
				reasonStage.setScene(new Scene(reasonBox, 420, 240)); // execute statement
				reasonStage.showAndWait(); // execute statement
			}
		}); // execute statement

		Button btnClose = new Button(state.t("Close", "Isara")); // execute statement
		btnClose.setStyle(state.outlineButtonStyle()); // apply inline css styling
		btnClose.setOnAction(e -> modal.close()); // attach button click event handler

		actionBar.getChildren().addAll(btnClose, btnSuspend, btnAssignAdmin, btnVerify); // add child nodes to container

		card.getChildren().addAll(lblHeader, grid, activityBox, lblFeedback, actionBar); // add child nodes to container

		Scene scene = new Scene(card, 660, 490); // execute statement
		modal.setScene(scene); // execute statement
		modal.showAndWait(); // execute statement
	}



	// modal for creating a new farm / user account.
	private void openAdminCreateAccountModal() { // handle openadmincreateaccountmodal operation
		Stage modal = new Stage(); // execute statement
		modal.initOwner(state.mainStage); // execute statement
		modal.initModality(Modality.APPLICATION_MODAL); // execute statement
		modal.initStyle(StageStyle.UTILITY); // execute statement
		modal.setTitle(state.t("Add Farm / User Account", "Magdagdag ng Account")); // execute statement

		VBox card = new VBox(16); // execute statement
		card.setPadding(new Insets(26)); // set inner padding spacing
		card.setStyle("-fx-background-color: " + state.bgCard() + ";"); // apply inline css styling

		Label lblTitle = new Label(state.t("Add Farm Account", "Magdagdag ng Farm Account")); // execute statement
		lblTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + state.textPrimary() + ";"); // apply inline css styling

		GridPane grid = new GridPane(); // execute statement
		grid.setHgap(18); // execute statement
		grid.setVgap(12); // execute statement
		ColumnConstraints c1 = new ColumnConstraints(); // execute statement
		c1.setPercentWidth(50); // execute statement
		ColumnConstraints c2 = new ColumnConstraints(); // execute statement
		c2.setPercentWidth(50); // execute statement
		grid.getColumnConstraints().addAll(c1, c2); // execute statement

		TextField txtUsername = new TextField(); // execute statement
		txtUsername.setPromptText("e.g. pedro_farmer"); // set input placeholder text
		txtUsername.setStyle(state.inputStyle()); // apply inline css styling

		PasswordField txtPassword = new PasswordField(); // execute statement
		txtPassword.setPromptText("\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022"); // set input placeholder text
		txtPassword.setStyle(state.inputStyle()); // apply inline css styling

		TextField txtPhone = new TextField(); // execute statement
		txtPhone.setPromptText("0917-000-0000"); // set input placeholder text
		txtPhone.setStyle(state.inputStyle()); // apply inline css styling

		TextField txtEmail = new TextField(); // execute statement
		txtEmail.setPromptText("farmer@laguna.ph"); // set input placeholder text
		txtEmail.setStyle(state.inputStyle()); // apply inline css styling

		TextField txtFarmName = new TextField(); // execute statement
		txtFarmName.setPromptText("e.g. Bay Laguna Farm"); // set input placeholder text
		txtFarmName.setStyle(state.inputStyle()); // apply inline css styling

		TextField txtFarmAddress = new TextField(); // execute statement
		txtFarmAddress.setPromptText("Barangay, Municipality, Laguna"); // set input placeholder text
		txtFarmAddress.setStyle(state.inputStyle()); // apply inline css styling

		ComboBox<String> roleCombo = new ComboBox<>(); // execute statement
		roleCombo.getItems().addAll("Farmer", "Admin"); // execute statement
		roleCombo.setValue("Farmer"); // execute statement
		roleCombo.setMaxWidth(Double.MAX_VALUE); // configure node width constraint
		roleCombo.setStyle(state.inputStyle()); // apply inline css styling

		ComboBox<String> verifyCombo = new ComboBox<>(); // execute statement
		verifyCombo.getItems().addAll("Verified", "Pending"); // execute statement
		verifyCombo.setValue("Verified"); // option 2: accounts created by admin are active immediately
		verifyCombo.setMaxWidth(Double.MAX_VALUE); // configure node width constraint
		verifyCombo.setStyle(state.inputStyle()); // apply inline css styling

		grid.add(state.wrapField(state.t("Username *", "Username *"), txtUsername), 0, 0); // execute statement
		grid.add(state.wrapField(state.t("Password *", "Password *"), txtPassword), 1, 0); // execute statement
		grid.add(state.wrapField(state.t("Phone Number *", "Cellphone Number *"), txtPhone), 0, 1); // execute statement
		grid.add(state.wrapField(state.t("Email Address *", "Email Address *"), txtEmail), 1, 1); // execute statement
		grid.add(state.wrapField(state.t("Farm Name", "Pangalan ng Sakahan"), txtFarmName), 0, 2); // execute statement
		grid.add(state.wrapField(state.t("Farm Address *", "Address ng Sakahan *"), txtFarmAddress), 1, 2); // execute statement
		grid.add(state.wrapField(state.t("Account Role (Admin / Farmer) *", "Role ng Account (Admin / Farmer) *"), roleCombo), 0, 3); // execute statement
		grid.add(state.wrapField(state.t("Initial Verification Status", "Status ng Verification"), verifyCombo), 1, 3); // execute statement

		Label lblWarn = new Label(); // execute statement
		lblWarn.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold; -fx-font-size: 12px;"); // apply inline css styling

		HBox buttons = new HBox(12); // execute statement
		buttons.setAlignment(Pos.CENTER_RIGHT); // set container alignment

		Button btnCancel = new Button(state.t("Cancel", "Kanselahin")); // execute statement
		btnCancel.setStyle(state.outlineButtonStyle()); // apply inline css styling
		btnCancel.setOnAction(e -> modal.close()); // attach button click event handler

		Button btnSave = new Button(state.t("Save Account", "I-save ang Account")); // execute statement
		btnSave.setStyle(state.primaryButtonStyle()); // apply inline css styling
		btnSave.setOnAction(e -> { // attach button click event handler
			String uname = txtUsername.getText().trim(); // execute statement
			String pass = txtPassword.getText(); // execute statement
			String phone = txtPhone.getText().trim(); // execute statement
			String email = txtEmail.getText().trim(); // execute statement
			String addr = txtFarmAddress.getText().trim(); // execute statement
			String role = roleCombo.getValue(); // execute statement
			String fName = txtFarmName.getText().trim().isEmpty() ? uname + " Farm" : txtFarmName.getText().trim(); // execute statement
			String initStatus = "Admin".equalsIgnoreCase(role) ? "Verified" : verifyCombo.getValue(); // execute statement

			if (uname.isEmpty() || pass.isEmpty() || phone.isEmpty() || email.isEmpty() || addr.isEmpty()) { // check condition before proceeding
				lblWarn.setText(state.t("Error: All required fields (*) must be completed.", "Oops! Paki-fill up lahat ng required fields (*).")); // update displayed label text
				return; // execute statement
			}
			for (User u : state.users) { // iterate through collection items
				if (u.getUsername().equalsIgnoreCase(uname)) { // check condition before proceeding
					lblWarn.setText(state.t("Error: Username already exists.", "May gumagamit na ng username na 'yan.")); // update displayed label text
					return; // execute statement
				}
			}

			String prefix = "Admin".equalsIgnoreCase(role) ? "ADM-" : "FRM-"; // execute statement
			String uid = String.format("%s%03d", prefix, state.nextUserId++); // execute statement
			state.users.add(new User(uid, uname, pass, role, phone, email, fName, addr, "RSBSA-" + state.nextUserId, "1.5 ha", initStatus)); // execute statement
			state.saveUsers(state.userPath, state.users); // execute statement
			modal.close(); // execute statement
			showAdminShell(); // execute statement
		}); // execute statement

		buttons.getChildren().addAll(btnCancel, btnSave); // add child nodes to container
		card.getChildren().addAll(lblTitle, grid, lblWarn, buttons); // add child nodes to container

		modal.setScene(new Scene(card, 640, 470)); // execute statement
		modal.showAndWait(); // execute statement
	}



	// admin transactions view (slide 8 & slide 9: requests & returns)
	private VBox buildAdminTransactionsView() { // handle buildadmintransactionsview operation
		VBox container = new VBox(18); // execute statement

		long pendingReqs = state.requestData.stream().filter(r -> "Pending".equalsIgnoreCase(r.getStatus())).count(); // execute statement
		long activeLoans = state.requestData.stream().filter(r -> "Approved".equalsIgnoreCase(r.getStatus())).count(); // execute statement
		long dueSoon = state.requestData.stream().filter(r -> "Approved".equalsIgnoreCase(r.getStatus()) // continue statement
				&& r.getDueDate() != null && !r.getDueDate().isAfter(LocalDate.now().plusDays(14))).count(); // execute statement

		HBox topCards = new HBox(16); // execute statement
		topCards.getChildren().addAll( // add child nodes to container
				state.createStatCard(state.t("Pending Requests", "Mga Pending na Request"), String.valueOf(pendingReqs), state.t("Awaiting approval", "Pending pa ang approval"), "#d97706"), // continue statement
				state.createStatCard(state.t("Active Loans", "Mga Active na Hiram"), String.valueOf(activeLoans), state.t("Disbursed seed lots", "Naipahiram na binhi"), state.PRIMARY_GREEN), // continue statement
				state.createStatCard(state.t("Returns Due Soon / Late", "Malapit na / Late na Due Date"), String.valueOf(dueSoon), state.t("Within 14 days or overdue", "Sa loob ng 14 days o overdue na"), "#dc3545")); // execute statement

		VBox tableCard = new VBox(14); // execute statement
		tableCard.setStyle(state.cardStyle()); // apply inline css styling
		VBox.setVgrow(tableCard, Priority.ALWAYS); // allow vertical growth inside vbox

		HBox filterBar = new HBox(12); // execute statement
		filterBar.setAlignment(Pos.CENTER_LEFT); // set container alignment

		TextField txtSearch = new TextField(); // execute statement
		txtSearch.setPromptText(state.t("\ud83d\udd0d  Search requests by farmer, farm, or seed...", "\ud83d\udd0d  Maghanap ng request...")); // set input placeholder text
		txtSearch.setPrefWidth(360); // configure node width constraint
		txtSearch.setStyle(state.inputStyle()); // apply inline css styling

		Region spacer = new Region(); // execute statement
		HBox.setHgrow(spacer, Priority.ALWAYS); // allow horizontal growth inside hbox

		Button btnSort = new Button(state.t("\u21c5  Sort by Due Date", "\u21c5  Ayusin ayon sa Due Date")); // execute statement
		btnSort.setStyle(state.primaryButtonStyle()); // apply inline css styling
		btnSort.setOnAction(e -> FXCollections.sort(state.requestData, Comparator.comparing(SeedRequest::getDueDate))); // attach button click event handler

		filterBar.getChildren().addAll(txtSearch, spacer, btnSort); // add child nodes to container

		FilteredList<SeedRequest> filteredReqs = new FilteredList<>(state.requestData, r -> true); // execute statement
		txtSearch.textProperty().addListener((obs, oldV, newV) -> { // open code block
			String q = newV == null ? "" : newV.trim().toLowerCase(); // execute statement
			filteredReqs.setPredicate(r -> q.isEmpty() // continue statement
					|| r.getTransactionCode().toLowerCase().contains(q) // continue statement
					|| r.getFarmer().toLowerCase().contains(q) // continue statement
					|| r.getFarmName().toLowerCase().contains(q) // continue statement
					|| r.getSeedLabel().toLowerCase().contains(q)); // execute statement
		}); // execute statement

		TableView<SeedRequest> reqTable = new TableView<>(filteredReqs); // execute statement
		reqTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // execute statement
		reqTable.setPrefHeight(210); // configure node height constraint

		TableColumn<SeedRequest, String> colTxId = new TableColumn<>(state.t("Transaction ID", "Transaction ID")); // execute statement
		colTxId.setCellValueFactory(new PropertyValueFactory<>("transactionCode")); // execute statement

		TableColumn<SeedRequest, String> colFarm = new TableColumn<>(state.t("Farm / Farmer", "Sakahan / Farmer")); // execute statement
		colFarm.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFarmName() + " (" + c.getValue().getFarmer() + ")")); // execute statement

		TableColumn<SeedRequest, String> colSeed = new TableColumn<>(state.t("Seed Product", "Binhi")); // execute statement
		colSeed.setCellValueFactory(new PropertyValueFactory<>("seedLabel")); // execute statement

		TableColumn<SeedRequest, String> colQty = new TableColumn<>(state.t("Quantity", "Dami")); // execute statement
		colQty.setCellValueFactory(new PropertyValueFactory<>("quantityLabel")); // execute statement

		TableColumn<SeedRequest, String> colDue = new TableColumn<>(state.t("Due Date", "Due Date")); // execute statement
		colDue.setCellValueFactory(c -> new SimpleStringProperty( // continue statement
				c.getValue().getDueDate() == null ? "-" : c.getValue().getDueDate().format(state.DATE_FMT))); // execute statement

		TableColumn<SeedRequest, String> colStatus = new TableColumn<>(state.t("Status", "Status")); // execute statement
		colStatus.setCellValueFactory(new PropertyValueFactory<>("status")); // execute statement
		colStatus.setCellFactory(col -> state.createStatusBadgeCell()); // execute statement

		TableColumn<SeedRequest, Void> colView = new TableColumn<>(""); // execute statement
		colView.setCellFactory(col -> new TableCell<SeedRequest, Void>() { // open code block
			private final Button btnView = new Button(state.t("View", "Tingnan")); // store btnview field
			{
				btnView.setStyle("-fx-background-color: " + state.PRIMARY_GREEN + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 5 16 5 16; -fx-cursor: hand;"); // apply inline css styling
				btnView.setOnAction(e -> openTransactionDetailModal(getTableView().getItems().get(getIndex()))); // attach button click event handler
			}

			@Override // override parent method
			protected void updateItem(Void item, boolean empty) { // handle updateitem operation
				super.updateItem(item, empty); // execute statement
				setGraphic(empty ? null : btnView); // execute statement
			}
		}); // execute statement

		reqTable.getColumns().addAll(colTxId, colFarm, colSeed, colQty, colDue, colStatus, colView); // execute statement

		// seed returns sub-section
		Label lblReturnsHeader = new Label(state.t("Seed Returns Inspection Queue", "Mga Ibinalik na Binhi (For Inspection)")); // execute statement
		lblReturnsHeader.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + state.textPrimary() + ";"); // apply inline css styling

		TableView<SeedReturn> retTable = new TableView<>(state.returnData); // execute statement
		retTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // execute statement
		retTable.setPrefHeight(160); // configure node height constraint

		TableColumn<SeedReturn, String> colRetCode = new TableColumn<>(state.t("Return ID", "Return ID")); // execute statement
		colRetCode.setCellValueFactory(new PropertyValueFactory<>("returnCode")); // execute statement

		TableColumn<SeedReturn, String> colRetFarmer = new TableColumn<>(state.t("Farmer", "Farmer")); // execute statement
		colRetFarmer.setCellValueFactory(new PropertyValueFactory<>("farmer")); // execute statement

		TableColumn<SeedReturn, String> colRetSeed = new TableColumn<>(state.t("Seed Product", "Binhi")); // execute statement
		colRetSeed.setCellValueFactory(new PropertyValueFactory<>("seedLabel")); // execute statement

		TableColumn<SeedReturn, String> colRetQty = new TableColumn<>(state.t("Returned Qty", "Ibinalik na Dami")); // execute statement
		colRetQty.setCellValueFactory(new PropertyValueFactory<>("quantityLabel")); // execute statement

		TableColumn<SeedReturn, String> colRetStatus = new TableColumn<>(state.t("Status", "Status")); // execute statement
		colRetStatus.setCellValueFactory(new PropertyValueFactory<>("status")); // execute statement
		colRetStatus.setCellFactory(col -> state.createStatusBadgeCell()); // execute statement

		TableColumn<SeedReturn, Void> colRetAction = new TableColumn<>(state.t("Inspection", "Inspeksyon")); // execute statement
		colRetAction.setCellFactory(col -> new TableCell<SeedReturn, Void>() { // open code block
			private final Button btnAccept = new Button(state.t("Accept", "Tanggapin")); // store btnaccept field
			private final Button btnReject = new Button(state.t("Reject", "I-reject")); // store btnreject field
			private final HBox box = new HBox(6, btnAccept, btnReject); // store box field
			{
				btnAccept.setStyle("-fx-background-color: " + state.PRIMARY_GREEN + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 4 10 4 10; -fx-cursor: hand;"); // apply inline css styling
				btnReject.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 4 10 4 10; -fx-cursor: hand;"); // apply inline css styling
				btnAccept.setOnAction(e -> { // attach button click event handler
					SeedReturn ret = getTableView().getItems().get(getIndex()); // execute statement
					if ("Pending".equalsIgnoreCase(ret.getStatus())) { // check condition before proceeding
						Product prod = state.findProduct(ret.getLotId()); // execute statement
						if (prod != null) { // check condition before proceeding
							prod.setAmount(state.round2(prod.getAmount() + ret.getAmount())); // execute statement
							state.saveProducts(state.productPath, state.productData); // execute statement
						}
						ret.setStatus("Accepted"); // execute statement
						// clear overdue status on the linked seed request
						for (SeedRequest req : state.requestData) { // iterate through collection items
							if (req.getRequestId() == ret.getRequestId()) { // check condition before proceeding
								req.setStatus("Returned"); // execute statement
								break; // exit current loop
							}
						}
						showAdminShell(); // execute statement
					}
				}); // execute statement
				btnReject.setOnAction(e -> { // attach button click event handler
					SeedReturn ret = getTableView().getItems().get(getIndex()); // execute statement
					if ("Pending".equalsIgnoreCase(ret.getStatus())) { // check condition before proceeding
						ret.setStatus("Rejected"); // execute statement
						showAdminShell(); // execute statement
					}
				}); // execute statement
			}

			@Override // override parent method
			protected void updateItem(Void item, boolean empty) { // handle updateitem operation
				super.updateItem(item, empty); // execute statement
				if (empty) { // check condition before proceeding
					setGraphic(null); // execute statement
				} else { // handle alternative branch
					SeedReturn r = getTableView().getItems().get(getIndex()); // execute statement
					setGraphic("Pending".equalsIgnoreCase(r.getStatus()) ? box : new Label("-")); // execute statement
				}
			}
		}); // execute statement

		retTable.getColumns().addAll(colRetCode, colRetFarmer, colRetSeed, colRetQty, colRetStatus, colRetAction); // execute statement

		tableCard.getChildren().addAll(filterBar, reqTable, lblReturnsHeader, retTable); // add child nodes to container
		container.getChildren().addAll(topCards, tableCard); // add child nodes to container
		return container; // return container
	}

	// slide 9: transaction details modal (reject / approve)
	private void openTransactionDetailModal(SeedRequest req) { // handle opentransactiondetailmodal operation
		Stage modal = new Stage(); // execute statement
		modal.initOwner(state.mainStage); // execute statement
		modal.initModality(Modality.APPLICATION_MODAL); // execute statement
		modal.initStyle(StageStyle.UTILITY); // execute statement
		modal.setTitle(state.t("Transaction Details", "Detalye ng Transaksyon")); // execute statement

		VBox card = new VBox(16); // execute statement
		card.setPadding(new Insets(26)); // set inner padding spacing
		card.setStyle("-fx-background-color: " + state.bgCard() + ";"); // apply inline css styling

		Label lblTitle = new Label(state.t("Transaction Details", "Detalye ng Transaksyon")); // execute statement
		lblTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + state.textPrimary() + ";"); // apply inline css styling

		Label lblSubId = new Label("Transaction ID: " + req.getTransactionCode() + "   \u2022   Status: " + req.getStatus()); // execute statement
		lblSubId.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: " + state.textPrimary() + ";"); // apply inline css styling

		GridPane grid = new GridPane(); // execute statement
		grid.setHgap(18); // execute statement
		grid.setVgap(12); // execute statement
		ColumnConstraints c1 = new ColumnConstraints(); // execute statement
		c1.setPercentWidth(50); // execute statement
		ColumnConstraints c2 = new ColumnConstraints(); // execute statement
		c2.setPercentWidth(50); // execute statement
		grid.getColumnConstraints().addAll(c1, c2); // execute statement

		grid.add(state.makeReadOnlyBox(state.t("Farm", "Sakahan"), req.getFarmName()), 0, 0); // execute statement
		grid.add(state.makeReadOnlyBox(state.t("Farm Rep.", "Kinatawan"), req.getFarmer()), 1, 0); // execute statement
		grid.add(state.makeReadOnlyBox(state.t("Seed Product", "Binhi"), req.getSeedLabel()), 0, 1); // execute statement
		grid.add(state.makeReadOnlyBox(state.t("Quantity", "Dami"), req.getQuantityLabel() + " (" + req.getPurpose() + ")"), 1, 1); // execute statement
		grid.add(state.makeReadOnlyBox(state.t("Request Date", "Petsa ng Request"), // continue statement
				req.getRequestDate() == null ? "-" : req.getRequestDate().format(state.DATE_FMT)), 0, 2); // execute statement
		grid.add(state.makeReadOnlyBox(state.t("Seed Value", "Halaga ng Binhi"), "\u20b1" + String.format("%,.2f", req.getTotalValue())), 1, 2); // execute statement
		grid.add(state.makeReadOnlyBox(state.t("Due Date", "Due Date"), // continue statement
				req.getDueDate() == null ? "-" : req.getDueDate().format(state.DATE_FMT)), 0, 3); // execute statement
		grid.add(state.makeReadOnlyBox(state.t("Total Repayment Left", "Natitirang Ibabalik"), req.getQuantityLabel()), 1, 3); // execute statement

		Label lblWarn = new Label(); // execute statement
		lblWarn.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;"); // apply inline css styling

		HBox btnRow = new HBox(12); // execute statement
		btnRow.setAlignment(Pos.CENTER_RIGHT); // set container alignment

		Button btnClose = new Button(state.t("Close", "Isara")); // execute statement
		btnClose.setStyle(state.outlineButtonStyle()); // apply inline css styling
		btnClose.setOnAction(e -> modal.close()); // attach button click event handler

		Button btnReject = new Button(state.t("Reject", "I-reject")); // execute statement
		btnReject.setStyle(state.dangerOutlineButtonStyle()); // apply inline css styling
		btnReject.setDisable(!"Pending".equalsIgnoreCase(req.getStatus())); // execute statement
		btnReject.setOnAction(e -> { // attach button click event handler
			req.setStatus("Rejected"); // execute statement
			modal.close(); // execute statement
			showAdminShell(); // execute statement
		}); // execute statement

		Button btnApprove = new Button(state.t("Approve", "I-approve")); // execute statement
		btnApprove.setStyle(state.primaryButtonStyle()); // apply inline css styling
		btnApprove.setDisable(!"Pending".equalsIgnoreCase(req.getStatus())); // execute statement
		btnApprove.setOnAction(e -> { // attach button click event handler
			Product prod = state.findProduct(req.getLotId()); // execute statement
			if (prod == null || prod.getAmount() < req.getAmount()) { // check condition before proceeding
				lblWarn.setText(state.t("Error: Insufficient seed stock in inventory.", "Kulang na ang stock nito sa inventory.")); // update displayed label text
				return; // execute statement
			}
			prod.setAmount(state.round2(prod.getAmount() - req.getAmount())); // execute statement
			req.setStatus("Approved"); // execute statement
			if (req.getDueDate() == null) { // check condition before proceeding
				req.setDueDate(LocalDate.now().plusDays(90)); // execute statement
			}
			state.saveProducts(state.productPath, state.productData); // execute statement
			modal.close(); // execute statement
			showAdminShell(); // execute statement
		}); // execute statement

		btnRow.getChildren().addAll(btnClose, btnReject, btnApprove); // add child nodes to container
		card.getChildren().addAll(lblTitle, lblSubId, grid, lblWarn, btnRow); // add child nodes to container

		modal.setScene(new Scene(card, 640, 470)); // execute statement
		modal.showAndWait(); // execute statement
	}



	// admin donations view
	private VBox buildAdminDonationsView() { // handle buildadmindonationsview operation
		VBox container = new VBox(18); // execute statement
		double totalDonations = state.donationData.stream().mapToDouble(Donation::getAmount).sum(); // execute statement

		HBox topCards = new HBox(16); // execute statement
		topCards.getChildren().addAll( // add child nodes to container
				state.createStatCard(state.t("Total Community Donations", "Kabuuang Donasyon"), // continue statement
						"\u20b1" + String.format("%,.2f", totalDonations), // continue statement
						state.donationData.size() + state.t(" recorded contributions", " naitalang donasyon"), state.PRIMARY_GREEN)); // execute statement

		VBox card = new VBox(14); // execute statement
		card.setStyle(state.cardStyle()); // apply inline css styling
		VBox.setVgrow(card, Priority.ALWAYS); // allow vertical growth inside vbox

		TableView<Donation> table = new TableView<>(state.donationData); // execute statement
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // execute statement
		VBox.setVgrow(table, Priority.ALWAYS); // allow vertical growth inside vbox

		TableColumn<Donation, String> colDonor = new TableColumn<>(state.t("Donor Name", "Pangalan ng Donor")); // execute statement
		colDonor.setCellValueFactory(new PropertyValueFactory<>("donorName")); // execute statement

		TableColumn<Donation, String> colEmail = new TableColumn<>("Email"); // execute statement
		colEmail.setCellValueFactory(new PropertyValueFactory<>("email")); // execute statement

		TableColumn<Donation, String> colAmount = new TableColumn<>(state.t("Amount", "Halaga")); // execute statement
		colAmount.setCellValueFactory(c -> new SimpleStringProperty("\u20b1" + String.format("%,.2f", c.getValue().getAmount()))); // execute statement

		TableColumn<Donation, String> colMessage = new TableColumn<>(state.t("Message", "Mensahe")); // execute statement
		colMessage.setCellValueFactory(new PropertyValueFactory<>("message")); // execute statement

		table.getColumns().addAll(colDonor, colEmail, colAmount, colMessage); // execute statement
		card.getChildren().add(table); // append child node to container

		container.getChildren().addAll(topCards, card); // add child nodes to container
		return container; // return container
	}


	// slide 5: add inventory slot modal + product detail / edit dialogs
	private void openAddInventorySlotModal() { // handle openaddinventoryslotmodal operation
		Stage modal = new Stage(); // execute statement
		modal.initOwner(state.mainStage); // execute statement
		modal.initModality(Modality.APPLICATION_MODAL); // execute statement
		modal.initStyle(StageStyle.UTILITY); // execute statement
		modal.setTitle(state.t("Add Inventory Slot", "Magdagdag ng Seed Lot")); // execute statement

		VBox card = new VBox(16); // execute statement
		card.setPadding(new Insets(26)); // set inner padding spacing
		card.setStyle("-fx-background-color: " + state.bgCard() + ";"); // apply inline css styling

		Label lblTitle = new Label(state.t("Add Inventory Slot", "Magdagdag ng Seed Lot")); // execute statement
		lblTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + state.textPrimary() + ";"); // apply inline css styling

		GridPane grid = new GridPane(); // execute statement
		grid.setHgap(18); // execute statement
		grid.setVgap(12); // execute statement
		ColumnConstraints c1 = new ColumnConstraints(); // execute statement
		c1.setPercentWidth(50); // execute statement
		ColumnConstraints c2 = new ColumnConstraints(); // execute statement
		c2.setPercentWidth(50); // execute statement
		grid.getColumnConstraints().addAll(c1, c2); // execute statement

		TextField txtProduct = new TextField(); // execute statement
		txtProduct.setPromptText("e.g. Rice / Pineapple"); // set input placeholder text
		txtProduct.setStyle(state.inputStyle()); // apply inline css styling

		TextField txtLotId = new TextField(); // execute statement
		txtLotId.setPromptText("e.g. 4"); // set input placeholder text
		txtLotId.setStyle(state.inputStyle()); // apply inline css styling

		TextField txtQty = new TextField(); // execute statement
		txtQty.setPromptText("e.g. 200"); // set input placeholder text
		txtQty.setStyle(state.inputStyle()); // apply inline css styling

		ComboBox<String> unitBox = new ComboBox<>(); // execute statement
		unitBox.getItems().addAll("kg", "sacks", "grams"); // execute statement
		unitBox.setValue("kg"); // execute statement
		unitBox.setMaxWidth(Double.MAX_VALUE); // configure node width constraint
		unitBox.setStyle(state.inputStyle()); // apply inline css styling

		TextField txtLocation = new TextField(); // execute statement
		txtLocation.setPromptText("e.g. Storage A"); // set input placeholder text
		txtLocation.setStyle(state.inputStyle()); // apply inline css styling

		TextField txtThreshold = new TextField("25"); // execute statement
		txtThreshold.setStyle(state.inputStyle()); // apply inline css styling

		TextField txtVariety = new TextField(); // execute statement
		txtVariety.setPromptText("e.g. NSIC Rc222"); // set input placeholder text
		txtVariety.setStyle(state.inputStyle()); // apply inline css styling

		TextField txtExpiry = new TextField("2027-12-31"); // execute statement
		txtExpiry.setStyle(state.inputStyle()); // apply inline css styling

		TextField txtPrice = new TextField("50.0"); // execute statement
		txtPrice.setStyle(state.inputStyle()); // apply inline css styling

		TextField txtDesc = new TextField(); // execute statement
		txtDesc.setPromptText(state.t("Seed notes / origin", "Notes o pinagmulan ng binhi")); // set input placeholder text
		txtDesc.setStyle(state.inputStyle()); // apply inline css styling

		grid.add(state.wrapField(state.t("Product *", "Pangalan ng Pananim *"), txtProduct), 0, 0); // execute statement
		grid.add(state.wrapField(state.t("Lot ID *", "Lot ID *"), txtLotId), 1, 0); // execute statement
		grid.add(state.wrapField(state.t("Quantity *", "Dami *"), txtQty), 0, 1); // execute statement
		grid.add(state.wrapField(state.t("Unit", "Unit"), unitBox), 1, 1); // execute statement
		grid.add(state.wrapField(state.t("Storage Location *", "Lokasyon sa Imbakan *"), txtLocation), 0, 2); // execute statement
		grid.add(state.wrapField(state.t("Minimum Threshold", "Minimum Stock Alert"), txtThreshold), 1, 2); // execute statement
		grid.add(state.wrapField(state.t("Variety *", "Variety *"), txtVariety), 0, 3); // execute statement
		grid.add(state.wrapField(state.t("Expiry Date", "Expiry Date"), txtExpiry), 1, 3); // execute statement
		grid.add(state.wrapField(state.t("Price per Unit (\u20b1) *", "Presyo Bawat Unit (\u20b1) *"), txtPrice), 0, 4); // execute statement
		grid.add(state.wrapField(state.t("Description *", "Paglalarawan *"), txtDesc), 1, 4); // execute statement

		Label lblWarn = new Label(); // execute statement
		lblWarn.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;"); // apply inline css styling

		HBox buttons = new HBox(12); // execute statement
		buttons.setAlignment(Pos.CENTER_RIGHT); // set container alignment

		Button btnCancel = new Button(state.t("Cancel", "Kanselahin")); // execute statement
		btnCancel.setStyle(state.outlineButtonStyle()); // apply inline css styling
		btnCancel.setOnAction(e -> modal.close()); // attach button click event handler

		Button btnSave = new Button(state.t("Save Slot", "I-save ang Seed Lot")); // execute statement
		btnSave.setStyle(state.primaryButtonStyle()); // apply inline css styling
		btnSave.setOnAction(e -> { // attach button click event handler
			try { // start protected execution block
				String name = txtProduct.getText().trim(); // execute statement
				int lot = Integer.parseInt(txtLotId.getText().trim()); // execute statement
				double qty = Double.parseDouble(txtQty.getText().trim()); // execute statement
				String loc = txtLocation.getText().trim(); // execute statement
				double thresh = Double.parseDouble(txtThreshold.getText().trim()); // execute statement
				String var = txtVariety.getText().trim(); // execute statement
				String exp = txtExpiry.getText().trim(); // execute statement
				double price = Double.parseDouble(txtPrice.getText().trim()); // execute statement
				String desc = txtDesc.getText().trim().isEmpty() ? name + " (" + var + ")" : txtDesc.getText().trim(); // execute statement

				if (name.isEmpty() || loc.isEmpty() || var.isEmpty()) { // check condition before proceeding
					lblWarn.setText(state.t("Error: Fill in all required fields (*).", "Oops! Paki-fill up lahat ng required fields (*).")); // update displayed label text
					return; // execute statement
				}
				state.productData.add(new Product(name, price, qty, lot, loc, var, desc, unitBox.getValue(), thresh, exp)); // execute statement
				modal.close(); // execute statement
				showAdminShell(); // execute statement
			} catch (NumberFormatException ex) { // handle caught exception
				lblWarn.setText(state.t("Error: Lot ID, Quantity, Threshold, and Price must be numeric.", "Oops! Dapat number ang Lot ID, Dami, Threshold, at Presyo.")); // update displayed label text
			}
		}); // execute statement

		buttons.getChildren().addAll(btnCancel, btnSave); // add child nodes to container
		card.getChildren().addAll(lblTitle, grid, lblWarn, buttons); // add child nodes to container

		modal.setScene(new Scene(card, 640, 520)); // execute statement
		modal.showAndWait(); // execute statement
	}

	private void openProductDetailDialog(Product product) { // handle openproductdetaildialog operation
		Stage detailStage = new Stage(); // execute statement
		detailStage.initOwner(state.mainStage); // execute statement
		detailStage.initModality(Modality.APPLICATION_MODAL); // execute statement
		detailStage.setTitle(state.t("Product Details", "Detalye ng Binhi")); // execute statement

		VBox vbox = new VBox(10); // execute statement
		vbox.setPadding(new Insets(22)); // set inner padding spacing
		vbox.setStyle("-fx-background-color: " + state.bgCard() + ";"); // apply inline css styling

		Label title = new Label(product.getLotCode() + " \u2014 " + product.getName()); // execute statement
		title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + state.textPrimary() + ";"); // apply inline css styling

		Button btnEdit = new Button(state.t("Edit", "I-edit")); // execute statement
		btnEdit.setStyle(state.primaryButtonStyle()); // apply inline css styling
		btnEdit.setOnAction(e -> openEditProductDialog(product, detailStage)); // attach button click event handler

		Button btnDelete = new Button(state.t("Delete", "I-delete")); // execute statement
		btnDelete.setStyle(state.dangerOutlineButtonStyle()); // apply inline css styling
		btnDelete.setOnAction(e -> { // attach button click event handler
			state.productData.remove(product); // execute statement
			detailStage.close(); // execute statement
			showAdminShell(); // execute statement
		}); // execute statement

		Button btnClose = new Button(state.t("Close", "Isara")); // execute statement
		btnClose.setStyle(state.outlineButtonStyle()); // apply inline css styling
		btnClose.setOnAction(e -> detailStage.close()); // attach button click event handler

		HBox btns = new HBox(10, btnClose, btnDelete, btnEdit); // execute statement
		btns.setAlignment(Pos.CENTER_RIGHT); // set container alignment

		vbox.getChildren().addAll( // add child nodes to container
				title, // continue statement
				state.makeDetailLine("Variety:", product.getVariety()), // continue statement
				state.makeDetailLine("Location:", product.getLocation()), // continue statement
				state.makeDetailLine("Amount:", product.getQuantityLabel()), // continue statement
				state.makeDetailLine("Price:", "\u20b1" + product.getPrice()), // continue statement
				state.makeDetailLine("Min Threshold:", product.getMinThreshold() + " " + product.getUnit()), // continue statement
				state.makeDetailLine("Expiry:", product.getExpiryDate()), // continue statement
				state.makeDetailLine("Description:", product.getDescription()), // continue statement
				btns); // execute statement

		detailStage.setScene(new Scene(vbox, 420, 340)); // execute statement
		detailStage.showAndWait(); // execute statement
	}

	private void openEditProductDialog(Product product, Stage detailStage) { // handle openeditproductdialog operation
		Stage editStage = new Stage(); // execute statement
		editStage.initOwner(detailStage); // execute statement
		editStage.initModality(Modality.APPLICATION_MODAL); // execute statement
		editStage.setTitle(state.t("Edit Product", "I-edit ang Binhi")); // execute statement

		TextField textName = new TextField(product.getName()); // execute statement
		textName.setStyle(state.inputStyle()); // apply inline css styling
		TextField textPrice = new TextField(String.valueOf(product.getPrice())); // execute statement
		textPrice.setStyle(state.inputStyle()); // apply inline css styling
		TextField textAmount = new TextField(String.valueOf(product.getAmount())); // execute statement
		textAmount.setStyle(state.inputStyle()); // apply inline css styling
		TextField textLotID = new TextField(String.valueOf(product.getLotID())); // execute statement
		textLotID.setStyle(state.inputStyle()); // apply inline css styling
		TextField textLocation = new TextField(product.getLocation()); // execute statement
		textLocation.setStyle(state.inputStyle()); // apply inline css styling
		TextField textVariety = new TextField(product.getVariety()); // execute statement
		textVariety.setStyle(state.inputStyle()); // apply inline css styling
		TextField textDescription = new TextField(product.getDescription()); // execute statement
		textDescription.setStyle(state.inputStyle()); // apply inline css styling

		Label labelWarn = new Label(); // execute statement
		labelWarn.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;"); // apply inline css styling

		Button btnSave = new Button(state.t("Save Changes", "I-save ang Pagbabago")); // execute statement
		btnSave.setStyle(state.primaryButtonStyle()); // apply inline css styling
		btnSave.setOnAction(e -> { // attach button click event handler
			try { // start protected execution block
				product.setName(textName.getText().trim()); // execute statement
				product.setPrice(Double.parseDouble(textPrice.getText().trim())); // execute statement
				product.setAmount(Double.parseDouble(textAmount.getText().trim())); // execute statement
				product.setLotID(Integer.parseInt(textLotID.getText().trim())); // execute statement
				product.setLocation(textLocation.getText().trim()); // execute statement
				product.setVariety(textVariety.getText().trim()); // execute statement
				product.setDescription(textDescription.getText().trim()); // execute statement

				int idx = state.productData.indexOf(product); // execute statement
				if (idx >= 0) state.productData.set(idx, product); // check condition before proceeding

				editStage.close(); // execute statement
				detailStage.close(); // execute statement
				showAdminShell(); // execute statement
			} catch (NumberFormatException ex) { // handle caught exception
				labelWarn.setText("Error: Invalid numeric values."); // update displayed label text
			}
		}); // execute statement

		VBox vbox = new VBox(8, // continue statement
				state.wrapField("Name", textName), // continue statement
				state.wrapField("Price", textPrice), // continue statement
				state.wrapField("Amount", textAmount), // continue statement
				state.wrapField("Lot ID", textLotID), // continue statement
				state.wrapField("Location", textLocation), // continue statement
				state.wrapField("Variety", textVariety), // continue statement
				state.wrapField("Description", textDescription), // continue statement
				labelWarn, btnSave); // execute statement
		vbox.setPadding(new Insets(18)); // set inner padding spacing
		vbox.setStyle("-fx-background-color: " + state.bgCard() + ";"); // apply inline css styling

		editStage.setScene(new Scene(vbox, 420, 520)); // execute statement
		editStage.showAndWait(); // execute statement
	}

}
