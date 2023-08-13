/*
 * Copyright (c) 2019 The sky Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tk.anysoft.xposed.lark.plugin.feishu;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import tk.anysoft.xposed.lark.Constant;
import tk.anysoft.xposed.lark.data.model.PluginInfo;
import tk.anysoft.xposed.lark.plugin.base.BasePlugin;
import tk.anysoft.xposed.lark.plugin.interfaces.XPlugin;
import tk.anysoft.xposed.lark.plugin.interfaces.XPluginManager;
import tk.anysoft.xposed.lark.ui.dialog.SettingsDialog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/**
 * Created by sky on 2019/3/14.
 */
public class FeishuPlugin extends BasePlugin {

    private SharedPreferences preferences;//共享引用数据
    private Context myContext;


    private Handler mHandler;


    public FeishuPlugin(Build build) {
        super(build.mPluginManager);
        mHandler = build.mHandler;
        myContext = build.mPluginManager.getContext();
    }

    @Override
    public boolean isHandler() {
        return getVersionManager().getSupportConfig() != null;
    }

    @Override
    public void setEnable(int flag, boolean enable) {
//        super.setEnable(flag, enable);    // 不需要处理
        mHandler.setEnable(flag, enable);
    }

    @Override
    public Info getInfo() {
        return new PluginInfo(Constant.Plugin.DING_DING, Constant.Name.TITLE);
    }

