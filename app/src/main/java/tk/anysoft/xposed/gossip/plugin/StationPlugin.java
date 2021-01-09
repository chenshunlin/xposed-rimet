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

import android.os.Bundle;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;

import com.sky.xposed.annotations.APlugin;
import com.sky.xposed.common.util.Alog;
import com.sky.xposed.core.interfaces.XCoreManager;

import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import tk.anysoft.xposed.gossip.BuildConfig;
import tk.anysoft.xposed.gossip.XConstant;
import tk.anysoft.xposed.gossip.plugin.base.BasePlugin;

/**
 * Created by sky on 2020/3/25.
 */
@APlugin
public class StationPlugin extends BasePlugin {

    public StationPlugin(XCoreManager coreManager) {
        super(coreManager);
    }

    @Override
    public void hook() {

        findMethod(TelephonyManager.class, "getCellLocation")
                .before(this::handlerGetCellLocation);

        findMethod(TelephonyManager.class, "getAllCellInfo")
                .before(this::handlerGetAllCellInfo);

        findMethod(PhoneStateListener.class, "onCellLocationChanged", CellLocation.class)
                .before(param -> {
                    Alog.d(this.getClass().getName(), "invoke onCellInfoChanged");
                });

        findMethod(PhoneStateListener.class, "onCellInfoChanged", List.class)
                .before(param -> {
                    Alog.d(this.getClass().getName(), "invoke onCellInfoChanged");
                });


    }

    private void handlerGetCellLocation(XC_MethodHook.MethodHookParam param) {
        Alog.d(this.getClass().getName(), "invoke handlerGetCellLocation");
        if (!isEnable(XConstant.Key.ENABLE_VIRTUAL_STATION)) {
            // 没有启用不需要处理
            return;
        }

        int mcc = getPInt(XConstant.Key.STATION_MCC, -99);
        int mnc = getPInt(XConstant.Key.STATION_MNC);
        int lac = getPInt(XConstant.Key.STATION_LAC);
        int cellId = getPInt(XConstant.Key.STATION_CELL_ID);

        if (mcc == -99) {
            // 没有设置值暂时不处理
            return;
        }

        if (mnc != 3 && mnc != 5 && mnc != 11) {
            Bundle bundle = new Bundle();
            bundle.putInt("lac", lac);
            bundle.putInt("cid", cellId);
            param.setResult(new GsmCellLocation(bundle));

            Alog.d(this.getClass().getName(), " 设置Gsm信息: " + bundle);
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putInt("networkId", lac);
        bundle.putInt("baseStationId", cellId);
        param.setResult(new CdmaCellLocation(bundle));

        Alog.d(this.getClass().getName(), " 设置Cdma信息: " + bundle);
    }

    private void handlerGetAllCellInfo(XC_MethodHook.MethodHookParam param) {
        Alog.d(this.getClass().getName(), "invoke handlerGetAllCellInfo");
        if (!isEnable(XConstant.Key.ENABLE_VIRTUAL_STATION)) {
            // 没有启用不需要处理
            return;
        }

        int mcc = getPInt(XConstant.Key.STATION_MCC, -99);
        int mnc = getPInt(XConstant.Key.STATION_MNC);
        int lac = getPInt(XConstant.Key.STATION_LAC);
        int cellId = getPInt(XConstant.Key.STATION_CELL_ID);

        if (mcc == -99) {
            // 没有设置值暂时不处理
            return;
        }

        if (mnc != 3 && mnc != 5 && mnc != 11) {
            Bundle bundle = new Bundle();
            bundle.putInt("lac", lac);
            bundle.putInt("cid", cellId);
            param.setResult(new GsmCellLocation(bundle));

            Alog.d(this.getClass().getName(), " 设置Gsm信息: " + bundle);
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putInt("networkId", lac);
        bundle.putInt("baseStationId", cellId);
        param.setResult(new CdmaCellLocation(bundle));

        Alog.d(this.getClass().getName(), " 设置Cdma信息: " + bundle);
    }

    @Override
    public boolean isEnable(String key) {
        return BuildConfig.DEBUG || super.isEnable(key);
    }
}

