package com.icerockdev.icedroid.signinexample.utils;

import com.google.gson.JsonObject;

public class NetworkProtocol {
    private static NetworkProtocol ourInstance = new NetworkProtocol();
    public static int SERVER_PORT = 20202;
    public static String SERVER_IP_V4 = "192.168.0.5";

    public static final int TYPE_NEW_USER = 0;
    public static final int TYPE_AUTHORIZATION = 1;
    public static final int TYPE_LOBBY = 3;
    public static final int TYPE_START_GAME = 4;

    public static final String PROPERTY_TYPE = "Type";
    public static final String PROPERTY_LOGIN = "Login";
    public static final String PROPERTY_E_MAIL = "E-mail";
    public static final String PROPERTY_PASSWORD = "Password";
    public static final String PROPERTY_NUMBER_OF_PLAYERS = "Number";
    public static final String PROPERTY_STATUS = "Status";

    public static NetworkProtocol getInstance() {
        return ourInstance;
    }

    private NetworkProtocol() {
    }

    public String registrateUserMsg(String login, String email, String password) {
        JsonObject regInfo = new JsonObject();

        regInfo.addProperty(PROPERTY_TYPE, TYPE_NEW_USER);
        regInfo.addProperty(PROPERTY_LOGIN, login);
        regInfo.addProperty(PROPERTY_E_MAIL, email);
        regInfo.addProperty(PROPERTY_PASSWORD, password);

        return regInfo.toString();
    }

    public String logInUserMsg(String login, String password) {
        JsonObject logInInfo = new JsonObject();

        logInInfo.addProperty(PROPERTY_TYPE, TYPE_AUTHORIZATION);
        logInInfo.addProperty(PROPERTY_LOGIN, login);
        logInInfo.addProperty(PROPERTY_PASSWORD, password);

        return logInInfo.toString();
    }

    public String createLobbyMsg(String login, int numberOfPlayers) {
        JsonObject createLobbyInfo = new JsonObject();

        createLobbyInfo.addProperty(PROPERTY_TYPE, TYPE_LOBBY);
        createLobbyInfo.addProperty(PROPERTY_LOGIN, login);
        createLobbyInfo.addProperty(PROPERTY_NUMBER_OF_PLAYERS, numberOfPlayers);

        return createLobbyInfo.toString();
    }

    public String startGameMsg(String login) {
        JsonObject startGameInfo = new JsonObject();

        startGameInfo.addProperty(PROPERTY_TYPE, TYPE_START_GAME);
        startGameInfo.addProperty(PROPERTY_LOGIN, login);

        return startGameInfo.toString();
    }
}