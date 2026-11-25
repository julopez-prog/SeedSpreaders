package application.farmer; // farmer dashboard, seed requests, and harvest returns

import application.Product; // seed inventory model
import application.SeedRequest; // seed request model
import application.SeedReturn; // seed return model
import application.User; // user account model
import application.shared.AppState; // shared state and ui helpers
import javafx.beans.property.SimpleStringProperty; // computed string property
import javafx.collections.FXCollections; // observable list utility
import javafx.collections.ObservableList; // reactive list
import javafx.collections.transformation.FilteredList; // filtered view of shared lists
import javafx.geometry.Insets; // layout padding
import javafx.geometry.Pos; // alignment helper
import javafx.scene.Node; // scene graph node
import javafx.scene.Scene; // scene container
import javafx.scene.control.Button; // button control
import javafx.scene.control.Label; // text label
import javafx.scene.control.ScrollPane; // scrollable viewport
import javafx.scene.control.TableColumn; // table column
import javafx.scene.control.TableView; // table view
import javafx.scene.control.TextField; // text input
import javafx.scene.control.cell.PropertyValueFactory; // property binding
import javafx.scene.layout.BorderPane; // shell layout
import javafx.scene.layout.ColumnConstraints; // grid column sizing
import javafx.scene.layout.GridPane; // form grid layout
import javafx.scene.layout.HBox; // horizontal box
import javafx.scene.layout.Priority; // flex grow priority
import javafx.scene.layout.Region; // flexible spacer
import javafx.scene.layout.VBox; // vertical box
import javafx.stage.Stage; // primary window

import java.time.LocalDate; // current date tracking
import java.util.List; // list collection
import java.util.stream.Collectors; // stream collection helper

// handles all farmer dashboard views: overview, request seeds, transactions, and return seeds
public class FarmerPages { // declare farmerpages class
	private final Stage mainStage; // primary stage reference
	private final AppState state; // shared application state
	private final Runnable onLogout; // logout callback

	private String activeTab = "Overview"; // currently selected sidebar route
	private BorderPane shellRoot; // root layout container
	private Label lblPageTitle; // top header page title

	// constructor for farmer screens controller
	public FarmerPages(Stage stage, AppState state, Runnable onLogout) { // define farmerpages method
		this.mainStage = stage; // store primary stage
		this.state = state; // store shared state
		this.onLogout = onLogout; // store logout callback
	}

	// display the farmer shell with persistent sidebar, top header, and active tab
	public void showFarmerShell() { // define showfarmershell method
		shellRoot = new BorderPane(); // initialize root border pane
		shellRoot.setStyle("-fx-background-color: " + AppState.BG_PAGE + ";"); // off-white page background

		shellRoot.setLeft(buildSidebar()); // attach left forest-green sidebar
		shellRoot.setTop(buildTopHeader()); // attach top header bar
		switchTab(activeTab); // load active tab view

		Scene scene = new Scene(shellRoot, 1180, 745); // create farmer scene
		mainStage.setScene(scene); // display farmer scene
		mainStage.setTitle("Seed Spreaders - Farmer Portal"); // set window title
	}

	// build the left navigation sidebar with brand logo, route buttons, and logout
	private VBox buildSidebar() { // define buildsidebar helper
		VBox sidebar = new VBox(8); // vertical sidebar container
		sidebar.setPrefWidth(240); // fixed sidebar width
		sidebar.setMinWidth(240); // prevent shrinking
		sidebar.setPadding(new Insets(24, 16, 22, 16)); // inner padding
		sidebar.setStyle("-fx-background-color: " + AppState.FOREST_DARK + ";"); // dark forest background

		Node logo = state.buildLogo(0.92, true); // build white logo lockup for dark sidebar
		VBox logoBox = new VBox(logo); // wrapper for spacing
		logoBox.setPadding(new Insets(0, 6, 24, 6)); // space below logo

		Button btnOverview = createNavButton("Overview", "Overview"); // farmer overview tab
		Button btnRequest = createNavButton("Request Seeds", "Request"); // seed loan request tab
		Button btnTransactions = createNavButton("My Transactions", "Transactions"); // request & return history tab
		Button btnReturn = createNavButton("Return Seeds", "Return"); // harvest seed return tab

		Region spacer = new Region(); // push profile summary and logout to bottom
		VBox.setVgrow(spacer, Priority.ALWAYS); // expand spacer vertically

		Label lblRole = new Label("REGISTERED FARMER"); // role badge text
		lblRole.setStyle("-fx-text-fill: #8ec9a4; -fx-font-size: 10px; -fx-font-weight: bold;"); // sage green text

		Label lblName = new Label(state.currentUser != null ? state.currentUser.getFullName() : "Farmer"); // logged-in farmer name
		lblName.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 13px; -fx-font-weight: bold;"); // white bold text

		VBox userCard = new VBox(3, lblRole, lblName); // session info box
		userCard.setPadding(new Insets(12)); // inner padding
		userCard.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 10;"); // subtle dark card

		Button btnLogout = new Button("Log Out"); // logout button
		btnLogout.setMaxWidth(Double.MAX_VALUE); // full width
		btnLogout.setStyle("-fx-background-color: rgba(255,255,255,0.12); -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 12px; -fx-background-radius: 8; -fx-padding: 10 14 10 14; -fx-cursor: hand;"); // light button on dark green
		btnLogout.setOnAction(e -> { // attach click listener
			state.saveUsers(); // persist any profile edits
			state.saveRequests(); // persist any new requests
			state.saveReturns(); // persist any new returns
			state.currentUser = null; // clear current session
			onLogout.run(); // return to landing page
		});

		sidebar.getChildren().addAll(logoBox, btnOverview, btnRequest, btnTransactions, btnReturn, spacer, userCard, btnLogout); // assemble sidebar
		return sidebar; // return sidebar node
	}

