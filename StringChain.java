/*
 * StringChain.java
 * Copyright (c) Shea Polansky 2014.
 * Purpose: Represents a Markov Chain String Generator.
 * Created for Brooke Chenoweth's Intermediate Programming course.
 * Usage: Use the Markov.java file included
 * Note: This class uses Java 8 features
 */

import java.util.Iterator;

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