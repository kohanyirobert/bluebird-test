package com.github.kohanyirobert.bbt.model;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

// Ősosztály tábla leképzésekhez, ahol szükség van azonosítóra.
@MappedSuperclass
public abstract class AbstractModelWithId {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    protected AbstractModelWithId() {
    }

    public final Long getId() {
        return id;
    }

    public final void setId(Long id) {
        this.id = id;
    }
}
