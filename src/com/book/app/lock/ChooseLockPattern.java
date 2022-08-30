/*
 * Copyright (C) 2007 The Android Open Source Project
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
import book.support.v7.app.ActionBarActivity;
import com.google.android.collect.Lists;
import com.android.internal.widget.LinearLayoutWithDefaultTouchRecepient;
import com.book.app.lock.ChooseLockPassword.ChooseLockPasswordFragment;
import com.android.internal.widget.LockPatternView.Cell;

import static com.android.internal.widget.LockPatternView.DisplayMode;
import com.android.internal.widget.LockPatternView;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import book.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * If the user has a lock pattern set already, makes them confirm the existing
 * one.
 * 
 * @auther liuwenshuai Then, prompts the user to choose a lock pattern: -
 *         prompts for initial pattern - asks for confirmation / restart - saves
 *         chosen password when confirmed
 */
public class ChooseLockPattern extends PreferenceActivity {
    static final int RESULT_FINISHED = RESULT_FIRST_USER;

    @Override
    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(EXTRA_SHOW_FRAGMENT,
                ChooseLockPatternFragment.class.getName());
        modIntent.putExtra(EXTRA_NO_HEADERS, true);
        return modIntent;
    }
    protected boolean isValidFragment(String fragmentName) {
        if (ChooseLockPatternFragment.class.getName().equals(fragmentName))
            return true;
        return false;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        CharSequence msg = getText(R.string.lockpassword_choose_your_pattern_header);
        showBreadCrumbs(msg, msg);
        // ActionBar actionBar = ((ActionBarActivity)getSupportActionBar());
        // actionBar.setTitle(R.string.unlock_set_unlock_pattern_title);
        // actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // *** TODO ***
        // chooseLockPatternFragment.onKeyDown(keyCode, event);
        return super.onKeyDown(keyCode, event);
    }

    public static class ChooseLockPatternFragment extends Fragment implements
            View.OnClickListener {

        public static final int CONFIRM_EXISTING_REQUEST = 55;

        // how long after a confirmation message is shown before moving on
        static final int INFORMATION_MSG_TIMEOUT_MS = 3000;

        // how long we wait to clear a wrong pattern
        private static final int WRONG_PATTERN_CLEAR_TIMEOUT_MS = 2000;
        private static final long ERROR_MESSAGE_TIMEOUT = 300;

        private static final int ID_EMPTY_MESSAGE = -1;

        protected TextView mHeaderText;
        protected LockPatternView mLockPatternView;
        protected TextView mFooterText;
        private TextView mFooterLeftButton;
        private TextView mFooterRightButton;
        protected List<Cell> mChosenPattern = null;
        private static final int MSG_SHOW_NEXT = 11111;
        private LinearLayout mLayout;
        private Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MSG_SHOW_NEXT) {
                    updateView();
                }
            }
        };
        /**
         * The patten used during the help screen to show how to draw a pattern.
         */
        private final List<Cell> mAnimatePattern = Collections
                .unmodifiableList(Lists.newArrayList(Cell.of(0, 0),
                        Cell.of(0, 1), Cell.of(1, 1), Cell.of(2, 1)));

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
                updateStage(Stage.Introduction);
                break;
            }
        }

        /**
         * The pattern listener that responds according to a user choosing a new
         * lock pattern.
         */
        protected LockPatternView.OnPatternListener mChooseNewLockPatternListener = new LockPatternView.OnPatternListener() {

            public void onPatternStart() {
                mLockPatternView.removeCallbacks(mClearPatternRunnable);
                patternInProgress();
            }

            public void onPatternCleared() {
                mLockPatternView.removeCallbacks(mClearPatternRunnable);
            }

            public void onPatternDetected(List<Cell> pattern) {
                if (mUiStage == Stage.NeedToConfirm
                        || mUiStage == Stage.ConfirmWrong) {
                    if (mChosenPattern == null)
                        throw new IllegalStateException(
                                "null chosen pattern in stage 'need to confirm");
                    if (mChosenPattern.equals(pattern)) {
                        updateStage(Stage.ChoiceConfirmed);
                    } else {
                        updateStage(Stage.ConfirmWrong);
                    }
                } else if (mUiStage == Stage.Introduction
                        || mUiStage == Stage.ChoiceTooShort) {
                    if (pattern.size() < LockPatternUtils.MIN_LOCK_PATTERN_SIZE) {
                        updateStage(Stage.ChoiceTooShort);
                    } else {
                        mChosenPattern = new ArrayList<Cell>(pattern);
                        updateStage(Stage.FirstChoiceValid);
                        mHandler.removeMessages(MSG_SHOW_NEXT);
                        mHandler.sendEmptyMessageDelayed(MSG_SHOW_NEXT,
                                ERROR_MESSAGE_TIMEOUT);
                    }
                } else {
                    throw new IllegalStateException("Unexpected stage "
                            + mUiStage + " when " + "entering the pattern.");
                }
            }

            public void onPatternCellAdded(List<Cell> pattern) {

            }

            private void patternInProgress() {
                mHeaderText.setText(R.string.lockpattern_recording_inprogress);
                mFooterText.setText("");
                mFooterLeftButton.setEnabled(false);
                mFooterRightButton.setEnabled(false);
            }
        };

        /**
         * The states of the left footer button.
         */
        enum LeftButtonMode {
            Cancel(R.string.cancel, true), CancelDisabled(R.string.cancel,
                    false), Retry(R.string.lockpattern_retry_button_text, true), RetryDisabled(
                    R.string.lockpattern_retry_button_text, false), Gone(
                    ID_EMPTY_MESSAGE, false);
            LeftButtonMode(int text, boolean enabled) {
                this.text = text;
                this.enabled = enabled;
            }

            final int text;
            final boolean enabled;
        }

        /**
         * The states of the right button.
         */
        enum RightButtonMode {
            Continue(R.string.lockpattern_continue_button_text, true), ContinueDisabled(
                    R.string.lockpattern_continue_button_text, false), Confirm(
                    R.string.lockpattern_confirm_button_text, true), ConfirmDisabled(
                    R.string.lockpattern_confirm_button_text, false), Ok(
                    android.R.string.ok, true);
            RightButtonMode(int text, boolean enabled) {
                this.text = text;
                this.enabled = enabled;
            }

            final int text;
            final boolean enabled;
        }

        protected enum Stage {

            Introduction(R.string.lockpattern_recording_intro_header,
                    LeftButtonMode.Cancel, RightButtonMode.ContinueDisabled,
                    ID_EMPTY_MESSAGE, true), HelpScreen(
                    R.string.lockpattern_settings_help_how_to_record,
                    LeftButtonMode.Gone, RightButtonMode.Ok, ID_EMPTY_MESSAGE,
                    false), ChoiceTooShort(
                    R.string.lockpattern_recording_incorrect_too_short,
                    LeftButtonMode.Retry, RightButtonMode.ContinueDisabled,
                    ID_EMPTY_MESSAGE, true), FirstChoiceValid(
                    R.string.lockpattern_pattern_entered_header,
                    LeftButtonMode.Retry, RightButtonMode.Continue,
                    ID_EMPTY_MESSAGE, false), NeedToConfirm(
                    R.string.lockpattern_need_to_confirm,
                    LeftButtonMode.Cancel, RightButtonMode.ConfirmDisabled,
                    ID_EMPTY_MESSAGE, true), ConfirmWrong(
                    R.string.lockpattern_need_to_unlock_wrong,
                    LeftButtonMode.Cancel, RightButtonMode.ConfirmDisabled,
                    ID_EMPTY_MESSAGE, true), ChoiceConfirmed(
                    R.string.lockpattern_pattern_confirmed_header,
                    LeftButtonMode.Cancel, RightButtonMode.Confirm,
                    ID_EMPTY_MESSAGE, false);

            Stage(int headerMessage, LeftButtonMode leftMode,
                    RightButtonMode rightMode, int footerMessage,
                    boolean patternEnabled) {
                this.headerMessage = headerMessage;
                this.leftMode = leftMode;
                this.rightMode = rightMode;
                this.footerMessage = footerMessage;
                this.patternEnabled = patternEnabled;
            }

            final int headerMessage;
            final LeftButtonMode leftMode;
            final RightButtonMode rightMode;
            final int footerMessage;
            final boolean patternEnabled;
        }

        private Stage mUiStage = Stage.Introduction;

        private Runnable mClearPatternRunnable = new Runnable() {
            public void run() {
                mLockPatternView.clearPattern();
            }
        };

        private ChooseLockSettingsHelper mChooseLockSettingsHelper;

        private static final String KEY_UI_STAGE = "uiStage";
        private static final String KEY_PATTERN_CHOICE = "chosenPattern";

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mChooseLockSettingsHelper = new ChooseLockSettingsHelper(
                    getActivity());
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {

            // setupViews()
            View view = inflater.inflate(R.layout.choose_lock_pattern, null);
            mHeaderText = (TextView) view.findViewById(R.id.headerText);
            mLayout = (LinearLayout) view.findViewById(R.id.patternButton);
            mLockPatternView = (LockPatternView) view
                    .findViewById(R.id.lockPattern);
            mLockPatternView
                    .setOnPatternListener(mChooseNewLockPatternListener);
            mLockPatternView
                    .setTactileFeedbackEnabled(mChooseLockSettingsHelper
                            .utils().isTactileFeedbackEnabled());

            mFooterText = (TextView) view.findViewById(R.id.footerText);

            mFooterLeftButton = (TextView) view
                    .findViewById(R.id.footerLeftButton);
            mFooterRightButton = (TextView) view
                    .findViewById(R.id.footerRightButton);

            mFooterLeftButton.setOnClickListener(this);
            mFooterRightButton.setOnClickListener(this);

            final LinearLayoutWithDefaultTouchRecepient topLayout = (LinearLayoutWithDefaultTouchRecepient) view
                    .findViewById(R.id.topLayout);
            topLayout.setDefaultTouchRecepient(mLockPatternView);

            final boolean confirmCredentials = getActivity().getIntent()
                    .getBooleanExtra("confirm_credentials", false);

            if (savedInstanceState == null) {
                if (confirmCredentials) {
                    updateStage(Stage.NeedToConfirm);
                    boolean launchedConfirmationActivity = mChooseLockSettingsHelper
                            .launchConfirmationActivity(
                                    CONFIRM_EXISTING_REQUEST, null, null);
                    if (!launchedConfirmationActivity) {
                        updateStage(Stage.Introduction);
                    }
                } else {
                    updateStage(Stage.Introduction);
                }
            } else {
                // restore from previous state
                final String patternString = savedInstanceState
                        .getString(KEY_PATTERN_CHOICE);
                if (patternString != null) {
                    mChosenPattern = LockPatternUtils
                            .stringToPattern(patternString);
                }
                updateStage(Stage.values()[savedInstanceState
                        .getInt(KEY_UI_STAGE)]);
            }
            return view;
        }

        public void onClick(View v) {
            if (v == mFooterLeftButton) {
                if (mUiStage.leftMode == LeftButtonMode.Retry) {
                    mChosenPattern = null;
                    mLockPatternView.clearPattern();
                    updateStage(Stage.Introduction);
                } else if (mUiStage.leftMode == LeftButtonMode.Cancel) {
                    // They are canceling the entire wizard
                    mChosenPattern = null;
                    mLockPatternView.clearPattern();
                    mLayout.setVisibility(View.INVISIBLE);
                    updateStage(Stage.Introduction);
                } else {
                    throw new IllegalStateException(
                            "left footer button pressed, but stage of "
                                    + mUiStage + " doesn't make sense");
                }
            } else if (v == mFooterRightButton) {

                if (mUiStage.rightMode == RightButtonMode.Continue) {
                    if (mUiStage != Stage.FirstChoiceValid) {
                        throw new IllegalStateException("expected ui stage "
                                + Stage.FirstChoiceValid + " when button is "
                                + RightButtonMode.Continue);
                    }
                    updateStage(Stage.NeedToConfirm);
                } else if (mUiStage.rightMode == RightButtonMode.Confirm) {
                    if (mUiStage != Stage.ChoiceConfirmed) {
                        throw new IllegalStateException("expected ui stage "
                                + Stage.ChoiceConfirmed + " when button is "
                                + RightButtonMode.Confirm);
                    }
                    saveChosenPatternAndFinish();
                } else if (mUiStage.rightMode == RightButtonMode.Ok) {
                    if (mUiStage != Stage.HelpScreen) {
                        throw new IllegalStateException(
                                "Help screen is only mode with ok button, but "
                                        + "stage is " + mUiStage);
                    }
                    mLockPatternView.clearPattern();
                    mLockPatternView.setDisplayMode(DisplayMode.Correct);
                    updateStage(Stage.Introduction);
                }
            }
        }

        public boolean onKeyDown(int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
                if (mUiStage == Stage.HelpScreen) {
                    updateStage(Stage.Introduction);
                    return true;
                }
            }
            if (keyCode == KeyEvent.KEYCODE_MENU
                    && mUiStage == Stage.Introduction) {
                updateStage(Stage.HelpScreen);
                return true;
            }
            return false;
        }
        @Override
        public void onPause() {
            getActivity().finish();
            super.onPause();
        }
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);

            outState.putInt(KEY_UI_STAGE, mUiStage.ordinal());
            if (mChosenPattern != null) {
                outState.putString(KEY_PATTERN_CHOICE,
                        LockPatternUtils.patternToString(mChosenPattern));
            }
        }

        protected void updateStage(Stage stage) {

            mUiStage = stage;
            if (stage == Stage.ChoiceTooShort) {
                mHeaderText.setText(getResources().getString(
                        stage.headerMessage,
                        LockPatternUtils.MIN_LOCK_PATTERN_SIZE));
            } else {
                mHeaderText.setText(stage.headerMessage);
            }
            if (stage.footerMessage == ID_EMPTY_MESSAGE) {
                mFooterText.setText("");
            } else {
                mFooterText.setText(stage.footerMessage);
            }

            if (stage.leftMode == LeftButtonMode.Gone) {
                mFooterLeftButton.setVisibility(View.GONE);
            } else {
                mFooterLeftButton.setVisibility(View.VISIBLE);
                mFooterLeftButton.setText(stage.leftMode.text);
                mFooterLeftButton.setEnabled(stage.leftMode.enabled);
            }
            mFooterRightButton.setText(stage.rightMode.text);
            mFooterRightButton.setEnabled(stage.rightMode.enabled);
            if (stage.patternEnabled) {
                mLockPatternView.enableInput();
            } else {
                mLockPatternView.disableInput();
            }

            mLockPatternView.setDisplayMode(DisplayMode.Correct);

            switch (mUiStage) {
            case Introduction:
                mLockPatternView.clearPattern();
                break;
            case HelpScreen:
                mLockPatternView.setPattern(DisplayMode.Animate,
                        mAnimatePattern);
                break;
            case ChoiceTooShort:
                mLockPatternView.setDisplayMode(DisplayMode.Wrong);
                postClearPatternRunnable();
                break;
            case FirstChoiceValid:
                break;
            case NeedToConfirm:
                mLayout.setVisibility(View.VISIBLE);
                mLockPatternView.clearPattern();
                break;
            case ConfirmWrong:
                mLockPatternView.setDisplayMode(DisplayMode.Wrong);
                postClearPatternRunnable();
                break;
            case ChoiceConfirmed:
                break;
            }
        }

        private void postClearPatternRunnable() {
            mLockPatternView.removeCallbacks(mClearPatternRunnable);
            mLockPatternView.postDelayed(mClearPatternRunnable,
                    WRONG_PATTERN_CLEAR_TIMEOUT_MS);
        }

        private void saveChosenPatternAndFinish() {
            LockPatternUtils utils = mChooseLockSettingsHelper.utils();
            utils.saveLockPattern(mChosenPattern);
            getActivity().setResult(RESULT_FINISHED);
            getActivity().finish();
        }

        private void updateView() {
            if (mUiStage != Stage.FirstChoiceValid) {
                throw new IllegalStateException("expected ui stage "
                        + Stage.FirstChoiceValid + " when button is "
                        + RightButtonMode.Continue);
            }
            updateStage(Stage.NeedToConfirm);
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
