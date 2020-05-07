package eugene.hku.foodnavigator.dataClass;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

public class Restaurant {

    private String place_id;
    private String place_name;
    private double lat;
    private double lng;
    private String description;
    private String address;
    private int rating_good;
    private int rating_ok;
    private int rating_bad;
    private boolean operation;
    private String time_open;
    private String time_close;
    private boolean open_now;
    private String icon;
    private int type;
    private int price;

    public Restaurant() {
        // empty constructor
    }

    public Restaurant(String place_id, String place_name, double lat, double lng, String description, String address, int rating_good, int rating_ok, int rating_bad, boolean operation, String time_open, String time_close, boolean open_now, String icon, int type, int price) {
        this.place_id = place_id;
        this.place_name = place_name;
        this.lat = lat;
        this.lng = lng;
        this.description = description;
        this.address = address;
        this.rating_good = rating_good;
        this.rating_ok = rating_ok;
        this.rating_bad = rating_bad;
        this.operation = operation;
        this.time_open = time_open;
        this.time_close = time_close;
        this.open_now = open_now;
        this.icon = icon;
        this.type = type;
        this.price = price;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getRating_good() {
        return rating_good;
    }

    public void setRating_good(int rating_good) {
        this.rating_good = rating_good;
    }

    public int getRating_ok() {
        return rating_ok;
    }

    public void setRating_ok(int rating_ok) {
        this.rating_ok = rating_ok;
    }

    public int getRating_bad() {
        return rating_bad;
    }

    public void setRating_bad(int rating_bad) {
        this.rating_bad = rating_bad;
    }

    public boolean isOperation() {
        return operation;
    }

    public void setOperation(boolean operation) {
        this.operation = operation;
    }

    public String getTime_open() {
        return time_open;
    }

    public void setTime_open(String time_open) {
        this.time_open = time_open;
    }

    public String getTime_close() {
        return time_close;
    }

    public void setTime_close(String time_close) {
        this.time_close = time_close;
    }

    public boolean isOpen_now() {
        return open_now;
    }

    public void setOpen_now(boolean open_now) {
        this.open_now = open_now;
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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
