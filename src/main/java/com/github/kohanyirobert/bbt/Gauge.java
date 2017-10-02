package com.github.kohanyirobert.bbt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;

import com.github.kohanyirobert.bbt.model.Building;
import com.github.kohanyirobert.bbt.model.Consumer;
import com.github.kohanyirobert.bbt.model.House;
import com.github.kohanyirobert.bbt.model.Producer;
import com.github.kohanyirobert.bbt.model.Street;
import com.github.kohanyirobert.bbt.model.Town;

// "Mérőszűszer". Összesítve lekérdezhetővé teszi az energia fogyasztást és
// termelést településenként/utcánként/épületenként/lakásonként.
public final class Gauge {

    // "Mérőműszer" építő - a megadott paraméterek alapján fogja a műszer
    // összesítve lekérdezni az energia fogyasztást és termelést.
    public static final class Builder {

        private final EntityManagerFactory emf;

        private final List<Town> towns;
        private final List<Street> streets;
        private final List<Building> buildings;
        private final List<House> houses;

        public Builder(EntityManagerFactory emf) {
            this.emf = emf;

            towns = new ArrayList<>();
            streets = new ArrayList<>();
            buildings = new ArrayList<>();
            houses = new ArrayList<>();
        }

        public Builder addTowns(Town... towns) {
            return addTowns(Arrays.asList(towns));
        }

        public Builder addTowns(Iterable<Town> towns) {
            for (Town town : towns) {
                addTown(town);
            }
            return this;
        }

        public Builder addTownById(Long townId) {
            Town town = new Town();
            town.setId(townId);
            return addTown(town);
        }

        public Builder addTownByName(String townName) {
            Town town = new Town();
            town.setName(townName);
            return addTown(town);
        }

        public Builder addTown(Town town) {
            if (town.getId() == null && town.getName() == null) {
                throw new IllegalArgumentException();
            }
            towns.add(town);
            return this;
        }

        public Builder addStreets(Street... streets) {
            return addStreets(Arrays.asList(streets));
        }

        public Builder addStreets(Iterable<Street> streets) {
            for (Street street : streets) {
                addStreet(street);
            }
            return this;
        }

        public Builder addStreetById(Long streetId) {
            Street street = new Street();
            street.setId(streetId);
            return addStreet(street);
        }

        public Builder addStreetByName(String streetName) {
            Street street = new Street();
            street.setName(streetName);
            return addStreet(street);
        }

        public Builder addStreet(Street street) {
            if (street.getId() == null && street.getName() == null) {
                throw new IllegalArgumentException();
            }
            streets.add(street);
            return this;
        }

        public Builder addBuildings(Building... buildings) {
            return addBuildings(Arrays.asList(buildings));
        }

        public Builder addBuildings(Iterable<Building> buildings) {
            for (Building building : buildings) {
                addBuilding(building);
            }
            return this;
        }

        public Builder addBuildingById(Long buildingId) {
            Building building = new Building();
            building.setId(buildingId);
            return addBuilding(building);
        }

        public Builder addBuildingByName(String buildingName) {
            Building building = new Building();
            building.setName(buildingName);
            return addBuilding(building);
        }

        public Builder addBuildingByNumber(Integer buildingNumber) {
            Building building = new Building();
            building.setNumber(buildingNumber);
            return addBuilding(building);
        }

        public Builder addBuilding(Building building) {
            if (building.getId() == null
                    && building.getName() == null
                    && building.getNumber() == null) {
                throw new IllegalArgumentException();
            }
            buildings.add(building);
            return this;
        }

        public Builder addHouses(House... houses) {
            return addHouses(Arrays.asList(houses));
        }

        public Builder addHouses(Iterable<House> houses) {
            for (House house : houses) {
                addHouse(house);
            }
            return this;
        }

        public Builder addHouseById(Long houseId) {
            House house = new House();
            house.setId(houseId);
            return addHouse(house);
        }

        public Builder addHouseByName(String houseName) {
            House house = new House();
            house.setName(houseName);
            return addHouse(house);
        }

        public Builder addHouseByNumber(Integer houseNumber) {
            House house = new House();
            house.setNumber(houseNumber);
            return addHouse(house);
        }

        public Builder addHouse(House house) {
            if (house.getId() == null
                    && house.getName() == null
                    && house.getNumber() == null) {
                throw new IllegalArgumentException();
            }
            houses.add(house);
            return this;
        }

        public Gauge build() {
            return new Gauge(emf, towns, streets, buildings, houses);
        }
    }

    private final EntityManagerFactory emf;

    private final Iterable<Town> towns;
    private final Iterable<Street> streets;
    private final Iterable<Building> buildings;
    private final Iterable<House> houses;

    private AtomicBoolean initialized;
    private Long consumption;
    private Long production;

    private Gauge(EntityManagerFactory emf,
            Iterable<Town> towns,
            Iterable<Street> streets,
            Iterable<Building> buildings,
            Iterable<House> houses) {
        this.emf = emf;
        this.towns = towns;
        this.streets = streets;
        this.buildings = buildings;
        this.houses = houses;

        initialized = new AtomicBoolean(false);
        consumption = null;
        production = null;
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }

    public Long getConsumption() {
        return getConsumption(false);
    }

    public Long getConsumption(boolean subtractProduction) {
        if (initialized.compareAndSet(false, true)) {
            initialize();
        }
        if (subtractProduction) {
            return consumption - production;
        }
        return consumption;
    }

    public Long getProduction() {
        return getProduction(false);
    }

    public Long getProduction(boolean subtractConsumption) {
        if (initialized.compareAndSet(false, true)) {
            initialize();
        }
        if (subtractConsumption) {
            return production - consumption;
        }
        return production;
    }

