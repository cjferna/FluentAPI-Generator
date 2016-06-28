package es.um.java.constructors

import java.lang.invoke.ConstantCallSite
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EAttribute
import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EClassifier
import org.eclipse.emf.ecore.EReference

import es.um.generator.ClassData
import es.um.generator.ClassPrinter
import es.um.generator.Utils
import es.um.java.JavaResult;
import es.um.java.control.JavaAttributeControl;
import es.um.java.control.JavaGeneralMethodsControl;
import es.um.java.control.JavaPackageControl;;

class JavaClass {

	protected ClassData classData
	protected JavaResult javaResult

	protected JavaPackageControl packageControl
	protected JavaAttributeControl attributesControl
	protected JavaGeneralMethodsControl setsMethodsControl

	JavaClass(ClassData classData) {
		this.classData = classData
		javaResult = new JavaResult(classData.diagramName, classData.builderClassName, classData.packageName)

		packageControl = new JavaPackageControl(classData.diagramName)
		setsMethodsControl = new JavaGeneralMethodsControl(classData, javaResult, packageControl)
	}

	JavaClass(String diagramName, String builderClassName, String packageName) {
		this.classData = new ClassData(null, null, null, null, diagramName, "", packageName)
		javaResult = new JavaResult(diagramName, builderClassName, packageName)

		packageControl = new JavaPackageControl(diagramName)
		setsMethodsControl = new JavaGeneralMethodsControl(classData, javaResult, packageControl)
	}

	def construct() {
		// TODO: Cuando es referenciando por más de uno
		generateAttributes() // TODO: No parece lo más apropiado
		generateConstructors()
		generateMethods()
		generateImports()
	}

	protected generateAttributes() {
		attributesControl = new JavaAttributeControl(classData, packageControl)
		attributesControl.generate()

		javaResult.putAttributes(attributesControl.getJavaAttributeList())
	}

	protected generateMethods() {
		javaResult.putMethods(attributesControl.getJavaSetMethodList())
		putMethod(setsMethodsControl.generateFatherEndMethod())
		putMethod(setsMethodsControl.generateFinalEndMethod())
		putMethod(setsMethodsControl.generateGetValueMethod())
	}

	private putMethod(String method) {
		if (method) {
			javaResult.putMethod(method)
		}
	}

	protected generateConstructors() {
		putConstructor(attributesControl.generateVoidConstructor())
		putConstructor(attributesControl.generateParameterizedConstructor())
	}

	protected generateImports() {
		packageControl.addModelClassImport(classData.eClass)
		javaResult.putImports(packageControl.getImports().asList())
	}

	protected putConstructor(String constructor) {
		if (constructor) {
			javaResult.putConstructor(constructor)
		}
	}

	def ClassPrinter getClassPrinter() {
		return new ClassPrinter(javaResult)
	}

}
