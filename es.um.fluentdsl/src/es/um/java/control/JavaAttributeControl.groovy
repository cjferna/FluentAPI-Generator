package es.um.java.control

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList
import org.eclipse.emf.ecore.EAttribute
import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EReference

import es.um.generator.ClassData
import es.um.generator.Utils;

class JavaAttributeControl {
	
	private ClassData classData
	
	private EClass father
	private List<String> javaAttributeList
	private List<String> javaListsList
	private List<String> javaConstantsList
	private List<String> javaMethodsList
	private List<String> javaGetMethodsList
	
	private JavaPackageControl packageControl;

	JavaAttributeControl(ClassData classData, JavaPackageControl packageControl) {
		this.classData = classData		
		this.packageControl = packageControl
		this.father = classData.father

		javaAttributeList = []
		javaListsList = []
		javaConstantsList = []
		javaMethodsList = []
		javaGetMethodsList = []
		
		putConstants();
	}
	
	def generate() {
		for(EAttribute attribute in classData.eClass.EAllAttributes) {
			generateAttribute(attribute)
		}

		printParentAttribute(father)

		generateRefereces()
	}
	
	private putConstants() {
		def type = "String"
		def name = "MODEL_CLASS_NAME"
		def value = /"${Utils.firstToUpperCase(classData.className)}"/

		putConstant(type, name, value)
	}

	private putConstant(String type, String name, String value) {
		javaConstantsList << /private static final ${type} ${name} = ${value};/
	}

	private boolean parentRefered(EReference reference) {
		def eClassRefered = reference.getEReferenceType()
		return eClassRefered.equals(father)
	}


	private generateRefereces() {
		for(EReference reference in classData.eClass.EAllReferences) {			
			
			def referencesList = classData.referencesMap.get(reference.EReferenceType.name)
			if (reference.isContainment() || referencesList.size() == 1) {
				def referedClass = reference.EReferenceType
				
				if (referedClass.isAbstract() || referedClass.isInterface()) {
					generateSubClassesMethods(reference, referedClass)
				} else {
					generateCompositionReferenceClassMethods(reference)
				}
			} else {
				generateReference(reference)
			}
		} 
		
		
	}
	
	private generateReference(EReference reference) {
		
		if (reference.upperBound == 1) {
			generateUniqueReference(reference)
			printReferenceAttribute(reference)
		} else {
			generateMultipleReference(reference)	
			printMultipleReferenceAttribute(reference)	
		}
		
	}
	
	private generateMultipleReference(EReference reference) {
		def attributeName = Utils.firstToLowerCase(reference.name)
		def className = Utils.firstToUpperCase(classData.className)
		
		putListImports()
		
		javaMethodsList <<
				/public ${className}Builder ${attributeName}(String ...identifier) {
		|		${attributeName}.addAll(Arrays.asList(identifier));
		|
		|		return this;
		|	}/
	}

	private generateUniqueReference(EReference reference) {
		def attributeName = Utils.firstToLowerCase(reference.name)
		def className = Utils.firstToUpperCase(classData.className)		
		
		putListImports()

		javaMethodsList <<
				/public ${className}Builder ${attributeName}(String identifier) {
		|		${attributeName} = identifier;
		|
		|		return this;
		|	}/
	}

	private printReferenceAttribute(EReference reference) {
		def name = Utils.firstToLowerCase(reference.name)
		def type = "String"
		
		printAttribute(type, name)
	}
	
	private printMultipleReferenceAttribute(EReference reference) {
		def name = Utils.firstToLowerCase(reference.name)
		def type = "List<String>"
		
		putListImports()
		packageControl.addClassPathImport("java.util.Arrays")
		
		javaListsList << /${name} = new ArrayList<String>();/
		
		printAttribute(type, name)
	}	

	private generateCompositionReferenceClassMethods(EReference reference) {
		generateAttributeCompositionReference(reference)

		if (reference.upperBound != -1) {
			generateCompositionReferenceBuilderMethod(reference)
		} else {
			generateList(reference)
			generateMultipleCompositionReferenceBuilderMethod(reference)
			generateGetListMethod(reference)
		}
	}

	private generateSubClassesMethods(EReference reference, EClass referedClass) {

		for (EClass subClass in classData.heritageMap.get(referedClass.name)) {
			generateAttributeCompositionReference(subClass)

			if (reference.upperBound != -1) {
				generateCompositionReferenceBuilderMethod(subClass)
			} else {
				generateList(subClass)
				generateMultipleCompositionReferenceBuilderMethod(subClass)
				generateGetListMethod(subClass)
			}
		}
	}

