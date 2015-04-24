package Compiler;

import java.util.LinkedList;

import org.antlr.v4.runtime.TokenStream;

import Compiler.Ast.BinaryOperatorNode;
import Compiler.Ast.BlockStatementNode;
import Compiler.Ast.BreakStatementNode;
import Compiler.Ast.CharNode;
import Compiler.Ast.CharTypeNode;
import Compiler.Ast.ConstTypeNode;
import Compiler.Ast.ContinueStatementNode;
import Compiler.Ast.DeclarationNode;
import Compiler.Ast.ExprStatementNode;
import Compiler.Ast.ExpressionNode;
import Compiler.Ast.FileNode;
import Compiler.Ast.ForStatementNode;
import Compiler.Ast.FormalParameterNode;
import Compiler.Ast.FormalParametersNode;
import Compiler.Ast.FunctionCallNode;
import Compiler.Ast.FunctionDeclarationNode;
import Compiler.Ast.IdNode;
import Compiler.Ast.IfStatementNode;
import Compiler.Ast.IntNode;
import Compiler.Ast.IntTypeNode;
import Compiler.Ast.Node;
import Compiler.Ast.NothingNode;
import Compiler.Ast.ParamNode;
import Compiler.Ast.PointerTypeNode;
import Compiler.Ast.ReturnStatementNode;
import Compiler.Ast.StatementNode;
import Compiler.Ast.StaticArrayTypeNode;
import Compiler.Ast.StringNode;
import Compiler.Ast.TypeNode;
import Compiler.Ast.UnaryOperatorNode;
import Compiler.Ast.VoidTypeNode;
import Compiler.Ast.WhileStatementNode;

public class AstParser extends CParser {

	private Node root;
	private LinkedList<Node> list;
	private int scope = 0;

	public AstParser(TokenStream input) {
		super(input);
	}

	/**
	 * Handle variable declaration
	 *
	 * @param id
	 */
	@Override
	public void handleVarDecl(String id) {
		Log.debug("handleVarDecl: " + id);

		// Initializer
		Ast.Node initializer = list.removeFirst();

		// type
		Assert.Assert(list.peekFirst() instanceof TypeNode);
		TypeNode type = (TypeNode) list.removeFirst();

		DeclarationNode node = new DeclarationNode(id, type, initializer);
		insertNode(0, node);
	}

	/**
	 * Handle function declaration
	 *
	 * @param id
	 */
	@Override
	public void handleFuncDecl(String id) {
		Log.debug("handleFuncDecl: " + id);

		FormalParametersNode params = null;
		// Add empty formal parameter list if no parameters.
		if(list.size() < 2 || !(list.get(1) instanceof FormalParametersNode)) {
			params = new FormalParametersNode();
		} else {
			params = (FormalParametersNode) list.remove(1);
		}

		// Block
		Assert.Assert(list.peekFirst() instanceof BlockStatementNode);
		BlockStatementNode body = (BlockStatementNode) list.removeFirst();

		// Return type
		Assert.Assert(list.peekFirst() instanceof TypeNode);
		TypeNode returnType = (TypeNode) list.removeFirst();

		FunctionDeclarationNode node = new FunctionDeclarationNode(id,
				returnType, params, body);

		insertNode(0, node);
	}

	/**
	 * Handle formal parameters (of a function declaration)
	 */
	@Override
	public void handleFormalParameters() {
		Log.debug("handleFormalParameters");

		FormalParametersNode node = new FormalParametersNode();

		// Add all Formalparameters to our children
		Node paramNode;
		while((paramNode = list.peekFirst()) != null) {
			if(!(paramNode instanceof FormalParameterNode)) {
				break;
			}

			FormalParameterNode param = (FormalParameterNode) list
					.removeFirst();
			node.addParam(0, param);
		}

		insertNode(0, node);
	}

	/**
	 * Handle one single formal parameter
	 *
	 * @param id
	 */
	@Override
	public void handleFormalParameter(String id) {
		Log.debug("handleFormalParameter: " + id);

		Assert.Assert(list.peekFirst() instanceof TypeNode);
		TypeNode type = (TypeNode) list.removeFirst();

		FormalParameterNode node = new FormalParameterNode(id, type);

		insertNode(0, node);
	}

	/**
	 * Handle block
	 */
	@Override
	public void handleBlock() {
		Log.debug("handleBlock");

		BlockStatementNode node = new BlockStatementNode();

		// Add all statements in our scope.
		Node stmtNode;
		while((stmtNode = list.peekFirst()) != null) {
			if(!(stmtNode instanceof StatementNode)
					|| stmtNode.scope != scope + 1) {
				break;
			}

			node.addStatement(0, (StatementNode) list.removeFirst());
		}

		insertNode(0, node);
	}

	/**
	 * Handle expression
	 */
	@Override
	public void handleExpr() {
		Log.debug("handleExpr");
	}

	/**
	 * Handle expression statement
	 */
	@Override
	public void handleExprStatement() {
		Log.debug("handleExprStatement");

		Assert.Assert(list.peekFirst() instanceof ExpressionNode);
		ExprStatementNode node = new ExprStatementNode(
				(ExpressionNode) list.removeFirst());

		insertNode(0, node);
	}

