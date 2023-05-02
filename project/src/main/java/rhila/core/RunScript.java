package rhila.core;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.WrappedException;

import rhila.RhilaException;

/**
 * jsを実行する.
 */
public final class RunScript {
	
	// static専用.
	protected RunScript() {}
	
	// スクリプト実行.
	public static final Object eval(String script) {
		return eval(null, script, "eval", 1);
	}
	
	// スクリプト実行.
	public static final Object eval(String script, String scriptName) {
		return eval(null, script, scriptName, 1);
	}
	
	// スクリプト実行.
	public static final Object eval(String script, String scriptName, int lineNo) {
		return eval(null, script, scriptName, lineNo);
	}
	
	// スクリプト実行.
	public static final Object eval(Scriptable scope, String script) {
		return eval(scope, script, "eval", 1);
	}
	
	// スクリプト実行.
	public static final Object eval(
			Scriptable scope, String script, String scriptName) {
		return eval(scope, script, scriptName, 1);
	}
	
	// スクリプト実行.
	public static final Object eval(
		Scriptable scope, String script, String scriptName, int lineNo) {
		if(scope == null || scope instanceof Undefined) {
			scope = Global.getInstance();
		}
		try {
			return Context.getCurrentContext().evaluateString(
				scope, script, scriptName, lineNo, null);
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
	public static final Object loadLibrary(Scriptable scope, String script, String scriptName) {
		return eval(
			scope
			,new StringBuilder(LIB_HEADER)
				.append(script)
				.append(LIB_FOODER)
				.toString() 
			,scriptName, LIB_START_LINE);
	}
	
}
