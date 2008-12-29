
EZNumberSC : EZGui{
	var <numberView, <unitView, <>controlSpec, 
		 <>action,<value, popUp=false, numSize,numberWidth,unitWidth, gap, gap2;
	var <>round = 0.001;
	
	*new { arg parent, bounds, label, controlSpec, action, initVal, 
			initAction=false, labelWidth=80, numberWidth=40, 
			unitWidth=0, labelHeight=20,  layout=\horz, gap;
			
		^super.new.init(parent, bounds, label, controlSpec, action, 
			initVal, initAction, labelWidth, numberWidth, 
				unitWidth, labelHeight, layout, gap)
	}
	
	init { arg parentView, bounds, label, argControlSpec, argAction, initVal, 
			initAction, labelWidth, argNumberWidth,argUnitWidth, 
			labelHeight, argLayout, argGap;
			
		var labelBounds, numBounds,w, winBounds, 
				viewBounds, unitBounds;
						
		// try to use the paretn decorator gap
		var	decorator = parentView.asView.tryPerform(\decorator);
		argGap.isNil.if{ 
			gap = decorator.tryPerform(\gap);
			gap = gap ? (2@2)}
			{gap=argGap};
		
		unitWidth = argUnitWidth;
		numberWidth = argNumberWidth;
		layout=argLayout;
		
		// pop up window
		parentView.isNil.if{
			popUp=true;
				// if bounds is a point the place the window on screen
				bounds.isNil.if {bounds= 160@20};
				if (bounds.class==Point)
					{ bounds = bounds.x@max(bounds.y,bounds.y+24);// window minimum height;
					 winBounds=Rect(200, Window.screenBounds.height-bounds.y-100,
								bounds.x,bounds.y)
					}{// window minimum height;
					winBounds = bounds.height_(max(bounds.height,bounds.height+24))
					};
				w = GUI.window.new("",winBounds).alwaysOnTop_(alwaysOnTop);
				parentView=w.asView;
				w.front;
				bounds=bounds.asRect;
				// If numberWidth is set, then adjust the parent view size  both subviews
				//numberWidth.notNil.if{ bounds=bounds.width_(labelWidth+gap.x+numberWidth)};
				// inset the bounds to make a nice margin
				bounds=Rect(4,4,bounds.width-8,bounds.height-24);
				view=GUI.compositeView.new(parentView,bounds)
					.relativeOrigin_(true).resize_(2);
			}{
			bounds.isNil.if{bounds= 160@20};
			bounds=bounds.asRect;
				// If numberWidth is set, then adjust the parent view size  both subviews
				//numberWidth.notNil.if{ bounds=bounds.width_(labelWidth+gap.x+numberWidth)};
			view=GUI.compositeView.new(parentView,bounds).relativeOrigin_(true);
		};
		
		labelSize=labelWidth@labelHeight;
		numSize = numberWidth@labelHeight;
		
		// calcualate bounds
		# labelBounds,numBounds, unitBounds 
				= this.prSubViewBounds(bounds, label.notNil, unitWidth>0);

		label.notNil.if{ //only add a label if desired
				labelView = GUI.staticText.new(view, labelBounds);
			if (layout==\line2)
				{labelView.align = \left;}
				{labelView.align = \right;};
			labelView.string = label;
		};

		(unitWidth>0).if{ //only add a unitLabel if desired
			unitView = GUI.staticText.new(view, unitBounds);
		};

		controlSpec = argControlSpec.asSpec;
		(unitWidth>0).if{ unitView.string = " "++controlSpec.units.asString};
		initVal = initVal ? controlSpec.default;
		action = argAction;
		
		numberView = GUI.numberBox.new(view, numBounds).resize_(2);
		numberView.action = {
			this.valueAction_(numberView.value);
		};
		
		if (initAction) {
			this.valueAction = initVal;
		}{
			this.value = initVal;
		};
		
		this.prSetViewParams;
	}
	
	value_{ arg val; 
		value = controlSpec.constrain(val);
		numberView.value = value.round(round);
	}
	valueAction_ { arg val; 
		this.value_(val);
		this.doAction;
	}
	doAction { action.value(this) }

	set { arg label, spec, argAction, initVal, initAction=false;
		labelView.notNil.if{labelView.string=label};
		controlSpec = spec.asSpec;
		action = argAction;
		initVal = initVal ? controlSpec.default;
		if (initAction) {
			this.value = initVal;
		}{
			value = initVal;
			numberView.value = value.round(round);
		};
	}
			
	enabled {  ^numberView.enabled } 
	enabled_ { |bool| numberView.enabled_(bool) }
	
	
	prSetViewParams{
	
		switch (layout,
		\line2, {
			labelView.notNil.if{labelView.resize_(2).align_(\left)};
			unitView.notNil.if{unitView.resize_(6).align_(\left)};
			numberView.resize_(5);
		},
		\horz, {
			labelView.notNil.if{
				labelView.resize_(4).align_(\right);
				numberView.resize_(5);
				unitView.notNil.if{unitView.resize_(6)};
			}{
				unitView.notNil.if{	unitView.resize_(6)};
				numberView.resize_(5);
			};
		});
			popUp.if{view.resize_(2)};
	}
	
	prSubViewBounds{arg rect, hasLabel, hasUnit;
		var numBounds,labelBounds,sliderBounds, unitBounds, gap1, gap2, numY;
		gap1 = gap;	
		gap2 = gap1;
		hasLabel.not.if{ gap1 = 0@0; labelSize=0@0};
		hasUnit.not.if{gap2 = 0@0};
		
		switch (layout,
			\line2, {
			
				labelBounds = Rect(
						0,
						0,
						rect.width, 
						labelSize.y;
						);
						
				numSize.y=numSize.y-gap1.y;
				numY=labelBounds.height+gap1.y;
				unitBounds = Rect( view.bounds.width - unitWidth,
					numY, unitWidth, numSize.y);
				numBounds = Rect(0, numY, 
					rect.width-unitWidth-gap2.x, numSize.y); // view to right
					
				},
							
			 \horz, {
				labelSize.y=view.bounds.height;
				labelBounds = (labelSize.x@labelSize.y).asRect;
				unitBounds = (unitWidth@labelSize.y).asRect.left_(rect.width-unitWidth);
				numBounds  =  Rect(
					labelBounds.width+gap1.x,
					0,
					rect.width - labelBounds.width - unitBounds.width - gap1.x - gap2.x , 
					labelBounds.height
					);
		});
		
		
		^[labelBounds, numBounds, unitBounds]
	}
	
	setColors{ arg stringBackground, strColor,boxColor,boxStringColor,
			 boxNormalColor, boxTypingColor, background ;
			
			stringBackground.notNil.if{
				labelView.notNil.if{labelView.background_(stringBackground)};
				unitView.notNil.if{unitView.background_(stringBackground)};};
			strColor.notNil.if{	
				labelView.notNil.if{labelView.stringColor_(strColor)};
				unitView.notNil.if{unitView.stringColor_(strColor)};};
			boxColor.notNil.if{		
				numberView.boxColor_(boxColor);	};
			boxNormalColor.notNil.if{	
				numberView.normalColor_(boxNormalColor);};
				
			boxTypingColor.notNil.if{	
				numberView.typingColor_(boxTypingColor);};
			boxStringColor.notNil.if{	
				numberView.stringColor_(boxStringColor);};
			background.notNil.if{	
				view.background=background;};
	}


	font_{ arg font;

			labelView.notNil.if{labelView.font=font};
			unitView.notNil.if{unitView.font=font};
			numberView.font=font;
	}
	
	

}

