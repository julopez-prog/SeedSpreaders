package application.farmer; // declare package application.farmer

import application.Product; // import product class
import application.SeedRequest; // import seedrequest class
import application.SeedReturn; // import seedreturn class
import application.shared.AppState; // import appstate class
import javafx.beans.property.SimpleStringProperty; // import simplestringproperty class
import javafx.collections.transformation.FilteredList; // import filteredlist class
import javafx.geometry.Insets; // import insets class
import javafx.geometry.Pos; // import pos class
import javafx.scene.Node; // import node class
import javafx.scene.Scene; // import scene class
import javafx.scene.control.Button; // import button class
import javafx.scene.control.ComboBox; // import combobox class
import javafx.scene.control.Label; // import label class
import javafx.scene.control.ScrollPane; // import scrollpane class
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
import java.util.List; // import list class
import java.util.stream.Collectors; // import collectors class

public class FarmerPages { // define farmerpages class

	private final AppState state; // store state field
	private final Runnable onLogout; // store onlogout field

	public FarmerPages(AppState state, Runnable onLogout) { // initialize farmerpages instance
		this.state = state; // assign state value
		this.onLogout = onLogout; // assign onlogout value
	}

	public void showFarmerShell() { // handle showfarmershell operation
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

		Button navDashboard = state.createNavButton(this::showFarmerShell, state.t("Dashboard", "Dashboard"), "Dashboard", false); // execute statement
		Button navRequests = state.createNavButton(this::showFarmerShell, state.t("Requests", "Humiram ng Binhi"), "Requests", false); // execute statement
		Button navTransactions = state.createNavButton(this::showFarmerShell, state.t("Transactions", "Mga Transaksyon"), "Transactions", false); // execute statement

		Region spacer = new Region(); // execute statement
		VBox.setVgrow(spacer, Priority.ALWAYS); // allow vertical growth inside vbox

		String verLabel = state.currentUser.isVerified() ? state.t("VERIFIED", "VERIFIED NA") : state.t("PENDING VERIFICATION", "HINDI PA VERIFIED"); // execute statement
		Label lblRoleBadge = new Label(state.currentUser.getUsername().toUpperCase() + " \u2022 " + verLabel); // execute statement
		lblRoleBadge.setStyle("-fx-text-fill: " + (state.currentUser.isVerified() ? state.LIME_ACCENT : "#f4c44e") + "; -fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 0 0 6 12;"); // apply inline css styling

		Button btnLogout = new Button(state.t("Logout", "Mag-log Out")); // execute statement
		btnLogout.setMaxWidth(Double.MAX_VALUE); // configure node width constraint
		btnLogout.setStyle("-fx-background-color: rgba(255,255,255,0.10); -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 10 16 10 16; -fx-cursor: hand;"); // apply inline css styling
		btnLogout.setOnAction(e -> { // attach button click event handler
			state.saveUsers(state.userPath, state.users); // execute statement
			state.currentUser = null; // execute statement
			onLogout.run(); // execute statement
		}); // execute statement

		sidebar.getChildren().addAll(logoWrap, navDashboard, navRequests, navTransactions, spacer, lblRoleBadge, btnLogout); // add child nodes to container

		VBox centerWrapper = new VBox(18); // execute statement
		centerWrapper.setPadding(new Insets(24, 32, 24, 32)); // set inner padding spacing
		centerWrapper.setStyle("-fx-background-color: " + state.bgPage() + ";"); // apply inline css styling

		HBox topHeader = state.buildTopHeader(state.getFarmerPageTitle(), false, this::showFarmerShell); // execute statement

		// limited access banner for unverified farmers
		if (!state.currentUser.isVerified()) { // check condition before proceeding
			HBox warnBanner = new HBox(10); // execute statement
			warnBanner.setAlignment(Pos.CENTER_LEFT); // set container alignment
			warnBanner.setPadding(new Insets(12, 18, 12, 18)); // set inner padding spacing
			warnBanner.setStyle("-fx-background-color: #fff3cd; -fx-border-color: #ffe69c; -fx-border-radius: 12; -fx-background-radius: 12;"); // apply inline css styling
			Label lblBanner = new Label(state.t("Limited Access Mode: Your farmer account is awaiting Admin verification. You can view your Overview and edit your Basic Information, but submitting new seed requests or returns is locked until verified.", "Limited Access Muna: Nag-aantay pa ng verification ng Admin ang account mo. Pwede mong tingnan ang Overview at i-edit ang Basic Info mo, pero naka-lock muna ang pag-request at pagbalik ng binhi hanggang ma-verify ka.")); // execute statement
			lblBanner.setWrapText(true); // enable text wrapping
			lblBanner.setStyle("-fx-text-fill: #664d03; -fx-font-weight: bold; -fx-font-size: 12px;"); // apply inline css styling
			warnBanner.getChildren().add(lblBanner); // append child node to container
			centerWrapper.getChildren().add(warnBanner); // append child node to container
		}

		Node pageBody; // execute statement
		switch (state.currentRoute) { // open code block
			case "Requests": // continue statement
				pageBody = buildFarmerRequestsView(); // execute statement
				break; // exit current loop
			case "Transactions": // continue statement
				pageBody = buildFarmerTransactionsView(); // execute statement
				break; // exit current loop
			default: // continue statement
				pageBody = buildFarmerDashboardOverview(); // execute statement
				break; // exit current loop
		}
		VBox.setVgrow(pageBody, Priority.ALWAYS); // allow vertical growth inside vbox

		centerWrapper.getChildren().addAll(topHeader, pageBody); // add child nodes to container

		shell.setLeft(sidebar); // execute statement
		shell.setCenter(centerWrapper); // execute statement

		state.applyScene(new Scene(shell, 1200, 780), "Seed Spreaders - Farmer (" + state.currentRoute + ")"); // execute statement
	}

