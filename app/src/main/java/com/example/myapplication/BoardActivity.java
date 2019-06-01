package com.example.myapplication;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class BoardActivity extends AppCompatActivity
{
    String src_name;
    Button make_room;
    String username;
    String root_url = "https://everytaxi95.cafe24.com";
    String show_room_url = "https://everytaxi95.cafe24.com/show_room.php";
    String make_room_url = "https://everytaxi95.cafe24.com/make_room.php";
    String show_user_url = "https://everytaxi95.cafe24.com/show_room_user.php";
    String image_url = "https://everytaxi95.cafe24.com/uploads/";
    String make_result;
    String dest;
    String room_list;
    String thread_room_number;
    String user_str;
    String[] user_arr;
    String[] room_arr;
    String[] tmp_adapter_room_arr;
    String[] tmp_room_number;
    String[] adapter_room_arr;
    String[] room_number;
    int room_number_index;
    int room_list_index;
    int delete_count;
    boolean value_find;
    boolean skip;
    View dlg;

    TextView[] tv = new TextView[4];
    ImageView[] img = new ImageView[4];
    String[] image_path = new String[4];
    Bitmap[] bitmap = new Bitmap[4];

    URL connect_url_0;
    URL connect_url_1;
    URL connect_url_2;
    URL connect_url_3;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.board);

        SSLConnect ssl = new SSLConnect();
        ssl.postHttps(root_url, 1000, 1000);

        Intent intent = getIntent();

        src_name = intent.getStringExtra("src_name");

        setTitle("출발지 : " + src_name);

        SharedPreferences sharedPreferences = getSharedPreferences("cookie",MODE_PRIVATE);

        Thread server_connection = new Thread() // 서버 연결 스레드
        {
            public void run()
            {
                synchronized (this)
                {
                    room_list = HttpShowRoom(show_room_url, "", src_name);

                    this.notify();
                }
            }
        };

        server_connection.start();

        synchronized (server_connection) // 스레드 작업 종료를 기다림
        {
            try
            {
                server_connection.wait();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        if (room_list.equals("No Result"))
        {
            adapter_room_arr = new String[1];

            adapter_room_arr[0] = "아직 방이 없습니다. 버튼을 눌러 방을 생성해보세요!";
        }
        else
        {
            room_arr = room_list.split(",");

            tmp_adapter_room_arr = new String[(room_arr.length) / 2];
            tmp_room_number = new String[(room_arr.length) / 2];

            for (int k = 0; k < (room_arr.length / 2); k++)
            {
                tmp_adapter_room_arr[k] = "INIT";
                tmp_room_number[k] = "INIT";
            }

            room_number_index = 0;
            room_list_index = 0;

            value_find = false;
            skip = false;

            for (int i = 0; i < room_arr.length; i++)
            {
                if (i % 2 == 0)
                {
                    value_find = Arrays.asList(tmp_room_number).contains(room_arr[i]);

                    if (value_find)
                    {
                        value_find = false;

                        skip = true;
                    }
                    else
                    {
                        tmp_room_number[room_number_index] = room_arr[i];
                        room_number_index = room_number_index + 1;
                    }
                }
                else
                {
                    if (skip)
                    {
                        skip = false;
                    }
                    else
                    {
                        tmp_adapter_room_arr[room_list_index] = src_name + " -> " + room_arr[i];
                        room_list_index = room_list_index + 1;
                    }
                }
            }

            delete_count = 0;

            for (int i = 0; i < tmp_room_number.length; i++)
            {
                if (tmp_room_number[i].equals("INIT"))
                {
                    delete_count++;
                }
            }

            adapter_room_arr = new String[tmp_adapter_room_arr.length - delete_count];
            room_number = new String[tmp_room_number.length - delete_count];

            for (int i = 0; i < (tmp_adapter_room_arr.length - delete_count); i++)
            {
                adapter_room_arr[i] = tmp_adapter_room_arr[i];
                room_number[i] = tmp_room_number[i];
            }
        }

        ListView board = (ListView) findViewById(R.id.board);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, adapter_room_arr);

        board.setAdapter(adapter);

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
                    SharedPreferences sharedPreferences = getSharedPreferences("cookie",MODE_PRIVATE);

                    SharedPreferences.Editor editor = sharedPreferences.edit();

                    editor.putString("room_number", room_number[position]);

                    editor.commit();

                    Thread show_user_connection = new Thread() // 유저 목록 받아오는 스레드
                    {
                        public void run()
                        {
                            synchronized (this)
                            {
                                SharedPreferences sharedPreferences = getSharedPreferences("cookie",MODE_PRIVATE);

                                thread_room_number = sharedPreferences.getString("room_number", "");

                                if (!thread_room_number.equals(""))
                                {
                                    user_str = HttpShowUser(show_user_url, "", thread_room_number);
                                }

                                this.notify();
                            }
                        }
                    };

                    show_user_connection.start();

                    synchronized (show_user_connection) // 스레드 작업 종료를 기다림
                    {
                        try
                        {
                            show_user_connection.wait();
                        }
                        catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }


                        Thread get_user_picture = new Thread() // 유저 사진 받아오는 스레드
                        {
                            public void run()
                            {
                                synchronized (this)
                                {
                                    try
                                    {
                                        user_arr = user_str.split(",");

                                        for (int i = 0; i < user_arr.length; i++)
                                        {
                                            image_path[i] = image_url + user_arr[i] + "_p_picture";
                                        }

                                        connect_url_0 = new URL(image_path[0]);
                                        connect_url_1 = new URL(image_path[1]);
                                        connect_url_2 = new URL(image_path[2]);
                                        connect_url_3 = new URL(image_path[3]);

                                        HttpURLConnection conn = (HttpURLConnection)connect_url_0.openConnection();
                                        conn.setDoInput(true);
                                        conn.connect();
                                        InputStream input_file = conn.getInputStream();
                                        bitmap[0] = BitmapFactory.decodeStream(input_file);
                                        conn.disconnect();

                                        conn = (HttpURLConnection)connect_url_1.openConnection();
                                        conn.setDoInput(true);
                                        conn.connect();
                                        input_file = conn.getInputStream();
                                        bitmap[1] = BitmapFactory.decodeStream(input_file);
                                        conn.disconnect();

                                        conn = (HttpURLConnection)connect_url_2.openConnection();
                                        conn.setDoInput(true);
                                        conn.connect();
                                        input_file = conn.getInputStream();
                                        bitmap[2] = BitmapFactory.decodeStream(input_file);
                                        conn.disconnect();

                                        conn = (HttpURLConnection)connect_url_3.openConnection();
                                        conn.setDoInput(true);
                                        conn.connect();
                                        input_file = conn.getInputStream();
                                        bitmap[3] = BitmapFactory.decodeStream(input_file);
                                        conn.disconnect();
                                    }
                                    catch (Exception e)
                                    {
                                        e.printStackTrace();
                                    }

                                    this.notify();
                                }
                            }
                        };

                        get_user_picture.start();

                        synchronized (get_user_picture) // 스레드 작업 종료를 기다림
                        {
                            try
                            {
                                get_user_picture.wait();
                            }
                            catch (InterruptedException e)
                            {
                                e.printStackTrace();
                            }

                            /*

                            sharedPreferences = getSharedPreferences("cookie",MODE_PRIVATE);

                            thread_room_number = sharedPreferences.getString("room_number", "");

                            */

                            dlg = (View) View.inflate(BoardActivity.this, R.layout.user_list_dlg, null);

                            AlertDialog.Builder dlg_builder = new AlertDialog.Builder(BoardActivity.this);

                            dlg_builder.setTitle("참가자 목록");
                            dlg_builder.setIcon(R.mipmap.ic_launcher);
                            dlg_builder.setView(dlg);
                            dlg_builder.setPositiveButton("참가", null);
                            dlg_builder.setNegativeButton("취소", null);

                            tv[0] = dlg.findViewById(R.id.tv1);
                            tv[1] = dlg.findViewById(R.id.tv2);
                            tv[2] = dlg.findViewById(R.id.tv3);
                            tv[3] = dlg.findViewById(R.id.tv4);

                            img[0] = dlg.findViewById(R.id.img1);
                            img[1] = dlg.findViewById(R.id.img2);
                            img[2] = dlg.findViewById(R.id.img3);
                            img[3] = dlg.findViewById(R.id.img4);

                            for (int i = 0; i < user_arr.length; i++)
                            {
                                tv[i].setText(user_arr[i]);
                                img[i].setImageBitmap(bitmap[i]);
                            }

                            dlg_builder.show();
                        }
                    }
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

                            Thread make_room_connection = new Thread() // 방 만들기 스레드
                            {
                                public void run()
                                {
                                    synchronized (this)
                                    {
                                        make_result = HttpMakeRoom(make_room_url, "", username, src_name, dest);

                                        this.notify();
                                    }
                                }
                            };

                            make_room_connection.start();

                            synchronized (make_room_connection) // 스레드 작업 종료를 기다림
                            {
                                try
                                {
                                    make_room_connection.wait();
                                }
                                catch (InterruptedException e)
                                {
                                    e.printStackTrace();
                                }

                                if (make_result.equals("Exist"))
                                {
                                    Toast.makeText(BoardActivity.this, "이미 속해있는 방이 있습니다.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });

                    AlertDialog mDialog = mBuilder.create();

                    mDialog.show();
                }
            }
        });
    }

    public String HttpShowRoom(String urlString, String params, String src)
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

            send_msg = "src=" + src;

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

    public String HttpShowUser(String urlString, String params, String room_number)
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

            send_msg = "room_number=" + room_number;

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