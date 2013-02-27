// sometimes you just need a chronometer
Chronometer {
	
	var <>w, <>x, <>y, <>clockField ;
	var <>r, <>quant, <>startTime, <>title ;

	*new { arg x = 30, y = 120, title ="Tempus fugit", quant = 1 ;�
		^super.new.initChronometer(x, y, title, quant)�
	}

	initChronometer { arg aX, aY, aTitle, aQuant ;
		x = aX ;
		y = aY ;		�
		title = aTitle ;
		quant = aQuant ;
		startTime = thisThread.seconds ;
		this.createGUI(x, y, title, quant) ;
	}�
	
	createGUI { arg x = 10, y = 120, title =� "Tempus fugit", quant = 1 ;
		w = Window.new(title, Rect(x, y, 200, 60)) ;
		clockField = StaticText.new(w, Rect(5,5, 190, 30))
			.align_(\center)
			.stringColor_(Color(1.0, 0.0, 0.0))
			.background_(Color(0,0,0))
			.font_(Font.new("Optima", 24)) ;�
		r = Task.new({ arg i ;�
			inf.do({�arg times ;
			clockField.string_((thisThread.seconds-startTime).round(quant).asTimeString) ;
			quant.wait })// a clock refreshing once a second
			}).play(AppClock) ;	
		w.front ;
		w.onClose_({
			r.stop ;
			}) ;

	}


	close {w.close}

}