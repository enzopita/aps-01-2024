package com.enzopita.structures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class Game {
    // Máximo de questões para cada dificuldade
    private static final int MAX_EASY_QUESTIONS = 40;
    private static final int MAX_MEDIUM_QUESTIONS = 40;
    private static final int MAX_HARD_QUESTIONS = 20;

    // A lista de questões disponíveis do arquivo "questions.json"
    private final List<Question> questions;

    // O índice da questão atual do arquivo "questions.json"
    private int currentQuestionIndex;

    // O total de dinheiro ganho com base nas perguntas
    private int moneyEarned;

    // Variáveis do sistema de eliminação de perguntas
    private int remainingEliminations;
    private final List<Option> removedOptions;

    private int remainingUniversityAssists;

    private boolean shouldStop;

    // O scanner que vai ler as respostas do usuário através do console
    private final Scanner scanner;

    public Game(List<Question> questions) {
        this.questions = shuffleQuestions(questions);
        this.currentQuestionIndex = 0;
        this.moneyEarned = 0;

        this.remainingEliminations = 2;
        this.removedOptions = new ArrayList<>();

        this.remainingUniversityAssists = 2;
        this.shouldStop = false;

        this.scanner = new Scanner(System.in);
    }

    public void start() {
        while (currentQuestionIndex < questions.size() && !shouldStop) {
            Question currentQuestion = questions.get(currentQuestionIndex);

            // Mostra a questão na tela
            System.out.println("Questão " + (currentQuestionIndex + 1) + ")");
            System.out.println("Dificuldade: " + translateDifficulty(currentQuestion.getDifficulty()));
            System.out.println("Valendo: R$ " + currentQuestion.getAmount());
            System.out.println();
            System.out.println(currentQuestion.getText());
            System.out.println();

            // Avisa das alternativas removidas pela ajuda
            if (!removedOptions.isEmpty()) {
                System.out.println(removedOptions.size() + " alternativas foram removidas dessa alternativa.");
                System.out.println();
            }

            // Lista as alternativas que não foram removidas
            for (Option option : currentQuestion.getOptions()) {
                if (!removedOptions.contains(option)) {
                    System.out.println("\t" + option.getNumber() + ") " + option.getText());
                }
            }

            System.out.println();

            if (remainingEliminations > 0) {
                System.out.println("Você tem " + remainingEliminations + " eliminações disponíveis.");
                System.out.println("Digite 'e' para utilizar uma das suas eliminações.");
                System.out.println();
            }

            if (remainingUniversityAssists > 0) {
                System.out.println("Você tem " + remainingUniversityAssists + " ajudas dos universitários disponíveis.");
                System.out.println("Digite 'a' para pedir uma ajuda.");
                System.out.println();
            }

            if (moneyEarned > 0) {
                System.out.println("Você pode parar por aqui digitando 'p' e ficar com R$ " + moneyEarned + " ou arriscar e ganhar mais respondendo a pergunta acima.");
            }

            // Respostas
            // 1..4 => alternativas das questões
            // e => eliminar 2x alternativas incorretas
            // a => ajuda dos universitários
            // p => parar
            String answer;

            do {
                System.out.print("Sua resposta: ");
                answer = scanner.nextLine().toLowerCase();
            } while (!isValidAnswer(answer) || isEliminatedAnswer(currentQuestion, answer));

            try {
                handleAnswer(currentQuestion, answer);
            } catch (InterruptedException e) {
                System.out.println("A thread em aguardo foi interrompida no meio da execução: " + e.getMessage());
            }
        }

        scanner.close();
    }

    public void printSummary() {
        System.out.println("Você respondeu " + (currentQuestionIndex + 1) + " questões.");
        System.out.println("Valor ganho: R$ " + moneyEarned);
        System.out.println("Abra o programa novamente caso queira jogar de novo.");
    }

    private void handleAnswer(Question question, String answer) throws InterruptedException {
        if (answer.equals("e")) {
            if (remainingEliminations == 0) {
                System.out.println("Você não tem mais eliminações disponíveis nessa partida.");
                return;
            }

            if (removedOptions.isEmpty()) {
                eliminateOptions(question);
            } else {
                System.out.println("Você já usou uma eliminação nessa rodada e não pode usar novamente!");
                Thread.sleep(2000);
                System.out.println();
            }
        } else if (answer.equals("a")) {
            if (remainingUniversityAssists == 0) {
                System.out.println("Você não tem mais ajuda dos universitários disponíveis nessa partida.");
                return;
            }

            universityAssist(question);
        } else {
            checkAnswer(question, answer);
        }
    }

    private void universityAssist(Question question) {
        Option correctOption = null;
        List<Option> incorrectOptions = new ArrayList<>();

        for (Option option : question.getOptions()) {
            if (option.isCorrect()) {
                correctOption = option;
            } else {
                incorrectOptions.add(option);
            }
        }

        if (correctOption == null) {
            correctOption = question.getOptions().getFirst();
        }

        System.out.println("Respostas dos universitários:");
        for (int i = 0; i < 3; i++) {
            if (Math.random() < 0.5 && !incorrectOptions.isEmpty()) {
                Option option = incorrectOptions.removeFirst();
                System.out.println("Universitário " + (i + 1) + " acredita que a resposta correta é: " + option.getNumber() + ") " + option.getText());
            } else {
                System.out.println("Universitário " + (i + 1) + " acredita que a resposta correta é: " + correctOption.getText());
            }
        }

        remainingUniversityAssists--;

        System.out.println();
    }

    private void eliminateOptions(Question question) {
        List<Option> options = question.getOptions();
        List<Option> incorrectOptions = new ArrayList<>();

        for (Option option : options) {
            if (!option.isCorrect()) {
                incorrectOptions.add(option);
            }
        }

        // Remover duas opções incorretas aleatórias
        Collections.shuffle(incorrectOptions);
        removedOptions.addAll(incorrectOptions.subList(0, Math.min(2, incorrectOptions.size())));

        System.out.println("Duas opções incorretas foram eliminadas.");
        System.out.println();

        remainingEliminations--;
    }

    private void checkAnswer(Question question, String answer) {
        int optionIndex = Integer.parseInt(answer) - 1;
        Option selectedOption = question.getOptions().get(optionIndex);

        if (selectedOption.isCorrect()) {
            moneyEarned += question.getAmount();
            System.out.println("Resposta correta! Você ganhou R$" + question.getAmount() + ".");
        } else {
            System.out.println("Resposta incorreta! O jogo acabou. Você ganhou R$" + moneyEarned + ".");
            this.shouldStop = true;

            return;
        }

        System.out.println("Total de dinheiro ganho até agora: R$" + moneyEarned);

        removedOptions.clear();
        currentQuestionIndex++;

        char choice;
        do {
            System.out.println();
            System.out.print("Pressione 'c' para continuar para a próxima pergunta ou 's' para parar: ");
            choice = scanner.next().charAt(0);
        } while (choice != 'c' && choice != 's');

        if (choice == 's') {
            System.out.println();
            this.shouldStop = true;
        } else {
            scanner.nextLine();
        }
    }

    private boolean isValidAnswer(String answer) {
        Question currentQuestion = questions.get(currentQuestionIndex);

        boolean isElimination = answer.equals("e");
        boolean isUniversityAssist = answer.equals("a");
        boolean isStop = answer.equals("p");

        boolean isNumericOption = answer.length() == 1 && Character.isDigit(answer.charAt(0));
        boolean isOptionInRange = isNumericOption && Integer.parseInt(answer) >= 1 && Integer.parseInt(answer) <= 4;

        return isElimination || isUniversityAssist || isStop || (isOptionInRange && !isEliminatedAnswer(currentQuestion, answer));
    }

    private boolean isEliminatedAnswer(Question question, String answer) {
        if (answer.matches("[1-4]")) {
            int optionIndex = Integer.parseInt(answer) - 1;
            return removedOptions.contains(question.getOptions().get(optionIndex));
        }

        return false;
    }

    private String translateDifficulty(String difficulty) {
        return switch (difficulty) {
            case "easy" -> "Fácil";
            case "medium" -> "Médio";
            case "hard" -> "Difícil";
            default -> "Desconhecido";
        };
    }

    private List<Question> limitQuestions(List<Question> questions, int maxQuestions) {
        if (questions.size() > maxQuestions) {
            return questions.subList(0, maxQuestions);
        } else {
            return questions;
        }
    }

    private List<Question> shuffleQuestions(List<Question> questions) {
        List<Question> easyQuestions = new ArrayList<>();
        List<Question> mediumQuestions = new ArrayList<>();
        List<Question> hardQuestions = new ArrayList<>();

        // Separa as questões por dificuldade
        for (Question question : questions) {
            if (question.getDifficulty().equalsIgnoreCase("easy")) {
                easyQuestions.add(question);
            } else if (question.getDifficulty().equalsIgnoreCase("medium")) {
                mediumQuestions.add(question);
            } else if (question.getDifficulty().equalsIgnoreCase("hard")) {
                hardQuestions.add(question);
            }
        }

        // Embaralha as questões dentro de cada lista de dificuldade
        Collections.shuffle(easyQuestions);
        Collections.shuffle(mediumQuestions);
        Collections.shuffle(hardQuestions);

        // Limita o número de questões para cada dificuldade
        easyQuestions = limitQuestions(easyQuestions, MAX_EASY_QUESTIONS);
        mediumQuestions = limitQuestions(mediumQuestions, MAX_MEDIUM_QUESTIONS);
        hardQuestions = limitQuestions(hardQuestions, MAX_HARD_QUESTIONS);

        // Junta todas novamente com base na dificuldade
        List<Question> shuffledQuestions = new ArrayList<>();
        shuffledQuestions.addAll(easyQuestions);
        shuffledQuestions.addAll(mediumQuestions);
        shuffledQuestions.addAll(hardQuestions);

        return shuffledQuestions;
    }
}
