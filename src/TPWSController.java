import Components.*;
import GUIs.AlertGUI;
import GUIs.GUI;
import Sensors.*;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.io.IOException;

public class TPWSController {
    /*** GUIs Init ***/
    private GUI mainGUI;
    private AlertGUI alertGUI;

    //change current speed, get it mn el gps module
    private double currentSpeed;
    private double safeDistance;
    private volatile double speedLimit; // updated via Esper
    private volatile String signalStatus;

    // Engine to set up subscribers w kda
    private EPServiceProvider engine = null;

    /**Sensors init**/
    private final WeatherSensor weatherSensor = new WeatherSensor(1);
    private final BrakeStatusSensor brakeStatusSensor = new BrakeStatusSensor(1, 0);
    private final DistanceSensor distanceSensor = new DistanceSensor(1, 0);
    private final SpeedSensor speedSensor = new SpeedSensor(1, 100);

    /**System Components init**/
    private TrackSideTransmitterEvent transmitterEvent;
    private final EmergencyBrakingSystem brakingSystem = new EmergencyBrakingSystem();
    private final SignalStatusMonitor signalMonitor = new SignalStatusMonitor();
    private final PowerSupplyMonitor powerMonitor = new PowerSupplyMonitor();
    private final BuzzerEvent buzzer = new BuzzerEvent();
    private final GPSModule gps = new GPSModule(20.0, 30.0, 60.0);


    public TPWSController(String controllerID, EPServiceProvider engine) throws UnsupportedAudioFileException, LineUnavailableException, IOException {
        this.engine = engine;
        this.transmitterEvent = new TrackSideTransmitterEvent();
        registerEvents();
        SwingUtilities.invokeLater(() -> {
            try {
                this.alertGUI = new AlertGUI();
                this.mainGUI = new GUI();
                this.mainGUI.showGUI("SHOWING GUI IN CONTROLLER");
            } catch (UnsupportedAudioFileException | LineUnavailableException | IOException e) {
                e.printStackTrace();
            }
        });
        // Wait senna for GUI init
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        monitorConditions();
    }

    public void monitorConditions() {
        System.out.println("Monitoring conditions...");
        // receive data from esper engine
        if (!powerMonitor.checkPower()) {
            System.out.println("Power failure detected.");
            // maybe make it RETURN the string so it can be used in a pop up or smth in the GUIs.GUI
            powerMonitor.alertPowerFailure();
            powerMonitor.activateBackup();
        }
        String status = signalMonitor.getSignalStatus();
        if ("STOP".equalsIgnoreCase(status)) {
            System.out.println("Signal is STOP. Applying brakes.");
        }

        // get all the sensor data from esper engine + start their threads
        Thread speedThread = getSensorData(engine, speedSensor, 250);
        Thread brakeThread = getSensorData(engine, brakeStatusSensor, 100);
        Thread distanceThread = getSensorData(engine, distanceSensor, 100);
        Thread weatherThread = getSensorData(engine, weatherSensor, 100);
        speedThread.start();
        brakeThread.start();
        distanceThread.start();
        weatherThread.start();

        Thread transmitterThread = new Thread(transmitterEvent);
        transmitterThread.start();
    }

    public void registerEvents(){
        // Registering the events
        engine.getEPAdministrator().getConfiguration().addEventType(SpeedSensor.class);
        engine.getEPAdministrator().getConfiguration().addEventType(BrakeStatusSensor.class);
        engine.getEPAdministrator().getConfiguration().addEventType(DistanceSensor.class);
        engine.getEPAdministrator().getConfiguration().addEventType(WeatherSensor.class);
        engine.getEPAdministrator().getConfiguration().addEventType(TrackSideTransmitterEvent.class);
        engine.getEPAdministrator().getConfiguration().addEventType(TrackSideTransmitter.class);
        engine.getEPAdministrator().getConfiguration().addEventType(PowerSupplyMonitor.class);
        engine.getEPAdministrator().getConfiguration().addEventType(SignalStatusMonitor.class);
        engine.getEPAdministrator().getConfiguration().addEventType(EmergencyBrakingSystem.class);
        engine.getEPAdministrator().getConfiguration().addEventType(BuzzerEvent.class);
        engine.getEPAdministrator().getConfiguration().addEventType(WarningBuzzer.class);
        engine.getEPAdministrator().getConfiguration().addEventType(GPSModule.class);
    }

