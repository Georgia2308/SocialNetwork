package socialnetwork.controller;

import com.google.common.hash.Hashing;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import socialnetwork.domain.Utilizator;
import socialnetwork.domain.validators.ValidationException;
import socialnetwork.service.UtilizatorService;

import javax.swing.text.html.ImageView;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginController {
    UtilizatorService service;
    Stage stage;

    @FXML
    TextField username;

    @FXML
    TextField password;

    @FXML
    Label UsernameLabel;

    @FXML
    Label PaswLabel;

    @FXML
    Button LoginButton;

    @FXML
    Button SignButton;

    @FXML
    CheckBox remember;

    @FXML
    AnchorPane pane;

    public void setService(UtilizatorService service) {
        this.service = service;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void loginHandler() throws IOException, NoSuchAlgorithmException {
        String hash = Hashing.sha256()
            .hashString(password.getText(), StandardCharsets.UTF_8)
            .toString();
        if (username.getText().equals("admin") && password.getText().equals("admin")) {
            MessageAlert.showMessage(null, null, null, "Logged in as admin!");
        } else if (service.findByName(username.getText()).isPresent() && service.findByName(username.getText()).get().getHash().equals(hash)) {
            if (remember.isSelected()) {
                if (!service.lastLogin().isPresent() || (service.lastLogin().isPresent() && !service.lastLogin().get().getLastName().equals(username.getText()))) {
                    service.setLogin(service.findByName(username.getText()).get());
                }
            } else {
                service.setLogin(null);
            }
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/views/mainView.fxml"));
            AnchorPane layout = loader.load();
            stage.setScene(new Scene(layout));
            stage.setWidth(430);
            MainController controller = loader.getController();
            controller.setService(service);
            controller.setUser(username.getText());
            controller.setStage(stage);
        } else {
            MessageAlert.showErrorMessage(null, "Incorrect username or password!");
        }
    }

    public void signupHandler() {
        username.setVisible(false);
        password.setVisible(false);
        remember.setVisible(false);
        UsernameLabel.setVisible(false);
        PaswLabel.setVisible(false);
        LoginButton.setVisible(false);
        SignButton.setVisible(false);

        TextField nume = new TextField();
        nume.setPrefWidth(180);
        nume.setPrefHeight(26);
        nume.setLayoutX(117);
        nume.setLayoutY(193);
        Label numelabel = new Label("Nume:");
        numelabel.setStyle("-fx-text-fill: white;");
        numelabel.setLayoutX(190);
        numelabel.setLayoutY(175);
        TextField prenume = new TextField();
        prenume.setPrefWidth(180);
        prenume.setPrefHeight(26);
        prenume.setLayoutX(117);
        prenume.setLayoutY(237);
        Label prenumelabel = new Label("Prenume:");
        prenumelabel.setStyle("-fx-text-fill: white;");
        prenumelabel.setLayoutX(183);
        prenumelabel.setLayoutY(220);
        PasswordField parola = new PasswordField();
        parola.setPrefWidth(180);
        parola.setPrefHeight(26);
        parola.setLayoutX(117);
        parola.setLayoutY(278);
        Label parolalabel = new Label("Parola:");
        parolalabel.setStyle("-fx-text-fill: white;");
        parolalabel.setLayoutX(189);
        parolalabel.setLayoutY(262);
        Label piclabel = new Label("");
        piclabel.setStyle("-fx-text-fill: #fe4566;");
        piclabel.setLayoutX(145);
        piclabel.setLayoutY(383);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("C:\\Users\\georg\\Documents\\Aan2_Semestru1\\MAP\\Laburi\\LabExtins3\\src\\main\\resources\\design"));
        Button pic = new Button("Choose Profile Pic");
        pic.setOnAction(e -> {
            File selectedFile = fileChooser.showOpenDialog(stage);
            try {
                piclabel.setText("(  " + selectedFile.getCanonicalPath().replace("C:\\Users\\georg\\Documents\\Aan2_Semestru1\\MAP\\Laburi\\LabExtins3\\src\\main\\resources\\design\\", "design/") + "  )");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });
        pic.setStyle("-fx-font-size: 14;");
        pic.setPrefWidth(180);
        pic.setPrefHeight(30);
        pic.setLayoutX(117);
        pic.setLayoutY(355);
        Button create = new Button("Create Account");
        create.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: #ff7b94;");
        create.setPrefWidth(180);
        create.setPrefHeight(30);
        create.setLayoutX(117);
        create.setLayoutY(410);
        Button back = new Button("Back");
        back.setLayoutX(24);
        back.setLayoutY(17);
        back.setOnMouseClicked(e -> {
            pane.getChildren().remove(nume);
            pane.getChildren().remove(numelabel);
            pane.getChildren().remove(prenume);
            pane.getChildren().remove(prenumelabel);
            pane.getChildren().remove(parola);
            pane.getChildren().remove(parolalabel);
            pane.getChildren().remove(pic);
            pane.getChildren().remove(piclabel);
            pane.getChildren().remove(create);
            pane.getChildren().remove(back);
            username.setVisible(true);
            password.setVisible(true);
            remember.setVisible(true);
            UsernameLabel.setVisible(true);
            PaswLabel.setVisible(true);
            LoginButton.setVisible(true);
            SignButton.setVisible(true);
        });
        create.setOnAction(e -> {
            try {
                Utilizator u = new Utilizator(service.getNextUserId(), nume.getText(), prenume.getText());
                u.setPic(piclabel.getText().replace("( ", "").replace(" )", ""));
                u.setHash(Hashing.sha256()
                        .hashString(parola.getText(), StandardCharsets.UTF_8)
                        .toString());
                service.addUtilizator(u);
                pane.getChildren().remove(nume);
                pane.getChildren().remove(numelabel);
                pane.getChildren().remove(prenume);
                pane.getChildren().remove(prenumelabel);
                pane.getChildren().remove(parola);
                pane.getChildren().remove(parolalabel);
                pane.getChildren().remove(pic);
                pane.getChildren().remove(piclabel);
                pane.getChildren().remove(create);
                pane.getChildren().remove(back);
                username.setVisible(true);
                password.setVisible(true);
                remember.setVisible(true);
                UsernameLabel.setVisible(true);
                PaswLabel.setVisible(true);
                LoginButton.setVisible(true);
                SignButton.setVisible(true);
                username.setText(prenume.getText());
                password.setText(parola.getText());
            } catch (ValidationException ex) {
                MessageAlert.showErrorMessage(null, ex.getMessage());
            }
        });
        pane.getChildren().add(nume);
        pane.getChildren().add(numelabel);
        pane.getChildren().add(prenume);
        pane.getChildren().add(prenumelabel);
        pane.getChildren().add(parola);
        pane.getChildren().add(parolalabel);
        pane.getChildren().add(pic);
        pane.getChildren().add(piclabel);
        pane.getChildren().add(create);
        pane.getChildren().add(back);
    }

}
