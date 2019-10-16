package ToyTest;


public class A  {
	
	public A(BufferedImage iAnimation, float x, float y, float width, float height, int tiles, long time, int maxDirections, boolean bRGB) {
		
		
		this.setTiles(tiles);
		this.setTime(time);
		this.setBLoop(true);
		this.setBAnimation(true);
		
		this.bRGB = bRGB;
		this.maxDirection = maxDirections;
		
		
	}
	
	

	public int getDirection() {
		return this.direction;
	}

		this.direction = direction;
	}


	public int getTiles() {
		return this.tiles;
	}


	public void setTiles(int tiles) {
		this.tiles = tiles;
	}

	
	public int getFrame() {
		return this.frame;
	}

	
	public void setFrame(int frame) {
		this.frame = frame;
	}

	
	public long getCurTime() {
		return this.curTime;
	}


	public void setCurTime(long curTime) {
		this.curTime = curTime;
	}

	public long getTime() {
		return time;
	}


	public void setTime(long time) {
		this.time = time;
	}

	
	public boolean isBLoop() {
		return this.bLoop;
	}


	public void setBLoop(boolean bLoop) {
		this.bLoop = bLoop;
	}

	public boolean isBAnimation() {
		return this.bAnimation;
	}


	public void setBAnimation(boolean bAnimation) {
		this.bAnimation = bAnimation;
	}


	public void think(int time) {
		if (this.isBAnimation()) {
			this.setCurTime(this.getCurTime() + time);
			while ( this.getCurTime() >= this.getTime() ) {
				this.setCurTime(this.getCurTime() - this.getTime());
				this.setFrame(this.getFrame() + 1);
				if ( this.getFrame() >= this.getTiles() ) {
					this.setFrame(0);
					if (!this.isBLoop()) {
						this.setBAnimation(false);
					}
				}
			}
		}
	}


	
	
}
