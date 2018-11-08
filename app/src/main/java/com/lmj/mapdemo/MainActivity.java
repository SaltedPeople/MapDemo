package com.lmj.mapdemo;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.lmj.mapdemo.data.LocationEntity;
import com.lmj.mapdemo.loaction.MapLocationActivity;
import com.lmj.mapdemo.poi.PoiSearchActivity;
import com.lmj.mapdemo.util.FileUtil;
import com.lmj.mapdemo.util.LocationUtil;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1001; //权限请求code

    private TextView mAddressTv;

    private LocationEntity mSelectEntity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FileUtil.copyMapFromAssets(this);
        findViewById(R.id.enter_poi_map).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,PoiSearchActivity.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.enter_location_map).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MapLocationActivity.class);
                intent.putExtra(MapLocationActivity.SELECT_ENTITY,mSelectEntity);
                startActivityForResult(intent,MapLocationActivity.Location_REQUEST_CODE);
            }
        });
        mAddressTv = findViewById(R.id.select_address);
        requestPermission();

    }

    private void requestPermission(){

        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.ACCESS_FINE_LOCATION
                ,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION};
        //收集未授权或者拒绝过的权限
        ArrayList<String> deniedPermissionList = new ArrayList<>();
        for (String per : permissions) {
            int checkSelfPermission = ContextCompat.checkSelfPermission(this, per);
            if (checkSelfPermission == PackageManager.PERMISSION_DENIED) {
                deniedPermissionList.add(per);
            }
        }

        if (!deniedPermissionList.isEmpty()) {
            String[] permissionArray = deniedPermissionList.toArray(new String[0]);
            ActivityCompat.requestPermissions(this,permissionArray, PERMISSION_REQUEST_CODE);
        }
    }


    /**
     * 显示提示对话框
     */
    private void showTipsDialog(final String[] permissions) {
        new AlertDialog.Builder(this)
                .setTitle("提示信息")
                .setMessage("再次提醒设置")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissions(permissions,PERMISSION_REQUEST_CODE );
                    }
                }).show();
    }

    private void showSettingDialog() {
        new AlertDialog.Builder(this)
                .setTitle("提示信息")
                .setMessage("当前应用缺少必要权限，该功能暂时无法使用。如若需要，请单击【确定】按钮前往设置中心进行权限授权。")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startAppSettings();
                    }
                }).show();
    }

    /**
     * 启动当前应用设置页面
     */

    private void startAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull final String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (PERMISSION_REQUEST_CODE == requestCode) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    if (shouldShowRequestPermissionRationale(permissions[i])) {
                        showTipsDialog(permissions);
                    }else{
                        showSettingDialog();
                    }
                    return;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == MapLocationActivity.Location_REQUEST_CODE&&data!=null){
           mSelectEntity = data.getParcelableExtra(MapLocationActivity.SELECT_ENTITY);
           String address = mSelectEntity.province + mSelectEntity.city + mSelectEntity.street + mSelectEntity.address;
            mAddressTv.setText(getString(R.string.current_select_address,address));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocationUtil.getInstance().release();
    }
}
