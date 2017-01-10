package com.icerockdev.icedroid.signinexample.utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by alex on 19.12.16.
 */
public class NetworkProtocol {
    private static NetworkProtocol ourInstance = new NetworkProtocol();
    public static int SERVER_PORT = 20202;
    public static String SERVER_IP_V4 = "192.168.0.5";

    public static NetworkProtocol getInstance() {
        return ourInstance;
    }

    private NetworkProtocol() {
    }

    public String registrateUser(String login, String email, String password) {

        JSONObject regInfo = new JSONObject();
        try {
            regInfo.put("Type", 0);
            regInfo.put("Login", login);
            regInfo.put("E-mail", email);
            regInfo.put("Password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String message = regInfo.toString();

        return message;
    }

    public String logInUser(String login, String password) {
        JSONObject logInInfo = new JSONObject();
        try {
            logInInfo.put("Type", 1);
            logInInfo.put("Login", login);
            logInInfo.put("Password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String message = logInInfo.toString();

        return message;
    }
}