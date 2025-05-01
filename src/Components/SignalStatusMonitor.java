package Components;

public class SignalStatusMonitor {
    private String currentSignalStatus;

    public void updateSignalStatus(String newStatus) {
        currentSignalStatus = newStatus;
    }

    public String getSignalStatus() {
        return currentSignalStatus;
    }
}
