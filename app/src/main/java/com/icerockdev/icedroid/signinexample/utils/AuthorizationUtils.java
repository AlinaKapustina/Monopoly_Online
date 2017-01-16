package com.icerockdev.icedroid.signinexample.utils;


import android.content.Context;
import android.content.SharedPreferences;

public class AuthorizationUtils
{
	private static final String PREFERENCES_AUTHORIZED_KEY = "isAuthorized";
	private static final String LOGIN_PREFERENCES = "LoginData";

	/**
	 * This method makes the user authorized
	 *
	 * @param context current context
	 */

	public static void setAuthorized(Context context, String login)
	{

		context.getSharedPreferences(LOGIN_PREFERENCES, Context.MODE_PRIVATE)
				.edit()
				.putString(PREFERENCES_AUTHORIZED_KEY, login)
				.apply();
	}

	/**
	 * This method makes the user unauthorized
	 *
	 * @param context current context
	 */

	public static void logout(Context context)
	{
		context.getSharedPreferences(LOGIN_PREFERENCES, Context.MODE_PRIVATE)
				.edit()
				.remove(PREFERENCES_AUTHORIZED_KEY)
				.apply();
	}

	/**
	 * This method checks if the user is authorized
	 *
	 * @param context current context
	 * @return {@code true} if the user is authorized and {@code false} if not
	 */

	public static boolean isAuthorized(Context context)
	{
		SharedPreferences sp = context.getSharedPreferences(LOGIN_PREFERENCES, Context.MODE_PRIVATE);
		String login = sp.getString(PREFERENCES_AUTHORIZED_KEY, "");
		return !login.isEmpty();
	}

	public static String getLogin(Context context) {
		return context.getSharedPreferences(LOGIN_PREFERENCES, Context.MODE_PRIVATE).getString(PREFERENCES_AUTHORIZED_KEY, null);
	}
}
