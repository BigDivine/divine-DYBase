package com.divine.dy.lib_base.base;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Author: Divine
 * CreateDate: 2020/9/29
 * Describe:
 */

public abstract class BaseFragment extends Fragment {
    private final String TAG = "D-BaseFragment";

    public Context mContext;
    // 可以对控件进行初始化
    protected abstract void initView(View view);
    // 对数据进行初始化
    protected abstract void getData();
    // 布局id
    public abstract int getContentView();

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getContentView(), null);
        initView(view);
        getData();
        return view;
    }
}
