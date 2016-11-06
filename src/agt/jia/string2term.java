// Internal action code for project swot_agents

package jia;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.StringTerm;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;

public class string2term extends DefaultInternalAction {

    private static final long serialVersionUID = -9087866350378537029L;
    
    
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
        String input = ((StringTerm) args[0]).getString();
        return un.unifies(Structure.parse(input), args[1]);
    }
}
