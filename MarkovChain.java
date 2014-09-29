import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MarkovChain<T> {
    private final int order;
    private final T blankValue;
    private final HashMap<MarkovKey, ProbabilityMapping> markovTable =
            new HashMap<>();

    public MarkovChain(int order, T blankValue) {
        this.order = order;
        this.blankValue = blankValue;
    }

    public void addItems(Iterator<T> iterator) {
        ItemsAdder adder = new ItemsAdder();
        iterator.forEachRemaining(adder);
        Collections.nCopies(order, blankValue).iterator()
                .forEachRemaining(adder); //padding with blanks at the end
    }

    public Stream<T> getRandomTStream() {
        return getRandomTStream(new Random());
    }

    public Stream<T> getRandomTStream(Random rand) {
        return Stream.generate(new RandomTSupplier(rand));
    }

    public List<T> generate(int number, Random rand) {
        return getRandomTStream(rand).limit(number)
                .collect(Collectors.toList());
    }

    private class ItemsAdder implements Consumer<T> {
        private MarkovKey key = new MarkovKey();

        public void accept(T t) {
            if (!markovTable.containsKey(key)) {
                markovTable.put(key, new ProbabilityMapping());
            }
            markovTable.get(key).add(t);
            key = key.getNext(t);
        }
    }

    private class RandomTSupplier implements Supplier<T> {
        private final Random rand;
        private MarkovKey key = new MarkovKey();

        public RandomTSupplier(Random random) {
            rand = random;
        }

        public T get() {
            T t;
            do {
                t = markovTable.get(key).getNextTRandomly(rand);
                key = key.getNext(t);
            }
            while (t.equals(blankValue)); //no need to return blanks
            return t;
        }
    }

    private class ProbabilityMapping {
        //LinkedHashMap so that getNextStringByIndex has a deterministic output
        private final LinkedHashMap<T, Integer> probabilityMap =
                new LinkedHashMap<>();
        private int totalValues;

        public void add(T t) {
            if (probabilityMap.containsKey(t)) {
                probabilityMap.put(t, probabilityMap.get(t) + 1);
            }
            else {
                probabilityMap.put(t, 1);
            }

            totalValues++;
        }

        public int getNumberOfSamples() {
            return totalValues;
        }

        public T getNextTByIndex(int index) { //0 based
            if (index >= getNumberOfSamples()) {
                throw new IllegalArgumentException();
            }
            for (T key : probabilityMap.keySet()) {
                index -= probabilityMap.get(key);
                if (index < 0) {
                    return key;
                }
            }
            throw new RuntimeException(); //should never happen because of
            //earlier bounds checking
        }

        public T getNextTRandomly() {
            return getNextTRandomly(new Random());
        }

        public T getNextTRandomly(Random rand) {
            return getNextTByIndex(rand.nextInt(getNumberOfSamples()));
        }

        public String toString() {
            return probabilityMap.toString();
        }
    }

    private class MarkovKey {
        private final LinkedList<T> prefixes;

        public MarkovKey() {
            this(Collections.nCopies(order, blankValue));
        }

        public MarkovKey(Collection<T> clone) {
            prefixes = new LinkedList<>(clone);
        }

        public MarkovKey(Collection<T> previous, T next) {
            this(previous);
            prefixes.addLast(next);
            prefixes.removeFirst();
        }

        public MarkovKey getNext(T next) {
            return new MarkovKey(prefixes, next);
        }

        @Override
        @SuppressWarnings("unchecked") //I'm checking it but java doesn't
        //think so, and I compile with -Werror so I need to suppress the warning
        public boolean equals(Object o) {
            return o != null && o.getClass().equals(getClass()) &&
                    ((MarkovKey) o).prefixes.equals(prefixes);
        }

        @Override
        public int hashCode() {
            return prefixes.hashCode();
        }

        @Override
        public String toString() {
            return prefixes.toString();
        }
    }
}