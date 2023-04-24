package rhila.core;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mozilla.javascript.Undefined;

import rhila.lib.DateUtil;
import rhila.lib.NumberUtil;

/**
 * Json変換処理.
 */
@SuppressWarnings("rawtypes")
public final class Json {
	protected Json() {}

	private static final int TYPE_ARRAY = 0;
	private static final int TYPE_MAP = 1;

	/**
	 * JSON変換.
	 * 
	 * @param target 対象のターゲットオブジェクトを設定します.
	 * @return String 変換されたJSON情報が返されます.
	 */
	public static final String encode(Object target) {
		StringBuilder buf = new StringBuilder();
		encodeObject(buf, target, target);
		return buf.toString();
	}

	/**
	 * JSON形式から、オブジェクト変換.
	 * 
	 * @param json 対象のJSON情報を設定します.
	 * @return Object 変換されたJSON情報が返されます.
	 */
	public static final Object decode(String json) {
		if (json == null) {
			return null;
		}
		// 前後の無駄なスペース、タブ、改行などを除く.
		json = json.trim();
		List<Object> list;
		int[] n = new int[1];
		while (true) {
			// token解析が必要な場合.
			// [ ... ] or { .... }
			if (json.startsWith("[") || json.startsWith("{")) {
				// JSON形式をToken化.
				list = analysisJsonToken(json);
				// Token解析処理.
				if ("[".equals(list.get(0))) {
					// List解析.
					return createJsonInfo(n, list, TYPE_ARRAY, 0, list.size());
				} else {
					// Map解析.
					return createJsonInfo(n, list, TYPE_MAP, 0, list.size());
				}
			// (...) のような形式の場合は、このカッコを無視して再処理.
			} else if (json.startsWith("(") && json.endsWith(")")) {
				json = json.substring(1, json.length() - 1).trim();
				continue;
			}
			// それ以外の場合.
			break;
		}
		return decodeJsonValue(json);
	}

	/** [encodeJSON]jsonコンバート. **/
	private static final void encodeObject(
		final StringBuilder buf, final Object base, Object target) {
		
		if(Undefined.instance.equals(target)) {
			target = null;
		} else if(target != null) {
			target = WrapUtil.unwrap(target);
		}
		if (target == null) {
			buf.append("null");
		} else if (target instanceof Map) {
			if(((Map)target).size() == 0) {
				buf.append("{}");
			} else {
				encodeJsonMap(buf, base, (Map) target);
			}
		} else if (target instanceof List) {
			if(((List)target).size() == 0) {
				buf.append("[]");
			} else {
				encodeJsonList(buf, base, (List) target);
			}
		} else if (target instanceof Number || target instanceof Boolean) {
			buf.append(target);
		} else if (target instanceof Character || target instanceof CharSequence) {
			buf.append("\"").append(target).append("\"");
		} else if (target instanceof byte[]) {
			buf.append("null");
		} else if (target instanceof char[]) {
			buf.append("\"").append(new String((char[]) target)).append("\"");
		} else if (target instanceof java.util.Date) {
			buf.append("\"").append(dateToString((java.util.Date) target)).append("\"");
		} else if (target.getClass().isArray()) {
			if (Array.getLength(target) == 0) {
				buf.append("[]");
			} else {
				encodeJsonArray(buf, base, target);
			}
		} else {
			buf.append("\"").append(target.toString()).append("\"");
		}
	}

	/** [encodeJSON]jsonMapコンバート. **/
	private static final void encodeJsonMap(final StringBuilder buf, final Object base, final Map map) {
		boolean flg = false;
		Map mp = (Map) map;
		Iterator it = mp.keySet().iterator();
		buf.append("{");
		while (it.hasNext()) {
			String key = (String) it.next();
			Object value = mp.get(key);
			if (base == value) {
				continue;
			}
			if (flg) {
				buf.append(",");
			}
			flg = true;
			buf.append("\"").append(key).append("\":");
			encodeObject(buf, base, value);
		}
		buf.append("}");
	}

