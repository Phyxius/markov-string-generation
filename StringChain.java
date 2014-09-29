import java.util.Iterator;
import java.util.function.Function;

public class StringChain extends MarkovChain<String> {
    public StringChain(int order) {
        super(order);
    }

    @Override
    public void addItems(Iterator<String> iterator) {
        super.addItems(new IteratorMapper<>(iterator,
                s -> s.length() > 1 ? s.trim() : s)); //trim spaces from words
        //but not from single characters
    }
}