package com.nesl.ucla.snakefruit;

import java.util.ArrayList;

import android.util.Log;

public abstract class Snake {
	private static final String TAG = MainGamePanel.class.getSimpleName();
	
	private final int INITIAL_GROWTH = 10;
	
	protected ArrayList<Hexagon> snakeBody;
	
	private int nrToGrow;
	private int oriDirection;
	protected int curDirection;
	protected int targetDirection;
	protected int color;
	
	private int punishment = 0;
	private FruitDeliver fruitDeliver;
	
	public Snake(int initRow, int initCol, int initDir, int initColor, FruitDeliver _fruitDeliver) {
		//initialize
		fruitDeliver = _fruitDeliver;
		
		//snake grows RIGHT for initial 5 stages
		nrToGrow = INITIAL_GROWTH - 1;
		oriDirection = initDir;
		curDirection = initDir;
		targetDirection = initDir;
		color = initColor;
		
		//make initial head
		snakeBody = new ArrayList<Hexagon>();
		snakeBody.add(0, GamePlanner.field[initRow][initCol]);
		GamePlanner.field[initRow][initCol].updateType(HexagonType.HEAD);
		GamePlanner.field[initRow][initCol].updateColor(color);
		GamePlanner.field[initRow][initCol].updateDir(curDirection);
	}
	
	// call from GamePlanner, to move or do something in it
	public void trigger() {
		// hello
		move();
			
	}

	//This function takes the number to grow and number to cut into consideration
	public void move() {
		if(snakeBody.size() > 0) {
			makeFinalDecision();
			preferTargetDirection();
			int prevHeadRow = snakeBody.get(0).getRow();
			int prevHeadCol = snakeBody.get(0).getCol();
			int newHeadRow = prevHeadRow + Utils.DIR_2_R[curDirection];
			int newHeadCol = prevHeadCol + Utils.DIR_2_C[curDirection];
			//Log.i(TAG, "dir=" + curDirection + " :: " + prevHeadRow + ", " + prevHeadCol + " -> " + newHeadRow + ", " + newHeadCol);
			Hexagon nextHex = GamePlanner.field[newHeadRow][newHeadCol];
			if (nextHex.getType() == HexagonType.FRUIT) {
				nrToGrow += 3;
				fruitDeliver.eatFruit(newHeadRow, newHeadCol);
				addHead(prevHeadRow, prevHeadCol, newHeadRow, newHeadCol);
			}
			else if (nextHex.getType() == HexagonType.EMPTY) {
				addHead(prevHeadRow, prevHeadCol, newHeadRow, newHeadCol);
			}
			else {
				Log.i(TAG, "removeHead");
				removeHead();
			}
		}
		if (snakeBody.size() > 0) {
			//if in initial growth stage, do not remove tail
			if(nrToGrow > 0){
				nrToGrow--;
			}
			//else remove the tail
			else{
				removeTail();
			}
		}
		//Log.i(TAG, "size: " + snakeBody.size());
	}
	
	abstract protected void makeFinalDecision();
	
	private void addHead(int pr, int pc, int nr, int nc){
		GamePlanner.field[pr][pc].updateType(HexagonType.BODY);
		GamePlanner.field[pr][pc].updateColor(color);
		GamePlanner.field[nr][nc].updateType(HexagonType.HEAD);
		GamePlanner.field[nr][nc].updateColor(color);
		GamePlanner.field[nr][nc].updateDir(curDirection);
		snakeBody.add(0, GamePlanner.field[nr][nc]);
		oriDirection = curDirection;
		preferTargetDirection();
	}
	
	private void removeTail(){
		int row;
		int col;
		
		//remove the tail from the snakeBody
		//get the coordinates of the tail
		row = snakeBody.get(snakeBody.size()-1).getRow();
		col = snakeBody.get(snakeBody.size()-1).getCol();	
		
		//update the snakeBody
		snakeBody.remove(snakeBody.size()-1);
		
		//update the field HexagonType to EMPTY accordingly
		GamePlanner.field[row][col].updateType(HexagonType.EMPTY);
	}
	
	private void removeHead() {
		int nrToCut = (snakeBody.size() + 1) / 2 + punishment;
		int remaining = snakeBody.size() - nrToCut;
		if (remaining < 3)
			remaining = 0;
		
		while (snakeBody.size() > remaining) {
			int row = snakeBody.get(0).getRow();
			int col = snakeBody.get(0).getCol();
			GamePlanner.field[row][col].updateType(HexagonType.EMPTY);
			snakeBody.remove(0);
		}
		if (snakeBody.size() > 0) {
			int row0 = snakeBody.get(0).getRow();
			int col0 = snakeBody.get(0).getCol();
			int row1 = snakeBody.get(1).getRow();
			int col1 = snakeBody.get(1).getCol();
			curDirection = Utils.rc_2_dir(row0 - row1, col0 - col1);
			oriDirection = curDirection;
			targetDirection = curDirection;
			GamePlanner.field[row0][col0].updateType(HexagonType.HEAD);
			GamePlanner.field[row0][col0].updateColor(color);
			GamePlanner.field[row0][col0].updateDir(curDirection);
		}
		punishment++;
	}
	
	
	
	protected void preferTargetDirection() {
		int dd = targetDirection - oriDirection;
		if (dd < -3)
			dd += 6;
		if (dd > 3)
			dd -= 6;
		if (dd != -3 && dd != 3) {
			if (dd < 0)
				curDirection = (oriDirection + 5) % 6;
			else if (dd > 0)
				curDirection = (oriDirection + 1) % 6;
		}
	}
		
	public void setNrToGrow(int num){
		nrToGrow = num;
	}
}
