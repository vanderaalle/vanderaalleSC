/*
THE BABAKOTO PROJECT: BabaXSession +  gui
-a- started: ~20/06/08

Defines an interface to M-Audio XSession interface
through the intermediate XSession class

It implements a parse method. This is called by BabaParser on the controller var.
When in BabaParser you call the command "ext", it forwards the following line to 
the controller by calling its parse methos.

BabaXSessionGui is just a GUI for BabaXSession

Last updated: 030708

// andrea valle
// http://www.cirma.unito.it/andrea/
// andrea.valle@unito.it

*/


BabaXSession {

	var <>xSession ;	
	var <>patcher, <>tasker ;
	var <>mapDict ;
		
	*new { 	arg patcher, tasker ;
			^super.new.initBabaXSession(patcher, tasker);
	}	

	initBabaXSession { arg aPatcher, aTasker ;
		patcher = aPatcher ;
		tasker = aTasker ;
		xSession = XSession.new ;
		mapDict = IdentityDictionary.new ;
	
	}
	
	parse { arg arr ;
			var selector, method, argName, val, xElement ;
			var act ;
			selector = arr[0].asSymbol ;
			method = \set ; 
			argName = arr[1].asSymbol ;
			xElement = arr[2].asSymbol ;
			act =  
			if ([\aHead, \bHead].includes((xElement).asSymbol) )
				{ { |cVal| patcher.perform(method, selector, argName, 1.0.rand) }  }
				{ { |cVal|
				val = cVal.linlin(0, 127, 								patcher.getMin(selector, argName), 
					patcher.getMax(selector, argName)) ;
				patcher.perform(method, selector, argName, val) ;
			} } ;
			xSession.perform((xElement++"_").asSymbol, act) ;
			mapDict[xElement] = [selector, argName] ;
			this.changed ;
	}

	gui { arg step = 50 ;
		BabaXSessionGui(this, step).makeGui
	
	}

}


