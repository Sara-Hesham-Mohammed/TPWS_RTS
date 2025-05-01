import Components.*;
import Sensors.BrakeStatusSensor;
import Sensors.Sensor;
import Sensors.WeatherSensor;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;

public class TPWSController {
    private String controllerID;
    private double currentSpeed;
    private double safeDistance;

    private final EmergencyBrakingSystem brakingSystem = new EmergencyBrakingSystem();
    private final WeatherSensor weatherSensor = new WeatherSensor(1);
    private final SignalStatusMonitor signalStatusMonitor = new SignalStatusMonitor();
    private final BrakeStatusSensor brakeStatusSensor = new BrakeStatusSensor(2, 50);
    private final PowerSupplyMonitor powerSupplyMonitor = new PowerSupplyMonitor();
    private final WarningBuzzer buzzer = new WarningBuzzer();
    private final GPSModule gps = new GPSModule();
    private final TrackSideTransmitter transmitter = new TrackSideTransmitter("seg100", "100", 50, "green",50);


    double trainPosition = gps.provideRealTimeLocation();
    double speedLimit = transmitter.getSpeedLimit();
    String signalStatus = transmitter.getSignalStatus();

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

    // find a proper usage for this
    public void monitorConditions() {
        weatherSensor.detectWeather();
        String signalStatus = signalStatusMonitor.getSignalStatus();
        powerSupplyMonitor.checkPower();

    }

    public void checkBrakes() {
        // Implementation
        brakeStatusSensor.getBrakeStatus();
//        if(){
//
//        }
    }

    public void reduceSpeed() {
        if(currentSpeed > speedLimit + 10 || signalStatus.equalsIgnoreCase("red")){
            brakingSystem.applyBrakes();
        }else brakingSystem.releaseBrakes();
    }

    public void activateWarningSound() {
        if(currentSpeed > speedLimit + 5){
            buzzer.activateBuzzer();
            System.out.println("BUZZER ACTIVATED. OVER SPEED LIMIT.");
        }else buzzer.stopBuzzer();

    }
}