	/** [encodeJSON]jsonListコンバート. **/
	private static final void encodeJsonList(final StringBuilder buf, final Object base, final List list) {
		boolean flg = false;
		List lst = (List) list;
		buf.append("[");
		int len = lst.size();
		for (int i = 0; i < len; i++) {
			Object value = lst.get(i);
			if (base == value) {
				continue;
			}
			if (flg) {
				buf.append(",");
			}
			flg = true;
			encodeObject(buf, base, value);
		}
		buf.append("]");
	}

	/** [encodeJSON]json配列コンバート. **/
	private static final void encodeJsonArray(final StringBuilder buf, final Object base, final Object list) {
		boolean flg = false;
		int len = Array.getLength(list);
		buf.append("[");
		for (int i = 0; i < len; i++) {
			Object value = Array.get(list, i);
			if (base == value) {
				continue;
			}
			if (flg) {
				buf.append(",");
			}
			flg = true;
			encodeObject(buf, base, value);
		}
		buf.append("]");
	}

	/** [decodeJSON]１つの要素を変換. **/
	private static final Object decodeJsonValue(String json) {
		try {
			return _decodeJsonValue(json);
		} catch(RhilaException re) {
			throw re;
		} catch(Exception e) {
			throw new RhilaException(500, e);
		}
	}
	
	/** [decodeJSON]１つの要素を変換. **/
	private static final Object _decodeJsonValue(String json)
		throws Exception {
		int len;
		// NULL文字.
		if(json == null || eq("null", json)) {
			return null;
		}
		// 空文字.
		else if ((len = json.length()) <= 0) {
			return "";
		}
		// BOOLEAN true.
		else if (eq("true", json)) {
			return Boolean.TRUE;
		}
		// BOOLEAN false.
		else if (eq("false", json)) {
			return Boolean.FALSE;
		}
		// 数値.
		else if (isNumeric(json)) {
			if (json.indexOf(".") != -1) {
				return Double.parseDouble(json);
			}
			return Long.parseLong(json);
		}
		// 文字列コーテーション区切り.
		else if ((json.startsWith("\"") && json.endsWith("\"")) || (json.startsWith("\'") && json.endsWith("\'"))) {
			json = json.substring(1, len - 1);
		}
		// ISO8601の日付フォーマットかチェック.
		else if (DateUtil.isISO8601(json)) {
			final java.util.Date d = stringToDate(json);
			if(d != null) {
				return d;
			}
		}
		// その他文字列.
		return json;
	}

	/** JSON_Token_解析処理 **/
	private static final List<Object> analysisJsonToken(String json) {
		int s = -1;
		char c;
		int cote = -1;
		int bef = -1;
		int len = json.length();
		List<Object> ret = new ArrayList<Object>();
		// Token解析.
		for (int i = 0; i < len; i++) {
			c = json.charAt(i);
			// コーテーション内.
			if (cote != -1) {
				// コーテーションの終端.
				if (bef != '\\' && cote == c) {
					ret.add(json.substring(s - 1, i + 1));
					cote = -1;
					s = i + 1;
				}
			}
			// コーテーション開始.
			else if (bef != '\\' && (c == '\'' || c == '\"')) {
				cote = c;
				if (s != -1 && s != i && bef != ' ' && bef != '　' && bef != '\t' && bef != '\n' && bef != '\r') {
					ret.add(json.substring(s, i + 1));
				}
				s = i + 1;
				bef = -1;
			}
			// ワード区切り.
			else if (c == '[' || c == ']' || c == '{' || c == '}' || c == '(' || c == ')' || c == ':' || c == ';'
					|| c == ',' || (c == '.' && (bef < '0' || bef > '9'))) {
				if (s != -1 && s != i && bef != ' ' && bef != '　' && bef != '\t' && bef != '\n' && bef != '\r') {
					ret.add(json.substring(s, i));
				}
				ret.add(new String(new char[] { c }));
				s = i + 1;
			}
			// 連続空間区切り.
			else if (c == ' ' || c == '　' || c == '\t' || c == '\n' || c == '\r') {
				if (s != -1 && s != i && bef != ' ' && bef != '　' && bef != '\t' && bef != '\n' && bef != '\r') {
					ret.add(json.substring(s, i));
				}
				s = -1;
			}
			// その他文字列.
			else if (s == -1) {
				s = i;
			}
			bef = c;
		}
		return ret;
	}

