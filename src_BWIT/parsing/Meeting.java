package parsing;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import opennlp.DetailedNLPer;

import org.apache.commons.lang3.StringUtils;

import utils.SidExtractAbbrev;
import utils.SidExtractAbbrev.Abbr;
import utils.WordFrequencyCounter;
import utils.WordFrequencyCounter.Word;


// TODO: Auto-generated Javadoc
/**
 * The Class Affiliation.
 */
public class Meeting {
	DetailedNLPer nlp;
	ConferenceNameSplitter confSplitter;
	Dictionaries dict;
	
	/** The mention. */
	public String originalMention;
	public String mention;
	public String normalizedConfName;
	public ArrayList<String> conferenceNames;
	public ArrayList<String> societies;
	public ArrayList<String> societiesNER;
	public ArrayList<String> societiesChunking;
	public ArrayList<String> societiesLookUp;

	HashMap<String, String> mentionTable;
	HashMap<String, String> societyAbbreviationTable;
	
	//HashMap<String, String> replacementDictionary;
	
	
	public ArrayList<Abbr> abbrs;

	public HashMap<String, String> society2Conference;
	public HashMap<String, ArrayList<String>> conference2Societies;
	
	
	/**
	 * Prints the parsed meeting output.
	 */
	public void print() {
		for (String conf : conference2Societies.keySet()) {
			System.out.print("\""+originalMention+"\"");
			System.out.print("|\""+conf+"\"");
			for (String soc : conference2Societies.get(conf)) {
				System.out.print("|"+"\""+soc+"\"");
			}
			System.out.println();
		}
		
		
		System.out.println(originalMention+"\nconferenceNames="+StringUtils.join(conferenceNames,"|||")
				+"\nsocieties="+StringUtils.join(societies,"|||")
	//			+"\nsocietiesNER="+StringUtils.join(societiesNER,"|||")
	//			+"\nsocietiesChunking="+StringUtils.join(societiesChunking,"|||")
	//			+"\nsocietiesLookUp="+StringUtils.join(societiesLookUp,"|||")
				);
		
	}

	public void print(PrintWriter pwr ) {
		ArrayList<String> conferences = new ArrayList<String>(conference2Societies.keySet());
		Collections.sort(conferences);
		String normalizedMeetingName = StringUtils.join(conferences, "@@@");
		for (String conf : conferences) {
			pwr.print("\""+originalMention+"\"");
			pwr.print("|\""+normalizedMeetingName+"\"");
			pwr.print("|\""+conf+"\"");
			for (String soc : conference2Societies.get(conf)) {
				pwr.print("|"+"\""+soc+"\"");
			}
			pwr.println();
		}
		
	}

