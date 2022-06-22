package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Collection;

@Data
@Builder
public class Film {
    private int id;
    private String name;
    private LocalDate releaseDate;
    private String description;
    private Integer duration;
    private Integer rate;
    private Mpa mpa;
    private Collection<Genre> genres;

    public Film(int id, String name, LocalDate releaseDate, String description, Integer duration, Integer rate,
                Mpa mpa, Collection<Genre> genres) {
        this.id = id;
        this.name = name;
        this.releaseDate = releaseDate;
        this.description = description;
        this.duration = duration;
        this.rate = rate;
        this.mpa = mpa;
        this.genres = genres;
    }
}
