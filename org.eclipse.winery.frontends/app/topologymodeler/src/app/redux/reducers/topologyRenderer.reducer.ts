/********************************************************************************
 * Copyright (c) 2017-2019 Contributors to the Eclipse Foundation
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
 ********************************************************************************/

import { Action } from 'redux';
import { HighlightNodesAction, TopologyRendererActions } from '../actions/topologyRenderer.actions';

export interface TopologyRendererState {
    buttonsState: {
        targetLocationsButton?: boolean;
        policiesButton?: boolean;
        requirementsCapabilitiesButton?: boolean;
        deploymentArtifactsButton?: boolean;
        propertiesButton?: boolean;
        typesButton?: boolean;
        idsButton?: boolean;
        layoutButton?: boolean;
        alignHButton?: boolean;
        alignVButton?: boolean;
        importTopologyButton?: boolean;
        threatModelingButton?: boolean;
        splitTopologyButton?: boolean;
        matchTopologyButton?: boolean;
        problemDetectionButton?: boolean;
        enrichmentButton?: boolean;
        substituteTopologyButton?: boolean;
        refineTopologyButton?: boolean;
        refineTopologyWithTestsButton?: boolean;
        determineStatefulComponents?: boolean;
        determineFreezableComponentsButton?: boolean;
        cleanFreezableComponentsButton?: boolean;
    };
    nodesToSelect?: string[];
}

export const INITIAL_TOPOLOGY_RENDERER_STATE: TopologyRendererState = {
    buttonsState: {
        targetLocationsButton: false,
        policiesButton: false,
        requirementsCapabilitiesButton: false,
        deploymentArtifactsButton: false,
        propertiesButton: false,
        typesButton: true,
        idsButton: false,
        layoutButton: false,
        alignHButton: false,
        alignVButton: false,
        importTopologyButton: false,
        threatModelingButton: false,
        splitTopologyButton: false,
        matchTopologyButton: false,
        problemDetectionButton: false,
        enrichmentButton: false,
        substituteTopologyButton: false,
        refineTopologyButton: false,
        refineTopologyWithTestsButton: false,
        determineStatefulComponents: false,
        determineFreezableComponentsButton: false,
        cleanFreezableComponentsButton: false,
    }
};
/**
 * Reducer for the TopologyRenderer
 */
