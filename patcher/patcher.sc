Patcher {

	var <>defDict ; 
	var <>audioDict, <>controlDict ;
	var <>audioNum, <>controlNum ;
	var <>ind ;

	*new { arg audioNum = 20, controlNum = 20 ;
		^super.new.initPatcher(audioNum, controlNum)
	}

	initPatcher { arg anAudioNum, aControlNum ;
		audioNum = anAudioNum ;
		controlNum = aControlNum ;
		audioDict = IdentityDictionary.new ;
		controlDict = IdentityDictionary.new ;
		// we are using stereo
		audioDict[0] = 0 ;
		audioDict[1] = 1 ;
		audioNum.do({ arg i ;
			audioDict[i+2] = Bus.audio(Server.local) ;
		}) ;
		controlNum.do({ arg i ;
			controlDict[i] = Bus.control(Server.local) ;
		}) ;

		defDict = IdentityDictionary.new ;
		ind = 0 ;
	}

	addDef { arg def, target, addAction=\addToHead ;
		// here we create a synth
		var synth ;
		synth = def.play(target,addAction=addAction) ;
		defDict[def.name.asSymbol] = [def, synth, nil, ind] ;
		ind = ind +1 ;
	}


	

}


PatcherGui {

	var <>patcher, <>controller ;
	var audioSat ;
	var controlSat ;
	var window1, flow1, window2, flow2 ;
	
	*new { arg patcher, controller ;
		^super.new.initPatcherGui(patcher, controller) 
	}

	initPatcherGui { arg aPatcher, aController ;
		patcher = aPatcher ;
		controller = aController ; // external MIDI controller --> dirty implementation 
		window1 = GUI.window.new("Patcher KR Control Panel", Rect(30,30, 600,1000), scroll:true) ;
		flow1 = FlowLayout.new( window1.view.bounds );
	    	window1.view.decorator = flow1;
	     window1.view.background_( Color.hsv(0.9, 0.8, 0.8)) ;

								//Rect(30,30, 600, controlArr.size+1*30)) ;
		window2 = GUI.window.new("Patcher AR Control Panel", Rect(30+610,30, 600,1000), scroll:true) ;
		flow2 = FlowLayout.new( window2.view.bounds );
	    	window2.view.decorator = flow2;
	     window2.view.background_( Color.hsv(0.5, 0.8, 0.8) ) ;
								//Rect(30,30, 600, controlArr.size+1*30)) ;

	}

	makeAllGui { arg vStep = 10 ;
		var indDict = IdentityDictionary.new ;
		patcher.defDict.keys.do ({ |defName| 
			indDict[patcher.defDict[defName][3]] = defName ;
		}) ;
		
		indDict.keys.asArray.sort.do({ |ind| this.makeGui(indDict[ind], vStep)
		})
		//patcher.defDict.keys.do({ |defName| this.makeGui(defName, vStep) })
	}
	
	scope {
		patcher.controlDict.do({ arg bus ;
			bus.scope	
			})
		
	}
	
	makeGui { arg defName, vStep = 20 ;
	// creates a GUI where synthDef's args 
	// can be controlled thru sliders
	// min|max define each slider's range
	// av 14/04/07
			
		var controlArr = [], guiArr = [] ;
		var st ;
		
		var window, flow ;
		var def = patcher.defDict[defName][0] ; 
		var synth = patcher.defDict[defName][1] ; 
//		var rate = if (def.children.select({ |item| item.rate==\audio})==[]) 
		var rate = if(def.children.select({ |item| item.name.asSymbol == \Out && item.rate==\audio})==[])
			{\control} {\audio} ;
		var dict = if (rate == \control) {patcher.controlDict} {patcher.audioDict};
		def.allControlNames[1..].do({ arg item ;
				var name = item.asString.split($ )[4] ;
				var max = item.asString.split($ )[6].asFloat ;
				controlArr = controlArr.add([name, 0.0, max])
				}) ;
		window  = if ( rate==\control ) { window1 } { window2 } ;
		flow  = if ( rate==\control ) { flow1 } { flow2 } ;
		
		flow.nextLine ;

		GUI.staticText.new( window, Rect( 60, 0, 400, vStep ))
							.string_(def.name+" " ).align_( \right)
							.font_(GUI.font.new("Futura", vStep))
							 ;		
		
		GUI.button.new(window, Rect( 20, 0, vStep*3, vStep ))
			.states_([
				["stop", Color.white, Color.red],
				["start", Color.hsv(0.1,1,0.85), Color.white]
			])
			.action_({ arg butt ;
				if (butt.value == 0) {synth.run(true)} {synth.run(false)}
			} )
			
		 ;
		flow.nextLine ;
	
		// labels
		GUI.staticText.new( window, Rect( 60, 0, 180, vStep ))
							.string_("  in" ).align_( \left)
							.font_(GUI.font.new("Helvetica", vStep*0.75)) ;

		GUI.staticText.new( window, Rect( 160, 0, 50, vStep ))
						.string_( "min" ).align_( \center)
						.font_(GUI.font.new("Helvetica", vStep*0.75)) ;
		GUI.staticText.new( window, Rect( 240, 0, 205, vStep ))
						.string_( "     max" ).align_( \left)
						.font_(GUI.font.new("Helvetica", vStep*0.75)) ;
		GUI.staticText.new( window, Rect( 400, 0, 50, vStep ))
						.string_( "out -->" ).align_( \center) 						.font_(GUI.font.new("Helvetica", vStep*0.75)) ;
		GUI.textField.new(window, Rect( 860, 0, 60, vStep ))
			.font_(GUI.font.new("Helvetica", vStep*0.75)) 
			.action_({ arg txt ;
			var val = txt.string.asInteger ;
			var bus = dict[val] ; 
			txt.boxColor_(Color.hsv(1.0/patcher.controlNum*val, 1, 1)) ;
			synth.set(\out, bus) ;
			patcher.defDict[synth.defName.asSymbol][2] = val
			}) ;
		
		flow.nextLine ;						
		// GUI creation
		controlArr.do({ arg item, ind ;
				var index = ind+1 ;
				var guiElement = [
				GUI.textField.new( window, Rect( 20, 30*index, 80, vStep ))
						.string_("")
						.font_(GUI.font.new("Helvetica", vStep*0.75)) ,
				GUI.staticText.new( window, Rect( 40, 30*index, 100, vStep ))
							.string_( item[0] ).align_( \right)							.font_(GUI.font.new("Helvetica", vStep*0.75)),
				GUI.numberBox.new( window, Rect( 160, 30*index, 50, vStep ))
						.value_(item[1])							.font_(GUI.font.new("Helvetica", vStep*0.75)) ,
				GUI.numberBox.new( window, Rect( 240, 30*index, 50, vStep ))
						.font_(GUI.font.new("Helvetica", vStep*0.75)) 
						.value_(item[2]),				
				GUI.slider.new( window, Rect( 310, 30*index, 200, vStep )),
				GUI.textField.new( window, Rect( 520, 30*index, 60, vStep ))
						.string_( "0.0" )
						.font_(GUI.font.new("Helvetica", vStep*0.75)) 
				] ;
				guiArr = guiArr.add(guiElement) ;	
				flow.nextLine ;		
		}) ;				
		
		// GUI action definition
		controlArr.do({ arg item, index ;
			var guiElement = guiArr[index] ;
			var paramValue = 0 ;
			var range ;
			var bus ;
			var act ;
			guiElement[0].action  = { arg mapBox ;
					var val = mapBox.value ;
					if (val[0].isDecDigit)
						{ val = val.asInteger ;
				 		synth.map(item[0].asSymbol, val) ;
				 		mapBox.boxColor_(Color.hsv(1.0/patcher.controlNum*val, 1, 1)) ;
				 		patcher.controlDict[val]
				 		}
				 	// dirty, not abstract implementation: works...
				 	{ 
				 	// for triggering
				 	act = if ([\aHead, \bHead].includes((val).asSymbol) )
				 		{ { |cVal| guiElement[4].valueAction_(1.0.rand) }  }
				 		{ { |cVal| guiElement[4].valueAction_(cVal/127) } }
				 		;
				 	controller.perform((val++"_").asSymbol, act)
				 		}
			} ;
			guiElement[2].action  = { arg minBox ;
				var val ;
				item[1] = minBox.value ;
				val = (paramValue-item[1].value)/(item[2].value-item[1].value) ;
				guiElement[4].valueAction_(val) ;
			} ;
			guiElement[3].action  = { arg maxBox ;
				var val ;
				item[2] = maxBox.value ;
				val = (paramValue-item[1].value)/(item[2].value-item[1].value) ;
				guiElement[4].valueAction_(val) ;
				
			} ;
			guiElement[4].action = { arg slider ; 
				var name = item[0] ;
				var offset = item[1].value ;
				range = item[2].value - item[1].value ;
				paramValue = slider.value*range + offset ;
				guiElement[0].value_("") ;
				synth.set(name, paramValue) ;
				guiElement[5].string_(paramValue.trunc(0.0001).asString ) ; 
				guiElement[0].boxColor_(Color.white) ; 
				} ;
			guiElement[5].action = { arg txt ;
				var name = item[0] ;
				var val = txt.value.asFloat ;
				case 
				
				{ val > item[2] } 
					{ paramValue = val ;
					guiElement[3].valueAction_(val) } 
				{ val < item[1] } 
					{ paramValue = val ; 
					guiElement[2].valueAction_(val) }
				{ val.inclusivelyBetween(item[1], item[2]) }
					{
					synth.set(name, val) ;
					guiElement[4].valueAction_(val/range) }
					} 
				;
			guiElement[5].value_(item[2]) ;
			guiElement[4].value_(1) ;
		}) ;	
		window.onClose_({ synth.free }) ;
		window.front ;
	}
		
}
