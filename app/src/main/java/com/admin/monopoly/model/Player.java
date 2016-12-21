
package com.admin.monopoly.model;

public class Player {

    private String login;
    private int sum;
    private int pos;

    public Player(String login, int sum) {
        this.login = login;
        this.sum = sum;
        this.pos = 0;
    }

    public String getName() {
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
