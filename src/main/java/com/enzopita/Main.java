package com.enzopita;

import com.enzopita.structures.Game;
import com.enzopita.structures.Question;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<Question> questions;

        try {
            questions = getQuestions();
        } catch (Exception e) {
            System.out.println("Erro ao carregar as questões: " + e.getMessage());
            return;
        }

        Game game = new Game(questions);
        game.start();
        game.printSummary();
    }

    public static List<Question> getQuestions() throws Exception {
        List<Question> questions = new ArrayList<>();

        InputStream inputStream = Main.class.getResourceAsStream("/questions.json");

        if (inputStream == null) {
            throw new Exception("Arquivo do banco de questões não foi encontrado.");
        }

        try (InputStreamReader reader = new InputStreamReader(inputStream)) {
            Type questionListType = new TypeToken<List<Question>>() {}.getType();

            Gson gson = new Gson();
            Question[] questionsArray = gson.fromJson(reader, Question[].class);

            questions = List.of(questionsArray);
        } catch (Exception e) {
            throw new Exception("Erro ao ler e desserializar o JSON: " + e.getMessage());
        }

        return questions;
    }
}