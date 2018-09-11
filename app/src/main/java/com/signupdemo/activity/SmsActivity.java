package com.signupdemo.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Telephony.Sms.Intents;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.signupdemo.OAuthManager;
import com.signupdemo.OAuthManager.AccountActionCallback;
import com.signupdemo.R;
import com.signupdemo.helper.MyViewPager;
import com.signupdemo.helper.PrefManager;
import com.signupdemo.receiver.SmsBroadcastReceiver;
import com.signupdemo.utill.ActivityUtils;
import com.signupdemo.utill.CommonFunction;
import com.signupdemo.utill.NetworkUitils;
import org.json.JSONException;
import org.json.JSONObject;


public class SmsActivity extends AppCompatActivity implements OnClickListener {


  @BindView(R.id.inputName)
  EditText mInputName;
  @BindView(R.id.inputEmail)
  EditText mInputEmail;
  @BindView(R.id.edt_country_code)
  EditText mEdtCountryCode;
  @BindView(R.id.inputMobile)
  EditText mInputMobile;
  @BindView(R.id.btn_request_sms)
  Button mBtnRequestSms;
  @BindView(R.id.layout_sms)
  LinearLayout mLayoutSms;
  @BindView(R.id.inputOtp)
  EditText mInputOtp;
  @BindView(R.id.btn_verify_otp)
  Button mBtnVerifyOtp;
  @BindView(R.id.layout_otp)
  LinearLayout mLayoutOtp;
  @BindView(R.id.viewPagerVertical)
  MyViewPager mViewPagerVertical;
  @BindView(R.id.progressBar)
  ProgressBar mProgressBar;
  @BindView(R.id.txt_edit_mobile)
  TextView mTxtEditMobile;
  @BindView(R.id.btn_edit_mobile)
  ImageButton mBtnEditMobile;
  @BindView(R.id.layout_edit_mobile)
  LinearLayout mLayoutEditMobile;
  @BindView(R.id.viewContainer)
  RelativeLayout mViewContainer;

  private ViewPagerAdapter mAdapter;
  private SmsBroadcastReceiver mSmsBroadcastReceiver;
  private BroadcastReceiver mReceiver;
  private PrefManager mpref;
  private static String TAG = SmsActivity.class.getSimpleName();


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_sms);
    ButterKnife.bind(this);

    if (!ActivityUtils.hasReadSmsPermission(this)) {
      ActivityUtils.showRequestPermissionsInfoAlertDialog(this);
    }
    intializeView();
    registerSmsRecivier();


  }


  @Override
  public void onResume() {
    LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("otp"));
    super.onResume();
  }


  @Override
  protected void onDestroy() {
    super.onDestroy();
    unregisterReceiver(mSmsBroadcastReceiver);
    LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
  }

  @Override
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.btn_request_sms:
        validatingCountryAndMobile();
        break;

      case R.id.btn_verify_otp:
        // validateForm();
        //requestForSMS()
        break;

      case R.id.btn_edit_mobile:
        mViewPagerVertical.setCurrentItem(0);
        mLayoutEditMobile.setVisibility(View.GONE);
        mpref.setIsWaitingForSms(false);
        break;
    }
  }


  /**
   * intializeview
   */
  private void intializeView() {
    // view click listeners
    mBtnEditMobile.setOnClickListener(this);
    mBtnRequestSms.setOnClickListener(this);
    mBtnVerifyOtp.setOnClickListener(this);

    // hiding the edit mobile number
    mLayoutEditMobile.setVisibility(View.GONE);

    mpref = new PrefManager(this);

    // Checking for user session
    // if user is already logged in, take him to main activity
    if (mpref.isLoggedIn()) {
      Intent intent = new Intent(SmsActivity.this, MainActivity.class);
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
      startActivity(intent);

      finish();
    }

    mAdapter = new ViewPagerAdapter();
    mViewPagerVertical.setAdapter(mAdapter);
    mViewPagerVertical.setOnPageChangeListener(new OnPageChangeListener() {
      @Override
      public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
      }

      @Override
      public void onPageSelected(int position) {
      }

      @Override
      public void onPageScrollStateChanged(int state) {

      }
    });

    /**
     * Checking if the device is waiting for sms
     * showing the user OTP screen
     */
    if (mpref.isWaitingForSms()) {
      mViewPagerVertical.setCurrentItem(1);
      mLayoutEditMobile.setVisibility(View.VISIBLE);
    }

    mReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equalsIgnoreCase("otp")) {
          final String code = intent.getStringExtra("message");
          onOtpReceived(code);
          //Do whatever you want with the code here
        }
      }
    };
  }


  private void registerSmsRecivier() {
    mSmsBroadcastReceiver = new SmsBroadcastReceiver("9090", "YELO");
    registerReceiver(mSmsBroadcastReceiver,
        new IntentFilter(Intents.SMS_RECEIVED_ACTION));
  }


  private void validatingCountryAndMobile() {
    String mobile = mInputMobile.getText().toString().trim();
    String mobileCountryCode = mEdtCountryCode.getText().toString().trim();
    StringBuilder completeMobileNumber = new StringBuilder();
    completeMobileNumber.append(mobileCountryCode).append(mobile);

    // validating mobile number
    // it should be of 10 digits length
    if (CommonFunction.isValidPhoneNumber(mobile)) {
      handleOtp(completeMobileNumber.toString());
    }
  }


  private void handleOtp(String mobileNumber) {

    if (NetworkUitils.isNetworkAvailable(this)) {
      mProgressBar.setVisibility(View.VISIBLE);

      OAuthManager.getInstance().verifyOtp(this, mobileNumber,
          new AccountActionCallback() {
            @Override
            public void onSuccess(String response) {

              if (response != null && !response.isEmpty()) {

                Toast.makeText(SmsActivity.this, getString(R.string.success_otp),
                    Toast.LENGTH_LONG).show();


              } else {
                Toast
                    .makeText(getApplicationContext(), getString(R.string.err_otp_try),
                        Toast.LENGTH_SHORT)
                    .show();
              }

            }

            @Override
            public void onFailed(int errorCode) {
              Toast
                  .makeText(getApplicationContext(), getString(R.string.err_otp_try),
                      Toast.LENGTH_SHORT)
                  .show();
            }
          });
    } else {

      Toast
          .makeText(getApplicationContext(), getString(R.string.err_network), Toast.LENGTH_SHORT)
          .show();
    }
  }


   void onOtpReceived(String text) {

    if (text != null && !text.isEmpty()) {
      // boolean flag saying device is waiting for sms
      mpref.setIsWaitingForSms(true);

      // moving the screen to next pager item i.e otp screen
      mViewPagerVertical.setCurrentItem(1);
      mTxtEditMobile.setText(mpref.getMobileNumber());
      mLayoutEditMobile.setVisibility(View.VISIBLE);

      mInputOtp.setText(text);
      mInputOtp.setSelection(mInputOtp.length());
    } else {
      Toast.makeText(getApplicationContext(), "Error: " + getString(R.string.err_no_otp),
          Toast.LENGTH_LONG).show();
    }

  }


  class ViewPagerAdapter extends PagerAdapter {

    @Override
    public int getCount() {
      return 2;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
      return view == ((View) object);
    }

    public Object instantiateItem(View collection, int position) {

      int resId = 0;
      switch (position) {
        case 0:
          resId = R.id.layout_sms;
          break;
        case 1:
          resId = R.id.layout_otp;
          break;
      }
      return findViewById(resId);
    }
  }

}
