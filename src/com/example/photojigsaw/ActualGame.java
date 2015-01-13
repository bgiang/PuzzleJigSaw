package com.example.photojigsaw;

import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

public class ActualGame extends SurfaceView implements SurfaceHolder.Callback
{
	//the thread that will do the drawing
	private Thread mDrawingThread;
	private final SurfaceHolder mSurfaceHolder;
    
	//the maximum piece size and locking will make sure that the user does not
	//have to put the piece in the exact spot
    private int maxPieceSize;
    private static int locking = 2;
    
    //this will hold the length of each side of the piece
    private int lengthOfSide = -1;
    //the spot where the draw the box (so that it will be in the center)
    private int beginX = -1, beginY = -1;
    
    //the size of the screen
    private int heightOfScreen;
    private int widthOfScreen;
    
    //holds all puzzle pieces in the current puzzle section
    private Piece [][] allPuzzlePieces;
    private Piece currentPiece = null;

    //this will hold the painter
    private Paint painter;
    private Context context; // used when generating a BitmapDrawable
    
    //will check if the user has completed the puzzle
    public boolean completed = false; 

    public ActualGame(Context context, int height, int width) 
    {
    	super(context);
    	
    	this.context = context;
    	this.heightOfScreen = height;
    	this.widthOfScreen = width;
    	
    	//the size of the piece will depend on the screen size
    	this.maxPieceSize = (int) Math.min(heightOfScreen, widthOfScreen)/15;
    	
    	mSurfaceHolder = getHolder();
		mSurfaceHolder.addCallback(this);

		painter = new Paint();
    }


    public void setPuzzle(Piece[][] puzzle) 
    {
        Random r = new Random();

        this.allPuzzlePieces = puzzle;
        
        this.lengthOfSide = maxPieceSize*(allPuzzlePieces.length);
        beginX = this.widthOfScreen/2 - lengthOfSide/2;
        beginY = this.heightOfScreen/2 - lengthOfSide/2;

        for (int x = 0; x < allPuzzlePieces.length; x++) 
        {
        	for (int y = 0; y < allPuzzlePieces[x].length; y++)
        	{
        		//grabbing a piece
        		//generating a BitmapDrawable to make the piece movable
        		Piece curPiece = allPuzzlePieces[x][y];
        		curPiece.imageDrawable = new BitmapDrawable (context.getResources(), curPiece.toShow);
        		
        		//The topleft-hand corner of the piece
        		int topLeftX = r.nextInt(widthOfScreen - maxPieceSize);
                int topLeftY = r.nextInt(heightOfScreen - 2*maxPieceSize);

                //setting the bounds for the drawable
                curPiece.imageDrawable.setBounds(topLeftX, topLeftY, topLeftX + maxPieceSize, topLeftY + maxPieceSize);
                
                //making a rectangle that will hold the spot where the piece has to be placed for it to be valid
                curPiece.targetBounds = new Rect(beginX + locking + x*maxPieceSize, beginY + locking + y*maxPieceSize, beginX + locking + x*maxPieceSize + maxPieceSize, beginY + locking + y*maxPieceSize + maxPieceSize);
        	}
        }
    }

    protected void redrawCanv(Canvas canvas) 
    {
    	if (canvas == null)
    		return;
    	
    	//tan background color
        canvas.drawColor(Color.argb(150, 242, 242, 218));
        
        //This will define the rectangle in the middle where 
        //all of the pieces will go
        if (this.beginX > 0 && this.beginY > 0 && this.lengthOfSide > 0)
        {
        	painter.setColor(Color.BLACK);
        	painter.setStrokeWidth(2);
        	painter.setStyle(Paint.Style.STROKE);

	        canvas.drawRect(beginX, beginY, beginX+lengthOfSide+locking, beginY+lengthOfSide+locking, painter);
        }

        //redrawing all of the pieces on the current canvas
        for (int x = 0; x < allPuzzlePieces.length; x++) 
        {
        	for (int y = 0; y < allPuzzlePieces[x].length; y++)
        		allPuzzlePieces[x][y].imageDrawable.draw(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) 
    {
        int xPos =(int) event.getX();
        int yPos =(int) event.getY();

        switch (event.getAction()) 
        {
            case MotionEvent.ACTION_DOWN:
            	//finding if the current piece is picked up or just the screen is rouched
                for (int x = 0; x < allPuzzlePieces.length; x++) 
                {
                	for (int y = 0; y < allPuzzlePieces[x].length; y++)
                	{
                		Piece curPiece = allPuzzlePieces[x][y];
                		Rect pieceLoc = curPiece.imageDrawable.getBounds();
                		
                		if (pieceLoc.contains(xPos, yPos) && !curPiece.disabled)
                		{
                			currentPiece = curPiece;
                			break;
                		}
                	}
                }
                break;
            case MotionEvent.ACTION_MOVE:
            	//if the actual piece is picked up and it is not locked in place, then move it
                if (currentPiece != null && !currentPiece.disabled) 
                {
                	//if the potential boundarie contain the current piece, then put the piece there and lock it. 
                	if (currentPiece.targetBounds.contains(xPos, yPos))
                	{
                		currentPiece.imageDrawable.setBounds(currentPiece.targetBounds);
                		currentPiece.disabled = true;
                	}
                	//otherwise just move the piece, updating its current center
                	//to be in the spot of the event
                	else
                	{
                		 Rect currentRect = currentPiece.imageDrawable.copyBounds();

                		 currentRect.left = xPos - maxPieceSize/2;
                		 currentRect.top = yPos - maxPieceSize/2;
                		 currentRect.right = xPos + maxPieceSize/2;
                		 currentRect.bottom = yPos + maxPieceSize/2;
                         currentPiece.imageDrawable.setBounds(currentRect);
                	}
                	
                	//redraw the canvas after the piece was moved or locked
                	Canvas tempCanv = mSurfaceHolder.lockCanvas();
                	redrawCanv(tempCanv);
                	mSurfaceHolder.unlockCanvasAndPost(tempCanv);
                	
                	//since we can be potentially locking the last piece
                	//see if the puzzle was completed
                	boolean complete = true;
                	
                	for (int x = 0; x < allPuzzlePieces.length; x++)
                	{
                		for (int y = 0; y < allPuzzlePieces[x].length; y++)
                		{
                			if (allPuzzlePieces[x][y].disabled == false)
                				complete = false;
                		}
                	}
                	
                	if (complete)
                	{
                		Toast.makeText(context, "You have completed the current puzzle", Toast.LENGTH_LONG).show();
                		//should probably return something here
                		Intent intent=new Intent();
                		intent.putExtra("COMPLETE", true);
                		((Activity) this.context).setResult(Activity.RESULT_OK,intent);
                		((Activity) this.context).finish();
                	}
                }
                break;
            case MotionEvent.ACTION_UP:
            	//making sure that the no piece is picked up
                currentPiece = null;
                break;

        }


        return true;
    }

    //the 3 functions below were taken from Adam Porter's Coursera examples page
	@Override
	public void surfaceCreated(SurfaceHolder holder) 
	{
		mDrawingThread = new Thread(new Runnable() 
		{
			public void run() {
				Canvas canvas = null;
				while (!Thread.currentThread().isInterrupted()) 
				{
					canvas = mSurfaceHolder.lockCanvas();
					if (null != canvas) 
					{
						redrawCanv(canvas);
						mSurfaceHolder.unlockCanvasAndPost(canvas);
					}
				}
			}
		});
		mDrawingThread.start();
		
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) 
	{
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) 
	{
		if (null != mDrawingThread)
			mDrawingThread.interrupt();		
	}
}