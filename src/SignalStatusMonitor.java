public class SignalStatusMonitor {
    private String currentSignalStatus;

    public void updateSignalStatus(String newStatus) {
        if (newStatus != null && !newStatus.equals(currentSignalStatus)) {
            System.out.println("Signal status updated to: " + newStatus);
            currentSignalStatus = newStatus;
            // Could trigger status-dependent actions
        }
    }

    public String getSignalStatus() {
        return currentSignalStatus;
    }
}
