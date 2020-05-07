package eugene.hku.foodnavigator.dataClass;

public class Bookmark {
    private String restaurantId;
    private String userId;

    public Bookmark() {
        // empty constructor
    }

    public Bookmark(String restaurantId, String userId) {
        this.restaurantId = restaurantId;
        this.userId = userId;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
