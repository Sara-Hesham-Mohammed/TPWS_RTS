import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;

public class TPWSController {
    private String controllerID;
    private double currentSpeed;
    private double safeDistance;


    public Thread EsperRun(EPServiceProvider engine, Sensor sensor){
        System.out.println("ESPER RUN");
        String[] sensorString = String.valueOf(sensor).split("@");
        // Creating EPL statement
        EPStatement speed_select_statement = engine
                .getEPAdministrator()
                .createEPL("select lastReading from " + sensorString[0]);

        // Attaching callback to EPL statements
        speed_select_statement.setSubscriber(new Object() {
            public void update(double lastReading) {
                System.out.println("\n NEW SENSOR READING: " +  lastReading);
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
        // Implementation
    }
}