	// build a styled sidebar navigation button
	private Button createNavButton(String label, String routeKey) { // define createnavbutton helper
		Button btn = new Button(label); // create nav button
		btn.setMaxWidth(Double.MAX_VALUE); // full sidebar width
		btn.setAlignment(Pos.CENTER_LEFT); // left-align label
		boolean selected = activeTab.equals(routeKey); // check active state
		if (selected) { // active style
			btn.setStyle("-fx-background-color: #1d6b50; -fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 9; -fx-padding: 11 16 11 16; -fx-cursor: hand;"); // highlighted green pill
		} else { // inactive style
			btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #cfe0d7; -fx-font-weight: bold; -fx-font-size: 13px; -fx-background-radius: 9; -fx-padding: 11 16 11 16; -fx-cursor: hand;"); // muted sage text
		}
		btn.setOnAction(e -> switchTab(routeKey)); // navigate on click
		return btn; // return styled button
	}

	// build top header bar with page title and per-tab help button (no bell icon)
	private HBox buildTopHeader() { // define buildtopheader helper
		lblPageTitle = new Label("Farmer Overview"); // dynamic page heading
		lblPageTitle.setStyle("-fx-font-size: 21px; -fx-font-weight: bold; -fx-text-fill: " + AppState.TEXT_DARK + ";"); // dark heading text

		Region spacer = new Region(); // push help button right
		HBox.setHgrow(spacer, Priority.ALWAYS); // expand spacer

		Button btnHelp = new Button("? Help"); // per-tab manual help button
		btnHelp.setStyle(state.outlineButtonStyle()); // outline style
		btnHelp.setOnAction(e -> openTabHelp()); // show feature overview popup

		HBox header = new HBox(12, lblPageTitle, spacer, btnHelp); // header bar container
		header.setAlignment(Pos.CENTER_LEFT); // center vertically
		header.setPadding(new Insets(18, 28, 18, 28)); // outer padding
		header.setStyle("-fx-background-color: #ffffff; -fx-border-color: transparent transparent #e3ebe6 transparent;"); // white header bar
		return header; // return header bar
	}

	// switch active center view and update sidebar highlight
	private void switchTab(String routeKey) { // define switchtab helper
		this.activeTab = routeKey; // update active route
		shellRoot.setLeft(buildSidebar()); // refresh sidebar highlight

		Node content; // target tab view
		switch (routeKey) { // continue statement
			case "Request": // continue statement
				lblPageTitle.setText("Request Seeds"); // update header title
				content = createFarmerRequestPane(); // build request view
				break; // exit switch block
			case "Transactions": // continue statement
				lblPageTitle.setText("My Transactions"); // update header title
				content = createFarmerTransactionsPane(); // build transactions view
				break; // exit switch block
			case "Return": // continue statement
				lblPageTitle.setText("Return Seeds"); // update header title
				content = createFarmerReturnPane(); // build return view
				break; // exit switch block
			default: // default route case
				lblPageTitle.setText("Farmer Overview"); // update header title
				content = createFarmerOverviewPane(); // build overview dashboard
				break; // exit switch block
		}

		ScrollPane scroller = new ScrollPane(content); // wrap view in scroll pane
		scroller.setFitToWidth(true); // match viewport width
		scroller.setStyle("-fx-background: " + AppState.BG_PAGE + "; -fx-background-color: " + AppState.BG_PAGE + ";"); // match page background
		shellRoot.setCenter(scroller); // place in center
	}

