import java.util.Random;

public class WeatherSensor extends Sensor {
    private Random random = new Random();
    private String[] conditions = {"Clear", "Rain", "Snow", "Fog"};

    public WeatherSensor(int id) {
        super(id, "Weather");
    }

    @Override
    public double readData() {
        if(!isActive) return -1;
        lastReading = random.nextInt(conditions.length);
        return lastReading;
    }

    public String detectWeather() {
        return isActive ? conditions[(int)readData()] : "Inactive";
    }
}