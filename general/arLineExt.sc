
+ Pen {

	*arLine { arg p1, p2 ;
		var l, q, k = 10 ;
	   	l = sqrt(((p2.x-p1.x)**2) + ((p2.y-p1.y)**2)) ;
	    	case 	{ p2.y < p1.y } 
	    			{ q = asin((p2.x-p1.x)/l) + (pi*3*0.5) } 
	    		{ p2.y > p1.y }
	        		{ q = asin((p2.x-p1.x)/l).neg + (pi*0.5) }
	   		{ p2.y == p1.y }
	        		{ q = 0 } ;
	    	this.push ;
	    	this.translate(p1.x, p1.y) ;
	    	this.rotate(q, 0, 0) ;
	    	this.moveTo(l @ 0) ;
	    	this.lineTo((l-k) @ (-5)) ;
	    	this.lineTo((l-k) @ 5) ;
	    	this.lineTo(l @ 0) ;
	    	this.pop ;
	    	this.fill ;
		this.line(p1, p2)
	    } 
   
 }