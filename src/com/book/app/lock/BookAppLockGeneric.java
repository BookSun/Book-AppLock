package com.book.app.lock;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import book.support.v7.app.ActionBarActivity;

public class bookAppLockGeneric extends ActionBarActivity {
    private LockPatternUtils mLockPatternUtils;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        mLockPatternUtils = new LockPatternUtils(this);
        Intent newIntent = new Intent();
        if (mLockPatternUtils.savedbookPatternExists()) {
            newIntent.setClassName("com.book.app.lock", "com.book.app.lock.bookAppLockConfirmControl");
        } else {
            newIntent.setClassName("com.book.app.lock", "com.book.app.lock.bookChooseLockPattern");
        }
        startActivity(newIntent);
        finish();
    }
    
}