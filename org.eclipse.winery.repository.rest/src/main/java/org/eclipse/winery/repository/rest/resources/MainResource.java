/********************************************************************************
 * Copyright (c) 2012-2019 Contributors to the Eclipse Foundation
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
package org.eclipse.winery.repository.rest.resources;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.eclipse.winery.repository.importing.CsarImportOptions;
import org.eclipse.winery.repository.importing.CsarImporter;
import org.eclipse.winery.repository.importing.ImportMetaInformation;
import org.eclipse.winery.repository.rest.RestUtils;
import org.eclipse.winery.repository.rest.resources.API.APIResource;
import org.eclipse.winery.repository.rest.resources.admin.AdminTopResource;
import org.eclipse.winery.repository.rest.resources.compliancerules.ComplianceRulesResource;
import org.eclipse.winery.repository.rest.resources.entitytemplates.artifacttemplates.ArtifactTemplatesResource;
import org.eclipse.winery.repository.rest.resources.entitytemplates.policytemplates.PolicyTemplatesResource;
import org.eclipse.winery.repository.rest.resources.entitytypeimplementations.nodetypeimplementations.NodeTypeImplementationsResource;
import org.eclipse.winery.repository.rest.resources.entitytypeimplementations.relationshiptypeimplementations.RelationshipTypeImplementationsResource;
import org.eclipse.winery.repository.rest.resources.entitytypes.artifacttypes.ArtifactTypesResource;
import org.eclipse.winery.repository.rest.resources.entitytypes.capabilitytypes.CapabilityTypesResource;
import org.eclipse.winery.repository.rest.resources.entitytypes.nodetypes.NodeTypesResource;
import org.eclipse.winery.repository.rest.resources.entitytypes.policytypes.PolicyTypesResource;
import org.eclipse.winery.repository.rest.resources.entitytypes.relationshiptypes.RelationshipTypesResource;
import org.eclipse.winery.repository.rest.resources.entitytypes.requirementtypes.RequirementTypesResource;
import org.eclipse.winery.repository.rest.resources.imports.ImportsResource;
import org.eclipse.winery.repository.rest.resources.patternrefinementmodels.PatternRefinementModelsResource;
import org.eclipse.winery.repository.rest.resources.servicetemplates.ServiceTemplatesResource;
import org.eclipse.winery.repository.rest.resources.testrefinementmodels.TestRefinementModelsResource;
import org.eclipse.winery.repository.rest.resources.threats.ThreatsResource;
import org.eclipse.winery.repository.rest.resources.yaml.YAMLParserResource;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * All paths listed here have to be listed in Jersey's filter configuration
 */
