package es.um.generator

import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EReference

import es.um.java.constructors.JavaBuilderFactoryClass;
import es.um.java.constructors.JavaClass;
import es.um.java.constructors.JavaInitialClass;
import es.um.java.constructors.JavaReferenceResolutionClass

class Director {

	static final FACTORY_NAME = "BuilderFactory"

	private List<EClass> eClassesList					// Lista de las clases generadas.
	private Map<String, List<EClass>> heritageMap		// Mapa de herencia de entidades.
	private Map<String, List<EClass>> dslHierarchyMap 	// Mapa de relaciones de composición de jerarquía del DSL.
	private Map<String, List<EClass>> referencesMap 	// Mapa de relaciones de jerarquía del DSL.
	private List<JavaClass> javaGeneratedClasses		// Lista de Classes Java Generadas. TODO: Solo JAVA?
	private String diagramName							// Nombre del diagrama, representa el nombre de los paquetes.

	Director(List<EClass>list, String diagramName, String ecoreFile) {
		this.eClassesList = list;
		this.diagramName = diagramName;
		javaGeneratedClasses = []
		heritageMap = [:]
		dslHierarchyMap = [:]
		referencesMap = [:]
	}

	/**
	 * Construye todas las clases.
	 */
	def build() {
		def ecoreClassesGenerated = new HashSet<String>()

		def initialClass = preProcessing()
		def initialModelClassName = Utils.firstToUpperCase(initialClass.name)
		constructInitialClass(initialClass, initialModelClassName, ecoreClassesGenerated)

		def classData
		for (eClass in eClassesList) {
			classData = generateJavaClass(classData, eClass, initialModelClassName, initialClass, ecoreClassesGenerated)
			ecoreClassesGenerated << classData.getClassName()
		}

		generateBuilderFactory(initialClass)
	}

	private ClassData generateJavaClass(ClassData classData, EClass eClass, String initialModelClassName,
			EClass initialClass, HashSet ecoreClassesGenerated) {

		EClass father = dslHierarchyMap.get(eClass.name)?.get(0)
		if (!father && referencesMap.get(eClass.name)?.size() == 1) {
			father = referencesMap.get(eClass.name)?.get(0)
		}

		classData = new ClassData(eClass, father, heritageMap, referencesMap, diagramName,
				initialModelClassName, eClass.EPackage.getName())
		def javaClass = new JavaClass(classData)
		construct(eClass, dslHierarchyMap.get(initialClass.name), javaClass, initialModelClassName, ecoreClassesGenerated)

		return classData
	}

	private ClassData constructInitialClass(EClass initialClass, String initialModelClassName, HashSet ecoreClassesGenerated) {
		def classData = new ClassData(initialClass, dslHierarchyMap.get(initialClass.name), heritageMap, referencesMap, diagramName,
				initialModelClassName, initialClass.EPackage.getName())
		def javaClass = new JavaInitialClass(classData)
		construct(initialClass, null, javaClass, initialModelClassName, ecoreClassesGenerated)

		return classData
	}



	/**
	 * Rellena los mapas de Herencia de Entidades, y de Relacion de entidades (HierarchyMap)
	 * @return EClass, la clase inicial. Aquella no referenciada por ningún elemento.
	 */
	private EClass preProcessing() {
		setMaps()
		addSubClassesSuperClassMaps()

		return searchInitialClass()
	}

	private addSubClassesSuperClassMaps() {
		for(EClass eClass in eClassesList) {
			for (EClass eSuperClass in eClass.getEAllSuperTypes()) {
				addAllListOnList(eClass, eSuperClass, dslHierarchyMap)
				addAllListOnList(eClass, eSuperClass, referencesMap)
			}
		}
	}

	private setMaps() {
		for(EClass eClass in eClassesList) {

			for (EReference reference in eClass.EAllReferences) {
				if (reference.isContainment()) {
					putItemOnMapList(eClass, reference.EReferenceType, dslHierarchyMap)
				}
				putItemOnMapList(eClass, reference.EReferenceType, referencesMap)

			}

			if (!eClass.isAbstract() && !eClass.isInterface()) {
				searchSuperClasses(eClass);
			}
		}
	}

	/**
	 * @param eClass
	 */
	private void searchSuperClasses(EClass eClass) {
		for(EClass eSuperClass in eClass.getEAllSuperTypes()) {
			putItemOnMapList(eClass, eSuperClass, heritageMap)
		}
	}


	/**
	 * Devuelve la clase inicial desde la cual empieza el DSL.
	 * @return EClass, la primera clase que no es referenciada por nadie.
	 */
	private EClass searchInitialClass() {
		for(EClass eClass in eClassesList) {
			if (!eClass.isAbstract() && !eClass.isInterface()) {
				if (!dslHierarchyMap.containsKey(eClass.name)) {
					if (constainsReferences(eClass)) {
						return eClass
					}
				}
			}
		}
	}

	private constainsReferences(EClass eClass) {
		for (String key in referencesMap.keySet()) {
			if (key.equals(eClass.name)) {
				return false
			}
		}
		return true
	}

