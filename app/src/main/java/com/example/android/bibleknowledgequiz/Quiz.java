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
import static android.widget.Toast.makeText;
import static com.example.android.bibleknowledgequiz.AppTools.showDialogBoxNewQuiz;
import static com.example.android.bibleknowledgequiz.HomeScreen.BUNDLE_LEVEL;
import static com.example.android.bibleknowledgequiz.HomeScreen.BUNDLE_QUESTIONS;
import static com.example.android.bibleknowledgequiz.HomeScreen.INTENT_TOQUIZ;
import static com.example.android.bibleknowledgequiz.R.layout.activity_quiz;
import static java.math.RoundingMode.UP;

public class Quiz extends AppCompatActivity {
    int difficultyLevel, nrOfQuestions;                         // these two variables retrieve data from bundle from HomeScreen;
    int currentQuestion, crtQ;                                  // these two variables helps us to go through the all the questions and to show the questions in a shuffled order
    public Question[][] quizQuestion = new Question[2][10];     // quizQuestion - array of questions: [2] meaning beginner or advanced; [10] meaning the number of total questions in the quiz

    // below we initialize the answers and questions showing order variables;
    int[] showAnswersOrder = {0, 1, 2, 3};      // after shuffling, the showAnswersOrder might look like this: {2, 0, 1, 3} or any other combination
    int[] showQuestionsOrder = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    long startTime, endTime, totalTime;
    boolean reviewQuiz = false;

    // below are the variables for swiping left or right
    float x1, x2;
    static final int MIN_DISTANCE = 150;

    // below we declare a variable Array of UserAnswer where we record all user answers
    ArrayList<UserAnswer> allUserAnswers = new ArrayList<UserAnswer>();

    // below we constants for bundle saving (rotation / onSavedInstanceState)
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

