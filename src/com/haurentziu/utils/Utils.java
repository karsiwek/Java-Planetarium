package com.haurentziu.utils;

import java.util.ArrayList;

/**
 * Created by haurentziu on 28.04.2016.
 */
public final class Utils {
    private Utils(){

    }

    public static String rad2String(double d, boolean normalise, boolean inHours){
        double deg = Math.toDegrees(d);
        if(normalise){
            while(deg > 360)
                deg -= 360;
            while(deg < 0)
                deg +=360;
        }
        if(inHours)
            deg /= 15.0;
        int degrees = (int)deg;
        int minutes = (int)((deg - degrees)*60);
        int seconds = (int)((deg - degrees - minutes/60.0)*3600);

        String degString = Integer.toString(degrees);
        String minString = String.format("%02d", Math.abs(minutes));
        String secString = String.format("%02d", Math.abs(seconds));

        String s;


        if(inHours)
            s = String.format("%sh%sm%ss", degString, minString, secString);
        else
            s = String.format("%s\u00b0%s\u2032%s\u2033", degString, minString, secString);

        return s;
    }

    public static float[] floatArrayList2FloatArray(ArrayList<Float> array){
        int size = array.size();
        float[] newArray = new float[size];
        for(int i = 0; i < size; i++){
            newArray[i] = array.get(i);
        }
        return newArray;

    }


}
