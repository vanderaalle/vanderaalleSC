/*

+ JPen {

   *dottedLine { arg p1, p2, pointsPerUnit = 0.15, w = 1, h = 1, bpp = 1;
       // if bpp == 1, points are drawn as ovals
       // if bpp == 0, points are drawn as rectangles
       // values in between 0 and 1 are the probability of a point being drawn as a rectangle or an oval
       // ... 0.3 is a 30% chance of an oval ... 0.75 is a 75% chance of an oval
       var x1 = p1.x; var y1 = p1.y; var x2 = p2.x; var y2 = p2.y ;
       var length = ((x2-x1).squared + (y2 - y1).squared).sqrt;
       var density = pointsPerUnit * length;
       var xIncr = (x2-x1)/density;
       var yIncr = (y2-y1)/density;
       var basicPointPrim;
             (density+1).do({ arg i ;
                 if( bpp.coin, {
               this.fillOval(Rect(
                       xIncr * i + x1,
                       yIncr * i + y1,
                       w,
                       h
                       )
                   )
           },{
               this.fillRect(
               		Rect(
                       xIncr * i + x1,
                       yIncr * i + y1,
                       w,
                       h
                       )
                   )
           });
       })
   }

//
	*texture { arg rect, pointsPerUnit = 0.15, w = 1, h = 1, bpp = 1 ;
		var totalPoints = (rect.width * rect.height)*pointsPerUnit ;
		var x = rect.left, y = rect.top ;
		totalPoints.do({ arg i ;
                 if( bpp.coin, {
               this.fillOval(Rect(
               	rrand(x, x + rect.width-w),
               	rrand(y, y + rect.height-h),
                       	w,
                       h
                       )
                   )
           },{
               this.fillRect(Rect(
               	rrand(x, x + rect.width-w),
               	rrand(y, y + rect.height-h),
                       	w,
                       h
                       )
                   )

		})

	})
	}
}

*/

+ Pen {

   *dottedLine { arg p1, p2, pointsPerUnit = 0.15, w = 1, h = 1, bpp = 1;
       // if bpp == 1, points are drawn as ovals
       // if bpp == 0, points are drawn as rectangles
       // values in between 0 and 1 are the probability of a point being drawn as a rectangle or an oval
       // ... 0.3 is a 30% chance of an oval ... 0.75 is a 75% chance of an oval
       var x1 = p1.x; var y1 = p1.y; var x2 = p2.x; var y2 = p2.y ;
       var length = ((x2-x1).squared + (y2 - y1).squared).sqrt;
       var density = pointsPerUnit * length;
       var xIncr = (x2-x1)/density;
       var yIncr = (y2-y1)/density;
       var basicPointPrim;
             (density+1).do({ arg i ;
                 if( bpp.coin, {
               this.fillOval(Rect(
                       xIncr * i + x1,
                       yIncr * i + y1,
                       w,
                       h
                       )
                   )
           },{
               this.fillRect(
               		Rect(
                       xIncr * i + x1,
                       yIncr * i + y1,
                       w,
                       h
                       )
                   )
           });
       })
   }


//
	*texture { arg rect, pointsPerUnit = 0.15, w = 1, h = 1, bpp = 1 ;
		var totalPoints = (rect.width * rect.height)*pointsPerUnit ;
		var x = rect.left, y = rect.top ;
		totalPoints.do({ arg i ;
                 if( bpp.coin, {
               this.fillOval(Rect(
               	rrand(x, x + rect.width-w),
               	rrand(y, y + rect.height-h),
                       	w,
                       h
                       )
                   )
           },{
               this.fillRect(Rect(
               	rrand(x + w, x + rect.width-w),
               	rrand(y + h, y + rect.height-h),
                       	w,
                       h
                       )
                   )

		})

	})
	}


}

