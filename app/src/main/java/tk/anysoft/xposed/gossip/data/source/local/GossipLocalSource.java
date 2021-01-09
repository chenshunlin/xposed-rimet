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

package tk.anysoft.xposed.gossip.data.source.local;

import tk.anysoft.xposed.gossip.data.cache.IGossipCache;
import tk.anysoft.xposed.gossip.data.model.ConfigModel;
import tk.anysoft.xposed.gossip.data.model.UpdateModel;
import tk.anysoft.xposed.gossip.data.model.VersionModel;
import tk.anysoft.xposed.gossip.data.source.IGossipSource;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

/**
 * Created by sky on 2019-05-27.
 */
public class GossipLocalSource implements IGossipSource {

    private IGossipCache mGossipCache;

    public GossipLocalSource(IGossipCache iGossipCache) {
        mGossipCache = iGossipCache;
    }

    @Override
    public Observable<UpdateModel> checkUpdate() {
        return Observable.create(emitter -> subscribe(emitter, () -> mGossipCache.getUpdateInfo()));
    }

    @Override
    public Observable<VersionModel> getSupportVersion() {
        return Observable.create(emitter -> subscribe(emitter, () -> mGossipCache.getSupportVersion()));
    }

    @Override
    public Observable<ConfigModel> getVersionConfig(String versionCode) {
        return Observable.create(emitter -> subscribe(emitter, () -> mGossipCache.getVersionConfig(versionCode)));
    }

    private <T> void subscribe(ObservableEmitter<T> emitter, OnHandler<T> handler) {

        try {
            // 开始处理
            T value = handler.onHandler();

            if (value != null) {
                // 返回结果
                emitter.onNext(value);
            }
            emitter.onComplete();
        } catch (Throwable tr) {
            emitter.onError(tr);
        }
    }

    private interface OnHandler<T> {

        T onHandler();
    }
}
