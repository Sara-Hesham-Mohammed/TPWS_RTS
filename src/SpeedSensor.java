import java.util.Random;

public class SpeedSensor extends Sensor {
    private Random random = new Random();
    private double speedLimit;

    public SpeedSensor(int id, double speedLimit) {
        super(id, "Speed");
        this.speedLimit = speedLimit;
    }

    @Override
    public double readData() {
        if(!isActive) return -1;
        // Simulate speed ±15 km/h around limit
        lastReading = speedLimit + (random.nextDouble() * 30 - 15);
        return lastReading;
    }

    public double measureSpeed() {
        return readData();
    }
}