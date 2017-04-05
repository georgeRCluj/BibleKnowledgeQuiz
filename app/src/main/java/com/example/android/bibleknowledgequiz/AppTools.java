package com.example.android.bibleknowledgequiz;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Random;

/*************************************************************************
 * THIS CLASS CONTAINS USEFUL TOOLS USED REPEATEDLY IN THE OTHER CLASSES *
 ************************************************************************/

public class AppTools {

    /*******************************************************************************************
     * The below method compares two string arrays: it firstly creates a copy of each of them, *
     * then it sorts them alphabetically, then it compares them                                *
     ******************************************************************************************/
    public static boolean compareStringArrays(String[] arr1, String[] arr2) {
        if (arr1.length != arr2.length)
            return false;

        String[] arr1Copy = arr1.clone();
        String[] arr2Copy = arr2.clone();

        Arrays.sort(arr1Copy);
        Arrays.sort(arr2Copy);
        for (int i = 0; i < arr1Copy.length; i++) {
            if (!arr1Copy[i].equals(arr2Copy[i]))
                return false;
        }
        return true;
    }

    /***********************************************************************************************************
     * The below method implements the Fisher–Yates shuffle, which randomizes an array of primitive int values *
     **********************************************************************************************************/
    public static void shuffleArray(int[] ar) {
        Random rnd = new Random();
        for (int i = ar.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);         // "rnd.nextInt(x)" returns a number between 0 and x
            // Simple swap
            int a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }

    /********************************************************************************************************************
     * The below method implements a custom Toast method which uses custom_toast.xml from the layout resource directory *
     *******************************************************************************************************************/
    public static void customToast(Activity activity, int gravity, int dX, int dY, int duration, String textInput) {
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(activity.LAYOUT_INFLATER_SERVICE);                             // when used in an activity, replace with "= getLayoutInflater();"
        View layout = inflater.inflate(R.layout.custom_toast, (ViewGroup) activity.findViewById(R.id.custom_toast_container));              // when used in an activity, remove "activity.";
        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setTextSize(activity.getResources().getInteger(R.integer.toast_text_size));
        text.setText(textInput);
        Toast toast = new Toast(activity);                                                                                                  // when used in an activity, replace "activity" with context
        toast.setGravity(gravity, dX, dY);
        toast.setDuration(duration);
        toast.setView(layout);
        toast.show();
    }

    /*******************************************************************************************************************
     * The below method implements the popup menu on when clicking on the overFlow button on the left of the upper bar *
     ******************************************************************************************************************/
    public static void showOverFlowPopUpMenu(final Activity activity, View view) {

        // Inflate the popup_layout.xml
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(activity.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.custom_home_popup_menu, (LinearLayout) activity.findViewById(R.id.homePopUpMenu));

        // Creating the PopupWindow;
        final PopupWindow popup = new PopupWindow(activity);
        popup.setContentView(layout);
        popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setFocusable(true);

        // Clear the default background and show the popup menu
        popup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popup.showAsDropDown(view);

        TextView popUpMenuNewQuiz = (TextView) layout.findViewById(R.id.popUpMenu_new_quiz);            // !!on trying to create the third onClickListener in the PopupWindow, I got an error; very probable PopUpWindow does not support more than 2 buttons
        popUpMenuNewQuiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activity.getClass().getSimpleName().equals("Quiz"))                                 // when the button is clicked from the Quiz activity, the user receives a dialog box on the screen
                    AppTools.showDialogBoxNewQuiz(activity);
                else AppTools.restartQuiz(activity);
                popup.dismiss();
            }
        });

        TextView popUpMenuExit = (TextView) layout.findViewById(R.id.popUpMenu_exit);
        popUpMenuExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.finishAffinity(activity);                                                // "finishAffinity" closes all the sibling activities, related to the activity passed as parameter
            }
        });
    }

    /****************************************************************************************************************
     * The below method shows the dialogBox "Are you sure you want to start a new quiz?" when clicking on new Quiz  *
     * from the overFlow button from the Quiz activity                                                              *
     ***************************************************************************************************************/
    public static void showDialogBoxNewQuiz(final Activity activity) {
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(activity.LAYOUT_INFLATER_SERVICE);                                      // when used in an activity, replace with "= getLayoutInflater();"
        View layout = inflater.inflate(R.layout.custom_dialog_box_new_quiz, (ViewGroup) activity.findViewById(R.id.custom_dialog_box));              // when used in an activity, remove "activity.";
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);                                                                                        // requestFeature has to be added before setting the content; otherwise will throw a runtime error
        dialog.setContentView(layout);
        dialog.setCancelable(true);                                                                                                                  // setCancelable cancels the option of the user to press "back" button
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Button yesButton = (Button) dialog.findViewById(R.id.yes_bttn);
        Button noButton = (Button) dialog.findViewById(R.id.no_bttn);

        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restartQuiz(activity);
                dialog.dismiss();
            }
        });
        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    /**************************************
     * The below method restarts the quiz *
     *************************************/
    public static void restartQuiz(Activity activity) {
        ActivityCompat.finishAffinity(activity);
        Intent homeScreenIntent = new Intent(activity, HomeScreen.class);      // restart activity through Intent
        activity.startActivity(homeScreenIntent);
    }
}
