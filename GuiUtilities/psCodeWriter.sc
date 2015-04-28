/*

Syntax Colorizer in ps/pdf for SC, requires SCLexer.

AV scripsit 24/02/13

Color scheme:

Solarized scheme
http://ethanschoonover.com/solarized

BG
[99.0,	96.0,	85.0]

frame
[5.0,	16.0,	20.0]

Black
[28.0,	36.0,	39.0]

Lighter
[44.0, 51.0, 52.0]

Yellow
[64.0,	47.0,	0.0	]

Orange
[73.0,	22.0,	0.0]

Red
[80.0,	13.0,	12.0]

Magenta
[76.0,	11.0,	45.0]

Violet
[35.0,	34.0,	75.0]

Blue
[15.0,	45.0,	81.0]

Cyan
[18.0,	57.0,	53.0]

Green
[45.0,	55.0,	0.0]


*/

// NOTE: Elaborate stuff into comments can break the lexer and the colorizer

PsCodeWriter {

	var <>source, <>path ; // source where to get code, path where to write ps
	var <>pathName, <>ext ;
	var <>str ;
	var <>width, <>height ;
	var <>frame, <>frameCol, <>frameWidth ;
	// colors
	var <>separator, <>string, <>symbol, <>number, <>name, <>comment,
	<>envVar, <>bclass, <>primitive, <>reserved ;
	var <>background ; // background color
	var <>colDict ;
	var <>fontName, <>fontSize, <>fontCol ; // font col is for line numbering
	var <>numberingOffset, <>frameWidth ;
	var <>inBetween ;



	*new { arg source, path,
		width = 400, height = nil,
		frame = 30, frameCol = [0.05,	0.16,	0.20],
		separator = [0.44, 0.51, 0.52], string = [0.35, 0.34, 0.75],
		symbol = [0.18,0.57,0.53],
		number = [0.76,0.11,0.45], name = [0.28,0.36, 0.39], comment = [0.8,0.13,0.12] ,
		envVar = [0.73,	0.22,0.0], bclass = [0.15,0.45,0.81],
		primitive = [0.45,0.55,0.0], reserved = [0.45,0.55,0.0],
		background = [0.96, 0.96, 0.93], // but shitty[0.99, 0.96,0.85],
		fontName = "Monaco", fontSize = 7, fontCol = [0,0,0],
		numberingOffset = 25.neg, frameWidth = 1, inBetween = 1.5 ;

		^super.new.initPsCodeWriter(source, path, width, height,
			frame, frameCol,
			separator, string, symbol, number, name, comment,
			envVar, bclass, primitive, reserved, background,
			fontName, fontSize, fontCol, numberingOffset, frameWidth,
			inBetween
		)

	}


	initPsCodeWriter {
		arg asource, apath, awidth, aheight,
		aframe, aframeCol, aseparator, astring, asymbol, anumber, aname, acomment,
		aenvVar, abclass, aprimitive, areserved, abackground,
		afontName, afontSize, afontCol,
		anumberingOffset, aframeWidth, ainBetween ;


		# source, path, width, height, frame, frameCol,
		separator, string, symbol, number, name, comment,
		envVar, bclass, primitive, reserved, background,
		fontName, fontSize, fontCol, numberingOffset, frameWidth, inBetween =
		[asource, apath, awidth, aheight,
		aframe, aframeCol,
		aseparator, astring, asymbol, anumber, aname, acomment,
		aenvVar, abclass, aprimitive, areserved, abackground,
		afontName, afontSize, afontCol, anumberingOffset, aframeWidth, ainBetween] ;
		colDict = (\separator:separator, \string:string, \symbol:symbol,
			\number:number, \name:name, \comment:comment, \multiComment:comment,
		\envVar:envVar, \class:bclass, \source:source, \primitive:primitive, \background:background, \reserved:reserved) ;
		source = SCLexer(source).tokenize ; // list of two lists
		source = this.processMultiComment(source) ;
		# pathName, ext = path.split($.) ;
	}

	processMultiComment { arg lexemeList, typeList ;
		var newLex, newTyp;
		var type, lines ;
		# lexemeList, typeList = source ;
		# newLex, newTyp = [[], []] ;
		lexemeList.do{|lexeme, idx|
			type = typeList[idx] ;
			if (type == \multiComment) {
				lines = lexeme.split($\n) ;
				(lines.size-1).do{|i|
					newLex = newLex.add(lines[i]);
					newTyp = newTyp.add(\comment) ;
					newLex = newLex.add($\n); newTyp = newTyp.add(\separator)
				} ;
				newLex = newLex.add(lines.last);
				newTyp = newTyp.add(\comment) ;
			} { newLex = newLex.add(lexeme); newTyp = newTyp.add(type)}
		} ;
		^[newLex, newTyp]
	}

	setWidth {
		height = if (height.isNil) {source[0].count{|i| i==$\n}+3*fontSize*inBetween}{height} ; // was 1*fontSize
		str = str++"% document size\n";
		str = str++"<< /PageSize [X Y] >> setpagedevice\n"
			.replace("X", width+(frame*2))
			.replace("Y", height+(frame*2))
	}


	setFont {
		str = str++"% font setting grid\n";
		str = str++"/"++fontName ++" findfont "++ fontSize.asString++" scalefont setfont\n" ;
	}

	drawFrame {
		str = str++"% frame\n";
		str = str++frame.asString++" "++frame.asString++" translate\n" ;
		str = str++frameWidth.asString++" setlinewidth\n" ;
		background.do{|i| str = str++i.asString++" "} ;
		str = str++"setrgbcolor\n" ;
		str = str++"newpath\n0 0 moveto\n";
		str = str++width.asString++" "+ 0.asString++" lineto\n";
		str = str++width.asString++" "+ height.asString++" lineto\n";
		str = str++0.asString++" "+ height.asString++" lineto\n";
		str = str++0.asString++" "+ 0.asString++" lineto\n";
		str = str++"closepath fill\n" ;
		frameCol.do{|i| str = str++i.asString++" "} ;
		str = str++"setrgbcolor\n" ;
		str = str++"newpath\n0 0 moveto\n";
		str = str++width.asString++" "+ 0.asString++" lineto\n";
		str = str++width.asString++" "+ height.asString++" lineto\n";
		str = str++0.asString++" "+ height.asString++" lineto\n";
		str = str++0.asString++" "+ 0.asString++" lineto\n";
		str = str++"closepath stroke\n" ;
		str = str++"3 "++(fontSize.neg).asString++" translate\n" ;
	}

// PS: how to get width
//	/Courier findfont 8 scalefont setfont
// (M) stringwidth pop 20 string cvs show
// --> 4.80078 ; // this is for 8, should change and be parameterised

	// TODO
	// space after (  and )
	typeCode {
		var x = 0, y = 0 ;
		var col = [0,0,0] ;
		var lexemeList, typeList, type ;
		var flag = true ;
		# lexemeList, typeList = source ;
		col.do{|i| str = str++i.asString++" "};
		str = str++"setrgbcolor\n" ;
		lexemeList.do{|lexeme, idx|
			type = typeList[idx] ;
			if(lexeme ==$\n) {
				x = 0;
				y = y+1;
				flag = true ;
			} {
				if (flag){
				frameCol.do{|i| str = str++i.asString++" "};
				str = str++"setrgbcolor\n" ;
					str = str++(fontSize*0.6000975*(y+1).asString.size.neg-5).asString++" "
					++(height-((y+1)*fontSize*inBetween))++" moveto\n" ;
					str = str++"("++(y+1).asString++") show\n" ;
				flag = false ;
				col = frameCol ;
				col = this.setCol(type, col) ;
				} ;
				col = this.setCol(type, col) ;
				lexeme = this.createLine(lexeme) ;
				str = str++(x*fontSize*0.6000975)++" "++(height-((y+1)*fontSize*inBetween))++" moveto\n" ;
				str = str++"("++lexeme++") show\n" ;
				x = x+lexeme.size ;
				// special case for ps syntax
				if(['\\(','\\)'].includes(lexeme.asSymbol) ) { x = x-1 } ;
			}
		} ;

	}

	setCol { arg type, col ;
		var newCol = colDict[type] ;
		if (newCol != col) {
			newCol.do{|i| str = str++i.asString++" "};
			str = str++"setrgbcolor\n" ;
			} ;
		^newCol
	}

	createLine { arg lexeme ;
		lexeme = lexeme.asString ;
		// first we solve symbols
		lexeme = lexeme.replace($\\.asString, $\\.asString++$\\.asString) ;
		// special case for ps syntax
		lexeme = lexeme.replace(")", "\\)").replace("(", "\\(") ;
		// tabs replaced as 4 spaces
		lexeme = lexeme.replace("\t", "    ") ;
		^lexeme
	}


	typeset {
		this.setWidth ;
		this.setFont ;
		this.drawFrame ;
		this.typeCode
	}

	write {
		var file = File(pathName++".ps", "w") ;
		file.write(str++"showpage\n") ;
		file.close ;
		if (ext.asSymbol == \pdf) {
			("pstopdf"+pathName++".ps").unixCmd{
				("rm"+pathName++".ps").unixCmd
			}
		}
	}

}


/*

u = PsCodeWriter("/Users/andrea/Desktop/test.scd", "/Users/andrea/Desktop/untitled2.pdf", height: nil).typeset.write ;

u = PsCodeWriter("/Users/andrea/Library/Application Support/SuperCollider/Extensions/SCClassLibrary/vanderaalleSC/rumentarium/ruEasyGui.sc", "/Users/andrea/Desktop/untitled2.pdf").typeset.write ;

*/


