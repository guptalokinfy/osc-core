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
package org.osc.core.broker.service.tasks.conformance.deleteda;

import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.osc.core.broker.job.lock.LockObjectReference;
import org.osc.core.broker.model.entities.appliance.DistributedAppliance;
import org.osc.core.broker.model.entities.appliance.DistributedApplianceInstance;
import org.osc.core.broker.model.entities.appliance.VirtualSystem;
import org.osc.core.broker.model.entities.virtualization.SecurityGroup;
import org.osc.core.broker.model.entities.virtualization.SecurityGroupInterface;
import org.osc.core.broker.model.entities.virtualization.openstack.DeploymentSpec;
import org.osc.core.broker.service.persistence.DistributedApplianceEntityMgr;
import org.osc.core.broker.service.persistence.OSCEntityManager;
import org.osc.core.broker.service.tasks.TransactionalTask;
import org.osgi.service.component.annotations.Component;
@Component(service = ForceDeleteDATask.class)
public class ForceDeleteDATask extends TransactionalTask {
    private static final Logger log = Logger.getLogger(ForceDeleteDATask.class);

    private DistributedAppliance da;

    public ForceDeleteDATask create(DistributedAppliance da) {
        ForceDeleteDATask task = new ForceDeleteDATask();
        task.da = da;
        task.name = task.getName();
        task.dbConnectionManager = this.dbConnectionManager;
        task.txBroadcastUtil = this.txBroadcastUtil;

        return task;
    }

    @Override
    public String getName() {
        return "Force Delete Distributed Appliance '" + this.da.getName() + "'";
    }

    @Override
    public void executeTransaction(EntityManager em) {
        log.debug("Force Delete Task for DA: " + this.da.getId());
        // load Distributed Appliance from Database
        DistributedAppliance da = DistributedApplianceEntityMgr.findById(em, this.da.getId());

        // remove all virtual systems for this DA
        for (VirtualSystem vs : da.getVirtualSystems()) {

            // remove all DAI(s)
            for (DistributedApplianceInstance dai : vs.getDistributedApplianceInstances()) {
                OSCEntityManager.delete(em, dai, this.txBroadcastUtil);
            }

            // remove all SGI(s) - SG references
            for (SecurityGroupInterface sgi : vs.getSecurityGroupInterfaces()) {
                for (SecurityGroup sg : sgi.getSecurityGroups()) {
                    sgi.removeSecurity(sg);
                    sg.removeSecurityInterface(sgi);
                    OSCEntityManager.update(em, sg, this.txBroadcastUtil);
                    OSCEntityManager.update(em, sgi, this.txBroadcastUtil);
                }
            }

            // remove all Deployment Specs for this virtual system
            for (DeploymentSpec ds : vs.getDeploymentSpecs()) {
                OSCEntityManager.delete(em, ds, this.txBroadcastUtil);
            }

            // remove all SGI for this virtual system
            for (SecurityGroupInterface sgi : vs.getSecurityGroupInterfaces()) {
                OSCEntityManager.delete(em, sgi, this.txBroadcastUtil);
            }

            // delete virtual system from database
            OSCEntityManager.delete(em, vs, this.txBroadcastUtil);
        }

        // delete distributed appliance from database
        OSCEntityManager.delete(em, da, this.txBroadcastUtil);
    }

    @Override
    public Set<LockObjectReference> getObjects() {
        return LockObjectReference.getObjectReferences(this.da);
    }

}
