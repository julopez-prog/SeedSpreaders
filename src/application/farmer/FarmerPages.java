package application.farmer;

import application.Product;
import application.SeedRequest;
import application.SeedReturn;
import application.shared.AppState;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
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
import java.util.List;
import java.util.stream.Collectors;

public class FarmerPages {

	private final AppState state;
	private final Runnable onLogout;

	public FarmerPages(AppState state, Runnable onLogout) {
		this.state = state;
		this.onLogout = onLogout;
	}

	public void showFarmerShell() {
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

		Button navDashboard = state.createNavButton(this::showFarmerShell, state.t("Dashboard", "Dashboard"), "Dashboard", false);
		Button navRequests = state.createNavButton(this::showFarmerShell, state.t("Requests", "Humiram ng Binhi"), "Requests", false);
		Button navTransactions = state.createNavButton(this::showFarmerShell, state.t("Transactions", "Mga Transaksyon"), "Transactions", false);

		Region spacer = new Region();
		VBox.setVgrow(spacer, Priority.ALWAYS);

		String verLabel = state.currentUser.isVerified() ? state.t("VERIFIED", "VERIFIED NA") : state.t("PENDING VERIFICATION", "HINDI PA VERIFIED");
		Label lblRoleBadge = new Label(state.currentUser.getUsername().toUpperCase() + " \u2022 " + verLabel);
		lblRoleBadge.setStyle("-fx-text-fill: " + (state.currentUser.isVerified() ? state.LIME_ACCENT : "#f4c44e") + "; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 0 0 6 12;");

		Button btnLogout = new Button(state.t("Logout", "Mag-log Out"));
		btnLogout.setMaxWidth(Double.MAX_VALUE);
		btnLogout.setStyle("-fx-background-color: rgba(255,255,255,0.10); -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 10 16 10 16; -fx-cursor: hand;");
		btnLogout.setOnAction(e -> {
			state.saveUsers(state.userPath, state.users);
			state.currentUser = null;
			onLogout.run();
		});

		sidebar.getChildren().addAll(logoWrap, navDashboard, navRequests, navTransactions, spacer, lblRoleBadge, btnLogout);

		VBox centerWrapper = new VBox(18);
		centerWrapper.setPadding(new Insets(24, 32, 24, 32));
		centerWrapper.setStyle("-fx-background-color: " + state.bgPage() + ";");

		HBox topHeader = state.buildTopHeader(state.getFarmerPageTitle(), false, this::showFarmerShell);

		// Limited access banner for unverified farmers
		if (!state.currentUser.isVerified()) {
			HBox warnBanner = new HBox(10);
			warnBanner.setAlignment(Pos.CENTER_LEFT);
			warnBanner.setPadding(new Insets(12, 18, 12, 18));
			warnBanner.setStyle("-fx-background-color: #fff3cd; -fx-border-color: #ffe69c; -fx-border-radius: 12; -fx-background-radius: 12;");
			Label lblBanner = new Label(state.t("Limited Access Mode: Your farmer account is awaiting Admin verification. You can view your Overview and edit your Basic Information, but submitting new seed requests or returns is locked until verified.", "Limited Access Muna: Nag-aantay pa ng verification ng Admin ang account mo. Pwede mong tingnan ang Overview at i-edit ang Basic Info mo, pero naka-lock muna ang pag-request at pagbalik ng binhi hanggang ma-verify ka."));
			lblBanner.setWrapText(true);
			lblBanner.setStyle("-fx-text-fill: #664d03; -fx-font-weight: bold; -fx-font-size: 12px;");
			warnBanner.getChildren().add(lblBanner);
			centerWrapper.getChildren().add(warnBanner);
		}

		Node pageBody;
		switch (state.currentRoute) {
			case "Requests":
				pageBody = buildFarmerRequestsView();
				break;
			case "Transactions":
				pageBody = buildFarmerTransactionsView();
				break;
			default:
				pageBody = buildFarmerDashboardOverview();
				break;
		}
		VBox.setVgrow(pageBody, Priority.ALWAYS);

		centerWrapper.getChildren().addAll(topHeader, pageBody);

		shell.setLeft(sidebar);
		shell.setCenter(centerWrapper);

		state.applyScene(new Scene(shell, 1200, 780), "Seed Spreaders - Farmer (" + state.currentRoute + ")");
	}

