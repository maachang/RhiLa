package rhila.core;

/**
 * RhiLa用例外.
 */
public class RhilaException extends RuntimeException {
	private static final long serialVersionUID = 685708741908367818L;
	protected int status;
	protected String msg;

	public RhilaException(int status) {
		super();
		this.status = status;
	}

	public RhilaException(int status, String message) {
		super(message);
		this.status = status;
	}

	public RhilaException(int status, Throwable e) {
		super(e);
		this.status = status;
	}

	public RhilaException(int status, String message, Throwable e) {
		super(message, e);
		this.status = status;
	}

	public RhilaException() {
		this(500);
	}

	public RhilaException(String m) {
		this(500, m);
	}

	public RhilaException(Throwable e) {
		this(500, e);
	}

	public RhilaException(String m, Throwable e) {
		this(500, m, e);
	}

	public int getStatus() {
		return status;
	}
	
	public void setMessage(String msg) {
		this.msg = msg;
	}
	
	public String getMessage() {
		return msg == null ? super.getMessage() : msg;
	}
	
	public String getLocalizedMessage() {
		return msg == null ? super.getLocalizedMessage() : msg;
	}
}
