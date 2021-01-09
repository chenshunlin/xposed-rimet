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

package tk.anysoft.xposed.gossip.plugin;

import android.net.NetworkInfo;

import com.sky.xposed.annotations.APlugin;
import com.sky.xposed.common.util.Alog;
import com.sky.xposed.core.interfaces.XCoreManager;

import tk.anysoft.xposed.gossip.BuildConfig;
import tk.anysoft.xposed.gossip.plugin.base.BasePlugin;

/**
 * Created by anysoft on 2021/01/08
 */
@APlugin
public class NetWorkPlugin extends BasePlugin {

    public NetWorkPlugin(XCoreManager coreManager) {
        super(coreManager);
    }

    @Override
    public void hook() {

        findMethod(NetworkInfo.class, "isConnectedOrConnecting")
                .before(param -> {
                    Alog.d(this.getClass().getName(), "invoke isConnectedOrConnecting");
                    param.setResult(true);
                });

        findMethod(NetworkInfo.class, "isConnected")
                .before(param -> {
                    Alog.d(this.getClass().getName(), "invoke isConnected");
                    param.setResult(true);
                });

        findMethod(NetworkInfo.class, "isAvailable")
                .before(param -> {
                    Alog.d(this.getClass().getName(), "invoke isAvailable");
                    param.setResult(true);
                });
    }

    @Override
    public boolean isEnable(String key) {
        return BuildConfig.DEBUG || super.isEnable(key);
    }

}

