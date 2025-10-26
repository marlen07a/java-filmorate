package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum DirectorSortBy {
    YEAR("year"),
    LIKES("likes"),
    RATE("rate");

    private final String value;

    DirectorSortBy(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @JsonCreator
    public static DirectorSortBy fromValue(String value) {
        for (DirectorSortBy sortBy : values()) {
            if (sortBy.value.equalsIgnoreCase(value)) {
                return sortBy;
            }
        }
        throw new IllegalArgumentException("Unknown sort type: " + value);
    }
}