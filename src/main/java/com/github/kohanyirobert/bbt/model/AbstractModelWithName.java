package com.github.kohanyirobert.bbt.model;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

//Ősosztály tábla leképzésekhez, ahol numerikus azonosítón felül
// egyedi szöveges azonosítóra is szükség van.
@MappedSuperclass
public abstract class AbstractModelWithName extends AbstractModelWithId {

    @Column(unique = true)
    @Basic(optional = false)
    private String name;

    protected AbstractModelWithName() {
    }

    public final String getName() {
        return name;
    }

    public final void setName(String name) {
        this.name = name;
    }
}
