package com.example.android.bibleknowledgequiz;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.Image;
import android.os.PersistableBundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

import static java.util.logging.Logger.global;

/********************************************
 * THIS CLASS IS RELATED TO THE HOME SCREEN *
 *******************************************/
public class HomeScreen extends AppCompatActivity {

    // private - only within the class;
    // final - it will not change its value, is constant;
    // static - it is a variable common to all objects, it is associated with the class, rather than with any object;
    // global (variable) - it is used within various methods in the class;
    public final static String INTENT_TOQUIZ = "com.example.android.bibleknowledgequiz.GoToQuiz"; // the level and the number of questions used in Intent as per Android guidelines: https://developer.android.com/reference/android/content/Intent.html#putExtra(java.lang.String, android.os.Bundle)
    public final static String BUNDLE_LEVEL = "level";
    public final static String BUNDLE_QUESTIONS = "questions";
    private final static int MIN_NR_QUEST = 3; // minimum number of questions

    /*******************
     * onCreate Method *
     ******************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        // initialization of buttons and radioButtons variables for Home Screen Activity
        final Button homeStartQuiz_button = (Button) findViewById(R.id.home_startQuizButton);
        final ImageView overFlowButton = (ImageView) findViewById(R.id.overflow_button);
        final RadioGroup difficultyLevel_radioGroup = (RadioGroup) findViewById(R.id.home_difficulty_level_radioGroup);
        final TextView nrOfQuestions = (TextView) findViewById(R.id.home_number_of_questions);
        final SeekBar homeSeekBar = (SeekBar) findViewById(R.id.home_seekbar_nr_of_questions);

        // below is set up the listener for the seekBar; the three methods below are included automatically in the syntax
        // if you would place a Log in each of them, you can see that if you touch the seekBar then the second and the third method are called in this order;
        // if the seek bar cursor is moved, then the second method, the first method and the third method are called in this order;
        homeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                nrOfQuestions.setText(String.valueOf(progress + MIN_NR_QUEST));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // below is the listener for the "Start Quiz" button
        homeStartQuiz_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startQuizButton(difficultyLevel_radioGroup, homeSeekBar);
            }
        });

        // below is the listener for the "overflow menu" button
        overFlowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppTools.showOverFlowPopUpMenu(HomeScreen.this, v);
            }
        });
    }

    /***********************************************************************
     * The below method is implemented on click of "Start quiz" button *
     **********************************************************************/
    private void startQuizButton(RadioGroup difficultyLevel_radioGroup, SeekBar homeSeekBar)
    {
        // BELOW: we check if any difficulty radio button is checked; if not, a Toast message is shown on the screen;
        // -1 is the default value for RadioGroup.getCheckedRadioButtonId if no radio group button is checked;
        // setGravity (int gravity, int xOffset, int yOffset; I used a resource integer in setGravity because of differentiation on orientation of the screen
        // (150 on the Y vertical to lift up the Toast)
        switch (difficultyLevel_radioGroup.getCheckedRadioButtonId()) {
            case -1: {
                AppTools.customToast(HomeScreen.this, Gravity.BOTTOM, 0, getResources().getInteger(R.integer.home_toast_vertical_uplift), Toast.LENGTH_SHORT, getResources().getString(R.string.home_toast_message_start_quiz));
                break;
            }
            case (R.id.home_level_1_Button):
            case (R.id.home_level_2_Button): {
                Bundle bundle = new Bundle();   // pass number of questions and difficulty level to the Quiz activity through a bundle
                int level = (difficultyLevel_radioGroup.getCheckedRadioButtonId() == R.id.home_level_1_Button) ? 0 : 1; // used ternary operator for quiz difficulty level selection;
                bundle.putInt(BUNDLE_LEVEL, level);
                bundle.putInt(BUNDLE_QUESTIONS, homeSeekBar.getProgress() + MIN_NR_QUEST);
                Intent quizIntent = new Intent(HomeScreen.this, Quiz.class);
                quizIntent.putExtra(INTENT_TOQUIZ, bundle);
                startActivity(quizIntent);
                break;
            }
        }
    }
}