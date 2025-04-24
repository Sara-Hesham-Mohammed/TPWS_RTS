public class Sensor {
    protected int sensorID;
    protected double lastReading;
    protected String type;

    public double readData() {
        return lastReading;
    }
}