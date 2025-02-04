/*******************************************************************************
 * Copyright (c) 2019 Contributors to the Eclipse Foundation
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
import { Directive, Input, OnInit, TemplateRef, ViewContainerRef } from '@angular/core';
import { WineryRepositoryConfigurationService } from './WineryRepositoryConfiguration.service';

export enum FeatureEnum {
    Accountability = 'accountability', Completion = 'completion', Compliance = 'compliance', FreezeAndDefrost = 'freezeAndDefrost',
    ManagementFeatureEnrichment = 'managementFeatureEnrichment', NFV = 'nfv', PatternRefinement = 'patternRefinement', ProblemDetection = 'problemDetection',
    Splitting = 'splitting', TestRefinement = 'testRefinement',
}

@Directive({
    selector: '[wineryRepositoryFeatureToggle]'
})
export class FeatureToggleDirective implements OnInit {
    @Input() wineryRepositoryFeatureToggle: string;

    constructor(
        private templateRef: TemplateRef<any>,
        private viewContainer: ViewContainerRef,
        private configService: WineryRepositoryConfigurationService
    ) {
    }

    ngOnInit() {
        if (this.configService.configuration.features[this.wineryRepositoryFeatureToggle]) {
            this.viewContainer.createEmbeddedView(this.templateRef);
        } else {
            this.viewContainer.clear();
        }
    }
}
