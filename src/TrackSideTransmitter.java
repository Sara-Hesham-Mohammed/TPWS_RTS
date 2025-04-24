public class TrackSideTransmitter {
    private String transmitterID;
    private String segmentIdentifier;
    private int speedLimit;
    private String signalStatus;

    public float broadcastSpeedLimit() {
        return speedLimit;
    }

    public String broadcastSignalStatus() {
        return signalStatus;
    }

    public String broadcastSegmentIdentifier() {
        return segmentIdentifier;
    }
}