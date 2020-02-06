import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/*Method to calculated TFIDF weights & conduct search*/
public class TFIDFSearcher {
	private List<Song> songs; //Corpus of songs
	private Map<Integer, HashMap<String, Double>> titleWeights; //Map between song Ids & their titles' TFIDF weights
	private Map<String, HashMap<Integer, Double>> titleTF; //Map between titles' terms & their TF values
	private Map<String, Double> titleIDF; //Map between titles' terms & their IDF values
	private Map<Integer, HashMap<String, Double>> lyricWeights; //Map between song Ids & their lyrics' TFIDF weights
	private Map<String, HashMap<Integer, Double>> lyricsTF; //Map between lyrics' terms & their TF values
	private Map<String, Double> lyricsIDF; //Map between lyrics' terms & their TF values
	
	/*Constructor:	-Parse songs into memory
	 * 				-Initialize weight components
	 * 				-Calculate Songs' TFIDF weights*/
	public TFIDFSearcher(String docFilename){
		songs = parseDocumentFromFile(docFilename);
		lyricWeights = new HashMap<Integer,HashMap<String,Double>>();
		lyricsTF = new HashMap<String, HashMap<Integer, Double>>();
		lyricsIDF = new HashMap<String, Double>();
		titleWeights = new HashMap<Integer,HashMap<String,Double>>();
		titleTF = new HashMap<String, HashMap<Integer, Double>>();
		titleIDF = new HashMap<String, Double>();
		calculateCorpusTFIDF();
	}
	
