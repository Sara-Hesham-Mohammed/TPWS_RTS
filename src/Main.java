

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class Main {

    public static void main(String[] args) {

        TPWSController controller = new TPWSController();

        // Disable logging from Esper for cleaner output
        Logger.getRootLogger().setLevel(Level.OFF);

        // Get Esper engine reference
        EPServiceProvider engine = EPServiceProviderManager.getDefaultProvider();

        // Registering the events for Esper engine (SpeedSensor event)
        engine.getEPAdministrator().getConfiguration().addEventType(SpeedSensor.class);

        // SENSORS INITIALIZATION
        SpeedSensor speedSensor = new SpeedSensor(1, 100);  // Speed sensor initialized with ID and limit
        GPSModule gpsModule = new GPSModule(37.7749, -122.4194, 60); // Example initial GPS data
        Screen screen = new Screen();

        // Initialize WarningBuzzer
        WarningBuzzer buzzer = new WarningBuzzer();

        // Run sensors in separate threads
        Thread speedThread = controller.EsperRun(engine, speedSensor);

        // Test the sensors
        System.out.println("=== Testing Speed Sensor WITH THREAD ===");
        speedThread.start();

        // Simulate GPS updates and display
        System.out.println("=== Simulating GPS and Screen Updates ===");
        gpsModule.updateGPS(37.7750, -122.4195, 80);
        screen.displaySpeed(gpsModule.getSpeed());
        screen.displaySignalStatus("Green");

        // Triggering the buzzer based on sensor readings
        System.out.println("=== Speed Sensor Readings and Buzzer ===");
        try {
            // Simulate speed readings to trigger the buzzer
            buzzer.sendSpeedEvent(76, 70);  // Speed: 76 km/h, Limit: 70 km/h (Exceeds by 6 km/h)
            screen.displayWarning("Speed Limit Exceeded!");
            Thread.sleep(500);  // Simulate some delay

            buzzer.sendSpeedEvent(72, 70);  // Speed: 72 km/h, Limit: 70 km/h (Exceeds by 2 km/h)
            screen.displayWarning("Check Speed");
            Thread.sleep(500);  // Simulate some delay

            // Stop buzzer and update screen
            buzzer.stopBuzzer();  // Stop buzzer after testing
            screen.displayInfo("Buzzer stopped.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}


