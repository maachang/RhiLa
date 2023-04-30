package rhila.lib.http;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import rhila.lib.DateUtil;
import rhila.lib.ObjectUtil;
import rhila.scriptable.LowerKeyMapScriptable;

/**
 * Httpヘッダ.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public final class HttpHeader implements Map<String, String> {
    // lambda snapStart CRaC用.
    protected static final HttpHeader LOAD_CRAC = new HttpHeader();
    
	// headerValue.
	private final LowerKeyMapScriptable values =
		new LowerKeyMapScriptable();
	// cookieValue.
	private LowerKeyMapScriptable cookieValue = null;
	
	////////////
	// map実装.
	////////////

	@Override
	public int size() {
		return values.size();
	}

	@Override
	public boolean isEmpty() {
		return values.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return values.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return values.containsValue(value);
	}

	@Override
	public String get(Object key) {
		return (String)values.get(key);
	}

	@Override
	public String put(String key, String value) {
		return (String)values.put(key, value);
	}

	@Override
	public String remove(Object key) {
		return (String)values.remove(key);
	}

	@Override
	public void putAll(Map<? extends String, ? extends String> m) {
		values.putAll(m);
	}

	@Override
	public void clear() {
		values.clear();
	}

	@Override
	public Set<String> keySet() {
		return (Set)values.keySet();
	}

	@Override
	public Collection<String> values() {
		return (Collection)values.values();
	}

	@Override
	public Set<Entry<String, String>> entrySet() {
		return (Set)values.entrySet();
	}
	
	//////////////////
	// httpHeader実装.
	//////////////////
	
	// コンテンツタイプを取得.
	public String getContentType() {
		return get("content-type");
	}
	
	// コンテンツタイプを設定.
	public void setContentType(String value) {
		put("content-type", value);
	}
	
	// cookie情報を取得.
	private final LowerKeyMapScriptable cookie() {
		if(cookieValue == null) {
			cookieValue = new LowerKeyMapScriptable();
		}
		return cookieValue;
	}
	
	// cookieをすべて削除.
	public void clearCookie() {
		cookieValue = null;
	}
	
	// cookie情報を取得.
	//  {value: value, "Max-Age": 2592000, Secure: true}
	//  のような返却値.
	public LowerKeyMapScriptable getCookie(String key) {
		if(cookieValue == null) {
			return null;
		}
		return (LowerKeyMapScriptable)cookie().get(key);
	}
	
	// cookie内容 "key=value" をパース.
	// out {"key": "value"} がセットされます.
	// value "key=value" のような条件を設定します.
	private static final void parseCookie(
		LowerKeyMapScriptable out, String value) {
	    int p = value.indexOf("=");
	    if(p == -1) {
	    	out.put(value, true);
	    } else {
	        out.put(value.substring(0, p),
	        	value.substring(p + 1));
	    }
	}
	
	// cookie内容を設定.
	//   value="value; Max-Age=2592000; Secure;"
	// ※必ず先頭文字は "value;" 必須.
	// または
	//   value={value: value, "Max-Age": 2592000, Secure: true}
	// のような感じで設定します.
	public boolean putCookie(String key, Object value) {
        // 文字の場合.
        if(value instanceof String) {
        	LowerKeyMapScriptable v = new LowerKeyMapScriptable();
            // 文字列から LowerKeyMapScriptable に変換.
            String n;
            final String[] list = ((String)value).split(";");
            final int len = list.length;
            for(int i = 0; i < len; i ++) {
                n = list[i].trim();
                if(i == 0) {
                    v.put("value", n);
                } else {
                    parseCookie(v, n);
                }
            }
            cookie().put(key, v);
            return true;
        // Mapの場合.
        } else if(value instanceof Map) {
        	LowerKeyMapScriptable v = new LowerKeyMapScriptable();
        	Entry e;
        	Object o;
        	Map mp = (Map)value;
        	Iterator<Entry> it = mp.entrySet().iterator();
        	while(it.hasNext()) {
        		e = it.next();
        		o = e.getValue();
        		if(o instanceof Date) {
        			v.put(String.valueOf(e.getKey()),
        				DateUtil.toRfc822((Date)o));
        		} else {
        			v.put(String.valueOf(e.getKey()),
        				String.valueOf(o));
        		}
        	}
            cookie().put(key, v);
            return true;
        }
        return false;
	}
	
	// 指定cookieを削除.
	public boolean removeCookie(String key) {
		if(cookieValue == null) {
			return false;
		}
		cookieValue.remove(key);
		return true;
	}
	
    // 登録されたCookie情報をレスポンス用headerに設定.
    // 戻り値: cookieリストが返却されます.
    public final String[] toCookies() {
        // "cookies": [....];
		if(cookieValue == null) {
			return new String[0];
		}
		int p = 0;
    	int cookieLen = cookieValue.size();
        final String[] ret = new String[cookieLen];
        Map em;
        String value;
        Entry<String, Object> e;
        Iterator<Entry<String, Object>> it = cookieValue.entrySet().iterator();
        while(it.hasNext()) {
        	e = it.next();
        	em = (Map)e.getValue();
            // 最初の条件は key=value条件.
            value = ObjectUtil.encodeURIComponent(e.getKey()) +
                "=" + ObjectUtil.encodeURIComponent((String)em.get("value"));
            // cookie要素を連結.
            Iterator<Entry<String, Object>> itn = em.entrySet().iterator();
            while(itn.hasNext()) {
            	e = itn.next();
        		// valueは無視.
            	if("value".equals(e.getKey())) {
            		continue;
            	} else if(e.getValue() instanceof Boolean) {
            		value += "; " + ObjectUtil.encodeURIComponent(e.getKey());
            	} else {
                    value += "; " + ObjectUtil.encodeURIComponent(e.getKey()) +
                        "=" +  ObjectUtil.encodeURIComponent((String)e.getValue());
            	}
            }
            ret[p ++] = value;
        }
        return ret;
    }

	
	
}
