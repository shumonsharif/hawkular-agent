#!/usr/bin/env bash
#
# Copyright 2015-2017 Red Hat, Inc. and/or its affiliates
# and other contributors as indicated by the @author tags.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#


docker > /dev/null 2>&1 || { echo "docker is required, but is not found. Make sure it is accessible."; exit 1; }
TAG="${1:-latest}"

pushd "$( dirname "${BASH_SOURCE[0]}" )"

echo "Building Docker image for Wildfly + Hawkular javaagent with tag $TAG."
docker build -t wildfly-hawkular-javaagent:$TAG . -f Dockerfile

echo "Building Docker image for Wildfly + Hawkular Wildfly Agent (Standalone) with tag $TAG."
docker build -t wildfly-hawkular-agent:$TAG -f Dockerfile-wf-agent .
echo "Building Docker image for Wildfly + Hawkular Wildfly Agent (Domain) with tag $TAG."
docker build -t wildfly-hawkular-agent-domain:$TAG -f Dockerfile-wf-agent-domain .

popd
