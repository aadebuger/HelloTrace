package com.baidu.trackshow;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Looper;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.baidu.trace.OnGeoFenceListener;

/**
 * 地理围栏
 */
@SuppressLint("NewApi")
public class Geofence implements OnClickListener {

    protected PopupWindow popupwindow = null;

    private Button btnSetfence = null;
    private Button btnMonitoredstatus = null;
    private Button btnHistoryalarm = null;
    private Button btnDelayalarm = null;

    private LayoutInflater mInflater = null;

    // 围栏圆心纬度
    private double latitude = 0;

    // 围栏圆心经度
    private double longitude = 0;

    // 围栏半径
    protected static int radius = 100;

    protected static int radiusTemp = radius;

    // 围栏编号
    protected static int fenceId = 0;

    // 延迟时间（单位: 分）
    private int delayTime = 5;

    // 地理围栏监听器
    protected static OnGeoFenceListener geoFenceListener = null;

    // 围栏覆盖物
    protected static OverlayOptions fenceOverlay = null;

    protected static OverlayOptions fenceOverlayTemp = null;

    protected static boolean isShow = false;

    private Context mContext = null;

    protected OnMapClickListener mapClickListener = new OnMapClickListener() {

        public void onMapClick(LatLng arg0) {
            // TODO Auto-generated method stub
            MainActivity.mBaiduMap.clear();
            latitude = arg0.latitude;
            longitude = arg0.longitude;

            MapStatus mMapStatus = new MapStatus.Builder().target(arg0).zoom(18).build();
            TrackUploadFragment.msUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);

            fenceOverlayTemp = fenceOverlay;
            fenceOverlay = new CircleOptions().fillColor(0x000000FF).center(arg0)
                    .stroke(new Stroke(5, Color.rgb(0xff, 0x00, 0x33)))
                    .radius(radius);

            TrackUploadFragment.addMarker();
            createOrUpdateDialog();
        }

