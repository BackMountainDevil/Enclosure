package com.example.enclosure;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
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
import com.amap.api.maps2d.model.Polyline;
import com.amap.api.maps2d.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity  implements AMap.OnMapClickListener,
        AMap.OnMapLongClickListener, AMap.OnCameraChangeListener {
    private MapView mMapView = null;
    private AMap aMap = null;
    private UiSettings mUiSettings;                     //定位按钮
    private List<LatLng> latLngs = new ArrayList<>();     //坐标列表,可以用latLngs.size()获取点数
    //private Polygon polygon;
    //private PolygonOptions polygonOptions;
    private Polyline polyline;       //线段
    private Button btn_click;
    private Button btn_down;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("大师抄的地图");
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

//        polygon = new Polygon();
//        polygonOptions = new PolygonOptions();

        btn_click = findViewById(R.id.btn_gps);
        btn_down = findViewById(R.id.btn_down);

        btn_click.setOnClickListener(new MyOnClickListener());
        btn_down.setOnClickListener(new MyOnClickListener());
        aMap.setOnMapClickListener(this);// 对amap添加单击地图事件监听器
        aMap.setOnMapLongClickListener(this);// 对amap添加长按地图事件监听器
        aMap.setOnCameraChangeListener(this);// 对amap添加移动地图事件监听器

    }


    /**
     * 对单击地图事件回调
     */
    @Override
    public void onMapClick(LatLng point) {
        Toast.makeText(getApplicationContext(), "tapped, point=" + point, Toast.LENGTH_SHORT).show();
    }

    /**
     * 对长按地图事件回调
     */
    @Override
    public void onMapLongClick(LatLng point) {
        latLngs.add(point);

//        polygonOptions.add(point);

        final Marker marker = aMap.addMarker(new MarkerOptions().position(point).title("").snippet("DefaultMarker"));
        //Toast.makeText(getApplicationContext(),  "long pressed, point=" + point, Toast.LENGTH_SHORT).show();

        //手动绘制圈地部分，不包含首尾自动封闭，如果边框的起点与终点不一致，API会自动将它封闭。
//        if(latLngs.size() >= 3)
//        {
//            //polygonOptions.addAll(latLngs);
//            polygonOptions.strokeWidth(15) // 多边形的边框
//                    .strokeColor(Color.argb(50, 1, 1, 1)) // 边框颜色
//                    .fillColor(Color.argb(1, 1, 1, 1));   // 多边形的填充色
//
//            polygon.setPoints(latLngs);
//            aMap.invalidate();//刷新地图
//
//        }

        //手动绘制圈地部分，不包含首尾自动封闭
        if(latLngs.size() >= 2)
        {
            polyline = aMap.addPolyline((new PolylineOptions())
                    .addAll(latLngs)
                    .width(10).color(Color.argb(255, 1, 1, 1)));
        }
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


    class MyOnClickListener implements View.OnClickListener {
            @Override
            public void onClick(View v) { // 点击事件的处理方法
                if (v.getId() == R.id.btn_gps) { // 判断是否为btn_click被点击
                    if(aMap.isMyLocationEnabled())
                    {
                        double la = aMap.getMyLocation().getLatitude();
                        double lo = aMap.getMyLocation().getLongitude();
                        //Toast.makeText(getApplicationContext(), " la：" +la+" lo: "+lo, Toast.LENGTH_SHORT).show();
                        //Toast.makeText(getApplicationContext(), " point num：" +latLngs.size(), Toast.LENGTH_SHORT).show();

                        getArea(latLngs);
                    }
                }
                else if(v.getId() == R.id.btn_down)
                {
                    Toast.makeText(getApplicationContext(), " Btn Down click" , Toast.LENGTH_SHORT).show();
                }
            }
        }

    public void  getArea(List<LatLng> latLngs)  {
        double area = 0;
        //to do


        Toast.makeText(getApplicationContext(), " point num：" +latLngs.size() + "Area ： " +area + " 单位", Toast.LENGTH_SHORT).show();

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