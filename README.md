# FluentAPI-Generator

El generador de Fluent-API contruye los paquetes y clases Java desde un metamodelo Ecore de entrada.

## Restricciones
Algunas restricciones son impuestas al metamodelo de entrada:
- Sólo una clase root.
- Toda clase debe tener una propiedad que sirva como identificador. (name o id es utilizado por defecto)

## Uso - Groovy
```
def generator = new FluentDSLGenerator(ecoreFileUri)
generator.generate()
generator.printToFile(outFileUri)
```
