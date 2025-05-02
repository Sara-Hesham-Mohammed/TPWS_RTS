package Components;

import com.espertech.esper.client.EPServiceProvider;

/**
 * Simulates a track-side beacon that broadcasts:
 * • segmentIdentifier (String, e.g. "S-12B")
 * • speedLimit        (int km/h)
 * • signalStatus      (String: "RED", "YELLOW", "GREEN")
 * <p>
 * Broadcast every 50 ms; values are fully refreshed every <cycleSeconds>.
 * Uses a single-thread ScheduledExecutorService and is AutoCloseable.
 */

public class TrackSideTransmitterEvent {
    private final String transmitterID;
    private final String segmentIdentifier;
    private final int speedLimit;
    private final String signalStatus;

    public TrackSideTransmitterEvent(String transmitterID, String segmentIdentifier, int speedLimit, String signalStatus) {
        this.transmitterID = transmitterID;
        this.segmentIdentifier = segmentIdentifier;
        this.speedLimit = speedLimit;
        this.signalStatus = signalStatus;
    }

    // Getters (cause they're req by Esper)
    public String getSegmentIdentifier() {
        return segmentIdentifier;
    }

    public int getSpeedLimit() {
        return speedLimit;
    }

    public String getSignalStatus() {
        return signalStatus;
    }

}

