package es.um.program

import es.um.generator.FluentDSLGenerator

class MainProgram {
	static void main(String[] args) {
		if (args.length == 0 || args[0] == null) {
			println("Ruta no especificada")
		} else {
			def generator = new FluentDSLGenerator(args[0])
			generator.generate()
			generator.printToFile("C:/Users/PCx/Documents/workspace/TFG/out")
			
			println "Generado en: " + "C:/Users/PCx/Documents/workspace/TFG/out"
		}
	}
}
