package parsing;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.text.DateFormatSymbols;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import neobio.alignment.BasicScoringScheme;
import neobio.alignment.NeoBio;
import neobio.alignment.PairwiseAlignmentAlgorithm;
import neobio.alignment.SmithWaterman;

public class Dictionaries implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5852666838523061960L;
	HashSet<String> lastNames;
	
	HashMap<String, String> countryTable;
	HashMap<String, String> Id2countryTable;
	
	HashMap<String, String> uniqueRegionTable;
	HashMap<String, String> USAabbrev2FullState;
	HashSet<String> allRegions;
	
	HashMap<String, String> uniqueCityTable;
	HashSet<String> allCities;
	HashSet<String> allCitiesSmall;
	//HashMap<String, String> usaPopularCities;
	
	public HashSet<String> orgKeywords;	
	public HashSet<String> universityKeywords;	
	public HashSet<String> level2OrgKeywords;	
	
	HashSet<String> mainOrgKeywords;	
	HashSet<String> keywords;
	HashMap<String, String> orgAbbreviations;
	
	HashSet<String> addressWords;
	
	HashSet<String> orgNames;
	HashMap<String, String> org2CountryTable;
	HashSet<String> deptNames;
	public HashSet<String> englishWords;
	public HashSet<String> origEnglishWords;
	
	public HashMap<String, String> ccTLDs;
	public HashMap<String, String> country2ccTLDs;
	HashSet<String> directionWords;
	
	ArrayList<String> months;
	HashMap<String, String> regionExpansions;
	public Dictionaries() throws IOException {
		
		/*algorithm = new SmithWaterman();
		BasicScoringScheme scoring = new BasicScoringScheme (1, -1, -1);
		// set scoring scheme
		algorithm.setScoringScheme(scoring);*/
		
		orgKeywords=new HashSet<String>(); keywords=new HashSet<String>();
		orgKeywords.addAll(Arrays.asList(new String[]{"ACADEMY", "ASSOCIATES", "ASSOCIATION", "CENTER", "CENTERS", "CENTERS", "CENTRE", "CENTRES", "CLINIC", "COLLEGE", "COMPANIES", "COMPANY", "CORPORATION", "DEPARTMENT", "DEPARTMENTS", "DEPT.", "DEVELOPMENT", "DIVISION", "DIVISIONS", "ENGINEERING", "ENGINEERING", "FACULTAT", "FACULTY", "GRADUATE", "GROUP", "HOSPITAL", "HOSPITALS", "INC", "INC.", "INSTITUTE", "INSTITUTES", "INSTITUTION", "INSTITUTIONS", "LAB", "LABORATORY", "LLC", "LLC.", "LLP", "LLP.", "LTD", "LTD.", "MEDICAL", "#MEDICINE", "PHARMA", "#PROGRAM", "PROGRAMS", "PROJECT", "RESEARCH", "SCHOOL", "SCIENCES", "SECTION", "SERVICE", "SPECIALISTS", "STUDIES", "SYSTEM", "UNIT", "UNIVERSITY", "UNIV", "UNIVERSIDAD", "UNIVERSIDADE", "TRUST", "COLLEGE", "BRANCH", "BIOSCIENCES",
				"ASOCIACION","ASSOCIACION", "ARBEITSGEMEINSCHAFT", "ASSOCIAO", "ASOCIACIN", "ALLIANCE", "INSTITUUT"}));
		
		universityKeywords=new HashSet<String>(); universityKeywords.addAll(Arrays.asList(new String[]{"UNIVERSITY", "UNIV", "UNIVERSIDAD", "UNIVERSIDADE", "UNIVERSITE", "UNIVERSITAT", "UNIVERSITAIRES", "UNIVERSITA", "UNIVERSITATSKLINIKUM", "UNIVERZITY", "UNIVERSITATSMEDIZIN", "JUSTUS-LIEBIG-UNIVERSITAT", "UNIVERSITATSSPITAL", "UNIVERSITET", "UNIVERSITYOF", "UNIVERSITARIA", "UNIVERSITETSHOSPITAL", "UNIVERSIADE", "UNIVERSITAIRE", "UNIVERISTY", "UNIVERSITI", "UNIVERSITEIT", "UNIVERISITY", "UNIVERSITAET","UNIWERSYTET","AKADEMIA", "UNIVERSITARIO", "UNIVERSTAIRES", "UNIVERSIAT"
				,"CHU", "ECOLE"}));
		
		level2OrgKeywords=new HashSet<String>(); level2OrgKeywords.addAll(Arrays.asList(new String[]{"US", "HOSPITAL", "HOSPITALS", "INFIRMARY", "HOPITAL", "CENTAR", "OSPEDALE", "ACADEMY", "OSPEDALIERA", "VETERANS", "HOSPITALARIO", "HOPITAUX", 
				"NETWORK"//LEHIGH VALLEY HEALTH NETWORK,
				, "KLINIK", "KLINIKUM", "KRANKENHAUS", "KINDERKRANKENHAUS", "HOCHSCHULE", "INSTITUT", "BUNDESWEHRZENTRALKRANKENHAUS", "UNFALLKRANKENHAUS", "INSTITUTO"
				, "FOUNDATION"//North Estonia Medical Centre Foundation ,  Psychiatry Clinic, Tallinn ,  Estonia.
				, "HUPITAL", "CONSULTANTS", "INST", "PHARMACEUTICALS"
				}));
		
		mainOrgKeywords=new HashSet<String>(); mainOrgKeywords.addAll(Arrays.asList(new String[]{"CLINIC", "CLINICA", "COMPANIES", "COMPANY", "CORPORATION", "GROUP", "HOSPITAL", "HOSPITALS", "INC", "INC.", "INSTITUTE", "INSTITUTES", "INSTITUTION", "INSTITUTIONS", "LLC", "LLC.", "LLP", "LLP.", "LTD","LIMITED",  "LTD.", "UNIVERSITY", "UNIV", "UNIVERSIDAD", "UNIVERSIDADE", "UNIVERSITAT", "UNIVERSITE", "UNIVERSITAIRES", "UNIVERSITA", "UNIVERSITATSKLINIKUM", "UNIVERZITY","UNIVERSITATSMEDIZIN", "UNIVERSITATSSPITAL", "UNIVERSITET", "UNIVERSITYOF", "UNIVERSITARIA", "UNIVERSITETSHOSPITAL", "UNIVERSIADE", "UNIVERSITAIRE", "UNIVERISTY", "UNIVERSITI", "UNIVERSITEIT", "UNIVERISITY", "UNIVERSITAET", "R&D",
				"INFIRMARY","SCHOOL","HOPITAL", "COLLEGE","INSTYTUT","UNIWERSYTET","AKADEMIA", "ACADEMIA", "ACADEMIC",
				"UNIVERSITARIO", "OSPEDALIERA",
				"TRUST",
				"CENTER" //check if this makes sense
		}));
		mainOrgKeywords.addAll(level2OrgKeywords);mainOrgKeywords.addAll(universityKeywords);
		orgKeywords.addAll(mainOrgKeywords);
		keywords.addAll(orgKeywords);
		
		countryTable = new HashMap<String, String>();
		Id2countryTable = new HashMap<String, String>();
		ccTLDs=new HashMap<String, String>();country2ccTLDs=new HashMap<String, String>();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("dict/countries_sid.tsv"), "UTF-8"));
		br.readLine();//first line doesn't contains headers
		while(br.ready()){
			String splits[] = this.deAccent(br.readLine()).toUpperCase().split("\t");
				
			for (int i = 1; i < splits.length; i++) {
				splits[i]=splits[i].replace('"', '\0').trim();
				if(//i!=3 && 
						i!=2 && splits[i].length()>0) //i=3 -- dealing with capitals by forcing them to be cities -- for now including capitals also for country definition (PARIS)
					//i=2 -- compressing the ccTLDS as they are giving false matches. -- MG, BRASIL.	country=MADAGASCAR
					countryTable.put(splits[i],splits[1]);
			}
			Id2countryTable.put(splits[0].trim(), splits[1]);
			if(splits[2].length()>0){
				ccTLDs.put(splits[2], splits[1]);
				country2ccTLDs.put(splits[1], splits[2]);
			}
		}
		br.close();
		
		uniqueRegionTable = new HashMap<String, String>();
		allRegions = new HashSet<String>();
		HashSet<String> duplicates = new HashSet<String>();
		br = new BufferedReader(new InputStreamReader(new FileInputStream("dict/regions_sid.tsv"), "UTF-8"));

		br.readLine();//first line doesn't contains headers
		while(br.ready()){
			String splits[] = this.deAccent(br.readLine()).toUpperCase().split("\t");
			for (int i = 1; i < splits.length; i++) {
				splits[i]=splits[i].trim();
			}
			if(uniqueRegionTable.containsKey(splits[2]))//make an exception for United States -- priority 
				duplicates.add(splits[2]);
			
			uniqueRegionTable.put(splits[2], Id2countryTable.get(splits[1]));
			allRegions.add(splits[2]);allRegions.add(splits[3]);
			if(splits[1].equals("254")){
				//example - CA standas for Canada and California
				if(countryTable.containsKey(splits[3]))
					countryTable.remove(splits[3]);
				else
					uniqueRegionTable.put(splits[3], Id2countryTable.get(splits[1]));
			}
		}
		for (String dupString : duplicates) {
			uniqueRegionTable.remove(dupString);
		}
		for (String country : countryTable.keySet()) {
			uniqueRegionTable.remove(country);//sometimes country names are repeated as regions
		}
		br.close();
		
		USAabbrev2FullState = new HashMap<String, String>();
		br = new BufferedReader(new InputStreamReader(new FileInputStream("dict/USA_states.txt"), "UTF-8"));
		while(br.ready()){
			String splits[] = this.deAccent(br.readLine()).toUpperCase().split("\t");
			USAabbrev2FullState.put(splits[0], splits[1]);
		}
		br.close();
		
		uniqueCityTable = new HashMap<String, String>();
		allCities = new HashSet<String>();
		duplicates = new HashSet<String>();
		br = new BufferedReader(new InputStreamReader(new FileInputStream("dict/cities_sid.tsv"), "UTF-8"));
		//br = new BufferedReader(new InputStreamReader(new FileInputStream("/home/solr/ConfParser/dict/test/cities_sid.tsv"), "UTF-8"));
		br.readLine();//first line contains headers
		while(br.ready()){
			String splits[] = this.deAccent(br.readLine()).toUpperCase().split("\t");

			for (int i = 1; i < splits.length; i++) {
				splits[i]=splits[i].trim();
			}
			
			splits[3]=splits[3].replaceAll("\\. ", " ");
			
			if(uniqueCityTable.containsKey(splits[3])) 
				duplicates.add(splits[3]);
			uniqueCityTable.put(splits[3], Id2countryTable.get(splits[1]));
			allCities.add(splits[3]);
			
			//remove cities like Georgia from countries
			//if(countryTable.containsKey(splits[3])&&!duplicates.contains(splits[3]))
				countryTable.remove("GEORGIA");
		}
		for (String dupString : duplicates) {
			uniqueCityTable.remove(dupString);
		}

		br.close();
		allCitiesSmall = new HashSet<String>(allCities);
		
		br = new BufferedReader(new InputStreamReader(new FileInputStream("dict/worldcitiespop_uniqCities.txt"), "UTF-8"));
		//br = new BufferedReader(new InputStreamReader(new FileInputStream("/home/solr/ConfParser/dict/test/worldcitiespop_uniqCities.txt"), "UTF-8"));
		while(br.ready()){
			allCities.add(this.deAccent(br.readLine().replaceAll("\\. ", " ")));
		}
		br.close();
		br = new BufferedReader(new InputStreamReader(new FileInputStream("dict/QCCities.txt"), "UTF-8"));
		while(br.ready()){
			allCities.add(this.deAccent(br.readLine().replaceAll("\\. ", " ").toUpperCase()));
		}
		br.close();
		
		//a capital is always a city even if it is a duplicate
		br = new BufferedReader(new InputStreamReader(new FileInputStream("dict/countries_sid.tsv"), "UTF-8"));
		br.readLine();//first line doesn't contains headers
		while(br.ready()){
			String splits[] = this.deAccent(br.readLine()).toUpperCase().split("\t");
			splits[3]=splits[3].trim();
			if(splits[3].length()>0)
				uniqueCityTable.put(splits[3],splits[1]);
		}
		br.close();
		
		br = new BufferedReader(new InputStreamReader(new FileInputStream("dict/USA popular cities.txt"), "UTF-8"));
		while(br.ready()){
			uniqueCityTable.put(this.deAccent(br.readLine()).toUpperCase().trim(), Id2countryTable.get("254"));
		}

		br.close();
		
		
		
		addressWords = new HashSet<String>(Arrays.asList(new String[]{"ALLEY", "AVE", "AVENUE", "BEND", "BLVD", "BOULEVARD", "BLDG", "BUILDING", "CIR", "CIRCLE", "COURT", "CR", "CREEK", "CRSG", "CROSSING", "CT", "DR", "DRIVE", "FL", "FLOOR", "HIGHWAY", "HEIGHTS", "LANE", "LN", "LOOP", "PARKWAY", "PASSAGE", "PKWY", "PL", "PLACE", "PLAZA", "RAMP", "RD", "ROAD", "ROUTE", "SQ", "SQUARE", "ST", "STREET", "STREETS", "SUITE", "TERRACE", "TRAIL", "TRL", "WARD", "WAY", "WING"}));
		directionWords = new HashSet<String>(Arrays.asList(new String[]{"NORTH", "SOUTH", "WEST", "EAST"}));
		
		orgNames = new HashSet<String>();
		br = new BufferedReader(new InputStreamReader(new FileInputStream("dict/orgNamesFromIndiaTeam.txt"), "UTF-8"));
		br.readLine();//first line doesn't contains headers
		while(br.ready()){
			String line = this.deAccent(br.readLine());
			if(line.length()>0)
				orgNames.add(cleanAff(line));
		}
		br.close();
		
		deptNames = new HashSet<String>();
		br = new BufferedReader(new InputStreamReader(new FileInputStream("dict/deptNamesFromIndiaTeam.txt"), "UTF-8"));
		br.readLine();//first line doesn't contains headers
		while(br.ready()){
			String line = this.deAccent(br.readLine());
			if(line.length()>0)
				deptNames.add(cleanAff(line));
		}
		br.close();
		
		englishWords = new HashSet<String>();
		br = new BufferedReader(new InputStreamReader(new FileInputStream("dict/allEnglishWords.txt"), "UTF-8"));
		br.readLine();//first line doesn't contains headers
		while(br.ready()){
			String line = br.readLine();
			if(line.length()>0)
				englishWords.add(cleanAff(line));
		}
		br.close();
		origEnglishWords=new HashSet<String>(englishWords);
		//englishWords.removeAll(allCitiesSmall);
		englishWords.removeAll(allCities);
		englishWords.removeAll(allRegions);englishWords.removeAll(countryTable.keySet());englishWords.removeAll(USAabbrev2FullState.values());
		
		orgAbbreviations = new HashMap<String, String>();
		br = new BufferedReader(new InputStreamReader(new FileInputStream("dict/orgAbbreviationsCompiledDec2014.txt"), "UTF-8"));
		while(br.ready()){
			//String splits[] = this.deAccent(br.readLine()).toUpperCase().split("\t");
			String splits[] = this.deAccent(br.readLine()).split("\t");
			if(splits.length!=2 || splits[1].matches("\\s+")) continue;
			if(splits[0].length()<=2) continue;//abbreviations for regions - VT is Vermont, not Virginia Tech
			orgAbbreviations.put(splits[0], splits[1]);
		}
		br.close();
		
		org2CountryTable=new HashMap<String, String>();
		br = new BufferedReader(new InputStreamReader(new FileInputStream("../NLP Process/Organization Master.tsv.txt"), "UTF-8"));
		br.readLine();
		while(br.ready()){
			String splits[] = this.deAccent(br.readLine()).toUpperCase().split("\t");

			if(splits.length<14||!ccTLDs.containsKey(splits[13])) continue;
			String org=splits[2];
			String country = ccTLDs.get(splits[13]);
			//String city = ccTLDs.get(splits[12]);

			if(splits[5].equalsIgnoreCase("MAIN")){
				org2CountryTable.put(org, country);
			}
		}
		br.close();
		
		
		//months
		String[] longMonths = new DateFormatSymbols().getMonths();
		String[] shortMonths = new DateFormatSymbols().getShortMonths();
		this.months=new ArrayList<String>();
		this.months.addAll(Arrays.asList(longMonths));
		this.months.addAll(Arrays.asList(shortMonths));
		this.months.remove("");
		for(int i=0; i<this.months.size();i++) this.months.set(i, this.months.get(i).toUpperCase());
		
		//last names
		lastNames = new HashSet<String>();
		br = new BufferedReader(new InputStreamReader(new FileInputStream("dict/lastNames_CMU.txt"), "UTF-8"));
		while(br.ready()){
			String line = br.readLine();
			if(line.length()>0)
				lastNames.add(line.toUpperCase());
		}
		br.close();

		regionExpansions = new HashMap<String, String>();
		br = new BufferedReader(new InputStreamReader(new FileInputStream("dict/RegionCountryState_Mapping.txt"), "UTF-8"));
		br.readLine();
		while(br.ready()){
			String[] splits = br.readLine().split("\t");
			if(splits.length==5)
				regionExpansions.put(splits[1].trim()+"_"+splits[3].trim(), splits[4].trim().toUpperCase());
		}
		br.close();
	}
	
	public String deAccent(String str) {
	    String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD); 
	    Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
	    return pattern.matcher(nfdNormalizedString).replaceAll("").replaceAll("[^\\p{ASCII}]", ""); //for non-ascii characters like º degree symbol
	}

	public static void main(String[] args) throws IOException {
		Dictionaries d= new Dictionaries();
		System.out.println(d.deAccent("São Paulo"));
		System.out.println(d.orgAbbreviations.get("NCI"));
		System.out.println(d.replaceOrgAbbr("UCSF Medical Center, Department of Urology, Ambulatory Care Center, Suite A633, San Francisco, CA 94117, USA. daaronson@urology.ucsf.edu", "UCSF Medical Center, Department of Urology, Ambulatory Care Center, Suite A633, San Francisco, CA 94117, USA. daaronson@urology.ucsf.ed"));
		System.out.println(d.orgAbbreviations.get("UCSF"));
		System.out.println(d.countryTable.size());
		System.out.println(d.countryTable.get("USA"));
		System.out.println(d.uniqueRegionTable.size());
		System.out.println(d.uniqueRegionTable.get("ANDHRA PRADESH"));
		System.out.println(d.uniqueCityTable.size());
		System.out.println(d.uniqueCityTable.get("SAN DIEGO"));
	}

	Pattern abbr=Pattern.compile("(\\s|^)([A-Z]\\.\\s*)+");
	public String cleanAff(String aff) {
		Matcher m=abbr.matcher(aff);
		while(m.find()){
			aff=aff.substring(0,m.start())+m.group().replaceAll("\\.\\s*", "")+ " "+aff.substring(m.end()).trim();
			m.reset(aff);
		}
		
		//clean numbers and * in front
		///allowing 1st or 1ST
		if(aff.matches("[0-9\\*]\\S{4,}.+"))
			aff=aff.substring(1).trim();
		aff=aff.replaceAll("Authors' Affiliations:", "")
				.replaceAll("\\s*-\\s*", " ")//University of California-San Francisco, 94117, USA. daaronson@urology.ucsf.edu
				.replaceAll("(.*[A-Za-z])[.,:?:]*", "$1").trim();
		
		return this.deAccent(aff.toUpperCase().replaceAll(", INC.", " INC.").replaceAll(", L\\.?L\\.?C\\.?,", " LLC,").replaceAll(",? CO\\.,? ", " COMPANY ").replaceAll(", L\\.?L\\.?P\\.?,", " LLP,").replaceAll(", L\\.?T\\.?D\\.?,", " LTD,").replaceAll("\"","").replaceAll("VETERI?ANS (AFFAIRS|ADMINISTRATION) MEDICAL", "VA MEDICAL"));
	}

	public boolean allEnglishWords(String org) {
		String[] splits = org.split("\\s+");
		for (String split : splits) {
			if(!englishWords.contains(split))
				return false;
		}
		return true;
	}

	public String expandAbbr(String string) {
		if(string.equals("DEPT")) string="DEPARTMENT";
		if(string.equals("UNIVESITY")) string="UNIVERSITY";
		if(string.equals("UNIVERSITAT")) string="UNIVERSITY OF";
		if(string.matches("UNIVERSIT[A-Z]{0,3}")) string="UNIVERSITY";
		if(string.equals("LMU,")) string="LUDWIG MAXIMILIANS UNIVERSITY MUNICH,";
		if(string.equalsIgnoreCase("Lab,")) 
			string= "LABORATORY,";
		
		return string;
	}

	//Pattern abbrPattern = Pattern.compile("[A-Z]+\\-?[A-Z]+");
	public String replaceOrgAbbr(String aff, String origMention) {
		String[] toks=aff.split("\\s+|\\-");//Department of Pediatrics, Los Angeles Biomedical Research Institute at Harbor-UCLA Medical Center at David Geffen School of Medicine, 1124 West Carson Street, Torrance 90502, USA. vrehan@labiomed.org

		String ret="";
		for (String tok : toks) {
			if(tok.endsWith(",")){//Division de physiopathologie clinique, CHUV, 1011 Lausanne.
				tok=tok.substring(0, tok.length()-1);
				if(this.orgAbbreviations.containsKey(tok)
						&& origMention.contains(tok))//make sure now that it is also present as uppercase in the original mention
					ret+=this.orgAbbreviations.get(tok);
				else
					ret+=tok;
				ret+=", ";
			}
			else{
				if(this.orgAbbreviations.containsKey(tok)
						&& origMention.contains(tok))//make sure now that it is also present as uppercase in the original mention
					ret+=this.orgAbbreviations.get(tok);
				else
					ret+=tok;
				ret+=" ";
			}
		}
		return ret.trim();
		/*HashSet<String> matches;
		Matcher m = abbrPattern.matcher(aff);
		while(m.find()){
			String match = m.group();
			
			System.out.println(aff.substring(0,m.start()))
		}*/
	}

	public double matchingNonOrgWords(String orgUDict, String orgU2, HashSet<String> stopwordsU) {
		int ed = NeoBio.editDistance(orgUDict, orgU2);
		if(ed<=2){
			return 1.0;
		}
		
		for (String dist : this.orgDistinguishers) {
			if(orgUDict.contains(dist)&&!orgU2.contains(dist)) return 0;
			if(orgU2.contains(dist)&&!orgUDict.contains(dist)) return 0;
		}
		
		//"\\s+|'|-"
		ArrayList<String> orgU1List= new ArrayList<String>(Arrays.asList(orgUDict.split("\\W+")));
		orgU1List.removeAll(this.orgKeywords);
		orgU1List.removeAll(stopwordsU);
		
		//"\\s+|'|-"
		ArrayList<String> orgU2List=new ArrayList<String>(Arrays.asList(orgU2.split("\\W+")));
		orgU2List.removeAll(this.orgKeywords);
		orgU2List.removeAll(stopwordsU);
		
		if(orgU1List.size()>1&&orgU2List.size()>1&&!this.englishWords.containsAll(orgU1List)&&!this.englishWords.containsAll(orgU2List)){
			//TODO: minimum size just 1???
			orgU1List.removeAll(this.englishWords);
			orgU2List.removeAll(this.englishWords);
		}
		
		double score;
		if(orgU1List.size()==0||orgU2List.size()==0)
			return 0;
		String orgUDict_=String.join("     ", orgU1List);
		String orgU2_=String.join("     ", orgU2List);
		/*String orgUDict_=String.join(" ", orgU1List);
		String orgU2_=String.join(" ", orgU2List);*/
		score=NeoBio.getSmithWatermanMatch("     "+orgUDict_+"     ", "     "+orgU2_+"     ");//5 times more weight to " " - BioM and Biomedicine.
		//might be sufficient just at the end
		//University of Texas M D Anderson Cancer Center
		//University of Texas MD Anderson Cancer Center
		
		//Nagaski University School of Medicine and Nagasaki University not matching
		//moving this rule to Affiliation and impose that the city should be the same if the first character is not
		/*if((orgU1List.size()==1||orgU2List.size()==1) &&
				//(score<1.0 && !(orgU1.charAt(0)==orgU2.charAt(0)&&orgU1.charAt(orgU1.length()-1)==orgU2.charAt(orgU2.length()-1)))//University of Genova and University of Genoa
				(score<1.0 && !(orgUDict_.charAt(0)==orgU2_.charAt(0)&&orgUDict_.charAt(orgUDict_.length()-1)==orgU2_.charAt(orgU2_.length()-1)))//Nagaski University School of Medicine and Nagasaki University
				)
			score= 0;//Kerman University of Medical Sciences	Iran University of Medical Sciences
*/		
		
		//TODO: there needs to be better misspelling benefit of doubt. The below does more damage than benefit.
		/*if(score<Affiliation.SW_THRESHOLD){
			for (String string : orgU2List) {//orgU2 is the one that is from input
				if(
						!this.englishWords.contains(string)&&!this.allCities.contains(string)&&!this.allRegions.contains(string)&&!this.countryTable.containsKey(string)
						&&!this.orgAbbreviations.containsKey(string)//Ucla in Ucla Medical Center, Los Angeles, CA, United States
						){
					System.err.println("benefit of doubt:"+orgU2);
					return 1.0;//benefit of doubt for spelling error
				}
			}
		}*/
		return score;
	}
	public HashSet<String> orgDistinguishers = new HashSet<String>(Arrays.asList("WOMEN"));
	//Department of Microbiology, Tokyo Medical University, 6-1-1 Shinjuku, Shinjuku-ku, Tokyo 160-8402, Japan. hiroki01@tokyo-med.ac.jp	country=Japan	region=null	city=Tokyo	address=	organizations=[Department of Microbiology, Tokyo Medical University, 6 1 1 Shinjuku, Shinjuku ku]	mainInstitution=Tokyo Medical University	masterMainOrganization=Tokyo Women's Medical University
}
