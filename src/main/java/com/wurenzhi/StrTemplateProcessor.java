package com.wurenzhi;

import com.google.auto.service.AutoService;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Names;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 编译器重写
 */
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("com.wurenzhi.StrTemplate")
public class StrTemplateProcessor extends AbstractProcessor {
	
	private Messager messager; // 编译时期输入日志的
	private JavacTrees javacTrees; // 提供了待处理的抽象语法树
	private TreeMaker treeMaker; // 封装了创建AST节点的一些方法
	private Names names; // 提供了创建标识符的方法
	
	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		this.messager = processingEnv.getMessager();
		this.javacTrees = JavacTrees.instance(processingEnv);
		Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
		this.treeMaker = TreeMaker.instance(context);
		this.names = Names.instance(context);
		super.init(processingEnv);
	}
	
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		Set<? extends Element> elementsAnnotatedWith = roundEnv.getElementsAnnotatedWith(StrTemplate.class);
		elementsAnnotatedWith.forEach(e -> {
			javacTrees.getTree(e).accept(new TreeTranslator() {
				@Override
				public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
					try {
						for (JCTree def : ((JCTree.JCClassDecl) jcClassDecl).defs) {
							if (def instanceof JCTree.JCMethodDecl) {
								for (JCTree.JCStatement tstat : ((JCTree.JCMethodDecl) def).body.stats) {
									recursion(tstat);
								}
							} else {//内部类,成员变量
								recursion((JCTree.JCStatement) def);
							}
						}
						System.err.println("编译结果:\n" + jcClassDecl.toString());
						super.visitClassDef(jcClassDecl);
					} catch (Exception e) {
						e.printStackTrace();
						throw new RuntimeException("编译异常:" + e);
					}
				}
			});
		});
		return true;
	}
	
	/**
	 * 外层递归
	 * @param stat JCTree.JCStatement类型
	 */
	private void recursion(JCTree.JCStatement stat) throws Exception {
		// String name = stat.getClass().getName(); System.out.println("类型:" + name);
		if (stat instanceof JCTree.JCTry) {
			JCTree.JCTry stat1 = ((JCTree.JCTry) stat);
			final JCTree.JCBlock body = stat1.body;
			final List<JCTree.JCStatement> stats = body.stats;
			for (JCTree.JCStatement statement : stats) {
				recursion(statement);
			}
			final List<JCTree.JCCatch> catchers = stat1.catchers;
			for (JCTree.JCCatch catcher : catchers) {
				for (JCTree.JCStatement statement : catcher.body.stats) {
					recursion(statement);
				}
			}
			final JCTree.JCBlock finalizer = stat1.finalizer;
			for (JCTree.JCStatement statement : finalizer.stats) {
				recursion(statement);
			}
			final List<JCTree> resources = stat1.resources;
			for (JCTree resource : resources) {
				if (resource instanceof JCTree.JCVariableDecl) {
					JCTree.JCVariableDecl res = (JCTree.JCVariableDecl) resource;
					recursion(res);
				}
			}
		} else if (stat instanceof JCTree.JCClassDecl) {//类
			for (JCTree def : ((JCTree.JCClassDecl) stat).defs) {
				if (def instanceof JCTree.JCMethodDecl) {
					for (JCTree.JCStatement tstat : ((JCTree.JCMethodDecl) def).body.stats) {
						recursion(tstat);
					}
				} else {//内部类,成员变量
					recursion((JCTree.JCStatement) def);
				}
			}
		} else if (stat instanceof JCTree.JCVariableDecl) {//变量
			((JCTree.JCVariableDecl) stat).init = insideRecursion(((JCTree.JCVariableDecl) stat).init);
		} else if (stat instanceof JCTree.JCSynchronized) {
			JCTree.JCSynchronized stat1 = ((JCTree.JCSynchronized) stat);
			recursion(stat1.body);
			stat1.lock = insideRecursion(stat1.lock);
		} else if (stat instanceof JCTree.JCThrow) {
			JCTree.JCThrow stat1 = ((JCTree.JCThrow) stat);
			stat1.expr = insideRecursion(stat1.expr);
		} else if (stat instanceof JCTree.JCAssert) {
			JCTree.JCAssert stat1 = ((JCTree.JCAssert) stat);
			stat1.cond = insideRecursion(stat1.cond);
			stat1.detail = insideRecursion(stat1.detail);
		} else if (stat instanceof JCTree.JCEnhancedForLoop) {
			JCTree.JCEnhancedForLoop stat1 = ((JCTree.JCEnhancedForLoop) stat);
			recursion(stat1.var);
			recursion(stat1.body);
			stat1.expr = insideRecursion(stat1.expr);
		} else if (stat instanceof JCTree.JCLabeledStatement) {
			JCTree.JCLabeledStatement stat1 = ((JCTree.JCLabeledStatement) stat);
			recursion(stat1.body);
		} else if (stat instanceof JCTree.JCForLoop) {
			JCTree.JCForLoop stat1 = (JCTree.JCForLoop) stat;
			//声明
			List<JCTree.JCStatement> init = stat1.init;
			for (JCTree.JCStatement statement : init) {
				recursion(statement);
			}
			//条件
			stat1.cond = insideRecursion(stat1.cond);
			//自增
			List<JCTree.JCExpressionStatement> step = stat1.step;
			for (JCTree.JCExpressionStatement statement : step) {
				recursion(statement);
			}
			//语句
			JCTree.JCStatement thenpart = stat1.body;
			if (thenpart instanceof JCTree.JCBlock) {
				JCTree.JCBlock thenpart1 = (JCTree.JCBlock) thenpart;
				for (JCTree.JCStatement statement : thenpart1.stats) {
					if (statement instanceof JCTree.JCExpressionStatement) {
						recursion(statement);
					}
				}
			}
		} else if (stat instanceof JCTree.JCWhileLoop) {
			JCTree.JCWhileLoop stat1 = (JCTree.JCWhileLoop) stat;
			//条件
			stat1.cond = insideRecursion(stat1.cond);
			//语句
			recursion(stat1.body);
		} else if (stat instanceof JCTree.JCVariableDecl) {//变量声明
			JCTree.JCVariableDecl stat1 = (JCTree.JCVariableDecl) stat;
			JCTree.JCExpression 变量声明递归 = insideRecursion(stat1.init);
			if (变量声明递归 != stat1.init) {
				stat1.init = 变量声明递归;
			}
		} else if (stat instanceof JCTree.JCExpressionStatement) {//表达式
			JCTree.JCExpressionStatement stat1 = (JCTree.JCExpressionStatement) stat;
			stat1.expr = insideRecursion(stat1.expr);
		} else if (stat instanceof JCTree.JCIf) {
			JCTree.JCIf stat1 = (JCTree.JCIf) stat;
			//if中的条件
			stat1.cond = insideRecursion(stat1.cond);
			//if中的语句
			JCTree.JCStatement thenpart = stat1.thenpart;
			recursion(thenpart);
		} else if (stat instanceof JCTree.JCReturn) {
			JCTree.JCReturn stat1 = (JCTree.JCReturn) stat;
			stat1.expr = insideRecursion(stat1.expr);
		} else if (stat instanceof JCTree.JCBlock) {
			JCTree.JCBlock stat1 = (JCTree.JCBlock) stat;
			for (JCTree.JCStatement statement : stat1.stats) {
				if (statement instanceof JCTree.JCExpressionStatement) {
					recursion(statement);
				}
			}
		} else if (stat instanceof JCTree.JCSwitch) {
			JCTree.JCSwitch stat1 = ((JCTree.JCSwitch) stat);
			JCTree.JCExpression selector = stat1.selector;
			stat1.selector = insideRecursion(stat1.selector);
			List<JCTree.JCCase> cases = stat1.cases;
			for (JCTree.JCCase aCase : cases) {
				List<JCTree.JCStatement> stats = aCase.stats;
				for (JCTree.JCStatement statement : stats) {
					recursion(statement);
				}
				aCase.pat = insideRecursion(aCase.pat);
			}
		} else if (stat instanceof JCTree.JCCase) {
			JCTree.JCCase stat1 = ((JCTree.JCCase) stat);
			List<JCTree.JCStatement> stats = stat1.stats;
			for (JCTree.JCStatement statement : stats) {
				recursion(statement);
			}
			stat1.pat = insideRecursion(stat1.pat);
		} else if (stat instanceof JCTree.JCDoWhileLoop) {
			JCTree.JCDoWhileLoop stat1 = ((JCTree.JCDoWhileLoop) stat);
			//条件
			stat1.cond = insideRecursion(stat1.cond);
			//语句
			JCTree.JCStatement thenpart = stat1.body;
			recursion(thenpart);
		} else if (stat instanceof JCTree.JCSkip) {//无需
		} else if (stat instanceof JCTree.JCContinue) {//无需
		} else if (stat instanceof JCTree.JCBreak) {//无需
		} else {
			// System.out.println("未知类型:" + (stat == null ? null : stat.getClass().getName()));
		}
	}
	
	/**
	 * 内部递归
	 * @param stat JCTree.JCExpression类型
	 */
	private JCTree.JCExpression insideRecursion(JCTree.JCExpression init) throws Exception {
		ListBuffer<JCTree.JCExpression> buffer = new ListBuffer<>();
		if (init instanceof JCTree.JCLiteral) {// JCTree.JCLiteral 字符串 到头了
			JCTree.JCLiteral init1 = (JCTree.JCLiteral) init;
			String value = String.valueOf(init1.value);//获取value
			if (value.indexOf("${") != -1) {//判断包含,然后生成binary赋值回init1
				Pattern pattern = Pattern.compile("\\$\\{([^}]+)}");//正则匹配双花括号
				Matcher matcher = pattern.matcher(value);
				JCTree.JCExpression binary = null;
				StringBuffer pin = new StringBuffer();
				while (matcher.find()) {
					String key = matcher.group(1).trim();//避免空格无法匹配
					String[] split = value.split("\\$\\{" + key + "}", -1);
					for (int i = 0; i < split.length; i++) {
						JCTree.JCExpression jcTree = null;
						if (i != split.length - 1) {//不是最后一次循环,还要继续拼接
							if (split[i].equals("")) {//空串不需要拼接
								jcTree = treeMaker.Ident(names.fromString(key));
							} else {
								jcTree = treeMaker.Binary(JCTree.Tag.PLUS, treeMaker.Literal(split[i]), treeMaker.Ident(names.fromString(key)));
							}
						} else {//最后一次循环,不再加key
							if (split[i].equals("")) break; //空串不需要拼接
							jcTree = treeMaker.Literal(split[i]);
						}
						if (binary == null) {
							binary = jcTree;
							// } else if (("\"\" + " + key).equals(binary.toString())) {//格式为 "" + key,无意义,去掉前置引号
							// 	binary = treeMaker.Ident(names.fromString(key));
						} else {
							binary = treeMaker.Binary(JCTree.Tag.PLUS, binary, jcTree);
						}
					}
					break;
				}
				if (binary != null) {
					init = binary;
				}
			}
		} else if (init instanceof JCTree.JCMethodInvocation) {
			JCTree.JCMethodInvocation init1 = (JCTree.JCMethodInvocation) init;
			for (JCTree.JCExpression arg : init1.args) {
				buffer.append(insideRecursion(arg));//不管是不是,反正递归就行了
			}
			init1.args = buffer.toList();
			JCTree.JCExpression meth = ((JCTree.JCMethodInvocation) init).meth;//必有
			((JCTree.JCMethodInvocation) init).meth = insideRecursion(meth);
		} else if (init instanceof JCTree.JCFieldAccess) {
			JCTree.JCFieldAccess init1 = (JCTree.JCFieldAccess) init;
			JCTree.JCExpression selected = init1.selected;
			JCTree.JCExpression 变量声明递归 = insideRecursion(selected);
			init1.selected = 变量声明递归;
		} else if (init instanceof JCTree.JCBinary) {
			JCTree.JCBinary init1 = (JCTree.JCBinary) init;
			init1.lhs = insideRecursion(init1.lhs);
			init1.rhs = insideRecursion(init1.rhs);
		} else if (init instanceof JCTree.JCIdent) {
			// System.out.println("直接声明,无需转换:" + init.toString());
		} else if (init instanceof JCTree.JCAssignOp) {
			JCTree.JCAssignOp init1 = (JCTree.JCAssignOp) init;
			init1.lhs = insideRecursion(init1.lhs);
			init1.rhs = insideRecursion(init1.rhs);
		} else if (init instanceof JCTree.JCNewClass) {
			JCTree.JCNewClass init1 = (JCTree.JCNewClass) init;
			for (JCTree.JCExpression arg : init1.args) {
				buffer.append(insideRecursion(arg));//不管是不是,反正递归就行了
			}
			init1.args = buffer.toList();
			final JCTree.JCClassDecl def = init1.def;//方法
			recursion(def);
		} else if (init instanceof JCTree.JCParens) {
			JCTree.JCParens cond1 = (JCTree.JCParens) init;
			cond1.expr = insideRecursion(cond1.expr);
		} else if (init instanceof JCTree.JCLambda) {
			JCTree.JCLambda cond1 = (JCTree.JCLambda) init;
			final List<JCTree.JCVariableDecl> params = cond1.params;
			for (JCTree.JCVariableDecl param : params) recursion(param);//Lambda参数
			final JCTree body = cond1.body;//Lambda内容
			if (body instanceof JCTree.JCStatement) recursion(((JCTree.JCStatement) body));
		} else {
			// System.out.println("未知类型:" + ((init == null) ? "空" : init.getClass().getName() + ",内容:" + init.toString()));
		}
		return init;
	}
	
}