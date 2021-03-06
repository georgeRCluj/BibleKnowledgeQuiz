package com.example.android.bibleknowledgequiz;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import static android.R.attr.data;
import static android.R.attr.left;
import static android.R.attr.right;
import static android.R.attr.rotation;
import static android.R.attr.start;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.V;
import static android.widget.Toast.makeText;
import static com.example.android.bibleknowledgequiz.AppTools.showDialogBoxNewQuiz;
import static com.example.android.bibleknowledgequiz.HomeScreen.BUNDLE_LEVEL;
import static com.example.android.bibleknowledgequiz.HomeScreen.BUNDLE_QUESTIONS;
import static com.example.android.bibleknowledgequiz.HomeScreen.INTENT_TOQUIZ;
import static com.example.android.bibleknowledgequiz.R.layout.activity_quiz;
import static java.math.RoundingMode.UP;

/********************************************
 * THIS CLASS IS RELATED TO THE QUIZ SCREEN *
 *******************************************/

public class Quiz extends AppCompatActivity {

    /********************************************************
     * INITIALIZATION OF GLOBAL VARIABLES USED IN THE CLASS *
     *******************************************************/
    int difficultyLevel, nrOfQuestions;                         // these two variables retrieve data from bundle from HomeScreen;
    int currentQuestion, crtQ;                                  // these two variables helps us to go through the all the questions and to show the questions in a shuffled order
    public Question[][] quizQuestion = new Question[2][10];     // quizQuestion - array of questions: [2] meaning "beginner" or "advanced"; [10] meaning the number of total questions in the quiz; at any time there can be added questions

