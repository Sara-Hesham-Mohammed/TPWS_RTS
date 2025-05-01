public class EmergencyBrakingSystem {
    private boolean isBraking = false;

    public void applyBrakes() {
        if (!isBraking) {
            System.out.println("Brakes applied!");
            isBraking = true;
        }
    }

    public void releaseBrakes() {
        if (isBraking) {
            System.out.println("Brakes released.");
            isBraking = false;
        }
    }

    public boolean isBraking() {
        return isBraking;
    }
}