	/**
	 * Handle int literal
	 *
	 * @param n
	 */
	@Override
	public void handleInt(String n) {
		Log.debug("handleInt " + n);

		IntNode node = new IntNode(Integer.parseInt(n));
		insertNode(0, node);
	}

	/**
	 * Handle char literal
	 *
	 * @param n
	 */
	@Override
	public void handleChar(String n) {
		Log.debug("handleChar " + n);

		CharNode node = new CharNode(n.charAt(0));
		insertNode(0, node);
	}

	/**
	 * Handle string literal
	 *
	 * @param n
	 */
	@Override
	public void handleString(String n) {
		Log.debug("handleString " + n);

		StringNode node = new StringNode(n.substring(1, n.length() - 1));
		insertNode(0, node);
	}

	/**
	 * Handle function parameter
	 */
	@Override
	public void handleParam() {
		Log.debug("handleParam");

		Assert.Assert(list.peekFirst() instanceof ExpressionNode);
		ParamNode node = new ParamNode((ExpressionNode) list.removeFirst());

		insertNode(0, node);
	}

	/**
	 * Handle function call
	 *
	 * @param n
	 */
	@Override
	public void handleFunctionCall(String n) {
		Log.debug("handleFunctionCall " + n);

		FunctionCallNode node = new FunctionCallNode(n);

		while(list.peekFirst() instanceof ParamNode) {
			node.addParam(0, (ParamNode) list.removeFirst());
		}

		insertNode(0, node);

	}

	/**
	 * Handle binary operator
	 *
	 * @param operator
	 */
	@Override
	public void handleBinaryOperator(String operator) {
		Log.debug("handleBinaryOperator: " + operator);

		Assert.Assert(list.peekFirst() instanceof ExpressionNode);
		ExpressionNode right = (ExpressionNode) list.removeFirst();
		Assert.Assert(list.peekFirst() instanceof ExpressionNode);
		ExpressionNode left = (ExpressionNode) list.removeFirst();

		BinaryOperatorNode node = new BinaryOperatorNode(operator, left, right);

		insertNode(0, node);
	}

	/**
	 * Handle unary operator
	 *
	 * @param operator
	 */
	@Override
	public void handleUnaryOperator(String operator) {
		Log.debug("handleUnaryOperator: " + operator);

		Assert.Assert(list.peekFirst() instanceof ExpressionNode);
		UnaryOperatorNode node = new UnaryOperatorNode(operator,
				(ExpressionNode) list.removeFirst());

		insertNode(0, node);
	}

	/**
	 * Handle identifier
	 *
	 * @param id
	 */
	@Override
	public void handleID(String id) {
		Log.debug("handleID: " + id);

		IdNode node = new IdNode(id);

		insertNode(0, node);
	}

	/**
	 * Handle type
	 *
	 * @param t
	 *            : type name
	 * @param c
	 *            : const
	 */
	@Override
	public void handleType(String t, String c) {
		Log.debug("handleType: " + t + " c=" + c);

		TypeNode node = null;

		switch(t.toLowerCase()) {
		case "int":
			node = new IntTypeNode();
			break;
		case "char":
			node = new CharTypeNode();
			break;
		case "void":
			node = new VoidTypeNode();
			break;
		default:
			Assert.Assert(false);
		}

		node.constant = false;
		node.topLevel = false;

		if(c != null) {
			if(c.equals("const")) {
				node.constant = true;
			}
		}

		if(list.peekFirst() instanceof NothingNode) {
			list.removeFirst();
			insertNode(0, node);
			return;
		}

		if(list.peekFirst() instanceof ConstTypeNode) {
			node.constant = true;
			list.removeFirst();

			// check again if it wasn't the last type on the stack
			if(list.peekFirst() instanceof NothingNode) {
				list.removeFirst();
				insertNode(0, node);
				return;
			}
		}

		if(list.peekFirst() instanceof PointerTypeNode
				&& !((TypeNode) list.peekFirst()).topLevel) {
			list.get(0).insertLefMostLeaf(node);
			((TypeNode) list.get(0)).topLevel = true;
		} else {
			// Log.debug("    Inserting node");
			insertNode(0, node);
		}
	}

	/**
	 * Handle static array
	 *
	 * @param n
	 */
	@Override
	public void handleStaticArray(String n) {
		Log.debug("handleStaticArray: " + n);

		Assert.Assert(list.peekFirst() instanceof TypeNode);
		StaticArrayTypeNode node = new StaticArrayTypeNode(Integer.parseInt(n),
				(TypeNode) list.removeFirst());
		node.topLevel = true;

		insertNode(0, node);
	}

	/**
	 * Handle const keyword in type
	 */
	@Override
	public void handleConst() {
		Log.debug("handleConst");

		ConstTypeNode node = new ConstTypeNode();
		insertNode(0, node);
	}

