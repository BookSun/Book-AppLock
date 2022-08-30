/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.book.app.lock;

import android.app.Activity;
import android.app.Fragment;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import book.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import com.book.app.lock.utils.bookColorfulAdapter;
import com.book.app.lock.ChooseLockGeneric.ChooseLockGenericFragment;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import com.book.app.lock.ChooseLockPattern.ChooseLockPatternFragment.Stage;
import book.support.v7.app.ActionBarActivity;
public class ChooseLockPassword extends PreferenceActivity {
    public static final String PASSWORD_MIN_KEY = "lockscreen.password_min";
    public static final String PASSWORD_MAX_KEY = "lockscreen.password_max";
    public static final String PASSWORD_MIN_LETTERS_KEY = "lockscreen.password_min_letters";
    public static final String PASSWORD_MIN_LOWERCASE_KEY = "lockscreen.password_min_lowercase";
    public static final String PASSWORD_MIN_UPPERCASE_KEY = "lockscreen.password_min_uppercase";
    public static final String PASSWORD_MIN_NUMERIC_KEY = "lockscreen.password_min_numeric";
    public static final String PASSWORD_MIN_SYMBOLS_KEY = "lockscreen.password_min_symbols";
    public static final String PASSWORD_MIN_NONLETTER_KEY = "lockscreen.password_min_nonletter";
    public static Context mContext;

