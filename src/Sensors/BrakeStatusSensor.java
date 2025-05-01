package Sensors;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BrakeStatusSensor extends Sensor implements Runnable {
    private static final Random random = new Random();

    public BrakeStatusSensor(int id, double lastReading) {
        super(id, "BrakeStatus");
        this.lastReading = lastReading;
    }

    @Override
    public double readData() {
        if (!isActive) {
            System.out.println("Brake sensor inactive");
            return -1;
        } else {
            // 10% chance brakes applied
            lastReading = random.nextDouble() > 0.9 ? 1 : 0;
            return lastReading;
        }
    }

    public boolean getBrakeStatus() {
        return isActive && readData() == 1;
    }

    @Override
    public void run() {
        EPServiceProvider engine = EPServiceProviderManager.getDefaultProvider();

        while (true) {
            engine.getEPRuntime().sendEvent(new BrakeStatusSensor(getSensorID(), readData()));

            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                System.out.println("Error: " + ex);
                Logger.getLogger(BrakeStatusSensor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}