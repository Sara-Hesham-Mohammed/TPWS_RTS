package Components;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class WarningBuzzer {
    private boolean isOn;
    String audioFilePath = "src/audio/buzzer.wav";
    AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(audioFilePath));
    Clip clip = AudioSystem.getClip();

    public WarningBuzzer() throws UnsupportedAudioFileException, IOException, LineUnavailableException {
    }

    public void activateBuzzer() {
        isOn = true;
        try {
            clip.open(audioInputStream);
            clip.start();
        } catch (IOException | LineUnavailableException e) {
            throw new RuntimeException(e);
        }

    }

    public void stopBuzzer() {
        isOn = false;
        clip.close();
        clip.stop();
    }
}