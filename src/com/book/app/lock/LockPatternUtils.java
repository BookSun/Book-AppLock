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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import book.support.v7.app.ActionBarActivity;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.os.FileObserver;
import android.util.Log;
import android.provider.Settings;
import android.os.Environment;
import android.os.RemoteException;
import android.os.IBinder;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.text.TextUtils;

import java.security.SecureRandom;
import com.android.internal.widget.LockPatternView;
import com.android.internal.widget.LockPatternView.Cell;
import com.book.app.lock.provider.AppLockData;
import com.book.app.lock.provider.PreferencesProvider;

/**
 * 
 * @author seven
 * 
 */
public class LockPatternUtils {
    private static final String TAG = "liuwenshuai";
    /**
     * The minimum number of dots in a valid pattern.
     */
    public static final int MIN_LOCK_PATTERN_SIZE = 4;
    public static final int FAILED_ATTEMPTS_BEFORE_TIMEOUT = 5;
    public static final int MIN_PATTERN_REGISTER_FAIL = MIN_LOCK_PATTERN_SIZE;
    private static final String DATA_DIRECTORY = "/data/";
    private static final String CACHE_DIRECTORY = "/cache/";
    private static final String SYSTEM_DIRECTORY = "/system/";
    private static final String LOCK_PASSWORD_FILE = ".password.key";
    private static final String LOCK_PATTERN_FILE = ".gesture.key";
    private static final String book_LOCK_PATTERN_FILE = "applock.key";
    public static final long FAILED_ATTEMPT_TIMEOUT_MS = 30000L;
    public static final long FAILED_ATTEMPT_COUNTDOWN_INTERVAL_MS = 1000L;
    private final static String LOCK_PASSWORD_SALT_KEY = "lockscreen.password_salt";
    public final static String PASSWORD_TYPE_KEY = "lockscreen.password_type";
    protected final static String PASSWORD_HISTORY_KEY = "lockscreen.passwordhistory";
    protected final static String LOCKOUT_ATTEMPT_DEADLINE = "lockscreen.lockoutattemptdeadline";
    private final static String LOCK_PATTERN_TACTILE_FEEDBACK_ENABLED = "lock_pattern_tactile_feedback_enabled";
    private AppLockData mAppLockData;
    private final Context mContext;
    private final String mNewPatternPath;
    private final String mNewPasswordPath;
    private DevicePolicyManager mDevicePolicyManager;
    private String mPackName;


    public LockPatternUtils(Context context) {
        mContext = context;
        mAppLockData = new AppLockData(context);
        mPackName = PreferencesProvider.getDefaultPackage(mContext);
        Log.d("liuwenshuai", "LockPatternUtils->dataSystemDirectory:"
                + getLockPatternFilename(mPackName));
        Log.d("liuwenshuai","system data:"+getbookLockPatternFilename());
        mNewPatternPath = getLockPatternFilename(mPackName);
        mNewPasswordPath = getLockPasswordFilename(mPackName);
    }

    public DevicePolicyManager getDevicePolicyManager() {
        if (mDevicePolicyManager == null) {
            mDevicePolicyManager = (DevicePolicyManager) mContext
                    .getSystemService(Context.DEVICE_POLICY_SERVICE);
            if (mDevicePolicyManager == null) {
                Log.e(TAG,
                        "Can't get DevicePolicyManagerService: is it running?",
                        new IllegalStateException("Stack trace:"));
            }
        }
        return mDevicePolicyManager;
    }

    private String getLockPatternFilename(String appId) {
        String dataSystemDirectory = mContext.getFilesDir().getAbsolutePath();
        return dataSystemDirectory + "/"+appId + LOCK_PATTERN_FILE;
    }

    private String getLockPasswordFilename(String appId) {
        String dataSystemDirectory = mContext.getFilesDir().getAbsolutePath();
        return dataSystemDirectory + "/"+appId + LOCK_PASSWORD_FILE;
    }

    private String getbookLockPatternFilename() {
        String dataSystemDirectory = Environment.getDataDirectory().getAbsolutePath();
        return dataSystemDirectory + SYSTEM_DIRECTORY + book_LOCK_PATTERN_FILE;
    }

    /**
     * Check to see if the user has stored a lock pattern.
     * 
     * @return Whether a saved pattern exists.
     */
    public boolean savedPasswordExists(String appName) {
        return new File(getLockPasswordFilename(appName)).length() > 0;
    }

