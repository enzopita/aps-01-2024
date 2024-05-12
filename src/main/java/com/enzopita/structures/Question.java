package com.enzopita.structures;

import java.util.List;

public class Question {
    private String text;
    private String difficulty;
    private int amount;
    private List<Option> options;

    public String getText() {
        return text;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public int getAmount() {
        return amount;
    }

    public List<Option> getOptions() {
        return options;
    }
}
