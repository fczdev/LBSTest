package com.example.lbstest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public LocationClient mLocationClient;

    private TextView positionText;

    private TextureMapView mapView;

    private BaiduMap baiduMap;

    private boolean isFirstLocate = true;//是否首次定位   默认是

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationLister());
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        mapView =(TextureMapView) findViewById (R.id.bmapView) ;
        positionText = (TextView)findViewById(R.id.position_text_view);
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);
        List<String> permissionList = new ArrayList<>();
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.READ_PHONE_STATE)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if(!permissionList.isEmpty()){
            String [] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this,permissions,1);
        }else {
            requestLocation();
        }
    }

    //导航到当前坐标位置
    private void navigateTo(BDLocation location){
//            if(isFirstLocate) {
//                //将BDLocation对象中封装的地理位置信息到LatLng对象中
//                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//                Log.i("纬度： ", String.valueOf(location.getLatitude()));
//                Log.i("经度： ", String.valueOf(location.getLongitude()));
//                MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(latLng);
//                baiduMap.animateMapStatus(update);
//                update = MapStatusUpdateFactory.zoomTo(16f);
//                baiduMap.animateMapStatus(update);
//                isFirstLocate = false;
//            }
//
//            MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
//            locationBuilder.latitude(location.getLatitude());
//            locationBuilder.longitude(location.getLongitude());
//            MyLocationData locationData = locationBuilder.build();
//            baiduMap.setMyLocationData(locationData);
            if(isFirstLocate) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                MapStatus mMapStatus = new MapStatus.Builder().target(latLng).zoom(16f).build();
                MapStatusUpdate mMapStatusUpdata = MapStatusUpdateFactory.newMapStatus(mMapStatus);
                baiduMap.setMapStatus(mMapStatusUpdata);
                isFirstLocate = false;
            }
            MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
            locationBuilder.latitude(location.getLatitude());
            locationBuilder.longitude(location.getLongitude());
            MyLocationData locationData = locationBuilder.build();
            baiduMap.setMyLocationData(locationData);
    }
    private void requestLocation() {
        //初始化当前位置
        initLocation();
        mLocationClient.start();
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        //设置更新的间隔  5000表示每五秒更新一次当前的位置
        option.setScanSpan(5000);
        //将定位模式指定为传感器模式
        //option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);
        //需要获取当前位置的详细地址信息
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //用stop来停止定位  不然程序会持续在后台不停地进行定位  从而严重消耗手机的电量
        mLocationClient.stop();
        mapView.onDestroy();
        baiduMap.setMyLocationEnabled(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if(result!=PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(this, "必须同意所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                }
                    requestLocation();
            }else {
                    Toast.makeText(this,"发生未知错误",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    private class MyLocationLister implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            StringBuilder currentPosition = new StringBuilder();
            currentPosition.append("纬度: ").append(location.getLatitude()).append("\n");
            currentPosition.append("经度: ").append(location.getLongitude()).append("\n");
            currentPosition.append("国家: ").append(location.getCountry()).append("\n");
            currentPosition.append("省份: ").append(location.getProvince()).append("\n");
            currentPosition.append("市: ").append(location.getCity()).append("\n");
            currentPosition.append("区: ").append(location.getDistrict()).append("\n");
            currentPosition.append("街道: ").append(location.getStreet()).append("\n");

            currentPosition.append("定位方式: ");
            if(location.getLocType() == BDLocation.TypeGpsLocation){
                currentPosition.append("GPS");
            }else if(location.getLocType() == BDLocation.TypeNetWorkLocation){
                currentPosition.append("网络");
            }
            positionText.setText(currentPosition);
            if(location.getLocType() == BDLocation.TypeGpsLocation||location.getLocType()==BDLocation.TypeNetWorkLocation){
                navigateTo(location);
            }
        }
    }
}