	/**
	 * Handle pointer in type
	 */
	@Override
	public void handlePointer() {
		Log.debug("handlePointer");

		PointerTypeNode node = new PointerTypeNode();
		node.constant = false;
		node.topLevel = false;

		// just insert the node if it's the last pointer on the stack
		if(list.peekFirst() instanceof NothingNode) {
			list.removeFirst();
			insertNode(0, node);
			return;
		}

		// check if it is constant
		if(list.peekFirst() instanceof ConstTypeNode) {
			node.constant = true;
			list.removeFirst();

			// check again if it wasn't the last pointer on the stack
			if(list.peekFirst() instanceof NothingNode) {
				list.removeFirst();
				insertNode(0, node);
				return;
			}
		}

		if(list.peekFirst() instanceof PointerTypeNode) {
			list.get(0).insertLefMostLeaf(node);
		} else {
			insertNode(0, node);
		}
	}

	/**
	 * Handle for statement
	 */
	@Override
	public void handleForStatement() {
		Log.debug("handleForStatement");

		Assert.Assert(list.peekFirst() instanceof StatementNode);
		StatementNode body = (StatementNode) list.removeFirst();
		Node third = list.removeFirst();
		Node second = list.removeFirst();
		Node first = list.removeFirst();
		ForStatementNode node = new ForStatementNode(first, second, third, body);

		insertNode(0, node);
	}

	/**
	 * Handle return statement
	 */
	@Override
	public void handleReturnStatement() {
		Log.debug("handleReturnStatement");

		Assert.Assert(list.peekFirst() instanceof ExpressionNode || list.peekFirst() instanceof NothingNode);
        ReturnStatementNode node;
        if(list.peekFirst() instanceof NothingNode) {
            node = new ReturnStatementNode(new NothingNode());
            list.removeFirst();
        }
        else {
            node = new ReturnStatementNode(
                    (ExpressionNode) list.removeFirst());
        }
		insertNode(0, node);
	}

	/**
	 * Handle break statement
	 */
	@Override
	public void handleBreakStatement() {
		Log.debug("handleBreakStatement");

		BreakStatementNode node = new BreakStatementNode();

		insertNode(0, node);
	}

	/**
	 * Handle continue statement
	 */
	@Override
	public void handleContinueStatement() {
		Log.debug("handleContinueStatement");

		ContinueStatementNode node = new ContinueStatementNode();

		insertNode(0, node);
	}

	/**
	 * Handle while statement
	 */
	@Override
	public void handleWhileStatement() {
		Log.debug("handleWhileStatement");

		Assert.Assert(list.peekFirst() instanceof StatementNode);
		StatementNode body = (StatementNode) list.removeFirst();
		Assert.Assert(list.peekFirst() instanceof ExpressionNode);
		ExpressionNode condition = (ExpressionNode) list.removeFirst();

		WhileStatementNode node = new WhileStatementNode(condition, body);

		insertNode(0, node);
	}

	/**
	 * Handle if statement
	 */
	@Override
	public void handleIfStatement() {
		Log.debug("handleIfStatement");

		Node elseBody = list.removeFirst();
		Assert.Assert(list.peekFirst() instanceof StatementNode);
		StatementNode body = (StatementNode) list.removeFirst();
		Assert.Assert(list.peekFirst() instanceof ExpressionNode);
		ExpressionNode condition = (ExpressionNode) list.removeFirst();

		IfStatementNode node = new IfStatementNode(condition, body, elseBody);

		insertNode(0, node);
	}

	/**
	 * Handle type cast
	 */
	@Override
	public void handleTypeCast() {
		Log.debug("handleTypeCast");
		Assert.Assert(list.peekFirst() instanceof ExpressionNode);
		ExpressionNode expr = (ExpressionNode) list.removeFirst();

		Assert.Assert(list.peekFirst() instanceof TypeNode);
		TypeNode type = (TypeNode) list.removeFirst();

		expr.cast = type;
		insertNode(0, expr);

	}

	/**
	 * Handle 'nothing'
	 */
	@Override
	public void handleNothing() {
		Log.debug("handleNothing");

		NothingNode node = new NothingNode();

		insertNode(0, node);
	}

	/**
	 * Handle file
	 */
	@Override
	public void handleFile() {
		Log.debug("handleFile");

		FileNode node = new FileNode();

		while(list.size() != 0) {
			node.addDeclaration(0, list.removeFirst());
		}

		insertNode(0, node);
	}


    /**
     * Handle
     *   #include <stdio.h>
     */
    @Override
    public void handleIncludeIO() {} {
        Log.debug("handleIncludeIO");
    }



	@Override
	public void startScope() {
		scope += 1;
	}

	@Override
	public void endScope() {
		scope -= 1;
	}

	private void insertNode(int pos, Node node) {
		node.scope = scope;
		node.line = getCurrentToken().getLine();
		list.add(pos, node);
	}

	/**
	 * Build the AST
	 */
	public Node buildAst() {
		// Reset ast
		root = null;
		list = new LinkedList<Node>();

		// Start the parsing
		start();

		// FileNode should be the only 1 on the stack
		if(list.size() != 1) {
			Log.debug("Stack should contain 1 element");
		} else {
			root = list.removeFirst();
			if(!(root instanceof FileNode)) {
				Log.debug("Root should be a FileNode");
			}
		}

		return root;
	};
}
