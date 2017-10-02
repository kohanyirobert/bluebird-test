package com.github.kohanyirobert.bbt.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(catalog = "", schema = "bbt", name = "street")
public class Street extends AbstractModelWithName {

    @ManyToOne(cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            optional = false,
            targetEntity = Town.class)
    private Town town;

    @OneToMany(cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            mappedBy = "street",
            targetEntity = Building.class)
    private Set<Building> buildings;

    public Street() {
        buildings = new HashSet<Building>();
    }

    public Town getTown() {
        return town;
    }

    public void setTown(Town town) {
        this.town = town;
    }

    public Set<Building> getBuildings() {
        return buildings;
    }

    public void setBuildings(Set<Building> buildings) {
        this.buildings = buildings;
    }
}
