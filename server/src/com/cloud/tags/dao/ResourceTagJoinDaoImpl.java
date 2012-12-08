// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package com.cloud.tags.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

import javax.ejb.Local;

import org.apache.log4j.Logger;

import com.cloud.api.ApiDBUtils;
import com.cloud.api.ApiResponseHelper;
import com.cloud.api.response.DomainRouterResponse;
import com.cloud.api.response.NicResponse;
import com.cloud.api.response.ResourceTagResponse;
import com.cloud.api.response.SecurityGroupResponse;
import com.cloud.api.response.UserVmResponse;
import com.cloud.api.view.vo.DomainRouterJoinVO;
import com.cloud.api.view.vo.ResourceTagJoinVO;
import com.cloud.dc.DataCenter;
import com.cloud.network.Network;
import com.cloud.network.Networks.TrafficType;
import com.cloud.network.router.VirtualRouter;
import com.cloud.offering.ServiceOffering;
import com.cloud.projects.Project;
import com.cloud.server.ResourceTag;
import com.cloud.user.Account;
import com.cloud.uservm.UserVm;
import com.cloud.utils.Pair;
import com.cloud.utils.db.Attribute;
import com.cloud.utils.db.DB;
import com.cloud.utils.db.Filter;
import com.cloud.utils.db.GenericDaoBase;
import com.cloud.utils.db.JoinBuilder;
import com.cloud.utils.db.SearchBuilder;
import com.cloud.utils.db.SearchCriteria;
import com.cloud.utils.db.Transaction;
import com.cloud.utils.exception.CloudRuntimeException;
import com.cloud.vm.NicProfile;


@Local(value={ResourceTagJoinDao.class})
public class ResourceTagJoinDaoImpl extends GenericDaoBase<ResourceTagJoinVO, Long> implements ResourceTagJoinDao {
    public static final Logger s_logger = Logger.getLogger(ResourceTagJoinDaoImpl.class);

    private SearchBuilder<ResourceTagJoinVO> vrSearch;

    private SearchBuilder<ResourceTagJoinVO> vrIdSearch;

    protected ResourceTagJoinDaoImpl() {

        vrSearch = createSearchBuilder();
        vrSearch.and("idIN", vrSearch.entity().getId(), SearchCriteria.Op.IN);
        vrSearch.done();

        vrIdSearch = createSearchBuilder();
        vrIdSearch.and("id", vrIdSearch.entity().getId(), SearchCriteria.Op.EQ);
        vrIdSearch.done();

        this._count = "select count(distinct id) from resource_tag_view WHERE ";
    }





    @Override
    public ResourceTagResponse newResourceTagResponse(ResourceTagJoinVO resourceTag, boolean keyValueOnly) {
        ResourceTagResponse response = new ResourceTagResponse();
        response.setKey(resourceTag.getKey());
        response.setValue(resourceTag.getValue());

        if (!keyValueOnly) {
            response.setResourceType(resourceTag.getResourceType().toString());
            response.setResourceId(resourceTag.getResourceUuid());

            ApiResponseHelper.populateOwner(response, resourceTag);

            response.setDomainId(resourceTag.getDomainUuid());
            response.setDomainName(resourceTag.getDomainName());

            response.setCustomer(resourceTag.getCustomer());
        }

        response.setObjectName("tag");

        return response;
    }


    @Override
    public List<ResourceTagJoinVO> searchByIds(Long... ids) {
        SearchCriteria<ResourceTagJoinVO> sc = vrSearch.create();
        sc.setParameters("idIN", ids);
        return searchIncludingRemoved(sc, null, null, false);
    }


    @Override
    public ResourceTagJoinVO newResourceTagView(ResourceTag vr) {

        List<ResourceTagJoinVO> uvList = new ArrayList<ResourceTagJoinVO>();
        SearchCriteria<ResourceTagJoinVO> sc = vrIdSearch.create();
        sc.setParameters("id", vr.getId());
        List<ResourceTagJoinVO> vms = searchIncludingRemoved(sc, null, null, false);
        assert vms != null && vms.size() == 1 : "No tag found for tag id " + vr.getId();
        return vms.get(0);

    }

}
