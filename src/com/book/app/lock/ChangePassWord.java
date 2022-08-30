package com.book.app.lock;

import com.book.app.lock.provider.AppLockData;
import com.book.app.lock.provider.PreferencesProvider;
import book.support.v7.app.ActionBarActivity;
import book.support.v7.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.view.MenuItem;
import android.widget.Toast;

public class ChangePassWord extends PreferenceActivity {
    private static String mAppName;
    private static AppLockData mAppLockData;
    private static final int CONFIRM_EXISTING_REQUEST = 10003;

    @Override
    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(EXTRA_SHOW_FRAGMENT,
                ChangePassWordFragment.class.getName());
        modIntent.putExtra(EXTRA_NO_HEADERS, true);
        return modIntent;
    }

    protected boolean isValidFragment(String fragmentName) {
        if (ChangePassWordFragment.class.getName().equals(fragmentName))
            return true;
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        mAppLockData = new AppLockData(this);
        mAppName = PreferencesProvider
                .getDefaultPackage(getApplicationContext());
        // ActionBar actionBar = ((ActionBarActivity)getSupportActionBar());
        // actionBar.setTitle(R.string.manager_app_lock);
        // actionBar.setDisplayHomeAsUpEnabled(true);
        super.onCreate(savedInstanceState);
    }

    public static class ChangePassWordFragment extends PreferenceFragment {
        private ChooseLockSettingsHelper mChooseLockSettingsHelper;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            // TODO Auto-generated method stub
            super.onCreate(savedInstanceState);
            mChooseLockSettingsHelper = new ChooseLockSettingsHelper(
                    this.getActivity(), this);
            addPreferencesFromResource(R.xml.change_password);
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode,
                Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == CONFIRM_EXISTING_REQUEST
                    && resultCode == Activity.RESULT_OK) {
                mChooseLockSettingsHelper.utils().clearLock();
                mAppLockData.delete(mAppName);
                Toast.makeText(getActivity(), R.string.cancel_password,
                        Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                Preference preference) {
            // TODO Auto-generated method stub
            String keySetting = preference.getKey();
            if (keySetting.equals("change_password")) {
                Intent intent = new Intent();
                intent.putExtra("APP_PACKAGE_NAME", mAppName);
                intent.setClassName("com.book.app.lock",
                        "com.book.app.lock.ChooseLockGeneric");
                startActivity(intent);
            } else if (keySetting.equals("forget_password")) {
                mChooseLockSettingsHelper.launchConfirmationActivity(
                        CONFIRM_EXISTING_REQUEST, null, null);
            }
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
