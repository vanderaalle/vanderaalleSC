PhilologusGuiScore {
	
	var <>philologus, alphaDict ;
	var <window, <>hStep, <>vStep ;
	var <>width, <>height ;
	var drawBars, drawCharRange, drawTimeRange, drawChars, <>time ;
	
	*new { arg philologus, hStep, vStep, time  ; // text is a string
		^super.new.initPhilologusGui(philologus, hStep, vStep, time) ;
	}

	
	initPhilologusGui { arg aPhilologus, aHStep, aVStep, aTime ;
		time = aTime ;
		philologus = aPhilologus ;
		alphaDict = philologus.alphaDict ; 
		vStep = aHStep ;
		vStep = vStep ? 10 ;
		hStep = aHStep ;
		hStep = hStep ? 1000/philologus.text.size ;
		width = hStep * philologus.text.size ;
		height = vStep * philologus.total.size ;
		window = GUI.window.new("Letters", Rect(100, 100, width, height)) ;
		window.view.background = Color.white ;
		drawCharRange = { arg maximum ;
			var height ;
			philologus.total.do({ arg ch, i ;
				ch = ch.asAscii.asSymbol ;
				GUI.pen.color = Color.red ;
				GUI.pen.stringAtPoint(ch.asString, 1 @ i*vStep-4) ;
			}) ;
			GUI.pen.strokeColor = Color(0.7, 0.7, 0.7, 0.7) ;
			philologus.total.size.do({ arg n ;
				GUI.pen.line(0 @ (n+1)*vStep, width @ (n+1)*vStep) ;
				}) ;
			GUI.pen.stroke ;	
		} ;
		drawTimeRange = { arg step ;	
		       	step = step ? hStep ;
		       	GUI.pen.strokeColor = Color(0.7, 0.7, 0.7, 0.7) ;
				philologus.text.size.do({ arg n ;
					GUI.pen.line((n+1)*step @ 0, (n+1)*step @ height) ;
				}) ;
			GUI.pen.stroke ;	
			} ;
		drawChars = {
			var ind ;
			philologus.text.do({ arg ch, i ;
				//ch.postln.ascii.postln ;
				ind = philologus.total.indexOf(ch.ascii) ;
				GUI.pen.color = Color.black ;
				GUI.pen.fillRect(Rect(i*hStep, ind*vStep, hStep, vStep))
			})
		} ;
		window.drawHook = { 
			drawChars.value ;
			drawCharRange.value ;
			if (time) { drawTimeRange.value } ;
		} ;
		window.front
	}

	

}