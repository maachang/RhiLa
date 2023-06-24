package rhila.lib;

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
	
}
