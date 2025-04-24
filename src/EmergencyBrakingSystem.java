public class EmergencyBrakingSystem {
    private boolean isBraking;

    public void applyBrakes() {
        isBraking = true;
    }

    public void releaseBrakes() {
        isBraking = false;
    }
}