	// farmer overview dashboard (slide 10 + medium priority specs)
	private ScrollPane buildFarmerDashboardOverview() { // handle buildfarmerdashboardoverview operation
		VBox container = new VBox(18); // execute statement
		container.setPadding(new Insets(2, 4, 16, 2)); // set inner padding spacing

		long myPendingReqs = state.requestData.stream() // continue statement
				.filter(r -> r.getFarmer().equalsIgnoreCase(state.currentUser.getUsername()) && "Pending".equalsIgnoreCase(r.getStatus())) // continue statement
				.count(); // execute statement
		long myPendingRets = state.returnData.stream() // continue statement
				.filter(r -> r.getFarmer().equalsIgnoreCase(state.currentUser.getUsername()) && "Pending".equalsIgnoreCase(r.getStatus())) // continue statement
				.count(); // execute statement
		List<SeedRequest> activeLoans = state.requestData.stream() // continue statement
				.filter(r -> r.getFarmer().equalsIgnoreCase(state.currentUser.getUsername()) && "Approved".equalsIgnoreCase(r.getStatus())) // continue statement
				.collect(Collectors.toList()); // execute statement

		String nextDeadline = activeLoans.stream() // continue statement
				.map(SeedRequest::getDueDate) // continue statement
				.filter(d -> d != null) // continue statement
				.min(LocalDate::compareTo) // continue statement
				.map(d -> d.format(state.DATE_FMT)) // continue statement
				.orElse(state.t("No active deadlines", "Walang upcoming na due date")); // execute statement

		// top 3 cards matching slide 10 (pending seed requests, pending seed returns, deadlines of returns)
		HBox topCards = new HBox(16); // execute statement
		topCards.getChildren().addAll( // add child nodes to container
				state.createStatCard(state.t("Pending Seed Requests", "Mga Pending na Request"), // continue statement
						String.valueOf(myPendingReqs), // continue statement
						state.t("Awaiting admin review", "Nire-review pa ng admin"), "#d97706"), // continue statement
				state.createStatCard(state.t("Pending Seed Returns", "Mga Pending na Return"), // continue statement
						String.valueOf(myPendingRets), // continue statement
						state.t("Submitted for inspection", "Naipasa na para i-inspect"), state.PRIMARY_GREEN), // continue statement
				state.createStatCard(state.t("Deadlines of Returns", "Mga Due Date ng Pagbabalik"), // continue statement
						nextDeadline, // continue statement
						activeLoans.size() + state.t(" active seed loan(s)", " active na hiram na binhi"), "#dc3545")); // execute statement

		// middle row: return deadlines schedule (left) + editable basic information card (right)
		HBox midRow = new HBox(16); // execute statement
		VBox.setVgrow(midRow, Priority.ALWAYS); // allow vertical growth inside vbox

		// left card: return deadlines & quick seed return action
		VBox deadlinesCard = new VBox(12); // execute statement
		deadlinesCard.setStyle(state.cardStyle()); // apply inline css styling
		HBox.setHgrow(deadlinesCard, Priority.ALWAYS); // allow horizontal growth inside hbox
		deadlinesCard.setPrefWidth(540); // configure node width constraint

		Label lblDeadlines = new Label(state.t("Upcoming & Overdue Return Deadlines", "Mga Upcoming at Overdue na Return Deadlines")); // execute statement
		lblDeadlines.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + state.textPrimary() + ";"); // apply inline css styling

