package com.example.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class BoardActivity extends AppCompatActivity
{
    String src_name;
    Button make_room;
    String username;
    String make_room_url = "https://everytaxi95.cafe24.com/make_room.php";
    String result;
    String dest;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.board);

        Intent intent = getIntent();

        src_name = intent.getStringExtra("src_name");

        setTitle("출발지 : " + src_name);

        final String[] creator = {"TEST", "AFSDF", "ASDASD"};

        ListView board = (ListView) findViewById(R.id.board);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, creator);

        board.setAdapter(adapter);

        SharedPreferences sharedPreferences = getSharedPreferences("cookie",MODE_PRIVATE);

        username = sharedPreferences.getString("username", "");

        board.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                if (username.equals("")) // 로그인 하지 않은 상태
                {
                    Toast.makeText(BoardActivity.this, "로그인 후에 이용해주세요.", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(getApplicationContext(), SignInActivity.class);

                    startActivity(intent);

                    finish();
                }
                else
                {
                    Toast.makeText(BoardActivity.this, creator[position], Toast.LENGTH_SHORT).show();
                }
            }
        });

        make_room = (Button) findViewById(R.id.make_room_btn);

        make_room.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (username.equals("")) // 로그인 하지 않은 상태
                {
                    Toast.makeText(BoardActivity.this, "로그인 후에 이용해주세요.", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(getApplicationContext(), SignInActivity.class);

                    startActivity(intent);

                    finish();
                }
                else
                {
                    final String[] dest_list = getResources().getStringArray(R.array.dest_list);

                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(BoardActivity.this);

                    mBuilder.setTitle("목적지 선택");

                    mBuilder.setSingleChoiceItems(dest_list, -1, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            dest = dest_list[i];

                            Intent intent = getIntent();

                            src_name = intent.getStringExtra("src_name");

                            dialogInterface.dismiss();

                            /*
                            new Thread()
                            {
                                public void run()
                                {
                                    result = HttpMakeRoom(make_room_url, "", username, src_name, dest);
                                }
                            }.start();

                            */
                        }
                    });

                    AlertDialog mDialog = mBuilder.create();

                    mDialog.show();
                }
            }
        });
    }

    public String HttpMakeRoom(String urlString, String params, String username, String src, String dest)
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

            send_msg = "username=" + username + "&src=" + src + "&dest=" + dest;

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

                if (result.equals("Exist")) // 이미 들어가있는 방이 있는 경우
                {
                    SharedPreferences sharedPreferences = getSharedPreferences("cookie",MODE_PRIVATE);

                    SharedPreferences.Editor editor = sharedPreferences.edit();

                    editor.putString("username", username);

                    editor.putString("hello_msg", "Hello");

                    editor.commit();
                }
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
