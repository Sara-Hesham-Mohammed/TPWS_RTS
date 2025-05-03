

import Components.WarningBuzzer;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public class BuzzerEvent {

    private boolean isOn = false;

    public BuzzerEvent() {
        EPServiceProvider engine = EPServiceProviderManager.getDefaultProvider();

        // Register BuzzerEvent (boolean-based event for activation)
        engine.getEPAdministrator().getConfiguration().addEventType(WarningBuzzer.class);

        // EPL: Listen for activation events where isActive = true
        String epl = "select isActive from BuzzerEvent where isActive = true";

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
    }

    public void stopBuzzer() {
        if (isOn) {
            isOn = false;
            System.out.println("WARNING BUZZER: Deactivated.");
        }
    }

    public void sendActivationEvent(boolean isActive) throws UnsupportedAudioFileException, LineUnavailableException, IOException {
        EPServiceProvider engine = EPServiceProviderManager.getDefaultProvider();
        engine.getEPRuntime().sendEvent(new WarningBuzzer(isActive));
    }


}
