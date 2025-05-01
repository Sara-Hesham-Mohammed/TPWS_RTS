package Sensors;

import com.espertech.esper.client.EPServiceProvider;

import java.util.Random;

public class SpeedSensor extends Sensor implements Runnable {
    private static final Random random = new Random();
    double maxVoltage = 10;
    EPServiceProvider engine;


    public SpeedSensor(int id, double lastReading) {
        super(id, "analog");
        this.lastReading = lastReading;
    }

    @Override
    public double readData() {
        // the reading that gets sent to esper engine
        if (!isActive) {
            //if inactive then the speed = 0
            System.out.println("Sensors.Sensor inactive");
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
            // Sending the event to the Esper engine
            while (true) engine.getEPRuntime().sendEvent(new SpeedSensor(1, readData()));

    }
}

