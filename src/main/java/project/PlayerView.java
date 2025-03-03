package project;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.geometry.Pos;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class PlayerView extends VBox {
    private MediaPlayer mediaPlayer;
    private Song currentSong;
    private Label songLabel;
    private Label artistLabel;
    private Slider timeSlider;
    private Label timeLabel;
    private Button playButton;
    private Button likeButton;
    private Button nextButton;
    private Button prevButton;
    private ImageView albumArt;

    public interface NextSongHandler {
        void onNextSong();
    }

    public interface PrevSongHandler {
        void onPrevSong();
    }

    private NextSongHandler nextSongHandler;
    private PrevSongHandler prevSongHandler;

    public void setNextSongHandler(NextSongHandler handler) {
        this.nextSongHandler = handler;
    }

    public void setPrevSongHandler(PrevSongHandler handler) {
        this.prevSongHandler = handler;
    }

    public PlayerView() {
        setSpacing(10);
        setAlignment(Pos.CENTER);

        albumArt = new ImageView();
        albumArt.setFitHeight(200);
        albumArt.setFitWidth(200);

        songLabel = new Label();
        songLabel.getStyleClass().add("song-label");
        artistLabel = new Label();
        artistLabel.getStyleClass().add("artist-label");

        timeSlider = new Slider();
        timeSlider.setMin(0);
        timeLabel = new Label("0:00 / 0:00");

        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER);

        prevButton = new Button("⏮");
        playButton = new Button("▶");
        nextButton = new Button("⏭");
        likeButton = new Button("\uD83D\uDC4D");
        Button stopButton = new Button("⏹");

        controls.getChildren().addAll(
                prevButton, playButton, nextButton, likeButton, stopButton
        );


        Slider volumeSlider = new Slider();
        volumeSlider.setMin(0);
        volumeSlider.setMax(100);
        volumeSlider.setValue(100);

        getChildren().addAll(
                albumArt,
                songLabel,
                artistLabel,
                timeSlider,
                timeLabel,
                controls,
                new HBox(10, new Label("🔈"), volumeSlider)
        );

        setupControlHandlers(volumeSlider);
        stopButton.setOnAction(e -> stop());
    }


    private void setupControlHandlers(Slider volumeSlider) {
        playButton.setOnAction(e -> togglePlay());

        nextButton.setOnAction(e -> {
            if (nextSongHandler != null) {
                nextSongHandler.onNextSong();
            }
        });

        prevButton.setOnAction(e -> {
            if (prevSongHandler != null) {
                prevSongHandler.onPrevSong();
            }
        });

        timeSlider.setOnMousePressed(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.pause();
            }
        });

        timeSlider.setOnMouseReleased(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.seek(Duration.seconds(timeSlider.getValue()));
                mediaPlayer.play();
            }
        });

        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(newVal.doubleValue() / 100.0);
            }
        });
    }

    public void likeSong(Song song) {
        if (!song.isLiked()) {
            likeButton.setText("\uD83D\uDC4D");
        } else {
            likeButton.setText("\uD83D\uDC4E");
        }
        likeButton.setOnAction(e -> {
            if (song != null) {
                if (song.isLiked()) {
                    song.setLiked(false);
                    likeButton.setText("\uD83D\uDC4D");
                } else {
                    song.setLiked(true);
                    likeButton.setText("\uD83D\uDC4E");
                }
            }
        });
    }

    public void playSong(Song song) {
        if (song == null) {
            System.out.println("Error: Cannot play null song");
            return;
        }

        if (song.getPreviewUrl() == null || song.getPreviewUrl().isEmpty()) {
            System.out.println("Error: Song has no preview URL: " + song.getName());
            return;
        }

        try {
            currentSong = song;

            if (mediaPlayer != null) {
                mediaPlayer.dispose();
            }

            songLabel.setText(song.getName());
            artistLabel.setText(song.getArtist());

            if (song.getImageUrl() != null && !song.getImageUrl().isEmpty()) {
                try {
                    albumArt.setImage(new Image(song.getImageUrl()));
                } catch (Exception e) {
                    System.out.println("Error loading image: " + e.getMessage());
                }
            }

            if (!song.getPreviewUrl().startsWith("http")) {
                System.out.println("Invalid URL format: " + song.getPreviewUrl());
                return;
            }

            Media media = new Media(song.getPreviewUrl());
            mediaPlayer = new MediaPlayer(media);

            mediaPlayer.setOnError(() -> {
                System.out.println("Media error: " + (mediaPlayer.getError() != null ?
                        mediaPlayer.getError().toString() : "Unknown error"));
            });

            mediaPlayer.setOnReady(() -> {
                if (mediaPlayer.getTotalDuration() != null) {
                    timeSlider.setMax(mediaPlayer.getTotalDuration().toSeconds());
                    updateTimeLabel(Duration.ZERO, mediaPlayer.getTotalDuration());

                    new Thread(() -> {
                        try {
                            Thread.sleep(1000);
                            javafx.application.Platform.runLater(() -> {
                                mediaPlayer.play();
                                playButton.setText("⏸");
                            });
                        } catch (InterruptedException ie) {
                            ie.printStackTrace();
                        }
                    }).start();
                }
            });

            mediaPlayer.currentTimeProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    timeSlider.setValue(newVal.toSeconds());
                    updateTimeLabel(newVal, mediaPlayer.getTotalDuration());
                }
            });

        } catch (Exception e) {
            System.out.println("Error creating media player: " + e.getMessage());
            e.printStackTrace();
        }
    }

    void togglePlay() {
        if (mediaPlayer != null) {
            if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
                playButton.setText("▶");
            } else {
                mediaPlayer.play();
                playButton.setText("⏸");
            }
        }
    }

    private void updateTimeLabel(Duration current, Duration total) {
        String currentTime = formatDuration(current);
        String totalTime = formatDuration(total);
        timeLabel.setText(currentTime + " / " + totalTime);
    }

    private String formatDuration(Duration duration) {
        long seconds = (long) duration.toSeconds();
        return String.format("%d:%02d", seconds / 60, seconds % 60);
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }
    }
}