	// =========================================================================
	// FARMER OVERVIEW DASHBOARD (SLIDE 10 + MEDIUM PRIORITY SPECS)
	// =========================================================================
	private ScrollPane buildFarmerDashboardOverview() {
		VBox container = new VBox(18);
		container.setPadding(new Insets(2, 4, 16, 2));

		long myPendingReqs = state.requestData.stream()
				.filter(r -> r.getFarmer().equalsIgnoreCase(state.currentUser.getUsername()) && "Pending".equalsIgnoreCase(r.getStatus()))
				.count();
		long myPendingRets = state.returnData.stream()
				.filter(r -> r.getFarmer().equalsIgnoreCase(state.currentUser.getUsername()) && "Pending".equalsIgnoreCase(r.getStatus()))
				.count();
		List<SeedRequest> activeLoans = state.requestData.stream()
				.filter(r -> r.getFarmer().equalsIgnoreCase(state.currentUser.getUsername()) && "Approved".equalsIgnoreCase(r.getStatus()))
				.collect(Collectors.toList());

		String nextDeadline = activeLoans.stream()
				.map(SeedRequest::getDueDate)
				.filter(d -> d != null)
				.min(LocalDate::compareTo)
				.map(d -> d.format(state.DATE_FMT))
				.orElse(state.t("No active deadlines", "Walang upcoming na due date"));

		// Top 3 Cards matching Slide 10 (Pending Seed Requests, Pending Seed Returns, Deadlines of Returns)
		HBox topCards = new HBox(16);
		topCards.getChildren().addAll(
				state.createStatCard(state.t("Pending Seed Requests", "Mga Pending na Request"),
						String.valueOf(myPendingReqs),
						state.t("Awaiting admin review", "Nire-review pa ng admin"), "#d97706"),
				state.createStatCard(state.t("Pending Seed Returns", "Mga Pending na Return"),
						String.valueOf(myPendingRets),
						state.t("Submitted for inspection", "Naipasa na para i-inspect"), state.PRIMARY_GREEN),
				state.createStatCard(state.t("Deadlines of Returns", "Mga Due Date ng Pagbabalik"),
						nextDeadline,
						activeLoans.size() + state.t(" active seed loan(s)", " active na hiram na binhi"), "#dc3545"));

		// Middle Row: Return Deadlines Schedule (Left) + Editable Basic Information Card (Right)
		HBox midRow = new HBox(16);
		VBox.setVgrow(midRow, Priority.ALWAYS);

		// Left Card: Return Deadlines & Quick Seed Return Action
		VBox deadlinesCard = new VBox(12);
		deadlinesCard.setStyle(state.cardStyle());
		HBox.setHgrow(deadlinesCard, Priority.ALWAYS);
		deadlinesCard.setPrefWidth(540);

		Label lblDeadlines = new Label(state.t("Upcoming & Overdue Return Deadlines", "Mga Upcoming at Overdue na Return Deadlines"));
		lblDeadlines.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + state.textPrimary() + ";");

