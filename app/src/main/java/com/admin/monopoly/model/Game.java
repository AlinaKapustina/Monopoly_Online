package com.admin.monopoly.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Game {

    private List<Player> player = new ArrayList<>();
    private List<Cell> cells = new ArrayList<>();
    private String onStep;
    private Trade trade = null;

    public Trade getTrade() {
        return trade;
    }

    public void setTrade(Trade trade) {
        this.trade = trade;
    }

    Game(List<String> logins, int sum) {
        for (int i = 0; i < logins.size(); i++) {
            player.add(new Player(logins.get(i), sum));
        }
        onStep = player.get(0).getName();
        addCell();
    }

    final void addCell() {
        try {
            Scanner reader = new Scanner(new File("./data.txt"));
            for (int k = 0; k < 28; k++) {
                int arenda = reader.nextInt();
                int[] cost = new int[5];
                for (int i = 0; i < 5; i++) {
                    cost[i] = reader.nextInt();
                }
                cells.add(new Cell(arenda, cost));
            }

        } catch (FileNotFoundException ex) {

        }

    }

    public List<Player> getPlayer() {
        return player;
    }

    public void setPlayer(List<Player> player) {
        this.player = player;
    }

    public List<Cell> getCells() {
        return cells;
    }

    public String getOnStep() {
        return onStep;
    }

    public void setOnStep(String onStep) {
        this.onStep = onStep;
    }

    public int search(String login) {
        for (int i = 0; i < player.size(); i++) {
            if (player.get(i).getName().equals(login)) {
                return i;
            }
        }
        return -1;
    }

    
}
