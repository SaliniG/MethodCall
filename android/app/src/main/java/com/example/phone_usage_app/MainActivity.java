package com.example.phone_usage_app;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugins.GeneratedPluginRegistrant;

public class MainActivity extends FlutterActivity {
    private static final String CHANNEL = "flutter.native/helper";
    private static final String TAG = "Error";
    long total;
    String text;
    int limit;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS);
        startActivity(intent);

        Calendar c = new GregorianCalendar();
        c.set(Calendar.HOUR_OF_DAY, 0); //anything 0 - 23
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        Date startTime = c.getTime();
        GregorianCalendar gcal = new GregorianCalendar();
        Date endTime = gcal.getTime();

        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);

        Map<String, UsageStats> usageStatsMap = usageStatsManager.queryAndAggregateUsageStats(startTime.getTime(), endTime.getTime());
        HashMap<String, Double> usageMap = new HashMap<>();


        for (String packageName : usageStatsMap.keySet()) {
            UsageStats us = usageStatsMap.get(packageName);
            try {
                long timeMs = us.getTotalTimeInForeground();
                Double timeSeconds = new Double(timeMs / 1000);
                usageMap.put(packageName, timeSeconds);

                total = total + timeMs;

            } catch (Exception e) {
                Log.d(TAG, "Getting timeInForeground resulted in an exception");
            }
        }
        System.out.println(startTime);
        System.out.println(endTime);
        System.out.println(usageMap);
        System.out.println(total);
//        getDurationBreakdown(total);

    }

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);
        GeneratedPluginRegistrant.registerWith(flutterEngine);
        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL).setMethodCallHandler(

                new MethodChannel.MethodCallHandler() {
                    @Override
                    public void onMethodCall(MethodCall call, MethodChannel.Result result) {

                        if (call.method.equals("helloFromNativeCode")) {
                            String greetings = helloFromNativeCode();
                            result.success(greetings);
                        } else if (call.method.equals("_getlimitUsage")) {
                            text = call.argument("text");
                            limit = Integer.parseInt(text);

                            System.out.println("From flutter" + text);
                            if (text != null) {
                                result.success(text);

                            } else {
                                result.error("UNAVAILABLE", "Battery level not available.", null);
                            }
                        } else {
                            result.notImplemented();
                        }

                        localNotification();

                    }
                });

    }

    void localNotification() {
        int totalUsage = Integer.parseInt(getDurationBreakdown(total));
        System.out.println(totalUsage + ">" + limit);
        if (totalUsage > limit) {
           /* Intent intent = new Intent(this, MainActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder b = new NotificationCompat.Builder(this);
            System.out.println(totalUsage + ">" + limit);
            b.setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setWhen(System.currentTimeMillis())
                    .setTicker("Hearty365")
                    .setContentTitle("Default notification")
                    .setContentText("Lorem ipsum dolor sit amet, consectetur adipiscing elit.")
                    .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                    .setContentIntent(contentIntent)
                    .setContentInfo("Info");
            Toast.makeText(this, "haiiii", Toast.LENGTH_LONG);
            System.out.println(totalUsage + ">" + limit);

            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(1, b.build());*/
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setContentTitle("My notification")
                    .setContentText("Hello World!")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    // Set the intent that will fire when the user taps the notification
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

        }
    }

    private String helloFromNativeCode() {
        return getDurationBreakdown(total);
    }

    public static String getDurationBreakdown(long millis) {
        if (millis < 0) {
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }

        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);

        StringBuilder sb = new StringBuilder(64);
        sb.append(hours);
        /*sb.append(":");
        sb.append(minutes);*/
        System.out.println(sb.toString());

        return (sb.toString());

    }
}