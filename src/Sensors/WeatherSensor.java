package Sensors;

import java.util.Random;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;

import java.util.logging.Level;
import java.util.logging.Logger;

public class WeatherSensor extends Sensor implements Runnable {
    private final Random random = new Random();
    private final String[] conditions = {"Clear", "Rain", "Snow", "Fog"};

    public WeatherSensor(int id) {
        super(id, "Weather");
    }

    @Override
    public double readData() {
        if (!isActive) return -1;
        lastReading = random.nextInt(conditions.length);
        return lastReading;
    }

    public String detectWeather() {
        return isActive ? conditions[(int) readData()] : "Inactive";
    }

    // recieveing 0s because reasding isn't changing bc change occurs in detect weather
    @Override
    public void run() {
        EPServiceProvider engine = EPServiceProviderManager.getDefaultProvider();

        while (true) {
            engine.getEPRuntime().sendEvent(new WeatherSensor(1));

            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                System.out.println("Error" + ex);
                Logger.getLogger(SpeedSensor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}