		VBox deadlineList = new VBox(10);
		if (activeLoans.isEmpty()) {
			Label none = new Label(state.t("You have no active seed loans with pending return deadlines.", "Wala kang active na hiram na may pending deadline ngayon."));
			none.setStyle("-fx-text-fill: " + state.textMuted() + ";");
			deadlineList.getChildren().add(none);
		} else {
			for (SeedRequest loan : activeLoans) {
				boolean overdue = loan.isOverdue();
				String dueStr = loan.getDueDate() != null ? loan.getDueDate().format(state.DATE_FMT) : "-";
				deadlineList.getChildren().add(state.createAlertRow(
						overdue ? state.t("OVERDUE", "OVERDUE NA") : state.t("DUE ", "DUE ") + dueStr,
						loan.getTransactionCode() + " \u2022 " + loan.getSeedLabel() + " (" + loan.getAmount() + " kg)",
						overdue ? "#fde8e8" : "#dcfce7",
						overdue ? "#9b1c1c" : "#166534"));
			}
		}

		Button btnGoReturn = new Button(state.t("Submit a Seed Return  \u2192", "Magbalik ng Binhi  \u2192"));
		btnGoReturn.setStyle(state.primaryButtonStyle());
		btnGoReturn.setDisable(!state.currentUser.isVerified());
		btnGoReturn.setOnAction(e -> {
			state.currentRoute = "Transactions";
			showFarmerShell();
		});

		deadlinesCard.getChildren().addAll(lblDeadlines, deadlineList, btnGoReturn);

		// Right Card: Editable Basic Information
		VBox profileCard = new VBox(10);
		profileCard.setStyle(state.cardStyle());
		profileCard.setPrefWidth(420);

