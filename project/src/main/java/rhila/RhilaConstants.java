package rhila;

/**
 * Rhila定義.
 */
public class RhilaConstants {
	private RhilaConstants() {}
	
    // lambda snapStart CRaC用.
	protected static final RhilaConstants LOAD_CRAC = new RhilaConstants();
	
	// version.
	public static final String VERSION = "0.0.1";
	
	// Server名.
	public static final String NAME = "Rhila";
	
	
	// Server名.
	public static final String SERVER_NAME = NAME + "(" + VERSION + ")";
	
	// UserAgent.
	public static final String USER_AGENT = NAME + "(" + VERSION + ")";
}
