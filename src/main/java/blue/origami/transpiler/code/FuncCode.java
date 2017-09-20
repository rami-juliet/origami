package blue.origami.transpiler.code;

import blue.origami.nez.ast.Tree;
import blue.origami.transpiler.FuncParam;
import blue.origami.transpiler.FunctionContext;
import blue.origami.transpiler.FunctionUnit;
import blue.origami.transpiler.TArrays;
import blue.origami.transpiler.TCodeSection;
import blue.origami.transpiler.TEnv;
import blue.origami.transpiler.TFmt;
import blue.origami.transpiler.type.FuncTy;
import blue.origami.transpiler.type.Ty;
import blue.origami.util.ODebug;
import blue.origami.util.OStrings;

public class FuncCode extends Code1 implements FuncParam, FunctionUnit {

	String[] paramNames;
	Ty[] paramTypes;
	Ty returnType;

	Tree<?> body = null;
	FunctionContext funcContext = null;

	public FuncCode(String[] paramNames, Ty[] paramTypes, Ty returnType, Tree<?> body) {
		super(new DeclCode());
		this.paramNames = paramNames;
		this.paramTypes = paramTypes;
		this.returnType = returnType;
		this.body = body;
	}

	public FuncCode(String[] paramNames, Ty[] paramTypes, Ty returnType, Code body) {
		super(body);
		this.paramNames = paramNames;
		this.paramTypes = paramTypes;
		this.returnType = returnType;
		this.body = null;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public String[] getParamNames() {
		return this.paramNames;
	}

	@Override
	public Ty[] getParamTypes() {
		return this.paramTypes;
	}

	@Override
	public Ty getReturnType() {
		return this.returnType;
	}

	@Override
	public void setParamTypes(Ty[] pats) {
		this.paramTypes = pats;
	}

	@Override
	public void setReturnType(Ty ret) {
		this.returnType = ret;
	}

	@Override
	public int getStartIndex() {
		return this.funcContext.getStartIndex();
	}

	public String[] getFieldNames() {
		return this.funcContext.getFieldNames();
	}

	public Code[] getFieldCode() {
		return this.funcContext.getFieldCode();
	}

	public Ty[] getFieldTypes() {
		return this.funcContext.getFieldTypes();
	}

	private FunctionContext sync(TEnv env) {
		FunctionContext fcx = new FunctionContext(env.get(FunctionContext.class));
		if (this.funcContext != null) {
			fcx.syncIndex(this.funcContext);
		}
		this.funcContext = fcx;
		return fcx;
	}

	@Override
	public Code asType(TEnv env, Ty ret) {
		if (this.isUntyped()) {
			FunctionContext fcx = this.sync(env);
			Code inner = this.body != null ? env.parseCode(env, this.body) : this.getInner();
			this.inner = this.typeBody(env, fcx, inner);
			this.setType(Ty.tFunc(this.returnType, this.paramTypes));
		}
		if (ret.isFunc() && isGenericFunc(this.getType())) {
			FuncTy retFuncTy = (FuncTy) ret.real();
			if (retFuncTy.getParamSize() != this.paramTypes.length) {
				throw new ErrorCode(this.getSource(), TFmt.mismatched_parameter_size_S_S, retFuncTy.getParamSize(),
						this.paramTypes.length);
			}
			for (int i = 0; i < this.paramTypes.length; i++) {
				if (this.paramTypes[i].hasVar()) {
					this.paramTypes[i] = retFuncTy.getParamTypes()[i];
				}
			}
			this.returnType = retFuncTy.getReturnType();
			FunctionContext fcx = this.sync(env);
			Code inner = this.body != null ? env.parseCode(env, this.body) : this.getInner();
			this.inner = this.typeBody(env, fcx, inner);
			this.setType(Ty.tFunc(this.returnType, this.paramTypes));
			ODebug.trace("TODO: %s as %s", this.getType(), ret);
			return this;
		}
		return this.castType(env, ret);
	}

	public static boolean isGenericFunc(Ty ty) {
		if (ty.isFunc()) {
			FuncTy funcTy = (FuncTy) ty.real();
			return TArrays.testSomeTrue((t -> t.hasVar()), funcTy.getParamTypes()) || funcTy.getReturnType().isUnion();
		}
		return false;
	}

	@Override
	public boolean showError(TEnv env) {
		FuncTy funcTy = (FuncTy) this.getType();
		// funcTy.isGeneric()
		if (isGenericFunc(funcTy)) {
			env.reportError(this.getSource(), TFmt.abstract_function_YY1__YY2, this, funcTy);
			return true;
		}
		return false;
	}

	@Override
	public void emitCode(TEnv env, TCodeSection sec) {
		sec.pushFuncExpr(env, this);
	}

	@Override
	public void strOut(StringBuilder sb) {
		sb.append("(");
		for (int i = 0; i < this.paramNames.length; i++) {
			sb.append("\\");
			sb.append(this.paramNames[i]);
			sb.append(" ");
		}
		OStrings.append(sb, this.getInner());
		sb.append(")");
	}

	@Override
	public void dumpCode(SyntaxHighlight sb) {
		sb.TypeAnnotation(this.getType(), () -> {
			if (this.paramNames.length == 0) {
				sb.Keyword("\\() ");
			} else {
				for (int i = 0; i < this.paramNames.length; i++) {
					sb.Keyword("\\");
					sb.Name(this.paramNames[i]);
					sb.append(" ");
				}
			}
			sb.Expr(this.getInner());
		});
	}

}
