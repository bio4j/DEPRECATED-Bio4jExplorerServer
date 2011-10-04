/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
 *
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
            System.out.println("selectExpression = " + selectExpression);
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