	// open per-tab feature overview dialog when Help is clicked
	private void openTabHelp() { // define opentabhelp helper
		switch (activeTab) { // continue statement
			case "Request": // continue statement
				state.showTabHelpDialog("Request Seeds", // pass value
						"\u2022 Select an available seed lot from the catalog table.\n" // continue statement
								+ "\u2022 Enter the weight in kilograms and your planting purpose.\n" // continue statement
								+ "\u2022 Total active seed loans per farm cannot exceed \u20b110,000.00."); // request help text
				break; // exit switch block
			case "Transactions": // continue statement
				state.showTabHelpDialog("My Transactions", // pass value
						"\u2022 Track the approval status and return due dates of your seed requests.\n" // continue statement
								+ "\u2022 View the inspection status of all harvest seed returns you submitted."); // transactions help text
				break; // exit switch block
			case "Return": // continue statement
				state.showTabHelpDialog("Return Seeds", // pass value
						"\u2022 Select an approved seed loan from the list to submit your post-harvest repayment.\n" // continue statement
								+ "\u2022 Enter the weight of viable seeds returned (including agreed seed interest).\n" // continue statement
								+ "\u2022 Once accepted by an admin, any overdue status on that loan is cleared."); // return help text
				break; // exit switch block
			default: // default route case
				state.showTabHelpDialog("Farmer Overview", // pass value
						"\u2022 Check your Pending Seed Requests, Pending Seed Returns, and Deadlines of Returns at a glance.\n" // continue statement
								+ "\u2022 Update your Basic Information (name, contact details, farm name, and farm address) below."); // overview help text
				break; // exit switch block
		}
	}

	// =========================================================================
	// 1. OVERVIEW TAB (Pending Requests, Pending Returns, Deadlines of Returns, Basic Information)
	// =========================================================================
	private VBox createFarmerOverviewPane() { // define createfarmeroverviewpane helper
		String username = state.currentUser.getUsername(); // logged-in username

		long pendingReqCount = state.requestData.stream() // continue statement
				.filter(r -> r.getFarmer().equalsIgnoreCase(username) && "Pending".equalsIgnoreCase(r.getStatus())) // continue statement
				.count(); // count farmer's pending requests

		long pendingRetCount = state.returnData.stream() // continue statement
				.filter(r -> r.getFarmer().equalsIgnoreCase(username) && "Pending".equalsIgnoreCase(r.getStatus())) // continue statement
				.count(); // count farmer's pending returns

		List<SeedRequest> activeLoans = state.requestData.stream() // continue statement
				.filter(r -> r.getFarmer().equalsIgnoreCase(username) && "Approved".equalsIgnoreCase(r.getStatus())) // continue statement
				.collect(Collectors.toList()); // active loans with upcoming or overdue deadlines

		double activeLoanPhp = state.getFarmerActiveLoanValue(username); // current php loan usage out of 10,000

		VBox cardReq = buildStatCard("Pending Seed Requests", String.valueOf(pendingReqCount), "Awaiting admin approval", "#9a6700"); // card 1
		VBox cardRet = buildStatCard("Pending Seed Returns", String.valueOf(pendingRetCount), "Awaiting seed inspection", "#17684d"); // card 2
		VBox cardDue = buildStatCard("Deadlines of Returns", String.valueOf(activeLoans.size()), "Active loans to repay", "#b42318"); // card 3
		VBox cardCap = buildStatCard("Loan Limit Used", "\u20b1" + String.format("%,.0f", activeLoanPhp), "of \u20b110,000 max per farm", AppState.FOREST_DARK); // card 4

		HBox statsRow = new HBox(16, cardReq, cardRet, cardDue, cardCap); // top KPI row
		for (Node n : statsRow.getChildren()) { // equal card widths
			HBox.setHgrow(n, Priority.ALWAYS); // expand evenly
		}

		// Deadlines of Returns Card
		Label lblDeadlines = new Label("Deadlines of Returns"); // section title
		lblDeadlines.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + AppState.TEXT_DARK + ";"); // bold heading

		Button btnSubmitReturn = new Button("Return Seeds \u2192"); // shortcut to return seeds tab
		btnSubmitReturn.setStyle(state.outlineButtonStyle()); // outline style
		btnSubmitReturn.setOnAction(e -> switchTab("Return")); // navigate to return view

		Region spacer = new Region(); // spacer
		HBox.setHgrow(spacer, Priority.ALWAYS); // expand spacer
		HBox deadlineHeader = new HBox(10, lblDeadlines, spacer, btnSubmitReturn); // header bar
		deadlineHeader.setAlignment(Pos.CENTER_LEFT); // center vertically

		TableView<SeedRequest> deadlineTable = new TableView<>(); // active return deadlines table
		deadlineTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // stretch columns
		deadlineTable.setPrefHeight(195); // table height
		deadlineTable.setPlaceholder(new Label("No active seed loans requiring return right now.")); // empty message

		TableColumn<SeedRequest, Integer> colReqId = new TableColumn<>("Request ID"); // request id column
		colReqId.setCellValueFactory(new PropertyValueFactory<>("requestId")); // bind requestId

		TableColumn<SeedRequest, String> colSeed = new TableColumn<>("Seed Variety"); // seed column
		colSeed.setCellValueFactory(new PropertyValueFactory<>("seedLabel")); // bind seedLabel

		TableColumn<SeedRequest, Double> colQty = new TableColumn<>("Borrowed (kg)"); // amount column
		colQty.setCellValueFactory(new PropertyValueFactory<>("amount")); // bind amount

		TableColumn<SeedRequest, String> colDueDate = new TableColumn<>("Return Deadline"); // due date column
		colDueDate.setCellValueFactory(new PropertyValueFactory<>("dueDate")); // bind dueDate

