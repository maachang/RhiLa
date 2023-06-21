package rhila.scriptable;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import rhila.RhilaException;
import rhila.lib.ArrayMap;

/**
 * [js]Rhino用のCustom用のObjectをnewするためのabstract class.
 * 
 * rhinoの利用で
 *   > new CustomObject(....);
 * を実行して、それぞれFunctionコール可能なオブジェクト実装用の
 * 継承クラスとして利用します.
 */
public abstract class AbstractRhinoCustomNewInstance<T>
	extends AbstractRhinoFunction {
	
	// instance可能なFunction定義.
	protected static ArrayMap<String, Scriptable> instanceList;
	
	// objectインスタンスリスト.
	protected ArrayMap<String, Scriptable> objInsList;
	
	// staticFlag.
	// 初期値はstaticFlagはOFF.
	// staticFlagがONのオブジェクトだけがnewInstanceできる.
	// statifFlagがOFFのオブジェクトだけ、提供Function群が利用できる.
	// 非常に重要な条件だが、普通にinitFunctionsで主Objectを初期化
	// すれば、良い形なのでその形で実装するだけで良い.
	protected boolean staticFlag = false;
	
	// [js]FunctionList.
	@SuppressWarnings("rawtypes")
	private static final class FunctionList
		extends AbstractRhinoFunctionInstance {
	    // lambda snapStart CRaC用.
		@SuppressWarnings("unused")
		protected static final FunctionList LOAD_CRAC =
			new FunctionList();
		
		private int type;
		private String typeString;
		private AbstractRhinoCustomNewInstance object;
		
		// コンストラクタ.
		private FunctionList() {}
		
		// コンストラクタ.
		private FunctionList(int type, AbstractRhinoCustomNewInstance o) {
			this.type = type;
			this.typeString = o.getFunctionNames()[type];
			this.object = o;
		}
		
		// 新しいインスタンスを生成.
		@Override
		public final Scriptable getInstance(Object... args) {
			// 個々のFunction用のインスタンスを作成.
			FunctionList ret = new FunctionList
				(type, (AbstractRhinoCustomNewInstance)args[0]);
			return ret;
		}
		
		@Override
		public String getName() {
			return typeString;
		}
		
		// メソッド実行.
		@Override
		public Object function(
			Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			// 実行処理.
			return object.callFunction(type, args);
		}
	}
	
	// function初期化.
	protected boolean initFunctions() {
		return initFunctions(true);
	}
	
	// function初期化.
	// staticFlagがtrueの場合 new CustomObjectの母体として定義.
	// staticFlagがfalseの場合 CustomObject,functionで利用できるようになる.
	protected boolean initFunctions(boolean staticFlag) {
		// 既に初期化済みの場合.
		if(instanceList != null) {
			// 処理しない.
			return false;
		}
		// この処理で呼び出すオブジェクトはstaticFlagをONにする.
		setStaticFlag(staticFlag);
		// 配列で直接追加.
		String[] functionList = getFunctionNames();
		final int len = functionList.length * 2;
		Object[] list = new Object[len];
		for(int i = 0, j = 0; i < len; i += 2, j ++) {
			list[i] = functionList[j];
			list[i + 1] = new FunctionList(j, this);
		}
		instanceList = new ArrayMap<String, Scriptable>(list);
		return true;
	}
	
	// function取得.
	protected Object getFunction(String name) {
		if(staticFlag) {
			// staticフラグがONの場合.
			// オブジェクト名を返却する.
			return getObjectName();
		}
		// objectInstance内のFunction管理が存在しない場合.
		if(objInsList == null) {
			// 生成する.
			objInsList = new ArrayMap<String, Scriptable>();
		}
		// オブジェクト管理の生成Functionを取得.
		Scriptable ret = objInsList.get(name);
		// 存在しない場合.
		if(ret == null) {
			// static管理のFunction定義を取得.
			ret = instanceList.get(name);
			// 存在する場合.
			if(ret != null) {
				// オブジェクト管理の生成Functionとして管理.
				ret = ((AbstractRhinoFunctionInstance)ret)
					.getInstance(this);
				objInsList.put(name, ret);
			}
		}
		return ret;
	}
	
	
	// function問い合わせ実装.
	@Override
	public Object get(String arg0, Scriptable arg1) {
		return getFunction(arg0); 
	}
	
	@Override
	public String toString() {
		if(staticFlag) {
			// staticフラグがONの場合.
			// オブジェクト名を返却する.
			return getObjectName();
		}
		return resultToString();
	}
	
	// staticFlagをセット.
	protected void setStaticFlag(boolean b) {
		staticFlag = b;
	}
	
	// staticFlagを取得.
	protected boolean isStaticFlag() {
		return staticFlag;
	}
	
	// 元のオブジェクトを取得.
	public T getRaw() {
		// Java側で利用するオブジェクトを返却する場合は
		// ここで返却内容を設定する.
		return null;
	}
	
	// オブジェクト名を返却.
	public String getObjectName() {
		// オブジェクト名.
		return "[" + getName() + "]";
	}
	
	// new CustomObject(...);
	@Override
	public Scriptable newInstance(Context arg0, Scriptable arg1, Object[] arg2) {
		if(!staticFlag) {
			// staticフラグがOFFの場合エラー.
			throw new RhilaException("new cannot be done from the created instance.");
		}
		return newInstance(arg2);
	}
	
	// function実行.
	@Override
	public Object function(
		Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
		// オブジェクトなのでFunctionコールはgetObjectName返却.
		return getObjectName();
	}
	
	// [js]newInstance.
	protected abstract Scriptable newInstance(Object[] args);
	
	// function名群を返却.
	// ここでの返却値はstaticで名前はソート済みのものを返却.
	protected abstract String[] getFunctionNames();
	
	// function実行.
	// type＝実行対象のgetFunctionNames()のIndex値が設定されます.
	protected abstract Object callFunction(int type, Object[] args);
	
	// toString返却を行う場合はこちらで実装.
	protected abstract String resultToString();
}
