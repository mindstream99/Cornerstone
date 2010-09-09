/*
 * Copyright 2010 the original author or authors.
 * Copyright 2009 Paxxis Technology LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 */

package com.paxxis.chime.client.common;

/**
 *
 * @author Robert Englander
 */
public abstract class BackReferencingDataInstance extends DataInstance {
    private InstanceId brId = InstanceId.create("0");
    private String brName = "";

    @Override
    protected <T extends DataInstance> T copy(T target) {
        if (!(target instanceof BackReferencingDataInstance)) {
            throw new RuntimeException();
        }

        BackReferencingDataInstance result = (BackReferencingDataInstance)super.copy(target);
        result.brId = brId;
        result.brName = brName;
        return (T)result;
    }

    @Override
    public boolean equals(Object other) {

        if (this == other) {
            return true;
        }

        if (other == null) {
            return false;
        }

        if (other.getClass() != getClass()) {
            return false;
        }

        BackReferencingDataInstance inst = (BackReferencingDataInstance)other;

        if (hashCode() != inst.hashCode()) {
            return false;
        }

        if (!super.equals(other)) {
            return false;
        }

        if (brId == null) {
            if (inst.brId != null) {
                return false;
            }
        } else {
            if (!brId.equals(inst.brId)) {
                return false;
            }
        }

        if (brName == null) {
            if (inst.brName != null) {
                return false;
            }
        } else {
            if (!brName.equals(inst.brName)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public void setBackRefId(InstanceId id) {
        brId = id;
    }

    public InstanceId getBackRefId() {
        return brId;
    }

    public void setBackRefName(String name) {
        brName = name;
    }

    public String getBackRefName() {
        return brName;
    }
}
