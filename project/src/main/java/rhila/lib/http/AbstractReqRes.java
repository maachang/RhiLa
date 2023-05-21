package rhila.lib.http;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import rhila.RhilaException;
import rhila.lib.Base64;
import rhila.lib.DateUtil;
import rhila.lib.JsValidate;
import rhila.lib.Json;
import rhila.lib.ObjectUtil;
import rhila.scriptable.AbstractRhinoFunction;
import rhila.scriptable.BinaryScriptable;
import rhila.scriptable.MapScriptable;

/**
 * HttpRequest/HttpResponse共通部分の処理.
 */
@SuppressWarnings("unchecked")
abstract class AbstractReqRes<T> extends AbstractRhinoFunction {
	
	// HTTPバージョン.
	protected static final String HTTP_VERSION = "HTTP/";
	
	// HttpVersion.
	protected String httpVersion = "1.1";
	
	// HttpHeader.
	protected HttpHeader header = null;
	
	// body.
	protected BinaryScriptable body = null;
	
	// staticFlag.
	protected boolean staticFlag = false;
	
	// HttpHeaderを取得.
	public HttpHeader getHeader() {
		if(header == null) {
			header = new HttpHeader();
		}
		return header;
	}
	
	// HttpVersionを取得.
	public String getHttpVersion() {
		return httpVersion;
	}
	
	// HttpVersionを設定.
	public T setHttpVersion(String version) {
		if(version == null) {
			version = "1.1";
		}
		this.httpVersion = version;
		return (T)this;
	}
	
	// bodyを取得.
	public BinaryScriptable getBody() {
		return body;
	}
	
	// body内容を文字列で取得.
	public String getBodyToString() {
		if(body == null) {
			return null;
		}
		// HttpHeaderの文字コードを取得.
		String charset = header.getCharset();
		if(charset == null) {
			// 取得出来ない場合はUTF8.
			charset = "UTF8";
		}
		// 文字変換.
		return body.convertString(charset);
	}
	
	// body内容をJSON形式で取得.
	public Object getBodyToJSON() {
		if(body == null) {
			return null;
		}
		return Json.decode(getBodyToString());
	}
	
	// body内容をForm形式で取得.
	public Map<String, Object> getBodyToForm() {
		if(body == null) {
			return null;
		}
		String value = getBodyToString();
		String charset = header.getCharset();
		MapScriptable ret = new MapScriptable();
		boolean loop = true;
		int b = 0, p, pp;
		while(loop) {
			if((p = value.indexOf("&", b)) == -1) {
				p = value.length();
				loop = false;
			}
			pp = value.indexOf("=", b);
			if(b == -1) {
				break;
			}
			ret.put(
				ObjectUtil.decodeURIComponent(
						value.substring(b, pp).trim(),
					charset),
				ObjectUtil.decodeURIComponent(
						value.substring(pp + 1, p).trim(),
					charset));
			b = p + 1;
		}
		return ret;
	}
	
	// body設定内容をクリア.
	public T clearBody() {
		// binaryをクリア.
		this.body = null;
		// content-lengthをクリア.
		this.header.removeHeader("content-length");
		// content-typeをクリア.
		this.header.removeHeader("content-type");
		return (T)this;
	}
	
	// base64形式のbodyをセット.
	public T setBodyToBase64(String body, String mimeType) {
		JsValidate.noArgsToError(body);
		// binaryをセット.
		BinaryScriptable b = new BinaryScriptable(
			Base64.decode(body));
		return setBody(b, mimeType);
	}
	
	// bodyをセット.
	public T setBody(BinaryScriptable body) {
		return setBody(body, null);
	}
	
	// bodyをセット.
	public T setBody(String body) {
		return setBody(body, null);
	}
	
	// bodyをセット.
	public T setBody(String body, String mimeType) {
		JsValidate.noArgsToError(body);
		// binaryをセット.
		BinaryScriptable b = new BinaryScriptable(body);
		return setBody(b, mimeType);
	}
	
	// bodyをセット.
	public T setBody(byte[] body) {
		return setBody(body, null);
	}
	
	// bodyをセット.
	public T setBody(byte[] body, String mimeType) {
		JsValidate.noArgsToError(body);
		// binaryをセット.
		BinaryScriptable b = new BinaryScriptable(body);
		return setBody(b, mimeType);
	}
	
