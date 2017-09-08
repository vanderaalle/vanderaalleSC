// sometimes you just need a chronometer
Chronometer {

	var <>window, <>x, <>y, <>clockField ;
	var <>r, <>quant, <>startTime, <>title ;

	*new { arg x = 30, y = 120, title ="Tempus fugit", quant = 1, window ;
		^super.new.initChronometer(x, y, title, quant, window)
	}

	initChronometer { arg aX, aY, aTitle, aQuant, aWindow ;
		x = aX ;
		y = aY ;
		title = aTitle ;
		quant = aQuant ;
		startTime = thisThread.seconds ;
		this.createGUI(x, y, title, quant, aWindow) ;
	}

	createGUI { arg x = 10, y = 120, title = "Tempus fugit", quant = 1, win ;
		if (win.isNil){
			window = Window.new(title, Rect(x, y, 200, 60)) }
		{ window = win} ;
		clockField = StaticText.new(window, Rect(5,5, 190, 30))
			.align_(\center)
			.stringColor_(Color(1.0, 0.0, 0.0))
			.background_(Color(0,0,0))
			.font_(Font.new("Optima", 24)) ;
		r = Task.new({ arg i ;
			inf.do({ arg times ;
			clockField.string_((thisThread.seconds-startTime).round(quant).asTimeString) ;
			quant.wait })// a clock refreshing once a second
			}).play(AppClock) ;
		window.front ;
		window.onClose_({
			r.stop ;
			}) ;

	}


	close {window.close}

}