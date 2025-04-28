import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SpeedSensor extends Sensor implements Runnable {
    private static final Random random = new Random();
    double maxVoltage = 10;


    public SpeedSensor(int id, double lastReading) {
        super(id, "analog");
        this.lastReading = lastReading;
    }

    @Override
    public double readData() {
        // the reading that gets sent to esper engine
        if (!isActive) {
            //if inactive then the speed = 0
            System.out.println("Sensor inactive");
            return -1;
        } else {
            double voltageReading = measureSpeed();
            //calculates the speed
            lastReading = (voltageReading / maxVoltage) * 180;
            return lastReading;// in voltages
        }

    }

    private double measureSpeed(){
        //mimicking the "real" way of getting speed from a sensor
        double minVoltage = 0;
        return minVoltage + (maxVoltage - minVoltage) * random.nextDouble();
    }

    @Override
    public void run() {
        EPServiceProvider engine = EPServiceProviderManager.getDefaultProvider();

        while (true) {
            engine.getEPRuntime().sendEvent(new SpeedSensor(1, readData()));

            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                System.out.println("Error" + ex);
                Logger.getLogger(SpeedSensor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}