    @Override
    public void onHandleLoadPackage() {
        Log.d("LarkHelper","start to load FeishuPlugin");
//        findMethod(
//                M.classz.class_lightapp_runtime_LightAppRuntimeReverseInterfaceImpl,
//                M.method.method_lightapp_runtime_LightAppRuntimeReverseInterfaceImpl_initSecurityGuard,
//                Context.class)
//                .before(param -> param.setResult(null));
//
//        Method methodMessage = findMatcherMethod(
//                M.classz.class_defpackage_MessageDs,
//                M.method.method_defpackage_MessageDs_handler,
//                String.class, Collection.class, boolean.class);
//
//        XposedBridge.hookMethod(methodMessage, new XC_MethodHook() {
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                super.afterHookedMethod(param);
//                // 处理消息
//                mHandler.onHandlerMessage((String) param.args[0], (Collection) param.args[1]);
//            }
//        });
//
//        findMethod(
//                M.classz.class_android_dingtalk_redpackets_activities_FestivalRedPacketsPickActivity,
//                M.method.method_android_dingtalk_redpackets_activities_FestivalRedPacketsPickActivity_initView)
//                .after(param -> {
//                    // 处理快速打开红包
//                    mHandler.onHandlerFestivalRedPacketsPick((Activity) param.thisObject);
//                });
//
//        findMethod(
//                M.classz.class_android_dingtalk_redpackets_activities_PickRedPacketsActivity,
//                M.method.method_android_dingtalk_redpackets_activities_PickRedPacketsActivity_initView)
//                .after(param -> {
//                    // 处理快速打开红包
//                    mHandler.onHandlerPickRedPackets((Activity) param.thisObject);
//                });
//
//        Method methodRecall = findMatcherMethod(
//                M.classz.class_defpackage_MessageDs,
//                M.method.method_defpackage_MessageDs_recall,
//                String.class, List.class, ContentValues.class);
//
//        XposedBridge.hookMethod(methodRecall, new XC_MethodHook() {
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                super.beforeHookedMethod(param);
//
//                // 处理撤回消息
//                if (mHandler.onRecallMessage((ContentValues) param.args[2])) {
//                    // 直接返回0
//                    param.setResult(0);
//                }
//            }
//        });


        /****************  位置信息处理 ******************/
        Log.d("LarkHelper","hook com.amap.api.location.AMapLocationClient  getLastKnownLocation");
        findMethod(
                "com.amap.api.location.AMapLocationClient",
                "getLastKnownLocation")
                .after(param -> param.setResult(mHandler.getLastKnownLocation(param.getResult())));
        Log.d("LarkHelper","hook com.amap.api.location.AMapLocationClient  setLocationListener");
        findMethod(
                "com.amap.api.location.AMapLocationClient",
                "setLocationListener",
                "com.amap.api.location.AMapLocationListener")
                .before(param -> param.args[0] = mHandler.onHandlerLocationListener(param.args[0]));

        /****************  基站和wifi ******************/

//        preferences = preferences = PreferenceManager.getDefaultSharedPreferences(this.getPluginManager().getContext());


        XposedHelpers.findAndHookMethod(TelephonyManager.class, "getCellLocation", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                try {
//                    Context context = myContext;
                    Context context = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");

                    final SharedPreferences sharedPreferences = context.getSharedPreferences(Constant.Name.LARK, Context.MODE_PRIVATE);
                    boolean enbaleBaseStation = sharedPreferences.getBoolean(Integer.toString(Constant.XFlag.ENABLE_BASESTATION), false);
                    String baseStationData = sharedPreferences.getString(Integer.toString(Constant.XFlag.BASESTATIONDATA), "{}");

                    if (enbaleBaseStation && null != baseStationData) {
                        JSONObject object = new JSONObject(baseStationData);
                        if (object.length() > 0) {
                            Bundle bundle = new Bundle();
                            Integer mcc = object.getInt("mcc");
                            Integer mnc = object.getInt("mnc");
                            Integer lac = object.getInt("lac");
                            Integer cellId = object.getInt("cellId");

                            if (mnc != 3 && mnc != 5 && mnc != 11) {
                                if (null != lac && null != cellId) {
                                    bundle.putInt("lac", lac);
                                    bundle.putInt("cid", cellId);
                                    param.setResult(new GsmCellLocation(bundle));
                                    XposedBridge.log("LarkHelper：模拟为GSM卡，lac:" + lac + "，cid" + cellId);
                                }
                            } else {
                                if (null != lac && null != cellId) {
                                    bundle.putInt("networkId", lac);
                                    bundle.putInt("baseStationId", cellId);
                                    param.setResult(new CdmaCellLocation(bundle));
                                    XposedBridge.log("LarkHelper：模拟为CDMA卡，lac:" + lac + "，cid:" + cellId);
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    XposedBridge.log("LarkHelper：" + "hook BaseStation fail！" + ex.getMessage());
                }
            }
        });

        //wifi
        XposedHelpers.findAndHookMethod(android.net.wifi.WifiManager.class, "isWifiEnabled", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult(true);
                        XposedBridge.log("LarkHelper：模拟 wifi开启");
                    }
                }
        );

        XposedHelpers.findAndHookMethod(android.net.wifi.WifiManager.class, "getScanResults", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                try {
                    Context context = myContext;
                    final SharedPreferences sharedPreferences = context.getSharedPreferences(Constant.Name.LARK, Context.MODE_PRIVATE);
                    boolean enbaleWifi = sharedPreferences.getBoolean(Integer.toString(Constant.XFlag.ENABLE_WIFI), false);
                    String wifiData = sharedPreferences.getString(Integer.toString(Constant.XFlag.WIFIDATA), "{}");
                    if (enbaleWifi && null != wifiData) {
                        JSONObject object = new JSONObject(wifiData);
                        if (object.length() > 0 && object.has("scanResults")) {
                            JSONArray scanResultsArray = object.getJSONArray("scanResults");
                            List<ScanResult> wifiList = new ArrayList<>();
                            for (int i = 0; i < scanResultsArray.length(); i++) {
                                try {
                                    ScanResult scanResult = ScanResult.class.newInstance();
                                    scanResult.SSID = scanResultsArray.getJSONObject(i).getString("ssid");
                                    scanResult.BSSID = scanResultsArray.getJSONObject(i).getString("bssid");
                                    ;
                                    wifiList.add(scanResult);
                                } catch (Exception ex) {
                                    XposedBridge.log(ex.getCause());
                                }

                            }
                            XposedBridge.log("LarkHelper：模拟了" + wifiList.size() + "个WIFI信息");
                            param.setResult(wifiList);
                        }
                    }
                } catch (Exception ex) {
                    XposedBridge.log("LarkHelper：" + "hook wifi fail！" + ex.getMessage());
                }
            }
        });

        XposedHelpers.findAndHookMethod(android.net.wifi.WifiManager.class, "getConnectionInfo", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                try {
                    XposedBridge.log("LarkHelper 1");
                    Context context = myContext;
                    final SharedPreferences sharedPreferences = context.getSharedPreferences(Constant.Name.LARK, Context.MODE_PRIVATE);
                    boolean enbaleWifi = sharedPreferences.getBoolean(Integer.toString(Constant.XFlag.ENABLE_WIFI), false);
                    boolean enbaleWifiCurrent = sharedPreferences.getBoolean(Integer.toString(Constant.XFlag.ENABLE_WIFI_CURRENT), false);
                    String wifiData = sharedPreferences.getString(Integer.toString(Constant.XFlag.WIFIDATA), "{}");
                    XposedBridge.log("LarkHelper：读取wifi配置信息:" + wifiData);
                    if (enbaleWifi && enbaleWifiCurrent && null != wifiData) {
                        JSONObject object = new JSONObject(wifiData);
                        if (object.length() > 0 && object.has("connectionInfo")) {
                            JSONObject connectionInfo = object.getJSONObject("connectionInfo");
                            WifiInfo wifiInfo = (WifiInfo) XposedHelpers.newInstance(WifiInfo.class);
                            XposedHelpers.setIntField((Object) wifiInfo, "mNetworkId", 68); // MAX_RSSI
                            XposedHelpers.setObjectField((Object) wifiInfo, "mSupplicantState", SupplicantState.COMPLETED);
                            XposedHelpers.setObjectField((Object) wifiInfo, "mBSSID", connectionInfo.getString("bssid"));
                            XposedHelpers.setObjectField((Object) wifiInfo, "mMacAddress", connectionInfo.getString("mac"));
//                            InetAddress ipAddress = (InetAddress) XposedHelpers.newInstance(InetAddress.class);
//                            XposedHelpers.setObjectField((Object) wifiInfo, "mIpAddress", "192.168.3.102");
                            XposedHelpers.setIntField((Object) wifiInfo, "mLinkSpeed", 433);  // Mbps
                            XposedHelpers.setIntField((Object) wifiInfo, "mFrequency", 5785); // MHz
                            XposedHelpers.setIntField((Object) wifiInfo, "mRssi", -49); // MAX_RSSI
                            try {
                                Class cls = XposedHelpers.findClass("android.net.wifi.WifiSsid", myContext.getClassLoader());
                                Object wifissid = XposedHelpers.callStaticMethod(cls, "createFromAsciiEncoded", connectionInfo.getString("ssid"));
                                XposedHelpers.setObjectField((Object) wifiInfo, "mWifiSsid", wifissid);
                            } // Kitkat
                            catch (Error e) {
                                XposedHelpers.setObjectField((Object) wifiInfo, "mSSID", connectionInfo.getString("ssid"));
                            }
                            XposedBridge.log("LarkHelper：模拟了已连接wifi:" + connectionInfo.getString("ssid"));
                            param.setResult(wifiInfo);
                        } else {
                            XposedBridge.log("LarkHelper：未采集 已连接wifi信息");

                        }
                    }
                } catch (Exception ex) {
                    XposedBridge.log("LarkHelper：" + "hook wifi connect fail！" + ex.getMessage());
                }
            }
        });


    }

    @Override
    public void openSettings(Activity activity) {

        SettingsDialog dialog = new SettingsDialog();
        dialog.show(activity.getFragmentManager(), "dingDing");
    }

    public interface Handler {

        void setEnable(int flag, boolean enable);

        void onHandlerMessage(String cid, Collection messages);

        void onHandlerFestivalRedPacketsPick(Activity activity);

        void onHandlerPickRedPackets(Activity activity);

        boolean onRecallMessage(ContentValues contentValues);

        Object getLastKnownLocation(Object location);

        Object onHandlerLocationListener(Object listener);
    }

    public static class Build {

        private XPluginManager mPluginManager;
        private Handler mHandler;

        public Build(XPluginManager pluginManager) {
            mPluginManager = pluginManager;
        }

        public Build setHandler(Handler handler) {
            mHandler = handler;
            return this;
        }

        public XPlugin build() {
            return new FeishuPlugin(this);
        }
    }
}
