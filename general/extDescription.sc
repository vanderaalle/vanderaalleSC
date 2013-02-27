/*
// extract from helpfile (if existant) the description string
+ Class {

	desc {
		var file, line, description, index ;
		var cln = this.name ;
		if ( this.helpFilePath.isNil) {^cln++": !! no available description (--> no help file)"} ;
		file = File(this.helpFilePath, "r") ;
		line = file.getLine ;
		description =
			block {arg break ;
				inf.do {
					if (line[..13].asSymbol == "<p class=\"p1\">".asSymbol )
						{ break.value(line) } ;
					line = file.getLine
					}
				} ;
		file.close ;
		description = description
			.replace("<p class=\"p1\">", "").replace("</p>", "")
			.replace("<b>", "").replace("</b>", "")
			.replace("<span class=\"Apple-tab-span\">", "").replace("</span>", "") ;
		// just to be sure
		10.do({ arg i ;
			description = description.replace("<span class=\"s"++i.asString++"\">", "") ;
				}) ;
		index = description.find(this.name.asString) ;
		if (index.isNil) {^cln++": !! can't extract description (--> unconsistent help file format)"} ;
		description = description[index+(this.name.asString.size)..]			.replace("\t", "") ;
		if ( description.size == 0 ) {^cln++": !! empty description in help file"} ;
		^cln++":"+description
	}

}
*/