package com.sps.lab3_renew;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private Button start;
    private Button calibration;
    private Button measure_data;
    private int distancePerStep=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        start=(Button) findViewById(R.id.start_button);
        calibration=(Button)findViewById(R.id.button2);
        Intent intent=getIntent();
        Bundle bundle=intent.getExtras();
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent2=new Intent(MainActivity.this, core.class);
                if(bundle!=null){
                    distancePerStep=bundle.getInt("distanceperstep");
                    bundle.putInt("distanceperstep_result", distancePerStep);
                    intent2.putExtras(bundle);
                    startActivity(intent2);
                }else{
                    Toast.makeText(MainActivity.this,"Please first calibrate your step", Toast.LENGTH_SHORT).show();
                }


            }
        });
        calibration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent3=new Intent(MainActivity.this, calibration.class);
                startActivity(intent3);
            }
        });

    }
}