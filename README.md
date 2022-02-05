# FluentAPI-Generator

The fluent-API generator is a tool to automatically create a method chaining
Fluent-API (continous use of method calls) to create new Ecore models in
Eclipse Modeling Framework. Although the implementation language is groovy,
the resulting classes from the process are java classes ready to be used in
any language of the virtual java machine. 

## The work

The aim of this work is to create a Fluent-API generator for the EMF Java
API from a given metamodel. This facilitate manipulation of Ecore models.
The EMF API is not very productive and it is unreadable. Generator allows
to get a readable notation (Fluent-API) directly from an Ecore metamodel.
The work was part of a more ambitious project to obtain a
generator for any existing Java API. Therefore, the architecture of the
generator is designed taking into account that it has to be easily
extensible to support other languages and the construction of other
Fluent-API techniques. Specifically the component that analyzes the
metamodel is completely reusable. For this, the analyzer is implemented
with Reflective EMF API in order to obtain the information necessary for
the construction of the Java classes.

This idea came from the low productivity of the EMF API. To use the EMF API
a lot of code is needed. In addition, the code is very unreadable. This
makes it difficult to establish relationships between entities. It is a
fundamental aspect. Chaining method technique solves this problem.

## Input metamodel restrictions

In order to create a fluent-api, there is a restrictions that the input model must satisfy:
- Only one root class is supported. This is, only a meta-class do not beloging to another one trough a aggregation.
- If the reference mechanish is being used, every class should have an identifier property. Property 'id' or 'name' will be used as default if no other is specified in the identifier map. 

## Use example (Groovy)

```
def generator = new FluentDSLGenerator(ecoreFileUri)
generator.generate()
generator.printToFile(outFileUri)
```

## EcoreMetamodel example

The following code shows the use of the fluent-api generated to create models of a state machine metamodel.

```
StateMachine maquinaEstados = StateMachineBuilder
    .stateMachine()
        .name("Traffic light")
        .state()
            .name("Red")
        .endState()
        .state()
            .name("Green")
        .endState()
        .transition()
            .name("Transition to green")
            .source("Red")
            .target("Verde")
            .guard()
                .name("30 seconds")
            .endGuard()
            .trigger()
                .name("Turn off red light")
            .endTrigger()
        .endTransition()
    .end();
```
