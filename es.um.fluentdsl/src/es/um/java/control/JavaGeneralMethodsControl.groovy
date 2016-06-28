package es.um.java.control

import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EAttribute
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference

import es.um.generator.ClassData
import es.um.generator.Utils
import es.um.java.JavaResult
import sun.security.x509.AttributeNameEnumeration;

class JavaGeneralMethodsControl {

	private ClassData classData
	private JavaResult javaResult
	private JavaPackageControl packageControl

	public JavaGeneralMethodsControl(ClassData classData, JavaResult javaResult, JavaPackageControl packageControl) {
		this.classData = classData
		this.javaResult = javaResult
		this.packageControl = packageControl
	}

	private boolean parentRefered(EReference reference) {
		def eClassRefered = reference.getEReferenceType()

		if (classData.father != null) {
			eClassRefered.equals(classData.father)
		} else {
			return false
		}
	}

	private boolean isBidirectionally(EReference reference) {
		def refered = reference.EOpposite
		return refered != null;
	}


	private String generateNoBidirectionallySetsMethods(String attributeFirstUpper, String attributeFirstLower, String variableName, EReference reference) {
		def result = ""
		if (reference.upperBound == 1) {
			if (reference.EReferenceType.isAbstract() || reference.EReferenceType.isInterface()) {
				result += generateSetsUniqueAbstractElement(reference, variableName)
			} else {
				def attributeName = Utils.firstToLowerCase(reference.EReferenceType.name)
				result += generateSetsUniqueElement(attributeFirstUpper, attributeFirstLower, attributeName, variableName)
			}
		} else {
			result += generateSetsMultipleElements(reference, variableName)
		}
		return result
	}

	private String generateBidirectionallySetsMethods(EReference reference, String attributeFirstUpper, String attributeFirstLower, String variableName) {
		def result = ""
		// Si solo refiere a un elemento.
		if (reference.upperBound == 1) {
			result += generateSetsUniqueElementBidirectionally(reference,
					attributeFirstUpper, attributeFirstLower, variableName)
		} else if (reference.upperBound != 1) {
			result += generateSetsMultipleElementsBidirectionally (reference, variableName)
		}
		return result
	}

	private String generateAttirubteSets(String variableName) {
		def result = ""
		for (EAttribute eAttribute in classData.eClass.EAllAttributes) {
			def attributeFirstUpper = Utils.firstToUpperCase(eAttribute.name)
			def attributeFirstLower = Utils.firstToLowerCase(eAttribute.name)
			result += /|		${variableName}.set${attributeFirstUpper}(this.${attributeFirstLower});/ + Utils.END_LINE
		}

		return result
	}

	// Metodos Sets para una relación uno a uno, bidireccional.
	private String generateSetsUniqueElementBidirectionally(EReference reference,
			String attributeFirstUpper, String attributeFirstLower, String variableName) {
		def result = ""
		def classNameFirstUp = Utils.firstToUpperCase(classData.eClass.name)
		result +=
				/|		${attributeFirstUpper} ${attributeFirstLower + "Var"} = this.${attributeFirstLower}.getValue();
		 |		${variableName}.set${attributeFirstUpper}(${attributeFirstLower + "Var"});
		 | 		${attributeFirstLower + "Var"}.set${classNameFirstUp}(${variableName});
		/ + Utils.END_LINE

		def propertyRefered = Utils.firstToUpperCase(reference.EType.name)
		packageControl.addModelClassImport(propertyRefered)

		return result
	}

	private String generateSetsUniqueElement(String attributeFirstUpper, String attributeFirstLower, String attributeName, String variableName) {
		return "|		${variableName}.set${attributeFirstUpper}(this.${attributeName} != null ? this.${attributeName}.getValue() : null);" + Utils.END_LINE
	}