@Api()
@Path("/")
public class MainResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainResource.class);

    @Path("API/")
    public APIResource api() {
        return new APIResource();
    }

    @Path("artifacttemplates/")
    public ArtifactTemplatesResource artifacttemplates() {
        return new ArtifactTemplatesResource();
    }

    @Path("artifacttypes/")
    public ArtifactTypesResource artifactypes() {
        return new ArtifactTypesResource();
    }

    @Path("admin/")
    public AdminTopResource admin() {
        return new AdminTopResource();
    }

    @Path("capabilitytypes/")
    public CapabilityTypesResource capabilitytypes() {
        return new CapabilityTypesResource();
    }

    @Path("imports/")
    public ImportsResource imports() {
        return new ImportsResource();
    }

    @Path("nodetypes/")
    public NodeTypesResource nodetypes() {
        return new NodeTypesResource();
    }

    @Path("nodetypeimplementations/")
    public NodeTypeImplementationsResource nodetypeimplementations() {
        return new NodeTypeImplementationsResource();
    }

    @Path("policytemplates/")
    public PolicyTemplatesResource policytemplates() {
        return new PolicyTemplatesResource();
    }

    @Path("policytypes/")
    public PolicyTypesResource policytypes() {
        return new PolicyTypesResource();
    }

    @Path("relationshiptypes/")
    public RelationshipTypesResource relationshiptypes() {
        return new RelationshipTypesResource();
    }

    @Path("requirementtypes/")
    public RequirementTypesResource requirementtypes() {
        return new RequirementTypesResource();
    }

    @Path("relationshiptypeimplementations/")
    public RelationshipTypeImplementationsResource relationshiptypeimplementations() {
        return new RelationshipTypeImplementationsResource();
    }

    @Path("servicetemplates/")
    public ServiceTemplatesResource servicetemplates() {
        return new ServiceTemplatesResource();
    }

    @Path("compliancerules/")
    public ComplianceRulesResource compliancerules() {
        return new ComplianceRulesResource();
    }

    @Path("patternrefinementmodels/")
    public PatternRefinementModelsResource patternRefinementModels() {
        return new PatternRefinementModelsResource();
    }

    @Path("testrefinementmodels/")
    public TestRefinementModelsResource testRefinementModelsResource() {
        return new TestRefinementModelsResource();
    }

    @Path("yaml/")
    public YAMLParserResource yamlParser() {
        return new YAMLParserResource();
    }

    @Path("threats")
    public ThreatsResource threats() {
        return new ThreatsResource();
    }

    /**
     * Returns the main page of winery.
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response onGet() {
        return Response.ok("<html>\n"
            + "<body>\n"
            + "<p>\n"
            + "This is Winery's API.\n"
            + "Please open the Angular UI.\n"
            + "In <strong>development mode</strong>, it is available on <a href=\"http://localhost:4200/\">http://localhost:4200/</a> (ng serve) or <a href=\"http://localhost:8080/\">http://localhost:8080/</a> (configured in IntelliJ).\n"
            + "</p>\n"
            + "\n"
            + "<p>\n"
            + "Configuration of IntelliJ is described <a href=\"http://eclipse.github.io/winery/dev/config/IntelliJ%20IDEA/\">here</a>.\n"
            + "</p>\n"
            + "\n"
            + "<p>Swagger API description is available at <a href=\"http://localhost:8080/winery/swagger\">http://localhost:8080/winery/swagger</a>.\n"
            + "Open <a href=\"http://petstore.swagger.io/\">the Swagger Petsore</a>.\n"
            + "Key in <code>http://localhost:8080/winery/swagger</code> as URL at the top of the page.\n"
            + "</p>\n"
            + "</body>\n"
            + "\n").build();
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ApiOperation(value = "Imports the given CSAR (sent by simplesinglefileupload.jsp)")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "success",
            responseHeaders = @ResponseHeader(description = "If the CSAR could be partially imported, the points where it failed are returned in the body"))
    })
    // @formatter:off
    public Response importCSAR(
        @FormDataParam("file") InputStream uploadedInputStream, @FormDataParam("file") FormDataContentDisposition fileDetail,
        @FormDataParam("overwrite") @ApiParam(value = "true: content of CSAR overwrites existing content. false (default): existing content is kept") Boolean overwrite,
        @FormDataParam("validate") @ApiParam(value = "true: validates the hash of the manifest file with the one stored in the accountability layer") Boolean validate,
        @Context UriInfo uriInfo) {
        LocalDateTime start = LocalDateTime.now();
        // @formatter:on
        CsarImporter importer = new CsarImporter();
        CsarImportOptions options = new CsarImportOptions();
        options.setOverwrite((overwrite != null) && overwrite);
        options.setAsyncWPDParsing(false);
        options.setValidate((validate != null) && validate);
        ImportMetaInformation importMetaInformation;
        try {
            importMetaInformation = importer.readCSAR(uploadedInputStream, options);
        } catch (Exception e) {
            return Response.serverError().entity("Could not import CSAR").entity(e.getMessage()).build();
        }
        if (importMetaInformation.errors.isEmpty()) {
            if (options.isValidate()) {

                return Response.ok(importMetaInformation, MediaType.APPLICATION_JSON).build();
            } else if (Objects.nonNull(importMetaInformation.entryServiceTemplate)) {
                URI url = uriInfo.getBaseUri().resolve(RestUtils.getAbsoluteURL(importMetaInformation.entryServiceTemplate));
                LOGGER.debug("CSAR import lasted {}", Duration.between(LocalDateTime.now(), start).toString());
                return Response.created(url).build();
            } else {
                LOGGER.debug("CSAR import lasted {}", Duration.between(LocalDateTime.now(), start).toString());
                return Response.noContent().build();
            }
        } else {
            LOGGER.debug("CSAR import lasted {}", Duration.between(LocalDateTime.now(), start).toString());
            // In case there are errors, we send them as "bad request"
            return Response.status(Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).entity(importMetaInformation).build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_JSON)
    public Response importDefinitions(InputStream is) throws IOException {
        File toscaFile;
        toscaFile = File.createTempFile("TOSCA", ".tosca");
        FileUtils.copyInputStreamToFile(is, toscaFile);
        CsarImporter importer = new CsarImporter();
        List<String> errors = new ArrayList<>();
        CsarImportOptions options = new CsarImportOptions();
        options.setOverwrite(false);
        options.setAsyncWPDParsing(true);
        options.setValidate(false);
        importer.importDefinitions(null, toscaFile.toPath(), errors, options);

        if (errors.isEmpty()) {
            return Response.noContent().build();
        } else {
            return Response.status(Status.BAD_REQUEST).entity(errors).build();
        }
    }
}
