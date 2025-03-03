package project;

import javafx.scene.control.*;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;

import javafx.geometry.Insets;

import java.util.List;

public class Main extends Application {
    private DeezerService deezerService;
    private DatabaseService databaseService;
    private MediaPlayer mediaPlayer;
    private ObservableList<Song> songs;
    private User currentUser = null;
    private int songCounter = 0;
    private ListView<Song> songList;
    private int currentSongIndex = -1;
    private Button buyPremiumButton;

    @Override
    public void start(Stage primaryStage) {
        deezerService = new DeezerService();
        databaseService = new DatabaseService();
        songs = FXCollections.observableArrayList();
        showLoginScreen(primaryStage);
        primaryStage.show();
    }

    @Override
    public void stop() {
        if (databaseService != null) {
            databaseService.closeConnection();
        }
    }

    private void showMainScreen(Stage stage) {
        VBox mainBox = new VBox(10);
        mainBox.setAlignment(Pos.CENTER);
        mainBox.setPadding(new Insets(20, 20, 20, 20));
        PlayerView playerView = new PlayerView();

        playerView.setNextSongHandler(() -> {
            playNextSong(playerView);
        });

        playerView.setPrevSongHandler(() -> {
            playPreviousSong(playerView);
        });

        TextField searchField = new TextField();
        searchField.setPromptText("Search for songs...");

        songList = new ListView<>(songs);
        songList.setPrefHeight(400);
        songList.setCellFactory(lv -> new ListCell<>() {
            private final HBox content = new HBox(10);
            private final ImageView imageView = new ImageView();
            private final Label title = new Label();
            private final Label artist = new Label();

            {
                imageView.setFitHeight(50);
                imageView.setFitWidth(50);
                content.setAlignment(Pos.CENTER_LEFT);
                content.getChildren().addAll(imageView,
                        new VBox(title, artist));
            }

            @Override
            protected void updateItem(Song song, boolean empty) {
                super.updateItem(song, empty);
                if (empty || song == null) {
                    setGraphic(null);
                } else {
                    title.setText(song.getName());
                    artist.setText(song.getArtist());

                    if (song.getImageUrl() != null && !song.getImageUrl().isEmpty()) {
                        imageView.setImage(new Image(song.getImageUrl()));
                    } else {
                        imageView.setImage(null);
                    }

                    setGraphic(content);
                }
            }
        });

        songList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) {
                Song selectedSong = songList.getSelectionModel().getSelectedItem();
                if(songCounter >= 4 && currentUser != null && !currentUser.isPremium()){
                    selectedSong = null;
                    playerView.stop();
                }
                if (selectedSong != null) {
                    currentSongIndex = songList.getSelectionModel().getSelectedIndex();
                    playerView.playSong(selectedSong);
                    songCounter++;
                    playerView.likeSong(selectedSong);
                }else{
                    premiumError();
                }
            }
        });

        mainBox.getChildren().add(playerView);

        searchField.setOnAction(e -> {
            String query = searchField.getText();
            List<Song> searchResults = deezerService.searchTracks(query);
            songs.clear();
            songs.addAll(searchResults);
            currentSongIndex = -1;
        });

        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER);

        Button logoutButton = new Button("Logout");
        if(!currentUser.isPremium()){
            buyPremiumButton = new Button("Buy premium");
        }else{
            buyPremiumButton = new Button("Cancel premium");
        }

        controls.getChildren().addAll(logoutButton, buyPremiumButton);

        buyPremiumButton.setOnAction(e->{
            if (currentUser != null && !currentUser.isPremium()) {
                VBox buyPremiumBox = new VBox(10);
                buyPremiumBox.setAlignment(Pos.CENTER);
                buyPremiumBox.setPadding(new Insets(20));

                TextField usernameField = new TextField();
                usernameField.setPromptText("Username");
                PasswordField passwordField = new PasswordField();
                passwordField.setPromptText("Password");

                Button buyPremium = new Button("Buy premium");
                Label messageLabel = new Label();

                buyPremium.setOnAction(l->{
                    if(currentUser.getPassword().equals(passwordField.getText()) && currentUser.getUsername().equals(usernameField.getText())){
                        currentUser.setPremium(true);
                        databaseService.updateUser(currentUser);
                        songCounter = 0;
                        showMainScreen(stage);
                        stage.setTitle("Spotify Clone - Welcome " + currentUser.getUsername() + " (Premium)");
                    }else{
                        messageLabel.setText("Invalid credentials!");
                    }
                });

                buyPremiumBox.getChildren().addAll(usernameField, passwordField, buyPremium, messageLabel);


                Scene scene = new Scene(buyPremiumBox, 300, 400);
                stage.setTitle("Spotify Clone - Login");
                stage.setScene(scene);
            } else if (currentUser.isPremium()) {

                VBox cancelPremiumBox = new VBox(10);
                cancelPremiumBox.setAlignment(Pos.CENTER);
                cancelPremiumBox.setPadding(new Insets(20));

                TextField usernameField = new TextField();
                usernameField.setPromptText("Username");
                PasswordField passwordField = new PasswordField();
                passwordField.setPromptText("Password");

                Button cancelPremium = new Button("Cancel premium");
                Label messageLabel = new Label();

                cancelPremium.setOnAction(l->{
                    if(currentUser.getPassword().equals(passwordField.getText()) && currentUser.getUsername().equals(usernameField.getText())){
                        currentUser.setPremium(false);
                        databaseService.updateUser(currentUser);
                        songCounter = 0;
                        showMainScreen(stage);
                        stage.setTitle("Spotify Clone - Welcome " + currentUser.getUsername());
                    }else{
                        messageLabel.setText("Invalid credentials!");
                    }
                });

                cancelPremiumBox.getChildren().addAll(usernameField, passwordField, cancelPremium, messageLabel);

                Scene scene = new Scene(cancelPremiumBox, 300, 400);
                stage.setTitle("Spotify Clone - Login");
                stage.setScene(scene);

            }
        });

        logoutButton.setOnAction(e -> {
            currentUser = null;
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }
            showLoginScreen(stage);
        });

        mainBox.getChildren().addAll(searchField, songList, controls);

        Scene scene = new Scene(mainBox, 800, 600);
        stage.setTitle("Spotify Clone - Welcome " + currentUser.getUsername() +
                (currentUser.isPremium() ? " (Premium)" : ""));
        stage.setScene(scene);
    }

    private void playNextSong(PlayerView playerView) {
        if (songs.isEmpty() || currentSongIndex == -1) {
            return;
        }

        int nextIndex = currentSongIndex + 1;

        if (nextIndex >= songs.size()) {
            nextIndex = 0;
        }

        premiumValidation();

        songList.getSelectionModel().select(nextIndex);
        currentSongIndex = nextIndex;

        Song nextSong = songs.get(nextIndex);
        playerView.playSong(nextSong);
        songCounter++;
        playerView.likeSong(nextSong);
    }

    private void playPreviousSong(PlayerView playerView) {
        if (songs.isEmpty() || currentSongIndex == -1) {
            return;
        }

        int prevIndex = currentSongIndex - 1;

        if (prevIndex < 0) {
            prevIndex = songs.size() - 1;
        }

        premiumValidation();


        songList.getSelectionModel().select(prevIndex);
        currentSongIndex = prevIndex;

        Song prevSong = songs.get(prevIndex);
        playerView.playSong(prevSong);
        songCounter++;
        playerView.likeSong(prevSong);
    }

    private void showLoginScreen(Stage stage) {
        VBox loginBox = new VBox(10);
        loginBox.setAlignment(Pos.CENTER);
        loginBox.setPadding(new Insets(20));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Button loginButton = new Button("Login");
        Button registerButton = new Button("Register");
        Label messageLabel = new Label();

        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();

            if (databaseService.validateUser(username, password)) {
                currentUser = databaseService.getUser(username);
                songCounter = 0;
                showMainScreen(stage);
            } else {
                messageLabel.setText("Invalid credentials!");
            }
        });

        registerButton.setOnAction(e -> {
            showRegistrationScreen(stage);
        });

        loginBox.getChildren().addAll(
                new Label("Spotify Clone"),
                usernameField,
                passwordField,
                loginButton,
                registerButton,
                messageLabel
        );

        Scene scene = new Scene(loginBox, 300, 400);
        stage.setTitle("Spotify Clone - Login");
        stage.setScene(scene);
    }

    private void showRegistrationScreen(Stage stage){
        VBox registerBox = new VBox(10);
        registerBox.setAlignment(Pos.CENTER);
        registerBox.setPadding(new Insets(20));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        Button registerButton1 = new Button("Register");
        Label messageLabel = new Label();
        registerBox.getChildren().addAll(
                new Label("Create Account"),
                usernameField,
                passwordField,
                registerButton1,
                messageLabel
        );

        Scene scene = new Scene(registerBox, 300, 400);
        stage.setTitle("Spotify Clone - Register");
        stage.setScene(scene);

        registerButton1.setOnAction(e1 -> {
            String username = usernameField.getText();
            String password = passwordField.getText();

            if (databaseService.getUser(username) != null) {
                messageLabel.setText("User already exists!");
                showLoginScreen(stage);
            } else {
                User newUser = new User(username, password, false);
                boolean success = databaseService.addUser(newUser);

                if (success) {
                    messageLabel.setText("Registration successful!");
                    new Thread(() -> {
                        try {
                            Thread.sleep(2000);
                            javafx.application.Platform.runLater(() -> showLoginScreen(stage));
                        } catch (InterruptedException ie) {
                            ie.printStackTrace();
                        }
                    }).start();
                } else {
                    messageLabel.setText("Registration failed!");
                }
            }
        });
    }

    private void premiumError() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText("You Should buy premium for better experience.");
        alert.showAndWait();
    }

    private void premiumValidation(){
        if (songCounter >= 4 && currentUser != null && !currentUser.isPremium()) {
            premiumError();
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}