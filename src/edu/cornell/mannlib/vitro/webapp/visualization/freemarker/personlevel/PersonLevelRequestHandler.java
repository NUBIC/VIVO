/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.visualization.freemarker.personlevel;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.beans.Portal;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.ResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.freemarker.responsevalues.TemplateResponseValues;
import edu.cornell.mannlib.vitro.webapp.controller.visualization.freemarker.VisualizationFrameworkConstants;
import edu.cornell.mannlib.vitro.webapp.visualization.exceptions.MalformedQueryParametersException;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.coauthorship.CoAuthorshipQueryRunner;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.coauthorship.CoAuthorshipVisCodeGenerator;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.collaborationutils.CollaborationData;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.coprincipalinvestigator.CoPIGrantCountConstructQueryRunner;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.coprincipalinvestigator.CoPIGrantCountQueryRunner;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.coprincipalinvestigator.CoPIVisCodeGenerator;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.persongrantcount.PersonGrantCountQueryRunner;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.persongrantcount.PersonGrantCountVisCodeGenerator;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.personpubcount.PersonPublicationCountQueryRunner;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.personpubcount.PersonPublicationCountVisCodeGenerator;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.valueobjects.Activity;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.valueobjects.SparklineData;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.visutils.ModelConstructor;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.visutils.QueryRunner;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.visutils.UtilityFunctions;
import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.visutils.VisualizationRequestHandler;

/**
 * This request handler is used to serve content rendered on the person level vis page
 * like,
 * 		1. Front end of the vis including the co-author & publication sparkline.
 * 		2. Downloadable file having the co-author network in graphml format.
 * 		3. Downloadable file having the list of co-authors that the individual has
 * worked with & count of such co-authorships.
 * 
 * @author cdtank
 */
public class PersonLevelRequestHandler implements VisualizationRequestHandler {

    private static final String EGO_PUB_SPARKLINE_VIS_CONTAINER_ID = "ego_pub_sparkline";
    private static final String UNIQUE_COAUTHORS_SPARKLINE_VIS_CONTAINER_ID = 
    									"unique_coauthors_sparkline";
    private static final String EGO_GRANT_SPARKLINE_VIS_CONTAINER_ID = "ego_grant_sparkline";
    private static final String UNIQUE_COPIS_SPARKLINE_VIS_CONTAINER_ID = 
    									"unique_copis_sparkline";

	@Override
	public Object generateAjaxVisualization(VitroRequest vitroRequest, Log log,
			Dataset dataset) throws MalformedQueryParametersException {
		throw new UnsupportedOperationException("Person Level does not provide Ajax Response.");
	}

	@Override
	public Map<String, String> generateDataVisualization(
			VitroRequest vitroRequest, Log log, Dataset dataset)
			throws MalformedQueryParametersException {
		throw new UnsupportedOperationException("Person Level does not provide Data Response.");
	}
    
	
	@Override
	public ResponseValues generateStandardVisualization(
			VitroRequest vitroRequest, Log log, Dataset dataset)
			throws MalformedQueryParametersException {

        String egoURI = vitroRequest.getParameter(
        							VisualizationFrameworkConstants.INDIVIDUAL_URI_KEY);

        String visMode = vitroRequest.getParameter(
        							VisualizationFrameworkConstants.VIS_MODE_KEY);
        
        
        if (VisualizationFrameworkConstants.COPI_VIS_MODE.equalsIgnoreCase(visMode)) { 
        	
        	ModelConstructor constructQueryRunner = 
        			new CoPIGrantCountConstructQueryRunner(egoURI, dataset, log);
    		Model constructedModel = constructQueryRunner.getConstructedModel();
    		
    		QueryRunner<CollaborationData> coPIQueryManager = 
    				new CoPIGrantCountQueryRunner(egoURI, constructedModel, log);
           
            QueryRunner<Set<Activity>> grantQueryManager = 
            		new PersonGrantCountQueryRunner(egoURI, dataset, log);
            
            CollaborationData coPIData = coPIQueryManager.getQueryResult();
            
	    	/*
	    	 * grants over time sparkline
	    	 */
			
			Set<Activity> piGrants = grantQueryManager.getQueryResult();
			
	    	/*
	    	 * Create a map from the year to number of grants. Use the Grant's
	    	 * parsedGrantYear to populate the data.
	    	 * */
	    	Map<String, Integer> yearToGrantCount = 
	    			UtilityFunctions.getYearToActivityCount(piGrants);	    	
	    	
	    	PersonGrantCountVisCodeGenerator personGrantCountVisCodeGenerator = 
	    		new PersonGrantCountVisCodeGenerator(
	    			egoURI,
	    			VisualizationFrameworkConstants.FULL_SPARKLINE_VIS_MODE,
	    			EGO_GRANT_SPARKLINE_VIS_CONTAINER_ID,
	    			yearToGrantCount,
	    			log);
	    	
	    	SparklineData grantSparklineVO = personGrantCountVisCodeGenerator
			.getValueObjectContainer();
	    	
	    	
	    	/*
	    	 * Co-PI's over time sparkline
	    	 */
	    	CoPIVisCodeGenerator uniqueCopisVisCodeGenerator = 
	    		new CoPIVisCodeGenerator(
	    			egoURI,
	    			VisualizationFrameworkConstants.FULL_SPARKLINE_VIS_MODE,
	    			UNIQUE_COPIS_SPARKLINE_VIS_CONTAINER_ID,
	    			UtilityFunctions.getActivityYearToCollaborators(coPIData),
	    			log);
	    	
	    	SparklineData uniqueCopisSparklineVO = uniqueCopisVisCodeGenerator
			.getValueObjectContainer();
	    	
	    	
	    	return prepareCoPIStandaloneResponse(
					egoURI, 
					grantSparklineVO,
					uniqueCopisSparklineVO,
					coPIData,
	    			vitroRequest);
	    	
        	
        } else {
        	
        	QueryRunner<CollaborationData> coAuthorshipQueryManager = 
        			new CoAuthorshipQueryRunner(egoURI, dataset, log);
        
        	QueryRunner<Set<Activity>> publicationQueryManager = 
        			new PersonPublicationCountQueryRunner(egoURI, dataset, log);
        	
        	CollaborationData coAuthorshipData = coAuthorshipQueryManager.getQueryResult();
        	
        	/*
			 * When the front-end for the person level vis has to be displayed we render couple of 
			 * sparklines. This will prepare all the data for the sparklines & other requested 
			 * files.
			 * */
			Set<Activity> authorDocuments = publicationQueryManager.getQueryResult();
			
	    	/*
	    	 * Create a map from the year to number of publications. Use the BiboDocument's
	    	 * parsedPublicationYear to populate the data.
	    	 * */
	    	Map<String, Integer> yearToPublicationCount = 
	    			UtilityFunctions.getYearToActivityCount(authorDocuments);
	    														
	    	/*
	    	 * Computations required to generate HTML for the sparklines & related context.
	    	 * */
	    	PersonPublicationCountVisCodeGenerator personPubCountVisCodeGenerator = 
	    		new PersonPublicationCountVisCodeGenerator(
	    			egoURI,
	    			VisualizationFrameworkConstants.FULL_SPARKLINE_VIS_MODE,
	    			EGO_PUB_SPARKLINE_VIS_CONTAINER_ID,
	    			yearToPublicationCount,
	    			log);	  
	    	
	    	SparklineData publicationSparklineVO = personPubCountVisCodeGenerator
	    														.getValueObjectContainer();
	    	
            CoAuthorshipVisCodeGenerator uniqueCoauthorsVisCodeGenerator = 
	    		new CoAuthorshipVisCodeGenerator(
	    			egoURI,
	    			VisualizationFrameworkConstants.FULL_SPARKLINE_VIS_MODE,
	    			UNIQUE_COAUTHORS_SPARKLINE_VIS_CONTAINER_ID,
	    			UtilityFunctions.getActivityYearToCollaborators(coAuthorshipData),
	    			log);
	    	
	    	SparklineData uniqueCoauthorsSparklineVO = uniqueCoauthorsVisCodeGenerator
	    															.getValueObjectContainer();
	    	
	    	return prepareCoAuthorStandaloneResponse(
					egoURI, 
	    			publicationSparklineVO,
	    			uniqueCoauthorsSparklineVO,
	    			coAuthorshipData,
	    			vitroRequest);

        }
        
	}
	
