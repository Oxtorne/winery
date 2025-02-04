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
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { backendBaseURL } from '../configuration';
import { Observable, Subject } from 'rxjs';

export interface WineryConfiguration {
    features: {
        accountability: boolean;
        completion: boolean;
        compliance: boolean;
        freezeAndDefrost: boolean;
        managementFeatureEnrichment: boolean;
        nfv: boolean;
        patternRefinement: boolean;
        problemDetection: boolean;
        splitting: boolean;
        testRefinement: boolean;
    };
    endpoints: {
        container: String;
        topologymodeler: String;
        workflowmodeler: String;
    };
}

@Injectable()
export class WineryRepositoryConfigurationService {
    configuration: WineryConfiguration;

    constructor(private http: HttpClient) {
    }

    /**
     * Sets the configuration Attribute for the service => Access the configuration file from the resource
     * In case of error
     * Is the style below the method better/is it applicable?
     */
    getConfigurationFromBackend(backendUrl?: string): Observable<boolean> {
        const subject = new Subject<boolean>();
        const baseUrl = backendUrl ? backendUrl : backendBaseURL;
        this.http.get<WineryConfiguration>(baseUrl + '/admin' + '/config')
            .subscribe((value) => {
                    this.configuration = value;
                    subject.next(true);
                    subject.complete();
                }, (err) => {
                    subject.next(false);
                    subject.complete();
                }
            );
        return subject.asObservable();
    }
}
