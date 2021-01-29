package com.example.puzzleblock1;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.BaseColumns;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.PuzzleBlock1.MESSAGE";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
        TextView textView1 = findViewById(R.id.textView3);
        Intent intent1 = new Intent(this, DisplayPuzzle.class);
        startPuzzle(10000,textView1, intent1);

    }

    public void startPuzzle(int time, final TextView timerDisp, final Intent onFinish)
    {

        new CountDownTimer(time, 1000) {

            public void onTick(long millisUntilFinished) {
                timerDisp.setText("seconds remaining: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                timerDisp.setText("done!");
                startActivity(onFinish);
            }
        }.start();
    }

    public void sendMessage(View view) {
        Intent intent = new Intent(this, DisplayPuzzle.class);
        startActivity(intent);
    }

    public void createDatabase()
    {
        String appDataPath = this.getApplicationInfo().dataDir;

        File dbFolder = new File(appDataPath + "/databases");//Make sure the /databases folder exists
        dbFolder.mkdir();//This can be called multiple times.

        File dbFilePath = new File(appDataPath + "/databases/PuzzleDatabase.db");

        try {
            InputStream inputStream = this.getAssets().open("PuzzleDatabase.db");
            OutputStream outputStream = new FileOutputStream(dbFilePath);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer))>0)
            {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
        } catch (IOException e){
            //handle
        }
    }
}