package rhila.scriptable;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhila.RhilaException;
import rhila.WrapUtil;

/**
 * 基本RhinoFunction定義.
 */
public interface RhinoFunction extends Function {

	// Funtion呼び出し処理.
	// 処理の実装は function を継承して実装してください.
	@Override
	default Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
		try {
			return WrapUtil.wrap(function(ctx, scope, thisObj, WrapUtil.unwrapArgs(args)));
		} catch(RhilaException rwe) {
			throw rwe;
		} catch(Throwable t) {
			throw new RhilaException(t);
		}
	}
	
	// new コンストラクタ呼び出し処理.
	// 処理の実装は newInstance を継承して実装してください.
	@Override
	default Scriptable construct(Context arg0, Scriptable arg1, Object[] arg2) {
		try {
			return newInstance(arg0, arg1, WrapUtil.unwrapArgs(arg2));
		} catch(RhilaException rwe) {
			throw rwe;
		} catch(Throwable t) {
			throw new RhilaException(t);
		}
	}
	
	/**
	 * function名を設定します.
	 */
	public String getName();
		
	/**
	 * Function実装.
	 */
	default Object function(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
		// 実装しない場合は空返却.
		return Undefined.instance;
	}
	
	/**
	 * new (オブジェクト名) の実装用.
	 */
	default Scriptable newInstance(Context arg0, Scriptable arg1, Object[] arg2) {
		throw new RhilaException("This method '" + getName() +
			"' does not support instantiation.");
	}
	
	/**
	 * 引数エラーを返却.
	 */
	default Object argsException() {
		return argsException(null);
	}

	/**
	 * 引数エラーを返却.
	 */
	default Object argsException(String objName) {
		if (objName == null) {
			throw new RhilaException("Insufficient arguments for " + getName() + ".");
		}
		throw new RhilaException("Insufficient arguments for " + objName + "." + getName() + ".");
	}

}
