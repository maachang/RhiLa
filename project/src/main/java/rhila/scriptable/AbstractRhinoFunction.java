package rhila.scriptable;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhila.RhilaException;
import rhila.WrapUtil;

/**
 * RhinoFunction雛形.
 * 
 * 以下2つの実装を行う事で、rhinoのjs用function呼び出しを実施します.
 */
public abstract class AbstractRhinoFunction implements Function {
	public static final Object[] BLANK_ARGS = new Object[0];
	
	@Override
	public void delete(String arg0) {
	}

	@Override
	public void delete(int arg0) {
	}

	@Override
	public Object get(String arg0, Scriptable arg1) {
		return null;
	}

	@Override
	public Object get(int arg0, Scriptable arg1) {
		return null;
	}

	@Override
	public String getClassName() {
		return getName();
	}

	@Override
	public Object getDefaultValue(Class<?> clazz) {
		return (clazz == null || String.class.equals(clazz)) ? toString() : Undefined.instance;
	}

	@Override
	public Object[] getIds() {
		return BLANK_ARGS;
	}

	@Override
	public Scriptable getParentScope() {
		return null;
	}

	@Override
	public Scriptable getPrototype() {
		return null;
	}

	@Override
	public boolean has(String arg0, Scriptable arg1) {
		return false;
	}

	@Override
	public boolean has(int arg0, Scriptable arg1) {
		return false;
	}

	@Override
	public boolean hasInstance(Scriptable instance) {
		if(instance != null) {
			return this.getClassName().equals(instance.getClassName());
		}
		return false;
	}

	@Override
	public void put(String arg0, Scriptable arg1, Object arg2) {
	}

	@Override
	public void put(int arg0, Scriptable arg1, Object arg2) {
	}

	@Override
	public void setParentScope(Scriptable arg0) {
	}

	@Override
	public void setPrototype(Scriptable arg0) {
	}
	
	// Object[] を unwrap.
	private static final Object[] unwrapArray(Object[] args) {
		int len = args.length;
		for(int i = 0; i < len; i ++) {
			args[i] = WrapUtil.unwrap(args[i]);
		}
		return args;
	}

	// Funtion呼び出し処理.
	// 処理の実装は jcall を継承して実装してください.
	@Override
	public final Object call(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
		try {
			return WrapUtil.wrap(function(ctx, scope, thisObj, unwrapArray(args)));
		} catch(RhilaException rwe) {
			throw rwe;
		} catch(Throwable t) {
			throw new RhilaException(t);
		}
	}
	
	// new コンストラクタ呼び出し処理.
	// 処理の実装は jconstruct を継承して実装してください.
	@Override
	public final Scriptable construct(Context arg0, Scriptable arg1, Object[] arg2) {
		try {
			return newInstance(arg0, arg1, unwrapArray(arg2));
		} catch(RhilaException rwe) {
			throw rwe;
		} catch(Throwable t) {
			throw new RhilaException(t);
		}
	}
	
	/**
	 * function名を設定します.
	 */
	public abstract String getName();
		
	/**
	 * Function実装.
	 */
	public abstract Object function(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args);
	
	/**
	 * new (オブジェクト名) の実装用.
	 */
	public Scriptable newInstance(Context arg0, Scriptable arg1, Object[] arg2) {
		throw new RhilaException("This method '" + getName() +
			"' does not support instantiation.");
	}

	@Override
	public String toString() {
		return "function " + getName() + "() {\n  [native code]\n}";
	}
	
	/**
	 * 引数エラーを返却.
	 */
	protected Object argsException() {
		return argsException(null);
	}

	/**
	 * 引数エラーを返却.
	 */
	protected Object argsException(String objName) {
		if (objName == null) {
			throw new RhilaException("Insufficient arguments for " + getName() + ".");
		}
		throw new RhilaException("Insufficient arguments for " + objName + "." + getName() + ".");
	}
}
