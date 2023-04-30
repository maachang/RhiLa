package rhila.lib;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

/**
 * ArrayMapオブジェクト.
 * ※非Mapオブジェクト.
 *
 * BinarySearchを使って、データの追加、削除、取得を行います.
 * HashMapと比べると速度は１０倍ぐらいは遅いですが、リソースは
 * List構造のものと同じぐらいしか食わないので、リソースを重視
 * する場合は、こちらを利用することをおすすめします.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ArrayMap<K, V> {
    // lambda snapStart CRaC用.
	protected static final ArrayMap LOAD_CRAC = new ArrayMap();
	
	// 管理する情報.
	protected final ObjectList<Entry<K, V>> list;

	/**
	 * コンストラクタ.
	 */
	public ArrayMap() {
		list = new ObjectList<Entry<K, V>>();
	}

	/**
	 * コンストラクタ.
	 * @param args args.length == 1 で args[0]にMap属性を設定すると、その内容がセットされます.
	 *     また、 key, value .... で設定することも可能です.
	 */
	public ArrayMap(final Object... args) {
		if(args.length == 1) {
			if(args[0] == null) {
				list = new ObjectList<Entry<K, V>>();
			} else if(args[0] instanceof Map) {
				list = new ObjectList<Entry<K, V>>(((Map)args[0]).size());
				putAll(args[0]);
				return;
			} else if(args[0] instanceof Number) {
				list = new ObjectList<Entry<K, V>>(((Number)args[0]).intValue());
				return;
			} else if(args[0] instanceof String && NumberUtil.isNumeric((String)args[0])) {
				list = new ObjectList<Entry<K, V>>(NumberUtil.parseInt((String)args[0]));
				return;
			}
			throw new IllegalArgumentException("Key and Value need to be set.");
		}
		list = new ObjectList<Entry<K, V>>(args.length >> 1);
		putAll(args);
	}

	/**
	 * データクリア.
	 */
	public void clear() {
		list.clear();
	}

	/**
	 * データセット.
	 * @param key
	 * @param value
	 * @return
	 */
	public V put(final K key, final V value) {
		if(key == null) {
			return null;
		} else if(list.size() == 0) {
			list.add(new Entry<K, V>(key, value));
			return null;
		}
		int mid, cmp;
		int low = 0;
		int high = list.size() - 1;
		Object[] olst = list.rawArray();
		mid = -1;
		while (low <= high) {
			mid = (low + high) >>> 1;
			if ((cmp = ((Comparable)((Entry<K, V>)olst[mid]).key)
				.compareTo(key)) < 0) {
				low = mid + 1;
			} else if (cmp > 0) {
				high = mid - 1;
			} else {
				// 一致条件が見つかった場合.
				final Entry<K, V> o = (Entry<K, V>)olst[mid];
				final Object ret = o.value;
				o.value = value;
				return (V)ret;
			}
		}
		// 一致条件が見つからない場合.
		mid = (((Comparable)((Entry<K, V>)olst[mid]).key)
			.compareTo(key) < 0) ? mid + 1 : mid;
		list.add(null);
		final int len = list.size();
		olst = list.rawArray();
		System.arraycopy(olst, mid, olst, mid + 1, len - (mid + 1));
		olst[mid] = new Entry<K, V>(key, value);
		return null;
	}

	/**
	 * 指定データ群の設定.
	 * @param args args.length == 1 で args[0]にMap属性を設定すると、
	 *             その内容がセットされます.
	 *             また、 key, value .... で設定することも可能です.
	 */
	public void putAll(final Object... args) {
		if (args == null) {
			return;
		} else if(args.length == 1) {
			// mapの場合.
			if(args[0] instanceof Map) {
				Map mp = (Map)args[0];
				if (mp.size() == 0) {
					return;
				}
				K k;
				final Iterator it = mp.keySet().iterator();
				// 設定元データが存在しない場合.
				if(size() == 0) {
					int i = 0;
					int len = mp.size();
					// sortKeyでソートする.
					SortKey[] keys = new SortKey[len];
					while (it.hasNext()) {
						k = (K)it.next();
						if(k == null) {
							continue;
						}
						keys[i ++] = new SortKey(
							(Comparable)k, mp.get(k));
					}
					len = i;
					Arrays.sort(keys, 0, len);
					for(i = 0; i < len; i ++) {
						list.add(new Entry(
							keys[i].getKey(), keys[i].getValue()));
					}
				// 設定元データが存在する場合.
				} else {
					while (it.hasNext()) {
						k = (K)it.next();
						if(k == null) {
							continue;
						}
						put(k, (V)mp.get(k));
					}
				}
			// ArrayMap設定の場合.
			} else if(args[0] instanceof ArrayMap) {
				int len;
				ArrayMap mp = (ArrayMap)args[0];
				if ((len = mp.size()) == 0) {
					return;
				}
				// 設定元データが存在しない場合.
				if(size() == 0) {
					for(int i = 0; i < len; i ++) {
						list.add(new Entry(
							mp.keyAt(i), mp.valueAt(i)));
					}
				// 設定元データが存在する場合.
				} else {
					for(int i = 0; i < len; i ++) {
						put((K)mp.keyAt(i), (V)mp.valueAt(i));
					}
				}
			} else {
				throw new IllegalArgumentException(
					"Key and Value need to be set.");
			}
		} else {
			// key, value ... の場合.
			int len = args.length;
			// 設定元データが存在しない場合.
			if(size() == 0) {
				// sortKeyでソートする.
				SortKey[] keys = new SortKey[len >> 1];
				for (int i = 0, j = 0; i < len; i += 2, j ++) {
					keys[j] = new SortKey(
						(Comparable)args[i], args[i + 1]);
				}
				Arrays.sort(keys);
				len = keys.length;
				// ソート結果をObjectListに設定.
				for (int i = 0; i < len; i ++) {
					list.add(new Entry(
						keys[i].getKey(), keys[i].getValue()));
				}
			// 設定元データが存在する場合.
			} else {
				for (int i = 0; i < len; i += 2) {
					put((K)args[i], (V)args[i + 1]);
				}
			}
		}
		return;
	}
	
	// キーソート用.
	private static final class SortKey implements Comparable {
		private Comparable key;
		private Object value;
		
		public SortKey(Comparable key, Object value) {
			this.key = key;
			this.value = value;
		}
		
		public Comparable getKey() {
			return key;
		}
		
		public Object getValue() {
			return value;
		}

		@Override
		public int compareTo(Object o) {
			return key.compareTo(((SortKey)o).key);
		}
	}

	/**
	 * データ取得.
	 * @param key
	 * @return
	 */
	public V get(final Object key) {
		final Entry<K, V> e = getEntry((K)key);
		if(e == null) {
			return null;
		}
		return e.value;
	}

	/**
	 * データ確認.
	 * @param key
	 * @return
	 */
	public boolean containsKey(final K key) {
		return searchKey(key) != -1;
	}

	/**
	 * データ削除.
	 * @param key
	 * @return
	 */
	public V remove(final K key) {
		final int no = searchKey(key);
		if (no != -1) {
			return (V)list.remove(no);
		}
		return null;
	}

	/**
	 * データ数を取得.
	 * @return
	 */
	public int size() {
		return list.size();
	}

	/**
	 * キー名一覧を取得.
	 * @return
	 */
	public Object[] names() {
		final int len = list.size();
		final Object[] ret = new Object[len];
		for (int i = 0; i < len; i++) {
			ret[i] = list.get(i).key;
		}
		return ret;
	}

	/**
	 * 指定項番でキー情報を取得.
	 * @param no
	 * @return
	 */
	public K keyAt(int no) {
		return list.get(no).key;
	}

	/**
	 * 指定項番で要素情報を取得.
	 * @param no
	 * @return
	 */
	public V valueAt(int no) {
		return list.get(no).value;
	}

	// 指定キーのEntry情報を取得.
	protected final Entry<K, V> getEntry(final K key) {
		final int no = searchKey(key);
		if (no == -1) {
			return null;
		}
		return list.get(no);
	}

	@Override
	public int hashCode() {
		return list.size();
	}

	/**
	 * 対象オブジェクトと一致するかチェック.
	 * @param o
	 * @return
	 */
	@Override
	public boolean equals(Object o) {
		if(o instanceof ArrayMap) {
			final ArrayMap ix = (ArrayMap)o;
			int len = list.size();
			if(len != ix.size()) {
				return false;
			}
			Entry s, d;
			final Object[] lst = list.rawArray();
			for(int i = 0; i < len; i ++) {
				s = (Entry)lst[i];
				d = ix.getEntry(s.key);
				if(d == null || !s.equals(d)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * 文字列として出力.
	 * @return
	 */
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		int len = list.size();
		buf.append("{");
		for (int i = 0; i < len; i++) {
			if (i != 0) {
				buf.append(", ");
			}
			buf.append(list.get(i));
		}
		return buf.append("}").toString();
	}
	
	/**
	 * 同じ内容で別のオブジェクトを作成.
	 * @return IndexKeyValueList コピーされた内容が返却されます.
	 */
	public ArrayMap copy() {
		return copy(null);
	}

	/**
	 * 同じ内容で別のオブジェクトを作成.
	 * @param out 指定した場合、このオブジェクトに格納されます.
	 * @return IndexKeyValueList コピーされた内容が返却されます.
	 */
	public ArrayMap copy(ArrayMap<K, V> out) {
		final ArrayMap<K, V> ret = out == null ?
			new ArrayMap<K, V>(this.size()) :
			out;
		ret.clear();
		final ObjectList<Entry<K, V>> srcList = this.list;
		final ObjectList<Entry<K, V>> retList = ret.list;
		final int len = srcList.size();
		for(int i = 0; i < len; i ++) {
			retList.add(srcList.get(i).copy());
		}
		return ret;
	}

	// バイナリサーチ.
	private final int searchKey(final K n) {
		if(n != null) {
			final Object[] olst = list.rawArray();
			int low = 0;
			int high = list.size() - 1;
			int mid, cmp;
			while (low <= high) {
				mid = (low + high) >>> 1;
				if ((cmp = ((Comparable)((Entry<K, V>)olst[mid]).key).compareTo(n)) < 0) {
					low = mid + 1;
				} else if (cmp > 0) {
					high = mid - 1;
				} else {
					return mid; // key found
				}
			}
		}
		return -1;
	}

	/**
	 *  Index用KeyValue.
	 */
	protected static final class Entry<K, V> implements Comparable<K> {
		K key;
		V value;
		public Entry(K k, V v) {
			key = k;
			value = v;
		}
		public Entry<K, V> copy() {
			return new Entry(key, value);
		}
		public K getKey() {
			return key;
		}
		public V getValue() {
			return value;
		}
		@Override
		public int compareTo(K n) {
			return ((Comparable)key).compareTo(n);
		}
		@Override
		public int hashCode() {
			return key.hashCode();
		}
		@Override
		public boolean equals(Object o) {
			if(o instanceof Entry) {
				Entry e = (Entry)o;
				return key.equals(e.key) &&
					(value == null ? e.value == null : value.equals(e.value));
			}
			return false;
		}
		@Override
		public String toString() {
			if(value instanceof CharSequence || value instanceof Character) {
				return new StringBuilder().append("\"").append(key).append("\": \"")
					.append(value).append("\"").toString();
			}
			return new StringBuilder().append("\"").append(key).append("\": \"")
				.append(value).append("\"").toString();
		}
	}
}

