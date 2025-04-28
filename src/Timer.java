import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * High-resolution stopwatch used by TPWS to guarantee
 * timing constraints (e.g. “brakes must engage ≤ 100 ms
 * after overspeed is detected”).
 *
 * All operations are thread-safe – the class uses
 * java.time.Instant for nanosecond precision and an
 * AtomicBoolean to flag whether the timer is currently running.
 */
public class Timer {

    /*====================  attribute (from UML)  =========================*/
    private volatile Instant startTime;         // null ⇒ not started
    private final AtomicBoolean running = new AtomicBoolean(false);

    /*====================  operations (from UML)  ========================*/
    /** Records the current wall-clock time and marks the timer as running. */
    public void start() {
        startTime = Instant.now();
        running.set(true);
    }

    /** Stops the timer – further duration calls freeze at the stop instant. */
    public void stop() {
        if (running.compareAndSet(true, false)) {
            // do nothing else – startTime already holds the start,
            // and we regard 'now' as the stop instant
        }
    }

    /** Clears any recorded times and returns the timer to its initial state. */
    public void reset() {
        running.set(false);
        startTime = null;
    }

    /**
     * Returns the elapsed duration **in milliseconds**.
     * If the timer has never been started, the result is 0.
     * If the timer is still running, the duration is measured up to ‘now’.
     */
    public long calculateDuration() {
        if (startTime == null) return 0;
        Instant end = running.get() ? Instant.now() : startTime; // stop() froze time
        return Duration.between(startTime, end).toMillis();
    }
}
