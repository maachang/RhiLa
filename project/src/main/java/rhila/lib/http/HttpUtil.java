package rhila.lib.http;

import java.util.Map;

import rhila.RhilaException;
import rhila.lib.NumberUtil;
import rhila.lib.ObjectUtil;
import rhila.scriptable.BinaryScriptable;
import rhila.scriptable.MapScriptable;

// httpユーティリティ.
public class HttpUtil {
	protected HttpUtil() {}
	
	// URLを作成.
	public static final String createURL(String host, String path) {
		// url のプロトコルが http:// or https:// であるかチェック.
		int p = host.indexOf("://");
		if(p == -1) {
			host = "https://" + host;
			p = 4;
		}
		// プロトコル＋ドメイン名＋ポート番号までを取得.
		final int pp = host.indexOf("/", p + 3);
		if(pp == -1) {
			return host + (!path.startsWith("/") ? "/" : "") + path;
		}
		return host.substring(0, pp) +
			(!path.startsWith("/") ? "/" : "") + path;
	}
	
	// URLをパース.
	public static final String[] parseUrl(String url) {
		int b = 0;
		int p = url.indexOf("://");
		if (p == -1) {
			return null;
		}
		String protocol = url.substring(0, p);
		String domain = null;
		String path = null;
		String port = "http".equals(protocol) ?
			"80" : "443";
		b = p + 3;
		p = url.indexOf(":", b);
		int pp = url.indexOf("/", b);
		if (p == -1) {
			if (pp == -1) {
				domain = url.substring(b);
				path = "/";
			} else {
				domain = url.substring(b, pp);
				path = url.substring(pp);
			}
		} else if (pp == -1) {
			domain = url.substring(b, p);
			port = url.substring(p + 1);
			path = "/";
		} else if (p < pp) {
			domain = url.substring(b, p);
			port = url.substring(p + 1, pp);
			path = url.substring(pp);
		} else {
			domain = url.substring(b, p);
			path = url.substring(p);
		}
		if (!NumberUtil.isNumeric(port)) {
			throw new RhilaException(
				"Port number is not a number: " +
				port);
		}
		return new String[] { protocol, domain, port, path };
	}
	
	// methodの整理.
	public static final String trimMethod(String method) {
		return (method != null) ? method.toUpperCase() : "GET";
	}
	
	// bodyを文字列変換.
	public static final String bodyString(Object body) {
		if(ObjectUtil.isNull(body)) {
			return null;
		}
		try {
			if(body instanceof String) {
				return (String)body;
			} else if(body instanceof byte[]) {
				return new String((byte[])body, "UTF8");
			} else if(body instanceof BinaryScriptable) {
				return ((BinaryScriptable)body).convertString();
			}
		} catch(RhilaException re) {
			throw re;
		} catch(Exception e) {
			throw new RhilaException(e);
		}
		throw new RhilaException(
			"The conversion target is not binary: " + body.getClass().getName());
	}

	// MethodがGETやDELETEの場合、URLに対してFormDataを付与.
	public static final String appendUrlParams(
		String url, Map<String, Object> options) {
		String method = trimMethod((String)options.get("method"));
		Object body = options.get("body");
		if(!ObjectUtil.isNull(body) &&
			("GET".equals(method) || "DELETE".equals(method))) {
			if(url.indexOf("?") == -1) {
				return url + "?" + bodyString(body);
			} else {
				return url + "&" + bodyString(body);
			}
		}
		return url;
	}
	
	// URLパラメータの解析処理.
	public static final MapScriptable decodeHttpUrlParams(
		String body, String charset) {
		String k;
		boolean loop = true;
		int p, b = 0, n = 0;
		MapScriptable ret = new MapScriptable();
		while (loop) {
			// &が見つからない.
			if ((n = body.indexOf("&", b)) == -1) {
				loop = false;
				k = body.substring(b);
			// nextが存在する場合.
			} else {
				k = body.substring(b, n);
			}
			if ((p = k.indexOf("=")) == -1) {
				b = n + 1;
				continue;
			}
			if (k.indexOf("%") != -1) {
				ret.put(ObjectUtil.decodeURIComponent(k.substring(0, p), charset),
						ObjectUtil.decodeURIComponent(k.substring(p + 1), charset));
			} else {
				ret.put(k.substring(0, p),
					ObjectUtil.decodeURIComponent(k.substring(p + 1), charset));
			}
			b = n + 1;
		}
		return ret;
	}
	
	// content-typeに対する文字コードを取得.
	public static final String getContentTypeToCharset(String contentType) {
		if(contentType == null) {
			return null;
		}
		int p, pp;
		p = contentType.indexOf(";");
		if(p == -1) {
			return null;
		}
		p = contentType.indexOf("charset", p + 1);
		if(p == -1) {
			return null;
		}
		p = contentType.indexOf("=", p + 7);
		if(p == -1) {
			return null;
		}
		pp = contentType.indexOf(";", p + 1);
		if(pp == -1) {
			return contentType.substring(p + 1).trim();
		}
		return contentType.substring(p + 1, pp).trim();
	}
}
