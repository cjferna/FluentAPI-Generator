package es.um.generator

import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EClass

import es.um.java.control.JavaAttributeControl;
import es.um.java.control.JavaGeneralMethodsControl;
import es.um.java.control.JavaPackageControl;;;

class ClassData {

	EClass eClass							// Entidad actual.
	EClass father							// Lista de Entidades que referencia a la entidad actual. 
	Map<String, List<EClass>> heritageMap	// Mapa de herencia de entidades en el diagrama.
	Map<String, List<EClass>> referencesMap	// Mapa de herencia de entidades en el diagrama.
	String diagramName						// Nombre del diagrama DSL.
	String initialModelClassName			// Nombre de la priemra entidad del DSL.
	String packageName

	public ClassData(EClass eClass, EClass father, Map<String, List<EClass>> heritageMap, 
		Map<String, List<EClass>> referencesMap, String diagramName, 
		String initialModelClassName, String packageName) {	
		this.eClass = eClass
		this.father = father
		this.heritageMap = heritageMap
		this.referencesMap = referencesMap
		this.diagramName = diagramName
		this.initialModelClassName = initialModelClassName
		this.packageName = packageName
	}	
	
	public String getClassName() {
		return Utils.firstToUpperCase(eClass.name)
	}
	
	public String getBuilderClassName() {
		return Utils.firstToUpperCase(eClass.name) + "Builder"
	}
	
}
