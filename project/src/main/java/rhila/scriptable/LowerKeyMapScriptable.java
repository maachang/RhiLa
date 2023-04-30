package rhila.scriptable;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

/**
 * Keyを小文字管理するMapScriptable.
 */
public class LowerKeyMapScriptable extends MapScriptable {
	private static final long serialVersionUID = -3484541618105346510L;
	
    // lambda snapStart CRaC用.
    protected static final LowerKeyMapScriptable LOAD_CRAC = new LowerKeyMapScriptable();
	
	// コンストラクタ.
	public LowerKeyMapScriptable() {
		this.map = new HashMap<String, Object>();
	}
	
	// コンストラクタ.
	public LowerKeyMapScriptable(Map<String, Object> map) {
		Map<String, Object> m = new HashMap<String, Object>();
		Iterator<String> it = map.keySet().iterator();
		String key;
		while(it.hasNext()) {
			key = it.next();
			m.put(key.toLowerCase(), map.get(key));
		}
		this.map = m;
	}
	
	// コンストラクタ.
	public LowerKeyMapScriptable(Object[] args) {
		Map<String, Object> m = new HashMap<String, Object>();
		final int len = args == null ? 0 : args.length;
		for(int i = 0; i < len; i += 2) {
			m.put(((String)args[i]).toLowerCase(), args[i + 1]);
		}
	}
	
	// コンストラクタ.
	public LowerKeyMapScriptable(List<Object> args) {
		Map<String, Object> m = new HashMap<String, Object>();
		final int len = args == null ? 0 : args.size();
		for(int i = 0; i < len; i += 2) {
			m.put(((String)args.get(i)).toLowerCase(), args.get(i + 1));
		}
	}
	
	@Override
	public boolean containsKey(Object key) {
		return super.containsKey(((String)key).toLowerCase());
	}

	@Override
	public Object get(Object key) {
		return super.get(((String)key).toLowerCase());
	}

	@Override
	public Object put(String key, Object value) {
		return super.put(key.toLowerCase(), value);
	}

	@Override
	public Object remove(Object key) {
		return super.remove(((String)key).toLowerCase());
	}
	
	@Override
	public void delete(String arg0) {
		super.remove(arg0.toLowerCase());
	}
	
	@Override
	public Object get(String arg0, Scriptable arg1) {
		final Object ret = super.get(arg0, arg1);
		if(ret instanceof Undefined || ret == null) {
			return ret;
		}
		return super.get(arg0.toLowerCase(), arg1);
	}

	@Override
	public boolean has(String arg0, Scriptable arg1) {
		return super.has(arg0.toLowerCase(), arg1);
	}
	
	@Override
	public void put(String arg0, Scriptable arg1, Object arg2) {
		super.put(arg0.toLowerCase(), arg1, arg2);
	}
	
	// LowerKeyMapScriptableのオブジェクト利用.
	public static final class LowerKeyMapScriptableObject
		extends AbstractRhinoFunction {
	    // lambda snapStart CRaC用.
	    protected static final LowerKeyMapScriptableObject LOAD_CRAC =
	    	new LowerKeyMapScriptableObject();
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public Scriptable newInstance(
			Context arg0, Scriptable arg1, Object[] arg2) {
			if(arg2 == null || arg2.length == 0) {
				return new LowerKeyMapScriptable();
			}
			if(arg2.length == 1) {
				Object o = arg2[0];
				if(o == null) {
					return new LowerKeyMapScriptable();
				} else if(o instanceof Map) {
					return new LowerKeyMapScriptable((Map)o);
				} else if(o instanceof List) {
					return new LowerKeyMapScriptable((List)o);
				} else if(o.getClass().isArray()) {
					final int len = Array.getLength(o);
					final Object[] oo = new Object[len];
					System.arraycopy(o, 0, oo, 0, len);
					return new LowerKeyMapScriptable(oo);
				}
			}
			return new LowerKeyMapScriptable(arg2);
		}
		@Override
		public String getName() {
			return "LowerKeyMap";
		}
		@Override
		public String toString() {
			return "[LowerKeyMap]";
		}
	}
}