    /**
     * Check to see if the user has stored a lock pattern.
     * 
     * @return Whether a saved pattern exists.
     */
    public boolean savedPatternExists(String appName) {
        return new File(getLockPatternFilename(appName)).length() > 0;
    }
    
    public boolean savedPasswordExists() {
        return new File(getLockPasswordFilename(mPackName)).length() > 0;
    }
    public boolean savedbookPatternExists() {
        return new File(getbookLockPatternFilename()).length() > 0;
    }
    /**
     * Check to see if the user has stored a lock pattern.
     * 
     * @return Whether a saved pattern exists.
     */
    public boolean savedPatternExists() {
        return new File(getLockPatternFilename(mPackName)).length() > 0;
    }

    public void clearLock() {
        saveLockPattern(null);
        saveLockPassword(null, DevicePolicyManager.PASSWORD_QUALITY_NUMERIC);
        setLong(PASSWORD_TYPE_KEY,
                DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED);
    }
    
    public void clearbookAppLock() {
        savebookAppLockPattern(null);
    }
    /**
     * Deserialize a pattern
     * 
     * @param string
     *            The pattern serialized with {@link #patternToString}
     * @return The pattern.
     */
    public static List<Cell> stringToPattern(String string) {
        List<Cell> result = new ArrayList<Cell>();

        final byte[] bytes = string.getBytes();
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            result.add(Cell.of(b / 3, b % 3));
        }
        return result;
    }

    public static String patternToString(List<Cell> pattern) {
        if (pattern == null) {
            return "";
        }
        final int patternSize = pattern.size();

        byte[] res = new byte[patternSize];
        for (int i = 0; i < patternSize; i++) {
            Cell cell = pattern.get(i);
            res[i] = (byte) (cell.getRow() * 3 + cell.getColumn());
        }
        return new String(res);
    }

    /**
     * Save a lock pattern.
     * 
     * @param pattern
     *            The new pattern to save.
     * @param isFallback
     *            Specifies if this is a fallback to biometric weak
     */
    public void saveLockPattern(List<Cell> pattern) {
        // Compute the hash
        final byte[] hash = LockPatternUtils.patternToHash(pattern);
        Log.d("liuwenshuai", "saveLockPattern->hash:" + hash);
        try {
            // Write the hash to file
            RandomAccessFile raf = new RandomAccessFile(
                    getLockPatternFilename(mPackName), "rwd");
            // Truncate the file if pattern is null, to clear the lock
            if (pattern == null) {
                raf.setLength(0);
            } else {
                raf.write(hash, 0, hash.length);
            }
            raf.close();
            setLong(PASSWORD_TYPE_KEY,
                    DevicePolicyManager.PASSWORD_QUALITY_SOMETHING);
        } catch (FileNotFoundException fnfe) {
            // Cant do much, unless we want to fail over to using the settings
            // provider
            Log.e(TAG, "Unable to save lock pattern to "
                    + getLockPatternFilename(mPackName));
        } catch (IOException ioe) {
            // Cant do much
            Log.e(TAG, "Unable to save lock pattern to "
                    + getLockPatternFilename(mPackName));
        }
    }

    /**
     * Save a lock pattern.
     * 
     * @param pattern
     *            The new pattern to save.
     * @param isFallback
     *            Specifies if this is a fallback to biometric weak
     */
    public void savebookAppLockPattern(List<Cell> pattern) {
        // Compute the hash
        final byte[] hash = LockPatternUtils.patternToHash(pattern);
        Log.d("liuwenshuai", "savebookAppLockPattern->hash:" + hash);
        try {
            // Write the hash to file
            RandomAccessFile raf = new RandomAccessFile(getbookLockPatternFilename(), "rwd");
            // Truncate the file if pattern is null, to clear the lock
            if (pattern == null) {
                raf.setLength(0);
            } else {
                raf.write(hash, 0, hash.length);
                Log.d(TAG, "save the pattern file....");
            }
            raf.close();
        } catch (FileNotFoundException fnfe) {
            // Cant do much, unless we want to fail over to using the settings
            // provider
            Log.e(TAG, "Unable to save lock pattern to "
                    + getbookLockPatternFilename());
        } catch (IOException ioe) {
            // Cant do much
            Log.e(TAG, "Unable to save lock pattern to "
                    + getbookLockPatternFilename());
        }
    }
    /*
     * Generate an SHA-1 hash for the pattern. Not the most secure, but it is at
     * least a second level of protection. First level is that the file is in a
     * location only readable by the system process.
     * 
     * @param pattern the gesture pattern.
     * 
     * @return the hash of the pattern in a byte array.
     */
    private static byte[] patternToHash(List<Cell> pattern) {
        if (pattern == null) {
            return null;
        }

        final int patternSize = pattern.size();
        byte[] res = new byte[patternSize];
        for (int i = 0; i < patternSize; i++) {
            Cell cell = pattern.get(i);
            res[i] = (byte) (cell.getRow() * 3 + cell.getColumn());
        }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hash = md.digest(res);
            return hash;
        } catch (NoSuchAlgorithmException nsa) {
            return res;
        }
    }

    /**
     * Check to see if a pattern matches the saved pattern. If no pattern
     * exists, always returns true.
     * 
     * @param pattern
     *            The pattern to check.
     * @return Whether the pattern matches the stored one.
     */
    public boolean checkPattern(List<Cell> pattern) {
        try {
            // Read all the bytes from the file
            RandomAccessFile raf = new RandomAccessFile(
                    getLockPatternFilename(mPackName), "r");
            final byte[] stored = new byte[(int) raf.length()];
            int got = raf.read(stored, 0, stored.length);
            raf.close();
            if (got <= 0) {
                return true;
            }
            // Compare the hash from the file with the entered pattern's hash
            return Arrays.equals(stored,
                    LockPatternUtils.patternToHash(pattern));
        } catch (FileNotFoundException fnfe) {
            return true;
        } catch (IOException ioe) {
            return true;
        }
    }

    /**
     * Check to see if a pattern matches the saved pattern. If no pattern
     * exists, always returns true.
     * 
     * @param pattern
     *            The pattern to check.
     * @return Whether the pattern matches the stored one.
     */
    public boolean checkbookAppLockPattern(List<Cell> pattern) {
        try {
            // Read all the bytes from the file
            RandomAccessFile raf = new RandomAccessFile(getbookLockPatternFilename(), "r");
            final byte[] stored = new byte[(int) raf.length()];
            int got = raf.read(stored, 0, stored.length);
            raf.close();
            if (got <= 0) {
                return true;
            }
            // Compare the hash from the file with the entered pattern's hash
            return Arrays.equals(stored,
                    LockPatternUtils.patternToHash(pattern));
        } catch (FileNotFoundException fnfe) {
            return true;
        } catch (IOException ioe) {
            return true;
        }
    }

    /**
     * Compute the password quality from the given password string.
     */
    static public int computePasswordQuality(String password) {
        boolean hasDigit = false;
        boolean hasNonDigit = false;
        final int len = password.length();
        for (int i = 0; i < len; i++) {
            if (Character.isDigit(password.charAt(i))) {
                hasDigit = true;
            } else {
                hasNonDigit = true;
            }
        }

        if (hasNonDigit && hasDigit) {
            return DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC;
        }
        if (hasNonDigit) {
            return DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC;
        }
        if (hasDigit) {
            return DevicePolicyManager.PASSWORD_QUALITY_NUMERIC;
        }
        return DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED;
    }
    public void saveLockPassword(String password, int quality) {
        // Compute the hash
        final byte[] hash = passwordToHash(password);
        Log.d("liuwenshuai", "saveLockPassword->hash:" + hash);
        try {
            // Write the hash to file
            RandomAccessFile raf = new RandomAccessFile(
                    getLockPasswordFilename(mPackName), "rwd");
            try {
                if (password == null) {
                    raf.setLength(0);
                } else {
                    raf.write(hash, 0, hash.length);
                    raf.getFD().sync();
                }
            } finally {
                if (raf != null)
                    raf.close();
            }
            setLong(PASSWORD_TYPE_KEY, quality);
        } catch (FileNotFoundException fnfe) {
            // Cant do much, unless we want to fail over to using the settings
            // provider
            Log.e(TAG, "Unable to save lock pattern to "
                    + getLockPasswordFilename(mPackName));
        } catch (IOException ioe) {
            // Cant do much
            Log.e(TAG, "Unable to save lock pattern to "
                    + getLockPasswordFilename(mPackName));
        }
    }

    /**
     * Check to see if a password matches the saved password. If no password
     * exists, always returns true.
     * 
     * @param password
     *            The password to check.
     * @return Whether the password matches the stored one.
     */
    public boolean checkPassword(String password) {
        try {
            // Read all the bytes from the file
            RandomAccessFile raf = new RandomAccessFile(
                    getLockPasswordFilename(mPackName), "r");
            final byte[] stored = new byte[(int) raf.length()];
            int got = raf.read(stored, 0, stored.length);
            raf.close();
            if (got <= 0) {
                return true;
            }
            // Compare the hash from the file with the entered password's hash
            return Arrays.equals(stored, passwordToHash(password));
        } catch (FileNotFoundException fnfe) {
            return true;
        } catch (IOException ioe) {
            return true;
        }
    }

    /*
     * Generate a hash for the given password. To avoid brute force attacks, we
     * use a salted hash. Not the most secure, but it is at least a second level
     * of protection. First level is that the file is in a location only
     * readable by the system process.
     * 
     * @param password the gesture pattern.
     * 
     * @return the hash of the pattern in a byte array.
     */
    public byte[] passwordToHash(String password) {
        if (password == null) {
            return null;
        }
        String algo = null;
        byte[] hashed = null;
        try {
            byte[] saltedPassword = (password /*+ getSalt()*/).getBytes();
            byte[] sha1 = MessageDigest.getInstance(algo = "SHA-1").digest(
                    saltedPassword);
            byte[] md5 = MessageDigest.getInstance(algo = "MD5").digest(
                    saltedPassword);
            hashed = (toHex(sha1) + toHex(md5)).getBytes();
        } catch (NoSuchAlgorithmException e) {
            Log.w(TAG, "Failed to encode string because of missing algorithm: "
                    + algo);
        }
        return hashed;
    }

    private String getSalt() {
        long salt = getLong(LOCK_PASSWORD_SALT_KEY, 0);
        Log.d("liuwenshuai","getsalt->salt"+salt);
        if (salt == 0) {
            try {
                salt = SecureRandom.getInstance("SHA1PRNG").nextLong();
                setLong(LOCK_PASSWORD_SALT_KEY, salt);
                Log.v(TAG, "Initialized lock password salt");
            } catch (NoSuchAlgorithmException e) {
                // Throw an exception rather than storing a password we'll never
                // be able to recover
                throw new IllegalStateException(
                        "Couldn't get SecureRandom number", e);
            }
        }
        return Long.toHexString(salt);
    }

    private static String toHex(byte[] ary) {
        final String hex = "0123456789ABCDEF";
        String ret = "";
        for (int i = 0; i < ary.length; i++) {
            ret += hex.charAt((ary[i] >> 4) & 0xf);
            ret += hex.charAt(ary[i] & 0xf);
        }
        return ret;
    }

    private String getString(String secureSettingKey) {
        return mAppLockData.readFromDb(secureSettingKey, null, mPackName);

    }

    /**
     * @return Whether the visible pattern is enabled.
     */
    // public boolean isVisiblePatternEnabled() {
    // return getBoolean(Settings.Secure.LOCK_PATTERN_VISIBLE, false);
    // }

    /**
     * Set whether the visible pattern is enabled.
     */
    // public void setVisiblePatternEnabled(boolean enabled) {
    // setBoolean(Settings.Secure.LOCK_PATTERN_VISIBLE, enabled);
    // }

    /**
     * @return Whether tactile feedback for the pattern is enabled.
     */
    public boolean isTactileFeedbackEnabled() {
        return getBoolean(LOCK_PATTERN_TACTILE_FEEDBACK_ENABLED, true);
    }

    /**
     * Set whether tactile feedback for the pattern is enabled.
     */
    public void setTactileFeedbackEnabled(boolean enabled) {
        setBoolean(LOCK_PATTERN_TACTILE_FEEDBACK_ENABLED, enabled);
    }

    private boolean getBoolean(String secureSettingKey, boolean defaultValue) {
        String value = mAppLockData.readFromDb(secureSettingKey, null,
                mPackName);
        return TextUtils.isEmpty(value) ? defaultValue
                : (value.equals("1") || value.equals("true"));

    }

    private void setBoolean(String secureSettingKey, boolean enabled) {

        mAppLockData
                .writeToDb(secureSettingKey, enabled ? "1" : "0", mPackName);

    }

    /**
     * Set and store the lockout deadline, meaning the user can't attempt
     * his/her unlock pattern until the deadline has passed.
     * 
     * @return the chosen deadline.
     */
    public long setLockoutAttemptDeadline() {
        final long deadline = SystemClock.elapsedRealtime()
                + FAILED_ATTEMPT_TIMEOUT_MS;
        setLong(LOCKOUT_ATTEMPT_DEADLINE, deadline);
        return deadline;
    }

    public void setLong(String secureSettingKey, long value) {
        mAppLockData.writeToDb(secureSettingKey, Long.toString(value),
                mPackName);
    }

    private long getLong(String secureSettingKey, long defaultValue) {
        String value = mAppLockData.readFromDb(secureSettingKey, null,
                mPackName);
        Log.d("liuwenshuai","getLong->value"+value);
        return TextUtils.isEmpty(value) ? defaultValue : Long.parseLong(value);
    }

    /**
     * @return The elapsed time in millis in the future when the user is allowed
     *         to attempt to enter his/her lock pattern, or 0 if the user is
     *         welcome to enter a pattern.
     */
    public long getLockoutAttemptDeadline() {
        final long deadline = getLong(LOCKOUT_ATTEMPT_DEADLINE, 0L);
        final long now = SystemClock.elapsedRealtime();
        if (deadline < now || deadline > (now + FAILED_ATTEMPT_TIMEOUT_MS)) {
            return 0L;
        }
        return deadline;
    }

    /**
     * Retrieves the quality mode we're in. {@see
     * DevicePolicyManager#getPasswordQuality(android.content.ComponentName)}
     * 
     * @return stored password quality
     */
    public int getKeyguardStoredPasswordQuality() {
        int quality = (int) getLong(PASSWORD_TYPE_KEY,
                DevicePolicyManager.PASSWORD_QUALITY_SOMETHING);
        return quality;
    }

    public int getRequestedMinimumPasswordLength() {
        return getDevicePolicyManager().getPasswordMinimumLength(null);
    }

    /**
     * Gets the device policy password mode. If the mode is non-specific,
     * returns MODE_PATTERN which allows the user to choose anything.
     */
    public int getRequestedPasswordQuality() {
        return getDevicePolicyManager().getPasswordQuality(null);
    }

    public int getRequestedPasswordHistoryLength() {
        return getDevicePolicyManager().getPasswordHistoryLength(null);
    }

    public int getRequestedPasswordMinimumLetters() {
        return getDevicePolicyManager().getPasswordMinimumLetters(null);
    }

    public int getRequestedPasswordMinimumUpperCase() {
        return getDevicePolicyManager().getPasswordMinimumUpperCase(null);
    }

    public int getRequestedPasswordMinimumLowerCase() {
        return getDevicePolicyManager().getPasswordMinimumLowerCase(null);
    }

    public int getRequestedPasswordMinimumNumeric() {
        return getDevicePolicyManager().getPasswordMinimumNumeric(null);
    }

    public int getRequestedPasswordMinimumSymbols() {
        return getDevicePolicyManager().getPasswordMinimumSymbols(null);
    }

    public int getRequestedPasswordMinimumNonLetter() {
        return getDevicePolicyManager().getPasswordMinimumNonLetter(null);
    }
    /**
     * Check to see if a password matches any of the passwords stored in the
     * password history.
     * 
     * @param password
     *            The password to check.
     * @return Whether the password matches any in the history.
     */
    public boolean checkPasswordHistory(String password) {
        String passwordHashString = new String(passwordToHash(password));
        String passwordHistory = getString(PASSWORD_HISTORY_KEY);
        if (passwordHistory == null) {
            return false;
        }
        // Password History may be too long...
        int passwordHashLength = passwordHashString.length();
        int passwordHistoryLength = getRequestedPasswordHistoryLength();
        if (passwordHistoryLength == 0) {
            return false;
        }
        int neededPasswordHistoryLength = passwordHashLength
                * passwordHistoryLength + passwordHistoryLength - 1;
        if (passwordHistory.length() > neededPasswordHistoryLength) {
            passwordHistory = passwordHistory.substring(0,
                    neededPasswordHistoryLength);
        }
        return passwordHistory.contains(passwordHashString);
    }
    
    public String getNewPatternPath() {
        return mNewPatternPath;
    }
    public String getNewPasswordPath() {
        return mNewPasswordPath;
    }
}