/*
 * MarkovChain.java
 * Copyright (c) Shea Polansky 2014.
 * Purpose: Fully generic implementation of a Markov Chain based object
 * generator.
 * Created for Brooke Chenoweth's Intermediate Programming course.
 * Usage: None. Use Markov.java for usage.
 * Note: This class uses Java 8 features.
 */

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MarkovChain<T> {
    private final int order;
    private final HashMap<MarkovKey, ProbabilityMapping> markovTable =
            new HashMap<>();

    /**
     * MarkovChain(int): Constructs a new MarkovChain object of order n
     * @param order: The order of the chain.
     */
    public MarkovChain(int order) {
        this.order = order;
    }

    /**
     * addItems(Iterator): Adds the sequence of items in the Iterator to
     * the chain.
     *
     * @param iterator: The iterator that gives the sequence of items
     */
    public void addItems(Iterator<T> iterator) {
        ItemsAdder adder = new ItemsAdder();
        new IteratorMapper<>(iterator, Optional::of).forEachRemaining(adder);
        Collections.nCopies(order, Optional.<T>empty()).iterator()
                .forEachRemaining(adder); //padding with blanks at the end
    }

    /**
     * getRandomTStream: Returns an infinite stream of generated objects using
     * a default implementation of Random
     *
     * @return: An infinite stream of generated values
     */
    public Stream<T> getRandomTStream() {
        return getRandomTStream(new Random());
    }

    /**
     * getRandomTStream: Returns an infinite stream of generated objects
     *
     * @param rand: The RNG to use
     * @return: An infinite stream of generated values
     */
    public Stream<T> getRandomTStream(Random rand) {
        return Stream.generate(new RandomTSupplier(rand));
    }

    /**
     * generate(int, Random): Gives a randomly generated, using a given RNG,
     * list of values, length n.
     *
     * @param number: The length of the list to return
     * @param rand:   The RNG to use
     * @return: The list of values
     */
    public List<T> generate(int number, Random rand) {
        return getRandomTStream(rand).limit(number)
                .collect(Collectors.toList());
    }

    /**
     * ItemsAdder: 'Consumes' Optionals of the appropriate type by adding them
     * to the Markov table
     */
    private class ItemsAdder implements Consumer<Optional<T>> {
        private MarkovKey key = new MarkovKey();

        /**
         * accept: Adds the given item to the Markov table
         *
         * @param t: The item to add
         */
        public void accept(Optional<T> t) {
            if (!markovTable.containsKey(key)) {
                markovTable.put(key, new ProbabilityMapping());
            }
            markovTable.get(key).add(t);
            key = key.getNext(t);
        }
    }

    /**
     * RandomTSupplier: The supplier for the random streams
     */
    private class RandomTSupplier implements Supplier<T> {
        private final Random rand;
        private MarkovKey key = new MarkovKey();

        /**
         * RandomTSupplier(Random): Constructs a new RandomTSupplier with the
         * given RNG.
         *
         * @param random: The RNG to use
         */
        public RandomTSupplier(Random random) {
            rand = random;
        }

        /**
         * get: Returns a single Markov-generated element
         *
         * @return: The generated element
         */
        public T get() {
            Optional<T> t;
            do {
                t = markovTable.get(key).getNextTRandomly(rand);
                key = key.getNext(t);
            }
            while (!t.isPresent()); //no need to return blanks
            return t.get();
        }
    }

    /**
     * ProbabilityMapping: mapped-to portion of the markov table
     * Represents a list of suffixes mapped to their overall frequencies
     * Uses a LinkedHashMap internally to ensure that its non-random methods
     * have deterministic outputs.
     */
    private class ProbabilityMapping {
        //LinkedHashMap so that getNextStringByIndex has a deterministic output
        //use of Optional<T> for a well-defined non-value for all types
        private final LinkedHashMap<Optional<T>, Integer> probabilityMap =
                new LinkedHashMap<>();
        private int totalValues;

        /**
         * add(Optional): adds a suffix to the ma
         *
         * @param t: The suffix to add
         */
        public void add(Optional<T> t) {
            if (probabilityMap.containsKey(t)) {
                probabilityMap.put(t, probabilityMap.get(t) + 1);
            } else {
                probabilityMap.put(t, 1);
            }

            totalValues++;
        }

        /**
         * getNumberOfSamples: Returns the overall number of suffixes captured
         *
         * @return: The number of suffixes captured
         */
        public int getNumberOfSamples() {
            return totalValues;
        }

        /**
         * Returns a value based on its index. The method is an unrolled
         * version of the following algorithm:
         * Each suffix is expanded to a list of length equal to that suffix's
         * frequency, and the lists are flat-mapped together, to form a single
         * list. The index'th value in the generated list is the value returned.
         *
         * @param index: The index of the value to return
         * @return: The value to return.
         */
        public Optional<T> getNextTByIndex(int index) { //0 based
            if (index >= getNumberOfSamples()) {
                throw new IllegalArgumentException();
            }
            for (Optional<T> key : probabilityMap.keySet()) {
                index -= probabilityMap.get(key);
                if (index < 0) {
                    return key;
                }
            }
            throw new RuntimeException(); //should never happen because of
            //earlier bounds checking
        }

        /**
         * Returns a random element from the suffixes list, weighted by
         * frequency, using a default RNG.
         *
         * @return: The random value.
         */
        public Optional<T> getNextTRandomly() {
            return getNextTRandomly(new Random());
        }

        /**
         * Returns a random element from the suffixes list, weighted by
         * frequency, using a given RNG
         *
         * @param rand: The RNG to use
         * @return: The randomly generated value
         */
        public Optional<T> getNextTRandomly(Random rand) {
            return getNextTByIndex(rand.nextInt(getNumberOfSamples()));
        }

        /**
         * Pass-Through toString, to make debugging easier.
         *
         * @return: The string representation of the underlying map.
         */
        public String toString() {
            return probabilityMap.toString();
        }
    }

    /**
     * MarkovKey: The key class used for the Markov table
     */
    private class MarkovKey {
        private final LinkedList<Optional<T>> prefixes;

        /**
         * MarkovKey: Creates an key containing empty values
         */
        public MarkovKey() {
            this(Collections.nCopies(order, Optional.empty()));
        }

        /**
         * MarkovKey(Collection): Constructs a key containing the given
         * collection
         *
         * @param clone: The collection to clone
         */
        public MarkovKey(Collection<Optional<T>> clone) {
            prefixes = new LinkedList<>(clone);
        }

        /**
         * MarkovKey(Collection, Optional): Generates a new MarkovKey using
         * the given collection as a template and the given value as the next
         * value in line. The first value in the clone of the collection is
         * removed, and the given value is added at the end of the resultant.
         * @param previous: The previous collection to clone
         * @param next: The value to add afterward.
         */
        public MarkovKey(Collection<Optional<T>> previous, Optional<T> next) {
            this(previous);
            prefixes.addLast(next);
            prefixes.removeFirst();
        }

        /**
         * Returns a new MarkovKey based on this one, using the given value
         * as the next value in line.
         * @param next: The next value to add
         * @return: The new MarkovKey.
         */
        public MarkovKey getNext(Optional<T> next) {
            return new MarkovKey(prefixes, next);
        }

        /**
         * equals: Returns whether this object is equal to another object.
         * Two MarkovKey's are equal if and only if their respective
         * LinkedLists are equal.
         * @param o: The object to compare against
         * @return: Whether or not the objects are equal
         */
        @Override
        @SuppressWarnings("unchecked") //I'm checking it but java doesn't
        //think so and I compile with -Werror so I need to suppress the warning
        public boolean equals(Object o) {
            return (o != null)
                    && this.getClass().isAssignableFrom(o.getClass())
                    && ((MarkovKey) o).prefixes.equals(prefixes);
        }

        /**
         * hashCode: Returns the hash code of the underlying list
         * @return: The hash code
         */
        @Override
        public int hashCode() {
            return prefixes.hashCode();
        }

        /**
         * toString: Returns the String representation of the underlying List,
         * to ease debugging.
         * @return: The String representation of the underlying List
         */
        @Override
        public String toString() {
            return prefixes.toString();
        }
    }

    /**
     * IteratorMapper: Creates a new Iterator from a given iterator by applying
     * a given function to each value the iterator contains
     * @param <T>: The type of the iterator
     * @param <R>: The return type of the function
     */
    protected class IteratorMapper<T, R> implements Iterator<R> {
        private final Iterator<T> wrapped;
        private final Function<T, R> mapFunction;

        /**
         * IteratorMapper(Iterator, Function): Constructs a new IteratorMapper
         * @param wrapped: The iterator to wrap around
         * @param mapFunction: The function to apply
         */
        public IteratorMapper(Iterator<T> wrapped, Function<T, R> mapFunction) {
            this.wrapped = wrapped;
            this.mapFunction = mapFunction;
        }

        /**
         * hasNext: Returns whether the underlying iterator has items remaining
         * @return: true if there are items remaining, false otherwise
         */
        @Override
        public boolean hasNext() {
            return wrapped.hasNext();
        }

        /**
         * Returns the next item in the underlying iterator after the function
         * is applied to it
         * @return: The return value of the function
         */
        @Override
        public R next() {
            return mapFunction.apply(wrapped.next());
        }

        /**
         * remove: Removes the next item in the iterator, <b>without</b>
         * applying the function to it.
         */
        @Override
        public void remove() {
            wrapped.remove();
        }
    }
}