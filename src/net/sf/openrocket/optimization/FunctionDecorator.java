package net.sf.openrocket.optimization;

public class FunctionDecorator implements Function {
	
	private final Function function;
	
	public FunctionDecorator(Function function) {
		this.function = function;
	}
	
	
	@Override
	public double evaluate(Point x) throws InterruptedException {
		return function.evaluate(x);
	}
	
	@Override
	public double preComputed(Point x) {
		return function.preComputed(x);
	}
	
}
