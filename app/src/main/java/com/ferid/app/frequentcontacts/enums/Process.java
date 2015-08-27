package com.ferid.app.frequentcontacts.enums;

/**
 * Created by ferid.cafer on 11/12/2014.
 */
public enum Process {
    CALL(0),
    CHANGE_PHOTO(1),
    DELETE_CONTACT(2);

    private final int value;

    private Process(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }
}
