package eugene.hku.foodnavigator.Login;

import androidx.lifecycle.ViewModel;

public class LoginViewModel extends ViewModel {

    private String login;
    private String password;
    public void setLogin(String loginName){
        login = loginName;
    }
    public String getLogin() {
        return login;
    }

    public void setPassword(String pw) {
        password = pw;
    }
    public String getPassword() {
        return password;
    }
}
