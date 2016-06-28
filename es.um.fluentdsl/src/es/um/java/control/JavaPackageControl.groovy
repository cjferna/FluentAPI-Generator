package es.um.java.control

import org.eclipse.emf.ecore.EClassifier

import com.sun.org.apache.bcel.internal.util.ClassPath;

class JavaPackageControl {
	
	private static final String[] PRIMITIVE_TYPES = ["void", "boolean", "byte", "char", "short", "int", "float", "double", "long"]
	
	private Set<String> imports	
	private String diagramName
	
	JavaPackageControl(String diagramName) {
		this.diagramName = diagramName
		imports = new HashSet<String>()
	}
		
	def boolean isPrimitive(String classPath) {		
		
		if (classPath.contains("java.lang.")) {
			return true
		}
		
		for (primitive in PRIMITIVE_TYPES) {
			if (classPath.equalsIgnoreCase(primitive)) {
				return true
			}
		}
		
		return false
	}	
	
	def addImport(EClassifier eClass) {
		addClassPathImport(eClass.instanceClassName)
	}
	
	def addModelClassImport(EClassifier eClass) {
		addClassPathImport(diagramName + "." + eClass.getName())
	}
	
	def addModelClassImport(String className) {
		addClassPathImport(diagramName + "." + className)
	}
	
	def addImplImport(EClassifier eClass) {
		addClassPathImport(diagramName + ".impl." + eClass.getName() + "Impl")
	}
	
	def addClassPathImport(String classPath) {
		if (classPath && !isPrimitive(classPath)/* && !isPartOfDsl(classPath)*/) {
			imports << "import " + classPath + ";"
		}
		
	}
	/*
	def boolean isPartOfDsl(EClassifier eClass) {
		return eClass.EPackage.name.equals(diagramName)
	}*/
	
	public Set<String> getImports() {
		return imports.collect()
	}

}