		VBox deadlineList = new VBox(10); // execute statement
		if (activeLoans.isEmpty()) { // check condition before proceeding
			Label none = new Label(state.t("You have no active seed loans with pending return deadlines.", "Wala kang active na hiram na may pending deadline ngayon.")); // execute statement
			none.setStyle("-fx-text-fill: " + state.textMuted() + ";"); // apply inline css styling
			deadlineList.getChildren().add(none); // append child node to container
		} else { // handle alternative branch
			for (SeedRequest loan : activeLoans) { // iterate through collection items
				boolean overdue = loan.isOverdue(); // execute statement
				String dueStr = loan.getDueDate() != null ? loan.getDueDate().format(state.DATE_FMT) : "-"; // execute statement
				deadlineList.getChildren().add(state.createAlertRow( // append child node to container
						overdue ? state.t("OVERDUE", "OVERDUE NA") : state.t("DUE ", "DUE ") + dueStr, // continue statement
						loan.getTransactionCode() + " \u2022 " + loan.getSeedLabel() + " (" + loan.getAmount() + " kg)", // continue statement
						overdue ? "#fde8e8" : "#dcfce7", // continue statement
						overdue ? "#9b1c1c" : "#166534")); // execute statement
			}
		}

		Button btnGoReturn = new Button(state.t("Submit a Seed Return  \u2192", "Magbalik ng Binhi  \u2192")); // execute statement
		btnGoReturn.setStyle(state.primaryButtonStyle()); // apply inline css styling
		btnGoReturn.setDisable(!state.currentUser.isVerified()); // execute statement
		btnGoReturn.setOnAction(e -> { // attach button click event handler
			state.currentRoute = "Transactions"; // execute statement
			showFarmerShell(); // execute statement
		}); // execute statement

		deadlinesCard.getChildren().addAll(lblDeadlines, deadlineList, btnGoReturn); // add child nodes to container

		// right card: editable basic information
		VBox profileCard = new VBox(10); // execute statement
		profileCard.setStyle(state.cardStyle()); // apply inline css styling
		profileCard.setPrefWidth(420); // configure node width constraint

