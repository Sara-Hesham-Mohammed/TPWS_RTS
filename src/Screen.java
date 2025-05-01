
public class Screen {

    public void displaySpeed(double currentSpeed) {
        System.out.println("Current Speed: " + currentSpeed + " km/h");
    }

    public void displaySignalStatus(String signalStatus) {
        System.out.println("Signal Status: " + signalStatus);
    }

    public void displayBrakingStatus(String brakingStatus) {
        System.out.println("Braking Status: " + brakingStatus);
    }

    public void displayWarning(String message) {
        System.out.println("WARNING: " + message);
    }

    public void displayInfo(String message) {
        System.out.println("INFO: " + message);
    }
}