BabaXSessionGui {

	var <>xSession ;
	var window, step ;
	var window, guiDict ;
	
	*new { arg xSession, step = 50 ;
		^super.new.initXGui(xSession, step) 
	}

	initXGui { arg anXSession, aStep ;
		xSession = anXSession ;
		xSession.addDependant(this) ;
		guiDict = IdentityDictionary.new ;
		step = aStep ;
	}

	makeGui { 
		var item, arr ;
		window = GUI.window.new("XSession", Rect(850, 0, 600, 300)) ;
		guiDict[\a1] = [JKnob.new(window, Rect(step, step, step*0.45,step*0.45)), 
			GUI.textField.new(window, Rect(step*1.5, step, step*1.5, step*0.5)),
			GUI.staticText.new(window, Rect(step, step*1.15, step, step)).string_("a1")
			] ;
		
		guiDict[\a2] = [JKnob.new(window, Rect(step, step*2, step*0.45,step*0.45)), 
			GUI.textField.new(window, Rect(step*1.5, step*2, step*1.5, step*0.5)),
			GUI.staticText.new(window, Rect(step, step*2.15, step, step)).string_("a2")
			] ;
		guiDict[\a3] = [JKnob.new(window, Rect(step, step*3, step*0.45,step*0.45)), 
			GUI.textField.new(window, Rect(step*1.5, step*3, step*1.5, step*0.5)),
			GUI.staticText.new(window, Rect(step, step*3.15, step, step)).string_("a3")
			] ;
		//
		guiDict[\aHi] = [JKnob.new(window, Rect(step*3.5, step, step*0.45,step*0.45)), 
			GUI.textField.new(window, Rect(step*4, step, step*1.5, step*0.5)),
			GUI.staticText.new(window, Rect(step*3.5, step*1.15, step, step)).string_("aHi")
			] ;
		
		guiDict[\aMid] = [JKnob.new(window, Rect(step*3.5, step*2, step*0.45,step*0.45)), 
			GUI.textField.new(window, Rect(step*4, step*2, step*1.5, step*0.5)),
			GUI.staticText.new(window, Rect(step*3.5, step*2.15, step, step)).string_("aMid")
			] ;
		guiDict[\aLo] = [JKnob.new(window, Rect(step*3.5, step*3, step*0.45,step*0.45)), 
			GUI.textField.new(window, Rect(step*4, step*3, step*1.5, step*0.5)),
			GUI.staticText.new(window, Rect(step*3.5, step*3.15, step, step)).string_("aLo")
			] ;

////////////////
		guiDict[\bHi] = [JKnob.new(window, Rect(step*6.5, step, step*0.45,step*0.45)), 
			GUI.textField.new(window, Rect(step*7, step, step*1.5, step*0.5)),
			GUI.staticText.new(window, Rect(step*6.5, step*1.15, step, step)).string_("bHi")
			] ;
		
		guiDict[\bMid] = [JKnob.new(window, Rect(step*6.5, step*2, step*0.45,step*0.45)), 
			GUI.textField.new(window, Rect(step*7, step*2, step*1.5, step*0.5)),
			GUI.staticText.new(window, Rect(step*6.5, step*2.15, step, step)).string_("bMid")
			] ;
		guiDict[\bLo] = [JKnob.new(window, Rect(step*6.5, step*3, step*0.45,step*0.45)), 
			GUI.textField.new(window, Rect(step*7, step*3, step*1.5, step*0.5)),
			GUI.staticText.new(window, Rect(step*6.5, step*3.15, step, step)).string_("bLo")
			] ;
		//
		guiDict[\b1] = [JKnob.new(window, Rect(step*9, step, step*0.45,step*0.45)), 
			GUI.textField.new(window, Rect(step*9.5, step, step*1.5, step*0.5)),
			GUI.staticText.new(window, Rect(step*9, step*1.15, step, step)).string_("b1")
			] ;
		
		guiDict[\b2] = [JKnob.new(window, Rect(step*9, step*2, step*0.45,step*0.45)), 
			GUI.textField.new(window, Rect(step*9.5, step*2, step*1.5, step*0.5)),
			GUI.staticText.new(window, Rect(step*9, step*2.15, step, step)).string_("b2")
			] ;
		guiDict[\b3] = [JKnob.new(window, Rect(step*9, step*3, step*0.45,step*0.45)), 
			GUI.textField.new(window, Rect(step*9.5, step*3, step*1.5, step*0.5)),
			GUI.staticText.new(window, Rect(step*9, step*3.15, step, step)).string_("b3")
			] ;


////////////////
		guiDict[\aHead] = [GUI.button.new(window, Rect(step*2, step*4, step*1.25,step*0.5))
					.states_([
				["bang   ", Color.white, Color.red],
				]),			
			GUI.textField.new(window, Rect(step*3.5, step*4, step*1.5, step*0.5)),
			GUI.staticText.new(window, Rect(step*2, step*4.15, step, step)).string_("aHead")
			] ;
		guiDict[\bHead] = [GUI.button.new(window, Rect(step*7, step*4, step*1.25,step*0.5))
			.states_([
				["bang   ", Color.white, Color.red],
				]), 
			GUI.textField.new(window, Rect(step*8.5, step*4, step*1.5, step*0.5)),
			GUI.staticText.new(window, Rect(step*7, step*4.15, step, step)).string_("bHead")
			] ;

		guiDict.keys.do({ |xElement|
			item = guiDict[xElement] ;
			item[1].action_({ arg field ;
				arr = (field.string+xElement.asString).split($ ).reject({|i| i=="" }) ;
				xSession.parse(arr) ;
			})
		}) ;
		window.front ;
	}
	
	update {
		var selAndArg ;
		xSession.mapDict.keys.do({ arg xElement ;
			selAndArg = xSession.mapDict[xElement][0]++" "++xSession.mapDict[xElement][1] ;
			guiDict[xElement][1].string_(selAndArg)
		})
	}

}
