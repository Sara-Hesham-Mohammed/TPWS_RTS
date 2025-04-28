//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class main {
    public static void main(String[] args) {
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        System.out.printf("Hello and welcome!");

        for (int i = 1; i <= 5; i++) {
            //TIP Press <shortcut actionId="Debug"/> to start debugging your code. We have set one <icon src="AllIcons.Debugger.Db_set_breakpoint"/> breakpoint
            // for you, but you can always add more by pressing <shortcut actionId="ToggleLineBreakpoint"/>.
            System.out.println("i = " + i);
        }

                // Create sensor instances
                SpeedSensor speedSensor = new SpeedSensor(1, 100); // ID 1, speed limit 100 km/h
                DistanceSensor distanceSensor = new DistanceSensor(2);
                WeatherSensor weatherSensor = new WeatherSensor(3);
                BrakeStatusSensor brakeSensor = new BrakeStatusSensor(4);

                // Test each sensor
                System.out.println("=== Testing Speed Sensor ===");
                testSensor(speedSensor, 5);

                System.out.println("\n=== Testing Distance Sensor ===");
                testSensor(distanceSensor, 5);

                System.out.println("\n=== Testing Weather Sensor ===");
                for(int i = 0; i < 5; i++) {
                    System.out.println("Weather: " + weatherSensor.detectWeather());
                }

                System.out.println("\n=== Testing Brake Status Sensor ===");
                for(int i = 0; i < 10; i++) { // Test more times due to low probability
                    System.out.println("Brakes applied: " + brakeSensor.getBrakeStatus());
                }

                // Test deactivation
                System.out.println("\n=== Testing Deactivation ===");
                speedSensor.deactivate();
                System.out.println("Speed reading (inactive): " + speedSensor.measureSpeed());
                speedSensor.activate();
                System.out.println("Speed reading (reactivated): " + speedSensor.measureSpeed());
            }

            private static void testSensor(Sensor sensor, int iterations) {
                for (int i = 0; i < iterations; i++) {
                    System.out.println(sensor.getType() + " reading: " + sensor.readData());
                }
            }
            
    }
}