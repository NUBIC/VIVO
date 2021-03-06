/* $This file is distributed under the terms of the license in /doc/license.txt$ */
package edu.cornell.mannlib.vitro.webapp.visualization.modelconstructor.factory;

import com.hp.hpl.jena.rdf.model.Model;

import edu.cornell.mannlib.vitro.webapp.rdfservice.RDFService;
import edu.cornell.mannlib.vitro.webapp.visualization.exceptions.MalformedQueryParametersException;
import edu.cornell.mannlib.vitro.webapp.visualization.valueobjects.ConstructedModelTracker;
import edu.cornell.mannlib.vitro.webapp.visualization.visutils.ModelConstructor;

/**
 * No longer used - will be removed
 */
@Deprecated
@SuppressWarnings("deprecation")
public class PeopleToPublicationsFactory implements ModelFactoryInterface {
	@Override
	public Model getOrCreateModel(String uri, RDFService rdfService) throws MalformedQueryParametersException {
		Model candidateModel = ConstructedModelTracker.getModel(
				ConstructedModelTracker
				.generateModelIdentifier(
						null,
						edu.cornell.mannlib.vitro.webapp.visualization.modelconstructor.PeopleToPublicationsModelConstructor.MODEL_TYPE));
		
		if (candidateModel != null) {
			
			return candidateModel;
			
		} else {
		
			ModelConstructor model = new edu.cornell.mannlib.vitro.webapp.visualization.modelconstructor.PeopleToPublicationsModelConstructor(rdfService);
			
			Model constructedModel = model.getConstructedModel();
			ConstructedModelTracker.trackModel(
					ConstructedModelTracker
						.generateModelIdentifier(
								null,
								edu.cornell.mannlib.vitro.webapp.visualization.modelconstructor.PeopleToPublicationsModelConstructor.MODEL_TYPE),
								constructedModel);
			
			return constructedModel;
		}
		
	}

}
