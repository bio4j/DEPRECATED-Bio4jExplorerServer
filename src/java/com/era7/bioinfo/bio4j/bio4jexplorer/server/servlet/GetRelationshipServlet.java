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
import com.era7.lib.bioinfoxml.bio4j.Bio4jNodeXML;
import com.era7.lib.bioinfoxml.bio4j.Bio4jRelationshipIndexXML;
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
 * Gets information about a specific relationship
 * @author Pablo Pareja Tobes <ppareja@era7.com>
 */
public class GetRelationshipServlet extends BasicServlet {

    @Override
    protected Response processRequest(Request request, BasicSession bs,
            Connection cnctn, HttpServletRequest hsr) throws Throwable {

        Response response = new Response();

        if (request.getMethod().equals(RequestList.GET_RELATIONSHIP_REQUEST)) {

            String relName = request.getParameters().getChildText("name");

            AmazonSimpleDBClient simpleDBClient = new AmazonSimpleDBAsyncClient(CredentialsRetriever.getBasicAWSCredentialsFromOurAMI());

            SelectRequest selectRequest = new SelectRequest();
            String selectExpression = "SELECT * from bio4j where NAME = '" + relName + "' AND ITEM_TYPE = 'relationship'";
            System.out.println("selectExpression = " + selectExpression);
            selectRequest.setSelectExpression(selectExpression);

            SelectResult selectResult = simpleDBClient.select(selectRequest);

            if (selectResult.getItems().size() > 0) {
                
                Item item = selectResult.getItems().get(0);
                Bio4jRelationshipXML rel = new Bio4jRelationshipXML();
                rel.setRelationshipName(item.getName());
                List<Attribute> atts = item.getAttributes();
                for (Attribute attribute : atts) {
                    if (attribute.getName().equals(CommonData.DESCRIPTION_ATTRIBUTE)) {
                        rel.setDescription(attribute.getValue());
                    } else if (attribute.getName().equals(CommonData.ITEM_TYPE_ATTRIBUTE)) {
                        rel.setItemType(Bio4jRelationshipXML.RELATIONSHIP_ITEM_TYPE);
                    } else if (attribute.getName().equals(CommonData.START_NODES_ATTRIBUTE)) {

                        if (!attribute.getValue().equals("-")) {
                            Bio4jNodeXML node = new Bio4jNodeXML();
                            node.setNodeName(attribute.getValue());
                            rel.addStartNode(node);
                        }

                    } else if (attribute.getName().equals(CommonData.END_NODES_ATTRIBUTE)) {

                        if (!attribute.getValue().equals("-")) {
                            Bio4jNodeXML node = new Bio4jNodeXML();
                            node.setNodeName(attribute.getValue());
                            rel.addEndNode(node);
                        }
                        
                    }else if(attribute.getName().equals(CommonData.JAVADOC_URL_ATTRIBUTE)){
                        
                        rel.setJavadocUrl(attribute.getValue());
                        
                    }
                }
                
                
                //-------------GETTING NODE INDEXES--------------

                String indexesSelectExpression = "SELECT * from bio4j where ITEM_TYPE = 'relationship_index' AND RELATIONSHIP_NAME = '" + relName + "'";
                selectRequest.setSelectExpression(indexesSelectExpression);
                selectResult = simpleDBClient.select(selectRequest);

                if (selectResult.getItems().size() > 0) {

                    for (Item indexItem : selectResult.getItems()) {
                        Bio4jRelationshipIndexXML relIndex = new Bio4jRelationshipIndexXML();
                        relIndex.setIndexName(indexItem.getName());

                        atts = indexItem.getAttributes();

                        for (Attribute attribute : atts) {

                            if (attribute.getName().equals(CommonData.RELATIONSHIP_NAME_ATTRIBUTE)) {
                                relIndex.setRelationshipName(attribute.getValue());
                            }
                        }

                        rel.addIndex(relIndex);
                    }

                }                
                
                response.addChild(rel);
                
                response.setStatus(Response.SUCCESSFUL_RESPONSE);
                
            } else {
                response.setError("There is no such relationship...");
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