	/**
	 * Introduce el parametro 'father' en el mapa 'map' con la clave 'child'.
	 * Especifica, que se ha encontrado un padre 'father' para un clase hija 'child'.
	 * Si no existe la entrada en el mapa, se crea la lista, se introduce el elemento y se asocia al mapa.
	 *
	 * @param father Elemento a almacenar.
	 * @param child Clave del mapa donde almacenar a 'father'
	 * @param map Mapa donde almacenar el elemento.
	 * @return
	 */
	private putItemOnMapList(EClass father, EClass child, Map<String, List<EClass>> map) {
		def list = map.get(child.getName())

		if (!list) {
			list = []
			map.put(child.getName(), list)
		}

		list << father
	}

	private addAllListOnList(EClass eClass, EClass eSuperClass, Map<String, List<String>> map) {
		def superClassList = map.get(eSuperClass.name)

		if (superClassList) {
			def list = map.get(eClass.name)
			if (list == null) {
				map.put(eClass.name, [])
				list = map.get(eClass.name)
			}

			list.addAll(superClassList)
		}
	}

	// TODO: Refeactoring
	/**
	 * Construye la entidad 'eClass' y llama a crear a todas las entidades que referencia.
	 * @param eClass	Entidad a generar.
	 * @param father	Padre que la referencia.
	 * @param javaClass JavaClass a generar
	 * @param initialModelClassName Nombre de la entidad inicial del DSL.
	 * @param ecoreGeneratedClasses Clases generadas.
	 * @return
	 */
	private construct(EClass eClass, EClass father, JavaClass javaClass, String initialModelClassName, HashSet<String> ecoreClassesGenerated) {
		if (!ecoreClassesGenerated.contains(eClass.name)) {
			javaGeneratedClasses << javaClass
			javaClass.construct()

			ecoreClassesGenerated << eClass.name
			generateClassesReferrered(eClass, initialModelClassName, ecoreClassesGenerated)
		}

	}

	private generateClassesReferrered(EClass eClass, String initialModelClassName, HashSet ecoreClassesGenerated) {
		for (EReference reference in eClass.EAllReferences) {
			if (reference.isContainment()) {
				def referredClass = reference.getEReferenceType()

				if (referredClass.isAbstract() || referredClass.isInterface()) {
					generateAllSubclasses(referredClass, eClass, initialModelClassName, ecoreClassesGenerated)
				} else {
					generateClass(referredClass, eClass, initialModelClassName, ecoreClassesGenerated)
				}
			}
		}
	}

	/**
	 * @param referredClass
	 * @param father
	 * @param initialModelClassName
	 * @param ecoreClassesGenerated
	 * @return
	 */
	private generateAllSubclasses(EClass referredClass, EClass father, String initialModelClassName, HashSet<String> ecoreClassesGenerated) {
		for (EClass subClass in heritageMap.get(referredClass.name)) {
			generateClass(subClass, father, initialModelClassName, ecoreClassesGenerated)
		}
	}

	/**
	 * @param eClass
	 * @param father
	 * @param initialModelClassName
	 * @param ecoreClassesGenerated
	 * @return
	 */
	private generateClass(EClass eClass, EClass father, String initialModelClassName, HashSet<String> ecoreClassesGenerated) {
		def classData = new ClassData(eClass, father, heritageMap, referencesMap, diagramName,
				initialModelClassName, eClass.EPackage.name)
		def newJavaClass = new JavaClass(classData)

		construct(eClass, father, newJavaClass, initialModelClassName, ecoreClassesGenerated)
	}


	/**
	 * Crea la clase factoría de builders.
	 */
	def generateBuilderFactory(EClass initialClass) {
		def javaClass = new JavaBuilderFactoryClass(diagramName, FACTORY_NAME,
				initialClass.EPackage.name, javaGeneratedClasses, initialClass,
				dslHierarchyMap, referencesMap)
		javaClass.construct()
		javaGeneratedClasses << javaClass
	}

	/**
	 * Imprime las clases en la URI 'uri' pasada como parámetro.
	 * @param dslName Especifica el nombre del paquete.
	 * @param uri URI donde almacenar las clases.
	 * @return
	 */
	def printToFile(String dslName, String uri) {
		for(JavaClass javaGeneratedClass in javaGeneratedClasses) {
			def classPrinter = javaGeneratedClass.getClassPrinter()
			classPrinter.printClass(uri + File.separator + "src" + File.separator);
		}

		def packageName = eClassesList.get(0)?.EPackage.name
		def referenceResolutionClass = new JavaReferenceResolutionClass(packageName)

		printClass(uri, packageName, "BuilderOptions", referenceResolutionClass.getBuilderOptions())
		printClass(uri, packageName, "ReferencesResolution", referenceResolutionClass.getReferencesResolution())
		printClass(uri, packageName, "Reference", referenceResolutionClass.getReference())
		printClass(uri, packageName, "ReferenceMultiple", referenceResolutionClass.getReferenceMultiple())

	}

	private printClass(String uri, String packageName, String name, String data) {
		Utils.printClass(uri + File.separator + "src" + File.separator + packageName
				+ File.separator + "builders" + File.separator + name + ".java"
				, data)
	}

}
