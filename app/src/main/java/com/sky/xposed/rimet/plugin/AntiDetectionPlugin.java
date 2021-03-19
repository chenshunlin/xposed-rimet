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

import android.content.pm.PackageInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.sky.xposed.annotations.APlugin;
import com.sky.xposed.common.util.Alog;
import com.sky.xposed.core.interfaces.XCoreManager;
import com.sky.xposed.rimet.XConstant;
import com.sky.xposed.rimet.plugin.base.BaseDingPlugin;

import de.robv.android.xposed.XposedHelpers;

/**
 * Created by anysoft on 2020/12/19.
 */
@APlugin
public class AntiDetectionPlugin extends BaseDingPlugin {

    public AntiDetectionPlugin(XCoreManager coreManager) {
        super(coreManager);
    }

    @Override
    public void hook() {
        Alog.d(this.getClass().getName(), " Loading and init pugin....");
        //反检测 xp fakegps等 应该是获取列表hook 还有钉钉自身方法hook 返回通过
        //降级处理，获取版本这里尝试获取官方或者其他应用商店最新版本，让钉钉始终认为最新版打卡，或者设置指定版本号

        //设备号模拟，虽然thanox和设备变量都支持，但是集成到助手方便太极这类没全局插件的设备

        //版本信息检测
        findMethod(
                "android.app.ApplicationPackageManager",
                "getPackageInfo", String.class, int.class)
                .after(param -> {
                    if (isEnable(XConstant.Key.ENABLE_ANTI_DETECTION)) {
                        if (isEnable(XConstant.Key.ENABLE_ANTI_DOWNGRADE)) {
                            if (XConstant.Rimet.PACKAGE_NAME.contains(param.args[0])) {
                                Alog.d(this.getClass().getName(), String.format("getPackageInfo: packageName=%s , flags=%d", param.args[0], param.args[1]));
                                Alog.d(this.getClass().getName(), "getPackageInfo:" + param.args[0] + "   " + param.args[1]);
                                PackageInfo info = (PackageInfo) param.getResult();
                                Alog.d(this.getClass().getName(), String.format("PackageInfo: versionName=%s , versionCode=%d", info.versionName, info.versionCode));
                                info.versionName = getPString(XConstant.Key.PACKAGE_VERSION_NAME);
                                info.versionCode = getPInt(XConstant.Key.PACKAGE_VERSION_CODE);
                                Alog.d(this.getClass().getName(), String.format("PackageInfo: versionName=%s , versionCode=%d", info.versionName, info.versionCode));
                                param.setResult(info);
                            }
                        }
                    }
                });
        //android9+ versioncode
        if (Build.VERSION.SDK_INT >= 28) {
            findMethod(
                    PackageInfo.class,
                    "getLongVersionCode")
                    .after(param -> {
                        if (isEnable(XConstant.Key.ENABLE_ANTI_DETECTION)
                                && isEnable(XConstant.Key.ENABLE_ANTI_DOWNGRADE)) {
                            if (XConstant.Rimet.PACKAGE_NAME.contains(param.args[0])) {
                                Integer versionCode = getPInt(XConstant.Key.PACKAGE_VERSION_NAME);
                                param.setResult(versionCode);
                                Alog.d(this.getClass().getName(), String.format("getLongVersionCode: versionCode=%d", versionCode));
                            }
                        }
                    });
        }
        //imei
        findMethod(TelephonyManager.class, "getDeviceId")
                .after(param -> {
                    if (isEnable(XConstant.Key.ENABLE_ANTI_DETECTION)
                            && isEnable(XConstant.Key.ENABLE_ANTI_DEVICE)) {
                        String deviceId = getPString(XConstant.Key.DEVICE_IMEI);
                        if (!TextUtils.isEmpty(deviceId)) {
                            param.setResult(deviceId);
                        }
                    }
//                    param.setResult("863818039613410");
                    Alog.d(this.getClass().getName(), String.format("getDeviceId: imei=%s", param.getResult().toString()));
                });
        //systemproperty

        findMethod("android.os.SystemProperties", "get",String.class, String.class)
                .before(param -> {
                    if (isEnable(XConstant.Key.ENABLE_ANTI_DETECTION)
                            && isEnable(XConstant.Key.ENABLE_ANTI_DEVICE)) {
//                        String deviceId = getPString(XConstant.Key.DEVICE_IMEI);
//
//                        if (!TextUtils.isEmpty(deviceId)) {
//                            param.setResult(deviceId);
//                        }
                    }
                    Alog.d(this.getClass().getName(), String.format("SystemProperties: arg1=%s arg2=%s result=%s", param.args[0],param.args[1],param.getResult()));
                });

//String model = android.os.Build.MODEL;获取手机型号。Mi5splus
//   String brand = android.os.Build.BRAND;获取手机品牌。 xiaomi
// String carrier= android.os.Build.MANUFACTURER;

        try {
            XposedHelpers.setStaticObjectField(Class.forName("com.alibaba.android.rimet.BuildConfig").getClass(), "VERSION_NAME", "5.1.41");
            XposedHelpers.setStaticObjectField(Class.forName("com.alibaba.android.rimet.BuildConfig"), "VERSION_CODE", 726);
            XposedHelpers.setStaticObjectField(Class.forName("com.alibaba.android.rimet.BuildConfig"), "BUILD_ID", "14368995");
        } catch (ClassNotFoundException e) {
            Log.i("Launch pacakage","Launch pacakage");
            e.printStackTrace();
        }
    }
}
