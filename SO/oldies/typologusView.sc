
TypologusViewController : SimpleController {
	
	var <>objectDict, <>guiDict ;
	var <>dotList ;
	var <>step, <>dotStep ;
	var <>container, <>background, <>upSide, <>frontSide, <>lateralSide ; 
	var <>textBox1, <>textBox2, <>textBox3, <>textBox4, <>textBox ;
	var <>actions ;
	
	
	*new { arg model;
		^super.newCopyArgs(model).init.initViewController
	}

	initViewController {
		var texts = ["Name", "Postion", "Remarks", "Rank"] ;
		// set some variables
		step = 100 ;
		dotStep = 6 ;
		objectDict = model.objectDict ;
		// actions dict
		actions = IdentityDictionary[
		// on changePosition
			\position		-> { arg model, what, rankAndPosition ;
				var x, y, z ;
				var name, position, remarks, rank ;
				#name, position, remarks, rank = rankAndPosition ;
				#x,y,z = position.postln ; 
				guiDict[rank][3].bounds_(
					Rect(
						(x+2.5+(1/3))*step-(dotStep/2), 
						((3-z)+(1/3))*step-(dotStep/2),
						dotStep, dotStep)
						) ;
				guiDict[rank][4].bounds_(
					Rect(
						(x+2.5+(1/3))*step-(dotStep/2), 
						(y+(2/3)+3)*step-(dotStep/2),
						dotStep, dotStep) );						guiDict[rank][5].bounds_(
					Rect(
						(z+5+(2/3))*step-(dotStep/2),
						(y+(2/3)+3)*step-(dotStep/2),
						dotStep, dotStep )
						);
				textBox[0].string_(name) ;
				textBox[1].string_(position.asString) ;
				textBox[2].string_(remarks) ;
				//textBox[3].string_(rank) ;
				container.refresh
				 }
				] ;

		// select Swing GUI system
		if ( GUI.current != SwingGUI, { GUI.swing } ) ;
		// boot it and create some widget 
		if (SwingOSC.local.serverRunning.not, { SwingOSC.local.boot } ) ;
		SwingOSC.local.doWhenBooted( 
			container = GUI.window.new("Typological Space", 
				Rect(50,50, 9*step, 6*step)).front ; 
			// displaying some text
			Array.fill(3, { arg ind ;
				GUI.staticText.new(container, 
					Rect(step/3*2+(5*step),  step/3+(ind*20), 3*step, 20))
					//.background_(Color.blue)
					.stringColor_(Color.new255(0, 47, 167))
					.string_(texts[ind] ;
				)
				}) ;
			// entry fields
			textBox = Array.fill(3, { arg ind ;
				var plus = if (ind == 2, {100 }, { 0 }) ;
				GUI.textView.new(container,									Rect(step/3*2+(5*step)+step,  step/3+(ind*20), 3*step+step/2, 20+plus))
						.action_({ arg view, state ...params ; 
							if ( (state == 'insert').and(params[2] == "\n"), 
							{ "Test Me".postln })
							}) ;
				}) ;
			guiDict = this.createGuiDictionary ;
			this.createClassSpace ;
			frontSide = GUI.userView.new(container, 
				Rect(step/3, step/3*2 + (3*step), 5*step, 2*step))
				.background_(Color.white) ;
			upSide = GUI.userView.new(container, 
				Rect(step/3, step/3, 5*step, 3*step))
				.background_(Color.white)  ;
			lateralSide = GUI.userView.new(container, 
				Rect(step/3*2+(5*step), step/3*2 + (3*step), 3*step, 2*step))
				.background_(Color.white)  ;
			background = GUI.userView.new(container, 
				Rect(0, 0, 9*step, 6*step))
				.background_(Color.white) ;
		// Color.new255(0, 47, 167)
			container.front 
			) ;
//			container.refresh
	}
	

	calculateMaxRank {
		var rank, rankList = [] ;
		objectDict.keys.do({ arg key ;
					rank = objectDict[key][2] ;
					rankList = rankList.add(rank) ;
		}) ;
		^rankList.sort.reverse[0]
		}

	createGuiDictionary { 
		var guiDict = IdentityDictionary.new ;
		var position, remarks, rank ;
		var x, y ,z, xs, ys, zs ;
		var col, maxRank ;
		maxRank = this.calculateMaxRank ;
		objectDict.keys.do({ arg key ;
			#position, remarks, rank = objectDict[key] ;
			#x, y, z = position ;
			#xs, ys, zs  = position ;
			col = Color.new(rank/maxRank, rank/maxRank, 0.5) ;
	 		guiDict.add(rank -> [key, position, rank, 
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
							[xo.round(0.01), yo.round(0.01), zo.round(0.01)]) ;
						model.play(key) ;
						}) 
					.mouseMoveAction_({ arg view, xv, yv ;
						var xo = ((xv-(step/3))/step-2.5).clip(-2.5, 2.5) ;
						var zo = 3-(((yv-(step/3))/step).clip(0,3)) ;
						var yo = ys ;
						#xs, ys, zs = [xo, yo, zo] ;
						model.changePosition(key, 
							[xo.round(0.01), yo.round(0.01), zo.round(0.01)])
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
								(yv.postln-(
								(3+(2/3))*step).postln
								).postln
							/step
							).clip(0,2) ; 
						//(2-((yv-(3+(2/3)*step))/step)).clip(0,2) ;
						var zo = zs ;
						#xs, ys, zs = [xo, yo, zo] ;
						"DOWN ".post; [xo, yo, zo].postln ;
						model.changePosition(key, 
							[xo.round(0.01), yo.round(0.01), zo.round(0.01)]) ;
						model.play(key) ;
						}) 
					.mouseMoveAction_({ arg view, xv, yv ;
						var xo = ((xv-(step/3))/step -2.5).clip(-2.5, 2.5) ;
						var yo =  (yv-((3+(2/3))*step)/step).clip(0,2) ; 
						//var yo = (2-((yv-(3+(2/3)*step))/step)).clip(0,2) ;
						var zo = zs ;
						#xs, ys, zs = [xo, yo, zo] ;
						"MOVE ".post; [xo, yo, zo].postln ;
						model.changePosition(key, 
							[xo.round(0.01), yo.round(0.01), zo.round(0.01)])
			 		})
					.background_(col),
				GUI.userView.new(container, 
					Rect(
						(z+5+(2/3))*step-(dotStep/2),
						(y+(3)+(2/3))*step-(dotStep/2), 
						//(6 - ((2-y)+(1/3)))*step-(dotStep/2),
						 dotStep, dotStep))
				 	.mouseDownAction_({	arg view, xv, yv ; 
				 		var zo = ((xv-(step/3*2)-(5*step))/step).clip(0, 3) ;
						var yo =  ((yv-((3+(2/3))*step))/step).clip(0,2) ; 
						//var yo = (2-((yv-(3+(2/3)*step))/step)).clip(0, 2) ;
						var xo = xs ;
						yo.postln ;
						#xs, ys, zs = [xo, yo, zo] ;
						model.changePosition(key, 
							[xo.round(0.01), yo.round(0.01), zo.round(0.01)]) ;
						model.play(key) ;
						}) 
					.mouseMoveAction_({ arg view, xv, yv ;
						var zo = ((xv-(step/3*2)-(5*step))/step).clip(0, 3) ;
						var yo =  ((yv-((3+(2/3))*step))/step).clip(0,2) ; 
						//var yo = (2-((yv-(3+(2/3)*step))/step)).clip(0, 2) ;
						var xo = xs ;
						yo.postln ;
						#xs, ys, zs = [xo, yo, zo] ;
						model.changePosition(key, 
							[xo.round(0.01), yo.round(0.01), zo.round(0.01)])
			 		})

					.background_(col)
			]) ;
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
			.string_(arr[i]).align_( \center ).font_(GUI.font.new(size: 60))
			.stringColor_(Color(1.0, 0.85, 0.85))
			.stringColor_(col) ;
		}) ;
		
		5.do({ arg i ;
			var arr = ["T", "Y", "Yi", "Yii", "P"] ;
			var col = (Color(1.0, 0.85, 0.85)) ;	  
			if ( [0, 4].includes(i) , { col = (Color(0.85, 0.85, 1.0)) }, {col = col} ) ; 
			if ( [1, 2, 3].includes(i) , { col = (Color(0.85, 1, 0.85)) }, {col = col} ) ; 
			GUI.staticText.new(container, Rect(i*step+third, step+third, step, step))
			.background_(Color(0.96, 0.96, 0.96))
			.string_(arr[i]).align_( \center ).font_(GUI.font.new(size: 60))
			.stringColor_(col) ;

		
		}) ;
		5.do({ arg i ;
			var arr = ["Hn", "N", "Ni", "Nii", "Zn"] ; 
			var col = (Color(1.0, 0.85, 0.85)) ;	  
			if ( [0, 4].includes(i) , { col = (Color(0.85, 0.85, 1.0)) }, {col = col} ) ; 
			if ( [1, 2, 3].includes(i) , { col = (Color(0.85, 1, 0.85)) }, {col = col} ) ; 
			GUI.staticText.new(container, Rect(i*step+third, 2*step+third, step, step))
			.background_(Color(0.96, 0.96, 0.96))
			.string_(arr[i]).align_( \center ).font_(GUI.font.new(size: 60))
			.stringColor_(col) ;
		}) ;

		5.do({ arg i ;
			var arr = ["Hn", "N", "Ni", "Nii", "Zn"] ;
				var col = (Color(1.0, 0.85, 0.85)) ;	  
			if ( [0, 4].includes(i) , { col = (Color(0.85, 0.85, 1.0)) }, {col = col} ) ; 
			if ( [1, 2, 3].includes(i) , { col = (Color(0.85, 1, 0.85)) }, {col = col} ) ; 
 
			GUI.staticText.new(container, Rect(i*step+third, 3*step+twothird, step, step))
			.background_(Color(0.96, 0.96, 0.96))
			.string_(arr[i]).align_( \center ).font_(GUI.font.new(size: 60))
			.stringColor_(col) ;
		}) ;
		5.do({ arg i ;
			var arr = ["Hx", "X", "Xi", "Xii", "Zx"] ; 
			var col = (Color(1.0, 0.85, 0.85)) ;	  
			if ( [0, 4].includes(i) , { col = (Color(0.85, 0.85, 1.0)) }, {col = col} ) ; 
			if ( [1, 2, 3].includes(i) , { col = (Color(0.85, 1, 0.85)) }, {col = col} ) ; 

			GUI.staticText.new(container, Rect(i*step+third, 4*step+twothird, step, step))
			.background_(Color(0.96, 0.96, 0.96))
			.string_(arr[i]).align_( \center ).font_(GUI.font.new(size: 60))
						.stringColor_(col) ;
		}) ;
		
		3.do({ arg i ;
			var arr = ["Zn", "P", "A"] ; 
			var col = (Color(1.0, 0.85, 0.85)) ;	  
			if ( [0,1].includes(i) , { col = (Color(0.85, 0.85, 1.0)) }, {col = col} ) ; 
			if ( [3].includes(i) , { col = (Color(0.85, 1, 0.85)) }, {col = col} ) ; 

			GUI.staticText.new(container, Rect(i*step+(5*step)+twothird, 3*step+twothird, step, step))
			.background_(Color(0.96, 0.96, 0.96))
			.string_(arr[i]).align_( \center ).font_(GUI.font.new(size: 60)) 
			.stringColor_(col) ;
		}) ;
		
		3.do({ arg i ;
			var arr = ["Zx", "P", "A"] ; 
			var col = (Color(1.0, 0.85, 0.85)) ;	  
			if ( [0,1].includes(i) , { col = (Color(0.85, 0.85, 1.0)) }, {col = col} ) ; 
			if ( [3].includes(i) , { col = (Color(0.85, 1, 0.85)) }, {col = col} ) ; 
			GUI.staticText.new(container, Rect(i*step+(5*step)+twothird, 4*step+twothird, step, step))
			.background_(Color(0.96, 0.96, 0.96))
			.string_(arr[i]).align_( \center ) .font_(GUI.font.new(size: 60))
			.stringColor_(col) ;		
		})


	}


	drawObject { arg item ;
	 	var name, position, remarks, rank ; 
	 	rank = item[2] ;
	 	#name, position, remarks = guiDict[rank] ;
	 	
	}
	
	drawAllObject { 
		objectDict.do({ arg item ;
			this.drawObject(item) ;			
		})
	}
	


}