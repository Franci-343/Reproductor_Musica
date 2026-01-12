package application;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class Controller implements Initializable {

    @FXML
    private TableView<MusicItem> musicTable;

    @FXML
    private TableColumn<MusicItem, String> colName;

    @FXML
    private TableColumn<MusicItem, String> colPath;

    @FXML
    private TableColumn<MusicItem, Long> colSize;

    @FXML
    private Label lblCurrentSong;

    @FXML
    private Button btnPrevious;

    @FXML
    private Button btnPlay;

    @FXML
    private Button btnPause;

    @FXML
    private Button btnStop;

    @FXML
    private Button btnNext;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label lblTime;

    private final ObservableList<MusicItem> data = FXCollections.observableArrayList();
    private MusicItem selectedSong = null;
    private MediaPlayer mediaPlayer = null;
    private boolean isPlaying = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configure columns
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPath.setCellValueFactory(new PropertyValueFactory<>("path"));
        colSize.setCellValueFactory(new PropertyValueFactory<>("size"));

        musicTable.setItems(data);

        // Listen for selection changes
        musicTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                boolean wasPlaying = isPlaying;
                selectedSong = newSelection;
                lblCurrentSong.setText(newSelection.getName());
                enablePlaybackControls(true);
                
                // Auto-play new song if music was already playing
                if (wasPlaying) {
                    handlePlay();
                }
            } else {
                selectedSong = null;
                lblCurrentSong.setText("Selecciona una canción");
                enablePlaybackControls(false);
            }
        });

        // Set up button actions
        btnPlay.setOnAction(e -> handlePlay());
        btnPause.setOnAction(e -> handlePause());
        btnStop.setOnAction(e -> handleStop());
        btnPrevious.setOnAction(e -> handlePrevious());
        btnNext.setOnAction(e -> handleNext());

        // Load music in background
        loadMusicAsync();
    }

    private void enablePlaybackControls(boolean enable) {
        btnPlay.setDisable(!enable);
        btnPause.setDisable(!enable);
        btnStop.setDisable(!enable);
        btnPrevious.setDisable(!enable);
        btnNext.setDisable(!enable);
    }

    private void handlePlay() {
        if (selectedSong != null) {
            if (mediaPlayer != null && isPlaying) {
                // Already playing, restart from beginning
                mediaPlayer.stop();
                mediaPlayer.play();
                return;
            }
            
            if (mediaPlayer != null && !isPlaying) {
                // Resume if paused
                mediaPlayer.play();
                isPlaying = true;
                lblCurrentSong.setText("▶ " + selectedSong.getName());
                return;
            }
            
            // Create new MediaPlayer
            try {
                File musicFile = new File(selectedSong.getPath());
                Media media = new Media(musicFile.toURI().toString());
                
                // Dispose old player if exists
                if (mediaPlayer != null) {
                    mediaPlayer.dispose();
                }
                
                mediaPlayer = new MediaPlayer(media);
                
                // Set up event handlers
                mediaPlayer.setOnReady(() -> {
                    Duration total = mediaPlayer.getTotalDuration();
                    updateTimeLabel(Duration.ZERO, total);
                });
                
                mediaPlayer.setOnPlaying(() -> {
                    isPlaying = true;
                    lblCurrentSong.setText("▶ " + selectedSong.getName());
                });
                
                mediaPlayer.setOnPaused(() -> {
                    isPlaying = false;
                    lblCurrentSong.setText("⏸ " + selectedSong.getName());
                });
                
                mediaPlayer.setOnStopped(() -> {
                    isPlaying = false;
                    lblCurrentSong.setText(selectedSong.getName());
                    progressBar.setProgress(0);
                });
                
                mediaPlayer.setOnEndOfMedia(() -> {
                    // Auto play next song
                    handleNext();
                });
                
                mediaPlayer.setOnError(() -> {
                    System.err.println("Media error: " + mediaPlayer.getError().getMessage());
                    lblCurrentSong.setText("Error: " + selectedSong.getName());
                });
                
                // Update progress bar and time label
                mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                    if (!mediaPlayer.getStatus().equals(MediaPlayer.Status.UNKNOWN)) {
                        Duration total = mediaPlayer.getTotalDuration();
                        if (total != null && total.greaterThan(Duration.ZERO)) {
                            progressBar.setProgress(newTime.toMillis() / total.toMillis());
                            updateTimeLabel(newTime, total);
                        }
                    }
                });
                
                // Start playback
                mediaPlayer.play();
                isPlaying = true;
                System.out.println("Playing: " + selectedSong.getName());
                
            } catch (Exception e) {
                System.err.println("Error playing file: " + e.getMessage());
                e.printStackTrace();
                lblCurrentSong.setText("Error al reproducir: " + selectedSong.getName());
            }
        }
    }

    private void handlePause() {
        if (mediaPlayer != null && isPlaying) {
            mediaPlayer.pause();
            isPlaying = false;
            System.out.println("Paused: " + selectedSong.getName());
        }
    }

    private void handleStop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            isPlaying = false;
            System.out.println("Stopped: " + selectedSong.getName());
        }
    }

    private void handlePrevious() {
        int currentIndex = musicTable.getSelectionModel().getSelectedIndex();
        if (currentIndex > 0) {
            musicTable.getSelectionModel().select(currentIndex - 1);
            musicTable.scrollTo(currentIndex - 1);
            System.out.println("Previous song");
        }
    }

    private void handleNext() {
        int currentIndex = musicTable.getSelectionModel().getSelectedIndex();
        if (currentIndex < data.size() - 1) {
            musicTable.getSelectionModel().select(currentIndex + 1);
            musicTable.scrollTo(currentIndex + 1);
            System.out.println("Next song");
        }
    }

    private void updateTimeLabel(Duration current, Duration total) {
        if (current != null && total != null && lblTime != null) {
            String currentStr = formatDuration(current);
            String totalStr = formatDuration(total);
            lblTime.setText(currentStr + " / " + totalStr);
        }
    }

    private String formatDuration(Duration duration) {
        if (duration == null) return "00:00";
        int seconds = (int) duration.toSeconds();
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void loadMusicAsync() {
        Task<List<MusicItem>> task = new Task<List<MusicItem>>() {
            @Override
            protected List<MusicItem> call() throws Exception {
                List<Path> paths = MusicFinder.findMusicInCommonDirs();
                return paths.stream().map(p -> {
                    long size = 0L;
                    try {
                        size = Files.size(p);
                    } catch (IOException ignored) {
                    }
                    return new MusicItem(p.getFileName().toString(), p.toString(), size);
                }).collect(Collectors.toList());
            }
        };

        task.setOnSucceeded(e -> {
            List<MusicItem> items = task.getValue();
            data.clear();
            data.addAll(items);
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            ex.printStackTrace();
        });

        Thread th = new Thread(task, "music-finder");
        th.setDaemon(true);
        th.start();
    }

    // Simple model for table rows
    public static class MusicItem {
        private final StringProperty name = new SimpleStringProperty();
        private final StringProperty path = new SimpleStringProperty();
        private final LongProperty size = new SimpleLongProperty();

        public MusicItem(String name, String path, long size) {
            this.name.set(name);
            this.path.set(path);
            this.size.set(size);
        }

        public String getName() { return name.get(); }
        public StringProperty nameProperty() { return name; }

        public String getPath() { return path.get(); }
        public StringProperty pathProperty() { return path; }

        public long getSize() { return size.get(); }
        public LongProperty sizeProperty() { return size; }
    }
}