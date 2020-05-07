package eugene.hku.foodnavigator.dataClass;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Review implements Comparable<Review>{

    private String restaurantId;
    private String userID;
    private String content;

    private Date created;

    private int rating; // 0|1|2 -> bad | ok | good

    public Review () {

    }

    public Review(String restaurantId, String userID, String content, Date created, int rating) {
        this.restaurantId = restaurantId;
        this.userID = userID;
        this.content = content;
        this.created = created;
        this.rating = rating;


    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    @Override
    public int compareTo (Review review) {
        int result;
        int dresult = this.getCreated().compareTo(review.getCreated());
        if (dresult < 0) {
            result = 1;
        } else {
            if (dresult > 0) {
                result = -1;
            } else {
                result = 0;
            }
        }
        return result;
    }
}
