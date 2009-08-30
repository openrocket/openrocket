import net.sf.openrocket.util.Coordinate;


public class Test {

	public static int COUNT = 10000000;

	public static void main(String[] args) {

		for (int i=1; ; i++) {
			long t1 = System.currentTimeMillis();
			run();
			long t2 = System.currentTimeMillis();
			System.out.println("Run " + i + " took " + (t2-t1) + " ms");
		}
		
	}
	
	
	private static void run() {
		Coordinate a = new Coordinate(1,1,1,1);
		Coordinate b = new Coordinate(1,1,1,1);
		
		for (int i=0; i < COUNT; i++) {
			a = a.add(b);
		}
		System.out.println("value:"+a);
		
		return;
	}

}
