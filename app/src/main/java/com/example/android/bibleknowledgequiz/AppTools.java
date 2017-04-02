package com.example.android.bibleknowledgequiz;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Random;

public class AppTools {

    /**
     * The below method compares two string arrays: it firstly creates a copy of each of them, then it sorts them alphabetically, then it compares them
     **/
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

    /**
     * The below method implements the Fisher–Yates shuffle, which randomizes an array of primitive int values
     **/
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

    /**
     * The below method implements a custom Toast method which uses custom_toast.xml from the layout resource directory
     **/
    public static void customToast(Activity activity, int gravity, int dX, int dY, int duration, String textInput) {
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(activity.LAYOUT_INFLATER_SERVICE);                             // when used in an activity, replace with "= getLayoutInflater();"
        View layout = inflater.inflate(R.layout.custom_toast, (ViewGroup) activity.findViewById(R.id.custom_toast_container));              // when used in an activity, remove "activity.";
        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setTextSize(activity.getResources().getInteger(R.integer.toast_text_size));                                        // when used in an activity, remove "activity."
        text.setText(textInput);
        Toast toast = new Toast(activity);                                                                                                  // when used in an activity, replace "activity" with context
        toast.setGravity(gravity, dX, dY);
        toast.setDuration(duration);
        toast.setView(layout);
        toast.show();
    }
}
