/*
THE BABAKOTO PROJECT: BabaDocument
-a- started: 18/06/08

BabaDocument is an extension of Document.
It allows a single Return keystroke to parse (fast & furious)
commands written in the Roar
Being a subclass of Document SC code is evaluated as usual
More precisely, being a subclass of CocoaDocument, it is not
portable at the moment
Modelled after GeoDocument

Last updated: 030708

// andrea valle
// http://www.cirma.unito.it/andrea/
// andrea.valle@unito.it

*/

/*
BabaDocument : CocoaDocument {

	var babaParser ;

	*new { arg babaParser, alpha = 0.9 ;
		^super.new.initBabaDocument(babaParser, alpha)
	}

	initBabaDocument { arg aBabaParser, anAlpha = 0.9 ;
		babaParser = aBabaParser ;
		this
			.title_("The Roar Manuscript");
		this
			.string_("// Return --> speak Roar\n// Enter --> SC as usual\n")
			.keyDownAction_({ arg doc, key, modifiers, keycode ;
				if (keycode == 13, {
					babaParser.parse(this.currentLine)
				})
			})
			.stringColor_(Color(0.9,0.9,0.9))
			.background_(Color(0.3, 0, 0, anAlpha)) ;

	}
}

*/

