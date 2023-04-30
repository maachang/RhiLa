package rhila.scriptable;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import rhila.lib.Json;

/**
 * java.util.Map用Scriptable.
 */
public class MapScriptable extends ScriptableObject
	implements Map<String, Object> {
	private static final long serialVersionUID = 6662772603383861744L;
	
    // lambda snapStart CRaC用.
    protected static final MapScriptable LOAD_CRAC = new MapScriptable();
	
	protected Scriptable parent = null;
	protected Scriptable prototype = null;
	
	// mapオブジェクト.
	protected Map<String, Object> map;
	
	// コンストラクタ.
	public MapScriptable() {
		this.map = new HashMap<String, Object>();
	}
	
	// コンストラクタ.
	public MapScriptable(Map<String, Object> map) {
		if(map instanceof MapScriptable) {
			this.map = ((MapScriptable)map).getRaw();
		} else {
			this.map = map;
		}
	}
	
	// コンストラクタ.
	public MapScriptable(Object[] args) {
		Map<String, Object> m = new HashMap<String, Object>();
		final int len = args == null ? 0 : args.length;
		for(int i = 0; i < len; i += 2) {
			m.put((String)args[i], args[i + 1]);
		}
	}
	
	// コンストラクタ.
	public MapScriptable(List<Object> args) {
		Map<String, Object> m = new HashMap<String, Object>();
		final int len = args == null ? 0 : args.size();
		for(int i = 0; i < len; i += 2) {
			m.put((String)args.get(i), args.get(i + 1));
		}
	}
	
	//@Override
	public Map<String, Object> getRaw() {
		return map;
	}
	
	@Override
	public void setParentScope(Scriptable arg0) {
		parent = arg0;
	}
	
	@Override
	public Scriptable getParentScope() {
		return parent;
	}

	@Override
	public void setPrototype(Scriptable arg0) {
		prototype = arg0;
	}

	@Override
	public Scriptable getPrototype() {
		return prototype;
	}

	@Override
	public void delete(String arg0) {
		map.remove(arg0);
	}

	@Override
	public void delete(int arg0) {
		map.remove(String.valueOf(arg0));
	}
	
	@Override
	public Object get(String arg0, Scriptable arg1) {
		if("length".equals(arg0)) {
			return map.size();
		} else if(map instanceof Scriptable) {
			return RhilaWrapper.wrap(
				((Scriptable)map).get(arg0, (Scriptable)map));
		} else if(!map.containsKey(arg0)) {
			return NOT_FOUND;
		}
		return RhilaWrapper.wrap(map.get(arg0));
	}

	@Override
	public Object get(int arg0, Scriptable arg1) {
		final String key = String.valueOf(arg0);
		if(map instanceof Scriptable) {
			return RhilaWrapper.wrap(
				((Scriptable)map).get(key, (Scriptable)map));
		}
		if(!map.containsKey(key)) {
			return NOT_FOUND;
		}
		return RhilaWrapper.wrap(map.get(key));
	}

	@Override
	public String getClassName() {
		return map.getClass().getName();
	}

	@Override
	public Object[] getIds() {
		if(map instanceof Scriptable) {
			return ((Scriptable)map).getIds();
		}
		int cnt = 0;
		final int len = map.size();
		final Object[] ret = new Object[len];
		final Iterator<String> it = map.keySet().iterator();
		while(it.hasNext()) {
			ret[cnt ++] = it.next();
		}
		return ret;
	}

	@Override
	public boolean has(String arg0, Scriptable arg1) {
		if(map instanceof Scriptable) {
			return ((Scriptable)map).has(arg0, (Scriptable)map);
		}
		return map.containsKey(arg0);
	}

	@Override
	public boolean has(int arg0, Scriptable arg1) {
		if(map instanceof Scriptable) {
			return ((Scriptable)map).has(arg0, (Scriptable)map);
		}
		return map.containsKey(String.valueOf(arg0));
	}
	
	@Override
	public void put(String arg0, Scriptable arg1, Object arg2) {
		if(map instanceof Scriptable) {
			((Scriptable)map).put(arg0, (Scriptable)map, RhilaWrapper.unwrap(arg2));
		} else {
			map.put(arg0, RhilaWrapper.unwrap(arg2));
		}
	}

	@Override
	public void put(int arg0, Scriptable arg1, Object arg2) {
		if(map instanceof Scriptable) {
			((Scriptable)map).put(
				String.valueOf(arg0), (Scriptable)map, RhilaWrapper.unwrap(arg2));
		} else {
			map.put(String.valueOf(arg0), RhilaWrapper.unwrap(arg2));
		}
	}
	
	@Override
	public String toString() {
		//return "[map]";
		return Json.encode(map);
	}
	
	@Override
	public void clear() {
		if(map instanceof Scriptable) {
			try {
				// clear失敗の場合は項目別に削除.
				map.clear();
				return;
			} catch(Exception e) {}
			// 項目別に削除だから遅い.
			Scriptable s = (Scriptable)map;
			Iterator<String> it = map.keySet().iterator();
			while(it.hasNext()) {
				s.delete(it.next());
			}
		} else {
			map.clear();
		}
	}

	@Override
	public int size() {
		return map.size();
	}

	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}

	@Override
	public Object get(Object key) {
		return map.get(key);
	}

	@Override
	public Object put(String key, Object value) {
		if(map instanceof Scriptable) {
			((Scriptable)map).put(key, (Scriptable)map, value);
			return null;
		}
		return map.put(key, value);
	}

	@Override
	public Object remove(Object key) {
		return map.remove(key);
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> m) {
		// 多分使う事は少ないのでunsupport.
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<String> keySet() {
		return map.keySet();
	}

	@Override
	public Collection<Object> values() {
		return map.values();
	}

	// unsupport.
	@Override
	public Set<Entry<String, Object>> entrySet() {
		return map.entrySet();
	}
	
	// MapScriptableのオブジェクト利用.
	public static final class MapScriptableObject extends AbstractRhinoFunction {
	    // lambda snapStart CRaC用.
	    protected static final MapScriptableObject LOAD_CRAC = new MapScriptableObject();
	    
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public Scriptable newInstance(Context arg0, Scriptable arg1, Object[] arg2) {
			if(arg2 == null || arg2.length == 0) {
				return new MapScriptable();
			}
			if(arg2.length == 1) {
				Object o = arg2[0];
				if(o == null) {
					return new MapScriptable();
				} else if(o instanceof Map) {
					return new MapScriptable((Map)o);
				} else if(o instanceof List) {
					return new MapScriptable((List)o);
				} else if(o.getClass().isArray()) {
					final int len = Array.getLength(o);
					final Object[] oo = new Object[len];
					System.arraycopy(o, 0, oo, 0, len);
					return new MapScriptable(oo);
				}
			}
			return new MapScriptable(arg2);
		}
		@Override
		public String getName() {
			return "Map";
		}
		@Override
		public String toString() {
			return "[Map]";
		}
	}
}
