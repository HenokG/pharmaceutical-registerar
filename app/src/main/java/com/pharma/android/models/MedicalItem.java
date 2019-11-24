package com.pharma.android.models;

import java.text.DateFormat;
import java.text.Format;
import java.util.Date;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class MedicalItem {

    @Id
    private long id;
    private String name;
    private int quantity;
    private Date expireDate;

    public MedicalItem(String name, int quantity, Date expireDate) {
        this.name = name;
        this.quantity = quantity;
        this.expireDate = expireDate;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return this.quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Date getExpireDate() {
        return this.expireDate;
    }

    public String getExpireDateString() {
        Format formatter = DateFormat.getDateInstance();
        return String.valueOf(formatter.format(this.expireDate));
    }

    public void setExpireDate(Date expireDate) {
        this.expireDate = expireDate;
    }
}
