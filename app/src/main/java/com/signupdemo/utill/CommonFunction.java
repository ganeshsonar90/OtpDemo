package com.signupdemo.utill;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gboss on 11/09/18.
 */

public class CommonFunction {

  /**
   * Regex to validate the mobile number
   * mobile number should be of 10 digits length
   */
  public static boolean isValidPhoneNumber(String mobile) {
    String regEx = "^[0-9]{10}$";
    return mobile.matches(regEx);
  }


}
