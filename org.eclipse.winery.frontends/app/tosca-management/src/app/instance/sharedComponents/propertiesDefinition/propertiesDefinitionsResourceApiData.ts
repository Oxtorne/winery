/*******************************************************************************
 * Copyright (c) 2017 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache Software License 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *******************************************************************************/

export enum PropertiesDefinitionEnum {
    Custom = 'Custom',
    Element = 'Element',
    Type = 'Type',
    None = 'None'
}

export class PropertiesDefinitionKVElement {
    key: string = null;
    type: string = null;
}

export class PropertiesDefinition {
    element: string = null;
    type: string = null;
}

export class WinerysPropertiesDefinition {
    namespace: string = null;
    elementName: string = null;
    propertyDefinitionKVList: PropertiesDefinitionKVElement[] = [];
    isDerivedFromXSD = false;
}

export class ExternalWorkspaceUrlDefinition {
    externalWorkspaceUrl: string = null;
}

export interface PropertiesDefinitionsResourceApiData {
    propertiesDefinition: PropertiesDefinition;
    winerysPropertiesDefinition: WinerysPropertiesDefinition;
    selectedValue: PropertiesDefinitionEnum;
    externalWorkspaceUrlDef: ExternalWorkspaceUrlDefinition;
}
