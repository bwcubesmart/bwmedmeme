package production;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import opennlp.DetailedNLPer;
import parsing.ConferenceNameSplitter;
import parsing.Dictionaries;
import parsing.Meeting;
import utils.SidExtractAbbrev;
import utils.SidExtractAbbrev.Abbr;

public class AbbreviationsFromAllMeetings {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		DetailedNLPer nlp = new DetailedNLPer();
		Dictionaries dict = new Dictionaries();

		SidExtractAbbrev extractAbbrev = new SidExtractAbbrev();
		process("2014 Spring Meeting of the Southwest Oncology Group (SWOG)",  extractAbbrev, nlp, dict);
		process("Leadership: The Key to Successful Telehealth Program (Preconference to Telephone Triage: Best Practices and Benchmarking)",  extractAbbrev, nlp, dict);
		process("101st Annual Clinical Assembly of the American Osteopathic Colleges of Ophthalmology and Otolaryngology-Head and Neck Surgery (AOCOO-HNS)",  extractAbbrev, nlp, dict);
		process("2012 Symposium on Aesthetic Plastic Surgery and Anti- Aging Medicine: The Next Generation (APSAM)",  extractAbbrev, nlp, dict);
		process("8th International Conference on Advanced Technologies and Treatments for Diabetes (ATTFD)",  extractAbbrev, nlp, dict);
		process("10th Asian and Oceanian Epilepsy Congress (AOEC)",  extractAbbrev, nlp, dict);
		process("7th Intercongress Symposium of the Asia and Oceania Society for the Comparative Endocrinology (AOSCCE)",  extractAbbrev, nlp, dict);
		if(args.length==0){
			Scanner sc = new Scanner(System.in);
			while(sc.hasNextLine()){
				process(sc.nextLine(),  extractAbbrev, nlp, dict);
			}
			sc.close();
		}
		
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(
				args.length==2?args[0]:"../batchProcessing/Meetings_input_MemeDEV.txt"
				), "UTF-8"));
		
		
		PrintStream ps = new PrintStream(args.length==2?args[1]:"../batchProcessing/Meetings_extractedAbbreviations.txt", "UTF-8");

//		HashMap<String, ArrayList<String>> Full2Abbrvs = new HashMap<String, ArrayList<String>>();
		int c=0;
		while(br.ready()){
			String meetingString = br.readLine();
			process(ps, meetingString,  extractAbbrev, nlp, dict);
			c++;
			if(c%10000==0) System.err.println("processed "+c+" lines");
		}
		br.close();
		
		//this will do it for two files
		/*br = new BufferedReader(new InputStreamReader(new FileInputStream(
				"../batchProcessing/Meetings_input_CIS.txt"
				), "UTF-8"));
		while(br.ready()){
			String meetingString = br.readLine();
			process(pwr, meetingString,  extractAbbrev, nlp);
			c++;
			if(c%10000==0) System.err.println("processed "+c+" lines");
		}
		br.close();*/
		
		ps.close();
	}

	public static void process(String meetingString,
			SidExtractAbbrev extractAbbrev, DetailedNLPer nlp, Dictionaries dict) {
		meetingString=dict.deAccent(meetingString);
		
		ArrayList<Abbr> abbrs = extractAbbrev.extractAbbrPairs(meetingString);
		for (Abbr abbr : abbrs) {
			boolean found=false;
			
			//"2013 Cardiology Review Course (British Cardiovascular Society and Mayo Clinic)"|"Cardiovascular Society and Mayo Clinic"|"Course"|"SOCIETY"
			if(abbr.shortform.matches(".*[a-z].*")&&dict.origEnglishWords.contains(abbr.shortform.toUpperCase()))
				continue;
			
			//"5th World Conference on Ecological Restoration -Society for Ecological Restoration (SER)"|"-Society for Ecological Restoration"|"SER"|"UNKNOWN"
			abbr.longform=abbr.longform.replaceAll("-", " ").trim();
			
			
			if(nlp.isConference(abbr.longform)){
				System.out.println("\""+meetingString+"\""+"|"+"\""+abbr.longform+"\""+"|"+"\""+abbr.shortform+"\""+"|"+"\"CONFERENCE\"");
				found=true;
			}
				
			else if(nlp.isSociety(abbr.longform)){
				System.out.println("\""+meetingString+"\""+"|"+"\""+abbr.longform+"\""+"|"+"\""+abbr.shortform+"\""+"|"+"\"SOCIETY\"");
				found=true;
			}
			else if(!nlp.notSocieties.contains(abbr.longform.toUpperCase())){
				for (String tok : abbr.longform.split("\\s+|-|'")) {
					if(dict.orgKeywords.contains(tok.toUpperCase()) || tok.toUpperCase().contains("UNIVERSIT")){
						System.out.println("\""+meetingString+"\""+"|"+"\""+abbr.longform+"\""+"|"+"\""+abbr.shortform+"\""+"|"+"\"SOCIETY\"");
						found = true;
					}
				}
				
			}
			if(!found){
				Meeting aMeeting = new Meeting(meetingString,nlp,new ConferenceNameSplitter(), dict);
				if(aMeeting.societies.contains(abbr.longform))
					System.out.println("\""+meetingString+"\""+"|"+"\""+abbr.longform+"\""+"|"+"\""+abbr.shortform+"\""+"|"+"\"SOCIETY\"");
				else if(aMeeting.conferenceNames.contains(abbr.longform)||abbr.longform.contains("Congress"))
					System.out.println("\""+meetingString+"\""+"|"+"\""+abbr.longform+"\""+"|"+"\""+abbr.shortform+"\""+"|"+"\"CONFERENCE\"");
				else
					System.out.println("\""+meetingString+"\""+"|"+"\""+abbr.longform+"\""+"|"+"\""+abbr.shortform+"\""+"|"+"\"UNKNOWN\"");
			}
		}
		
	}

	public static void process(PrintStream ps, String meetingString, SidExtractAbbrev extractAbbrev, DetailedNLPer nlp, Dictionaries dict) {
		PrintStream bak=System.out;
		System.setOut(ps);
		
		process(meetingString, extractAbbrev, nlp, dict);
		
		System.setOut(bak);
	}

}
