package com.github.kohanyirobert.bbt.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(catalog = "", schema = "bbt", name = "house")
public class House extends AbstractModelWithName {

    @Basic(optional = false)
    private Integer number;

    @ManyToOne(cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            optional = false,
            targetEntity = Building.class)
    private Building building;

    @ManyToMany(cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            targetEntity = Consumer.class)
    @JoinTable(catalog = "",
            schema = "bbt",
            name = "house_consumer",
            joinColumns = @JoinColumn(name = "house_id", table = "house", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "consumer_id", table = "consumer", referencedColumnName = "id"))
    private Set<Consumer> consumers;

    @ManyToOne(cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            targetEntity = Producer.class)
    private Producer producer;

    public House() {
        consumers = new HashSet<>();
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Building getBuilding() {
        return building;
    }

    public void setBuilding(Building building) {
        this.building = building;
    }

    public Set<Consumer> getConsumers() {
        return consumers;
    }

    public void setConsumers(Set<Consumer> consumers) {
        this.consumers = consumers;
    }

    public Producer getProducer() {
        return producer;
    }

    public void setProducer(Producer producer) {
        this.producer = producer;
    }
}
