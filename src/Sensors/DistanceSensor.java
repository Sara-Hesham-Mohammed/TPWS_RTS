package Sensors;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DistanceSensor extends Sensor implements Runnable {
    private static final Random random = new Random();

    public DistanceSensor(int id, double lastReading) {
        super(id, "Distance");
        this.lastReading = lastReading;
    }

    @Override
    public double readData() {
        if (!isActive) {
            System.out.println("Distance sensor inactive");
            return -1;
        } else {
            lastReading = random.nextDouble() * 20; // 0-20 meters
            return lastReading;
        }
    }

    public double measureDistance(double transmitterPosition, double trainPosition) {
        return Math.max(0, transmitterPosition - trainPosition);
    }

    @Override
    public void run() {
        EPServiceProvider engine = EPServiceProviderManager.getDefaultProvider();

        while (true) {
            engine.getEPRuntime().sendEvent(new DistanceSensor(getSensorID(), readData()));

            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                System.out.println("Error: " + ex);
                Logger.getLogger(DistanceSensor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}