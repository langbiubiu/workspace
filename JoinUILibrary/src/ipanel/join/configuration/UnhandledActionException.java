package ipanel.join.configuration;

public class UnhandledActionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7706523812677486571L;
	
	private String action;
	
	public UnhandledActionException(String action){
		super(action+" not handled");
		this.action = action;
	}
	
	public String getActionType(){
		return this.action;
	}
}
