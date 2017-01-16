package com.icerockdev.icedroid.signinexample.utils;

import android.annotation.TargetApi;
import android.os.Build;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.icerockdev.icedroid.signinexample.BusProvider;
import com.icerockdev.icedroid.signinexample.model.Game;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;


public class Client {

    private static Semaphore semaphore = new Semaphore(1);

    public static Observer<Game> getSubscriber() {
        return new Observer<Game>() {

            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
            }

            @Override
            public void onNext(Game game) {
                BusProvider.getInstance().post(game);
            }
        };
    }

    public static Observer<Boolean> getEmptySubscriber() {
        return new Observer<Boolean>() {

            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
            }

            @Override
            public void onNext(Boolean b) {
                BusProvider.getInstance().post(false);
            }
        };
    }

    public static void takeSemaphore() {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static Observable.OnSubscribe<Game> getModelByServer(final String login) {
        return new Observable.OnSubscribe<Game>() {

            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void call(Subscriber<? super Game> subscriber) {
                while (true) {
                    try {
                        semaphore.acquire();
                        try (Socket socket = new Socket(NetworkProtocol.SERVER_IP_V4, NetworkProtocol.SERVER_PORT)) {
                            try (InputStream in = socket.getInputStream(); OutputStream out = socket.getOutputStream()) {
                                JSONObject jsonObj = new JSONObject();
                                jsonObj.put("Type", NetworkProtocol.TYPE_UPDATE_GAME);
                                jsonObj.put("Login", login);
                                out.write(jsonObj.toString().getBytes(NetworkProtocol.CHARSET));
                                byte[] buffer = new byte[NetworkProtocol.BUFFER_SIZE];
                                String answer = "";
                                int size;
                                while (true) {
                                    size = in.read(buffer, 0, NetworkProtocol.BUFFER_SIZE);
                                    if (size == -1) {
                                        break;
                                    }
                                    answer += new String(buffer, 0, size, NetworkProtocol.CHARSET);
                                }
                                JsonObject json = new JsonParser().parse(answer).getAsJsonObject();
                                String game1 = json.get("Game").getAsString();
                                Gson gson = new GsonBuilder().create();
                                Game game = gson.fromJson(game1, Game.class);
                                subscriber.onNext(game);
                            }
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    semaphore.release();
                    try {
                        TimeUnit.SECONDS.sleep(NetworkProtocol.TIMEOUT);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    public static Observable.OnSubscribe<Boolean> transferStepToServer(final String login, final int cellNumber) {
        return new Observable.OnSubscribe<Boolean>() {

            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try (Socket socket = new Socket(NetworkProtocol.SERVER_IP_V4, NetworkProtocol.SERVER_PORT)) {
                    try (OutputStream out = socket.getOutputStream()) {
                        JSONObject jsonObj = new JSONObject();
                        jsonObj.put("Type", NetworkProtocol.TYPE_GAME);
                        jsonObj.put("Login", login);
                        jsonObj.put("Cell", cellNumber);
                        System.out.println(jsonObj.toString());
                        out.write(jsonObj.toString().getBytes(NetworkProtocol.CHARSET));
                        subscriber.onCompleted();
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
                semaphore.release();
            }
        };
    }

    public static Observable.OnSubscribe<Boolean> transferAddHouseToServer(final String login, final int cellNumber) {
        return new Observable.OnSubscribe<Boolean>() {

            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try (Socket socket = new Socket(NetworkProtocol.SERVER_IP_V4, NetworkProtocol.SERVER_PORT)) {
                    try (OutputStream out = socket.getOutputStream()) {
                        JSONObject jsonObj = new JSONObject();
                        jsonObj.put("Type", NetworkProtocol.TYPE_ADD_HOUSE);
                        jsonObj.put("Login", login);
                        jsonObj.put("Cell", cellNumber);
                        out.write(jsonObj.toString().getBytes(NetworkProtocol.CHARSET));
                        subscriber.onCompleted();
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
                semaphore.release();
            }
        };
    }

    public static Observable.OnSubscribe<Boolean> transferDeleteHouseToServer(final String login, final int cellNumber) {
        return new Observable.OnSubscribe<Boolean>() {

            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try (Socket socket = new Socket(NetworkProtocol.SERVER_IP_V4, NetworkProtocol.SERVER_PORT)) {
                    try (OutputStream out = socket.getOutputStream()) {
                        JSONObject jsonObj = new JSONObject();
                        jsonObj.put("Type", NetworkProtocol.TYPE_DELETE_HOUSE);
                        jsonObj.put("Login", login);
                        jsonObj.put("Cell", cellNumber);
                        out.write(jsonObj.toString().getBytes(NetworkProtocol.CHARSET));
                        subscriber.onCompleted();
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
                semaphore.release();
            }
        };
    }

    public static Observable.OnSubscribe<Boolean> transferDealToServer(final String login, final int cellNumber, final int cost) {
        return new Observable.OnSubscribe<Boolean>() {

            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    semaphore.acquire();
                    try (Socket socket = new Socket(NetworkProtocol.SERVER_IP_V4, NetworkProtocol.SERVER_PORT)) {
                        try (OutputStream out = socket.getOutputStream()) {
                            JSONObject jsonObj = new JSONObject();
                            jsonObj.put("Type", NetworkProtocol.TYPE_TRADE);
                            jsonObj.put("Login", login);
                            jsonObj.put("Cell", cellNumber);
                            jsonObj.put("Cost", cost);
                            out.write(jsonObj.toString().getBytes(NetworkProtocol.CHARSET));
                            subscriber.onCompleted();
                        }
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                semaphore.release();
            }
        };
    }

    public static Observable.OnSubscribe<Boolean> transferAnswerDealToServer(final String login, final boolean answer) {
        return new Observable.OnSubscribe<Boolean>() {

            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    semaphore.acquire();
                    try (Socket socket = new Socket(NetworkProtocol.SERVER_IP_V4, NetworkProtocol.SERVER_PORT)) {
                        try (OutputStream out = socket.getOutputStream()) {
                            JSONObject jsonObj = new JSONObject();
                            jsonObj.put("Type", NetworkProtocol.TYPE_ANSWER_TRADE);
                            jsonObj.put("Login", login);
                            jsonObj.put("Answer", answer);
                            out.write(jsonObj.toString().getBytes(NetworkProtocol.CHARSET));
                            subscriber.onCompleted();
                        }
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                semaphore.release();
            }
        };
    }
}
