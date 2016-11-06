// Internal action code for project swot_agents

package jia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Term;


public class currentTimeMillis extends DefaultInternalAction {

    private static final long serialVersionUID = 7821315711129166795L;
    
    
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        return un.unifies(new NumberTermImpl(System.currentTimeMillis()), args[0]);
    }
}
