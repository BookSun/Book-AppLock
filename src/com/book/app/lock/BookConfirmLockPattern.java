/*
 * Copyright (C) 2008 The Android Open Source Project
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

import java.util.List;
import book.support.v7.app.ActionBarActivity;
import android.R.string;
import book.support.v7.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.RemoteException;
import android.os.SystemClock;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;
import com.android.internal.widget.LinearLayoutWithDefaultTouchRecepient;
import com.android.internal.widget.LockPatternView.Cell;

import com.android.internal.widget.LockPatternView;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;



public class bookConfirmLockPattern extends PreferenceActivity {

    public static final String PACKAGE = "com.book.app.lock";
    public static final String HEADER_TEXT = PACKAGE
            + ".ConfirmLockPattern.header";
    public static final String FOOTER_TEXT = PACKAGE
            + ".ConfirmLockPattern.footer";
    public static final String HEADER_WRONG_TEXT = PACKAGE
            + ".ConfirmLockPattern.header_wrong";
    public static final String FOOTER_WRONG_TEXT = PACKAGE
            + ".ConfirmLockPattern.footer_wrong";
    private String mConfirmFlag;

    private enum Stage {
        NeedToUnlock, NeedToUnlockWrong, LockedOut
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CharSequence msg = getText(R.string.book_applock_title);
        showBreadCrumbs(msg, msg);
        // ActionBar actionBar = ((ActionBarActivity)getSupportActionBar());
        // actionBar.setTitle(R.string.book_applock_title);
        // actionBar.setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        mConfirmFlag = intent.getStringExtra("com.book.app.lock.APPLOCK_RESUME_FLAG");
    }

    @Override
    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(EXTRA_SHOW_FRAGMENT,
                bookConfirmLockPatternFragment.class.getName());
        modIntent.putExtra(EXTRA_NO_HEADERS, true);
        return modIntent;
    }
    protected boolean isValidFragment(String fragmentName) {
        if (bookConfirmLockPatternFragment.class.getName().equals(fragmentName))
            return true;
        return false;
    }

    public static class bookConfirmLockPatternFragment extends PreferenceFragment {

        // how long we wait to clear a wrong pattern
        private static final int WRONG_PATTERN_CLEAR_TIMEOUT_MS = 2000;

        private static final String KEY_NUM_WRONG_ATTEMPTS = "num_wrong_attempts";
        private static final String APPLOCK_RESUME = "applock_resume";
        private static final String EMERGENCY_CALL_DIALOG = "com.android.contacts.activities.DialtactsActivity";
        private LockPatternView mLockPatternView;
        private LockPatternUtils mLockPatternUtils;
        private int mNumWrongConfirmAttempts;
        private CountDownTimer mCountdownTimer;

        private TextView mHeaderTextView;
        private TextView mFooterTextView;
        private TextView mEmergencyCall;

        // caller-supplied text for various prompts
        private CharSequence mHeaderText;
        private CharSequence mFooterText;
        private CharSequence mHeaderWrongText;
        private CharSequence mFooterWrongText;
        private String mConfirmApp;
        private String mConfirmCLass;
        private String mConfirmFlag = "none";
        private IActivityManager mAm;

        // required constructor for fragments
        public bookConfirmLockPatternFragment() {

        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mAm = ActivityManagerNative.getDefault();
            mLockPatternUtils = new LockPatternUtils(getActivity());
            Intent intent = getActivity().getIntent();
            mConfirmApp = intent.getStringExtra("com.book.app.lock.SET_APPLOCK_PACKAGE");
            mConfirmCLass = intent.getStringExtra("com.book.app.lock.SET_APPLOCK_CLASSNAME");
            mConfirmFlag = intent.getStringExtra("com.book.app.lock.APPLOCK_RESUME_FLAG");
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.confirm_lock_pattern, null);
            mHeaderTextView = (TextView) view.findViewById(R.id.headerText);
            mLockPatternView = (LockPatternView) view
                    .findViewById(R.id.lockPattern);
            mFooterTextView = (TextView) view.findViewById(R.id.footerText);
            mEmergencyCall = (TextView) view.findViewById(R.id.emergency_call);
            mEmergencyCall.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                // TODO Auto-generated method stub
                    Intent intent = new Intent();
                    intent.setClassName("com.android.phone","com.android.phone.EmergencyDialer");
                    getActivity().startActivity(intent);
                }
            });
            if (mConfirmCLass != null) {
                if (mConfirmCLass.equals(EMERGENCY_CALL_DIALOG)) {
                    mEmergencyCall.setVisibility(View.VISIBLE);
                }
            } else {
                mEmergencyCall.setVisibility(View.GONE);
            }
            final LinearLayoutWithDefaultTouchRecepient topLayout = (LinearLayoutWithDefaultTouchRecepient) view
                    .findViewById(R.id.topLayout);
            topLayout.setDefaultTouchRecepient(mLockPatternView);

            Intent intent = getActivity().getIntent();
            if (intent != null) {
                mHeaderText = intent.getCharSequenceExtra(HEADER_TEXT);
                mFooterText = intent.getCharSequenceExtra(FOOTER_TEXT);
                mHeaderWrongText = intent
                        .getCharSequenceExtra(HEADER_WRONG_TEXT);
                mFooterWrongText = intent
                        .getCharSequenceExtra(FOOTER_WRONG_TEXT);
            }
            mLockPatternView.setTactileFeedbackEnabled(getPatternFeedBack());
            mLockPatternView.setInStealthMode(!getPatternVisible());
            mLockPatternView
                    .setOnPatternListener(mConfirmExistingLockPatternListener);
            updateStage(Stage.NeedToUnlock);

            if (savedInstanceState != null) {
                mNumWrongConfirmAttempts = savedInstanceState
                        .getInt(KEY_NUM_WRONG_ATTEMPTS);
            } else {
                if (!mLockPatternUtils.savedbookPatternExists()) {
                    getActivity().setResult(Activity.RESULT_OK);
                    getActivity().finish();
                }
            }
            return view;
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            outState.putInt(KEY_NUM_WRONG_ATTEMPTS, mNumWrongConfirmAttempts);
        }

        @Override
        public void onPause() {
            super.onPause();

            if (mCountdownTimer != null) {
                mCountdownTimer.cancel();
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            long deadline = mLockPatternUtils.getLockoutAttemptDeadline();
            if (deadline != 0) {
                handleAttemptLockout(deadline);
            } else if (!mLockPatternView.isEnabled()) {
                // The deadline has passed, but the timer was cancelled...
                // Need to clean up.
                mNumWrongConfirmAttempts = 0;
                updateStage(Stage.NeedToUnlock);
            }
        }
        private boolean getPatternVisible() {
            boolean patternVisible = Settings.System.getInt(getActivity()
                    .getContentResolver(), "book_applock_pattern_visible", 1) == 1 ? true
                    : false;
            return patternVisible;
        }
        private boolean getPatternFeedBack() {
            boolean patternFeedBack = Settings.System.getInt(getActivity()
                    .getContentResolver(), "book_applock_feedback", 1) == 1 ? true
                    : false;
            return patternFeedBack;
        }
        private void updateStage(Stage stage) {

            switch (stage) {
            case NeedToUnlock:
                if (mHeaderText != null) {
                    mHeaderTextView.setText(mHeaderText);
                } else {
                    mHeaderTextView
                            .setText(R.string.lockpattern_need_to_unlock);
                }
                if (mFooterText != null) {
                    mFooterTextView.setText(mFooterText);
                } else {
                    mFooterTextView
                            .setText(R.string.lockpattern_need_to_unlock_footer);
                }

                mLockPatternView.setEnabled(true);
                mLockPatternView.enableInput();
                break;
            case NeedToUnlockWrong:
                if (mHeaderWrongText != null) {
                    mHeaderTextView.setText(mHeaderWrongText);
                } else {
                    mHeaderTextView
                            .setText(R.string.lockpattern_need_to_unlock_wrong);
                }
                if (mFooterWrongText != null) {
                    mFooterTextView.setText(mFooterWrongText);
                } else {
                    mFooterTextView
                            .setText(R.string.lockpattern_need_to_unlock_wrong_footer);
                }

                mLockPatternView
                        .setDisplayMode(LockPatternView.DisplayMode.Wrong);
                mLockPatternView.setEnabled(true);
                mLockPatternView.enableInput();
                break;
            case LockedOut:
                mLockPatternView.clearPattern();
                mLockPatternView.setEnabled(false); // appearance of being
                break;
            }
        }

        private Runnable mClearPatternRunnable = new Runnable() {
            public void run() {
                mLockPatternView.clearPattern();
            }
        };
        // clear the wrong pattern unless they have started a new one
        // already
        private void postClearPatternRunnable() {
            mLockPatternView.removeCallbacks(mClearPatternRunnable);
            mLockPatternView.postDelayed(mClearPatternRunnable,
                    WRONG_PATTERN_CLEAR_TIMEOUT_MS);
        }

        /**
         * The pattern listener that responds according to a user confirming an
         * existing lock pattern.
         */
        private LockPatternView.OnPatternListener mConfirmExistingLockPatternListener = new LockPatternView.OnPatternListener() {

            public void onPatternStart() {
                mLockPatternView.removeCallbacks(mClearPatternRunnable);
            }

            public void onPatternCleared() {
                mLockPatternView.removeCallbacks(mClearPatternRunnable);
            }

            public void onPatternCellAdded(List<Cell> pattern) {

            }

            public void onPatternDetected(List<Cell> pattern) {
                if (pattern == null) {
                    return;
                }
                if (mLockPatternUtils.checkbookAppLockPattern(pattern)) {
                    // try {
                    //     mAm.removeAppLockControlPackage(mConfirmApp);
                    // } catch (RemoteException e) {
                    //     Log.d("liuwenshuai", "Failed talking with activity manager!");
                    // }
                    if (mConfirmFlag == null) {
                        Intent intent = new Intent();
                        intent.setClassName(mConfirmApp,mConfirmCLass);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                    getActivity().finish();
                } else {
                    if (pattern.size() >= LockPatternUtils.MIN_PATTERN_REGISTER_FAIL
                            && ++mNumWrongConfirmAttempts >= LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT) {
                        long deadline = mLockPatternUtils
                                .setLockoutAttemptDeadline();
                        handleAttemptLockout(deadline);
                    } else {
                        updateStage(Stage.NeedToUnlockWrong);
                        postClearPatternRunnable();
                    }
                }
            }
        };

        private void handleAttemptLockout(long elapsedRealtimeDeadline) {
            updateStage(Stage.LockedOut);
            long elapsedRealtime = SystemClock.elapsedRealtime();
            mCountdownTimer = new CountDownTimer(elapsedRealtimeDeadline
                    - elapsedRealtime,
                    LockPatternUtils.FAILED_ATTEMPT_COUNTDOWN_INTERVAL_MS) {

                @Override
                public void onTick(long millisUntilFinished) {
                    mHeaderTextView
                            .setText(R.string.lockpattern_too_many_failed_confirmation_attempts_header);
                    final int secondsCountdown = (int) (millisUntilFinished / 1000);
                    mFooterTextView
                            .setText(getString(
                                    R.string.lockpattern_too_many_failed_confirmation_attempts_footer,
                                    secondsCountdown));
                }

                @Override
                public void onFinish() {
                    mNumWrongConfirmAttempts = 0;
                    updateStage(Stage.NeedToUnlock);
                }
            }.start();
        }
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            if (mConfirmFlag != null) {
                Intent home = new Intent(Intent.ACTION_MAIN);
                home.addCategory(Intent.CATEGORY_HOME);
                startActivity(home);
            }
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        if (mConfirmFlag != null) {
            Intent home = new Intent(Intent.ACTION_MAIN);  
            home.addCategory(Intent.CATEGORY_HOME);
            startActivity(home);
        }
        finish();
        super.onBackPressed();
    }
}
