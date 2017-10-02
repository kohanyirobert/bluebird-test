package com.github.kohanyirobert.bbt;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

public abstract class AbstractTest {

    protected static EntityManagerFactory EMF;

    @BeforeSuite
    public static void beforeSuite() throws Exception {
        EMF = Persistence.createEntityManagerFactory("bluebird-test");
        final EntityManager em = EMF.createEntityManager();
        em.unwrap(Session.class).doWork(new Work() {

            @Override
            public void execute(Connection connection) throws SQLException {
                EntityTransaction tx = em.getTransaction();
                tx.begin();
                try (Statement createStatement = connection.createStatement();
                        InputStream is = AbstractTest.class.getResourceAsStream("/db.sql");
                        Scanner scanner = new Scanner(is)) {
                    String sql = scanner.useDelimiter("^").next();
                    for (String statement : sql.split(";")) {
                        if (statement.matches("[\\s]*")) {
                            continue;
                        } else {
                            em.createNativeQuery(statement).executeUpdate();
                        }
                    }
                    tx.commit();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        em.close();
    }

    @AfterSuite
    public static void afterSuite() throws Exception {
        EMF.close();
    }

    protected EntityManager em;
    protected EntityTransaction tx;

    protected AbstractTest() {
    }

    @BeforeMethod
    public void beforeMethod() throws Exception {
        em = EMF.createEntityManager();
        tx = em.getTransaction();
    }

    @AfterMethod
    public void afterMethod() throws Exception {
        if (tx == null) {
            throw new NullPointerException();
        } else if (tx.isActive()) {
            throw new IllegalStateException();
        }

        if (em == null) {
            throw new NullPointerException();
        } else if (em.isOpen()) {
            em.close();
        } else {
            throw new IllegalStateException();
        }
    }
}
