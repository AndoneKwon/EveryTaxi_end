package com.example.myapplication;

import android.Manifest;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
        , OnMapReadyCallback
{
    String username;
    String hello_msg;
    String src_name;
    NavigationView sidebar;
    Menu sidebar_menu;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("EveryTaxi");

        SharedPreferences sharedPreferences = getSharedPreferences("cookie",MODE_PRIVATE);

        hello_msg = sharedPreferences.getString("hello_msg", "");

        username = sharedPreferences.getString("username", "");

        if (hello_msg.equals("Hello"))
        {
            Toast.makeText(this, username + "님 환영합니다!", Toast.LENGTH_SHORT).show();

            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.remove("hello_msg");

            editor.commit();
        }

        sidebar = (NavigationView) findViewById(R.id.nav_view);

        sidebar_menu = sidebar.getMenu();

        if (username == "") // 로그인 하지 않은 상태
        {
            sidebar_menu.findItem(R.id.logout_menu).setVisible(false); // 로그아웃 제거
        }
        else
        {
            sidebar_menu.findItem(R.id.sign_in_menu).setVisible(false); // 로그인 제거
            sidebar_menu.findItem(R.id.sign_up_menu).setVisible(false); // 회원가입 제거
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)) {

            }
            else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);
            }
        }

        FragmentManager fragmentManager = getFragmentManager();
        MapFragment mapFragment = (MapFragment)fragmentManager
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.sign_in_menu)
        {
            Intent intent = new Intent(getApplicationContext(), SignInActivity.class);

            startActivity(intent);
        }
        else if (id == R.id.sign_up_menu)
        {
            Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);

            startActivity(intent);
        }
        else if (id == R.id.logout_menu)
        {
            SharedPreferences sharedPreferences = getSharedPreferences("cookie",MODE_PRIVATE);

            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.remove("username");

            editor.commit();

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);

            startActivity(intent);

            finish();
        }
        else if (id == R.id.test)
        {
            Intent intent = new Intent(getApplicationContext(), MessageActivity.class);

            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    @Override
    public void onMapReady(final GoogleMap map) {

        LatLng Gongneung = new LatLng(37.625593, 127.073196);
        LatLng Hagye = new LatLng(37.636088, 127.068692);
        LatLng Seokgye = new LatLng(37.615198, 127.066089);
        LatLng Taerung = new LatLng(37.617666, 127.075463);

        MarkerOptions markerOptions_G = new MarkerOptions();
        markerOptions_G.position(Gongneung);
        markerOptions_G.title("공릉역");
        map.addMarker(markerOptions_G);

        MarkerOptions markerOptions_H = new MarkerOptions();
        markerOptions_H.position(Hagye);
        markerOptions_H.title("하계역");
        map.addMarker(markerOptions_H);

        MarkerOptions markerOptions_S = new MarkerOptions();
        markerOptions_S.position(Seokgye);
        markerOptions_S.title("석계역");
        map.addMarker(markerOptions_S);

        MarkerOptions markerOptions_T = new MarkerOptions();
        markerOptions_T.position(Taerung);
        markerOptions_T.title("태릉입구역");
        map.addMarker(markerOptions_T);

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener()
        {
            @Override
            public boolean onMarkerClick(Marker marker)
            {
                src_name = marker.getTitle();

                Intent intent = new Intent(getApplicationContext(), BoardActivity.class);

                intent.putExtra("src_name", src_name);

                startActivity(intent);

                return false;
            }
        });
        map.moveCamera(CameraUpdateFactory.newLatLng(Gongneung));
        map.animateCamera(CameraUpdateFactory.zoomTo(14));
    }
}