	/*Method to load raw songs into Song objects in memory*/
	private List<Song> parseDocumentFromFile(String filename){
		List<Song> songsList = new Vector<Song>();
		Reader in = null;
		CSVParser parser = null;
		int id=1;
		try {
			in = new FileReader(filename);
			//in = new InputStreamReader(new FileInputStream(filename), "UTF-8");
			/*Using Commons CSV external JAR, create a new parser object to read the songdata.csv file
			 * in Excel format, first record as header (ignore letter case) with trimming */
			parser = new CSVParser(in, CSVFormat.EXCEL.withFirstRecordAsHeader().withIgnoreHeaderCase().withTrim());
			for (CSVRecord record : parser) {
			    String artist = record.get("artist");
			    String title = record.get("song");
			    String rawText = record.get("text");
			    Song song = new Song(id,title,artist,rawText,tokenize(title),tokenize(rawText));
			    songsList.add(song);	id++;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Songs initialization from "+filename+" complete with "+songsList.size()+" songs.");
		return songsList;
	}
	
	/*Method to preprocess and tokenize raw texts in each Song*/
	public static List<String> tokenize(String rawText){
		String text = rawText.toLowerCase();		
		text = text.replaceAll("[^a-zA-Z0-9]", " "); //Removing noises
		String[] tokenArray = text.split("\\s+");	
		//Cleaning individual characters, and stemming via Porter Stemmer (external JAR)
		List<String> tokens = new Vector<String>();
		for(String t: tokenArray){
			if(t.length() <= 1) continue;
			//t = porterStemmer.stem(t);
			tokens.add(t);
		}
		return tokens;
	}
	
	/*Method to calculate the Songs' TFIDF weights into memory*/
	private void calculateCorpusTFIDF() {
		for(Song song: songs) {
			/*1.1 Getting Freq(d,q) for titles whether if:
			 * -titleTF doesn't have this term -> new HashMap created
			 * -titleTF has this term, but in other song -> new HashMap record
			 * -titleTF has this term, same song -> (term -> <docID, Freq+1>) */
			List<String> titleTokens = song.getTitleTokens();
			for(String term: titleTokens) {
				if(!titleTF.containsKey(term)) {
					titleTF.put(term, new HashMap<Integer,Double>());
					titleTF.get(term).put(song.getId(), 1.0);
				}
				else {
					if(!titleTF.get(term).containsKey(song.getId())) 
						titleTF.get(term).put(song.getId(), 1.0);
					else 
						titleTF.get(term).replace(song.getId(), titleTF.get(term).get(song.getId()) + 1.0);
				}
			}
			/*1.2 Getting Freq(d,q) for lyrics, conditions same as above for lyricsTF*/
			List<String> lyricTokens = song.getLyricTokens();
			for(String term: lyricTokens) {
				if(!lyricsTF.containsKey(term)) {
					lyricsTF.put(term, new HashMap<Integer,Double>());
					lyricsTF.get(term).put(song.getId(), 1.0);
				}
				else {
					if(!lyricsTF.get(term).containsKey(song.getId())) 
						lyricsTF.get(term).put(song.getId(), 1.0);
					else 
						lyricsTF.get(term).replace(song.getId(), lyricsTF.get(term).get(song.getId()) + 1.0);
				}
			}
		}
		/*2.1 Calculating titles' TF & IDF values*/
		for(String term: titleTF.keySet()) {
			/*Calculating TF for each title that contains this term*/
			for(Integer id: titleTF.get(term).keySet()) {
				double newTf = 1.0 + Math.log10(titleTF.get(term).get(id));
				titleTF.get(term).replace(id, newTf);
			}
			/*Calculating IDF for this term*/
			double newIdf = Math.log10(1.0 + (double)songs.size() / (double)titleTF.get(term).size());
			titleIDF.put(term, newIdf);
		}
		/*2.2 Calculating lyrics' TF & IDF values*/
		for(String term: lyricsTF.keySet()) {
			/*Calculating TF for each lyrics that contains this term*/
			for(Integer id: lyricsTF.get(term).keySet()) {
				double newTf = 1.0 + Math.log10(lyricsTF.get(term).get(id));
				lyricsTF.get(term).replace(id, newTf);
			}
			/*Calculating IDF for this term*/
			double newIdf = Math.log10(1.0 + (double)songs.size() / (double)lyricsTF.get(term).size());
			lyricsIDF.put(term, newIdf);
		}
		
		/*3. Calculating & storing TFIDF weight for title & lyrics*/
		for(Song song: songs) {
			titleWeights.put(song.getId(), new HashMap<String, Double>());
			for(String term: song.getTitleTokens()) {
				double titleScore = titleTF.get(term).get(song.getId()) * titleIDF.get(term);
				titleWeights.get(song.getId()).put(term, titleScore);
			}
			lyricWeights.put(song.getId(), new HashMap<String, Double>());
			for(String term: song.getLyricTokens()) {
				double lyricsScore = lyricsTF.get(term).get(song.getId()) * lyricsIDF.get(term);
				lyricWeights.get(song.getId()).put(term, lyricsScore);
			}
		}
	}
	
	/*Method to display search results*/
	public static String displaySearchResults(List<SearchResult> results){
		StringBuilder str = new StringBuilder();
		for(int i = 0; i < results.size(); i++){
			str.append("<"+(i+1)+">"+results.get(i)+"<br/>");
		}
		System.out.println(str);
		return str.toString();
	}
	
	/* Method to search songs given a raw-text query
	 * Return a list of top k (or less) SearchResult objects raked by their TFIDF scores*/
	public List<SearchResult> search(String queryString, int k) {
		/*Tokenizing the query and calculating its TFIDF score*/
		List<String> queryTokensLyrics = tokenize(queryString);
		/*Creating the query vector for Cosine(q,d) for lyrics*/
		HashMap<String, Double> qVectorLyrics = new HashMap<String, Double>(); //query vector for lyrics
		for(String term: queryTokensLyrics) {
			if(!qVectorLyrics.containsKey(term))
				qVectorLyrics.put(term, 1.0);
			else 
				qVectorLyrics.replace(term, qVectorLyrics.get(term) + 1.0);
		}
		/*Calculating TF & IDF weight for each query (for lyrics)*/
		for(String term: qVectorLyrics.keySet()) {
			/*Calculating TF for each query*/
			double newTf = 1.0 + Math.log10(qVectorLyrics.get(term));
			double newIdf = 0.0; 
			/*Taking IDF from the created map*/
			if(lyricsIDF.get(term) != null)
				newIdf = lyricsIDF.get(term);
			qVectorLyrics.replace(term, newTf * newIdf);
		}
		/*Creating the query vector for Cosine(q,d) for titles
		 * Process is the same as above, but tokens will include stopwords
		 * As titles are short in terms of words*/
		List<String> queryTokensTitle = tokenize(queryString);
		HashMap<String, Double> qVectorTitle = new HashMap<String, Double>(); //query vector for titles
		for(String term: queryTokensTitle) {
			if(!qVectorTitle.containsKey(term))
				qVectorTitle.put(term, 1.0);
			else 
				qVectorTitle.replace(term, qVectorTitle.get(term) + 1.0);
		}
		/*Calculating TF & IDF weight for each query (for titles)*/
		for(String term: qVectorTitle.keySet()) {
			double newTf = 1.0 + Math.log10(qVectorTitle.get(term));
			double newIdf = 0.0; 
			if(titleIDF.get(term) != null)
				newIdf = titleIDF.get(term);
			qVectorTitle.replace(term, newTf * newIdf);
		}
		
		/*Final TFIDF score calculation
		 * The finalScore sums up the TFIDF scores for both lyrics & titles
		 * Tier 1 (Majority): 	the song's lyrics (90%)
		 * Tier 2:				the song's title (10%)*/
		List<SearchResult> results = new ArrayList<SearchResult>();
		for(Song song: songs) {
			double titleScore = cosineSimilarity(qVectorTitle, titleWeights.get(song.getId()));
			double lyricsScore = cosineSimilarity(qVectorLyrics, lyricWeights.get(song.getId()));
			double finalScore = (titleScore * 0.1) + (lyricsScore * 0.9);
			if(finalScore != 0) results.add(new SearchResult(song, finalScore, song.getId()));
		}
		/*Sorting the results by relevance score (followed by alphabetical order if applicable)*/
	    Collections.sort(results);
	    
		if(!results.isEmpty() && results.size() >= k) return results.subList(0, k); //Song amount >= k -> return k songs
		else return results.subList(0, results.size()); //Else return non-zero score results, or empty list (size == 0)
		/***********************************************/
	}
	
	/*Method to calculate the cosine similarity between query & document vector*/
	private double cosineSimilarity(Map<String,Double> qVector, Map<String,Double> dVector) {
	    /*Calculating magnitudes & dot product
	     * Dot product is only calculated if the term exists in both qVector and dVector*/
	    double dotProduct = 0.0, magQ = 0.0, magD = 0.0;
	    for(String term: qVector.keySet()) {
	    	magQ += Math.pow(qVector.get(term), 2);
	    	if(dVector.containsKey(term)) dotProduct += qVector.get(term) * dVector.get(term);
	    }
	    for(String term: dVector.keySet()) {
	    	magD += Math.pow(dVector.get(term), 2);
	    }
	    double cosim = dotProduct / (Math.sqrt(magQ) * Math.sqrt(magD));
	    if(Double.isNaN(cosim)) return 0; //NaN check, to prevent NaN scores from becoming the top of the search results
	    else return cosim;
	}
	
	public String getSongLyric(int ID)
	{
		return songs.get(ID).getRawLyrics();
	}
}
