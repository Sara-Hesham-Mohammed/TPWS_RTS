package Components;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class WarningBuzzer {
    private boolean isActive;
    String audioFilePath = "src/audio/buzzer.wav";
    AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(audioFilePath));
    Clip clip = AudioSystem.getClip();

    public WarningBuzzer(boolean isActive) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        this.isActive = isActive;
    }

    public void activateBuzzer() {
        isActive = true;
        try {
            clip.open(audioInputStream);
            clip.start();
        } catch (IOException | LineUnavailableException e) {
            throw new RuntimeException(e);
        }

    }

    public void stopBuzzer() {
        isActive = false;
        clip.close();
        clip.stop();
    }
}