package com.signupdemo.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.signupdemo.R;
import com.signupdemo.helper.PrefManager;
import com.signupdemo.utill.ActivityUtils;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

  @BindView(R.id.toolbar)
  Toolbar mToolbar;
  @BindView(R.id.name)
  TextView mName;
  @BindView(R.id.email)
  TextView mEmail;
  @BindView(R.id.mobile)
  TextView mMobile;


  private PrefManager mpref;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);

    if (!ActivityUtils.hasReadSmsPermission(this)) {
      ActivityUtils.showRequestPermissionsInfoAlertDialog(this);
    }
    // enabling toolbar
    setSupportActionBar(mToolbar);
    getSupportActionBar().setDisplayShowHomeEnabled(true);

    mpref = new PrefManager(getApplicationContext());

    // Displaying user information from shared preferences
    HashMap<String, String> profile = mpref.getUserDetails();
    mName.setText("Name: " + profile.get("name"));
    mEmail.setText("Email: " + profile.get("email"));
    mMobile.setText("Mobile: " + profile.get("mobile"));
  }


}
