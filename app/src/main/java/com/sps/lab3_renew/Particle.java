package com.sps.lab3_renew;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Particle {
    public Integer x;
    public Integer y;
    public float weight;
    public boolean isAlive;
    public ShapeDrawable p;
    public Cell cell;
    public int oldX, oldY;
    public List<String> fragment_list = new ArrayList<>(Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8"
    , "9", "10"));

    private ShapeDrawable drawable;
    public Particle(int x, int y,Cell cell){
        this.x=x;
        this.y=y;
        this.isAlive=true;
        this.weight=0f;
        this.cell=cell;
        drawable = new ShapeDrawable(new OvalShape());
        drawable.getPaint().setColor(Color.RED);
        drawable.setBounds(x, y, x+10, y+10);

    }
    public Particle(int x, int y, Cell c, float weight) {
        this.x = x;
        this.y = y;
        this.cell = c;
        this.isAlive = true;
        //第二个构造函数会更新weight
        this.weight = weight;
        p = new ShapeDrawable(new OvalShape());
        p.getPaint().setColor(Color.RED);
        p.setBounds(x, y, x + 10, y + 10);
    }
    public void drawparticle(Canvas canvas){
        drawable.draw(canvas);
    }


    //pixelcount是什么意思呢？
    //给出方向和pixelcount,particle移动

    public void move(int pixelCount, int direction) {
        int tempX = this.x;
        int tempY = this.y;

//        switch (direction) {
//            case 0:
//                this.y -= pixelCount;
//                break;
//            case 90:
//                this.x += pixelCount;
//                break;
//            case 180:
//                this.y += pixelCount;
//                break;
//            case 270:
//                this.x -= pixelCount;
//                break;
//        }

        switch (direction) {
            case 0:
                tempX -= pixelCount;
                break;
            case 90:
                tempY -= pixelCount;
                break;
            case 180:
                tempX += pixelCount;
                break;
            case 270:
                tempY += pixelCount;
                break;
        }

        this.oldX = this.x;
        this.oldY = this.y;

        if ((tempX < 50) || (tempX > 1012) || (tempY < 30) || (tempY > 2150)) {
            this.x = 1;
            this.y = 1;
        }
        else {
            this.x = tempX;
            this.y = tempY;
        }
        //System.out.println("moving");

    }

    public void  validateCell (HashMap<String, Cell> ch) {
        //System.out.println(this.y);

        if ((x == 1) || (y == 1)) {
            isAlive = false;
            return;
        }


        //System.out.printf("x of the cell %d\n", this.cell.x);
        //System.out.printf("this partilce is in %s\n", this.cell.cellID);
        if(this.cell.cellID.equals("8")){
            if(this.x>=387 & this.x<=612 & this.y>=this.cell.y){
                this.isAlive=false;
                return;
            }
        }
        if(this.cell.cellID.equals("8")){
            if(this.x>=612&this.x<=1012&this.y<=(this.cell.y-this.cell.height)){
                this.isAlive=false;
                return;
            }
        }
        if (x <cell.x || x==cell.x) {
            if(cell.door_location.equals("left")){
                if(this.y<=this.cell.doory&&this.y>=this.cell.doory-this.cell.doorlength){
                    this.isAlive=true;
                    this.cell=ch.get(cell.leftCell);
                }else{
                    this.isAlive=false;
                    return;
                }
            }else{
                if(this.cell.leftCell!=null){
                    this.isAlive=true;
                    this.cell=ch.get(cell.leftCell);
                }else{
                    this.isAlive=false;
                    return;
                }
            }
        }

        if (x >(cell.x + cell.length) || x==(cell.x+cell.length)) {
            if(cell.door_location.equals("right")){
                if(this.y<=this.cell.doory&&this.y>=this.cell.doory-this.cell.doorlength){
                    this.isAlive=true;
                    this.cell=ch.get(cell.rightCell);
                }else{
                    this.isAlive=false;
                    return;
                }
            }else{
                if(this.cell.rightCell!=null){
                    this.isAlive=true;
                    this.cell=ch.get(cell.rightCell);
                }else{
                    this.isAlive=false;
                    return;
                }
            }

        }

        if (y <(cell.y - cell.height) || y==(cell.y-cell.height)) {
            if(cell.door_location.equals("top")){
                if(this.x>=this.cell.doorx&&this.x<=this.cell.doorx+this.cell.doorlength){
                    this.isAlive=true;
                    this.cell=ch.get(cell.topCell);

                }else{
                    this.isAlive=false;
                    return;
                }
            }else{
                if(cell.topCell!=null){
                    this.isAlive=true;
                    this.cell=ch.get(cell.topCell);
                }else{
                    this.isAlive=false;
                    return;
                }
            }

        }

        if (y > cell.y || y==cell.y) {
            if(cell.cellID.equals("2")) {
                if(this.x>=this.cell.door2x&this.x<=this.cell.door2x+this.cell.door2length) {
                    isAlive = true;
                    cell = ch.get(cell.bottomCell);
                }
                    else{
                        isAlive=false;
                        return;
                    }
            }else if(cell.door_location.equals("bottom")) {
                if (this.x >= this.cell.doorx & this.x < this.cell.doorx + this.cell.doorlength) {
                    isAlive = true;
                    cell = ch.get(cell.bottomCell);
                } else {
                    this.isAlive = false;
                    return;
                }
            }else {
                System.out.println(this.cell.bottomCell);
                if (cell.bottomCell != null) {
                    this.isAlive = true;
                    this.cell = ch.get(cell.bottomCell);
                } else {
                    this.isAlive = false;
                    return;
                }
            }

        }

    }

}