	private String generateSetsUniqueAbstractElement(EReference reference, String variableName) {
		def childrenList = classData.heritageMap.get(reference.EReferenceType.name)

		def string = ""
		def elseWord = ""
		for (EClass child in childrenList) {
			def actual = child.name
			def abstractClassName = Utils.firstToUpperCase(reference.EReferenceType.name)
			def className = Utils.firstToUpperCase(actual)
			def variableClassName = Utils.firstToLowerCase(actual)

			string +=
					/|		${elseWord}if (${variableClassName} != null) {
			|			${variableName}.set${abstractClassName}(this.${variableClassName}.getValue());
			|		}
			/.stripMargin()

			if (elseWord.equals("")) {
				elseWord = "else "
			}
		}

		return string
	}

	// Metodos Sets para una relacion uno a muchos, bidireccional
	private String generateSetsMultipleElementsBidirectionally(EReference reference,
			String variableName) {
		def result = ""
		def diagramNameFirstUpper = Utils.firstToUpperCase(classData.diagramName)
		def classNameFirstUpper = Utils.firstToUpperCase(classData.eClass.getName())
		def propertyRefered = Utils.firstToUpperCase(reference.EType.name)	// Necesario debido a como se genera el modelo
		def propertyReferedName = Utils.firstToUpperCase(propertyRefered.toLowerCase())
		def getListMethodName = Utils.firstToLowerCase(reference.EType.name) + "s()"
		def classNameFirstUp = Utils.firstToUpperCase(classData.eClass.name)

		packageControl.addClassPathImport("java.util.ArrayList")
		packageControl.addClassPathImport("java.util.List")

		// TODO: No hablar con extraños. Get -> addAll
		result +=
				/|		List<${reference.EType.name}> ${reference.EType.name + "Var"} = ${getListMethodName};
			|		for (${reference.EType.name} iterableVar :${reference.EType.name + "Var"}) {
			|			iterableVar.set${classNameFirstUp}(${variableName});
			|		}
			|		${variableName}.get${propertyReferedName}().addAll(${reference.EType.name + "Var"});/ + Utils.END_LINE

		// El import si que se hace con el nombre real. Solo el metodo contiene el resto de la palabra en minuscula.
		packageControl.addModelClassImport(propertyRefered)

		return result
	}

	// Metodos Sets para una relacion uno a muchos.
	private String generateSetsMultipleElements(EReference reference, String variableName) {
		def result = ""
		def diagramNameFirstUpper = Utils.firstToUpperCase(classData.diagramName)
		def classNameFirstUpper = Utils.firstToUpperCase(classData.eClass.getName())
		def propertyRefered = Utils.firstToUpperCase(reference.name)	// Necesario debido a como se genera el modelo
		if (!propertyRefered) {
			propertyRefered = Utils.firstToUpperCase(reference.EType.name)
		}
		def propertyReferedName = Utils.firstToUpperCase(propertyRefered.toLowerCase())

		def subClasses = classData.heritageMap.get(reference.EType.name)
		if (subClasses != null && subClasses.size() > 0) {
			for (EClass eClass in subClasses) {
				def getListMethodName = Utils.firstToLowerCase(eClass.name) + "s()"
				result += "|		${variableName}.get${propertyReferedName}().addAll(${getListMethodName});" + Utils.END_LINE
			}
		} else {
			def getListMethodName = Utils.firstToLowerCase(reference.EType.name) + "s()"
			result = "|		${variableName}.get${propertyReferedName}().addAll(${getListMethodName});" + Utils.END_LINE
		}

		packageControl.addModelClassImport(reference.EType.name)
		packageControl.addClassPathImport("java.util.ArrayList")
		packageControl.addClassPathImport("java.util.List")

		return result
	}

	private generateIdentifierSet(String variableName) {
		def result = ""

		result += /|		ReferencesResolution instance = ReferencesResolution.getInstance();/ + Utils.END_LINE
		result += /|		String idFieldValue = instance.putIdentifier(this, MODEL_CLASS_NAME, ${variableName});/ + Utils.END_LINE

		return result
	}

