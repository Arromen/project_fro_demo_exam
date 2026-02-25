package org.example.model;

public class User {
    private int id;
    private String fio;
    private String login;
    private String roleName;

    public User(int id, String fio, String login, String roleName) {
        this.id = id;
        this.fio = fio;
        this.login = login;
        this.roleName = roleName;
    }

    public int getId() { return id; }
    public String getFio() { return fio; }
    public String getLogin() { return login; }
    public String getRoleName() { return roleName; }

    public boolean isAdmin() { return "Администратор".equals(roleName); }
    public boolean isManager() { return "Менеджер".equals(roleName); }
    public boolean isClient() { return "Авторизированный клиент".equals(roleName); }
}