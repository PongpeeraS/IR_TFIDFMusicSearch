import java.util.List;

/*Data structure to contain the information of the songs in database*/
public class Song implements Comparable<Song> {
	private int id;
	private String title = null;
	private String artist = null;
	private String rawLyrics = null; //raw lyrics from the text file
	private List<String> titleTokens = null; //tokens after tokenizing title (contains stop words)
	private List<String> lyricTokens = null; //tokens after tokenizing raw lyrics
	
	public Song(int id, String title, String artist, String rawLyrics, List<String> titleTokens, List<String> lyricTokens) {
		this.id = id;
		this.title = title;
		this.artist = artist;
		this.rawLyrics = rawLyrics;
		this.titleTokens = titleTokens;
		this.lyricTokens = lyricTokens;
	}
	public int getId() {return id;}
	public void setId(int id) {this.id = id;}
	public String getTitle() {return title;}
	public void setTitle(String title) {this.title = title;}
	public String getArtist() {return artist;}
	public void setArtist(String artist) {this.artist = artist;}
	public String getRawLyrics() {return rawLyrics;}
	public void setRawLyrics(String rawLyrics) {this.rawLyrics = rawLyrics;}
	public List<String> getLyricTokens() {return lyricTokens;}
	public void setLyricTokens(List<String> tokens) {this.lyricTokens = tokens;}	
	public List<String> getTitleTokens() {return titleTokens;}
	public void setTitleTokens(List<String> titleTokens) {this.titleTokens = titleTokens;}
	@Override
	public String toString(){
		return "\t[Title:"+this.title+", Artist:"+this.artist+"]";
	}
	@Override
	public int compareTo(Song arg0) {
		return this.title.compareTo(arg0.title);
	}

}
