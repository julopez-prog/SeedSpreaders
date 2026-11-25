package application; // root application package

import application.admin.AdminPages; // admin dashboard and management screens
import application.farmer.FarmerPages; // farmer dashboard, requests, and returns screens
import application.publicpages.PublicPages; // landing page, sign-in, and donation modal
import application.shared.AppState; // shared data state and file persistence
import javafx.application.Application; // javafx application base class
import javafx.scene.image.Image; // window icon image
import javafx.stage.Stage; // primary application window

// main entry point for the seed spreaders desktop application
public class Main extends Application { // declare main class

	private final AppState state = new AppState(); // single shared state across all screens
	private PublicPages publicPages; // controller for landing and login views
	private AdminPages adminPages; // controller for admin views
	private FarmerPages farmerPages; // controller for farmer views

	@Override
	public void start(Stage stage) { // invoked by javafx runtime on startup
		state.initialize(stage); // load text files and seed default records if empty

		Image appIcon = state.loadImage(AppState.LOGO_ICON_PATH); // load green seedling window icon
		if (appIcon != null) { // attach icon if found in resources
			stage.getIcons().add(appIcon); // set taskbar and titlebar icon
		}

		publicPages = new PublicPages(stage, state, user -> { // initialize public screens with login callback
			if ("Admin".equalsIgnoreCase(user.getRole())) { // route admin accounts to admin shell
				adminPages.showAdminShell(); // open admin dashboard
			} else { // route farmer accounts to farmer shell
				farmerPages.showFarmerShell(); // open farmer dashboard
			}
		});

		adminPages = new AdminPages(stage, state, () -> publicPages.showLandingPage()); // initialize admin screens with logout callback
		farmerPages = new FarmerPages(stage, state, () -> publicPages.showLandingPage()); // initialize farmer screens with logout callback

		publicPages.showLandingPage(); // display landing page first
		stage.show(); // show primary stage
	}

	public static void main(String[] args) { // standard java main method
		launch(args); // launch javafx application
	}
}