	public Meeting(String meetingMention, DetailedNLPer nlp, ConferenceNameSplitter confSplitter, Dictionaries dict) {
		this.confSplitter=confSplitter;
		this.dict=dict;
		this.originalMention=meetingMention;
		this.mention=normalizePunctuation(meetingMention);;
		this.mention=dict.deAccent(this.mention);
		
		this.abbrs = new SidExtractAbbrev().extractAbbrPairs(mention);
		for (Abbr abbr : this.abbrs) {
			abbr.longform=normalizePunctuation(abbr.longform);
		}
		this.societyAbbreviationTable = new HashMap<String, String>();
		
		this.nlp=nlp;
		
		this.societiesNER=nlp.getSocieties(mention);
		//this.societiesChunking=nlp.getSocietiesThruChunks(mention);
		
		this.mention=this.mention.replaceAll(" ,", ",");//commas creating issues
		mentionTable = new HashMap<String, String>();
		ArrayList<String> mentions = new ArrayList<String>();
		ArrayList<String> matchMentions = nlp.dlSociety.getMatches(this.nlp.removeStopWordsAndStem(mention));
		for (String string : matchMentions) {
			String[] splits = string.split("\t\t");
			mentions.add(splits[1]);
			mentionTable.put(splits[1], splits[0]);
		};

		for(int i=0; i<mentions.size();i++){
			mentions.set(i, this.normalizePunctuation(mentions.get(i)));
		}
			
		
		//remove bug - duplicates (2013 Annual Meeting of the Abdominal Radiology Group of Australia and New Zealand (ARGANZ))
		this.societiesLookUp=includeLongerOnesOnly(mentions);
		this.societies=new ArrayList<String>(); 
		
		bestMatchOfSocieties();
		HashMap<String,ArrayList<String>> currentSocDuplicates= new HashMap<String,ArrayList<String>>();
		for (String soc : this.societies) {
			String socNorm=normalizePunctuation(soc);
			socNorm = this.nlp.removeStopWordsAndStem(soc);
			if(!currentSocDuplicates.containsKey(socNorm))
				currentSocDuplicates.put(socNorm, new ArrayList<String>());
			currentSocDuplicates.get(socNorm).add(soc);
		}
		this.societies = new ArrayList<String>();
		for (String normSoc : currentSocDuplicates.keySet()) {
			this.societies.add(Collections.max(currentSocDuplicates.get(normSoc)));
		}
		
		this.normalizedConfName=normalizeConferenceName(
				this.mention
				//replaceMeetingName
				);
		this.conferenceNames=this.confSplitter.splitConfs(this.normalizedConfName);
		for(int i=0;i<this.conferenceNames.size();i++) this.conferenceNames.set(i, this.nlp.removeTrailingSymbols(this.conferenceNames.get(i)));
		
		this.society2Conference = new HashMap<String, String>();
		this.conference2Societies=new HashMap<String, ArrayList<String>>();
		for (String confName : this.conferenceNames) {
			if(!conference2Societies.containsKey(confName)) conference2Societies.put(confName, new ArrayList<String>());
		}
		for (String soc : this.societies) {
			String socNorm=this.nlp.removeStopWordsAndStem(this.normalizeConferenceName(soc));//so that removal of stopwords doesn't effect but storing the original society names since some of them are from dictionary 
			boolean found=false;
			for (String confName : this.conferenceNames) {
				String confNameNorm=this.nlp.removeStopWordsAndStem(confName);//also going through same norm
				if(confContainsSoc(confNameNorm,socNorm)){
					this.society2Conference.put(soc, confName);
					this.conference2Societies.get(confName).add(soc);
					found=true;
					break;
				}
			}
			
			if(!found&&this.conferenceNames.size()==1){
				this.society2Conference.put(soc, this.conferenceNames.get(0));
				this.conference2Societies.get(this.conferenceNames.get(0)).add(soc);
				found=true;
			}
			
			if(!found&&this.mentionTable.containsKey(soc)&&this.mentionTable.get(soc).matches("[A-Za-z]+"//meaning one word -all letters - abbreviations in this context
					)){
				for (String confName : this.conferenceNames) {
					if(confName.contains(this.mentionTable.get(soc))){
						this.society2Conference.put(soc, confName);
						this.conference2Societies.get(confName).add(soc);
						found=true;
						break;
					}
				}
			}
			
			if(!found) 
				System.err.println("No conference assigned to a society:\t"+soc+"\t in"+this.mention);
		}
	}


	private String normalizePunctuation(String str) {
		
		return str.replaceAll("\\s*/\\s*", " / ")
				//.replaceAll("\\s*-\\s*", " - ") -- need to decide what to use depending on the errors
				.replaceAll("\\s*-([A-Z])", " $1")
				
				.replaceAll("([a-z]{3})([A-Z])", "$1 $2") //BrainStem --> Brain Stem
				.replaceAll("\\s*,", " ,").replaceAll("\\s*;", " ;").replaceAll("\\s*:", " :").replaceAll("\\s*\\(", " (").trim().replaceAll("\\s+", " ");
	}

