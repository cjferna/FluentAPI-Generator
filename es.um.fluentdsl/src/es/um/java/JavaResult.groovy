package es.um.java

import java.util.List

class JavaResult {

	private List<String> imports		// Lista de líneas de código que representan los imports.
	private List<String> attributes		// Lista de líneas de código que representan los atributos.
	private List<String> constructor	// Lista de líneas de código que representan los constructores.
	private List<String> methods		// Lista de líneas de código que representan los métodos del builder.
	String diagramName
	String builderClassName
	String packageName

	public JavaResult(String diagramName, String builderClassName, String packageName) {
		this.imports = []
		this.attributes = []
		this.constructor = []
		this.methods = []
		this.diagramName = diagramName
		this.builderClassName = builderClassName
		this.packageName = packageName
	}	
	
	public putImport(String importLineCode) {
		imports << importLineCode
	}
	
	public putImports(List<String> importsLineCode) {
		imports.addAll(importsLineCode)
	}
	
	public putAttributes(List<String> importsLineCode) {
		attributes.addAll(importsLineCode)
	}
	
	public putConstructor(String constructorLinesCodes) {
		constructor << constructorLinesCodes
	}
	
	public putMethod(String methodLinesCodes) {
		methods << methodLinesCodes
	}

	public putMethods(List<String> methodsLinesCodes) {
		methods.addAll(methodsLinesCodes)
	}
	
	public List<String> getImports() {
		return imports;
	}
	
	public List<String> getAttributes() {
		return attributes;
	}

	public List<String> getConstructor() {
		return constructor;
	}

	public List<String> getMethods() {
		return methods;
	}
	
}
