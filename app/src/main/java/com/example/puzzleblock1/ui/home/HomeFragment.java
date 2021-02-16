package com.example.puzzleblock1.ui.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.provider.SyncStateContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.puzzleblock1.BackgroundService;
import com.example.puzzleblock1.BlockingChoice;
import com.example.puzzleblock1.DisplayPuzzle;
import com.example.puzzleblock1.R;
import com.example.puzzleblock1.UserCreation;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.MODE_PRIVATE;
import static androidx.core.content.ContextCompat.startForegroundService;


public class HomeFragment extends Fragment {

    private static final int APP_PERMISSION_REQUEST = 1 ;
    public boolean timingActive;
    private HomeViewModel homeViewModel;
    public boolean breakCheck = false;
    public boolean livesCheck = false;
    public int appTime = 60;
    public String appTimeStr = "60";
    public TextView time;
    public TextView breakTimer;
    public BottomNavigationView bottomBar;

    public FloatingActionButton buttonUp;
    public FloatingActionButton buttonDown;
    public FloatingActionButton buttonStart;

    private static final String CHANNEL_ID = "Puzzle" ;
    public Timer backTimer = new Timer();
    public Intent backgroundCheckService;
    public int breakTimeInt;
    public String breakStr;




    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);


        checkUserInit();
        buttonUp = v.findViewById(R.id.upButton);
        buttonDown = v.findViewById(R.id.downButton);
        buttonStart = v.findViewById(R.id.startButton);


        backgroundCheckService = new Intent(getContext(), BackgroundService.class);
        backgroundCheckService.setAction("start");
        breakTimer = v.findViewById(R.id.breakTimer);
        time = v.findViewById(R.id.userTime);
        bottomBar = getActivity().findViewById(R.id.nav_view);
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);


        String startTxt = (appTimeStr + " Minutes");
        time.setText(startTxt);
        backTimer.cancel();
        buttonUp.setVisibility(View.VISIBLE);
        buttonDown.setVisibility(View.VISIBLE);
        buttonStart.setVisibility(View.VISIBLE);
//        if(!timingActive)
//        {
//
//
//        }
        buttonUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String timeNew = (timeChange(appTimeStr, "5",1) + " Minutes");
                time.setText(timeNew);
            }
        });

        buttonDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String timeNew = (timeChange(appTimeStr, "5",0) + " Minutes");
                time.setText(timeNew);
            }
        });


        buttonStart.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                appTime = appTime * 60000;
                buttonUp.setVisibility(View.GONE);
                buttonDown.setVisibility(View.GONE);
                buttonStart.setVisibility(View.GONE);
//                bottomBar.setVisibility(View.INVISIBLE);
                bottomBar.setVisibility(View.GONE);

