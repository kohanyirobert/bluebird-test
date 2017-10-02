package com.github.kohanyirobert.bbt.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(catalog = "", schema = "bbt", name = "town")
public class Town extends AbstractModelWithName {

    @OneToMany(cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            mappedBy = "town",
            targetEntity = Street.class)
    private Set<Street> streets;

    public Town() {
        streets = new HashSet<>();
    }

    public Set<Street> getStreets() {
        return streets;
    }

    public void setStreets(Set<Street> streets) {
        this.streets = streets;
    }
}
