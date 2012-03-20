package net.sf.openrocket.plugin.example;


public class ExamplePlugin implements ExamplePluginInterface {
	
	private final String str;
	
	public ExamplePlugin(String str) {
		this.str = str;
	}
	
	@Override
	public void print() {
		System.out.println("ExamplePlugin: " + str);
	}
	
}
