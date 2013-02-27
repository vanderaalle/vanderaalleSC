// Extension to Class which allows to draw the the subclass graph. 
// Writes an ASCII file using the Dot graph language.  
// For rendering the .dot file see: http://www.graphviz.org/
// Inspired by Rohan Drape's dot classes.
// -> andrea valle, 12/12/06 
// http://www.semiotiche.it/andrea/


+ Class {

	subclassDictionary {
		var 	keys, 
			subclassDict = Dictionary[];
			
		keys = this.allSubclasses ;
		keys.do({ arg key, item ;
			subclassDict.put(key, key.subclasses )
				}) ;
				
		subclassDict.put(this, this.subclasses) ;

		^subclassDict
	}
	
	drawSubtree { arg file, shape = "box", options = "graph [bgcolor=gray100];
			node [color=navy, fontcolor=navy, fontsize=12, fillcolor=orange, fontname=Verdana];
			edge [color=gray60] ;" ;
		var subclassDict, header;
		subclassDict = this.subclassDictionary ;
		file.write("// generated via drawSubtree. av 2006\n") ;
		header = "digraph G \{\n"++options++format("\n node [shape=%]", shape) ;
		file.write(header);
		// keys are nodes
		subclassDict.keys.do({ arg key; 
					file.write("\""++key.name++"\";\n")
					});
		// key -> [ subclasses] : all the edges from key to each subclass  
		subclassDict.keys.do(
				{arg key; 
				subclassDict[key].do({ arg targetClass;
					file.write("\""++key++"\""++"->"++"\""++targetClass.name++"\";\n")
					});		
				});
		file.write("\}")
	}
	
}