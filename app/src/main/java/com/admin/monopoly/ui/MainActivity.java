package com.admin.monopoly.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.admin.monopoly.BusProvider;
import com.admin.monopoly.R;
import com.admin.monopoly.model.Game;
import com.admin.monopoly.net.Client;
import com.squareup.otto.Subscribe;

import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String[] PLAYER_ICONS = {"♥", "♦", "♣", "☺"};
    private static final int OFF = 0;
    private static final int ADD = 1;
    private static final int DELETE = 2;
    private String login = "admin";
    private Game game;

    private int modeOperationWithHouse = OFF;

    @Bind(R.id.dice)
    Button diceButton;
    @Bind(R.id.add_house)
    Button addHouseButton;
    @Bind(R.id.delete_house)
    Button deleteHouseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        ButterKnife.bind(this);
        TextView textView = (TextView) findViewById(getResources().getIdentifier("cell0", "id", getPackageName()));
        textView.setText(PLAYER_ICONS[0] + PLAYER_ICONS[1] + PLAYER_ICONS[2] + PLAYER_ICONS[3]);
        LinearLayout linearLayout = (LinearLayout) findViewById(getResources().getIdentifier("player1", "id", getPackageName()));
        linearLayout.setBackgroundResource(R.drawable.active_player_background);
        for (int i = 0; i < 28; i++) {
            linearLayout = (LinearLayout) findViewById(getResources().getIdentifier("cell_layout" + i, "id", getPackageName()));
            final int cellNumber = i;
            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (modeOperationWithHouse == ADD) {
                        System.out.println(game.getCells().get(cellNumber).getName());
                        if (login.equals(game.getCells().get(cellNumber).getName()) && game.getCells().get(cellNumber).getHouse() < 4) {
                            game.getCells().get(cellNumber).setHouse(game.getCells().get(cellNumber).getHouse() + 1);
                            TextView textView = (TextView) findViewById(getResources().getIdentifier("cell" + cellNumber, "id", getPackageName()));
                            textView.setText(textView.getText() + "▲");
                            Observable.create(Client.transferAddHouseToServer(login, cellNumber))
                                    .subscribeOn(Schedulers.newThread())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(Client.getEmptySubscriber());
                        }
                    } else if (modeOperationWithHouse == DELETE) {
                        if (login.equals(game.getCells().get(cellNumber).getName()) && game.getCells().get(cellNumber).getHouse() > 0) {
                            game.getCells().get(cellNumber).setHouse(game.getCells().get(cellNumber).getHouse() - 1);
                            TextView textView = (TextView) findViewById(getResources().getIdentifier("cell" + cellNumber, "id", getPackageName()));
                            textView.setText(textView.getText().toString().substring(0, textView.getText().length() - 1));
                            Observable.create(Client.transferDeleteHouseToServer(login, cellNumber))
                                    .subscribeOn(Schedulers.newThread())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(Client.getEmptySubscriber());
                        }
                    }
                }
            });
        }
        Observable.create(Client.getModelByServer(login))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Client.getSubscriber());
    }

    @OnClick(R.id.dice)
    void onClickDice(View view) {
        Random random = new Random();
        int number = random.nextInt(6) + 1;
        diceButton.setText(String.valueOf(number));
        int position = game.getPlayer().get(game.search(login)).getPos();
        TextView textView = (TextView) findViewById(getResources().getIdentifier("cell" + position, "id", getPackageName()));
        String icon = PLAYER_ICONS[game.search(login)];
        StringBuffer stringBuffer = new StringBuffer(textView.getText().toString());
        stringBuffer.deleteCharAt(stringBuffer.indexOf(icon));
        textView.setText(stringBuffer);
        position = (position + number) % 28;
        game.getPlayer().get(game.search(login)).setPos(position);
        textView = (TextView) findViewById(getResources().getIdentifier("cell" + position, "id", getPackageName()));
        textView.setText(textView.getText() + icon);
        diceButton.setEnabled(false);
        addHouseButton.setEnabled(false);
        deleteHouseButton.setEnabled(false);
        Observable.create(Client.transferStepToServer(login, position))
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Client.getEmptySubscriber());
    }

    @OnClick(R.id.add_house)
    void onClickAddHouse(View view) {
        if (modeOperationWithHouse == ADD) {
            modeOperationWithHouse = OFF;
            addHouseButton.setText(R.string.add_house);
        } else {
            modeOperationWithHouse = ADD;
            addHouseButton.setText(R.string.on);
            deleteHouseButton.setText(R.string.delete_house);
        }
    }

    @OnClick(R.id.delete_house)
    void onClickDeleteHouse(View view) {
        if (modeOperationWithHouse == DELETE) {
            modeOperationWithHouse = OFF;
            deleteHouseButton.setText(R.string.delete_house);
        } else {
            modeOperationWithHouse = DELETE;
            deleteHouseButton.setText(R.string.on);
            addHouseButton.setText(R.string.add_house);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    @Subscribe
    public void step(Game game) {
        this.game = game;
        updateView();
    }

    private void updateView() {
        int playerNumber = game.search(login);
        int size = game.getPlayer().size();
        LinearLayout linearLayout;
        for (int i = size + 1; i <= 4; i++) {
            linearLayout = (LinearLayout) findViewById(getResources().getIdentifier("player" + i, "id", getPackageName()));
            linearLayout.setVisibility(View.INVISIBLE);
        }
        TextView textView;
        for (int i = 1; i <= size; i++) {
            textView = (TextView) findViewById(getResources().getIdentifier("player" + i + "_login", "id", getPackageName()));
            textView.setText(game.getPlayer().get(i - 1).getName() + " " + PLAYER_ICONS[i - 1]);
            textView = (TextView) findViewById(getResources().getIdentifier("player" + i + "_sum", "id", getPackageName()));
            textView.setText(String.valueOf(game.getPlayer().get(i - 1).getSum()) + "$");
        }
        for (int i = 0; i < 28; i++) {
            textView = (TextView) findViewById(getResources().getIdentifier("cell" + i, "id", getPackageName()));
            int number = game.getCells().get(i).getHouse();
            String line = "";
            for (int j = 0; j < number; j++) {
                line += "▲";
            }
            textView.setText(line);
            if (!game.getCells().get(i).getName().isEmpty()) {
                number = game.search(game.getCells().get(i).getName());
                textView = (TextView) findViewById(getResources().getIdentifier("cell_country" + i, "id", getPackageName()));
                if (textView.getText().toString().lastIndexOf(PLAYER_ICONS[number]) == -1) {
                    textView.setText(textView.getText().toString() + PLAYER_ICONS[number]);
                }
            }
        }
        int onStep = game.search(game.getOnStep());
        for (int i = 0; i < size; i++) {
            if (i == onStep) {
                linearLayout = (LinearLayout) findViewById(getResources().getIdentifier("player" + (i + 1), "id", getPackageName()));
                linearLayout.setBackgroundResource(R.drawable.active_player_background);
            } else {
                linearLayout = (LinearLayout) findViewById(getResources().getIdentifier("player" + (i + 1), "id", getPackageName()));
                linearLayout.setBackgroundResource(R.drawable.player_background);
            }
            textView = (TextView) findViewById(getResources().getIdentifier("cell" + game.getPlayer().get(i).getPos(), "id", getPackageName()));
            textView.setText(PLAYER_ICONS[i] + textView.getText());
        }
        if (onStep == playerNumber) {
            diceButton.setEnabled(true);
            diceButton.setText(R.string.dice);
            addHouseButton.setEnabled(true);
            deleteHouseButton.setEnabled(true);
        }else{
            diceButton.setEnabled(false);
            addHouseButton.setEnabled(false);
            deleteHouseButton.setEnabled(false);
        }
    }
}