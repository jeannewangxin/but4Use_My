package org.but4reuse.adapters.java.elements.adapter;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.but4reuse.adapters.IAdapter;
import org.but4reuse.adapters.IElement;
import org.but4reuse.adapters.java.elements.FieldElement;
import org.but4reuse.adapters.java.elements.ImportElement;
import org.but4reuse.adapters.java.elements.InterfacesElement;
import org.but4reuse.adapters.java.elements.MethodElement;
import org.but4reuse.adapters.java.elements.SuperClassElement;
import org.but4reuse.adapters.java.elements.activator.Activator;
import org.but4reuse.adapters.java.elements.adapter.FileStructureAdapter;
import org.but4reuse.adapters.java.elements.jdtvsitor.Visitor;
import org.but4reuse.adapters.java.elements.preferences.JavaUnderstandingAdapterPreferencePage;
import org.but4reuse.adapters.java.elements.preferences.JavaUnderstandingAdapterPreferencePage.Choice;
import org.but4reuse.utils.files.FileUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class JavaUnderstandingAdapter implements IAdapter {

	public static void setOptionBodyA(Choice c) {
		MethodElement.setOptionBody(c);
	}

	public static void setOptionNameA(Choice c) {
		MethodElement.setOptionName(c);
	}

	public static void setOptionReturnTypeA(Choice c) {
		MethodElement.setOptionReturnType(c);
	}

	public static void setOptionParameterA(Choice c) {
		MethodElement.setOptionParameter(c);
	}

	public static void setOptionModifierA(Choice c) {
		MethodElement.setOptionModifier(c);
	}

	public static void setFieldOptionFullName(Choice c) {
		FieldElement.setOptionFullName(c);
	}

	public static void setFieldOptionType(Choice c) {
		FieldElement.setOptionType(c);
	}

	public static void setFieldOptiondModifier(Choice c) {
		FieldElement.setOptionModifier(c);
	}

	public static void setOptionSimilaritylevel(double d) {
		MethodElement.setOptionSimilaritylevel(d);
	}

	@Override
	public boolean isAdaptable(URI uri, IProgressMonitor monitor) {
		// check if there is at least one java class
		File file = FileUtils.getFile(uri);
		if (file == null || !file.exists()) {
			return false;
		}
		return FileUtils.containsFileWithExtension(file, "java");
	}

	@Override
	public List<IElement> adapt(URI uri, IProgressMonitor monitor) {

		File file = FileUtils.getFile(uri);
		List<File> allFiles = FileUtils.getAllFiles(file);

		// get values of flags from the preference
		boolean selectPackages = Activator.getDefault().getPreferenceStore()
						.getBoolean(JavaUnderstandingAdapterPreferencePage.PACKAGE);
		boolean selectFiles = Activator.getDefault().getPreferenceStore()
						.getBoolean(JavaUnderstandingAdapterPreferencePage.FILES);
		boolean selectFileds = Activator.getDefault().getPreferenceStore()
						.getBoolean(JavaUnderstandingAdapterPreferencePage.FIELDS);
		boolean selectMEthods = Activator.getDefault().getPreferenceStore()
						.getBoolean(JavaUnderstandingAdapterPreferencePage.METHODS);
		boolean selectInterfaces = Activator.getDefault().getPreferenceStore()
						.getBoolean(JavaUnderstandingAdapterPreferencePage.INTERFACE);
		boolean selectSuperClass = Activator.getDefault().getPreferenceStore().getBoolean(JavaUnderstandingAdapterPreferencePage.SUPERCLASS);
		boolean selectImport = Activator.getDefault().getPreferenceStore()
						.getBoolean(JavaUnderstandingAdapterPreferencePage.IMPORTS);
	
		
		ArrayList<IElement> elements = new ArrayList<IElement>();
		if (selectPackages || selectFiles) {
			FileStructureAdapter f = new FileStructureAdapter();
			elements.addAll(f.adapt(uri, monitor));
		}
		for (File f : allFiles) {
			String fileName = f.toString();
			if (fileName.endsWith("java")) {
				try {
					// create the compilationUnit of the fileName
					CompilationUnit cu = extractAST(fileName);

					// Error in the compilation of the file
					if (cu == null)
						continue;

					Visitor v = new Visitor();
					// Extract the donnne of the AST
					cu.accept(v);

					TypeDeclaration typedeclaration = v.getTypes();

					PackageDeclaration packageDeclaration = v.getPackage();
					
					
					// if typedeclaration is null, then the files does not have any class (only enum)
					if (typedeclaration == null) {
						continue;

					}
					
					String className = typedeclaration.getName().getIdentifier();
					String packageName = "";

					if (packageDeclaration != null) {
						packageName = packageDeclaration.getName().getFullyQualifiedName();
					}

					// ImportDepandancy
					if (selectImport) {
						for (ImportDeclaration imp : v.getImports()) {
							elements.add(new ImportElement(packageName, className, imp));
						}
					}
					// SuperClassElement
					if (selectSuperClass) {
						Type superclassType = typedeclaration.getSuperclassType();
						SuperClassElement superClass = new SuperClassElement(packageName, className, superclassType);
						elements.add(superClass);
					}

					// Interfaces
					if (selectInterfaces) {
						if (typedeclaration.superInterfaceTypes().isEmpty()) {
							InterfacesElement interfaceElement = new InterfacesElement(packageName, className, "");
							elements.add(interfaceElement);
						}
						for (Object tempInterfaces : typedeclaration.superInterfaceTypes()) {
							Type interfaces = (Type) tempInterfaces;
							InterfacesElement interfaceElement = new InterfacesElement(packageName, className,
									interfaces.toString());
							elements.add(interfaceElement);
						}
					}

					// methods
					if (selectMEthods) {
						for (MethodDeclaration md : v.getMethods()) {
							elements.add(new MethodElement(md, packageName, className, md.getName().toString(),
									md.getReturnType2(), md.modifiers(), md.parameters(), md.getBody()));
						}

					}

					if (selectFileds) {
						// FieldDeclatration refere to a declaration line but can contains multiple
						// declaration (ex: int a,b;)
						for (FieldDeclaration field : v.getFields()) {
							List<VariableDeclarationFragment> names = field.fragments();
							for (VariableDeclarationFragment fieldName : names) {
								elements.add(new FieldElement(packageName, className, field,
										fieldName.getName().getIdentifier(), fieldName.getInitializer()));
							}
						}
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return elements;
	}

	/**
	 * compile a javafile and return his compilationUnit
	 * 
	 * @param path of the file
	 * @return the compilationUnit of the file
	 * @throws IOException
	 */
	public static CompilationUnit extractAST(String path) throws IOException {

		ASTParser parser = ASTParser.newParser(AST.JLS8);
		File resource = new File(path);
		java.nio.file.Path sourcePath = Paths.get(resource.toURI());
		CompilationUnit cu = null;
		try {
			String sourceString = new String(Files.readAllBytes(sourcePath));
			parser.setSource(sourceString.toCharArray());
			cu = (CompilationUnit) parser.createAST(null);
		} catch (IOException e) {
		}
		return cu;
	}

	@Override
	public void construct(URI uri, List<IElement> elements, IProgressMonitor monitor) {
		// Do Nothing
	}

	public void addMoreDependencies(List<IElement> elements, URI uri, IProgressMonitor monitor) {
		// Do Nothing
	}

}
