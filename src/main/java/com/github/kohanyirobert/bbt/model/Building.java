package com.github.kohanyirobert.bbt.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(catalog = "", schema = "bbt", name = "building")
public class Building extends AbstractModelWithName {

    @Basic(optional = false)
    private Integer number;

    @ManyToOne(cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            optional = false,
            targetEntity = Street.class)
    private Street street;

    @OneToMany(cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            mappedBy = "building",
            targetEntity = House.class)
    private Set<House> houses;

    public Building() {
        houses = new HashSet<House>();
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Street getStreet() {
        return street;
    }

    public void setStreet(Street street) {
        this.street = street;
    }

    public Set<House> getHouses() {
        return houses;
    }

    public void setHouses(Set<House> houses) {
        this.houses = houses;
    }
}