	private boolean confContainsSoc(String confName, String soc) {
		if(confName.contains(soc) 
				|| (this.societyAbbreviationTable.containsKey(soc) && confName.contains(this.societyAbbreviationTable.get(soc)))
				|| (this.mentionTable.containsKey(soc) && confName.contains(this.mentionTable.get(soc)))
				)
			return true;
		/*for (String string : this.matchMentions) {
			String[] splits = string.split("\t\t");
			mentions.add(splits[1]);
			mentionTable.put(splits[1], splits[0]);
		}*/
		return false;
	}

//not in order
	private ArrayList<String> includeLongerOnesOnly(ArrayList<String> orig) {
		//if(orig.size()<2) return orig;
		HashSet<String> origSet = new HashSet<String>(orig);
		ArrayList<String> origUniq=new ArrayList<String>(origSet);
		Collections.sort(origUniq);
		ArrayList<String> ret=new ArrayList<String>();
		for(int i=0;i<origUniq.size();i++){
			boolean foundBetter=false;
			for(int j=0; j<origUniq.size();j++){
				if(i==j) continue;
				if(this.nlp.removeStopWordsAndStem(origUniq.get(j)).contains(this.nlp.removeStopWordsAndStem(origUniq.get(i))) 
						//&& !this.nlp.removeStopWordsAndStem(origUniq.get(j)).contains(this.nlp.removeStopWordsAndStem(origUniq.get(i)))
						&& origUniq.get(j).length()>origUniq.get(i).length()
						) {
					foundBetter=true; break;
					}
			}
			if(!foundBetter) 
				ret.add(origUniq.get(i));
		}
			
		return ret;
	}


