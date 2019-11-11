package org.but4reuse.versioncontrol.timeline;

/**
 * Generation of a timeline from a list of FeatureEvents.
 *
 * @author sandu.postaru, aarkoub 
 *
 */

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.but4reuse.versioncontrol.event.FeatureEvent;
import org.but4reuse.wordclouds.util.WordCloudUtil;

public class Timeline {

	private final String HEADER;
	
	private final String UNITEDHEADER;

	private final String FOOTER = " window.timeline = new TL.Timeline('timeline-embed', timeline_json);\n" + 
			"\n" + 
			"  </script>\n" + 
			"\n" + 
			"  <script type='text/javascript'>\n" + 
			"    const credits = document.getElementsByClassName('tl-attribution')[0],\n" + 
			"          but4reuse = 'Generated with <a href=\"https://but4reuse.github.io/\" target=\"_blank\"><span class=\"\"></span>but4reuse</a> using ';\n" + 
			"\n" + 
			"    credits.innerHTML = but4reuse + credits.innerHTML;\n" + 
			"   \n" + 
			"\n" + 
			"  </script>\n" + 
			"\n" + 
			"</html>";
	
	private final String headline;
	private final String headlineBody;

	private List<FeatureEvent> events;

	public Timeline(String headline, String headlineBody) {
		events = new ArrayList<>();			
		this.headline = headline;
		this.headlineBody = headlineBody;
		
		HEADER = "<!DOCTYPE html>\n" + 
				"<html>\n" + 
				"\n" + 
				"  <head>\n" + 
				"    <meta charset='UTF-8'>\n" + 
				"    <title>" + headline + " timeline</title>\n" + 
				"    <link title='timeline-styles' rel='stylesheet' href='https://cdn.knightlab.com/libs/timeline3/latest/css/timeline.css'>\n" + 
				"    <link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css\">\n" + 
				"\n" + 
				"  </head>\n" + 
				"\n" + 
				"  <body>\n" + 
				"    <style>\n" + 
				"      .new-feature { color: #66BB6A; }\n" + 
				"      .removed-feature { color: #EF5350; }\n" + 
				"      .tag { color: #42A5F5; }\n" + 
				"\n" + 
				"    </style>    \n" + 
				"    <div id='timeline-embed' style='width: 100%; height: 600px'></div>\n" + 
				"  </body>\n" + 
				"\n" + 
				"\n" + 
				"\n" + 
				"  <script src='https://cdn.knightlab.com/libs/timeline3/latest/js/timeline.js'></script>\n" + 
				"\n" + 
				"  <script type='text/javascript'>\n" + 
				"\n" + 
				"    var timeline_json = ";
		
		
		
		UNITEDHEADER = "<!DOCTYPE html>\n" + 
				"<html>\n" + 
				"\n" + 
				"  <head>\n" + 
				"    <meta charset='UTF-8'>\n" + 
				"    <title>" + headline + " 2timeline</title>\n" + 
				"    <link title='timeline-styles' rel='stylesheet' href='https://cdn.knightlab.com/libs/timeline3/latest/css/timeline.css'>\n" + 
				"    <link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css\">\n" + 
				"\n" + 
				"  </head>\n" + 
				"\n" + 
				"  <body>\n" + 
				"    <style>\n" + 
				"      .new-feature { color: #66BB6A; }\n" + 
				"      .removed-feature { color: #EF5350; }\n" + 
				"      .tag { color: #42A5F5; }\n" + 
				"\n" + 
				"    </style>    \n" + 
				"    <div id='timeline-embed' style='width: 100%; height: 600px'></div>\n" + 
				"  </body>\n" + 
				"\n" + 
				"\n" + 
				"\n" + 
				"  <script src='https://cdn.knightlab.com/libs/timeline3/latest/js/timeline.js'></script>\n" + 
				"\n" + 
				"  <script type='text/javascript'>\n" + 
				"\n" + 
				"    var timeline_json = ";
	}

	public void addEvent(FeatureEvent event) {
		events.add(event);
	}
	
	public void addEvents(List<FeatureEvent> events){
		this.events.addAll(events);		
	}

