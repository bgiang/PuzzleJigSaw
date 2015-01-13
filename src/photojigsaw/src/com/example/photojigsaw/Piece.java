package com.example.photojigsaw;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;

public class Piece 
{
	//when a piece is placed correctly on the board, then it is disabled, it does not move anymore
	public boolean disabled = false;

	public Bitmap toShow = null; //this is the image that is shown to the user
	public BitmapDrawable imageDrawable = null; //keeps track of where the image is on the screen
	public Rect targetBounds; //where the image is supposed to end up
	
	public Piece (Bitmap image)
	{
		this.toShow = image;
	}
}