import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import com.acrcloud.utils.ACRCloudExtrTool;
import com.acrcloud.utils.ACRCloudRecognizer;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class View {
   private JFrame mainFrame;//Main Frame of everything
   private JFrame songFrame;
   //TODO Add new frame for the whole song Data
   private JLabel headerLabel;//The top thingy that says the name
   private JLabel statusLabel;//This is the thing that shows Query : Something 
   
   private JPanel controlPanel;//A block that holds Labels and stuff
   private JPanel scorePanel;//Another block for TFIDF
   private JPanel searchBartxt;
   private JPanel Finalpanel;
   private JPanel songFrameInfo;
   private JPanel songFrameText;
   
   private JLabel msglabel;//Msg that is green
   private JLabel outputlabel;//the thing to show TFIDF
  // private JLabel SongName;
   
   private JTextArea SongText;
   
   private JButton Spotify;
   private JButton searchButton;
   private JButton ArcCloud;
   private JButton[] Lyric;
   
   private JScrollPane scroll;

   
   private JTextField input;//input xD
   
   //private JTable TfidfTable;
   
   private TFIDFSearcher searcher = new TFIDFSearcher(".\\songdata.csv");

   public View(){
      GUI();//calls GUI
   }
   public static void main(String[] args){
	  
      View  Show = new View();//Init  
     Show.show();//calls show for stuff out of initialization
   }
   private void GUI(){ //Rework to initialize all items on screen 
      mainFrame = new JFrame("Multi Tiered Music Search Using TFIDF");
      mainFrame.setSize(1200,780);//Size of the popup
      mainFrame.setLayout(new GridLayout(5, 1));//how many blocks 
      //Exit program
      mainFrame.addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent windowEvent){
            System.exit(0);
         }        
      });    
      //Exit Program
      
      songFrame = new JFrame("Temp");
      songFrame.setSize(1200, 780);
      songFrame.addWindowListener(new WindowAdapter() {
    	  public void windowClosing(WindowEvent windowEvent)
    	  {
    		  songFrame.setVisible(false);
    	  }
	});
      
      headerLabel = new JLabel("Multi Tiered Music Search Using TFIDF", JLabel.CENTER);        
      statusLabel = new JLabel("Prototype",JLabel.CENTER);    
      statusLabel.setSize(350,100);//Size of status Labal 
            
      SongText = new JTextArea(5,50);
      SongText.setFont(new Font("Calibri", Font.PLAIN, 15));
      scroll = new JScrollPane(SongText);
      scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      scroll.setPreferredSize(new Dimension(1000, 600));
      
      
      
    //  SongName = new JLabel("TempName", JLabel.CENTER);
      
      
      controlPanel = new JPanel();
      controlPanel.setLayout(new FlowLayout());
      scorePanel = new JPanel();
      scorePanel.setLayout(new FlowLayout());
      searchBartxt = new JPanel();
      searchBartxt.setLayout(new FlowLayout());
      Finalpanel = new JPanel();
      Finalpanel.setLayout(new FlowLayout());
      songFrameInfo = new JPanel();
      songFrameInfo.setLayout(new FlowLayout());
      songFrameText = new JPanel();
      songFrameText.setLayout(new FlowLayout());
      
      
      msglabel = new JLabel("temp text not gonna show anyways");
      outputlabel = new JLabel("Nope");//Just to initialize these text
      input = new JTextField(20);
      
      searchButton = new JButton("Search");
      Spotify = new JButton("Spotify");
      ArcCloud = new JButton("ArcCloud"); // Add in the yes no option to this button to make sure everything is right before clicking
      Lyric = new JButton[5];
      String[] nums = {"1", "2", "3", "4", "5"};
      for (int i = 0; i < Lyric.length; i++) {
    		   Lyric[i] = new JButton(nums[i]);
    		}
      
      mainFrame.add(headerLabel);//Head
      mainFrame.add(controlPanel);//1 block
      mainFrame.add(statusLabel);//2 block
      mainFrame.add(scorePanel);
      mainFrame.add(Finalpanel);
      
      
      songFrame.getContentPane().add(BorderLayout.CENTER, songFrameText);
      songFrame.getContentPane().add(BorderLayout.SOUTH, songFrameInfo);
      songFrameInfo.add(Spotify);
      songFrameText.add(scroll);
      
      searchBartxt.add(msglabel);
      controlPanel.add(searchBartxt);
      controlPanel.add(input);
      controlPanel.add(searchButton);
      controlPanel.add(ArcCloud);
    //TODO Add listener
      scorePanel.add(outputlabel);
      //scorePanel.add(Spotify);
      for(int i=0;i<Lyric.length;i++)
      {
      Finalpanel.add(Lyric[i]);
      }
   
      mainFrame.setVisible(true);
      scorePanel.setVisible(false);//Set false until search is done 
      Finalpanel.setVisible(false);
      songFrame.setVisible(false);
    //TODO set visibility for frames
   }
   private void show(){//Assign Values and Text here 
	  
      
      msglabel.setText("Enter Song Lyric Here or Enter Path");   
      //searchBartxt.add(msglabel);
      //controlPanel.add(searchBartxt);//1st part which is green
      
      //ARCCLOUD
      //////////////////////////// 
      ArcCloud.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		ArcCloud(input.getText());
		scorePanel.setVisible(true);
		}
	});
      
      //SEARCH IS HERE
      ///////////////////////////
      searchButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {     
            String data = "Query : " + input.getText();
            statusLabel.setText(data);  
            
            //For music search MOVED TO Bottom
           computeScore(input.getText());
           scorePanel.setVisible(true);//after clicking search and finishing compute score set the visibility to true
      								//If its long, will add scroll bar and box instead
         }
      }); 

      //////////////////////////////
      
      mainFrame.setVisible(true);      
   }   
   
   private void computeScore(String query)
   {
	   	for(ActionListener al : Spotify.getActionListeners())
	   	{
	    Spotify.removeActionListener(al);
	   	}
	   	for(JButton current : Lyric)
	   	{
	   		for(ActionListener al : current.getActionListeners())
	   		{
	   			current.removeActionListener(al);
	   		}
	   	}
	    String Temptext;
		/*String SearchQuery = query;
		SearchQuery = query.replace(" ", "_");
		final String tempS = SearchQuery;*/
		List<SearchResult> results = searcher.search(query, 5);
		//System.out.println("@@@ Results: "+(query.length() > 50? query.substring(0, 50)+"...":query));
		
		Temptext = TFIDFSearcher.displaySearchResults(results);
		
		//System.out.println();
		
		//Temptext = searcher.getSongLyric(results.get(0).getSongID());
		if(results.size() == 0 || query == "")
		{
			outputlabel.setText("No Match Found");
			scorePanel.setVisible(false);
			Finalpanel.setVisible(false);
		}
		else
		{
		Finalpanel.setVisible(true);
	   outputlabel.setText("<html>"+Temptext+"</html>");//Need Fixes
	   //TfidfTable = new JTable(TFIDFSearcher.getResults(results),"Stuff");//Table needs bug fixes
	   /*Spotify.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			//This part calls DEFAULT web browser... problems with accessing exe files.
			Spotify(tempS);
		}
	});*/

	   //Each Button does stuff
	   for (int i = 0; i < Lyric.length; i++) {
		   Lyric[i].addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JButton src = (JButton) e.getSource();
				   for (int i = 0; i < Lyric.length; i++) {
				      if (src==Lyric[i]) {
				    	//TODO Add stuff here to make it work
				    	 SongSet(results, i);
				    	
				      }
				   }
			}
		});
		}
	}
   }
   private void SongSet(List<SearchResult> results, int i)
   {
	 // SongName.setText(results.get(i).getSong().getTitle());
	  songFrame.setTitle(results.get(i).getSong().getTitle());
 	  SongText.setText(results.get(i).getSong().getRawLyrics());
 	  SongText.setCaretPosition(0);
 	  Spotify.addActionListener(new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 			//This part calls DEFAULT web browser... problems with accessing exe files.
 			Spotify(results.get(i).getSong().getTitle());
 		}
 	  });
 	  songFrame.setVisible(true);
   }
   private void Spotify(String input)
   {
	    String SearchQuery = input;
		SearchQuery = input.replace(" ", "%20");
		final String tempS = SearchQuery;
	   try {
           URI uri = new URI("https://open.spotify.com/search/results/"+tempS);
           Desktop desktop = null;
           if (Desktop.isDesktopSupported()) {
             desktop = Desktop.getDesktop();
           }

           if (desktop != null)
             desktop.browse(uri);
         } catch (IOException ioe) {
           ioe.printStackTrace();
         } catch (URISyntaxException use) {
           use.printStackTrace();
         }
   }
   private void ArcCloud(String FileLocation)
   {
	   Map<String, Object> config = new HashMap<String, Object>();
	   config.put("host", "identify-ap-southeast-1.acrcloud.com");
       config.put("access_key", "ACCESS_KEY_GOES_HERE");
       config.put("access_secret", "SECRET_KEY_GOES_HERE");
       config.put("debug", false);
       config.put("timeout", 20); // seconds
       
       ACRCloudRecognizer re = new ACRCloudRecognizer(config);
       
       String[] resultsnew;
       String qforTF = "";
       String query = FileLocation;
       String result = re.recognizeByFile(query, 40);
       resultsnew = result.split(",");
       for(int i =0 ;i<resultsnew.length;i++)
       {
       	System.out.println(i+" "+resultsnew[i]);
       	if(resultsnew[i].contains("\"track\":{\"name\":\"") || resultsnew[i].contains("\"title\":\""))
       	{
       		qforTF = resultsnew[i];
       		qforTF = qforTF.replace("\"track\":{\"name\":\"", "");
       		qforTF = qforTF.replace("\"title\":\"", "");
       		qforTF = qforTF.replace("\"", "");
       		System.out.println(qforTF);
       		break;
       	}
      }
       //QforTF is the name from the online db and sent to offline db to get lyrics
       computeScore(qforTF);
       statusLabel.setText("Found in Online DataBase : " + qforTF +" (Offline DB may vary)");
     }
}