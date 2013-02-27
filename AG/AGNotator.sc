
AGNotator : LilyPond {
	
	//var <>formantChartFile ;
	var <>formantDict ;
	var <> formantCharter ;

	
	*new { arg id ;
		^super.new.initAGN(id) ;
	 }


	initAGN { arg id ;
		formantDict = IdentityDictionary.new ;
		formantDict.add([0, 300, 500, 1200]-> "u") ;
		formantDict.add([0, 300, 1600, 2000]-> "y") ;
		formantDict.add([300, 550, 800, 1150]-> "o") ;
		formantDict.add([550, 1250, 800, 1250]-> "@oCap") ;
		formantDict.add([0, 300, 1200, 1600]-> "@oCut") ;  
		formantDict.add([300, 550, 1150, 1700]-> "@schwa") ; 
		formantDict.add([550, 1250, 1250, 1700]-> "a") ;
		formantDict.add([0, 300, 2000, 3200]-> "i") ; 
		formantDict.add([300, 500, 1700, 3100]-> "e") ;
		formantDict.add([500, 1250, 1700, 2900]-> "@ae") ; 
		formantCharter = FormantCharter.new(formantDict, id)
			.createHeader
			.drawFormantSpace
			.plotFormantChart ;		
	}	
	
	
		
	
/* 
	// intended to convert from attack do duration
	// to be completed 
	
	fromIntervalToDuration { arg arr, samplingUnit = 0.25, composite = 4 ;
			// NOTE!: last value will be discarded in notation, 
		//as we cannot know how much it lasts
		//		 more, first time value is assumed as a rest 		// samplingUnit: default is 1/4 sec (--> semiquaver at 60 bpm)
		// composite: grouping unit default is quaver (4 samplingUnits) 		 
		// TOO LONG (remember mantra "7-12 lines fr a method")

	
		 var times = arr.clump(2).flop[1].round(samplingUnit) ; // semiquaver
		 
		 var dur, noteRest, pauseRest, sub, subArr, newSubArr ;
		 var before = 0, newTimes ;
		 // converting attacks into durations
		 times.do({arg i; 
		 			newTimes = newTimes.add(i-before) ;		 			before = i ;
		 			}) ;
		^newTimes
	
	}
*/	
	createGrouping { arg arr, samplingUnit = 0.25, composite = 4 ;
		 // quantize
		 var newTimes = arr.clump(2).flop[1].round(samplingUnit) ; 
		 var dur, noteRest, pauseRest, sub, subArr, newSubArr ;
		 var before = 0 ;
		 // converting attacks into durations
		// init
		newTimes = newTimes/samplingUnit ;
		// newTimes.postln ;
		noteRest = 0 ;
		subArr = [] ;
		// cycle
		newTimes.do({ arg dur ;
			var head, tail, body, num ; 
			head = composite-noteRest ;
			tail = (dur-head)%composite ;
			body = dur - head -tail ;
			//[head, body, tail].postln;
			head = if (head > dur, { dur }, { head }) ;
			sub = [head] ;
			if (body > 0, { 
				num = body.div(composite) ;
				num.do({ sub = sub.add(composite) }) ;
				}) ;
			if ( head < dur, {sub = sub.add(tail)}) ;
			sub = sub.abs.asInteger ;
			sub.remove(0) ;
			noteRest  = tail ;
			subArr = subArr.add(sub) ;
			}) ;
		//___ Notation
		^subArr ;
		//this.createPitchDurationNotation(arr, subArr, quarterTone) ;
	}





// here we have dynamics relative to pitch in itemAndOccurences
	intensityToItemsAndOccurences { arg intensity, itemsAndOccurrences ;
		var newInt = [] ;
		var pitches = itemsAndOccurrences[0] ;
		var occurrences = itemsAndOccurrences[1] ;
		var newInd = 0 ;
		occurrences.do({ arg item, index ;
			if ( pitches[index] != -inf, { 
				newInt = newInt.add(intensity[newInd])},
				{newInt = newInt.add(-inf)} ); 
			newInd = newInd + item ;
		}) ;
		^newInt
	}

	createNotation {  arg arr, subArr, dynArr, clef = "treble", quarterTone = false,
				f1, f2, staffNames ;
		// f1 and f2 are NOT clustered in items and occurences
		var durations = (1:"16", 2:"8", 3:"8.", 4:"4", 8:"2", 12:"2.", 16:"1", 
		 		20:"1", 24:"2.", 28:"7", 32:"8") ;
		var noteBlock = "\\clef \""++clef++"\"";  
		var round = if (quarterTone == true, {0.5}, {1}) ;
		// assuming freqs to midi
		var pitches = arr.clump(2).flop[0].round(round) ; 
		var target, octave2 ;
		var lyricBlock = "\\addlyrics \{\n" ;
		var accidental = "\#(set-accidental-style \'forget)\n" ;
		// ^add
		var block ;
		var position = 0 ;
		var current = "" ;
		// midi semitones
		// we have to add on the head the rest 0 and discard last freq on tail
		pitches[..pitches.size-2].do({ arg item, index ;
					var targetItem = pitches[index+1] ;
					var sub = subArr[index] ;
					var note, octave ; 
					var noteIndex  = item%12 ;
					var targetIndex = pitches[index+1]%12;
					var dynamics = dynArr[index] ;
					var gliss, glissTag ;
					var beginOffset, offSet ;
					var nameMid ;
					var vocoid ;
					# note, octave = this.calculateNoteOctave(item, noteIndex, pitchNames) ;
					# target, octave2 = this.calculateNoteOctave(
							targetItem, targetIndex, pitchNames) ;
					
					note = note++octave ;
					
					//
					gliss = if ((item != -inf)
								.and(pitches[index+1] != -inf)
								.and(sub.sum > 2), // here we decide minimum dur for gliss
								{ true }, { false }) ;
					glissTag = if ( gliss == true,  { "\\glissando"}, { "" }) ;
					//
					
					noteBlock = noteBlock+note++durations[sub[0]]+glissTag ;
					noteBlock = noteBlock++dynamics ; 
					vocoid = this.createVocoid (f1[position], f2[position] ) ;
					if (vocoid == current, { vocoid = " - " }, { current = vocoid }) ;
					lyricBlock = if ( item != -inf, { lyricBlock + vocoid}, { lyricBlock }) ;
					
					if ( sub.size > 1,
						{sub[1..sub.size-1].do({ arg d, i ;
							if ( gliss == false, 
							// no gliss
							{ noteBlock = noteBlock++"~"+note++durations[d] ;

							  lyricBlock = lyricBlock + "" ;	
							 },
							 // ok gliss
							{												# beginOffset, offSet, nameMid = this.calculateOffset(item, octave, clef, sub.size-1, 
									targetItem, octave2) ;
							noteBlock = noteBlock 
							
							+"
	\\once \\override Voice.NoteHead \#\'transparent =\#\#t\n
	\\once \\override Voice.NoteHead \#\'Y-offset =\#"
							+(beginOffset+(offSet*(i+1)))++"\n"
							+nameMid++
							
							//note++ // delete me
							durations[d]+glissTag ;
							lyricBlock = lyricBlock + "-" ;
							});
							}) ;
						}) ;
					noteBlock = noteBlock++"\n" ;
					lyricBlock = lyricBlock++"\n" ;
					position = position + sub.sum ;
			}) ;
		block = "\\new Staff\n{\n" + staffNames + accidental + noteBlock +"}\n"
				+lyricBlock.replace("nil", "-") + "\}\n"
				// replace is for pitch without formant 
		^block ;
		
		}
		
		
	calculateNoteOctave { arg item, noteIndex, pitches ;
		var note = if (noteIndex.asString.reverse[0..1] == "5.", 
						{pitchNames[(item+1)%12]++"eh"}, {pitchNames[item%12]}) ; 
						
		var octave = octaves[(item/12).asInteger+1] ;
		// trick
		octave = if (note == "ceh", {octaves[(item/12).asInteger+2]}, {octave}) ;
		if (item <= 0, {note = "r"; octave = ""}) ;
		^[note, octave]
		}	
		
	calculateOffset { arg item, octave, clef, number, targetItem, octave2 ;
		var pitchNames2 = ["c", "d", "d", "e", "e", "f", "g", "g", "a", "a", "b", "b"] ;
		var beginOffset, endOffset, diffOffset, offSet ;
		var name = pitchNames2[item%12];
		var name2 = pitchNames2[targetItem%12];
		var treble = [ "a", "b", "c'", "d'", "e'", "f'", "g'", 
		"a'", "b'", "c''", "d''", "e''", "f''", "g''", 
		"a''", "b''", "c'''", "d'''", "e'''"] ;
		var treble8 = ["a,", "b,", "c", "d", "e", "f", "g", 
		"a", "b", "c'", "d'", "e'", "f'", "g'", 
		"a'", "b'", "c''", "d''", "e''"] ;
		var bass = ["c,", "d,", "e,", "f,", "g,", "a,", "b,", "c", "d", 
		"e", "f", "g", "a", "b", "c'", "d'", 
		"e'", "f'", "g'", "a'", "b'", "c''", "d''" ] ;
		var selectedClef = case  	{ clef == "treble" }{ treble }
								{ clef == "treble\_8" } { treble8 }
								{ clef == "bass" } { bass } ;
		// selectedClef.postln ;
		name = if (name == "ceh", { "c" }, { name }) ;
		/*		
		["name: ", name++octave].postln ;
		["name2: ", name2++octave2].postln ;
		*/
		beginOffset = selectedClef.detectIndex({ arg x ; x == (name++octave) })*0.5-4 ;
		endOffset = selectedClef.detectIndex({ arg x ; x == (name2++octave2) })*0.5-4 ;
		diffOffset = endOffset-beginOffset ;
		offSet = diffOffset / (number+1) ;
		// [item, targetItem].postln ;
		// [beginOffset, endOffset, diffOffset, number, offSet ].postln ;
		// "__________________\n\n\n".postln ;
		^[beginOffset, offSet, name]	
	}
	
	
	// convert each element of the array in an opportune string for lily
	
	
	createDynamicsNotation { arg pitchArr, dynArr ;
		var dynamicsList = ["\\pp", "\\p", "\\mp", "\\mf", "\\f", "\\ff", "\\fff"] ;
		var newDynArr = [] ;
		var pitchArr2 = pitchArr[0] ;
		var durs = pitchArr[1] ;
		// step1: fill all but rests
		newDynArr = this.insertDynamicRests( pitchArr, dynArr ) ;
		// step 2: convert to opportune values
		newDynArr = this.convertDynamicsToIndices( newDynArr ) ;
		newDynArr = this.assignDynamicsStrings( newDynArr, dynamicsList, durs ) ;
		
		// step3: convert to strings
		//newDynArr = this.convertToDynamics( newDynArr, dynamicsList ) ;
		// step4: add pins
		//newDynArr = this.addPins( newDynArr, dynamicsList ) ;
		//newDynArr = this.indicesToIntervals( newDynArr ) ;
		^newDynArr	
	
	}
	
	insertDynamicRests { arg pitchArr, dynArr ;
		var newDynArr = [] ;
		dynArr.do({ arg item, index ;
			var pitch = pitchArr[index] ;
			var element ;
			element = if ( pitch == -inf, { -inf }, { item } ) ;
			newDynArr = newDynArr.add(element) ;
			}) ;	
		^newDynArr
	}

	convertDynamicsToIndices { arg dynArr ;
		var newDynArr = [] ;
		var value, dynamics ;
		dynArr.do ({ arg item, index ;
				if ( item == -inf, 
					{ value = -inf }, 
					{
					// the mapping formula
					value = (((item-60)/6)*1.5+1).round ;
					value = if (value < 0, { 0 }, { value }) ;
					} );
			newDynArr = newDynArr.add(value) 
		}) ;
		^newDynArr
	}
	

	assignDynamicsStrings { arg dynArr, dynamicsList, durs ;
		var i = Array.series(dynArr.size) ;
		var newDynArr = [] ;
		i.do({ arg index ;
				var item  = dynArr[index] ;
				var previous = dynArr[index - 1] ;
				var next = dynArr[index + 1] ;	  
				var glyph, mark, start ;
				previous = if ( previous == nil, { -inf }, { previous } ) ;
				next = if ( next == nil, { -inf }, { next } ) ;
				start = if ( (next - item) > 0, { "\\<"}, {"\\>"}) ;
				mark = dynamicsList[item] ;
				// this creates a bug
				// if ( mark == nil, { mark = dynamicsList[dynamicsList.size-1] }) ;
				//"\n\n__________".postln;
				//[previous, item, next].postln;
				case 
					
					// rest before
					{ item == -inf }{ glyph = [-inf, ""] }
					// no change
					{ (item == previous).and( item == next) } { glyph = ["", ""] }
					
					// near to -inf
					{ (item < previous).and(next == -inf) } { glyph = [mark, ""] }
					{ (item > previous).and(next == -inf) } { glyph = [mark, ""] }
					{ (item == previous).and(next == -inf) } { glyph = ["", ""] }					{ (previous == -inf).and(next == -inf) } { glyph = [mark, ""] }
					{ (previous == -inf).and(item != next ) } { glyph = [mark, start] }
					{ (previous == -inf).and(item == next ) } { glyph = [mark, ""] }					
					
					// positive and negative peaks
					{ (item > previous).and(item > next) } { glyph = [mark, start]}
					{(item < previous).and(item < next) } { glyph = [mark, start]}
					
					// peak but not start
					{ (item > previous).and(item >= next) } { glyph = [mark, ""]}
					{(item < previous).and(item <= next) } { glyph = [mark, ""]}
		
					// it's different
					{ (item >= previous).and(item == next) } { glyph = [mark, start]}
					{(item <= previous).and(item == next) } { glyph = [mark, start]}
					
					// increase and decrease
					{(item == previous).and(item < next) } { glyph = ["", start]} 
					{(item > previous).and(item < next) } { glyph = ["", ""]} 
					{(item <= previous).and(item > next) } { glyph = ["", ""]} 
										// this is start
					{ (previous == -inf).and(item == -inf ) } { glyph = ["", ""] } ;

					//	
					 newDynArr = newDynArr.add(glyph) ;
				//glyph.postln ; "__________\n\n".postln;
		}) ;
	//	newDynArr.postln;
	//	newDynArr = this.insertPins(newDynArr, dynamicsList) ;
	//	newDynArr.postln;"______\n\n\n\n".postln;
		newDynArr = this.processDynamicStrings(newDynArr, durs) ;
	//	newDynArr.postln;"+++++++++\n\n\n\n".postln;
		^newDynArr
	}



