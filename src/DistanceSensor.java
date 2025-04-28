import java.util.Random;

public class DistanceSensor extends Sensor {
    private Random random = new Random();

    public DistanceSensor(int id) {
        super(id, "Distance");
    }

    @Override
    public double readData() {
        if(!isActive) return -1;
        lastReading = random.nextDouble() * 20; // 0-20 meters
        return lastReading;
    }

    public double measureDistance() {
        return readData();
    }
}