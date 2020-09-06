package com.example.enclosure;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.UiSettings;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.Polygon;
import com.amap.api.maps2d.model.PolygonOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MainActivity extends AppCompatActivity implements AMap.OnMapClickListener,
        AMap.OnMapLongClickListener, AMap.OnCameraChangeListener {
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
    private static final int GAODE_READ_PHONE_STATE = 100;//定位权限请求
    private static final int PRIVATE_CODE = 1315;//开启GPS权限
    static final String[] LOCATIONGPS = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("大师地图");
        setContentView(R.layout.activity_main);
        mMapView = findViewById(R.id.mapview);       //获取地图控件引用
        mMapView.onCreate(savedInstanceState);// 此方法须覆写，虚拟机需要在很多情况下保存地图绘制的当前状态。
        init();
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

        Toast.makeText(getApplicationContext(), " point num：" + latLngs.size() + "Area ： " + area + "平方千米", Toast.LENGTH_SHORT).show();
    }
    public void showGPSContacts() {
        LocationManager lm = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);
        boolean ok = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (ok) {//开了定位服务
            if (Build.VERSION.SDK_INT >= 23) { //判断是否为android6.0系统版本，如果是，需要动态添加权限
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {// 没有权限，申请权限。
                    ActivityCompat.requestPermissions(this, LOCATIONGPS,
                            GAODE_READ_PHONE_STATE);
                } else {
                    gpsMaker();//getLocation为定位方法
                }
            } else {
                gpsMaker();//getLocation为定位方法
            }
        } else {
            Toast.makeText(this, "系统检测到未开启GPS定位服务,请开启", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, PRIVATE_CODE);
        }
    }
    private void updateLocation(Location lc) {
        if (lc != null) {
            double lat = lc.getLatitude();
            double lon = lc.getLongitude();
            LatLng ll = new LatLng(lat, lon);
            latLngs.add(ll);
            marker = aMap.addMarker(new MarkerOptions().position(ll).title("").snippet("DefaultMarker"));        //在地图上标记点
            marker.setSnippet(marker.getId() + marker.getPosition());
            mMarkers.add(marker);
            Toast.makeText(getApplicationContext(), "GPS采点接口", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(getApplicationContext(), "无法获取当前位置信息", Toast.LENGTH_SHORT).show();
        }
    }

    public void gpsMaker() {

        //定位到当前位置并标记Maker
        //参考onMapClick(）
        LocationManager locationManager;
        String serviceName = Context.LOCATION_SERVICE;
        locationManager = (LocationManager) this.getSystemService(serviceName);
        // 查找到服务信息
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE); // 高精度
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW); // 低功耗
        String provider = locationManager.getBestProvider(criteria, true); // 获取GPS信息
        /**这段代码不需要深究，是locationManager.getLastKnownLocation(provider)自动生成的，不加会出错**/

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location lc = locationManager.getLastKnownLocation(provider); // 通过GPS获取位置
        updateLocation(lc);
        /*Location lc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        double lat=lc.getLatitude();
        double lon=lc.getLongitude();
        LatLng ll=new LatLng(lat,lon);
        latLngs.add(ll);
        marker = aMap.addMarker(new MarkerOptions().position(ll).title("").snippet("DefaultMarker"));        //在地图上标记点
        marker.setSnippet(marker.getId() + marker.getPosition());
        mMarkers.add(marker);*/


        //Toast.makeText(getApplicationContext(), "GPS采点接口" , Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            // requestCode即所声明的权限获取码，在checkSelfPermission时传入
            case GAODE_READ_PHONE_STATE:
                //如果用户取消，permissions可能为null.
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults.length > 0) { //有权限
                    // 获取到权限，作相应处理
                    gpsMaker();
                } else {
                    showGPSContacts();
                }
                break;
            default:
                break;
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PRIVATE_CODE:
                showGPSContacts();
                break;

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