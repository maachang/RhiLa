package rhila;

import java.lang.reflect.Method;

import org.mozilla.javascript.Context;

/**
 * RhiLa用例外.
 */
public class RhilaException extends RuntimeException {
	private static final long serialVersionUID = 685708741908367818L;
	protected int status;

	public RhilaException(int status) {
		super(rhinoJsErrorTraceMessage("rhila exception."));
		this.status = status;
	}

	public RhilaException(int status, String message) {
		super(rhinoJsErrorTraceMessage(message));
		this.status = status;
	}

	public RhilaException(int status, Throwable e) {
		super(rhinoJsErrorTraceMessage(e.getMessage()), e);
		this.status = status;
	}

	public RhilaException(int status, String message, Throwable e) {
		super(rhinoJsErrorTraceMessage(message), e);
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
	
	// rhinoエラートレース情報を付与.
	private static final String rhinoJsErrorTraceMessage(String message) {
		// Context.getSourcePositionFromStack(int[])をreflect呼び出し.
		try {
			Class<?> c = Context.class;
			Method m = c.getDeclaredMethod(
				"getSourcePositionFromStack", int[].class);
			m.setAccessible(true);
			int[] out = new int[] { 0 };
			Object fileName = m.invoke(null, out);
			if(fileName == null) {
				// ファイル名が取得出来ない場合は取得しない.
				return message;
			}
			return new StringBuilder(message)
				.append("(")
				.append(fileName)
				.append("#")
				.append(out[0])
				.append(")")
				.toString();
		} catch(Throwable t) {
		}
		// エラーで取得出来ない場合は所得しない.
		return message;
	}
}
