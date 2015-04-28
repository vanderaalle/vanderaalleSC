/*
Specials (5):
\n:
 , . ' ?

*/

ScialojaWriter {

	var <>log, <>notes, <>page, <>mapFunc ;
	var <>pitchDict ;
	
	*new { arg log, mapFunc ; 
		^super.new.initScialojaWriter(log, mapFunc) 
	}

	*newFromLog { arg path, mapFunc, notes ; 
		var aLog = Object.readArchive(path) ; 	
		^super.new.initScialojaWriter(aLog, mapFunc, notes) 
	}

	initScialojaWriter { arg aLog, aMapFunc, someNotes ;
		log = aLog[1..] ;
		notes = if (someNotes.isNil) { aLog[0] } { someNotes } ;
		this.createPage ;
		mapFunc = mapFunc ? this.defineMapFunc ;	 
				// dirty 
		pitchDict = (
			// first value is the line value for staff
			// second isSharp = true, hence natural = false
			36: [12, false], // c, lowest cello
			37: [12, true],
			38: [11.5, false],
			39: [11.5, true],
			// previous line sep is not used (neither correct)
			40: [11, false],
			
			41: [10.5, false], // f under bass clef
			42: [10.5, true],
			43: [10, false],
			44: [10, true],
			45: [9.5, false], 
			46: [9.5, true],
			47: [9, false], 
			48: [8.5, false],
			49: [8.5, true],
			50: [8, false],
			51: [8, true],
			52: [7.5, false],
			53: [7, false],
			54: [7, true],
			55: [6.5, false],
			56: [6.5, true],
			57: [6, false],
			58: [6, true],
			
			59: [5.5, false],// b is a special case
			
			60: [5, false], // c4
			61: [5, true],
			62: [4.5, false],
			63: [4.5, true],
			64: [4, false],
			65: [3.5, false],
			66: [3.5, true],
			67: [3, false],
			68: [3, true],
			69: [2.5, false], 
			70: [2.5, true],
			71: [2, false],
			72: [1.5, false],
			73: [1.5, true],
			74: [1, false],
			75: [1, true],
			76: [0.5, false],
			77: [0, false],
			78: [0, true], // f/f#
			// g: hence on seps are not used, but better list them
			79: [-0.5, false], 
			80: [-0.5, true],
			81: [-1, false], //a
			82: [-1, true],  
			83: [-1.5, false],
			84: [-2, false] //c
			// we need g/g#
		) ;
	}
	
	createPage {	
		var size = 1000 ;
		var a3 = [size*2.sqrt, size] ; 
		var corner = [85, 175] ; 
		var separator = 210 ;
		var width = size*2.sqrt*0.9 ;
		var partition = 10 ;
		var lineNumber = 11 ;
		var organization ;
		var step = 10 ;
		var staves = 4 ;
		var n = NB.new("testN", a3[0], a3[1], true, "pdf");
		// random filling
		// organization = Array.fill(lineNumber-1, {[1,2].choose}) ;
		organization = Array.fill(4, 1).addAll([1.5,1.5]).addAll(Array.fill(4, 1)) ;
		page = Page.new(n, staves, corner, separator,
			lineNumber, organization, partition, width, step)
		.createStaves ;

	}
	
	
	defineMapFunc { // just to have the mapfunc in separate place
		^ { arg pitch, x, staffIndex ;
		// item is a midi pitch
			var y = this.calculatePitchPosition(pitch, staffIndex) ;
			var dim = 7 ;
			var frac ;
			var posLine ;
			//page.nb.image("/musica/scialojaSC/img/head.pdf",x-(dim*0.5), y-(dim*0.5), dim*1.3) ;
			// this creates the note
			if (pitchDict[pitch.asInteger][1]) // sharp
				{ 
				page.nb.font("\"EngraverFontExtras\"", 22) ; //30
//				page.nb.text("m", x-(dim*2), y+(dim*1.2)) ;
				page.nb.text("m", x-(dim*1.6), y+(dim*1)) ;
				} ; 
			if (pitch.frac != 0.0) // it's fractional
				{ // n/8 tone
				frac = pitch.frac ;
				page.nb.font("\"Abadi MT Condensed Light\"", 14) ;
				case { frac == 0.25 }
				{ //page.nb.text("+1", x-(dim*1.5), y-dim)
					page.nb.stroke(1, 1,1) ;
					page.nb.fill(1,1,1) ;
					page.nb.rect(x-(dim*1.5)-2+10, y-dim-21, 11, 22) ;
					page.nb.fill(0, 0, 0) ;
					page.nb.stroke(0,0,0) ;
					page.nb.text("1", x-(dim*1.5)+9, y-13) ;
					page.nb.text("^", x-(dim*1.5)+8, y-2) ;
				 }					
				 	{ frac == 0.5 }
				{ //page.nb.text("+2", x-(dim*1.5), y-dim)
					page.nb.stroke(1, 1,1) ;
					page.nb.fill(1,1,1) ;
					page.nb.rect(x-(dim*1.5)-2+10, y-dim-21, 11, 22) ;
					page.nb.fill(0, 0, 0) ;
					page.nb.stroke(0,0,0) ;
					page.nb.text("2", x-(dim*1.5)+9, y-13) ;
					page.nb.text("^", x-(dim*1.5)+8, y-2) ;
				 }
					{ frac == 0.75 }
				{ // page.nb.text("+3", x-(dim*1.5), y-dim) 
					page.nb.stroke(1, 1,1) ;
					page.nb.fill(1,1,1) ;
					page.nb.rect(x-(dim*1.5)-2+10, y-dim-21, 11, 22) ;
					page.nb.fill(0, 0, 0) ;
					page.nb.stroke(0,0,0) ;
					page.nb.text("3", x-(dim*1.5)+9, y-13) ;
					page.nb.text("^", x-(dim*1.5)+8, y-2) ;
				} ;
			} ;
			// note heads
			page.nb.push ;
			page.nb.rotate(20) ;
			page.nb.oval(x-(dim*0.5), y-(dim*0.5), dim*1.4, dim) ;
			page.nb.pop ;	
			// evaluate if understaff lines are to be build
			//...
			case 
			{ [36, 37].includes(pitch.asInteger) }
				{
				posLine = staffIndex*page.separator + (12*page.step) ;
				page.nb.line(x-10, posLine, x+10, posLine) ;
				posLine = staffIndex*page.separator + (13*page.step) ;
				page.nb.line(x-10, posLine, x+10, posLine) ;
				}
			{ [38, 39, 40].includes(pitch.asInteger) }
				{
				posLine = staffIndex*page.separator + (12*page.step) ;
				page.nb.line(x-10, posLine, x+10, posLine) ;
				}
			{ [81, 82, 83].includes(pitch.asInteger) }
				{
				posLine = staffIndex*page.separator + (-1*page.step) ;
				page.nb.line(x-10, posLine, x+10, posLine) ;
				}
			{ pitch.asInteger == 84 }
				{
				posLine = staffIndex*page.separator + (-1*page.step) ;
				page.nb.line(x-10, posLine, x+10, posLine) ;
				posLine = staffIndex*page.separator + (-2*page.step) ;
				page.nb.line(x-10, posLine, x+10, posLine) ;
				}
							
		}
	}
	
	calculatePitchPosition { arg pitch, staffIndex ;
		var line, rest, y ; 
		line = pitchDict[pitch.asInteger][0] ;
		rest = if (line.asInteger != line) {0.5} {0} ;
		case 
		{ pitch.asInteger == 59 }
			{ // the b3 case
			y = 	staffIndex*page.separator + (6+rest*page.step) ;
			}
		{ [36, 37].includes(pitch.asInteger) }
			{ 
			y = staffIndex*page.separator + (13*page.step) ;
			}
		{ [38, 39].includes(pitch.asInteger) }
			{ 
			y = staffIndex*page.separator + (12.5*page.step) ;
			}
		{ pitch.asInteger == 40 }
			{ 
			y = staffIndex*page.separator + (12*page.step) ;
			}
		{ [79, 80].includes(pitch.asInteger) } // g
			{ y = 
			staffIndex*page.separator + 
			(-0.5*page.step) ; }
		{ [81, 82].includes(pitch.asInteger) } // a
			{ y = 
			staffIndex*page.separator + 
			(-1*page.step) ; }
		{ pitch.asInteger == 83 } // b
			{ y = 
			staffIndex*page.separator + 
			(-1.5*page.step) ; }
		{ pitch.asInteger == 84 } // c6
			{ y = 
			staffIndex*page.separator + 
			(-2*page.step) ; }
						
		{ pitch.asInteger != 59 } // else
		 	{ y = 
			staffIndex*page.separator + 
			(page.organization[..(line.asInteger-1)].sum+rest*page.step)
			} ;
		^y
	}
	
	createClefs {
		page.nb.font("\"EngraverFontSet\"", 30) ;
		4.do({  arg staffIndex ;
			var y = staffIndex*page.separator ;
			/*
			// treble and bass clefs
			page.nb.text("&", -25, y+48) ;
			page.nb.text("?", -25, y+95.5)
			*/
			page.nb.text("B", -22, y+70) ;
		})
	}
	
	placeNote { arg pitch, time, totalTime ; 
	// line: line index of the staff ; time: the time ; totalTime: duration of a page 
		var totalWidth = page.width * page.number ;
		var place = time*totalWidth/totalTime ; // conversion in space
		var x = place % page.width ;	 // wrap up
		var staffIndex = place.div(page.width) ;
	// item is simply forwarded (untouched) to mapFunc	
		mapFunc.value(pitch, x, staffIndex) ;
	}

	placeSuper { arg time, totalTime ; 
	// line: line index of the staff ; time: the time ; totalTime: duration of a page 
		var totalWidth = page.width * page.number ;
		var place = time*totalWidth/totalTime ; // conversion in space
		var x = place % page.width ;	 // wrap up
		var staffIndex = place.div(page.width) ;
		var pitch ;
		var dim = 7 ;
		var y = this.calculatePitchPosition(57, staffIndex) ;
		page.nb.font("\"EngraverFontExtras\"", 30) ;
		page.nb.text("x", x-(dim*0.7), y+(dim*1.1)) ;
		page.nb.text("o", x-(dim*0.5), y-1) ;
		page.nb.line(x+0.5, y, x+0.5, y+(dim*5)) ;
		page.nb.font("\"Abadi MT Condensed Light\"", 20) ;
		page.nb.push ;
		page.nb.rotate(90) ;
		page.nb.text("(", x-4, y+30) ;
		page.nb.pop ;
//		posLine = staffIndex*page.separator + (12*page.step) ;
//		page.nb.line(x-10, posLine, x+10, posLine) ;
	}

	placeSub { arg item, time, totalTime ; 
	// line: line index of the staff ; time: the time ; totalTime: duration of a page 
		var totalWidth = page.width * page.number ;
		var place = time*totalWidth/totalTime ; // conversion in space
		var x = place % page.width ;	 // wrap up
		var staffIndex = place.div(page.width) ;
		var pitch ;
		var dim = 7 ;
		var y, posLine ;
		var key = item[2].asSymbol ;
		case 
			{ [' ', "\'".asSymbol].includes(key) }
				{	
				y = this.calculatePitchPosition(36, staffIndex) ;
				page.nb.font("\"EngraverFontExtras\"", 30) ;
				page.nb.text("x", x-(dim*0.7), y+(dim*1.1)) ;
				page.nb.text("o", x-(dim*0.5), y+(dim*2.5)) ;
				page.nb.text("+", x-(dim*0.7), y+(dim*4)) ;
				posLine = staffIndex*page.separator + (12*page.step) ;
				page.nb.line(x-10, posLine, x+10, posLine) ;
				posLine = staffIndex*page.separator + (13*page.step) ;
				page.nb.line(x-10, posLine, x+10, posLine) ;
				}
			{ item[4] == 13 }
				{	
				y = this.calculatePitchPosition(36, staffIndex) ;
				page.nb.font("\"EngraverFontExtras\"", 30) ;
				page.nb.text("x", x-(dim*0.7), y+(dim*1.1)) ;
				page.nb.text("o", x-(dim*0.5), y+(dim*2.5)) ;
				page.nb.font("\"Abadi MT Condensed Light\"", 30) ;
				page.nb.push ;
				page.nb.rotate(90) ;
				page.nb.text("(", x+10, y+20) ;
				page.nb.pop ;
				page.nb.fontsize(20) ;
				page.nb.text(".", x+4, y+10) ;
				page.nb.text(".", x+10, y+10) ;
				page.nb.text(".", x+16, y+10) ;
				page.nb.text(".", x+22, y+10) ;
				posLine = staffIndex*page.separator + (12*page.step) ;
				page.nb.line(x-10, posLine, x+10, posLine) ;
				posLine = staffIndex*page.separator + (13*page.step) ;
				page.nb.line(x-10, posLine, x+10, posLine) ;
				}
			{ ['.', ',', '?' ].includes(key) }
				{	
				y = this.calculatePitchPosition(43, staffIndex) ;
				page.nb.font("\"EngraverFontExtras\"", 30) ;
				page.nb.text("x", x-(dim*0.7), y+(dim*1.1)) ;
				page.nb.text("o", x-(dim*0.5), y+(dim*2.5)) ;
				page.nb.text("+", x-(dim*0.7), y+(dim*4)) ;
				}

	}

	
// clean the log
	skipStartingSilenceAndClean {
		var startTime = log[0][0] ;
		var totalTime = log.reverse[0][0] ;
		var offset = totalTime*0.005 ;
		log.size.do({ arg index ;
			log[index][0] = log[index][0]-startTime+offset ;
		}) 
	}

	getTotalTime {
		^log.reverse[0][0]+(log.reverse[0][0]*0.005) ; 
		// 2% is only for giving a certain space at the end
		// like in SkipStarting, see before
	}

	createTempo { arg totalTime ;
		// assuming a usual 4/4
		var quarterDur = totalTime / (page.partition*page.number*4) ;
		var tempo = (60.0/quarterDur*0.25).asInteger ; 
		page.nb.font("\"EngraverTextNCS\"", 16) ;
		page.nb.text("4l16  :   q ="+tempo, 0, -10) ;	
	}
		
	createTotalDuration { arg totalTime ;
		page.nb.font("\"Optima\"", 16) ;
		page.nb.text("ca."+totalTime.asInteger.asTimeString.replace(":", "'"),
				1230, 610
			)
	}	
	
	createDate {
		var date = Date.getDate.format("%d/%m/%Y, %H.%m");
		page.nb.font("\"Optima\"", 12) ;
		page.nb.text(date, 1200, 800)
	}
		
	createNotes {
		page.nb.strokewidth(0) ;
		page.nb.font("\"Palatino\"", 30) ;
		page.nb.text(notes, 100, -70) ;
		page.nb.strokewidth(1) ;
	}
		
	placeAll {
		var totalTime, measureDur, time, pitch, key ;
		this.skipStartingSilenceAndClean ;
		totalTime = this.getTotalTime ;
		this.createClefs ;
		this.createTempo(totalTime) ;
		this.createTotalDuration(totalTime) ;
		this.createNotes ;
		this.createDate ;
		log.do({ arg item, index ;
			time = item[0] ;
			pitch = item[1] ;
			// treshold
			 case 
			 { (pitch >= 36).and(pitch <= 84) }
				{ this.placeNote(pitch, time, totalTime) }
			{ pitch < 36 }
				{  
				// we pas item to avoid \n troubles
				this.placeSub(item, time, totalTime) }
			// the case for accents
			{ pitch > 85 }
				{ 
				this.placeSuper(time, totalTime) }	
		})
	}
	
	// wrapper for NB
	displayCode {
		this.page.nb.displayCode ;
	}
	
}