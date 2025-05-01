package Sensors;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;

public abstract class Sensor implements Runnable{
    protected int sensorID;
    protected double lastReading;
    protected String type;
    protected boolean isActive;
    protected EPServiceProvider engine;

    public Sensor(int id, String type) {
        this.sensorID = id;
        this.type = type;
        this.isActive = true;
        this.engine = EPServiceProviderManager.getDefaultProvider();

    }

    public abstract double readData();

    // Getters
    public int getSensorID() { return sensorID; }
    public double getLastReading() { return lastReading; }
    public String getType() { return type; }
    public boolean isActive() { return isActive; }

    // Control methods
    public void activate() { isActive = true; }
    public void deactivate() { isActive = false; }
    @Override
    public void run() {
        synchronized (engine) {
            // Sending the event to the Esper engine
            while (true) engine.getEPRuntime().sendEvent(new SpeedSensor(1, readData()));
        }
    }



}