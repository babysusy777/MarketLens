package it.unipi.MarketLens.dto;

public class ModifyPasswordRequest {
    private String username;   // identificativo
    private String password;   // nuova password (opzionale)

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
