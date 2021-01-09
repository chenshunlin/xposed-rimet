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

package tk.anysoft.xposed.gossip.data.source;

import tk.anysoft.xposed.gossip.data.model.ConfigModel;
import tk.anysoft.xposed.gossip.data.model.UpdateModel;
import tk.anysoft.xposed.gossip.data.model.VersionModel;

import io.reactivex.Observable;

/**
 * Created by sky on 2019-05-27.
 */
public class GossipRepository implements IGossipSource {

    private IGossipSource mLocal;
    private IGossipSource mRemote;

    public GossipRepository(IGossipSource local, IGossipSource remote) {
        mLocal = local;
        mRemote = remote;
    }

    @Override
    public Observable<UpdateModel> checkUpdate() {
        return Observable
                .concat(mLocal.checkUpdate(), mRemote.checkUpdate())
                .firstElement()
                .toObservable();
    }

    @Override
    public Observable<VersionModel> getSupportVersion() {
        return Observable
                .concat(mLocal.getSupportVersion(), mRemote.getSupportVersion())
                .firstElement()
                .toObservable();
    }

    @Override
    public Observable<ConfigModel> getVersionConfig(String versionCode) {
        return Observable
                .concat(mLocal.getVersionConfig(versionCode), mRemote.getVersionConfig(versionCode))
                .firstElement()
                .toObservable();
    }
}
