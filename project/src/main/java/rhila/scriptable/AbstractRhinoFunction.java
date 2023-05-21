package rhila.scriptable;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

/**
 * RhinoFunction雛形.
 * 
 * 以下2つの実装を行う事で、rhinoのjs用function呼び出しを実施します.
 */
public abstract class AbstractRhinoFunction implements RhinoFunction {
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
		return (clazz == null || String.class.equals(clazz)) ?
			toString() : Undefined.instance;
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
			return this.getClassName()
				.equals(instance.getClassName());
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
	
	@Override
	public String toString() {
		return "function " + getName() +
			"() {\n  [native code]\n}";
	}
}
