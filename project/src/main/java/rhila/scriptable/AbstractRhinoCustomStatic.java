package rhila.scriptable;

import java.util.Arrays;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import rhila.lib.ArrayMap;

/**
 * [js]Rhino用のCustom用のstaticObject定義.
 * 
 * Rhinoの利用で、
 * AbstractRhinoCustomNewInstanceのように
 *   > new CustomObject(...);
 * ではなく、直接
 *   > CustomObject.function(...);
 * で実行するstatic定義用のjs用StaticObject実装用の
 * 継承クラスとして利用します.
 */
public abstract class AbstractRhinoCustomStatic
	extends AbstractRhinoFunction {
	
	// instance可能なFunction定義.
	protected static ArrayMap<String, Scriptable> instanceList =
		new ArrayMap<String, Scriptable>();
	
	// [js]FunctionList.
	private static final class FunctionList
		extends AbstractRhinoFunctionInstance {
	    // lambda snapStart CRaC用.
		protected static final FunctionList LOAD_CRAC =
			new FunctionList();
		
		private int type;
		private String typeString;
		private AbstractRhinoCustomStatic object;
		
		// コンストラクタ.
		private FunctionList() {}
		
		// コンストラクタ.
		private FunctionList(int type, AbstractRhinoCustomStatic o) {
			this.type = type;
			this.typeString = o.getFunctionNames()[type];
			this.object = o;
		}
		
		// Function用の新しいインスタンスを生成.
		@Override
		public final Scriptable getInstance(Object... args) {
			// 個々のFunction用のインスタンスを作成.
			FunctionList ret = new FunctionList
				((Integer)args[0], (AbstractRhinoCustomStatic)args[1]);
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
	
	// function取得.
	protected Object getFunction(String name) {
		Object value = instanceList.get(name);
		if(value != null) {
			return value;
		}
		int no = Arrays.binarySearch(getFunctionNames(), name);
		if(no == -1) {
			return null;
		}
		Scriptable ret = FunctionList.LOAD_CRAC.getInstance(no, this);
		instanceList.put(name, ret);
		return ret;
	}
	
	
	// function問い合わせ実装.
	@Override
	public Object get(String arg0, Scriptable arg1) {
		return getFunction(arg0); 
	}
	
	@Override
	public String toString() {
		// オブジェクト名.
		return "[" + getName() + "]";
	}
	
	// function実行.
	@Override
	public Object function(
		Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
		// オブジェクトなのでFunctionコールはgetObjectName返却.
		return toString();
	}
	
	// function名群を返却.
	// ここでの返却値はstaticで名前はソート済みのものを返却.
	protected abstract String[] getFunctionNames();
	
	// function実行.
	// type＝実行対象のgetFunctionNames()のIndex値が設定されます.
	protected abstract Object callFunction(int type, Object[] args);
}
