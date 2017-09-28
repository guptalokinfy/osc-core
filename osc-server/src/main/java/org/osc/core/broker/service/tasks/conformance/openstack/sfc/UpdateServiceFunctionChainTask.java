/*******************************************************************************
 * Copyright (c) Intel Corporation
 * Copyright (c) 2017
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
 *******************************************************************************/
package org.osc.core.broker.service.tasks.conformance.openstack.sfc;

import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import org.osc.core.broker.job.lock.LockObjectReference;
import org.osc.core.broker.model.entities.virtualization.SecurityGroup;
import org.osc.core.broker.model.entities.virtualization.ServiceFunctionChain;
import org.osc.core.broker.model.plugin.ApiFactoryService;
import org.osc.core.broker.service.tasks.TransactionalTask;
import org.osc.sdk.controller.element.NetworkElement;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(service = UpdateServiceFunctionChainTask.class)
public class UpdateServiceFunctionChainTask extends TransactionalTask {

    @Reference
    private ApiFactoryService apiFactory;

    private ServiceFunctionChain sfc;
    private SecurityGroup securityGroup;
    private List<NetworkElement> updatedPortPairGroups;

    public UpdateServiceFunctionChainTask create(ServiceFunctionChain sfc, SecurityGroup securityGroup,
            List<NetworkElement> updatedPortPairGroups) {
        UpdateServiceFunctionChainTask task = new UpdateServiceFunctionChainTask();

        task.sfc = sfc;
        task.securityGroup = securityGroup;
        task.updatedPortPairGroups = updatedPortPairGroups;
        task.apiFactory = this.apiFactory;
        task.name = task.getName();
        task.dbConnectionManager = this.dbConnectionManager;
        task.txBroadcastUtil = this.txBroadcastUtil;

        return task;
    }

    @Override
    public String getName() {
        return String.format("Updating Service Function Chain '%s' for Security Group '%s' under Project '%s'",
                this.sfc.getName(), this.securityGroup.getName(), this.securityGroup.getProjectName());
    }

    @Override
    public void executeTransaction(EntityManager em) throws Exception {
        this.sfc = em.find(ServiceFunctionChain.class, this.sfc.getId());
        this.securityGroup = em.find(SecurityGroup.class, this.securityGroup.getId());
        this.updatedPortPairGroups.size();
        //TODO: arvind implement this

    }

    @Override
    public Set<LockObjectReference> getObjects() {
        return LockObjectReference.getObjectReferences(this.sfc, this.securityGroup);
    }

}