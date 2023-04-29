package rhila.scriptable;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import rhila.RhilaException;
import rhila.lib.ArrayMap;
import rhila.lib.Base64;

/**
 * base64Scriptable.
 */
public class Base64Scriptable implements RhinoScriptable<Object> {
	private static final ArrayMap<String, Scriptable> instanceList =
		new ArrayMap<String, Scriptable>();
	
	// 初期設定.
	static {
		instanceList.put("encode", new Encode());
		instanceList.put("decode", new Decode());
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
	private static final class Encode extends AbstractRhinoFunction {
		protected Encode() {}
		@Override
		public String getName() {
			return "encode";
		}

		@Override
		public Object function(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
	    	if(args == null || args.length <= 0) {
	    		throw new RhilaException("Argument not set.");
	    	}
	    	Object in = args[0];
	    	if(in instanceof byte[]) {
		        return Base64.encode((byte[])in);
	    	} else if(in instanceof BinaryScriptable) {
		        return Base64.encode(((BinaryScriptable)in).getRaw());
	    	} else if(in instanceof String) {
	    		try {
		    		return Base64.encode(((String)in).getBytes("UTF8"));
	    		} catch(RhilaException re) {
	    			throw re;
	    		} catch(Exception e) {
	    			throw new RhilaException(e);
	    		}
	    	}
    		throw new RhilaException("Encode condition argument not set.");
		}
	}
	
	// jsonデコード.
	private static final class Decode extends AbstractRhinoFunction {
		protected Decode() {}
		@Override
		public String getName() {
			return "decode";
		}

		@Override
		public Object function(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
	    	if(args == null || args.length <= 0) {
	    		throw new RhilaException("Argument not set.");
	    	}
	    	Object in = args[0];
	    	if(in instanceof String) {
		        return new BinaryScriptable(Base64.decode((String)in));
	    	}
    		throw new RhilaException("Decode condition argument not set.");
		}
	}
}
