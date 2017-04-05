package com.example.android.bibleknowledgequiz;

import java.util.ArrayList;

public class UserAnswer {

    /***********************************************************************************
     * THIS CLASS CONSISTS OF THE OBJECT "USERANSWER", WHICH STORES ALL USER'S ANSWERS *
     **********************************************************************************/
    String answerType = "";                                         // answerType can be "R" (radio), "C" (checkbox), "E" (edit box)
    String answerEdit = "";                                         // correct answer for edit questions
    String radioAnswer = "";                                        // correct answer for radio questions
    ArrayList<String> answersCheckBox = new ArrayList<String>();         // correct answers for the checkbox questions; we used ArrayList because we do not know the exact number of answers given by the user; String Array must have a fixed length (not applicable here)
    int[] radioOrder = {0, 1, 2, 3};                                // order of answers radio questions (here we store the order of answers that the user receives for each question; when the user reviews the quiz, the same order will be shown)
    int[] checkBoxOrder = {0, 1, 2, 3};                             // order of answers checkBox questions (same explanation as above)

    private UserAnswer(String answerType) {                                             // this is the base constructor, used for the other below constructors
        this.answerType = answerType;
    }

    // Below is the constructor for the Edit questions
    public UserAnswer(String answerType, String answerEdit) {
        this(answerType);                                                               // initialization of the base constructor
        this.answerEdit = answerEdit;                                                   // edit has only one input from the user as a correct answer
    }

    // Below is the constructor for the Radio questions
    public UserAnswer(String answerType, String radioAnswer, int[] radioOrder) {
        this(answerType);                                                               // initialization of the base constructor
        this.radioAnswer = radioAnswer;                                                 // initialization of the radio answer
        for (int i = 0; i < 4; i++)
            this.radioOrder[i] = radioOrder[i];                                         // initialization of the radio answers order
    }

    // Below is the constructor for the Checkbox questions
    public UserAnswer(String answerType, ArrayList<String> answersCheckBox, int[] checkBoxOrder) {
        this(answerType);                                                               // initialization of the base constructor
        this.answersCheckBox = answersCheckBox;                                         // initialization of the checkbox answers
        for (int i = 0; i < 4; i++)
            this.checkBoxOrder[i] = checkBoxOrder[i];                                   // initialization of the checkbox answers order
    }
}