		Label lblProfileTitle = new Label("Basic Information");
		lblProfileTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + state.textPrimary() + ";");

		TextField txtPhone = new TextField(state.currentUser.getPhoneNumber());
		txtPhone.setStyle(state.inputStyle());

		TextField txtEmail = new TextField(state.currentUser.getEmail());
		txtEmail.setStyle(state.inputStyle());

		TextField txtFarmName = new TextField(state.currentUser.getFarmName());
		txtFarmName.setStyle(state.inputStyle());

		TextField txtFarmAddress = new TextField(state.currentUser.getFarmAddress());
		txtFarmAddress.setStyle(state.inputStyle());

		Label lblSaveMsg = new Label();
		lblSaveMsg.setStyle("-fx-text-fill: " + state.PRIMARY_GREEN + "; -fx-font-weight: bold; -fx-font-size: 12px;");

		Button btnSaveInfo = new Button(state.t("Save Profile Changes", "I-save ang Profile"));
		btnSaveInfo.setStyle(state.primaryButtonStyle());
		btnSaveInfo.setOnAction(e -> {
			state.currentUser.setPhoneNumber(txtPhone.getText().trim());
			state.currentUser.setEmail(txtEmail.getText().trim());
			state.currentUser.setFarmName(txtFarmName.getText().trim());
			state.currentUser.setFarmAddress(txtFarmAddress.getText().trim());
			state.saveUsers(state.userPath, state.users);
			lblSaveMsg.setText(state.t("Profile updated successfully!", "Na-save na ang updated info mo!"));
		});

		profileCard.getChildren().addAll(
				lblProfileTitle,
				state.wrapField(state.t("Phone Number", "Cellphone Number"), txtPhone),
				state.wrapField(state.t("Email Address", "Email Address"), txtEmail),
				state.wrapField(state.t("Farm Name", "Pangalan ng Sakahan"), txtFarmName),
				state.wrapField(state.t("Farm Address", "Address ng Sakahan"), txtFarmAddress),
				lblSaveMsg,
				btnSaveInfo);

		midRow.getChildren().addAll(deadlinesCard, profileCard);
		container.getChildren().addAll(topCards, midRow);

		ScrollPane scroll = new ScrollPane(container);
		scroll.setFitToWidth(true);
		return scroll;
	}

	// =========================================================================
	// FARMER REQUESTS VIEW (SLIDE 11: SELECTED SEEDS + REQUEST SUMMARY)
	// =========================================================================
	private VBox buildFarmerRequestsView() {
		VBox container = new VBox(18);

		HBox splitCards = new HBox(18);
		VBox.setVgrow(splitCards, Priority.ALWAYS);

		// Left Card: Available Seeds Table ("Selected Seeds")
		VBox leftCard = new VBox(14);
		leftCard.setStyle(state.cardStyle());
		HBox.setHgrow(leftCard, Priority.ALWAYS);
		leftCard.setPrefWidth(620);

		Label lblLeft = new Label(state.t("Selected Seeds (Browse Seed Bank)", "Pumili ng Binhi sa Seed Bank"));
		lblLeft.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + state.textPrimary() + ";");

		TableView<Product> seedTable = new TableView<>(state.productData);
		seedTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		VBox.setVgrow(seedTable, Priority.ALWAYS);

		TableColumn<Product, String> colLot = new TableColumn<>("Lot ID");
		colLot.setCellValueFactory(new PropertyValueFactory<>("lotCode"));

		TableColumn<Product, String> colName = new TableColumn<>(state.t("Seed", "Binhi"));
		colName.setCellValueFactory(new PropertyValueFactory<>("name"));

		TableColumn<Product, String> colVar = new TableColumn<>(state.t("Variety", "Variety"));
		colVar.setCellValueFactory(new PropertyValueFactory<>("variety"));

		TableColumn<Product, String> colAvail = new TableColumn<>(state.t("Available", "Available"));
		colAvail.setCellValueFactory(new PropertyValueFactory<>("quantityLabel"));

		TableColumn<Product, String> colLoc = new TableColumn<>(state.t("Location", "Lokasyon"));
		colLoc.setCellValueFactory(new PropertyValueFactory<>("location"));

		seedTable.getColumns().addAll(colLot, colName, colVar, colAvail, colLoc);

		// Right Card: Request Summary Form
		VBox rightCard = new VBox(14);
		rightCard.setStyle(state.cardStyle());
		rightCard.setPrefWidth(360);

		Label lblRight = new Label(state.t("Request Summary", "Buod ng Request"));
		lblRight.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + state.textPrimary() + ";");

		Label lblSelected = new Label(state.t("Selected Lot: None (Click a row on the left)", "Napiling Lot: Wala pa (Mag-click sa table sa kaliwa)"));
		lblSelected.setWrapText(true);
		lblSelected.setStyle("-fx-font-weight: bold; -fx-text-fill: " + state.PRIMARY_GREEN + "; -fx-font-size: 13px;");

		seedTable.getSelectionModel().selectedItemProperty().addListener((obs, oldP, newP) -> {
			if (newP != null) {
				lblSelected.setText(state.t("Selected: ", "Napili: ") + newP.getLotCode() + " \u2022 " + newP.getName() + " (" + newP.getVariety() + ")");
			}
		});

		TextField txtAmount = new TextField();
		txtAmount.setPromptText("e.g. 25.0");
		txtAmount.setStyle(state.inputStyle());

		TextField txtPurpose = new TextField();
		txtPurpose.setPromptText(state.t("e.g. Dry season planting", "hal. Pagtatanim ngayong tag-init"));
		txtPurpose.setStyle(state.inputStyle());

		Label lblWarn = new Label();
		lblWarn.setWrapText(true);
		lblWarn.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

		Button btnSubmit = new Button(state.t("\ud83c\udf31  Submit Seed Request", "\ud83c\udf31  I-submit ang Request"));
		btnSubmit.setMaxWidth(Double.MAX_VALUE);
		btnSubmit.setStyle(state.primaryButtonStyle());
		btnSubmit.setDisable(!state.currentUser.isVerified());

		btnSubmit.setOnAction(e -> {
			if (!state.currentUser.isVerified()) {
				lblWarn.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
				lblWarn.setText(state.t("Your account must be verified by an Admin before requesting seeds.", "Kailangan munang ma-verify ng Admin ang account mo bago makahiram ng binhi."));
				return;
			}
			Product chosen = seedTable.getSelectionModel().getSelectedItem();
			if (chosen == null) {
				lblWarn.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
				lblWarn.setText(state.t("Error: Select a seed lot from the table first.", "Pumili muna ng seed lot sa table sa kaliwa."));
				return;
			}
			try {
				double amt = Double.parseDouble(txtAmount.getText().trim());
				String purpose = txtPurpose.getText().trim();
				if (amt <= 0 || purpose.isEmpty()) {
					lblWarn.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
					lblWarn.setText(state.t("Error: Enter a positive quantity and planting purpose.", "Oops! Maglagay ng tamang dami at dahilan ng pagtatanim."));
					return;
				}
				if (amt > chosen.getAmount()) {
					lblWarn.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
					lblWarn.setText(state.t("Error: Requested amount exceeds available stock.", "Sobra sa available stock ang hinihingi mo."));
					return;
				}

				state.requestData.add(new SeedRequest(state.nextRequestId++, state.currentUser.getUsername(), state.currentUser.getFarmName(),
						chosen.getLotID(), chosen.getName(), chosen.getVariety(), state.round2(amt), chosen.getPrice(),
						purpose, "Pending", LocalDate.now(), LocalDate.now().plusDays(90)));

				lblWarn.setStyle("-fx-text-fill: " + state.PRIMARY_GREEN + "; -fx-font-weight: bold;");
				lblWarn.setText(state.t("Request submitted! Track status under Transactions.", "Na-submit na ang request! I-check ang status sa Transactions."));
				txtAmount.clear();
				txtPurpose.clear();
			} catch (NumberFormatException ex) {
				lblWarn.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
				lblWarn.setText(state.t("Error: Quantity must be a valid number.", "Oops! Dapat number ang dami."));
			}
		});

		rightCard.getChildren().addAll(lblRight, lblSelected,
				state.wrapField(state.t("Quantity (kg)", "Dami (kg)"), txtAmount),
				state.wrapField(state.t("Planting Purpose", "Dahilan ng Pagtatanim"), txtPurpose),
				lblWarn, btnSubmit);

		leftCard.getChildren().addAll(lblLeft, seedTable);
		splitCards.getChildren().addAll(leftCard, rightCard);

		container.getChildren().add(splitCards);
		return container;
	}

	// =========================================================================
	// FARMER TRANSACTIONS & RETURN SEEDS VIEW (SLIDE 12)
	// =========================================================================
	private VBox buildFarmerTransactionsView() {
		VBox container = new VBox(18);

		long activeLoans = state.requestData.stream()
				.filter(r -> r.getFarmer().equalsIgnoreCase(state.currentUser.getUsername()) && "Approved".equalsIgnoreCase(r.getStatus()))
				.count();
		long pendingReqs = state.requestData.stream()
				.filter(r -> r.getFarmer().equalsIgnoreCase(state.currentUser.getUsername()) && "Pending".equalsIgnoreCase(r.getStatus()))
				.count();
		long overdueCount = state.requestData.stream()
				.filter(r -> r.getFarmer().equalsIgnoreCase(state.currentUser.getUsername()) && r.isOverdue())
				.count();

		HBox topCards = new HBox(16);
		topCards.getChildren().addAll(
				state.createStatCard(state.t("Active Loans", "Mga Active na Hiram"), String.valueOf(activeLoans), state.t("Approved seed lots", "Na-approve na binhi"), state.PRIMARY_GREEN),
				state.createStatCard(state.t("Pending Requests", "Mga Pending na Request"), String.valueOf(pendingReqs), state.t("Awaiting approval", "Pending pa ang approval"), "#d97706"),
				state.createStatCard(state.t("Due / Overdue", "Due Date / Overdue"), String.valueOf(overdueCount), state.t("Requires seed return", "Kailangang ibalik pagka-ani"), "#dc3545"));

		VBox card = new VBox(14);
		card.setStyle(state.cardStyle());
		VBox.setVgrow(card, Priority.ALWAYS);

		HBox searchBar = new HBox(12);
		searchBar.setAlignment(Pos.CENTER_LEFT);

		TextField txtSearch = new TextField();
		txtSearch.setPromptText(state.t("\ud83d\udd0d  Search your transactions...", "\ud83d\udd0d  Maghanap sa mga transaksyon mo..."));
		txtSearch.setPrefWidth(360);
		txtSearch.setStyle(state.inputStyle());

		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		Button btnReturnSeed = new Button(state.t("\u21a9  Submit Seed Return", "\u21a9  Magbalik ng Binhi"));
		btnReturnSeed.setStyle(state.primaryButtonStyle());
		btnReturnSeed.setDisable(!state.currentUser.isVerified());
		btnReturnSeed.setOnAction(e -> openFarmerReturnModal());

		searchBar.getChildren().addAll(txtSearch, spacer, btnReturnSeed);

		FilteredList<SeedRequest> myReqs = new FilteredList<>(state.requestData,
				r -> r.getFarmer().equalsIgnoreCase(state.currentUser.getUsername()));

		txtSearch.textProperty().addListener((obs, oldV, newV) -> {
			String q = newV == null ? "" : newV.trim().toLowerCase();
			myReqs.setPredicate(r -> r.getFarmer().equalsIgnoreCase(state.currentUser.getUsername())
					&& (q.isEmpty() || r.getTransactionCode().toLowerCase().contains(q)
							|| r.getSeedLabel().toLowerCase().contains(q)
							|| r.getStatus().toLowerCase().contains(q)));
		});

		TableView<SeedRequest> reqTable = new TableView<>(myReqs);
		reqTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		reqTable.setPrefHeight(210);

		TableColumn<SeedRequest, String> colTx = new TableColumn<>(state.t("Transaction ID", "Transaction ID"));
		colTx.setCellValueFactory(new PropertyValueFactory<>("transactionCode"));

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

		reqTable.getColumns().addAll(colTx, colSeed, colQty, colDue, colStatus);

		// My Seed Returns Table
		Label lblMyRets = new Label(state.t("My Submitted Seed Returns", "Mga Ibinalik Kong Binhi"));
		lblMyRets.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + state.textPrimary() + ";");

		FilteredList<SeedReturn> myRets = new FilteredList<>(state.returnData,
				r -> r.getFarmer().equalsIgnoreCase(state.currentUser.getUsername()));
		TableView<SeedReturn> retTable = new TableView<>(myRets);
		retTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		retTable.setPrefHeight(150);

		TableColumn<SeedReturn, String> colRetId = new TableColumn<>("Return ID");
		colRetId.setCellValueFactory(new PropertyValueFactory<>("returnCode"));

		TableColumn<SeedReturn, String> colRetSeed = new TableColumn<>(state.t("Seed Product", "Binhi"));
		colRetSeed.setCellValueFactory(new PropertyValueFactory<>("seedLabel"));

		TableColumn<SeedReturn, String> colRetQty = new TableColumn<>(state.t("Returned Qty", "Ibinalik na Dami"));
		colRetQty.setCellValueFactory(new PropertyValueFactory<>("quantityLabel"));

		TableColumn<SeedReturn, String> colRetStatus = new TableColumn<>(state.t("Status", "Status"));
		colRetStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
		colRetStatus.setCellFactory(col -> state.createStatusBadgeCell());

		retTable.getColumns().addAll(colRetId, colRetSeed, colRetQty, colRetStatus);

		card.getChildren().addAll(searchBar, reqTable, lblMyRets, retTable);
		container.getChildren().addAll(topCards, card);
		return container;
	}

	private void openFarmerReturnModal() {
		Stage modal = new Stage();
		modal.initOwner(state.mainStage);
		modal.initModality(Modality.APPLICATION_MODAL);
		modal.initStyle(StageStyle.UTILITY);
		modal.setTitle(state.t("Return Harvested Seeds", "Magbalik ng Inaning Binhi"));

		VBox card = new VBox(14);
		card.setPadding(new Insets(24));
		card.setStyle("-fx-background-color: " + state.bgCard() + ";");

		Label lblTitle = new Label(state.t("Submit Seed Return", "Magbalik ng Binhi"));
		lblTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + state.textPrimary() + ";");

		ComboBox<SeedRequest> loanCombo = new ComboBox<>();
		for (SeedRequest r : state.requestData) {
			if (r.getFarmer().equalsIgnoreCase(state.currentUser.getUsername()) && "Approved".equalsIgnoreCase(r.getStatus())) {
				loanCombo.getItems().add(r);
			}
		}
		loanCombo.setPromptText(state.t("Select an approved seed loan...", "Pumili ng na-approve na hiram..."));
		loanCombo.setMaxWidth(Double.MAX_VALUE);
		loanCombo.setStyle(state.inputStyle());
		loanCombo.setCellFactory(lv -> new javafx.scene.control.ListCell<SeedRequest>() {
			@Override
			protected void updateItem(SeedRequest item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty || item == null ? "" : item.getTransactionCode() + " \u2022 " + item.getSeedLabel() + " (" + item.getAmount() + " kg)");
			}
		});
		loanCombo.setButtonCell(new javafx.scene.control.ListCell<SeedRequest>() {
			@Override
			protected void updateItem(SeedRequest item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty || item == null ? state.t("Select approved loan...", "Pumili ng na-approve na hiram...")
						: item.getTransactionCode() + " \u2022 " + item.getSeedLabel());
			}
		});

		TextField txtAmount = new TextField();
		txtAmount.setPromptText("e.g. 50.0");
		txtAmount.setStyle(state.inputStyle());

		Label lblWarn = new Label();
		lblWarn.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold; -fx-font-size: 12px;");

		HBox btns = new HBox(10);
		btns.setAlignment(Pos.CENTER_RIGHT);

		Button btnCancel = new Button(state.t("Cancel", "Kanselahin"));
		btnCancel.setStyle(state.outlineButtonStyle());
		btnCancel.setOnAction(e -> modal.close());

		Button btnSubmit = new Button(state.t("Submit Return", "I-submit ang Pagbabalik"));
		btnSubmit.setStyle(state.primaryButtonStyle());
		btnSubmit.setOnAction(e -> {
			SeedRequest chosen = loanCombo.getValue();
			if (chosen == null) {
				lblWarn.setText(state.t("Error: Select an approved loan first.", "Pumili muna ng na-approve na hiram."));
				return;
			}
			try {
				double amt = Double.parseDouble(txtAmount.getText().trim());
				if (amt <= 0 || amt > chosen.getAmount()) {
					lblWarn.setText(state.t("Error: Enter an amount between 0.1 and " + chosen.getAmount() + " kg.",
							"Error: Maglagay ng halagang hanggang " + chosen.getAmount() + " kg."));
					return;
				}
				state.returnData.add(new SeedReturn(state.nextReturnId++, chosen.getRequestId(), state.currentUser.getUsername(),
						chosen.getLotId(), chosen.getSeedLabel(), state.round2(amt), "Pending", LocalDate.now()));
				modal.close();
				showFarmerShell();
			} catch (NumberFormatException ex) {
				lblWarn.setText(state.t("Error: Enter a valid numeric quantity.", "Oops! Dapat number ang ilagay sa dami."));
			}
		});

		btns.getChildren().addAll(btnCancel, btnSubmit);
		card.getChildren().addAll(lblTitle,
				state.wrapField(state.t("Approved Seed Loan", "Na-approve na Hiram"), loanCombo),
				state.wrapField(state.t("Quantity to Return (kg)", "Daming Ibabalik (kg)"), txtAmount),
				lblWarn, btns);

		modal.setScene(new Scene(card, 460, 320));
		modal.showAndWait();
	}
}
