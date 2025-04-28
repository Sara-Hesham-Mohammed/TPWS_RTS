import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Track-side beacon that advertises the current track-segment data
 * (speed limit, signal aspect, segment identifier) every 50 ms.
 *
 * The class follows a very light-weight publish–subscribe pattern so that
 * TPWSController, SignalStatusMonitor, WarningBuzzer, EmergencyBrakingSystem
 * – or any other component – can register as listeners and react in real time.
 */
public class TrackSideTransmitter {

    /*====================  attributes (from UML)  =========================*/
    private String transmitterID;         // immutable identity
    private volatile String segmentIdentifier;  // e.g. "A27-03"
    private volatile int    speedLimit;         // km/h
    private volatile String signalStatus;       // "RED" | "YELLOW" | "GREEN"

    public TrackSideTransmitter() {

    }

    /*====================  pub-sub support  ===============================*/
    @FunctionalInterface
    public interface Listener {
        /**
         * Called every time the transmitter broadcasts one of its values.
         *
         * @param key   "speedLimit", "signalStatus" or "segmentIdentifier"
         * @param value the value that was broadcast
         * @param ts    wall-clock time of transmission
         */
        void onBroadcast(String key, Object value, Instant ts);
    }

    private final List<Listener> listeners = new CopyOnWriteArrayList<>();

    /*====================  construction  =================================*/
    public TrackSideTransmitter(String transmitterID,
                                String initialSegment,
                                int    initialLimit,
                                String initialSignal) {
        this.transmitterID      = transmitterID;
        this.segmentIdentifier  = initialSegment;
        this.speedLimit         = initialLimit;
        this.signalStatus       = initialSignal;
    }

    /*====================  “broadcast” operations  =======================*/
    /** Broadcasts (and returns) the current speed limit in km/h. */
    public float broadcastSpeedLimit() {
        float v = speedLimit;
        notifyAll("speedLimit", v);
        return v;
    }

    /** Broadcasts (and returns) the current signal aspect. */
    public String broadcastSignalStatus() {
        String v = signalStatus;
        notifyAll("signalStatus", v);
        return v;
    }

    /** Broadcasts (and returns) the current segment identifier. */
    public String broadcastSegmentIdentifier() {
        String v = segmentIdentifier;
        notifyAll("segmentIdentifier", v);
        return v;
    }

    /*====================  listener management  ==========================*/
    public void addListener(Listener l)    { listeners.add(l);    }
    public void removeListener(Listener l) { listeners.remove(l); }

    private void notifyAll(String key, Object value) {
        Instant now = Instant.now();
        for (Listener l : listeners) l.onBroadcast(key, value, now);
    }

    /*====================  mutators (maintenance or test)  ===============*/
    public void setSegmentIdentifier(String segmentIdentifier) { this.segmentIdentifier = segmentIdentifier; }
    public void setSpeedLimit(int speedLimit)                 { this.speedLimit        = speedLimit;        }
    public void setSignalStatus(String signalStatus)          { this.signalStatus      = signalStatus;      }

    /*====================  getters (optional)  ===========================*/
    public String getTransmitterID()      { return transmitterID;     }
    public String getSegmentIdentifier()  { return segmentIdentifier; }
    public int    getSpeedLimit()         { return speedLimit;        }
    public String getSignalStatus()       { return signalStatus;      }
}
