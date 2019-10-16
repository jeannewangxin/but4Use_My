package org.but4reuse.adapters.java.elements;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.WhileStatement;

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
			body=node.getElseStatement();
			node.setElseStatement(ast.newEmptyStatement());
			body.accept(this);
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
	

	public static double pourcentageSemblable(List<ASTNode> body1,List<ASTNode> body2) {
		ASTMatcher matcher=new ASTMatcher();
		if (body1.size()<body2.size()){
			List<ASTNode> t = body1;
			body1=body2;
			body2=t;
		}
		int equals=0;
		for(ASTNode node1:body1) {
			for(ASTNode node2:body2) {
				if (node1.subtreeMatch(matcher,node2)) {
					equals++;
				}
			}	
		}

		return (1.0*equals)/body1.size();
	}
	
	public static double pourcentageSemblable(Block block1, Block block2) {
		BodyVisitor v1=new BodyVisitor();
		BodyVisitor v2=new BodyVisitor();
		block1.accept(v1);
		block2.accept(v2);
		return  pourcentageSemblable(v1.getNodes(),v2.getNodes());
	}


}
