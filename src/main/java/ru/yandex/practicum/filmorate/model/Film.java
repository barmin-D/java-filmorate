package ru.yandex.practicum.filmorate.model;

import lombok.Data;



import java.time.Duration;
import java.time.LocalDate;

@Data
public class Film {
    private int id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Duration duration;
    private static int idInc=1;

    public Film(String name, String description, LocalDate releaseDate, Duration duration) {
        this.id = idInc++;
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
    }
}
