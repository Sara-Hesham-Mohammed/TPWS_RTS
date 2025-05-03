package Components;

public class GPSModule {
    private double latitude;
    private double longitude;
    private double speed;  // in km/h

    public GPSModule(double initialLatitude, double initialLongitude, double initialSpeed) {
        this.latitude = initialLatitude;
        this.longitude = initialLongitude;
        this.speed = initialSpeed;
    }

    // simulate GPS data update (every 250 ms)
    public void updateGPS(double newLatitude, double newLongitude, double newSpeed) {
        this.latitude = newLatitude;
        this.longitude = newLongitude;
        this.speed = newSpeed;
        System.out.println("GPS Updated: Latitude = " + latitude + ", Longitude = " + longitude + ", Speed = " + speed + " km/h");
    }

    public double getSpeed() {
        return speed;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
