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

    private final EmergencyBrakingSystem brakingSystem = new EmergencyBrakingSystem();
    private final WeatherSensor weatherSensor = new WeatherSensor(1);
    private final SignalStatusMonitor signalStatusMonitor = new SignalStatusMonitor();
    private final BrakeStatusSensor brakeStatusSensor = new BrakeStatusSensor(2, 0);
    private final PowerSupplyMonitor powerSupplyMonitor = new PowerSupplyMonitor();
    private final WarningBuzzer buzzer = new WarningBuzzer();
    private final GPSModule gps = new GPSModule();
    private final TrackSideTransmitter transmitter = new TrackSideTransmitter("seg100", "100", 50, "green",50);


    double trainPosition = gps.provideRealTimeLocation();
    double speedLimit = transmitter.getSpeedLimit();
    String signalStatus = transmitter.getSignalStatus();


    public TPWSController(String controllerID, double currentSpeed) throws UnsupportedAudioFileException, LineUnavailableException, IOException {
        this.controllerID = controllerID;
        this.currentSpeed = currentSpeed;
    }

    // Function for receiving the event(whatever event) continuously every X time interval
    public Thread EsperRun(EPServiceProvider engine, Sensor sensor, int time) {
        System.out.println("ESPER RUN");
        String[] sensorString = String.valueOf(sensor).split("@");
        // Creating EPL statement
        EPStatement speed_select_statement = engine
                .getEPAdministrator()
                .createEPL("select lastReading from " + sensorString[0]+ "timer:interval("+time+" milliseconds)");

        // Attaching callback to EPL statements
        speed_select_statement.setSubscriber(new Object() {
            public void update(double lastReading) {
                System.out.printf("\n NEW %s READING: %f " , sensorString[0].toUpperCase(), lastReading);
            }
        });

        return new Thread((Runnable) sensor);
    }

    //nour
    public void monitorConditions(PowerSupplyMonitor powerMonitor, SignalStatusMonitor signalMonitor) {
        if (!powerMonitor.checkPower()) {
            System.out.println("Power failure detected.");
            powerMonitor.alertPowerFailure();
            powerMonitor.activateBackup();
        }

            //monitoring conditions could include signal status also
            String status = signalMonitor.getSignalStatus();
            System.out.println("Current signal status: " + status);

            if ("STOP".equalsIgnoreCase(status)) {
                System.out.println("Signal is STOP. Applying brakes.");
            }

    }

    //nour
    public void checkBrakes(EmergencyBrakingSystem brakeSystem) {
        if (!brakeSystem.isBraking()) {
            System.out.println("Brakes not applied. Applying now.");
            brakeSystem.applyBrakes();
        } else {
            System.out.println("Brakes already applied.");
        }
    }

    //nour (but there could be connections to other classes idk yet)
    public void reduceSpeed(EmergencyBrakingSystem brakeSystem) {
        System.out.println("Reducing speed. Brakes engaged.");
        brakeSystem.applyBrakes();
    }


    public void activateWarningSound() {
        if(currentSpeed > speedLimit + 5){
            buzzer.activateBuzzer();
            System.out.println("BUZZER ACTIVATED. OVER SPEED LIMIT. PLEASE REDUCE SPEED");
        }else buzzer.stopBuzzer();

    }
}