	private void bestMatchOfSocieties() {		
		HashSet<String> mentionToks = new HashSet<String>(Arrays.asList(this.nlp.removeStopWordsAndStem(this.mention).toUpperCase().split("\\s+")));

		this.societies=new ArrayList<String>(this.societiesLookUp);
		ArrayList<String> socs = new ArrayList<String>(this.societies);
		for (String soc : socs) {
			
			//TODO - this is generally not needed. but unfortunately, there are things in dictionary such as "Animal Welfare" and "Applied Biosystems"
			if(!this.nlp.isSociety(soc)){
				this.societies.remove(soc);
				continue;
			}
			
			if(this.nlp.isConference(soc)||this.nlp.notSocieties.contains(soc.toUpperCase())//things like Congress of the Asian Society of Transplantation is in dictionary and Mental Health which is in negative dictionary is not picked.
					||this.dict.allCities.contains(soc.toUpperCase())||this.dict.allRegions.contains(soc.toUpperCase())||this.dict.countryTable.keySet().contains(soc.toUpperCase())){
				this.societies.remove(soc);
				continue;
			}
			
			//also remove lookup from abbreviation expansions if they don't share a common word
			HashSet<String> socToks = new HashSet<String>(Arrays.asList(this.nlp.removeStopWordsAndStem(soc).toUpperCase().split("\\s+")));
			boolean found=false;
			for (String socTok : socToks) {
				if(!this.nlp.stopwords.contains(socTok) && !this.nlp.socKeywords.contains(socTok) && mentionToks.contains(socTok))
					found=true;
			}
			if(!found){
				this.societies.remove(soc);
				continue;
			}
				
			
			for (Abbr abbr : abbrs) {
				if((abbr.longform.contains(soc)&&!soc.contains(abbr.longform))
					)//this doesn't always work - 2014 Cadaver Course on Pain Medicine and Musculoskeletal Ultrasound- American Society of Regional Anesthesia and Pain Medicine (ASRA) 	|| this.mentionTable.get(soc).equals(abbr.shortform))//prefer long-form over expansion found in society master database
				{
					if(this.nlp.isConference(abbr.longform)||this.nlp.notSocieties.contains(abbr.longform.toUpperCase())//things like Congress of the Asian Society of Transplantation is in dictionary and Mental Health which is in negative dictionary is not picked.
							||this.dict.allCities.contains(abbr.longform.toUpperCase())||this.dict.allRegions.contains(abbr.longform.toUpperCase())||this.dict.countryTable.keySet().contains(abbr.longform.toUpperCase()))
						continue;
					
					this.societies.remove(soc);
					this.societies.add(abbr.longform);
					this.societyAbbreviationTable.put(abbr.longform, abbr.shortform);
				}
			}
		}
		
		//remove one word society names from lookup
		for (String socl : this.societiesLookUp) {
			if(socl.split("\\s+").length<2){
				this.societies.remove(socl);//if it is already replaced by the longer abbreviation, nothing to worry.
			}
		}
		
		//consider only those from NER which don't have an overlap or if they do use some criterion to replace
		for (String socn : this.societiesNER) {
			if(socn.split("\\s+").length<=1||socn.contains("-")) continue;
			
			//American Society of Pediatric Endocrinology / XXVIII Reunion Anual de
			if(socn.contains("/")) continue;
			//Congress of Asian Society for Child and Adolescent
			if(socn.contains("Congress")&&this.nlp.isSociety(socn.replaceAll("\\s*Congress\\s*", " ").trim())) continue;
			
			String socnNorm = this.nlp.removeStopWordsAndStem(socn);
			boolean foundBetter=false;
			for (int i = 0; i < this.societies.size(); i++) {
				String soc=this.societies.get(i);
				String socNorm = this.nlp.removeStopWordsAndStem(soc);
				if(socNorm.contains(socnNorm)||socnNorm.contains(socNorm)) {foundBetter=true; break;}
				/*else if(socn.contains(soc) && !nlp.isSociety(socn.replaceFirst(soc, ""))) {
					this.societies.set(i, socn);foundBetter=true;
				}*/
			}
			
			/*for (String soc : this.societies) {
				if(soc.contains(socn)||socn.contains(soc)) {foundBetter=true; break;}
			}*/
			if(!foundBetter) this.societies.add(socn);
		}
		
		//most stringent criteria possible for chunking output
		/*socs = new ArrayList<String>(this.societiesChunking);
		for (String soc : socs) {
			
			for (Abbr abbr : abbrs) {
				if(abbr.longform.contains(soc)&&!soc.contains(abbr.longform)){
					if(this.nlp.isConference(abbr.longform))
						continue;
					
					this.societies.add(abbr.longform);
					this.societyAbbreviationTable.put(abbr.longform, abbr.shortform);
				}
			}
		}*/
		
		for (Abbr abbr : abbrs) {
			if(this.nlp.isConference(abbr.longform)||this.nlp.notSocieties.contains(abbr.longform.toUpperCase())//things like Congress of the Asian Society of Transplantation is in dictionary and Mental Health which is in negative dictionary is not picked.
					||this.dict.allCities.contains(abbr.longform.toUpperCase())||this.dict.allRegions.contains(abbr.longform.toUpperCase())||this.dict.countryTable.keySet().contains(abbr.longform.toUpperCase()))
				continue;
			boolean foundBetter=false;
			for (int i = 0; i < this.societies.size(); i++) {
				String soc=this.societies.get(i);
				if(soc.contains(abbr.longform)){//||abbr.longform.contains(soc)) {//2nd Biennial Scientific Meeting of the International Federation of the Surgery of Obesity (Asia Pacific Chapter) held jointly with the Asia-Pacific Chapter (APC) and Japanese Society for Surgery of Obesity and Metabolic Disorders (JSSO). Society for Surgery of Obesity was found as society
					foundBetter=true; break;
				}
			}
			if(!foundBetter && this.nlp.isSociety(abbr.longform)) this.societies.add(abbr.longform);
		}
		
		this.societies=includeLongerOnesOnly(this.societies);
		this.societies=mergeOverlapping(this.societies);
		
	}


