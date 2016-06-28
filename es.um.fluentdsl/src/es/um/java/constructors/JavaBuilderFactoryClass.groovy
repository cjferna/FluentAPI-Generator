package es.um.java.constructors

import java.util.List
import java.util.Map;

import es.um.generator.Utils;

import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EReference

class JavaBuilderFactoryClass extends JavaClass {
	
	private List<JavaClass> javaGeneratedClasses
	private Map<String, List<EClass>> dslHierarchyMap
	private Map<String, List<EClass>> referencesMap
	private EClass initialClass
	
	JavaBuilderFactoryClass(String diagramName, String builderClassName, 
		String packageName, List<JavaClass> javaGeneratedClasses, EClass initialClass,
		Map<String, List<EClass>> dslHierarchyMap, Map<String, List<EClass>> referencesMap) {
		super(diagramName, builderClassName, packageName)
		this.javaGeneratedClasses = javaGeneratedClasses
		this.referencesMap = referencesMap
		this.initialClass = initialClass
		this.dslHierarchyMap = dslHierarchyMap
	}
	
	@Override
	public construct() {	
		def generatedClasses = new HashSet<String>()
		
		for (javaClass in javaGeneratedClasses) {
			def eClass = javaClass.classData.eClass
			def fatherEClass = dslHierarchyMap.get(eClass.name)?.get(0)
			def list = [fatherEClass]
			if (fatherEClass) {
				list.addAll(fatherEClass.eAllSuperTypes)
			}
			
			for (EClass superClass in list) {
				if (!generatedClasses.contains(eClass.name)) {
					if (!eClass.isAbstract() && !eClass.isInterface()) {	
						def references = referencesMap.get(eClass.name)
						
						if (superClass) {
							javaResult.putMethod(generateSpecificFactoryMethod(eClass, superClass))	
						} else if (references?.size() == 1) {
							javaResult.putMethod(generateSpecificFactoryMethod(eClass, references.get(0)))	
						} else {
							javaResult.putMethod(generateSpecificFactoryMethod(eClass))
						}			
						generatedClasses << eClass.name
					}		
				}
			}
		}
		
	}
	
	private String generateSpecificFactoryMethod(EClass referredClass, EClass father) {
		def referredBuilderName = getBuilderName(referredClass, father)
			
		def builderName = Utils.firstToUpperCase(referredClass.name) + "Builder"
		def parentBuilderName = Utils.firstToUpperCase(father.name) + "Builder"
		def parentBuilderNameFirstLower = Utils.firstToLowerCase(father.name) + "Builder"
		
		return generateSpecificFactoryMethod(referredBuilderName, builderName, parentBuilderName, parentBuilderNameFirstLower)
	}
	
	private String generateSpecificFactoryMethod(EClass referredClass) {
		def referredBuilderName = Utils.firstToUpperCase(referredClass.name) + "Builder"
		def builderName = Utils.firstToUpperCase(referredClass.name) + "Builder"
			
		return generateSpecificFactoryMethod(referredBuilderName, builderName, null, "")
	}
	
	private String getBuilderName(EClass eClass, EClass father) {
		for (JavaClass javaClass : javaGeneratedClasses) {
			def classData = javaClass.classData
			
			if (classData.eClass.equals(eClass)) {
				return classData.getBuilderClassName()
			}
		}	
		return ""
	}
	
	private String generateSpecificFactoryMethod(String referredBuilderName, String builderName, String parentBuilderName, String parentBuilderNameFirstLower) {
		def parameter = ""
		if (parentBuilderName) {
			parameter = parentBuilderName + " " + parentBuilderNameFirstLower
		}
		
		def result =
				/public static ${referredBuilderName} create${builderName}(${parameter}) {
			|		return new ${referredBuilderName}(${parentBuilderNameFirstLower});
			|	}/.stripMargin().toString()
			
		return result
	}
	
}
