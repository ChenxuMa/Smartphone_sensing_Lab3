package com.sps.lab3_renew;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public class core extends AppCompatActivity implements SensorEventListener {
    private TextView step_count;
    private TextView direction_text;
    private TextView predict_res;
    private int particleDirection = 0;
    private int stepCount = 0;
    private int distancePerStep=0;    // set to 0 if calibration problem is fixed!
    private Button close;
    private Button start;
    private Button stop;

    //private Canvas canvas;
    private ShapeDrawable drawable;
    private SensorManager sensorManager;
    private Sensor rotationSensor;
    private Sensor accel;
    private HashMap<String, Cell> cellHash = new HashMap<>();
    //private HashMap<String, Cell> cellHash = new HashMap<>();
    private List<Particle> particles = new ArrayList<>();
    //private Canvas canvas;
    private StepDetector sd = new StepDetector();
    private List<ShapeDrawable> walls = new ArrayList<>();
    private float maxWeight = 0;
    private static long count;
    private static long start_time;
    private static long end_time;
    private boolean isconvergence = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_core);
        Intent intent=getIntent();
        Bundle bundle=intent.getExtras();
        distancePerStep=bundle.getInt("distanceperstep_result");
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        start = (Button) findViewById(R.id.button);
        stop = (Button) findViewById(R.id.button3);
        close = (Button) findViewById(R.id.button4);
        //Calibrate_step = (Button) findViewById(R.id.button5);
        direction_text = (TextView) findViewById(R.id.textView2);
        step_count = (TextView) findViewById(R.id.textView3);
        predict_res = (TextView) findViewById(R.id.textView4);
        String step_text = "Total steps:";
        String direction_output = "Direction:";
        String predict_string = "Location: ";
        direction_text.setText(direction_output);
        step_count.setText(step_text);
        predict_res.setText(predict_string);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String predict_string = "Location: searching";
                predict_res.setText(predict_string);
                sensorManager.registerListener(core.this, accel, SensorManager.SENSOR_DELAY_GAME);
                sensorManager.registerListener(core.this, rotationSensor, SensorManager.SENSOR_DELAY_GAME);
                construct_fragment(walls, drawable);
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                particles.clear();

                stepCount = 0;
                //particles.clear();
                String step_text = "Total steps:";
                String direction_output = "Direction:";
                String predict_string = "Location:";
                direction_text.setText(direction_output);
                step_count.setText(step_text);
                predict_res.setText(predict_string);
                sensorManager.unregisterListener(core.this, accel);
                sensorManager.unregisterListener(core.this, rotationSensor);
                //Toast.makeText(core.this, Float.toString(freq), Toast.LENGTH_SHORT).show();
                //canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sensorManager.unregisterListener(core.this, accel);
                sensorManager.unregisterListener(core.this, rotationSensor);
                finish();
            }
        });
    }

    private void construct_fragment(List<ShapeDrawable> walls, ShapeDrawable drawable) {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        ImageView canvasView = (ImageView) findViewById(R.id.canvas);
        Bitmap blankBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(blankBitmap);
        canvasView.setImageBitmap(blankBitmap);
        try {
            constructCells(canvas);
            generateParticles(canvas);
            System.out.println("adjacent_relation");
            adjacent_relation();
        } catch (Exception e) {
            Toast.makeText(this, "Cell data unavailable", Toast.LENGTH_LONG);
        }
    }

    private void adjacent_relation() throws JSONException {
        InputStream is = this.getResources().openRawResource(R.raw.cell_data);
        Scanner s = new Scanner(is).useDelimiter("\\A");
        String storedString = s.hasNext() ? s.next() : "";

        JSONObject js = new JSONObject(storedString);
        for (Iterator<String> it = js.keys(); it.hasNext(); ) {
            String cellID = it.next();

            JSONObject cell_data = js.getJSONObject(cellID);
            String top_door=(String)cell_data.get("top");
            String bottom_door=(String)cell_data.get("bottom");
            String left_door=(String)cell_data.get("left");
            String right_door=(String)cell_data.get("right");
            if (top_door.equals("none")) {
                cellHash.get(cellID).topCell=null;

            }else{
                cellHash.get(cellID).topCell=top_door;
            }
            if (right_door.equals("none")) {
                cellHash.get(cellID).rightCell=null;

            }else{
                cellHash.get(cellID).rightCell=right_door;
            }
            if (left_door.equals("none")) {
                cellHash.get(cellID).leftCell=null;

            }else{
                cellHash.get(cellID).leftCell=left_door;
            }
            if (bottom_door.equals("none")) {
                cellHash.get(cellID).bottomCell=null;

            }else{
                cellHash.get(cellID).bottomCell=bottom_door;
            }



        }
    }

    private void generateParticles(Canvas canvas) {
        int particle_number_per_cell = 200;
        for (Map.Entry<String, Cell> entry : cellHash.entrySet()) {

            generateParticlesin_onecell(entry.getKey(), entry.getValue(), particle_number_per_cell, canvas);
        }
    }

    private void generateParticlesin_onecell(String cellID, Cell cell_feature, int particle_number_per_cell, Canvas canvas) {
        int horizon_left_bound = cell_feature.x;
        int horizon_right_bound = horizon_left_bound + cell_feature.length;
        int column_bottom_bound = cell_feature.y;
        int column_top_bound = column_bottom_bound - cell_feature.height;

        int random_x;
        int random_y;
        for (int i = 0; i < particle_number_per_cell; i++) {
            System.out.println(particle_number_per_cell);
            System.out.println(horizon_left_bound);
            System.out.println(horizon_right_bound);
            random_x = getRandomIntInRange(horizon_left_bound, horizon_right_bound);
            random_y = getRandomIntInRange(column_top_bound, column_bottom_bound);

            Particle a_particle = new Particle(random_x, random_y, cell_feature);
            a_particle.drawparticle(canvas);
            particles.add(a_particle);
        }
    }

    private int getRandomIntInRange(int min, int max) {
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    private void constructCells(Canvas canvas) {
        for (int i = 1; i <= 10; i++) {
            if (i == 1) {
                Cell cell_1 = new Cell(50, 2150, 562, 330, "none", "none",
                        "none", "none", 412, 1820, 150, "top");
                cell_1.drawCell(canvas);
                cell_1.cellID = Integer.toString(i);
                cellHash.put(Integer.toString(i), cell_1);
                continue;
            } else if (i == 2) {

                Cell cell_2 = new Cell(50, 1820, 562, 330, "3", "none",
                        "none", "none", 612, 1655, 165, "right");


                cell_2.door2x = 412;
                cell_2.door2y = 1820;
                cell_2.door2length = 150;
                cell_2.door2_location = "bottom";
                cell_2.cellID = Integer.toString(i);
                cell_2.drawCell(canvas);
                cellHash.put(Integer.toString(i), cell_2);


                continue;

            } else if (i == 3) {
                Cell cell_3 = new Cell(50, 1490, 562, 330, "none", "2",
                        "none", "none", 0, 0, 0, "none");
                cell_3.drawCell(canvas);
                cell_3.cellID = Integer.toString(i);
                cellHash.put(Integer.toString(i), cell_3);
                continue;
            } else if (i == 4) {
                Cell cell_4 = new Cell(50, 1160, 337, 310, "none", "none",
                        "none", "8", 0, 0, 0, "none");
                cell_4.drawCell(canvas);
                cell_4.cellID = Integer.toString(i);
                cellHash.put(Integer.toString(i), cell_4);
                continue;
            } else if (i == 5) {
                Cell cell_5 = new Cell(612, 2150, 400, 495, "6", "none",
                        "none", "none", 0, 0, 0, "none");
                cell_5.drawCell(canvas);
                cell_5.cellID = Integer.toString(i);
                cellHash.put(Integer.toString(i), cell_5);
                continue;
            } else if (i == 6) {
                Cell cell_6 = new Cell(612, 1655, 400, 495, "8", "5",
                        "none", "none", 612, 1655, 165, "left");
                cell_6.drawCell(canvas);
                cell_6.cellID = Integer.toString(i);
                cellHash.put(Integer.toString(i), cell_6);
                continue;
            } else if (i == 7) {
                Cell cell_7 = new Cell(50, 850, 337, 620, "none", "none",
                        "none", "none", 387, 750, 100, "right");
                cell_7.drawCell(canvas);
                cell_7.cellID = Integer.toString(i);
                cellHash.put(Integer.toString(i), cell_7);
                continue;
            } else if (i == 8) {
                Cell cell_8 = new Cell(387, 1160, 625, 310, "10", "6",
                        "4", "none", 412, 2150, 200, "none");
                cell_8.drawCell(canvas);
                cell_8.cellID = Integer.toString(i);
                cellHash.put(Integer.toString(i), cell_8);
                continue;
            } else if (i == 9) {
                Cell cell_9 = new Cell(50, 230, 962, 200, "none", "none",
                        "7", "10", 387, 850, 225, "bottom");
                cell_9.drawCell(canvas);
                cell_9.cellID = Integer.toString(i);
                cellHash.put(Integer.toString(i), cell_9);
                continue;
            } else if (i == 10) {
                Cell cell_10 = new Cell(387, 850, 225, 620, "9", "8",
                        "none", "none", 387, 750, 100, "left");
                cell_10.drawCell(canvas);
                cell_10.cellID = Integer.toString(i);
                cellHash.put(Integer.toString(i), cell_10);
                continue;
            }

        }
        //System.out.println(cellHash);
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


            String direction_output = "Direction:" + Integer.toString(particleDirection);
            direction_text.setText(direction_output);
        } else if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            boolean stepDetected = sd.isStepDetected(event.values.clone());
            System.out.println(stepDetected);
            if ((stepDetected)) {
                int pixelsToMove = 1;
                stepCount++;

                //todo 协商像素移动值
                //每动一步，更新一次

                switch (particleDirection) {
                    case 0:
                        pixelsToMove = (int) (distancePerStep * 562 / (3.13 * 100));
                        break;
                    case 180:
                        //pixelsToMove = (int) ((2200f / (10*4*100)) * distancePerStep);
                        pixelsToMove = (int) (distancePerStep * 562 / (3.13 * 100));
                        break;
                    case 90:
                        pixelsToMove = (int) (distancePerStep * 330 / (1.68 * 100));
                        break;
                    case 270:
                        //pixelsToMove = (int) ((1080f / (14.3*100)) * distancePerStep);
                        pixelsToMove = (int) (distancePerStep * 330 / (1.68 * 100));
                        break;
                }

                try {
                    moveAllParticles(pixelsToMove, getCanvas());
                } catch (Exception e) {
                    return;
                }



            }


            String step_text = "Total steps:" + Integer.toString(stepCount);
            step_count.setText(step_text);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        super.onResume();
    }

    private float getCompensatedDirection(float current) {
        current = current < 0 ? 360 + current : current;
        float res = current;
        return res;
    }


    private Canvas getCanvas() {
        System.out.println("getCanvas");
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        ImageView canvasView = (ImageView) findViewById(R.id.canvas);
        Bitmap blankBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas temp = new Canvas(blankBitmap);
        canvasView.setImageBitmap(blankBitmap);
        System.out.println("complete");
        return temp;
    }


    private void moveAllParticles(int movePixelCount, Canvas canvas) throws JSONException {
        int deletedParticleCount = 0;
        int temp = 0;
        String predict_string;

        List<Particle> nextGenParticles = new ArrayList<>();
        //通过list来存储particle,particle是一种对象
        // Try moving all particles
        // Update weight if the particle exists; else delete the particle from nextGenParticles

        System.out.println("-----------move all particles---------------");
        int iterator=0;
        System.out.println(particles.size());
        for (Particle p : particles) {
            //System.out.printf("iterator %d\n", iterator);
            //System.out.println(iterator);
            iterator++;
            temp = (int) getRandomFromGaussian((float) movePixelCount, 4.0f);
            p.move(temp, particleDirection);//按一定规则移动
            //System.out.println(p.x);
            //System.out.println("move complete");
            p.validateCell(cellHash);//判断是否撞墙
            //System.out.println(p.isAlive);
            //System.out.println("validate complete");

            if (p.isAlive) {
                //System.out.println("-----------nextGenparticles");
                p.weight += 1.0;
                nextGenParticles.add(cloneParticle(p));
            } else {

                deletedParticleCount += 1;//统计被删除的particle数
            }
            //System.out.println(p.cell.cellID.equals("6"));
        }
        System.out.println(nextGenParticles.size());
        System.out.println(deletedParticleCount);
        System.out.println("iteration complete");
        List<Particle> resampledParticles = resampleParticles(nextGenParticles, deletedParticleCount);
        System.out.println(resampledParticles.size());
        particles = new ArrayList<Particle>();
        for (Particle r : resampledParticles)
            particles.add(cloneParticle(r));

        //System.out.println(particles.size());
        //System.out.println("complete");
        constructCells(canvas);
        adjacent_relation();
        /*
        for (Cell c : cellHash.values())
            c.drawCell(canvas);

         */


        for (int i = 0; i < particles.size(); i++) {
            Particle p = (Particle) particles.get(i);
            //System.out.println("--------------draw particles---------------");
            p.p.draw(canvas);
        }
        //todo 以int形式返回收敛结果，即cell_num
        int max_particle_cell_num = isConvergence(particles);
        if(max_particle_cell_num==0){
            predict_string="Location:searching";
        }else{
            predict_string="Location:"+Integer.toString(max_particle_cell_num);
        }

        predict_res.setText(predict_string);
    }

    private int isConvergence(List<Particle> LiveParticles) {
        int count_1 = 0;
        int count_2 = 0;
        int count_3 = 0;
        int count_4 = 0;
        int count_5 = 0;
        int count_6 = 0;
        int count_7 = 0;
        int count_8 = 0;
        int count_9 = 0;
        int count_10 = 0;

        int max_particle_cell_num= 0;
        //Particle max_particle = new Particle();
        for (Particle p : LiveParticles)
        {
            if (p.cell.cellID.equals("1"))
                count_1++;
            if (p.cell.cellID.equals("2"))
                count_2++;
            if (p.cell.cellID.equals("3"))
                count_3++;
            if (p.cell.cellID.equals("4"))
                count_4++;
            if (p.cell.cellID.equals("5"))
                count_5++;
            if (p.cell.cellID.equals("6"))
                count_6++;
            if (p.cell.cellID.equals("7"))
                count_7++;
            if (p.cell.cellID.equals("8"))
                count_8++;
            if (p.cell.cellID.equals("9"))
                count_9++;
            if (p.cell.cellID.equals("10"))
                count_10++;
        }

        int[] number = {count_1,count_2,count_3,count_4,count_5,count_6,count_7,count_8,count_9,count_10};
        int total_count = count_1+count_2+count_3+count_4+count_5+count_6+count_7+count_8+count_9+count_10;
        int max_count = 0;
        for (int value : number) {
            if (value > max_count)
                max_count = value;
        }


        if (((double)max_count/total_count)>0.8)
        {
            {
                this.isconvergence = true;
                for (int i=0;i<number.length;i++)
                {
                    if((number[i]) == max_count)
                        max_particle_cell_num = i+1;
                }
            }


        }
        return max_particle_cell_num;
    }

    private Particle cloneParticle(Particle inp) {
        int x = inp.x;
        int y = inp.y;
        Cell c = inp.cell;
        float w = inp.weight;
        return new Particle(x, y, c, w);
    }
    public float getRandomFromGaussian(float mean, float sd) {
        Random r = new Random();
        return (float) ((r.nextGaussian() * sd) + mean);
    }
    private List<Particle> resampleParticles(List<Particle> nextGenParticles, int deleteCount) {
        List<Particle> res = new ArrayList<>();
        int generatedCount = 0;


        List<Float> skd = new ArrayList<>();
        //生成一个list，统计所有particle的weight


        if (deleteCount == 0)
            return nextGenParticles;
        else if (nextGenParticles.size() == 0) {
            for (Particle p : particles)
                res.add(new Particle(p.oldX, p.oldY, p.cell, p.weight));
            return res;

        }
        float totalWeight = 0;
        float maxweight = 0;
        Particle maxWeightParticle = null;

//resampling过程开始,首先统计移动后还有多少活着的particle
        for (Particle p : nextGenParticles)
            totalWeight += p.weight;

// Normalising weights
        for (Particle p : nextGenParticles) {
            p.weight /= totalWeight;
            res.add (cloneParticle(p));
            skd.add(p.weight);
            if (p.weight > maxweight) {
                maxWeightParticle = p;
                maxweight = p.weight;
            }
        }
        System.out.printf("maximum weight is %f\n", maxweight);
        System.out.printf("this maximum weight particle is in the cell %s\n", maxWeightParticle.cell.cellID);

        int iterator=0;
        //首先在任一particle附近，随机生成一定数量的新particle
        //之后,如果生成的particle比删除的少，就在权重最大的particle附件继续生成deleteCount - generatedCount个particle

        for (Particle p : nextGenParticles) {
            iterator++;
            //System.out.println("start iteration");
            // Add the alive particles
            //System.out.printf("Generated count=%d\n", generatedCount);

            int currentGenerateCount = (int) Math.floor((double) p.weight * deleteCount);
            if ((generatedCount + currentGenerateCount) > deleteCount)
                break;
            generatedCount += currentGenerateCount;

//            Particle a_particle=(Particle)nextGenParticles.get(iterator);
//            System.out.printf("x of the particle=%d\n", a_particle.x);
//            System.out.printf("y of the particle=%d\n", a_particle.y);
//            System.out.printf("the particle is the cell%s\n", a_particle.cell.cellID);
            List<Particle> temp = getNNewParticlesAroundP(p, currentGenerateCount);
            //System.out.printf("Current generated count=%d\n", currentGenerateCount);

            for (Particle t : temp)
                res.add(cloneParticle(t));

        }
        //System.out.println("add new particle");
        if (generatedCount < deleteCount) {
            List<Particle> temp = getNNewParticlesAroundP(maxWeightParticle, (deleteCount - generatedCount));
            for (Particle t : temp)
                res.add(cloneParticle(t));
            //生成新的particle
            //res是一个particle的集合

        }
        //this.maxWeight= maxweight;
        return res;



    }

    private List<Particle> getNNewParticlesAroundP(Particle p, int count) {
        int x = 1;
        int y = 1;
        //System.out.println(p.x);
        //System.out.println(p.y);
        List <Particle> res = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            //System.out.println(isCoordinateInCell(x, 0, p.cell));
            //System.out.println(isCoordinateInCell(0, y, p.cell));
            /*
            while (!isCoordinateInCell(x, y, p.cell))
                //System.out.println("------------");
                x = (int) getRandomFromGaussian((float)p.x, 1f);
                y = (int) getRandomFromGaussian((float)p.y, 0.5f);

             */
            while (!isCoordinateInCell(x, 0, p.cell))
                //System.out.println("------------");
                x = (int) getRandomFromGaussian((float)p.x, 2);

            while (!isCoordinateInCell(0, y, p.cell))
                //System.out.println("************");
                y = (int) getRandomFromGaussian((float)p.y, 1);


            Particle newParticle = new Particle(x, y, p.cell, p.weight);
            res.add(newParticle);
        }
        return res;
    }

    private boolean isCoordinateInCell(int x, int y, Cell c) {
        int x1 = c.x;
        int x2 = x1 + c.length;
        int y1 = c.y;
        int y2 = y1 - c.height;

        if (x == 0) {
            // Check only y
            if ((y > y2) && (y < y1))
                return true;
            else
                return false;
        }
        else if (y == 0) {
            // Check x
            if ((x > x1) && (x < x2))
                return true;
            else
                return false;
        }
        else {
            // Check both
            if (isCoordinateInCell (x, 0, c) && isCoordinateInCell(0, y, c)){
                //System.out.println("end get new particles");
                return true;
            }

            else
                return false;
        }
    }

}