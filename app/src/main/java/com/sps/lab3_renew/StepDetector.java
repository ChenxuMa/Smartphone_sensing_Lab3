package com.sps.lab3_renew;

public class StepDetector {
    private float[] oldData;    // Old accel data (0 is x, 1 is y and 2 is z)
    final private float STEP_MIN_ACCEL_RES = 0.5f;
    final private int MIN_STRIDE_DURATION = 40;  // 7 Readings at 20ms each -> min 140 ms
    final private int MAX_STRIDE_DURATION = 70; // Stride can be of max 500ms
    private int highDuration;   // Duration for which accel readings have been higher than thresh

    public StepDetector()
    {
        highDuration = 0;
        oldData = null;
    }

    public boolean isStepDetected (float[] curretACCEL)
    {

        boolean stepDetected = false;

        if (oldData == null) {
            oldData = new float[3];
            for (int i = 0; i < 3; i ++)
                oldData[i] = curretACCEL[i];
        }

        float res = getAccelRes(curretACCEL);

        if (res > STEP_MIN_ACCEL_RES) {

            highDuration ++;
            System.out.println(highDuration);
            //return stepDetected;
        }
        if((highDuration>MIN_STRIDE_DURATION)&&(highDuration<MAX_STRIDE_DURATION)){
            stepDetected=true;
            highDuration=0;
            return stepDetected;
        }else{
            return stepDetected;
        }


        /*
        else{
            if ((highDuration > MIN_STRIDE_DURATION) && (highDuration < MAX_STRIDE_DURATION))
                stepDetected = true;
            highDuration = 0;
            return stepDetected;
        }

         */
    }

    public float getAccelRes (float[] curretACCEL)
    {
        float res = (float) Math.sqrt((Math.pow(curretACCEL[0], 2))
                + (Math.pow(curretACCEL[1], 2))
                + (Math.pow(curretACCEL[2], 2)));
        //System.out.println(res);
        return res;
    }
}
