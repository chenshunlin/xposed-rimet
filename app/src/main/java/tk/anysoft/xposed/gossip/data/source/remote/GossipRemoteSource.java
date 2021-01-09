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

package tk.anysoft.xposed.gossip.data.source.remote;

import tk.anysoft.xposed.gossip.data.cache.IGossipCache;
import tk.anysoft.xposed.gossip.data.model.ConfigModel;
import tk.anysoft.xposed.gossip.data.model.UpdateModel;
import tk.anysoft.xposed.gossip.data.model.VersionModel;
import tk.anysoft.xposed.gossip.data.service.IGossipService;
import tk.anysoft.xposed.gossip.data.service.IServiceFactory;
import tk.anysoft.xposed.gossip.data.source.IGossipSource;

import io.reactivex.Observable;

/**
 * Created by sky on 2019-05-27.
 */
public class GossipRemoteSource implements IGossipSource {

    private IServiceFactory mServiceFactory;
    private IGossipCache mGossipCache;

    public GossipRemoteSource(IServiceFactory iServiceFactory, IGossipCache iGossipCache) {
        mServiceFactory = iServiceFactory;
        mGossipCache = iGossipCache;
    }

    @Override
    public Observable<UpdateModel> checkUpdate() {
        return mServiceFactory
                .createService(IGossipService.class)
                .checkUpdate()
                .doOnNext(model -> mGossipCache.saveUpdateInfo(model));
    }

    @Override
    public Observable<VersionModel> getSupportVersion() {
        return mServiceFactory
                .createService(IGossipService.class)
                .getSupportVersion()
                .doOnNext(model -> mGossipCache.saveSupportVersion(model));
    }

    @Override
    public Observable<ConfigModel> getVersionConfig(String versionCode) {
        return mServiceFactory
                .createService(IGossipService.class)
                .getVersionConfig(versionCode)
                .doOnNext(model -> mGossipCache.saveConfigModel(versionCode, model));
    }
}
