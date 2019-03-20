package sample;

import Database.Database;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sample.Controller.mainMenuController;

public class Main extends Application {

    public static Database db = new Database();
    public static Stage window;

    @Override
    public void start(Stage primaryStage) throws Exception {
        window = primaryStage;
        Parent root = FXMLLoader.load(getClass().getResource("/sample/View/login.fxml"));
        primaryStage.setTitle("Binary Warfare");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void stop() {
        // executed when the application shuts down
        if (mainMenuController.user_id > 0) {
            db.logout(mainMenuController.user_id);
        }
    }
}
