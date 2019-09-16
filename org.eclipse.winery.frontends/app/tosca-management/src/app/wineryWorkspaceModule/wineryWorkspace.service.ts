import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { InstanceService } from '../instance/instance.service';
import { Observable } from 'rxjs';
import { backendBaseURL } from '../configuration';

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

@Injectable()
export class WorkspaceService {

    constructor(private http: HttpClient,
                private sharedData: InstanceService) {
    }

    getData(): Observable<string> {
        const headers = new HttpHeaders({ 'Accept': 'text/plain' });
        return this.http.get(
            backendBaseURL + this.sharedData.path + '/workspaceurl.md',
            { headers: headers, responseType: 'text' }
        );
    }

    save(workspaceUrl: String): Observable<HttpResponse<string>> {
        return this.http.put<string>(
            backendBaseURL + this.sharedData.path + '/workspaceurl.md',
            workspaceUrl,
            { observe: 'response' });
    }

}
