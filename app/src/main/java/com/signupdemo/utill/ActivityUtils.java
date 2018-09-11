package com.signupdemo.utill;

import android.Manifest.permission;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog.Builder;
import com.signupdemo.R;

/**
 * Created by gboss on 11/09/18.
 */


public class ActivityUtils {

  private static final int SMS_PERMISSION_CODE = 0;

  /**
   * Optional informative alert dialog to explain the user why the app needs the Read/Send SMS
   * permission
   */
  public static void showRequestPermissionsInfoAlertDialog(final Context context) {
    Builder builder = new Builder(context);
    builder.setTitle(R.string.permission_alert_dialog_title);
    builder.setMessage(R.string.permission_dialog_message);
    builder.setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
        requestReadAndSendSmsPermission(context);
      }
    });
    builder.show();
  }


  /**
   * Runtime permission shenanigans
   */
  public static boolean hasReadSmsPermission(Context context) {
    return ContextCompat.checkSelfPermission(context,
        permission.READ_SMS) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(context,
            permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED;
  }

  public static void requestReadAndSendSmsPermission(Context context) {
    if (ActivityCompat
        .shouldShowRequestPermissionRationale((Activity) context, permission.READ_SMS)) {
      //   Log.d(TAG, "shouldShowRequestPermissionRationale(), no permission requested");
      return;
    }
    ActivityCompat.requestPermissions((Activity) context,
        new String[]{permission.READ_SMS, permission.RECEIVE_SMS},
        SMS_PERMISSION_CODE);
  }


}
