package application.admin;

import application.Donation;
import application.Product;
import application.SeedRequest;
import application.SeedReturn;
import application.User;
import application.shared.AppState;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AdminPages {

	private final AppState state;
	private final Runnable onLogout;

	public AdminPages(AppState state, Runnable onLogout) {
		this.state = state;
		this.onLogout = onLogout;
	}

	// =========================================================================
	// PERSISTENT SIDEBAR + HEADER SHELL (MATCHING FIGMA DASHBOARD SCREENS)
	// =========================================================================
	public void showAdminShell() {
		BorderPane shell = new BorderPane();
		shell.setStyle("-fx-background-color: " + state.bgPage() + ";");

		// Left Dark Forest Sidebar (#0c3026)
		VBox sidebar = new VBox(8);
		sidebar.setPrefWidth(250);
		sidebar.setMinWidth(250);
		sidebar.setPadding(new Insets(26, 16, 24, 16));
		sidebar.setStyle("-fx-background-color: " + state.FOREST_DARK + ";");

		Node logo = state.buildLogo(0.95, true);
		VBox logoWrap = new VBox(logo);
		logoWrap.setPadding(new Insets(0, 8, 28, 8));

		Button navDashboard = state.createNavButton(this::showAdminShell, state.t("Dashboard", "Dashboard"), "Dashboard", true);
		Button navInventory = state.createNavButton(this::showAdminShell, state.t("Inventory", "Imbentaryo"), "Inventory", true);
		Button navFarmers = state.createNavButton(this::showAdminShell, state.t("Farmers & Users", "Mga Farmer at User"), "Farmers", true);
		Button navTransactions = state.createNavButton(this::showAdminShell, state.t("Transactions", "Mga Transaksyon"), "Transactions", true);
		Button navDonations = state.createNavButton(this::showAdminShell, state.t("Donations", "Mga Donasyon"), "Donations", true);

		Region spacer = new Region();
		VBox.setVgrow(spacer, Priority.ALWAYS);

		Label lblRoleBadge = new Label("ADMIN \u2022 " + state.currentUser.getUsername());
		lblRoleBadge.setStyle("-fx-text-fill: #9cb8ad; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 0 0 6 12;");

		Button btnLogout = new Button(state.t("Logout", "Mag-log Out"));
		btnLogout.setMaxWidth(Double.MAX_VALUE);
		btnLogout.setStyle("-fx-background-color: rgba(255,255,255,0.10); -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 10 16 10 16; -fx-cursor: hand;");
		btnLogout.setOnAction(e -> {
			state.saveProducts(state.productPath, state.productData);
			state.saveUsers(state.userPath, state.users);
			state.currentUser = null;
			onLogout.run();
		});

		sidebar.getChildren().addAll(logoWrap, navDashboard, navInventory, navFarmers, navTransactions, navDonations, spacer, lblRoleBadge, btnLogout);

		// Main Content Area
		VBox centerWrapper = new VBox(18);
		centerWrapper.setPadding(new Insets(24, 32, 24, 32));
		centerWrapper.setStyle("-fx-background-color: " + state.bgPage() + ";");

		HBox topHeader = state.buildTopHeader(state.getAdminPageTitle(), true, this::showAdminShell);

		Node pageBody;
		switch (state.currentRoute) {
			case "Inventory":
				pageBody = buildAdminInventoryView();
				break;
			case "Farmers":
				pageBody = buildAdminUserManagementView();
				break;
			case "Transactions":
				pageBody = buildAdminTransactionsView();
				break;
			case "Donations":
				pageBody = buildAdminDonationsView();
				break;
			default:
				pageBody = buildAdminDashboardOverview();
				break;
		}
		VBox.setVgrow(pageBody, Priority.ALWAYS);

		centerWrapper.getChildren().addAll(topHeader, pageBody);

		shell.setLeft(sidebar);
		shell.setCenter(centerWrapper);

		state.applyScene(new Scene(shell, 1200, 780), "Seed Spreaders - Admin (" + state.currentRoute + ")");
	}

	// =========================================================================
	// ADMIN OVERVIEW DASHBOARD (SLIDE 3 + MEDIUM PRIORITY SPECS)
	// =========================================================================
	private ScrollPane buildAdminDashboardOverview() {
		VBox container = new VBox(18);
		container.setPadding(new Insets(2, 4, 16, 2));

		long pendingReqCount = state.requestData.stream().filter(r -> "Pending".equalsIgnoreCase(r.getStatus())).count();
		long pendingRetCount = state.returnData.stream().filter(r -> "Pending".equalsIgnoreCase(r.getStatus())).count();
		long missingRetCount = state.requestData.stream().filter(SeedRequest::isOverdue).count();
		double totalStockKg = state.productData.stream().mapToDouble(Product::getAmount).sum();
		int totalVarieties = (int) state.productData.stream().map(Product::getVariety).distinct().count();

		// Top 4 Metric Cards (Matching Figma card proportions + Medium Priority specs)
		HBox topCards = new HBox(16);
		topCards.getChildren().addAll(
				state.createStatCard(state.t("Pending Seed Requests", "Mga Pending na Request"),
						String.valueOf(pendingReqCount),
						state.t("Awaiting admin approval", "Nag-aantay ng approval ng admin"), "#d97706"),
				state.createStatCard(state.t("Pending Seed Returns", "Mga Pending na Return"),
						String.valueOf(pendingRetCount),
						state.t("Awaiting quality inspection", "Ie-inspect pa ng admin"), state.PRIMARY_GREEN),
				state.createStatCard(state.t("Missing Returns (Late)", "Mga Late na Balik-Binhi"),
						String.valueOf(missingRetCount),
						state.t("Past return deadline", "Lampas na sa due date"), "#dc3545"),
				state.createStatCard(state.t("Inventory Info", "Lagay ng Imbentaryo"),
						String.format("%.1f kg", totalStockKg),
						totalVarieties + state.t(" varieties across ", " varieties sa ") + state.productData.size() + state.t(" lots", " lots"), state.PRIMARY_GREEN));

		// Middle Row: Tasks / Alerts Card (Left) + Stock per Crop Card (Right) matching Slide 3
		HBox midRow = new HBox(16);
		VBox.setVgrow(midRow, Priority.ALWAYS);

		// Left Card: Pending Requests, Pending Returns & Overdue Missing Returns
		VBox tasksCard = new VBox(12);
		tasksCard.setStyle(state.cardStyle());
		HBox.setHgrow(tasksCard, Priority.ALWAYS);
		tasksCard.setPrefWidth(620);

		Label lblTasksTitle = new Label(state.t("Action Items & Missing Returns", "Mga Kailangang Aksyon at Late na Returns"));
		lblTasksTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + state.textPrimary() + ";");

		VBox taskList = new VBox(10);
		for (SeedRequest r : state.requestData) {
			if (r.isOverdue()) {
				taskList.getChildren().add(state.createAlertRow(
						"OVERDUE \u2022 " + r.getTransactionCode(),
						r.getFarmer() + " (" + r.getSeedLabel() + " - " + r.getAmount() + " kg) \u2014 Due: " + r.getDueDate(),
						"#fde8e8", "#9b1c1c"));
			}
		}
		for (SeedRequest r : state.requestData) {
			if ("Pending".equalsIgnoreCase(r.getStatus())) {
				taskList.getChildren().add(state.createAlertRow(
						"REQUEST \u2022 " + r.getTransactionCode(),
						r.getFarmer() + " requested " + r.getAmount() + " kg of " + r.getSeedLabel(),
						"#fef3c7", "#92400e"));
			}
		}
		for (SeedReturn ret : state.returnData) {
			if ("Pending".equalsIgnoreCase(ret.getStatus())) {
				taskList.getChildren().add(state.createAlertRow(
						"RETURN \u2022 " + ret.getReturnCode(),
						ret.getFarmer() + " returned " + ret.getAmount() + " kg of " + ret.getSeedLabel(),
						"#e0f2fe", "#075985"));
			}
		}
		if (taskList.getChildren().isEmpty()) {
			Label emptyLbl = new Label(state.t("All seed requests, returns, and deadlines are up to date.", "Goods lahat! Walang pending o late na returns ngayon."));
			emptyLbl.setStyle("-fx-text-fill: " + state.textMuted() + ";");
			taskList.getChildren().add(emptyLbl);
		}
		tasksCard.getChildren().addAll(lblTasksTitle, taskList);

		// Right Card: Stock per Crop / Variety
		VBox stockCard = new VBox(12);
		stockCard.setStyle(state.cardStyle());
		stockCard.setPrefWidth(340);

		Label lblStockTitle = new Label(state.t("Stock per Crop", "Stock Bawat Pananim"));
		lblStockTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + state.textPrimary() + ";");

		VBox stockList = new VBox(10);
		for (Product p : state.productData) {
			HBox row = new HBox(10);
			row.setAlignment(Pos.CENTER_LEFT);
			row.setPadding(new Insets(8, 12, 8, 12));
			row.setStyle("-fx-background-color: " + state.bgPage() + "; -fx-background-radius: 10;");
			Label lblCrop = new Label(p.getName() + " (" + p.getVariety() + ")");
			lblCrop.setStyle("-fx-font-weight: bold; -fx-text-fill: " + state.textPrimary() + ";");
			Region sp = new Region();
			HBox.setHgrow(sp, Priority.ALWAYS);
			Label lblQty = new Label(p.getQuantityLabel());
			lblQty.setStyle("-fx-font-weight: bold; -fx-text-fill: " + state.PRIMARY_GREEN + ";");
			row.getChildren().addAll(lblCrop, sp, lblQty);
			stockList.getChildren().add(row);
		}
		stockCard.getChildren().addAll(lblStockTitle, stockList);

		midRow.getChildren().addAll(tasksCard, stockCard);

		// Bottom Wide Card: Unverified Farmer Accounts & Quick Summary
		VBox bottomCard = new VBox(10);
		bottomCard.setStyle(state.cardStyle());
		long unverifiedCount = state.users.stream().filter(u -> "Farmer".equalsIgnoreCase(u.getRole()) && !u.isVerified()).count();
		double totalDonations = state.donationData.stream().mapToDouble(Donation::getAmount).sum();

		HBox bottomRow = new HBox(20);
		bottomRow.setAlignment(Pos.CENTER_LEFT);
		Label lblBottom = new Label(state.t(
				"Seed Bank Summary: " + unverifiedCount + " farmer account(s) awaiting verification  \u2022  Total Community Donations: \u20b1" + String.format("%,.2f", totalDonations),
				"Buod ng Seed Bank: " + unverifiedCount + " account ng magsasaka ang naghihintay ng beripikasyon  \u2022  Kabuuang Donasyon: \u20b1" + String.format("%,.2f", totalDonations)));
		lblBottom.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + state.textPrimary() + ";");

		Region spBottom = new Region();
		HBox.setHgrow(spBottom, Priority.ALWAYS);

		Button btnVerifyShortcut = new Button(state.t("Manage Users  \u2192", "I-manage ang Users  \u2192"));
		btnVerifyShortcut.setStyle(state.primaryButtonStyle());
		btnVerifyShortcut.setOnAction(e -> {
			state.currentRoute = "Farmers";
			showAdminShell();
		});
		bottomRow.getChildren().addAll(lblBottom, spBottom, btnVerifyShortcut);
		bottomCard.getChildren().add(bottomRow);

		container.getChildren().addAll(topCards, midRow, bottomCard);

		ScrollPane scroll = new ScrollPane(container);
		scroll.setFitToWidth(true);
		return scroll;
	}





	// =========================================================================
	// ADMIN INVENTORY VIEW (SLIDE 4 & SLIDE 5 - NO "SAVE AND QUIT" BUTTON)
	// =========================================================================
	private VBox buildAdminInventoryView() {
		VBox container = new VBox(18);

		int varieties = (int) state.productData.stream().map(Product::getVariety).distinct().count();
		double totalStocks = state.productData.stream().mapToDouble(Product::getAmount).sum();
		int storageLots = state.productData.size();

		HBox topCards = new HBox(16);
		topCards.getChildren().addAll(
				state.createStatCard(state.t("Plant Variety", "Variety ng Pananim"), String.valueOf(varieties), state.t("Distinct seed varieties", "Iba't ibang variety ng binhi"), state.PRIMARY_GREEN),
				state.createStatCard(state.t("Seed Stock", "Stock ng Binhi"), String.format("%.1f kg", totalStocks), state.t("Total available weight", "Kabuuang timbang na available"), state.PRIMARY_GREEN),
				state.createStatCard(state.t("Storage Lots", "Mga Seed Lot"), String.valueOf(storageLots), state.t("Active inventory slots", "Mga active na seed lot"), state.PRIMARY_GREEN));

		VBox tableCard = new VBox(14);
		tableCard.setStyle(state.cardStyle());
		VBox.setVgrow(tableCard, Priority.ALWAYS);

		HBox bar = new HBox(12);
		bar.setAlignment(Pos.CENTER_LEFT);

		TextField txtSearch = new TextField();
		txtSearch.setPromptText(state.t("\ud83d\udd0d  Search inventory lots...", "\ud83d\udd0d  Maghanap ng seed lot..."));
		txtSearch.setPrefWidth(360);
		txtSearch.setStyle(state.inputStyle());

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		Button btnAddSlot = new Button(state.t("+  Add Inventory Slot", "+  Magdagdag ng Seed Lot"));
		btnAddSlot.setStyle(state.primaryButtonStyle());
		btnAddSlot.setOnAction(e -> openAddInventorySlotModal());

		// Inventory is saved automatically when stock changes.
		bar.getChildren().addAll(txtSearch, spacer, btnAddSlot);

		FilteredList<Product> filtered = new FilteredList<>(state.productData, p -> true);
		txtSearch.textProperty().addListener((obs, oldV, newV) -> {
			String q = newV == null ? "" : newV.trim().toLowerCase();
			filtered.setPredicate(p -> q.isEmpty()
					|| p.getName().toLowerCase().contains(q)
					|| p.getVariety().toLowerCase().contains(q)
					|| p.getLocation().toLowerCase().contains(q)
					|| p.getLotCode().toLowerCase().contains(q));
		});

		TableView<Product> table = new TableView<>(filtered);
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		VBox.setVgrow(table, Priority.ALWAYS);

		TableColumn<Product, String> colLot = new TableColumn<>(state.t("Lot ID", "Lot ID"));
		colLot.setCellValueFactory(new PropertyValueFactory<>("lotCode"));

		TableColumn<Product, String> colVariety = new TableColumn<>(state.t("Variety", "Variety"));
		colVariety.setCellValueFactory(new PropertyValueFactory<>("variety"));

		TableColumn<Product, String> colLocation = new TableColumn<>(state.t("Location", "Lokasyon"));
		colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));

		TableColumn<Product, String> colQty = new TableColumn<>(state.t("Quantity", "Dami"));
		colQty.setCellValueFactory(new PropertyValueFactory<>("quantityLabel"));

		TableColumn<Product, String> colStatus = new TableColumn<>(state.t("Status", "Status"));
		colStatus.setCellValueFactory(new PropertyValueFactory<>("stockStatus"));
		colStatus.setCellFactory(col -> new TableCell<Product, String>() {
			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setGraphic(null);
				} else {
					Label badge = new Label(item);
					boolean low = !"Available".equalsIgnoreCase(item);
					badge.setStyle("-fx-background-color: " + (low ? "#fef3c7" : "#dcfce7") + ";"
							+ "-fx-text-fill: " + (low ? "#92400e" : "#166534") + ";"
							+ "-fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 4 10 4 10; -fx-background-radius: 8;");
					setGraphic(badge);
				}
			}
		});

		TableColumn<Product, Void> colAction = new TableColumn<>("");
		colAction.setCellFactory(col -> new TableCell<Product, Void>() {
			private final Button btnView = new Button(state.t("View", "Tingnan"));
			{
				btnView.setStyle("-fx-background-color: " + state.PRIMARY_GREEN + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 5 16 5 16; -fx-cursor: hand;");
				btnView.setOnAction(e -> {
					Product p = getTableView().getItems().get(getIndex());
					openProductDetailDialog(p);
				});
			}

			@Override
			protected void updateItem(Void item, boolean empty) {
				super.updateItem(item, empty);
				setGraphic(empty ? null : btnView);
			}
		});

		table.getColumns().addAll(colLot, colVariety, colLocation, colQty, colStatus, colAction);
		tableCard.getChildren().addAll(bar, table);

		container.getChildren().addAll(topCards, tableCard);
		return container;
	}

	// =========================================================================
	// ADMIN USER MANAGEMENT VIEW (SLIDE 6 & SLIDE 7 + HIGH PRIORITY SPECS)
	// =========================================================================
	private VBox buildAdminUserManagementView() {
		VBox container = new VBox(18);

		long registeredFarms = state.users.stream().filter(u -> "Farmer".equalsIgnoreCase(u.getRole())).count();
		long verifiedFarms = state.users.stream().filter(u -> "Farmer".equalsIgnoreCase(u.getRole()) && u.isVerified()).count();
		long pendingReview = state.users.stream().filter(u -> "Farmer".equalsIgnoreCase(u.getRole()) && "Pending".equalsIgnoreCase(u.getStatus())).count();

		HBox topCards = new HBox(16);
		topCards.getChildren().addAll(
				state.createStatCard(state.t("Registered Farms", "Mga Rehistradong Sakahan"), String.valueOf(registeredFarms), state.t("Total farmer accounts", "Bilang ng mga farmer"), state.PRIMARY_GREEN),
				state.createStatCard(state.t("Verified Farms", "Mga Verified na Sakahan"), String.valueOf(verifiedFarms), state.t("Full seed request access", "May full access sa paghiram"), "#166534"),
				state.createStatCard(state.t("Pending Review", "Nire-review pa"), String.valueOf(pendingReview), state.t("Awaiting admin verification", "Nag-aantay ma-verify ng admin"), "#d97706"));

		VBox tableCard = new VBox(14);
		tableCard.setStyle(state.cardStyle());
		VBox.setVgrow(tableCard, Priority.ALWAYS);

		HBox bar = new HBox(12);
		bar.setAlignment(Pos.CENTER_LEFT);

		TextField txtSearch = new TextField();
		txtSearch.setPromptText(state.t("\ud83d\udd0d  Search users, farms, or locations...", "\ud83d\udd0d  Maghanap ng user, sakahan, o lugar..."));
		txtSearch.setPrefWidth(360);
		txtSearch.setStyle(state.inputStyle());

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		// Admins can create Farmer or Admin accounts from this tab.
		Button btnAddAccount = new Button(state.t("+  Add Farm / User Account", "+  Magdagdag ng Account"));
		btnAddAccount.setStyle(state.primaryButtonStyle());
		btnAddAccount.setOnAction(e -> openAdminCreateAccountModal());

		bar.getChildren().addAll(txtSearch, spacer, btnAddAccount);

		FilteredList<User> filteredUsers = new FilteredList<>(state.users, u -> true);
		txtSearch.textProperty().addListener((obs, oldV, newV) -> {
			String q = newV == null ? "" : newV.trim().toLowerCase();
			filteredUsers.setPredicate(u -> q.isEmpty()
					|| u.getUsername().toLowerCase().contains(q)
					|| u.getUserId().toLowerCase().contains(q)
					|| u.getFarmName().toLowerCase().contains(q)
					|| u.getFarmAddress().toLowerCase().contains(q)
					|| u.getRole().toLowerCase().contains(q));
		});

		TableView<User> table = new TableView<>(filteredUsers);
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		VBox.setVgrow(table, Priority.ALWAYS);

		TableColumn<User, String> colId = new TableColumn<>(state.t("User ID", "User ID"));
		colId.setCellValueFactory(new PropertyValueFactory<>("userId"));

		TableColumn<User, String> colFarm = new TableColumn<>(state.t("User / Farm", "User / Sakahan"));
		colFarm.setCellValueFactory(cell -> new SimpleStringProperty(
				cell.getValue().getUsername() + " (" + cell.getValue().getFarmName() + ")"));

		TableColumn<User, String> colLocation = new TableColumn<>(state.t("Location", "Lokasyon"));
		colLocation.setCellValueFactory(new PropertyValueFactory<>("farmAddress"));

		TableColumn<User, String> colRole = new TableColumn<>(state.t("Role", "Role"));
		colRole.setCellValueFactory(new PropertyValueFactory<>("role"));

		TableColumn<User, String> colStatus = new TableColumn<>(state.t("Status", "Status"));
		colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
		colStatus.setCellFactory(col -> new TableCell<User, String>() {
			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setGraphic(null);
				} else {
					Label badge = new Label(item);
					String bg = "#fef3c7";
					String fg = "#92400e";
					if ("Verified".equalsIgnoreCase(item)) {
						bg = "#dcfce7";
						fg = "#166534";
					} else if ("Suspended".equalsIgnoreCase(item)) {
						bg = "#fde8e8";
						fg = "#9b1c1c";
					}
					badge.setStyle("-fx-background-color: " + bg + "; -fx-text-fill: " + fg + ";"
							+ "-fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 4 10 4 10; -fx-background-radius: 8;");
					setGraphic(badge);
				}
			}
		});

		TableColumn<User, Void> colAction = new TableColumn<>("");
		colAction.setCellFactory(col -> new TableCell<User, Void>() {
			private final Button btnView = new Button(state.t("View", "Tingnan"));
			{
				btnView.setStyle("-fx-background-color: " + state.PRIMARY_GREEN + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 5 16 5 16; -fx-cursor: hand;");
				btnView.setOnAction(e -> {
					User u = getTableView().getItems().get(getIndex());
					openUserDetailManagementModal(u);
				});
			}

			@Override
			protected void updateItem(Void item, boolean empty) {
				super.updateItem(item, empty);
				setGraphic(empty ? null : btnView);
			}
		});

		table.getColumns().addAll(colId, colFarm, colLocation, colRole, colStatus, colAction);
		tableCard.getChildren().addAll(bar, table);

		container.getChildren().addAll(topCards, tableCard);
		return container;
	}

	// User Management -> View Details Modal (Farm Info, Farmer Info, Pending Returns & Requests, Verify, Assign Admin, Suspend User)
	private void openUserDetailManagementModal(User targetUser) {
		Stage modal = new Stage();
		modal.initOwner(state.mainStage);
		modal.initModality(Modality.APPLICATION_MODAL);
		modal.initStyle(StageStyle.UTILITY);
		modal.setTitle(state.t("User & Farm Details", "Detalye ng User at Sakahan"));

		VBox card = new VBox(16);
		card.setPadding(new Insets(24));
		card.setStyle("-fx-background-color: " + state.bgCard() + ";");

		Label lblHeader = new Label(state.t("Account Details \u2014 ", "Detalye ng Account \u2014 ") + targetUser.getUserId());
		lblHeader.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + state.textPrimary() + ";");

		// 2-Column Info Grid: Farmer Info & Farm Info
		GridPane grid = new GridPane();
		grid.setHgap(18);
		grid.setVgap(10);
		ColumnConstraints cc1 = new ColumnConstraints();
		cc1.setPercentWidth(50);
		ColumnConstraints cc2 = new ColumnConstraints();
		cc2.setPercentWidth(50);
		grid.getColumnConstraints().addAll(cc1, cc2);

		VBox farmerInfoBox = new VBox(6);
		farmerInfoBox.setStyle("-fx-background-color: " + state.bgPage() + "; -fx-padding: 14; -fx-background-radius: 12;");
		Label lblFarmerSec = new Label(state.t("Farmer Information", "Impormasyon ng Farmer"));
		lblFarmerSec.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: " + state.PRIMARY_GREEN + ";");
		farmerInfoBox.getChildren().addAll(
				lblFarmerSec,
				state.makeDetailLine(state.t("Username:", "Username:"), targetUser.getUsername()),
				state.makeDetailLine(state.t("Role:", "Role:"), targetUser.getRole()),
				state.makeDetailLine("Status:", targetUser.getStatus()
						+ (targetUser.isSuspended() && !targetUser.getSuspensionReason().isEmpty()
							? " — " + targetUser.getSuspensionReason() : "")),
				state.makeDetailLine(state.t("Phone Number:", "Cellphone Number:"), targetUser.getPhoneNumber().isEmpty() ? "N/A" : targetUser.getPhoneNumber()),
				state.makeDetailLine(state.t("Email Address:", "Email Address:"), targetUser.getEmail().isEmpty() ? "N/A" : targetUser.getEmail()));

		VBox farmInfoBox = new VBox(6);
		farmInfoBox.setStyle("-fx-background-color: " + state.bgPage() + "; -fx-padding: 14; -fx-background-radius: 12;");
		Label lblFarmSec = new Label(state.t("Farm Information", "Impormasyon ng Sakahan"));
		lblFarmSec.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: " + state.PRIMARY_GREEN + ";");
		farmInfoBox.getChildren().addAll(
				lblFarmSec,
				state.makeDetailLine(state.t("Farm Name:", "Pangalan ng Sakahan:"), targetUser.getFarmName()),
				state.makeDetailLine(state.t("Farm Address:", "Address ng Sakahan:"), targetUser.getFarmAddress().isEmpty() ? "N/A" : targetUser.getFarmAddress()),
				state.makeDetailLine(state.t("RSBSA Number:", "RSBSA Number:"), targetUser.getRsbsaNumber()),
				state.makeDetailLine(state.t("Farm Area:", "Laki ng Sakahan:"), targetUser.getFarmArea()));

		grid.add(farmerInfoBox, 0, 0);
		grid.add(farmInfoBox, 1, 0);

		// Pending Seed Requests & Pending Seed Returns for this user
		VBox activityBox = new VBox(8);
		activityBox.setStyle("-fx-background-color: " + state.bgPage() + "; -fx-padding: 14; -fx-background-radius: 12;");
		Label lblActTitle = new Label(state.t("Pending Seed Requests & Pending Seed Returns", "Mga Pending na Seed Request at Seed Return"));
		lblActTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: " + state.textPrimary() + ";");

		List<SeedRequest> userPendingReqs = state.requestData.stream()
				.filter(r -> r.getFarmer().equalsIgnoreCase(targetUser.getUsername()) && "Pending".equalsIgnoreCase(r.getStatus()))
				.collect(Collectors.toList());
		List<SeedReturn> userPendingRets = state.returnData.stream()
				.filter(r -> r.getFarmer().equalsIgnoreCase(targetUser.getUsername()) && "Pending".equalsIgnoreCase(r.getStatus()))
				.collect(Collectors.toList());

		VBox actItems = new VBox(4);
		if (userPendingReqs.isEmpty() && userPendingRets.isEmpty()) {
			Label none = new Label(state.t("No pending seed requests or returns for this user.", "Walang pending na request o return ang user na ito."));
			none.setStyle("-fx-text-fill: " + state.textMuted() + "; -fx-font-size: 12px;");
			actItems.getChildren().add(none);
		} else {
			for (SeedRequest r : userPendingReqs) {
				Label l = new Label("\u2022 [Request " + r.getTransactionCode() + "] " + r.getSeedLabel() + " (" + r.getAmount() + " kg) \u2014 " + r.getPurpose());
				l.setStyle("-fx-text-fill: " + state.textPrimary() + "; -fx-font-size: 12px;");
				actItems.getChildren().add(l);
			}
			for (SeedReturn ret : userPendingRets) {
				Label l = new Label("\u2022 [Return " + ret.getReturnCode() + "] " + ret.getSeedLabel() + " (" + ret.getAmount() + " kg)");
				l.setStyle("-fx-text-fill: " + state.textPrimary() + "; -fx-font-size: 12px;");
				actItems.getChildren().add(l);
			}
		}
		activityBox.getChildren().addAll(lblActTitle, actItems);

		Label lblFeedback = new Label();
		lblFeedback.setStyle("-fx-text-fill: " + state.PRIMARY_GREEN + "; -fx-font-weight: bold; -fx-font-size: 12px;");

		// Action Controls: Verify User, Assign Admin, Suspend / Reactivate User
		HBox actionBar = new HBox(10);
		actionBar.setAlignment(Pos.CENTER_RIGHT);

		Button btnVerify = new Button(targetUser.isVerified() ? state.t("Verified \u2713", "Verified Na \u2713") : state.t("Verify User", "I-verify ang User"));
		btnVerify.setStyle(state.primaryButtonStyle());
		btnVerify.setDisable(targetUser.isVerified());
		btnVerify.setOnAction(e -> {
			targetUser.setStatus("Verified");
			state.saveUsers(state.userPath, state.users);
			lblFeedback.setText(state.t("User verified! Full farmer access granted.", "Verified na ang farmer! May full access na siya."));
			modal.close();
			showAdminShell();
		});

		boolean isAlreadyAdmin = "Admin".equalsIgnoreCase(targetUser.getRole());
		Button btnAssignAdmin = new Button(isAlreadyAdmin ? state.t("Revoke Admin (Set Farmer)", "Alisin Bilang Admin (Gawing Farmer)") : state.t("Assign Admin", "Gawing Admin"));
		btnAssignAdmin.setStyle(state.outlineButtonStyle());
		btnAssignAdmin.setDisable(targetUser.getUsername().equalsIgnoreCase(state.currentUser.getUsername()));
		btnAssignAdmin.setOnAction(e -> {
			if (isAlreadyAdmin) {
				targetUser.setRole("Farmer");
			} else {
				targetUser.setRole("Admin");
				targetUser.setStatus("Verified");
			}
			state.saveUsers(state.userPath, state.users);
			modal.close();
			showAdminShell();
		});

		boolean isSuspended = targetUser.isSuspended();
		Button btnSuspend = new Button(isSuspended ? "Unsuspend User" : "Suspend User");
		btnSuspend.setStyle(state.dangerOutlineButtonStyle());
		btnSuspend.setDisable(targetUser.getUsername().equalsIgnoreCase(state.currentUser.getUsername()));
		btnSuspend.setOnAction(e -> {
			if (isSuspended) {
				targetUser.setStatus("Verified");
				targetUser.setSuspensionReason("");
				state.saveUsers(state.userPath, state.users);
				modal.close();
				showAdminShell();
			} else {
				// Ask admin for a suspension reason
				Stage reasonStage = new Stage();
				reasonStage.initOwner(modal);
				reasonStage.initModality(Modality.APPLICATION_MODAL);
				reasonStage.initStyle(StageStyle.UTILITY);
				reasonStage.setTitle("Suspension Reason");

				VBox reasonBox = new VBox(12);
				reasonBox.setPadding(new Insets(20));
				reasonBox.setStyle("-fx-background-color: " + state.bgCard() + ";");

				Label lblR = new Label("Please provide a reason for suspending this account:");
				lblR.setStyle("-fx-font-size: 13px; -fx-text-fill: " + state.textPrimary() + ";");

				TextArea txtReason = new TextArea();
				txtReason.setPromptText("e.g. Repeated overdue seed returns, policy violation...");
				txtReason.setPrefRowCount(3);
				txtReason.setWrapText(true);
				txtReason.setStyle(state.inputStyle());

				Label reasonWarn = new Label();
				reasonWarn.setStyle("-fx-text-fill: #dc3545; -fx-font-size: 12px;");

				Button btnConfirm = new Button("Confirm Suspension");
				btnConfirm.setStyle(state.dangerOutlineButtonStyle());
				btnConfirm.setOnAction(ev -> {
					String reason = txtReason.getText().trim();
					if (reason.isEmpty()) {
						reasonWarn.setText("A reason is required.");
						return;
					}
					targetUser.setStatus("Suspended");
					targetUser.setSuspensionReason(reason);
					state.saveUsers(state.userPath, state.users);
					reasonStage.close();
					modal.close();
					showAdminShell();
				});

				Button btnCancel = new Button("Cancel");
				btnCancel.setStyle(state.outlineButtonStyle());
				btnCancel.setOnAction(ev -> reasonStage.close());

				HBox rBtns = new HBox(10, btnCancel, btnConfirm);
				rBtns.setAlignment(Pos.CENTER_RIGHT);
				reasonBox.getChildren().addAll(lblR, txtReason, reasonWarn, rBtns);
				reasonStage.setScene(new Scene(reasonBox, 420, 240));
				reasonStage.showAndWait();
			}
		});

		Button btnClose = new Button(state.t("Close", "Isara"));
		btnClose.setStyle(state.outlineButtonStyle());
		btnClose.setOnAction(e -> modal.close());

		actionBar.getChildren().addAll(btnClose, btnSuspend, btnAssignAdmin, btnVerify);

		card.getChildren().addAll(lblHeader, grid, activityBox, lblFeedback, actionBar);

		Scene scene = new Scene(card, 660, 490);
		modal.setScene(scene);
		modal.showAndWait();
	}



	// Modal for creating a new farm / user account.
	private void openAdminCreateAccountModal() {
		Stage modal = new Stage();
		modal.initOwner(state.mainStage);
		modal.initModality(Modality.APPLICATION_MODAL);
		modal.initStyle(StageStyle.UTILITY);
		modal.setTitle(state.t("Add Farm / User Account", "Magdagdag ng Account"));

		VBox card = new VBox(16);
		card.setPadding(new Insets(26));
		card.setStyle("-fx-background-color: " + state.bgCard() + ";");

		Label lblTitle = new Label(state.t("Add Farm Account", "Magdagdag ng Farm Account"));
		lblTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + state.textPrimary() + ";");

		GridPane grid = new GridPane();
		grid.setHgap(18);
		grid.setVgap(12);
		ColumnConstraints c1 = new ColumnConstraints();
		c1.setPercentWidth(50);
		ColumnConstraints c2 = new ColumnConstraints();
		c2.setPercentWidth(50);
		grid.getColumnConstraints().addAll(c1, c2);

		TextField txtUsername = new TextField();
		txtUsername.setPromptText("e.g. pedro_farmer");
		txtUsername.setStyle(state.inputStyle());

		PasswordField txtPassword = new PasswordField();
		txtPassword.setPromptText("\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022");
		txtPassword.setStyle(state.inputStyle());

		TextField txtPhone = new TextField();
		txtPhone.setPromptText("0917-000-0000");
		txtPhone.setStyle(state.inputStyle());

		TextField txtEmail = new TextField();
		txtEmail.setPromptText("farmer@laguna.ph");
		txtEmail.setStyle(state.inputStyle());

		TextField txtFarmName = new TextField();
		txtFarmName.setPromptText("e.g. Bay Laguna Farm");
		txtFarmName.setStyle(state.inputStyle());

		TextField txtFarmAddress = new TextField();
		txtFarmAddress.setPromptText("Barangay, Municipality, Laguna");
		txtFarmAddress.setStyle(state.inputStyle());

		ComboBox<String> roleCombo = new ComboBox<>();
		roleCombo.getItems().addAll("Farmer", "Admin");
		roleCombo.setValue("Farmer");
		roleCombo.setMaxWidth(Double.MAX_VALUE);
		roleCombo.setStyle(state.inputStyle());

		ComboBox<String> verifyCombo = new ComboBox<>();
		verifyCombo.getItems().addAll("Verified", "Pending");
		verifyCombo.setValue("Verified"); // Option 2: accounts created by admin are active immediately
		verifyCombo.setMaxWidth(Double.MAX_VALUE);
		verifyCombo.setStyle(state.inputStyle());

		grid.add(state.wrapField(state.t("Username *", "Username *"), txtUsername), 0, 0);
		grid.add(state.wrapField(state.t("Password *", "Password *"), txtPassword), 1, 0);
		grid.add(state.wrapField(state.t("Phone Number *", "Cellphone Number *"), txtPhone), 0, 1);
		grid.add(state.wrapField(state.t("Email Address *", "Email Address *"), txtEmail), 1, 1);
		grid.add(state.wrapField(state.t("Farm Name", "Pangalan ng Sakahan"), txtFarmName), 0, 2);
		grid.add(state.wrapField(state.t("Farm Address *", "Address ng Sakahan *"), txtFarmAddress), 1, 2);
		grid.add(state.wrapField(state.t("Account Role (Admin / Farmer) *", "Role ng Account (Admin / Farmer) *"), roleCombo), 0, 3);
		grid.add(state.wrapField(state.t("Initial Verification Status", "Status ng Verification"), verifyCombo), 1, 3);

		Label lblWarn = new Label();
		lblWarn.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold; -fx-font-size: 12px;");

		HBox buttons = new HBox(12);
		buttons.setAlignment(Pos.CENTER_RIGHT);

		Button btnCancel = new Button(state.t("Cancel", "Kanselahin"));
		btnCancel.setStyle(state.outlineButtonStyle());
		btnCancel.setOnAction(e -> modal.close());

		Button btnSave = new Button(state.t("Save Account", "I-save ang Account"));
		btnSave.setStyle(state.primaryButtonStyle());
		btnSave.setOnAction(e -> {
			String uname = txtUsername.getText().trim();
			String pass = txtPassword.getText();
			String phone = txtPhone.getText().trim();
			String email = txtEmail.getText().trim();
			String addr = txtFarmAddress.getText().trim();
			String role = roleCombo.getValue();
			String fName = txtFarmName.getText().trim().isEmpty() ? uname + " Farm" : txtFarmName.getText().trim();
			String initStatus = "Admin".equalsIgnoreCase(role) ? "Verified" : verifyCombo.getValue();

			if (uname.isEmpty() || pass.isEmpty() || phone.isEmpty() || email.isEmpty() || addr.isEmpty()) {
				lblWarn.setText(state.t("Error: All required fields (*) must be completed.", "Oops! Paki-fill up lahat ng required fields (*)."));
				return;
			}
			for (User u : state.users) {
				if (u.getUsername().equalsIgnoreCase(uname)) {
					lblWarn.setText(state.t("Error: Username already exists.", "May gumagamit na ng username na 'yan."));
					return;
				}
			}

			String prefix = "Admin".equalsIgnoreCase(role) ? "ADM-" : "FRM-";
			String uid = String.format("%s%03d", prefix, state.nextUserId++);
			state.users.add(new User(uid, uname, pass, role, phone, email, fName, addr, "RSBSA-" + state.nextUserId, "1.5 ha", initStatus));
			state.saveUsers(state.userPath, state.users);
			modal.close();
			showAdminShell();
		});

		buttons.getChildren().addAll(btnCancel, btnSave);
		card.getChildren().addAll(lblTitle, grid, lblWarn, buttons);

		modal.setScene(new Scene(card, 640, 470));
		modal.showAndWait();
	}



	// =========================================================================
	// ADMIN TRANSACTIONS VIEW (SLIDE 8 & SLIDE 9: REQUESTS & RETURNS)
	// =========================================================================
	private VBox buildAdminTransactionsView() {
		VBox container = new VBox(18);

		long pendingReqs = state.requestData.stream().filter(r -> "Pending".equalsIgnoreCase(r.getStatus())).count();
		long activeLoans = state.requestData.stream().filter(r -> "Approved".equalsIgnoreCase(r.getStatus())).count();
		long dueSoon = state.requestData.stream().filter(r -> "Approved".equalsIgnoreCase(r.getStatus())
				&& r.getDueDate() != null && !r.getDueDate().isAfter(LocalDate.now().plusDays(14))).count();

		HBox topCards = new HBox(16);
		topCards.getChildren().addAll(
				state.createStatCard(state.t("Pending Requests", "Mga Pending na Request"), String.valueOf(pendingReqs), state.t("Awaiting approval", "Pending pa ang approval"), "#d97706"),
				state.createStatCard(state.t("Active Loans", "Mga Active na Hiram"), String.valueOf(activeLoans), state.t("Disbursed seed lots", "Naipahiram na binhi"), state.PRIMARY_GREEN),
				state.createStatCard(state.t("Returns Due Soon / Late", "Malapit na / Late na Due Date"), String.valueOf(dueSoon), state.t("Within 14 days or overdue", "Sa loob ng 14 days o overdue na"), "#dc3545"));

		VBox tableCard = new VBox(14);
		tableCard.setStyle(state.cardStyle());
		VBox.setVgrow(tableCard, Priority.ALWAYS);

		HBox filterBar = new HBox(12);
		filterBar.setAlignment(Pos.CENTER_LEFT);

		TextField txtSearch = new TextField();
		txtSearch.setPromptText(state.t("\ud83d\udd0d  Search requests by farmer, farm, or seed...", "\ud83d\udd0d  Maghanap ng request..."));
		txtSearch.setPrefWidth(360);
		txtSearch.setStyle(state.inputStyle());

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		Button btnSort = new Button(state.t("\u21c5  Sort by Due Date", "\u21c5  Ayusin ayon sa Due Date"));
		btnSort.setStyle(state.primaryButtonStyle());
		btnSort.setOnAction(e -> FXCollections.sort(state.requestData, Comparator.comparing(SeedRequest::getDueDate)));

		filterBar.getChildren().addAll(txtSearch, spacer, btnSort);

		FilteredList<SeedRequest> filteredReqs = new FilteredList<>(state.requestData, r -> true);
		txtSearch.textProperty().addListener((obs, oldV, newV) -> {
			String q = newV == null ? "" : newV.trim().toLowerCase();
			filteredReqs.setPredicate(r -> q.isEmpty()
					|| r.getTransactionCode().toLowerCase().contains(q)
					|| r.getFarmer().toLowerCase().contains(q)
					|| r.getFarmName().toLowerCase().contains(q)
					|| r.getSeedLabel().toLowerCase().contains(q));
		});

		TableView<SeedRequest> reqTable = new TableView<>(filteredReqs);
		reqTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		reqTable.setPrefHeight(210);

		TableColumn<SeedRequest, String> colTxId = new TableColumn<>(state.t("Transaction ID", "Transaction ID"));
		colTxId.setCellValueFactory(new PropertyValueFactory<>("transactionCode"));

		TableColumn<SeedRequest, String> colFarm = new TableColumn<>(state.t("Farm / Farmer", "Sakahan / Farmer"));
		colFarm.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFarmName() + " (" + c.getValue().getFarmer() + ")"));

		TableColumn<SeedRequest, String> colSeed = new TableColumn<>(state.t("Seed Product", "Binhi"));
		colSeed.setCellValueFactory(new PropertyValueFactory<>("seedLabel"));

		TableColumn<SeedRequest, String> colQty = new TableColumn<>(state.t("Quantity", "Dami"));
		colQty.setCellValueFactory(new PropertyValueFactory<>("quantityLabel"));

		TableColumn<SeedRequest, String> colDue = new TableColumn<>(state.t("Due Date", "Due Date"));
		colDue.setCellValueFactory(c -> new SimpleStringProperty(
				c.getValue().getDueDate() == null ? "-" : c.getValue().getDueDate().format(state.DATE_FMT)));

		TableColumn<SeedRequest, String> colStatus = new TableColumn<>(state.t("Status", "Status"));
		colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
		colStatus.setCellFactory(col -> state.createStatusBadgeCell());

		TableColumn<SeedRequest, Void> colView = new TableColumn<>("");
		colView.setCellFactory(col -> new TableCell<SeedRequest, Void>() {
			private final Button btnView = new Button(state.t("View", "Tingnan"));
			{
				btnView.setStyle("-fx-background-color: " + state.PRIMARY_GREEN + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 5 16 5 16; -fx-cursor: hand;");
				btnView.setOnAction(e -> openTransactionDetailModal(getTableView().getItems().get(getIndex())));
			}

			@Override
			protected void updateItem(Void item, boolean empty) {
				super.updateItem(item, empty);
				setGraphic(empty ? null : btnView);
			}
		});

		reqTable.getColumns().addAll(colTxId, colFarm, colSeed, colQty, colDue, colStatus, colView);

		// Seed Returns Sub-section
		Label lblReturnsHeader = new Label(state.t("Seed Returns Inspection Queue", "Mga Ibinalik na Binhi (For Inspection)"));
		lblReturnsHeader.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: " + state.textPrimary() + ";");

		TableView<SeedReturn> retTable = new TableView<>(state.returnData);
		retTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		retTable.setPrefHeight(160);

		TableColumn<SeedReturn, String> colRetCode = new TableColumn<>(state.t("Return ID", "Return ID"));
		colRetCode.setCellValueFactory(new PropertyValueFactory<>("returnCode"));

		TableColumn<SeedReturn, String> colRetFarmer = new TableColumn<>(state.t("Farmer", "Farmer"));
		colRetFarmer.setCellValueFactory(new PropertyValueFactory<>("farmer"));

		TableColumn<SeedReturn, String> colRetSeed = new TableColumn<>(state.t("Seed Product", "Binhi"));
		colRetSeed.setCellValueFactory(new PropertyValueFactory<>("seedLabel"));

		TableColumn<SeedReturn, String> colRetQty = new TableColumn<>(state.t("Returned Qty", "Ibinalik na Dami"));
		colRetQty.setCellValueFactory(new PropertyValueFactory<>("quantityLabel"));

		TableColumn<SeedReturn, String> colRetStatus = new TableColumn<>(state.t("Status", "Status"));
		colRetStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
		colRetStatus.setCellFactory(col -> state.createStatusBadgeCell());

		TableColumn<SeedReturn, Void> colRetAction = new TableColumn<>(state.t("Inspection", "Inspeksyon"));
		colRetAction.setCellFactory(col -> new TableCell<SeedReturn, Void>() {
			private final Button btnAccept = new Button(state.t("Accept", "Tanggapin"));
			private final Button btnReject = new Button(state.t("Reject", "I-reject"));
			private final HBox box = new HBox(6, btnAccept, btnReject);
			{
				btnAccept.setStyle("-fx-background-color: " + state.PRIMARY_GREEN + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 4 10 4 10; -fx-cursor: hand;");
				btnReject.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-padding: 4 10 4 10; -fx-cursor: hand;");
				btnAccept.setOnAction(e -> {
					SeedReturn ret = getTableView().getItems().get(getIndex());
					if ("Pending".equalsIgnoreCase(ret.getStatus())) {
						Product prod = state.findProduct(ret.getLotId());
						if (prod != null) {
							prod.setAmount(state.round2(prod.getAmount() + ret.getAmount()));
							state.saveProducts(state.productPath, state.productData);
						}
						ret.setStatus("Accepted");
						// Clear overdue status on the linked seed request
						for (SeedRequest req : state.requestData) {
							if (req.getRequestId() == ret.getRequestId()) {
								req.setStatus("Returned");
								break;
							}
						}
						showAdminShell();
					}
				});
				btnReject.setOnAction(e -> {
					SeedReturn ret = getTableView().getItems().get(getIndex());
					if ("Pending".equalsIgnoreCase(ret.getStatus())) {
						ret.setStatus("Rejected");
						showAdminShell();
					}
				});
			}

			@Override
			protected void updateItem(Void item, boolean empty) {
				super.updateItem(item, empty);
				if (empty) {
					setGraphic(null);
				} else {
					SeedReturn r = getTableView().getItems().get(getIndex());
					setGraphic("Pending".equalsIgnoreCase(r.getStatus()) ? box : new Label("-"));
				}
			}
		});

		retTable.getColumns().addAll(colRetCode, colRetFarmer, colRetSeed, colRetQty, colRetStatus, colRetAction);

		tableCard.getChildren().addAll(filterBar, reqTable, lblReturnsHeader, retTable);
		container.getChildren().addAll(topCards, tableCard);
		return container;
	}

	// SLIDE 9: Transaction Details Modal (Reject / Approve)
	private void openTransactionDetailModal(SeedRequest req) {
		Stage modal = new Stage();
		modal.initOwner(state.mainStage);
		modal.initModality(Modality.APPLICATION_MODAL);
		modal.initStyle(StageStyle.UTILITY);
		modal.setTitle(state.t("Transaction Details", "Detalye ng Transaksyon"));

		VBox card = new VBox(16);
		card.setPadding(new Insets(26));
		card.setStyle("-fx-background-color: " + state.bgCard() + ";");

		Label lblTitle = new Label(state.t("Transaction Details", "Detalye ng Transaksyon"));
		lblTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + state.textPrimary() + ";");

		Label lblSubId = new Label("Transaction ID: " + req.getTransactionCode() + "   \u2022   Status: " + req.getStatus());
		lblSubId.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: " + state.textPrimary() + ";");

		GridPane grid = new GridPane();
		grid.setHgap(18);
		grid.setVgap(12);
		ColumnConstraints c1 = new ColumnConstraints();
		c1.setPercentWidth(50);
		ColumnConstraints c2 = new ColumnConstraints();
		c2.setPercentWidth(50);
		grid.getColumnConstraints().addAll(c1, c2);

		grid.add(state.makeReadOnlyBox(state.t("Farm", "Sakahan"), req.getFarmName()), 0, 0);
		grid.add(state.makeReadOnlyBox(state.t("Farm Rep.", "Kinatawan"), req.getFarmer()), 1, 0);
		grid.add(state.makeReadOnlyBox(state.t("Seed Product", "Binhi"), req.getSeedLabel()), 0, 1);
		grid.add(state.makeReadOnlyBox(state.t("Quantity", "Dami"), req.getQuantityLabel() + " (" + req.getPurpose() + ")"), 1, 1);
		grid.add(state.makeReadOnlyBox(state.t("Request Date", "Petsa ng Request"),
				req.getRequestDate() == null ? "-" : req.getRequestDate().format(state.DATE_FMT)), 0, 2);
		grid.add(state.makeReadOnlyBox(state.t("Seed Value", "Halaga ng Binhi"), "\u20b1" + String.format("%,.2f", req.getTotalValue())), 1, 2);
		grid.add(state.makeReadOnlyBox(state.t("Due Date", "Due Date"),
				req.getDueDate() == null ? "-" : req.getDueDate().format(state.DATE_FMT)), 0, 3);
		grid.add(state.makeReadOnlyBox(state.t("Total Repayment Left", "Natitirang Ibabalik"), req.getQuantityLabel()), 1, 3);

		Label lblWarn = new Label();
		lblWarn.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");

		HBox btnRow = new HBox(12);
		btnRow.setAlignment(Pos.CENTER_RIGHT);

		Button btnClose = new Button(state.t("Close", "Isara"));
		btnClose.setStyle(state.outlineButtonStyle());
		btnClose.setOnAction(e -> modal.close());

		Button btnReject = new Button(state.t("Reject", "I-reject"));
		btnReject.setStyle(state.dangerOutlineButtonStyle());
		btnReject.setDisable(!"Pending".equalsIgnoreCase(req.getStatus()));
		btnReject.setOnAction(e -> {
			req.setStatus("Rejected");
			modal.close();
			showAdminShell();
		});

		Button btnApprove = new Button(state.t("Approve", "I-approve"));
		btnApprove.setStyle(state.primaryButtonStyle());
		btnApprove.setDisable(!"Pending".equalsIgnoreCase(req.getStatus()));
		btnApprove.setOnAction(e -> {
			Product prod = state.findProduct(req.getLotId());
			if (prod == null || prod.getAmount() < req.getAmount()) {
				lblWarn.setText(state.t("Error: Insufficient seed stock in inventory.", "Kulang na ang stock nito sa inventory."));
				return;
			}
			prod.setAmount(state.round2(prod.getAmount() - req.getAmount()));
			req.setStatus("Approved");
			if (req.getDueDate() == null) {
				req.setDueDate(LocalDate.now().plusDays(90));
			}
			state.saveProducts(state.productPath, state.productData);
			modal.close();
			showAdminShell();
		});

		btnRow.getChildren().addAll(btnClose, btnReject, btnApprove);
		card.getChildren().addAll(lblTitle, lblSubId, grid, lblWarn, btnRow);

		modal.setScene(new Scene(card, 640, 470));
		modal.showAndWait();
	}



	// =========================================================================
	// ADMIN DONATIONS VIEW
	// =========================================================================
	private VBox buildAdminDonationsView() {
		VBox container = new VBox(18);
		double totalDonations = state.donationData.stream().mapToDouble(Donation::getAmount).sum();

		HBox topCards = new HBox(16);
		topCards.getChildren().addAll(
				state.createStatCard(state.t("Total Community Donations", "Kabuuang Donasyon"),
						"\u20b1" + String.format("%,.2f", totalDonations),
						state.donationData.size() + state.t(" recorded contributions", " naitalang donasyon"), state.PRIMARY_GREEN));

		VBox card = new VBox(14);
		card.setStyle(state.cardStyle());
		VBox.setVgrow(card, Priority.ALWAYS);

		TableView<Donation> table = new TableView<>(state.donationData);
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		VBox.setVgrow(table, Priority.ALWAYS);

		TableColumn<Donation, String> colDonor = new TableColumn<>(state.t("Donor Name", "Pangalan ng Donor"));
		colDonor.setCellValueFactory(new PropertyValueFactory<>("donorName"));

		TableColumn<Donation, String> colEmail = new TableColumn<>("Email");
		colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

		TableColumn<Donation, String> colAmount = new TableColumn<>(state.t("Amount", "Halaga"));
		colAmount.setCellValueFactory(c -> new SimpleStringProperty("\u20b1" + String.format("%,.2f", c.getValue().getAmount())));

		TableColumn<Donation, String> colMessage = new TableColumn<>(state.t("Message", "Mensahe"));
		colMessage.setCellValueFactory(new PropertyValueFactory<>("message"));

		table.getColumns().addAll(colDonor, colEmail, colAmount, colMessage);
		card.getChildren().add(table);

		container.getChildren().addAll(topCards, card);
		return container;
	}


	// =========================================================================
	// SLIDE 5: ADD INVENTORY SLOT MODAL + PRODUCT DETAIL / EDIT DIALOGS
	// =========================================================================
	private void openAddInventorySlotModal() {
		Stage modal = new Stage();
		modal.initOwner(state.mainStage);
		modal.initModality(Modality.APPLICATION_MODAL);
		modal.initStyle(StageStyle.UTILITY);
		modal.setTitle(state.t("Add Inventory Slot", "Magdagdag ng Seed Lot"));

		VBox card = new VBox(16);
		card.setPadding(new Insets(26));
		card.setStyle("-fx-background-color: " + state.bgCard() + ";");

		Label lblTitle = new Label(state.t("Add Inventory Slot", "Magdagdag ng Seed Lot"));
		lblTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + state.textPrimary() + ";");

		GridPane grid = new GridPane();
		grid.setHgap(18);
		grid.setVgap(12);
		ColumnConstraints c1 = new ColumnConstraints();
		c1.setPercentWidth(50);
		ColumnConstraints c2 = new ColumnConstraints();
		c2.setPercentWidth(50);
		grid.getColumnConstraints().addAll(c1, c2);

		TextField txtProduct = new TextField();
		txtProduct.setPromptText("e.g. Rice / Pineapple");
		txtProduct.setStyle(state.inputStyle());

		TextField txtLotId = new TextField();
		txtLotId.setPromptText("e.g. 4");
		txtLotId.setStyle(state.inputStyle());

		TextField txtQty = new TextField();
		txtQty.setPromptText("e.g. 200");
		txtQty.setStyle(state.inputStyle());

		ComboBox<String> unitBox = new ComboBox<>();
		unitBox.getItems().addAll("kg", "sacks", "grams");
		unitBox.setValue("kg");
		unitBox.setMaxWidth(Double.MAX_VALUE);
		unitBox.setStyle(state.inputStyle());

		TextField txtLocation = new TextField();
		txtLocation.setPromptText("e.g. Storage A");
		txtLocation.setStyle(state.inputStyle());

		TextField txtThreshold = new TextField("25");
		txtThreshold.setStyle(state.inputStyle());

		TextField txtVariety = new TextField();
		txtVariety.setPromptText("e.g. NSIC Rc222");
		txtVariety.setStyle(state.inputStyle());

		TextField txtExpiry = new TextField("2027-12-31");
		txtExpiry.setStyle(state.inputStyle());

		TextField txtPrice = new TextField("50.0");
		txtPrice.setStyle(state.inputStyle());

		TextField txtDesc = new TextField();
		txtDesc.setPromptText(state.t("Seed notes / origin", "Notes o pinagmulan ng binhi"));
		txtDesc.setStyle(state.inputStyle());

		grid.add(state.wrapField(state.t("Product *", "Pangalan ng Pananim *"), txtProduct), 0, 0);
		grid.add(state.wrapField(state.t("Lot ID *", "Lot ID *"), txtLotId), 1, 0);
		grid.add(state.wrapField(state.t("Quantity *", "Dami *"), txtQty), 0, 1);
		grid.add(state.wrapField(state.t("Unit", "Unit"), unitBox), 1, 1);
		grid.add(state.wrapField(state.t("Storage Location *", "Lokasyon sa Imbakan *"), txtLocation), 0, 2);
		grid.add(state.wrapField(state.t("Minimum Threshold", "Minimum Stock Alert"), txtThreshold), 1, 2);
		grid.add(state.wrapField(state.t("Variety *", "Variety *"), txtVariety), 0, 3);
		grid.add(state.wrapField(state.t("Expiry Date", "Expiry Date"), txtExpiry), 1, 3);
		grid.add(state.wrapField(state.t("Price per Unit (\u20b1) *", "Presyo Bawat Unit (\u20b1) *"), txtPrice), 0, 4);
		grid.add(state.wrapField(state.t("Description *", "Paglalarawan *"), txtDesc), 1, 4);

		Label lblWarn = new Label();
		lblWarn.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");

		HBox buttons = new HBox(12);
		buttons.setAlignment(Pos.CENTER_RIGHT);

		Button btnCancel = new Button(state.t("Cancel", "Kanselahin"));
		btnCancel.setStyle(state.outlineButtonStyle());
		btnCancel.setOnAction(e -> modal.close());

		Button btnSave = new Button(state.t("Save Slot", "I-save ang Seed Lot"));
		btnSave.setStyle(state.primaryButtonStyle());
		btnSave.setOnAction(e -> {
			try {
				String name = txtProduct.getText().trim();
				int lot = Integer.parseInt(txtLotId.getText().trim());
				double qty = Double.parseDouble(txtQty.getText().trim());
				String loc = txtLocation.getText().trim();
				double thresh = Double.parseDouble(txtThreshold.getText().trim());
				String var = txtVariety.getText().trim();
				String exp = txtExpiry.getText().trim();
				double price = Double.parseDouble(txtPrice.getText().trim());
				String desc = txtDesc.getText().trim().isEmpty() ? name + " (" + var + ")" : txtDesc.getText().trim();

				if (name.isEmpty() || loc.isEmpty() || var.isEmpty()) {
					lblWarn.setText(state.t("Error: Fill in all required fields (*).", "Oops! Paki-fill up lahat ng required fields (*)."));
					return;
				}
				state.productData.add(new Product(name, price, qty, lot, loc, var, desc, unitBox.getValue(), thresh, exp));
				modal.close();
				showAdminShell();
			} catch (NumberFormatException ex) {
				lblWarn.setText(state.t("Error: Lot ID, Quantity, Threshold, and Price must be numeric.", "Oops! Dapat number ang Lot ID, Dami, Threshold, at Presyo."));
			}
		});

		buttons.getChildren().addAll(btnCancel, btnSave);
		card.getChildren().addAll(lblTitle, grid, lblWarn, buttons);

		modal.setScene(new Scene(card, 640, 520));
		modal.showAndWait();
	}

	private void openProductDetailDialog(Product product) {
		Stage detailStage = new Stage();
		detailStage.initOwner(state.mainStage);
		detailStage.initModality(Modality.APPLICATION_MODAL);
		detailStage.setTitle(state.t("Product Details", "Detalye ng Binhi"));

		VBox vbox = new VBox(10);
		vbox.setPadding(new Insets(22));
		vbox.setStyle("-fx-background-color: " + state.bgCard() + ";");

		Label title = new Label(product.getLotCode() + " \u2014 " + product.getName());
		title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + state.textPrimary() + ";");

		Button btnEdit = new Button(state.t("Edit", "I-edit"));
		btnEdit.setStyle(state.primaryButtonStyle());
		btnEdit.setOnAction(e -> openEditProductDialog(product, detailStage));

		Button btnDelete = new Button(state.t("Delete", "I-delete"));
		btnDelete.setStyle(state.dangerOutlineButtonStyle());
		btnDelete.setOnAction(e -> {
			state.productData.remove(product);
			detailStage.close();
			showAdminShell();
		});

		Button btnClose = new Button(state.t("Close", "Isara"));
		btnClose.setStyle(state.outlineButtonStyle());
		btnClose.setOnAction(e -> detailStage.close());

		HBox btns = new HBox(10, btnClose, btnDelete, btnEdit);
		btns.setAlignment(Pos.CENTER_RIGHT);

		vbox.getChildren().addAll(
				title,
				state.makeDetailLine("Variety:", product.getVariety()),
				state.makeDetailLine("Location:", product.getLocation()),
				state.makeDetailLine("Amount:", product.getQuantityLabel()),
				state.makeDetailLine("Price:", "\u20b1" + product.getPrice()),
				state.makeDetailLine("Min Threshold:", product.getMinThreshold() + " " + product.getUnit()),
				state.makeDetailLine("Expiry:", product.getExpiryDate()),
				state.makeDetailLine("Description:", product.getDescription()),
				btns);

		detailStage.setScene(new Scene(vbox, 420, 340));
		detailStage.showAndWait();
	}

	private void openEditProductDialog(Product product, Stage detailStage) {
		Stage editStage = new Stage();
		editStage.initOwner(detailStage);
		editStage.initModality(Modality.APPLICATION_MODAL);
		editStage.setTitle(state.t("Edit Product", "I-edit ang Binhi"));

		TextField textName = new TextField(product.getName());
		textName.setStyle(state.inputStyle());
		TextField textPrice = new TextField(String.valueOf(product.getPrice()));
		textPrice.setStyle(state.inputStyle());
		TextField textAmount = new TextField(String.valueOf(product.getAmount()));
		textAmount.setStyle(state.inputStyle());
		TextField textLotID = new TextField(String.valueOf(product.getLotID()));
		textLotID.setStyle(state.inputStyle());
		TextField textLocation = new TextField(product.getLocation());
		textLocation.setStyle(state.inputStyle());
		TextField textVariety = new TextField(product.getVariety());
		textVariety.setStyle(state.inputStyle());
		TextField textDescription = new TextField(product.getDescription());
		textDescription.setStyle(state.inputStyle());

		Label labelWarn = new Label();
		labelWarn.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");

		Button btnSave = new Button(state.t("Save Changes", "I-save ang Pagbabago"));
		btnSave.setStyle(state.primaryButtonStyle());
		btnSave.setOnAction(e -> {
			try {
				product.setName(textName.getText().trim());
				product.setPrice(Double.parseDouble(textPrice.getText().trim()));
				product.setAmount(Double.parseDouble(textAmount.getText().trim()));
				product.setLotID(Integer.parseInt(textLotID.getText().trim()));
				product.setLocation(textLocation.getText().trim());
				product.setVariety(textVariety.getText().trim());
				product.setDescription(textDescription.getText().trim());

				int idx = state.productData.indexOf(product);
				if (idx >= 0) state.productData.set(idx, product);

				editStage.close();
				detailStage.close();
				showAdminShell();
			} catch (NumberFormatException ex) {
				labelWarn.setText("Error: Invalid numeric values.");
			}
		});

		VBox vbox = new VBox(8,
				state.wrapField("Name", textName),
				state.wrapField("Price", textPrice),
				state.wrapField("Amount", textAmount),
				state.wrapField("Lot ID", textLotID),
				state.wrapField("Location", textLocation),
				state.wrapField("Variety", textVariety),
				state.wrapField("Description", textDescription),
				labelWarn, btnSave);
		vbox.setPadding(new Insets(18));
		vbox.setStyle("-fx-background-color: " + state.bgCard() + ";");

		editStage.setScene(new Scene(vbox, 420, 520));
		editStage.showAndWait();
	}

}