		Label lblProfileTitle = new Label("Basic Information"); // execute statement
		lblProfileTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + state.textPrimary() + ";"); // apply inline css styling

		TextField txtPhone = new TextField(state.currentUser.getPhoneNumber()); // execute statement
		txtPhone.setStyle(state.inputStyle()); // apply inline css styling

		TextField txtEmail = new TextField(state.currentUser.getEmail()); // execute statement
		txtEmail.setStyle(state.inputStyle()); // apply inline css styling

		TextField txtFarmName = new TextField(state.currentUser.getFarmName()); // execute statement
		txtFarmName.setStyle(state.inputStyle()); // apply inline css styling

		TextField txtFarmAddress = new TextField(state.currentUser.getFarmAddress()); // execute statement
		txtFarmAddress.setStyle(state.inputStyle()); // apply inline css styling

		Label lblSaveMsg = new Label(); // execute statement
		lblSaveMsg.setStyle("-fx-text-fill: " + state.PRIMARY_GREEN + "; -fx-font-weight: bold; -fx-font-size: 12px;"); // apply inline css styling

		Button btnSaveInfo = new Button(state.t("Save Profile Changes", "I-save ang Profile")); // execute statement
		btnSaveInfo.setStyle(state.primaryButtonStyle()); // apply inline css styling
		btnSaveInfo.setOnAction(e -> { // attach button click event handler
			state.currentUser.setPhoneNumber(txtPhone.getText().trim()); // execute statement
			state.currentUser.setEmail(txtEmail.getText().trim()); // execute statement
			state.currentUser.setFarmName(txtFarmName.getText().trim()); // execute statement
			state.currentUser.setFarmAddress(txtFarmAddress.getText().trim()); // execute statement
			state.saveUsers(state.userPath, state.users); // execute statement
			lblSaveMsg.setText(state.t("Profile updated successfully!", "Na-save na ang updated info mo!")); // update displayed label text
		}); // execute statement

		profileCard.getChildren().addAll( // add child nodes to container
				lblProfileTitle, // continue statement
				state.wrapField(state.t("Phone Number", "Cellphone Number"), txtPhone), // continue statement
				state.wrapField(state.t("Email Address", "Email Address"), txtEmail), // continue statement
				state.wrapField(state.t("Farm Name", "Pangalan ng Sakahan"), txtFarmName), // continue statement
				state.wrapField(state.t("Farm Address", "Address ng Sakahan"), txtFarmAddress), // continue statement
				lblSaveMsg, // continue statement
				btnSaveInfo); // execute statement

		midRow.getChildren().addAll(deadlinesCard, profileCard); // add child nodes to container
		container.getChildren().addAll(topCards, midRow); // add child nodes to container

		ScrollPane scroll = new ScrollPane(container); // execute statement
		scroll.setFitToWidth(true); // execute statement
		return scroll; // return scroll
	}

	// farmer requests view (slide 11: selected seeds + request summary)
	private VBox buildFarmerRequestsView() { // handle buildfarmerrequestsview operation
		VBox container = new VBox(18); // execute statement

		HBox splitCards = new HBox(18); // execute statement
		VBox.setVgrow(splitCards, Priority.ALWAYS); // allow vertical growth inside vbox

		// left card: available seeds table ("selected seeds")
		VBox leftCard = new VBox(14); // execute statement
		leftCard.setStyle(state.cardStyle()); // apply inline css styling
		HBox.setHgrow(leftCard, Priority.ALWAYS); // allow horizontal growth inside hbox
		leftCard.setPrefWidth(620); // configure node width constraint

		Label lblLeft = new Label(state.t("Selected Seeds (Browse Seed Bank)", "Pumili ng Binhi sa Seed Bank")); // execute statement
		lblLeft.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + state.textPrimary() + ";"); // apply inline css styling

		TableView<Product> seedTable = new TableView<>(state.productData); // execute statement
		seedTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // execute statement
		VBox.setVgrow(seedTable, Priority.ALWAYS); // allow vertical growth inside vbox

		TableColumn<Product, String> colLot = new TableColumn<>("Lot ID"); // execute statement
		colLot.setCellValueFactory(new PropertyValueFactory<>("lotCode")); // execute statement

		TableColumn<Product, String> colName = new TableColumn<>(state.t("Seed", "Binhi")); // execute statement
		colName.setCellValueFactory(new PropertyValueFactory<>("name")); // execute statement

		TableColumn<Product, String> colVar = new TableColumn<>(state.t("Variety", "Variety")); // execute statement
		colVar.setCellValueFactory(new PropertyValueFactory<>("variety")); // execute statement

		TableColumn<Product, String> colAvail = new TableColumn<>(state.t("Available", "Available")); // execute statement
		colAvail.setCellValueFactory(new PropertyValueFactory<>("quantityLabel")); // execute statement

		TableColumn<Product, String> colLoc = new TableColumn<>(state.t("Location", "Lokasyon")); // execute statement
		colLoc.setCellValueFactory(new PropertyValueFactory<>("location")); // execute statement

		seedTable.getColumns().addAll(colLot, colName, colVar, colAvail, colLoc); // execute statement

		// right card: request summary form
		VBox rightCard = new VBox(14); // execute statement
		rightCard.setStyle(state.cardStyle()); // apply inline css styling
		rightCard.setPrefWidth(360); // configure node width constraint

		Label lblRight = new Label(state.t("Request Summary", "Buod ng Request")); // execute statement
		lblRight.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + state.textPrimary() + ";"); // apply inline css styling

		Label lblSelected = new Label(state.t("Selected Lot: None (Click a row on the left)", "Napiling Lot: Wala pa (Mag-click sa table sa kaliwa)")); // execute statement
		lblSelected.setWrapText(true); // enable text wrapping
		lblSelected.setStyle("-fx-font-weight: bold; -fx-text-fill: " + state.PRIMARY_GREEN + "; -fx-font-size: 13px;"); // apply inline css styling

		seedTable.getSelectionModel().selectedItemProperty().addListener((obs, oldP, newP) -> { // open code block
			if (newP != null) { // check condition before proceeding
				lblSelected.setText(state.t("Selected: ", "Napili: ") + newP.getLotCode() + " \u2022 " + newP.getName() + " (" + newP.getVariety() + ")"); // update displayed label text
			}
		}); // execute statement

		TextField txtAmount = new TextField(); // execute statement
		txtAmount.setPromptText("e.g. 25.0"); // set input placeholder text
		txtAmount.setStyle(state.inputStyle()); // apply inline css styling

		TextField txtPurpose = new TextField(); // execute statement
		txtPurpose.setPromptText(state.t("e.g. Dry season planting", "hal. Pagtatanim ngayong tag-init")); // set input placeholder text
		txtPurpose.setStyle(state.inputStyle()); // apply inline css styling

		Label lblWarn = new Label(); // execute statement
		lblWarn.setWrapText(true); // enable text wrapping
		lblWarn.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;"); // apply inline css styling

		Button btnSubmit = new Button(state.t("\ud83c\udf31  Submit Seed Request", "\ud83c\udf31  I-submit ang Request")); // execute statement
		btnSubmit.setMaxWidth(Double.MAX_VALUE); // configure node width constraint
		btnSubmit.setStyle(state.primaryButtonStyle()); // apply inline css styling
		btnSubmit.setDisable(!state.currentUser.isVerified()); // execute statement

		btnSubmit.setOnAction(e -> { // attach button click event handler
			if (!state.currentUser.isVerified()) { // check condition before proceeding
				lblWarn.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;"); // apply inline css styling
				lblWarn.setText(state.t("Your account must be verified by an Admin before requesting seeds.", "Kailangan munang ma-verify ng Admin ang account mo bago makahiram ng binhi.")); // update displayed label text
				return; // execute statement
			}
			Product chosen = seedTable.getSelectionModel().getSelectedItem(); // execute statement
			if (chosen == null) { // check condition before proceeding
				lblWarn.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;"); // apply inline css styling
				lblWarn.setText(state.t("Error: Select a seed lot from the table first.", "Pumili muna ng seed lot sa table sa kaliwa.")); // update displayed label text
				return; // execute statement
			}
			try { // start protected execution block
				double amt = Double.parseDouble(txtAmount.getText().trim()); // execute statement
				String purpose = txtPurpose.getText().trim(); // execute statement
				if (amt <= 0 || purpose.isEmpty()) { // check condition before proceeding
					lblWarn.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;"); // apply inline css styling
					lblWarn.setText(state.t("Error: Enter a positive quantity and planting purpose.", "Oops! Maglagay ng tamang dami at dahilan ng pagtatanim.")); // update displayed label text
					return; // execute statement
				}
				if (amt > chosen.getAmount()) { // check condition before proceeding
					lblWarn.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;"); // apply inline css styling
					lblWarn.setText(state.t("Error: Requested amount exceeds available stock.", "Sobra sa available stock ang hinihingi mo.")); // update displayed label text
					return; // execute statement
				}

				state.requestData.add(new SeedRequest(state.nextRequestId++, state.currentUser.getUsername(), state.currentUser.getFarmName(), // continue statement
						chosen.getLotID(), chosen.getName(), chosen.getVariety(), state.round2(amt), chosen.getPrice(), // continue statement
						purpose, "Pending", LocalDate.now(), LocalDate.now().plusDays(90))); // execute statement

				lblWarn.setStyle("-fx-text-fill: " + state.PRIMARY_GREEN + "; -fx-font-weight: bold;"); // apply inline css styling
				lblWarn.setText(state.t("Request submitted! Track status under Transactions.", "Na-submit na ang request! I-check ang status sa Transactions.")); // update displayed label text
				txtAmount.clear(); // execute statement
				txtPurpose.clear(); // execute statement
			} catch (NumberFormatException ex) { // handle caught exception
				lblWarn.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;"); // apply inline css styling
				lblWarn.setText(state.t("Error: Quantity must be a valid number.", "Oops! Dapat number ang dami.")); // update displayed label text
			}
		}); // execute statement

		rightCard.getChildren().addAll(lblRight, lblSelected, // add child nodes to container
				state.wrapField(state.t("Quantity (kg)", "Dami (kg)"), txtAmount), // continue statement
				state.wrapField(state.t("Planting Purpose", "Dahilan ng Pagtatanim"), txtPurpose), // continue statement
				lblWarn, btnSubmit); // execute statement

		leftCard.getChildren().addAll(lblLeft, seedTable); // add child nodes to container
		splitCards.getChildren().addAll(leftCard, rightCard); // add child nodes to container

		container.getChildren().add(splitCards); // append child node to container
		return container; // return container
	}

	// farmer transactions & return seeds view (slide 12)
	private VBox buildFarmerTransactionsView() { // handle buildfarmertransactionsview operation
		VBox container = new VBox(18); // execute statement

		long activeLoans = state.requestData.stream() // continue statement
				.filter(r -> r.getFarmer().equalsIgnoreCase(state.currentUser.getUsername()) && "Approved".equalsIgnoreCase(r.getStatus())) // continue statement
				.count(); // execute statement
		long pendingReqs = state.requestData.stream() // continue statement
				.filter(r -> r.getFarmer().equalsIgnoreCase(state.currentUser.getUsername()) && "Pending".equalsIgnoreCase(r.getStatus())) // continue statement
				.count(); // execute statement
		long overdueCount = state.requestData.stream() // continue statement
				.filter(r -> r.getFarmer().equalsIgnoreCase(state.currentUser.getUsername()) && r.isOverdue()) // continue statement
				.count(); // execute statement

		HBox topCards = new HBox(16); // execute statement
		topCards.getChildren().addAll( // add child nodes to container
				state.createStatCard(state.t("Active Loans", "Mga Active na Hiram"), String.valueOf(activeLoans), state.t("Approved seed lots", "Na-approve na binhi"), state.PRIMARY_GREEN), // continue statement
				state.createStatCard(state.t("Pending Requests", "Mga Pending na Request"), String.valueOf(pendingReqs), state.t("Awaiting approval", "Pending pa ang approval"), "#d97706"), // continue statement
				state.createStatCard(state.t("Due / Overdue", "Due Date / Overdue"), String.valueOf(overdueCount), state.t("Requires seed return", "Kailangang ibalik pagka-ani"), "#dc3545")); // execute statement

		VBox card = new VBox(14); // execute statement
		card.setStyle(state.cardStyle()); // apply inline css styling
		VBox.setVgrow(card, Priority.ALWAYS); // allow vertical growth inside vbox

		HBox searchBar = new HBox(12); // execute statement
		searchBar.setAlignment(Pos.CENTER_LEFT); // set container alignment

		TextField txtSearch = new TextField(); // execute statement
		txtSearch.setPromptText(state.t("\ud83d\udd0d  Search your transactions...", "\ud83d\udd0d  Maghanap sa mga transaksyon mo...")); // set input placeholder text
		txtSearch.setPrefWidth(360); // configure node width constraint
		txtSearch.setStyle(state.inputStyle()); // apply inline css styling

		Region spacer = new Region(); // execute statement
		HBox.setHgrow(spacer, Priority.ALWAYS); // allow horizontal growth inside hbox

		Button btnReturnSeed = new Button(state.t("\u21a9  Submit Seed Return", "\u21a9  Magbalik ng Binhi")); // execute statement
		btnReturnSeed.setStyle(state.primaryButtonStyle()); // apply inline css styling
		btnReturnSeed.setDisable(!state.currentUser.isVerified()); // execute statement
		btnReturnSeed.setOnAction(e -> openFarmerReturnModal()); // attach button click event handler

		searchBar.getChildren().addAll(txtSearch, spacer, btnReturnSeed); // add child nodes to container

		FilteredList<SeedRequest> myReqs = new FilteredList<>(state.requestData, // continue statement
				r -> r.getFarmer().equalsIgnoreCase(state.currentUser.getUsername())); // execute statement

		txtSearch.textProperty().addListener((obs, oldV, newV) -> { // open code block
			String q = newV == null ? "" : newV.trim().toLowerCase(); // execute statement
			myReqs.setPredicate(r -> r.getFarmer().equalsIgnoreCase(state.currentUser.getUsername()) // continue statement
					&& (q.isEmpty() || r.getTransactionCode().toLowerCase().contains(q) // continue statement
							|| r.getSeedLabel().toLowerCase().contains(q) // continue statement
							|| r.getStatus().toLowerCase().contains(q))); // execute statement
		}); // execute statement

		TableView<SeedRequest> reqTable = new TableView<>(myReqs); // execute statement
		reqTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // execute statement
		reqTable.setPrefHeight(210); // configure node height constraint

		TableColumn<SeedRequest, String> colTx = new TableColumn<>(state.t("Transaction ID", "Transaction ID")); // execute statement
		colTx.setCellValueFactory(new PropertyValueFactory<>("transactionCode")); // execute statement

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

		reqTable.getColumns().addAll(colTx, colSeed, colQty, colDue, colStatus); // execute statement

		// my seed returns table
		Label lblMyRets = new Label(state.t("My Submitted Seed Returns", "Mga Ibinalik Kong Binhi")); // execute statement
		lblMyRets.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: " + state.textPrimary() + ";"); // apply inline css styling

		FilteredList<SeedReturn> myRets = new FilteredList<>(state.returnData, // continue statement
				r -> r.getFarmer().equalsIgnoreCase(state.currentUser.getUsername())); // execute statement
		TableView<SeedReturn> retTable = new TableView<>(myRets); // execute statement
		retTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // execute statement
		retTable.setPrefHeight(150); // configure node height constraint

		TableColumn<SeedReturn, String> colRetId = new TableColumn<>("Return ID"); // execute statement
		colRetId.setCellValueFactory(new PropertyValueFactory<>("returnCode")); // execute statement

		TableColumn<SeedReturn, String> colRetSeed = new TableColumn<>(state.t("Seed Product", "Binhi")); // execute statement
		colRetSeed.setCellValueFactory(new PropertyValueFactory<>("seedLabel")); // execute statement

		TableColumn<SeedReturn, String> colRetQty = new TableColumn<>(state.t("Returned Qty", "Ibinalik na Dami")); // execute statement
		colRetQty.setCellValueFactory(new PropertyValueFactory<>("quantityLabel")); // execute statement

		TableColumn<SeedReturn, String> colRetStatus = new TableColumn<>(state.t("Status", "Status")); // execute statement
		colRetStatus.setCellValueFactory(new PropertyValueFactory<>("status")); // execute statement
		colRetStatus.setCellFactory(col -> state.createStatusBadgeCell()); // execute statement

		retTable.getColumns().addAll(colRetId, colRetSeed, colRetQty, colRetStatus); // execute statement

		card.getChildren().addAll(searchBar, reqTable, lblMyRets, retTable); // add child nodes to container
		container.getChildren().addAll(topCards, card); // add child nodes to container
		return container; // return container
	}

	private void openFarmerReturnModal() { // handle openfarmerreturnmodal operation
		Stage modal = new Stage(); // execute statement
		modal.initOwner(state.mainStage); // execute statement
		modal.initModality(Modality.APPLICATION_MODAL); // execute statement
		modal.initStyle(StageStyle.UTILITY); // execute statement
		modal.setTitle(state.t("Return Harvested Seeds", "Magbalik ng Inaning Binhi")); // execute statement

		VBox card = new VBox(14); // execute statement
		card.setPadding(new Insets(24)); // set inner padding spacing
		card.setStyle("-fx-background-color: " + state.bgCard() + ";"); // apply inline css styling

		Label lblTitle = new Label(state.t("Submit Seed Return", "Magbalik ng Binhi")); // execute statement
		lblTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + state.textPrimary() + ";"); // apply inline css styling

		ComboBox<SeedRequest> loanCombo = new ComboBox<>(); // execute statement
		for (SeedRequest r : state.requestData) { // iterate through collection items
			if (r.getFarmer().equalsIgnoreCase(state.currentUser.getUsername()) && "Approved".equalsIgnoreCase(r.getStatus())) { // check condition before proceeding
				loanCombo.getItems().add(r); // execute statement
			}
		}
		loanCombo.setPromptText(state.t("Select an approved seed loan...", "Pumili ng na-approve na hiram...")); // set input placeholder text
		loanCombo.setMaxWidth(Double.MAX_VALUE); // configure node width constraint
		loanCombo.setStyle(state.inputStyle()); // apply inline css styling
		loanCombo.setCellFactory(lv -> new javafx.scene.control.ListCell<SeedRequest>() { // open code block
			@Override // override parent method
			protected void updateItem(SeedRequest item, boolean empty) { // handle updateitem operation
				super.updateItem(item, empty); // execute statement
				setText(empty || item == null ? "" : item.getTransactionCode() + " \u2022 " + item.getSeedLabel() + " (" + item.getAmount() + " kg)"); // execute statement
			}
		}); // execute statement
		loanCombo.setButtonCell(new javafx.scene.control.ListCell<SeedRequest>() { // open code block
			@Override // override parent method
			protected void updateItem(SeedRequest item, boolean empty) { // handle updateitem operation
				super.updateItem(item, empty); // execute statement
				setText(empty || item == null ? state.t("Select approved loan...", "Pumili ng na-approve na hiram...") // continue statement
						: item.getTransactionCode() + " \u2022 " + item.getSeedLabel()); // execute statement
			}
		}); // execute statement

		TextField txtAmount = new TextField(); // execute statement
		txtAmount.setPromptText("e.g. 50.0"); // set input placeholder text
		txtAmount.setStyle(state.inputStyle()); // apply inline css styling

		Label lblWarn = new Label(); // execute statement
		lblWarn.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold; -fx-font-size: 12px;"); // apply inline css styling

		HBox btns = new HBox(10); // execute statement
		btns.setAlignment(Pos.CENTER_RIGHT); // set container alignment

		Button btnCancel = new Button(state.t("Cancel", "Kanselahin")); // execute statement
		btnCancel.setStyle(state.outlineButtonStyle()); // apply inline css styling
		btnCancel.setOnAction(e -> modal.close()); // attach button click event handler

		Button btnSubmit = new Button(state.t("Submit Return", "I-submit ang Pagbabalik")); // execute statement
		btnSubmit.setStyle(state.primaryButtonStyle()); // apply inline css styling
		btnSubmit.setOnAction(e -> { // attach button click event handler
			SeedRequest chosen = loanCombo.getValue(); // execute statement
			if (chosen == null) { // check condition before proceeding
				lblWarn.setText(state.t("Error: Select an approved loan first.", "Pumili muna ng na-approve na hiram.")); // update displayed label text
				return; // execute statement
			}
			try { // start protected execution block
				double amt = Double.parseDouble(txtAmount.getText().trim()); // execute statement
				if (amt <= 0 || amt > chosen.getAmount()) { // check condition before proceeding
					lblWarn.setText(state.t("Error: Enter an amount between 0.1 and " + chosen.getAmount() + " kg.", // update displayed label text
							"Error: Maglagay ng halagang hanggang " + chosen.getAmount() + " kg.")); // execute statement
					return; // execute statement
				}
				state.returnData.add(new SeedReturn(state.nextReturnId++, chosen.getRequestId(), state.currentUser.getUsername(), // continue statement
						chosen.getLotId(), chosen.getSeedLabel(), state.round2(amt), "Pending", LocalDate.now())); // execute statement
				modal.close(); // execute statement
				showFarmerShell(); // execute statement
			} catch (NumberFormatException ex) { // handle caught exception
				lblWarn.setText(state.t("Error: Enter a valid numeric quantity.", "Oops! Dapat number ang ilagay sa dami.")); // update displayed label text
			}
		}); // execute statement

		btns.getChildren().addAll(btnCancel, btnSubmit); // add child nodes to container
		card.getChildren().addAll(lblTitle, // add child nodes to container
				state.wrapField(state.t("Approved Seed Loan", "Na-approve na Hiram"), loanCombo), // continue statement
				state.wrapField(state.t("Quantity to Return (kg)", "Daming Ibabalik (kg)"), txtAmount), // continue statement
				lblWarn, btns); // execute statement

		modal.setScene(new Scene(card, 460, 320)); // execute statement
		modal.showAndWait(); // execute statement
	}
}
