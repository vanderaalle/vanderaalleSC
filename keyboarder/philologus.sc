Philologus {

	var <alphas ;
	var <nums ;
	var <punct ;
	var <total ;
	
	var <alphaDict ;
	var <>text ;
	var <rankDict	;

	*new { arg from ; // text is a string
		^super.new.initPhilologus(from) ;
	}
		
	initPhilologus { arg from ; // switch with respect to passed obj
		nums 	= Array.series(10, 48) ;
		alphas	= Array.series(92-65, 65)
			.addAll(Array.series(123-97, 97))
			// Italian accented vowels (up/down)
			.addAll([131, 136, 142, 143, 147, 152, 157, 233, 237, 241, 244]) 
			//problem here with enc
			.addAll([195]) ;
		punct = [10, 32, 33, 34, 39, 40, 41, 44, 45, 46, 58, 59, 63, 95] ;
		total = alphas.addAll(punct).addAll(nums) ;
		alphaDict = Dictionary. new ;
		if (from.class == String,
			{text  = from }, { text = String.readNew(from) }) ;
		this.analyzeLetters ;
	}
	
	
	analyzeLetters {
		text.do({
			arg alpha ;
			alpha = alpha.asSymbol ;
			if (alphaDict.includesKey(alpha),
				{ alphaDict[alpha] = alphaDict[alpha] + 1 }, 
				{ alphaDict[alpha] = 1 }
			)
		}) ;		
		this. createRankDict ;
	}

	createRankDict {
		var num ;
		rankDict = Dictionary.new ;
		alphaDict.keys.do({ arg alpha ;
			num = alphaDict[alpha] ; 
			if (rankDict.includesKey(num),
				{ rankDict[num] = rankDict[num].addAll(alpha) }, 
				{ rankDict[num] = [alpha] }
			)
		})
	}

	mostPresent { arg howMany ;
		var whichKeys ;
		howMany = howMany ?  rankDict.size ;
		whichKeys = rankDict.keys.asArray.sort.reverse[..(howMany-1)] ;
		whichKeys.do({ arg key ;
			(rankDict[key].asString + ": " + key.asString).postln 
		})
		^whichKeys
	}
	
	maxRank {
		// maxs presences
		^rankDict.keys.asArray.sort.reverse[0] ;
	}
	
	// the present items
	items { ^alphaDict.keys }
	
	// how many?
	numItems { ^alphaDict.keys.size }

	// lenght of the text
	size { ^text.size }
	
	// interfaces to GUI
	makeTable { arg hStep, vStep, range = true ;
		PhilologusGuiTable(this, hStep, vStep, range) 
	}
	
	makeScore { arg hStep, vStep, time = true ;
		PhilologusGuiScore( this, hStep, vStep, time)
	}
	
}


PhilologusGuiTable {
	
	var <>philologus, alphaDict ;
	var <window, <>hStep, <>vStep ;
	var <>width, <>height ;
	var drawBars, drawRange, <>range ;
	
	*new { arg philologus, hStep, vStep, range = true  ; // text is a string
		^super.new.initPhilologusGui(philologus, hStep, vStep, range) ;
	}

	
	initPhilologusGui { arg aPhilologus, aHStep, aVStep, aRange ;
		var maximum ;
		range = aRange ;
		philologus = aPhilologus ;
		maximum = maximum ? philologus.maxRank ;
		alphaDict = philologus.alphaDict ; 
		hStep = aHStep ;
		hStep = hStep ? 15 ;
		vStep = aVStep ;
		vStep = vStep ? 600/maximum ;
		width = hStep* philologus.total.size ;
		height = vStep*maximum ;
		window = GUI.window.new("Letters", Rect(100, 100, width, height)) ;
		drawBars = { arg maximum ;
			var height ;
			philologus.total.do({ arg ch, i ;
				ch = ch.asAscii.asSymbol ;
				if ( alphaDict.includesKey(ch) )
				{
				height = alphaDict[ch]*vStep ;
				GUI.pen.color = Color.black ;
				GUI.pen.fillRect(Rect(i*hStep, 0, hStep, height))
				} ;
				GUI.pen.color = Color.red ;
				GUI.pen.stringAtPoint(ch.asString, i*hStep+(hStep*0.25) @ 2) ;
			}) ;
		} ;
		drawRange = { arg step ;	
		       	step = step ? vStep ;
		       	GUI.pen.strokeColor = Color(0.7, 0.7, 0.7, 0.7) ;
				maximum.do({ arg n ;
					GUI.pen.line(0 @ (n+1*step), width @ (n+1*step)) ;
				}) ;
			GUI.pen.stroke ;	
			} ;

		window.drawHook = { 
			drawBars.value(maximum) ;
			if (range){ drawRange.value } ;
		} ;
		window.front
	}

	

}


