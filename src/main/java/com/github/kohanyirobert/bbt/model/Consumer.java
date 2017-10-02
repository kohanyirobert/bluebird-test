package com.github.kohanyirobert.bbt.model;

import java.util.concurrent.TimeUnit;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

@Entity
@Table(catalog = "", schema = "bbt", name = "consumer")
public class Consumer extends AbstractModelWithId {

    public enum Type {
        OWEN,
        REFRIDGERATOR,
        WASHING_MACHINE,
        DISH_WASHER,
        COMPUTER,
        HAIR_DRYER,
        TELEVISION,
        RADIO_SET
    }

    @Column(unique = true)
    @Basic(optional = false)
    @Enumerated(EnumType.STRING)
    private Type type;

    @Basic(optional = false)
    private Long consumption;

    @Basic(optional = false)
    @Enumerated(EnumType.STRING)
    private TimeUnit rate;

    public Consumer() {
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Long getConsumption() {
        return consumption;
    }

    public void setConsumption(Long consumption) {
        this.consumption = consumption;
    }

    public TimeUnit getRate() {
        return rate;
    }

    public void setRate(TimeUnit rate) {
        this.rate = rate;
    }
}
