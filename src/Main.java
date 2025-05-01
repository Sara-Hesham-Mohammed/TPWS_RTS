import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    public static void main(String[] args) {

        TPWSController controller = new TPWSController();
        // Disable logging
        Logger.getRootLogger().setLevel(Level.OFF);
        // Get engine reference
        EPServiceProvider engine = EPServiceProviderManager.getDefaultProvider();
        // Registering the events
        engine.getEPAdministrator().getConfiguration().addEventType(SpeedSensor.class);

        //SENSORS INITIALIZATION
        SpeedSensor speedSensor = new SpeedSensor(1, 100);
//        DistanceSensor distanceSensor = new DistanceSensor(2);
//        WeatherSensor weatherSensor = new WeatherSensor(3);
//        BrakeStatusSensor brakeSensor = new BrakeStatusSensor(4);

        Thread speedThread = controller.EsperRun(engine,speedSensor);
//        Thread distanceThread = controller.EsperRun(engine,distanceSensor);
//        Thread weatherThread = controller.EsperRun(engine,weatherSensor);
//        Thread brakeThread = controller.EsperRun(engine,brakeSensor);

        // Test each sensor
        System.out.println("=== Testing Speed Sensor WITH THREADS ===");
        speedThread.start();

        // Test each sensor
//        System.out.println("=== Testing Distance Sensor WITH THREADS ===");
//        distanceThread.start();
//
//        // Test each sensor
//        System.out.println("=== Testing Weather Sensor WITH THREADS ===");
//        weatherThread.start();
//
//        // Test each sensor
//        System.out.println("=== Testing Brake Sensor WITH THREADS ===");
//        brakeThread.start();


//        // Test deactivation
//        System.out.println("\n=== Testing Deactivation ===");
//        speedSensor.deactivate();
//        System.out.println("Speed reading (inactive): " + speedSensor.measureSpeed());
//        speedSensor.activate();
//        System.out.println("Speed reading (reactivated): " + speedSensor.measureSpeed());
    }

}