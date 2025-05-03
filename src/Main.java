import Components.TrackSideTransmitterEvent;
import Sensors.SpeedSensor;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.Random;

public class main {

    private static final Random random = new Random();

    public static void main(String[] args) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        // Disable logging
        Logger.getRootLogger().setLevel(Level.OFF);

        // Get engine reference
        EPServiceProvider engine = EPServiceProviderManager.getDefaultProvider();
        TPWSController controller = new TPWSController("TPWS_1",engine);


    }
}
