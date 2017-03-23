package com.example.android.bibleknowledgequiz;

import android.content.Intent;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import static android.R.attr.x;
import static android.R.id.edit;
import static android.view.View.GONE;
import static android.widget.Toast.makeText;
import static com.example.android.bibleknowledgequiz.HomeScreen.BUNDLE_LEVEL;
import static com.example.android.bibleknowledgequiz.HomeScreen.BUNDLE_QUESTIONS;
import static com.example.android.bibleknowledgequiz.HomeScreen.INTENT_TOQUIZ;
import static com.example.android.bibleknowledgequiz.R.layout.activity_quiz;

public class Quiz extends AppCompatActivity {
    int difficultyLevel, nrOfQuestions;                         // these two variables retrieve data from bundle from HomeScreen;
    int currentQuestion, crtQ;                                  // these two variables helps us to go through the all the questions and to show the questions in a shuffled order
    public Question[][] quizQuestion = new Question[2][10];     // quizQuestion - array of questions: [2] meaning beginner or advanced; [10] meaning the number of total questions in the quiz

    // below we initialize the answers and questions showing order variables;
    int[] showAnswersOrder = {0, 1, 2, 3};      // after shuffling, the showAnswersOrder might look like this: {2, 0, 1, 3} or any other combination
    int[] showQuestionsOrder = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

