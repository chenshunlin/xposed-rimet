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

package com.sky.xposed.rimet;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.sky.xposed.common.util.Alog;
import com.sky.xposed.common.util.ToastUtil;
import com.sky.xposed.core.XStore;
import com.sky.xposed.core.adapter.CoreListenerAdapter;
import com.sky.xposed.core.adapter.ThrowableAdapter;
import com.sky.xposed.core.component.ComponentFactory;
import com.sky.xposed.core.interfaces.XConfig;
import com.sky.xposed.core.interfaces.XCoreManager;
import com.sky.xposed.core.interfaces.XPlugin;
import com.sky.xposed.core.internal.CoreManager;
import com.sky.xposed.javax.XposedPlus;
import com.sky.xposed.javax.XposedUtil;
import com.sky.xposed.rimet.plugin.LuckyPlugin;
import com.sky.xposed.rimet.util.FileUtil;
import com.sky.xposed.ui.util.CoreUtil;
import com.sky.xposed.ui.util.DisplayUtil;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by sky on 2019/3/14.
 */
public class Main implements IXposedHookLoadPackage {



    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpParam) throws Throwable {

        if (!XConstant.Rimet.PACKAGE_NAME.contains(lpParam.packageName)) return;

        // 初始化XposedPlus
        XposedPlus.setDefaultInstance(new XposedPlus.Builder(lpParam)
                .throwableCallback(new ThrowableAdapter())
                .build());
        Alog.i(this.getClass().getName(), String.format("Launch pacakage=%s processName=%s",lpParam.packageName,lpParam.processName));

        try {
            XposedUtil.findMethod(
                    "com.alibaba.android.dingtalkbase.multidexsupport.DDApplication", "onCreate")
                    .after(param -> handleLoadPackage(param, lpParam));

//            XposedUtil.findMethod(
//                    Application.class,
//                    "attach",
//                    Context.class)
//                    .after(param -> handleLoadPackage(param, lpParam));

            XposedUtil.findMethod(
                    "com.alibaba.android.rimet.LauncherApplication", "onCreate")
                    .after(param -> handleLoadPackage(param, lpParam));

            XposedUtil.findMethod(
                    "com.alibaba.android.rimet.biz.SplashActivity", "onResume")
                    .after(param -> handleLoadPackage(param, lpParam));



        } catch (Throwable throwable) {
            StringWriter sw = new StringWriter();
            throwable.printStackTrace(new PrintWriter(sw, true));
            Alog.setDebug(BuildConfig.DEBUG);
            Alog.i(this.getClass().getName(), sw.getBuffer().toString());

        }


    }

    /**
     * 处理加载的包
     *
     * @param param
     * @param lpParam
     * @throws Throwable
     */
    private void handleLoadPackage(
            XC_MethodHook.MethodHookParam param,
            XC_LoadPackage.LoadPackageParam lpParam) throws Throwable {
        Alog.i(this.getClass().getName(), "handleLoadPackage");
        Application application = (Application) param.thisObject;
        Context context = application.getApplicationContext();
        final String className = application.getClass().getName();
        Alog.i(this.getClass().getName(), String.format("handleLoadPackage: className=%s processName=%s",className,lpParam.processName));
        if (!"com.alibaba.android.rimet.LauncherApplication".equals(className)) {
            // 不需要处理
            Alog.i(this.getClass().getName(), "handleLoadPackage:" + className + " unneed");
//            return;
        }





//        XposedHelpers.setStaticObjectField(android.os.Build.class, "MODEL", "MI 6s Plus");
//        XposedHelpers.setStaticObjectField(android.os.Build.class, "MANUFACTURER", "Xiaomi");
//        XposedHelpers.setStaticObjectField(android.os.Build.class, "BRAND","Xiaomi");
//        XposedHelpers.setStaticObjectField(android.os.Build.class, "PRODUCT","natrium1");
//        XposedHelpers.setStaticObjectField(android.os.Build.class, "SERIAL", "cb16db331");

//        XposedHelpers.setStaticObjectField(android.os.Build.class, "HARDWARE", GetCatValue("hardware"));
//        XposedHelpers.setStaticObjectField(android.os.Build.class, "DEVICE", GetCatValue("device"));
//        XposedHelpers.setStaticObjectField(android.os.Build.class, "ID", GetCatValue("id"));
//        XposedHelpers.setStaticObjectField(android.os.Build.class, "DISPLAY", GetCatValue("display"));
//        XposedHelpers.setStaticObjectField(android.os.Build.class, "FINGERPRINT", GetCatValue("fingerprint"));

        XCoreManager coreManager = new CoreManager.Build(context)
                .setPluginPackageName(BuildConfig.APPLICATION_ID)
                .setProcessName(lpParam.processName)
                .setClassLoader(lpParam.classLoader)
                .setComponentFactory(new ComponentFactory() {
                    @Override
                    protected List<Class<? extends XConfig>> getVersionData() {
                        return XStore.getConfigClass();
                    }

                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    protected List<Class<? extends XPlugin>> getPluginData() {
                        List<Class<? extends XPlugin>> plugins = XStore.getPluginClass();
                        if (XConstant.Rimet.PACKAGE_NAME.get(1).equals(lpParam.packageName)) {
                            plugins.remove(LuckyPlugin.class);
                        }
                        StringBuilder lists = new StringBuilder();
                        plugins.forEach(
                                item -> {
                                    lists.append(item.getName()).append(",");
                                }
                        );
                        Alog.d(this.getClass().getName() + "init plugins:", lpParam.packageName + ": " + lists.toString());
                        return plugins;
                    }
                })
                .setCoreListener(new CoreListenerAdapter() {

                    @Override
                    public void onInitComplete(XCoreManager coreManager) {
                        super.onInitComplete(coreManager);

                        Alog.setDebug(BuildConfig.DEBUG);

                        final Context context = coreManager.getLoadPackage().getContext();

                        // 保存当前版本MD5信息
                        String md5 = FileUtil.getFileMD5(new File(context.getApplicationInfo().sourceDir));
                        coreManager.getDefaultPreferences().putString(XConstant.Key.PACKAGE_MD5, md5);

                        // 初始化
                        CoreUtil.init(coreManager);
                        DisplayUtil.init(context);
                        ToastUtil.getInstance().init(context);
                        Picasso.setSingletonInstance(new Picasso.Builder(context).build());
                    }
                })
                .build();
        Alog.setDebug(BuildConfig.DEBUG);
        Alog.d(this.getClass().getName(), "hook all init success!");
        // 开始处理加载的包
        coreManager.loadPlugins();
    }
}
