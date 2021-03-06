/*
 * Copyright 2015-2017 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hawkular.agent.monitor.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.hawkular.agent.monitor.util.Util;

/**
 * @author Joel Takvorian
 */
class InventoryMetric {
    static final int DATA_RETENTION = 7;

    private final String feed;
    private final String type;
    private final String id;
    private final Set<String> resourceTypes;
    private final Set<String> metricTypes;

    private InventoryMetric(String feed, String type, String id, Set<String> resourceTypes, Set<String> metricTypes) {
        this.feed = feed;
        this.type = type;
        this.id = id;
        this.resourceTypes = resourceTypes;
        this.metricTypes = metricTypes;
    }

    static InventoryMetric resource(String feed, String id, Set<String> resourceTypes, Set<String> metricTypes) {
        return new InventoryMetric(feed, "r", id, resourceTypes, metricTypes);
    }

    static InventoryMetric resourceType(String feed, String id) {
        return new InventoryMetric(feed, "rt", id, null, null);
    }

    static InventoryMetric metricType(String feed, String id) {
        return new InventoryMetric(feed, "mt", id, null, null);
    }

    String getFeed() {
        return feed;
    }

    String getType() {
        return type;
    }

    String getId() {
        return id;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InventoryMetric that = (InventoryMetric) o;

        if (feed != null ? !feed.equals(that.feed) : that.feed != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override public int hashCode() {
        int result = feed != null ? feed.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return name();
    }

    public String name() {
        return "inventory." + feed + "." + type + "." + id;
    }

    public String encodedName() {
        return Util.urlEncode(name());
    }

    public WithData full(byte[] data) {
        return new WithData(feed, type, id, resourceTypes, metricTypes,
                Collections.singletonList(InventoryStringDataPoint.create(System.currentTimeMillis(), data)));
    }

    public WithData chunks(List<byte[]> chunks, int totalSize) {
        List<InventoryStringDataPoint> data = new ArrayList<>(chunks.size());
        // Put all chunks in reverse timestamp order so that it can be easily queried with sort=DESC&fromEarliest=true
        long timestamp = System.currentTimeMillis();
        for (byte[] chunk : chunks) {
            data.add(InventoryStringDataPoint.create(timestamp, chunk));
            timestamp--;
        }
        // Set size in master chunk
        data.get(0).setMasterInfo(chunks.size(), totalSize);
        return new WithData(feed, type, id, resourceTypes, metricTypes, data);
    }

    public MetricDefinition toMetricDefinition() {
        MetricDefinition def = new MetricDefinition(name(), DATA_RETENTION);
        def.addTag("module", "inventory");
        def.addTag("feed", feed);
        def.addTag("type", type);
        def.addTag("id", id);
        if (resourceTypes != null) {
            def.addTag("restypes",
                    "|" + resourceTypes.stream().collect(Collectors.joining("|")) + "|");
        }
        if (metricTypes != null) {
            def.addTag("mtypes",
                    "|" + metricTypes.stream().collect(Collectors.joining("|")) + "|");
        }
        return def;
    }

    static class WithData extends InventoryMetric {
        private List<InventoryStringDataPoint> data;

        private WithData(String feed,
                         String type,
                         String id,
                         Set<String> resourceTypes,
                         Set<String> metricTypes,
                         List<InventoryStringDataPoint> data) {
            super(feed, type, id, resourceTypes, metricTypes);
            this.data = data;
        }

        @Override
        public MetricDefinition toMetricDefinition() {
            MetricDefinition def = super.toMetricDefinition();
            if (!data.isEmpty()) {
                data.get(0).getNbChunks().ifPresent(n -> def.addTag("chunks", n));
            }
            return def;
        }

        public boolean isEmpty() {
            return data.isEmpty();
        }

        public String getPayload() {
            return Util.toJson(data);
        }
    }
}
