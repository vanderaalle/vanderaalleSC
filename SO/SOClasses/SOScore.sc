SOScore {

	var 	<>so, <>duration, <>objectDict ; 	// so properties
	var 	<width, <height ;				// graphical properties
	var 	<>timeRatio ;
	var 	<>w, <>title ; 				// the main window 
							// drawingFunc
	var 	drawTicks, drawRegister, drawTickLabels, drawObjects, drawProfile,
		drawVariation, drawSustain, drawName ;
	var 	<>objects, <name, <profile, <sustain, <variation, <ticks, <register, 
		<timeLabels, <from, <to, <groups, <tickInterval ;

	*new { arg so, objects = true, 
				name = true, 
				profile = true, 
				sustain = true,
				variation = true, 
				ticks = true, 
				register = true, 
				timeLabels = true,
				from = 0, to, groups,
				tickInterval,
		
				width = 800, height = 400, title, update = true ;	
		^super.new.initSOScore(so, 			
			[objects, name, profile, sustain, variation, ticks, register, timeLabels, from, to,
				groups, tickInterval],
				width, height, title, update 
			)
			.paint
		}

	initSOScore { arg aSo, 
			opts, aWidth, aHeight, aTitle, isUpdated   ;
		so = aSo ;
		#objects, name, profile, sustain, variation, ticks, register, timeLabels, from, to,
				groups, tickInterval = opts ;
		duration = so.duration ;
		objectDict = so.objectDict ;
		width = aWidth ;
		height = aHeight ;
		timeRatio = width/duration ;
		title = if (aTitle.isNil, { so.soundPath.split($/).reverse[0].split($.)[0] }, 
			{ aTitle }) ;
		if (isUpdated) { so.addDependant(this) } ;
		w = GUI.window.new(title, Rect(100, 100,width, height)) ;
		//w.onClose_(so.removeDependant(this)) ;
		w.view.background = Color.white ;
		this.preProcess ;
// defining drawFunc 
//________________________________________________________________
		drawTicks = { arg timeInterval, from, to, timeRatio ; 
			var number = (duration/timeInterval).asInteger ;
			GUI.pen.strokeColor = Color(0.7, 0.7, 0.7, 0.7) ;
			number.do({ arg n ;	
				GUI.pen.line(timeInterval*timeRatio* (n+1)-(from*timeRatio) @ 0, 
					timeInterval*timeRatio*(n+1)-(from*timeRatio) @ height) ;
//				GUI.pen.dottedLine(timeInterval*timeRatio* (n+1)-(from*timeRatio) @ 0, 
//					timeInterval*timeRatio*(n+1)-(from*timeRatio) @ height) ;
			}) ;
			GUI.pen.stroke ;				
		} ;
//________________________________________________________________

		drawTickLabels = { arg timeInterval, from, to, timeRatio ; 
			var number = (duration/timeInterval).asInteger ;
			var label, pt = 10 ;
			GUI.pen.font = GUI.font.new( "Helvetica", pt );
       		GUI.pen.strokeColor = Color.red;
			number.do({ arg n ;	
				label = (timeInterval*(n+1)).asTimeString ;
				GUI.pen.stringAtPoint( label,
					timeInterval*timeRatio* (n+1)-(from*timeRatio)+3 @ (height-pt-3)) ;
			})			
		} ;
//________________________________________________________________
		drawRegister = { arg hStep ;	
		       	GUI.pen.strokeColor = Color(0.7, 0.7, 0.7, 0.7) ;
				9.do({ arg n ;
					GUI.pen.line(0 @ (n+1)*hStep, width @ (n+1)*hStep) ;
//					GUI.pen.dottedLine(0 @ (n+1)*hStep, width @ (n+1)*hStep) ;
				}) ;
			GUI.pen.stroke ;	
			} ;
	//________________________________________________________________
		drawObjects = { arg objectDef, from, to, timeRatio, x, h, w, y, hStep  ;
			// --> using group to select color 	
			var group, color ;
			var colorStep = 1.0/so.groupNumber ;
			GUI.pen.width_(2) ;       		
			group = objectDef[7] ;
			color = this.groupColor(group) ;
			// please use hsv, but pay attention to limits
			GUI.pen.strokeColor = color ;
			GUI.pen.strokeRect(Rect(x, y, w, h)) ;
			GUI.pen.stroke ;	
			} ;
	
	//________________________________________________________________
		drawProfile = { arg objectDef, from, to, timeRatio, x, h, w, y, hStep ; 
			var dyn, attack, duration, color ;
			var chunk ;
			var colorStep = 1.0/so.groupNumber ;
			GUI.pen.width_(1) ;       		
			dyn = objectDef[6] ;
			color = 1.0-(dyn/9) ; // here we are assuming that 0 < dyn < 9 
			GUI.pen.color = Color(color, color, color, 0.7) ;
			chunk = h*0.4*(2.5-objectDef[2].abs) ;
			GUI.pen.moveTo(x @ y) ;
			GUI.pen.lineTo((x+w) @ (y+chunk)) ; // here
			GUI.pen.lineTo((x+w) @ (y+h)) ;
			GUI.pen.lineTo(x @ (y+h) );
			GUI.pen.fill ;
			} ;
	//________________________________________________________________
		drawVariation = { arg objectDef, from, to, timeRatio, x, h, w, y ;
			var variation = objectDef[4] ;
			var dyn = objectDef[6] ;
			var color = 1.0-(dyn/9) ; // here we are assuming that 0 < dyn < 9 
			var density = variation*0.1 ;
			GUI.pen.color = Color(color, color, color, 0.8) ;
			GUI.pen.texture(Rect(x, y, w, h), density, 2, 2) ;
		} ;	 

	//________________________________________________________________
		drawSustain = { arg objectDef, from, to, timeRatio,  x, h, w, y ;
			var color, colorStep, group ;
			var step = 7 ;
			colorStep = 1.0/so.groupNumber ;
			group = objectDef[7] ;
			color = this.groupColor(group) ;
			case { objectDef[2].abs <= 0.5 }
				 {
				GUI.pen.fillColor = color ;
				GUI.pen.fillOval(Rect(x-5, y-5, 10 ,10)) ;
				}
				{ objectDef[2] > 0.5 } 
				{
				GUI.pen.width_(1) ;       
				GUI.pen.strokeColor = color ;
				(w/step).do({ arg i ;
					GUI.pen.line( (i*step+x) @ y,  (i*step+x) @ (y+h)) ;
				}) ;
				GUI.pen.stroke ;
				} ;
		} ;	 

	//________________________________________________________________
		drawName = { arg objectDef, from, to, timeRatio, x, h, w, y ;
			var name ;
			name = objectDict.findKeyForValue(objectDef) ;
			GUI.pen.color = Color.black ;
			GUI.pen.stringAtPoint(name.asString, (x+1) @ (y+1))
		} ; 
	
	}
	
	
	groupColor { arg group ;
			var colorStep = 1.0/(so.groupNumber) ;
			var color = colorStep*so.groupArray.indexOf(group) ;
			var col = Color.hsv(color, 1, 0.7, 0.9) ;
			^col
	}

			
	paint {	
			var timeRatio ;
			var hStep = height/9 ;
			var xx, hh, ww, yy ;
			to = to ? duration ;
			from = from ? 0 ;	
			groups = groups ? so.groupArray ;
			timeRatio = width/(to-from) ;
			tickInterval = tickInterval ? (to-from/10) ;
			w.drawHook = { 
				if ( register ) { drawRegister.value(hStep) } ;
				if (ticks) { drawTicks.value(tickInterval, from, to, timeRatio) };
				if (timeLabels) { drawTickLabels.value(tickInterval, from, to, timeRatio) } ;
				objectDict.do({ arg objectDef ;
					if ( groups.includes(objectDef[7]))
						{
						xx = (objectDef[0]-from)*timeRatio ;
						hh = hStep*objectDef[3]*4.5 ;
						ww = objectDef[1]*timeRatio ;
						yy = (height-(objectDef[5]-1 * hStep)-hh) ; // we assume that ultra-low is oct 1
						if ( objects ) 
							{ drawObjects.value(objectDef, from, to, timeRatio, xx, hh, ww, yy, hStep) } ;
						if ( profile )
							{ drawProfile.value(objectDef, from, to, timeRatio, xx, hh, ww, yy, hStep) } ; 
						if ( sustain ) 
							{ drawSustain.value(objectDef, from, to, timeRatio, xx, hh, ww, yy) } ;
						if ( variation ) 
							{ drawVariation.value(objectDef, from,to, timeRatio, xx, hh, ww, yy) };
						if ( name ) 
							{ÊdrawName.value(objectDef, from, to, timeRatio, xx, hh, ww, yy) } ;
						}
				})
			} ;
			w.front ;
		}

	// allows for drawing with nil values
	preProcess {
		var objectDef ;
		var profile, caliber, variation, site, dynamics, group, text ;
		objectDict.keys.do({ arg key ;
			objectDef = objectDict[key] ;
			#profile, caliber, variation, site, dynamics, group, text = 
				objectDef[2..8] ;
			profile = profile ? -2 ; 
			caliber = caliber ? 1 ; 
			variation = variation ? 0 ; 
			site = site ? 0 ;
			dynamics = dynamics ? 2 ; 
			so.changeFeatures ( key, profile, caliber, variation, 
				site, dynamics, group, text )		
		})
	
	}

	from_		{ arg val ; if (val.class == String) { val = val.asSecs } ;
					from = val ; this.update }
	to_			{ arg val ; if (val.class == String) { val = val.asSecs } ;
					to = val ; this.update }
					
	name_		{ arg val ; name = val ; this.update }
	profile_		{ arg val ; profile = val ; this.update }
	sustain_		{ arg val ; sustain = val ; this.update }
	variation_	{ arg val ; variation = val ; this.update }
	ticks_		{ arg val ; ticks = val ; this.update }
	register_		{ arg val ; register = val ; this.update }
	timeLabels_	{ arg val ; timeLabels = val ; this.update }
	tickInterval_	{ arg val ; tickInterval = val ; this.update }
	
	width_		{ arg val ; width = val ; 
			w.bounds_(Rect(100,100, width, height)) ;
			this.update }
			
	height_		{ arg val ; height = val ; 
			w.view.bounds_(Rect(100, 100, width, height)) ; 
			this.update }
	
	update { this.paint }


	toProcessing { arg path, pdf = true, pdfPath,
				objects = true ; 
		SOScoreProcessing.new(
				so,
				path, pdf, pdfPath,
				objects, 
				name, 
				profile, 
				sustain,
				variation, 
				ticks, 
				register, 
				timeLabels,
				from = 0, to, groups,
				tickInterval)
	}
	
	toNodeBox { arg path, pdf = true, pdfPath,
				objects = true ;
		SOScoreNodeBox.new(
				so,
				path, pdf, pdfPath,
				objects, 
				name, 
				profile, 
				sustain,
				variation, 
				ticks, 
				register, 
				timeLabels,
				from = 0, to, groups,
				tickInterval)
	}


}

