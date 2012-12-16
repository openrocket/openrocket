package net.sf.openrocket.util;

public class QuaternionMultiply {

	private static class Value {
		public int sign = 1;
		public String value;
		
		@SuppressWarnings("unused")
		public Value multiply(Value other) {
			Value result = new Value();
			result.sign = this.sign * other.sign;
			if (this.value.compareTo(other.value) < 0)
				result.value = this.value + "*" + other.value;
			else
				result.value = other.value + "*" + this.value;
			return result;
		}
		
		@Override
		public String toString() {
			String s;
			
			if (sign < 0)
				s = "-";
			else
				s = "+";
			
			if (sign == 0)
				s += " 0";
			else
				s += " " + value;
			
			return s;
		}
	}
	
	private static Value[] multiply(Value[] first, Value[] second) {
		return null;
	}
	
	public static void main(String[] arg) {
		if (arg.length % 4 != 0  || arg.length < 4) {
			System.out.println("Must have modulo 4 args, at least 4");
			return;
		}
		
		Value[][] values = new Value[arg.length/4][4];
		
		for (int i=0; i<arg.length; i++) {
			Value value = new Value();
			
			if (arg[i].equals("")) {
				value.sign = 0;
			} else {
				if (arg[i].startsWith("-")) {
					value.sign = -1;
					value.value = arg[i].substring(1);
				} else if (arg[i].startsWith("+")) {
					value.sign = 1;
					value.value = arg[i].substring(1);
				} else {
					value.sign = 1;
					value.value = arg[i];
				}
			}
			
			values[i/4][i%4] = value;
		}

		System.out.println("Multiplying:");
		for (int i=0; i < values.length; i++) {
			print(values[i]);
		}
		System.out.println("Result:");
		
		Value[] result = values[0];
		for (int i=1; i < values.length; i++) {
			result = multiply(result, values[i]);
		}
		print(result);
	}
	
	private static void print(Value[] q) {
		System.out.println("   " + q[0] + " " + q[1] + " i " + q[2] + " j " + q[3] + " k");
	}
	
}

