import java.util.*;

/** Plays with StringChain with some hardcoded values. */
public class MarkovStringDemo {

	private static final int ORDER = 1;
	private static final int NUM_WORDS = 50;

	/** Regular expression for breaking up words. */
    //    private static final String WORD_REGEX = "(?<=\\b\\s)";
	private static final String WORD_REGEX = "(?<=\\w\\W)";

	/** Regular expression for getting individual characters. */
	private static final String CHAR_REGEX = "(?<=.)";

	/** Random number generator for picking random items. */
	private static final Random rand = new Random();

	public static void main(String[] args) {

		String regex = WORD_REGEX;

		String demoString = "I am not a number! I am a free man!";

		System.out.println("Initial String: " + demoString);

		StringChain chain = new StringChain(ORDER);

	// Make a scanner that uses string as source
		Scanner strScan = new Scanner(demoString);
	// Use word boundaries to break up input.
		strScan.useDelimiter(regex);

	// Add words from Scanner to the chain
		chain.addItems(strScan);

	// Generate words
		List<String> gibberish = chain.generate(NUM_WORDS, rand);

	// Print out the result.
		System.out.println("Generated gibberish: ");
		for(String word : gibberish) {
			System.out.print(word);
		}
		System.out.println();

	// Now let's add another sequence to the same chain
		String anotherStr = "They are the eggmen. I am the walrus.";
		System.out.println("Another string: " + anotherStr);
		
		chain.addItems(new Scanner(anotherStr).useDelimiter(regex));

	// Print out the new result.
		System.out.println("More gibberish: ");
		for(String word : chain.generate(NUM_WORDS, rand)) {
			System.out.print(word);
		}
		System.out.println();
	}
}