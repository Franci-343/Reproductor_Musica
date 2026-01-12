package application;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.AudioSpectrumListener;

import java.util.Random;

/**
 * Audio visualizer that displays animated bars similar to classic music players.
 * Shows frequency spectrum analysis of the playing audio.
 */
public class AudioVisualizer extends Canvas {
    
    private static final int NUM_BARS = 32;
    private static final double BAR_WIDTH_RATIO = 0.8;
    private static final Color BAR_COLOR = Color.rgb(0, 200, 255);
    private static final Color BAR_GRADIENT_COLOR = Color.rgb(0, 100, 200);
    
    private final float[] magnitudes;
    private final float[] phases;
    private MediaPlayer mediaPlayer;
    private AnimationTimer animationTimer;
    private final Random random = new Random();
    private boolean isPlaying = false;
    
    public AudioVisualizer(double width, double height) {
        super(width, height);
        this.magnitudes = new float[NUM_BARS];
        this.phases = new float[NUM_BARS];
        
        // Initialize with zeros
        for (int i = 0; i < NUM_BARS; i++) {
            magnitudes[i] = 0;
            phases[i] = 0;
        }
        
        // Start animation timer for rendering
        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                render();
            }
        };
        animationTimer.start();
        
        // Initial render
        render();
    }
    
    /**
     * Attach this visualizer to a MediaPlayer to display its audio spectrum
     */
    public void attachMediaPlayer(MediaPlayer player) {
        // Detach from previous player if exists
        if (mediaPlayer != null) {
            mediaPlayer.setAudioSpectrumListener(null);
        }
        
        this.mediaPlayer = player;
        
        if (player != null) {
            // Configure spectrum listener
            player.setAudioSpectrumNumBands(NUM_BARS);
            player.setAudioSpectrumInterval(0.05); // Update every 50ms
            
            player.setAudioSpectrumListener(new AudioSpectrumListener() {
                @Override
                public void spectrumDataUpdate(double timestamp, double duration,
                                               float[] newMagnitudes, float[] newPhases) {
                    // Copy the spectrum data
                    System.arraycopy(newMagnitudes, 0, magnitudes, 0, 
                                   Math.min(newMagnitudes.length, NUM_BARS));
                    System.arraycopy(newPhases, 0, phases, 0, 
                                   Math.min(newPhases.length, NUM_BARS));
                    isPlaying = true;
                }
            });
        }
    }
    
    /**
     * Detach from current MediaPlayer
     */
    public void detach() {
        if (mediaPlayer != null) {
            mediaPlayer.setAudioSpectrumListener(null);
            mediaPlayer = null;
        }
        isPlaying = false;
        // Reset magnitudes
        for (int i = 0; i < NUM_BARS; i++) {
            magnitudes[i] = 0;
        }
    }
    
    /**
     * Set playing state (used when paused/stopped)
     */
    public void setPlaying(boolean playing) {
        this.isPlaying = playing;
        if (!playing) {
            // Smoothly reduce bars to zero when stopped
            for (int i = 0; i < NUM_BARS; i++) {
                magnitudes[i] *= 0.9f;
            }
        }
    }
    
    /**
     * Render the visualization
     */
    private void render() {
        GraphicsContext gc = getGraphicsContext2D();
        double width = getWidth();
        double height = getHeight();
        
        // Clear background
        gc.setFill(Color.rgb(20, 20, 30));
        gc.fillRect(0, 0, width, height);
        
        // Calculate bar dimensions
        double barWidth = (width / NUM_BARS) * BAR_WIDTH_RATIO;
        double spacing = (width / NUM_BARS) * (1 - BAR_WIDTH_RATIO);
        
        // Draw bars
        for (int i = 0; i < NUM_BARS; i++) {
            double x = i * (barWidth + spacing) + spacing / 2;
            
            // Convert magnitude (in dB, typically -60 to 0) to height
            // magnitudes are in dB, typically ranging from -60 to 0
            float magnitude = magnitudes[i];
            
            // Normalize magnitude: -60dB = 0, 0dB = 1
            double normalizedMagnitude = (magnitude + 60) / 60.0;
            normalizedMagnitude = Math.max(0, Math.min(1, normalizedMagnitude));
            
            // Add some smoothing and minimum height
            if (!isPlaying) {
                normalizedMagnitude *= 0.95; // Decay when not playing
                magnitudes[i] *= 0.9f;
            }
            
            double barHeight = normalizedMagnitude * height * 0.9;
            
            // Add minimum visible height when playing
            if (isPlaying && barHeight < 2) {
                barHeight = 2 + random.nextDouble() * 5;
            }
            
            double y = height - barHeight;
            
            // Draw gradient bar
            Color topColor = BAR_COLOR;
            Color bottomColor = BAR_GRADIENT_COLOR;
            
            // Create gradient effect manually
            int steps = (int) Math.max(1, barHeight / 2);
            for (int s = 0; s < steps; s++) {
                double ratio = (double) s / steps;
                Color color = topColor.interpolate(bottomColor, ratio);
                gc.setFill(color);
                
                double segmentHeight = barHeight / steps;
                gc.fillRect(x, y + s * segmentHeight, barWidth, segmentHeight + 1);
            }
            
            // Draw reflection (subtle)
            gc.setGlobalAlpha(0.2);
            gc.setFill(BAR_GRADIENT_COLOR);
            double reflectionHeight = Math.min(barHeight * 0.3, height - barHeight - y);
            gc.fillRect(x, height - barHeight + barHeight, barWidth, reflectionHeight);
            gc.setGlobalAlpha(1.0);
        }
    }
    
    /**
     * Clean up resources
     */
    public void dispose() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
        detach();
    }
}
