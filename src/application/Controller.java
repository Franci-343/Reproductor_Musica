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
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

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
                selectedSong = newSelection;
                lblCurrentSong.setText(newSelection.getName());
                enablePlaybackControls(true);
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
            System.out.println("Playing: " + selectedSong.getName());
            // TODO: Implement actual playback logic here
            lblCurrentSong.setText("▶ " + selectedSong.getName());
        }
    }

    private void handlePause() {
        if (selectedSong != null) {
            System.out.println("Paused: " + selectedSong.getName());
            // TODO: Implement actual pause logic here
            lblCurrentSong.setText("⏸ " + selectedSong.getName());
        }
    }

    private void handleStop() {
        if (selectedSong != null) {
            System.out.println("Stopped: " + selectedSong.getName());
            // TODO: Implement actual stop logic here
            lblCurrentSong.setText(selectedSong.getName());
            progressBar.setProgress(0);
            lblTime.setText("00:00 / 00:00");
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