    // below we declare a variable Array of UserAnswer where we record all user answers
    ArrayList<UserAnswer> allUserAnswers = new ArrayList<UserAnswer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_quiz);

        // below we initialize radio buttons, radio group, checkboxes and edit text from ACTIVITY_QUIZ.XML; we do it here in order not to use expensive "findViewById" too often;
        // take care: findViewById in java file related to activity before setContentView will always return NULL!!!
        // this is the reason for creating method "displayQuestion" with all parameters below because we could not use them as global variables, since we could not initialize them before setContentView
        // if we try to initialize them as global e.g. "RadioGroup radioGroupId;" and then declare them in OnCreate method after "setContentView", we will get a NullPointerException error
        final RadioGroup radioGroupId = (RadioGroup) findViewById(R.id.radio_group);
        final RadioButton[] radioButtonId = {(RadioButton) findViewById(R.id.answers_radio_1), (RadioButton) findViewById(R.id.answers_radio_2),
                (RadioButton) findViewById(R.id.answers_radio_3), (RadioButton) findViewById(R.id.answers_radio_4)};
        final LinearLayout checkBoxGroupId = (LinearLayout) findViewById(R.id.answers_checkbox_layout);
        final CheckBox[] checkBoxId = {(CheckBox) findViewById(R.id.answers_checkbox_1), (CheckBox) findViewById(R.id.answers_checkbox_2),
                (CheckBox) findViewById(R.id.answers_checkbox_3), (CheckBox) findViewById(R.id.answers_checkbox_4)};
        final EditText editTextId = (EditText) findViewById(R.id.possible_answers_editText);
        final ImageView nextButton = (ImageView) findViewById(R.id.next_button);
        final ImageView previousButton = (ImageView) findViewById(R.id.previous_button);
        final TextView current_questionText = (TextView) findViewById(R.id.current_question);
        final ImageView current_questionImage = (ImageView) findViewById(R.id.current_picture);

        // below we retrieve information saved in Bundle from the Home Activity
        Intent intent = getIntent();                            // getIntent(), when called in an Activity, gives you a reference to the Intent which was used to launch this Activity.
        Bundle bundle = intent.getBundleExtra(INTENT_TOQUIZ);   // take care - getExtras() not working for retrieving a Bundle, it returns null;
        nrOfQuestions = bundle.getInt(BUNDLE_QUESTIONS);
        difficultyLevel = bundle.getInt(BUNDLE_LEVEL);      // difficulty level is 0 or 1

        // the order is shuffled every time, for the user not to "learn" which question follows or the order of the answers within every question
        AppTools.shuffleArray(showQuestionsOrder);  // after shuffling, the showQuestionsOrder might look like this: {8, 6, 2, 0, 9, 1, 3, 5, 7, 4} or any other combination

        AppTools.customToast(Quiz.this, Gravity.CENTER, 0, getResources().getInteger(R.integer.quiz_toast_previous_question), Toast.LENGTH_LONG, "Please select the right arrow to go to the next question!");
        initializeQuestions(difficultyLevel);           // questions are defined in an array of questions in the method "initializeQuestions" based on the difficulty level chosen by the user;
        currentQuestion = 0;                            // even if is initialized by default with 0, for better tracking we initialized it here;
        crtQ = showQuestionsOrder[currentQuestion];     // we use a different counter for questions, based on the previous shuffling;
        displayQuestion(quizQuestion[difficultyLevel][crtQ].answerType, radioGroupId, radioButtonId, checkBoxGroupId, checkBoxId, editTextId, current_questionText, current_questionImage);

        nextButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent buttonAction) {
                switch (buttonAction.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        nextButton.setAlpha(.8f);
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        nextButton.setAlpha(.6f);
                        Boolean userAnswered = false;
                        if (currentQuestion + 1 <= nrOfQuestions) {
                            switch (quizQuestion[difficultyLevel][crtQ].answerType) {
                                case "C": {
                                    ArrayList<String> userAnswersCheckbox = new ArrayList<String>();
                                    int nrOfCheckedBoxes = 0;
                                    for (int i = 0; i < quizQuestion[difficultyLevel][crtQ].correctAnswersCheckBox.length; i++)
                                        if (checkBoxId[i].isChecked()) {
                                            nrOfCheckedBoxes += 1;
                                            userAnswersCheckbox.add((String) checkBoxId[i].getText());
                                        }
                                    if (nrOfCheckedBoxes == 0) userAnswered = false;
                                    else {
                                        userAnswered = true;
                                        allUserAnswers.add(new UserAnswer("C", userAnswersCheckbox));
                                    }
                                    /** String[] answerListCheckbox = {"", "", "", ""};
                                     for (int i = 0; i < answerListCheckbox.length; i++)
                                     if (checkBoxId[i].isChecked())
                                     answerListCheckbox[i] = (String) checkBoxId[i].getText();
                                     if (AppTools.compareStringArrays(answerListCheckbox, quizQuestion[difficultyLevel][crtQ].correctAnswersCheckBox))  // compareStringArrays - method used for comparing string arrays
                                     AppTools.customToast(Quiz.this, Gravity.BOTTOM, 0, getResources().getInteger(R.integer.home_toast_vertical_uplift), Toast.LENGTH_SHORT, "Right answer");
                                     else
                                     AppTools.customToast(Quiz.this, Gravity.BOTTOM, 0, getResources().getInteger(R.integer.home_toast_vertical_uplift), Toast.LENGTH_SHORT, "Wrong answer"); **/
                                    break;
                                }
                                case "R": {
                                    for (int i = 0; i < radioButtonId.length; i++)
                                        if (radioButtonId[i].isChecked()) {
                                            allUserAnswers.add(new UserAnswer("R", (String) radioButtonId[i].getText()));
                                            userAnswered = true;
                                            /**
                                             if (radioButtonId[i].getText().equals(quizQuestion[difficultyLevel][crtQ].correctAnswerRadio))  //Strings' values' are compared with equals.(), not with ==, because they are objects, not primitive data.
                                             AppTools.customToast(Quiz.this, Gravity.BOTTOM, 0, getResources().getInteger(R.integer.home_toast_vertical_uplift), Toast.LENGTH_SHORT, "Right answer");
                                             else
                                             AppTools.customToast(Quiz.this, Gravity.BOTTOM, 0, getResources().getInteger(R.integer.home_toast_vertical_uplift), Toast.LENGTH_SHORT, "Wrong answer");
                                             **/
                                            break;      // exit for loop after the radio button is checked
                                        }
                                    break;
                                }
                                case "E": {
                                    if (editTextId.getText().toString().trim().length() > 0) {          // "trim" returns a copy of this string with leading and trailing white space removed; the user will not be able to give an answer consisting just of white spaces
                                        userAnswered = true;
                                        allUserAnswers.add(new UserAnswer("E", editTextId.getText().toString()));
                                        Log.v("string equals to", "starting point ->" + editTextId.getText().toString() + "<- ending point ");
                                    }
                                    /** boolean answeredCorrectly = false;
                                     for (int i = 0; i < quizQuestion[difficultyLevel][crtQ].correctAnswerEdit.length; i++)
                                     if (editTextId.getText().toString().equalsIgnoreCase(quizQuestion[difficultyLevel][crtQ].correctAnswerEdit[i])) {
                                     answeredCorrectly = true;
                                     break;
                                     }
                                     if (answeredCorrectly)
                                     AppTools.customToast(Quiz.this, Gravity.BOTTOM, 0, getResources().getInteger(R.integer.home_toast_vertical_uplift), Toast.LENGTH_SHORT, "Right answer");
                                     else
                                     AppTools.customToast(Quiz.this, Gravity.BOTTOM, 0, getResources().getInteger(R.integer.home_toast_vertical_uplift), Toast.LENGTH_SHORT, "Wrong answer"); **/
                                    break;
                                }
                            }
                            if (!userAnswered)
                                AppTools.customToast(Quiz.this, Gravity.CENTER, 0, getResources().getInteger(R.integer.quiz_toast_previous_question), Toast.LENGTH_SHORT, "Please answer the question!");
                            else if (currentQuestion + 1 == nrOfQuestions) {
                                float score = 0;
                                for (int count = 0; count + 1 <= nrOfQuestions; count++)
                                    switch (allUserAnswers.get(count).answerType) {
                                        case "C": {
                                            String[] answerListCheckbox = {"", "", "", ""};
                                            for (int i = 0; i < allUserAnswers.get(count).answersCheckBox.size(); i++)
                                                answerListCheckbox[i] = allUserAnswers.get(count).answersCheckBox.get(i);
                                            if (AppTools.compareStringArrays(answerListCheckbox, quizQuestion[difficultyLevel][showAnswersOrder[count]].correctAnswersCheckBox))
                                                score += 1;
                                        }
                                        case "R": {
                                            if (allUserAnswers.get(count).answerRadioEdit.equals(quizQuestion[difficultyLevel][showAnswersOrder[count]].correctAnswerRadio))
                                                score += 1;
                                            break;
                                        }
                                        case "E": {
                                            for (int i = 0; i < quizQuestion[difficultyLevel][showAnswersOrder[count]].correctAnswerEdit.length; i++)
                                                if (allUserAnswers.get(count).answerRadioEdit.equalsIgnoreCase(quizQuestion[difficultyLevel][showAnswersOrder[count]].correctAnswerEdit[i])) {
                                                    score += 1;
                                                    break;
                                                }
                                            break;
                                        }
                                    }
                                score = (score / nrOfQuestions) * 10;
                                AppTools.customToast(Quiz.this, Gravity.CENTER, 0, getResources().getInteger(R.integer.quiz_toast_previous_question), Toast.LENGTH_SHORT, "Your score is " + String.valueOf(score) + " out of 10");
                            } else {
                                currentQuestion += 1;
                                crtQ = showQuestionsOrder[currentQuestion];
                                displayQuestion(quizQuestion[difficultyLevel][crtQ].answerType, radioGroupId, radioButtonId, checkBoxGroupId, checkBoxId, editTextId, current_questionText, current_questionImage);
                            }
                        }
                        /** else {
                         for (int i = 0; i < nrOfQuestions; i++) {
                         Log.v("question nr ", String.valueOf(i + 1) + "\n");
                         switch (allUserAnswers.get(i).answerType) {
                         case "C": {
                         for (int x = 0; x < allUserAnswers.get(i).answersCheckBox.size(); x++)
                         Log.v("checkbox ", String.valueOf(x + 1) + ":" + allUserAnswers.get(i).answersCheckBox.get(x) + "\n");
                         break;
                         }
                         case "R": {
                         Log.v("radio ", allUserAnswers.get(i).answerRadioEdit + "\n");
                         break;
                         }
                         case "E": {
                         Log.v("edit ", allUserAnswers.get(i).answerRadioEdit + "\n");
                         break;
                         }
                         }
                         }
                         } **/
                    }
                }
                return false;
            }
        });

        previousButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent buttonAction) {
                switch (buttonAction.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        previousButton.setAlpha(.8f);
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        previousButton.setAlpha(.6f);
                        AppTools.customToast(Quiz.this, Gravity.CENTER, 0, getResources().getInteger(R.integer.quiz_toast_previous_question), Toast.LENGTH_SHORT, getResources().getString(R.string.previous_button_forbidden_message));
                        break;
                    }
                }
                return false;
            }
        });
    }

    public void displayQuestion(String answerType, RadioGroup radioGroupId, RadioButton[] radioButtonId, LinearLayout checkBoxGroupId, CheckBox[] checkBoxId, EditText editTextId, TextView current_questionText, ImageView current_questionImage) {
        // when displaying a new question, we firstly erase the content from the previous question
        checkBoxGroupId.setVisibility(View.GONE);
        for (int i = 0; i < showAnswersOrder.length; i++) {
            checkBoxId[i].setChecked(false);
            radioButtonId[i].setChecked(false);
            editTextId.setText("");
            editTextId.setHint(getResources().getString(R.string.edit_text_hint));
        }
        radioGroupId.setVisibility(View.GONE);
        editTextId.setVisibility(View.GONE);

        //below we show on the screen the question text, image and the question number
        current_questionText.setText(quizQuestion[difficultyLevel][crtQ].question + "\n(question " + String.valueOf(currentQuestion + 1) + " out of " + String.valueOf(nrOfQuestions) + ")");
        current_questionImage.setImageResource(quizQuestion[difficultyLevel][crtQ].imageResId);

        switch (answerType) {
            case "C": {
                checkBoxGroupId.setVisibility(View.VISIBLE);
                AppTools.shuffleArray(showAnswersOrder);    // here we shuffle the answers order for the checkbox question
                for (int i = 0; i < showAnswersOrder.length; i++) {
                    checkBoxId[i].setVisibility(View.VISIBLE);
                    checkBoxId[i].setText(quizQuestion[difficultyLevel][crtQ].possibleAnswers[showAnswersOrder[i]]);
                }
                break;
            }
            case "R": {
                radioGroupId.setVisibility(View.VISIBLE);
                AppTools.shuffleArray(showAnswersOrder);        // here we shuffle the answers order for the radio question
                for (int i = 0; i < showAnswersOrder.length; i++)
                    radioButtonId[i].setText(quizQuestion[difficultyLevel][crtQ].possibleAnswers[showAnswersOrder[i]]);
                break;
            }
            case "E": {
                editTextId.setVisibility(View.VISIBLE);
                break;
            }
        }
    }

    private void initializeQuestions(int difficultyLevel) {
        if (difficultyLevel == 0) {
            quizQuestion[difficultyLevel][0] = new Question(R.drawable.b01_david_goliath, getResources().getString(R.string.q_b_1_txt), "C",
                    getResources().getStringArray(R.array.q_b_1_ans), getResources().getStringArray(R.array.q_b_1_cr));
            quizQuestion[difficultyLevel][1] = new Question(R.drawable.b02_red_sea, getResources().getString(R.string.q_b_2_txt), "E",
                    getResources().getStringArray(R.array.q_b_2_cr));
            quizQuestion[difficultyLevel][2] = new Question(R.drawable.b03_jesus_multiplication_loaves, getResources().getString(R.string.q_b_3_txt), "R",
                    getResources().getStringArray(R.array.q_b_3_ans), getResources().getString(R.string.q_b_3_cr));
            quizQuestion[difficultyLevel][3] = new Question(R.drawable.b04_absalom, getResources().getString(R.string.q_b_4_txt), "C",
                    getResources().getStringArray(R.array.q_b_4_ans), getResources().getStringArray(R.array.q_b_4_cr));
            quizQuestion[difficultyLevel][4] = new Question(R.drawable.b05_pothiphar_wife, getResources().getString(R.string.q_b_5_txt), "E",
                    getResources().getStringArray(R.array.q_b_5_cr));
            quizQuestion[difficultyLevel][5] = new Question(R.drawable.b06_hannah_samuel, getResources().getString(R.string.q_b_6_txt), "R",
                    getResources().getStringArray(R.array.q_b_6_ans), getResources().getString(R.string.q_b_6_cr));
            quizQuestion[difficultyLevel][6] = new Question(R.drawable.b07_jesus_died, getResources().getString(R.string.q_b_7_txt), "C",
                    getResources().getStringArray(R.array.q_b_7_ans), getResources().getStringArray(R.array.q_b_7_cr));
            quizQuestion[difficultyLevel][7] = new Question(R.drawable.b08_paul, getResources().getString(R.string.q_b_8_txt), "E",
                    getResources().getStringArray(R.array.q_b_8_cr));
            quizQuestion[difficultyLevel][8] = new Question(R.drawable.b09_the_pentecost, getResources().getString(R.string.q_b_9_txt), "R",
                    getResources().getStringArray(R.array.q_b_9_ans), getResources().getString(R.string.q_b_9_cr));
            quizQuestion[difficultyLevel][9] = new Question(R.drawable.b10_nehemiah_wall, getResources().getString(R.string.q_b_10_txt), "C",
                    getResources().getStringArray(R.array.q_b_10_ans), getResources().getStringArray(R.array.q_b_10_cr));

        } else {
            quizQuestion[difficultyLevel][0] = new Question(R.drawable.a01_daniel_friends_refusing_food, getResources().getString(R.string.q_a_1_txt), "C",
                    getResources().getStringArray(R.array.q_a_1_ans), getResources().getStringArray(R.array.q_a_1_cr));
            quizQuestion[difficultyLevel][1] = new Question(R.drawable.a02_golden_calf, getResources().getString(R.string.q_a_2_txt), "C",
                    getResources().getStringArray(R.array.q_a_2_ans), getResources().getStringArray(R.array.q_a_2_cr));
            quizQuestion[difficultyLevel][2] = new Question(R.drawable.a03_jonah_fish, getResources().getString(R.string.q_a_3_txt), "R",
                    getResources().getStringArray(R.array.q_a_3_ans), getResources().getString(R.string.q_a_3_cr));
            quizQuestion[difficultyLevel][3] = new Question(R.drawable.a04_samson_the_lion, getResources().getString(R.string.q_a_4_txt), "R",
                    getResources().getStringArray(R.array.q_a_4_ans), getResources().getString(R.string.q_a_4_cr));
            quizQuestion[difficultyLevel][4] = new Question(R.drawable.a05_john_patmos, getResources().getString(R.string.q_a_5_txt), "E",
                    getResources().getStringArray(R.array.q_a_5_cr));
            quizQuestion[difficultyLevel][5] = new Question(R.drawable.a06_simon_cyrene, getResources().getString(R.string.q_a_6_txt), "E",
                    getResources().getStringArray(R.array.q_a_6_cr));
            quizQuestion[difficultyLevel][6] = new Question(R.drawable.a07_jesus_died, getResources().getString(R.string.q_a_7_txt), "C",
                    getResources().getStringArray(R.array.q_a_7_ans), getResources().getStringArray(R.array.q_a_7_cr));
            quizQuestion[difficultyLevel][7] = new Question(R.drawable.a08_ruth_obed, getResources().getString(R.string.q_a_8_txt), "C",
                    getResources().getStringArray(R.array.q_a_8_ans), getResources().getStringArray(R.array.q_a_8_cr));
            quizQuestion[difficultyLevel][8] = new Question(R.drawable.a09_peter_liberated, getResources().getString(R.string.q_a_9_txt), "R",
                    getResources().getStringArray(R.array.q_a_9_ans), getResources().getString(R.string.q_a_9_cr));
            quizQuestion[difficultyLevel][9] = new Question(R.drawable.a10_queen_esther, getResources().getString(R.string.q_a_10_txt), "E",
                    getResources().getStringArray(R.array.q_a_10_cr));
        }
    }
}
