package rhila.scriptable;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import rhila.RhilaException;
import rhila.lib.ArrayMap;
import rhila.lib.Json;

/**
 * jsonScriptable.
 */
public class JsonScriptable implements RhinoScriptable<Object> {
	private static final ArrayMap<String, Scriptable> instanceList =
		new ArrayMap<String, Scriptable>();
	
	// 初期設定.
	static {
		instanceList.put("stringify", new Stringify());
		instanceList.put("parse", new Parse());
	}
	
	@Override
	public Object get(String arg0, Scriptable arg1) {
		return getFunction(arg0);
	}
	
	// function取得.
	private static final Object getFunction(String name) {
		return instanceList.get(name);
	}
	
	// jsonエンコード.
	private static final class Stringify extends AbstractRhinoFunction {
		protected Stringify() {}
		@Override
		public String getName() {
			return "stringify";
		}

		@Override
		public Object function(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
	    	if(args == null || args.length <= 0) {
	    		throw new RhilaException("No object specified for JSON conversion.");
	    	}
	        return Json.encode(args[0]);
		}
	}
	
	// jsonデコード.
	private static final class Parse extends AbstractRhinoFunction {
		protected Parse() {}
		@Override
		public String getName() {
			return "parse";
		}

		@Override
		public Object function(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
	    	if(args == null || args.length <= 0 || !(args[0] instanceof String)) {
	    		throw new RhilaException("JSON string to parse is not set.");
	    	}
	        return (Scriptable)Json.decode((String)args[0]);
		}
	}
}
