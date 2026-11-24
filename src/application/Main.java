package application;

import application.admin.AdminPages;
import application.farmer.FarmerPages;
import application.publicpages.PublicPages;
import application.shared.AppState;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

	private final AppState state = new AppState();

	private PublicPages publicPages;
	private AdminPages adminPages;
	private FarmerPages farmerPages;

	@Override
	public void start(Stage stage) {
		state.initialize(stage);

		Image appIcon = state.loadLogoImage(AppState.LOGO_ICON_PATH);
		if (appIcon != null) {
			stage.getIcons().add(appIcon);
		}

		publicPages = new PublicPages(
				state,
				() -> adminPages.showAdminShell(),
				() -> farmerPages.showFarmerShell()
		);

		adminPages = new AdminPages(
				state,
				() -> publicPages.showLandingPage()
		);

		farmerPages = new FarmerPages(
				state,
				() -> publicPages.showLandingPage()
		);

		publicPages.showLandingPage();
		stage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