// private 
	// doesn't exactly work as expected
	// not that far but not ok
	insertPins { arg dynArr, dynamicsList ;
		var newArr = [], reduced = [], reduced2 = [] ;
		var add, item, index = 0, i ;
		while ( 	{ index < dynArr.size },
				{
			item = dynArr[index] ;
			// condition: if there's a dynamic mark without pin
			if ( ((item[0] != -inf).and(item[0] != "")).and((item[1]=="")),
				// yes
				{ 
					add = if (dynArr[index+1][0] == -inf, {""}, {"add"});  
					add = dynArr[index][1]+add ;	
					newArr = newArr.add([ dynArr[index][0], add]) ;
					index = index + 1;
					while ( {( dynArr[index][0]!= -inf).and(dynArr[index][0]=="")},
						{ 
						newArr = newArr.add([ dynArr[index][0], ""]) ;
						index = index+1 ;
						} 
						 ) ;
						

				},
				// no
				{add = item[1] ; 
				newArr = newArr.add([item[0], add]) ;
				index = index+1; 
				}) ;
				}
			) ;
		newArr.do({ arg item, index ;
			if ( dynamicsList.includes(item[0]).and(item[1]!="\\>").and(item[1]!="\\<"), 
				{reduced = reduced.add(item)}) ;
		}) ;
		i = 0 ;
		newArr.do({ arg item, index ;
			var mark, val, valNext, start ;
			if ( item[1] ==" add", 
				{ 
				 mark = item[0] ; 
				 val = dynamicsList.indexOf(item[0]) ;
				 valNext = dynamicsList.indexOf(reduced[i+1][0]);
				 start = case
						{ valNext > val} { "\\<" }
						{ valNext < val} { "\\>"} 
						{ valNext == val} { ""} ; 	
				// [item, reduced[i+1][0], val, valNext].postln ;
				reduced2 = reduced2.add([mark, start]) ;
				i = i+1 ;
				}, 
				{ reduced2 = reduced2.add([item[0], item[1]]) }
				) ;
		}) ;
		reduced = [];
		reduced2.do({ arg item, index ;
			var mark = if ( item[0] == -inf, { "\\!" }, { item[0]} ) ;
			reduced = reduced.add([ mark , item[1]]) ;
		}) ;
		
		^reduced ;
	}

	processDynamicStrings { arg dynArr, durs, threshold = 3 ;
		var newArr = [] ;
		var glyph ;
		dynArr[..dynArr.size-2].do({ arg item, index ;
			//[item, durs[index]].postln ; 
			if( ((item[1] == "\\<").or( item[1] == "\\>") ).and(durs[index] <= threshold ) //==1
					.and(dynArr[index+1][0] != ""), 
				{ glyph = [item[0], ""] }, { glyph = [item[0], item[1]]}) ;
			glyph = glyph[0].asString.replace("-inf", "")+glyph[1] ;
			newArr = newArr.add(glyph)				 
		}) ;
		newArr = newArr.add(dynArr[dynArr.size-1][0].asString.replace("-inf", "")
			+dynArr[dynArr.size-1][1]) ;
		^newArr
		}
		


	createVocoid { arg f1Val, f2Val ;
	// formant1 and formant2 must NOT be clustered in items and occurrences
		var vocoid ;	
		var f1 = (f1Val).midicps ; 
		var f2 = (f2Val).midicps ;
		formantDict.do({ arg vocal ;
			var vocalRange = formantDict.findKeyForValue(vocal) ;
			var xMin, xMax, yMin, yMax ;

			# xMin, xMax, yMin, yMax = vocalRange ;
			if ( f1.inclusivelyBetween(xMin, xMax).and(f2.inclusivelyBetween( yMin, yMax)), 
					{ vocoid = vocal ;
					  formantCharter.plotVocoid( f1, f2 ) ;
					}) ;  
				
		}) ;
				
		if ( (f1Val != -inf).and(vocoid == nil), 
		{"______".postln ;
		[f1, f2].postln ;
		vocoid.postln ;
		"______".postln }) ;
		^vocoid
	} 



		
	mergeVoices { arg voicesArrays, title = "Conversion", prop = false ;
	// for a2 (or other formats) format you must hack Lily: change file 
	// Resources/share/lilypond/current/scm/paper.scm  
		var header = "
\\header{
title = \""+title+"\"
tagline = \#\#f
}\n
" ;	
		var paper = "\n\n
\#(set-default-paper-size \"a3\")
\#(set-global-staff-size 11.22)

\n\n" ;
		//var paper = "\#(set-default-paper-size \"a3\")\n\n\n" ;
		var proportional = if ( prop == true,
			{ "\n\\set Score.proportionalNotationDuration = \#(ly:make-moment 1 16)\n" },
			{ "" }) ;
		var tempo = "\\tempo 4=60\n"	;
		var score =  header + "<<" + proportional ; //+ tempo ;
		var version ="\\version \"2.11.20\"\n";
		var layout = "
	
 \\layout\{
     ragged-last = \#\#t
     \\context\{
     	\\Score
     
        	\\override BarNumber  \#\'break-visibility = \#end-of-line-invisible
   		barNumberVisibility = \#(every-nth-bar-number-visible 4)
   		\\override BarNumber \#\'font-size = \#2
   		\\override BarNumber  \#\'stencil
   		= \#(make-stencil-boxer 0.1 1.0 ly:text-interface::print)

     
             \\override Glissando \#\'bound-details \#\'right \#\'padding = \#0.0
             \\override Glissando \#\'bound-details \#\'left \#\'padding = \#0.0
      \}
      \\context\{\\Staff
       			fontSize = \#-1
			    \\override VerticalAxisGroup \#\'minimum-Y-extent = \#\'(-6 . 6) 
				\% This set space around staff (i.e. under and above)
		\}

       
 \}\n\n
 
\\paper { 
	horizontal-shift = 1\\cm
	line-width = 25\\cm
	top-margin =  3\\cm
	bottom-margin = 3\\cm
	systemSeparatorMarkup = \\slashSeparator
	printpagenumber = \#\#f
	myStaffSize = \#11.22 
	\#(define fonts 
	(make-pango-font-tree 
		\"Helvetica\" 
		\"Helvetica\" 
		\"Helvetica\" 
	(/ myStaffSize 20))) 
} 

 
 " ;
		voicesArrays.do({ arg voicesArray ;
			score = score + "\\new StaffGroup <<\n" ;
			voicesArray.do({ arg item ;
				score = score + item+ "\n\n" ;
				// "\\new Staff\n{\n" + accidental + item +"}\n"
			}) ; 	
			score = score+">>\n"
		}) ;
		score = version + paper + layout + score + ">>\n\n" ;
		^score ;	
	}




