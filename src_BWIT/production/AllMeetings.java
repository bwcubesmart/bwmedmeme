
package production;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.lang3.StringUtils;

import normalization.ConferenceNormalizer;
import normalization.SocietyNormalizer;

import opennlp.DetailedNLPer;
import parsing.ConferenceNameSplitter;
import parsing.Dictionaries;
import parsing.Meeting;
import utils.MyDateFormatter;


public class AllMeetings {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String inPath="../NLP Process/Meetings_Society/Input/";
		String outPath="../NLP Process/Meetings_Society/Output/";
		String inputFile = //"randomized_5000_09182014162522EVENTS_CIS_MemeDEV.txt";
							//"EVENTS_CIS_MemeDEV.txt"
							"Event_Medmeme.txt"
							//"Event_Medmeme_sampleFromQC.txt"
				;
		//comment the below if you want to use the input file name specified above
		File f = new File(inPath);
		String[] ls = f.list();
		for (String fileName : ls) {
			if(fileName.endsWith(".txt"))
				inputFile=fileName;
		}		
		
		String outputFile=inputFile.substring(0, inputFile.lastIndexOf(".txt"))+"_MAP_"+
				MyDateFormatter.getDate()+
				".txt";
		String outputFile2=inputFile.substring(0, inputFile.lastIndexOf(".txt"))+"_CONF_NORMALIZED_"+
				MyDateFormatter.getDate()+
				".txt";
		
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(
				inPath+inputFile
				), "UTF-8"));
		PrintWriter pwr = new PrintWriter(outPath+outputFile, "UTF-8");

		DetailedNLPer nlp = new DetailedNLPer();
		ConferenceNameSplitter confSplitter = new ConferenceNameSplitter();
		Dictionaries dict = new Dictionaries();

		int c=0;
		while(br.ready()){
			String meetingString = br.readLine().replaceAll("\"", "");///doing this to remove extra "s
			Meeting aMeeting = new Meeting(meetingString,nlp,confSplitter, dict);
			aMeeting.print(pwr);
			c++;
			if(c%1000==0) System.err.println("processed "+c+" lines");
		}
		br.close();
		pwr.close();
		
		
		nlp=null;confSplitter=null;

		System.err.println("calling ConferenceNormalizer.main");
		String[] argsConference={outPath+outputFile,
				outPath+outputFile2};
		ConferenceNormalizer.main(argsConference);

		System.err.println("calling SocietyNormalizer.main");
		String[] argsSociety={outPath+outputFile,
				outPath+inputFile.substring(0, inputFile.lastIndexOf(".txt"))+"_SOCIETY_NORMALIZED_"+MyDateFormatter.getDate()+".txt"};
		SocietyNormalizer.main(argsSociety);
		
		System.err.println("calling AbbreviationsFromAllMeetings.main");
		String[] argsAbbr={inPath+inputFile,
				outPath+inputFile.substring(0, inputFile.lastIndexOf(".txt"))+"_ABBREVIATIONS_"+MyDateFormatter.getDate()+".txt"};
		AbbreviationsFromAllMeetings.main(argsAbbr);
		
		System.err.println("creating normalized meeting names");
		HashMap<String, String> meeting2Confs=new HashMap<String, String>();
		BufferedReader br1 = new BufferedReader(new InputStreamReader(new FileInputStream(
				outPath+outputFile
				), "UTF-8"));
		String outputFileRepeat=inputFile.substring(0, inputFile.lastIndexOf(".txt"))+"_MAP_"+MyDateFormatter.getDate()+".txt";
		PrintWriter pwrRepeat= new PrintWriter(outPath+outputFileRepeat, "UTF-8");
		while(br1.ready()){
			String[] splits = br1.readLine().split("\"\\|\"");
			String meetingString = splits[0].replaceAll("\"", "");
			String confs=splits[1].replaceAll("\"", "");
			meeting2Confs.put(meetingString, confs);
			
			ArrayList<String> splitList = new ArrayList<String>(Arrays.asList(splits));
			splitList.remove(1);
			pwrRepeat.println(StringUtils.join(splitList,"\"|\""));
		}
		br1.close();
		pwrRepeat.close();
		
		
		HashMap<String, String> conf2Norm = new HashMap<String, String>();
		br1 = new BufferedReader(new InputStreamReader(new FileInputStream(
				outPath+outputFile2
				), "UTF-8"));
		while(br1.ready()){
			String[] splits = br1.readLine().split("\"\\|\"");
			String conf = splits[0].replaceAll("\"", "");
			String normConf=splits[1].replaceAll("\"", "");
			conf2Norm.put(conf, normConf);
		}
		br1.close();
		
		br1 = new BufferedReader(new InputStreamReader(new FileInputStream(
				inPath+inputFile
				), "UTF-8"));
		String outputFile5=inputFile.substring(0, inputFile.lastIndexOf(".txt"))+"_MTNGS_"+MyDateFormatter.getDate()+".txt";
		PrintWriter pwr5 = new PrintWriter(outPath+outputFile5, "UTF-8");
		while(br1.ready()){
			String meetingString = br1.readLine().replaceAll("\"", "");
			ArrayList<String> normConfs=new ArrayList<String>();
			for (String conf : meeting2Confs.get(meetingString).split("@@@")) {
				normConfs.add(conf2Norm.get(conf));
			}
			Collections.sort(normConfs);
			String normalizedMeetingName = StringUtils.join(normConfs, "@@@");
			pwr5.print("\""+meetingString+"\"");
			pwr5.println("|\""+normalizedMeetingName+"\"");
		}
		br1.close();
		pwr5.close();
		
		if(new File(outPath+outputFile).delete())
			System.err.println("deleted "+outPath+outputFile);
		else
			System.err.println("failed to delete "+outPath+outputFile);
		
	}

}