    // below we initialize the "answers and questions showing order" variables, time, game mode variables
    int[] showAnswersOrder = {0, 1, 2, 3};                      // after shuffling, the showAnswersOrder might look like this: {2, 0, 1, 3} or any other combination
    int[] showQuestionsOrder = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};  // after shuffling, the showAnswersOrder might look like this: {8, 6, 7, 5, 2, 0, 1, 3, 5, 4, 9} or any other combination
    long startTime, endTime, totalTime;                         // these variables will help us to determine how much time it takes the user to do the quiz
    boolean reviewQuiz = false;                                 // this variable determines the game mode - "reviewQuiz = false" meaning we are in quiz mode; "reviewQuiz = true" meaning we are in the review mode

    // below we initialize the variables related to the "showing up" on the screen of dialog boxes and overflow menu
    boolean showFinalDialogBox = false;

    // below are the variables for swiping left or right
    float x1, x2;
    static final int MIN_DISTANCE = 150;

    // below we declare a variable Array of UserAnswer where we record all user answers
    ArrayList<UserAnswer> allUserAnswers = new ArrayList<UserAnswer>();

    // below we declare constants for bundle saving (rotation / onSavedInstanceState)
    static final String BUNDLE_DIFFICULTY = "difficulty_level";
    static final String BUNDLE_NRQUEST = "no_of_questions";
    static final String BUNDLE_CRTQUEST = "current_question";
    static final String BUNDLE_CRTQ = "current_question_shuffled";
    static final String BUNDLE_ALLQUESTIONS = "questions";
    static final String BUNDLE_ANSWERSORDER = "answers_order";
    static final String BUNDLE_QUESTIONSORDER = "questions_order";
    static final String BUNDLE_STARTTIME = "start_time";
    static final String BUNDLE_ENDTIME = "end_time";
    static final String BUNDLE_TOTALTIME = "total_time";
    static final String BUNDLE_REVIEWMODE = "review_mode";
    static final String BUNDLE_ALLUSERANSWERS = "all_user_answers";
    static final String BUNDLE_SHOWFINALDIALOGBOX = "show_final_dialog_box";

    // below we initialize radio buttons, radio group, checkboxes, edit text box, current question text and image from ACTIVITY_QUIZ.XML;
    RadioGroup radioGroupId;
    RadioButton[] radioButtonId = new RadioButton[4];
    LinearLayout checkBoxGroupId;
    CheckBox[] checkBoxId = new CheckBox[4];
    EditText editTextId;
    TextView current_questionText;
    ImageView current_questionImage;

    /*****************************************************************************************************
     * ONSAVEDINSTANCESTATE METHOD, USED FOR ROTATION OF SCREEN, WHEN THE ACTIVITY IS KILLED BY THE O.S. *
     ****************************************************************************************************/
    @Override
    protected void onSaveInstanceState(Bundle dataToSave) {
        dataToSave.putInt(BUNDLE_DIFFICULTY, difficultyLevel);
        dataToSave.putInt(BUNDLE_NRQUEST, nrOfQuestions);
        dataToSave.putInt(BUNDLE_CRTQUEST, currentQuestion);
        dataToSave.putInt(BUNDLE_CRTQ, crtQ);
        dataToSave.putSerializable(BUNDLE_ALLQUESTIONS, quizQuestion);
        dataToSave.putIntArray(BUNDLE_ANSWERSORDER, showAnswersOrder);
        dataToSave.putIntArray(BUNDLE_QUESTIONSORDER, showQuestionsOrder);
        dataToSave.putLong(BUNDLE_STARTTIME, startTime);
        dataToSave.putLong(BUNDLE_ENDTIME, endTime);
        dataToSave.putLong(BUNDLE_TOTALTIME, totalTime);
        dataToSave.putBoolean(BUNDLE_REVIEWMODE, reviewQuiz);
        dataToSave.putSerializable(BUNDLE_ALLUSERANSWERS, allUserAnswers);
        dataToSave.putBoolean(BUNDLE_SHOWFINALDIALOGBOX, showFinalDialogBox);
        super.onSaveInstanceState(dataToSave);
    }

    /*******************
     * ONCREATE METHOD *
     ******************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_quiz);

        // below we declare radio buttons, radio group, checkboxes, edit text box, current question text and image from ACTIVITY_QUIZ.XML; we do it here in order not to use expensive "findViewById" too often;
        // findViewById in java file related to activity before setContentView will always return NULL!!!
        int[] radioButtons = {R.id.answers_radio_1, R.id.answers_radio_2, R.id.answers_radio_3, R.id.answers_radio_4};
        radioGroupId = (RadioGroup) findViewById(R.id.radio_group);
        for (int i = 0; i < radioButtons.length; i++)
            radioButtonId[i] = (RadioButton) findViewById(radioButtons[i]);
        checkBoxGroupId = (LinearLayout) findViewById(R.id.answers_checkbox_layout);
        int[] checkBoxes = {R.id.answers_checkbox_1, R.id.answers_checkbox_2, R.id.answers_checkbox_3, R.id.answers_checkbox_4};
        for (int i = 0; i < checkBoxes.length; i++)
            checkBoxId[i] = (CheckBox) findViewById(checkBoxes[i]);
        editTextId = (EditText) findViewById(R.id.possible_answers_editText);
        current_questionText = (TextView) findViewById(R.id.current_question);
        current_questionImage = (ImageView) findViewById(R.id.current_picture);
        final ImageView overFlowButton = (ImageView) findViewById(R.id.overflow_button);
        final RelativeLayout quizLayout = (RelativeLayout) findViewById(R.id.activity_quiz_screen);

        // below we initialize the game and take into account the rotation of the screen;
        if (savedInstanceState == null) {
            startTime = System.currentTimeMillis();

            // below we retrieve information saved in Bundle from the Home Activity
            Intent intent = getIntent();                            // getIntent(), when called in an Activity, gives you a reference to the Intent which was used to launch the Activity
            Bundle bundle = intent.getBundleExtra(INTENT_TOQUIZ);   // N.B.: "getExtras()" not working for retrieving a Bundle, it returns null; it works with "getBundleExtra(intent)"
            nrOfQuestions = bundle.getInt(BUNDLE_QUESTIONS);
            difficultyLevel = bundle.getInt(BUNDLE_LEVEL);          // difficulty level is 0 (beginner) or 1 (advanced)

            // the order is shuffled every time, for the user not to "learn" which question follows or the order of the answers within every question
            AppTools.shuffleArray(showQuestionsOrder);              // after shuffling, the showQuestionsOrder might look like this: {8, 6, 2, 0, 9, 1, 3, 5, 7, 4} or any other combination

            // below a toast telling the user that can swipe left or right in order to navigate through the quiz
            AppTools.customToast(Quiz.this, Gravity.CENTER, 0, getResources().getInteger(R.integer.quiz_toast_previous_question), Toast.LENGTH_LONG, getResources().getString(R.string.please_swipe_left_right));

            // below all the questions are initialized in an array of questions in the method "initializeQuestions" based on the difficulty level chosen by the user;
            initializeQuestions(difficultyLevel);

            // below we initialize currentQuestion (0) and crtQ (crtQ is the variable that helps us with shuffling); the first question is displayed
            currentQuestion = 0;
            crtQ = showQuestionsOrder[currentQuestion];
            displayQuestion(false);
        } else {
            // here we save data in a Bundle in case of rotation; the last argument in the "displayQuestion" method below ("true") transmits that a rotation took place
            difficultyLevel = savedInstanceState.getInt(BUNDLE_DIFFICULTY);
            nrOfQuestions = savedInstanceState.getInt(BUNDLE_NRQUEST);
            currentQuestion = savedInstanceState.getInt(BUNDLE_CRTQUEST);
            crtQ = savedInstanceState.getInt(BUNDLE_CRTQ);
            quizQuestion = (Question[][]) savedInstanceState.getSerializable(BUNDLE_ALLQUESTIONS);
            showAnswersOrder = savedInstanceState.getIntArray(BUNDLE_ANSWERSORDER);
            showQuestionsOrder = savedInstanceState.getIntArray(BUNDLE_QUESTIONSORDER);
            startTime = savedInstanceState.getLong(BUNDLE_STARTTIME);
            endTime = savedInstanceState.getLong(BUNDLE_ENDTIME);
            totalTime = savedInstanceState.getLong(BUNDLE_TOTALTIME);
            reviewQuiz = savedInstanceState.getBoolean(BUNDLE_REVIEWMODE);
            allUserAnswers = (ArrayList<UserAnswer>) savedInstanceState.getSerializable(BUNDLE_ALLUSERANSWERS);
            showFinalDialogBox = savedInstanceState.getBoolean(BUNDLE_SHOWFINALDIALOGBOX);
            displayQuestion(true);
            if (showFinalDialogBox)
                showFinishQuizDialogBox(this, calculateScore());
        }

        // below is the listener for the "swipe gesture" button (right or left)
        quizLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent touchEvent) {
                switch (touchEvent.getAction()) {
                    // when user first touches the screen we get x1 coordinates; the method can be extended in the future for y1 and y2 (down to up and up to down swiping)
                    case MotionEvent.ACTION_DOWN: {
                        x1 = touchEvent.getX();
                        break;
                    }
                    // when user releases the finger from the screen we get x2 coordinates
                    case MotionEvent.ACTION_UP: {
                        x2 = touchEvent.getX();
                        float deltaX = x2 - x1;
                        if (Math.abs(deltaX) > MIN_DISTANCE) {
                            //if right to left sweep event on screen (NEXT)
                            if (x2 < x1) {
                                nextQuestion();
                            }
                            // if right to left sweep event on screen
                            else
                                previousQuestion();
                        }
                        break;
                    }
                }
                return false;
            }
        });

        // below is the listener for the "overflow menu" button
        overFlowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppTools.showOverFlowPopUpMenu(Quiz.this, view);
            }
        });
    }

    /************************************************************
     * THE BELOW METHOD HANDLES THE BACK BUTTON AND MENU BUTTON *
     ***********************************************************/
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                AppTools.showOverFlowPopUpMenu(Quiz.this, findViewById(R.id.overflow_button));
                return false;
            case KeyEvent.KEYCODE_BACK:    // it also works with method "@Override public void onBackPressed() {};"
                AppTools.showDialogBoxNewQuiz(this);
                return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    /********************************************************************************
     * THE BELOW METHOD IMPLEMENTS THE NEXT QUESTION, EITHER IN QUIZ OR REVIEW MODE *
     *******************************************************************************/

    public void nextQuestion() {

        // the below block of code applies when "Quiz mode" is active
        if (!reviewQuiz) {
            if (currentQuestion + 1 <= nrOfQuestions) {
                if (!quizMode(checkBoxId, radioButtonId, editTextId))   // the method quizMode records user's answers and returns "true" or "false" if the user answered the question or not
                    AppTools.customToast(Quiz.this, Gravity.CENTER, 0, getResources().getInteger(R.integer.quiz_toast_previous_question), Toast.LENGTH_SHORT, getResources().getString(R.string.please_answer_the_question));
                else if (currentQuestion + 1 == nrOfQuestions)
                    showFinishQuizDialogBox(Quiz.this, calculateScore());
                else {
                    currentQuestion += 1;
                    crtQ = showQuestionsOrder[currentQuestion];
                    displayQuestion(false);
                }
            }
        }

        // the below block of code applies when "Review mode" is active
        else {
            if (currentQuestion + 1 == nrOfQuestions)
                AppTools.customToast(Quiz.this, Gravity.CENTER, 0, getResources().getInteger(R.integer.quiz_toast_previous_question), Toast.LENGTH_SHORT, getResources().getString(R.string.this_is_the_last_question));
            else {
                currentQuestion += 1;
                crtQ = showQuestionsOrder[currentQuestion];
                displayQuestion(false);
            }
        }
    }

    /*******************************************************************************************
     * THE BELOW METHOD IMPLEMENTS THE PREVIOUS QUESTION, IN BOTH "QUIZ MODE" OR "REVIEW MODE" *
     ******************************************************************************************/
    public void previousQuestion() {

        // the below block of code applies when Quiz mode is active
        if (!reviewQuiz)
            AppTools.customToast(Quiz.this, Gravity.CENTER, 0, getResources().getInteger(R.integer.quiz_toast_previous_question), Toast.LENGTH_SHORT, getResources().getString(R.string.previous_button_forbidden_message));
        else

        // the below block of code applies when Review mode is active
        {
            if (currentQuestion == 0)
                AppTools.customToast(Quiz.this, Gravity.CENTER, 0, getResources().getInteger(R.integer.quiz_toast_previous_question), Toast.LENGTH_SHORT, getResources().getString(R.string.previous_button_forbidden_message_2));
            else {
                currentQuestion -= 1;
                crtQ = showQuestionsOrder[currentQuestion];
                displayQuestion(false);
            }
        }
    }

    /********************************************************************************************************
     * THE METHOD RECORDS USERS' ANSWERS AND RETURNS TRUE OR FALSE IF THE USER ANSWERED THE QUESTION OR NOT *
     *******************************************************************************************************/
    private boolean quizMode(CheckBox[] checkBoxId, RadioButton[] radioButtonId, EditText editTextId) {
        boolean userAnswered = false;
        switch (quizQuestion[difficultyLevel][crtQ].answerType) {
            case "C": {                                                                        // "C" stands for "Checkbox" question
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
                    allUserAnswers.add(new UserAnswer("C", userAnswersCheckbox, showAnswersOrder));
                }
                break;
            }
            case "R": {                                                                         // "R" stands for "RadioButton" question
                for (int vr3 = 0; vr3 < radioButtonId.length; vr3++) {
                    if (radioButtonId[vr3].isChecked()) {
                        allUserAnswers.add(new UserAnswer("R", (String) radioButtonId[vr3].getText(), showAnswersOrder));
                        userAnswered = true;
                        break;      // exit for loop after the radio button is checked
                    }
                }
                break;
            }
            case "E": {                                                                         // "E" stands for "Edit" question
                if (editTextId.getText().toString().trim().length() > 0) {                      // "trim" returns a copy of this string with leading and trailing white space removed; the user will not be able to give an answer consisting just of white spaces
                    userAnswered = true;
                    allUserAnswers.add(new UserAnswer("E", editTextId.getText().toString().trim()));
                }
                break;
            }
        }
        return userAnswered;
    }

    /************************************************
     * THIS METHOD CALCULATES AND RETURNS THE SCORE *
     ***********************************************/
    private float calculateScore() {
        float score = 0;
        for (int count = 0; count + 1 <= nrOfQuestions; count++)
            switch (allUserAnswers.get(count).answerType) {
                case "C": {
                    String[] answerListCheckbox = {"", "", "", ""};
                    for (int i = 0; i < allUserAnswers.get(count).answersCheckBox.size(); i++)
                        answerListCheckbox[i] = allUserAnswers.get(count).answersCheckBox.get(i);
                    if (AppTools.compareStringArrays(answerListCheckbox, quizQuestion[difficultyLevel][showQuestionsOrder[count]].correctAnswersCheckBox))
                        score += 1;
                    break;
                }
                case "R": {
                    if (allUserAnswers.get(count).radioAnswer.equals(quizQuestion[difficultyLevel][showQuestionsOrder[count]].correctAnswerRadio))
                        score += 1;
                    break;
                }
                case "E": {
                    for (int i = 0; i < quizQuestion[difficultyLevel][showQuestionsOrder[count]].correctAnswerEdit.length; i++)
                        if (allUserAnswers.get(count).answerEdit.equalsIgnoreCase(quizQuestion[difficultyLevel][showQuestionsOrder[count]].correctAnswerEdit[i])) {
                            score += 1;
                            break;
                        }
                    break;
                }
            }
        score = (score / nrOfQuestions) * 10;
        return score;
    }

    /**************************************************************************************
     * THIS METHOD IS USED FOR DISPLAYING THE DIALOG BOX AFTER THE USER FINISHED THE QUIZ *
     **************************************************************************************/
    private void showFinishQuizDialogBox(Activity activity, float score) {

        // below the section of calculating time spent in the quiz
        if (!showFinalDialogBox)                    // we have to add this condition to cover the case when the user rotates the screen when the dialog box is shown and the end time is updated...
            endTime = System.currentTimeMillis();
        totalTime = (endTime - startTime) / 1000;
        int minSpent = (int) totalTime / 60;
        int secSpent = (int) totalTime % 60;
        String messageToShow = "";
        String yourScoreIs = getResources().getString(R.string.your_score_is);
        String outOf10 = getResources().getString(R.string.out_of_10);
        String inMinutes = getResources().getString(R.string.in_minutes);
        String inSeconds = getResources().getString(R.string.in_seconds);
        String in1Minute = getResources().getString(R.string.one_minute);
        String in1MinuteAnd = getResources().getString(R.string.one_minute_and);
        String minutesAnd = getResources().getString(R.string.minutes_and);

        if (minSpent != 0) {
            if (secSpent == 0) {
                if (minSpent == 1)
                    messageToShow = yourScoreIs + String.format("%.1f", score) + outOf10 + String.valueOf(minSpent) + in1Minute;
                else
                    messageToShow = yourScoreIs + String.format("%.1f", score) + outOf10 + String.valueOf(minSpent) + inMinutes;
            } else {
                if (minSpent == 1)
                    messageToShow = yourScoreIs + String.format("%.1f", score) + outOf10 + String.valueOf(minSpent) + in1MinuteAnd + String.valueOf(secSpent) + inSeconds;
                else
                    messageToShow = yourScoreIs + String.format("%.1f", score) + outOf10 + String.valueOf(minSpent) + minutesAnd + String.valueOf(secSpent) + inSeconds;
            }
        } else
            messageToShow = yourScoreIs + String.format("%.1f", score) + outOf10 + String.valueOf(secSpent) + inSeconds;

        // below we implement the dialog box which appears on the screen when the user finishes the quiz
        final Dialog dialog = new Dialog(activity);
        showFinalDialogBox = true;
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);                           // "requestFeature" has to be added before setting the content, otherwise an error will be thrown at runtime
        dialog.setContentView(R.layout.custom_dialog_box_finish_quiz);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView text = (TextView) dialog.findViewById(R.id.txt_dia);
        text.setText(messageToShow);
        Button reviewButton = (Button) dialog.findViewById(R.id.btn_review);
        Button exitButton = (Button) dialog.findViewById(R.id.btn_exit);
        Button newButton = (Button) dialog.findViewById(R.id.btn_new_quiz);

        newButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppTools.restartQuiz(Quiz.this);
            }
        });
        reviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reviewQuiz = true;
                currentQuestion = 0;
                crtQ = showQuestionsOrder[currentQuestion];
                showFinalDialogBox = false;
                dialog.dismiss();
                displayQuestion(false);
            }
        });
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.finishAffinity(Quiz.this);                               // ends up all activities, but does not force the app to close; use "finishAffinity();" for API > 15
            }
        });
        dialog.show();
    }

    /*************************************************************************************
     * THIS METHOD IS USED FOR DISPLAYING QUESTIONS EITHER IN QUIZ MODE OR IN REVIEW MODE *
     *************************************************************************************/
    private void displayQuestion(boolean screenRotation) {

        // when displaying a new question, we firstly erase the content from the previous question, except when we rotate the screen
        if (!screenRotation) {
            checkBoxGroupId.setVisibility(View.GONE);
            radioGroupId.setVisibility(View.GONE);
            editTextId.setVisibility(View.GONE);
            radioGroupId.clearCheck();
            for (int i = 0; i < showAnswersOrder.length; i++) {
                checkBoxId[i].setChecked(false);
                checkBoxId[i].setBackgroundColor(Color.TRANSPARENT);
                radioButtonId[i].setBackgroundColor(Color.TRANSPARENT);
            }
            editTextId.setText("");
            editTextId.setHint(getResources().getString(R.string.edit_text_hint));
        }

        //below we show on the screen the question text, image and the question number
        current_questionText.setText(quizQuestion[difficultyLevel][crtQ].question + getResources().getString(R.string.current_question_1) + String.valueOf(currentQuestion + 1) +
                getResources().getString(R.string.current_question_2) + String.valueOf(nrOfQuestions) + getResources().getString(R.string.current_question_3));
        current_questionImage.setImageResource(quizQuestion[difficultyLevel][crtQ].imageResId);

        switch (quizQuestion[difficultyLevel][crtQ].answerType) {
            case "C": {
                checkBoxGroupId.setVisibility(View.VISIBLE);
                if (reviewQuiz)
                    for (int i1 = 0; i1 < showAnswersOrder.length; i1++)
                        showAnswersOrder[i1] = allUserAnswers.get(currentQuestion).checkBoxOrder[i1];   // if we are in the review mode, we do not want to shuffle the answers order
                else if (!reviewQuiz && !screenRotation)                                                // else, we shuffle the answers order for the checkbox question if we are in the quiz mode and the screen has not been rotated
                    AppTools.shuffleArray(showAnswersOrder);                                            // if we are in the quiz mode [!reviewQuiz] and screen has not been rotated, we will not change the answers order, which was saved in the Bundle
                for (int i2 = 0; i2 < showAnswersOrder.length; i2++) {
                    checkBoxId[i2].setVisibility(View.VISIBLE);
                    checkBoxId[i2].setText(quizQuestion[difficultyLevel][crtQ].possibleAnswers[showAnswersOrder[i2]]);
                    if (reviewQuiz) {
                        checkBoxId[i2].setClickable(false);
                        for (int i3 = 0; i3 < quizQuestion[difficultyLevel][crtQ].correctAnswersCheckBox.length; i3++)              // checking if the answer is the correct answer, then its background will be coloured
                            if (checkBoxId[i2].getText().equals(quizQuestion[difficultyLevel][crtQ].correctAnswersCheckBox[i3]))
                                checkBoxId[i2].setBackgroundColor(ContextCompat.getColor(Quiz.this, R.color.correct_answers));      // ContextCompat - will choose the Marshmallow two parameter method or the pre-Marshmallow method appropriately.
                        for (int i4 = 0; i4 < allUserAnswers.get(currentQuestion).answersCheckBox.size(); i4++)                     // setting "checked" the answers given by the user
                            if (checkBoxId[i2].getText().equals(allUserAnswers.get(currentQuestion).answersCheckBox.get(i4)) && !screenRotation)   // when screen has not been rotated, the "checked" status is saved within the object "Checkbox" in the Bundle and is shown when we make it visible and setText above
                                checkBoxId[i2].setChecked(true);                                                                    // but when the screen is not rotated, all checkboxes are unchecked above, thus we need to recheck them here
                    }
                }
                break;
            }
            case "R": {
                radioGroupId.setVisibility(View.VISIBLE);
                if (!reviewQuiz) {
                    if (!screenRotation)
                        AppTools.shuffleArray(showAnswersOrder);                                                        // here we shuffle the answers order for the radio questions just if the screen was not rotated
                    for (int i = 0; i < 4; i++) {
                        radioButtonId[i].setVisibility(View.VISIBLE);
                        radioButtonId[i].setText(quizQuestion[difficultyLevel][crtQ].possibleAnswers[showAnswersOrder[i]]);
                    }
                } else {
                    for (int i = 0; i < 4; i++) {
                        showAnswersOrder[i] = allUserAnswers.get(currentQuestion).radioOrder[i];
                        radioButtonId[i].setText(quizQuestion[difficultyLevel][crtQ].possibleAnswers[showAnswersOrder[i]]);
                        if (radioButtonId[i].getText().equals(allUserAnswers.get(currentQuestion).radioAnswer) && !screenRotation)  // when screen has not been rotated, the "checked" status is saved within the object "RadioButton" or "RadioGroup" in the Bundle and is shown when we make it visible and setText above
                            radioGroupId.check(radioButtonId[i].getId());                                                           // or "radioButtonId[i].setChecked(true);" both solutions work fine
                        if (radioButtonId[i].getText().equals(quizQuestion[difficultyLevel][crtQ].correctAnswerRadio))
                            radioButtonId[i].setBackgroundColor(ContextCompat.getColor(Quiz.this, R.color.correct_answers));
                        radioButtonId[i].setClickable(false);
                    }
                }
                break;
            }

            case "E": {
                editTextId.setVisibility(View.VISIBLE);

                // the whole section below is for the quiz review
                if (reviewQuiz) {
                    // below we initialize "userCorrAnswerPos" (the index of the correct answer given by the user in the array of all correct answers; "allCorrectAnswers" contains all possible correct answers;
                    int userCorrAnswerPos = -1;
                    String allCorrectAnswers = "";

                    // below we extract the position of the correct answer given by the user in the array of possible correct answers, ignoring case
                    for (int i = 0; i < quizQuestion[difficultyLevel][crtQ].correctAnswerEdit.length; i++)
                        if (allUserAnswers.get(currentQuestion).answerEdit.equalsIgnoreCase(quizQuestion[difficultyLevel][crtQ].correctAnswerEdit[i])) {
                            userCorrAnswerPos = i;
                            break;
                        }

                    // below we define the text to be shown on the screen if the user did not give any correct answer
                    if (userCorrAnswerPos == -1)
                        allCorrectAnswers = getResources().getString(R.string.edit_answer_1) + allUserAnswers.get(currentQuestion).answerEdit + getResources().getString(R.string.edit_answer_2);

                    // below we build a single String with all possible correct answers
                    allCorrectAnswers += quizQuestion[difficultyLevel][crtQ].correctAnswerEdit[0];
                    for (int i = 1; i < quizQuestion[difficultyLevel][crtQ].correctAnswerEdit.length; i++) {
                        allCorrectAnswers += "; " + quizQuestion[difficultyLevel][crtQ].correctAnswerEdit[i];
                    }

                    // below we use Spannable in order to set colour of the correct answer within the whole String with correct answers
                    if (userCorrAnswerPos != -1) {
                        Spannable allSpan = new SpannableString(allCorrectAnswers);
                        // in the next 3 lines of code we identify the length of words and spaces and ";" before the correct answer;
                        int posStart = userCorrAnswerPos * 2;
                        for (int x = 0; x < userCorrAnswerPos; x++)
                            posStart += quizQuestion[difficultyLevel][crtQ].correctAnswerEdit[x].length();
                        allSpan.setSpan(new ForegroundColorSpan(ContextCompat.getColor(Quiz.this, R.color.correct_answers_text)), posStart,
                                posStart + quizQuestion[difficultyLevel][crtQ].correctAnswerEdit[userCorrAnswerPos].length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        editTextId.setText(allSpan);
                    } else
                        editTextId.setText(allCorrectAnswers);

                    // below we make the text un-editable; another method which works fine is "editTextId.setKeyListener(null);"
                    editTextId.setFocusable(false);
                    editTextId.setClickable(false);
                }
                break;
            }
        }

    }

    /****************************************************************************************************************
     * THIS METHOD IS USED FOR INITIALIZING ALL QUESTIONS; WHENEVER NEEDED, THE ARRAY OF QUESTIONS CAN BE INCREASED *
     ****************************************************************************************************************/

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