	private generateCompositionReferenceBuilderMethod(EReference reference) {
		def variableName = Utils.firstToLowerCase(reference.EReferenceType.name)
		def className = Utils.firstToUpperCase(reference.EReferenceType.name)

		javaMethodsList <<
		/public ${className}Builder ${variableName}() {
		|		if (${variableName} != null) {
		|			throw new IllegalStateException("\'${variableName}\' has been set above. It can only be set one time.");
		|		}
		|
		|		${variableName} = BuilderFactory.create${className}Builder(this);
		|
		|		return ${variableName};
		|	}/
	}

	private generateCompositionReferenceBuilderMethod(EClass subClass) {
		def variableName = Utils.firstToLowerCase(subClass.name)
		def className = Utils.firstToUpperCase(subClass.name)

		javaMethodsList <<
		/public ${className}Builder ${variableName}() {
		|		if (${variableName} != null) {
		|			throw new IllegalStateException("\'${variableName}\' has been set above. It can only be set one time.");
		|		}
		|
		|		${variableName} = BuilderFactory.create${className}Builder(this);
		|
		|		return ${variableName};
		|	}/
	}

	private generateMultipleCompositionReferenceBuilderMethod(EReference reference) {
		def variableName = Utils.firstToLowerCase(reference.EType.name)
		def listName = Utils.firstToLowerCase(reference.EType.name) + "List"
		def className = Utils.firstToUpperCase(reference.EType.name)

		putListImports()
		
		javaMethodsList <<
			/public ${className}Builder ${variableName}() {
		|		if (${variableName} != null) {
		|			${listName}.add(${variableName}.getValue());
		|		}
		|
		|		${variableName} = BuilderFactory.create${className}Builder(this);
		|		return ${variableName};
		|	}/
	}

	private generateMultipleCompositionReferenceBuilderMethod(EClass subClass) {
		def variableName = Utils.firstToLowerCase(subClass.name)
		def listName = Utils.firstToLowerCase(subClass.name) + "List"
		def className = Utils.firstToUpperCase(subClass.name)

		putListImports()
		
		javaMethodsList <<
			/public ${className}Builder ${variableName}() {
		|		if (${variableName} != null) {
		|			${listName}.add(${variableName}.getValue());
		|		}
		|
		|		${variableName} = BuilderFactory.create${className}Builder(this);
		|		return ${variableName};
		|	}/
	}
	private generateList(EReference reference) {
		def eType = reference.EReferenceType
		def name = Utils.firstToLowerCase(eType.name) + "List"
		def type = /List<${eType.name}>/

		putListImports()	
		packageControl.addModelClassImport(eType)
		
		printAttribute(type, name);
		javaListsList << /${name} = new ArrayList<${eType.name}>();/

		putListImports()
	}

	private generateList(EClass subClass) {
		def name = Utils.firstToLowerCase(subClass.name) + "List"
		def type = /List<${subClass.name}>/
		
		putListImports()	
		packageControl.addModelClassImport(subClass)
		
		printAttribute(type, name);
		javaListsList << /${name} = new ArrayList<${subClass.name}>();/

		putListImports()
	}

	private generateGetListMethod(EReference reference) {
		def variableName = Utils.firstToLowerCase(reference.EType.name)
		def listName = Utils.firstToLowerCase(reference.EType.name) + "List"
		def className = Utils.firstToUpperCase(reference.EType.name)

		putListImports()
		
		javaMethodsList <<
			/private List<${className}> ${variableName}s() {
		|		List<${className}> list = new ArrayList<${className}>();
		|		list.addAll(${listName});
		|
		|		if (${variableName} != null) {
		|			list.add(${variableName}.getValue());
		|		}
		|
		|		return list;
		|	}/

	}

	private generateGetListMethod(EClass subClass) {
		def variableName = Utils.firstToLowerCase(subClass.name)
		def listName = Utils.firstToLowerCase(subClass.name) + "List"
		def className = Utils.firstToUpperCase(subClass.name)

		putListImports()
		
		javaMethodsList <<
			/private List<${className}> ${variableName}s() {
		|		List<${className}> list = new ArrayList<${className}>();
		|		list.addAll(${listName});
		|
		|		if (${variableName} != null) {
		|			list.add(${variableName}.getValue());
		|		}
		|
		|		return list;
		|	}/

	}

