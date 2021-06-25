package com.sps.lab3_renew;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

public class calibration extends AppCompatActivity implements SensorEventListener {
    private TextView step;
    private TextView calibration;
    private TextView direction_text;
    private Button start;
    private Button close;
    private SensorManager sensorManager;
    private Sensor rotationSensor;
    private Sensor accel;
    private Button Calibrate_step;
    private StepDetector sd = new StepDetector();
    private int stepCount = 0;
    private int particleDirection = 0;
    private int distancePerStep = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        start = (Button) findViewById(R.id.button6);
        close = (Button) findViewById(R.id.button7);
        Calibrate_step = (Button) findViewById(R.id.button5);
        step=(TextView) findViewById(R.id.textView5);
        calibration=(TextView) findViewById(R.id.textView6);
        direction_text=(TextView)findViewById(R.id.textView7);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stepCount=0;
                sensorManager.registerListener(calibration.this, accel, SensorManager.SENSOR_DELAY_GAME);
                sensorManager.registerListener(calibration.this, rotationSensor, SensorManager.SENSOR_DELAY_GAME);
            }
        });
        Calibrate_step.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stepDistanceCalibration(calibration.this.stepCount);
            }
        });
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent =new Intent(calibration.this, MainActivity.class);
                Bundle bundle=new Bundle();
                bundle.putInt("distanceperstep", calibration.this.distancePerStep);
                intent.putExtras(bundle);
                startActivity(intent);
                sensorManager.unregisterListener(calibration.this, accel);
                sensorManager.unregisterListener(calibration.this, rotationSensor);
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            float[] rotationMatrix = new float[16];
            float[] orientationVals = new float[3];
            float directionConsiderationWindow = 30;

            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
            SensorManager.getOrientation(rotationMatrix, orientationVals);
            float direction = (float) Math.toDegrees(orientationVals[0]);
            direction = getCompensatedDirection(direction);
            int offset = 15;

            //calibrationDirectionList.add(direction);
            //todo 左北右南上东下西
            // If East (+- directionConsiderationWindow around 0), return 0
            if ((direction + offset > (360 - directionConsiderationWindow)) || (direction + offset < directionConsiderationWindow))
                direction = 0;

                // If South (+- directionConsiderationWindow around 90), return 90
            else if ((direction + offset > (90 - directionConsiderationWindow)) && (direction + offset < (90 + directionConsiderationWindow)))
                direction = 90;

                // If West (+- directionConsiderationWindow around 180), return 180
            else if ((direction + offset > (180 - directionConsiderationWindow)) && (direction + offset < (180 + directionConsiderationWindow)))
                direction = 180;

                // If North (+- directionConsiderationWindow around 270), return 270
            else if ((direction + offset > (270 - directionConsiderationWindow)) && (direction + offset < (270 + directionConsiderationWindow)))
                direction = 270;
            else
                direction = 9999;
            if ((direction != particleDirection) && (direction != 9999)) {
                particleDirection = (int) direction;

            }
            //String direction_output = "Your direction:" + Integer.toString(particleDirection);
            direction_text.setText(Integer.toString(particleDirection));
        } else if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            boolean stepDetected = sd.isStepDetected(event.values.clone());

            if ((stepDetected)) {
                int pixelsToMove = 1;
                stepCount++;

                //todo 协商像素移动值
                //每动一步，更新一次
                String step_text = "Total steps:" + Integer.toString(stepCount);
                step.setText(step_text);

            }





        }
    }
    private void stepDistanceCalibration(int stepCount)
    {
        float new_distancePerStep=0;
        switch (particleDirection) {
            case 0:
                new_distancePerStep = (int) ((3.13 * 100)/stepCount);
                break;
            case 180:
                //pixelsToMove = (int) ((2200f / (10*4*100)) * distancePerStep);
                new_distancePerStep = (int) ((3.13 * 100)/stepCount);
                break;
            case 90:
                new_distancePerStep = (int) ((1.68 * 100)/stepCount);
                break;
            case 270:
                //pixelsToMove = (int) ((1080f / (14.3*100)) * distancePerStep);
                new_distancePerStep = (int) ((1.68 * 100)/stepCount);
                break;
        }
        this.distancePerStep = (int) new_distancePerStep;
        String result="calibration result:"+Integer.toString(this.distancePerStep);
        calibration.setText(result);
    }
    @Override
    public void onAccuracyChanged (Sensor sensor,int i){
        super.onResume();
    }
    private float getCompensatedDirection ( float current){
        current = current < 0 ? 360 + current : current;
        float res = current;
        return res;
    }
}