import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class main {

    public static void main(String[] args) {

        TPWSController controller = new TPWSController();
        // Disable logging
        Logger.getRootLogger().setLevel(Level.OFF);

        // Get engine reference
        EPServiceProvider engine = EPServiceProviderManager.getDefaultProvider();

        // Registering the events
        engine.getEPAdministrator().getConfiguration().addEventType(SpeedSensor.class);
        engine.getEPAdministrator().getConfiguration().addEventType(WarningBuzzer.SpeedEvent.class);

        // SENSORS INITIALIZATION
        SpeedSensor speedSensor = new SpeedSensor(1, 100);
//        DistanceSensor distanceSensor = new DistanceSensor(2);
//        WeatherSensor weatherSensor = new WeatherSensor(3);
//        BrakeStatusSensor brakeSensor = new BrakeStatusSensor(4);

        Thread speedThread = controller.EsperRun(engine, speedSensor);
//        Thread distanceThread = controller.EsperRun(engine, distanceSensor);
//        Thread weatherThread = controller.EsperRun(engine, weatherSensor);
//        Thread brakeThread = controller.EsperRun(engine, brakeSensor);

        // Test each sensor
        System.out.println("=== Testing Speed Sensor WITH THREADS ===");
        speedThread.start();

        // Initialize GPSModule and Screen
        GPSModule gpsModule = new GPSModule(37.7749, -122.4194, 60);
        Screen screen = new Screen();

        // Initialize and test WarningBuzzer
        WarningBuzzer buzzer = new WarningBuzzer();

        // Simulate GPS update and display info
        gpsModule.updateGPS(37.7750, -122.4195, 80);
        screen.displaySpeed(gpsModule.getSpeed());
        screen.displaySignalStatus("Green");

        // Trigger WarningBuzzer based on simulated speeds
        try {
            buzzer.sendSpeedEvent(76, 70); // Exceeds limit by 6 km/h
            screen.displayWarning("Speed Limit Exceeded!");
            Thread.sleep(500);

            buzzer.sendSpeedEvent(72, 70); // Exceeds limit by 2 km/h (may or may not trigger)
            screen.displayWarning("Check Speed");
            Thread.sleep(500);

            buzzer.stopBuzzer();
            screen.displayInfo("Buzzer stopped.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}