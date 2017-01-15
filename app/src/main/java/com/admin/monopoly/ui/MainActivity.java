package com.admin.monopoly.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.admin.monopoly.BusProvider;
import com.admin.monopoly.R;
import com.admin.monopoly.model.Game;
import com.admin.monopoly.model.Player;
import com.admin.monopoly.model.Trade;
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
    private static final int DEAL = 3;
    private String login = "admin1";
    private Game game;
    private boolean dialogIsShow = false;
    private int modeOperation = OFF;
    private AlertDialog dialog;

    @Bind(R.id.deal)
    Button dealButton;
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
                    if (modeOperation == ADD) {
                        if (login.equals(game.getCells().get(cellNumber).getName()) && game.getCells().get(cellNumber).getHouse() < 4) {
                            Client.takeSemaphore();
                            game.getCells().get(cellNumber).setHouse(game.getCells().get(cellNumber).getHouse() + 1);
                            TextView textView = (TextView) findViewById(getResources().getIdentifier("cell" + cellNumber, "id", getPackageName()));
                            textView.setText(textView.getText() + "▲");
                            Observable.create(Client.transferAddHouseToServer(login, cellNumber))
                                    .subscribeOn(Schedulers.newThread())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(Client.getEmptySubscriber());
                        }
                    } else if (modeOperation == DELETE) {
                        if (login.equals(game.getCells().get(cellNumber).getName()) && game.getCells().get(cellNumber).getHouse() > 0) {
                            Client.takeSemaphore();
                            game.getCells().get(cellNumber).setHouse(game.getCells().get(cellNumber).getHouse() - 1);
                            TextView textView = (TextView) findViewById(getResources().getIdentifier("cell" + cellNumber, "id", getPackageName()));
                            textView.setText(textView.getText().toString().substring(0, textView.getText().length() - 1));
                            Observable.create(Client.transferDeleteHouseToServer(login, cellNumber))
                                    .subscribeOn(Schedulers.newThread())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(Client.getEmptySubscriber());
                        }
                    } else if (modeOperation == DEAL) {
                        String cellOwner = game.getCells().get(cellNumber).getName();
                        if (!cellOwner.isEmpty() && !login.equals(cellOwner)) {
                            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                            alert.setTitle("Цена");
                            TextView textView = (TextView) findViewById(getResources().getIdentifier("cell_country" + cellNumber, "id", getPackageName()));
                            alert.setMessage(textView.getText());
                            final EditText input = new EditText(MainActivity.this);
                            input.setInputType(InputType.TYPE_CLASS_NUMBER);
                            alert.setView(input);
                            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    int value = Integer.parseInt(input.getText().toString());
                                    if (value > 0) {
                                        dealButton.setText(R.string.deal);
                                        modeOperation = OFF;
                                        dealButton.setEnabled(false);
                                        diceButton.setEnabled(false);
                                        addHouseButton.setEnabled(false);
                                        deleteHouseButton.setEnabled(false);
                                        Observable.create(Client.transferDealToServer(login, cellNumber, value))
                                                .subscribeOn(Schedulers.newThread())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(Client.getEmptySubscriber());
                                    } else {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                        builder.setTitle("Error")
                                                .setMessage("Negative cost")
                                                .setCancelable(false)
                                                .setNegativeButton("ОК",
                                                        new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int id) {
                                                            }
                                                        });
                                        AlertDialog alert = builder.create();
                                        alert.show();
                                    }
                                }
                            });

                            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                }
                            });

                            AlertDialog dialog = alert.create();
                            dialog.show();
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

    @OnClick(R.id.deal)
    void onClickDeal(View view) {
        if (modeOperation == DEAL) {
            modeOperation = OFF;
            dealButton.setText(R.string.deal);
        } else {
            modeOperation = DEAL;
            dealButton.setText(R.string.on);
            addHouseButton.setText(R.string.add_house);
            deleteHouseButton.setText(R.string.delete_house);
        }
    }

    @OnClick(R.id.dice)
    void onClickDice(View view) {
        Client.takeSemaphore();
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
        dealButton.setEnabled(false);
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
        if (modeOperation == ADD) {
            modeOperation = OFF;
            addHouseButton.setText(R.string.add_house);
        } else {
            modeOperation = ADD;
            addHouseButton.setText(R.string.on);
            dealButton.setText(R.string.deal);
            deleteHouseButton.setText(R.string.delete_house);
        }
    }

    @OnClick(R.id.delete_house)
    void onClickDeleteHouse(View view) {
        if (modeOperation == DELETE) {
            modeOperation = OFF;
            deleteHouseButton.setText(R.string.delete_house);
        } else {
            modeOperation = DELETE;
            deleteHouseButton.setText(R.string.on);
            dealButton.setText(R.string.deal);
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
                String text = textView.getText().toString();
                for (String PLAYER_ICON : PLAYER_ICONS) {
                    if (textView.getText().toString().lastIndexOf(PLAYER_ICON) != -1) {
                        text = text.substring(0, text.length() - 1);
                        break;
                    }
                }
                textView.setText(text + PLAYER_ICONS[number]);
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
            if (!game.getPlayer().get(i).isIsLife()) {
                linearLayout = (LinearLayout) findViewById(getResources().getIdentifier("player" + (i + 1), "id", getPackageName()));
                linearLayout.setBackgroundResource(R.drawable.negative_player_background);
            }
            textView = (TextView) findViewById(getResources().getIdentifier("cell" + game.getPlayer().get(i).getPos(), "id", getPackageName()));
            textView.setText(PLAYER_ICONS[i] + textView.getText());
        }
        if (onStep == playerNumber) {
            dealButton.setEnabled(true);
            diceButton.setEnabled(true);
            diceButton.setText(R.string.dice);
            addHouseButton.setEnabled(true);
            deleteHouseButton.setEnabled(true);
        } else {
            dealButton.setEnabled(false);
            diceButton.setEnabled(false);
            addHouseButton.setEnabled(false);
            deleteHouseButton.setEnabled(false);
        }
        Trade trade = game.getTrade();
        if (!dialogIsShow) {
            if (trade != null) {
                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                alert.setTitle("Сделка");
                alert.setMessage(trade.salesman + " -> " + trade.customer);
                String text = ((TextView) findViewById(getResources().getIdentifier("cell_country" + trade.cell, "id", getPackageName()))).getText().toString();
                final TextView textView1 = new TextView(MainActivity.this);
                textView1.setText(text + "(" + trade.price + "$)");
                alert.setView(textView1);
                alert.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Observable.create(Client.transferAnswerDealToServer(login, true))
                                .subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(Client.getEmptySubscriber());
                        dialogIsShow = false;
                    }
                });

                alert.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Observable.create(Client.transferAnswerDealToServer(login, false))
                                .subscribeOn(Schedulers.newThread())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(Client.getEmptySubscriber());
                        dialogIsShow = false;
                    }
                });

                dialog = alert.create();
                dialog.show();
                dialogIsShow = true;
                if (!login.equals(trade.salesman)) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
                }
            }
        } else {
            if (trade == null) {
                dialog.dismiss();
                dialogIsShow = false;
            }
        }
        if (game.getLifeNumber() == 1) {
            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
            alert.setTitle("Конец");
            for (Player player : game.getPlayer()) {
                if (player.isIsLife()) {
                    alert.setMessage("Победил " + player.getLogin());
                }
            }
            dialog = alert.create();
            dialog.setCancelable(false);
            dialog.show();
        }
    }
}