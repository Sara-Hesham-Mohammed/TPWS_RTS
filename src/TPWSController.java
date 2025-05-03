import Components.*;
import GUIs.AlertGUI;
import GUIs.Dashboard;
import Sensors.*;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import java.io.IOException;

public class TPWSController {
    /*** GUIs Init ***/
//    private GUI mainGUI;
    private Dashboard mainGUI;
    private AlertGUI alertGUI;

    /**** Some Local vars (bc. they're shared between diff fns)****/
    private double currentSpeed;
    private double safeDistance;
    private volatile double speedLimit; // updated via Esper
    private volatile String signalStatus;
    private volatile String segmentIdentifier;

    /*** Engine to set up subscribers w kda ****/
    private EPServiceProvider engine = null;

    /**
     * Sensors init
     **/
    private final WeatherSensor weatherSensor = new WeatherSensor(1);
    private final BrakeStatusSensor brakeStatusSensor = new BrakeStatusSensor(1, 0);
    private final DistanceSensor distanceSensor = new DistanceSensor(1, 0);
    private final SpeedSensor speedSensor = new SpeedSensor(1, 100);

    /**
     * System Components init
     **/
    private TrackSideTransmitterEvent transmitterEvent;
    private final EmergencyBrakingSystem brakingSystem = new EmergencyBrakingSystem();
    private final SignalStatusMonitor signalMonitor = new SignalStatusMonitor();
    private final PowerSupplyMonitor powerMonitor = new PowerSupplyMonitor();
    private final BuzzerEvent buzzer = new BuzzerEvent();
    private final GPSModule gps = new GPSModule(20.0, 30.0, 60.0);


    /***Lock objects (new sync attempt bc it's not rlly working 3dl :( )***/
    private final Object speedLock = new Object();
    private final Object brakeLock = new Object();
    private final Object transLock = new Object();