	private TemplateResponseValues prepareCoAuthorStandaloneResponse(
					String egoURI, 
					SparklineData egoPubSparklineVO, 
					SparklineData uniqueCoauthorsSparklineVO, 
					CollaborationData coAuthorshipVO, 
					VitroRequest vitroRequest) {
		
		Map<String, Object> body = new HashMap<String, Object>();
		
        Portal portal = vitroRequest.getPortal();
        String	standaloneTemplate = "coAuthorPersonLevel.ftl";
        
        body.put("egoURIParam", egoURI);
        
        String title = "";
        
        if (coAuthorshipVO.getCollaborators() != null 
        			&& coAuthorshipVO.getCollaborators().size() > 0) {
        	body.put("numOfAuthors", coAuthorshipVO.getCollaborators().size());
        	title = coAuthorshipVO.getEgoCollaborator().getCollaboratorName() + " - ";
		}
		
		if (coAuthorshipVO.getCollaborations() != null 
					&& coAuthorshipVO.getCollaborations().size() > 0) {
			body.put("numOfCoAuthorShips", coAuthorshipVO.getCollaborations().size());
		}
		
		body.put("egoPubSparklineVO", egoPubSparklineVO);
		body.put("uniqueCoauthorsSparklineVO", uniqueCoauthorsSparklineVO);

		body.put("portalBean", portal);
		body.put("title",  title + "Person Level Visualization");

		return new TemplateResponseValues(standaloneTemplate, body);
		
	}
	
	private TemplateResponseValues prepareCoPIStandaloneResponse(
					String egoURI, 
					SparklineData egoGrantSparklineVO, 
					SparklineData uniqueCopisSparklineVO, 
					CollaborationData coPIVO, 
					VitroRequest vitroRequest) {
		
		Map<String, Object> body = new HashMap<String, Object>();

		Portal portal = vitroRequest.getPortal();
        
        body.put("egoURIParam", egoURI);
        
        String title = "";
        
        if (coPIVO.getCollaborators() != null && coPIVO.getCollaborators().size() > 0) {
        	body.put("numOfInvestigators", coPIVO.getCollaborators().size());
        	title = coPIVO.getEgoCollaborator().getCollaboratorName() + " - ";
		}
		
		if (coPIVO.getCollaborations() != null && coPIVO.getCollaborations().size() > 0) {
			body.put("numOfCoInvestigations", coPIVO.getCollaborations().size());
		}
		
        String	standaloneTemplate = "coPIPersonLevel.ftl";
		
		body.put("egoGrantSparklineVO", egoGrantSparklineVO);
		body.put("uniqueCoInvestigatorsSparklineVO", uniqueCopisSparklineVO);        	

		body.put("portalBean", portal);
		body.put("title",  title + "Person Level Visualization");

		return new TemplateResponseValues(standaloneTemplate, body);
		
	}
	
}