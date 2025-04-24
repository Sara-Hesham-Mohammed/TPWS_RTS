public class BrakeStatusSensor extends Sensor {
    public boolean getBrakeStatus() {
        return lastReading > 0;
    }
}