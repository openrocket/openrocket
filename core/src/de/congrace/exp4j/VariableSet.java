package de.congrace.exp4j;

import java.util.HashSet;

public class VariableSet extends HashSet<Variable> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4212803364398351279L;

	public boolean add(Variable v){
		Variable previous = getVariableNamed(v.getName());
		if ( previous != null ){
			this.remove( previous );
		}
			
		return super.add(v);
	}
	
	public Variable getVariableNamed(String name){
		for (Variable var : this){
			if (var.getName().equals(name) ){
				return var;
			}
		}
		return null;
	}
	
	public String[] getVariableNames(){
		if (this.size() == 0){
			return null;
		}
		String names[] = new String[this.size()];
		int i = 0;
		for (Variable var : this){
			names[i] = var.getName();
			i++;
		}
		return names;
	}	
}
