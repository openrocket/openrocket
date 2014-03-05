package net.sf.openrocket.file.csv;

class CSVException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	
	public CSVException() {
	}
	
	public CSVException(Throwable throwable) {
		super(throwable);
	}
	
	public CSVException(String message) {
		super(message);
	}
	
	public CSVException(String message, Throwable throwable) {
		super(message, throwable);
	}
	
}