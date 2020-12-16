package tk.anysoft.xposed.lark.ui.activity;

import android.Manifest;
import android.annotation.NonNull;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.sky.xposed.common.util.ToastUtil;
import tk.anysoft.xposed.lark.BuildConfig;
import tk.anysoft.xposed.lark.Constant;
import tk.anysoft.xposed.lark.R;
import tk.anysoft.xposed.lark.ui.util.DialogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManagerActivity extends AppCompatActivity {


    private SharedPreferences preferences;//共享引用数据
    private int flag;//标记模式 定位 WIFI 基站
    private int listFlag;//标记模式 定位 WIFI 基站

    String from[] = new String[0]; //自定义 listitem 中的元素名称
    int to[] = new int[0]; //自定义 listitem 中的元素id

    private ListView listView;
    private SimpleAdapter listViewAdapter;
    private ArrayList<HashMap<String, Object>> dataArrayList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager);
        FloatingActionButton fab = findViewById(R.id.fab);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });




        Intent intent = getIntent();
        flag = intent.getIntExtra("flag", Constant.XFlag.ENABLE_LOCATION);//取出模式flag
        listFlag = Constant.XFlag.LOCATIONLIST;//shareperference 中存储的对应list名称
        //SimpleAdapter 参数

        int rLayoutId = R.layout.list_position; //自定义item layout id
        switch (flag){
            case Constant.XFlag.ENABLE_LOCATION:
                listFlag = Constant.XFlag.LOCATIONLIST;
                toolbar.setTitle(R.string.location_manager_title);
                rLayoutId = R.layout.list_position;
                from = new String[]{"title", "address", "latitude", "longitude"};
                to = new int[]{R.id.tv_title, R.id.tv_address, R.id.tv_latitude, R.id.tv_longitude};
                //fab事件绑定
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.setClassName(BuildConfig.APPLICATION_ID, MapActivity.class.getName());
                        startActivityForResult(intent, 99);
                    }
                });
                break;
            case Constant.XFlag.ENABLE_WIFI:
                listFlag = Constant.XFlag.WIFILIST;
                toolbar.setTitle(R.string.wifi_manager_title);
                rLayoutId = R.layout.list_wifi;
                from = new String[]{"title", "wifiData", "connectionInfo", "scanResults"};
                to = new int[]{R.id.tv_title, R.id.tv_wifi_status, R.id.tv_wifi_current, R.id.tv_wifi_lists};
                //fab事件绑定
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        WifiManager wifiMgr = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                        if (!wifiMgr.isWifiEnabled()) {
                            wifiMgr.setWifiEnabled(true);
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(
                                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[] { Manifest.permission.ACCESS_COARSE_LOCATION },
                                    100);
                            return;
                        }
                        JSONObject wifiData = new JSONObject();
                        try {
                            wifiData.put("wifiEnabled",wifiMgr.isWifiEnabled());
                            wifiData.put("wifiState",wifiMgr.getWifiState());
                            JSONObject connectionInfo = new JSONObject();
                            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
                            String ssid = wifiInfo.getSSID();
                            if (ssid.startsWith("\"") && ssid.endsWith("\"")){
                                ssid = ssid.substring(1,ssid.length()-1);
                            }
                            connectionInfo.put("ssid",ssid);
                            connectionInfo.put("bssid",wifiInfo.getBSSID());
                            connectionInfo.put("mac",wifiInfo.getMacAddress());

                            JSONArray scanResults = new JSONArray();

                            List<ScanResult> lists = wifiMgr.getScanResults();
                            if (lists == null || lists.size() == 0) {
                                Toast.makeText(getApplicationContext(), "获取附近WIFI列表失败，无法采集数据！", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            for (int i = 0; i < lists.size(); i++) {
                                JSONObject scanResult = new JSONObject();
                                scanResult.put("ssid",lists.get(i).SSID);
                                scanResult.put("bssid",lists.get(i).BSSID);
                                scanResult.remove("mac");
                                scanResults.put(scanResult);
                            }
                            DialogUtil.showSaveDialog(ManagerActivity.this, ssid, (wifiName) -> {

                                if (TextUtils.isEmpty(wifiName)) {
                                    ToastUtil.show("方案名不能为空!");
                                    return;
                                }
                                // 保存最后记录
                                HashMap<String, Object> map = new HashMap<String, Object>();
                                map.put("title",wifiName);
                                map.put("wifiData",wifiData);
                                map.put("connectionInfo",connectionInfo);
                                map.put("scanResults",scanResults);
                                dataArrayList.add(map);
                                listViewAdapter.notifyDataSetChanged();
                                saveListDatas();
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
//                        Intent intent = new Intent(Intent.ACTION_MAIN);
//                        intent.setClassName(BuildConfig.APPLICATION_ID, WifiActivity.class.getName());
//                        startActivityForResult(intent, 99);
                    }
                });
                break;
            case Constant.XFlag.ENABLE_BASESTATION:
                listFlag = Constant.XFlag.BASESTATIONLIST;
                toolbar.setTitle(R.string.basestation_manager_title);
                rLayoutId = R.layout.list_base_station;
                from = new String[]{"title", "mcc", "mnc","lac","cellId"};
                to = new int[]{R.id.tv_title, R.id.tv_mcc, R.id.tv_mnc, R.id.tv_lac,R.id.tv_cellid};
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        TelephonyManager mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                        JSONObject baseStationInfo = new JSONObject();
                        // 返回值MCC + MNC
                        String operator = mTelephonyManager.getNetworkOperator();
                        final int mcc = Integer.parseInt(operator.substring(0, 3));
                        final int mnc = Integer.parseInt(operator.substring(3));
                        /*
                            * MNC
                            中国移动：00、02、04、07
                            中国联通：01、06、09
                            中国电信：03、05、11
                            中国铁通：20
                        */
                        @SuppressLint("MissingPermission") CellLocation locationBase = mTelephonyManager.getCellLocation();
                        int lac = -1;
                        int cellId = -1;
                        // 中国移动和中国联通获取LAC、CID的方式
                        if (mnc != 3 && mnc != 5 && mnc != 11) {
                            @SuppressLint("MissingPermission") GsmCellLocation gsmCellLocation = (GsmCellLocation) locationBase;
                            lac = gsmCellLocation.getLac();
                            cellId = gsmCellLocation.getCid();
                        } else {
                            @SuppressLint("MissingPermission") CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) locationBase;
                            if (cdmaCellLocation != null) {
                                lac = cdmaCellLocation.getNetworkId();
                                cellId = cdmaCellLocation.getBaseStationId();
                            }
                        }
                        if (lac == -1 || cellId == -1) {
                            Toast.makeText(getApplicationContext(), "获取基站信息失败，无法采集数据！", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        final int lacFinal = lac;
                        final int cellIdFinal = cellId;

                        DialogUtil.showSaveDialog(ManagerActivity.this, "", (name) -> {

                            if (TextUtils.isEmpty(name)) {
                                ToastUtil.show("方案名不能为空!");
                                return;
                            }
                            // 保存最后记录
                            HashMap<String, Object> map = new HashMap<String, Object>();
                            map.put("title",name);
                            map.put("mcc",mcc);
                            map.put("mnc",mnc);
                            map.put("lac",lacFinal);//网络id
                            map.put("cellId",cellIdFinal);//基站id
                            dataArrayList.add(map);
                            listViewAdapter.notifyDataSetChanged();
                            saveListDatas();
                        });
//                        Intent intent = new Intent(Intent.ACTION_MAIN);
//                        intent.setClassName(BuildConfig.APPLICATION_ID, BaseStationActivity.class.getName());
//                        startActivityForResult(intent, 99);
                    }
                });
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + flag);
        }
        //初始化 listView
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String listDatas = preferences.getString(Integer.toString(listFlag), "[]");//列表所有数据
        JSONArray listDataArrays = new JSONArray();//列表所有数据转JSONARRAY
        try {
            listView = findViewById(R.id.list_view_data);
            dataArrayList = new ArrayList<HashMap<String, Object>>();
            listDataArrays = new JSONArray(listDatas);
            if (listDataArrays.length() > 0) {
                for (int i = 0; i < listDataArrays.length(); i++) {
                    JSONObject data = listDataArrays.getJSONObject(i);
                    HashMap<String, Object> map = new HashMap<String, Object>();
                    for (int j=0;j<from.length;j++){
                        map.put(from[j], data.getString(from[j]));
                    }
                    dataArrayList.add(map);
                }
            }
            listViewAdapter = new SimpleAdapter(this, //没什么解释
                    dataArrayList,//数据来源
                    rLayoutId,//ListItem的XML实现
                    //动态数组与ListItem对应的名称
                    from,//名称映射
                    //ListItem的XML文件里面的元素id
                    to);//id映射
            //添加并且显示
            listView.setAdapter(listViewAdapter);
            listView.setItemsCanFocus(true);
            //长按弹出删除对话框
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                    final int index = i;
                    DialogUtil.showDialog(ManagerActivity.this, getString(R.string.del_item),
                            "确认要删除 " + dataArrayList.get(i).get(from[0]),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Toast.makeText(getApplicationContext(), dataArrayList.get(index).get(from[0]) + " 已删除",Toast.LENGTH_SHORT).show();
                                    dataArrayList.remove(index);
                                    listViewAdapter.notifyDataSetChanged();
                                    saveListDatas();
                                }
                            });
                    return true;
                }
            });
            //点击 自动选中
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Map<String, Object> map = dataArrayList.get(i);
                    Intent data = new Intent();
                    data.putExtra("flag", flag);//标签返回
                    JSONObject dataObeject = new JSONObject();
                    for (int j=0;j<from.length;j++){
                        try {
                            dataObeject.put(from[j],map.get(from[j]));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    data.putExtra("title",(String) map.get("title"));
                    data.putExtra("data",dataObeject.toString());
                    setResult(Activity.RESULT_OK, data);
                    listView.setSelection(i);
                    Toast.makeText(getApplicationContext(), "已选择 " + map.get(from[0]), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            data.putExtra("flag",flag);
            if (requestCode == 99 && resultCode == Activity.RESULT_OK) {
                if (Constant.XFlag.ENABLE_LOCATION == flag){
                    // 保存位置信息
                    String title = data.getStringExtra("title");
                    String datas = data.getStringExtra("data");
                    setResult(Activity.RESULT_OK, data);
                    if (null != listViewAdapter && !TextUtils.isEmpty(title) && !TextUtils.isEmpty(datas)) {
                        HashMap<String, Object> map = new HashMap<String, Object>();
                        JSONObject dataObject = new JSONObject(datas);
                        for (int i = 0; i < from.length; i++) {
                            map.put(from[i], dataObject.get(from[i]));
                        }
                        map.put("title", title);
                        dataArrayList.add(map);
                        listViewAdapter.notifyDataSetChanged();
                        saveListDatas();
                    }else if (Constant.XFlag.ENABLE_WIFI == flag){

                    }else if (Constant.XFlag.ENABLE_BASESTATION == flag){

                    }
                }
            }
        }catch (Throwable e){
            e.printStackTrace();
        }

    }

    private void saveListDatas() {
        try {
            if (null != dataArrayList && dataArrayList.size() >= 0) {
                JSONArray datas = new JSONArray();
                for (int i = 0; i < dataArrayList.size(); i++) {
                    JSONObject data = new JSONObject();
                    for (int j = 0; j < from.length; j++) {
                        data.put(from[j], dataArrayList.get(i).get(from[j]));
                    }
                    datas.put(data);
                }
                preferences
                        .edit()
                        .putString(Integer.toString(listFlag), datas.toString())
                        .apply();
            }/*else if (null != dataArrayList && dataArrayList.size() > 0){//列表为空清空存储的方案名称
                preferences
                        .edit()
                        .remove(Integer.toString())
                        .apply();
            }*/
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_manager_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();

        if (android.R.id.home == itemId) {
            // 退出
            onBackPressed();
            return true;
        } else if (R.id.menu_import == itemId) {
            // 确定
            importConfigs();
            return true;
        } else if (R.id.menu_export == itemId) {
            // 搜索
            exportConfigs();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void exportConfigs() {
        if (dataArrayList.size()>0){
            JSONArray datas = new JSONArray();
            for (int i = 0; i < dataArrayList.size(); i++) {
                JSONObject data = new JSONObject();
                for (int j = 0; j < from.length; j++) {
                    try {
                        data.put(from[j], dataArrayList.get(i).get(from[j]));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                datas.put(data);
            }
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setText(datas.toString());
            Toast.makeText(getApplicationContext(),"导出设置成功",Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(getApplicationContext(),"当前无设置",Toast.LENGTH_SHORT).show();
        }
    }

    private void importConfigs() {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        String settings = String.valueOf(cm.getText());
        if (!TextUtils.isEmpty(settings) && settings.startsWith("[") && settings.endsWith("]")){
            try {
                JSONArray datas = new JSONArray(settings);
                for (int i = 0; i < datas.length(); i++) {
                    JSONObject data = datas.getJSONObject(i);
                    HashMap<String, Object> map = new HashMap<>();
                    for (int j = 0; j < from.length; j++) {
                        map.put(from[j],data.get(from[j]));
                    }
                    dataArrayList.add(map);
                }
                listViewAdapter.notifyDataSetChanged();
                saveListDatas();
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }else {
            Toast.makeText(getApplicationContext(), R.string.clip_no_settings,Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                FloatingActionButton fab = findViewById(R.id.fab);
                fab.callOnClick();
            }
        }
    }

}