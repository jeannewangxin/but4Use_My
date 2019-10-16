BUT4Reuse plugin for the creation of plant UML component model. I called it org.but4reuse.adapters.pstl.plantUML
but of course you can modify this name (if you modify it, take a care to modify also this information in the MANIFEST and 
in plugin.xml).

You can find here some instructions to refine this plugin

* Step1: You can modify the icon that represents the icon that you want to display for extraction of plant UML models. 
I proposed the use od the plant logo but you can modify it.

* You need modify  the plugn.xml to add the a dependency  to your BUT4Reuse adapter projevct (the project containing
the adapter class)

* The source code to implement the generation of the UML diagram can implemented in the class UMLComponentModelsExtractor. 
You have an example of a code to manipulate the adapted model and blocks..etc.