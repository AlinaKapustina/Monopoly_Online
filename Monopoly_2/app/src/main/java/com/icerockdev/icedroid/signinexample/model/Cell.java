package com.icerockdev.icedroid.signinexample.model;

public class Cell {

    private String name = "";
    private int house = 0;
    private int arenda;
    private int[] cost;

    public Cell(int arenda, int[] cost) {
        this.arenda = arenda;
        this.cost = cost;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }
    

    public int getHouse() {
        return house;
    }

    public void setHouse(int house) {
        this.house = house;
    }

    public int getArenda() {
        return arenda;
    }

    public void setArenda(int arenda) {
        this.arenda = arenda;
    }

    public int[] getCost() {
        return cost;
    }

    public void setCost(int[] cost) {
        this.cost = cost;
    }
    
    
}
