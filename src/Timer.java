import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Realtime timer that fulfils two use-cases:
 *
 *   ① «Calculate duration»  –  calculateDuration() returns elapsed ms.
 *   ② «Trigger Periodic Update»  –  notifies listeners every <periodMs>.
 *
 * Thread-safe and self-contained: call start(periodMs) and the timer
 * spins on its own thread until stop() is called.
 */
public class Timer implements Runnable {

    /* ───── attributes (from UML) ───── */
    private volatile Instant startTime;           // null ⇒ not running
    private final AtomicBoolean running = new AtomicBoolean(false);
    private long periodMs;                        // broadcast period

    /* ───── pub-sub support ───── */
    @FunctionalInterface
    public interface TickListener {
        /** called every <periodMs> with current elapsed millis */
        void onTick(long elapsedMs, Instant ts);
    }
    private final List<TickListener> listeners = new CopyOnWriteArrayList<>();

    public void addListener(TickListener l)    { listeners.add(l);    }
    public void removeListener(TickListener l) { listeners.remove(l); }

    /* ───── life-cycle ───── */
    public synchronized void start(long periodMs) {
        if (running.get()) return;               // already running
        this.periodMs = periodMs;
        running.set(true);
        startTime = Instant.now();
        new Thread(this, "TPWS-Timer").start();
    }
    public void stop()   { running.set(false); }
    public void reset()  { startTime = null; }

    /* ───── Runnable loop ───── */
    @Override public void run() {
        try {
            while (running.get()) {
                long elapsed = calculateDuration();
                Instant now  = Instant.now();
                for (TickListener l : listeners) l.onTick(elapsed, now);
                Thread.sleep(periodMs);
            }
        } catch (InterruptedException ignored) { }
    }

    /* ───── green “Calculate duration” operation ───── */
    public long calculateDuration() {
        return (startTime == null)
                ? 0
                : Duration.between(startTime,
                        running.get() ? Instant.now() : startTime)
                .toMillis();
    }
}
