// W. Snoei 2006

// a button with ++ functionality and rounded border

// SCv3.3.1 revision

RoundButton : RoundView2 { 
	
	// requires a version of SuperCollider where String:prBounds is working (after april 2007?)
	
	var <value = 0;
	var <font, <states;
	var <pressed = false;
	var <radius, <border = 2, <>moveWhenPressed = 1;
	var <extrude = true;
	var <inverse = false;
	var <focusColor;
	
	var <expanded = true;
	var <shrinkForFocusRing = false;
	
	var <>textOffset; // not used anymore, still there to prevent code breaking 
	
	// *viewClass { ^SCUserView }
	
	refresh { { super.refresh }.defer }
		
	mouseDown {
		arg x, y, modifiers, buttonNumber, clickCount;
		if( enabled ) {	
			if( x.isNil or: { this.drawBounds.containsPoint(x@y) } )
				{ mouseDownAction.value(this, x, y, modifiers, buttonNumber, clickCount);		       pressed = true; this.refresh; };
			};
		}
	
	mouseUp {arg x, y, modifiers;
		if( pressed == true ) // pressed can never be true if not enabled
			{ mouseUpAction.value(this, x, y, modifiers);
			  pressed = false; 
			  this.valueAction = value + 1; };
		//this.refresh;	
		}
	
	mouseMove { arg x, y, modifiers, buttonNumber, clickCount;
		if( enabled ) {	
			if( this.drawBounds.containsPoint(x@y) )
				{ pressed = true; this.refresh; }
				{ pressed = false; this.refresh; };
			};
		}
	
	radius_ { |newRadius| radius = newRadius; this.refresh; }
	
	focusColor_ { |newColor| focusColor = newColor; this.parent.refresh; }
	
	draw {
		var rect, localRadius;
		var shadeSide, lightSide;

		rect = this.drawBounds;
		
		radius = radius ?? { rect.width.min( rect.height ) / 2 };

		if( this.hasFocus ) // rounded focus rect
			{
			Pen.use({
				Pen.color = focusColor ?? { Color.gray(0.2).alpha_(0.8) };
				Pen.width = 2;
				Pen.roundedRect( rect.insetBy(-2,-2), radius + 1 );
				Pen.stroke;
				});
			};
			
		if( inverse )
			{ lightSide = Color.black.alpha_(0.5);
		       shadeSide = Color.white.alpha_(0.5); }
			{ lightSide = Color.white.alpha_(0.5);
		       shadeSide = Color.black.alpha_(0.5); };
		
			
		states !? {
			
			Pen.use {
							
				states[ value ][ 2 ] !? {
					Pen.roundedRect( rect, radius );
					states[ value ][ 2 ].fill( rect ); // requires Gradient:fill method
					};				
					
					
				if( extrude )
					{ Pen.extrudedRect( rect, radius, border, 0.17pi, pressed,
						[ lightSide, shadeSide ] ); }
					{   if( pressed, { Pen.color = lightSide }, { Pen.color = shadeSide } ); 
					   Pen.width = border;
					   Pen.roundedRect( rect.insetBy( border/2,border/2 ), radius - 
					   	(border/2)  ).stroke; 
					};							
			};
			
		case { states[value][0].isString }
			{	
				Pen.font = font ? Font.default;
				Pen.color = states[value][1] ? Color.black;
				Pen.stringCenteredIn( states[value][0], 
					rect  + ( if( pressed ) 
						{ Rect( moveWhenPressed, moveWhenPressed, 0, 0 ) } 
						{ Rect(0,0,0,0) } )
					);
				
			} 
			{ states[value][0].class == Symbol }
			{
			Pen.use {
				Pen.color_(states[value][1] ? Color.black);
				if( pressed ) { Pen.translate( moveWhenPressed, moveWhenPressed ) };
				DrawIcon.symbolArgs( states[value][0], rect.insetBy( border/2,border/2 ) );
				};
			 }
			{ states[value][0].class.name == Image.implClass.name }
			{
			Pen.use {
				if( pressed ) { Pen.translate( moveWhenPressed, moveWhenPressed ) };
				states[ value ][0].drawAtPoint( rect.center - 
					((states[ value ][0].width/2)@(states[ value ][0].height/2)) );
				};
			 }
			{ true }
			{
			Pen.use {
				Pen.color_(states[value][1] ? Color.black);
				if( pressed ) { Pen.translate( moveWhenPressed, moveWhenPressed ) };
				states[value][0].value( this, rect, radius ); // can be a Pen function
				};
			};
			};
			
		if( enabled.not )
			{
			Pen.use {
				Pen.fillColor = Color.white.alpha_(0.5);
				Pen.roundedRect( rect, radius ).fill;
				};
			};
		}
		
	*paletteExample { arg parent, bounds;
		var v;
		v = this.new(parent, bounds);
		v.states = [
			["Push", Color.black, Color.red],
			["Pop", Color.white, Color.blue]];
		^v
	}
	
	states_ { |inStates| states = inStates; this.refresh; }

	value_ { arg val;
		value = val % states.size;
		this.refresh;
	}

	valueAction_ { arg val; // changed 08/03/08
		if( val.round(1) != value )
			{ value = val % states.size;
				this.doAction;
				this.refresh;
			};
	}	

	font_ { |newFont| font = newFont; this.refresh; }

	extrude_ { |bool| extrude = bool; this.refresh; }
	
	// same as extrude
	bevel { ^extrude }
	bevel_ { |bool| extrude = bool; this.refresh; }
	
	border_ { |newBorder| border = newBorder; this.refresh; }
	
	
	inverse_ { |bool| inverse = bool; this.refresh; }
	
	
	doAction {
		if( action.size > 0 ) // if action is in fact an array; couple states to actions
			{ action.wrapAt( this.value.asInt ).value( this ); }
			{ action.value( this ); };
	}
	
	defaultKeyDownAction { arg char, modifiers, unicode;
		if( [ $ , $\r, $\n, 3.asAscii ].includes( char ) )
			{ this.value = this.value + 1; this.doAction; ^this }; // also trigger single-state
		/*
		if (char == $ , { this.value = this.value + 1; this.doAction; ^this });
		if (char == $\r, { this.value = this.value + 1; this.doAction; ^this });
		if (char == $\n, { this.value = this.value + 1; this.doAction; ^this });
		if (char == 3.asAscii, { this.valueAction = this.value + 1; ^this });
		*/
		^nil		// bubble if it's an invalid key
	}
	
	defaultGetDrag { 
		^this.value
	}
	
	defaultCanReceiveDrag {
		^View.currentDrag.isNumber or: { View.currentDrag.isKindOf(Function) };
	}
	defaultReceiveDrag {
		if (View.currentDrag.isNumber) {
			this.valueAction = View.currentDrag;
		}{
			this.action = View.currentDrag;
		};
	}
}


	
SmoothButton : RoundButton {
	
	init { |parent, bounds|
		super.init( parent, bounds );
		extrude = false;
		moveWhenPressed = 0;
		}
	
	}