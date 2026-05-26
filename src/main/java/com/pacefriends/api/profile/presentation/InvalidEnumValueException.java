package com.pacefriends.api.profile.presentation;

public class InvalidEnumValueException extends RuntimeException {

    private final String field;
    private final String value;

    public InvalidEnumValueException(String field, String value) {
        super("Valor invalido para o campo '" + field + "': " + value);
        this.field = field;
        this.value = value;
    }

    public String getField() {
        return field;
    }

    public String getValue() {
        return value;
    }
}
