package com.github.kohanyirobert.bbt.model;

import java.util.concurrent.TimeUnit;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

@Entity
@Table(catalog = "", schema = "bbt", name = "producer")
public class Producer extends AbstractModelWithId {

    public enum Type {
        SOLAR,
        WIND,
        HYDRO
    }

    @Column(unique = true)
    @Basic(optional = false)
    @Enumerated(EnumType.STRING)
    private Type type;

    @Basic(optional = false)
    private Long production;

    @Basic(optional = false)
    @Enumerated(EnumType.STRING)
    private TimeUnit rate;

    public Producer() {
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Long getProduction() {
        return production;
    }

    public void setProduction(Long production) {
        this.production = production;
    }

    public TimeUnit getRate() {
        return rate;
    }

    public void setRate(TimeUnit rate) {
        this.rate = rate;
    }
}
