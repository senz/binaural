package com.github.senz.binaural;

/**
 * Created by mrrussell on 2/13/16.
 */
public class Helpers {

    private static final int AMPLITUDE_MAX = 32767;

    public static int getAdjustedAmplitudeMax(float frequency) {
        //scale amplitude for human perception. 100hz or less = max
        int amplitudeMax;
        float amplitudeScale = 100 / frequency;
        if (frequency <= 100)
            amplitudeMax = AMPLITUDE_MAX;
        else {
            amplitudeMax = (int)(AMPLITUDE_MAX * amplitudeScale);
        }
        return amplitudeMax;
    }

    public static int getLCM(int a,int b)
    {
        int x;
        int y;
        if(a<b)
        {
            x=a;
            y=b;
        }
        else
        {
            x=b;
            y=a;
        }
        int i=1;
        while(true)
        {
            int x1=x*i;
            int y1=y*i;
            for(int j=1;j<=i;j++)
            {
                if(x1==y*j)
                {
                    return x1;
                }
            }
            i++;
        }
    }

    /**
     * Converts remaining seconds to minutes and seconds for display (e.g. notification, status).
     * @param remainingSeconds total remaining seconds (clamped to >= 0)
     * @return int[2] with { minutes, seconds }
     */
    public static int[] getRemainingMinutesSeconds(long remainingSeconds) {
        if (remainingSeconds <= 0) {
            return new int[] { 0, 0 };
        }
        long sec = remainingSeconds % 60;
        long min = remainingSeconds / 60;
        return new int[] { (int) min, (int) sec };
    }

    public static void napThread() {
        try {
            Thread.currentThread();
            Thread.sleep(50);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}
