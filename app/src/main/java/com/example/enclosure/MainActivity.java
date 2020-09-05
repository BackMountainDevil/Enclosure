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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MainActivity extends AppCompatActivity  implements AMap.OnMapClickListener,
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

            btn_click = findViewById(R.id.btn_gps);
            btn_down = findViewById(R.id.btn_reset);
            btn_maker = findViewById(R.id.btn_maker);

            btn_click.setOnClickListener(new MyOnClickListener());
            btn_down.setOnClickListener(new MyOnClickListener());
            btn_maker.setOnClickListener(new MyOnClickListener());
        }
    }

    /**
     * 设置一些aMap的属性,添加一些事件监听器
     */
    private void setUpMap() {
        mUiSettings = aMap.getUiSettings();//实例化UiSettings类对象
        aMap.setMapType(AMap.MAP_TYPE_SATELLITE);       //卫星地图模式，不显示文字？？
        //aMap.setMapType(AMap.MAP_TYPE_NORMAL);              //普通地图模式

        mUiSettings.setMyLocationButtonEnabled(true); //显示默认的定位按钮
        mUiSettings.setZoomControlsEnabled(false);  //取消显示默认的缩放按钮
        aMap.setMyLocationEnabled(true);// 可触发定位并显示当前位置

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
        marker.setSnippet(marker.getId()+marker.getPosition());
        mMarkers.add(marker);

        //手动绘制圈地部分，如果边框的起点与终点不一致，API会自动将它封闭。test best
        if(latLngs.size() >= 3)
        {
            if(polygon != null)         //清除上一次的图形，避免重叠变丑
            {polygon.remove();}
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
                    if(aMap.isMyLocationEnabled())
                    {
                        getArea(latLngs);
                    }
                }
                else if(v.getId() == R.id.btn_reset)        //撤销
                {
                    clearAll();
                }
                else if(v.getId() == R.id.btn_maker)        //gps采点
                {
                    gpsMaker();
                }
            }
        }

    /**
     * 计算圈地面积
     */
    public void  getArea(List<LatLng> latLngs)  {
        double area = 0;
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
            area = 0.5 * Math.abs(area);

        }

        Toast.makeText(getApplicationContext(),  "Area ： " +area + " 单位", Toast.LENGTH_SHORT).show();

    }

    public void gpsMaker()
    {
        //定位到当前位置并标记Maker
        //参考onMapClick(）
        Toast.makeText(getApplicationContext(), "GPS采点接口" , Toast.LENGTH_SHORT).show();
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
            aMap.clear();
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