package rhila.lib;

import org.mozilla.javascript.Function;

import rhila.RhilaException;
import rhila.core.Console;
import rhila.core.Global;
import rhila.core.GlobalFactory;
import rhila.core.RunScript;
import rhila.lib.http.HttpRequest;
import rhila.lib.http.HttpResponse;
import rhila.lib.http.MimeType;
import rhila.scriptable.MapScriptable;

// 埋め込みhtmlをjsに変換.
public class JHtml {
	
	// 基本的にこれと同じ仕様にする.
	// https://github.com/maachang/LFU/blob/main/src/lib/jhtml.js
	//
	// - jhtml組み込みタグ説明.
	//  <% ... %>
	//    基本的な組み込みタグ情報
	//  <%= ... %>
	//    実行結果をhtmlとして出力する組み込みタグ.
	//  <%# ... %>
	//    コメント用の組み込みタグ.
	//  ${ ... }
	//    実行結果をテンプレートとして出力する組み込みタグ.
	//    ただ利用推奨としては、変数出力時に利用する.
	//
	//- jhtml組み込み機能.
	//  $out = function(string)
	//    stringをhtmlとして出力するFunction.
	//  $params = MapScriptable
	//    getまたはpostで渡されたパラメータ情報.
	//    - getパラメータの場合 {key: value} のような形で格納される.
	//    - postパラメータの場合 `application/x-www-form-urlencoded`の
	//      場合は {key: value} のような形で格納される.
	//      また`application/json` の場合は、JSONで渡された内容が格納される.
	//  $request = HttpRequest.
	//    HttpRequestオブジェクト.
 	//  $response = httpHeader.
	//    HttpResponseオブジェクト.
	//
	
	private static final String OUT = "$out";
	
	// クォーテーションに対するインデントの増減を行う.
	// string 対象の文字列を設定します.
	// dc [true]の場合は["], [false]の場合は['].
	// 戻り値: 変換された内容が返却されます.
	private static final String indentQuote(String string, boolean dc) {
	    int len = string.length();
	    if (len == 0) {
	        return "";
	    }
	    char c, target;
	    int j, yenLen;
	    target = (dc) ? '\"' : '\'';
	    yenLen = 0;
	    StringBuilder buf = new StringBuilder(
	    	(int)(len * 1.25));
	    for (int i = 0; i < len; i++) {
	        if ((c = string.charAt(i)) == target) {
	            if (yenLen > 0) {
	                yenLen <<= 1;
	                for (j = 0; j < yenLen; j++) {
	                    buf.append("\\");
	                }
	                yenLen = 0;
	            }
	            buf.append("\\").append(target);
	        } else if ('\\' == c) {
	            yenLen ++;
	        } else {
	            if (yenLen != 0) {
	                for (j = 0; j < yenLen; j++) {
	                    buf.append("\\");
	                }
	                yenLen = 0;
	            }
	            buf.append(c);
	        }
	    }
	    if (yenLen != 0) {
	        for (j = 0; j < yenLen; j++) {
	        	buf.append("\\");
	        }
	    }
	    return buf.toString();
	}
	
	// 改行に対するインデントの増減を行う.
	// string 対象の文字列を設定します.
	// 戻り値: 変換された内容が返却されます.
	private static final String indentEnter(String s) {
	    int len = s.length();
	    if (len == 0) {
	        return "";
	    }
	    char c;
	    StringBuilder buf = new StringBuilder(
	    	(int)(len * 1.25));
	    for(int i = 0; i < len; i++) {
	        if((c = s.charAt(i)) == '\n') {
	            buf.append("\\n");
	        } else {
	            buf.append(c);
	        }
	    }
	    return buf.toString();
	}

