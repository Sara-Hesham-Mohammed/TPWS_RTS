import Components.*;
import Sensors.BrakeStatusSensor;
import Sensors.Sensor;
import Sensors.WeatherSensor;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public class TPWSController {
    private String controllerID;
    private double currentSpeed;
    private double safeDistance;
    //to set up listeners
    private final EPServiceProvider engine;

    private final EmergencyBrakingSystem brakingSystem = new EmergencyBrakingSystem();
    private final WeatherSensor weatherSensor = new WeatherSensor(1);
    private final SignalStatusMonitor signalMonitor = new SignalStatusMonitor();
    private final BrakeStatusSensor brakeStatusSensor = new BrakeStatusSensor(2, 0);
    private final PowerSupplyMonitor powerMonitor = new PowerSupplyMonitor();
    private final WarningBuzzer buzzer = new WarningBuzzer();
    private final GPSModule gps = new GPSModule();
    private final TrackSideTransmitter transmitter = new TrackSideTransmitter("seg100", "100", 50, "green", 50);


    double trainPosition = gps.provideRealTimeLocation();
    double speedLimit = transmitter.getSpeedLimit();
    String signalStatus = transmitter.getSignalStatus();


    public TPWSController(String controllerID, double currentSpeed, EPServiceProvider engine) throws UnsupportedAudioFileException, LineUnavailableException, IOException {
        this.controllerID = controllerID;
        this.currentSpeed = currentSpeed;
        this.engine = engine;

        monitorConditions();
    }

    // Function for receiving the sensor event(whatever event) continuously every X time interval
    public Thread getEsperData(EPServiceProvider engine, Sensor sensor, int time) {
        String[] sensorString = String.valueOf(sensor).split("@");
        // Creating EPL statement
        EPStatement select_statement = engine
                .getEPAdministrator()
                .createEPL("select lastReading from " + sensorString[0] + "timer:interval(" + time + " milliseconds)");

        // Attaching callback to EPL statements
        select_statement.setSubscriber(new Object() {
            public void update(double lastReading) {
                System.out.printf("\n NEW %s READING: %f ", sensorString[0].toUpperCase(), lastReading);
            }
        });

        return new Thread((Runnable) sensor);
    }

    public void monitorConditions() {
        if (!powerMonitor.checkPower()) {
            System.out.println("Power failure detected.");
            // maybe make it RETURN the string so it can be used in a pop up or smth in the GUI
            powerMonitor.alertPowerFailure();
            powerMonitor.activateBackup();
        }
        String status = signalMonitor.getSignalStatus();
        if ("STOP".equalsIgnoreCase(status)) {
            System.out.println("Signal is STOP. Applying brakes.");
        }

        // 1) Getting the Transmitter broadcast data every 50ms
        String eplTransmitter = "select segmentIdentifier, signalStatus, speedLimit "
                + "from TransmitterEvent.win:time(50 milliseconds)";
        EPStatement transmitterStatement = engine.getEPAdministrator().createEPL(eplTransmitter);

        transmitterStatement.setSubscriber(new Object() {
            public void update(String segmentIdentifier, String signalStatus, int speedLimit) {
                System.out.printf("SEG=%s SIG=%s LIM=%d%n", segmentIdentifier, signalStatus, speedLimit);
            }
        });

        // 2) Getting the brakes sensor data every 100ms
        String eplBrake = "select brakeEngaged from BrakeStatusEvent.win:time(100 milliseconds)";
        EPStatement brakeStatement = engine.getEPAdministrator().createEPL(eplBrake);

        brakeStatement.setSubscriber(new Object() {
            public void update(boolean brakeEngaged) {
                if (!brakeEngaged) checkBrakes();
            }
        });


    }


    public void checkBrakes() {
        if (brakeStatusSensor.getBrakeStatus()) {
            System.out.println("Brake status: OK");
        } else {
            System.out.println("Brakes status: in need of maintenance. Please check.");
        }
    }

    public void reduceSpeed() {
        System.out.println("Reducing speed. Brakes engaged.");
        while (currentSpeed > speedLimit + 10) {
            brakingSystem.applyBrakes();
            currentSpeed = currentSpeed - 10;
        }
    }

    public void stopTrain() {
        if (currentSpeed > 0 && signalStatus.equalsIgnoreCase("red")) {
            brakingSystem.applyBrakes();
        } else brakingSystem.releaseBrakes();
        currentSpeed = currentSpeed - 10;
        System.out.println("Train stopping.");
    }

    public void activateWarningSound() {
        if (currentSpeed > speedLimit + 5) {
            buzzer.activateBuzzer();
            System.out.println("BUZZER ACTIVATED. OVER SPEED LIMIT. PLEASE REDUCE SPEED");
        } else buzzer.stopBuzzer();

    }
}