

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;

public class WarningBuzzer {

    private boolean isOn = false;

    public WarningBuzzer() {

        EPServiceProvider engine = EPServiceProviderManager.getDefaultProvider();


        engine.getEPAdministrator().getConfiguration().addEventType(SpeedEvent.class);

        String epl = "select speed, limit from SpeedEvent where speed > limit + 5 and speed <= limit + 10";

        EPStatement statement = engine.getEPAdministrator().createEPL(epl);

        statement.setSubscriber(new Object() {
            public void update(double speed, double limit) {
                activateBuzzer(speed, limit);
            }
        });
    }

    // Send a speed event to Esper
    public void sendSpeedEvent(double speed, double limit) {
        EPServiceProvider engine = EPServiceProviderManager.getDefaultProvider();
        engine.getEPRuntime().sendEvent(new SpeedEvent(speed, limit));
    }

    // Activate the buzzer if not already on
    private void activateBuzzer(double speed, double limit) {
        if (!isOn) {
            isOn = true;
            System.out.println("WARNING BUZZER: Speed = " + speed + " km/h, Limit = " + limit + " km/h");
            System.out.println("WARNING BUZZER: Activated due to overspeed!");
        }
    }

    public void stopBuzzer() {
        if (isOn) {
            isOn = false;
            System.out.println("WARNING BUZZER: Deactivated.");
        }
    }


    public static class SpeedEvent {
        private double speed;
        private double limit;

        public SpeedEvent(double speed, double limit) {
            this.speed = speed;
            this.limit = limit;
        }

        public double getSpeed() {
            return speed;
        }

        public double getLimit() {
            return limit;
        }
    }
}