	// ${ ... } を <% ... %>変換する.
	// jhtml 変換対象のjhtml内容を設定します.
	// 戻り値: 変換された内容が返却されます.
	private static final String analysis_D_braces(String jhtml) {
	    int len = jhtml.length();
	    if(len == 0) {
	    	return "";
	    }
	    char c;
	    int qt, pos, braces;
	    boolean by = false;
	    by = false;
	    pos = -1;
	    qt = -1;
	    braces = 0;
	    StringBuilder buf = new StringBuilder(
	    	(int)(len * 1.25));
	    for(int i = 0; i < len; i ++) {
	        c = jhtml.charAt(i);

	        // ${ 検出中
	        if(pos != -1) {
	            // クォーテーション内.
	            if(qt != -1) {
	                // 今回の文字列が対象クォーテーション終端.
	                if(!by && qt == c) {
	                    qt = -1;
	                }
	            // クォーテーション開始.
	            } else if(c == '\"' || c == '\'') {
	                qt = c;
	            // 波括弧開始.
	            } else if(c == '{') {
	                braces ++;
	            // 波括弧終了.
	            } else if(c == '}') {
	                braces --;
	                // 波括弧が終わった場合.
	                if(braces == 0) {
	                    // <%= ... %> に置き換える.
	                    buf.append("<%=")
	                    	.append(jhtml.substring(pos + 2, i))
	                    	.append("%>");
	                    pos = -1;
	                }
	            }
	        // ${ ... }の開始位置を検出.
	        } else if(c == '$' && i + 1 < len &&
	        	jhtml.charAt(i + 1) == '{') {
	            pos = i;
	        // それ以外.
	        } else {
	            buf.append(c);
	        }
	        // 円マークの場合.
	        by = (c == '\\');
	    }
	    return buf.toString();
	}

	// jhtmlを解析して実行可能なjs変換を行う.
	// jhtml 対象のjhtmlを設定します.
	// 戻り値: 実行可能なjs形式の情報が返却されます.
	protected static final String analysisJHtml(String jhtml) {
	    int len = jhtml.length();
	    if(len == 0) {
	    	return "";
	    }
		char c, cc;
		String n;
		int start, bef;
	    bef = 0;
	    start = -1;
	    StringBuilder buf = new StringBuilder(
	    	(int)(len * 1.5));
	    for(int i = 0; i < len; i ++) {
	        c = jhtml.charAt(i);
	        if(start != -1) {
	            if(c == '%' && i + 1 < len &&
	            	jhtml.charAt(i + 1) == '>') {
	                if(buf.length() != 0) {
	                    buf.append("\n");
	                }
	                n = jhtml.substring(bef, start);
	                n = indentEnter(n);
	                n = indentQuote(n, true);
	                // HTML部分を出力.
	                buf.append(OUT).append("(\"")
	                	.append(n).append("\");\n");
	                bef = i + 2;
	                
	                // 実行処理部分を実装.
	                cc = jhtml.charAt(start + 2);
	                if(cc == '=') {
	                    // 直接出力.
	                    n = jhtml.substring(start + 3, i).trim();
	                    if(n.endsWith(";")) {
	                        n = n.substring(0, n.length() - 1).trim();
	                    }
	                    buf.append(OUT).append("(")
	                    	.append(n).append(");\n");
	                } else if(cc == '#') {
	                    // コメントなので、何もしない.
	                } else {
	                    // 出力なしの実行部分.
	                    buf.append(jhtml.substring(start + 2, i).trim())
	                    	.append("\n");
	                }
	                start = -1;
	            }
	        } else if(c == '<' && i + 1 < len &&
	        	jhtml.charAt(i + 1) == '%') {
	            start = i;
	            i += 1;
	        }
	    }
	    // のこりのHTML部分を出力.
	    n = jhtml.substring(bef);
	    n = indentEnter(n);
	    n = indentQuote(n, true);
	    // HTML部分を出力.
	    buf.append(OUT).append("(\"").append(n).append("\");\n");
	    
	    return buf.toString();
	}
	
	// ￥r￥nを ￥nに変換.
	// s 対象の文字列を設定します.
	// 戻り値: 変換された内容が返却されます.
	protected static final String convertYrYnToYn(String s) {
	    char c;
	    int len = s.length();
	    StringBuilder buf = new StringBuilder(
	    	(int)(len * 1.25));
	    for(int i = 0; i < len; i ++) {
	        if((c = s.charAt(i)) != '\r') {
	            buf.append(c);
	        }
	    }
	    return buf.toString();
	}
	
