SOScoreProcessing {

	var 	<>so, <>duration, <>objectDict ; 	// so properties
	var 	<>width, <>height ;				// graphical properties
	var 	<>timeRatio ;
	var 	<>w, <>title, <>path, <>pdfPath, <>pdf ; 				// drawing properties
	var <>text, <>file ; 
							// drawingFunc
	var 	drawTicks, drawRegister, drawTickLabels, drawObjects, drawProfile,
		drawVariation, drawSustain, drawName ;
	var 	<>objects, <>name, <>profile, <>sustain, <>variation, <>ticks, <>register, 
		<>timeLabels, <>from, <>to, <>groups, <>tickInterval ;

	*new { arg so, path, pdf = true, pdfPath,
				objects = true, 
				name = true, 
				profile = true, 
				sustain = true,
				variation = true, 
				ticks = true, 
				register = true, 
				timeLabels = true,
				from = 0, to, groups,
				tickInterval,
		
				width = 800, height = 400, title, update = true ;			^super.new.initSOProcessing(so, path,	pdf, pdfPath,		
			[objects, name, profile, sustain, variation, ticks, register, timeLabels, from, to,
				groups, tickInterval],
				width, height, title, update 
			)
		}

	initSOProcessing { arg aSo, aPath, aPdf, aPdfPath, 
			opts, aWidth, aHeight, aTitle, isUpdated   ;
		so = aSo ;
		#objects, name, profile, sustain, variation, ticks, register, timeLabels, from, to,
				groups, tickInterval = opts ;
		duration = so.duration ;
		objectDict = so.objectDict ;
		width = aWidth ;
		height = aHeight ;
		timeRatio = width/duration ;
		path 	= if (aPath.isNil, { so.soundPath.split($.)[0]++".pde"}, { aPath }) ;
		pdf = aPdf ;
		pdfPath 	= if (aPdfPath.isNil, { path.split($.)[0]++".pdf"}, { aPath }) ;
		title 	= if (aTitle.isNil, { so.soundPath.split($/).reverse[0].split($.)[0] }, 
			{ aTitle }) ;
		if (isUpdated) { so.addDependant(this) } ;
		this.update
	}
	
	open { file = File.new(path, "w") ;
		file.write("import processing.pdf.* ;\n") ;
		if (pdf) { file.write("beginRecord(PDF, \""++pdfPath++"\");\n ")} ;
	}

	close { if ( pdf) {file.write("endRecord() ;\n")} ;file.close }


	drawSpace {
		file.write(format("size(%, %) ;\n", width, height)) ;
		file.write("background(255, 255, 255) ;\n") ;
		file.write("colorMode(RGB, 1.0) ;\n") ;
	}
		
	drawTicks { arg timeInterval, from, to, timeRatio ; 
		var number = (duration/timeInterval).asInteger ;
		var a, b,c,d ;
		file.write("strokeWeight(0.5) ;\n") ;
		file.write("stroke(0.7, 0.7, 0.7, 0.7) ;\n") ;
		number.do({ arg n ;	
			a = timeInterval*timeRatio* (n+1)-(from*timeRatio) ;
			b = 0 ;
			c = timeInterval*timeRatio*(n+1)-(from*timeRatio) ;
			d = height ;
			//this.dottedLine(timeInterval*timeRatio* (n+1)-(from*timeRatio) @ 0, 
			//		timeInterval*timeRatio*(n+1)-(from*timeRatio) @ height)
			file.write(format("fill(%, %, %) ;\n", 0.5, 0.5, 0.5)) ;
			file.write(format("line(%,%,%,%) ;\n", a, b ,c ,d)) ;
		})			
	} 
//________________________________________________________________

/*
	drawTickLabels { arg timeInterval, from, to, timeRatio ; 
		var number = (duration/timeInterval).asInteger ;
		var label, pt = 10 ;
		GUI.pen.font = GUI.font.new( "Helvetica", pt );
       		GUI.pen.strokeColor = Color.red;
		number.do({ arg n ;	
			label = (timeInterval*(n+1)+from).asTimeString ;
			GUI.pen.stringAtPoint( label,
				timeInterval*timeRatio* (n+1)-(from*timeRatio)+3 @ (height-pt-3)) ;
		})			
	} ;
*/
//________________________________________________________________
	drawRegister { arg hStep ;	
				file.write("stroke(0.7, 0.7, 0.7, 0.7) ;\n") ;
				9.do({ arg n ;				
					file.write("strokeWeight(0.5) ;\n") ;
//					this.dottedLine(0 @ (n+1)*hStep, width @ (n+1)*hStep) ;
					file.write(format("line(%,%,%,%) ;\n", 0, (n+1)*hStep ,width ,(n+1)*hStep)) ;
				})
		} 
//________________________________________________________________
	drawObjects { arg objectDef, from, to, timeRatio, x, h, w, y, hStep  ;
		// --> using group to select color 	
		var group, color ;
		var colorStep = 1.0/so.groupNumber ;
		var u, s, v, a ;
		file.write("colorMode(HSB, 1.0) ;\n") ;
		file.write("strokeWeight(1.25) ;\n") ;
		file.write("noFill() ;\n") ;
		group = objectDef[7] ;
		#u, s, v, a = this.groupColor(group) ;
		file.write(format("stroke(%, %, %, %) ;\n", u, s, v, a)) ;
		file.write(format("rect(%, %, %, %) ;\n", x, y, w, h)) ;
		file.write("colorMode(RGB, 1.0) ;\n") ;
		} 

//________________________________________________________________
	drawProfile { arg objectDef, from, to, timeRatio, x, h, w, y, hStep ; 
		var dyn, attack, duration, color ;
		var chunk ;
		var colorStep = 1.0/so.groupNumber ;
		file.write("strokeWeight(1) ;\n") ;
		dyn = objectDef[6] ;
		color = 1.0-(dyn/9) ; // here we are assuming that 0 < dyn < 9 
		file.write(format("fill(%, %, %, %) ;\n", color, color, color, 0.7)) ;
		chunk = h*0.4*(2.5-objectDef[2].abs) ;
		file.write("beginShape() ;\n") ;
		file.write(format("vertex(%, %) ;\n", x, y)) ;
		file.write(format("vertex(%, %) ;\n", x+w, y+chunk)) ;
		file.write(format("vertex(%, %) ;\n", x+w, y+h)) ;
		file.write(format("vertex(%, %) ;\n", x, y+h)) ;
			file.write(format("vertex(%, %) ;\n", x, y)) ;
		file.write("endShape() ;\n") ;
		file.write("noFill() ;\n") ;
		} 
//________________________________________________________________
	drawVariation { arg objectDef, from, to, timeRatio, x, h, w, y ;
		var variation = objectDef[4] ;
		var dyn = objectDef[6] ;
		var color = 1.0-(dyn/9) ; // here we are assuming that 0 < dyn < 9 
		var density = variation*0.1 ;
		file.write("strokeWeight(1) ;\n") ;
		file.write(format("stroke(%, %, %, %) ;\n", color, color, color, color, 0.8)) ;
		file.write(format("fill(%, %, %, %) ;\n", color, color, color, color, 0.8)) ;
		this.texture(Rect(x, y, w, h), density, 2, 2) ;
	}  

//________________________________________________________________
	drawSustain { arg objectDef, from, to, timeRatio,  x, h, w, y ;
		var color, colorStep, group ;
		var step = 7 ;
		var u, s, v, a ;
		colorStep = 1.0/so.groupNumber ;
		group = objectDef[7] ;
		#u, s, v, a = this.groupColor(group) ;
		file.write("strokeWeight(1) ;\n") ;
		case { objectDef[2].abs <= 0.5 }
			 {
			 file.write("colorMode(HSB, 1.0) ;\n") ;
			file.write(format("fill(%, %, %, %) ;\n", u, s, v, a)) ;
			file.write(format("ellipse(%, %, %, %) ;\n", x, y, 10 ,10)) ;
			file.write("colorMode(RGB, 1.0) ;\n") ;
			}
			{ objectDef[2] > 0.5 } 
			{
			file.write("colorMode(HSB, 1.0) ;\n") ;
			file.write("strokeWeight(1) ;\n") ;
			file.write(format("fill(%, %, %, %) ;\n", h, s, v, a)) ;
			(w/step).do({ arg i ;
				file.write(format("line(%, %, %, %) ;\n", i*step+x, y, i*step+x, y+h)) ;
			}) ;
			file.write("colorMode(RGB, 1.0) ;\n") ;
			} 
	}	 

//________________________________________________________________
	drawName { arg objectDef, from, to, timeRatio, x, h, w, y ;
		var name ;
		name = objectDict.findKeyForValue(objectDef) ;
		GUI.pen.color = Color.black ;
		GUI.pen.stringAtPoint(name.asString, (x+1) @ (y+1))
	} 
	
	
	groupColor { arg group ;
			var colorStep = 1.0/(so.groupNumber) ;
			var color = colorStep*so.groupArray.indexOf(group) ;
			^[color, 1, 0.7, 0.9]  
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
			if ( register ) { this.drawRegister(hStep) } ;
			if (ticks) { this.drawTicks(tickInterval, from, to, timeRatio) };
//			if (timeLabels) { this.drawTickLabels(tickInterval, from, to, timeRatio) } ;
			objectDict.do({ arg objectDef ;
				if ( groups.includes(objectDef[7]))
					{
					xx = (objectDef[0]-from)*timeRatio ;
					hh = hStep*objectDef[3]*4.5 ;
					ww = objectDef[1]*timeRatio ;
					yy = (height-(objectDef[5]-1 * hStep)-hh+1) ; // we assume that ultra-low is oct 1
					if ( objects ) 
						{ this.drawObjects(objectDef, from, to, timeRatio, xx, hh, ww, yy, hStep) } ;
					if ( profile )
						{ this.drawProfile(objectDef, from, to, timeRatio, xx, hh, ww, yy, hStep) } ;
					if ( sustain ) 
						{ this.drawSustain(objectDef, from, to, timeRatio, xx, hh, ww, yy) } ;
					if ( variation ) 
						{ this.drawVariation(objectDef, from,to, timeRatio, xx, hh, ww, yy) };
/*
					if ( name ) 
						{Êthis.drawName(objectDef, from, to, timeRatio, xx, hh, ww, yy) } ;
*/
					}
			}) ;

		}


	texture { arg rect, pointsPerUnit = 0.15, w = 1, h = 1, bpp = 1 ;
		var totalPoints = (rect.width * rect.height)*pointsPerUnit ; 
		var x = rect.left, y = rect.top ;
		totalPoints.do({ arg i ;
                 if( bpp.coin, {
               file.write(format("ellipse(%, %, %, %) ;\n", 
               	rrand(x, x + rect.width-w),
               	rrand(y, y + rect.height-h),
                       	w,
                       h
                       )
                   )               
           },{
               file.write(format("rect(%, %, %, %) ;\n", 
               	rrand(x + w, x + rect.width-w),
               	rrand(y + h, y + rect.height-h),
                       	w,
                       h
                       )
                   )
          
		})

	})
	}	

	dottedLine { arg p1, p2, pointsPerUnit = 0.15, w = 1, h = 1, bpp = 1;
       // if bpp == 1, points are drawn as ovals
       // if bpp == 0, points are drawn as rectangles
       // values in between 0 and 1 are the probability of a point being drawn as a rectangle or an oval
       // ... 0.3 is a 30% chance of an oval ... 0.75 is a 75% chance of an oval
       var x1 = p1.x; var y1 = p1.y; var x2 = p2.x; var y2 = p2.y ;
       var length = ((x2-x1).squared + (y2 - y1).squared).sqrt;
       var density = pointsPerUnit * length;
       var xIncr = (x2-x1)/density;
       var yIncr = (y2-y1)/density;
       (density+1).do({ arg i ;
              file.write(format("ellipse(%, %, %, %) ;\n",
                       xIncr * i + x1, yIncr * i + y1, w, h))
       })
   }


	update { 	
		this.open ;
		this.drawSpace ;
		this.paint ;
		this.close
	}

}