    /* Synchronization for the stuff that modifies the values/ writes ONLY*/
    public synchronized void checkBrakes() {
        if (brakeStatusSensor.getBrakeStatus()) {
            System.out.println("Brake status: OK");
        } else {
            System.out.println("Brakes status: in need of maintenance. Please check.");
        }
    }

    public synchronized void activateWarningSound() {
        if (currentSpeed > speedLimit + 5) {SwingUtilities.invokeLater(() -> {
            if (alertGUI != null) {
                alertGUI.showMessage("SPEED WARNING: Exceeding speed limit!");
                alertGUI.setVisible(true);
            }
        });
            buzzer.activateBuzzer();
            System.out.println("BUZZER ACTIVATED. OVER SPEED LIMIT. PLEASE REDUCE SPEED");
        } else buzzer.stopBuzzer();

    }

    public synchronized void reduceSpeed() {
        while (currentSpeed > speedLimit + 10) {
            brakingSystem.applyBrakes();
            updateGUISpeed(currentSpeed);
            currentSpeed -= 10;
            System.out.println("Reducing speed. Brakes engaged.");
        }
    }

    public synchronized void stopTrain() {
        while (signalStatus.equalsIgnoreCase("red")) {
            if (currentSpeed <= 0) {
                currentSpeed = 0; // ensure it doesn't go negative
                break;
            }
            //engage brakes and decrement speed
            brakingSystem.applyBrakes();
            currentSpeed -= 10;

            System.out.println("Train stopping.");
        }
    }

    // Function for receiving the sensor event(whatever event) continuously every X time interval
    public Thread getSensorData(EPServiceProvider engine, Sensor sensor, int time) {
        String[] sensorString = String.valueOf(sensor).split("@");
        String sensorType = sensorString[0];
        // Creating EPL statement
        EPStatement select_statement = engine.getEPAdministrator().createEPL("select lastReading from " + sensorType + ".win:time(" + time + "  milliseconds)");

        // Attaching callback to EPL statements
        select_statement.setSubscriber(new Object() {
            public void update(double lastReading) {
                switch (sensorType) {
                    case "Sensors.SpeedSensor":
                        currentSpeed = lastReading;
                        // Check speed limits and warnings and activate the buzzer if necessary
                        activateWarningSound();
                        System.out.printf("Current speed: " + currentSpeed);
                        updateGUISpeed(currentSpeed);
                        // reduce speed if necessary
                        reduceSpeed();
                        break;
                    case "Sensors.BrakeStatusSensor":
                        if (lastReading != 1) {
                            checkBrakes();
                        }
                        break;
                    case "Sensors.DistanceSensor":
                        safeDistance = lastReading;
                        if (safeDistance <= 10) {
                            //can only receive the data if the distance is 10m
                            System.out.println("Transmitter data received.");
                            getTransmitterData();
                        }
                        // When the train is close enough to read the signal (can know if it's red nor not)
                        // then it checks if it needs to stop
                        stopTrain();
                        break;
                    default:
                        System.out.println("this default");
//                        System.out.printf("\n NEW %s READING: %f ", sensorType.toUpperCase(), lastReading);
                        break;
                }
            }
        });
        return new Thread((Runnable) sensor);
    }

    public synchronized void getTransmitterData() {
        //Get the Transmitter broadcast data every 50ms
        String eplTransmitter = "select segmentIdentifier, signalStatus, speedLimit from Components.TrackSideTransmitter.win:time(50 milliseconds)";
        EPStatement transmitterStatement = engine.getEPAdministrator().createEPL(eplTransmitter);

        //this sets the speed
        transmitterStatement.setSubscriber(new Object() {
            public void update(String segmentIdentifier, String segSignalStatus, double segSpeedLimit) {
                speedLimit = segSpeedLimit;
                signalStatus = segSignalStatus;
//                System.out.printf("SEG=%s SIG=%s LIM=%f%n", segmentIdentifier, signalStatus, speedLimit);
            }
        });
    }

    /*** Helper method for el GUI***/

    private void updateGUISpeed(final double speed) {
        if (mainGUI != null) {
            SwingUtilities.invokeLater(() -> mainGUI.updateSpeed(speed));
        }
    }
}