//                SQLiteDatabase mydatabase = getActivity().openOrCreateDatabase("PuzzleDatabase.db",MODE_PRIVATE,null);
//                Cursor resultSet2 = mydatabase.rawQuery("UPDATE User SET timingActive = '1' WHERE userId=1",null);
//                resultSet2.moveToFirst();
                startTimer(appTime, time);

            }
        });



        return v;
    }

    public void checkUserInit() {
        SQLiteDatabase mydatabase = getActivity().openOrCreateDatabase("PuzzleDatabase.db",MODE_PRIVATE,null);
        Cursor resultSet = mydatabase.rawQuery("Select * from User WHERE userId=1",null);
        if(resultSet.getCount() == 0)
        {
            Intent newUser = new Intent(getContext(), UserCreation.class);
            startActivity(newUser);

            Intent newUser2 = new Intent (getContext(), BlockingChoice.class);
            startActivity(newUser2);
        } else if (resultSet.getCount() == 1)
        {
            resultSet.moveToFirst();
            breakStr = resultSet.getString(5);
            String lives = resultSet.getString(6);
            Integer livesInt = Integer.parseInt(lives);
            breakTimeInt = Integer.parseInt(breakStr);

            String breakChecker = resultSet.getString(4);
            int breakCheckerInt = Integer.parseInt(breakChecker);

            if(breakCheckerInt == 1)
            {
                Cursor resultSet2 = mydatabase.rawQuery("UPDATE User SET Break = '0' WHERE userId=1",null);
                resultSet2.moveToFirst();
            }
            if(livesInt != 3)
            {
                Cursor resultSet3 = mydatabase.rawQuery("UPDATE User SET Lives = '3' WHERE userId=1",null);
                resultSet3.moveToFirst();
            }
            Cursor resultSet4 = mydatabase.rawQuery("UPDATE User SET PuzzleActive = '0' WHERE userId=1",null);
            resultSet4.moveToFirst();




        }
        mydatabase.close();
    }

    public void checkUser() {
        SQLiteDatabase mydatabase = getActivity().openOrCreateDatabase("PuzzleDatabase.db",MODE_PRIVATE,null);
        Cursor resultSet = mydatabase.rawQuery("Select * from User WHERE userId=1",null);
        if(resultSet.getCount() == 0)
        {
            Intent newUser = new Intent(getContext(), UserCreation.class);
            startActivity(newUser);
        } else if (resultSet.getCount() == 1)
        {
            resultSet.moveToFirst();
            breakStr = resultSet.getString(5);
            breakTimeInt = Integer.parseInt(breakStr);
            System.out.println("This one " + breakStr);
        }
        mydatabase.close();
    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    public void startTimer(int time, final TextView timerDisp)
    {
        startService();
        displayInfo();

        new CountDownTimer(time, 1000) {
            int minute = 0;
            public void onTick(long millisUntilFinished) {

               if (minute == 0)
               {
                   timerDisp.setText("Time remaining: " + ((millisUntilFinished /60000) + 1) +" : 00");
                   minute = 59;
               }else if (minute < 10)
               {
                   timerDisp.setText("Time remaining: " + (millisUntilFinished /60000) +" : 0" + (minute));
                   minute = minute -1;
               }else{
                   timerDisp.setText("Time remaining: " + (millisUntilFinished /60000) +" : " + (minute));
                   minute = minute -1;
               }

            }

            public void onFinish() {
                timerDisp.setText("done!");
                Intent stopService = new Intent(getContext(), BackgroundService.class);
                stopService.setAction("stop");
                bottomBar.setVisibility(View.VISIBLE);
                String startTxt = (appTimeStr + " Minutes");
                timerDisp.setText(startTxt);
                backTimer.cancel();
                buttonUp.setVisibility(View.VISIBLE);
                buttonDown.setVisibility(View.VISIBLE);
                buttonStart.setVisibility(View.VISIBLE);
                appTime = 60;
                getActivity().startService(stopService);
                backTimer.cancel();
            }
        }.start();
    }


    public void displayInfo()
    {
        TimerTask backgroundChecker = new TimerTask() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {
                getBreak();
            }
        };
        backTimer = new Timer();
        backTimer.schedule(backgroundChecker,0, 5000 );
    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    public void startService(){
        getActivity().startForegroundService(backgroundCheckService);


//        TimerTask backgroundChecker = new TimerTask() {
//                @RequiresApi(api = Build.VERSION_CODES.O)
//                @Override
//                public void run() {
//                    getBreak();
//                }
//            };
//            backTimer = new Timer();
//            backTimer.schedule(backgroundChecker,0, 5000 );

    }

    public String timeChange(String initial, String addition, int posNeg)
    {
        Integer newTime;
        int timeIn = Integer.parseInt(initial);
        int timeadd = Integer.parseInt(addition);
        if(posNeg == 1)
        {
            newTime = timeIn + timeadd;
        }else
        {
            newTime = timeIn - timeadd;

        }
        appTimeStr = newTime.toString();
        appTime = newTime;
        return appTimeStr;
    }

    public void backgroundNotification(String message){
        Intent fullScreenIntent = new Intent(getActivity(), DisplayPuzzle.class);
        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(getActivity(), 0,
                fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(getContext(), CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_home_black_24dp)
                        .setContentTitle("Puzzle Time")
                        .setContentText(message)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_ALARM)
                        .setFullScreenIntent(fullScreenPendingIntent, true)
                        .setAutoCancel(true);

        Notification incomingCallNotification = notificationBuilder.build();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getContext());
        int notificationId = 1;
        notificationManager.notify(notificationId, incomingCallNotification);

    }



    public void getBreak()
    {
        checkUser();
        SQLiteDatabase mydatabase = getActivity().openOrCreateDatabase("PuzzleDatabase.db",MODE_PRIVATE,null);
        Cursor resultSet = mydatabase.rawQuery("Select * from User WHERE userId=1",null);
        resultSet.moveToFirst();
        String breaker = resultSet.getString(4);
        String lives = resultSet.getString(6);
        int livesInt = Integer.parseInt(lives);
        mydatabase.close();
        int breakTime = breakTimeInt * 60000;
        if(breaker.equals("1") && breakCheck == false)
        {
//            backTimer.cancel();
//            backgroundNotification((breakStr + " Minute Break Begun!"));
////            Toast.makeText(getContext(), (breakStr + " Minute Break Begun!"), Toast.LENGTH_SHORT).show();
            final int finalBreakTime = breakTime;
            breakCheck = true;
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    new CountDownTimer(finalBreakTime, 1000) {
                        int minute = 0;
                        boolean halfCheck = false;
                        public void onTick(long millisUntilFinished) {

                            if (minute == 0) {
                                breakTimer.setText("Break Time Remaining: " + ((millisUntilFinished / 60000) + 1) + " : 00");
                                minute = 59;
                            } else if (minute < 10) {
                                breakTimer.setText("Break Time Remaining: " + (millisUntilFinished / 60000) + " : 0" + (minute));
                                minute = minute - 1;
                            } else {
                                breakTimer.setText("Break Time Remaining: " + (millisUntilFinished / 60000) + " : " + (minute));
                                minute = minute - 1;
                            }
                            System.out.println("Hiii :" + (millisUntilFinished / 60000) + " | " + (breakTimeInt / 2)  );
                            if ((millisUntilFinished / 60000) == ((breakTimeInt / 2)-1) && !halfCheck) {
//                        Toast.makeText(getContext(), "Halfway Through Break!", Toast.LENGTH_SHORT).show();
//                                backgroundNotification(("Halfway Through Break!"));
                                halfCheck = true;
                            }

                        }

                        public void onFinish() {
                            breakTimer.setText(null);
                            breakCheck = false;

//                    Toast.makeText(getContext(), "Break Time Over!", Toast.LENGTH_SHORT).show();
//                            checkUserActivity();

                        }
                    }.start();
                }

            });
        }else if ((livesInt == 0) && (!livesCheck))
        {
            final int finalBreakTime = breakTime;
            livesCheck = true;
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    new CountDownTimer(finalBreakTime, 1000) {
                        int minute = 0;
                        boolean halfCheck = false;
                        public void onTick(long millisUntilFinished) {

                            if (minute == 0) {
                                breakTimer.setText("Try Again in: " + ((millisUntilFinished / 60000) + 1) + " : 00");
                                minute = 59;
                            } else if (minute < 10) {
                                breakTimer.setText("Try Again in: " + (millisUntilFinished / 60000) + " : 0" + (minute));
                                minute = minute - 1;
                            } else {
                                breakTimer.setText("Try Again in " + (millisUntilFinished / 60000) + " : " + (minute));
                                minute = minute - 1;
                            }
                            System.out.println("Hiii :" + (millisUntilFinished / 60000) + " | " + (breakTimeInt / 2)  );
                            if ((millisUntilFinished / 60000) == ((breakTimeInt / 2)-1) && !halfCheck) {
//                        Toast.makeText(getContext(), "Halfway Through Break!", Toast.LENGTH_SHORT).show();
//                                backgroundNotification(("Halfway Through Break!"));
                                halfCheck = true;
                            }

                        }

                        public void onFinish() {
                            breakTimer.setText(null);
                            breakCheck = false;

//                    Toast.makeText(getContext(), "Break Time Over!", Toast.LENGTH_SHORT).show();
//                            checkUserActivity();

                        }
                    }.start();
                }

            });
        }


    }


}