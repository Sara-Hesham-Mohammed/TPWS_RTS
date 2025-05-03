

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;

public class WarningBuzzer {

    private boolean isOn = false;

    public WarningBuzzer() {
        EPServiceProvider engine = EPServiceProviderManager.getDefaultProvider();

        // Register BuzzerEvent (boolean-based event for activation)
        engine.getEPAdministrator().getConfiguration().addEventType(BuzzerEvent.class);

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

    public void sendActivationEvent(boolean isActive) {
        EPServiceProvider engine = EPServiceProviderManager.getDefaultProvider();
        engine.getEPRuntime().sendEvent(new BuzzerEvent(isActive));
    }


    public static class BuzzerEvent {
        private boolean isActive;

        public BuzzerEvent(boolean isActive) {
            this.isActive = isActive;
        }

        public boolean isActive() {
            return isActive;
        }
    }
}
