package hashAndSalt.testing;

import Database.Database;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;

import java.sql.SQLException;

public class DatabaseLogin extends Application {

    //Test av login med bruk av Database klassen.

    private Scene scene1,scene2,scene3;
    private int userId;
    Database db = new Database();

    @Override
    public void start(Stage primaryStage) {

        Stage window = primaryStage;

        VBox layout = new VBox();
        HBox buttons = new HBox();
        Label notice = new Label("");
        TextField username = new TextField("admin");
        TextField password = new PasswordField();
        Button enter = new Button("Enter");
        Button signup = new Button("SignUp");
        buttons.getChildren().addAll(enter,signup);
        layout.getChildren().addAll(notice,username,password,buttons);
        layout.setPadding(new Insets(20, 20, 20, 20));

        enter.setOnAction(event -> {
            userId = db.login(username.getText(),password.getText());
            if (userId > 0) {
                window.setScene(scene2);
            } else {
                notice.setText("Error logging in");
            }
        });

        signup.setOnAction(event -> {
            window.setScene(scene3);
        });

        VBox layout3 = new VBox();
        TextField su_username = new TextField();
        TextField su_password = new PasswordField();
        TextField su_email = new TextField();
        Label notice2 = new Label("");
        Button add = new Button("Sign up");
        layout3.getChildren().addAll(notice2,su_username,su_password,su_email,add);

        add.setOnAction(event -> {
            if (db.signUp(su_username.getText(),su_password.getText(),su_email.getText())>0) {
                window.setScene(scene1);
                notice.setText("User added");
            } else {
                notice2.setText("User not added");
                //Catch SQLIntegrityConstraintViolationException to notify about username already taken
            }
        });

        VBox layout2 = new VBox();
        Label text = new Label("You are logged in");
        Button logout = new Button("Log out");
        layout2.getChildren().addAll(text,logout);

        logout.setOnAction(event -> {
            db.logout(userId);
            window.setScene(scene1);
        });
        layout.setAlignment(Pos.CENTER);
        layout2.setAlignment(Pos.CENTER);

        scene1 = new Scene(layout,200,200);
        scene2 = new Scene(layout2,200,200);
        scene3 = new Scene(layout3,200,200);

        window.setScene(scene1);
        window.setTitle("Login");
        window.show();
    }

    @Override
    public void stop() throws SQLException {
        // executed when the application shuts down
        db.logout(userId);
        db.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}