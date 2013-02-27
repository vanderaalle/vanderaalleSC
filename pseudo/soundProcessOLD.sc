///*
//Andrea Valle
//for Marcelál“ Antœnez Roca's *Pseudo* (2012) - 12/06/12
//
//This is the main class for managing sound processes.
//A sound process is a dsp/algorithmic composition process
//written in SC code and stored as a scd file in the soundProcesses folder.
//A process is written as a prototype: see template for info 
//
//SoundProcess provides:
//	- source code loading and managing
//	- automatic gui generation
//	- automatic documentation from source code
//	- OSC connection
//	- automatic Server booting	
//	- mixing
//*/
//
//
//SoundProcess {
//
//	var <>path ;			// the project path
//	var <>processDict ; 	// this is the dict that stores all the processes
//	var <>windowsDict ; 		// here we store gui stuff, not so clean
//	var <>oscFunc ;		// the connection func
//	var <>hostname, <>port, <>netAddr ;		// where you want to *send*
//	
//	var <>mixer, <>busArr ;
//			
//	*new { arg path, hostname, port  ;  // sending stuff
//		^super.new.initSoundProcessLoader(path, hostname, port) 	
//	}
//
//
//	// 1. INIT
//	initSoundProcessLoader { arg aPath, aHostname, aPort   ;
//		path = aPath ;
//		hostname = aHostname; port = aPort ;
//		netAddr = NetAddr(hostname, port) ;  
//		windowsDict = Dictionary.new ;
//	}
//
//	connect {
//		oscFunc = { |msg, time, replyAddr, recvPort|
//		var block, first, processName, param, val ;
//Ê Ê Ê	//msg.postln ;
//		first = msg[0].asString ; 
//		if(first[..3].asSymbol == '/sp_') {
//			block = first.split($_) ;
//			processName = block[1].asSymbol ;
//			param = block[2].asSymbol ;
//			val = msg[1].asFloat ;
//			this.set(processName, param, val) ;
//	}
//		} ;
//	thisProcess.addOSCRecvFunc(oscFunc);
//	}
//
//	setupMixer { arg busNum = 16, chans = 4 ;
//		busNum.do{
//			busArr = busArr.add(Bus.audio(Server.local, chans));
//		} ;
//		mixer = BusMixer(busArr, chans, out:2) 
//		// we use busArr on 2-->6
//		// and reserve 0-1 to stereo head. You have to address explicitly the channels
//		// in that case
//	}
//
//	// create the dict by reading the content of soundProcesses folder
//	// and associating the ID retrieved from each file with its contained event 
//	createDict {
//		var folder = path++"/soundProcesses/" ;
//		var pipe, line, name ;
//		// clean up for easy reload 
//		processDict = Dictionary.new ;
//		pipe = Pipe.new("ls -r "++folder, "r") ; // list directory contents
//		line = pipe.getLine ; Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê// get the first line
//		while({line.notNil}, {
//			name = line.asSymbol ; 
//			if ([\doc].includes(name).not) {
//				processDict.put(name, (folder++line++"/"++line++".scd").load) ;
//				processDict[name].netAddr = netAddr ;
//				processDict[name].busArr = busArr ;
//				processDict[name].soundProcess = this ; // so we can control it from inside 
//				} ;
//				line = pipe.getLine; 
//				}); // post until l = nil
//		pipe.close; Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê // close the pipe to avoid that nasty buildup
//		// remove doc & sound
////		processDict.removeAt(\doc) ;
////		processDict.removeAt(\sound) ;
//		} 
//			
//	refresh { this.createDict } // easier to remember, when you have to reload stuff you call this
//	
//	// easy startup
//	start {
//		Server.local.waitForBoot {
//			Server.local.sync ;
//			this.setupMixer ;
//			Server.local.sync ;
//			this.createDict ;
//			this.connect 
//		}
//	}
//	
//	// 2. CONTROL interface
//		
//	begin { arg processName ;
//		var log, logPath ;
//		if ( processDict[processName].notNil) 
//			{
//		{windowsDict.put(processName, SoundProcessGui(processDict[processName]).makeGui.window)
//			}.defer ;
//		logPath = path++"/soundProcesses/"++processName++"/"++processName++".log";
//		mixer.path_(logPath) ;
//		if (File.exists(mixer.path)) { mixer.read } ;
//		processDict[processName].begin ;
//			}
//			{("WARNING: the soundprocess" + processName + "does not exist").postln }
//	}
//
//	end { arg processName ;
//		if (windowsDict[processName].notNil) 
//		{ // window could be already closed
//			{
//		windowsDict[processName].close ;
//		windowsDict.removeAt(processName);
//		processDict[processName].end ;
//			}.defer
//		}
//	}
//
//	setParam { arg processName, command, value ;
//		var cmd = ("set_"++command.asString).asSymbol ;
//		processDict[processName].perform(cmd, value) ;
//	}
//
//	// the general method	
//	set { arg processName, command, value ;
//		case 
//			{ [\begin, \end].includes(command).not } 
//				{ this.setParam(processName, command, value) }
//			{command == \begin } 
//				{ this.begin(processName) }
//			{command == \end } 
//				{ this.end(processName) }
//	}	
//	
//	
//	// 3. DOCUMENTATION
//	
//	// Document generator
//	createDoc {
//		var folder = path++"/soundProcesses/" ;
//		var pipe, line, name ;
//		// clean up for easy reload 
//		var docDict = Dictionary.new ;
//		pipe = Pipe.new("ls -r "++folder, "r") ; // list directory contents
//		line = pipe.getLine ; Ê Ê Ê Ê Ê Ê Ê Ê Ê Ê// get the first line
//		while({line.notNil}, {
//			//line.postln;
//			name = line.split($.)[0].asSymbol ;
//			if ([\doc].includes(name).not) {
//			docDict.put(name, (folder++line++"/"++line++".scd").load) };
//			line = pipe.getLine; 
//			}); // post until l = nil
//		pipe.close; Ê// close the pipe to avoid that nasty buildup
//		// remove doc & sound
//		docDict.removeAt(\doc) ;
//		this.writeDoc(docDict) ;
//	} 
//	 
//	writeDoc { arg docDict ;
//		var file = File(path++"/soundProcesses/doc/soundProcessesDoc.tex", "w") ;
//		var block, head, temp ;
//		var name, desc, params ;
//		temp = File(path++"/soundProcesses/doc/pseudoDocTemplate.tex", "r") ;
//		head = temp.readAllString ;
//		file.write(head+"\n\\type{"++Date.getDate++"}\n\\blank[3cm]\n\n\n\n") ;
//		temp.close ;
//		file.write("");
//		docDict.keys.do{|k| 
//			// here we have to work
//			name = docDict[k][\nfo][\name] ;
//			desc = docDict[k][\nfo][\desc] ;
//			file.write("\\startpacked\n{\\bf "++name++" }\\crlf\n") ;
//			file.write(desc+"\n") ;
//			params = docDict[k][\nfo].keys.select{ |k|  [\name, \desc].includes(k).not} ;
//			file.write("\\startitemize\n") ;
//			params.do { |p|
//				file.write("\\item\n{\\em "+p++"}: ") ; 
//				file.write(docDict[k][\nfo][p]+"\n") ;
//				docDict[k][\nfo][p] ;
//			};
//			file.write("\\stopitemize\n\\stoppacked\\blank\n") ;	
//	
//		} ;
//		file.write("\\stopcolumns\n\\stoptext") ;
//		file.close ;
//	} 
//
//}
//
///*
//
//// USAGE
//l = SoundProcess("/musica/pseudo").start ;
//
//l.createDoc ; // if you need the ConTeXt --> pdf file for documentation
//
//
//// OSC
//n = NetAddr("127.0.0.1", 57120) ;
//n.sendMsg('/sp_sinusoidalTheremin_begin', 0)
//
//n.sendMsg("/sp_sinusoidalTheremin_pitch", 0.6)
//n.sendMsg("/sp_sinusoidalTheremin_end", 0)
//
//// normal usage, set method as a general interface
//l.set(\sinusoidalTheremin, \begin) ;
//l.set(\sinusoidalTheremin, \end) ;
//l.set(\sinusoidalTheremin, \pitch, 0.9) ;
//
//
//// these are internals
//l.begin(\sinusoidalTheremin) ;
//l.end(\sinusoidalTheremin) ;
//
//
//// direct acces to dict,  you won't use it
//l.processDict[\template].testSynth ; // works 
//l.processDict[\sinusoidalTheremin].begin ; // works 
//l.processDict[\sinusoidalTheremin].end ; // works 
//
//*/
//
//
//// GUI support
//SoundProcessGui {
//	
//	var <>process ;	// the process to be controlled
//	var <>controls ;
//	var <>step ;
//	// gui stuff
//	var <>window ;
//	var <>slArr, <>nbArr ;
//	var <>task, <>rate ;
//			
//	*new { arg process, step = 20, rate = 5 ; 
//		^super.new.initSoundProcessGui(process, step,rate) 	
//	}
//
//	initSoundProcessGui { arg aProcess, aStep, aRate ;
//		process = aProcess ;
//		controls = process[\nfo].keys.select{|k| [\desc, \name].includes(k).not } ;
//		step = aStep ;
//		rate = aRate ;
//		// the monitoring task
//		task = Task({inf.do{
//			controls.do{|i,j|
//				slArr[j].value_(process[i]) ;
//				nbArr[j].value_(process[i]) ;
//				(1/rate/controls.size).wait	;
//			} ;
//		}})
//	}
//	
//	// PRIVATE 
//	createLabelView { arg name, desc ;
//		var descSize = 10 ;
//		var lineSpace = ((desc.size/20).asInteger+1)*(descSize*1.6) ;
//		StaticText(Window(name, Rect(10, 40, 300, lineSpace+10)).front, Rect(10, 10, 270, lineSpace-10))
//		.string_(desc)
//		.font_(Font("Futura", 10))	
//	}
//	
//	makeGui {
//		var name = process[\nfo][\name].asString ;
//		var desc = process[\nfo][\desc].asString ;
//		var descSize = 10 ;
//		var lineSpace = desc.size/60.asInteger*(descSize*1.6) ;
//		window = Window(name,Rect(300, 100, 500, step*(controls.size+2)+50+lineSpace)).front ;
//		window.onClose_{ task.pause } ;
//		StaticText(window, Rect(10, 10, 100, 20)).string_(name++":").font_(Font("Futura", 14)) ;
//		Button(window, Rect(10, 20+lineSpace, 60, 24))
//			.states_([["STOP!", Color.white, Color.red],["done", Color.black, Color.grey]])
//			.action_{|me| if ( me.value == 1 ) { process.end ; window.close  }  } ;
//		StaticText(window, Rect(150, 10, 300, lineSpace))
//			.string_(desc).font_(Font("Futura", 10)) ;
//		StaticText(window, Rect(450, 10+lineSpace, 40, 20)).string_("Monitor").font_(Font("Futura", 10)) ;
//		Button(window, Rect(450, 30+lineSpace, 40, 30))
//			.states_([["on", Color.white, Color.red],["off", Color.black, Color.grey]])
//			.action_{|me| if ( me.value == 0 ) { task.play } { task.pause } } ;
//		StaticText(window, Rect(450, 60+lineSpace, 50, 20)).string_("times/sec").font_(Font("Futura", 10)) ;
//		NumberBox(window, Rect(450, 80+lineSpace, 40, 20))
//			.value_(5)
//			.action_{|me| rate = me.value.clip(1, 20)} ;
//		controls.do{|i, j|
//			StaticText(window, Rect(10, j*step+50+lineSpace, 100, step)).string_(i.asString)
//				.font_(Font("Futura", 10))
//				.mouseDownAction_{this.createLabelView(i, process[\nfo][i])} ;
//			slArr = slArr.add(Slider(window, Rect(80, j*step+50+lineSpace, 300,  step,))) ;
//			nbArr = nbArr.add(NumberBox(window, Rect(390, j*step+50+lineSpace, 50, step))) ;
//			nbArr[j].action_{|me| slArr[j].value_(me.value); 
//				process.perform(("set_"++i).asSymbol, me.value)} ;
//			slArr[j].action_{|me| nbArr[j].value_(me.value);
//				process.perform(("set_"++i).asSymbol, me.value)} ;
//		};	
//		task.play(AppClock) ;
//	}
//
//	
//
//
//}
//
//
///*
//
//(
//
//
//g = SoundProcessGui(~processDict[\MaraMara1]).makeGui
//)
//~processDict[\template].param2 = 0.786
//g.makeGui
//g.controls
//g.rate
//*/