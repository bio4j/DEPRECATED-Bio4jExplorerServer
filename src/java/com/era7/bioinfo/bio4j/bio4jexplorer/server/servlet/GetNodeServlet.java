/*
 * Copyright (C) 2011  "Bio4j"
 *
 * This file is part of Bio4j
 *
 * Bio4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.era7.bioinfo.bio4j.bio4jexplorer.server.servlet;

import com.amazonaws.services.simpledb.AmazonSimpleDBAsyncClient;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.amazonaws.services.simpledb.model.SelectResult;
import com.era7.bioinfo.bio4j.bio4jexplorer.server.CommonData;
import com.era7.bioinfo.bio4j.bio4jexplorer.server.RequestList;
import com.era7.bioinfo.bioinfoaws.util.CredentialsRetriever;
import com.era7.lib.bioinfoxml.bio4j.Bio4jNodeIndexXML;
import com.era7.lib.bioinfoxml.bio4j.Bio4jNodeXML;
import com.era7.lib.bioinfoxml.bio4j.Bio4jPropertyXML;
import com.era7.lib.bioinfoxml.bio4j.Bio4jRelationshipXML;
import com.era7.lib.communication.model.BasicSession;
import com.era7.lib.communication.xml.Request;
import com.era7.lib.communication.xml.Response;
import com.era7.lib.servletlibrary.servlet.BasicServlet;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

/**
 * Gets information about a specific node
 * @author Pablo Pareja Tobes <ppareja@era7.com>
 */
public class GetNodeServlet extends BasicServlet {

