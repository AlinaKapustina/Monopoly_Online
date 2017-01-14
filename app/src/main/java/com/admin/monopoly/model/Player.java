
package com.admin.monopoly.model;

public class Player {
    
    private String login;
    private int sum;
    private int pos;
    private boolean isLife = true;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public boolean isIsLife() {
        return isLife;
    }

    public void setIsLife(boolean isLife) {
        this.isLife = isLife;
    }

    public Player(String login, int sum) {
        this.login = login;
        this.sum = sum;
        this.pos = 0;
    }
    
    public String getName(){
        return login;
    }

    public int getSum() {
        return sum;
    }

    public void setSum(int sum) {
        this.sum = sum;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }
    
    
        
    
}
