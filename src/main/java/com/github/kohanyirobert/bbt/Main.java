package com.github.kohanyirobert.bbt;

import java.io.Console;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import com.beust.jcommander.JCommander;
import com.github.kohanyirobert.bbt.model.Building;
import com.github.kohanyirobert.bbt.model.Consumer;
import com.github.kohanyirobert.bbt.model.House;
import com.github.kohanyirobert.bbt.model.Producer;
import com.github.kohanyirobert.bbt.model.Street;
import com.github.kohanyirobert.bbt.model.Town;
import com.github.kohanyirobert.bbt.util.Names;
import com.github.kohanyirobert.bbt.util.Resources;

public final class Main {

    private static final Random RND;
    private static final EntityManagerFactory EMF;
    private static final EntityManager EM;
    private static final EntityTransaction TX;

    private static final List<String> TABLE_LABELS;
    private static final String HORIZONTAL_TABLE_LABEL_SEPARATOR;
    private static final String VERTICAL_TABLE_LABEL_SEPARATOR;

    private static final Options OPTIONS;
    private static final JCommander COMMANDER;

    // Ha van megadva "--output-file" paraméter, akkor *ez* arra a fájlra fog
    // mutatni, a fájlba pedig minden belekerül, ami a standard kimenetre
    // íródik.
    private static PrintStream outputFilePrintStream;

    private static List<Consumer> consumers;
    private static List<Producer> producers;

    static {
        System.out.println("Initializing in-memory object-relational mappings, creating database schema.");

        RND = new Random();
        EMF = Persistence.createEntityManagerFactory("bluebird-test");
        EM = EMF.createEntityManager();
        TX = EM.getTransaction();

        TABLE_LABELS = Arrays.asList("Town", "Street", "Building", "House", "Consumers", "Producers");
        HORIZONTAL_TABLE_LABEL_SEPARATOR = "-";
        VERTICAL_TABLE_LABEL_SEPARATOR = ":";

        OPTIONS = new Options();
        COMMANDER = new JCommander(OPTIONS);
    }

    public static void main(String[] args) throws IOException {
        COMMANDER.parse(args);

        // Ez itt egy nagyon fapados *tee*.
        if (OPTIONS.getOutputFile() != null || !"".equals(OPTIONS.getOutputFile())) {
            outputFilePrintStream = new PrintStream(new FileOutputStream(OPTIONS.getOutputFile()));
            System.setOut(new PrintStream(System.out) {

                @Override
                public void println(String x) {
                    super.println(x);
                    outputFilePrintStream.println(x);
                }

                @Override
                public PrintStream format(String format, Object... args) {
                    PrintStream ps = super.format(format, args);
                    outputFilePrintStream.format(format, args);
                    return ps;
                }
            });
        }

        consumers = new ArrayList<>();
        for (Consumer.Type type : Consumer.Type.values()) {
            Consumer consumer = new Consumer();
            consumer.setType(type);
            consumer.setConsumption(Long.valueOf(getConsumption()));
            consumer.setRate(TimeUnit.DAYS);
            consumers.add(consumer);
        }

        producers = new ArrayList<>();
        for (Producer.Type type : Producer.Type.values()) {
            Producer producer = new Producer();
            producer.setType(type);
            producer.setProduction(Long.valueOf(getProduction()));
            producer.setRate(TimeUnit.DAYS);
            producers.add(producer);
        }

        System.out.println("Inserting random data into the previously created in-memory database.");
        beforeMain();

        System.out.println("Waiting for user input. (Write \"help\" and press enter for the available commands.)");
        loopMain(args);

        afterMain();
        System.out.println("Exiting.");
    }

    // Feltölti a memóriában lévő adatbázist fiktív adatokkal.
    private static void beforeMain() {
        TX.begin();

        Town town = new Town();
        town.setName(Names.nextTownName());

        for (int i = 0; i < getStreets(); ++i) {
            Street street = new Street();
            street.setName(Names.nextStreetName());

            street.setTown(town);
            town.getStreets().add(street);

            for (int j = 0; j < getBuildings(); ++j) {
                Building building = new Building();
                building.setName(Names.nextLastName() + " Building");
                building.setNumber(j + 1);

                building.setStreet(street);
                street.getBuildings().add(building);

                for (int k = 0; k < getHouses(); ++k) {
                    House house = new House();
                    house.setName(Names.nextLastName() + " Residence");
                    house.setNumber(k + 1);

                    house.setBuilding(building);
                    building.getHouses().add(house);

                    for (int l = 0; l < getConsumers(); ++l) {
                        house.getConsumers().add(getConsumer());
                    }

                    house.setProducer(getProducer());
                }
            }
        }

        EM.persist(town);
        TX.commit();
    }

    private static int getStreets() {
        return getBetween(OPTIONS.getMinStreets(), OPTIONS.getMaxStreets());
    }

