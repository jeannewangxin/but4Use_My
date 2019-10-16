package org.but4reuse.adapters.java.elements;
import java.util.ArrayList;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class Visitor extends ASTVisitor{
		PackageDeclaration packageDeclaration;
		ArrayList<MethodDeclaration> methods = new ArrayList<>();
		TypeDeclaration type;
		ArrayList<ImportDeclaration> imports= new ArrayList<>();
		ArrayList<FieldDeclaration> fields=new ArrayList<>();
		
		
		@Override
		public boolean visit(PackageDeclaration node) {
			packageDeclaration=node;
			return super.visit(node);
		}
		@Override
		public boolean visit(ImportDeclaration node) {
			imports.add(node);
			return super.visit(node);
		}
		
		public boolean visit(FieldDeclaration node) {
			fields.add(node);
			return super.visit(node);
		}
		
		@Override
		public boolean visit(TypeDeclaration node) {
			type=node;
			return super.visit(node);
		}

		
		@Override
		public boolean visit(MethodDeclaration node) {
			methods.add(node);
			return super.visit(node);
		}
		

		public ArrayList<MethodDeclaration> getMethods() {
			return methods;
		}
		
		public TypeDeclaration getTypes() {
			return type;
		}
		
		public ArrayList<ImportDeclaration> getImports() {
			return imports;
		}
		
		
		
		public ArrayList<FieldDeclaration> getFields() {
			return fields;
		}
		
		public PackageDeclaration getPackage() {
			return packageDeclaration;
		}

	}
