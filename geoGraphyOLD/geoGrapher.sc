GeoGrapher {
	var 	<>vertexXY, <>positionXY, <>oldPositionXY, <>projectionXY,
		<>coefficient, <>orthogonalCoefficient;

	*new { arg vertexXY, positionXY, oldPositionXY;
		^super.new.init( vertexXY, positionXY, oldPositionXY );
		}

	// here we define the segment on two trajectory points
	calculatePositionLineCoefficient { arg positionXY, oldPositionXY;
		var coefficient;
		var 	positionX = positionXY[0], positionY = positionXY[1], 
			oldPositionX = oldPositionXY[0], oldPositionY = oldPositionXY[1];
		
		coefficient = (oldPositionY-positionY)/(oldPositionX-positionX);
		^coefficient;
	 	}

	calculateOrthogonalCoefficient { arg coefficient;
		^coefficient.reciprocal;
		}


        /*
        input coefficiente e le coord di m e v (coppia punti)"
       restituisce le coord della proiezione di v sulla ortogonale per m" 
        */

    calculateProjection { arg coefficient,
    				vertexXY, positionXY;
    				
    	   var 	vertexX = vertexXY[0], vertexY = vertexXY[1], 
    	   		positionX = positionXY[0], positionY = positionXY[1];	
        var 	orthoCoefficient, projectionX, projectionY, num_ort, num_paral;
        
        projectionX = vertexXY[0]; projectionY = positionXY[1] ;
        
        projectionXY = case
        		{ coefficient == 0 } {
                 	projectionX = positionX;
                 	projectionY = vertexY;
                 	}
              {  [-inf, inf].includes(coefficient)}	{
                 	projectionX = vertexXY[0]; 
                 	projectionY = positionXY[1];
                 	}
			{ [0, -inf, inf].includes(coefficient).not } {  
                 orthoCoefficient = coefficient.reciprocal.neg;
                 num_ort = positionY-(orthoCoefficient*positionX);
                 num_paral = vertexY-(coefficient*vertexX);
                 projectionX = (num_paral-num_ort)/(orthoCoefficient-coefficient);
                 projectionY = (coefficient*projectionX)+num_paral;
                 };
        
        
        /*
        
        if ( coefficient == 0, {
                 projectionX = positionX;
                 projectionY = vertexY;
        }, {  
                 orthoCoefficient = coefficient.reciprocal.neg;
                 num_ort = positionY-(orthoCoefficient*positionX);
                 num_paral = vertexY-(coefficient*vertexX);
                 projectionX = (num_paral-num_ort)/(orthoCoefficient-coefficient);
                 projectionY = (coefficient*projectionX)+num_paral;
         });
        */
        
        ^[projectionX, projectionY]
        }

	calculateDistance { arg xy1, xy2;  // distance between (x1, y1) and (x2, y2)
	
		var x1 = xy1[0], y1 = xy1[1], x2 =  xy2[0], y2 = xy2[1];
		var distance; 
		
		distance = ((x1-x2).pow(2) + (y1-y2).pow(2)).sqrt;
		^distance;
		}

/*
	// vertical line --> x = k
	calculateConstantXPan { arg vertexXY, positionXY, projectionXY;
		var 	vertexPositionDistance, vertexProjectionDistance, 
			positionProjectionDistance, lateralPan, frontalPan, pans; 
				 
		vertexPositionDistance = this.calculateDistance( vertexXY, positionXY ).postln;
		vertexProjectionDistance = this.calculateDistance( vertexXY, 				projectionXY ).postln;
		positionProjectionDistance = this.calculateDistance( positionXY, 				projectionXY ).postln;
		// pan is returned as abs, so that positive cases are not included in setGeneralSign
		lateralPan = abs( positionProjectionDistance / vertexPositionDistance );
		frontalPan = abs( vertexProjectionDistance / vertexPositionDistance );
		^[lateralPan, frontalPan];
		}
*/		

	// here we calculate all and set 
	init { arg vertexXY, positionXY, oldPositionXY;
		this.vertexXY = vertexXY;
	//	if ( positionXY == vertexXY, { positionXY = positionXY + 0.000000000001}); // oh you dirty boy!
		this.positionXY = positionXY;
		//this.positionXY.postln;
		this.oldPositionXY = oldPositionXY;
		this.coefficient = this.calculatePositionLineCoefficient ( this.positionXY, this.oldPositionXY);
		this.orthogonalCoefficient = this.calculateOrthogonalCoefficient( this.coefficient );
		this.projectionXY = this.calculateProjection( this.coefficient, this.vertexXY, this.positionXY );
    				
	}
	
	
	calculateGeneralPans { arg vertexXY, positionXY, projectionXY, vertexPositionDistance;

		var 	vertexProjectionDistance, 
			positionProjectionDistance, lateralPan, frontalPan, pans; 
		
//		vertexPositionDistance = this.calculateDistance( vertexXY, positionXY );
		vertexProjectionDistance = this.calculateDistance( vertexXY, 				projectionXY );
		positionProjectionDistance = this.calculateDistance( positionXY, 				projectionXY );
		// pan is returned as abs, so that positive cases are not included in setGeneralSign
		
		lateralPan = abs( positionProjectionDistance / vertexPositionDistance );
		frontalPan = abs( vertexProjectionDistance / vertexPositionDistance );		
		// case: vertexXY == positionXY
		if ( vertexPositionDistance == 0, { lateralPan = 0; frontalPan = 0});
		^[ lateralPan, frontalPan ];
		}	
	
	setSign { arg pans;
		
		var 	x, y, oldX, oldY;
			
		// handful
		x = this.positionXY[0];
		y = this.positionXY[1];
		oldX = this.oldPositionXY[0]; 
		oldY = this.oldPositionXY[1]; 

		case 
			{ x == oldX } { pans = this.setSpecialSignVertical( pans ) }
			{ y == oldY } { pans = this.setSpecialSignHorizontal( pans )}			{ y != oldY && x!= oldX } { pans = this.setGeneralSign( pans )};		^pans
			}
			
			
	// special case
	setSpecialSignHorizontal { arg pans;
		var 	lateralPan, frontalPan, x, y, oldX, oldY, coeff, orthoCoeff,
			vx, vy, q, orthoQ;
			
		// handful
		lateralPan = pans[0];
		frontalPan = pans[1];
		x = this.positionXY[0];
		y = this.positionXY[1];
		oldX = this.oldPositionXY[0]; 
		oldY = this.oldPositionXY[1]; 
		coeff = this.coefficient;
		orthoCoeff = this.orthogonalCoefficient;
		vx = this.vertexXY[0];
		vy = this.vertexXY[1];
	
		if (vy > y, { lateralPan = lateralPan.neg });  
		if (vx < x, { frontalPan = frontalPan.neg }); 
		if (x < oldX,{ lateralPan = lateralPan.neg; frontalPan = frontalPan.neg });  
		^[ lateralPan, frontalPan ]
		}
	
	// special case
	setSpecialSignVertical { arg pans;
		var 	lateralPan, frontalPan, x, y, oldX, oldY, coeff, orthoCoeff,
			vx, vy, q, orthoQ;
			
		// handful
		lateralPan = pans[0];
		frontalPan = pans[1];
		x = this.positionXY[0];
		y = this.positionXY[1];
		oldX = this.oldPositionXY[0]; 
		oldY = this.oldPositionXY[1]; 
		coeff = this.coefficient;
		orthoCoeff = this.orthogonalCoefficient;
		vx = this.vertexXY[0];
		vy = this.vertexXY[1];
	
		if (vx < x, { lateralPan = lateralPan.neg });  
		if (vy < y, { frontalPan = frontalPan.neg }); 
		if (y < oldY,{ lateralPan = lateralPan.neg; frontalPan = frontalPan.neg });  
		^[ lateralPan, frontalPan ]
		}
	
	// general case
	setGeneralSign { arg pans;
		var 	lateralPan, frontalPan, x, y, oldX, oldY, coeff, orthoCoeff,
			vx, vy, q, orthoQ;
			
		// handful
		lateralPan = pans[0];
		frontalPan = pans[1];
		x = this.positionXY[0];
		y = this.positionXY[1];
		oldX = this.oldPositionXY[0]; 
		oldY = this.oldPositionXY[1]; 
		coeff = this.coefficient;
		orthoCoeff = this.orthogonalCoefficient;
		vx = this.vertexXY[0];
		vy = this.vertexXY[1];
		
		q = y-(coeff*x);                 	// y = mx+q
		orthoQ = y-(orthoCoeff*x);    		// orthoQ: position solves both equation

         if ( vy > (coeff*vx+q), {			// inequation on y = mx+q
         		lateralPan = lateralPan.neg;
         		});
          if ( vy < (orthoCoeff*vx+orthoQ), {	// inequation on y' = (1/m)x+q'
          	frontalPan = frontalPan.neg;
          	});
		if (oldX > x, {					// direction with respect to lat
			lateralPan = lateralPan.neg;
			});
		if (oldY > y, {					// direction with respect to front
			frontalPan = frontalPan.neg;
			}); 
		^[ lateralPan, frontalPan ]
		}

	run { 
		var coeff, vertex, position, old, projection, distance, pans;
	
		vertex = this.vertexXY;
		position = this.positionXY;
		old = this.oldPositionXY;
		
		distance = this.calculateDistance( vertex, position );
		coeff = this.calculatePositionLineCoefficient(position, old);
		projection = this.calculateProjection(coeff, vertex, position);
		pans = this.calculateGeneralPans(vertex, position, projection, distance);
		pans = this.setSign(pans);
		//this.display(vertex, position, old, projection, distance, pans);
		^[ distance, pans ];
		}


  //////////////////////////////////////////////////////////////////////////////////
 // Display method to test: works on a 10x10 space 	                              //
//////////////////////////////////////////////////////////////////////////////////

	display { arg vertexXY, positionXY, oldPositionXY, projectionXY, distance, pans, scale = 40;
	
		var window, infoWindow;
		var pText, oldText, vText, rText, infoText;
		var 	vertexX = vertexXY[0]*scale, vertexY = 10-vertexXY[1]*scale, 
    	   		positionX = positionXY[0]*scale, positionY = 10-positionXY[1]*scale,
    	   		oldPositionX = oldPositionXY[0]*scale, oldPositionY = 10-oldPositionXY[1]*scale,
    	   		projectionX = projectionXY[0]*scale, projectionY = 10-projectionXY[1]*scale;
    	   		
		window = SCWindow("Testing GeoGrapher", Rect(128, 64, 10*scale, 10*scale));
		window.view.background = Color.white;
		window.front;		
		
		infoWindow = SCWindow("Testing GeoGrapher", Rect(128+(10*scale), 64+(7*scale), 10*scale, 3*scale));
		infoWindow.view.background = Color.white;
		infoWindow.front;		
		
		infoText = SCStaticText(infoWindow, Rect(3,3, 300,100));
		infoText.string = format(" p:   %\n p-1:   %\n v:   %\n pro: %\n distance: %\n pan (l, f): %", positionXY, oldPositionXY, vertexXY, projectionXY, distance, pans);
		window.onClose_({infoWindow.close});
		
		pText = SCStaticText(window, Rect(positionX, positionY, 150, 20));
		pText.string = "p";
		oldText = SCStaticText(window, Rect(oldPositionX, oldPositionY, 150, 20));
		oldText.string = "p-1";
		Color(1,0,0).set; // vertex
		vText = SCStaticText(window, Rect(vertexX, vertexY, 150, 20)).stringColor_(Color(1,0,0)); // vertex;
		vText.string = "v";
		rText = SCStaticText(window, Rect(projectionX, projectionY, 150, 20)).stringColor_(Color(0,0,1));
		rText.string = "pro";
		window.drawHook = {
			Pen.use {
				Color(0.0,0.0,0).set; // trajectory
					Pen.fillOval(Rect.new(oldPositionX-3,oldPositionY-3,6,6));
					Pen.fillOval(Rect.new(positionX-3,positionY-3,6,6));
				Color(1,0,0).set; // vertex
					Pen.fillOval(Rect.new(vertexX-3,vertexY-3,6,6));
				Color(0,0,1).set; // projection
					Pen.fillOval(Rect.new(projectionX-3,projectionY-3,6,6));
				
				// oldPosition to position	
				Color(0.0,0.0,0).set; // trajectory
				Pen.width = 0.5;
					Pen.beginPath;
					Pen.moveTo(Point(oldPositionX,oldPositionY));
					Pen.lineTo(Point(positionX,positionY));
					Pen.stroke;
					
				// vertex to position	
				Color(0,0.5,0).set; // trajectory
				Pen.width = 0.5;
					Pen.beginPath;
					Pen.moveTo(Point(vertexX,vertexY));
					Pen.lineTo(Point(positionX,positionY));
					Pen.stroke;
					
				// vertex to projection	
				Color(1,0,0).set; // vertex
				Pen.width = 0.5;
					Pen.beginPath;
					Pen.moveTo(Point(vertexX,vertexY));
					Pen.lineTo(Point(projectionX,projectionY));
					Pen.stroke;
					
				// projection to position	
				Color(0.6,0.6,0.6).set; // vertex
				Pen.width = 0.5;
					Pen.beginPath;
					Pen.moveTo(Point(projectionX,projectionY));
					Pen.lineTo(Point(positionX,positionY));
					Pen.stroke;
						};
				}
			}

}
