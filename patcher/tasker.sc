/* 
you pass a patcher and a func with globals, it creates a gui by linking to a patcher 
*/

Tasker {
	
	var <>patcher ;
	var <>taskDict ;
	var <>numTask ;
	 
	*new { arg patcher ;
		^super.new.initTasker(patcher)
	}

	initTasker { arg aPatcher ;
		patcher = aPatcher ;
		taskDict = IdentityDictionary.new ;
		numTask = 0 ;
	}

	addTask { arg name, func, arr ;
		var names, values ;
		var env = Environment.make({}) ;
		arr = arr.clump(2).flop ;
		names = arr[0] ;
		values = arr[1] ;
		names.do({ arg item, index ;
			env.put(item, values[index])
			}) ;
		taskDict[name.asSymbol] = [Task(func.inEnvir(env)), env, numTask] ;
		numTask = numTask + 1 ;
	}
	
// task control
// it just wraps up Task control methods

	start { arg num ;
		var task = taskDict[num][0] ;
		var env = taskDict[num][1] ;
		env.use({
			task.start(SystemClock)	
		}) ;
	}


	play { arg num ;
		var task = taskDict[num][0] ;
		var env = taskDict[num][1] ;
		env.use({
			task.play(SystemClock)	
		}) ;
	}

	pause { arg num ;
		var task = taskDict[num][0] ;
		var env = taskDict[num][1] ;
		env.use({
			task.pause(SystemClock)	
		}) ;
	}

	stop { arg num ;
		var task = taskDict[num][0] ;
		var env = taskDict[num][1] ;
		env.use({
			task.stop(SystemClock)	
		}) ;
	}
	
	resume { arg num ;
		var task = taskDict[num][0] ;
		var env = taskDict[num][1] ;
		env.use({
			task.resume(SystemClock)	
		}) ;
	}

	reset { arg num ;
		var task = taskDict[num][0] ;
		var env = taskDict[num][1] ;
		env.use({
			task.reset(SystemClock)	
		}) ;
	}
	
}



