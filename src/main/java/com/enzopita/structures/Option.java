package com.enzopita.structures;

import com.google.gson.annotations.SerializedName;

public class Option {
    private int number;
    private String text;

    @SerializedName("is_correct")
    private boolean isCorrect;

    public int getNumber() {
        return number;
    }

    public String getText() {
        return text;
    }

    public boolean isCorrect() {
        return isCorrect;
    }
}
