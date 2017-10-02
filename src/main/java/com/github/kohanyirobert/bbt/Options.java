package com.github.kohanyirobert.bbt;

import com.beust.jcommander.Parameter;

// A program parancssoros paramétereit tartalmazó osztály. (http://jcommander.org/)
public final class Options {

    @Parameter(names = "--output-file", description = "Writes the output written to stdout to the specified file")
    private String outputFile = ".\\out.log";

    @Parameter(names = { "--min-streets" }, description = "Minimum number of streets per town.")
    private int minStreets = 1;

    @Parameter(names = { "--max-streets" }, description = "Maximum number of streets per town.")
    private int maxStreets = 10;

    @Parameter(names = { "--min-buildings" }, description = "Minimum number of buildings per street.")
    private int minBuildings = 1;

    @Parameter(names = { "--max-buildings" }, description = "Maximum number of buildings per street.")
    private int maxBuildings = 10;

    @Parameter(names = { "--min-houses" }, description = "Minimum number of houses per building.")
    private int minHouses = 1;

    @Parameter(names = { "--max-houses" }, description = "Maximum number of houses per building.")
    private int maxHouses = 10;

    @Parameter(names = { "--min-consumers" }, description = "Minimum number of consumers per house.")
    private int minConsumers = 1;

    @Parameter(names = { "--max-consumers" }, description = "Maximum number of consumers per house.")
    private int maxConsumers = 10;

    @Parameter(names = { "--min-consumption" }, description = "Minimum level of consumption per consumer.")
    private int minConsumption = 50;

    @Parameter(names = { "--max-consumption" }, description = "Maximum level of consumption per consumer.")
    private int maxConsumption = 100;

    @Parameter(names = { "--min-production" }, description = "Minimum level of production per producer.")
    private int minProduction = 0;

    @Parameter(names = { "--max-production" }, description = "Maximum level of production per producer.")
    private int maxProduction = 100;

    public Options() {
    }

    public String getOutputFile() {
        return outputFile;
    }

    public int getMinStreets() {
        return minStreets;
    }

    public int getMaxStreets() {
        return maxStreets;
    }

    public int getMinBuildings() {
        return minBuildings;
    }

    public int getMaxBuildings() {
        return maxBuildings;
    }

    public int getMinHouses() {
        return minHouses;
    }

    public int getMaxHouses() {
        return maxHouses;
    }

    public int getMinConsumers() {
        return minConsumers;
    }

    public int getMaxConsumers() {
        return maxConsumers;
    }

    public int getMinConsumption() {
        return minConsumption;
    }

    public int getMaxConsumption() {
        return maxConsumption;
    }

    public int getMinProduction() {
        return minProduction;
    }

    public int getMaxProduction() {
        return maxProduction;
    }
}
