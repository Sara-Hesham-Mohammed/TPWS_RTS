package Components;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
/**
 * Simulates a track-side beacon that broadcasts:
 * • segmentIdentifier (String, e.g. "S-12B")
 * • speedLimit        (int km/h)
 * • signalStatus      (String: "RED", "YELLOW", "GREEN")
 * <p>
 * Broadcast every 50 ms; values are fully refreshed every <cycleSeconds>.
 * Uses a single-thread ScheduledExecutorService and is AutoCloseable.
 */

public class TrackSideTransmitterEvent implements Runnable{

    ArrayList<String> signals = new ArrayList<>(Arrays.asList("red", "yellow", "green"));
    ArrayList<String> segID = new ArrayList<>(Arrays.asList("SEG_100", "SEG_200", "SEG_300"));


    Random rand = new Random();

    // 50 to 180 speed lim
    static int randomSpeedLim(int min, int max) {
        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }
        Random r = new Random();
        return r.nextInt(max - min) + min;
    }


    public String getRandomChoice(ArrayList<String> list) {
        return list.get(rand.nextInt(list.size()));
    }

    @Override
    public void run() {
        EPServiceProvider engine = EPServiceProviderManager.getDefaultProvider();

        while (true) {
            engine.getEPRuntime().sendEvent(
                    new TrackSideTransmitter("Trans_1",getRandomChoice(segID), randomSpeedLim(50,180), getRandomChoice(signals),50)
            );
        }
    }
}