	private putListImports() {
		packageControl.addClassPathImport("java.util.List")
		packageControl.addClassPathImport("java.util.ArrayList")
	}

	private generateAttributeCompositionReference(EReference reference) {
		def eType = reference.EReferenceType
		def name = Utils.firstToLowerCase(eType.name)
		def type = eType.name + "Builder"

		printAttribute(type, name)
	}

	private generateAttributeCompositionReference(EClass referencedClass) {
		def name = Utils.firstToLowerCase(referencedClass.name)
		def type = Utils.firstToUpperCase(referencedClass.name) + "Builder"

		printAttribute(type, name)
	}

	private generateAttribute(EAttribute attribute) {
		def name = attribute.name
		def type = Utils.extractClassName(attribute.EType.instanceClassName)
	
		packageControl.addImport(attribute.EType)

		printAttribute(type, name)
		if (attribute.defaultValue) {
			printDefaultValue(attribute)
		}
			
		javaPutSetMethod(type, name)
	}

	private printDefaultValue(EAttribute attribute) {
		def type = attribute.EType.instanceClassName
		def name = "DEFAULT_" + Utils.toConstantConvention(attribute.name)
		def value = attribute.defaultValueLiteral
		
		if (!value) {
			value = attribute.defaultValue
		}
		
		if (type.equals("String")) {
			value = /"${value}"/
		} else if (type.equals("char")) {
			value = /'${value}'/
		}

		putConstant(type, name, value)
	}

	private printParentAttribute(EClass parent) {
		if (parent) {	// TODO: no parece el sitio mas adecuado
			packageControl.addImport(parent)
			printAttribute(parent.getName() + "Builder", "parent")
		}
	}

	private printAttribute(String type, String name) {
		javaAttributeList << /private ${type} ${name};/
		
		generateGetMethod(name, type);
	}

	private generateGetMethod(String name, String type) {
		def methodName = Utils.firstToUpperCase(name)
		
		if (!name.equals("parent")) {
			javaGetMethodsList <<
					/${type} get${methodName}() {
			|		return ${name};
			|	}/.stripMargin()
		}
	}

	def javaPutSetMethod(String type, String name) {

		javaMethodsList <<
			/public ${classData.eClass.name + "Builder"} ${name}(${type} ${name}) {
		|		this.${name} = ${name};
		|		return this;
		|	}/.stripMargin();
	}

	def putReferenceAttribute(type, name) {
		javaAttributeList << /private ${type} ${name};/
	}

	public List<String> getJavaAttributeList() {
		return javaConstantsList + javaAttributeList
	}

	public List<String> getJavaSetMethodList() {
		return javaMethodsList + javaGetMethodsList
	}

	public String generateParameterizedConstructor(String className, String  fatherClassName) {
		def attributeDefaultValueAssignment = printAttributeListIdented()
		def lists = Utils.printListIdented(javaListsList)

			/public ${className}(${fatherClassName} parent) {
				${attributeDefaultValueAssignment}${lists}
		|		this.parent = parent;
		|	}/.stripMargin()
	}

	public generateVoidConstructor(String className) {
		def attributeDefaultValueAssignament = printAttributeListIdented()
		def lists = Utils.printListIdented(javaListsList)

			/public ${className}() {
				${attributeDefaultValueAssignament}${lists}
		|	}/.stripMargin()
	}
	
	public generateParameterizedConstructor() {
		if (classData.father) {
			def fatherClassName = classData.father.name + "Builder"
			
			return generateParameterizedConstructor(classData.builderClassName, fatherClassName)
		}
	}
	
	public generateVoidConstructor() {
		if (classData.father) {
				/public ${classData.builderClassName}() {
			|		this(null);
			|	}/.stripMargin()
		}
	}

	private printAttributeListIdented() {
		def string = ""
		for (EAttribute attribute in classData.eClass.EAllAttributes) {
			if (attribute.defaultValueLiteral) {
				def attirubteName = Utils.firstToLowerCase(attribute.getName())
				def attirubteDefaultValueName = Utils.toConstantConvention(attribute.name)
	
				string += "|		this.${attirubteName} = DEFAULT_${attirubteDefaultValueName};" + Utils.END_LINE
			}
		}

		return string
	}

}