    @Override
    protected Response processRequest(Request request, BasicSession bs,
            Connection cnctn, HttpServletRequest hsr) throws Throwable {

        Response response = new Response();

        if (request.getMethod().equals(RequestList.GET_NODE_REQUEST)) {

            String nodeName = request.getParameters().getChildText("name");

            AmazonSimpleDBClient simpleDBClient = new AmazonSimpleDBAsyncClient(CredentialsRetriever.getBasicAWSCredentialsFromOurAMI());

            SelectRequest selectRequest = new SelectRequest();
            String selectExpression = "SELECT * from bio4j where NAME = '" + nodeName + "' AND ITEM_TYPE = 'node'";
            //System.out.println("selectExpression = " + selectExpression);
            selectRequest.setSelectExpression(selectExpression);

            SelectResult selectResult = simpleDBClient.select(selectRequest);

            if (selectResult.getItems().size() > 0) {

                Item item = selectResult.getItems().get(0);
                Bio4jNodeXML node = new Bio4jNodeXML();
                node.setNodeName(item.getName());
                List<Attribute> atts = item.getAttributes();

                for (Attribute attribute : atts) {

                    if (attribute.getName().equals(CommonData.DESCRIPTION_ATTRIBUTE)) {
                        node.setDescription(attribute.getValue());
                    } else if (attribute.getName().equals(CommonData.ITEM_TYPE_ATTRIBUTE)) {
                        node.setItemType(Bio4jNodeXML.NODE_ITEM_TYPE);
                    } else if (attribute.getName().equals(CommonData.INCOMING_RELATIONSHIPS_ATTRIBUTE)) {

                        if (!attribute.getValue().equals("-")) {
                            Bio4jRelationshipXML rel = new Bio4jRelationshipXML();
                            rel.setRelationshipName(attribute.getValue());
                            node.addIncomingRelationship(rel);
                        }


                    } else if (attribute.getName().equals(CommonData.OUTGOING_RELATIONSHIPS_ATTRIBUTE)) {

                        if (!attribute.getValue().equals("-")) {
                            Bio4jRelationshipXML rel = new Bio4jRelationshipXML();
                            rel.setRelationshipName(attribute.getValue());
                            node.addOutgoingRelationship(rel);
                        }

                    } else if (attribute.getName().equals(CommonData.JAVADOC_URL_ATTRIBUTE)) {

                        node.setJavadocUrl(attribute.getValue());

                    } else if (attribute.getName().equals(CommonData.DATA_SOURCE_ATTRIBUTE)) {

                        node.setDataSource(attribute.getValue());

                    }
                }

                //-------------GETTING NODE INDEXES--------------

                String indexesSelectExpression = "SELECT * from bio4j where ITEM_TYPE = 'node_index' AND NODE_NAME = '" + nodeName + "'";
                selectRequest.setSelectExpression(indexesSelectExpression);
                selectResult = simpleDBClient.select(selectRequest);

                if (selectResult.getItems().size() > 0) {

                    for (Item indexItem : selectResult.getItems()) {
                        Bio4jNodeIndexXML nodeIndex = new Bio4jNodeIndexXML();
                        nodeIndex.setIndexName(indexItem.getName());

                        atts = indexItem.getAttributes();

                        for (Attribute attribute : atts) {

                            if (attribute.getName().equals(CommonData.NODE_NAME_ATTRIBUTE)) {
                                nodeIndex.setNodeName(attribute.getValue());
                            } else if (attribute.getName().equals(CommonData.PROPERTY_INDEXED_ATTRIBUTE)) {
                                nodeIndex.setPropertyIndexed(attribute.getValue());
                            } else if (attribute.getName().equals(CommonData.INDEX_TYPE_ATTRIBUTE)) {
                                nodeIndex.setIndexType(attribute.getValue());
                            }
                        }

                        node.addIndex(nodeIndex);
                    }
                }
                
                //-------------GETTING NODE PROPERTIES--------------

                String proertiesSelectExpression = "SELECT * from bio4j where ITEM_TYPE = 'node_property' AND NODE_NAME = '" + nodeName + "'";
                selectRequest.setSelectExpression(proertiesSelectExpression);
                selectResult = simpleDBClient.select(selectRequest);

                if (selectResult.getItems().size() > 0) {

                    for (Item propertyItem : selectResult.getItems()) {
                        
                        Bio4jPropertyXML property = new Bio4jPropertyXML();

                        atts = propertyItem.getAttributes();

                        for (Attribute attribute : atts) {

                            if (attribute.getName().equals(CommonData.PROPERTY_NAME_ATTRIBUTE)) {
                                property.setPropertyName(attribute.getValue());
                            } else if (attribute.getName().equals(CommonData.INDEXED_ATTRIBUTE)) {
                                property.setIndexed(attribute.getValue());
                            } else if (attribute.getName().equals(CommonData.INDEX_TYPE_ATTRIBUTE)) {
                                property.setIndexType(attribute.getValue());
                            } else if (attribute.getName().equals(CommonData.INDEX_NAME_ATTRIBUTE)) {
                                property.setIndexName(attribute.getValue());
                            } else if (attribute.getName().equals(CommonData.PROPERTY_TYPE_ATTRIBUTE)) {
                                property.setType(attribute.getValue());
                            }
                        }

                        node.addProperty(property);
                    }
                }

                response.addChild(node);
                response.setStatus(Response.SUCCESSFUL_RESPONSE);

            } else {
                response.setError("There is no such node...");
            }

        } else {
            response.setError("There's no such method");
        }

        return response;
    }

    @Override
    protected void logSuccessfulOperation(Request rqst, Response rspns, Connection cnctn, BasicSession bs) {
    }

    @Override
    protected void logErrorResponseOperation(Request rqst, Response rspns, Connection cnctn, BasicSession bs) {
    }

    @Override
    protected void logErrorExceptionOperation(Request rqst, Response rspns, Throwable thrwbl, Connection cnctn) {
    }

    @Override
    protected void noSession(Request rqst) {
    }

    @Override
    protected boolean checkPermissions(ArrayList<?> al, Request rqst) {
        return true;
    }

    @Override
    protected boolean defineCheckSessionFlag() {
        return false;
    }

    @Override
    protected boolean defineCheckPermissionsFlag() {
        return false;
    }

    @Override
    protected boolean defineLoggableFlag() {
        return false;
    }

    @Override
    protected boolean defineLoggableErrorsFlag() {
        return false;
    }

    @Override
    protected boolean defineDBConnectionNeededFlag() {
        return false;
    }

    @Override
    protected boolean defineUtf8CharacterEncodingRequest() {
        return false;
    }

    @Override
    protected void initServlet() {
    }
}
