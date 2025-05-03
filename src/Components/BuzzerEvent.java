package Components;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

import javax.sound.sampled.*;
import java.io.File;

public class BuzzerEvent implements Runnable{

    private boolean isOn = false;
    String audioFilePath = "src/audio/buzzer.wav";
    AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(audioFilePath));
    Clip clip = AudioSystem.getClip();

    public BuzzerEvent() throws LineUnavailableException, UnsupportedAudioFileException, IOException {
        EPServiceProvider engine = EPServiceProviderManager.getDefaultProvider();

        // Register Components.BuzzerEvent (boolean-based event for activation)
        engine.getEPAdministrator().getConfiguration().addEventType(WarningBuzzer.class);

        // EPL: Listen for activation events where isActive = true
        String epl = "select isActive from WarningBuzzer where isActive = true";

        EPStatement statement = engine.getEPAdministrator().createEPL(epl);
        statement.setSubscriber(new Object() {
            public void update(boolean isActive) {
                activateBuzzer();
            }
        });
    }

    public void activateBuzzer() {
        if (!isOn) {
            isOn = true;
            System.out.println("WARNING BUZZER: Activated due to external trigger!");
        }
        try {
            clip.open(audioInputStream);
            clip.start();
        } catch (IOException | LineUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    public void stopBuzzer() {
        if (isOn) {
            isOn = false;
            System.out.println("WARNING BUZZER: Deactivated.");
        }
        clip.close();
        clip.stop();
    }

    @Override
    public void run() {
        EPServiceProvider engine = EPServiceProviderManager.getDefaultProvider();
        while (true) {
            engine.getEPRuntime().sendEvent(new WarningBuzzer(isOn));
            try {
                Thread.sleep(1000); // Sleep for 1 second before sending the next event
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break; // Exit the loop if interrupted
            }
        }

    }
}
