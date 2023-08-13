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

package tk.anysoft.xposed.lark;

import java.util.Arrays;
import java.util.List;

/**
 * Created by sky on 2019/3/14.
 */
public interface Constant {

    interface Service {

        String BASE_URL = "https://coding.net/u/sky_wei/p/xposed-rimet/git/raw/develop/";
    }

    interface Rimet {

        String PACKAGE_NAME = "com.larksuite.suite";
        List<String> PACKAGES_NAME = Arrays.asList(
                "com.ss.android.lark",
                "com.larksuite.suite"
        );
    }

    interface Event {

        int CLICK = 0x01;
    }

    interface Color {

        int BLUE = 0xFF393A3F;

        int TOOLBAR = 0xff303030;

        int TITLE = 0xff004198;

        int DESC = 0xff303030;
    }

    interface GroupId {

        int GROUP = 999;
    }

    interface Name {

        String TITLE = "飞书助手";

        String LARK = "lark";
    }

    interface XFlag {

        int MAIN_MENU = 0x000001;

        int ENABLE_LUCKY = 0x000002;//启用自动抢红包

        int LUCKY_DELAYED = 0x000003;//红包延时

        int ENABLE_FAST_LUCKY = 0x000004;//启用快速抢红包

        int ENABLE_RECALL = 0x000005;//消息防撤回


        int ENABLE_LOCATION = 0x000006;//启用定位模拟
        int LOCATIONNAME = 0x0000007;//当前使用的定位标题
        int LOCATIONDATA = 0x000008;//当前定位数据

        int LOCATIONLIST = 0x0000009;//存储的定位列表

        int ENABLE_WIFI = 0x00000A;//启用WIFI模拟
        int WIFINAME = 0x000000B;//当前WIFI名称
        int WIFIDATA = 0x000000C;//当前WIFI数据
        int WIFILIST = 0x000000D;//存储的WIFI列表
        int ENABLE_WIFI_CURRENT = 0x00000E;//启用WIFI模拟



        int ENABLE_BASESTATION = 0x00000F;//启用基站模拟
        int BASESTATIONNAME = 0x000010;//当前基站名称
        int BASESTATIONDATA = 0x000011;//当前基站数据
        int BASESTATIONLIST = 0x000012;//启用基站模拟

        int UPDATE_LAST_TIME = 0x000FFF;//更新


    }

    interface Preference {

        String MAIN_MENU = "main.menu";

        String AUTO_LOGIN = "other.autoLogin";

        String ACTIVITY_CYCLE = "develop.activityCycle";

        String ACTIVITY_START = "develop.activityStart";

        String ACTIVITY_RESULT = "develop.activityResult";

        String WECHAT_LOG = "develop.wechatLog";
    }

    interface Flag {

        int MAIN = 0xFF000000;
    }

    interface Plugin {

        int MAIN_SETTINGS = 0x00000000;

        int DEBUG = 0x01000000;

        int DING_DING = 0x02000000;

        int LUCKY_MONEY = 0x03000000;

        int REMITTANCE = 0x04000000;
    }

    interface ItemId {

        int MAIN_SETTINGS = 60001;
    }
}
