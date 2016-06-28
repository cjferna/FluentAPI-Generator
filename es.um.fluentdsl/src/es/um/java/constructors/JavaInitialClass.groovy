package es.um.java.constructors

import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EClass

import es.um.generator.ClassData
import es.um.generator.Utils;

class JavaInitialClass extends JavaClass {
	
	JavaInitialClass(ClassData classData) {
		super(classData)
	}	
	
	@Override
	public construct() {
		generateAttributes() 		
		generateConstructors()		
		generateMethods()
		
		// Métodos especéficos de inicio
		generateInitialMethod()		
		generateImports()
	}
	
	@Override
	protected generateConstructors() {
		putConstructor(attributesControl.generateVoidConstructor(classData.builderClassName))
	}
	
	private generateInitialMethod() {
		def methodName = Utils.firstToLowerCase(classData.eClass.getName())
		javaResult.putMethod(
				/public static ${classData.builderClassName} ${methodName}() {
			|		return new ${classData.builderClassName}();
			|	}/
		)
	}
}
