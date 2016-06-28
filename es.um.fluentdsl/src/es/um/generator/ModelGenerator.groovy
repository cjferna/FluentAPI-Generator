package es.um.generator

import java.io.File
import java.io.IOException
import java.util.Collections
import java.util.UUID

import org.apache.commons.io.FileUtils
import org.eclipse.core.runtime.IPath
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.Path
import org.eclipse.emf.codegen.ecore.generator.Generator
import org.eclipse.emf.codegen.ecore.generator.GeneratorAdapterFactory
import org.eclipse.emf.codegen.ecore.genmodel.GenModel
import org.eclipse.emf.codegen.ecore.genmodel.GenModelFactory
import org.eclipse.emf.codegen.ecore.genmodel.GenModelPackage
import org.eclipse.emf.codegen.ecore.genmodel.GenPackage
import org.eclipse.emf.codegen.ecore.genmodel.generator.GenBaseGeneratorAdapter
import org.eclipse.emf.codegen.ecore.genmodel.generator.GenModelGeneratorAdapterFactory
import org.eclipse.emf.common.util.BasicMonitor
import org.eclipse.emf.common.util.Diagnostic
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EPackage
import org.eclipse.emf.ecore.plugin.EcorePlugin
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl

class ModelGenerator {

	def createGenModel(String ecorepath, String outputPath) {
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl())
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("genmodel", new EcoreResourceFactoryImpl())

		IPath ecorePath = new Path(ecorepath)
		ResourceSet resourceSet = new ResourceSetImpl()
		resourceSet.getURIConverter().getURIMap()
				.putAll(EcorePlugin.computePlatformResourceToPlatformPluginMap(Collections.emptyList()))
		URI ecoreURI = URI.createFileURI(ecorePath.toString())
		Resource resource = resourceSet.getResource(ecoreURI, true)
		EPackage ePackage = (EPackage) resource.getContents().get(0)

		// Se fuerza a que siga el nombrado.
		ePackage.setNsPrefix(Utils.firstToUpperCase(ePackage.getNsPrefix()))

		IPath genModelPath = ecorePath.removeFileExtension().addFileExtension("genmodel")

		URI genModelURI = URI.createFileURI(genModelPath.toString())
		Resource genModelResource = Resource.Factory.Registry.INSTANCE.getFactory(genModelURI)
				.createResource(genModelURI)

		GenModel genModel = GenModelFactory.eINSTANCE.createGenModel()
		genModelResource.getContents().add(genModel)
		resourceSet.getResources().add(genModelResource)
		genModel.setModelDirectory("/" + "output")
		genModel.getForeignModel().add(ecorePath.toString())
		genModel.initialize(Collections.singleton(ePackage))

		GenPackage genPackage = (GenPackage) genModel.getGenPackages().get(0)
		// Nombre del archivo
		genModel.setModelName(genModelURI.trimFileExtension().lastSegment())

		genPackage.setPrefix(genPackage.getNSName())
		genPackage.setBasePackage("")

		File f = new File("output" + UUID.randomUUID().toString())
		f.mkdir()

		EcorePlugin.getPlatformResourceMap().put("output", URI.createFileURI(f.getAbsolutePath() + "/"))
		generate(genModel)

		try {
			FileUtils.copyDirectory(f, new File(outputPath))
			deleteDirectory(f)
		} catch (IOException e) {
			e.printStackTrace()
		}
	}

	def generate(GenModel genModel) {
		// Generate Code
		genModel.setCanGenerate(true)
		GeneratorAdapterFactory.Descriptor.Registry.INSTANCE.addDescriptor(GenModelPackage.eNS_URI,
				GenModelGeneratorAdapterFactory.DESCRIPTOR)

		// Create the generator and set the model-level input object.
		Generator generator = new Generator()
		generator.setInput(genModel)

		// Generator model code.
		Diagnostic d = generator.generate(genModel, GenBaseGeneratorAdapter.MODEL_PROJECT_TYPE,
				BasicMonitor.toMonitor(new NullProgressMonitor()))
	}

	def deleteDirectory(File path) {
		if (path.exists()) {
			File[] files = path.listFiles()
			
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i])
				} else {
					files[i].delete()
				}
			}
		}
		
		return (path.delete())
	}

}
