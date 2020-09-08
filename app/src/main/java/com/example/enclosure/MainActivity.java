package com.example.enclosure;

import android.Manifest;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.AMapLocationQualityReport;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.UiSettings;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.Polygon;
import com.amap.api.maps2d.model.PolygonOptions;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MainActivity extends AppCompatActivity implements AMap.OnMapClickListener,
        AMap.OnMapLongClickListener, AMap.OnCameraChangeListener {

    //权限设置
    private static final int REQUEST_PERMISSION_LOCATION = 0;
    //声明AMapLocationClient类对象
    AMapLocationClient mLocationClient = null;
    //声明AMapLocationClientOption对象
    public AMapLocationClientOption mLocationOption = null;

    private TextView tv_action;
    private MapView mMapView = null;
    private AMap aMap = null;
    private UiSettings mUiSettings;                     //定位按钮
    private Marker marker = null;
    private List<Marker> mMarkers = new ArrayList<>();
    private List<LatLng> latLngs = new ArrayList<>();     //坐标列表,可以用latLngs.size()获取点数
    private Polygon polygon;                                //圈地封闭区域
    private Button btn_click;
    private Button btn_down;
    private Button btn_maker;
    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;
    protected String[] needPermissions = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE
    };

    private static final int PERMISSON_REQUESTCODE = 0;

    /**
     * 判断是否需要检测，防止不停的弹框
     */
    private boolean isNeedCheck = true;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("大师地图");
        setContentView(R.layout.activity_main);
        mMapView = findViewById(R.id.mapview);       //获取地图控件引用
        mMapView.onCreate(savedInstanceState);// 此方法须覆写，虚拟机需要在很多情况下保存地图绘制的当前状态。
        init();
        //初始化定位
        initLocation();
        //获取权限
        getPermissions();
    }
    private void getPermissions() {
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE
        )
                .subscribe(granted -> {
                    if (granted) {
                        Toast.makeText(getApplicationContext(), "已经获取所需权限", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "未能获取所需权限", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    /**
     * 初始化AMap对象 - 地图控制器
     */

    private void init() {
        if (aMap == null) {
            aMap = mMapView.getMap();
            setUpMap();
        }
    }

    /**
     * 设置一些aMap的属性,添加一些事件监听器
     */
    private void setUpMap() {
        mUiSettings = aMap.getUiSettings();//实例化UiSettings类对象
        aMap.setMapType(AMap.MAP_TYPE_SATELLITE);       //卫星地图模式

        //定位按钮
        mUiSettings.setMyLocationButtonEnabled(true); //显示默认的定位按钮
        mUiSettings.setZoomControlsEnabled(false);  //取消显示默认的缩放按钮
        aMap.setMyLocationEnabled(true);// 可触发定位并显示当前位置


        btn_click = findViewById(R.id.btn_gps);
        btn_down = findViewById(R.id.btn_reset);
        btn_maker = findViewById(R.id.btn_maker);

        btn_click.setOnClickListener(new MyOnClickListener());
        btn_down.setOnClickListener(new MyOnClickListener());
        btn_maker.setOnClickListener(new MyOnClickListener());

        aMap.setOnMapClickListener(this);// 对amap添加单击地图事件监听器
        aMap.setOnMapLongClickListener(this);// 对amap添加长按地图事件监听器
        aMap.setOnCameraChangeListener(this);// 对amap添加移动地图事件监听器
    }


    /**
     * 对单击地图事件回调，缩放会误触发????
     */
    @Override
    public void onMapClick(LatLng point) {
        //Toast.makeText(getApplicationContext(), "tapped, point=" + point, Toast.LENGTH_SHORT).show();
        latLngs.add(point);
        marker = aMap.addMarker(new MarkerOptions().position(point).title("").snippet("DefaultMarker"));        //在地图上标记点
        marker.setSnippet(marker.getId() + marker.getPosition());
        mMarkers.add(marker);

        //手动绘制圈地部分，如果边框的起点与终点不一致，API会自动将它封闭。test best
        if (latLngs.size() >= 3) {
            if (polygon != null)         //清除上一次的图形，避免重叠变丑
            {
                polygon.remove();
            }
            polygon = aMap.addPolygon(new PolygonOptions().addAll(Collections.unmodifiableList(latLngs))
                    .strokeColor(Color.argb(50, 1, 1, 1))
                    .fillColor(Color.argb(50, 1, 1, 1)));
            aMap.invalidate();//刷新地图
        }
    }


    /**
     * 按钮短按时回调
     */
    class MyOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) { // 点击事件的处理方法
            if (v.getId() == R.id.btn_gps) {        //完成，弹窗显示点位坐标和面积，请求输入
                if (aMap.isMyLocationEnabled()) {
                    getArea(latLngs);
                }
            } else if (v.getId() == R.id.btn_reset)        //撤销
            {
                clearAll();
            } else if (v.getId() == R.id.btn_maker)        //gps采点
            {
                gpsMaker();
            }
        }
    }
    /**
     * 初始化定位
     */
    private void initLocation(){
        //初始化client
        locationClient = new AMapLocationClient(this.getApplicationContext());
        locationOption = getDefaultOption();
        //设置定位参数
        locationClient.setLocationOption(locationOption);
        // 设置定位监听
        locationClient.setLocationListener(locationListener);
    }
    /**
     * 默认的定位参数
     * @since 2.8.0
     * @author hongming.wang
     *
     */
    private AMapLocationClientOption getDefaultOption(){
        AMapLocationClientOption mOption = new AMapLocationClientOption();
        mOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);//可选，设置定位模式，可选的模式有高精度、仅设备、仅网络。默认为高精度模式
        mOption.setGpsFirst(false);//可选，设置是否gps优先，只在高精度模式下有效。默认关闭
        mOption.setHttpTimeOut(30000);//可选，设置网络请求超时时间。默认为30秒。在仅设备模式下无效
        mOption.setInterval(2000);//可选，设置定位间隔。默认为2秒
        mOption.setNeedAddress(true);//可选，设置是否返回逆地理地址信息。默认是true
        mOption.setOnceLocation(true);//可选，设置是否单次定位。默认是false
        mOption.setOnceLocationLatest(true);//可选，设置是否等待wifi刷新，默认为false.如果设置为true,会自动变为单次定位，持续定位时不要使用
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTP);//可选， 设置网络请求的协议。可选HTTP或者HTTPS。默认为HTTP
        mOption.setSensorEnable(false);//可选，设置是否使用传感器。默认是false
        mOption.setWifiScan(true); //可选，设置是否开启wifi扫描。默认为true，如果设置为false会同时停止主动刷新，停止以后完全依赖于系统刷新，定位位置可能存在误差
        mOption.setLocationCacheEnable(true); //可选，设置是否使用缓存定位，默认为true
        mOption.setGeoLanguage(AMapLocationClientOption.GeoLanguage.DEFAULT);//可选，设置逆地理信息的语言，默认值为默认语言（根据所在地区选择语言）
        return mOption;
    }
    /**
     * 定位监听
     */
    AMapLocationListener locationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation location) {
            if (null != location) {
                StringBuffer sb = new StringBuffer();
                //errCode等于0代表定位成功，其他的为定位失败，具体的可以参照官网定位错误码说明
                if(location.getErrorCode() == 0){
                    double lon=location.getLongitude();
                    double lat=location.getLatitude();
                    LatLng ll=new LatLng(lon,lat);
                    latLngs.add(ll);
                    marker = aMap.addMarker(new MarkerOptions().position(ll).title("").snippet("DefaultMarker"));        //在地图上标记点
                    marker.setSnippet(marker.getId() + marker.getPosition());
                    mMarkers.add(marker);
                    Toast.makeText(getApplicationContext(), "经度："+lon+"\n纬度：" +lat, Toast.LENGTH_SHORT).show();

                } else {
                    //定位失败
                    sb.append("定位失败" + "\n");
                    sb.append("错误码:" + location.getErrorCode() + "\n");
                    sb.append("错误信息:" + location.getErrorInfo() + "\n");
                    sb.append("错误描述:" + location.getLocationDetail() + "\n");
                    //解析定位结果，
                    String result = sb.toString();
                    //定位失败
                    Toast.makeText(getApplicationContext(), result , Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "定位失败，地点不存在" , Toast.LENGTH_SHORT).show();
            }
        }
    };
    /**
     * 获取GPS状态的字符串
     * @param statusCode GPS状态码
     * @return
     */
    private String getGPSStatusString(int statusCode){
        String str = "";
        switch (statusCode){
            case AMapLocationQualityReport.GPS_STATUS_OK:
                str = "GPS状态正常";
                break;
            case AMapLocationQualityReport.GPS_STATUS_NOGPSPROVIDER:
                str = "手机中没有GPS Provider，无法进行GPS定位";
                break;
            case AMapLocationQualityReport.GPS_STATUS_OFF:
                str = "GPS关闭，建议开启GPS，提高定位质量";
                break;
            case AMapLocationQualityReport.GPS_STATUS_MODE_SAVING:
                str = "选择的定位模式中不包含GPS定位，建议选择包含GPS定位的模式，提高定位质量";
                break;
            case AMapLocationQualityReport.GPS_STATUS_NOGPSPERMISSION:
                str = "没有GPS定位权限，建议开启gps定位权限";
                break;
        }
        return str;
    }

    public void getArea(List<LatLng> latLngs) {
        double area = 0;
        //to do
        int num = latLngs.size();
        int j, k;//开始第一步排序
        for (j = 0; j < num - 1; j++) {
            for (k = 0; k < num - 1 - j; k++) {
                if (latLngs.get(k).longitude > latLngs.get(k + 1).longitude)
                    Collections.swap(latLngs, k, (k + 1));
            }
            int m, n;//开始逆时针排序
            for (m = 1; m < num - 1; m++) {
                for (n = 1; n < num - m; n++) {
                    double cos1 = (latLngs.get(n).latitude - latLngs.get(0).latitude) / Math.sqrt(Math.pow((latLngs.get(n).latitude - latLngs.get(0).latitude), 2) + Math.pow((latLngs.get(n).longitude - latLngs.get(0).longitude), 2));
                    double cos2 = (latLngs.get(n + 1).latitude - latLngs.get(0).latitude) / Math.sqrt(Math.pow((latLngs.get(n + 1).latitude - latLngs.get(0).latitude), 2) + Math.pow((latLngs.get(n + 1).longitude - latLngs.get(0).longitude), 2));
                    if (cos1 > cos2)
                        Collections.swap(latLngs, n, (n + 1));
                }
            }
            area = latLngs.get(num - 1).latitude * latLngs.get(0).longitude - latLngs.get(0).latitude * latLngs.get(num - 1).longitude;
            for (int h = 0; h < num - 1; h++) {
                area = area + latLngs.get(h).latitude * latLngs.get(h + 1).longitude - latLngs.get(h).longitude * latLngs.get(h + 1).latitude;
            }
            area = 0.5 * Math.abs(area) * 9101160000.085981 / 1000000;

        }

        Toast.makeText(getApplicationContext(), "取点个数：" + latLngs.size() + "\n面积 ： " + area + "平方千米", Toast.LENGTH_SHORT).show();
    }


    public void gpsMaker() {
        //根据控件的选择，重新设置定位参数
        //resetOption();

        // 设置定位参数
        locationClient.setLocationOption(locationOption);
        // 启动定位
        locationClient.startLocation();
/*
        //定位到当前位置并标记Maker
        //参考onMapClick(）
        //Toast.makeText(getApplicationContext(), "GPS采点接口" , Toast.LENGTH_SHORT).show();*/
    }
    /**
     * 定位回调监听器
     */
    private void stopLocation(){
        // 停止定位
        locationClient.stopLocation();
    }

    /**
     * 销毁
     *
     */
    private void destroyLocation(){
        if (null != locationClient) {
            /**
             * 如果AMapLocationClient是在当前Activity实例化的，
             * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
             */
            locationClient.onDestroy();
            locationClient = null;
            locationOption = null;
        }
    }
    public void clearAll()
    {
        if(latLngs.size() != 0)  //清除全部的多边形和点
        {
            polygon.remove();                       //删除多边形
            for (Marker marker : mMarkers) {        //遍历删除点
                //marker.remove();
                marker.destroy();
            }
            mMarkers.clear();                       //删除点集合
            latLngs.clear();                        //删除坐标集合
            aMap.invalidate();//刷新地图
        }
        else
        {Toast.makeText(getApplicationContext(), "尚未标记采点，无需撤销点位" , Toast.LENGTH_SHORT).show();}
    }
    /**
     * 对长按地图事件回调
     */
    @Override
    public void onMapLongClick(LatLng point) {
//        latLngs.add(point);
//        marker = aMap.addMarker(new MarkerOptions().position(point).title("").snippet("DefaultMarker"));        //在地图上标记点
//        marker.setSnippet(marker.getId()+marker.getPosition());
//        mMarkers.add(marker);
//
//        //Toast.makeText(getApplicationContext(),  "long pressed, point=" + point, Toast.LENGTH_SHORT).show();
//
//        //手动绘制圈地部分，如果边框的起点与终点不一致，API会自动将它封闭。test best
//        if(latLngs.size() >= 3)
//        {
//            if(polygon != null)         //清除上一次的图形，避免重叠变丑
//            {polygon.remove();}
//            polygon = aMap.addPolygon(new PolygonOptions().addAll(Collections.unmodifiableList(latLngs))
//                    .strokeColor(Color.argb(50, 1, 1, 1))
//                    .fillColor(Color.argb(50, 1, 1, 1)));
//            aMap.invalidate();//刷新地图
//        }
    }

    /**
     * 对正在移动地图事件回调
     */
    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        //Toast.makeText(getApplicationContext(), "onCameraChange:" + cameraPosition.toString(), Toast.LENGTH_SHORT).show();
    }

    /**
     * 对移动地图结束事件回调
     */
    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }


}