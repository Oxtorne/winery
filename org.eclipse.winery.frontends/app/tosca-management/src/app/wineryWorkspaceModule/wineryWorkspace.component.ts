/********************************************************************************
 * *******************************************************************************
 *  * Copyright (c) ${YEAR} Contributors to the Eclipse Foundation
 *  *
 *  * See the NOTICE file(s) distributed with this work for additional
 *  * information regarding copyright ownership.
 *  *
 *  * This program and the accompanying materials are made available under the
 *  * terms of the Eclipse Public License 2.0 which is available at
 *  * http://www.eclipse.org/legal/epl-2.0, or the Apache Software License 2.0
 *  * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *  *
 *  * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *  *******************************************************************************
 *******************************************************************************/

import { Component, OnInit } from '@angular/core';
import { ReadmeService } from '../wineryReadmeModule/wineryReadme.service';
import { ToscaTypes } from '../model/enums';
import { WineryNotificationService } from '../wineryNotificationModule/wineryNotification.service';
import { InstanceService } from '../instance/instance.service';
import { HttpErrorResponse } from '@angular/common/http';
import { WorkspaceService } from './wineryWorkspace.service';

@Component({
    templateUrl: 'wineryWorkspace.component.html',
    styleUrls: ['wineryWorkspace.component.css'],
    providers: [WorkspaceService]
})

export class WineryWorkspaceComponent implements OnInit {

    loading = true;
    externalUrl = '';
    initialExternalUrl = '';
    
    externalUrlAvailable = true;
    toscaType: ToscaTypes;

    constructor(private service: WorkspaceService, private notify: WineryNotificationService, public sharedData: InstanceService) {
        this.toscaType = this.sharedData.toscaComponent.toscaType;

    }

    ngOnInit() {
        this.service.getData().subscribe(
            data => {
                this.externalUrl = data;
                this.initialExternalUrl = data;
            },
            () => this.handleMissingExternalUrl()
        );
    }

    saveExternalUrl() {
        this.service.save(this.externalUrl).subscribe(
            () => this.handleSave(),
            error => this.handleError(error)
        );
    }

    private handleError(error: HttpErrorResponse) {
        this.loading = false;
        this.notify.error(error.message);
    }

    private handleMissingExternalUrl() {
        this.loading = false;
        this.externalUrlAvailable = false;
    }

    private handleSave() {
        this.notify.success('Successfully saved ExternalUrl');
    }

}
