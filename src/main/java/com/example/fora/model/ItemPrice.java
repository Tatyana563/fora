package com.example.fora.model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table
public class ItemPrice {

    private int id;

    @ManyToOne
    private City city;
    @ManyToOne
    private Item item;

    private Double price;

    // kiev ref 123
    // nikolaev ref 321
    // kiev phone 111
    // nikolaev phone 111
}
