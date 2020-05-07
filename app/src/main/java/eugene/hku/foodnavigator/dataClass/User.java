package eugene.hku.foodnavigator.dataClass;

public class User {
    private String userID; // system generated, cannot be modified once user created
    private String userName;
    private String password;
    private String icon;
    public User() {}

    public User(String userID, String userName, String password, String icon) {
        this.userID = userID;
        this.userName = userName;
        this.password = password;
        this.icon = icon;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
