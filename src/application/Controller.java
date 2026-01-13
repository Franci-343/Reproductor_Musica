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
import javafx.scene.control.Slider;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.HBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.Cursor;
import javafx.stage.Stage;
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

    @FXML
    private Slider volumeSlider;

    @FXML
    private Label lblVolume;

    @FXML
    private Canvas visualizerCanvas;

    @FXML
    private HBox titleBar;
    
    @FXML
    private BorderPane root;

    @FXML
    private Button btnMinimize;

    @FXML
    private Button btnMaximize;

    @FXML
    private Button btnClose;

    private final ObservableList<MusicItem> data = FXCollections.observableArrayList();
    private MusicItem selectedSong = null;
    private MediaPlayer mediaPlayer = null;
    private boolean isPlaying = false;
    private AudioVisualizer audioVisualizer = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configure columns
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPath.setCellValueFactory(new PropertyValueFactory<>("path"));
        colSize.setCellValueFactory(new PropertyValueFactory<>("size"));

        musicTable.setItems(data);

        // Set up window controls (minimize, maximize, close)
        setupWindowControls();
        
        // Set up window resize functionality
        setupWindowResize();
        
        // Set up responsive behavior
        setupResponsiveBehavior();

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

        // Set up volume slider
        if (volumeSlider != null && lblVolume != null) {
            volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                double volume = newVal.doubleValue();
                lblVolume.setText(String.format("%.0f%%", volume * 100));
                if (mediaPlayer != null) {
                    mediaPlayer.setVolume(volume);
                }
            });
            // Initialize volume label
            lblVolume.setText(String.format("%.0f%%", volumeSlider.getValue() * 100));
        }

        // Set up interactive progress bar (click to seek)
        if (progressBar != null) {
            progressBar.setOnMouseClicked(event -> {
                if (mediaPlayer != null && selectedSong != null) {
                    // Calculate the percentage clicked
                    double mouseX = event.getX();
                    double width = progressBar.getWidth();
                    double percentage = mouseX / width;
                    
                    // Clamp between 0 and 1
                    percentage = Math.max(0, Math.min(1, percentage));
                    
                    // Seek to that position
                    Duration totalDuration = mediaPlayer.getTotalDuration();
                    if (totalDuration != null && !totalDuration.isUnknown()) {
                        Duration seekTime = totalDuration.multiply(percentage);
                        mediaPlayer.seek(seekTime);
                        System.out.println("Seeking to: " + formatDuration(seekTime) + " / " + formatDuration(totalDuration));
                    }
                }
            });
        }

        // Initialize audio visualizer
        if (visualizerCanvas != null) {
            audioVisualizer = new AudioVisualizer(
                visualizerCanvas.getWidth(), 
                visualizerCanvas.getHeight()
            );
            // Replace the FXML canvas with our custom AudioVisualizer
            if (visualizerCanvas.getParent() instanceof javafx.scene.layout.Pane) {
                javafx.scene.layout.Pane parent = (javafx.scene.layout.Pane) visualizerCanvas.getParent();
                int index = parent.getChildren().indexOf(visualizerCanvas);
                parent.getChildren().remove(visualizerCanvas);
                parent.getChildren().add(index, audioVisualizer);
            }
        }

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
                
                // Set initial volume from slider
                if (volumeSlider != null) {
                    mediaPlayer.setVolume(volumeSlider.getValue());
                }
                
                // Connect audio visualizer
                if (audioVisualizer != null) {
                    audioVisualizer.attachMediaPlayer(mediaPlayer);
                }
                
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
            if (audioVisualizer != null) {
                audioVisualizer.setPlaying(false);
            }
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

    /**
     * Set up responsive behavior based on window size
     */
    private void setupResponsiveBehavior() {
        if (root == null) return;
        
        Platform.runLater(() -> {
            Stage stage = (Stage) root.getScene().getWindow();
            if (stage == null) return;
            
            // Initial size check
            updateResponsiveStyles(stage.getWidth(), stage.getHeight());
            
            // Listen for width changes
            stage.widthProperty().addListener((obs, oldVal, newVal) -> {
                updateResponsiveStyles(newVal.doubleValue(), stage.getHeight());
            });
            
            // Listen for height changes
            stage.heightProperty().addListener((obs, oldVal, newVal) -> {
                updateResponsiveStyles(stage.getWidth(), newVal.doubleValue());
            });
        });
    }
    
    /**
     * Update CSS classes based on window dimensions
     */
    private void updateResponsiveStyles(double width, double height) {
        if (root == null) return;
        
        // Remove all size-mode classes
        root.getStyleClass().removeAll("compact-mode", "standard-mode", "comfortable-mode");
        root.getStyleClass().removeAll("compact-height", "standard-height", "comfortable-height");
        
        // Apply width-based classes
        if (width < 800) {
            root.getStyleClass().add("compact-mode");
        } else if (width < 1100) {
            root.getStyleClass().add("standard-mode");
        } else {
            root.getStyleClass().add("comfortable-mode");
        }
        
        // Apply height-based classes
        if (height < 600) {
            root.getStyleClass().add("compact-height");
        } else if (height < 750) {
            root.getStyleClass().add("standard-height");
        } else {
            root.getStyleClass().add("comfortable-height");
        }
    }

    /**
     * Set up custom window controls for undecorated window
     */
    private void setupWindowControls() {
        if (titleBar == null) return;
        
        // Variables for dragging
        final double[] xOffset = {0};
        final double[] yOffset = {0};
        
        // Make title bar draggable
        titleBar.setOnMousePressed(event -> {
            Stage stage = (Stage) titleBar.getScene().getWindow();
            xOffset[0] = event.getSceneX();
            yOffset[0] = event.getSceneY();
        });
        
        titleBar.setOnMouseDragged(event -> {
            Stage stage = (Stage) titleBar.getScene().getWindow();
            stage.setX(event.getScreenX() - xOffset[0]);
            stage.setY(event.getScreenY() - yOffset[0]);
        });
        
        // Minimize button
        if (btnMinimize != null) {
            btnMinimize.setOnAction(e -> {
                Stage stage = (Stage) btnMinimize.getScene().getWindow();
                stage.setIconified(true);
            });
        }
        
        // Maximize/Restore button
        if (btnMaximize != null) {
            btnMaximize.setOnAction(e -> {
                Stage stage = (Stage) btnMaximize.getScene().getWindow();
                if (stage.isMaximized()) {
                    stage.setMaximized(false);
                    btnMaximize.setText("□");
                } else {
                    stage.setMaximized(true);
                    btnMaximize.setText("❐");
                }
            });
        }
        
        // Close button
        if (btnClose != null) {
            btnClose.setOnAction(e -> {
                // Clean up resources
                if (mediaPlayer != null) {
                    mediaPlayer.dispose();
                }
                if (audioVisualizer != null) {
                    audioVisualizer.dispose();
                }
                Stage stage = (Stage) btnClose.getScene().getWindow();
                stage.close();
                Platform.exit();
            });
        }
    }
    
    private void setupWindowResize() {
        if (root == null) return;
        
        final double RESIZE_BORDER = 8.0;
        final double[] xOffset = {0};
        final double[] yOffset = {0};
        final boolean[] isResizing = {false};
        final String[] resizeDirection = {""};
        
        root.setOnMouseMoved(event -> {
            if (isResizing[0]) return;
            
            double mouseX = event.getX();
            double mouseY = event.getY();
            double width = root.getWidth();
            double height = root.getHeight();
            
            boolean isLeft = mouseX < RESIZE_BORDER;
            boolean isRight = mouseX > width - RESIZE_BORDER;
            boolean isTop = mouseY < RESIZE_BORDER;
            boolean isBottom = mouseY > height - RESIZE_BORDER;
            
            if (isTop && isLeft) {
                root.setCursor(Cursor.NW_RESIZE);
            } else if (isTop && isRight) {
                root.setCursor(Cursor.NE_RESIZE);
            } else if (isBottom && isLeft) {
                root.setCursor(Cursor.SW_RESIZE);
            } else if (isBottom && isRight) {
                root.setCursor(Cursor.SE_RESIZE);
            } else if (isLeft) {
                root.setCursor(Cursor.W_RESIZE);
            } else if (isRight) {
                root.setCursor(Cursor.E_RESIZE);
            } else if (isTop) {
                root.setCursor(Cursor.N_RESIZE);
            } else if (isBottom) {
                root.setCursor(Cursor.S_RESIZE);
            } else {
                root.setCursor(Cursor.DEFAULT);
            }
        });
        
        root.setOnMousePressed(event -> {
            Stage stage = (Stage) root.getScene().getWindow();
            double mouseX = event.getX();
            double mouseY = event.getY();
            double width = root.getWidth();
            double height = root.getHeight();
            
            boolean isLeft = mouseX < RESIZE_BORDER;
            boolean isRight = mouseX > width - RESIZE_BORDER;
            boolean isTop = mouseY < RESIZE_BORDER;
            boolean isBottom = mouseY > height - RESIZE_BORDER;
            
            if (isTop && isLeft) {
                resizeDirection[0] = "NW";
                isResizing[0] = true;
            } else if (isTop && isRight) {
                resizeDirection[0] = "NE";
                isResizing[0] = true;
            } else if (isBottom && isLeft) {
                resizeDirection[0] = "SW";
                isResizing[0] = true;
            } else if (isBottom && isRight) {
                resizeDirection[0] = "SE";
                isResizing[0] = true;
            } else if (isLeft) {
                resizeDirection[0] = "W";
                isResizing[0] = true;
            } else if (isRight) {
                resizeDirection[0] = "E";
                isResizing[0] = true;
            } else if (isTop) {
                resizeDirection[0] = "N";
                isResizing[0] = true;
            } else if (isBottom) {
                resizeDirection[0] = "S";
                isResizing[0] = true;
            }
            
            if (isResizing[0]) {
                xOffset[0] = event.getScreenX();
                yOffset[0] = event.getScreenY();
            }
        });
        
        root.setOnMouseDragged(event -> {
            if (!isResizing[0]) return;
            
            Stage stage = (Stage) root.getScene().getWindow();
            double deltaX = event.getScreenX() - xOffset[0];
            double deltaY = event.getScreenY() - yOffset[0];
            
            String direction = resizeDirection[0];
            
            if (direction.contains("E")) {
                stage.setWidth(Math.max(stage.getMinWidth(), stage.getWidth() + deltaX));
            }
            if (direction.contains("W")) {
                double newWidth = Math.max(stage.getMinWidth(), stage.getWidth() - deltaX);
                if (newWidth >= stage.getMinWidth()) {
                    stage.setX(stage.getX() + deltaX);
                    stage.setWidth(newWidth);
                }
            }
            if (direction.contains("S")) {
                stage.setHeight(Math.max(stage.getMinHeight(), stage.getHeight() + deltaY));
            }
            if (direction.contains("N")) {
                double newHeight = Math.max(stage.getMinHeight(), stage.getHeight() - deltaY);
                if (newHeight >= stage.getMinHeight()) {
                    stage.setY(stage.getY() + deltaY);
                    stage.setHeight(newHeight);
                }
            }
            
            xOffset[0] = event.getScreenX();
            yOffset[0] = event.getScreenY();
        });
        
        root.setOnMouseReleased(event -> {
            isResizing[0] = false;
            resizeDirection[0] = "";
            root.setCursor(Cursor.DEFAULT);
        });
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