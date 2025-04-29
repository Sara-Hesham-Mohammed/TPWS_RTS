import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simulates a track-side beacon that broadcasts:
 *   • segmentIdentifier (String, e.g. "S-12B")
 *   • speedLimit        (int km/h)
 *   • signalStatus      (String: "RED", "YELLOW", "GREEN")
 *
 * Broadcast every 50 ms; values are fully refreshed every <cycleSeconds>.
 * Uses a single-thread ScheduledExecutorService and is AutoCloseable.
 */
public class TrackSideTransmitter implements AutoCloseable {

    /* ── configuration ── */
    private static final int BROADCAST_MS = 50;           // 20 Hz
    private final int cycleSeconds;

    /* ── live state ── */
    private String transmitterID;      // never null after ctor
    private volatile String segmentIdentifier;
    private volatile int    speedLimit;
    private volatile String signalStatus;

    /* ── scheduler ── */
    private final ScheduledExecutorService exec =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "Tx-" + transmitterID);
                t.setDaemon(true);
                return t;
            });
    private final AtomicInteger tick = new AtomicInteger();

    /* ── pub-sub ── */
    @FunctionalInterface
    public interface Listener {
        void onBroadcast(String key, Object value, Instant ts);
    }
    private final CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<>();
    public void addListener(Listener l)    { listeners.add(l); }
    public void removeListener(Listener l) { listeners.remove(l); }

    /* ── constructor ── */
    public TrackSideTransmitter(String id,
                                String segment,
                                int    limit,
                                String signal,
                                int    cycleSeconds) {
        this.transmitterID      = id;
        this.segmentIdentifier  = segment;
        this.speedLimit         = limit;
        this.signalStatus       = signal;
        this.cycleSeconds       = cycleSeconds;
    }

    /* ── life-cycle ── */
    public void start() {
        exec.scheduleAtFixedRate(this::broadcast,
                0, BROADCAST_MS,
                TimeUnit.MILLISECONDS);
    }
    @Override
    public void close() {
        exec.shutdownNow();
    }

    /* ── helpers ── */
    private void broadcast() {
        if (tick.incrementAndGet() >= cycleSeconds * 1000 / BROADCAST_MS) {
            randomise(); tick.set(0);
        }
        Instant now = Instant.now();
        listeners.forEach(l -> {
            l.onBroadcast("segment",     segmentIdentifier, now);
            l.onBroadcast("speedLimit",  speedLimit,        now);
            l.onBroadcast("signal",      signalStatus,      now);
        });
    }

    private void randomise() {
        int n = ThreadLocalRandom.current().nextInt(1, 30);
        segmentIdentifier = "S-" + n + (char) ('A' + n % 3);
        speedLimit        = switch (n % 3) { case 0 -> 0;  case 1 -> 80; default -> 140; };
        signalStatus      = switch (n % 3) { case 0 -> "RED";
            case 1 -> "YELLOW";
            default -> "GREEN"; };
    }

    /* ── convenience getters ── */
    public String getSegmentIdentifier() { return segmentIdentifier; }
    public int    getSpeedLimit()        { return speedLimit; }
    public String getSignalStatus()      { return signalStatus; }
}
