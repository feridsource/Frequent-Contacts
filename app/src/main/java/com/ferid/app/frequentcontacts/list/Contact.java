package com.ferid.app.frequentcontacts.list;

import java.io.Serializable;

/**
 * Created by ferid.cafer on 11/12/2014.
 */
public class Contact implements Serializable {
    private int id;         //ID
    private String name;    //contact's name
    private String photo;   //contact photo
    private String number;  //phone number

    public Contact() {
        id = 0;
        name = "";
        photo = "";
        number = "";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
