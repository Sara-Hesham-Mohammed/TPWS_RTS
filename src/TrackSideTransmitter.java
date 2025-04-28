import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Track-side beacon that:
 *   • broadcasts segmentIdentifier, speedLimit, signalStatus every 50 ms
 *   • automatically picks NEW values every <cycleSeconds>
 */
public class TrackSideTransmitter implements Runnable {

    /* ---------- state ---------- */
    private final String transmitterID;
    private volatile String segmentIdentifier;
    private volatile int    speedLimit;
    private volatile String signalStatus;

    private final int cycleSeconds;
    private int tickCounter = 0;

    /* ---------- pub-sub ---------- */
    @FunctionalInterface public interface Listener {
        void onBroadcast(String key, Object value, Instant ts);
    }
    private final List<Listener> listeners = new CopyOnWriteArrayList<>();
    public void addListener(Listener l){ listeners.add(l); }
    public void removeListener(Listener l){ listeners.remove(l); }

    /* ---------- ctor (5 args) ---------- */
    public TrackSideTransmitter(String id,
                                String seg,
                                int    limit,
                                String signal,
                                int    cycleSeconds){
        this.transmitterID = id;
        this.segmentIdentifier = seg;
        this.speedLimit = limit;
        this.signalStatus = signal;
        this.cycleSeconds = cycleSeconds;
    }

    /* ---------- internal helpers ---------- */
    private void fire(String k, Object v){
        Instant ts = Instant.now();
        for (Listener l : listeners) l.onBroadcast(k, v, ts);
    }
    private void randomise(){
        int n = ThreadLocalRandom.current().nextInt(1, 30);
        segmentIdentifier = "S-" + n + (char)('A'+ n%3);
        speedLimit = switch(n%3){ case 0->80; case 1->100; default->120; };
        signalStatus = switch(n%3){ case 0->"RED"; case 1->"YELLOW"; default->"GREEN"; };
    }

    /* ---------- background loop ---------- */
    private final AtomicBoolean running = new AtomicBoolean(true);
    @Override public void run(){
        try{
            while(running.get()){
                if(++tickCounter >= cycleSeconds*1000/50){ randomise(); tickCounter=0; }
                fire("segmentIdentifier", segmentIdentifier);
                fire("speedLimit", speedLimit);
                fire("signalStatus", signalStatus);
                Thread.sleep(50);
            }
        }catch(InterruptedException ignored){}
    }
    public void stop(){ running.set(false); }

    /* ---------- getters ---------- */
    public String getSegmentIdentifier(){ return segmentIdentifier; }
    public int    getSpeedLimit(){ return speedLimit; }
    public String getSignalStatus(){ return signalStatus; }
}
