package rhila.scriptable;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import rhila.Json;
import rhila.RhilaException;

/**
 * jsonScriptable.
 */
public class JsonScriptable implements BaseScriptable<Object> {
	
	@Override
	public Object get(String arg0, Scriptable arg1) {
		return getFunction(arg0);
	}
	
	// function取得.
	private static final Object getFunction(String name) {
		switch(name) {
		case "stringify":
			if(STRINGIFY == null) {
				STRINGIFY = new Stringify();
			}
			return STRINGIFY;
		case "parse":
			if(PARSE == null) {
				PARSE = new Parse();
			}
			return PARSE;
		}
		return null;
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
	private static Stringify STRINGIFY = null;
	
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
	private static Parse PARSE = null;
}