        public boolean onMapPoiClick(MapPoi arg0) {
            // TODO Auto-generated method stub
            return false;
        }
    };

    public Geofence(Context context, LayoutInflater inflater) {
        initOnGeoFenceListener();
        mContext = context;
        mInflater = inflater;
        if (null == fenceOverlay) {
            queryFenceList();
        }
    }

    /**
     * 添加entity
     * 
     */
    protected static void addEntity() {
        // entity标识
        String entityName = MainActivity.entityName;
        // 属性名称（格式 : "key1=value1,columnKey2=columnValue2......."）
        String columnKey = "";
        MainActivity.client.addEntity(MainActivity.serviceId, entityName, columnKey, MainActivity.entityListener);
    }

    /**
     * 创建围栏（若创建围栏时，还未创建entity标识，请先使用addEntity(...)添加entity）
     * 
     */
    private void createFence() {

        // 创建者（entity标识）
        String creator = MainActivity.entityName;
        // 围栏名称
        String fenceName = MainActivity.entityName + "_fence";
        // 围栏描述
        String fenceDesc = "test";
        // 监控对象列表（多个entityName，以英文逗号"," 分割）
        String monitoredPersons = MainActivity.entityName;
        // 观察者列表（多个entityName，以英文逗号"," 分割）
        String observers = MainActivity.entityName;
        // 生效时间列表
        String validTimes = "0800,2300";
        // 生效周期
        int validCycle = 4;
        // 围栏生效日期
        String validDate = "";
        // 生效日期列表
        String validDays = "";
        // 坐标类型 （1：GPS经纬度，2：国测局经纬度，3：百度经纬度）
        int coordType = 3;
        // 围栏圆心（圆心位置, 格式 : "经度,纬度"）
        String center = longitude + "," + latitude;
        // 围栏半径（单位 : 米）
        double radius = Geofence.radius;
        // 报警条件（1：进入时触发提醒，2：离开时触发提醒，3：进入离开均触发提醒）
        int alarmCondition = 3;

        MainActivity.client.createCircularFence(MainActivity.serviceId, creator, fenceName, fenceDesc,
                monitoredPersons, observers,
                validTimes, validCycle, validDate, validDays, coordType, center, radius, alarmCondition,
                geoFenceListener);

    }

    /**
     * 删除围栏
     * 
     */
    @SuppressWarnings("unused")
    private static void deleteFence(int fenceId) {
        MainActivity.client.deleteFence(MainActivity.serviceId, fenceId, geoFenceListener);
    }

    /**
     * 更新围栏
     * 
     */
    private void updateFence() {
        // 围栏名称
        String fenceName = MainActivity.entityName + "_fence";
        // 围栏ID
        int fenceId = Geofence.fenceId;
        // 围栏描述
        String fenceDesc = "test fence";
        // 监控对象列表（多个entityName，以英文逗号"," 分割）
        String monitoredPersons = MainActivity.entityName;
        // 观察者列表（多个entityName，以英文逗号"," 分割）
        String observers = MainActivity.entityName;
        // 生效时间列表
        String validTimes = "0800,2300";
        // 生效周期
        int validCycle = 4;
        // 围栏生效日期
        String validDate = "";
        // 生效日期列表
        String validDays = "";
        // 坐标类型 （1：GPS经纬度，2：国测局经纬度，3：百度经纬度）
        int coordType = 3;
        // 围栏圆心（圆心位置, 格式 : "经度,纬度"）
        String center = longitude + "," + latitude;
        // 围栏半径（单位 : 米）
        double radius = Geofence.radius;
        // 报警条件（1：进入时触发提醒，2：离开时触发提醒，3：进入离开均触发提醒）
        int alarmCondition = 3;

        MainActivity.client.updateCircularFence(MainActivity.serviceId, fenceName, fenceId, fenceDesc,
                monitoredPersons,
                observers, validTimes, validCycle, validDate, validDays, coordType, center, radius, alarmCondition,
                geoFenceListener);
    }

    /**
     * 围栏列表
     * 
     */
    private void queryFenceList() {
        // 创建者（entity标识）
        String creator = MainActivity.entityName;
        // 围栏ID列表
        String fenceIds = "";
        MainActivity.client.queryFenceList(MainActivity.serviceId, creator, fenceIds, geoFenceListener);
    }

    /**
     * 监控状态
     * 
     */
    private void monitoredStatus() {
        // 围栏ID
        int fenceId = Geofence.fenceId;
        // 监控对象列表（多个entityName，以英文逗号"," 分割）
        String monitoredPersons = MainActivity.entityName;
        MainActivity.client.queryMonitoredStatus(MainActivity.serviceId, fenceId, monitoredPersons,
                geoFenceListener);
    }

    /**
     * 报警信息
     * 
     */
    private void historyAlarm() {
        // 围栏ID
        int fenceId = Geofence.fenceId;
        // 监控对象列表（多个entityName，以英文逗号"," 分割）
        String monitoredPersons = MainActivity.entityName;
        // 开始时间（unix时间戳）
        int beginTime = (int) (System.currentTimeMillis() / 1000 - 12 * 60 * 60);
        // 结束时间（unix时间戳）
        int endTime = (int) (System.currentTimeMillis() / 1000);

        MainActivity.client.queryFenceHistoryAlarmInfo(MainActivity.serviceId, fenceId, monitoredPersons, beginTime,
                endTime,
                geoFenceListener);
    }

    /**
     * 延迟报警
     * 
     */
    private void delayAlarm() {
        // 围栏ID
        int fenceId = Geofence.fenceId;
        // 观察者列表（多个entityName，以英文逗号"," 分割）
        String observer = MainActivity.entityName;
        // 延迟时间（unix时间戳）
        int delayTime = (int) (System.currentTimeMillis() / 1000 + this.delayTime * 60);
        MainActivity.client.delayFenceAlarm(MainActivity.serviceId, fenceId, observer, delayTime,
                geoFenceListener);
    }

    /**
     * 点击事件
     */
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {

        // 设置围栏
            case R.id.btn_setfence:
                inputDialog();
                MainActivity.mBaiduMap.setOnMapClickListener(mapClickListener);
                popupwindow.dismiss();
                break;

            // 历史报警
            case R.id.btn_historyalarm:
                historyAlarm();
                popupwindow.dismiss();
                break;

            // 监控对象状态
            case R.id.btn_monitoredstatus:
                monitoredStatus();
                popupwindow.dismiss();
                break;

            // 延迟报警
            case R.id.btn_delayalarm:
                delayAlarm();
                popupwindow.dismiss();
                break;

            default:
                break;
        }

    }

    @SuppressLint({ "InflateParams", "ClickableViewAccessibility" })
    protected void initPopupWindowView() {

        // 获取自定义布局文件menu_geofence.xml的视图
        View customView = mInflater.inflate(R.layout.menu_geofence, null);
        popupwindow = new PopupWindow(customView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
        // 自定义view添加触摸事件
        customView.setOnTouchListener(new OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                if (popupwindow != null && popupwindow.isShowing()) {
                    popupwindow.dismiss();
                    popupwindow = null;
                }

                return false;
            }

        });

        btnSetfence = (Button) customView.findViewById(R.id.btn_setfence);
        btnMonitoredstatus = (Button) customView.findViewById(R.id.btn_monitoredstatus);
        btnHistoryalarm = (Button) customView.findViewById(R.id.btn_historyalarm);
        btnDelayalarm = (Button) customView.findViewById(R.id.btn_delayalarm);

        btnSetfence.setOnClickListener(this);
        btnMonitoredstatus.setOnClickListener(this);
        btnHistoryalarm.setOnClickListener(this);
        btnDelayalarm.setOnClickListener(this);

    }

    /**
     * 初始化OnGeoFenceListener
     */
    private void initOnGeoFenceListener() {
        // 初始化geoFenceListener
        geoFenceListener = new OnGeoFenceListener() {

            // 请求失败回调接口
            @Override
            public void onRequestFailedCallback(String arg0) {
                // TODO Auto-generated method stub
                MainActivity.mBaiduMap.clear();
                if (null != fenceOverlayTemp) {
                    fenceOverlay = fenceOverlayTemp;
                    fenceOverlayTemp = null;
                }
                radius = radiusTemp;
                TrackUploadFragment.addMarker();
                showMessage("geoFence请求失败回调接口消息 : " + arg0);
            }

            // 创建圆形围栏回调接口
            @Override
            public void onCreateCircularFenceCallback(String arg0) {
                // TODO Auto-generated method stub

                JSONObject dataJson = null;
                try {
                    dataJson = new JSONObject(arg0);
                    int status = dataJson.getInt("status");
                    if (0 == status) {
                        fenceId = dataJson.getInt("fence_id");
                        fenceOverlayTemp = null;
                        showMessage("围栏创建成功");
                    } else {
                        MainActivity.mBaiduMap.clear();
                        fenceOverlay = fenceOverlayTemp;
                        fenceOverlayTemp = null;
                        radius = radiusTemp;
                        TrackUploadFragment.addMarker();
                        showMessage("创建圆形围栏回调接口消息 : " + arg0);
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    showMessage("解析创建围栏回调消息失败");
                }

            }

            // 更新圆形围栏回调接口
            @Override
            public void onUpdateCircularFenceCallback(String arg0) {
                // TODO Auto-generated method stub
                showMessage("更新圆形围栏回调接口消息 : " + arg0);
            }

            // 延迟报警回调接口
            @Override
            public void onDelayAlarmCallback(String arg0) {
                // TODO Auto-generated method stub
                JSONObject dataJson = null;
                try {
                    dataJson = new JSONObject(arg0);
                    int status = dataJson.getInt("status");
                    if (0 == status) {
                        showMessage(delayTime + "分钟内不再报警");
                    } else {
                        showMessage("延迟报警回调接口消息 : " + arg0);
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    showMessage("解析延迟报警回调消息失败");
                }
            }

            // 删除围栏回调接口
            @Override
            public void onDeleteFenceCallback(String arg0) {
                // TODO Auto-generated method stub
                showMessage(" 删除围栏回调接口消息 : " + arg0);
            }

            // 查询围栏列表回调接口
            @Override
            public void onQueryFenceListCallback(String arg0) {
                // TODO Auto-generated method stub

                JSONObject dataJson = null;
                try {
                    dataJson = new JSONObject(arg0);
                    int status = dataJson.getInt("status");
                    if (0 == status) {
                        if (dataJson.has("size")) {
                            JSONArray jsonArray = dataJson.getJSONArray("fences");
                            JSONObject jsonObj = jsonArray.getJSONObject(0);
                            fenceId = jsonObj.getInt("fence_id");
                            JSONObject center = jsonObj.getJSONObject("center");

                            latitude = center.getDouble("latitude");
                            longitude = center.getDouble("longitude");
                            radius = (int) (jsonObj.getDouble("radius"));

                            LatLng latLng = new LatLng(latitude, longitude);

                            MapStatus mMapStatus = new MapStatus.Builder().target(latLng).zoom(18).build();
                            TrackUploadFragment.msUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);

                            fenceOverlay = new CircleOptions().fillColor(0x000000FF).center(latLng)
                                    .stroke(new Stroke(5, Color.rgb(0xff, 0x00, 0x33)))
                                    .radius(radius);

                            TrackUploadFragment.addMarker();
                        }
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    System.out.println("解析围栏列表回调消息失败");
                }

            }

            // 查询历史报警回调接口
            @Override
            public void onQueryHistoryAlarmCallback(String arg0) {
                // TODO Auto-generated method stub
                showMessage(" 查询历史报警回调接口消息 : " + arg0);
            }

            // 查询监控对象状态回调接口
            @Override
            public void onQueryMonitoredStatusCallback(String arg0) {
                // TODO Auto-generated method stub
                JSONObject dataJson = null;
                try {
                    dataJson = new JSONObject(arg0);
                    int status = dataJson.getInt("status");
                    if (0 == status) {
                        int size = dataJson.getInt("size");
                        if (size >= 1) {
                            JSONArray jsonArray = dataJson.getJSONArray("monitored_person_statuses");
                            JSONObject jsonObj = jsonArray.getJSONObject(0);
                            String mPerson = jsonObj.getString("monitored_person");
                            int mStatus = jsonObj.getInt("monitored_status");
                            if (1 == mStatus) {
                                showMessage("监控对象[ " + mPerson + " ]在围栏内");
                            } else {
                                showMessage("监控对象[ " + mPerson + " ]在围栏外");
                            }
                        }
                    } else {
                        showMessage("查询监控对象状态回调消息 : " + arg0);
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    showMessage("解析查询监控对象状态回调消息失败");
                }
            }
        };
    }

    // 输入围栏信息对话框
    private void inputDialog() {

        final EditText circleRadius = new EditText(mContext);
        circleRadius.setFocusable(true);
        circleRadius.setText(radius + "");
        circleRadius.setInputType(InputType.TYPE_CLASS_NUMBER);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        builder.setTitle("围栏半径(单位:米)").setView(circleRadius)
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        MainActivity.mBaiduMap.setOnMapClickListener(null);
                    }

                });

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                String radiusStr = circleRadius.getText().toString();
                if (!TextUtils.isEmpty(radiusStr)) {
                    radiusTemp = radius;
                    radius = Integer.parseInt(radiusStr) > 0 ? Integer.parseInt(radiusStr) : radius;
                }
                Toast.makeText(mContext, "请点击地图标记围栏圆心", Toast.LENGTH_LONG).show();
            }
        });
        builder.show();
    }

    /**
     * 设置围栏对话框
     */
    private void createOrUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        builder.setTitle("确定设置围栏?");

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                MainActivity.mBaiduMap.clear();
                // 添加覆盖物
                if (null != fenceOverlayTemp) {
                    fenceOverlay = fenceOverlayTemp;
                }
                radius = radiusTemp;
                TrackUploadFragment.addMarker();
                MainActivity.mBaiduMap.setOnMapClickListener(null);
            }
        });

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                if (0 == fenceId) {
                    // 创建围栏
                    createFence();
                } else {
                    // 更新围栏
                    updateFence();
                }
                MainActivity.mBaiduMap.setOnMapClickListener(null);
            }
        });
        builder.show();
    }

    private void showMessage(String message) {
        Looper.prepare();
        Toast.makeText(MainActivity.mContext, message, Toast.LENGTH_LONG).show();
        Looper.loop();
    }

}
