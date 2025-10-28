package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum SearchBy {
    TITLE("title"),
    DIRECTOR("director");

    private final String value;

    SearchBy(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @JsonCreator
    public static SearchBy fromValue(String value) {
        for (SearchBy searchBy : values()) {
            if (searchBy.value.equalsIgnoreCase(value)) {
                return searchBy;
            }
        }
        throw new IllegalArgumentException("Unknown search type: " + value);
    }
}
