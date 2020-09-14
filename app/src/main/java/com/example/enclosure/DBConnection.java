package com.example.enclosure;

import android.util.Log;
import android.widget.Toast;
import com.amap.api.maps2d.model.LatLng;
import  java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import android.util.Log;
import java.util.ArrayList;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.UiSettings;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.Polygon;
import com.amap.api.maps2d.model.PolygonOptions;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class DBConnection {
    private static final String TAG = "mysql11111";
    Connection conn = null;
    double area = 0;
    DBConnection(double area)
    {
        this.area = area;
        System.out.println("Area" +area);
    }
    public static void mymysql(final double area) {
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                    while (!Thread.interrupted()) {
                    try {
                        Thread.sleep(100);  // 每隔0.1秒尝试连接
                    } catch (InterruptedException e) {
                        Log.e(TAG, e.toString());
                    }
// 1.加载JDBC驱动
                    try {
                        Class.forName("com.mysql.jdbc.Driver");
                        Log.v(TAG, "加载JDBC驱动成功");
                    } catch (ClassNotFoundException e) {
                        Log.e(TAG, "加载JDBC驱动失败");
                        return;
                    }
                    // 2.设置好IP/端口/数据库名/用户名/密码等必要的连接信息
                    String id = "182.92.225.179";
                    int port = 3306;
                    String dbName = "Enclosure";
                    String url = "jdbc:mysql://" + id + ":" + port
                            + "/" + dbName;
                    // 构建连接mysql的字符串
                    String user = "root";
                    String password = "zxj.kxj.110";
                    // 3.连接JDBC
                        int ide=1;
                    try {
                        Connection conn = DriverManager.getConnection(url, user, password);
                        Log.d(TAG, "数据库连接成功");
                        //3.sql 语句
                        String sql = "INSERT INTO plus_block_info (plus_block_id,plus_block_area) VALUES ("+ide+", "+area+")";
                        //4.获取用于向数据库发送sql语句的ps
                        PreparedStatement ps = conn.prepareStatement(sql);
                        ps.execute(sql);
                        return;
                    } catch (SQLException e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            }
        });
        thread.start();
//        try {
//            Class.forName("com.mysql.jdbc.Driver");
//            Log.v(TAG, "加载JDBC驱动成功");
//        } catch (ClassNotFoundException e) {
//            Log.e(TAG, "加载JDBC驱动失败");
//            return;
//        }
//   // 2.设置好IP/端口/数据库名/用户名/密码等必要的连接信息
//    String id = "182.92.225.179";
//    int port = 3306;
//    String dbName = "Enclosure";
//    String url = "jdbc:mysql://" + id + ":" + port
//            + "/" + dbName;
//    // 构建连接mysql的字符串
//    String user = "root";
//    String password = "zxj.kxj.110";
//        try {
//
//            Connection conn = DriverManager.getConnection(url, user, password);
//            Log.d(TAG, "数据库连接成功");
//            //3.sql 语句
//            String sql = "INSERT INTO plus_block_info ( plus_block_area) VALUES (" + area + ")";
//            //4.获取用于向数据库发送sql语句的ps
//            PreparedStatement ps = conn.prepareStatement(sql);
//            ps.execute(sql);
//            return;
//        } catch (SQLException ex) {
//            ex.printStackTrace();
//            Log.d(TAG, "数据库连接fail");
//        }

    }

}