	// TODO: Refactoring.
	private generateSetsMethodsAfterReferenceConstructor(String variableName) {
		def result = "";

		if (classData.father) {
			result = generateIdentifierSet(variableName);
			result += "|" + Utils.END_LINE
		}
		result += generateAttirubteSets(variableName);

		for (EReference reference in classData.eClass.EAllReferences) {

			def referencesList = classData.referencesMap.get(reference.EReferenceType.name)
			if (reference.isContainment() || referencesList.size() == 1) {
				def attributeFirstUpper = Utils.firstToUpperCase(reference.name)
				def attributeFirstLower = Utils.firstToLowerCase(reference.name)

				if (isBidirectionally(reference)) {
					result += generateBidirectionallySetsMethods(reference, attributeFirstUpper, attributeFirstLower, variableName)
				} else {
					result += generateNoBidirectionallySetsMethods(attributeFirstUpper, attributeFirstLower, variableName, reference)
				}
			} else {
				if (classData.father) {
					result += generatePutReference(reference)
				}
			}

		}
		return result
	}

	private generatePutReference(EReference reference) {
		def referredFieldId = Utils.firstToLowerCase(reference.name)

		def result = ""

		if (reference.upperBound == 1) {
			def methodName = "set" + Utils.firstToUpperCase(reference.name)
			result = /|		if (this.${referredFieldId} != null) {
						|			instance.putReference(idFieldValue, "${methodName}", this.${referredFieldId});
						|		}/ + Utils.END_LINE
		} else {

			def methodName = "get" + Utils.firstToUpperCase(reference.name)
			result = /|		if (this.${referredFieldId} != null) {
				|			instance.putMultipleReference(idFieldValue, "${methodName}", this.${referredFieldId}.toArray(new String[0]));
				|		}/ + Utils.END_LINE
		}

		return result

	}

	public String generateGetValueMethod() {
		def eClass = classData.eClass
		def factoryName = Utils.firstToUpperCase(classData.diagramName)
		def modelClassName = Utils.firstToUpperCase(eClass.name)
		def variableName = Utils.firstToLowerCase(eClass.name)

		def packageName = Utils.firstToLowerCase(classData.diagramName)
		packageControl.addClassPathImport(/${packageName}.${factoryName}Factory/)
		packageControl.addClassPathImport(/${packageName}.impl.${factoryName}FactoryImpl/)

		def result = ""
		if (!eClass.isAbstract() && !eClass.isInterface()) {
			result =
					/public ${classData.eClass.getName()} getValue() {
				|		${factoryName}Factory factory = ${factoryName}FactoryImpl.init();
				|		${modelClassName} ${variableName} = factory.create${modelClassName}();
				|
				|		${generateSetsMethodsAfterReferenceConstructor(variableName)}
				|		return ${variableName};
				|	}/.stripMargin().toString()
		}
		return result
	}

	public String generateFinalEndMethod() {
		packageControl.addModelClassImport(classData.initialModelClassName)
		def eClass = classData.eClass;

		if (classData.father) {
			// TODO: Hacer Casting si hay multiples padres.
			// TODO: Controlar Multiples Padres
			def result =
					/public ${classData.initialModelClassName} end() {
				|		if (parent != null) {
				|			return parent.end();
				|		} else {
				|			return null;
				|		}
				|	}/.stripMargin().toString()
			return result
		} else if (eClass.name.equals(classData.initialModelClassName) ){
			def className = Utils.firstToUpperCase(classData.className)
			def result =
					/public ${classData.initialModelClassName} end() {
				|		${className} value = getValue();
				|		ReferencesResolution instance = ReferencesResolution.getInstance();
				|		instance.execute();
				|		instance.purge();
				|
				|		return value;
				|	}/.stripMargin().toString()
			return result
		}
	}

	public String generateFatherEndMethod() {
		packageControl.addModelClassImport(classData.initialModelClassName)

		if (classData.father) {
			def className = Utils.firstToUpperCase(classData.eClass.getName())

			def result =
					/public ${classData.father.name + "Builder"} end${className}() {
				|		return parent;
				|	}/.stripMargin().toString()
			return result
		}
		return null
	}
}
