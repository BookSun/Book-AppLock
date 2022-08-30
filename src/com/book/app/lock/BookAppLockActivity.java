package com.book.app.lock;

import java.util.ArrayList;
import java.util.List;
import book.support.v7.app.ActionBarActivity;
import book.support.v7.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView.RecyclerListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.app.Activity;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.view.MotionEvent;
import android.view.View.OnTouchListener;
import android.widget.CompoundButton;

import com.book.app.lock.provider.AppInfo;
import com.book.app.lock.provider.AppInfoProvider;
import com.book.app.lock.provider.AppViewHolder;
import com.book.app.lock.provider.bookAppLockHelper;
import com.book.app.lock.service.bookAppLockService;

import book.internal.v5.widget.SlidingButton;

public class bookAppLockActivity extends PreferenceActivity {

    private static final int OPTIONS_MENU_SETTINGS = Menu.FIRST;

    @Override
    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(EXTRA_SHOW_FRAGMENT,
                bookAppLockActivityFragment.class.getName());
        modIntent.putExtra(EXTRA_NO_HEADERS, true);
        return modIntent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService(new Intent(bookAppLockActivity.this,
                bookAppLockService.class));
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        if (bookAppLockActivityFragment.class.getName().equals(fragmentName))
            return true;
        return false;
    }

    public static class bookAppLockActivityFragment extends PreferenceFragment {

        private static final String PIM_PACKAGE = "com.book.PIM";
        private static final String CAMERA_PACKAGE = "com.book.gallery3d";
        private List<AppInfo> mAppInfos;
        private AppInfoProvider mAppInfoProvider;
        List<String> mLockAppPackage = new ArrayList<String>();
        private MyAdapter myAdapter;
        private View mLoadingContainer;
        private View mListContainer;
        private boolean mPassWordConfirm;
        private static final int RESULTFROMSETPASSWORD = 1000;
        private static final int RESULTFROMCONFIRMPASSWORD = 1001;
        private static final int CONFIRM_EXISTING_REQUEST = 10002;
        private static final String LOCK_VALUE = "locked";
        private ListView mListview;
        private IActivityManager mAm;
        private bookAppLockHelper mLockDB;
        private Handler mHandler = new Handler();
        private TextView mHeaderText;
        private int mAppNumber;
        private Context mContext;

        private Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                super.handleMessage(msg);
                mLoadingContainer.setVisibility(View.INVISIBLE);
                mListContainer.setVisibility(View.VISIBLE);
                myAdapter = new MyAdapter(mContext);
                mListview.setAdapter(myAdapter);
                myAdapter.clear();
                myAdapter.addAll(mAppInfos);
            }

        };
        final Runnable mRunningProcessesAvail = new Runnable() {
            public void run() {
                handleRunningProcessesAvail();
            }
        };

        private void handleRunningProcessesAvail() {
            // TODO Auto-generated method stub
            mLoadingContainer.startAnimation(AnimationUtils.loadAnimation(
                    getActivity(), android.R.anim.fade_out));
        }

        public boolean packageExist(String packaString) {
            String info = null;
            try {
                info = getActivity().getPackageManager().getPackageInfo(
                        packaString, PackageManager.GET_ACTIVITIES).packageName;
            } catch (NameNotFoundException e) {
                info = null;
                e.printStackTrace();
            }
            if (info == null || !info.equals(packaString)) {
                return false;
            } else {
                return true;
            }
        }

        private void initUI() {
            new Thread() {
                @Override
                public void run() {
                    mHandler.post(mRunningProcessesAvail);
                    mAppInfoProvider = new AppInfoProvider(getActivity());
                    mAppInfos = mAppInfoProvider.getSortLockApps();
                    handler.sendEmptyMessage(0);
                }

            }.start();
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            mContext = activity;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // ActionBar actionBar = ((ActionBarActivity)getSupportActionBar());
            // actionBar.setTitle(R.string.book_applock_title);
            // actionBar.setDisplayHomeAsUpEnabled(true);
            initUI();
            mAm = ActivityManagerNative.getDefault();
            mLockDB = new bookAppLockHelper(getActivity());
            mLockAppPackage = mLockDB.getAllLockedPackName(LOCK_VALUE);
            for (String appPackage : mLockAppPackage) {
                if (!packageExist(appPackage)) {
                    mLockDB.delete(appPackage);
                }
            }
            mAppNumber = mLockDB.getLockedAppNumber(LOCK_VALUE);
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.book_app_lock_activity, null);
            View headerView = inflater.inflate(R.layout.applock_header, null);
            mLoadingContainer = view.findViewById(R.id.loading_container);
            mListContainer = view.findViewById(R.id.list_container);
            mHeaderText = (TextView) headerView
                    .findViewById(R.id.applock_header);
            mHeaderText.setText(mAppNumber
                    + getString(R.string.app_lock_header));
            mLoadingContainer.setVisibility(View.VISIBLE);
            ListView lv = (ListView) view.findViewById(android.R.id.list);
            // lv.setOnItemClickListener(this);
            lv.setSaveEnabled(true);
            mListview = lv;
            mListview.addHeaderView(headerView);
            mListview.setRecyclerListener((RecyclerListener) myAdapter);
            return view;
        }

        @Override
        public void onResume() {
            // TODO Auto-generated method stub
            super.onResume();
        }

        public class MyAdapter extends ArrayAdapter<AppInfo> {
            private SlidingButton mLockSwitch;
            private final LayoutInflater mInflater;

            public MyAdapter(Context context) {
                super(context, 0);
                mInflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                final AppInfo info = getItem(position);
                final View appView = convertView != null ? convertView
                        : createAppInfoRow(parent);
                ((ImageView) appView.findViewById(R.id.app_icon))
                        .setImageDrawable(info.getIcon());
                TextView textView = ((TextView) appView
                        .findViewById(R.id.app_name));
                String packName = info.getPackname();
                if (packName.equals(PIM_PACKAGE)) {
                    textView.setText(getString(R.string.book_pim_name));
                } else if (packName.equals(CAMERA_PACKAGE)) {
                    textView.setText(getString(R.string.book_camera_name));
                } else {
                    textView.setText(info.getAppname());
                }
                mLockSwitch = (SlidingButton) appView
                        .findViewById(R.id.app_lock_switch);
                mLockSwitch.setChecked(info.getActive());
                mLockSwitch
                        .setOnCheckedChangedListener(new SlidingButton.OnCheckedChangedListener() {
                            public void onCheckedChanged(
                                    CompoundButton buttonView, boolean isChecked) {
                                String nameString = info.getPackname();
                                info.setActive(isChecked);
                                if (isChecked) {
                                    // try {
                                    //     mAm.addAppLockControlPackage(nameString);
                                    // } catch (RemoteException e) {
                                    //     // TODO: handle exception
                                    // }
                                    if (mLockDB != null) {
                                        mLockDB.addLLockedPackageName(
                                                LOCK_VALUE, nameString);
                                        updateHeaderView();
                                    }
                                } else {
                                    // try {
                                    //     mAm.removeAppLockControlPackage(nameString);
                                    // } catch (RemoteException e) {
                                    //     // TODO: handle exception
                                    // }
                                    if (mLockDB != null) {
                                        mLockDB.delete(nameString);
                                        updateHeaderView();
                                    }
                                }
                            }
                        });
                appView.setTag(info);
                return appView;
            }

            private void updateHeaderView() {
                mAppNumber = mLockDB.getLockedAppNumber(LOCK_VALUE);
                mHeaderText.setText(mAppNumber
                        + getString(R.string.app_lock_header));
            }

            private String getString(int resID) {
                return getActivity().getResources().getString(resID);
            }

            private void activate(AppInfo appInfo) {
                // if (appInfo.equals(getCurrentSelection()))
                // return;
                // for (int i = 0; i < getCount(); i++) {
                // getItem(i).setActive(false);
                // }
                appInfo.setActive(true);
                mLockSwitch.setChecked(appInfo.getActive());
            }

            private View createAppInfoRow(ViewGroup parent) {
                final View row = mInflater.inflate(R.layout.book_app_lock_item,
                        parent, false);
                row.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.setPressed(true);
                        Log.d("liuwenshuai", "onClick.....");
                        // activate((AppInfo) row.getTag());
                    }
                });
                return row;
            }
        }

        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            MenuItem actionItem = menu.add(Menu.NONE, OPTIONS_MENU_SETTINGS, 0,
                    R.string.app_lock_settings).setIcon(
                    R.drawable.ic_menu_settings);
            actionItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            super.onCreateOptionsMenu(menu, inflater);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            final int itemId = item.getItemId();
            if (itemId == android.R.id.home) {
                getActivity().finish();
                return true;
            } else if (itemId == OPTIONS_MENU_SETTINGS) {
                Intent newIntent = new Intent();
                newIntent.setClassName("com.book.app.lock",
                        "com.book.app.lock.bookAppLockSettings");
                getActivity().startActivity(newIntent);
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
}