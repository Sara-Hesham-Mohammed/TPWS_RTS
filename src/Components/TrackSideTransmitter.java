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

public class TrackSideTransmitter implements Runnable {
    EPServiceProvider engine;
    private final String transmitterID;
    private final String segmentIdentifier;
    private final int speedLimit;
    private final String signalStatus;

    public TrackSideTransmitter(String transmitterID, String segmentIdentifier, int speedLimit, String signalStatus, EPServiceProvider engine) {
        this.engine = engine;
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

    private void broadcast() {
        engine.getEPRuntime().sendEvent(new TrackSideTransmitter(transmitterID, segmentIdentifier, speedLimit, signalStatus, engine));
    }

    @Override
    public void run() {
        broadcast();
    }
}

