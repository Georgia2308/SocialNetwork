package socialnetwork.controller;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import socialnetwork.domain.*;
import socialnetwork.repository.paging.Page;
import socialnetwork.service.UtilizatorService;
import socialnetwork.utils.ChangeEvent;
import socialnetwork.utils.Event;
import socialnetwork.utils.Observer;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MainController implements Observer<ChangeEvent> {
    UtilizatorService service;
    Stage stage;
    Utilizator user;
    ObservableList<Utilizator> friendsmodel = FXCollections.observableArrayList();
    ObservableList<Request> requestmodel = FXCollections.observableArrayList();
    ObservableList<Utilizator> usermodel = FXCollections.observableArrayList();
    List<Eveniment> eventmodel;

    @FXML
    AnchorPane labelanchor;
    @FXML
    Label label;
    @FXML
    TableView<Utilizator> friends;
    @FXML
    TableColumn<Utilizator, String> numeFriend;
    @FXML
    TableColumn<Utilizator, String> prenumeFriend;
    @FXML
    TableColumn<Utilizator, Rectangle> PicFriend;
    @FXML
    Button chat;
    @FXML
    Button remove;
    @FXML
    TableView<Request> requests;
    @FXML
    TableColumn<Request, String> numeRequest;
    @FXML
    TableColumn<Request, String> prenumeRequest;
    @FXML
    TableColumn<Request, String> statusRequest;
    @FXML
    Button accept;
    @FXML
    Button decline;
    @FXML
    TableView<Utilizator> people;
    @FXML
    TableColumn<Utilizator, String> numePeople;
    @FXML
    TableColumn<Utilizator, String> prenumePeople;
    @FXML
    Pagination page;
    @FXML
    Button add;
    @FXML
    Button cancel;
    @FXML
    LineChart<Number, Number> chart;
    @FXML
    DatePicker from;
    @FXML
    DatePicker to;
    @FXML
    AnchorPane events;
    @FXML
    AnchorPane news;
    @FXML
    Tab n;
    @FXML
    TabPane tabPane;
    @FXML
    Button bell;
    @FXML
    ImageView pic;

    @Override
    public void update(ChangeEvent messageTaskChangeEvent) {
        initFriendModel();
        numeFriend.setCellValueFactory(new PropertyValueFactory<Utilizator, String>("firstName"));
        prenumeFriend.setCellValueFactory(new PropertyValueFactory<Utilizator, String>("lastName"));

        friends.setItems(friendsmodel);
        initRequestModel();
        numeRequest.setCellValueFactory(new PropertyValueFactory<Request, String>("nume1"));
        prenumeRequest.setCellValueFactory(new PropertyValueFactory<Request, String>("prenume1"));
        statusRequest.setCellValueFactory(new PropertyValueFactory<Request, String>("status"));
        requests.setItems(requestmodel);
        initUserModel();
        numePeople.setCellValueFactory(new PropertyValueFactory<Utilizator, String>("firstName"));
        prenumePeople.setCellValueFactory(new PropertyValueFactory<Utilizator, String>("lastName"));
        people.setItems(usermodel);
        initEventModel();
        initNewsModel();
    }

    public void setService(UtilizatorService service) {
        this.service = service;
        this.service.addObserver(this);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setUser(String username) {
        this.user = service.findByName(username).get();
        label.setText("Logged in as ");
        Label l = new Label();
        l.setText(user.getLastName() + " " + user.getFirstName());
        l.setLayoutX(label.getLayoutX()+73);
        l.setLayoutY(label.getLayoutY()-5);
        l.setStyle("-fx-font-size: 16;-fx-text-fill: #fe4566");
        labelanchor.getChildren().add(l);

        //pic.setImage(new Image(user.getPic()));
        Rectangle c = new Rectangle(56, 56);
        c.setFill(new ImagePattern(new Image(user.getPic())));
        c.setLayoutX(0);
        c.setLayoutY(3);
        labelanchor.getChildren().add(c);

        initFriendModel();
        initRequestModel();
        initUserModel();
        initEventModel();
        initNewsModel();
    }

    private void initFriendModel() {
        Iterable<Utilizator> prieteni = service.findPrieteni(user.getId());
        List<Utilizator> list = StreamSupport.stream(prieteni.spliterator(), false)
                .collect(Collectors.toList());
        friendsmodel.setAll(list);
    }

    private void initRequestModel() {
        Iterable<Request> req = service.getAllRequests();
        List<Request> list = StreamSupport.stream(req.spliterator(), false)
                .map(r -> {
                    r.setNume1(service.findUtilizator(r.getId().getLeft()).get().getFirstName());
                    r.setNume2(service.findUtilizator(r.getId().getRight()).get().getFirstName());
                    r.setPrenume1(service.findUtilizator(r.getId().getLeft()).get().getLastName());
                    r.setPrenume2(service.findUtilizator(r.getId().getRight()).get().getLastName());
                    return r;
                })
                .filter(r ->
                        r.getId().getRight() == user.getId())
                .collect(Collectors.toList());
        requestmodel.setAll(list);
    }

    private void initUserModel() {
        page.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) ->
                pageHandler());
        Page<Utilizator> users = service.getPagedUsers(page.getCurrentPageIndex(), user);
        usermodel.setAll(users.getContent().collect(Collectors.toList()));
    }

    private void initEventModel() {
        Iterable<Eveniment> list = service.getEvents(user);
        eventmodel = StreamSupport.stream(list.spliterator(), false)
                .collect(Collectors.toList());
        double pos = 20;
        for (int i = 0; i < eventmodel.size(); i++) {
            Eveniment e = eventmodel.get(i);
            Rectangle r = new Rectangle(300, 60, Paint.valueOf("white"));
            CheckBox c = new CheckBox();
            r.setArcWidth(20);
            r.setArcHeight(20);
            r.setLayoutY(pos - 5);
            Label l = new Label();
            Label l2 = new Label();
            Label l3 = new Label();
            l.setText(e.getTitlu());
            l.setFont(new Font(15));
            l2.setText("- " + e.getDescriere() + " -");
            l3.setText(e.getData().toString());
            l.setLayoutY(pos);
            l2.setLayoutY(pos + 30);
            l3.setLayoutY(pos + 17);
            c.setLayoutY(pos);
            pos += 68;
            r.setLayoutX(43);
            l.setLayoutX(193 - e.getTitlu().length() * 4);
            l2.setLayoutX(193 - e.getDescriere().length() * 3);
            l3.setLayoutX(193 - e.getData().toString().length() * 3);
            c.setLayoutX(310);
            if (service.notifications(user, e)) {
                c.setSelected(true);
            }
            c.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if (c.isSelected()) {
                        service.setNotifications(user, e, true);
                    } else {

                        service.setNotifications(user, e, false);
                    }
                }
            });
            events.getChildren().add(r);
            events.getChildren().add(l);
            events.getChildren().add(l2);
            events.getChildren().add(l3);
            events.getChildren().add(c);
        }
    }

    private void initNewsModel() {
        if (eventmodel.size() > 0) {
            bell.setStyle("-fx-text-fill: #dc0c3e; -fx-background-radius: 100");
        }
    }

    @FXML
    public void initialize() {
        numeFriend.setCellValueFactory(new PropertyValueFactory<Utilizator, String>("firstName"));
        prenumeFriend.setCellValueFactory(new PropertyValueFactory<Utilizator, String>("lastName"));
        friends.setItems(friendsmodel);
        numeRequest.setCellValueFactory(new PropertyValueFactory<Request, String>("nume1"));
        prenumeRequest.setCellValueFactory(new PropertyValueFactory<Request, String>("prenume1"));
        statusRequest.setCellValueFactory(new PropertyValueFactory<Request, String>("status"));
        requests.setItems(requestmodel);
        numePeople.setCellValueFactory(new PropertyValueFactory<Utilizator, String>("firstName"));
        prenumePeople.setCellValueFactory(new PropertyValueFactory<Utilizator, String>("lastName"));
        people.setItems(usermodel);
    }

    public void logoutHandler() throws IOException {
        service.setLogin(null);
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/views/loginView.fxml"));
        AnchorPane layout = loader.load();
        stage.setScene(new Scene(layout));
        LoginController controller = loader.getController();
        controller.setService(service);
        controller.setStage(stage);
        stage.setWidth(430);
    }

    public void removeHandler() {
        Utilizator u = friends.getSelectionModel().getSelectedItem();
        if (u != null) {
            service.deletePrietenie(new Tuple<>(u.getId(), user.getId()));
        } else {
            MessageAlert.showErrorMessage(null, "Nu s-a selectat nimic.");
        }
    }

    public void acceptHandler() {
        Request r = requests.getSelectionModel().getSelectedItem();
        if (r != null) {
            service.addRequest(r);
            accept.setDisable(true);
            decline.setDisable(true);
            //service.removeRequest(r.getId());
        } else {
            MessageAlert.showErrorMessage(null, "Nu s-a selectat nimic.");
        }
    }

    public void declineHandler() {
        Request r = requests.getSelectionModel().getSelectedItem();
        if (r != null) {
            service.declineRequest(r);
            accept.setDisable(true);
            decline.setDisable(true);
            //service.removeRequest(r.getId());
        } else {
            MessageAlert.showErrorMessage(null, "Nu s-a selectat nimic.");
        }
    }

    public void addHandler() {
        Utilizator u = people.getSelectionModel().getSelectedItem();
        if (u != null) {
            service.addRequest(new Request(new Tuple<Long, Long>(user.getId(), u.getId()), "pending"));
            add.setDisable(true);
        } else {
            MessageAlert.showErrorMessage(null, "Nu s-a selectat nimic.");
        }
    }

    public void checkPeopleHandler() {
        Utilizator u = people.getSelectionModel().getSelectedItem();
        Optional<Request> req = service.findRequest(new Tuple<Long, Long>(user.getId(), u.getId()));
        if (u != null) {
            if (req.isPresent() && !req.get().getStatus().equals("declined")) {
                add.setDisable(true);
                if (req.get().getId().getLeft() == user.getId())
                    cancel.setDisable(false);
                else
                    cancel.setDisable(true);
            } else {
                add.setDisable(false);
                cancel.setDisable(true);
            }
        } else {
            add.setDisable(false);
            cancel.setDisable(true);
        }
    }


    public void checkRequestHandler() {
        Request r = requests.getSelectionModel().getSelectedItem();
        if (r != null) {
            if (!r.getStatus().equals("pending")) {
                accept.setDisable(true);
                decline.setDisable(true);
            } else {
                accept.setDisable(false);
                decline.setDisable(false);
            }
        }
    }

    public void checkFriendsHandler() {
        Utilizator u = friends.getSelectionModel().getSelectedItem();
        if (u == null) {
            chat.setDisable(true);
            remove.setDisable(true);
        } else {
            chat.setDisable(false);
            remove.setDisable(false);
        }
    }

    public void chatHandler() throws IOException {
        Utilizator u = friends.getSelectionModel().getSelectedItem();
        if (u != null) {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/views/chatView.fxml"));
            AnchorPane layout = loader.load();
            stage.setScene(new Scene(layout));
            stage.setWidth(430);
            ChatController controller = loader.getController();
            controller.setService(service);
            controller.setUser(user.getLastName(), u.getLastName());
            controller.setStage(stage);
        } else {
            MessageAlert.showErrorMessage(null, "Nu s-a selectat nimic.");
        }
    }

    public void cancelHandler() {
        Utilizator u = people.getSelectionModel().getSelectedItem();
        if (u != null) {
            service.removeRequest(new Tuple<Long, Long>(user.getId(), u.getId()));
            add.setDisable(false);
            cancel.setDisable(true);
        } else {
            MessageAlert.showErrorMessage(null, "Nu s-a selectat nimic.");
        }
    }

    public void chartHandler() {
        if (from.getValue() != null && to.getValue() != null) {
            chart.getData().clear();
            XYChart.Series fseries = new XYChart.Series();
            fseries.setName("Friendships");
            XYChart.Series mseries = new XYChart.Series();
            mseries.setName("Messages");
            Iterable<Prietenie> list = service.getAllFriendships();
            List<Prietenie> prietenii = StreamSupport.stream(list.spliterator(), false)
                    .filter(p ->
                            (p.getId().getLeft() == user.getId() || p.getId().getRight() == user.getId()) && p.getDate().toLocalDate().isAfter(from.getValue().minusDays(1)) && p.getDate().toLocalDate().isBefore(to.getValue().plusDays(1)))
                    .collect(Collectors.toList());
            Iterable<Message> list2 = service.getAllMessages();
            List<Message> mesaje = StreamSupport.stream(list2.spliterator(), false)
                    .filter(m ->
                            (m.getFrom().getId() == user.getId() || StreamSupport.stream(m.getTo().spliterator(), false).filter(u -> u.getId() == user.getId()).collect(Collectors.toList()).size() != 0) && m.getData().toLocalDate().isAfter(from.getValue().minusDays(1)) && m.getData().toLocalDate().isBefore(to.getValue().plusDays(1)))
                    .collect(Collectors.toList());
            for (LocalDate date = from.getValue(); date.isBefore(to.getValue().plusDays(1)); date = date.plusDays(1)) {
                int fcount = 0, mcount = 0;
                for (Prietenie p : prietenii) {
                    if (p.getDate().toLocalDate().equals(date)) {
                        fcount++;
                    }
                }
                for (Message m : mesaje) {
                    if (m.getData().toLocalDate().equals(date)) {
                        mcount++;
                    }
                }
                fseries.getData().add(new XYChart.Data(date.toString(), fcount));
                mseries.getData().add(new XYChart.Data(date.toString(), mcount));
            }
            chart.getData().add(fseries);
            chart.getData().add(mseries);
        }
    }

    public void downloadHandler() throws FileNotFoundException, DocumentException {
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream("C:\\Users\\georg\\Documents\\Aan2_Semestru1\\MAP\\Laburi\\LabExtins3\\data\\activity.pdf"));
        document.open();
        document.addTitle("Activity of " + user.getLastName() + " " + user.getFirstName() + " from " + from.getValue().toString() + " to " + to.getValue().toString());
        Paragraph text = new Paragraph();
        text.add(new Paragraph("Activity of " + user.getLastName() + " " + user.getFirstName() + " from " + from.getValue().toString() + " to " + to.getValue().toString()));
        text.add(new Paragraph("\n"));
        text.add(new LineSeparator());
        text.add(new Paragraph("\n"));

        Iterable<Prietenie> list = service.getAllFriendships();
        List<Prietenie> prietenii = StreamSupport.stream(list.spliterator(), false)
                .filter(p ->
                        (p.getId().getLeft() == user.getId() || p.getId().getRight() == user.getId()) && p.getDate().toLocalDate().isAfter(from.getValue().minusDays(1)) && p.getDate().toLocalDate().isBefore(to.getValue().plusDays(1)))
                .collect(Collectors.toList());
        Iterable<Message> list2 = service.getAllMessages();
        List<Message> mesaje = StreamSupport.stream(list2.spliterator(), false)
                .filter(m ->
                        (m.getFrom().getId() == user.getId() || StreamSupport.stream(m.getTo().spliterator(), false).filter(u -> u.getId() == user.getId()).collect(Collectors.toList()).size() != 0) && m.getData().toLocalDate().isAfter(from.getValue().minusDays(1)) && m.getData().toLocalDate().isBefore(to.getValue().plusDays(1)))
                .collect(Collectors.toList());

        text.add(new Paragraph("Friendships:\n"));
        for (Prietenie prietenie : prietenii) {
            text.add(new Paragraph(service.findUtilizator(prietenie.getId().getLeft()).get().getFirstName() + " " + service.findUtilizator(prietenie.getId().getLeft()).get().getLastName() + " became friends with " + service.findUtilizator(prietenie.getId().getRight()).get().getFirstName() + " " + service.findUtilizator(prietenie.getId().getRight()).get().getLastName() + " on " +prietenie.getDate() + "\n"));
            text.add(new Paragraph("\n"));
        }

        text.add(new Paragraph("\n"));
        text.add(new LineSeparator());
        text.add(new Paragraph("\n"));
        text.add(new Paragraph("Messages:\n"));
        for (Message message : mesaje) {
            text.add(new Paragraph("'" + message.getMessage()+ "' from " + message.getFrom().getFirstName() + " " +message.getFrom().getLastName() + " to " + message.getTo().get(0).getFirstName() + " " + message.getTo().get(0).getLastName() + " at " + message.getData() + "\n"));
            text.add(new Paragraph("\n"));
        }
        document.add(text);
        document.close();

        MessageAlert.showMessage(null, Alert.AlertType.INFORMATION,"","Activity downloaded!");
    }

    public void eventHandler() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/views/eventView.fxml"));
        AnchorPane layout = loader.load();
        Stage s = new Stage();
        s.getIcons().add(new Image("/design/logo.png"));
        s.setTitle("Pinch");
        s.setScene(new Scene(layout));
        EventController controller = loader.getController();
        s.show();
        controller.setService(service);
        controller.setUser(user.getLastName());
        controller.setStage(s);
    }

    public void redHandler() {
        n.setStyle("");
    }

    public void pageHandler() {
        initUserModel();
        numePeople.setCellValueFactory(new PropertyValueFactory<Utilizator, String>("firstName"));
        prenumePeople.setCellValueFactory(new PropertyValueFactory<Utilizator, String>("lastName"));
        people.setItems(usermodel);
    }

    public void selectedHandler() {
        for (Tab t : tabPane.getTabs()) {
            t.setStyle("");
        }
        tabPane.getSelectionModel().getSelectedItem().setStyle("-fx-background-color: #ffedf0;");
    }

    public void newsHandler() {
        if (bell.getStyle() != "-fx-background-radius: 100") {
            bell.setStyle("-fx-background-radius: 100");
            Tab tab = new Tab("News");
            tab.setOnSelectionChanged(e->selectedHandler());
            news = new AnchorPane();
            double pos = 20;
            int i = 0;
            for (i = 0; i < eventmodel.size(); i++) {
                Eveniment e = eventmodel.get(i);
                if (service.notifications(user, e)) {
                    Rectangle r = new Rectangle(300, 60, Paint.valueOf("white"));
                    r.setArcWidth(20);
                    r.setArcHeight(20);
                    r.setLayoutY(pos - 5);
                    Label l = new Label();
                    Label l2 = new Label();
                    l.setText("Don't forget about " + e.getTitlu() + "!");
                    l.setFont(new Font(15));
                    l2.setText("Event coming up on " + e.getData().toString());
                    l.setLayoutY(pos);
                    l2.setLayoutY(pos + 25);
                    pos += 68;
                    r.setLayoutX(63);
                    l.setLayoutX(213 - (e.getTitlu().length() + 19) * 4);
                    l2.setLayoutX(213 - (e.getData().toString().length() + 19) * 4);
                    news.getChildren().add(r);
                    news.getChildren().add(l);
                    news.getChildren().add(l2);
                }
            }
            tab.setContent(news);
            tabPane.getTabs().add(tab);
            tabPane.getSelectionModel().select(tab);
        }
    }

}
