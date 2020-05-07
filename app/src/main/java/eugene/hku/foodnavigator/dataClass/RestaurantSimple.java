package eugene.hku.foodnavigator.dataClass;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

public class RestaurantSimple {
    private String place_id;
    private String place_name;
    private double lat;
    private double lng;
    private String description;
    private String vicinity;
    private boolean operation;
    private int distance;
    private String icon;

    public RestaurantSimple() {
        this.setOperation(true);

    }

    public RestaurantSimple(String place_id, String place_name, double lat, double lng, String description, String vicinity, boolean operation, String icon) {
        this.place_id = place_id;
        this.place_name = place_name;
        this.lat = lat;
        this.lng = lng;
        this.description = description;
        this.vicinity = vicinity;
        this.operation = operation;
        this.icon = icon;
    }

    public String getPlace_id() {
        return place_id;
    }

    public void setPlace_id(String place_id) {
        this.place_id = place_id;
    }

    public String getPlace_name() {
        return place_name;
    }

    public void setPlace_name(String place_name) {
        this.place_name = place_name;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;

    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;

    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVicinity() {
        return vicinity;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }

    public boolean isOperation() {
        return operation;
    }

    public void setOperation(boolean operation) {
        this.operation = operation;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int calDistance(double mlat, double mlng){
        LatLng startLatLng = new LatLng(mlat, mlng);
        LatLng endLatLng = new LatLng(lat, lng);
        double distance = SphericalUtil.computeDistanceBetween(startLatLng, endLatLng);
        return (int)Math.round(distance);
    }
}