	// jhtmlをjsに変換.
	// jhtml 対象のjhtmlを設定します.
	// 戻り値: 実行可能なjs形式の情報が返却されます.
	public static final String convertJhtmlToJs(String jhtml) {
	    return analysisJHtml(
	        analysis_D_braces(
	            convertYrYnToYn(jhtml)
	        )
	    );
	}
	
	// jhtml実行js用実行パラメータ.
	private static final String JHTML_JS_ARGS =
	    OUT + ",$params,$request,$response";

	// jhtml実行js用ヘッダ.
	private static final String JHTML_JS_HEADER =
	    "(function() {'use strict';" +
	    "return function(" + JHTML_JS_ARGS + "){\n";

	// jhtml実行js用フッダ.
	private static final String JHTML_JS_FOODER =
	    "\n}})();";

	// jhtmlを実行.
	// request 対象のリクエスト情報を設定します.
	// response 対象のレスポンスを設定します.
	// name jhtmlのファイルパスを設定します.
	// js jhtmlを変換してjsに置き換えた内容を設定します.
	public static final void executeJhtml(
		HttpRequest request, HttpResponse response,
		String name, String js) {
	    // jhtml実行JSのスクリプトを生成.
	    js = new StringBuilder(js.length() + 128)
	    	.append(JHTML_JS_HEADER).append(js)
	    	.append(JHTML_JS_FOODER).toString();
	    try {
	    	// パラメータを取得.
	    	MapScriptable params = request.getParameter();
	    	// globalオブジェクトを取得.
	    	Global global = GlobalFactory.getGlobal();
	    	
	    	// jhtmlをjs変換したスクリプトを実行して
	    	// functionを取得.
	    	Object o = RunScript.eval(global, js, name, 0);
	    	// 戻り値がrhinoのfunctionじゃない場合はエラー.
	    	if(o instanceof Function) {
	    		throw new RhilaException(
	    			"js execution of execution jhtml is illegal: " +
	    			name);
	    	}
	    	
	    	// 文字列返却用オブジェクトをセット.
	    	StringBuilder out = new StringBuilder(
	    		(int)(js.length() * 1.25));
	    	
	    	// jhtml用のfunctionを実行.
	    	((Function)o).call(global.getContext(), global, null, new Object[] {
	    		out, params, request, response
	    	});
	    	
	        // コンテンツタイプが設定されていない場合.
	        if(ObjectUtil.isNull(response.getHeader().getContentType())) {
	            // htmlのmimeTypeをセット.
	            response.getHeader().setContentType(MimeType.HTML);
	        }
	        // bodyが設定されていない場合.
	        if(ObjectUtil.isNull(response.getBody())) {
	        	// bodyセット.
	        	response.setBody(out.toString());
	        	out = null;
	        }
	    } catch(Exception e) {
	        Console.log("## [ERROR] executeJHTML name: " + name);
	        if(e instanceof RhilaException) {
	        	throw (RhilaException)e;
	        }
	        throw new RhilaException(e);
	    }
	}
	
	// jhtmlファイル指定の場合、対象のパスを返却.
	// path パスを設定します.
	// 戻り値: パスが.jhtml拡張子の場合 .js.htmlに変換されます.
	public static final String convertJhtmlPath(String path) {
	    if(path.toLowerCase().endsWith(".jhtml")) {
	        return path.substring(
	            0, path.length() - 6) + ".js.html";
	    }
	    return path;
	}

	// 拡張子が .js.html かチェック.
	// path 対象のパスを設定します.
	// 戻り値: 拡張子が .js.html の場合 true.
	public static final boolean isJsHTML(String path) {
	    return path.endsWith(".js.html");
	}
	 
	// jhtmlを実行.
	// path 拡張子が .js.html のパスが返却されます.
	// jhtml 対象のJHTML読み込み文字列が設定されます.
	// request 対象のリクエスト情報を設定します.
	// response 対象のレスポンスを設定します.
	public static final void execJHTML(
		HttpRequest request, HttpResponse response,
		String path, String jhtml) {
	    // jhtmlからjsに変換処理.
	    String js = analysisJHtml(jhtml);
	    jhtml = null;
	    // js実行.
	    executeJhtml(request, response, path, js);
	}
}
