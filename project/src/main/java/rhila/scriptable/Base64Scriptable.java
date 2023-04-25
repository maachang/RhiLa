package rhila.scriptable;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import rhila.Base64;
import rhila.RhilaException;
/**
 * base64Scriptable.
 */
public class Base64Scriptable implements BaseScriptable<Object> {
	// jsで利用するBASE64処理.
	public static final Base64Scriptable INSTANCE =
		new Base64Scriptable();
	
	@Override
	public Object get(String arg0, Scriptable arg1) {
		if("encode".equals(arg0)) {
			if(ENCODE == null) {
				ENCODE = new Encode();
			}
			return ENCODE;
		}
		if("decode".equals(arg0)) {
			if(DECODE == null) {
				DECODE = new Decode();
			}
			return DECODE;
		}
		return null;
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
	private static Encode ENCODE = null;
	
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
	private static Decode DECODE = null;
}
