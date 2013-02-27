+ SynthDef {

	playGUI { arg target,args,addAction=\addToHead;
	// creates a GUI where synthDef's args 
	// can be controlled thru sliders
	// min|max define each slider's range
	// av 14/04/07
			
		var controlArr = [], guiArr = [] ;
		
		var window ; 
		
		var synth = this.play(target,args,addAction=addAction) ;
		
		
		this.allControlNames.do({ arg item ;
				var name = item.asString.split($ )[4] ;
				controlArr = controlArr.add([name, 0.0, 1.0])
				}) ;		
		window = GUI.window.new(this.name++" Control Panel", 
								Rect(30,30, 900, controlArr.size+2*30)) ;
	
		// labels
		GUI.staticText.new( window, Rect( 240, 0, 50, 20 ))
						.string_( "min" ).align_( \center) ;
		GUI.staticText.new( window, Rect( 300, 0, 50, 20 ))
						.string_( "max" ).align_( \center) ;
						 
		
		// GUI creation
		controlArr.do({ arg item, ind ;
				var index = ind+1 ;
				var guiElement = [
				GUI.staticText.new( window, Rect( 20, 30*index, 200, 20 ))
							.string_( item[0] ).align_( \right),
				GUI.numberBox.new( window, Rect( 240, 30*index, 50, 20 ))
						.value_(item[1]),
				GUI.numberBox.new( window, Rect( 300, 30*index, 50, 20 ))
						.value_(item[2]),				
				GUI.slider.new( window, Rect( 370, 30*index, 340, 20 )),
				GUI.staticText.new( window, Rect( 720, 30*index, 200, 20 ))
						.string_( 0.0 ) 
				] ;
				guiArr = guiArr.add(guiElement) ;			
		}) ;				
		
		// GUI action definition
		controlArr.do({ arg item, index ;
			var guiElement = guiArr[index] ;
			guiElement[1].action  = { arg minBox ;
				item[1] = minBox.value ;
			} ;
			guiElement[2].action  = { arg maxBox ;
				item[2] = maxBox.value ;
			} ;
			guiElement[3].action = { arg slider ; 
				var name = item[0] ;
				var range = item[2].value - item[1].value ;
				var offset = item[1].value ;
				var paramValue = slider.value*range + offset ;
				//[name, paramValue].postln ;
				synth.set(name, paramValue) ;
				guiElement[4].string_(paramValue.trunc(0.0001) ) ; 
				}
		}) ;	
		
		window.onClose_({ synth.free }) ;
		
		window.front ;
		
		^synth
	}

}