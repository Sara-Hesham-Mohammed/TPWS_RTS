import Components.TrackSideTransmitterEvent;
import Sensors.SpeedSensor;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.Random;

public class main {

    private static final Random random = new Random();

    public static void main(String[] args) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        // Disable logging
        Logger.getRootLogger().setLevel(Level.OFF);

        // Get engine reference
        EPServiceProvider engine = EPServiceProviderManager.getDefaultProvider();
        TPWSController controller = new TPWSController("TPWS_1",engine);

<<<<<<< HEAD

=======
        // Registering the events
        engine.getEPAdministrator().getConfiguration().addEventType(SpeedSensor.class);
        engine.getEPAdministrator().getConfiguration().addEventType(WarningBuzzer.BuzzerEvent.class); // <-- Register new activation event

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


        GPSModule gpsModule = new GPSModule(37.7749, -122.4194, 60);
        Screen screen = new Screen();

        WarningBuzzer buzzer = new WarningBuzzer();

        gpsModule.updateGPS(37.7750, -122.4195, 80);
        screen.displaySpeed(gpsModule.getSpeed());
        screen.displaySignalStatus("Green");


        try {
            buzzer.sendActivationEvent(true); // Manual activation logic
            screen.displayWarning("Manual activation of buzzer!");
            Thread.sleep(1000);

            buzzer.stopBuzzer();
            screen.displayInfo("Buzzer stopped.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
>>>>>>> main
    }
}
