public class PowerSupplyMonitor {
    private boolean isPowerAvailable;
    private boolean backup = false;

    public boolean checkPower() {
        return isPowerAvailable;
    }

    public void activateBackup() {
        backup = true;
    }

    public void alertPowerFailure() {
        System.out.println("Power failure detected, activating backup.");
    }

    public void deactivateBackup() {
        backup = false;
    }
}