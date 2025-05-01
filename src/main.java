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
        //double speed = random.nextDouble();
        //for testing buzzer
        TPWSController controller = new TPWSController("TPWS_1",engine);

        // Registering the events
        engine.getEPAdministrator().getConfiguration().addEventType(SpeedSensor.class);
        System.out.println("=== Testing BUZZER ===");
        controller.activateWarningSound();
        System.out.println("=== DEACTIVATING BUZZER BY REDUCING SPEED ===");
        controller.reduceSpeed();
        //SENSORS INITIALIZATION
        Sensors.SpeedSensor speedSensor = new SpeedSensor(1, 100);
//        Sensors.DistanceSensor distanceSensor = new Sensors.DistanceSensor(2);
//        Sensors.WeatherSensor weatherSensor = new Sensors.WeatherSensor(3);
        Sensors.BrakeStatusSensor brakeStatusSensor = new Sensors.BrakeStatusSensor(4,0);


        Thread speedThread = controller.getSensorData(engine,speedSensor,250);
//        Thread distanceThread = controller.EsperRun(engine,distanceSensor);
        //Thread weatherThread = controller.EsperRun(engine,weatherSensor);
        Thread brakeThread = controller.getSensorData(engine,brakeStatusSensor,100);

        // Test each sensor

        System.out.println("=== Testing Speed Sensors.Sensor WITH THREADS ===");
        speedThread.start();
    }
}