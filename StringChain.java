import java.util.Iterator;
import java.util.function.Function;

public class StringChain extends MarkovChain<String> {
    public StringChain(int order) {
        super(order, "");
    }

    /*@Override
    public Stream<String> getRandomTStream(Random rand) {
        return super.getRandomTStream(rand).map(s -> s + " ");
    }*/

    @Override
    public void addItems(Iterator<String> iterator) {
        super.addItems(new IteratorMapper<>(iterator,
                s -> s.length() > 1 ? s.trim() : s)); //trim spaces from words
        //but not from single characters
    }

    private class IteratorMapper<T, R> implements Iterator<R> {
        private final Iterator<T> wrapped;
        private final Function<T, R> mapFunction;

        public IteratorMapper(Iterator<T> wrapped, Function<T, R> mapFunction) {
            this.wrapped = wrapped;
            this.mapFunction = mapFunction;
        }

        @Override
        public boolean hasNext() {
            return wrapped.hasNext();
        }

        @Override
        public R next() {
            return mapFunction.apply(wrapped.next());
        }

        @Override
        public void remove() {
            wrapped.remove();
        }
    }
}