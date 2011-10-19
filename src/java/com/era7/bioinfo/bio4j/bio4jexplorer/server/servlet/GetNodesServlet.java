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
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.amazonaws.services.simpledb.model.SelectResult;
import com.era7.bioinfo.bio4j.bio4jexplorer.server.RequestList;
import com.era7.bioinfo.bioinfoaws.util.CredentialsRetriever;
import com.era7.lib.bioinfoxml.bio4j.Bio4jNodeXML;
import com.era7.lib.communication.model.BasicSession;
import com.era7.lib.communication.xml.Request;
import com.era7.lib.communication.xml.Response;
import com.era7.lib.servletlibrary.servlet.BasicServlet;
import java.sql.Connection;
import java.util.ArrayList;
import javax.servlet.http.HttpServletRequest;

/**
 * Returns a list including all node types present in Bio4j
 * @author Pablo Pareja Tobes <ppareja@era7.com>
 */
public class GetNodesServlet extends BasicServlet {

    @Override
    protected Response processRequest(Request request, BasicSession bs,
            Connection cnctn, HttpServletRequest hsr) throws Throwable {

        Response response = new Response();

        if (request.getMethod().equals(RequestList.GET_NODES_REQUEST)) {

            AmazonSimpleDBClient simpleDBClient = new AmazonSimpleDBAsyncClient(CredentialsRetriever.getBasicAWSCredentialsFromOurAMI());

            SelectRequest selectRequest = new SelectRequest();
            selectRequest.setSelectExpression("SELECT NAME from bio4j where ITEM_TYPE = 'node'");

            SelectResult selectResult = simpleDBClient.select(selectRequest);
            boolean nextToken = true;

            while (nextToken) {

                for (Item item : selectResult.getItems()) {
                    Bio4jNodeXML node = new Bio4jNodeXML();
                    node.setNodeName(item.getName());
                    response.addChild(node);
                }

                if (selectResult.getNextToken() != null) {
                    selectRequest.setNextToken(selectResult.getNextToken());
                    selectResult = simpleDBClient.select(selectRequest);
                } else {
                    nextToken = false;
                }

            }

            response.setStatus(Response.SUCCESSFUL_RESPONSE);

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