    private static int getBuildings() {
        return getBetween(OPTIONS.getMinBuildings(), OPTIONS.getMaxBuildings());
    }

    private static int getHouses() {
        return getBetween(OPTIONS.getMinBuildings(), OPTIONS.getMaxBuildings());
    }

    private static int getConsumers() {
        return getBetween(OPTIONS.getMinConsumers(), OPTIONS.getMaxConsumers());
    }

    private static int getConsumption() {
        return getBetween(OPTIONS.getMinConsumption(), OPTIONS.getMaxConsumption());
    }

    private static int getProduction() {
        return getBetween(OPTIONS.getMinProduction(), OPTIONS.getMaxProduction());
    }

    private static Consumer getConsumer() {
        return consumers.get(RND.nextInt(consumers.size()));
    }

    private static Producer getProducer() {
        return producers.get(RND.nextInt(producers.size()));
    }

    private static int getBetween(int minInclusive, int maxInclusive) {
        if (minInclusive > maxInclusive) {
            throw new IllegalArgumentException();
        } else if (minInclusive == maxInclusive) {
            return minInclusive;
        }
        return RND.nextInt(maxInclusive - minInclusive + 1) + minInclusive;
    }

    // Ez felel a parancssoros kommunikációért - nem embernek való, de azért
    // működik.
    private static void loopMain(String[] args) {
        Town town = EM.find(Town.class, 1L);

        Console console = System.console();
        @SuppressWarnings("resource")
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String line = console == null
                    ? scanner.nextLine()
                    : console.readLine();

            if (line.matches("(?i)^.*?help.*$")) {
                System.out.println(Resources.getResourceAsString("/help.txt"));

            } else if (line.matches("(?i)^.*?(vertical table|vert).*$")) {
                System.out.println(buildTable(false));

            } else if (line.matches("(?i)^.*?(horizontal table|table).*$")) {
                System.out.println(buildTable(true));

            } else if (line.matches("(?i)^.*?town.*$")) {
                Gauge gauge = new Gauge.Builder(EMF)
                        .addTown(town)
                        .build();

                System.out.format("Town: %s, Consumption: %s%n",
                        town.getName(),
                        gauge.getConsumption(true));

            } else if (line.matches("(?i)^.*?street.*$")) {
                Matcher matcher = Pattern.compile("(?i)^.*?street (.*)$").matcher(line);
                if (matcher.matches()) {
                    String streetName = matcher.group(1);

                    Gauge gauge = new Gauge.Builder(EMF)
                            .addStreetByName(streetName)
                            .build();

                    System.out.format("Street: %s, Consumption: %s%n",
                            streetName,
                            gauge.getConsumption() == null
                                    ? "n/a"
                                    : gauge.getConsumption(true));

                } else {
                    for (Street street : town.getStreets()) {
                        Gauge gauge = new Gauge.Builder(EMF)
                                .addTown(town)
                                .addStreet(street)
                                .build();

                        System.out.format("Town: %s, Street: %s, Consumption: %s%n",
                                town.getName(),
                                street.getName(),
                                gauge.getConsumption(true));
                    }
                }

            } else if (line.matches("(?i)^.*?building.*$")) {
                Matcher matcher = Pattern.compile("(?i)^.*?building (.*)$").matcher(line);
                if (matcher.matches()) {
                    String buildingName = matcher.group(1);

                    Gauge gauge = new Gauge.Builder(EMF)
                            .addBuildingByName(buildingName)
                            .build();

                    System.out.format("Building: %s, Consumption: %s%n",
                            buildingName,
                            gauge.getConsumption() == null
                                    ? "n/a"
                                    : gauge.getConsumption(true));

                } else {
                    for (Street street : town.getStreets()) {
                        for (Building building : street.getBuildings()) {
                            Gauge gauge = new Gauge.Builder(EMF)
                                    .addTown(town)
                                    .addStreet(street)
                                    .addBuilding(building)
                                    .build();

                            System.out.format("Town: %s, Street: %s, Building: %s, Consumption: %s%n",
                                    town.getName(),
                                    street.getName(),
                                    building.getName(),
                                    gauge.getConsumption(true));
                        }
                    }
                }

            } else if (line.matches("(?i)^.*?house.*$")) {
                Matcher matcher = Pattern.compile("(?i)^.*?house (.*)$").matcher(line);
                if (matcher.matches()) {
                    String houseName = matcher.group(1);

                    Gauge gauge = new Gauge.Builder(EMF)
                            .addHouseByName(houseName)
                            .build();

                    System.out.format("House: %s, Consumption: %s%n",
                            houseName,
                            gauge.getConsumption() == null
                                    ? "n/a"
                                    : gauge.getConsumption(true));

                } else {
                    for (Street street : town.getStreets()) {
                        for (Building building : street.getBuildings()) {
                            for (House house : building.getHouses()) {
                                Gauge gauge = new Gauge.Builder(EMF)
                                        .addTown(town)
                                        .addStreet(street)
                                        .addBuilding(building)
                                        .addHouse(house)
                                        .build();

                                System.out.format("Town: %s, Street: %s, Building: %s, House: %s, Consumption: %s%n",
                                        town.getName(),
                                        street.getName(),
                                        building.getName(),
                                        house.getName(),
                                        gauge.getConsumption(true));
                            }
                        }
                    }
                }

            } else if (line.matches("(?i)^.*?usage.*$")) {
                COMMANDER.usage();

            } else if (line.matches("(?i)^.*?(args|params).*$")) {
                if (args.length == 0) {
                    System.out.println("No arguments were supplied to invoke the program.");
                } else {
                    for (String arg : args) {
                        if (arg.startsWith("--")) {
                            System.out.print(arg + ": ");
                        } else {
                            System.out.println(arg);
                        }
                    }
                }

            } else if (line.matches("(?i)^.*?(quit|exit).*$")) {
                break;

            } else {
                System.out.println("Unknown command: " + line);
                continue;
            }

            System.out.println("Waiting for user input.");
        }
    }

    // Ez nagyon szofisztikált táblázat összehányó függvény.
    private static String buildTable(boolean horizontal) {
        // Adatok begyűjtése.
        List<List<String>> table = new ArrayList<>();
        {
            Town town = EM.find(Town.class, 1L);
            for (Street street : town.getStreets()) {
                for (Building building : street.getBuildings()) {
                    for (House house : building.getHouses()) {
                        StringBuilder consumerSb = new StringBuilder();

                        Set<Consumer> consumers = house.getConsumers();
                        for (Consumer consumer : consumers) {
                            consumerSb.append(String.valueOf(consumer.getType()));
                            consumerSb.append("(");
                            consumerSb.append(consumer.getConsumption());
                            consumerSb.append(")");
                            consumerSb.append(", ");
                        }

                        int start = consumerSb.length() - 2;
                        int end = consumerSb.length();
                        if (", ".equals(consumerSb.substring(start, end))) {
                            consumerSb.delete(start, end);
                        }

                        Producer producer = house.getProducer();

                        table.add(Arrays.asList(
                                town.getName(),
                                street.getName(),
                                building.getName(),
                                house.getName(),
                                consumerSb.toString(),
                                String.format("%s(%s)", producer.getType(), producer.getProduction())));
                    }
                }
            }
        }

        // Ha fekvő a táblázat, akkor a tetejére kell rakni az oszlopok nevét.
        if (horizontal) {
            List<String> labelSeparators = new ArrayList<>();
            for (String label : TABLE_LABELS) {
                StringBuilder labelSeparatorSb = new StringBuilder();
                for (int i = 0; i < label.length(); ++i) {
                    labelSeparatorSb.append(HORIZONTAL_TABLE_LABEL_SEPARATOR);
                }
                labelSeparators.add(labelSeparatorSb.toString());
            }
            table.add(0, labelSeparators);
            table.add(0, TABLE_LABELS);
        }

        // Tábla formátum építés.
        String format;
        {
            StringBuilder formatSb = new StringBuilder();

            if (horizontal) {
                int length = table.get(0).size();
                int[] widths = new int[length];
                for (List<String> row : table) {
                    for (int i = 0; i < length; ++i) {
                        String cell = row.get(i);
                        if (widths[i] < cell.length()) {
                            widths[i] = cell.length();
                        }
                    }
                }

                for (int i = 0; i < length; ++i) {
                    formatSb.append("%");
                    formatSb.append(widths[i]);
                    formatSb.append("s|");
                }
                int start = formatSb.length() - 1;
                int end = formatSb.length();
                if ("|".equals(formatSb.substring(start, end))) {
                    formatSb.delete(start, end);
                }
                formatSb.append("%n");

            } else {
                int width = 0;
                for (String label : TABLE_LABELS) {
                    if (width < label.length()) {
                        width = label.length();
                    }
                }

                for (String label : TABLE_LABELS) {
                    formatSb.append(label);
                    formatSb.append(VERTICAL_TABLE_LABEL_SEPARATOR);
                    formatSb.append(" %s%n");
                }

                for (int i = 0; i < width; ++i) {
                    formatSb.append(HORIZONTAL_TABLE_LABEL_SEPARATOR);
                }
                formatSb.append("%n");
            }

            format = formatSb.toString();
        }

        // Behelyettesítés.
        StringBuilder tableSb = new StringBuilder();
        {
            for (List<String> row : table) {
                tableSb.append(String.format(format, row.toArray()));
            }
        }
        return tableSb.toString();
    }

    private static void afterMain() {
        EM.close();
        EMF.close();
    }

    private Main() {
    }
}
