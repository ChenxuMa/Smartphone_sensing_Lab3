package com.sps.lab3_renew;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.hardware.SensorEventListener;
import android.view.Display;

import org.json.JSONException;
import org.json.JSONObject;

public class Cell {
    public int x;   // Bottom Left X
    public int y;   // Bottom Left Y
    public int length;
    public int height;
    public String topCell,bottomCell, leftCell, rightCell;
    public String cellID;
    public int doorx;
    public int doory;
    public int doorlength;
    public String door_location;
    public int door2x;
    public int door2y;
    public int door2length;
    public String door2_location;
    public Cell (int x, int y, int length, int height, String top, String bottom, String left, String right, int doorx, int doory, int doorlength, String door_location) {
        this.x = x;
        this.y = y;
        this.height = height;
        this.length = length;
        this.doorx=doorx;
        this.doory=doory;
        this.doorlength=doorlength;
        this.door_location=door_location;

        if(bottom.equals("none")){
            this.bottomCell=null;
        }else{
            this.bottomCell = bottom;
        }
        if(top.equals("none")){
            this.topCell=null;
        }else{
            this.topCell = top;
        }
        if(left.equals("none")){
            this.leftCell=null;
        }else{
            this.leftCell = left;
        }
        if(right.equals("none")){
            this.rightCell=null;
        }else{
            this.rightCell = right;
        }

    }
    public void drawCell(Canvas canvas){
        ShapeDrawable left =  new ShapeDrawable(new RectShape());
        ShapeDrawable right =  new ShapeDrawable(new RectShape());
        ShapeDrawable top =  new ShapeDrawable(new RectShape());
        ShapeDrawable bottom =  new ShapeDrawable(new RectShape());
        if(door2_location!=null){
            bottom.setBounds(this.x, y-10, this.door2x, y);
            bottom.draw(canvas);
            left.setBounds(this.x, y - height - 10, x + 10, y);
            left.draw(canvas);

        } else {
            if (this.bottomCell == null) {
                if(door_location.equals("bottom")){
                    if(this.doorx==this.x){
                        bottom.setBounds(this.doorx+this.doorlength, y-10, this.x+length,y);
                        bottom.draw(canvas);
                    }else{
                        bottom.setBounds(this.x,y-10,this.doorx,y);
                        bottom.draw(canvas);
                        bottom.setBounds(this.doorx+this.doorlength,y-10,this.x+length,y);
                        bottom.draw(canvas);

                    }
                }else {
                    bottom.setBounds(this.x, y - 10, x + length, y);
                    bottom.draw(canvas);
                }

        /*
        if(this.doory==this.y){
            bottom.setBounds(this.x,y-10,doorx,y);
            bottom.draw(canvas);
        }else{
            // Draw wall in the bottom
            bottom.setBounds(this.x, y-10, x + length, y);
            bottom.draw(canvas);
        }

         */

            }
            if (this.rightCell == null) {
                if(this.door_location.equals("right")){
                    if(this.doory==this.y){
                        right.setBounds(this.x+length,this.y-height-10,this.x+length+10,this.doory-this.doorlength);
                        right.draw(canvas);
                    }else{
                        right.setBounds(this.x+length,this.doory,this.x+length+10,y);
                        right.draw(canvas);
                        right.setBounds(this.x+length, this.y-height,this.x+length+10,this.doory-this.doorlength);
                        right.draw(canvas);
                    }
                }else {
                    // Draw wall in the right
                    right.setBounds(this.x + length, y - height -10, x + length + 10, y);
                    right.draw(canvas);
                }

        /*
        if(this.doorx==this.x+length){
            // Draw wall in the right
            right.setBounds(this.x + length, y-height-10, x + length + 10, y-height-10-(y-doorlength));
            right.draw(canvas);
        }else{
            // Draw wall in the right
            right.setBounds(this.x + length, y - height -10, x + length + 10, y);
            right.draw(canvas);
        }

         */

            }
            if (this.topCell == null) {
                if(this.door_location.equals("top")){
                    if(this.doorx==this.x){
                        top.setBounds(this.doorx+this.doorlength, y-height-10, this.x+length,y-height);
                        top.draw(canvas);
                    }else{
                        top.setBounds(this.x,y-height-10,this.doorx,y-height);
                        top.draw(canvas);
                        top.setBounds(this.doorx+this.doorlength,y-height-10,this.x+length,y-height);
                        top.draw(canvas);

                    }
                }else {
                    top.setBounds(this.x, y-height-10, x + length+10, y-height);
                    top.draw(canvas);
                }
        /*
        if(this.doory==this.y-height){
            top.setBounds(this.x,y-height-10,doorx,y-height);
            top.draw(canvas);
        }else{
            // Draw wall at the top
            top.setBounds(this.x, y - height - 10, x + length, y - height);
            top.draw(canvas);
        }

         */

            }
            if (this.leftCell == null) {
                if(this.door_location.equals("left")){
                    if(this.doory==this.y){
                        //System.out.println("$$$$$$$$$$$$$$$$");
                        left.setBounds(this.x,this.y-height,this.x+10,this.doory-this.doorlength);
                        left.draw(canvas);
                    }else{
                        //System.out.println("^^^^^^^^^^^^^^^^^");
                        left.setBounds(this.x,this.doory,this.x+10,y);
                        left.draw(canvas);
                        left.setBounds(this.x, this.y-height,this.x+10,this.doory-this.doorlength);
                        left.draw(canvas);
                    }
                }else {
                    // Draw wall in the right
                    left.setBounds(this.x, y - height - 10, x + 10, y);
                    left.draw(canvas);
                }

        /*
        // Draw wall in the left
        if(this.doorx==this.x){
            left.setBounds(this.x - 10, y-height-10, x, y-height-10-(y-doorlength));
            left.draw(canvas);
        }else{
            left.setBounds(this.x-10,y-height-10,x,y);
            left.draw(canvas);
        }

         */
        /*
        if(!this.cellID.equals("8")){
            left.setBounds(this.x - 10, y - height - 10, x, y);
            left.draw(canvas);
        }

         */
        /*
        else{
            left.setBounds(this.x-10, y-310-10,x,y);
            left.draw(canvas);
        }

         */

            }
        }


    }
    /*
    public Cell (String cellID, JSONObject cellData) throws JSONException {
        String temp;
        this.cellID = cellID;
        this.x =  (Integer) cellData.get("x");
        this.y = (Integer) cellData.get("y");
        this.length = (Integer) cellData.get("length");
        this.height = (Integer) cellData.get("height");
        temp = (String) cellData.get("left");
        this.leftCell = temp.equals("none")? null: temp;
        temp = (String) cellData.get("right");
        this.rightCell = temp.equals("none")? null: temp;
        temp = (String) cellData.get("top");
        this.topCell = temp.equals("none")? null: temp;
        temp = (String) cellData.get("bottom");
        this.bottomCell = temp.equals("none")? null: temp;

        this.doorx=(Integer)cellData.get("doorx");
        this.doory=(Integer)cellData.get("doory");
        this.doorlength=(Integer)cellData.get("doorlength");


    }

     */
}