    public TPWSController(String controllerID, EPServiceProvider engine) throws UnsupportedAudioFileException, LineUnavailableException, IOException {
        this.engine = engine;
        this.transmitterEvent = new TrackSideTransmitterEvent();
        registerEvents();
        SwingUtilities.invokeLater(() -> {
                this.alertGUI = new AlertGUI();
                this.mainGUI = new Dashboard();
                this.mainGUI.showGUI();

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
            updateGUIPowerStatus("Power Failure");
            powerMonitor.alertPowerFailure();
            powerMonitor.activateBackup();
        }

        String status = signalMonitor.getSignalStatus();
        updateGUISignalStatus(status);
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

    public void registerEvents() {
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
    public void checkBrakes() {
        synchronized (brakeLock) {
            if (brakeStatusSensor.getBrakeStatus()) {
                System.out.println("Brake status: OK");
            } else {
                System.out.println("Brakes status: in need of maintenance. Please check.");
            }
        }

    }

    public void activateWarningSound() {
        double localSpeed;
        double localSpeedLimit;

        // Only synchronize the write method bs
        synchronized (speedLock) {
            localSpeed = currentSpeed;
            localSpeedLimit = speedLimit;
        }
        if (localSpeed > localSpeedLimit + 5) {
            SwingUtilities.invokeLater(() -> {
                if (alertGUI != null) {
                    alertGUI.showMessage("SPEED WARNING: Exceeding speed limit!");
                    alertGUI.setVisible(true);
                }
            });
            buzzer.activateBuzzer();
            System.out.println("BUZZER ACTIVATED. OVER SPEED LIMIT. PLEASE REDUCE SPEED");
        } else {
            buzzer.stopBuzzer();
            SwingUtilities.invokeLater(() -> alertGUI.setVisible(false));
        }

    }

    public void reduceSpeed() {
        while (true) {
            double localCurrentSpeed;
            double localSpeedLimit;

            synchronized (speedLock) {
                localCurrentSpeed = currentSpeed;
                localSpeedLimit = speedLimit;
                if (localCurrentSpeed <= localSpeedLimit + 10) {
                    break;
                }
                currentSpeed -= 10;
            }

            brakingSystem.applyBrakes();
            updateGUISpeed(localCurrentSpeed - 10);
            System.out.println("Reducing speed. Brakes engaged.");

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void stopTrain() {
        while (signalStatus.equalsIgnoreCase("red")) {
            double localCurrentSpeed;
            synchronized (speedLock) {
                localCurrentSpeed = currentSpeed;
                if (localCurrentSpeed <= 0) {
                    currentSpeed = 0; // Ensure speed doesn't go negative
                    break; // Exit loop if speed is 0 or below
                }
                currentSpeed = Math.max(0, localCurrentSpeed - 10); // Decrement speed but don't go below 0
            }

            // Engage brakes
            brakingSystem.applyBrakes();
            updateGUISpeed(currentSpeed);
            System.out.println("Train stopping. Current speed: " + currentSpeed);

            // timing
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
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
                        // checks if needs warning
                        activateWarningSound();
                        System.out.printf("Current speed: " + currentSpeed);
                        updateGUISpeed(currentSpeed);
                        reduceSpeed();
                        break;
                    case "Sensors.BrakeStatusSensor":
                        if (lastReading != 1) {
                            checkBrakes();
                            updateGUIBrakeStatus(brakeStatusSensor.getBrakeStatus() ? "OK" : "NOT OK");
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

                    case "Sensors.WeatherSensor":
                        String weatherStatus = weatherSensor.detectWeather();
                        updateGUIWeatherStatus(weatherStatus);
                        System.out.println("Weather status: " + weatherStatus);
                        break;
                    default:
                        System.out.println("this default");
                        break;
                }
            }
        });
        return new Thread((Runnable) sensor);
    }

    public void getTransmitterData() {
        // Get the Transmitter broadcast data every 50ms
        String eplTransmitter = "select segmentIdentifier, signalStatus, speedLimit " +
                "from Components.TrackSideTransmitter.win:time(50 milliseconds)";

        EPStatement transmitterStatement = engine.getEPAdministrator().createEPL(eplTransmitter);

        transmitterStatement.setSubscriber(new Object() {
            public void update(String segIdentifier, String segSignalStatus, double segSpeedLimit) {
                synchronized (transLock) {
                    segmentIdentifier = segIdentifier;
                    speedLimit = segSpeedLimit;
                    signalStatus = segSignalStatus;
                }
                updateGUITransData(segSignalStatus, segIdentifier, String.valueOf(segSpeedLimit));
            }
        });

        transmitterStatement.destroy();
    }
    /*** Helper method for GUI***/
    private void updateGUISpeed(final double speed) {
        if (mainGUI != null) {
            SwingUtilities.invokeLater(() -> mainGUI.updateSpeed(speed));
        }
    }

    private void updateGUITransData(final String signal, final String segmentIdentifier,String limit) {
        if (mainGUI != null) {
            SwingUtilities.invokeLater(() -> {
                mainGUI.updateSegmentId(segmentIdentifier);
                mainGUI.updateSegmentSignal(signal);
                mainGUI.updateSpeedLimit(limit);

            });
        }
    }


    private void updateGUIBrakeStatus(final String status) {
        if (mainGUI != null) {
            SwingUtilities.invokeLater(() -> mainGUI.updateBrakeStatus(status));
        }
    }

    private void updateGUIPowerStatus(final String status) {
        if (mainGUI != null) {
            SwingUtilities.invokeLater(() -> mainGUI.updatePowerStatus(status));
        }
    }

    private void updateGUIWeatherStatus(final String status) {
        if (mainGUI != null) {
            SwingUtilities.invokeLater(() -> mainGUI.updateWeatherStatus(status));
        }
    }

    private void updateGUISignalStatus(final String status) {
        if (mainGUI != null) {
            SwingUtilities.invokeLater(() -> mainGUI.updateSignalStatus(status));
        }
    }

}