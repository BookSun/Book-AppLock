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

import com.book.app.lock.ConfirmLockActivity.ConfirmLockActivityFragment;
import book.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.app.Fragment;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;
import book.support.v7.app.ActionBar;
import android.view.MenuItem;
import book.util.ColorUtils;
import com.book.app.lock.utils.bookColorfulAdapter;

public class ConfirmLockPassword extends PreferenceActivity {

    public static Context mContext;

    @Override
    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(EXTRA_SHOW_FRAGMENT,
                ConfirmLockPasswordFragment.class.getName());
        modIntent.putExtra(EXTRA_NO_HEADERS, true);
        return modIntent;
    }
    protected boolean isValidFragment(String fragmentName) {
        if (ConfirmLockPasswordFragment.class.getName().equals(fragmentName))
            return true;
        return false;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CharSequence msg = getText(R.string.lockpassword_confirm_your_password_header);
        showBreadCrumbs(msg, msg);
        // ActionBar actionBar = ((ActionBarActivity)getSupportActionBar());
        // actionBar.setTitle(R.string.unlock_set_unlock_pin_title);
        // actionBar.setDisplayHomeAsUpEnabled(true);
    }

    public static class ConfirmLockPasswordFragment extends Fragment implements
            OnClickListener {
        private static final long ERROR_MESSAGE_TIMEOUT = 200;
        private static final long PASSWORD_MESSAGE_TIMEOUT = 1000;
        private static final int MSG_SHOW_NEXT = 11111;
        private static final int MSG_SHOW_TEXT = 22222;
        private LockPatternUtils mLockPasswordUtils;
        private String mBeforePassword = "";
        private ImageButton mViewF, mViewS, mViewT, mViewE;
        private TextView mDeleteButton;
        private TextView mTextView;
        private Drawable mBackDrawable;
        private Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MSG_SHOW_NEXT) {
                    updateView();
                } else if (msg.what == MSG_SHOW_TEXT){
                    showToastMsg(getString(R.string.please_input_pwd));
                }
            }
        };
        public ConfirmLockPasswordFragment() {

        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mLockPasswordUtils = new LockPatternUtils(getActivity());
            Drawable pressedDrawable = getActivity().getResources().getDrawable(
                    R.drawable.view_back);
            mBackDrawable = bookColorfulAdapter.getColorDrawable(getActivity(),pressedDrawable);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.confirm_lock_password, null);
            NumberPasswordManager.getInstance().initView(view,getActivity(),
                    new NumberPasswordManager.OnNumberClickListener() {
                        @Override
                        public void clickedNumber(int number) {
                            // TODO Auto-generated method stub
                            Log.d("liuwenshuai", "clickedNumber:" + number);
                            mDeleteButton.setVisibility(View.VISIBLE);
                            mBeforePassword = mBeforePassword + number;
                            setPasswordView(mBeforePassword.length());
                            if (mBeforePassword.length() == 4) {
                                if (mLockPasswordUtils
                                        .checkPassword(mBeforePassword)) {
                                    Intent intent = new Intent();
                                    intent.putExtra(
                                            ChooseLockSettingsHelper.EXTRA_KEY_PASSWORD,
                                            mBeforePassword);
                                    getActivity().setResult(RESULT_OK, intent);
                                    getActivity().finish();
                                } else {
                                    mHandler.removeMessages(MSG_SHOW_NEXT);
                                    mHandler.sendEmptyMessageDelayed(MSG_SHOW_NEXT,
                                            ERROR_MESSAGE_TIMEOUT);
                                }
                            }
                        }
                    });
            initPasswordWidget(view);
            mDeleteButton = (TextView) view.findViewById(R.id.btn_delete);
            mDeleteButton.setOnClickListener(this);

            final boolean isAlpha = false;
            final Activity activity = getActivity();
            if (activity instanceof PreferenceActivity) {
                final PreferenceActivity PreferenceActivity = (PreferenceActivity) activity;
                int id = isAlpha ? R.string.lockpassword_confirm_your_password_header
                        : R.string.lockpassword_confirm_your_pin_header;
                CharSequence title = getText(id);
                PreferenceActivity.showBreadCrumbs(title, title);
            }

            return view;
        }

        @Override
        public void onPause() {
            getActivity().finish();
            super.onPause();
        }

        @Override
        public void onResume() {
            // TODO Auto-generated method stub
            super.onResume();
        }

        public void onClick(View v) {
            switch (v.getId()) {
            case R.id.btn_delete:
                deleteView();
                break;

            case R.id.footerLeftButton:
                getActivity().setResult(RESULT_CANCELED);
                getActivity().finish();
                break;
            }
        }

        private void initPasswordWidget(View v) {
            mTextView = (TextView) v.findViewById(R.id.tv_info);
            mViewF = (ImageButton) v.findViewById(R.id.et_pwd1);
            mViewS = (ImageButton) v.findViewById(R.id.et_pwd2);
            mViewT = (ImageButton) v.findViewById(R.id.et_pwd3);
            mViewE = (ImageButton) v.findViewById(R.id.et_pwd4);
        }
        private void updateView() {
            showToastMsg(getString(R.string.password_error));
            clearView();
            mBeforePassword = "";
            mHandler.removeMessages(MSG_SHOW_TEXT);
            mHandler.sendEmptyMessageDelayed(MSG_SHOW_TEXT,
                    PASSWORD_MESSAGE_TIMEOUT);
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
