package com.example.android.bibleknowledgequiz;

public class Question {

    /****************************************************************************************
     * THIS CLASS CONSISTS OF THE OBJECT "QUESTION", WHICH STORES ALL QUESTIONS IN THE QUIZ *
     ***************************************************************************************/
    int imageResId;
    String question = "";                                       // we initialize String variables, since they are not primitive data, in order to avoid NullPointerException error
    String answerType = "";                                     // answerType can be "R" (radio question), "C" (checkbox question), "E" (edit box question)
    String[] possibleAnswers = new String[]{"", "", "", ""};    // possible answers are 4 in case of radio buttons and checkboxes
    String correctAnswerRadio = "";                             // correct answer for radio questions
    String[] correctAnswersCheckBox = {"", "", "", ""};         // correct answers for the checkbox questions
    String[] correctAnswerEdit = {"", ""};                      // correct answers for the edit questions

    private Question(int imgResId, String question, String answerType) {                // this is the base constructor, used for the other below constructors
        this.imageResId = imgResId;
        this.question = question;
        this.answerType = answerType;
    }

    // Below is the constructor for the Radio questions
    public Question(int imgResId, String question, String answerType,
                    String[] possibleAnswers, String correctAnswerRadio) {
        this(imgResId, question, answerType);                                           // initialization of the base constructor
        for (int count = 0; count <= 3; count++)
            this.possibleAnswers[count] = possibleAnswers[count];                       // adding possible answers and the correct answer; if we assign "=", instead of going through the array, it will be passed just the reference, not the value
        this.correctAnswerRadio = correctAnswerRadio;
    }

    // Below is the constructor for the Checkbox questions
    public Question(int imgResId, String question, String answerType,
                    String[] possibleAnswers, String[] correctAnswersCheckBox) {
        this(imgResId, question, answerType);                                           // initialization of the base constructor
        for (int count = 0; count <= 3; count++) {
            this.possibleAnswers[count] = possibleAnswers[count];                       // adding possible answers and the correct answers
            this.correctAnswersCheckBox[count] = correctAnswersCheckBox[count];
        }
    }

    // Below is the constructor for the Edit questions
    public Question(int imgResId, String question, String answerType,
                    String[] correctAnswerEdit) {
        this(imgResId, question, answerType);                                           // initialization of the base constructor
        this.correctAnswerEdit = correctAnswerEdit;                                     // adding the correct answer; there are arrays of 1, 2 or 3 correct answers, this is why we put the "=" operator, because it will take the length of the array of correct answers
    }
}
