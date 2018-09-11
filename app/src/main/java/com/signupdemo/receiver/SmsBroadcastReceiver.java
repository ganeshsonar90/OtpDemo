package com.signupdemo.receiver;

/**
 * Created by gboss on 11/09/18.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsMessage;
import android.util.Log;


/**
 * A broadcast receiver who listens for incoming SMS
 */

public class SmsBroadcastReceiver extends BroadcastReceiver {

  private static final String TAG = "SmsBroadcastReceiver";

  private String serviceProviderNumber = "IX-INYELO";
  private String serviceProviderSmsCondition = "Greetings from YELO";
  //Greetings from YELO!Your OTP for Mobile login is 6167


  public SmsBroadcastReceiver() {
  }

  public SmsBroadcastReceiver(String serviceProviderNumber, String serviceProviderSmsCondition) {
    this.serviceProviderNumber = serviceProviderNumber;
    this.serviceProviderSmsCondition = serviceProviderSmsCondition;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    if (intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)) {
      String smsSender = "";
      String smsBody = "";
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
          smsSender = smsMessage.getDisplayOriginatingAddress();
          smsBody += smsMessage.getMessageBody();
        }
      } else {
        Bundle smsBundle = intent.getExtras();
        if (smsBundle != null) {
          Object[] pdus = (Object[]) smsBundle.get("pdus");
          if (pdus == null) {
            // Display some error to the user
            Log.e(TAG, "SmsBundle had no pdus key");
            return;
          }
          SmsMessage[] messages = new SmsMessage[pdus.length];
          for (int i = 0; i < messages.length; i++) {
            messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
            smsBody += messages[i].getMessageBody();
          }
          smsSender = messages[0].getOriginatingAddress();
        }
      }

      if (smsSender.equalsIgnoreCase(serviceProviderNumber) && smsBody
          .startsWith(serviceProviderSmsCondition)) {
        // if (listener != null) {

        String code = getVerificationCode(smsBody);

        Intent myIntent = new Intent("otp");
        myIntent.putExtra("message", code);
        LocalBroadcastManager.getInstance(context).sendBroadcast(myIntent);
      }
    }
  }

  /**
   * Getting the OTP from sms message body
   * ':' is the separator of OTP from the message
   */
  private String getVerificationCode(String message) {

    //Greetings from YELO!Your OTP for Mobile login is 6167
    String code = null;
    try {
      code = message.replaceAll("[^0-9]", "");
    } catch (Exception e) {

    }
    return code;
  }


}