	public void construct(String path) {

		try {
			Files.createDirectories(Paths.get(path));
		} catch (IOException e) {			
			e.printStackTrace();
		}
		
		// timeline cover
		StringBuilder body = new StringBuilder("{\n");
		body.append("            'title': {\n" + 
				"                'text': {\n" + 
				"                    'headline': '" + headline + "',\n" + 
				"                    'text': '" + headlineBody + "'\n" + 
				"                }\n" + 
				"            },");
		
		StringBuilder unitedBody = new StringBuilder("{\n");
		unitedBody.append("            'title': {\n" + 
				"                'text': {\n" + 
				"                    'headline': '" + headline + "',\n" + 
				"                    'text': '" + headlineBody + "'\n" + 
				"                }\n" + 
				"            },");
		
		
		// timeline events
		body.append("'events': [");
		unitedBody.append("'events': [");
		
		
		StringBuilder added = new StringBuilder();
		StringBuilder removed = new StringBuilder();
		
		String unitedPath = "wordclouds/united_cloud.png";
		String removedUnitedPath = "wordclouds/removedunited_cloud.png";

		for(FeatureEvent event: events){
			
			try{
				// Changement path :
				Files.createDirectories(Paths.get(path + "/wordclouds"));
				//Files.createDirectories(Paths.get("~/testWC/wordclouds"));
			}
			catch(IOException e){
				e.printStackTrace();
			}
			
			// déplacé avant le for
			//String addedPath = "wordclouds/added_cloud.png";
			//String removedPath = "wordclouds/removed_cloud.png";
			String addedPath = "wordclouds/added_" + event.getEndCommit().getSha()+".png";
			String removedPath = "wordclouds/removed_" + event.getEndCommit().getSha()+".png";
			
			if(event.getType() == FeatureEvent.Type.COMMIT){
				WordCloudUtil.saveCloud(event.getAddedCloud(), path.replaceAll("\\\\", "/")+"/"+addedPath);
				WordCloudUtil.saveCloud(event.getRemovedCloud(), path.replaceAll("\\\\", "/")+"/"+removedPath);
				/**/
				WordCloudUtil.saveCloud(event.getAddedCloud(), path.replaceAll("\\\\", "/")+"/"+unitedPath);
				WordCloudUtil.saveCloud(event.getRemovedCloud(), path.replaceAll("\\\\", "/")+"/"+removedUnitedPath);
				/**/
			}
			
			// reuse string builders
			added.setLength(0);
			removed.setLength(0);				
			
			Date date = event.getEndCommit().getDate();

			// Date.getMonth() deprecated, what a joke ...
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
											
			String eventHeadline = null;
			
			/**/
			String globalEventHeadline = null;
			/**/
			
			String text = "<div><strong>Commit id: </strong>" + event.getEndCommit().getSha();					
			
			if(event.getType() == FeatureEvent.Type.COMMIT){
				/* commit style */
				
				text += "<div><strong>Start commit id: </strong>" + event.getStartCommit().getSha() +
						"<div><strong>Number of added elements: </strong>" +  
						event.getNbAddedElements() + "<div><strong>Number of removed elements: </strong>" + event.getNbRemovedElements() +
						"<div><strong>Number of added words: </strong>" + event.getNbAddedWords() +
						"<div><strong>Main contributors: </strong>";
				
				for(String contributor : event.getContributors()){
					text += "-"+contributor+" ";
				}
				
				
				eventHeadline ="<div><br/><strong> Added Features </strong> " +
						"<div><img src=\""+addedPath+"\"></div>" +
						"<strong> Removed Features </strong> " +
						"<div><img src=\""+removedPath+"\"></div></div>";
			
				globalEventHeadline ="<div><br/><strong> Added Features </strong> " +
						"<div><img src=\""+unitedPath+"\"></div>" +
						"<strong> Removed Features </strong> " +
						"<div><img src=\""+removedUnitedPath+"\"></div></div>";
			
								
			}
			else if (event.getType() == FeatureEvent.Type.TAG){
				/* tag style */
				eventHeadline = "<span class=\"fa fa-info tag\" aria-hidden=\"true\"></span>" + event.getAddedFeatures().get(0);
				/*   . . . - - - . . .  */
				globalEventHeadline = "<span class=\"fa fa-info tag\" aria-hidden=\"true\"></span>" + event.getAddedFeatures().get(0);
				/*   ... --- .-. - . --..     -- --- ..     -.. .     .-.. .-   */
				
			}
			else{
				System.err.println("Wrong event type");
			}		
			
			text += "<div><strong>Commit message: </strong>" +
					event.getEndCommit().getMessage().replaceAll("\\r\\n|\\r|\\n", "<br>") + "</div>";
			
			body.append("{\n" + 
					"                    'start_date': {\n" + 
					"                        'month': '" + (cal.get(Calendar.MONTH) + 1) + "',\n" + 
					"                        'day': '" + cal.get(Calendar.DAY_OF_MONTH) + "',\n" + 
					"                        'year': '" + cal.get(Calendar.YEAR) + "'\n" + 
					"                    },\n" + 
					"                    'text': {\n" + 
					"                        'headline': '" + eventHeadline + "',\n" + 
					"                        'text': '" + text + "'\n" + 
					"                    }\n" + 
					"                },");
			
			unitedBody.append("{\n" + 
					"                    'start_date': {\n" + 
					"                        'month': '" + (cal.get(Calendar.MONTH) + 1) + "',\n" + 
					"                        'day': '" + cal.get(Calendar.DAY_OF_MONTH) + "',\n" + 
					"                        'year': '" + cal.get(Calendar.YEAR) + "'\n" + 
					"                    },\n" + 
					"                    'text': {\n" + 
					"                        'headline': '" + globalEventHeadline + "',\n" + 
					"                        'text': '" + text + "'\n" + 
					"                    }\n" + 
					"                },");
		}
		
		WordCloudUtil.saveCloud(path.replaceAll("\\\\", "/")+"/"+"wordclouds/added_cloud.png");
		 
		
		// end features
		body.append("\n     ]\n" + 
				"        };\n");
		
		unitedBody.append("\n     ]\n" + 
				"        };\n");
		
		try (BufferedWriter br = new BufferedWriter(new FileWriter(Paths.get(path, headline + ".html").toFile()))) {
			
			br.write(HEADER + body + FOOTER);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try (BufferedWriter br = new BufferedWriter(new FileWriter(Paths.get(path, headline + ".html").toFile()))) {
			
			br.write(UNITEDHEADER + unitedBody + FOOTER);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
