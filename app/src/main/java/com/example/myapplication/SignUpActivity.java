package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SignUpActivity extends AppCompatActivity {
    ImageView s_card_icon;
    ImageView p_picture_icon;
    Button select_s_card;
    Button select_p_picture;
    Button sign_up_okay;

    private Boolean isPermission = true;
    private static final String TAG = "EveryTaxi";
    private static final int PICK_FROM_ALBUM = 1;
    private File s_card_tempFile;
    private File p_picture_tempFile;
    int select = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);
        setTitle("회원가입");

        tedPermission();

        select_s_card = (Button) findViewById(R.id.select_s_card);
        select_p_picture = (Button) findViewById(R.id.select_p_picture);
        sign_up_okay = (Button) findViewById(R.id.sign_up_okay);

        select_s_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                select=0;
                goToAlbum();
            }
        });
        select_p_picture.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                select=1;
                goToAlbum();
            }
        });

        sign_up_okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // File s_card_file = new File();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();
            if(select==0){
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

    /**
     *  앨범에서 이미지 가져오기
     */
    private void goToAlbum() {

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }

    /**
     *  폴더 및 파일 만들기
     */
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

    /**
     *  tempFile 을 bitmap 으로 변환 후 ImageView 에 설정한다.
     */
    private void setImage() {
        if(select==0){
            s_card_icon = findViewById(R.id.s_card_icon);

            BitmapFactory.Options options = new BitmapFactory.Options();
            Bitmap originalBm = BitmapFactory.decodeFile(s_card_tempFile.getAbsolutePath(), options);
            Log.d(TAG, "setImage : " + s_card_tempFile.getAbsolutePath());

            s_card_icon.setImageBitmap(originalBm);

            /**
             *  tempFile 사용 후 null 처리를 해줘야 합니다.
             *  (resultCode != RESULT_OK) 일 때 tempFile 을 삭제하기 때문에
             *  기존에 데이터가 남아 있게 되면 원치 않은 삭제가 이뤄집니다.
             */
            s_card_tempFile = null;
        }
        else{
            p_picture_icon = findViewById(R.id.p_picture_icon);

            BitmapFactory.Options options = new BitmapFactory.Options();
            Bitmap originalBm = BitmapFactory.decodeFile(p_picture_tempFile.getAbsolutePath(), options);
            Log.d(TAG, "setImage : " + p_picture_tempFile.getAbsolutePath());

            p_picture_icon.setImageBitmap(originalBm);

            /**
             *  tempFile 사용 후 null 처리를 해줘야 합니다.
             *  (resultCode != RESULT_OK) 일 때 tempFile 을 삭제하기 때문에
             *  기존에 데이터가 남아 있게 되면 원치 않은 삭제가 이뤄집니다.
             */
            p_picture_tempFile = null;
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

