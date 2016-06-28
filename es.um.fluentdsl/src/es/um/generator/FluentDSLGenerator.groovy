package es.um.generator

import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl

import java.util.regex.Pattern.Neg;

import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EPackage
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl

class FluentDSLGenerator {

	private Director director
	private List<EClass> clases
	private String dslName
	private String diagramName
	private String ecoreFile;

	FluentDSLGenerator(String file) {
		doEMFSetup()
		def resourceSet = new ResourceSetImpl()
		def resource = resourceSet.getResource(URI.createFileURI(file), true) /*val resource = resourceSet.getResource(URI.createURI(file), true)*/
		
		this.clases = getEClasses(resource)
		this.diagramName = extractDiagramName(resource)
		this.ecoreFile = file;
	}

	private extractDiagramName(Resource resource) {
		def contentProperty = resource.getProperties().get("contents")
		List<String> name = contentProperty.getAt("name")
		dslName = name?.get(0)
	}

	/**
	 * Genera los ficheros con las clases del metamodelo pasado como parámetro.
	 */
	def generate(String output) {
		director = new Director(clases, diagramName, ecoreFile)
		director.build()
		
	}

	def printToFile() {
		director.printToFile(dslName, ".")
		
		def modelGenerator = new ModelGenerator()
		modelGenerator.createGenModel(ecoreFile, "." + File.separator + "src")
	}

	def printToFile(String uri) {
		director.printToFile(dslName, uri)
		
		def modelGenerator = new ModelGenerator()
		modelGenerator.createGenModel(ecoreFile, uri + File.separator + "src")
	}

	/**
	 * Devuelve una lista de las EClass contenidas en el fichero.
	 */
	def List<EClass> getEClasses(Resource resource) {
		List<EClass> list = []
		for (ecoreResource in resource.getAllContents()) {
			if (ecoreResource in EClass) {
				list << ecoreResource;
			}
		}
		list
	}

	/**
	 * Configuración para leer un fichero ecore.
	 */
	def doEMFSetup() {
		Resource.Factory.Registry.INSTANCE.extensionToFactoryMap.put("xmi", new XMIResourceFactoryImpl())
		Resource.Factory.Registry.INSTANCE.extensionToFactoryMap.put("ecore", new XMIResourceFactoryImpl())
	}
}
