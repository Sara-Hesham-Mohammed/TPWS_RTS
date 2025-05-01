public class PowerSupplyMonitor {
    private boolean isPowerAvailable = true;
    private boolean backup = false;

    public boolean checkPower() {
        return isPowerAvailable;
    }

    public void activateBackup() {
        if (!isPowerAvailable) {
            System.out.println("Backup power activated.");
            isPowerAvailable = false;
            backup = true;
        }
    }

        public void alertPowerFailure () {
            System.out.println("Power failure detected, activating backup.");
        }

        public void deactivateBackup () {
            System.out.println("Backup power deactivated.");
            isPowerAvailable = true;
            backup = false;
        }
}