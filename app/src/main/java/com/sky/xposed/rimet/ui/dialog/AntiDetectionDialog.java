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

package com.sky.xposed.rimet.ui.dialog;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sky.xposed.common.util.Alog;
import com.sky.xposed.common.util.ToastUtil;
import com.sky.xposed.core.interfaces.XPreferences;
import com.sky.xposed.rimet.XConstant;
import com.sky.xposed.rimet.ui.util.DialogUtil;
import com.sky.xposed.rimet.ui.util.XViewUtil;
import com.sky.xposed.ui.UIAttribute;
import com.sky.xposed.ui.base.BasePluginDialog;
import com.sky.xposed.ui.info.UAttributeSet;
import com.sky.xposed.ui.util.DisplayUtil;
import com.sky.xposed.ui.util.LayoutUtil;
import com.sky.xposed.ui.view.EditTextItemView;
import com.sky.xposed.ui.view.GroupItemView;
import com.sky.xposed.ui.view.PluginFrameLayout;
import com.sky.xposed.ui.view.XEditItemView;

import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sky on 2020-03-22.
 */
public class AntiDetectionDialog extends BasePluginDialog {

    private EditTextItemView settingsPackageVersion;

    private XPreferences mPreferences;


    @Override
    public void createView(PluginFrameLayout frameView) {

        LinearLayout.LayoutParams params = LayoutUtil.newWrapLinearLayoutParams();
        params.leftMargin = DisplayUtil.dip2px(getContext(), 15);
        params.topMargin = DisplayUtil.dip2px(getContext(), 10);
        params.bottomMargin = DisplayUtil.dip2px(getContext(), 5);

        TextView tvTip = new TextView(getContext());
        tvTip.setLayoutParams(params);
        tvTip.setText("配置设备、环境、应用信息，防止应用安全扫描！");
        tvTip.setTextSize(12);

        frameView.addSubView(tvTip);

        GroupItemView antiDetectionGroup = new GroupItemView(getContext());
        antiDetectionGroup.setVisibility(View.GONE);
        XViewUtil.newSwitchItemView(getContext(), "反检测", "开启时会修改当前设备ID、应用版本信息")
                .trackBind(XConstant.Key.ENABLE_ANTI_DETECTION, Boolean.FALSE, antiDetectionGroup)
                .addToFrame(frameView);

        /*****************   版本信息   ****************/
        XViewUtil.newSortItemView(getContext(), "版本信息")
                .addToFrame(antiDetectionGroup);
        GroupItemView downGradeGroup = new GroupItemView(getContext());
        downGradeGroup.setVisibility(View.GONE);
        XViewUtil.newSwitchItemView(getContext(), "版本信息", "开启时会修改当前应用版本信息")
                .trackBind(XConstant.Key.ENABLE_ANTI_DOWNGRADE, Boolean.FALSE, downGradeGroup)
                .addToFrame(antiDetectionGroup);
        downGradeGroup.addToFrame(antiDetectionGroup);

        settingsPackageVersion = new EditTextItemView(getContext(), new UAttributeSet.Build()
                .putInt(UIAttribute.EditTextItem.style, XEditItemView.Style.MULTI_LINE)
                .build());
        settingsPackageVersion.setName("版本号");
        settingsPackageVersion.setExtendHint("设置应用版本信息");
        settingsPackageVersion.setOnItemClickListener(view -> {
            AtomicReference<String> versionName = new AtomicReference<>("5.0.0");
            AtomicReference<Integer> versionCode = new AtomicReference<>(598);
            AtomicReference<String> versionInfo = new AtomicReference<>(versionName.get() + "(" + versionCode.get() + ")");
            String alias = mPreferences.getString(XConstant.Key.PACKAGE_VERSION_INFO, "");
            try {
                Alog.d(this.getClass().getName(), "PackageName=" + getContext().getPackageName());
                mPreferences.putBoolean(XConstant.Key.ENABLE_ANTI_DOWNGRADE, false);//
                PackageInfo info = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
                versionName.set(info.versionName);
                versionCode.set(Long.valueOf(info.versionCode).intValue());
                Alog.d(this.getClass().getName(), String.format("really versionName=%s versionCode=%s ", versionName.get(), versionCode.get()));
            } catch (PackageManager.NameNotFoundException e) {
                Alog.d(this.getClass().getName(), "get really packageinfo error NameNotFoundException");
            }
            if ("".equals(alias)) {
                alias = versionInfo.get();
            }
            mPreferences.putBoolean(XConstant.Key.ENABLE_ANTI_DOWNGRADE, true);
            DialogUtil.showEditDialog(getContext(),
                    "版本设置", alias, "请输入版本号", (dialogView, value) -> {

                        if (TextUtils.isEmpty(value)) {
                            showMessage("输入的信息不能为空!");
                            return;
                        }

                        Pattern p = Pattern.compile("([\\d+\\.]+\\d+)\\((\\d+)\\)");
                        Matcher m = p.matcher(value);
                        if (m.find() && m.groupCount() == 2) {
                            versionName.set(m.group(1));
                            versionCode.set(Integer.valueOf(m.group(2)));
                            versionInfo.set(versionName.get() + "(" + versionCode.get() + ")");
                            Alog.d(this.getClass().getName(), String.format("diy versionName=%s versionCode=%d ", versionName.get(), versionCode.get()));
                        } else {
                            ToastUtil.show("版本格式错误\r\n" +
                                    "eg:" + versionInfo.get());
                        }
                        // 保存版本号
                        mPreferences.putString(XConstant.Key.PACKAGE_VERSION_NAME, versionName.get());
                        mPreferences.putInt(XConstant.Key.PACKAGE_VERSION_CODE, versionCode.get());
                        mPreferences.putString(XConstant.Key.PACKAGE_VERSION_INFO, versionInfo.get());
                        settingsPackageVersion.setExtend(versionInfo.get());
                    });
        });
        settingsPackageVersion.trackBind(XConstant.Key.PACKAGE_VERSION_INFO, "");
        settingsPackageVersion.addToFrame(downGradeGroup);
        antiDetectionGroup.addToFrame(frameView);


    }

    @Override
    protected PluginFrameLayout onCreatePluginFrame() {
        return createLinePluginFrame();
    }

    @Override
    protected void initView(View view, Bundle args) {
        super.initView(view, args);

        mPreferences = getDefaultPreferences();
        getTitleView().setElevation(DisplayUtil.DIP_4);

        showBack();
        setTitle("反检测");
        showMoreMenu();
    }

    @Override
    public void onCreateMoreMenu(Menu menu) {
        super.onCreateMoreMenu(menu);

//        menu.add(0, 1, 0, "添加");
//        menu.add(0, 2, 0, "清空");
//        menu.add(0, 3, 0, "导入");
//        menu.add(0, 4, 0, "导出");
    }

    @Override
    public boolean onMoreItemSelected(MenuItem item) {

        int itemId = item.getItemId();

        if (1 == itemId) {
            return true;
        }
        return super.onMoreItemSelected(item);
    }

}
