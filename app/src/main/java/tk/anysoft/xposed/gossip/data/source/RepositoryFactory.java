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

import tk.anysoft.xposed.gossip.data.cache.ICacheManager;
import tk.anysoft.xposed.gossip.data.cache.IGossipCache;
import tk.anysoft.xposed.gossip.data.cache.GossipCache;
import tk.anysoft.xposed.gossip.data.service.IServiceFactory;
import tk.anysoft.xposed.gossip.data.service.ServiceFactory;
import tk.anysoft.xposed.gossip.data.source.local.GossipLocalSource;
import tk.anysoft.xposed.gossip.data.source.remote.GossipRemoteSource;

/**
 * Created by sky on 2019-05-27.
 */
public class RepositoryFactory implements IRepositoryFactory {

    private IServiceFactory mServiceFactory;
    private IGossipCache mGossipCache;

    public RepositoryFactory(ICacheManager iCacheManager) {
        mServiceFactory = new ServiceFactory();
        mGossipCache = new GossipCache(iCacheManager);
    }

    @Override
    public IGossipSource createRimetSource() {
        return new GossipRepository(
                new GossipLocalSource(mGossipCache),
                new GossipRemoteSource(mServiceFactory, mGossipCache));
    }
}