    // A rendelkezésre álló paraméterek alapján dinamikusan lekérdezi
    // az energia fogyasztást és termelést.
    private void initialize() {
        EntityManager em = emf.createEntityManager();
        try {
            for (Tuple tuple : query(em).getResultList()) {
                consumption = consumption == null
                        ? tuple.get("consumption", Long.class)
                        : consumption + tuple.get("consumption", Long.class);

                production = production == null
                        ? tuple.get("production", Long.class)
                        : production + tuple.get("production", Long.class);
            }
        } finally {
            if (em.isOpen()) {
                em.close();
            } else {
                throw new IllegalStateException();
            }
        }
    }

    private TypedQuery<Tuple> query(EntityManager em) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();

        Root<Town> townFrom = cq.from(Town.class);
        SetJoin<Town, Street> streetsJoin = townFrom.joinSet("streets");
        SetJoin<Street, Building> buildingsJoin = streetsJoin.joinSet("buildings");
        SetJoin<Building, House> housesJoin = buildingsJoin.joinSet("houses");
        SetJoin<House, Consumer> consumersJoin = housesJoin.joinSet("consumers");
        Join<House, Producer> producerJoin = housesJoin.join("producer");

        Path<Long> townIdPath = townFrom.get("id");
        Path<Long> streetIdPath = streetsJoin.get("id");
        Path<Long> buildingIdPath = buildingsJoin.get("id");
        Path<Long> houseIdPath = housesJoin.get("id");

        Path<String> townNamePath = townFrom.get("name");
        Path<String> streetNamePath = streetsJoin.get("name");
        Path<String> buildingNamePath = buildingsJoin.get("name");
        Path<String> houseNamePath = housesJoin.get("name");

        Path<String> buildingNumberPath = buildingsJoin.get("number");
        Path<String> houseNumberPath = housesJoin.get("number");

        Path<Long> consumptionPath = consumersJoin.get("consumption");
        Path<Long> productionPath = producerJoin.get("production");

        Predicate finalPredicate = null;

        // Town
        Predicate townPredicate = null;
        for (Town town : towns) {
            Predicate equalPredicate;
            if (town.getId() != null) {
                equalPredicate = cb.equal(townIdPath, town.getId());
            } else if (town.getName() != null) {
                equalPredicate = cb.equal(townNamePath, town.getName());
            } else {
                throw new IllegalStateException();
            }

            if (townPredicate == null) {
                townPredicate = equalPredicate;
            } else {
                townPredicate = cb.or(townPredicate, equalPredicate);
            }
        }

        if (townPredicate != null) {
            finalPredicate = townPredicate;
        }

        // Street
        Predicate streetPredicate = null;
        for (Street street : streets) {
            Predicate equalPredicate;
            if (street.getId() != null) {
                equalPredicate = cb.equal(streetIdPath, street.getId());
            } else if (street.getName() != null) {
                equalPredicate = cb.equal(streetNamePath, street.getName());
            } else {
                throw new IllegalStateException();
            }

            if (streetPredicate == null) {
                streetPredicate = equalPredicate;
            } else {
                streetPredicate = cb.or(streetPredicate, equalPredicate);
            }
        }

        if (streetPredicate != null) {
            if (finalPredicate == null) {
                finalPredicate = streetPredicate;
            } else {
                finalPredicate = cb.and(finalPredicate, streetPredicate);
            }
        }

        // Building
        Predicate buildingPredicate = null;
        for (Building building : buildings) {
            Predicate equalPredicate;
            if (building.getId() != null) {
                equalPredicate = cb.equal(buildingIdPath, building.getId());
            } else if (building.getName() != null) {
                equalPredicate = cb.equal(buildingNamePath, building.getName());
            } else if (building.getNumber() != null) {
                equalPredicate = cb.equal(buildingNumberPath, building.getNumber());
            } else {
                throw new IllegalStateException();
            }

            if (buildingPredicate == null) {
                buildingPredicate = equalPredicate;
            } else {
                buildingPredicate = cb.or(buildingPredicate, equalPredicate);
            }
        }

        if (buildingPredicate != null) {
            if (finalPredicate == null) {
                finalPredicate = buildingPredicate;
            } else {
                finalPredicate = cb.and(finalPredicate, buildingPredicate);
            }
        }

        // House
        Predicate housePredicate = null;
        for (House house : houses) {
            Predicate equalPredicate;
            if (house.getId() != null) {
                equalPredicate = cb.equal(houseIdPath, house.getId());
            } else if (house.getName() != null) {
                equalPredicate = cb.equal(houseNamePath, house.getName());
            } else if (house.getNumber() != null) {
                equalPredicate = cb.equal(houseNumberPath, house.getNumber());
            } else {
                throw new IllegalStateException();
            }

            if (housePredicate == null) {
                housePredicate = equalPredicate;
            } else {
                housePredicate = cb.or(housePredicate, equalPredicate);
            }
        }

        if (housePredicate != null) {
            if (finalPredicate == null) {
                finalPredicate = housePredicate;
            } else {
                finalPredicate = cb.and(finalPredicate, housePredicate);
            }
        }

        if (finalPredicate != null) {
            cq.where(finalPredicate);
        }

        // Mivel a lakás azonosítókon történik a csoportosítás és a
        // termelés értékből minden lakáshoz csak egy tartozik, ezért
        // kiválasztjuk a legkisebbet (nyilván a legnagyobb is jó lenne), a
        // fogyasztás értékeket pedig szummázuk.
        cq.multiselect(cb.sum(consumptionPath).alias("consumption"),
                cb.min(productionPath).alias("production"));

        cq.groupBy(houseIdPath);

        return em.createQuery(cq);
    }
}
