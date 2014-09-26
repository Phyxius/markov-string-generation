import java.util.*;
import java.util.function.Consumer;
import java.util.stream.*;
import java.util.function.*;

public class StringChain {
	private final int order;
	private final HashMap<List<String>, ProbabilityMapping> markovTable = 
		new HashMap<>(); //Lists have a well-defined .equals() as of Java 1.7

	public StringChain(int order) {
		this.order = order;
	}

	public void addItems(Iterator<String> iterator) {
		System.out.println("--Begin Adding Items--");
		ItemsAdder adder = new ItemsAdder();
		iterator.forEachRemaining(adder);
		Collections.nCopies(order - 1, "").iterator().forEachRemaining(adder);
		System.out.println("--End Adding Items--");
	}

	public Stream<String> getRandomStringStream() {
		return getRandomStringStream(new Random());
	}

	public Stream<String> getRandomStringStream(Random rand) {
		return Stream.generate(new RandomStringSupplier(rand)).filter(
			s -> !s.equals(""));
	}

	public List<String> generate(int number, Random rand) {
		System.out.println("--Begin Generating Strings (" + number + ")--");
		return getRandomStringStream()
			.limit(number)
			.map(s -> s + " ")
			.collect(Collectors.toList());
	}

	private class ItemsAdder implements Consumer<String> {
		private final LinkedList<String> previous = new LinkedList<>(
			Collections.nCopies(order, ""));
		public void accept(String s) {
			s = s.trim();
			//I like how String.join didn't exist until Java 8
			List<String> key = new ArrayList<>(previous.size()); 
				//javac complains if I don't explicitly make null
			Collections.copy(key, previous);
			key = Collections.unmodifiableList(key);
			if (!markovTable.containsKey(key)) {
				markovTable.put(key, new ProbabilityMapping());
			}
			markovTable.get(key).add(s);
			previous.removeFirst();
			previous.addLast(s);
			System.out.printf("%s -> %s\n", key, s);
		}
	}

	public class RandomStringSupplier implements Supplier<String> {
		private final Random rand;

		public RandomStringSupplier(Random random) {
			rand = random;
		}

		private LinkedList<String> previous = new LinkedList<>(
			Collections.nCopies(order, ""));
		public String get() {
			String s = markovTable.get(String.join(" ", previous))
					.getNextStringRandomly(rand);
			previous.addLast(s);
			previous.removeFirst();
			return s;
		}
	}

	private class ProbabilityMapping {
		//LinkedHashMap so that getNextStringByIndex has a deterministic output
		private final LinkedHashMap<String, Integer> probabilityMap = 
			new LinkedHashMap<>();
		private int totalValues;

		public void add(String s) {
			if (probabilityMap.containsKey(s)) {
				probabilityMap.put(s, probabilityMap.get(s) + 1);
			}
			else {
				probabilityMap.put(s, 1);
			}

			totalValues += 1;
		}
		public int getNumberOfSamples() {
			return totalValues;
		}

		public String getNextStringByIndex(int index) { //0 based
			if (index >= getNumberOfSamples()) {
				throw new IllegalArgumentException();
			}
			//this is actually nearly as memory-efficient as doing it the
			//'proper' weighted way due to the lazy nature of Streams. 
			return probabilityMap.keySet().stream()
				.flatMap(key -> 
					Collections.nCopies(probabilityMap.get(key), key).stream())
				.skip(index)
				.findFirst()
				.get(); //don't have to worry about empty optionals because max 
						//size is checked for
		}

		public String getNextStringRandomly() {
			return getNextStringRandomly(new Random());
		}

		public String getNextStringRandomly(Random rand) {
			return getNextStringByIndex(
				rand.nextInt(getNumberOfSamples()));
		}
	}
}