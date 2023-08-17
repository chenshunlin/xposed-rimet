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

package tk.anysoft.lark.plugin;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.sky.xposed.annotations.APlugin;
import com.sky.xposed.common.util.Alog;
import com.sky.xposed.common.util.ResourceUtil;
import com.sky.xposed.common.util.ToastUtil;
import com.sky.xposed.core.interfaces.XCoreManager;
import tk.anysoft.lark.BuildConfig;
import tk.anysoft.lark.R;
import tk.anysoft.lark.ui.dialog.SettingsDialog;
import tk.anysoft.lark.plugin.base.BaseDingPlugin;

import com.sky.xposed.ui.util.DisplayUtil;
import com.sky.xposed.ui.util.LayoutUtil;
import com.sky.xposed.ui.view.SimpleItemView;

import java.util.List;

/**
 * Created by sky on 2018/12/30.
 */
@APlugin()
public class SettingsPlugin extends BaseDingPlugin {

    public SettingsPlugin(XCoreManager coreManager) {
        super(coreManager);
    }

    @Override
    public void hook() {
        Alog.d(this.getClass().getName(), " Loading and init pugin....");
        // 插入菜单弹出
//        com.ss.android.lark.setting.page.function.SettingPageActivity
        findMethod(
                "com.ss.android.lark.upgrade.setting.about.AboutActivity",
                "onCreate",
                Bundle.class)
                .after(param -> onHandlerSettings((Activity) param.thisObject));
    }

    private void onHandlerSettings(Activity activity) {
//        LinearLayout  view = activity.findViewById(ResourceUtil.getId(activity, "openSourceSoftwareNotice_Title"));
//        ViewGroup viewGroup = (ViewGroup) view.getParent();
//
//        final int index = viewGroup.indexOfChild(view);

        LinearLayout linearLayout = new LinearLayout(activity);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        SimpleItemView itemView = new SimpleItemView(activity);
        itemView.setMinimumHeight(DisplayUtil.DIP_55);
        itemView.getNameView().setTextSize(16);
        itemView.setName(activity.getString(R.string.app_name));
        itemView.setExtend("v" + BuildConfig.VERSION_NAME);
        itemView.setOnClickListener(v -> {
            // 打开设置
            openSettings(activity);
        });
        linearLayout.addView(itemView);
        itemView.callOnClick();

//
//        View lineView = new View(activity);
//        lineView.setBackgroundColor(0xFFEFEFEF);
//        lineView.setBackgroundColor(view.getDrawingCacheBackgroundColor());//todo
//        linearLayout.addView(lineView, new LayoutUtil.Build()
//                .setHeight(2)
//                .linearParams());
////        viewGroup.addView(linearLayout, index);
//        ToastUtil.show("aaaab");
    }

    private void openSettings(Activity activity) {

        SettingsDialog dialog = new SettingsDialog();
        dialog.show(activity);
    }
}
