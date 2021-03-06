/**
 * Copyright 2016 Yahoo Inc.
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
package com.yahoo.pulsar.common.policies.data;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonGenerationException;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.yahoo.pulsar.common.util.ObjectMapperFactory;

@Test
public class PoliciesDataTest {

    @Test
    void policies() {
        Policies policies = new Policies();

        assertEquals(policies, new Policies());

        policies.auth_policies.namespace_auth.put("my-role", EnumSet.of(AuthAction.consume));

        assertTrue(!policies.equals(new Policies()));
        assertTrue(!policies.equals(new Object()));

        policies.auth_policies.namespace_auth.clear();
        Map<String, Set<AuthAction>> permissions = Maps.newTreeMap();
        permissions.put("my-role", EnumSet.of(AuthAction.consume));
        policies.auth_policies.destination_auth.put("persistent://my-dest", permissions);

        assertTrue(!policies.equals(new Policies()));
    }

    @Test
    void propertyAdmin() {
        PropertyAdmin pa1 = new PropertyAdmin();
        pa1.setAdminRoles(Lists.newArrayList("role1", "role2"));
        pa1.setAllowedClusters(Sets.newHashSet("use", "usw"));

        assertEquals(pa1, new PropertyAdmin(Lists.newArrayList("role1", "role2"), Sets.newHashSet("use", "usw")));
        assertTrue(!pa1.equals(new Object()));
        assertTrue(!pa1.equals(new PropertyAdmin()));
        assertTrue(!pa1.equals(new PropertyAdmin(Lists.newArrayList("role1", "role3"), Sets.newHashSet("usc"))));
        assertEquals(pa1.getAdminRoles(), Lists.newArrayList("role1", "role2"));
    }

    @Test
    void bundlesPolicies() throws JsonGenerationException, JsonMappingException, IOException {
        ObjectMapper jsonMapper = ObjectMapperFactory.create();
        String oldJsonPolicy = "{\"auth_policies\":{\"namespace_auth\":{},\"destination_auth\":{}},\"replication_clusters\":[],"
                + "\"backlog_quota_map\":{},\"persistence\":null,\"latency_stats_sample_rate\":{},\"message_ttl_in_seconds\":0}";
        Policies policies = jsonMapper.readValue(oldJsonPolicy.getBytes(), Policies.class);
        assertEquals(policies, new Policies());
        String newJsonPolicy = "{\"auth_policies\":{\"namespace_auth\":{},\"destination_auth\":{}},\"replication_clusters\":[],\"bundles\":null,"
                + "\"backlog_quota_map\":{},\"persistence\":null,\"latency_stats_sample_rate\":{},\"message_ttl_in_seconds\":0}";
        OldPolicies oldPolicies = jsonMapper.readValue(newJsonPolicy.getBytes(), OldPolicies.class);
        assertEquals(oldPolicies, new OldPolicies());
    }

    @Test
    void bundlesData() throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper jsonMapper = ObjectMapperFactory.create();
        String newJsonPolicy = "{\"auth_policies\":{\"namespace_auth\":{},\"destination_auth\":{}},\"replication_clusters\":[],\"bundles\":{\"boundaries\":[\"0x00000000\",\"0xffffffff\"]},\"backlog_quota_map\":{},\"persistence\":null,\"latency_stats_sample_rate\":{}}";

        List<String> bundleSet = Lists.newArrayList();
        bundleSet.add("0x00000000");
        bundleSet.add("0xffffffff");

        String newBundlesDataString = "{\"boundaries\":[\"0x00000000\",\"0xffffffff\"]}";
        BundlesData data = jsonMapper.readValue(newBundlesDataString.getBytes(), BundlesData.class);
        assertEquals(data.getBoundaries(), bundleSet);

        Policies policies = jsonMapper.readValue(newJsonPolicy.getBytes(), Policies.class);
        Policies expected = new Policies();
        expected.bundles = data;
        assertEquals(policies, expected);
    }
}
