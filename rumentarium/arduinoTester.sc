ArduinoTester {

	var <>arduino, <>address, <>names, <>baud ;
	var window ;
	
	*new { arg address = "/dev/tty.usbserial-A9007LnA", names = [], baud = 115200 ;
		^super.new.initArduinoTester(address, names, baud)
	}
	
	initArduinoTester { arg anAddress, aNames, aBaud ;
		arduino = ArduinoSMS(anAddress, aBaud) ;
		address = anAddress ;
		baud = aBaud ;
		names = aNames ;
		this.createInterface(names) ;
	}

	createInterface { arg names  = [];
		var col = Color.hsv(0.8, 0.8, 0.9) ;
		window = GUI.window.new(address, Rect(100, 100, 300, 500)) ;
		window.view.background_(col) ;
		[3,5,6,9,10,11].do({arg item, index ;
			var slider, num, lab ;
			slider = GUI.slider.new(window, Rect(index*50, 0, 50, 400))
				.value_(0) ;
			lab = GUI.staticText.new(window, Rect(index*50+20, 410, 45, 25))
				.string_(item.asString) ;
			lab.font = GUI.font.new("Century Gothic", 20 );
			lab.background = col ;
			lab.stringColor_(Color.white) ;
			lab = GUI.staticText.new(window, Rect(index*50+20, 430, 45, 25))
				.string_(names[index]) ;
			lab.font = GUI.font.new("Century Gothic", 10 );
			lab.background = col ;
			lab.stringColor_(Color.white) ;

			num = GUI.numberBox.new(window, Rect(index*50+10, 460, 35, 25))
				.value_(0) ;
			slider.action_({ arg sl ;
					arduino.send($w, $a, item, sl.value*255) ;
					num.value_((sl.value*255).round)
					}) ;
			num.action_({ arg num ;
					arduino.send($w, $a, item, num.value) ;
					slider.value_(num.value/255)
					}) ;		
		}) ;
		window.front
	}

	close{ arduino.close}
}