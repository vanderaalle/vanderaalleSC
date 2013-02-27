SOGui {
	
	var <so, <objectDict ;
	var <window, <textBox ;
	var slP, slA, bxP, bxA, vA, vP, bP, bA, bS ;
	var <hStep, <vStep ;
	var actual ; // the name to be set 
	
	*new { arg so ;
		^super.new.initSOGui(so)
			.createEntryBoxes ;
		}

	initSOGui { arg aSo ;
		var title = aSo.soundPath.split($/).reverse[0].split($.)[0] ;
		so = aSo ;
		objectDict = so.objectDict ;
		hStep = 200 ; 
		vStep = 30 ;
		window = GUI.window.new(title, Rect(200, 200, hStep*2+20+40, vStep * 10+70)).front ;
		window.view.background_(Color.white) ;
		this.createEntryBoxes ;
		this.createSecondBlock ;
		so.addDependant(this) ;
	}


	createEntryBoxes {
		var texts = ["Name",  "Attack", "Duration", "Profile", "Caliber", "Variation", 
			"Site", "Dynamics", "Group", "Text", "Rank"] ;
		var name,  profile, caliber, variation, 
				attack, duration, site, dynamics, group, text ;
		var first ;
		// labels
		Array.fill(10, { arg ind ;
			GUI.staticText.new(window, 
				Rect(10,  ind*vStep+10, 80, vStep-10))
				.stringColor_(Color.new255(0, 47, 167))
				.string_(texts[ind] ;
			)
		}) ;
		// entry fields
		textBox = Array.fill(8, { arg i ;
			var ind = i+1 ;
			GUI.textField.new(window,									Rect(hStep-100,  ind*vStep+10, hStep, vStep-10))
					.action_({ arg view, state ...params ; 
						name 		= textBox[0].string.asSymbol ;
						attack 		= textBox[1].string.asFloat ;
						duration 		= textBox[2].string.asFloat ;
						profile 		= textBox[3].string.asFloat.clip(-2.5, 2.5) ;
						caliber 		= textBox[4].string.asFloat.clip(0.0, 2.0) ;
						variation		= textBox[5].string.asFloat.clip(0.0, 3.0) ;
						site 		= textBox[6].string.asFloat ;
						dynamics 		= textBox[7].string.asFloat ;
						group 		= textBox[8].string.asInteger ;
						text 		= textBox[9].string ;
						so.changeAll(name, attack, duration, profile, caliber, variation, 
							site, dynamics, group, text)
						}) ;
				}) ;
		// name
		textBox = textBox.insert(0,
			GUI.textField.new(window,	
				Rect(hStep-100, 10, hStep*0.5-5, vStep-10))
					.action_({ arg view, state ...params ; 
						name = textBox[0].string.asSymbol ;
						actual = name ;
						# attack, duration, profile, caliber, variation, 
							site, dynamics, group, text = objectDict[name][0..8] ;
						textBox[1].string_(attack.asString) ;
						textBox[2].string_(duration.asString) ;
						textBox[3].string_(profile.asString) ;
						textBox[4].string_(caliber.asString) ;
						textBox[5].string_(variation.asString) ;
						textBox[6].string_(site.asString) ;
						textBox[7].string_(dynamics.asString) ;
						textBox[8].string_(group.asString) ;
						textBox[9].string_(text) ;
						so.play(name)
						}) 

				) ;
		// for text	
		textBox = textBox.add(
			GUI.textView.new(window,								Rect(hStep-100,  10+(vStep*9), hStep, vStep+50))
				.action_({ arg view, state ...params ; 
					if ( (state == 'insert').and(params[2] == "\n"), 
							{ 
						name 		= textBox[0].string.asSymbol ;
						attack 		= textBox[1].string.asFloat ;
						duration 		= textBox[2].string.asFloat ;
						profile 		= textBox[3].string.asFloat.clip(-2.5, 2.5) ;
						caliber 		= textBox[4].string.asFloat.clip(0.0, 2.0) ;
						variation		= textBox[5].string.asFloat.clip(0.0, 3.0) ;
						site 		= textBox[6].string.asFloat ;
						dynamics 		= textBox[7].string.asFloat ;
						group 		= textBox[8].string.asInteger ;
						text 		= textBox[9].string ;
						so.changeAll(name, attack, duration, profile, caliber, variation, 
							site, dynamics, group, text)
							}
							)
					})
			) ;
		// rename
		textBox = textBox.add(
			GUI.textField.new(window,									Rect(hStep*0.5+hStep+5-100, 10, hStep*0.5-5, vStep-10))
					.action_({ arg view, state ...params ; 
						name 		= textBox[0].string.asSymbol ;
						attack 		= textBox[1].string.asFloat ;
						duration 		= textBox[2].string.asFloat ;
						profile 		= textBox[3].string.asFloat.clip(-2.5, 2.5) ;
						caliber 		= textBox[4].string.asFloat.clip(0.0, 2.0) ;
						variation		= textBox[5].string.asFloat.clip(0.0, 3.0) ;
						site 		= textBox[6].string.asFloat ;
						dynamics 		= textBox[7].string.asFloat ;
						group 		= textBox[8].string.asInteger ;
						text 		= textBox[9].string ;
						so.rename(name, view.string) ;
						textBox[0].string_(view.string) ;
						view.string_("")
						}) ;			
			) ;

	}	


	createSecondBlock { 
		var pitch = 0, amp = 0;
		var sine ; 
		sine = Synth(\sineDef, [\pitch, pitch, \amp, amp]).run(false) ;
		GUI.staticText.new(window, Rect(320+10, 0, 50, 20)).string_("Pitch") ;
		GUI.staticText.new(window, Rect(320+60, 0, 50, 20)).string_("Amp") ;
		slP = GUI.slider.new(window, Rect(320+0, 20, 50, 260)) ;
		slA = GUI.slider.new(window, Rect(320+50, 20, 50, 260)) ;
		bxP = GUI.numberBox.new(window, Rect(320+10, 280, 40, 20)) ;
		bxA = GUI.numberBox.new(window, Rect(320+60, 280, 40, 20)) ;
		bP  = GUI.button.new(window, Rect(320+10, 310, 90, 50))
			.states_([["sine: start", Color.red], ["sine: stop", Color.black]]) ;
		// actions
		slP.action = { arg sl ; pitch = (sl.value*8+1).octcps ; bxP.value = pitch.cpsoct ;
			sine.set(\pitch, pitch, \amp, amp) ;
			if (actual.notNil)
				{so.changeSite(actual, pitch.cpsoct)}
			 };
		bxP.action = { arg bp ; pitch = bp.value.clip(1, 9).octcps ;
			 slP.value = pitch.cpsoct.linlin(1.0,9.0, 0.0,1.0) ;
			if (actual.notNil)
				{so.changeSite(actual, pitch)}
			  };
		slA.action = { arg sl ; amp = (sl.value*9) ; bxA.value = amp ; sine.set([\amp, amp]) ;
			if (actual.notNil)
				{so.changeDynamics(actual, amp)}
		 	};		
		 		bxA.action = { arg bp ; amp = bp.value.clip(0,9) ;
			 slA.value = amp/9 ; sine.set([\amp, amp.linlin(0, 9, -96, 0).dbamp]) ;
			 if (actual.notNil)
				{so.changeDynamics(actual, amp)}
			  };
		slA.valueAction = 0 ; bxA.value = 0 ;
		slP.valueAction = 0 ; bxP.value = 0 ;

		bP.action = { arg b ; if ( b.value == 1) 
				{ sine.run(true) }
				{ sine.run(false) } 	
		} ;

	}	



	update {  arg model, what, extObjectDef ;
		var name, attack, duration, profile, caliber, variation, 
			site, dynamics, group, text, rank ;
		# name, attack, duration, profile, caliber, variation, 
			site, dynamics, group, text, rank = extObjectDef ;
		textBox[0].string_(name.asString) ;
		textBox[1].string_(attack.asString) ;
		textBox[2].string_(duration.asString) ;
		textBox[3].string_(profile.asString) ;
		textBox[4].string_(caliber.asString) ;
		textBox[5].string_(variation.asString) ;
		textBox[6].string_(site.asString) ;
		textBox[7].string_(dynamics.asString) ;
		textBox[8].string_(group.asString) ;
		textBox[9].string_(text) ;
	}
	
}