export const TopologyRendererReducer =
    function (lastState: TopologyRendererState = INITIAL_TOPOLOGY_RENDERER_STATE, action: Action): TopologyRendererState {
        switch (action.type) {
            case TopologyRendererActions.TOGGLE_POLICIES:
                // console.log({...lastState, buttonsState: { ...lastState.buttonsState, policiesButton:
                // !lastState.buttonsState.policiesButton}});
                return {
                    ...lastState,
                    buttonsState: {
                        ...lastState.buttonsState,
                        policiesButton: !lastState.buttonsState.policiesButton
                    }
                };
            case TopologyRendererActions.TOGGLE_TARGET_LOCATIONS:
                return {
                    ...lastState,
                    buttonsState: {
                        ...lastState.buttonsState,
                        targetLocationsButton: !lastState.buttonsState.targetLocationsButton
                    }
                };
            case TopologyRendererActions.TOGGLE_PROPERTIES:
                return {
                    ...lastState,
                    buttonsState: {
                        ...lastState.buttonsState,
                        propertiesButton: !lastState.buttonsState.propertiesButton
                    }
                };
            case TopologyRendererActions.TOGGLE_REQUIREMENTS_CAPABILITIES:
                return {
                    ...lastState,
                    buttonsState: {
                        ...lastState.buttonsState,
                        requirementsCapabilitiesButton: !lastState.buttonsState.requirementsCapabilitiesButton
                    }
                };
            case TopologyRendererActions.TOGGLE_DEPLOYMENT_ARTIFACTS:
                return {
                    ...lastState,
                    buttonsState: {
                        ...lastState.buttonsState,
                        deploymentArtifactsButton: !lastState.buttonsState.deploymentArtifactsButton
                    }
                };
            case TopologyRendererActions.TOGGLE_IDS:
                return {
                    ...lastState,
                    buttonsState: {
                        ...lastState.buttonsState,
                        idsButton: !lastState.buttonsState.idsButton
                    }
                };
            case TopologyRendererActions.TOGGLE_TYPES:
                return {
                    ...lastState,
                    buttonsState: {
                        ...lastState.buttonsState,
                        typesButton: !lastState.buttonsState.typesButton
                    }
                };
            case TopologyRendererActions.EXECUTE_LAYOUT:
                return {
                    ...lastState,
                    buttonsState: {
                        ...lastState.buttonsState,
                        layoutButton: !lastState.buttonsState.layoutButton
                    }
                };
            case TopologyRendererActions.EXECUTE_ALIGN_H:
                return {
                    ...lastState,
                    buttonsState: {
                        ...lastState.buttonsState,
                        alignHButton: !lastState.buttonsState.alignHButton
                    }
                };
            case TopologyRendererActions.EXECUTE_ALIGN_V:
                return {
                    ...lastState,
                    buttonsState: {
                        ...lastState.buttonsState,
                        alignVButton: !lastState.buttonsState.alignVButton
                    }
                };
            case TopologyRendererActions.IMPORT_TOPOLOGY:
                return {
                    ...lastState,
                    buttonsState: {
                        ...lastState.buttonsState,
                        importTopologyButton: !lastState.buttonsState.importTopologyButton
                    }
                };
            case TopologyRendererActions.THREATMODEL_TOPOLOGY:
                return {
                    ...lastState,
                    buttonsState: {
                        ...lastState.buttonsState,
                        threatModelingButton: !lastState.buttonsState.threatModelingButton
                    }
                };
            case TopologyRendererActions.SPLIT_TOPOLOGY:
                return {
                    ...lastState,
                    buttonsState: {
                        ...lastState.buttonsState,
                        splitTopologyButton: !lastState.buttonsState.splitTopologyButton
                    }
                };
            case TopologyRendererActions.MATCH_TOPOLOGY:
                return {
                    ...lastState,
                    buttonsState: {
                        ...lastState.buttonsState,
                        matchTopologyButton: !lastState.buttonsState.matchTopologyButton
                    }
                };
            case TopologyRendererActions.DETECT_PROBLEMS:
                return {
                    ...lastState,
                    buttonsState: {
                        ...lastState.buttonsState,
                        problemDetectionButton: !lastState.buttonsState.problemDetectionButton
                    }
                };
            case TopologyRendererActions.ENRICH_NODE_TEMPLATES:
                return {
                    ...lastState,
                    buttonsState: {
                        ...lastState.buttonsState,
                        enrichmentButton: !lastState.buttonsState.enrichmentButton
                }
            };
            case TopologyRendererActions.SUBSTITUTE_TOPOLOGY:
                return {
                    ...lastState,
                    buttonsState: {
                        ...lastState.buttonsState,
                        substituteTopologyButton: !lastState.buttonsState.substituteTopologyButton
                    }
                };
            case TopologyRendererActions.REFINE_TOPOLOGY:
                return {
                    ...lastState,
                    buttonsState: {
                        ...lastState.buttonsState,
                        refineTopologyButton: !lastState.buttonsState.refineTopologyButton
                    }
                };
            case TopologyRendererActions.REFINE_TOPOLOGY_WITH_TESTS:
                return {
                    ...lastState,
                    buttonsState: {
                        ...lastState.buttonsState,
                        refineTopologyWithTestsButton: !lastState.buttonsState.refineTopologyWithTestsButton
                    }
                };
            case TopologyRendererActions.HIGHLIGHT_NODES:
                const data = <HighlightNodesAction>action;
                if (data.nodesToHighlight) {
                    return {
                        ...lastState,
                        nodesToSelect: data.nodesToHighlight
                    };
                } else {
                    delete lastState.nodesToSelect;
                }
                break;
            case TopologyRendererActions.DETERMINE_STATEFUL_COMPONENTS:
                return {
                    ...lastState,
                    buttonsState: {
                        ...lastState.buttonsState,
                        determineStatefulComponents: !lastState.buttonsState.determineStatefulComponents
                    }
                };
            case TopologyRendererActions.DETERMINE_FREEZABLE_COMPONENTS:
                return {
                    ...lastState,
                    buttonsState: {
                        ...lastState.buttonsState,
                        determineFreezableComponentsButton: !lastState.buttonsState.determineFreezableComponentsButton
                    }
                };
            case TopologyRendererActions.CLEAN_FREEZABLE_COMPONENTS:
                return {
                    ...lastState,
                    buttonsState: {
                        ...lastState.buttonsState,
                        cleanFreezableComponentsButton: !lastState.buttonsState.cleanFreezableComponentsButton
                    }
                };
        }
        return lastState;
    };
