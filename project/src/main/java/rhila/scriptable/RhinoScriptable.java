package rhila.scriptable;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Wrapper;

/**
 * 基本Scriptable.
 */
public interface RhinoScriptable<T> extends Scriptable {
	
	// 元情報を取得.
	default T getRaw() {
		return null;
	}
	
	@Override
	default boolean hasInstance(Scriptable instance) {
		Scriptable proto = instance.getPrototype();
		while (proto != null) {
			if (proto.equals(this)) {
				return true;
			}
			proto = proto.getPrototype();
		}
		return false;
	}

	@SuppressWarnings("rawtypes")
	@Override
	default Object getDefaultValue(Class typeHint) {
		for (int i = 0; i < 2; i++) {
			boolean tryToString;
			if (typeHint == ScriptRuntime.StringClass) {
				tryToString = (i == 0);
			} else {
				tryToString = (i == 1);
			}

			String methodName;
			Object[] args;
			if (tryToString) {
				methodName = "toString";
				args = ScriptRuntime.emptyArgs;
			} else {
				methodName = "valueOf";
				args = new Object[1];
				String hint;
				if (typeHint == null) {
					hint = "undefined";
				} else if (typeHint == ScriptRuntime.StringClass) {
					hint = "string";
				} else if (typeHint == ScriptRuntime.ScriptableClass) {
					hint = "object";
				} else if (typeHint == ScriptRuntime.FunctionClass) {
					hint = "function";
				} else if (typeHint == ScriptRuntime.BooleanClass || typeHint == Boolean.TYPE) {
					hint = "boolean";
				} else if (typeHint == ScriptRuntime.NumberClass || typeHint == ScriptRuntime.ByteClass
						|| typeHint == Byte.TYPE || typeHint == ScriptRuntime.ShortClass || typeHint == Short.TYPE
						|| typeHint == ScriptRuntime.IntegerClass || typeHint == Integer.TYPE
						|| typeHint == ScriptRuntime.FloatClass || typeHint == Float.TYPE
						|| typeHint == ScriptRuntime.DoubleClass || typeHint == Double.TYPE) {
					hint = "number";
				} else {
					throw Context.reportRuntimeError("Invalid JavaScript value of type " + typeHint.toString());
				}
				args[0] = hint;
			}
			Object v = ScriptableObject.getProperty(this, methodName);
			if (!(v instanceof Function))
				continue;
			Function fun = (Function) v;
			Context cx = ContextFactory.getGlobal().enterContext();
			try {
				v = fun.call(cx, fun.getParentScope(), this, args);
			} finally {
				Context.exit();
			}
			if (v != null) {
				if (!(v instanceof Scriptable)) {
					return v;
				}
				if (typeHint == ScriptRuntime.ScriptableClass || typeHint == ScriptRuntime.FunctionClass) {
					return v;
				}
				if (tryToString && v instanceof Wrapper) {
					// Let a wrapped java.lang.String pass for a primitive
					// string.
					Object u = ((Wrapper) v).unwrap();
					if (u instanceof String)
						return u;
				}
			}
		}
		String arg = (typeHint == null) ? "undefined" : typeHint.getName();
		throw Context.reportRuntimeError("Cannot find default value for object " + arg);
	}
	
	//////////////////////////////////
	// 以下空のScriptableメソッド定義.
	//////////////////////////////////
	
	@Override
	default void delete(String arg0) {
	}

	@Override
	default void delete(int arg0) {
	}

	@Override
	default Object get(String arg0, Scriptable arg1) {
		return null;
	}

	@Override
	default Object get(int arg0, Scriptable arg1) {
		return null;
	}

	@Override
	default String getClassName() {
		return this.getClass().getName();
	}

	@Override
	default Object[] getIds() {
		return new Object[0];
	}

	@Override
	default Scriptable getParentScope() {
		return null;
	}

	@Override
	default Scriptable getPrototype() {
		return null;
	}

	@Override
	default boolean has(String arg0, Scriptable arg1) {
		return false;
	}

	@Override
	default boolean has(int arg0, Scriptable arg1) {
		return false;
	}

	@Override
	default void put(String arg0, Scriptable arg1, Object arg2) {
	}

	@Override
	default void put(int arg0, Scriptable arg1, Object arg2) {
	}

	@Override
	default void setParentScope(Scriptable arg0) {
	}

	@Override
	default void setPrototype(Scriptable arg0) {
	}
}
