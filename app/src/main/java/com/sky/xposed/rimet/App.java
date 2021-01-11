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
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.sky.xposed.common.util.Alog;
import com.sky.xposed.common.util.ToastUtil;
import com.sky.xposed.core.XStore;
import com.sky.xposed.core.adapter.CoreListenerAdapter;
import com.sky.xposed.core.component.ComponentFactory;
import com.sky.xposed.core.interfaces.XConfig;
import com.sky.xposed.core.interfaces.XCoreManager;
import com.sky.xposed.core.interfaces.XPlugin;
import com.sky.xposed.core.internal.CoreManager;
import com.sky.xposed.rimet.plugin.LuckyPlugin;
import com.sky.xposed.ui.util.CoreUtil;
import com.sky.xposed.ui.util.DisplayUtil;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by sky on 2019/3/26.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        XCoreManager coreManager = new CoreManager.Build(this)
                .setProcessName(getPackageName())
                .setClassLoader(getClassLoader())
                .setPluginPackageName(getPackageName())
                .setComponentFactory(new ComponentFactory() {
                    @Override
                    protected List<Class<? extends XConfig>> getVersionData() {
                        return XStore.getConfigClass();
                    }

                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    protected List<Class<? extends XPlugin>> getPluginData() {
                        List<Class<? extends XPlugin>> plugins = XStore.getPluginClass();
                        if (XConstant.Rimet.PACKAGE_NAME.get(1).equals(getPackageName())) {
                            plugins.remove(LuckyPlugin.class);
                        }
                        StringBuilder lists = new StringBuilder();
                        plugins.forEach(
                                item -> {
                                    lists.append(item.getName()).append(",");
                                }
                        );
                        Alog.d(this.getClass().getName() + "init plugins:", getPackageName() + ": " + lists.toString());
                        return plugins;
                    }
                })
                .setCoreListener(new CoreListenerAdapter())
                .build();

        // 初始化
        Alog.setDebug(BuildConfig.DEBUG);
        Alog.d(this.getClass().getName(), "hook all init success!");
        CoreUtil.init(coreManager);
        DisplayUtil.init(this);
        ToastUtil.getInstance().init(this);
        Picasso.setSingletonInstance(new Picasso.Builder(this).build());
    }
}
