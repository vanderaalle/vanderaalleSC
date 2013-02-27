/*

A class intended to convert data structures in musical notation
via LilyPond notation sw

It *always* accept freqs

*/

LilyPond {
	
	var <>pitchNames ;
	var <>octaves ; 
	var <>lilyPath ;
		
	*new { ^super.new.init }
	
	init { 
		pitchNames = ["c", "des", "d", "ees", "e", "f", "ges", "g", "aes", "a", "bes", "b"] ;
		
		octaves = [",,,,,", ",,,,", ",,,", ",,", ",", "", "'", "''", "'''", "''''", "'''''"] ;
		lilyPath = "/Applications/LilyPond.app/Contents/Resources/bin/lilypond" ;
	}
	
	

		// here you pass an array of freqs: e.g. [345, 564, 223, 54]
	
	notateChord { arg freqArr, lilyFileName ;
		var lily, pitchArr ;
		var pitch, octave ;
		var sequence, chord ;
		var lyPath ;
		
		lilyFileName = lilyFileName ? String.scDir++"/tmp/lilyTmp.ly" ;
		lilyFileName = if ( lilyFileName[0]==$/, { lilyFileName }, 
									{ String.scDir++"/"++lilyFileName }) ;
		
		lily = File.new(lilyFileName, "w") ;	
		// assuming tempered quantization
		
		pitchArr = freqArr.cpsmidi.round ; // midi notes 
		chord = "<";
		pitchArr.do({ arg item, ind ;
				pitch  = pitchNames[item%12] ;
				octave = octaves[(item/12).asInt+1] ;
				chord = chord + pitch++octave ;
			}) ;
		chord = chord+"> " ;
		lily.write("
\#(set-default-paper-size \"a6\" \'landscape)
\\version \"2.8.1\"\n") ;
		lily.write("
\\header\{ title = \"Chord transcription\"\}
\{\n") ;
		lily.write(chord) ;
		lily.write("\n\}") ;		
		lily.close ;	
		// render
		lyPath = 	lilyPath
			+ "--pdf"
			+ "--output=" 
			++ lilyFileName.split($.)[0]
			+ lilyFileName ;  
		unixCmd(lyPath) ;
	}



		// generic: [[1,2], [3,4]] where each internal array is a chord
		// if there's no internal array, it is intended as a pitch sequence
			
	notate { arg arr, lilyFileName ;
		var lily, pitchArr ;
		var pitch, octave ;
		var sequence, chord ;
		var lyPath ;
		
		lilyFileName = lilyFileName ? String.scDir++"/tmp/lilyTmp.ly" ;
		lilyFileName = if ( lilyFileName[0]==$/, { lilyFileName }, 
									{ String.scDir++"/"++lilyFileName }) ;
		
		lily = File.new(lilyFileName, "w") ;	
		// assuming tempered quantization
		if (arr[0].isArray.not, { arr = arr.clump(1) }) ;
		// you can pass sequences, e.g pitchContour	
		pitchArr = arr.cpsmidi.round ; // midi notes 
		sequence = "" ;
		pitchArr.do({ arg frame, index ; 	
			chord = "" ; // "<";
			frame.do({ arg item, ind ;
				pitch  = pitchNames[item%12] ;
				octave = octaves[(item/12).asInt+1] ;
				if (pitch == nil, {pitch = "r"; octave = ""}) ;
				chord = chord + pitch++octave ;
			}) ;
			if (frame.size > 1, { chord = "<"+chord+"> " }) ;
			sequence = sequence + chord ;
		}) ;
		// sequence.postln ;
		lily.write("
\#(set-default-paper-size \"a3\" \'landscape)
\\version \"2.8.1\"\n") ;
		lily.write("
\\header\{ title = \"Transcription\"\}
\{ \#(set-accidental-style \'forget)\n") ;
		lily.write(sequence) ;
		lily.write("\n\}") ;		
		lily.close ;	
		// render
		lyPath = 	lilyPath
			+ "--pdf"
			+ "--output=" 
			++ lilyFileName.split($.)[0]
			+ lilyFileName ;  
		unixCmd(lyPath) ;
	}
	

		// here you pass the whole praatFormant object	
			
	notateFormant { arg praatFormant, lilyFileName ;
		var lily, pitchArr ;
		var pitch, octave ;
		var sequence, chord ;
		var lyPath ;
		
		lilyFileName = lilyFileName ? String.scDir++"/tmp/lilyTmp.ly" ;
		lilyFileName = if ( lilyFileName[0]==$/, { lilyFileName }, 
									{ String.scDir++"/"++lilyFileName }) ;
		
		lily = File.new(lilyFileName, "w") ;	
		// assuming tempered quantization
		
		pitchArr = praatFormant.freqList.cpsmidi.round ; // midi notes 
		sequence = "" ;
		pitchArr.do({ arg frame, index ; 	
			chord = "<";
			frame.do({ arg item, ind ;
				pitch  = pitchNames[item%12] ;
				octave = octaves[(item/12).asInt+1] ;
				chord = chord + pitch++octave ;
			}) ;
			chord = chord+"> " ;
			sequence = sequence + chord ;
		}) ;
		// sequence.postln ;
		lily.write("
\#(set-default-paper-size \"a3\" \'landscape)
\\version \"2.8.1\"\n") ;
		lily.write("
\\header\{ title = \"Formant transcription\"\}
\{ \#(set-accidental-style \'forget)\n") ;
		lily.write(sequence) ;
		lily.write("\n\}") ;		
		lily.close ;	
		// render
		lyPath = 	lilyPath
			+ "--pdf"
			+ "--output=" 
			++ lilyFileName.split($.)[0]
			+ lilyFileName ;  
		unixCmd(lyPath) ;
	}



/*
	open { arg fileName, openApp ;
		openApp = openApp ? "/Applications/Preview.app/Contents/MacOS/Preview" ;
		// lily bug: pdf not in the right place
		fileName = fileName ? String.scDir++"/praatTmp.pdf" ;
		fileName = if ( fileName[0]==$/, {fileName}, 
								{ String.scDir++"/"++fileName}) ;			unixCmd(openApp+fileName) ;
	}
*/

}
