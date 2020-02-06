
/*Data structure to contain the Songs & their TFIDF scores*/
public class SearchResult implements Comparable<SearchResult>{
	private Song song = null;
	private double score = -1;
	private int songID = -1;
	
	public SearchResult(Song song, double score, int songID) {
		this.song = song;
		this.score = score;
		this.songID = songID;
	}
	public Song getSong() {return song;}
	public void setSong(Song song) {this.song = song;}
	public double getScore() {return score;}
	public void setScore(double score) {this.score = score;}
	public int getSongID() {return songID;}
	public void setSongID(int songID) {this.songID = songID;}
	@Override
	public String toString() {
		return "[score=" + score + "]"+song+"\n";
	}
	/*Sort TFIDF weights in descending order*/
	@Override
	public int compareTo(SearchResult o) {		
		if(this.score == o.score) return this.song.compareTo(o.song);
		return -1 * Double.compare(this.score,o.score);
	}
}
