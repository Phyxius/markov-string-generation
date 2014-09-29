import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.Scanner;

public class Markov {

    /**
     * Regular expression for breaking up words.
     */
    private static final String WORD_REGEX = "(?<=\\b\\s)";

    /**
     * Regular expression for getting individual characters.
     */
    private static final String CHAR_REGEX = "(?<=.)";

    /**
     * Program prints generated gibberish to standard output using a
     * Markov Chain based on given file(s).
     *
     * @param args Command line arguments are expected to be:
     *             First: Order of the markov chain
     *             Second: Amount of output to generate
     *             Third: How to split the input, word or char
     *             Remaining arguments are names of text files
     */
    public static void main(String[] args) {

        // Parse the arguments
        int order = Integer.parseInt(args[0]);
        int count = Integer.parseInt(args[1]);
        String regex = WORD_REGEX;
        if (args[2].equalsIgnoreCase("char")) {
            regex = CHAR_REGEX;
        }

        Random rand = new Random();

        // Create chain of desired order    
        StringChain chain = new StringChain(order);

        // Read each file and add it to the chain
        for (int i = 3; i < args.length; ++i) {
            String file = args[i];

            addFile(chain, regex, Paths.get(file));
        }

        // Generate and print out desired amount of output
        String delimiter = " ";
        if (regex.equals(CHAR_REGEX)) {
            delimiter = "";
        }
        for (String s : chain.generate(count, rand)) {
            System.out.print(s + delimiter);
        }
        System.out.println();
    }

    /**
     * Read a text file, split it according to a regular expression,
     * and add the items to the Markov chain.
     *
     * @param chain The Markov chain object
     * @param regex Regular expression for splitting input text
     * @param file  Path to the text file
     */
    private static void addFile(StringChain chain, String regex, Path file) {
        // Using try-with-resources to auto-close Scanner
        try (Scanner sc = new Scanner(file)) {
            // Split with regex instead of default whitespace
            sc.useDelimiter(regex);

            // Scanner implements Iterator<String>, so this works
            chain.addItems(sc);
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}