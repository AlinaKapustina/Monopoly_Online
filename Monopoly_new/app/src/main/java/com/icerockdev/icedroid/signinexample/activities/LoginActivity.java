package com.icerockdev.icedroid.signinexample.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
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

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private TextInputLayout mLoginLayout;
    private TextInputLayout mPasswordLayout;
    private Button mSignInBtn;
    private String login = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mLoginLayout = (TextInputLayout) findViewById(R.id.login_log_layout);
        mPasswordLayout = (TextInputLayout) findViewById(R.id.password_log_layout);
        mSignInBtn = (Button) findViewById(R.id.sign_in_button);
        Button signUpButton = (Button) findViewById(R.id.reg_new_user_button);
        mSignInBtn.setOnClickListener(this);
        signUpButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button: {
                if (areFieldsValid()) {
                    login = mLoginLayout.getEditText().getText().toString();
                    String message = NetworkProtocol.getInstance().logInUserMsg(login,
                            mPasswordLayout.getEditText().getText().toString());
                    mSignInBtn.setEnabled(false);
                    (new LogInUserTask()).execute(message);
                } else {
                    setError();
                }
                break;
            }
            case R.id.reg_new_user_button: {
                Intent registration = new Intent(this, RegistrationActivity.class);
                startActivity(registration);
                break;
            }
        }
    }

    //	If user is authorized we launch the main activity
    private void onLoginCompleted(boolean status) {
        mSignInBtn.setEnabled(true);
        if (status) {
            (Toast.makeText(getApplicationContext(), "Authorisation complete", Toast.LENGTH_LONG)).show();
            AuthorizationUtils.setAuthorized(this, login);
            Intent main = new Intent(this, MainActivity.class);
            startActivity(main);
            finish();
        } else {
            setError();
        }

    }

    //	It checks the email field
    private boolean areFieldsValid() {
        return (mLoginLayout != null &&
                mLoginLayout.getEditText() != null &&
                !TextUtils.isEmpty(mLoginLayout.getEditText().getText())) &&
                (mPasswordLayout != null &&
                        mPasswordLayout.getEditText() != null &&
                        !TextUtils.isEmpty(mPasswordLayout.getEditText().getText()));
    }

    private void setError() {
        if (mLoginLayout != null) {
            mLoginLayout.setError(getString(R.string.login_is_not_valid_error_message));
        }
    }

    private class LogInUserTask extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... message) {
            String response = null;

            try {
                Socket client = new Socket(NetworkProtocol.SERVER_IP_V4, NetworkProtocol.SERVER_PORT);
                client.setSoTimeout(5 * 1000);
                InputStream in = new BufferedInputStream(client.getInputStream());
                BufferedOutputStream out = new BufferedOutputStream(client.getOutputStream());
                out.write(message[0].getBytes("UTF-8"), 0, message[0].length());
                out.flush();
                byte[] buffer = new byte[1000];
                int size = in.read(buffer);
                if (size <= 0) {
                    publishProgress("Network error");
                } else {
                    response = new String(buffer, 0, size, "UTF-8");
                }
            } catch (IOException e) {
                publishProgress("Server not respond");
            }
            response = "{Type:1,Status:true}";
            return response;
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            (Toast.makeText(getApplicationContext(), progress[0], Toast.LENGTH_SHORT)).show();
        }

        @Override
        protected void onPostExecute(String result) {
            boolean status = false;
            if (result != null) {
                JsonObject js = new JsonParser().parse(result).getAsJsonObject();
                if (js.get("Type").getAsInt() == 1) {
                    status = js.get("Status").getAsBoolean();
                }
            }
            onLoginCompleted(status);
        }

    }
}

