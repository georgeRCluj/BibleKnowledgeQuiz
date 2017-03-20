package com.example.android.bibleknowledgequiz;

import android.content.Intent;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import static android.R.id.edit;
import static android.widget.Toast.makeText;
import static com.example.android.bibleknowledgequiz.HomeScreen.BUNDLE_LEVEL;
import static com.example.android.bibleknowledgequiz.HomeScreen.BUNDLE_QUESTIONS;
import static com.example.android.bibleknowledgequiz.HomeScreen.INTENT_TOQUIZ;

public class Quiz extends AppCompatActivity {
    public Question[] qBeg = new Question[10];                          // qBeg - array of beginner questions
    public Question[] qAdv = new Question[10];                          // qBeg - array of advanced questions

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        // below we retrieve information saved in Bundle from the Home Activity
        Intent intent = getIntent();                            // getIntent(), when called in an Activity, gives you a reference to the Intent which was used to launch this Activity.
        Bundle bundle = intent.getBundleExtra(INTENT_TOQUIZ);   // take care - getExtras() not working for retrieving a Bundle, it returns null;
        int nrOfQuestions = bundle.getInt(BUNDLE_QUESTIONS);
        int difficultyLevel = bundle.getInt(BUNDLE_LEVEL);

        // below we initialize radio buttons, radio group, checkboxes and edit text from ACTIVITY_QUIZ.XML; we do it here in order not to use expensive "findViewById" too often;
        RadioGroup radioGroupId = (RadioGroup) findViewById(R.id.radio_group);
        final RadioButton[] radioButtonId = {(RadioButton) findViewById(R.id.answers_radio_1), (RadioButton) findViewById(R.id.answers_radio_2),
                (RadioButton) findViewById(R.id.answers_radio_3), (RadioButton) findViewById(R.id.answers_radio_4)};
        final Button nextButton = (Button) findViewById(R.id.next_button);
        final CheckBox[] checkBoxId = {(CheckBox) findViewById(R.id.answers_checkbox_1), (CheckBox) findViewById(R.id.answers_checkbox_2),
                (CheckBox) findViewById(R.id.answers_checkbox_3), (CheckBox) findViewById(R.id.answers_checkbox_4)};
        final EditText editTextId = (EditText) findViewById(R.id.possible_answers_editText);
        int[] showOrder = {0, 1, 2, 3};

        /** TEST OF VIEW ON THE SCREEN **/
        initializeQuestions(difficultyLevel);
        final int testCount = 1;
        TextView cnt_text = (TextView) findViewById(R.id.current_question);
        cnt_text.setText(qBeg[testCount].question);

        ImageView cnt_pict = (ImageView) findViewById(R.id.current_picture);
        cnt_pict.setImageResource(qBeg[testCount].imageResId);
        switch (qBeg[testCount].answerType) {
            case "C": {
                AppTools.shuffleArray(showOrder);        // method used for shuffling an array; the purpose is to shuffle possible answers on each question of the quiz;
                for (int i = 0; i < 4; i++) {
                    checkBoxId[i].setVisibility(View.VISIBLE);
                    checkBoxId[i].setText(qBeg[testCount].possibleAnswers[showOrder[i]]);
                }
                nextButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String[] answerListCheckbox = {"", "", "", ""};
                        for (int i = 0; i < 4; i++)
                            if (checkBoxId[i].isChecked())
                                answerListCheckbox[i] = (String) checkBoxId[i].getText();
                        if (AppTools.compareStringArrays(answerListCheckbox, getResources().getStringArray(R.array.q_b_1_cr)))  // compareStringArrays - method used for comparing string arrays
                            makeText(Quiz.this, "Right answer", Toast.LENGTH_LONG).show();
                        else
                            makeText(Quiz.this, "Wrong answer", Toast.LENGTH_LONG).show();
                    }
                });
                break;
            }
            case "R": {
                radioGroupId.setVisibility(View.VISIBLE);
                AppTools.shuffleArray(showOrder);        // method used for shuffling an array; the purpose is to shuffle possible answers on each question of the quiz;
                for (int i = 0; i < 4; i++)
                    radioButtonId[i].setText(qBeg[testCount].possibleAnswers[showOrder[i]]);
                nextButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for (int i = 0; i < 4; i++)
                            if (radioButtonId[i].isChecked()) {
                                if (radioButtonId[i].getText().equals(qBeg[testCount].correctAnswerRadio))  //Strings' values' are compared with equals.(), not with ==, because they are objects, not primitive data.
                                    makeText(Quiz.this, "Right answer", Toast.LENGTH_LONG).show();
                                else
                                    makeText(Quiz.this, "Wrong answer", Toast.LENGTH_LONG).show();
                                break;      // exit for loop after the radio button is checked
                            }
                    }
                });
                break;
            }
            case "E": {
                editTextId.setVisibility(View.VISIBLE);
                nextButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean answeredCorrectly = false;
                        for (int i = 0; i < qBeg[testCount].correctAnswerEdit.length; i++)
                            if (editTextId.getText().toString().equalsIgnoreCase(qBeg[testCount].correctAnswerEdit[i])) {
                                answeredCorrectly = true;
                                break;
                            }
                        if (answeredCorrectly)
                            Toast.makeText(Quiz.this, "Right answer", Toast.LENGTH_LONG).show();
                        else
                            makeText(Quiz.this, "Wrong answer", Toast.LENGTH_LONG).show();
                    }
                });
                break;
            }
        }
    }

    private void initializeQuestions(int difficultyLevel) {
        if (difficultyLevel == 1) {
            qBeg[0] = new Question(1, 1, R.drawable.b1_david_goliath, getResources().getString(R.string.q_b_1_txt), "C",
                    getResources().getStringArray(R.array.q_b_1_ans), getResources().getStringArray(R.array.q_b_1_cr));
            qBeg[1] = new Question(1, 2, R.drawable.b2_red_sea, getResources().getString(R.string.q_b_2_txt), "E",
                    getResources().getStringArray(R.array.q_b_2_cr));
            qBeg[2] = new Question(1, 3, R.drawable.b3_jesus_multiplication_loaves, getResources().getString(R.string.q_b_3_txt), "R",
                    getResources().getStringArray(R.array.q_b_3_ans), getResources().getString(R.string.q_b_3_cr));
        } else {
        }
    }
}
