package blue.origami.transpiler.rule;

import blue.origami.common.Symbol;
import blue.origami.transpiler.AST;
import blue.origami.transpiler.Env;
import blue.origami.transpiler.code.Code;
import blue.origami.transpiler.code.TypeCode;
import blue.origami.transpiler.type.Ty;

public class FuncType implements ParseRule, Symbols {
	public final static Symbol _TupleType = Symbol.unique("TupleType");

	@Override
	public Code apply(Env env, AST t) {
		Ty ret = env.parseType(env, t.get(_type), null);
		if (t.has(_base)) {
			AST from = t.get(_base);
			if (from.getTag() == _TupleType) {
				Ty[] a = new Ty[from.size()];
				for (int i = 0; i < from.size(); i++) {
					a[i] = env.parseType(env, from.get(i), null);
				}
				return new TypeCode(Ty.tFunc(ret, a));
			} else {
				Ty fromTy = env.parseType(env, t.get(_base), null);
				return new TypeCode(Ty.tFunc(ret, fromTy));
			}
		} else {
			AST params = t.get(_param);
			Ty[] a = new Ty[params.size()];
			for (int i = 0; i < params.size(); i++) {
				a[i] = env.parseType(env, params.get(i), null);
			}
			return new TypeCode(Ty.tFunc(ret, a));
		}
	}
}