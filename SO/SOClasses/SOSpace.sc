
SOSpace : SimpleController {
	
	var <>objectDict, <>guiDict ;
	var <>dotList ;
	var <>step, <>dotStep ;
	var <>container, <>title, <>background, <>upSide, <>frontSide, <>lateralSide ; 
	var <>textBox ;
	var <>actions ;
	var <>guiDict ;
	var <nameOn ;
	
	*new { arg so, title, nameOn = true ;
		^super.newCopyArgs(so).init.initViewController(title, nameOn)
	}

	initViewController { arg aTitle, aNameOn ;
		title = if (aTitle.isNil, { model.soundPath.split($/).reverse[0].split($.)[0] }, 
			{ aTitle }) ;
		// set some variables
		nameOn = aNameOn ;
		step = 100 ;
		dotStep = 6 ;
		objectDict = model.objectDict ;
		// actions dict
		actions = IdentityDictionary[
		// on changePosition
			\position		-> { arg model, what, extObjectDef ;
				var x, y, z ;
				var position ;
				var name, attack, duration, profile, caliber, variation, 
					 site, dynamics, group, text, rank ;
				var dim = if (nameOn, {[100,20]}, {[0,0]}) ; // dirty trick 
				#name, attack, duration, profile, caliber, variation, 
					site, dynamics, group, text, rank = extObjectDef ;
				position = [	profile,
							caliber, 
							variation
						] ; // legacy
				#x,y,z = position ; // legacy
				guiDict[rank][11].bounds_(
					Rect(
						(x+2.5+(1/3))*step-(dotStep/2), 
						((3-z)+(1/3))*step-(dotStep/2),
						dotStep, dotStep)
						)
						.background_(this.groupColor(group)) ;
				guiDict[rank][12].bounds_(
					Rect(
						(x+2.5+(1/3))*step-(dotStep/2), 
						(y+(2/3)+3)*step-(dotStep/2),
						dotStep, dotStep) )
						.background_(this.groupColor(group)) ;
				guiDict[rank][13].bounds_(
					Rect(
						(z+5+(2/3))*step-(dotStep/2),
						(y+(2/3)+3)*step-(dotStep/2),
						dotStep, dotStep )
						)
						.background_(this.groupColor(group)) ;
				guiDict[rank][14].bounds_(
					Rect(
						(x+2.5+(1/3))*step-(dotStep/2), 
						((3-z)+(1/3))*step-(dotStep/2),
						dim[0], dim[1])
						) ;
				guiDict[rank][15].bounds_(
					Rect(
						(x+2.5+(1/3))*step-(dotStep/2), 
						(y+(2/3)+3)*step-(dotStep/2),
						dim[0], dim[1]) ) ;
				guiDict[rank][16].bounds_(
					Rect(
						(z+5+(2/3))*step-(dotStep/2),
						(y+(2/3)+3)*step-(dotStep/2),
						dim[0], dim[1] )
						) ;
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
				container.refresh
				 }, 
			\addRemove -> { this.redraw }
				] ;

			container = GUI.window.new(title, 
				Rect(50,50, 9*step, 6*step)) ; 
			// displaying some text
			background = GUI.userView.new(container, 
				Rect(0, 0, 9*step, 6*step))
				.background_(Color.white) ;			
			frontSide = GUI.userView.new(container, 
				Rect(step/3, step/3*2 + (3*step), 5*step, 2*step))
				.background_(Color.white) ;
			upSide = GUI.userView.new(container, 
				Rect(step/3, step/3, 5*step, 3*step))
				.background_(Color.white)  ;
			lateralSide = GUI.userView.new(container, 
				Rect(step/3*2+(5*step), step/3*2 + (3*step), 3*step, 2*step))
				.background_(Color.white)  ;
		//	container.onClose_(model.removeDependant(this)) ; // why does it introduce a bug?
		this.createClassSpace ;
		this.createEntryBoxes ;
		guiDict = IdentityDictionary.new ;
		this.createGuiDictionary ;
		container.front 
	}


	createEntryBoxes {
		var texts = ["Name",  "Attack", "Duration", "Profile", "Caliber", "Variation", 
			"Site", "Dynamics", "Group", "Text", "Rank"] ;
		var name,  profile, caliber, variation, 
				attack, duration, site, dynamics, group, text ;
		Array.fill(10, { arg ind ;
		GUI.staticText.new(container, 
			Rect(step/3*2+(5*step),  step/3+(ind*20), 3*step, 20))
			.stringColor_(Color.new255(0, 47, 167))
			.string_(texts[ind] ;
			)
		}) ;
		// entry fields
		textBox = Array.fill(8, { arg i ;
			var ind = i+1 ;
			GUI.textField.new(container,									Rect(step/3*2+(5*step)+step,  step/3+(ind*20), 3*step+step/2, 20))
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
						model.changeAll(name, attack, duration, profile, caliber, variation, 
							site, dynamics, group, text)
						}) ;
				}) ;
		// display name
		textBox = textBox.insert(0,
			GUI.staticText.new(container,									Rect(step/3*2+(5*step)+step, step/3, (3*step+step/2)/2, 20))
				) ;
			// for text
			textBox = textBox.add(
					GUI.textView.new(container,									Rect(step/3*2+(5*step)+step,  step/3+(9*20), 3*step+step/2, 20+50))
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
						model.changeAll(name,  attack, duration, profile, caliber, variation, 
							site, dynamics, group, text)
							}
							)
					})
			) ;
		// rename
		textBox = textBox.add(
			GUI.textField.new(container,									Rect(step/3*2+(5*step)+step+((3*step+step/2)/2), step/3, 
							(3*step+step/2)/2, 20))
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
						model.rename(name, view.string) ;
						textBox[0].string_(view.string) ;
						view.string_("")
						}) ;			
			) ;

	}	


	groupColor { arg group ;
			var colorStep = 1.0/(model.groupNumber) ;
			var color = colorStep*model.groupArray.indexOf(group) ;
			var col = Color.hsv(color, 1, 0.7, 0.9) ;
			^col
	}


	createGuiElement { arg key, attack, duration, profile, caliber, variation, 
					site, dynamics, group, text, rank ;
			var position, x, y, z, xs, ys, zs, col ;				var dim = if (nameOn, {[100,20]}, {[0,0]}) ; // dirty trick 
			// protection
			profile = profile ? rrand(-2.5, 2.5) ;
			caliber = caliber ? 2.0.rand ;
			variation = variation ? 3.0.rand ;
			//
			position = [profile, caliber, variation] ;  // legacy: note the two following lines
			#x, y, z = position ;
			#xs, ys, zs  = position ;
			col = this.groupColor(group) ;
			guiDict.add(rank -> [key, attack, duration,
		 			profile, caliber, variation, 
					site, dynamics, group, text, rank,
				GUI.userView.new(container, 
					Rect( 
						(x+2.5+(1/3))*step-(dotStep/2), 
						(6-(z+(2/3)+2))*step-(dotStep/2),
						 dotStep, dotStep))
					.mouseDownAction_({ arg view, xv, yv ; 
						var xo = ((xv-(step/3))/step-2.5).clip(-2.5, 2.5) ;
						var zo = 3-(((yv-(step/3)-(dotStep/2))/step).clip(0,3)) ;
						var yo = ys ;
						#xs, ys, zs = [xo, yo, zo] ;
						model.changePosition(key, 
							xo.round(0.01), yo.round(0.01), zo.round(0.01)) ;
						model.play(attack, attack + duration) ;
						}) 
					.mouseMoveAction_({ arg view, xv, yv ;
						var xo = ((xv-(step/3))/step-2.5).clip(-2.5, 2.5) ;
						var zo = 3-(((yv-(step/3))/step).clip(0,3)) ;
						var yo = ys ;
						#xs, ys, zs = [xo, yo, zo] ;
						model.changePosition(key, 
							xo.round(0.01), yo.round(0.01), zo.round(0.01))
			 		})
					.background_(col),
				GUI.userView.new(container, 
					Rect(
						(x+2.5+(1/3))*step-(dotStep/2), 
						(y+(3)+(2/3))*step-(dotStep/2), 
						//(6-((2-y)+(2/3)))*step-(dotStep/2), 
						dotStep, dotStep))
					.mouseDownAction_({ arg view, xv, yv ; 
						var xo = ((xv-(step/3))/step -2.5).clip(-2.5, 2.5) ;
						var yo =  (
								(yv-(
								(3+(2/3))*step)
								)
							/step
							).clip(0,2) ; 
						//(2-((yv-(3+(2/3)*step))/step)).clip(0,2) ;
						var zo = zs ;
						#xs, ys, zs = [xo, yo, zo] ;
						model.changePosition(key, 
							xo.round(0.01), yo.round(0.01), zo.round(0.01)) ;
						model.play(attack, attack + duration) ;
						}) 
					.mouseMoveAction_({ arg view, xv, yv ;
						var xo = ((xv-(step/3))/step -2.5).clip(-2.5, 2.5) ;
						var yo =  (yv-((3+(2/3))*step)/step).clip(0,2) ; 
						var zo = zs ;
						#xs, ys, zs = [xo, yo, zo] ;
						model.changePosition(key, 
							xo.round(0.01), yo.round(0.01), zo.round(0.01))
			 		})
					.background_(col),
				GUI.userView.new(container, 
					Rect(
						(z+5+(2/3))*step-(dotStep/2),
						(y+(3)+(2/3))*step-(dotStep/2), 
						 dotStep, dotStep))
				 	.mouseDownAction_({	arg view, xv, yv ; 
				 		var zo = ((xv-(step/3*2)-(5*step))/step).clip(0, 3) ;
						var yo =  ((yv-((3+(2/3))*step))/step).clip(0,2) ; 
						var xo = xs ;
						#xs, ys, zs = [xo, yo, zo] ;
						model.changePosition(key, 
							xo.round(0.01), yo.round(0.01), zo.round(0.01)) ;
						model.play(attack, attack + duration) ;
						}) 
					.mouseMoveAction_({ arg view, xv, yv ;
						var zo = ((xv-(step/3*2)-(5*step))/step).clip(0, 3) ;
						var yo =  ((yv-((3+(2/3))*step))/step).clip(0,2) ; 
						var xo = xs ;
						#xs, ys, zs = [xo, yo, zo] ;
						model.changePosition(key, 
							xo.round(0.01), yo.round(0.01), zo.round(0.01))
			 		})

					.background_(col),
				GUI.staticText.new(container,		
					Rect( 
						(x+2.5+(1/3))*step-(dotStep/2), 
						(6-(z+(2/3)+2))*step-(dotStep/2),
						 dim[0], dim[1]))
						.string_(key),
				GUI.staticText.new(container,		
					Rect(
						(x+2.5+(1/3))*step-(dotStep/2), 
						(y+(3)+(2/3))*step-(dotStep/2), 
						dim[0], dim[1]))
						 .string_(key),	
				GUI.staticText.new(container,							Rect(
						(z+5+(2/3))*step-(dotStep/2),
						(y+(3)+(2/3))*step-(dotStep/2), 
						 dim[0], dim[1]))
						 .string_(key)
			]) ;
		// still protection
		model.changePosition(key,  profile, caliber, variation) ;
	}
	

	createGuiDictionary { 
		var attack, duration, profile, caliber, variation, 
				site, dynamics, group, text, rank ;
		var position, x, y ,z, xs, ys, zs ;
		objectDict.keys.do({ arg key ; // legacy: should be "name"
			#attack, duration, profile, caliber, variation, 
				site, dynamics, group, text, rank = objectDict[key] ;				this.createGuiElement(key, attack, duration, profile, caliber, variation, 
				site, dynamics, group, text, rank)
		}) ;
		^guiDict
	}


	createClassSpace {
		var x, y, z, third = step/3, twothird = third*2 ;
		
		5.do({ arg i ;
			var arr = ["E", "W", "Phi", "K", "A"] ; 
			var col = (Color(1.0, 0.85, 0.85)) ;	
			GUI.staticText.new(container, Rect(i*step+third, third, step, step))
			.background_(Color(0.96, 0.96, 0.96))
			.string_(arr[i]).align_( \center ).font_(GUI.font.new("Helvetica", size: 60))
			.stringColor_(col) ;
		}) ;
		
		5.do({ arg i ;
			var arr = ["T", "Y", "Yi", "Yii", "P"] ;
			var col = (Color(1.0, 0.85, 0.85)) ;	  
			if ( [0, 4].includes(i) , { col = (Color(0.85, 0.85, 1.0)) }, {col = col} ) ; 
			if ( [1, 2, 3].includes(i) , { col = (Color(0.85, 1, 0.85)) }, {col = col} ) ; 
			GUI.staticText.new(container, Rect(i*step+third, step+third, step, step))
			.background_(Color(0.96, 0.96, 0.96))
			.string_(arr[i]).align_( \center ).font_(GUI.font.new("Helvetica", size: 60))
			.stringColor_(col) ;

		
		}) ;
		5.do({ arg i ;
			var arr = ["Hn", "N", "Ni", "Nii", "Zn"] ; 
			var col = (Color(1.0, 0.85, 0.85)) ;	  
			if ( [0, 4].includes(i) , { col = (Color(0.85, 0.85, 1.0)) }, {col = col} ) ; 
			if ( [1, 2, 3].includes(i) , { col = (Color(0.85, 1, 0.85)) }, {col = col} ) ; 
			GUI.staticText.new(container, Rect(i*step+third, 2*step+third, step, step))
			.background_(Color(0.96, 0.96, 0.96))
			.string_(arr[i]).align_( \center ).font_(GUI.font.new("Helvetica", size: 60))
			.stringColor_(col) ;
		}) ;

		5.do({ arg i ;
			var arr = ["Hn", "N", "Ni", "Nii", "Zn"] ;
				var col = (Color(1.0, 0.85, 0.85)) ;	  
			if ( [0, 4].includes(i) , { col = (Color(0.85, 0.85, 1.0)) }, {col = col} ) ; 
			if ( [1, 2, 3].includes(i) , { col = (Color(0.85, 1, 0.85)) }, {col = col} ) ; 
 
			GUI.staticText.new(container, Rect(i*step+third, 3*step+twothird, step, step))
			.background_(Color(0.96, 0.96, 0.96))
			.string_(arr[i]).align_( \center ).font_(GUI.font.new("Helvetica", size: 60))
			.stringColor_(col) ;
		}) ;
		5.do({ arg i ;
			var arr = ["Hx", "X", "Xi", "Xii", "Zx"] ; 
			var col = (Color(1.0, 0.85, 0.85)) ;	  
			if ( [0, 4].includes(i) , { col = (Color(0.85, 0.85, 1.0)) }, {col = col} ) ; 
			if ( [1, 2, 3].includes(i) , { col = (Color(0.85, 1, 0.85)) }, {col = col} ) ; 

			GUI.staticText.new(container, Rect(i*step+third, 4*step+twothird, step, step))
			.background_(Color(0.96, 0.96, 0.96))
			.string_(arr[i]).align_( \center ).font_(GUI.font.new("Helvetica", size: 60))
						.stringColor_(col) ;
		}) ;
		
		3.do({ arg i ;
			var arr = ["Zn", "P", "A"] ; 
			var col = (Color(1.0, 0.85, 0.85)) ;	  
			if ( [0,1].includes(i) , { col = (Color(0.85, 0.85, 1.0)) }, {col = col} ) ; 
			if ( [3].includes(i) , { col = (Color(0.85, 1, 0.85)) }, {col = col} ) ; 

			GUI.staticText.new(container, Rect(i*step+(5*step)+twothird, 3*step+twothird, step, step))
			.background_(Color(0.96, 0.96, 0.96))
			.string_(arr[i]).align_( \center ).font_(GUI.font.new("Helvetica", size: 60)) 
			.stringColor_(col) ;
		}) ;
		
		3.do({ arg i ;
			var arr = ["Zx", "P", "A"] ; 
			var col = (Color(1.0, 0.85, 0.85)) ;	  
			if ( [0,1].includes(i) , { col = (Color(0.85, 0.85, 1.0)) }, {col = col} ) ; 
			if ( [3].includes(i) , { col = (Color(0.85, 1, 0.85)) }, {col = col} ) ; 
			GUI.staticText.new(container, Rect(i*step+(5*step)+twothird, 4*step+twothird, step, step))
			.background_(Color(0.96, 0.96, 0.96))
			.string_(arr[i]).align_( \center ) .font_(GUI.font.new("Helvetica", size: 60))
			.stringColor_(col) ;		
		})


	}

	redraw  {
		guiDict.do({|i| i[11..16].do({|v| v.remove }) }) ;
		guiDict = IdentityDictionary.new ; 
		container.refresh ; this.createGuiDictionary ;
	}

	nameOn_ { arg bool ;
		nameOn = bool ;
		this.redraw
	}


}