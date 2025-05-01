package Sensors;


public abstract class Sensor {
    protected int sensorID;
    protected double lastReading;
    protected String type;
    protected boolean isActive;

    public Sensor(int id, String type) {
        this.sensorID = id;
        this.type = type;
        this.isActive = true;
    }

    public abstract double readData();

    // Getters
    public int getSensorID() {
        return sensorID;
    }

    public double getLastReading() {
        return lastReading;
    }

    public String getType() {
        return type;
    }

    public boolean isActive() {
        return isActive;
    }

    // Control methods
    public void activate() {
        isActive = true;
    }

    public void deactivate() {
        isActive = false;
    }


}