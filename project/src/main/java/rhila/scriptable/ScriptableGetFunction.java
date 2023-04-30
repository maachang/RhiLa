package rhila.scriptable;

import org.mozilla.javascript.Scriptable;

import rhila.lib.ArrayMap;
import rhila.scriptable.BinaryScriptable.BinaryScriptableObject;
import rhila.scriptable.ListScriptable.ListScriptableObject;
import rhila.scriptable.LowerKeyMapScriptable.LowerKeyMapScriptableObject;
import rhila.scriptable.MapScriptable.MapScriptableObject;

/**
 * scriptable用function.
 */
public class ScriptableGetFunction implements RhinoGetFunction {
	protected ScriptableGetFunction() {}
    // lambda snapStart CRaC用.
	protected static final ScriptableGetFunction LOAD_CRAC = new ScriptableGetFunction();
	// functionインスタンス管理用.
	private static final ArrayMap<String, Scriptable> instanceList;
	
	// 初期設定.
	static {
		instanceList = new ArrayMap<String, Scriptable>(
			"Date", DateScriptable.LOAD_CRAC
			,"Map", MapScriptableObject.LOAD_CRAC
			,"Object", MapScriptableObject.LOAD_CRAC
			,"LowerKeyMap", LowerKeyMapScriptableObject.LOAD_CRAC
			,"LoMap", LowerKeyMapScriptableObject.LOAD_CRAC
			,"List", ListScriptableObject.LOAD_CRAC
			,"Array", ListScriptableObject.LOAD_CRAC
			,"JSON", JsonScriptableObject.LOAD_CRAC
			,"Base64", Base64ScriptableObject.LOAD_CRAC
			,"Binary", BinaryScriptableObject.LOAD_CRAC
		);
	}
	
	// オブジェクトを取得.
	public static final ScriptableGetFunction getInstance() {
		return LOAD_CRAC;
	}
	
    // Functionを取得.
	public final Scriptable getFunction(String name) {
		return instanceList.get(name);
	}
}
