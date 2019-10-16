package ToyTest;

public class A  {
	
	public B( BufferedImage iAnimation, float x, float y, float width, float height, int tiles, long time ) {
		
		this.tiles = tiles;
		this.time = time;
		
		this.init();
	}
	
	public void init() {	
		
		this.frame = 0;
		this.curTime = 0;
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


	public void think( int time ) {
		this.curTime += time;
		while ( this.getCurTime() >= this.getTime() ) {
			this.curTime -= this.time;
			this.frame += 1;
			if ( this.getFrame() >= this.getTiles() ) {
				this.frame = 0;
			}
		}
	}

	
	
	
}
