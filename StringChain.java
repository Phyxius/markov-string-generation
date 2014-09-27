import java.util.*;
import java.util.stream.*;
import java.util.function.*;

public class StringChain extends MarkovChain<String> {
	public StringChain(int order) {
		super(order, "");
	}

	@Override 
	public Stream<String> getRandomTStream(Random rand) {
		return super.getRandomTStream(rand).map(s -> s + " ");
	}
	
	@Override
	public void addItems(Iterator<String> iterator) {
		super.addItems(new IteratorMapper<String>(iterator, 
			s -> s.length() > 1 ? s.trim() : s)); //trim spaces from strings
				//but not from single characters
	}

	private class IteratorMapper<T> implements Iterator<T> {
		private final Iterator<T> wrapped;
		private final Function<T,T> mapFunction;
		public IteratorMapper(Iterator<T> wrapped, 
				Function<T,T> mapFunction) {
			this.wrapped = wrapped;
			this.mapFunction = mapFunction;
		}

		@Override
		public boolean hasNext() {
			return wrapped.hasNext();
		}

		@Override
		public T next() {
			return mapFunction.apply(wrapped.next());
		}

		@Override
		public void remove() {
			wrapped.remove();
		}
	}
}