package es.um.generator

import es.um.java.JavaResult

class ClassPrinter {

	private JavaResult javaResult

	public ClassPrinter(JavaResult javaResult) {
		this.javaResult = javaResult;
	}

	// TODO: Extraer Clase
	def String printClass() {
		/package ${javaResult.diagramName + ".builders"};
		|${javaResult.getImports() ? Utils.printList(javaResult.getImports()) : ""}
		|
		|public class ${javaResult.builderClassName} {
		|
			${Utils.printListIdented(javaResult.getAttributes(), false)}
			${Utils.printListIdented(javaResult.getConstructor(), true)}
			${Utils.printListIdented(javaResult.getMethods(), true)}
		|}/.stripMargin()
	}

	private generateURI(String uri) {
		def packageDirectory = ""
		for (packageName in javaResult.packageName.split("\\.")) {
			packageDirectory += packageName + File.separator
		}

		return uri + File.separator + packageDirectory + File.separator + "builders" + File.separator + javaResult.builderClassName + ".java"
	}

	/**
	 * Para el objeto contenedor del código, pasado como parámetro, crea el fichero en el sistema
	 * con el código correspondiente
	 */
	def printClass(String uri){
		try {
			def file = new File(generateURI(uri))
			file.getParentFile().mkdirs()

			def fos = new FileOutputStream(file)
			fos.write(printClass().getBytes())
		} catch(IOException e){
			// TODO: Controlar Excepcion
			e.printStackTrace
		}
	}

}
