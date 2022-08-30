/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0  
 *      @author wsliu@booktek.com
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.book.app.lock;

import android.app.Activity;
import android.app.WallpaperManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.content.Context;
import android.graphics.drawable.StateListDrawable;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import com.book.app.lock.utils.bookColorfulAdapter;

public class NumberPasswordManager implements OnClickListener {
    private static NumberPasswordManager nkManager;
    private TextView btn_one, btn_two, btn_three, btn_four, btn_five, btn_six,
            btn_seven, btn_eight, btn_neigh, btn_zero, btn_dele;
    private OnNumberClickListener onNumberClickListener;

    private NumberPasswordManager() {

    }

    public static NumberPasswordManager getInstance() {
        if (nkManager == null) {
            nkManager = new NumberPasswordManager();
        }
        return nkManager;
    }

    public StateListDrawable addStateDrawable(Context context) {
        StateListDrawable sd = new StateListDrawable();
        Drawable normal = context.getResources().getDrawable(
                R.drawable.circle_normal);
        Drawable pressed = context.getResources().getDrawable(
                R.drawable.circle_pressed);
        Drawable focus = context.getResources().getDrawable(
                R.drawable.circle_pressed);
        focus = pressed = bookColorfulAdapter.getColorDrawable(context,pressed);
        sd.addState(new int[] { android.R.attr.state_enabled,
                android.R.attr.state_focused }, focus);
        sd.addState(new int[] { android.R.attr.state_pressed,
                android.R.attr.state_enabled }, pressed);
        sd.addState(new int[] { android.R.attr.state_focused }, focus);
        sd.addState(new int[] { android.R.attr.state_pressed }, pressed);
        sd.addState(new int[] { android.R.attr.state_enabled }, normal);
        sd.addState(new int[] {}, normal);
        return sd;
    }

    public void initView(View ac,Context context,OnNumberClickListener onNumberClickListener) {
        btn_one = (TextView) ac.findViewById(R.id.btn_one);// 1
        btn_two = (TextView) ac.findViewById(R.id.btn_two);// 2
        btn_three = (TextView) ac.findViewById(R.id.btn_three);// 3
        btn_four = (TextView) ac.findViewById(R.id.btn_four);// 4
        btn_five = (TextView) ac.findViewById(R.id.btn_five);// 5
        btn_six = (TextView) ac.findViewById(R.id.btn_six);// 6
        btn_seven = (TextView) ac.findViewById(R.id.btn_seven);// 7
        btn_eight = (TextView) ac.findViewById(R.id.btn_eight);// 8
        btn_neigh = (TextView) ac.findViewById(R.id.btn_neigh);// 9
        btn_zero = (TextView) ac.findViewById(R.id.btn_zero);// 0
        btn_dele = (TextView) ac.findViewById(R.id.btn_delete);
        btn_one.setBackground(addStateDrawable(context));
        btn_two.setBackground(addStateDrawable(context));
        btn_three.setBackground(addStateDrawable(context));
        btn_four.setBackground(addStateDrawable(context));
        btn_five.setBackground(addStateDrawable(context));
        btn_six.setBackground(addStateDrawable(context));
        btn_seven.setBackground(addStateDrawable(context));
        btn_eight.setBackground(addStateDrawable(context));
        btn_neigh.setBackground(addStateDrawable(context));
        btn_zero.setBackground(addStateDrawable(context));
        btn_dele.setBackground(addStateDrawable(context));
        if (onNumberClickListener != null) {
            this.onNumberClickListener = onNumberClickListener;
            btn_one.setOnClickListener(this);// 1
            btn_two.setOnClickListener(this);// 2
            btn_three.setOnClickListener(this);// 3
            btn_four.setOnClickListener(this);// 4
            btn_five.setOnClickListener(this);// 5
            btn_six.setOnClickListener(this);// 6
            btn_seven.setOnClickListener(this);// 7
            btn_eight.setOnClickListener(this);// 8
            btn_neigh.setOnClickListener(this);// 9
            btn_zero.setOnClickListener(this);// 0
        }
    }

    public interface OnNumberClickListener {
        void clickedNumber(int number);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.btn_one:// 1
            if (onNumberClickListener != null) {
                onNumberClickListener.clickedNumber(1);
            }
            break;
        case R.id.btn_two:// 2
            if (onNumberClickListener != null) {
                onNumberClickListener.clickedNumber(2);
            }
            break;
        case R.id.btn_three:// 3
            if (onNumberClickListener != null) {
                onNumberClickListener.clickedNumber(3);
            }
            break;
        case R.id.btn_four:// 4
            if (onNumberClickListener != null) {
                onNumberClickListener.clickedNumber(4);
            }
            break;
        case R.id.btn_five:// 5
            if (onNumberClickListener != null) {
                onNumberClickListener.clickedNumber(5);
            }
            break;
        case R.id.btn_six:// 6
            if (onNumberClickListener != null) {
                onNumberClickListener.clickedNumber(6);
            }
            break;
        case R.id.btn_seven:// 7
            if (onNumberClickListener != null) {
                onNumberClickListener.clickedNumber(7);
            }
            break;
        case R.id.btn_eight:// 8
            if (onNumberClickListener != null) {
                onNumberClickListener.clickedNumber(8);
            }
            break;
        case R.id.btn_neigh:// 9
            if (onNumberClickListener != null) {
                onNumberClickListener.clickedNumber(9);
            }
            break;
        case R.id.btn_zero:// 0
            if (onNumberClickListener != null) {
                onNumberClickListener.clickedNumber(0);
            }
            break;
        default:
            break;
        }
    }

}
