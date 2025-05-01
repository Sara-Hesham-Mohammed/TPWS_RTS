import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * General-purpose stopwatch that can also tick listeners at
 * a fixed period.  Uses a ScheduledExecutorService and is closable.
 */
public class Timer implements AutoCloseable {

    /* ----- state ----- */
    private volatile Instant startTime;
    private final ScheduledExecutorService exec =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "TPWS-Timer");
                t.setDaemon(true);
                return t;
            });
    private ScheduledFuture<?> future;

    /* ----- pub-sub ----- */
    @FunctionalInterface
    public interface TickListener {
        void onTick(long elapsedMs, Instant ts);
    }
    private final CopyOnWriteArrayList<TickListener> listeners = new CopyOnWriteArrayList<>();
    public void addListener(TickListener l)    { listeners.add(l); }
    public void removeListener(TickListener l) { listeners.remove(l); }

    /* ----- life-cycle ----- */
    public synchronized void start(long periodMs) {
        if (future != null && !future.isDone()) return;    // already running
        startTime = Instant.now();
        future = exec.scheduleAtFixedRate(() -> {
            long elapsed = calculateDuration();
            Instant ts   = Instant.now();
            listeners.forEach(l -> l.onTick(elapsed, ts));
        }, 0, periodMs, TimeUnit.MILLISECONDS);
    }
    public synchronized void stop() { if (future != null) future.cancel(true); }
    public void reset()             { startTime = null; }

    @Override
    public void close() {
        stop();
        exec.shutdownNow();
    }

    /* ----- utility ----- */
    public long calculateDuration() {
        return (startTime == null) ? 0
                : Duration.between(startTime, Instant.now()).toMillis();
    }
}
