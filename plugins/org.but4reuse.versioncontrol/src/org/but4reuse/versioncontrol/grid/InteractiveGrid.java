package org.but4reuse.versioncontrol.grid;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.but4reuse.versioncontrol.event.FeatureEvent;

/**
 * Generation of a interactive grid from a list of FeatureEvents.
 *
 * @author sandu.postaru
 *
 */

public class InteractiveGrid {

	private final String HEADER;	
	private final String FOOTER;
	private final String TITLE;
	
	private List<FeatureEvent> events;

	public InteractiveGrid(String title) {		
		this.events = new ArrayList<FeatureEvent>();
				
		HEADER = "<!DOCTYPE html>\n" +
				 "<html>\n" +
		         " 	<head>\n" +
		         " 		<meta charset='UTF-8'>\n" +
		         "		<link rel=\"stylesheet\" type=\"text/css\" href=\"https://cdn.datatables.net/v/dt/jq-2.2.4/dt-1.10.15/datatables.min.css\"/>\n" +
		         "		<style type=\"text/css\" > .dataTables_filter, .dataTables_info { display: none; } </style>\n" +
		         "		<style type=\"text/css\" > tfoot {display: table-header-group;} </style>\n" +
		         "		<style type=\"text/css\" > input { width: 100px } </style>\n" +
		         "		<script type=\"text/javascript\" src=\"https://cdn.datatables.net/v/dt/jq-2.2.4/dt-1.10.15/datatables.min.js\"></script>\n" +
		         "		<script type=\"text/javascript\" class=\"init\"> \n" +
		         "			$(document).ready(function() {\n" +
		         "				$('#grid tfoot th').each( function () {\n" +
			     "   				var title = $(this).text();" +
			     "					$(this).html( '<input type=\"text\" placeholder=\"Search\" />' );\n" +
			     " 				} );\n" +
			     "			var table = $('#grid').DataTable({paging: false});\n" +
			     "			table.columns().every( function () {\n" +
			     "   			var title = this.header();" +
			     "				var that = this;\n" +
			     "				$( 'input', this.footer() ).on( 'keyup change', function () {\n" +
		         "					if ( that.search() !== this.value ) {\n" +
		         " 						that\n" +
		         "   						.search( this.value )\n" +
		         "   						.draw();\n" +
		         "					}\n" +
		         " 				} );\n" +
		         "				} );\n" +
		         "			} );\n" +
		         "		</script>\n" +
		         "		\n" +
		         "	</head>\n" +
		         "	<body>\n" +
		         "		<div>" + title + " interactive grid</div>\n" + 		         
		         " 		<table id=\"grid\" class=\"display\" width=\"100%\" cellspacing=\"0\">\n" +
		         "			<thead>\n" +
		         "      		<tr>\n" +
		         "          		<th>Date</th>\n" +
		         "           		<th>Sha</th>\n" +
		         "           		<th>Start commit sha</th>\n" +
		         "          		<th>Added features</th>\n" +
		         "            		<th>Nb. added elements</th>\n" +
		         "      	    	<th>Nb. added words</th>\n" +
		         "  	         	<th>Removed features</th>\n" +
		         "      	    	<th>Nb. removed elements</th>\n" +
		         "         			<th>Message</th>\n" +
			     "          		<th>Main contributors</th>\n" +
		         "       		</tr>\n" +
		         "   		</thead>\n" +
		         " 			<tfoot>\n" +
		         "				<tr>\n" +
			     "    		       	<th>Date</th>\n" +
			     "    		       	<th>Sha</th>\n" +
		         "           		<th>Start commit sha</th>\n" +
			     "       		   	<th>Added features</th>\n" +
			     "           	 	<th>Nb. added elements</th>\n" +
		         "      	    	<th>Nb. added words</th>\n" +
			     "           		<th>Removed features</th>\n" +
			     "          		<th>Nb. removed elements</th>\n" +
			     "          		<th>Message</th>\n" +
			     "          		<th>Main contributors</th>\n" +
		         "				</tr>\n" +
		         "			</tfoot>"
		     ;
		
		FOOTER = "		</table>\n" +
				"	</body>\n" +
				"</html>";
		
		TITLE = title;		
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

		StringBuilder body = new StringBuilder("<tbody>\n");
		
		Calendar cal = Calendar.getInstance();
		
		for (FeatureEvent event : events) {
			if (event.getType() == FeatureEvent.Type.COMMIT) {
				
				body.append("<tr>\n");
				
				cal.setTime(event.getEndCommit().getDate());
				
				String formatedDate = (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.DAY_OF_MONTH) + "/"
						+ cal.get(Calendar.YEAR);

				body.append("<td>" + formatedDate + "</td>\n");
				
				body.append("<td>" + event.getEndCommit().getSha() + "</td>\n");
				
				body.append("<td>" + event.getStartCommit().getSha() + "</td>\n");
				
				body.append("<td>");

				for (String addedFeature : event.getAddedFeatures()) {
					body.append(addedFeature + " ");
				}

				body.append("</td><td>" + event.getNbAddedElements() + "</td>\n");
				
				body.append("<td>" + event.getNbAddedWords() + "</td>\n");

				body.append("<td>");

				for (String removedFeature : event.getRemovedFeatures()) {
					body.append(removedFeature + " ");
				}

				body.append("</td><td>" + event.getNbRemovedElements() + "</td>\n");

				body.append("<td>" + event.getEndCommit().getMessage().replaceAll("\\r\\n|\\r|\\n", " ") + "</td>\n");
				
				body.append("<td>");
				
				for(String contributor : event.getContributors()){
					body.append("-"+contributor + " ");
				}
				
				body.append("</td></tr>\n");
			}						
		}
		
		body.append("</tbody>\n");

		try (BufferedWriter br = new BufferedWriter(new FileWriter(Paths.get(path, TITLE + ".html").toFile()))) {
			
			br.write(HEADER + body + FOOTER);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
