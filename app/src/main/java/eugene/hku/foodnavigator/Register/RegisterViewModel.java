package eugene.hku.foodnavigator.Register;

import androidx.lifecycle.ViewModel;

public class RegisterViewModel extends ViewModel {

    private String login;
    private String password;
    private String confirmPW;
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

    public String getConfirmPW() {
        return confirmPW;
    }

    public void setConfirmPW(String confirmPW) {
        confirmPW = confirmPW;
    }

}
