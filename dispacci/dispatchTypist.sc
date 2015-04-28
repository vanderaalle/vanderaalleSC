// A class for managing dispatch ps files

DispatchTypist {
	
	var <>path ; // the path we are working with, should contain template.ps
	var <>counter ;
	var <>current, <>templateFile, <>template ;
	// arrays for dynamics and moods, so that they can be accessed by index
	var <>dyns, <>tempos, <>rightActions, <>leftActions, <>harmonics ; 
	var <>state ; // stores current state 
		
	*new { arg path, templatePath ; 
		^super.new.initDispatchTypist(path, templatePath) 	
	}

	initDispatchTypist { arg aPath, aTemplatePath ;
		var templatetFile, templatePath ;
		path = aPath ; 
		counter = 0 ;
		// open template assuming is in the same working dir
		templatePath = if (aTemplatePath.isNil) { path} { aTemplatePath }+/+"template.ps"  ;
		templateFile = File(templatePath, "r") ; 
		// read the conten but delete the showpage
		template = templateFile.readAllString.replace("showpage", "") ;  
		templateFile.close ; // close
		// setup data arrays
		dyns = "ppp pp p mp mf f ff fff".split($ ) ;
		tempos = "Grave Lento Adagio Andante Allegretto Allegro Vivace Presto"
			.split($ ) ;
		rightActions = "downBow waving tremolo before brushing pizz".split($ ) ;
		leftActions = "open diamond mute leftPizz".split($ ) ;
		harmonics = "5 4 3M 3m".split($ ) ; 
		state = Dictionary.new ;
	}

	newDispatch {
		"NEW DISPATCH ON THE RUN!".postln ;
		// if current file not nil and is open, close it
		if (current.notNil) {if (current.isOpen) { this.close }} ;
		counter = counter + 1 ; // so we start from 1 
		state[\counter] = counter ;
		state[\left] = [] ; state[\right] = [] ;
		//create new file with increasing index, new date
		current = File(path+/+"dispatchNo"++counter++".ps", "w") ;
		// by filling it initially with template code
		current.write(template) ; 
		// write counter
		current.write("aval46 	numberPlace 	("++counter++") show\n") ;
		// write date
		current.write("courier11 14.25 cm 0.5 cm moveto ("+Date.getDate+") show\n") ;
	}
	
	writeDyn { arg dynID = 0 ; 
		current.write("("++dyns[dynID]++") dyn\n") ;  
		state[\dyn] = dynID ; 
	}

	writeTempo { arg tempoID = 0 ; 
		current.write("("++tempos[tempoID]++") tempo\n") ;  
		state[\tempo] = tempoID ;
	}


	writeBridge { arg leftFret = 5, rightFret = 10 ; // numbers indicate frets
		current.write(""++ leftFret + rightFret + "bridge\n") ; // numbers indicate frets
		state[\bridge] = [leftFret, rightFret] ;	
	}

	writeRight { arg whichString, actionID ;
		current.write(""++ whichString + rightActions[actionID] ++"\n") ;
		state[\right] = state[\right].add([whichString, actionID]) 
	}
	
	writeLeft { arg whichString, actionID, harmArr ; 
		if (actionID == 1){ this.writeDiamond(whichString, actionID, harmArr) }
			{
		current.write(""++ whichString + leftActions[actionID] ++"\n") ;
			} ;
		state[\left] = state[\left].add([whichString, actionID, harmArr].select{|i| i.notNil}) ;
	}
	
	
	writeDiamond { arg whichString, actionID, harmArr ;
		var more ;
		if (harmArr.notNil && (harmArr.size > 0))				{
			more = " ("++harmArr.collect{|i| harmonics[i]}.asString.replace("[", "").replace("]", "").replace(",", "")++") " ;
		current.write(""++ whichString ++more++ leftActions[actionID] ++"\n") ;
		}
	}
	
	close {
		var content ;
		// close gracely the file 
		current.write("showpage\n") ; // so we see stuff
		current.close ;
		("cp"+path+/+"dispatchNo"++counter++".ps"+ path+/+"dispatchUnique.ps").unixCmd ;
	}

	closeSession {
		var sess, tmp ; 
		// close current file ;
		this.close ;
		// create a session file 
		sess = File(path+/+"dispatchBook_"++Date.getDate.stamp++".ps", "w");
		sess.write("
% PROLOG
% some defs
/ppi 72 def
/cm  {0.393701 mul ppi mul} def
% this is A4, we have to force it
<< /PageSize [21 cm 29.7 cm] >> setpagedevice

%%%%% fonts
/futura { /Futura-CondensedMedium findfont } def
/aval { /Aval findfont } def
/courier { /Courier findfont } def
/didot { /Didot-Italic findfont } def

% num indicate dimension in pts
/f12 {futura 12 scalefont setfont} def
/f15 {futura 15 scalefont setfont} def
/f20 {futura 20 scalefont setfont} def
/f26 {futura 26 scalefont setfont} def
/f30 {futura 30 scalefont setfont} def
/aval20 {aval 20 scalefont setfont} def
/courier16 {courier 16 scalefont setfont} def


f15 3 cm 23 cm moveto (Andrea Valle) show	
f30 3 cm 22 cm moveto (Dispacci dal fronte interno) show



	
f26 3 cm 10 cm moveto (Data archiviazione:) show	\n") ;
		sess.write("courier16 9.5 cm 10.05 cm moveto ("++Date.getDate++") show\n showpage") ;
		// import all the files already generated
		counter.do{|i|
			tmp = File(path+/+"dispatchNo"++(i+1)++".ps", "r") ;
			sess.write(tmp.readAllString) ;
			tmp.close ;		
		} ;
		// creating a multipage doc 
		sess.close ;
	}

	print { arg device ;
		// send current to printer
		("lpr" + (path+/+"dispatchNo"++counter++".ps").postln + "-P" + device).unixCmd ;
	}	
	
		
}




/*
(
d = DispatchTypist("/musica/dispacci/ps/testFolder") ;
d.newDispatch ;

d.writeMood(1,1) ;
d.writeBridge(4,9) ;
d.writeRight(1,3) ;
d.writeLeft(2, 1, [1,2,4]) ;


d.newDispatch ;

d.writeMood(2,3) ;
d.writeBridge(2,7) ;
d.writeRight(0,4) ;
d.writeLeft(2, 1, [1,2,4]) ;
d.writeLeft(1, 1, [3,4]) ;
d.writeLeft(3, 2) ;

d.closeSession ;
)
*/


