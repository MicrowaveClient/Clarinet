package me.nuf.glade.screens.accountmanager;

public class Account {

    private String username, password;
    private boolean premium;

    public Account(String username, String password) {
        this.premium = true;
        this.username = username;
        this.password = password;
    }

    public Account(String username) {
        this.premium = false;
        this.username = username;
        this.password = "N/A";
    }

    public String getFileLine() {
        return premium ? username.concat(":").concat(password) : username;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() throws AccountException {
        if (premium) {
            return password;
        } else {
            throw new AccountException("Non-Premium accounts do not have passwords!");
        }
    }

    public boolean isPremium() {
        return premium;
    }

}
