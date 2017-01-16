package com.icerockdev.icedroid.signinexample.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.*;
import com.icerockdev.icedroid.signinexample.R;
import com.icerockdev.icedroid.signinexample.utils.NetworkProtocol;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * A login screen that offers login via email/password.
 */
public class RegistrationActivity extends AppCompatActivity implements View.OnClickListener {
    private TextInputLayout mEmailLayout;
    private TextInputLayout mLoginLayout;
    private TextInputLayout mPasswordLayout;
    private Button mRegistrationBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mEmailLayout = (TextInputLayout) findViewById(R.id.email_reg_layout);
        mLoginLayout = (TextInputLayout) findViewById(R.id.login_reg_layout);
        mPasswordLayout = (TextInputLayout) findViewById(R.id.password_reg_layout);
        mRegistrationBtn = (Button) findViewById(R.id.registration_btn);
        mRegistrationBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.registration_btn: {
                if (areFieldsValid()) {
                    final String message = NetworkProtocol.getInstance().registrateUserMsg(mLoginLayout.getEditText().getText().toString(),
                            mEmailLayout.getEditText().getText().toString(),
                            mPasswordLayout.getEditText().getText().toString());
                    mRegistrationBtn.setEnabled(false);
                    (new RegNewUserTask()).execute(message);
                } else {
                    setError();
                }
                break;
            }
        }
    }

    private void onRegisteredCompleted(boolean status) {
        mRegistrationBtn.setEnabled(true);
        if (status) {
            (Toast.makeText(getApplicationContext(), "Registration complete", Toast.LENGTH_LONG)).show();
            finish();
        } else {
            setError();
        }
    }

    //	It checks the email field
    private boolean areFieldsValid() {
        return (mEmailLayout != null &&
                mEmailLayout.getEditText() != null &&
                !TextUtils.isEmpty(mEmailLayout.getEditText().getText()) &&
                Patterns.EMAIL_ADDRESS.matcher(mEmailLayout.getEditText().getText()).matches()) &&
                (mLoginLayout != null &&
                        mLoginLayout.getEditText() != null &&
                        !TextUtils.isEmpty(mLoginLayout.getEditText().getText())) &&
                (mPasswordLayout != null &&
                        mPasswordLayout.getEditText() != null &&
                        !TextUtils.isEmpty(mPasswordLayout.getEditText().getText()));
    }

    private void setError() {
        if (mEmailLayout != null) {
            mEmailLayout.setError(getString(R.string.email_is_not_valid_error_message));
        }
    }

    private class RegNewUserTask extends AsyncTask<String, String, String> {
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
                if (js.get("Type").getAsInt() == 0) {
                    status = js.get("Status").getAsBoolean();
                }
            }
            onRegisteredCompleted(status);
        }

    }
}

