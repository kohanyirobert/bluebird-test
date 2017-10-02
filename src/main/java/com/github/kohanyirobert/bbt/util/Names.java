package com.github.kohanyirobert.bbt.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

// Egyedi neveket tartalmazó fájlokból olvas be sorokat
// és adja vissza ezeket, amíg azok el nem fogynak.
public final class Names {

    private static abstract class ForwardingIterator<E> implements Iterator<E> {

        protected ForwardingIterator() {
        }

        @Override
        public boolean hasNext() {
            return delegate().hasNext();
        }

        @Override
        public E next() {
            return delegate().next();
        }

        @Override
        public void remove() {
            delegate().remove();
        }

        protected abstract Iterator<E> delegate();
    }

    private static abstract class CyclicIterator<E> extends
            ForwardingIterator<E> {

        private final List<E> cache;

        private int current;

        protected CyclicIterator() {
            cache = new ArrayList<>();
            current = 0;
        }

        @Override
        public boolean hasNext() {
            return true;
        }

        @Override
        public E next() {
            E next;
            if (delegate().hasNext()) {
                next = delegate().next();
                cache.add(next);
                return next;
            } else {
                if (current < cache.size()) {
                    next = cache.get(current);
                    ++current;
                } else {
                    next = cache.get(0);
                    current = 0;
                }
                return next;
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private static final Iterator<String> TOWN_NAMES;
    private static final Iterator<String> LAST_NAMES;
    private static final Iterator<String> STREET_NAMES;

    static {
        List<String> townNames = Resources.getResourceAsLines("/town-names.txt");
        Collections.shuffle(townNames);
        TOWN_NAMES = townNames.iterator();

        List<String> lastNames = Resources.getResourceAsLines("/last-names.txt");
        Collections.shuffle(lastNames);
        LAST_NAMES = lastNames.iterator();

        Iterator<String> suffixes = new CyclicIterator<String>() {

            private final Iterator<String> delegate = Arrays.asList(
                    "Road",
                    "Street",
                    "Lane",
                    "Ave",
                    "Boulevard",
                    "Parkway").iterator();

            @Override
            protected Iterator<String> delegate() {
                return delegate;
            }
        };

        List<String> streetNames = Resources.getResourceAsLines("/street-names.txt");
        ListIterator<String> streetNamesIterator = streetNames.listIterator();
        while (streetNamesIterator.hasNext()) {
            streetNamesIterator.set(streetNamesIterator.next() + " " + suffixes.next());
        }
        Collections.shuffle(streetNames);
        STREET_NAMES = streetNames.iterator();
    }

    public static String nextTownName() {
        return TOWN_NAMES.next();
    }

    public static String nextLastName() {
        return LAST_NAMES.next();
    }

    public static String nextStreetName() {
        return STREET_NAMES.next();
    }

    private Names() {
    }
}