		TableColumn<SeedRequest, String> colDueState = new TableColumn<>("Deadline Status"); // overdue or on-schedule badge
		colDueState.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().isOverdue() ? "OVERDUE" : "ACTIVE")); // compute deadline status
		colDueState.setCellFactory(state.createStatusBadgeCell()); // render status badge

		deadlineTable.getColumns().addAll(colReqId, colSeed, colQty, colDueDate, colDueState); // attach columns
		deadlineTable.getItems().setAll(activeLoans); // populate active loans

		VBox deadlinesCard = new VBox(12, deadlineHeader, deadlineTable); // wrap in white card
		deadlinesCard.setStyle(state.cardStyle()); // card style

		// Basic Information Card (title says "Basic Information" without "(editable)")
		Label lblBasicInfo = new Label("Basic Information"); // clean section heading without "(editable)"
		lblBasicInfo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + AppState.TEXT_DARK + ";"); // bold heading

		User u = state.currentUser; // current farmer object
		GridPane formGrid = new GridPane(); // two-column profile form
		formGrid.setHgap(16); // horizontal gap
		formGrid.setVgap(10); // vertical gap
		ColumnConstraints c1 = new ColumnConstraints(140); // label column width
		ColumnConstraints c2 = new ColumnConstraints(280); // input column width
		ColumnConstraints c3 = new ColumnConstraints(140); // second label column width
		ColumnConstraints c4 = new ColumnConstraints(280); // second input column width
		formGrid.getColumnConstraints().addAll(c1, c2, c3, c4); // apply column widths

		TextField tfFullName = new TextField(u.getFullName()); // full name field
		tfFullName.setStyle(state.inputStyle()); // input css
		TextField tfPhone = new TextField(u.getPhone()); // phone field
		tfPhone.setStyle(state.inputStyle()); // input css
		TextField tfEmail = new TextField(u.getEmail()); // email field
		tfEmail.setStyle(state.inputStyle()); // input css
		TextField tfFarmName = new TextField(u.getFarmName()); // farm name field
		tfFarmName.setStyle(state.inputStyle()); // input css
		TextField tfFarmAddress = new TextField(u.getFarmAddress()); // farm address field
		tfFarmAddress.setStyle(state.inputStyle()); // input css
		TextField tfRsbsa = new TextField(u.getRsbsaNumber()); // rsbsa field
		tfRsbsa.setStyle(state.inputStyle()); // input css

		formGrid.addRow(0, new Label("Full Name:"), tfFullName, new Label("Farm Name:"), tfFarmName); // row 0
		formGrid.addRow(1, new Label("Phone Number:"), tfPhone, new Label("Farm Address:"), tfFarmAddress); // row 1
		formGrid.addRow(2, new Label("Email Address:"), tfEmail, new Label("RSBSA Number:"), tfRsbsa); // row 2

		Button btnSaveInfo = new Button("Save Changes"); // save profile updates button
		btnSaveInfo.setStyle(state.primaryButtonStyle()); // primary style
		Label lblProfileMsg = new Label(); // save confirmation label
		lblProfileMsg.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + AppState.PRIMARY_GREEN + ";"); // green status text

		btnSaveInfo.setOnAction(e -> { // attach click listener
			if (tfFullName.getText().trim().isEmpty() || tfPhone.getText().trim().isEmpty() // continue statement
					|| tfEmail.getText().trim().isEmpty() || tfFarmAddress.getText().trim().isEmpty()) { // check required fields
				lblProfileMsg.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #b42318;"); // red error text
				lblProfileMsg.setText("Please complete your name, phone, email, and farm address."); // error message
				return; // abort
			}
			u.setFullName(tfFullName.getText().trim()); // update full name
			u.setPhone(tfPhone.getText().trim()); // update phone
			u.setEmail(tfEmail.getText().trim()); // update email
			u.setFarmName(tfFarmName.getText().trim()); // update farm name
			u.setFarmAddress(tfFarmAddress.getText().trim()); // update farm address
			u.setRsbsaNumber(tfRsbsa.getText().trim()); // update rsbsa
			state.saveUsers(); // save updated profile to users.txt
			shellRoot.setLeft(buildSidebar()); // refresh sidebar name
			lblProfileMsg.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + AppState.PRIMARY_GREEN + ";"); // green success text
			lblProfileMsg.setText("Basic information saved."); // confirmation text
		});

		HBox profileFooter = new HBox(12, btnSaveInfo, lblProfileMsg); // button and feedback row
		profileFooter.setAlignment(Pos.CENTER_LEFT); // center vertically

		VBox basicInfoCard = new VBox(12, lblBasicInfo, formGrid, profileFooter); // wrap in white card
		basicInfoCard.setStyle(state.cardStyle()); // card style

		VBox root = new VBox(20, statsRow, deadlinesCard, basicInfoCard); // assemble overview page
		root.setPadding(new Insets(24, 28, 28, 28)); // outer margins
		return root; // return overview pane
	}

	// helper to build a KPI stat card
	private VBox buildStatCard(String title, String value, String subtitle, String accentColor) { // define buildstatcard helper
		Label lblTitle = new Label(title); // card title
		lblTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + AppState.TEXT_MUTED + ";"); // muted title

		Label lblValue = new Label(value); // main metric number
		lblValue.setStyle("-fx-font-size: 25px; -fx-font-weight: bold; -fx-text-fill: " + accentColor + ";"); // colored value

		Label lblSub = new Label(subtitle); // supporting subtitle
		lblSub.setStyle("-fx-font-size: 11px; -fx-text-fill: " + AppState.TEXT_MUTED + ";"); // muted caption

		VBox card = new VBox(6, lblTitle, lblValue, lblSub); // stack labels
		card.setStyle(state.cardStyle()); // card style
		return card; // return stat card
	}

	// =========================================================================
	// 2. REQUEST SEEDS TAB
	// =========================================================================
	private VBox createFarmerRequestPane() { // define createfarmerrequestpane helper
		TableView<Product> table = new TableView<>(); // available seed lots table
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // stretch columns
		table.setPrefHeight(290); // table height

		TableColumn<Product, Integer> colLotID = new TableColumn<>("Lot ID"); // lot id column
		colLotID.setCellValueFactory(new PropertyValueFactory<>("lotID")); // bind lotID

		TableColumn<Product, String> colName = new TableColumn<>("Seed Crop"); // crop name column
		colName.setCellValueFactory(new PropertyValueFactory<>("name")); // bind name

		TableColumn<Product, String> colVariety = new TableColumn<>("Variety"); // variety column
		colVariety.setCellValueFactory(new PropertyValueFactory<>("variety")); // bind variety

		TableColumn<Product, Double> colAmount = new TableColumn<>("Available (kg)"); // available weight column
		colAmount.setCellValueFactory(new PropertyValueFactory<>("amount")); // bind amount

		TableColumn<Product, Double> colPrice = new TableColumn<>("Value / kg (\u20b1)"); // unit value column
		colPrice.setCellValueFactory(new PropertyValueFactory<>("price")); // bind price

		TableColumn<Product, String> colLocation = new TableColumn<>("Storage Location"); // location column
		colLocation.setCellValueFactory(new PropertyValueFactory<>("location")); // bind location

		table.getColumns().addAll(colLotID, colName, colVariety, colAmount, colPrice, colLocation); // add columns
		table.setItems(state.productData); // bind shared inventory

		Label labelAmount = new Label("Amount (kg):"); // amount label
		labelAmount.setMinWidth(110); // label width
		TextField textAmount = new TextField(); // amount input
		textAmount.setPromptText("Enter kilograms to borrow"); // placeholder
		textAmount.setStyle(state.inputStyle()); // input css
		HBox hBoxAmount = new HBox(10, labelAmount, textAmount); // amount row
		hBoxAmount.setAlignment(Pos.CENTER_LEFT); // align vertically

		Label labelPurpose = new Label("Planting Purpose:"); // purpose label
		labelPurpose.setMinWidth(110); // label width
		TextField textPurpose = new TextField(); // purpose input
		textPurpose.setPromptText("e.g., Wet season vegetable plot planting"); // placeholder
		textPurpose.setStyle(state.inputStyle()); // input css
		textPurpose.setPrefWidth(320); // input width
		HBox hBoxPurpose = new HBox(10, labelPurpose, textPurpose); // purpose row
		hBoxPurpose.setAlignment(Pos.CENTER_LEFT); // align vertically

		Label lblEstimate = new Label("Estimated Loan Value: \u20b10.00 (Max limit per farm: \u20b110,000.00)"); // live php value preview
		lblEstimate.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + AppState.TEXT_MUTED + ";"); // muted bold text

		Runnable refreshEstimate = () -> { // update live php loan calculation
			Product sel = table.getSelectionModel().getSelectedItem(); // read selected lot
			try { // attempt risky block
				double kg = Double.parseDouble(textAmount.getText().trim()); // parse entered weight
				if (sel != null && kg > 0) { // compute php total
					double val = state.round2(kg * sel.getPrice()); // weight times unit price
					lblEstimate.setText("Estimated Loan Value: \u20b1" + String.format("%,.2f", val) + " (Max limit per farm: \u20b110,000.00)"); // update label
					return; // done
				}
			} catch (NumberFormatException ignored) { // handle exception
			}
			lblEstimate.setText("Estimated Loan Value: \u20b10.00 (Max limit per farm: \u20b110,000.00)"); // default text
		};

		textAmount.textProperty().addListener((o, a, b) -> refreshEstimate.run()); // update estimate as farmer types
		table.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> refreshEstimate.run()); // update estimate when row changes

		Button btnSubmit = new Button("Submit Seed Request"); // submit request button
		btnSubmit.setStyle(state.primaryButtonStyle()); // primary style
		Button btnRefresh = new Button("Refresh Catalog"); // refresh button
		btnRefresh.setStyle(state.outlineButtonStyle()); // outline style
		Label lblWarn = new Label(); // validation or confirmation label
		lblWarn.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;"); // bold feedback

		btnRefresh.setOnAction(e -> table.refresh()); // refresh table cells

		btnSubmit.setOnAction(e -> { // attach click listener
			Product product = table.getSelectionModel().getSelectedItem(); // get selected seed lot
			if (product == null) { // check selection
				lblWarn.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #b42318;"); // red text
				lblWarn.setText("Error: Please select a seed lot first."); // error text
				return; // abort
			}

			String amountStr = textAmount.getText().trim(); // read amount
			String purpose = textPurpose.getText().trim(); // read purpose
			if (amountStr.isEmpty() || purpose.isEmpty()) { // check required inputs
				lblWarn.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #b42318;"); // red text
				lblWarn.setText("Error: Amount and planting purpose are required!"); // error text
				return; // abort
			}

			try { // attempt risky block
				double amount = Double.parseDouble(amountStr); // parse weight
				if (amount <= 0) { // must be positive
					lblWarn.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #b42318;"); // red text
					lblWarn.setText("Error: Amount must be greater than zero."); // error text
					return; // abort
				}
				if (amount > product.getAmount()) { // check available stock
					lblWarn.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #b42318;"); // red text
					lblWarn.setText("Error: Not enough stock available in this lot."); // error text
					return; // abort
				}

				double requestPhpValue = state.round2(amount * product.getPrice()); // compute php value of this request
				double currentUsed = state.getFarmerActiveLoanValue(state.currentUser.getUsername()); // check farmer's existing active/pending loans
				if (currentUsed + requestPhpValue > AppState.MAX_LOAN_VALUE_PHP) { // enforce 10,000 php per-farm ceiling
					lblWarn.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #b42318;"); // red text
					lblWarn.setText("Error: Request exceeds the \u20b110,000.00 borrowing limit per farm."); // cap exceeded notice
					return; // abort
				}

				state.requestData.add(new SeedRequest( // pass value
						state.nextRequestId++, // next request id
						state.currentUser.getUsername(), // farmer username
						product.getLotID(), // lot id
						product.getName(), // crop name
						product.getVariety(), // variety
						state.round2(amount), // weight in kg
						requestPhpValue, // php value
						purpose, // planting purpose
						"Pending", // initial pending status
						LocalDate.now().toString(), // today's date
						"")); // due date set upon admin approval

				state.saveRequests(); // persist to requests.txt
				lblWarn.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + AppState.PRIMARY_GREEN + ";"); // green success text
				lblWarn.setText("Seed request submitted! An administrator will review it shortly."); // confirmation message
				textAmount.clear(); // reset amount input
				textPurpose.clear(); // reset purpose input
			} catch (NumberFormatException ex) { // handle exception
				lblWarn.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #b42318;"); // red text
				lblWarn.setText("Error: Amount must be a valid number!"); // parse error
			}
		});

		HBox buttons = new HBox(10, btnSubmit, btnRefresh); // action buttons
		VBox card = new VBox(14, table, hBoxAmount, hBoxPurpose, lblEstimate, buttons, lblWarn); // white card wrapper
		card.setStyle(state.cardStyle()); // card style

		VBox root = new VBox(20, card); // main request pane
		root.setPadding(new Insets(24, 28, 28, 28)); // outer margins
		return root; // return request view
	}

	// =========================================================================
	// 3. MY TRANSACTIONS TAB
	// =========================================================================
	private VBox createFarmerTransactionsPane() { // define createfarmertransactionspane helper
		String username = state.currentUser.getUsername(); // current farmer username

		FilteredList<SeedRequest> myRequests = new FilteredList<>(state.requestData, // continue statement
				request -> request.getFarmer().equalsIgnoreCase(username)); // filter farmer's requests

		Label lblRequests = new Label("My Seed Requests"); // section 1 heading
		lblRequests.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + AppState.TEXT_DARK + ";"); // bold heading

		TableView<SeedRequest> requestTable = new TableView<>(); // farmer requests table
		requestTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // stretch columns
		requestTable.setPrefHeight(225); // table height
		requestTable.setPlaceholder(new Label("You have no seed requests yet.")); // empty message

		TableColumn<SeedRequest, Integer> colRequestID = new TableColumn<>("Request ID"); // id column
		colRequestID.setCellValueFactory(new PropertyValueFactory<>("requestId")); // bind requestId

		TableColumn<SeedRequest, String> colSeed = new TableColumn<>("Seed"); // seed column
		colSeed.setCellValueFactory(new PropertyValueFactory<>("seedLabel")); // bind seedLabel

		TableColumn<SeedRequest, Double> colAmount = new TableColumn<>("Amount (kg)"); // weight column
		colAmount.setCellValueFactory(new PropertyValueFactory<>("amount")); // bind amount

		TableColumn<SeedRequest, String> colDue = new TableColumn<>("Return Due Date"); // due date column
		colDue.setCellValueFactory(cell -> { // bind column value
			SeedRequest r = cell.getValue(); // read row request
			if (r.getDueDate() == null || r.getDueDate().isBlank()) { // no deadline yet
				return new SimpleStringProperty("-"); // dash placeholder
			}
			return new SimpleStringProperty(r.isOverdue() ? r.getDueDate() + " (OVERDUE)" : r.getDueDate()); // highlight overdue dates
		});

		TableColumn<SeedRequest, String> colPurpose = new TableColumn<>("Purpose"); // purpose column
		colPurpose.setCellValueFactory(new PropertyValueFactory<>("purpose")); // bind purpose

		TableColumn<SeedRequest, String> colStatus = new TableColumn<>("Status"); // status badge column
		colStatus.setCellValueFactory(new PropertyValueFactory<>("status")); // bind status
		colStatus.setCellFactory(state.createStatusBadgeCell()); // render status pill

		requestTable.getColumns().addAll(colRequestID, colSeed, colAmount, colDue, colPurpose, colStatus); // add columns
		requestTable.setItems(myRequests); // bind filtered list

		FilteredList<SeedReturn> myReturns = new FilteredList<>(state.returnData, // continue statement
				ret -> ret.getFarmer().equalsIgnoreCase(username)); // filter farmer's returns

		Label lblReturns = new Label("My Seed Returns"); // section 2 heading
		lblReturns.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + AppState.TEXT_DARK + ";"); // bold heading

		TableView<SeedReturn> returnTable = new TableView<>(); // farmer returns table
		returnTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // stretch columns
		returnTable.setPrefHeight(205); // table height
		returnTable.setPlaceholder(new Label("You have no seed returns yet.")); // empty message

		TableColumn<SeedReturn, Integer> colReturnID = new TableColumn<>("Return ID"); // return id column
		colReturnID.setCellValueFactory(new PropertyValueFactory<>("returnId")); // bind returnId

		TableColumn<SeedReturn, Integer> colLinkedReq = new TableColumn<>("Request ID"); // linked request id column
		colLinkedReq.setCellValueFactory(new PropertyValueFactory<>("requestId")); // bind requestId

		TableColumn<SeedReturn, String> colReturnSeed = new TableColumn<>("Seed"); // seed column
		colReturnSeed.setCellValueFactory(new PropertyValueFactory<>("seedLabel")); // bind seedLabel

		TableColumn<SeedReturn, Double> colReturnAmount = new TableColumn<>("Returned (kg)"); // amount column
		colReturnAmount.setCellValueFactory(new PropertyValueFactory<>("amount")); // bind amount

		TableColumn<SeedReturn, String> colReturnDate = new TableColumn<>("Date Submitted"); // date column
		colReturnDate.setCellValueFactory(new PropertyValueFactory<>("returnDate")); // bind returnDate

		TableColumn<SeedReturn, String> colReturnStatus = new TableColumn<>("Status"); // status badge column
		colReturnStatus.setCellValueFactory(new PropertyValueFactory<>("status")); // bind status
		colReturnStatus.setCellFactory(state.createStatusBadgeCell()); // render status pill

		returnTable.getColumns().addAll(colReturnID, colLinkedReq, colReturnSeed, colReturnAmount, colReturnDate, colReturnStatus); // add columns
		returnTable.setItems(myReturns); // bind filtered returns

		VBox reqCard = new VBox(12, lblRequests, requestTable); // requests white card
		reqCard.setStyle(state.cardStyle()); // card style

		VBox retCard = new VBox(12, lblReturns, returnTable); // returns white card
		retCard.setStyle(state.cardStyle()); // card style

		VBox root = new VBox(20, reqCard, retCard); // main transactions container
		root.setPadding(new Insets(24, 28, 28, 28)); // outer margins
		return root; // return transactions pane
	}

	// =========================================================================
	// 4. RETURN SEEDS TAB (Repay approved loans + seed interest)
	// =========================================================================
	private VBox createFarmerReturnPane() { // define createfarmerreturnpane helper
		ObservableList<SeedRequest> returnable = FXCollections.observableArrayList(); // active approved requests awaiting return
		loadReturnableRequests(returnable); // populate eligible loans

		Label lblInfo = new Label("Select an approved seed loan below to submit your post-harvest seed repayment (principal + seed interest)."); // helper note
		lblInfo.setStyle("-fx-font-size: 13px; -fx-text-fill: " + AppState.TEXT_MUTED + ";"); // muted text

		TableView<SeedRequest> table = new TableView<>(); // returnable loans table
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // stretch columns
		table.setPrefHeight(270); // table height
		table.setPlaceholder(new Label("No approved requests are ready for return.")); // empty message

		TableColumn<SeedRequest, Integer> colRequestID = new TableColumn<>("Request ID"); // request id column
		colRequestID.setCellValueFactory(new PropertyValueFactory<>("requestId")); // bind requestId

		TableColumn<SeedRequest, String> colSeed = new TableColumn<>("Seed"); // seed column
		colSeed.setCellValueFactory(new PropertyValueFactory<>("seedLabel")); // bind seedLabel

		TableColumn<SeedRequest, Double> colAmount = new TableColumn<>("Borrowed (kg)"); // borrowed weight column
		colAmount.setCellValueFactory(new PropertyValueFactory<>("amount")); // bind amount

		TableColumn<SeedRequest, String> colDue = new TableColumn<>("Due Date"); // due date column
		colDue.setCellValueFactory(cell -> { // bind column value
			SeedRequest r = cell.getValue(); // read row request
			return new SimpleStringProperty(r.isOverdue() ? r.getDueDate() + " (OVERDUE)" : r.getDueDate()); // mark overdue loans
		});

		TableColumn<SeedRequest, String> colPurpose = new TableColumn<>("Purpose"); // purpose column
		colPurpose.setCellValueFactory(new PropertyValueFactory<>("purpose")); // bind purpose

		table.getColumns().addAll(colRequestID, colSeed, colAmount, colDue, colPurpose); // add columns
		table.setItems(returnable); // bind eligible loans

		Label labelAmount = new Label("Returned Seeds (kg):"); // amount input label
		labelAmount.setMinWidth(145); // label width
		TextField textAmount = new TextField(); // returned weight input
		textAmount.setPromptText("e.g. borrowed kg + 5% seed interest"); // placeholder hint
		textAmount.setStyle(state.inputStyle()); // input css
		HBox hBoxAmount = new HBox(10, labelAmount, textAmount); // amount row
		hBoxAmount.setAlignment(Pos.CENTER_LEFT); // center vertically

		Button btnReturn = new Button("Submit Harvest Return"); // submit return button
		btnReturn.setStyle(state.primaryButtonStyle()); // primary style
		Label lblWarn = new Label(); // feedback label
		lblWarn.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;"); // bold feedback

		btnReturn.setOnAction(e -> { // attach click listener
			SeedRequest request = table.getSelectionModel().getSelectedItem(); // get selected loan
			if (request == null) { // check selection
				lblWarn.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #b42318;"); // red text
				lblWarn.setText("Error: Please select an approved request first."); // error text
				return; // abort
			}

			String amountStr = textAmount.getText().trim(); // read weight string
			if (amountStr.isEmpty()) { // check non-empty
				lblWarn.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #b42318;"); // red text
				lblWarn.setText("Error: Returned seed weight is required!"); // error text
				return; // abort
			}

			try { // attempt risky block
				double amount = Double.parseDouble(amountStr); // parse weight
				if (amount <= 0) { // must be positive
					lblWarn.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #b42318;"); // red text
					lblWarn.setText("Error: Amount must be greater than zero."); // error text
					return; // abort
				}

				state.returnData.add(new SeedReturn( // pass value
						state.nextReturnId++, // next return id
						request.getRequestId(), // linked request id
						state.currentUser.getUsername(), // farmer username
						request.getLotId(), // destination lot id
						request.getSeedLabel(), // seed label
						state.round2(amount), // returned weight (allows principal + seed interest)
						"Pending", // awaiting admin quality inspection
						LocalDate.now().toString())); // today's date

				state.saveReturns(); // persist to returns.txt
				returnable.remove(request); // remove from pending return list
				textAmount.clear(); // clear input
				lblWarn.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + AppState.PRIMARY_GREEN + ";"); // green text
				lblWarn.setText("Harvest return submitted! Once accepted by an admin, any overdue status is cleared."); // confirmation text
			} catch (NumberFormatException ex) { // handle exception
				lblWarn.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #b42318;"); // red text
				lblWarn.setText("Error: Amount must be a valid number!"); // parse error
			}
		});

		VBox card = new VBox(14, lblInfo, table, hBoxAmount, btnReturn, lblWarn); // white card wrapper
		card.setStyle(state.cardStyle()); // card style

		VBox root = new VBox(20, card); // main return container
		root.setPadding(new Insets(24, 28, 28, 28)); // outer margins
		return root; // return view
	}

	// populate approved requests for the logged-in farmer that do not yet have a pending/accepted return
	private void loadReturnableRequests(ObservableList<SeedRequest> list) { // define loadreturnablerequests helper
		list.clear(); // clear existing items
		String username = state.currentUser.getUsername(); // logged-in farmer username
		for (SeedRequest request : state.requestData) { // inspect all requests
			if (!request.getFarmer().equalsIgnoreCase(username)) { // skip other farmers
				continue; // next request
			}
			if (!"Approved".equalsIgnoreCase(request.getStatus())) { // only active approved loans can be returned
				continue; // next request
			}

			boolean alreadySubmitted = false; // check if a non-rejected return already exists
			for (SeedReturn ret : state.returnData) { // scan existing returns
				if (ret.getRequestId() == request.getRequestId() && !"Rejected".equalsIgnoreCase(ret.getStatus())) { // found active or accepted return
					alreadySubmitted = true; // mark already submitted
					break; // stop scanning
				}
			}

			if (!alreadySubmitted) { // include if not yet returned
				list.add(request); // add to returnable list
			}
		}
	}
}