// Used for percussive contoids
	createContoidNotation { arg arr, subArr, dynArr, clef = "percussion", staffNames ; 
		var durations = (1:"16", 2:"8", 3:"8.", 4:"4", 8:"2", 12:"2.", 16:"1", 
		 		20:"1", 24:"2.", 28:"7", 32:"8") ;
		var noteBlock = "\\clef \""++clef++"\"";  
		var dynamicsList = ["\\ppp", "\\pp", "\\p", "\\mp", "\\mf", "\\f", "\\ff"] ;
		var pitches = arr.clump(2).flop[0].round ; // round is useless. No harm
		var dynRef = 0 ; 
		var lyricBlock = "\\addlyrics \{\n" ;
		var current = "" ;
		var currentDyn = "" ; 
		var sfp = "" ;
		pitches[..pitches.size-2].do({ arg item, index ;
					var sub = subArr[index] ;
					var note ; 
					var dyn, lastDyn ;
					var coeff, coeff2 ;
					var start = "" ;
					var block = "", otherBlock = "", tie = "" ;
					var utteranceArr, phone, register, continuationFlag, expressiveMark = "" ;
					if ( item != -inf, { 
									utteranceArr = item.asString.split($*) ;
									phone = utteranceArr[0] ;
									register = utteranceArr[1] ;
									continuationFlag = utteranceArr[2] ; 
									expressiveMark = if (utteranceArr[3].isNil.not, 
										{ utteranceArr[3] }, {""} ) ;
									}) ;
	
					note = case	{ item <= 0 } { "r" }
							 	{ register == "0" } { "c\'" }
							 	{ register == "-1" } { "e" }
							 	{ register == "1" } { "a\'" }
							 	 ;
					noteBlock = noteBlock+note++durations[sub[0]] + expressiveMark + sfp ;
					coeff = (dynArr[dynRef]*0.1-6*3.5+1).asInteger
							.clip(0,6) ;
					dyn = dynamicsList[coeff] ;		
					dyn = if (item != -inf, { dyn }, { "" }) ;
					if ( dyn != currentDyn, { currentDyn = dyn }, { dyn = "" } ) ;
					if ( phone == current, { phone = " - " }, { current = phone }) ;
					lyricBlock = if ( item != -inf, { lyricBlock + phone}, { lyricBlock }) ;						if ( sub.size > 1,
						{sub[1..sub.size-1].do({ arg d ; 
							tie = "~" ;
							otherBlock = otherBlock++tie+note++durations[d] ;
							}) ;										coeff2 = (dynArr[dynRef+sub[..(sub.size-2)].sum]*0.1-6*3.5+1)
								.asInteger
								.clip(0,6) ;
					lastDyn = dynamicsList[coeff2] ;
							if ((item != -inf).and(lastDyn != dyn), 
								{
								if ( coeff < coeff2, { start = "\\<" }, { start = "\\>" }) ;
								if (sub.sum <= 5, {start = "" ;} ) ;
					//			otherBlock = otherBlock + lastDyn }) ;//! deleted dynamics
								otherBlock = otherBlock  }) ;
							
						}) ;
//					noteBlock = noteBlock+dyn+start+otherBlock ; // next line deletes dyns
					start = "" ; //! deleted dynamics
					noteBlock = noteBlock+start+otherBlock ;					lyricBlock = lyricBlock++"\n" ;
					dynRef = dynRef + sub.sum ;
					
					sfp = if ((item == -inf).and(pitches[index+1] != -inf), { "\\sf ->" }, {""}) ;
						}) ;
		^("\\new Staff \n\{\n
\\override Staff.NoteHead  \#\'style = \#\'cross
\\override Staff.StaffSymbol \#\'line-count = \#3 
\\override Staff.StaffSymbol \#\'line-positions = \#'(-5 0 5)\n" + staffNames
				+noteBlock+"\n\}\n"+
lyricBlock++"}\n") ;
		}



	increaseContoidDuration {arg arr ;
	// fixed on adding a eight
		var newArr ;
		var index = 0, item ;
		while { index < (arr.size-3) }
		{ item = arr[index] ;
		if ( (item == 60).and( arr[index+1] == -inf).and(arr[index+2] == -inf), 
				{ newArr = newArr.add(60); newArr = newArr.add(60); newArr = newArr.add(60);
					index = index + 3 }, 
				{ newArr = newArr.add(item) ;
					index = index + 1 }
			) ;
			}
	^newArr 
	 }



	increaseContoidDurationIteratively {arg arr, times ;
		var newArr ;
		times.do({ arg i ;
			arr = this.increaseContoidDuration(arr)
		}) ;
		^arr 
	 }

