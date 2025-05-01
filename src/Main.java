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

        //double speed = random.nextDouble();
        //for testing buzzer
        double speed = 100;
        TPWSController controller = new TPWSController("TPWS_1",speed);


        // Disable logging
        Logger.getRootLogger().setLevel(Level.OFF);


        System.out.println("=== Testing BUZZER ===");
        controller.activateWarningSound();
        System.out.println("=== DEACTIVATING BUZZER BY REDUCING SPEED ===");
        controller.reduceSpeed();
        // Get engine reference
        EPServiceProvider engine = EPServiceProviderManager.getDefaultProvider();
        // Registering the events
        engine.getEPAdministrator().getConfiguration().addEventType(SpeedSensor.class);

        //SENSORS INITIALIZATION
        Sensors.SpeedSensor speedSensor = new SpeedSensor(1, 100);
//        Sensors.DistanceSensor distanceSensor = new Sensors.DistanceSensor(2);
//        Sensors.WeatherSensor weatherSensor = new Sensors.WeatherSensor(3);
        Sensors.BrakeStatusSensor brakeStatusSensor = new Sensors.BrakeStatusSensor(4,0);


        Thread speedThread = controller.EsperRun(engine,speedSensor,250);
//        Thread distanceThread = controller.EsperRun(engine,distanceSensor);
        //Thread weatherThread = controller.EsperRun(engine,weatherSensor);
        Thread brakeThread = controller.EsperRun(engine,brakeStatusSensor,100);

        // Test each sensor

        System.out.println("=== Testing Speed Sensors.Sensor WITH THREADS ===");
        speedThread.start();

        // Test each sensor
//        System.out.println("=== Testing Distance Sensors.Sensor WITH THREADS ===");
//        distanceThread.start();

//        // Test each sensor
//        System.out.println("=== Testing Weather Sensors.Sensor WITH THREADS ===");
//        weatherThread.start();

//
//        // Test each sensor
//        System.out.println("=== Testing Brake Sensors.Sensor WITH THREADS ===");
//        brakeThread.start();


//        // Test deactivation
//        System.out.println("\n=== Testing Deactivation ===");
//        speedSensor.deactivate();
//        System.out.println("Speed reading (inactive): " + speedSensor.measureSpeed());
//        speedSensor.activate();
//        System.out.println("Speed reading (reactivated): " + speedSensor.measureSpeed());
    }

}