/*
 * Copyright (c) 2020 The sky Authors.
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

package com.sky.xposed.rimet.plugin;

import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;

import com.sky.xposed.annotations.APlugin;
import com.sky.xposed.common.util.Alog;
import com.sky.xposed.core.interfaces.XCoreManager;
import com.sky.xposed.rimet.XConstant;
import com.sky.xposed.rimet.plugin.base.BaseDingPlugin;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Random;

/**
 * Created by sky on 2020-03-01.
 */
@APlugin()
public class LocationPlugin extends BaseDingPlugin {

    public LocationPlugin(XCoreManager coreManager) {
        super(coreManager);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void hook() {
        Alog.d(this.getClass().getName(), " Loading and init pugin....");
        /****************  位置信息处理 ******************/
        //hook provider all gps location will call this function
        hookGPSProviderStatus();

        //hook basicGPS
//        hookBasicGPS();

        /****************  位置信息处理 ******************/
        String packageName = getCoreManager().getLoadPackage().getPackageName();
        if (XConstant.Rimet.PACKAGE_NAME.get(1).equals(packageName)) {
            hookGoogleMap();
        } else {
            hookAMap();
        }
    }

    /**
     * hook 标准 gps isProviderEnabled 所有定位都会判断 GPS 开关状态
     */
    private void hookGPSProviderStatus() {
        //通用 hook GPS为打开状态
        findMethod(
                LocationManager.class,
                "isProviderEnabled", String.class)
                .before(param -> {
                    Alog.d(this.getClass().getName(), String.format("invoke isProviderEnabled arg0=%s", param.args[0]));
                    if (isEnable(XConstant.Key.ENABLE_VIRTUAL_LOCATION)) {
                        if ("gps".equals(param.args[0])) {
                            param.setResult(true);
                        }
                    }
                });
    }

    private void hookBasicGPS() {
        findMethod(
                LocationManager.class,
                "getBestProvider", Criteria.class, boolean.class)
                .before(param -> {
                    Log.d(">>>>>>>>>>>", "getBestProvider:" + param.args[0]);
                    if (isEnable(XConstant.Key.ENABLE_VIRTUAL_LOCATION)) {
                        param.setResult("gps");
                    }
                });

        findMethod(
                LocationManager.class,
                "getProviders", Criteria.class, boolean.class)
                .before(param -> {
                    Log.d(">>>>>>>>>>>", "getProviders:" + param.args[0]);
                    if (isEnable(XConstant.Key.ENABLE_VIRTUAL_LOCATION)) {
                        Log.e("", "getProviders");
                    }
                });

        findMethod(
                LocationManager.class,
                "getGpsStatus", GpsStatus.class)
                .after(param -> {
                    Log.d(">>>>>>>>>>>", "getGpsStatus");
                    if (isEnable(XConstant.Key.ENABLE_VIRTUAL_LOCATION)) {
                        GpsStatus gss = (GpsStatus) param.getResult();
                        setGpsStatus(gss);
                        param.setResult(gss);
                    }
                });

        findMethod(
                LocationManager.class,
                "requestLocationUpdates", String.class, long.class, float.class, LocationListener.class)
                .before(param -> {
                    Log.d(">>>>>>>>>>>", "requestLocationUpdates");
                    if (isEnable(XConstant.Key.ENABLE_VIRTUAL_LOCATION)) {
                        param.args[0] = proxyLocationListener(param.args[0]);
                    }
                });


        findMethod(
                LocationManager.class,
                "getLastKnownLocation", String.class)
                .after(param -> {
                    Log.d(">>>>>>>>>>>", "getLastKnownLocation");
//                        param.setResult(getLastKnownLocation(param.getResult()));
                    Location l = new Location(android.location.LocationManager.GPS_PROVIDER);
                    l.setLatitude(22.54245);
                    l.setLongitude(113.949098);
                    param.setResult(l);
                });

        findMethod(
                LocationManager.class,
                "getLastLocation")
                .after(param -> {
                    Log.d(">>>>>>>>>>>", "getLastKnownLocation");
//                        param.setResult(getLastKnownLocation(param.getResult()));
                    Location l = new Location(android.location.LocationManager.GPS_PROVIDER);
                    l.setLatitude(22.54245);
                    l.setLongitude(113.949098);
                    param.setResult(l);
                });
    }

    /**
     * amap sdk hook
     */
    private void hookAMap() {
        //hook amap getLastKnownLocation
        findMethod(
                "com.amap.api.location.AMapLocationClient",
                "getLastKnownLocation")
                .after(param -> {
                    Alog.d(this.getClass().getName(), String.format("invoke amap getLastKnownLocation"));
                    param.setResult(getLastKnownLocation(param.getResult()));
                });
        //hook amap setLocationListener
        findMethod(
                "com.amap.api.location.AMapLocationClient",
                "setLocationListener",
                "com.amap.api.location.AMapLocationListener")
                .before(param -> {
                    Alog.d(this.getClass().getName(), String.format("invoke amap AMapLocationListener"));
                    param.args[0] = proxyLocationListener(param.args[0]);
                });
    }

    /**
     * baidu map sdk hook
     */
    private void hookBaiduMap() {
        findMethod(
                "com.baidu.location.BDLocation",
                "getLatitude")
                .before(param -> {
                    Alog.d(this.getClass().getName(), String.format("invoke baidu map getLatitude"));
                    if (isEnable(XConstant.Key.ENABLE_VIRTUAL_LOCATION)) {
                        String latitude = getPString(XConstant.Key.LOCATION_LATITUDE);

                        if (!TextUtils.isEmpty(latitude)) {
                            Random mRandom = new Random();
                            int number = mRandom.nextInt(15 - 3 + 1) + 3;
                            param.setResult(Double.parseDouble(latitude) + Double.valueOf(number) / 100000);
                        }
                    }
                });
        findMethod(
                "com.baidu.location.BDLocation",
                "getLongitude")
                .before(param -> {
                    Alog.d(this.getClass().getName(), String.format("invoke baidu map getLongitude"));
                    if (isEnable(XConstant.Key.ENABLE_VIRTUAL_LOCATION)) {
                        String longitude = getPString(XConstant.Key.LOCATION_LONGITUDE);

                        if (!TextUtils.isEmpty(longitude)) {
                            Random mRandom = new Random();
                            int number = mRandom.nextInt(15 - 3 + 1) + 3;
                            param.setResult(Double.parseDouble(longitude) + Double.valueOf(number) / 100000);
                        }
                    }
                });
    }

    /**
     * tencent map sdk hook
     */
    private void hookTencentMap() {

    }

    /**
     * google map sdk hook
     */
    private void hookGoogleMap() {
//        findMethod(
//                com.google.android.gms.location.LocationCallback.class,
//                "onLocationResult",
//                LocationResult.class)
//                .before(param -> {
//                    Alog.d(this.getClass().getName(),
//                            "com.google.android.gms.location.LocationCallback.onLocationResult:"
//                                    + ((LocationResult) param.args[0]).getLastLocation().getLatitude()
//                                    + ((LocationResult) param.args[0]).getLastLocation().getLongitude());
//                });
//        findMethod(
//                GooglePlayServicesUtil.class,
//                "isGooglePlayServicesAvailable",
//                Context.class)
//                .after(param -> {
//                    Alog.d(this.getClass().getName(), "GooglePlayServicesUtil.isGooglePlayServicesAvailable:"
//                            + param.getResult());
//                });
//
//        findMethod(
//                FusedLocationProviderClient.class,
//                "getLastLocation"
//        )
//                .after(param -> {
//                    Alog.d(this.getClass().getName(), "FusedLocationProviderClient.getLastLocation:"
//                            + param.getResult());
//                });
//
//
//
//            findMethod(
//                    "com.alibaba.android.dingtalkbase.amap.GoogleLocationClient",
//                    "onConnected", Bundle.class)
//                    .before(param -> {
//                        Log.d("anysoft----------->",
//                                "GoogleLocationClient.onConnected:");
//                    });
//
//            findMethod(
//                    "com.alibaba.android.dingtalkbase.amap.GoogleLocationClient",
//                    "onConnectionSuspended", int.class)
//                    .after(param -> {
//                        Log.d("anysoft----------->",
//                                "GoogleLocationClient.onConnectionSuspended:");
//                    });
//
//            //com.alibaba.laiwang.xpn
//            findMethod(
//                    "com.alibaba.laiwang.xpn.XpnUtils",
//                    "isSupportMIUIPush",
//                    Context.class
//            )
//                    .after(param -> {
//                        Log.d("anysoft----------->", "XpnUtils.isSupportMIUIPush:"
//                                + param.getResult());
//                    });
//            findMethod(
//                    "com.alibaba.laiwang.xpn.XpnUtils",
//                    "isMIUIPushEnabled",
//                    Context.class
//            )
//                    .after(param -> {
//                        Log.d("anysoft----------->", "XpnUtils.isMIUIPushEnabled:"
//                                + param.getResult());
//                    });
//            findMethod(
//                    "com.alibaba.laiwang.xpn.XpnUtils",
//                    "isSupportHuaweiPush",
//                    Context.class
//            )
//                    .after(param -> {
//                        Log.d("anysoft----------->", "XpnUtils.isSupportHuaweiPush:"
//                                + param.getResult());
//                    });
//            findMethod(
//                    "com.alibaba.laiwang.xpn.XpnUtils",
//                    "isSupportFCM",
//                    Context.class
//            )
//                    .after(param -> {
//                        Log.d("anysoft----------->", "XpnUtils.isSupportFCM:"
//                                + param.getResult());
//                    });

    }

    private void setGpsStatus(GpsStatus gss) {
        Class<?> clazz = GpsStatus.class;
        Method m = null;
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals("setStatus")) {
                if (method.getParameterTypes().length > 1) {
                    m = method;
                    break;
                }
            }
        }
        m.setAccessible(true);
        //make the apps belive GPS works fine now
        int svCount = 5;
        int[] prns = {1, 2, 3, 4, 5};
        float[] snrs = {0, 0, 0, 0, 0};
        float[] elevations = {0, 0, 0, 0, 0};
        float[] azimuths = {0, 0, 0, 0, 0};
        int ephemerisMask = 0x1f;
        int almanacMask = 0x1f;
        //5 satellites are fixed
        int usedInFixMask = 0x1f;
        try {
            if (m != null) {
                m.invoke(gss, svCount, prns, snrs, elevations, azimuths, ephemerisMask, almanacMask, usedInFixMask);
            }
        } catch (Exception e) {
            Alog.d("locationplugin:", e.getLocalizedMessage());
        }
    }

    /**
     * 获取最后一次位置信息
     *
     * @param location
     * @return
     */
    private Object getLastKnownLocation(Object location) {
        return isEnableVirtualLocation() ? null : location;
    }

    /**
     * 代理位置监听
     *
     * @param listener
     * @return
     */
    private Object proxyLocationListener(Object listener) {

        if (!Proxy.isProxyClass(listener.getClass())) {
            // 创建代理类
            return Proxy.newProxyInstance(
                    listener.getClass().getClassLoader(),
                    listener.getClass().getInterfaces(),
                    new AMapLocationListenerProxy(listener));
        }
        return listener;
    }

    /**
     * 是否启用虚拟位置
     *
     * @return
     */
    private boolean isEnableVirtualLocation() {
        return isEnable(XConstant.Key.ENABLE_VIRTUAL_LOCATION);
    }


    /**
     * 位置监听代理
     */
    private final class AMapLocationListenerProxy implements InvocationHandler {

        private Object mListener;
        private Random mRandom = new Random();

        private AMapLocationListenerProxy(Object listener) {
            mListener = listener;
        }

        @Override
        public Object invoke(Object o, Method method, Object[] objects) throws Throwable {

            if (isEnableVirtualLocation()
                    && "onLocationChanged".equals(method.getName())) {
                // 开始处理
                handlerLocationChanged(objects);
            }
            return method.invoke(mListener, objects);
        }

        private void handlerLocationChanged(Object[] objects) {

            if (objects == null || objects.length != 1) return;

            Location location = (Location) objects[0];

            String latitude = getPString(XConstant.Key.LOCATION_LATITUDE);
            String longitude = getPString(XConstant.Key.LOCATION_LONGITUDE);

            if (!TextUtils.isEmpty(latitude) && !TextUtils.isEmpty(longitude)) {
                // 重新修改值
                int number = mRandom.nextInt(15 - 3 + 1) + 3;
                location.setLongitude(Double.parseDouble(longitude) + Double.valueOf(number) / 100000);
                location.setLatitude(Double.parseDouble(latitude) + Double.valueOf(number) / 100000);
            }
        }
    }
}
