package rhila.scriptable;

import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

import rhila.lib.Json;

/**
 * java.util.List用Scriptable.
 */
public class ListScriptable
	extends AbstractList<Object> 
	implements RhinoScriptable<List<Object>> {
	
	protected Scriptable parent = null;
	protected Scriptable prototype = null;
	
	// listオブジェクト.
	private List<Object> list = null;
	
	// コンストラクタ.
	public ListScriptable() {
		this.list = new ArrayList<Object>();
	}
	
	// コンストラクタ.
	public ListScriptable(int len) {
		this.list = new ArrayList<Object>(len);
	}
	
	// コンストラクタ.
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ListScriptable(List list) {
		if(list instanceof ListScriptable) {
			this.list = ((ListScriptable)list).getRaw();
		} else {
			this.list = list;
		}
	}
	
	// コンストラクタ.
	public ListScriptable(Object[] array) {
		if(array == null || array.length == 0) {
			this.list = new ArrayList<Object>();
			return;
		}
		final int len = array.length;
		this.list = new ArrayList<Object>(array.length);
		for(int i = 0; i < len; i ++) {
			this.list.add(array[i]);
		}
	}
	
	@Override
	public List<Object> getRaw() {
		return list;
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
		try {
			final int n = Integer.parseInt(arg0);
			if(list instanceof Scriptable) {
				((Scriptable)list).delete(n);
				return;
			}
			list.remove(n);
		} catch(Exception e) {}
	}

	@Override
	public void delete(int arg0) {
		try {
			if(list instanceof Scriptable) {
				((Scriptable)list).delete(arg0);
				return;
			}
			list.remove(arg0);
		} catch(Exception e) {}
	}

	@Override
	public Object get(String arg0, Scriptable arg1) {
		if("length".equals(arg0)) {
			return list.size();
		} else if("add".equals(arg0)) {
			if(ADD == null) {
				ADD = new Add();
			}
			return ADD;
		}
		try {
			Object ret = null;
			if(list instanceof Scriptable) {
				Scriptable s = (Scriptable)list;
				ret = s.get(arg0, s);
				if(ret instanceof Undefined) {
					ret = s.get(Integer.parseInt(arg0), s);
				}
			} else {
				ret = list.get(Integer.parseInt(arg0));
			}
			if(ret != null && !(ret instanceof Undefined)) {
				return RhilaWrapper.wrap(ret);
			}
		} catch(Exception e) {}
		return NOT_FOUND;
	}

	@Override
	public Object get(int arg0, Scriptable arg1) {
		try {
			Object ret = null;
			if(list instanceof Scriptable) {
				ret = ((Scriptable)list).get(arg0, (Scriptable)list);
			} else {
				ret = list.get(arg0);
			}
			if(ret != null && !(ret instanceof Undefined)) {
				return RhilaWrapper.wrap(ret);
			}
		} catch(Exception e) {}
		return NOT_FOUND;
	}

	@Override
	public String getClassName() {
		return list.getClass().getName();
	}

	@Override
	public Object[] getIds() {
		if(list instanceof Scriptable) {
			return ((Scriptable)list).getIds();
		}
		final int len = list.size();
		final Object[] ret = new Object[len];
		for(int i = 0; i < len; i ++) {
			ret[i] = i;
		}
		return ret;
	}

	@Override
	public boolean has(String arg0, Scriptable arg1) {
		return !(get(arg0, arg1) instanceof Undefined);
	}

	@Override
	public boolean has(int arg0, Scriptable arg1) {
		return !(get(arg0, arg1) instanceof Undefined);
	}
	
	// 指定サイズ分長さを増やす.
	private final void appendList(int max) {
		if(max >= list.size()) {
			final int len = (max + 1) - list.size();
			for(int i = 0; i < len; i ++) {
				list.add(null);
			}
		}
	}

	@Override
	public void put(String arg0, Scriptable arg1, Object arg2) {
		try {
			final int n = Integer.parseInt(arg0);
			if(list instanceof Scriptable) {
				((Scriptable)list).put(
					n, (Scriptable)list, RhilaWrapper.unwrap(arg2));
				return;
			}
			appendList(n);
			list.set(n, RhilaWrapper.unwrap(arg2));
		} catch(Exception e) {}
	}

	@Override
	public void put(int arg0, Scriptable arg1, Object arg2) {
		try {
			if(list instanceof Scriptable) {
				((Scriptable)list).put(
					arg0, (Scriptable)list, RhilaWrapper.unwrap(arg2));
				return;
			}
			appendList(arg0);
			list.set(arg0, RhilaWrapper.unwrap(arg2));
		} catch(Exception e) {}
	}
	
	@Override
	public String toString() {
		//return "[list]";
		return Json.encode(list);
	}
	
	@Override
	public void clear() {
		if(list instanceof Scriptable) {
			try {
				// clear失敗の場合は項目別に削除.
				list.clear();
			} catch(Exception e) {}
			// 項目別に削除だから遅い.
			Scriptable s = (Scriptable)list;
			int len = ((Long)s.get("length", null)).intValue();
			for(int i = 0; i < len; i ++) {
				s.delete(i);
			}
			return;
		}
		list.clear();
	}
	
	@Override
	public boolean equals(Object o) {
		return list.equals(o);
	}
	
	@Override
	public int hashCode() {
		return list.hashCode();
	}

	@Override
	public Object get(int index) {
		if(list instanceof Scriptable) {
			return ((Scriptable)list).get(
				index, (Scriptable)list);
		}
		return list.get(index);
	}

	@Override
	public int size() {
		if(list instanceof Scriptable) {
			return ((Number)get(
				"length", (Scriptable)list)).intValue();
		}
		return list.size();
	}
	
	@Override
	public boolean add(Object arg0) {
		if(list instanceof Scriptable) {
			((Scriptable)list).put(
				size(), (Scriptable)list, arg0);
			return true;
		}
		return list.add(arg0);
	}
	
	@Override
	public void add(int index, Object element) {
		// 多分使う事は少ないのでunsupport.
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean addAll(int index, Collection<? extends Object> c) {
		// 多分使う事は少ないのでunsupport.
		throw new UnsupportedOperationException();
	}
	
	@Override
	public int indexOf(Object o) {
		return list.indexOf(o);
	}
	
	@Override
	public Iterator<Object> iterator() {
		return list.iterator();
	}
	
	@Override
	public int lastIndexOf(Object o) {
		return list.lastIndexOf(o);
	}
	
	@Override
	public ListIterator<Object> listIterator() {
		return list.listIterator();		
	}
	
	@Override
	public ListIterator<Object> listIterator(int index) {
		return list.listIterator(index);		
	}
	
	@Override
	public Object remove(int index) {
		if(list instanceof Scriptable) {
			((Scriptable)list).delete(index);
			return null;
		}
		return list.remove(index);
	}
	
	@Override
	public Object set(int index, Object arg1) {
		if(list instanceof Scriptable) {
			((Scriptable)list).put(index, (Scriptable)list, arg1);
			return null;
		}
		return list.set(index, arg1);
	}
	
	@Override
	public List<Object> subList(int fromIndex, int toIndex) {
		return list.subList(fromIndex, toIndex);
	}
	
	// add function
	private final class Add extends AbstractRhinoFunction {
		@Override
		public String getName() {
			return "add";
		}
		
		@Override
		public Object function(Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			final int len = (args == null) ? 0 : args.length;
			for(int i = 0; i < len; i ++) {
				add(RhilaWrapper.unwrap(args[i]));
			}
			// 実装しない場合は空返却.
			return Undefined.instance;
		}
	}
	private Add ADD = null;
	
	// ListScriptableのオブジェクト利用.
	public static final class ListScriptableObject extends AbstractRhinoFunction {
		@SuppressWarnings("rawtypes")
		@Override
		public Scriptable newInstance(Context arg0, Scriptable arg1, Object[] arg2) {
			if(arg2 == null || arg2.length == 0) {
				return new ListScriptable();
			}
			if(arg2.length == 1) {
				Object o = arg2[0];
				if(o == null) {
					return new ListScriptable();
				} else if(o instanceof Number) {
					return new ListScriptable(((Number)o).intValue());
				} else if(o instanceof List) {
					return new ListScriptable((List)o);
				} else if(o.getClass().isArray()) {
					final int len = Array.getLength(o);
					final Object[] oo = new Object[len];
					System.arraycopy(o, 0, oo, 0, len);
					return new ListScriptable(oo);
				}
			}
			return new ListScriptable(arg2);

		}
		@Override
		public String getName() {
			return "List";
		}
		@Override
		public String toString() {
			return "[List]";
		}
	}
}
