package com.github.kohanyirobert.bbt;

import org.testng.Assert;
import org.testng.annotations.Test;

// Az src/test/resources/db.sql-ben vannak a releváns elvárt adatok.
public class GaugeTest extends AbstractTest {

    public GaugeTest() {
    }

    @Test
    public void reuse() {
        Gauge gauge = new Gauge.Builder(EMF).addHouseById(1L)
                .build();

        assertConsumption(gauge, 58L);
        assertConsumption(gauge, 58L);
        assertConsumption(gauge, 58L);
    }

    @Test
    public void house1() {
        assertConsumption(new Gauge.Builder(EMF).addHouseById(1L), 58L);
    }

    @Test
    public void house2() {
        assertConsumption(new Gauge.Builder(EMF).addHouseById(2L), 15L);
    }

    @Test
    public void building1() {
        assertConsumption(new Gauge.Builder(EMF).addBuildingById(1L), 73L);
    }

    @Test
    public void building2() {
        assertConsumption(new Gauge.Builder(EMF).addBuildingById(2L), 69L);
    }

    @Test
    public void building3() {
        assertConsumption(new Gauge.Builder(EMF).addBuildingById(3L), 172L);
    }

    @Test
    public void street1() {
        assertConsumption(new Gauge.Builder(EMF).addStreetById(1L), 73L);
    }

    @Test
    public void street2() {
        assertConsumption(new Gauge.Builder(EMF).addStreetById(2L), 241L);
    }

    @Test
    public void town1() {
        assertConsumption(new Gauge.Builder(EMF).addTownById(1L), 314L);
    }

    private static void assertConsumption(Gauge.Builder actual, long expected) {
        assertConsumption(actual.build(), expected);
    }

    private static void assertConsumption(Gauge actual, long expected) {
        Assert.assertEquals(actual.getConsumption(true).longValue(), expected);
    }
}
