package application;

import application.admin.AdminPages;
import application.farmer.FarmerPages;
import application.publicpages.PublicPages;
import application.shared.AppState;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    private final AppState state = new AppState();

    private PublicPages publicPages;
    private AdminPages adminPages;
    private FarmerPages farmerPages;

    @Override
    public void start(Stage stage) {
    	
        state.initialize();

        publicPages = new PublicPages(stage, state, user -> {

            if ("Admin".equals(user.getRole())) {
                adminPages.showAdminDashboard();
            } else {
                farmerPages.showFarmerDashboard();
            }

        });

        adminPages = new AdminPages(
            stage,
            state,
            () -> publicPages.showLandingPage()
        );

        farmerPages = new FarmerPages(
            stage,
            state,
            () -> publicPages.showLandingPage()
        );

        publicPages.showLandingPage();
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