/*
// Used for percussive contoids: original
	createContoidNotationOLD { arg arr, subArr, dynArr, clef = "percussion" ; 
		var durations = (1:"16", 2:"8", 3:"8.", 4:"4", 8:"2", 12:"2.", 16:"1", 
		 		20:"1", 24:"2.", 28:"7", 32:"8") ;
		var noteBlock = "\\clef \""++clef++"\"";  
		var dynamicsList = ["\\pp", "\\p", "\\mp", "\\mf", "\\f", "\\ff", "\\fff"] ;
		// assuming freqs to midi
		var pitches = arr.clump(2).flop[0].round ; 
		var dynRef = 0 ; 
		// we discard the last one following pitch notation
		pitches[..pitches.size-2].do({ arg item, index ;
					var sub = subArr[index] ;
					var note ; 
					var dyn, lastDyn ;
					var coeff, coeff2 ;
					var start = "" ;
					var block = "", otherBlock = "", tie = "" ;
					note = if (item <= 0, { "r" }, { "c'" }) ;
					noteBlock = noteBlock+note++durations[sub[0]] ;
					coeff = (dynArr[dynRef]*0.1-6*3.5+1).asInteger
							.clip(0,7) ;
					dyn = dynamicsList[coeff] ;		
					dyn = if (item > 0, { dyn }, { "" }) ;
					
					if ( sub.size > 1,
						{sub[1..sub.size-1].do({ arg d ; 
							tie = "~" ;
							otherBlock = otherBlock++tie+note++durations[d] ;
							}) ;										coeff2 = (dynArr[dynRef+sub[..(sub.size-2)].sum]*0.1-6*3.5+1)
								.asInteger
								.clip(0,6) ;
					lastDyn = dynamicsList[coeff2] ;

							if ((item > 0).and(lastDyn != dyn), 
								{
																				//[dyn, lastDyn].postln ;
								if ( coeff < coeff2, { start = "\\<" }, { start = "\\>" }) ;
								if (sub.sum <= 5, {start = "" ;} ) ;
								otherBlock = otherBlock + lastDyn }) ;
							
						}) ;
					noteBlock = noteBlock+dyn+start+otherBlock ;
					dynRef = dynRef + sub.sum ;
						}) ;
		^("\\new Staff \n\{\n
\\override Staff.NoteHead  \#\'style = \#\'cross
\\override Staff.StaffSymbol \#\'line-count = \#3 
\\override Staff.StaffSymbol \#\'line-positions = \#'(-4 0 4)\n"+noteBlock+"\n\}\n") ;
		}


*/






}