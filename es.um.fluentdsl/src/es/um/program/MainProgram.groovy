package es.um.program

import es.um.generator.FluentDSLGenerator

class MainProgram {
	static void main(String[] args) {
		if (args.length == 0 || args[0] == null) {
			println("No output folder provided. Usage: MainProgram [outputFolder]")
		} else {
			def generator = new FluentDSLGenerator(args[0])
			generator.generate()
			generator.printToFile(args[0])
			
			println "Fluent-API classes generated at: " + args[0]
		}
	}
}
