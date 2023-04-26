package rhila.scriptable;

import org.mozilla.javascript.Scriptable;

/**
 * GetFunction処理.
 */
public interface RhinoGetFunction {
	
    // Functionを取得.
	public Scriptable getFunction(String name);

}