TaskerGui {
	
	var <>tasker ;
	var window, flow ;

	
	*new { arg aTasker ;
			^super.new.initTaskerGui(aTasker) 
		}

	initTaskerGui { arg aTasker ;
		tasker = aTasker ;
		window = GUI.window.new("Tasker Control Panel", Rect(30,30, 500,1000), scroll:true) ;
		flow = FlowLayout.new( window.view.bounds );
	    	window.view.decorator = flow;
	     window.view.background_( Color.hsv(0.2, 0.8, 0.8)) ;
	}
	


	makeAllGui { arg vStep = 15 ;
		var indDict = IdentityDictionary.new ;
		tasker.taskDict.keys.do ({ |taskName| 
			indDict[tasker.taskDict[taskName][2]] = taskName ;
		}) ;
		
		indDict.keys.asArray.sort.do({ |ind| this.makeGui(indDict[ind], vStep)
		})
	}


	makeGui { arg taskName, vStep = 10 ;
			
		var controlArr = [], guiArr = [] ;
		var st ; // ?
		var task = tasker.taskDict[taskName][0] ;
		var env = tasker.taskDict[taskName][1] ; 
		env.keys.do({ arg item ;
				var name = item.asString.postln ;
				controlArr = controlArr.add([name, 0.0, 1.0, env[item]])
				}) ;
		
		flow.nextLine ;


		GUI.staticText.new( window, Rect( 0, 0, 130, vStep ))
						.string_( "min  " ).align_( \right)
						.font_(GUI.font.new("Helvetica", vStep*0.75)) ;
		GUI.staticText.new( window, Rect( 80, 0, 130, vStep ))
						.string_( "     max" ).align_( \left)
						.font_(GUI.font.new("Helvetica", vStep*0.75)) ;

		GUI.staticText.new( window, Rect( 140, 0, 100, vStep ))
							.string_(taskName+" " ).align_( \right)
							.font_(GUI.font.new("Futura", vStep))
							 ;		
		
		GUI.button.new(window, Rect( 160, 0, vStep*3, vStep ))
			.states_([
				["start", Color.hsv(0.1,1,0.85), Color.white],
				["stop", Color.white, Color.red]
			]) 
			.action_({ arg butt ;
				if (butt.value == 0) {task.pause} {task.start}
			} )
			
		 ;
			
		flow.nextLine ;						
		// GUI creation
		controlArr.do({ arg item, ind ;
				var index = ind+1 ;
				var guiElement = [
				GUI.staticText.new( window, Rect( 40, 30*index, 80, vStep ))
							.string_( item[0] ).align_( \right)							.font_(GUI.font.new("Helvetica", vStep*0.75)),
				GUI.numberBox.new( window, Rect( 160, 30*index, 50, vStep ))
						.value_(item[1])							.font_(GUI.font.new("Helvetica", vStep*0.75)) ,
				GUI.numberBox.new( window, Rect( 240, 30*index, 50, vStep ))
						.font_(GUI.font.new("Helvetica", vStep*0.75)) 
						.value_(item[2]),				
				GUI.slider.new( window, Rect( 310, 30*index, 200, vStep )),
				GUI.textField.new( window, Rect( 520, 30*index, 90, vStep ))
						.string_( "0.0" )
						.font_(GUI.font.new("Helvetica", vStep*0.75))
						.value_(item[3]) 
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
			guiElement[1].action  = { arg minBox ;
				var val ;
				item[1] = minBox.value ;
				val = (paramValue-item[1].value)/(item[2].value-item[1].value) ;
				guiElement[3].valueAction_(val) ;
			} ;
			guiElement[2].action  = { arg maxBox ;
				var val ;
				item[2] = maxBox.value ;
				val = (paramValue-item[1].value)/(item[2].value-item[1].value) ;
				guiElement[3].valueAction_(val) ;	
			} ;
			guiElement[3].action = { arg slider ; 
				var name = item[0] ;
				var offset = item[1].value ;
				range = item[2].value - item[1].value ;
				paramValue = slider.value*range + offset ;
				env[name.asSymbol] = paramValue ; 
				guiElement[4].string_(paramValue.trunc(0.0001).asString ) ; 
				} ;
			guiElement[4].action = { arg txt ;
				var name = item[0] ;
				var val = txt.value ;
				if (val[0].isDecDigit)
				{	
					val = val.asFloat ;
					case { val > item[2] } 
						{ paramValue = val ;
						guiElement[2].valueAction_(val) } 
					{ val < item[1] } 
						{ paramValue = val ; 
						guiElement[1].valueAction_(val) }
					{ val.inclusivelyBetween(item[1], item[2]) }
						{ env[name.asSymbol] = val ;
					guiElement[3].valueAction_(val/range) }
					}
				// else: it's the name of a synth
					{
					val = val.asSymbol ;	
// was for Patcher
//					env[name.asSymbol] = tasker.patcher.defDict[val][1] ;
					env[name.asSymbol] = val;//tasker.patcher.synthDict[val][0];
					}
				}
		}) ;	
		window.onClose_({ task.stop }) ;
		window.front ;
	}
} 

/*

f = {
	inf.do{
		"burp".postln ;
		~k.postln.wait ;
	}
} ;


g = {
	inf.do{
		"QQQQQ".postln ;
		~t.wait ;
	}
} ;


t = Tasker(nil) ;
t.addTask("dingDong", f, [\k, 1]) ;
t.addTask("muio", g, [\t, 2]) ;

t.taskDict ;
t.start(0) ;
t.start(1) ;

t.pause(0) ;
t.play(0) ;

// control
t.taskDict[0][1][\k] = 3 ;

h = TaskerGui(t)
h.makeGui(\dingDong)
h.makeGui(\muio)
*/


/*
s.reboot ;
p = BabaWrapper(Pulse).send(s) ;


b = BabaPatcher.new ;
b.add(p, \pulse) ;
b.setList(\pulse, [\freq, 50, \width, 0.5, \mul, 0.2]) ;

b.gui(1, 200) ;

t = Tasker(b) ;
t.addTask("setter", 
 	{
		inf.do({
			~synth.set(\freq, rrand(50, 80)) ;
		~time.wait
		
		})
	},
	[\synth,\pulse, \time, 0.2]
) ;

TaskerGui(t).makeAllGui

*/

