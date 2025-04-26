import java.util.Random;

public class BrakeStatusSensor extends Sensor {
    private Random random = new Random();

    public BrakeStatusSensor(int id) {
        super(id, "BrakeStatus");
    }

    @Override
    public double readData() {
        if(!isActive) return -1;
        lastReading = random.nextDouble() > 0.9 ? 1 : 0; // 10% chance brakes applied
        return lastReading;
    }

    public boolean getBrakeStatus() {
        return isActive && readData() == 1;
    }
}