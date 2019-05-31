package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class SignInActivity extends AppCompatActivity {
    Button sign_in_btn;
    Button sign_up_btn;

    private static final String TAG = "EveryTaxi";

    String root_url = "https://everytaxi95.cafe24.com";
    String sign_in_url = "https://everytaxi95.cafe24.com/sign_in_action.php";
    String username;
    String password;
    String result;
    TextView help_msg;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in);
        setTitle("로그인");

        SSLConnect ssl = new SSLConnect();
        ssl.postHttps(root_url, 1000, 1000);

        sign_up_btn = (Button) findViewById(R.id.sign_up_btn);
        sign_in_btn = (Button) findViewById(R.id.sign_in_btn);
        help_msg = (TextView) findViewById(R.id.sign_in_help_msg);

        sign_in_btn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                username = ((EditText) (findViewById(R.id.sign_in_username))).getText().toString();
                password = ((EditText) (findViewById(R.id.sign_in_password))).getText().toString();

                if (username.length() == 0 || password.length() == 0)
                {
                    Toast.makeText(SignInActivity.this, "아이디와 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    new Thread() {
                        public void run()
                        {
                            result = HttpSignIn(sign_in_url, "", username, password);

                            Log.d(TAG, "Login Result : " + result);

                            if (result.equals("Success"))
                            {
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);

                                startActivity(intent);
                            }
                            else if (result.equals("Connection Error"))
                            {
                                help_msg.setText("서버와의 연결에 문제가 발생했습니다.");
                            }
                            else if (result.equals("No Validation"))
                            {
                                help_msg.setText("아직 인증되지 않은 계정입니다. 잠시 후 시도해주세요.");
                            }
                            else if (result.equals("Wrong Input"))
                            {
                                help_msg.setText("아이디 또는 비밀번호 오류입니다.");
                            }
                            else
                            {
                                help_msg.setText("오류가 발생했습니다. 다시 시도해주세요.");
                            }
                        }
                    }.start();
                }
            }
        });

        sign_up_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);

                startActivity(intent);
            }
        });
    }

    public String HttpSignIn(String urlString, String params, String username, String password)
    {
        String send_msg = "";
        String tmp_str = "";
        String result = "";

        try
        {
            URL connectUrl = new URL(urlString);

            HttpURLConnection conn = (HttpURLConnection) connectUrl.openConnection();

            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestMethod("POST");

            OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());

            send_msg = "username=" + username + "&password=" + password;

            osw.write(send_msg);
            osw.flush();

            if (conn.getResponseCode() == conn.HTTP_OK)
            {
                InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                BufferedReader reader = new BufferedReader(tmp);
                StringBuffer buffer = new StringBuffer();

                while ((tmp_str = reader.readLine()) != null)
                {
                    buffer.append(tmp_str);
                }

                result = buffer.toString();

                Log.d(TAG, "Inner Result : " + result);
            }
            else
            {
                Log.i("통신 결과 : ", conn.getResponseCode() + " 에러");

                result = "Connection Error";
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();

            result = "Exception Error";
        }

        return result;
    }
}
