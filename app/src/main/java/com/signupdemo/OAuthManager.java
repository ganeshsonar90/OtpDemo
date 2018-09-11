package com.signupdemo;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.signupdemo.app.Config;
import com.signupdemo.app.MyApplication;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by gboss on 11/09/18.
 */

public class OAuthManager {

  public static final String TAG = OAuthManager.class.getSimpleName();

  protected OAuthManager() {
  }

  private static OAuthManager sInstance;

  public static synchronized OAuthManager getInstance() {
    if (sInstance == null) {
      sInstance = new OAuthManager();
    }
    return sInstance;
  }


  /**
   * Posting the OTP to server and activating the user
   *
   * @param mobile otp received in the SMS
   */
  public void verifyOtp(final Context context, final String mobile,
      final AccountActionCallback callback) {

    StringRequest strReq = new StringRequest(Method.GET,
        Config.URL_REQUEST_SMS + "?mobile=" + mobile, new Response.Listener<String>() {

      @Override
      public void onResponse(String response) {
        Log.d(TAG, response.toString());

        if (callback != null) {
          callback.onSuccess(response);
        }

      }
    }, new Response.ErrorListener() {

      @Override
      public void onErrorResponse(VolleyError error) {
        if (error != null) {
          Log.w(TAG, error.toString());
          Throwable cause = error.getCause();
          if (cause != null) {
            cause.printStackTrace();
          }
          if (callback != null) {
            if (error.networkResponse != null
                && error.networkResponse.statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
              Log.w(TAG, "login error response unauthorized");
              callback.onFailed(ResponseCode.AUTHENTICATION_ERR);
            } else {
              callback.onFailed(ResponseCode.CONNECTION_ERR);
            }
          }
        }
      }
    }) {


    };

    // Adding request to request queue
    MyApplication.getInstance().addToRequestQueue(strReq);
  }


  /**
   * Method initiates the SMS request on the server
   *
   * @param name user name
   * @param email user email address
   * @param mobile user valid mobile number
   */
  private void requestForSMS(final Context context, final String name, final String email,
      final String mobile) {

    StringRequest strReq = new StringRequest(Method.POST,
        Config.URL_REQUEST_SMS, new Listener<String>() {

      @Override
      public void onResponse(String response) {
        Log.d(TAG, response.toString());

        try {
          JSONObject responseObj = new JSONObject(response);

          // Parsing json object response
          // response will be a json object
          boolean error = responseObj.getBoolean("error");
          String message = responseObj.getString("message");

          // checking for error, if not error SMS is initiated
          // device should receive it shortly
          if (!error) {
            // boolean flag saying device is waiting for sms

            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

          } else {
            Toast.makeText(context,
                "Error: " + message,
                Toast.LENGTH_LONG).show();
          }


        } catch (JSONException e) {
          Toast.makeText(context,
              "Error: " + e.getMessage(),
              Toast.LENGTH_LONG).show();


        }

      }
    }, new ErrorListener() {

      @Override
      public void onErrorResponse(VolleyError error) {
        Log.e(TAG, "Error: " + error.getMessage());
        Toast.makeText(context,
            error.getMessage(), Toast.LENGTH_SHORT).show();

      }
    }) {

      /**
       * Passing user parameters to our server
       * @return
       */
      @Override
      protected Map<String, String> getParams() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("name", name);
        params.put("email", email);
        params.put("mobile", mobile);

        Log.e(TAG, "Posting params: " + params.toString());

        return params;
      }

    };

    int socketTimeout = 60000;
    RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
    strReq.setRetryPolicy(policy);

    // Adding request to request queue
    MyApplication.getInstance().addToRequestQueue(strReq);
  }


  public interface AccountActionCallback {

    public void onSuccess(String response);

    public void onFailed(int/* ResponseCode */errorCode);

  }

  public interface ResponseCode {

    int SUCCESS = 0;
    int CONNECTION_ERR = -1;
    int AUTHENTICATION_ERR = -2;
    int INITIALIZE_ERR = -3;
  }

}