	/** Json-Token解析. **/
	private static final Object createJsonInfo(int[] n, List<Object> token, int type, int no, int len) {
		String value;
		StringBuilder before = null;
		// List.
		if (type == TYPE_ARRAY) {
			List<Object> ret = new ListScriptable();
			int flg = 0;
			for (int i = no + 1; i < len; i++) {
				value = (String) token.get(i);
				if (",".equals(value) || "]".equals(value)) {
					if ("]".equals(value)) {
						if (flg == 1) {
							if (before != null) {
								ret.add(decodeJsonValue(before.toString()));
							}
						}
						n[0] = i;
						return ret;
					} else {
						if (flg == 1) {
							if (before == null) {
								ret.add(null);
							} else {
								ret.add(decodeJsonValue(before.toString()));
							}
						}
					}
					before = null;
					flg = 0;
				} else if ("[".equals(value)) {
					ret.add(createJsonInfo(n, token, TYPE_ARRAY, i, len));
					i = n[0];
					before = null;
					flg = 0;
				} else if ("{".equals(value)) {
					ret.add(createJsonInfo(n, token, TYPE_MAP, i, len));
					i = n[0];
					before = null;
					flg = 0;
				} else {
					if (before == null) {
						before = new StringBuilder();
						before.append(value);
					} else {
						before.append(" ").append(value);
					}
					flg = 1;
				}
			}
			n[0] = len - 1;
			return ret;
		}
		// map.
		else if (type == TYPE_MAP) {
			Map<Object, Object> ret = new HashMap<Object, Object>();
			String key = null;
			for (int i = no + 1; i < len; i++) {
				value = (String) token.get(i);
				if (":".equals(value)) {
					if (key == null) {
						throw new RhilaException(500, "Map format is invalid(No:" + i + ")");
					}
				} else if (",".equals(value) || "}".equals(value)) {
					if ("}".equals(value)) {
						if (key != null) {
							if (before == null) {
								ret.put(key, decodeJsonValue(null));
							} else {
								ret.put(key, decodeJsonValue(before.toString()));
							}
						}
						n[0] = i;
						return ret;
					} else {
						if (key == null) {
							if (before == null) {
								continue;
							}
							throw new RhilaException(500, "Map format is invalid(No:" + i + ")");
						}
						if (before == null) {
							ret.put(key, decodeJsonValue(null));
						} else {
							ret.put(key, decodeJsonValue(before.toString()));
						}
						before = null;
						key = null;
					}
				} else if ("[".equals(value)) {
					if (key == null) {
						throw new RhilaException(500, "Map format is invalid(No:" + i + ")");
					}
					ret.put(key, createJsonInfo(n, token, TYPE_ARRAY, i, len));
					i = n[0];
					key = null;
					before = null;
				} else if ("{".equals(value)) {
					if (key == null) {
						throw new RhilaException(500, "Map format is invalid(No:" + i + ")");
					}
					ret.put(key, createJsonInfo(n, token, TYPE_MAP, i, len));
					i = n[0];
					key = null;
					before = null;
				} else if (key == null) {
					key = value;
					if ((key.startsWith("'") && key.endsWith("'")) || (key.startsWith("\"") && key.endsWith("\""))) {
						key = key.substring(1, key.length() - 1).trim();
					}
				} else {
					if (before == null) {
						before = new StringBuilder();
						before.append(value);
					} else {
						before.append(" ").append(value);
					}
				}
			}
			n[0] = len - 1;
			return ret;
		}
		// その他.
		throw new RhilaException(500, "Failed to parse JSON.");
	}
	
	/** 大文字、小文字関係なく比較. **/
	protected static final boolean eq(String a, String b) {
		if(a == null || b == null) {
			return false;
		}
		return a.toLowerCase().equals(b.toLowerCase());
	}
	/** 数値チェック. **/
	protected static final boolean isNumeric(String o) {
		return NumberUtil.isNumeric(o);
	}

	/** 日付を文字変換. **/
	protected static final String dateToString(java.util.Date d) {
		return DateUtil.toISO8601(d);
	}
	/** 文字を日付変換. **/
	protected static final java.util.Date stringToDate(String s) {
		try {
			return DateUtil.parseDate(s);
		} catch(Exception e) {
			return null;
		}
	}
}
