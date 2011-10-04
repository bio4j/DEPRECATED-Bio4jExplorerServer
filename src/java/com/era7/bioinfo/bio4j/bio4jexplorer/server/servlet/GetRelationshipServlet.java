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
