
RuEasyGui24 {

	var <>ruMaster ;
	var window ;

	*new { arg ruMaster ;
		^super.new.initRuGui(ruMaster) ;
	}

	initRuGui { arg aRuMaster ;
		var vl, sl, bl, zeroB ;
		ruMaster = aRuMaster ;
		window = Window.new("Ru24 Easy Gui", Rect (20, 20, 800+20, 500)).front ;
		vl = Array.fill(24, {|i| StaticText(window, Rect(800/24*i+10, 380, 800/24, 80)).string_("-") }) ;

		24.do{|i|
			StaticText(window, Rect(800/24*i+20, 80, 800/24, 50)).string_(i+1) ;
			StaticText(window, Rect(800/24*i+20, 90, 800/24, 50)).string_(ruMaster.instrDict.findKeyForValue(i+1)).font_(Font("Avenir Next Condensed", 8)) ;
			sl = sl.add(
				Slider(window, Rect(800/24*i+10, 130, 800/24, 260))
				.action_{|v|
					ruMaster.set(i+1, v.value);
					vl[i].string_(v.value.round(0.01));
					}
				)

			} ;

		bl = Array.fill(24, {|i| Button(window, Rect(800/24*i+10, 20, 800/24, 60))
			.states_([["off", Color.grey, Color.black], ["on", Color.red, Color.black]])
			.action_{|me| ruMaster.set(i+1, me.value.postln); sl[i].valueAction_(me.value) ;}
			 }
			) ;

		zeroB = Button(window, Rect(10, 450, 80,40))
			.states_([["zero!", Color.white, Color.red]])
			.action_{ruMaster.zero; bl.do{|but, i| sl[i].valueAction_(0) }}
	}

}

/*
~rui = RuEasyGui24.new(k) ;
*/