    @Override
    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(EXTRA_SHOW_FRAGMENT,
                ChooseLockPasswordFragment.class.getName());
        modIntent.putExtra(EXTRA_NO_HEADERS, true);
        return modIntent;
    }
    protected boolean isValidFragment(String fragmentName) {
        if (ChooseLockPasswordFragment.class.getName().equals(fragmentName))
            return true;
        return false;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CharSequence msg = getText(R.string.lockpassword_choose_your_password_header);
        showBreadCrumbs(msg, msg);
        // ActionBar actionBar = ((ActionBarActivity)getSupportActionBar());
        // actionBar.setTitle(R.string.unlock_set_unlock_pin_title);
        // actionBar.setDisplayHomeAsUpEnabled(true);
    }

    public static class ChooseLockPasswordFragment extends Fragment implements
            OnClickListener {
        private static final String KEY_FIRST_PIN = "first_pin";
        private static final String KEY_UI_STAGE = "ui_stage";
        private LockPatternUtils mLockPasswordUtils;
        private int mRequestedQuality = DevicePolicyManager.PASSWORD_QUALITY_NUMERIC;
        private ChooseLockSettingsHelper mChooseLockSettingsHelper;
        private String mFirstPin;
        private boolean mIsAlphaMode;
        private TextView mDeleteButton;
        private static final int CONFIRM_EXISTING_REQUEST = 58;
        static final int RESULT_FINISHED = RESULT_FIRST_USER;
        private static final long ERROR_MESSAGE_TIMEOUT = 200;
        private static final long TEXT_MESSAGE_TIMEOUT = 1000;
        private static final int MSG_SHOW_NEXT = 111;
        private static final int MSG_SHOW_DONE = 222;
        private static final int MSG_SHOW_CONFIRM = 333;
        private static final int MSG_SHOW_TEXT = 444;
        private String mBeforePassword = "";
        private String mEndPassword = "";
        private StringBuffer mBuffer = new StringBuffer();
        private ImageButton mViewF, mViewS, mViewT, mViewE;
        private TextView mTextView;
        private boolean mConfirmPass = false;
        private View mLayoutView;
        private Drawable mBackDrawable;
        private Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MSG_SHOW_NEXT) {
                    updateView();
                    passwordAnima();
                } else if (msg.what == MSG_SHOW_DONE) {
                    //passwordDone();
                } else if (msg.what == MSG_SHOW_CONFIRM) {
                    passwordConfirm();
                } else if (msg.what == MSG_SHOW_TEXT) {
                    showToastMsg(getString(R.string.sure_input_pwd));
                }
            }
        };
        public ChooseLockPasswordFragment() {

        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mLockPasswordUtils = new LockPatternUtils(getActivity());
            mChooseLockSettingsHelper = new ChooseLockSettingsHelper(
                    getActivity());
            Drawable pressedDrawable = getActivity().getResources().getDrawable(
                    R.drawable.view_back);
            mBackDrawable = bookColorfulAdapter.getColorDrawable(getActivity(),pressedDrawable);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {

            final View view = inflater.inflate(R.layout.choose_lock_password, null);
            NumberPasswordManager.getInstance().initView(view, getActivity(),
                    new NumberPasswordManager.OnNumberClickListener() {
                        @Override
                        public void clickedNumber(int number) {
                            // TODO Auto-generated method stub
                            Log.d("liuwenshuai", "clickedNumber:" + number);
                            mDeleteButton.setVisibility(View.VISIBLE);
                            mBeforePassword = mBeforePassword + number;
                            if (mConfirmPass) {
                                if (mBeforePassword.equals(mBuffer.toString())) {
                                    setPasswordView(mBeforePassword.length());

                                   mLockPasswordUtils.clearLock();
                                   mLockPasswordUtils.saveLockPassword(mBuffer.toString(),mRequestedQuality);
                                    mBeforePassword = "";
                                    getActivity().setResult(RESULT_FINISHED);
                                    getActivity().finish();
                                } else {
                                    if (mBeforePassword.length() == 4) {
                                        setPasswordView(mBeforePassword
                                                .length());
                                        mHandler.removeMessages(MSG_SHOW_CONFIRM);
                                        mHandler.sendEmptyMessageDelayed(
                                                MSG_SHOW_CONFIRM,
                                                ERROR_MESSAGE_TIMEOUT);
                                        return;
                                    }
                                }
                            }
                            setPasswordView(mBeforePassword.length());
                            if (mBeforePassword.length() == 4) {
                                mBuffer.append(mBeforePassword);
                                mHandler.removeMessages(MSG_SHOW_NEXT);
                                mHandler.sendEmptyMessageDelayed(MSG_SHOW_NEXT,
                                        ERROR_MESSAGE_TIMEOUT);
                            }
                        }
                    });
            initPasswordWidget(view);
            mDeleteButton = (TextView) view.findViewById(R.id.btn_delete);
            mDeleteButton.setOnClickListener(this);
            mLayoutView = (RelativeLayout) view
                    .findViewById(R.id.password_view);
            mIsAlphaMode = false;
            final Activity activity = getActivity();
            Intent intent = getActivity().getIntent();
            final boolean confirmCredentials = intent.getBooleanExtra(
                    "confirm_credentials", true);
            if (savedInstanceState == null) {
                if (confirmCredentials) {
                    mChooseLockSettingsHelper.launchConfirmationActivity(
                            CONFIRM_EXISTING_REQUEST, null, null);
                }
            }
            if (activity instanceof PreferenceActivity) {
                final PreferenceActivity PreferenceActivity = (PreferenceActivity) activity;
                int id = mIsAlphaMode ? R.string.lockpassword_choose_your_password_header
                        : R.string.lockpassword_choose_your_pin_header;
                CharSequence title = getText(id);
                PreferenceActivity.showBreadCrumbs(title, title);
            }

            return view;
        }

        @Override
        public void onResume() {
            super.onResume();
        }

        @Override
        public void onPause() {
            mHandler.removeMessages(MSG_SHOW_NEXT);
            getActivity().finish();
            super.onPause();
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode,
                Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            switch (requestCode) {
            case CONFIRM_EXISTING_REQUEST:
                if (resultCode != Activity.RESULT_OK) {
                    getActivity().setResult(RESULT_FINISHED);
                    getActivity().finish();
                }
                break;
            }
        }

        protected void updateView() {
            clearView();
            mBeforePassword = "";
            mTextView.setText(R.string.sure_input_pwd);
        }



        private void passwordConfirm() {
            showToastMsg(getString(R.string.password_not_equals));
            clearView();
            mBeforePassword = "";
            mHandler.removeMessages(MSG_SHOW_TEXT);
            mHandler.sendEmptyMessageDelayed(MSG_SHOW_TEXT,
                    TEXT_MESSAGE_TIMEOUT);
        }

        private void initPasswordWidget(View v) {
            mTextView = (TextView) v.findViewById(R.id.tv_info);
            mViewF = (ImageButton) v.findViewById(R.id.et_pwd1);
            mViewS = (ImageButton) v.findViewById(R.id.et_pwd2);
            mViewT = (ImageButton) v.findViewById(R.id.et_pwd3);
            mViewE = (ImageButton) v.findViewById(R.id.et_pwd4);
        }

        private void deleteView() {
            int length = mBeforePassword.length();
            if (length == 0)
                return;
            mBeforePassword = mBeforePassword.substring(0, length - 1);
            switch (length) {
            case 1:
                mViewF.setBackgroundResource(R.drawable.edit_bg);
                mDeleteButton.setVisibility(View.INVISIBLE);
                break;
            case 2:
                mViewS.setBackgroundResource(R.drawable.edit_bg);
                break;
            case 3:
                mViewT.setBackgroundResource(R.drawable.edit_bg);
                break;
            case 4:
                mViewE.setBackgroundResource(R.drawable.edit_bg);
                break;
            default:
                break;
            }
        }

        private void setPasswordView(int length) {
            if (mBackDrawable == null)
                return;
            switch (length) {
            case 1:
                mViewF.setBackground(mBackDrawable);
                break;
            case 2:
                mViewS.setBackground(mBackDrawable);
                break;
            case 3:
                mViewT.setBackground(mBackDrawable);
                break;
            case 4:
                mViewE.setBackground(mBackDrawable);
                mConfirmPass = true;
                break;
            default:
                break;
            }
        }

        private void clearView() {
            mViewF.setBackgroundResource(R.drawable.edit_bg);
            mViewS.setBackgroundResource(R.drawable.edit_bg);
            mViewT.setBackgroundResource(R.drawable.edit_bg);
            mViewE.setBackgroundResource(R.drawable.edit_bg);
            mDeleteButton.setVisibility(View.INVISIBLE);
        }


        private void showToastMsg(String text) {
            mTextView.setText(text);
        }

        private void passwordAnima() {
            layoutAnimation(getActivity(), mLayoutView);
        }

        private void layoutAnimation(Context context, View view) {
            TranslateAnimation translateAnim = (TranslateAnimation) AnimationUtils
                    .loadAnimation(context, R.anim.password_left_in);
            view.setAnimation(translateAnim);
        }

        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.btn_delete:
                deleteView();
                break;
            }
        }
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
