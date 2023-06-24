package rhila.core;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.WrappedException;

import rhila.RhilaException;
import rhila.lib.ObjectUtil;
import rhila.scriptable.MapScriptable;

/**
 * jsを実行する.
 */
public final class RunScript {
    // lambda snapStart CRaC用.
    protected static final RunScript LOAD_CRAC = new RunScript();
	
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
		if(ObjectUtil.isNull(scope)) {
			scope = GlobalFactory.getGlobal();
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
	private static final String LIB_HEADER = "(function() { exports = {}; \n";
	
	// ライブラリフッタ.
	private static final String LIB_FOODER = "\nreturn exports; })();";
	
	// ライブラリ開始行.
	private static final int LIB_START_LINE = 0;
	
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
	
	public static final Object loadLibrary(String script, String scriptName) {
		return loadLibrary(null, script, scriptName);
	}
	
	public static final Object loadLibrary(String script) {
		return loadLibrary(null, script, "script");
	}
	
	// exports.handlerを実行.
	public static final Object callHandler(
		Scriptable scope, String script, String scriptName, Object... args) {
		if(scope == null) {
			scope = GlobalFactory.getGlobal();
		}
		Context ctx;
		if(scope instanceof Global) {
			ctx = ((Global)scope).getContext();
		} else {
			ctx = GlobalFactory.getGlobal().getContext();
		}
		MapScriptable exports = (MapScriptable)loadLibrary(
			scope, script, scriptName);
		Function func = (Function)exports.get("handler");
		return func.call(ctx, scope, exports, args);
	}
	
	// exports.handlerを実行.
	public static final Object callHandler(
		String script, String scriptName, Object... args) {
		return callHandler(null, script, scriptName, args);
	}
	
	// exports.handlerを実行.
	public static final Object callHandler(
		String script, Object... args) {
		return callHandler(null, script, "script", args);
	}
}
