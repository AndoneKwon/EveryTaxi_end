package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SignUpActivity extends AppCompatActivity
{
    ImageView s_card_icon;
    ImageView p_picture_icon;
    Button select_s_card;
    Button select_p_picture;
    Button sign_up_okay;

    private Boolean isPermission = true;
    private static final String TAG = "EveryTaxi";
    private static final int PICK_FROM_ALBUM = 1;
    private File s_card_tempFile = null;
    private File p_picture_tempFile = null;
    int select = 0;

    String root_url = "https://everytaxi95.cafe24.com";
    String register_url = "https://everytaxi95.cafe24.com/register_action.php";
    String file_upload_url = "https://everytaxi95.cafe24.com/file_upload_action.php";
    String s_number;
    String username;
    String password;
    String confirm;
    String result;
    TextView help_msg;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);
        setTitle("회원가입");

        SSLConnect ssl = new SSLConnect();
        ssl.postHttps(root_url, 1000, 1000);

        tedPermission();

        select_s_card = (Button) findViewById(R.id.select_s_card);
        select_p_picture = (Button) findViewById(R.id.select_p_picture);
        sign_up_okay = (Button) findViewById(R.id.sign_up_okay);
        help_msg = (TextView) findViewById(R.id.help_msg);

        select_s_card.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                select = 0;
                goToAlbum();
            }
        });

        select_p_picture.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v){
                select = 1;
                goToAlbum();
            }
        });

        sign_up_okay.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                s_number = ((EditText) (findViewById(R.id.s_number))).getText().toString();
                username = ((EditText) (findViewById(R.id.username))).getText().toString();
                password = ((EditText) (findViewById(R.id.password))).getText().toString();
                confirm = ((EditText) (findViewById(R.id.confirm))).getText().toString();

                if (s_number.length() == 0 || username.length() == 0 || password.length() == 0 || confirm.length() == 0)
                {
                    Toast.makeText(SignUpActivity.this, "빈칸을 모두 작성해주세요.", Toast.LENGTH_SHORT).show();
                }
                else if (!password.equals(confirm))
                {
                    Toast.makeText(SignUpActivity.this, "비밀번호와 확인이 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                }
                else if (s_card_tempFile == null || p_picture_tempFile == null)
                {
                    Toast.makeText(SignUpActivity.this, "사진을 등록해주세요.", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    new Thread() {
                        public void run()
                        {
                            result = HttpRegister(register_url, "", s_number, username, password);

                            Log.d(TAG, "Register Result : " + result);

                            if (result.equals("Success"))
                            {
                                DoFileUpload_s_card(file_upload_url, s_card_tempFile.getAbsolutePath());

                                DoFileUpload_p_picture(file_upload_url, p_picture_tempFile.getAbsolutePath());

                                Intent intent = new Intent(getApplicationContext(), SignInActivity.class);

                                startActivity(intent);

                                finish();
                            }
                            else if (result.equals("Duplicate"))
                            {
                                help_msg.setText("이미 사용 중인 아이디입니다.");
                            }
                            else if (result.equals("Connection Error"))
                            {
                                help_msg.setText("서버와의 연결에 문제가 발생했습니다.");
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
    }

    public String HttpRegister(String urlString, String params, String s_number, String username, String password)
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

            send_msg = "s_number=" + s_number + "&username=" + username + "&password=" + password;

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

                if (result.equals("Success"))
                {
                    SharedPreferences sharedPreferences = getSharedPreferences("cookie",MODE_PRIVATE);

                    SharedPreferences.Editor editor = sharedPreferences.edit();

                    editor.putString("sign_up_complete", "Complete");

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

    public void DoFileUpload_s_card(String apiUrl, String absolutePath)
    {
        HttpFileUpload_s_card(apiUrl, "", absolutePath);
    }

    public void HttpFileUpload_s_card(String urlString, String params, String fileName)
    {
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        String file_save_name;

        username = ((EditText) (findViewById(R.id.username))).getText().toString();

        file_save_name = username + "_s_card";

        try
        {
            File sourceFile = new File(fileName);
            DataOutputStream dos;

            if (!sourceFile.isFile())
            {
                Log.e("uploadFile", "Source File not exist :" + fileName);
            }
            else
            {
                FileInputStream mFileInputStream = new FileInputStream(sourceFile);

                URL connectUrl = new URL(urlString);

                HttpURLConnection conn = (HttpURLConnection) connectUrl.openConnection();

                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);

                dos = new DataOutputStream(conn.getOutputStream());
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + file_save_name + "\"" + lineEnd);
                dos.writeBytes(lineEnd);

                int bytesAvailable = mFileInputStream.available();
                int maxBufferSize = 1024 * 1024;
                int bufferSize = Math.min(bytesAvailable, maxBufferSize);

                byte[] buffer = new byte[bufferSize];
                int bytesRead = mFileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0)
                {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = mFileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = mFileInputStream.read(buffer, 0, bufferSize);
                }

                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                mFileInputStream.close();
                dos.flush();

                if (conn.getResponseCode() == 200)
                {
                    InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                    BufferedReader reader = new BufferedReader(tmp);
                    StringBuffer stringBuffer = new StringBuffer();
                    String line;

                    line = reader.readLine();
                }

                mFileInputStream.close();

                dos.close();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void DoFileUpload_p_picture(String apiUrl, String absolutePath)
    {
        HttpFileUpload_p_picture(apiUrl, "", absolutePath);
    }

    public void HttpFileUpload_p_picture(String urlString, String params, String fileName)
    {
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        String file_save_name;

        username = ((EditText) (findViewById(R.id.username))).getText().toString();

        file_save_name = username + "_p_picture";

        try
        {
            File sourceFile = new File(fileName);
            DataOutputStream dos;

            if (!sourceFile.isFile())
            {
                Log.e("uploadFile", "Source File not exist :" + fileName);
            }
            else
            {
                FileInputStream mFileInputStream = new FileInputStream(sourceFile);

                URL connectUrl = new URL(urlString);

                HttpURLConnection conn = (HttpURLConnection) connectUrl.openConnection();

                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);

                dos = new DataOutputStream(conn.getOutputStream());
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" + file_save_name + "\"" + lineEnd);
                dos.writeBytes(lineEnd);

                int bytesAvailable = mFileInputStream.available();
                int maxBufferSize = 1024 * 1024;
                int bufferSize = Math.min(bytesAvailable, maxBufferSize);

                byte[] buffer = new byte[bufferSize];
                int bytesRead = mFileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0)
                {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = mFileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = mFileInputStream.read(buffer, 0, bufferSize);
                }

                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                mFileInputStream.close();
                dos.flush();

                if (conn.getResponseCode() == 200)
                {
                    InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                    BufferedReader reader = new BufferedReader(tmp);
                    StringBuffer stringBuffer = new StringBuffer();
                    String line;

                    line = reader.readLine();
                }

                mFileInputStream.close();

                dos.close();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();
            if(select == 0){
                if(s_card_tempFile != null) {
                    if (s_card_tempFile.exists()) {
                        if (s_card_tempFile.delete()) {
                            Log.e(TAG, s_card_tempFile.getAbsolutePath() + " 삭제 성공");
                            s_card_tempFile = null;
                        }
                    }
                }
            }
            else{
                if(p_picture_tempFile != null) {
                    if (p_picture_tempFile.exists()) {
                        if (p_picture_tempFile.delete()) {
                            Log.e(TAG, p_picture_tempFile.getAbsolutePath() + " 삭제 성공");
                            p_picture_tempFile = null;
                        }
                    }
                }
            }
            return;
        }

        else {

            Uri photoUri = data.getData();
            Log.d(TAG, "PICK_FROM_ALBUM photoUri : " + photoUri);

            Cursor cursor = null;

            try {

                String[] proj = { MediaStore.Images.Media.DATA };

                assert photoUri != null;
                cursor = getContentResolver().query(photoUri, proj, null, null, null);

                assert cursor != null;
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

                cursor.moveToFirst();

                if (select == 0)
                {
                    s_card_tempFile = new File(cursor.getString(column_index));
                    Log.d(TAG, "s_card_tempFile Uri : " + Uri.fromFile(s_card_tempFile));
                }
                else
                {
                    p_picture_tempFile = new File(cursor.getString(column_index));
                    Log.d(TAG, "p_picture_tempFile Uri : " + Uri.fromFile(p_picture_tempFile));
                }

            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }

            setImage();

        }
    }

    private void goToAlbum() {

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }

    private File createImageFile() throws IOException {

        String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
        String imageFileName = "blackJin_" + timeStamp + "_";

        File storageDir = new File(Environment.getExternalStorageDirectory() + "/blackJin/");
        if (!storageDir.exists()) storageDir.mkdirs();

        // 파일 생성
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        Log.d(TAG, "createImageFile : " + image.getAbsolutePath());

        return image;
    }

    private void setImage() {
        if(select==0){
            s_card_icon = findViewById(R.id.s_card_icon);

            BitmapFactory.Options options = new BitmapFactory.Options();
            Bitmap originalBm = BitmapFactory.decodeFile(s_card_tempFile.getAbsolutePath(), options);
            Log.d(TAG, "setImage : " + s_card_tempFile.getAbsolutePath());

            s_card_icon.setImageBitmap(originalBm);
        }
        else{
            p_picture_icon = findViewById(R.id.p_picture_icon);

            BitmapFactory.Options options = new BitmapFactory.Options();
            Bitmap originalBm = BitmapFactory.decodeFile(p_picture_tempFile.getAbsolutePath(), options);
            Log.d(TAG, "setImage : " + p_picture_tempFile.getAbsolutePath());

            p_picture_icon.setImageBitmap(originalBm);
        }
    }

    private void tedPermission() {

        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                // 권한 요청 성공
                isPermission = true;

            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                // 권한 요청 실패
                isPermission = false;

            }
        };

        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setRationaleMessage(getResources().getString(R.string.permission_2))
                .setDeniedMessage(getResources().getString(R.string.permission_1))
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .check();
    }
}

