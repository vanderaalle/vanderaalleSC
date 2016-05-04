ControlBusViewer {

	var <>bus, <>minVal, <>maxVal ;
	var <>rate ;
	var <>window, <>knob, <>label, <>name ;
	var <>task ;

	// constructor: you can start with an existing graphDict
	*new { arg bus, minVal, maxVal, rate = 10, name = "bus" ;
		^super.new.initBCV(bus,  minVal, maxVal, rate, name)
	}

	initBCV { arg aBus, aMinVal, aMaxVal, aRate, aName ;
		bus = aBus;
		minVal = aMinVal; maxVal = aMaxVal;
		rate = aRate ;
		name = aName ;
		window = Window(name, Rect(300, 400, 220, 300)).front ;
		knob = Knob(window, Rect(10, 10, 200, 200)) ;
		label = StaticText(window, Rect(70, 210, 100, 50)) ;
		this.setTask ;
		this.start;
	}

	setTask {
		task = Task({
			inf.do{
				bus.get { arg val ;
					{label.string = val ;
					knob.value = val.linlin(minVal, maxVal, 0,1)
					}.defer
				} ;
				rate.reciprocal.wait
			}
		})
	}

	start { task.start}
	stop { task.stop }
}