    /*******************************
     * ONSAVEDINSTANCESTATE METHOD *
     ******************************/
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
        super.onSaveInstanceState(dataToSave);
    }

    /*******************
     * ONCREATE METHOD *
     ******************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_quiz);

        // below we initialize radio buttons, radio group, checkboxes and edit text from ACTIVITY_QUIZ.XML; we do it here in order not to use expensive "findViewById" too often;
        // findViewById in java file related to activity before setContentView will always return NULL!!!
        // this is the reason for creating method "displayQuestion" and other methods with all parameters below because we could not use them as global variables, since we could not initialize them before setContentView
        // if we try to initialize them as global e.g. "RadioGroup radioGroupId;" and then declare them in OnCreate method after "setContentView", we will get a NullPointerException error
        final RadioGroup radioGroupId = (RadioGroup) findViewById(R.id.radio_group);
        final RadioButton[] radioButtonId = {(RadioButton) radioGroupId.findViewById(R.id.answers_radio_1), (RadioButton) radioGroupId.findViewById(R.id.answers_radio_2),
                (RadioButton) radioGroupId.findViewById(R.id.answers_radio_3), (RadioButton) radioGroupId.findViewById(R.id.answers_radio_4)};
        final LinearLayout checkBoxGroupId = (LinearLayout) findViewById(R.id.answers_checkbox_layout);
        final CheckBox[] checkBoxId = {(CheckBox) findViewById(R.id.answers_checkbox_1), (CheckBox) findViewById(R.id.answers_checkbox_2),
                (CheckBox) findViewById(R.id.answers_checkbox_3), (CheckBox) findViewById(R.id.answers_checkbox_4)};
        final EditText editTextId = (EditText) findViewById(R.id.possible_answers_editText);
        final TextView current_questionText = (TextView) findViewById(R.id.current_question);
        final ImageView current_questionImage = (ImageView) findViewById(R.id.current_picture);
        final ImageView overFlowButton = (ImageView) findViewById(R.id.overflow_button);
        final RelativeLayout quizLayout = (RelativeLayout) findViewById(R.id.activity_quiz_screen);

        if (savedInstanceState == null) {
            startTime = System.currentTimeMillis();
            // below we retrieve information saved in Bundle from the Home Activity
            Intent intent = getIntent();                            // getIntent(), when called in an Activity, gives you a reference to the Intent which was used to launch this Activity.
            Bundle bundle = intent.getBundleExtra(INTENT_TOQUIZ);   // take care - getExtras() not working for retrieving a Bundle, it returns null;
            nrOfQuestions = bundle.getInt(BUNDLE_QUESTIONS);
            difficultyLevel = bundle.getInt(BUNDLE_LEVEL);          // difficulty level is 0 or 1

            // the order is shuffled every time, for the user not to "learn" which question follows or the order of the answers within every question
            AppTools.shuffleArray(showQuestionsOrder);  // after shuffling, the showQuestionsOrder might look like this: {8, 6, 2, 0, 9, 1, 3, 5, 7, 4} or any other combination

            AppTools.customToast(Quiz.this, Gravity.CENTER, 0, getResources().getInteger(R.integer.quiz_toast_previous_question), Toast.LENGTH_LONG, "Please swipe left or right to navigate through the quiz!");
            initializeQuestions(difficultyLevel);           // questions are defined in an array of questions in the method "initializeQuestions" based on the difficulty level chosen by the user;
            currentQuestion = 0;                            // even if is initialized by default with 0, for better tracking we initialized it here;
            crtQ = showQuestionsOrder[currentQuestion];     // we use a different counter for questions, based on the previous shuffling;
            displayQuestion(quizQuestion[difficultyLevel][crtQ].answerType, radioGroupId, radioButtonId, checkBoxGroupId, checkBoxId, editTextId, current_questionText, current_questionImage, false);
        } else {
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
            displayQuestion(quizQuestion[difficultyLevel][crtQ].answerType, radioGroupId, radioButtonId, checkBoxGroupId, checkBoxId, editTextId, current_questionText, current_questionImage, true);
        }
        // below is the listener for the "swipe gesture" button (right of left)
        quizLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent touchEvent) {
                switch (touchEvent.getAction()) {
                    // when user first touches the screen we get x1 coordinates
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
                                nextQuestion(radioGroupId, radioButtonId, checkBoxGroupId, checkBoxId, editTextId, current_questionText, current_questionImage);
                            }
                            // if right to left sweep event on screen
                            else
                                previousQuestion(radioGroupId, radioButtonId, checkBoxGroupId, checkBoxId, editTextId, current_questionText, current_questionImage);
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

    /********************************************
     * THE BELOW METHODS HANDLE THE BACK BUTTON *
     *******************************************/
    @Override
    public void onBackPressed() {
        AppTools.showDialogBoxNewQuiz(this);
    }

    /********************************************************************************
     * THE BELOW METHOD IMPLEMENTS THE NEXT QUESTION, EITHER IN QUIZ OR REVIEW MODE *
     *******************************************************************************/
    public void nextQuestion(RadioGroup radioGroupId, RadioButton[] radioButtonId, LinearLayout checkBoxGroupId, CheckBox[] checkBoxId,
                             EditText editTextId, TextView current_questionText, ImageView current_questionImage) {
        // the below block of code applies when Quiz mode is active
        if (!reviewQuiz) {
            if (currentQuestion + 1 <= nrOfQuestions) {
                if (!quizMode(checkBoxId, radioButtonId, editTextId))   // the method quizMode records user's answers and returns "true" or "false" if the user answered the question or not
                    AppTools.customToast(Quiz.this, Gravity.CENTER, 0, getResources().getInteger(R.integer.quiz_toast_previous_question), Toast.LENGTH_SHORT, "Please answer the question!");
                else if (currentQuestion + 1 == nrOfQuestions)
                    showFinishQuizDialogBox(Quiz.this, calculateScore(), radioGroupId, radioButtonId, checkBoxGroupId, checkBoxId, editTextId, current_questionText, current_questionImage);
                else {
                    currentQuestion += 1;
                    crtQ = showQuestionsOrder[currentQuestion];
                    displayQuestion(quizQuestion[difficultyLevel][crtQ].answerType, radioGroupId, radioButtonId, checkBoxGroupId, checkBoxId, editTextId, current_questionText, current_questionImage, false);
                }
            }
        }

        // the below block of code applies when Review mode is active
        else {
            if (currentQuestion + 1 == nrOfQuestions)
                AppTools.customToast(Quiz.this, Gravity.CENTER, 0, getResources().getInteger(R.integer.quiz_toast_previous_question), Toast.LENGTH_SHORT, "This is the last question!");
            else {
                currentQuestion += 1;
                crtQ = showQuestionsOrder[currentQuestion];
                displayQuestion(quizQuestion[difficultyLevel][crtQ].answerType, radioGroupId, radioButtonId, checkBoxGroupId, checkBoxId, editTextId, current_questionText, current_questionImage, false);
            }
        }
    }

    /************************************************************************************
     * THE BELOW METHOD IMPLEMENTS THE PREVIOUS QUESTION, EITHER IN QUIZ OR REVIEW MODE *
     ***********************************************************************************/
    public void previousQuestion(RadioGroup radioGroupId, RadioButton[] radioButtonId, LinearLayout checkBoxGroupId, CheckBox[] checkBoxId,
                                 EditText editTextId, TextView current_questionText, ImageView current_questionImage) {

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
                displayQuestion(quizQuestion[difficultyLevel][crtQ].answerType, radioGroupId, radioButtonId, checkBoxGroupId, checkBoxId, editTextId, current_questionText, current_questionImage, false);
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
                if (editTextId.getText().toString().trim().length() > 0) {          // "trim" returns a copy of this string with leading and trailing white space removed; the user will not be able to give an answer consisting just of white spaces
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
    private void showFinishQuizDialogBox(Activity activity, float score, final RadioGroup radioGroupId, final RadioButton[] radioButtonId, final LinearLayout checkBoxGroupId,
                                         final CheckBox[] checkBoxId, final EditText editTextId, final TextView current_questionText, final ImageView current_questionImage) {

        // below the section of calculating time spent in the quiz
        endTime = System.currentTimeMillis();
        totalTime = (endTime - startTime) / 1000;
        int minSpent = (int) totalTime / 60;
        int secSpent = (int) totalTime % 60;
        String messageToShow = "";
        if (minSpent != 0)
            messageToShow = "Your score is " + String.format("%.1f", score) + " out of 10. You finished the quiz in " + String.valueOf(minSpent) + " minutes and " + String.valueOf(secSpent) + " seconds";
        else
            messageToShow = "Your score is " + String.format("%.1f", score) + " out of 10. You finished the quiz in " + String.valueOf(secSpent) + " seconds";

        // below we implement the dialog box
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);                           // requestFeature has to be added before setting the content
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
                dialog.dismiss();
                displayQuestion(quizQuestion[difficultyLevel][crtQ].answerType, radioGroupId, radioButtonId, checkBoxGroupId, checkBoxId, editTextId, current_questionText, current_questionImage, false);
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
    private void displayQuestion(String answerType, RadioGroup radioGroupId, RadioButton[] radioButtonId, LinearLayout checkBoxGroupId, CheckBox[] checkBoxId,
                                 EditText editTextId, TextView current_questionText, ImageView current_questionImage, boolean screenRotation) {

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
        current_questionText.setText(quizQuestion[difficultyLevel][crtQ].question + "\n(question " + String.valueOf(currentQuestion + 1) + " out of " + String.valueOf(nrOfQuestions) + ")");
        current_questionImage.setImageResource(quizQuestion[difficultyLevel][crtQ].imageResId);

        switch (answerType) {
            case "C": {
                checkBoxGroupId.setVisibility(View.VISIBLE);
                if (reviewQuiz)
                    for (int i1 = 0; i1 < showAnswersOrder.length; i1++)
                        showAnswersOrder[i1] = allUserAnswers.get(currentQuestion).checkBoxOrder[i1];   // if we are in the review mode, we do not want to shuffle the answers order
                else if (!reviewQuiz && !screenRotation)                                                // else, we shuffle the answers order for the checkbox question if we are in the quiz mode and the screen has not been rotated
                    AppTools.shuffleArray(showAnswersOrder);                                            // one more case left: if we are in the quiz mode [!reviewQuiz] and screen has not been rotated, we will not change the answers order, which was saved in the Bundle
                for (int i2 = 0; i2 < showAnswersOrder.length; i2++) {
                    checkBoxId[i2].setVisibility(View.VISIBLE);
                    checkBoxId[i2].setText(quizQuestion[difficultyLevel][crtQ].possibleAnswers[showAnswersOrder[i2]]);

                    if (reviewQuiz) {
                        checkBoxId[i2].setClickable(false);
                        for (int i3 = 0; i3 < quizQuestion[difficultyLevel][crtQ].correctAnswersCheckBox.length; i3++)              // checking if the answer is the correct answer, then its background will be coloured
                            if (checkBoxId[i2].getText().equals(quizQuestion[difficultyLevel][crtQ].correctAnswersCheckBox[i3]))
                                checkBoxId[i2].setBackgroundColor(ContextCompat.getColor(Quiz.this, R.color.correct_answers));      // ContextCompat - will choose the Marshmallow two parameter method or the pre-Marshmallow method appropriately.
                        for (int i4 = 0; i4 < allUserAnswers.get(currentQuestion).answersCheckBox.size(); i4++)                     // setting "checked" the answers given by the user
                            if (checkBoxId[i2].getText().equals(allUserAnswers.get(currentQuestion).answersCheckBox.get(i4)) && !screenRotation)   // when screen has not been rotated, the "cheked" status is saved within the object "Checkbox" in the Bundle and is shown when we make it visible and setText above
                                checkBoxId[i2].setChecked(true);                                                                    // but when the screen is not rotated, all checkboxes are unchecked above, thus we need to recheck them here
                    }
                }
                break;
            }
            case "R": {
                radioGroupId.setVisibility(View.VISIBLE);
                if (!reviewQuiz) {
                    if (!screenRotation)
                        AppTools.shuffleArray(showAnswersOrder);                                                // here we shuffle the answers order for the radio questions just if the screen was not rotated
                    for (int i = 0; i < 4; i++) {
                        radioButtonId[i].setVisibility(View.VISIBLE);
                        radioButtonId[i].setText(quizQuestion[difficultyLevel][crtQ].possibleAnswers[showAnswersOrder[i]]);
                    }
                } else {
                    for (int i = 0; i < 4; i++) {
                        showAnswersOrder[i] = allUserAnswers.get(currentQuestion).radioOrder[i];
                        radioButtonId[i].setText(quizQuestion[difficultyLevel][crtQ].possibleAnswers[showAnswersOrder[i]]);
                        if (radioButtonId[i].getText().equals(allUserAnswers.get(currentQuestion).radioAnswer) && !screenRotation) // when screen has not been rotated, the "cheked" status is saved within the object "RadioButton" or "RadioGroup" in the Bundle and is shown when we make it visible and setText above
                            radioGroupId.check(radioButtonId[i].getId());                                               // or "//radioButtonId[i].setChecked(true);" both solutios work fine
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
                    // below we initialize "userCorrAnsw" (the index of the correct answer given by the user in the array of all correct answers; "allCorrectAnswers" contains all possible correct answers;
                    int userCorrAnswPos = -1;
                    String allCorrectAnswers = "";

                    // below we extract the position of the correct answer given by the user in the array of possible correct answers
                    for (int i = 0; i < quizQuestion[difficultyLevel][crtQ].correctAnswerEdit.length; i++)
                        if (allUserAnswers.get(currentQuestion).answerEdit.equalsIgnoreCase(quizQuestion[difficultyLevel][crtQ].correctAnswerEdit[i])) {
                            userCorrAnswPos = i;
                            break;
                        }

                    // below we define the text to be shown on the screen if the user did not give any correct answer
                    if (userCorrAnswPos == -1)
                        allCorrectAnswers = "Your answer: " + allUserAnswers.get(currentQuestion).answerEdit + "\nCorrect possible answer(s): ";

                    // below we build a single String with all possible correct answers
                    allCorrectAnswers += quizQuestion[difficultyLevel][crtQ].correctAnswerEdit[0];
                    for (int i = 1; i < quizQuestion[difficultyLevel][crtQ].correctAnswerEdit.length; i++) {
                        allCorrectAnswers += "; " + quizQuestion[difficultyLevel][crtQ].correctAnswerEdit[i];
                    }

                    // below we use Spannable in order to set colour the correct answer differently within the whole String with correct answers
                    if (userCorrAnswPos != -1) {
                        Spannable allSpan = new SpannableString(allCorrectAnswers);
                        // in the next 3 lines of code we identify the length of words and spaces and ";" before the correct answer;
                        int posStart = userCorrAnswPos * 2;
                        for (int x = 0; x < userCorrAnswPos; x++)
                            posStart += quizQuestion[difficultyLevel][crtQ].correctAnswerEdit[x].length();
                        allSpan.setSpan(new ForegroundColorSpan(ContextCompat.getColor(Quiz.this, R.color.correct_answers_text)), posStart,
                                posStart + quizQuestion[difficultyLevel][crtQ].correctAnswerEdit[userCorrAnswPos].length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        editTextId.setText(allSpan);
                    } else
                        editTextId.setText(allCorrectAnswers);

                    // below we make the text un-editable; another method to is "editTextId.setKeyListener(null);"
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
