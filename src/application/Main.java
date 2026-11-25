package application; // declare package application

import application.admin.AdminPages; // import adminpages class
import application.farmer.FarmerPages; // import farmerpages class
import application.publicpages.PublicPages; // import publicpages class
import application.shared.AppState; // import appstate class
import javafx.application.Application; // import application class
import javafx.scene.image.Image; // import image class
import javafx.stage.Stage; // import stage class

public class Main extends Application { // define main class

	private final AppState state = new AppState(); // store state field

	private PublicPages publicPages; // store publicpages field
	private AdminPages adminPages; // store adminpages field
	private FarmerPages farmerPages; // store farmerpages field

	@Override // override parent method
	public void start(Stage stage) { // handle start operation
		state.initialize(stage); // execute statement

		Image appIcon = state.loadLogoImage(AppState.LOGO_ICON_PATH); // execute statement
		if (appIcon != null) { // check condition before proceeding
			stage.getIcons().add(appIcon); // execute statement
		}

		publicPages = new PublicPages( // continue statement
				state, // continue statement
				() -> adminPages.showAdminShell(), // continue statement
				() -> farmerPages.showFarmerShell() // continue statement
		); // execute statement

		adminPages = new AdminPages( // continue statement
				state, // continue statement
				() -> publicPages.showLandingPage() // continue statement
		); // execute statement

		farmerPages = new FarmerPages( // continue statement
				state, // continue statement
				() -> publicPages.showLandingPage() // continue statement
		); // execute statement

		publicPages.showLandingPage(); // execute statement
		stage.show(); // execute statement
	}

	public static void main(String[] args) { // handle main operation
		launch(args); // execute statement
	}
}
