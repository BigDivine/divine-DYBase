package com.divine.base.utils.sys;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.File;


import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static android.app.Activity.RESULT_OK;

/**
 * Author: Divine
 * CreateDate: 2020/10/20
 * Describe:
 */
public class GetImgUtils {
    private OnGetImgSuccess onGetImgSuccess;
    private String mCameraFilePath;
    private static final int CARMER = 1002;
    private static final int IMG_FILE = 1003;
    private static final int FILE_REQUEST = 10001;
    private static final int CARMER_REQUEST = 10002;
    private Activity context;

    public GetImgUtils(Activity context) {
        this.context = context;
    }

    public interface OnGetImgSuccess {
        void onSuccess(String mCameraFilePath);
    }

    public void setOnGetImgSuccess(OnGetImgSuccess onGetImgSuccess) {
        this.onGetImgSuccess = onGetImgSuccess;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CARMER://相机
                    onGetImgSuccess.onSuccess(mCameraFilePath);
                    break;
                case IMG_FILE://图片选择
                    Uri uri = data.getData();
                    android.util.Log.e("imgPath", uri.getPath());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {//4.4以上
                        /**根据Uri获取图片的路径，包含文件名和扩展名*/
                        String[] proj = {MediaStore.Images.Media.DATA};
                        ContentResolver cr = context.getContentResolver();
                        Cursor cursor = cr.query(uri, proj, null, null, null);
                        if (cursor == null) {//部分4.4机型问题
                            mCameraFilePath = data.getData().getPath();
                        } else {
                            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                            cursor.moveToFirst();
                            //最后根据索引值获取图片路径
                            mCameraFilePath = cursor.getString(column_index);
                            cursor.close();
                        }
                        android.util.Log.e("imgPath", mCameraFilePath);
                    } else {//4.4，即4.4以下获取路径的方法
                        mCameraFilePath = data.getData().getPath();
                        android.util.Log.e("imgPath4.4以下", mCameraFilePath);
                    }
                    onGetImgSuccess.onSuccess(mCameraFilePath);
                    break;
                default:
                    break;
            }
        }
    }


    /**
     * 6.0 手机系统以上 检查并请求权限
     * 相机选择
     */
    public void openCamera() {
        int i = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA);
        int y = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (i != PackageManager.PERMISSION_GRANTED && y != PackageManager.PERMISSION_GRANTED) {//
            ActivityCompat.requestPermissions(context,
                                              new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, CARMER_REQUEST);
        } else if (i != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.CAMERA}, CARMER_REQUEST);
        } else if (y != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, CARMER_REQUEST);
        } else {
            doNext();
        }
    }

    /**
     * 6.0 手机系统以上 检查并请求权限
     * 文件选择
     */
    public void openFileChoice() {
        int y = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (y != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context,
                                              new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, FILE_REQUEST
            );
        } else {
            doFileNext();
        }
    }

    /**
     * 创建调用照相机的intent
     *
     * @return
     */
    private void doNext() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File externalDataDir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        System.out.println("externalDataDir:" + externalDataDir);
        File cameraDataDir = new File(externalDataDir.getAbsolutePath()
                                              + File.separator + "browser-photo");
        cameraDataDir.mkdirs();
        mCameraFilePath = cameraDataDir.getAbsolutePath() + File.separator
                + System.currentTimeMillis() + ".jpg";
        //判断是否是AndroidN以及更高的版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //            Uri contentUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileProvider", new File(mCameraFilePath));
            //            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri);
        } else {
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                  Uri.fromFile(new File(mCameraFilePath)));
        }
        context.startActivityForResult(Intent.createChooser(cameraIntent, "相机选择"), CARMER);
    }

    private void doFileNext() {
        Intent i = createFileIntent();
        context.startActivityForResult(Intent.createChooser(i, "图片选择"), IMG_FILE);
    }

    /**
     * 创建选择图库的intent
     *
     * @return
     */
    private Intent createFileIntent() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        Intent intent = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setDataAndType(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "image/*");
        return intent;
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == CARMER_REQUEST) {//申请相机权限回调结果
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(context, "请打开拍照和读取本地文件权限", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            doNext();
        }
        if (requestCode == FILE_REQUEST) {//文件选择回调
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(context, "请打开读取本地文件权限", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            doFileNext();
        }
    }
}

