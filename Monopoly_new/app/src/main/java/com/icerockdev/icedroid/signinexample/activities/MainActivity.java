package com.icerockdev.icedroid.signinexample.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.icerockdev.icedroid.signinexample.R;
import com.icerockdev.icedroid.signinexample.utils.AuthorizationUtils;
import com.icerockdev.icedroid.signinexample.utils.NetworkProtocol;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final int MIN_PLAYERS = 2;
    private final int MAX_PLAYERS = 4;

    private TextInputLayout mNumberOfPlayers;
    private Button mStartGameButton;
    private Handler handler;
    private Thread networkConnetion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//		Here is a checkpoint if the user is authorized.
        if (!AuthorizationUtils.isAuthorized(this)) {

            onLogout();
            return;
        }

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mNumberOfPlayers = (TextInputLayout) findViewById(R.id.number_of_players_layout);

        mStartGameButton = (Button) findViewById(R.id.start_game_button);
        mStartGameButton.setOnClickListener(this);

        handler = new Handler();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout: {
                AuthorizationUtils.logout(this);
                onLogout();
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    //	If user is not authorized we finish the main activity
    private void onLogout() {
        Intent login = new Intent(this, LoginActivity.class);
        login.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(login);
        finish();
    }

    private void onStartGame(boolean isStartGame) {
        mStartGameButton.setEnabled(true);
        if (isStartGame) {
            //This method for starts the game

		    /*Intent game = new Intent(this, Game.class);
            startActivity(game);*/
		    finish();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_game_button: {
                if (areFieldsValid()) {
                    mStartGameButton.setEnabled(false);
                    final String login = AuthorizationUtils.getLogin(this);
                    if (login == null) {
                        (Toast.makeText(getApplicationContext(), "Please log in", Toast.LENGTH_LONG)).show();
                        onLogout();
                    }

                    networkConnetion = new NetworkConnection(login, Integer.parseInt(mNumberOfPlayers.getEditText().getText().toString()));
                    networkConnetion.start();
                } else {
                    setError();
                }
                break;
            }
        }
    }

    private class NetworkConnection extends Thread {
        private String login;
        private int numberOfPlayers;

        public NetworkConnection(String login, int numberOfPlayers) {
            this.login = login;
            this.numberOfPlayers = numberOfPlayers;
        }

        @Override
        public void run() {

            String response = null;
            try {
                Socket client = new Socket(NetworkProtocol.SERVER_IP_V4, NetworkProtocol.SERVER_PORT);
                client.setSoTimeout(5 * 1000);
                InputStream in = new BufferedInputStream(client.getInputStream());
                BufferedOutputStream out = new BufferedOutputStream(client.getOutputStream());

                String message = NetworkProtocol.getInstance().createLobbyMsg(login,
                        numberOfPlayers);
                out.write(message.getBytes("UTF-8"), 0, message.length());
                out.flush();
                byte[] buffer = new byte[1000];
                int size = in.read(buffer);
                if (size <= 0) {
                    handler.post(new ToastInfo("Network error"));
                } else {
                    response = new String(buffer, 0, size, "UTF-8");
                }
            } catch (IOException e) {
                handler.post(new ToastInfo("Server not respond"));
            }
            if (!isLobbyCreate(response)) {
                handler.post(new ToastInfo("Game can't start"));
                handler.post(new StartGame(false));
                return;
            }

            response = null;
            do {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
                try {
                    Socket client = new Socket(NetworkProtocol.SERVER_IP_V4, NetworkProtocol.SERVER_PORT);
                    client.setSoTimeout(5 * 1000);
                    InputStream in = new BufferedInputStream(client.getInputStream());
                    BufferedOutputStream out = new BufferedOutputStream(client.getOutputStream());

                    String message = NetworkProtocol.getInstance().startGameMsg(login);
                    out.write(message.getBytes("UTF-8"), 0, message.length());
                    out.flush();
                    byte[] buffer = new byte[1000];
                    int size = in.read(buffer);
                    if (size <= 0) {
                    } else {
                        response = new String(buffer, 0, size, "UTF-8");
                    }
                } catch (IOException e) {
                }
                if (isGameStart(response)) {
                    break;
                }
            } while (!isInterrupted());
            handler.post(new StartGame(isGameStart(response)));
        }

        private class ToastInfo implements Runnable {
            private String message;

            public ToastInfo(String message) {
                this.message = message;
            }

            @Override
            public void run() {
                (Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT)).show();
            }
        }

        private class StartGame implements Runnable {
            private boolean isStartGame;

            StartGame(boolean isStartGame) {
                this.isStartGame = isStartGame;
            }

            @Override
            public void run() {
                onStartGame(isStartGame);
            }
        }

        boolean isLobbyCreate(String response) {
            boolean status = false;
            if (response != null) {
                JsonObject js = new JsonParser().parse(response).getAsJsonObject();
                if (js.get(NetworkProtocol.PROPERTY_TYPE).getAsInt() == NetworkProtocol.TYPE_LOBBY) {
                    status = js.get(NetworkProtocol.PROPERTY_STATUS).getAsBoolean();
                }
            }
            return status;
        }

        boolean isGameStart(String response) {
            boolean status = false;
            if (response != null) {
                JsonObject js = new JsonParser().parse(response).getAsJsonObject();
                if (js.get(NetworkProtocol.PROPERTY_TYPE).getAsInt() == NetworkProtocol.TYPE_START_GAME) {
                    status = js.get(NetworkProtocol.PROPERTY_STATUS).getAsBoolean();
                }
            }
            return status;
        }
    }

    private boolean areFieldsValid() {
        if (mNumberOfPlayers != null &&
                mNumberOfPlayers.getEditText() != null) {
            String numberOfPlayersText = mNumberOfPlayers.getEditText().getText().toString();
            if (!TextUtils.isEmpty(numberOfPlayersText) && TextUtils.isDigitsOnly(numberOfPlayersText)) {
                int numberOfPlayers = Integer.parseInt(numberOfPlayersText);
                if (MIN_PLAYERS <= numberOfPlayers && numberOfPlayers <= MAX_PLAYERS) {
                    mNumberOfPlayers.setError(null);
                    mNumberOfPlayers.setErrorEnabled(false);
                    return true;
                }
            }
        }
        return false;
    }

    private void setError() {
        if (mNumberOfPlayers != null) {
            mNumberOfPlayers.setError(getString(R.string.wrong_number_of_players));
            if (mNumberOfPlayers.getEditText() != null) {
                mNumberOfPlayers.getEditText().setText("");
            }
        }
    }

    @Override
    public void finish() {
        if (networkConnetion != null) {
            networkConnetion.interrupt();
        }
        super.finish();
    }
}