	private ArrayList<String> mergeOverlapping(ArrayList<String> orig) {
		ArrayList<String> ret=new ArrayList<String>();
		for(int i=0;i<orig.size();i++){
			if(!this.mention.contains(orig.get(i))) continue;
			int startFirst=this.mention.indexOf(orig.get(i));
			int endFirst = startFirst+orig.get(i).length();
			
			for(int j=0; j<orig.size();j++){
				if(i==j) continue;
				int startSecond= this.mention.indexOf(orig.get(j));
				int endSecond=startSecond+orig.get(j).length();
				if(startFirst<startSecond&&startSecond<endFirst){
					orig.set(i, this.mention.substring(startFirst, Math.max(endFirst, endSecond)));
					endFirst=startFirst+orig.get(i).length();
					orig.set(j, "");
				}
			}
			
		}
		
		for(int i=0;i<orig.size();i++){
			if(!orig.get(i).equals("")) 
				ret.add(orig.get(i));
		}
		return ret;
	}

	private String normalizeConferenceName(String meetingMention) {
		
		//no special characters
		meetingMention=this.dict.deAccent(meetingMention);
		
		//removing all brackets for now
		meetingMention=meetingMention.replaceAll("\\([^\\)]+\\)", "").replaceAll("\\[[^\\]]+\\]", "").replaceAll(" MEETING CANCELLED", "");
		/*//this might have to be checked. Abbreviations - with years included surrounded by (). Example:(COSM) and 50th Annual SOC1 (ANS)
		//TODO removing abbreviations for now. might have to retain them later on
		meetingMention=meetingMention.replaceAll("\\([A-Z0-9\\s/a-z]+\\)", "");
		//definitely replacing all those with year even if they start with smal caps
		meetingMention=meetingMention.replaceAll("\\(.*[12]\\d\\d\\d.*\\)", "");
		*/		
		
		
		//addressing issues like
		//2012 Alzheimer's Association International Conference (AAIC) - July
		//2012 AMA Guides Fifth Edition Impairment Rating Course - Orlando
		while(meetingMention.contains("-")){
			String suffix = meetingMention.substring(meetingMention.lastIndexOf("-"));
			String suffixTrim = suffix.substring(1).trim();
			//removing the suffix if it has 
			if(suffixTrim.split("\\s+",-1).length<=2 && this.dict.months.contains(suffixTrim.split("\\s+",-1)[0].toUpperCase())||this.dict.allCities.contains(suffixTrim.toUpperCase().split(",",-1)[0])||this.dict.allRegions.contains(suffixTrim.toUpperCase()))
				meetingMention=meetingMention.substring(0, meetingMention.lastIndexOf("-"));
			else
				break;
		}
		
		//example - Annual Cardiovascular Conference at Beaver Creek
		if (meetingMention.matches(".* at [^ ]+ ?[^ ]*")) {
			String suffix=meetingMention.substring(meetingMention.lastIndexOf(" at ")+4).trim().toUpperCase();
			if (!this.dict.origEnglishWords.contains(suffix) && (this.dict.allCitiesSmall.contains(suffix)//the big dictionary has too many general cities
					|| this.dict.allRegions.contains(suffix)))
				meetingMention = meetingMention.substring(0,
						meetingMention.lastIndexOf(" at "));
		}
		
		if (meetingMention.contains(" ")) {
			String lastWord = meetingMention.substring(
					meetingMention.lastIndexOf(" ")).trim().toUpperCase();
			if (this.dict.months.contains(lastWord)
					|| 
					(!this.dict.origEnglishWords.contains(lastWord) && !this.dict.lastNames.contains(lastWord) && (this.dict.allCitiesSmall.contains(lastWord)//the big dictionary has too many general cities
					|| this.dict.allRegions.contains(lastWord))))
				meetingMention = meetingMention.substring(0,
						meetingMention.lastIndexOf(" "));
		}
		
		List<String> words = Arrays.asList(meetingMention.split("\\s+"));
		if(words.size()>=2&&this.dict.allCities.contains((words.get(words.size()-2)+" "+words.get(words.size()-1)).toUpperCase())&&this.dict.allRegions.contains((words.get(words.size()-2)+" "+words.get(words.size()-1)).toUpperCase())){
			words.set(words.size()-2, ""); words.set(words.size()-1, "");
		}
		
		
		ArrayList<String> remWords = new ArrayList<String>();
		for (String word : words) {
			if(word.matches(""))
				continue;
			if(word.matches("\\d+(st|nd|rd|th|\\.)"))//1st, 2432th
				continue;	
			if(word.matches("['?]?(19|20)\\d\\d[:']?"))//1954, 2012
				continue;

//			if(word.matches("M{0,4}(CM|CD|D?C{0,3})(XC|XL|L?X{0,3})(IX|IV|V?I{0,3})(st|nd|rd|th)?") //exact roman
			if(word.matches("[MCDXLIV]+(st|nd|rd|th)?") //inexact roman -- captures inexact romans like IIX
					&&!word.matches("(st|nd|rd|th)?")
					&& !word.equals("I")
					&& !word.equals("II")
					&& !word.equals("III")
					&& !word.equals("IV")
					&& !word.equals("V")
					&& !word.equals("M")
					&& !word.equals("C")
					&& !word.equals("D")
//					&& !word.equals("X") //removing as per QC
					&& !word.equals("L")
					&& !word.equals("MD")
					&& !word.equals("CML")
					&& !word.equals("MI"))
					//gives exceptions to less than 6 and single letter like C, etc.
				continue;	
			
			if(word.toLowerCase().matches("(first|second|third|fourth|fifth|sixth|seventh|eighth|ninth|tenth|eleventh|twelfth|thirteenth|fourteenth|fifteenth|sixteenth|seventeenth|eighteenth|nineteenth|twentieth|twenty|thirty|forty|fifty|sixty|seventy|eighty|ninty|hundred)"))
				continue;
			/*if(word.matches("\\([A-Z0-9\\s]+\\)"))
				continue;*/

			if(word.matches("the"))
				continue;
			
			if(word.matches("\\d+[a-z°]*"))
				continue;
			
			if(word.matches("\\d+(st|nd|rd|th|\\.)?Annual"))
				word="Annual";
				
			remWords.add(word);
		}
		
		
		String curName = StringUtils.join(remWords, " ");
		
		//11 th National Congress of International Epidemiological Association with Italian Society of Urology Oncology
		curName=curName.replaceAll("\\d+ (st|nd|rd|th|\\.)", "").replaceAll("St\\.", "");
		
		
		
		//Gopal wants to remove hyphens, colons, numbers and dots, and ' (Advanced Angioplasty '98)
		//and also " ) (
		curName=curName.replaceAll("[:\\-\\d\\.'\"\\(\\)\\[\\]<>]", "").replaceAll(" (st|nd|rd|th) ", " ").replaceAll("\\s+", " ").trim();
		//and then there are non-alpha symbols at the beginning or end
		curName=this.nlp.removeTrailingSymbols(curName);
		//System.out.println(curName);
		
		
		
		return curName;
	}


	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void main(String[] args) throws IOException {
		DetailedNLPer nlp = new DetailedNLPer();
		ConferenceNameSplitter confSplitter = new ConferenceNameSplitter();
		Dictionaries dict = new Dictionaries();
		
		Meeting aMeeting = new Meeting("28th Annual Meeting of the Latin American Society of Pediatric Endocrinology / XXVIII Reunion Anual de la Sociedad Latinoamericana de Endocrinologia Pediatrica (SLEP)",nlp, confSplitter, dict);
		aMeeting.print();
		System.out.println("");
		
		aMeeting = new Meeting("Developing Biostatistics Resources at an Academic Health Center",nlp, confSplitter, dict);
		aMeeting.print();
		System.out.println("");
		
		Scanner sc = new Scanner(System.in);
		while(sc.hasNextLine()){
			aMeeting = new Meeting(sc.nextLine(),nlp, confSplitter, dict);
			aMeeting.print();
			System.out.println("");
		}
		sc.close();
		
	}


}
