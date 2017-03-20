package com.example.android.bibleknowledgequiz;

public class Question {
    int difficulty = 0;                                         // we initialize everything even if there are default values, since we had issues with NullPointerException error
    int questionNr = 0;
    int imageResId = 0;
    String question = "";
    String answerType = "";                                     // answerType can be "R" (radio), "C" (checkbox), "E" (edit box)
    String[] possibleAnswers = new String[]{"", "", "", ""};    // possible answers are 4 in case of radio buttons and checkboxes
    String correctAnswerRadio = "";                             // correct answer for radio questions
    String[] correctAnswersCheckBox = {"", "", "", ""};         // correct answers for the checkbox questions
    String[] correctAnswerEdit = {"", ""};                      // correct answers for the edit questions

    private Question(int difficulty, int questionNr, int imgResId, String question, String answerType) {        // this is the base constructor, used for the other below constructors
        this.difficulty = difficulty;                                                                           // this is why this constructor is private, because it is used just inside the class
        this.questionNr = questionNr;                                                                           // the other three constructors are public, because just they are used in constructing questions
        this.imageResId = imgResId;
        this.question = question;
        this.answerType = answerType;
    }

    // Below is the constructor for the Radio questions
    public Question(int difficulty, int questionNr, int imgResId, String question, String answerType,
                    String[] possibleAnswers, String correctAnswerRadio) {
        this(difficulty, questionNr, imgResId, question, answerType);                                           // initialization of the base constructor
        for (int count = 0; count <= 3; count++)
            this.possibleAnswers[count] = possibleAnswers[count];                                               // adding possible answers and the correct answer
        this.correctAnswerRadio = correctAnswerRadio;
    }

    // Below is the constructor for the Checkbox questions
    public Question(int difficulty, int questionNr, int imgResId, String question, String answerType,
                    String[] possibleAnswers, String[] correctAnswersCheckBox) {
        this(difficulty, questionNr, imgResId, question, answerType);                                           // initialization of the base constructor
        for (int count = 0; count <= 3; count++) {
            this.possibleAnswers[count] = possibleAnswers[count];                                               // adding possible answers and the correct answers
            this.correctAnswersCheckBox[count] = correctAnswersCheckBox[count];                                   // if the value in this array is 1, it means that it is a correct answer
        }
    }

    // Below is the constructor for the Edit questions
    public Question(int difficulty, int questionNr, int imgResId, String question, String answerType,
                    String[] correctAnswerEdit) {
        this(difficulty, questionNr, imgResId, question, answerType);                                           // initialization of the base constructor
        this.correctAnswerEdit = correctAnswerEdit;                                                             // adding the correct answer
    }

    public void radioRandomize() {

    }
}