	// formデータの送信.
	@SuppressWarnings("rawtypes")
	public T setBodyToForm(Object body) {
		JsValidate.noArgsToError(body);
		if(body instanceof String) {
			// 文字列の場合.
			return setBody((String)body, MimeType.FORM_DATA);
		} else if(!(body instanceof Map)) {
			// 文字列とMap以外の場合はエラー.
			throw new RhilaException(
				"Form definition of Body can not be set except character " +
				"string or Map definition.");
		}
		// Map処理.
		Map form = (Map)body;
		if(form.size() == 0) {
			// データが存在しない場合.
			return setBody("", MimeType.FORM_DATA);
		}
		// Mapを"Key=Value&Key=Value..."文字列に変換.
		StringBuilder buf = new StringBuilder();
		Object v;
		Entry e = null;
		Iterator<Entry> it = form.entrySet().iterator();
		while(it.hasNext()) {
			e = it.next();
			if(e.getKey() == null) {
				continue;
			} else if(buf.length() != 0) {
				buf.append("&");
			}
			// keyをセット.
			buf.append(ObjectUtil.encodeURIComponent(
				"" + e.getKey())).append("=");
			// valueをセット.
			if((v = e.getValue()) == null) {
				// 空文字の場合無視.
				continue;
			} else if(v instanceof String || v instanceof Number) {
				// 数字か文字列の場合.
				buf.append(ObjectUtil.encodeURIComponent(
					v.toString()));
			} else if(v instanceof Date) {
				// 日付情報.
				buf.append(ObjectUtil.encodeURIComponent(
						DateUtil.toISO8601((Date)v)));
			} else if(v instanceof BinaryScriptable) {
				// binaryの場合はBase64.
				buf.append(((BinaryScriptable)v).toBase64());
			}
			// それ以外は空でセット.
		}
		// formデータセット.
		return setBody(buf.toString(), MimeType.FORM_DATA);
	}
	
	// json内容を設定.
	public T setBodyToJSON(Object json) {
		JsValidate.noArgsToError(body);
		setBody(Json.encode(json), MimeType.JSON);
		return (T)this;
	}
	
	// base64形式のbodyをセット.
	public T setBodyToBase64(String body) {
		return setBodyToBase64(body, null);
	}
	
	// bodyをセット.
	public T setBody(BinaryScriptable body, String mimeType) {
		JsValidate.noArgsToError(body);
		// binaryをセット.
		this.body = body;
		// content-lengthをセット.
		this.header.setContentLength(this.body.size());
		// mimeTypeが設定されている場合.
		if(mimeType != null) {
			getHeader().setContentType(mimeType);
		}
		return (T)this; 
	}
		
	// Body: plainセット.
	protected static final int TYPE_PLAIN = 0;
	// Body: FORMセット.
	protected static final int TYPE_FORM = 1;
	// Body: JSONセット.
	protected static final int TYPE_JSON = 2;
	// Body: Base64セット.
	protected static final int TYPE_BASE64 = 3;
	
	// bodyをObjectとしてセット.
	public T setBodyToObject(Object body) {
		return setBodyToObject(body, null);
	}
	
	// bodyをObjectとしてセット.
	public T setBodyToObject(Object body, String mime) {
		return setBodyToObject(TYPE_PLAIN, body, mime);
	}
	
	// bodyをObjectとしてセット.
	@SuppressWarnings("rawtypes")
	protected T setBodyToObject(int type, Object body, String mime) {
		if(body instanceof String) {
			if(type == TYPE_BASE64) {
				setBodyToBase64((String)body, mime);
			} else if(type == TYPE_FORM) {
				setBodyToForm(body);
			} else {
				setBody((String)body, mime);
			}
			return (T)this;
		} else if(body instanceof byte[] ||
			body instanceof BinaryScriptable) {
			if(body instanceof BinaryScriptable) {
				body = ((BinaryScriptable)body).getRaw();
			}
			if(type == TYPE_BASE64) {
				try {
					setBodyToBase64(
						new String((byte[])body, "UTF8"), mime);
				} catch(Exception e) {
					throw new RhilaException(e);
				}
			} else if(type == TYPE_FORM) {
				try {
					setBodyToForm(new String((byte[])body, "UTF8"));
				} catch(Exception e) {
					throw new RhilaException(e);
				}
			} else {
				setBody((byte[])body, mime);
			}
			return (T)this;
		} else if(body instanceof Map) {
			if(type == TYPE_JSON) {
				setBodyToJSON(body);
			} else {
				setBodyToForm((Map)body);
			}
			return (T)this;
		} else if(type == TYPE_JSON) {
			setBodyToJSON(body);
			return (T)this;
		}
		throw new RhilaException(
				"Body Cannot be set due to condition: " +
					body.getClass().getName());
	}
	
	// bodyをargsパラメータでセット.
	protected static final void setBodyByArgs(
		int type, AbstractReqRes<?> req, Object[] args) {
		JsValidate.noArgsToError(args);
		try {
			Object body = null;
			String mime = null;
			if(args.length >= 1) {
				body = args[0];
			}
			if(args.length >= 2) {
				if(args[1] instanceof String) {
					mime = (String)args[1];
				}
			}
			req.setBodyToObject(type, body, mime);
		} catch(RhilaException re) {
			throw re;
		} catch(Exception e) {
			throw new RhilaException(e);
		}
	}
	
	// 指定Keyのヘッダが未設定の場合は設定する.
	protected T setDefaultHeader(String key, String value) {
		if(getHeader().getHeader(key) == null) {
			getHeader().setHeader(key, value);
		}
		return (T)this;
	}
	
	// body情報をGZIP変換.
	public T bodyToGzip() {
		if(body == null) {
			throw new RhilaException(
				"Target body information does not exist.");
		}
		body.toGzip();
		getHeader().setContentLength(body.size());
		getHeader().setHeader("content-encoding" , "gzip");
		return (T)this;
	}
	
	// body情報をUnGZIP変換.
	public T bodyToUnGzip() {
		if(body == null) {
			throw new RhilaException(
				"Target body information does not exist.");
		}
		getHeader().setContentLength(body.size());
		getHeader().removeHeader("content-encoding");
		return (T)this;
	}
}
