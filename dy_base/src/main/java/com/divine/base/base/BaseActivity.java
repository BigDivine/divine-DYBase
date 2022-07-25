package com.divine.base.base;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.divine.base.AppConstants;
import com.divine.base.R;
import com.divine.base.SecurityCheck;
import com.divine.base.getpermission.PermissionPageUtils;
import com.divine.base.getpermission.PermissionUtil;
import com.divine.base.utils.ActivitiesManager;
import com.divine.base.utils.ui.DialogUtils;
import com.divine.base.utils.ui.ToastUtils;

import java.util.List;

/**
 * Create by BigDivine on 2020/10/10
 */
public abstract class BaseActivity extends AppCompatActivity {
    private final String TAG = "DY-BaseActivity";
    // activity manager class:activity管理类
    private ActivitiesManager activitiesManager;
    // current time by millisecond:当前时间毫秒数
    private long currentTimeMillis = 0;
    // need to request permissions dynamically:需要动态申请的权限
    private String[] requestPermissions;
    private BaseToolbar mBaseToolbar;

    /**
     * 获取布局的id
     *
     * @return layoutResId like：R.layout.main_activity
     */
    public abstract int getContentViewId();

    public abstract boolean showToolbar();

    public BaseToolbar getBaseToolbar() {
        if (null == mBaseToolbar) {
            mBaseToolbar = new BaseToolbar(this);
        }
        return mBaseToolbar;
    }

    /**
     * 初始化控件
     */
    public abstract void initView();

    /**
     * 页面发出请求，获取页面的数据
     */
    public void getData() {
    }

    /**
     * 需要动态获取的权限
     *
     * @return
     */
    public String[] requestPermissions() {
        return new String[0];
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getContentViewId());
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.LoginThemeColor));
        if (showToolbar()) {
            View mToolbar = getBaseToolbar().getToolbar();
            getSupportActionBar().show();
            getSupportActionBar().setBackgroundDrawable(null);
            // 显示自定义视图
            getSupportActionBar().setDisplayShowCustomEnabled(true);
            // 自定义视图
            getSupportActionBar().setCustomView(mToolbar);
            //to avoid the space in tab left and right side
            Toolbar parent = (Toolbar) mToolbar.getParent();
            parent.setContentInsetsAbsolute(0, 0);
        } else {
            getSupportActionBar().hide();
        }

        activitiesManager = ActivitiesManager.getInstance();
        activitiesManager.addActivity(this);

        //手机root权限检查
        if (SecurityCheck.isRoot()) {
            DialogUtils.showConfirmDialog(this
                    , "提示"
                    , "您的手机处于Root状态，不允许应用APP，请解除Root状态后应用"
                    , (dialog, which) -> {
                        dialog.dismiss();
                        this.finish();
                    }
            );
            return;
        }
        //app应用签名校验,通过SHA1来验证
        if (!SecurityCheck.signCheck(this)) {
            DialogUtils.showConfirmDialog(this
                    , "提示"
                    , "您的应用签名信息验证失败，不允许使用，请下载官方版本使用"
                    , (dialog, which) -> {
                        dialog.dismiss();
                        this.finish();
                    }
            );
            return;
        }

        requestPermissions = requestPermissions();
        // 获取未授权的权限
        String[] deniedPermissions = PermissionUtil.getDeniedPermissions(this, requestPermissions);
        if (deniedPermissions != null && deniedPermissions.length > 0) {
            // 弹框请求权限
            PermissionUtil.requestPermissions(this, requestPermissions, AppConstants.REQUEST_CODE_PERMISSION);
        } else {
            initView();
        }
    }

    @Override
    protected void onStart() {
        getData();
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        boolean background = !isForeground(this);
        if (background)
            ToastUtils.showShort(this, "当前应用已转到后台运行");
    }

    /**
     * 判断当前应用在前台
     */
    protected boolean isForeground(Context mContext) {
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processes = am.getRunningAppProcesses();
        if (processes == null) {
            return false;
        }
        for (ActivityManager.RunningAppProcessInfo app : processes) {
            if (app.processName.equals(mContext.getPackageName()) &&
                    app.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == AppConstants.REQUEST_CODE_PERMISSION) {
            boolean isAllGranted = true;//是否全部权限已授权
            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_DENIED) {
                    isAllGranted = false;
                    break;
                }
            }
            if (isAllGranted) {
                //已全部授权
                initView();
            } else {
                // 获取未授权的权限
                //                String[] deniedPermissions = PermissionUtil.getDeniedPermissions(this, requestPermissions);
                //                if (deniedPermissions != null && deniedPermissions.length > 0) {
                //                    // 弹框请求权限
                //                    PermissionUtil.requestPermissions(this, requestPermissions, AppConstants.REQUEST_CODE_PERMISSION);
                //                }
                //权限有缺失
                new AlertDialog.Builder(this)
                        .setMessage("跳转到设置页面允许权限，否则无法正常使用。")
                        .setTitle("授权提示")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new PermissionPageUtils(activitiesManager.currentActivity()).jumpPermissionPage();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .create()
                        .show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onBackPressed() {
        currentTimeMillis = System.currentTimeMillis();
        if (activitiesManager.getActivityStack().size() == 1) {
            if (System.currentTimeMillis() - currentTimeMillis <= 2000) {
                activitiesManager.AppExit(this);
            } else {
                Toast.makeText(this, "再按一次返回键，退出应用", Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onBackPressed();
        }
    }

    public void startActivity(Class targetActivity, Bundle bundle) {
        Intent intent = new Intent(this, targetActivity);
        if (bundle != null) {
            intent.putExtra("bundle", bundle);
        }
        startActivity(intent);
    }
}
