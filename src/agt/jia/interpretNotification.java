// Internal action code for project swot_agents

package jia;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

import fr.emse.ci.stn.aacontainer.artifacts.STNClient;
import fr.emse.ci.stn.aacontainer.utils.STNCore;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.LiteralImpl;
import jason.asSyntax.StringTerm;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;


public class interpretNotification extends DefaultInternalAction {
    
    private static final long serialVersionUID = -1060153765703097743L;
    
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        String message = ((StringTerm) args[0]).getString();
        InputStream stream = new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8));
        
        Model model = ModelFactory.createDefaultModel().read(stream, null, STNClient.TURTLE);
        if (model.contains(null, RDF.type, STNCore.Relation)) {
            Resource source = (Resource) model.getRequiredProperty(null, STNCore.source).getObject();
            Resource target = (Resource) model.getRequiredProperty(null, STNCore.target).getObject();
            
            if (source.isURIResource() && target.isURIResource()) {
                ResIterator iterator = model.listResourcesWithProperty(RDF.type, STNCore.Relation);
                
                if (iterator.hasNext()) {
                    Resource relation = iterator.next();
                    if (relation.isURIResource()) {
                        Structure belief = new Structure("connectedTo");
                        belief.addTerm(new StringTermImpl(relation.getURI()));
                        belief.addTerm(new StringTermImpl(source.getURI()));
                        belief.addTerm(new StringTermImpl(target.getURI()));
                        
                        ts.getAg().getBB().add(new LiteralImpl(belief));
                    }
                }
            }
        }
        
        return true;
    }
}
