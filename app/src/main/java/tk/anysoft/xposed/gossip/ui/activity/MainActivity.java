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

package tk.anysoft.xposed.gossip.ui.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.sky.xposed.common.util.Alog;
import com.sky.xposed.common.util.PackageUtil;
import tk.anysoft.xposed.gossip.BuildConfig;
import tk.anysoft.xposed.gossip.R;
import tk.anysoft.xposed.gossip.XConstant;
import tk.anysoft.xposed.gossip.ui.dialog.LoveDialog;
import tk.anysoft.xposed.gossip.ui.dialog.SettingsDialog;
import tk.anysoft.xposed.gossip.ui.util.ActivityUtil;
import tk.anysoft.xposed.gossip.ui.util.DialogUtil;
import tk.anysoft.xposed.gossip.util.GsonUtil;

import com.sky.xposed.ui.view.ItemMenu;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ItemMenu imVersion = findViewById(R.id.im_version);
        imVersion.setDesc("v" + BuildConfig.VERSION_NAME);

        //钉钉版本信息
        ItemMenu imDingVersion = findViewById(R.id.im_ding_version);
        if (getVersionName(XConstant.Gossip.PACKAGE_NAME.get(0)).equals("Unknown")) {
            imDingVersion.setVisibility(View.GONE);

        } else {
            imDingVersion.setDesc(getVersionName(XConstant.Gossip.PACKAGE_NAME.get(0)));
        }

        //lite 版本信息
        ItemMenu imDingLiteVersion = findViewById(R.id.im_dinglite_version);
        imDingLiteVersion.setVisibility(View.GONE);
        TextView tvSupportVersion = findViewById(R.id.tv_support_version);


//        XVersionManager versionManager = CoreUtil.getCoreManager().getVersionManager();

        StringBuilder builder = new StringBuilder();
        builder.append("\n\n兼容: \nGPS、WIFI、Station、Tencent/Amap/Baidu map\n");
        tvSupportVersion.setText(builder.toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (BuildConfig.DEBUG) {
            getMenuInflater().inflate(R.menu.activity_main_menu, menu);
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.menu_settings) {
            // 显示
            SettingsDialog dialog = new SettingsDialog();
            dialog.show(getFragmentManager(), "setting");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("MissingPermission")
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.im_download:
                // 下载
                ActivityUtil.openUrl(this, "https://github.com/anysoft/xposed-rimet/releases");
                break;
            case R.id.im_source:
                // 源地址
                ActivityUtil.openUrl(this, "https://github.com/anysoft/xposed-rimet/tree/resurrection");
                break;
            case R.id.im_document:
                // 文档地址
                ActivityUtil.openUrl(this, "http://blog.skywei.info/2019-04-18/xposed_rimet");
                break;
            case R.id.im_love:
                // 公益
                LoveDialog loveDialog = new LoveDialog();
                loveDialog.show(getFragmentManager(), "love");
                break;
            case R.id.im_about:
                // 关于
                if (BuildConfig.DEBUG){
                    TelephonyManager telephonyManager = (TelephonyManager)this.getApplicationContext()
                            .getSystemService(Context.TELEPHONY_SERVICE);
                    @SuppressLint("MissingPermission")
                    CellLocation cellLocation = telephonyManager.getCellLocation();
                    Alog.d(this.getClass().getName(), GsonUtil.toJson(cellLocation));
                    Alog.d(this.getClass().getName(), GsonUtil.toJson(telephonyManager.getAllCellInfo()));


                    break;
                }
                DialogUtil.showAboutDialog(this);
                break;
        }
    }

    private String getVersionName(String packageName) {
        PackageUtil.SimplePackageInfo info = null;
        // 获取版本名
        try {
            info = PackageUtil
                    .getSimplePackageInfo(this, packageName);


        } catch (Exception e) {
            Alog.d("no package", packageName);
        }
        return info == null ? "Unknown" : "v" + info.getVersionName();
    }
}
