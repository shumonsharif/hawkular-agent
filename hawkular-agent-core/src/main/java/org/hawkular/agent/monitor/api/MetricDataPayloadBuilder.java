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
package org.hawkular.agent.monitor.api;

import org.hawkular.metrics.client.common.MetricType;

/**
 * When all metric data points are added (see the addDataPoint methods), call {@link #toPayload()}
 * to get the payload message that can be used to send to the storage backend via the storage adapter.
 */
public interface MetricDataPayloadBuilder {

    /**
     * Add a numeric metric data point.
     *
     * @param key identifies the metric
     * @param timestamp the time the metric was collected
     * @param value the numeric value of the metric
     * @param metricType the type of metric
     */
    void addDataPoint(String key, long timestamp, double value, MetricType metricType);

    /**
     * Add a string metric data point. The metric type is assumed to be {@link MetricType#STRING}.
     *
     * @param key identifies the metric
     * @param timestamp the time the metric was collected
     * @param value the string value of the metric
     */
    void addDataPoint(String key, long timestamp, String value);

    /**
     * @return the payload in a format suitable for the storage adapter.
     */
    Object toPayload();

    /**
     * @return the number of data points that were added to the payload.
     */
    int getNumberDataPoints();

    /**
     * If the metric data is to be stored with a special tenant ID, this sets that tenant ID.
     * If null is passed in, or if this method is not called, the agent's tenant ID is used.
     *
     * @param tenantId the tenant ID to associate the metric data with. May be null.
     */
    void setTenantId(String tenantId);

    /**
     * @return the tenant ID to be associated with the metric data. If null, the agent's tenant ID is used.
     */
    String getTenantId();
}
