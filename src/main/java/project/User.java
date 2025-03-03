package project;

public class User {
    private String username;
    private String password;
    private boolean premium;

    public User(String username, String password, boolean premium) {
        this.username = username;
        this.password = password;
        this.premium = premium;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isPremium() {
        return premium;
    }

    public void setPremium(boolean premium) {
        this.premium = premium;
    }
}
