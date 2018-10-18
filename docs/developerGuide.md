# Developer Guide
This guide aims to give you an overview of the eMoflon::IBeX Sirius project to customize, extend or adapt the editor to fit your needs.
## Package Structure
This project is divided into the following packages
* **org.emoflon.ibex.tgg.editor.diagram**:  This is the **main package** and cotains the description of the editor (.odesign) and the code of all implemented Sirius services and wizards
* **org.emoflon.ibex.tgg.editor.diagram.core**: This package contains a custom Sirius `Session` class. This class is a custom version of `DAnalysisSession` from the Sirius repository and was needed due to the fact that in a TGG project the ecore metamodels are referenced multiple times (each `TripleGraphGrammarFile` object references the same ecore metamodels through its `imports` attribute). This property of TGG projects was producing errors with the default `Session` implementation used by Sirius 5.0.
* **org.emoflon.ibex.tgg.editor.diagram.ui**: In this package you will find different classes that provide a custom style for TGG correspondences and different launchers and handlers that allow the user to open TGG files and projects with the Sirius editor through the eMoflon context menu and the Sirius toolbar. This package also contains the implementation of the Xtext embedded editor.
## How to modify a graphical property of an element of the editor?
If you want to modify properties such as color, form and label style of an element, follow these steps:
1. Open the editor description that is located in `org.emoflon.ibex.tgg.editor.diagram.ui/description/editor.odesign`
2. Expand the description of the diagram representation that you want to modify. Currently there are only these diagram representation: *TGG Projects*, *TGG Rule* and *Complement Rule*.
3. Find the element you want to modify and edit the desired property.

For more information on this, check out the Sirius [starter tutorial](https://wiki.eclipse.org/Sirius/Tutorials/StarterTutorial#Improve_the_Style_of_the_Nodel)

## How to modify the behaviour and functionality of elements and tools of the editor?
 If you want to modify things such as what happens after elements are added or deleted, or what a tool does, then you have to modify the corresponding Sirius service (or in some cases the Acceleo query) that handles that event. To do this, find the element or tool as described in the previous section and then modify it accordingly. If the property/behaviour you want to change is defined by an Acceleo query, you can directly edit it. Otherwise, if this is defined by a service call, click on the text of the service call and press `F3` to open the implementation of the service.

 For more information on Acceleo queries and Sirius services, check out these links:  
 https://wiki.eclipse.org/Sirius/Tutorials/AdvancedTutorial#Java_Services  
 https://www.eclipse.org/sirius/doc/specifier/general/Writing_Queries.html

## Structure of the editor description (editor.odesign)
The editor description is composed of the description of the diagram representations *TGG Projects*, *TGG Rule* and *Complement Rule*, where the last two have the following layers in addition to the default layer:
* Node Color: Layer that turns source nodes yellow and target nodes red
* Global View: Layer that shows global elements, i.e. elements defined in the parents of the rule (or complement rule).

More information about layers: https://wiki.eclipse.org/Sirius/Tutorials/AdvancedTutorial#Layers
