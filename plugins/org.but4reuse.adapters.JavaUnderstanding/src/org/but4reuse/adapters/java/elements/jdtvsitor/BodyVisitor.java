package org.but4reuse.adapters.java.elements.jdtvsitor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.*;

public class BodyVisitor extends ASTVisitor {
	ArrayList<ASTNode> nodes=new ArrayList<>();



	public BodyVisitor() {
		nodes=new ArrayList<>();
	}

	public boolean visit(ASTNode node) {
		nodes.add(node);
		return false;
	}

	public List<ASTNode> getNodes() {
		return nodes;
	}

	//Statement block
	/*public boolean visit(SwitchStatement node){
		nodes.add(node);
		return super.visit(node);
	}*/

	public boolean visit(IfStatement node) {
		AST ast=node.getAST();
	
		ASTNode body=node.getThenStatement();
		node.setThenStatement(ast.newEmptyStatement());
		nodes.add(node);
		body.accept(this);

		if (node.getElseStatement()!=null) {
			ASTNode elseBody=node.getElseStatement();
			node.setElseStatement(ast.newEmptyStatement());
			elseBody.accept(this);
		}
		return super.visit(node);
	}

	public boolean visit(DoStatement node) {
		ASTNode body=node.getBody();
		node.setBody(node.getAST().newEmptyStatement());
		nodes.add(node);
		body.accept(this);
		return super.visit(node);
	}

	public boolean visit(WhileStatement node) {

		ASTNode body=node.getBody();
		node.setBody(node.getAST().newEmptyStatement());
		nodes.add(node);
		body.accept(this);
		return super.visit(node);
	}

	public boolean visit(ForStatement node) {
		ASTNode body=node.getBody();
		node.setBody(node.getAST().newEmptyStatement());
		nodes.add(node);
		body.accept(this);
		return true;
	}

	//other statement
	public boolean visit(BreakStatement node) {
		nodes.add(node);
		return super.visit(node);
	}

	public boolean visit(ContinueStatement node) {
		nodes.add(node);
		return super.visit(node);
	}

	public boolean visit(ReturnStatement node) {
		nodes.add(node);
		return super.visit(node);
	}

	public boolean visit(VariableDeclarationStatement node) {
		nodes.add(node);
		return true;
	}
	
//	public boolean visit(VariableDeclarationFragment node) {
//		nodes.add(node);
//		return super.visit(node);
//	}
	
	public boolean visit(SingleVariableDeclaration node) {
		nodes.add(node);
		return super.visit(node);
	}

	public boolean visit(Assignment node) {
		nodes.add(node);
		return super.visit(node);
		
		
	}

	public boolean visit(MethodInvocation node) {
		nodes.add(node);
		return super.visit(node);
	}

	public boolean visit(VariableDeclarationExpression node) {
		nodes.add(node);
		return super.visit(node);
	}


	public double pourcentageSemblableOrderSensitive(List<ASTNode> body1,List<ASTNode> body2) {
		ASTMatcher matcher=new ASTMatcher();
		
		//body1 doit etre le plus grand body des deux
		if (body1.size()<body2.size()){
			List<ASTNode> t = body1;
			body1=body2;
			body2=t;
		}
		int equals=0;
		for(int i=0;i<body2.size();i++) {
				if (body1.get(i).subtreeMatch(matcher,body2.get(i))) {
					equals++;
				}
		}

		return (1.0*equals)/body1.size();
	}
	
	public static double pourcentageSemblableNotOrderSensitive(List<ASTNode> body1,List<ASTNode> body2) {
		ASTMatcher matcher=new ASTMatcher();
		if (body1.size()<body2.size()){
			List<ASTNode> t = body1;
			body1=body2;
			body2=t;
		}
		int equals=0;
		for(ASTNode node1:body2) {
			for(ASTNode node2:body1) {
				if (node1.subtreeMatch(matcher,node2)) {
					equals++;
				}
			}	
		}
		return (1.0*equals)/body1.size();
	}

	public double pourcentageSemblable(BodyVisitor body,boolean orderSensitive) {
		if (nodes.size()==0 && body.nodes.size()==0)
			return 1;
		if ( (nodes.size()==0 && body.nodes.size()!=0)||
				(nodes.size()!=0 && body.nodes.size()==0) )
			return 0;
		
		if (orderSensitive) {
			return  pourcentageSemblableOrderSensitive(nodes,body.nodes);
		}else {
			return pourcentageSemblableNotOrderSensitive(nodes,body.nodes);
		}
	}


}
