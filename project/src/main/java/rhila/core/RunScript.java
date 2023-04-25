package rhila.core;

import org.mozilla.javascript.WrappedException;

import rhila.RhilaException;

/**
 * js実行.
 */
public class RunScript {
	
	// static専用.
	private RunScript() {}
	
	// スクリプト実行.
	public static final Object eval(Global global, String script) {
		return eval(global, script, "eval", 1);
	}
	
	// スクリプト実行.
	public static final Object eval(
		Global global, String script, String scriptName) {
		return eval(global, script, scriptName, 1);
	}
	
	// スクリプト実行.
	public static final Object eval(
		Global global, String script, String scriptName, int lineNo) {
		try {
			return global.getContext().evaluateString(
				global, script, scriptName, lineNo, null);
		} catch(WrappedException we) {
			Throwable t = we.getWrappedException();
			if(t instanceof RhilaException) {
				throw (RhilaException)t;
			}
			throw new RhilaException(t);
		} catch(RhilaException re) {
			throw re;
		} catch(Throwable t) {
			throw new RhilaException(t);
		}
	}
	
	// ライブラリヘッダ.
	private static final String LIB_HEADER = "return (function() {\nexports = {};\n";
	
	// ライブラリフッタ.
	private static final String LIB_FOODER = "\nreturn exports;\n})();";
	
	// ライブラリ開始行.
	private static final int LIB_START_LINE = -2;
	
	// ライブラリ読み込み用.
	// こんな感じで実装する.
	//
	// return (function() {
	// exports = {};
	// .... 実行プログラム.
	// return exports;
	// })();
	//
	public static final Object loadLibrary(Global global, String script, String scriptName) {
		try {
			return global.getContext().evaluateString(global,
				new StringBuilder(LIB_HEADER)
					.append(script)
					.append(LIB_FOODER)
					.toString(),
				scriptName, LIB_START_LINE, null);
		} catch(WrappedException we) {
			Throwable t = we.getWrappedException();
			if(t instanceof RhilaException) {
				throw (RhilaException)t;
			}
			throw new RhilaException(t);
		} catch(RhilaException re) {
			throw re;
		} catch(Throwable t) {
			throw new RhilaException(t);
		}
	}
	
}
