package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class User {
    private int id;
    private String login;
    private String name;
    private String email;
    private LocalDate birthday;
    private static int idInc = 1;

    public User(String login, String name, String email, LocalDate birthday) {
        this.id = idInc++;
        this.login = login;
        this.name = name;
        this.email = email;
        this.birthday = birthday;
    }
}
