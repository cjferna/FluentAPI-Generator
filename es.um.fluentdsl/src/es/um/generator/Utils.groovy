package es.um.generator

import org.eclipse.emf.ecore.EClass

class Utils {
	public static final String END_LINE = System.getProperty("line.separator");

	// TODO: Mover a Utils
	static String firstToUpperCase(String string) {
		return new String(string.charAt(0).toUpperCase()) + string.substring(1,string.length())
	}

	// TODO: Mover a Utils
	static String firstToLowerCase(String string) {
		return new String(string.charAt(0).toLowerCase()) + string.substring(1,string.length())
	}

	static String toConstantConvention(String name) {
		def result = ""
		for (char ch in name) {
			if (Character.isUpperCase(ch)) {
				result += '_'
			}
			result += ch
		}

		return result.toUpperCase()
	}

	static String extractClassName(String fullName) {
		if (fullName.indexOf('.') != -1) {
			return fullName.substring(fullName.lastIndexOf('.') + 1, fullName.length())
		} else {
			return fullName;
		}
	}

	static printListIdented(list) {
		def string = ""
		for (listItem in list) {
			string += "|		${listItem}" + Utils.END_LINE
		}

		// Si había algo en la lista
		if (string.length() > 0) {
			return string.substring(0, string.length() - 1)	// Se elimina el último \n
		}
		return string;
	}

	static printListIdented(list, isMethod) {
		def string = ""
		for (listItem in list) {
			string += "|	${listItem}" + Utils.END_LINE + (isMethod ? Utils.END_LINE : "")
		}
		return string
	}

	static printList(list) {
		def string = ""
		for (listItem in list) {
			string += Utils.END_LINE + /${listItem}/
		}
		return string
	}

	static printClass(String uri, String data){
		try {
			def file = new File(uri)
			file.getParentFile().mkdirs()

			def fos = new FileOutputStream(file)
			fos.write(data.getBytes())
		} catch(IOException e){
			// TODO: Controlar Excepcion
			e.printStackTrace
		}
	}
}
