package com.book.app.lock;

import java.util.List;

import com.book.app.lock.provider.AppInfo;
import com.book.app.lock.provider.AppInfoProvider;
import com.book.app.lock.provider.AppViewHolder;
import com.book.app.lock.provider.PreferencesProvider;
import book.support.v7.app.ActionBarActivity;
import book.support.v7.app.ActionBar;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import book.support.v7.app.ActionBarListActivity;


public class ManagerAppLock extends ActionBarListActivity {

    private List<AppInfo> appInfos;
    private List<String> lockappinfos;
    private AppInfoProvider provider;
    private MyAdapter adapter;
    private View mLoadingContainer;
    private View mListContainer;
    private boolean mPassWordConfirm;
    private static final int RESULTFROMSETPASSWORD = 1000;
    private static final int RESULTFROMCONFIRMPASSWORD = 1001;
    private static final int CONFIRM_EXISTING_REQUEST = 10002;
    private ListView mlistview;
    private Handler mHandler = new Handler();

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            adapter = new MyAdapter();
            mlistview.setAdapter(adapter);
            mLoadingContainer.setVisibility(View.INVISIBLE);
            mListContainer.setVisibility(View.VISIBLE);
        }

    };
    final Runnable mRunningProcessesAvail = new Runnable() {
        public void run() {
            Log.d("liuwenshuai", "mRunningProcessesAvail...");
            handleRunningProcessesAvail();
        }
    };

    private void handleRunningProcessesAvail() {
        // TODO Auto-generated method stub
        mLoadingContainer.startAnimation(AnimationUtils.loadAnimation(this,
                android.R.anim.fade_out));
    }

    private void initUI() {
        new Thread() {
            @Override
            public void run() {
                Log.d("liuwenshuai", "ManagerAppLock->getAllApps...Start");
                mHandler.post(mRunningProcessesAvail);
                appInfos = provider.getAllApps();
                Log.d("liuwenshuai", "ManagerAppLock->getAllApps...End");
                handler.sendEmptyMessage(0);
            }

        }.start();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.applock_activity_main);
        mLoadingContainer = findViewById(R.id.loading_container);
        mListContainer = findViewById(R.id.list_container);
        mLoadingContainer.setVisibility(View.VISIBLE);
        mlistview = (ListView) this.getListView();
        provider = new AppInfoProvider(this);
        // ActionBar actionBar = ((ActionBarActivity)getSupportActionBar());
        // actionBar.setTitle(R.string.manager_app_lock);
        // actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        initUI();
        mlistview.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                AppInfo info = (AppInfo) mlistview.getItemAtPosition(position);
                String packName = info.getPackname();
                PreferencesProvider.setDefaultPackage(getApplicationContext(),
                        packName);
                Intent intent = new Intent();
                intent.setClassName("com.book.app.lock",
                        "com.book.app.lock.ChangePassWord");
                startActivity(intent);
            }
        });
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        }
        return super.onKeyDown(keyCode, event);
    }

    public class MyAdapter extends BaseAdapter {
        public AppViewHolder holder;

        @Override
        public int getCount() {

            return appInfos.size();
        }

        @Override
        public Object getItem(int arg0) {
            return appInfos.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            holder = null;
            if (convertView == null) {
                holder = new AppViewHolder();
                convertView = convertView.inflate(getApplicationContext(),
                        R.layout.app_view_item, null);
                holder.AppIcon = (ImageView) convertView
                        .findViewById(R.id.AppIcon);
                holder.AppName = (TextView) convertView
                        .findViewById(R.id.AppName);
                convertView.setTag(holder);
            } else {
                holder = (AppViewHolder) convertView.getTag();
            }
            AppInfo info = appInfos.get(position);
            String appname = info.getPackname();
            Log.d("liuwenshuai", "getView->appname:" + appname);

            holder.AppIcon.setImageDrawable(info.getIcon());
            holder.AppName.setText(info.getAppname());

            return convertView;
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