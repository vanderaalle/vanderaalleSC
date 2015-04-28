/*

A Lexer for SC, returns tokenization ad an array of two lists, one including lexemes, and the other their categories

AV scripsit 24/02/13
*/

// NOTE: Elaborate stuff into comments can break the lexer and the colorizer

SCLexer {

	var <>source ; // the source code, multi line
	var <>separators ; // separator list
	var <>reserved ; // reserved words

	*new { arg source ;
		^super.new.initSCLexer(source) ;
	}

	initSCLexer { arg aSource ; // you can pass code or file
		var file ;
		if (File.exists(aSource)) {
			file = File(aSource, "r") ;
			source = file.readAllString;
			file.close ;
		}{
			source = aSource ;
		} ;
		separators =	"\n > < [ ] ( ) { } \t = + - , . : ; ! ? ^ * / & % # @ $"
		.split($ ).collect{|i| i.asSymbol}++[' '] ;
		reserved = "var arg inf nil true false pi 2pi this thisProcess thisThread thisFunction"
		.split($ ).collect{|i| i.asSymbol} ;
	}

	evaluate { arg lexemeList ;
		var type, typeList = [] ;
		lexemeList.do{|lexeme|
			lexeme = lexeme.asString ;
			case
			{ lexeme[..1].asSymbol == '/*'} {type = \multiComment }
			{ lexeme[0..1].asSymbol == '//'} {type = \comment }
			{ separators.includes(lexeme[0].asSymbol) }
			{ type = \separator }
			{ lexeme[0] == $~ } {type = \envVar }
			{ lexeme[0].isDecDigit} {type = \number }
			{ lexeme[0] == $'} {type = \symbol }
			{ lexeme[0] == $\\ } {type = \symbol }
			{ lexeme[0] == $" } {type = \string }
			{ lexeme[0] == $$ } {type = \char }
			{ lexeme[0] == $_ } {type = \primitive }
			{ lexeme[0].isUpper} {type = \class }
			{ reserved.includes(lexeme.asSymbol)} {type = \reserved}
			{ lexeme[0].isLower} {type = \name } ;
		//[lexeme,lexeme[0], type].postln ;"\n".postln ;
		typeList = typeList.add(type)
		} ;
		^typeList
	}


	scan {
		//arg line ;
		var p = Pseq(source).asStream ;
		var lexeme ;
		var lexemeList = [] ; // an array containing the lexemes
		var buf ;
		lexeme = p.next ;

		// comments
		// TODO: / is fine but not vibisble
		// OSC msgs are problematic
		// careful with changing the order
		while {lexeme != nil} {
			case
			{ lexeme == $/}
			{ buf = lexeme ; lexeme = p.next ;
				// single comment
				case
				{lexeme == $/}{

				while {[nil, $\n].includes(lexeme).not}
					{buf = buf++lexeme; lexeme = p.next };
						lexemeList = lexemeList.add(buf) ;
					//lexeme = p.next
				}
				// multiline comment
				{lexeme == $*}
				{  buf = buf++lexeme ;
					lexeme = p.next;
					while { lexeme != $/}
					{ buf = buf++lexeme; lexeme = p.next  } ;
					buf = buf++lexeme ;
					lexemeList = lexemeList.add(buf) ;
					lexeme = p.next ;
				}
				{ [$/, $*].includes.(lexeme).not }{
				lexemeList = lexemeList.add(buf++lexeme) ;
				lexeme = p.next ;}

			}

			// func args with |
			{ lexeme == $|}
			{ buf = lexeme ;
				lexeme = p.next ;
				buf = buf++lexeme ;
				while {lexeme != $|}
				{ lexeme = p.next ;
					buf = buf++lexeme;
				};
				lexemeList = lexemeList.add(buf);
				lexeme = p.next ;
			}

		// strings
			{ lexeme == $"}
			{ buf = lexeme ;
				lexeme = p.next ;
				buf = buf++lexeme ;
				while {lexeme != $"}
				{ lexeme = p.next ;
					buf = buf++lexeme;
				};
				lexemeList = lexemeList.add(buf);
				lexeme = p.next ;
			}

			// symbols
			{ lexeme == $' }
			{ buf = lexeme ;
				lexeme = p.next ;
				buf = buf++lexeme ;
				while {lexeme != $' }
				{ lexeme = p.next;
					buf = buf++lexeme;
				};
				lexemeList = lexemeList.add(buf);
				lexeme = p.next ;
			}

			// symbol
			{ lexeme == $\\ }
			{ buf = lexeme ;
				lexeme = p.next ;
				while {lexeme.isAlphaNum} {buf = buf++lexeme; lexeme = p.next};
				lexemeList = lexemeList.add(buf) ;
				lexeme
			}

			// separator
			{ separators.includes(lexeme.asSymbol)}
			{ lexemeList = lexemeList.add(lexeme) ;
				lexeme = p.next ;

			}

			// num, method, class, var, arg etc
			// issue: we don't get floats, as they depend from context (num.num)
			{ lexeme.isAlphaNum }
			{ buf = "" ;
				while {lexeme.isAlphaNum} {buf = buf++lexeme; lexeme = p.next};
				lexemeList = lexemeList.add(buf)
			}


			// char
			{ lexeme == $$ }
			{ buf = lexeme ; lexeme = p.next ; buf = buf++lexeme; lexeme = p.next;
				lexemeList = lexemeList.add(buf)
			}

			// primitive
			{ lexeme == $_ }
			{ buf = lexeme ;
				lexeme = p.next ;
				while {lexeme.isAlphaNum} {buf = buf++lexeme; lexeme = p.next};
				lexemeList = lexemeList.add(buf)
			}

			// env var
			{ lexeme == $~ }
			{ buf = lexeme ;
				lexeme = p.next ;
				while {lexeme.isAlphaNum} {buf = buf++lexeme; lexeme = p.next};
				lexemeList = lexemeList.add(buf)
			}

		};
//}.fork ;
		^lexemeList
	}

	tokenize {
		var lexemes = this.scan ;
		var values = this.evaluate(lexemes) ;
		^[lexemes, values]
	}

}

/*
[true, false, nil, inf] specials
[var, arg, thisProcess, thisThread] reserved

c = "~minchia = /* multi rtyui . ++ \n multi \n*/\nsticazzi  // motherfucker \n{SinOsc.ar}.play(|suca| \"test\", 'cool', \shot, $y, 10.456) ; " ;


l = SCLexer("/Users/andrea/Desktop/test.scd")
l = SCLexer(c)
//n = l.scan


a = l.tokenize
a[0].size
a[1].size

k = l.evaluate(n)
u = 0; a[0].do{|i,j| a[1][j].postln ; i.postln }


$G.isDecDigit

*/