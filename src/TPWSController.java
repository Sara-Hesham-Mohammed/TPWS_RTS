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

    public void monitorConditions() {
        // Implementation
    }

    public void checkBrakes() {
        // Implementation
    }

    public void reduceSpeed() {
        // Implementation
    }

    public void activateWarningSound() {
        // Implementation
    }
}