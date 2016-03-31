package parsing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import opennlp.DetailedNLPer;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;

import production.AllAffiliations;
import neobio.alignment.BasicScoringScheme;
import neobio.alignment.IncompatibleScoringSchemeException;
import neobio.alignment.NeoBio;
import neobio.alignment.PairwiseAlignmentAlgorithm;
import neobio.alignment.SmithWaterman;


// TODO: Auto-generated Javadoc
/**
 * The Class Affiliation.
 */
public class Affiliation implements Serializable {

	/**
	 * 
	 */
	public  String remov(String input){
                String result;
                result = input.replaceAll("[0-9]","" );
                result=result.replaceAll("\\(.*?\\) ?", "");
                result=result.replaceAll("\\W","");
                return result;
        }
	
	private static final long serialVersionUID = 2977732759460833567L;

	/** The dict. */
	public Dictionaries dict;

	/** The aff id. */
	public int affID;

	/** The original mention. */
	public String mention;

	/** The mention. */
	public String origMention;

	/** The main instituion. */
	public String mainInstitution;

	public String masterMainOrganization;
	public int masterMainOrganizationId;

	public String masterBranchOrganization;
	public int masterBranchOrganizationId=-1;
	public String masterSchoolOrganization;
	public int masterSchoolOrganizationId=-1;
	public String masterCollegeOrganization;
	public int masterCollegeOrganizationId=-1;
	public String masterDepartmentOrganization;
	public int masterDepartmentOrganizationId=-1;
	public String masterServicesOrganization;
	public int masterServicesOrganizationId=-1;
	public String masterCentreOrganization;
	public int masterCentreOrganizationId=-1;

	public ArrayList<String> organizations;

	/** The address. */
	public String address;

	/** The city. */
	public String city;

	/** The region. */
	public String region;

	/** The country. */
	public String country;

	public String email;

	/** The aff phrases. */
	ArrayList<String> affPhrases ;

	/** The parsed aff phrases. */
	ArrayList<String> parsedAffPhrases ;

	/**
	 * Prints the parsed affiliation output.
	 */
	public void print() {
		System.out.println(origMention+"\tcountry="+country+"\tregion="+region+"\tcity="+city+"\taddress="+address+"\torganizations="+organizations+"\tmainInstitution="+mainInstitution+"\tmasterMainOrganization="+masterMainOrganization+"\tmasterMainOrganizationId="+masterMainOrganizationId+"\tmasterBranchOrganization="+masterBranchOrganization+"\tmasterSchoolOrganization="+masterSchoolOrganization+"\tmasterCollegeOrganization="+masterCollegeOrganization+"\tmasterDepartmentOrganization="+masterDepartmentOrganization+"\tmasterServicesOrganization="+masterServicesOrganization+"\tmasterCentreOrganization="+masterCentreOrganization);
	}

	public void print(PrintWriter pwr) {
		pwr.print("\""+origMention+"\"");
		pwr.print("|\""+country+"\"");
		pwr.print("|\""+region+"\"");
		pwr.print("|\""+city+"\"");
		pwr.print("|\""+address+"\"");
		pwr.print("|\""+StringUtils.join(organizations, "__")+"\"");
		pwr.print("|\""+mainInstitution+"\"");
		pwr.print("|\""+masterMainOrganization+"\"");
		pwr.print("|\""+masterMainOrganizationId+"\"");
		pwr.print("|\""+masterBranchOrganization+"\"");
		pwr.print("|\""+masterSchoolOrganization+"\"");
		pwr.print("|\""+masterCollegeOrganization+"\"");
		pwr.print("|\""+masterDepartmentOrganization+"\"");
		pwr.print("|\""+masterServicesOrganization+"\"");
		pwr.print("|\""+masterCentreOrganization+"\"");

		pwr.println();

	}
	public boolean equals(Affiliation aff, DetailedNLPer nlp) {


		//currently just checking country and orgs

		//11301767|7|2014|Medmeme|D.||Rubin||Stanford, CA||United States||Nair_V.S.:Guo_H.:Davidzon_G.:Zirlinger_A.:Chooljian_D.M.:Mittra_E.
		//5724316|5|2009|Medmeme|David||Rubin||||||Placantonakis_Dimitris G.:Zhang_Yi-Chen:Goldman_Marc Alan:Kara_Colevas:Tabar_Viviane
		if(this.mainInstitution.equals("")||aff.mainInstitution.equals(""))
			return false;

		//no need of approximate matching in these cases
		if(this.masterMainOrganizationId!=-1&&aff.masterMainOrganizationId!=-1&&this.masterMainOrganizationId!=aff.masterMainOrganizationId)
			return false;

		if( 
				//complete match
				this.mention.equalsIgnoreCase(aff.mention) || 

				//masterOrgID amtch
				(this.masterMainOrganizationId!=-1&&this.masterMainOrganizationId==aff.masterMainOrganizationId) ||

				//email match
				(this.email.length()>0 && this.email.equalsIgnoreCase(aff.email)) || 

				/*//same country if exists and same organization
				((this.country==null||aff.country==null||this.country.equalsIgnoreCase(aff.country))
						&& (this.hasOrgs(aff.organizations) || nlp.removeStopWordsAndStem(this.mainInstitution.toLowerCase()).equals(nlp.removeStopWordsAndStem(aff.mainInstitution.toLowerCase())))
						)
						//same country and same city
						//		|| (this.country!=null&&aff.country!=null&&this.country.equals(aff.country) &&this.city!=null &&aff.city!=null &&this.city.equals(aff.city))

						//same country and same city and similar organization
						//|| (this.country!=null&&aff.country!=null&&this.country.equals(aff.country) 
						||((this.country==null||aff.country==null||this.country.equalsIgnoreCase(aff.country))
								//&&this.city!=null &&aff.city!=null &&this.city.equals(aff.city)
								&&(this.city==null ||aff.city==null || this.city.equalsIgnoreCase(aff.city)
								//|| (this.region==null || aff.region==null || this.region.equalsIgnoreCase(aff.region))) //sid added on Feb 3 2015
								|| (this.region!=null && aff.region!=null && this.region.equalsIgnoreCase(aff.region))) //sid added on Feb 4 2015
								&& simOrg(aff.organizations))*/

				//same country and same city/region and (similar main organization or share  a main organization)
				(this.country!=null&&aff.country!=null&&this.country.equals(aff.country) 
				&&((this.city!=null &&aff.city!=null &&this.city.equals(aff.city))||(this.region!=null&&aff.region!=null&&this.region.equals(aff.region) ))
				&& this.mainInstitution!=null&&aff.mainInstitution!=null&& 
				(simMainInstitution(aff.mainInstitution) 
						|| this.organizations.contains(aff.mainInstitution)||aff.organizations.contains(this.mainInstitution)
						))||

				//12662398|3|2014|Medmeme|David||Rubin|Duke University|Durham, North Carolina||United States||Ogle_Christin:Siegler_Ilene|RD11|18268|0.9
				//6828510|2|2011|Medmeme|David C.||Rubin|Duke University|||||Berntsen_Dorthe:Siegler_Ilene C.|RD12|-1|0.8
				//this.mainInstitution.equals(aff.mainInstitution)&&NeoBio.getSmithWatermanMatch(this.mention, aff.mention)>Affiliation.SW_THRESHOLD ||

				//same country and same city and main institution exists in the other
				//in simOrg exclude department and division
				((this.country==null||aff.country==null||this.country.equalsIgnoreCase(aff.country))
						&&(this.city==null ||aff.city==null || this.city.equalsIgnoreCase(aff.city)
								|| (this.region!=null && aff.region!=null && this.region.equalsIgnoreCase(aff.region))) //sid added on Feb 4 2015
						&&(this.mainInstitution.equals(aff.mainInstitution) 
								|| NeoBio.getJaccardMinSimilarity(new HashSet<>(Arrays.asList(nlp.tokenizer.tokenize(this.mention))), new HashSet<>(Arrays.asList(nlp.tokenizer.tokenize(aff.mention))), nlp.stopwordsU) > SW_THRESHOLD 
								//|| NeoBio.getSmithWatermanMatch(this.mention, aff.mention)>SW_THRESHOLD
								)
							)
						//&& this.dict.matchingNonOrgWords(this.mainInstitution.toUpperCase(), aff.mainInstitution.toUpperCase(), nlp.stopwordsU)>SW_THRESHOLD)
				)
			return true;

		/*if(this.origMention.contains("Duke University")&&aff.origMention.contains("Duke University"))
			System.err.println("check");*/


		return false;
	}

	public boolean strictEquals(Affiliation aff) {

		return 
				//complete match
				this.mention.equalsIgnoreCase(aff.mention) || 

				//email match
				(this.email.length()>0 && this.email.equalsIgnoreCase(aff.email)) || 

				//same country and same main organization
				(this.country!=null&&aff.country!=null&&this.country.equals(aff.country) 
				&& this.mainInstitution!=null&&aff.mainInstitution!=null&& this.mainInstitution.equals(aff.mainInstitution))

				|| 

				//same city and same main organization
				(this.city!=null&&aff.city!=null&&this.city.equals(aff.city) 
				&& this.mainInstitution!=null&&aff.mainInstitution!=null&& this.mainInstitution.equals(aff.mainInstitution))

				|| 

				//same state and same main organization
				(this.region!=null&&aff.region!=null&&this.region.equals(aff.region) 
				&& this.mainInstitution!=null&&aff.mainInstitution!=null&& this.mainInstitution.equals(aff.mainInstitution))

				//same country and same city and (similar main organization or share  a main organization)
				|| (this.country!=null&&aff.country!=null&&this.country.equals(aff.country) 
				&&this.city!=null &&aff.city!=null &&this.city.equals(aff.city)
				&& this.mainInstitution!=null&&aff.mainInstitution!=null&& 
				(simMainInstitution(aff.mainInstitution) 
						|| this.organizations.contains(aff.mainInstitution)||aff.organizations.contains(this.mainInstitution)
						))

						//same country and same city and same region and share a similar organization - sid added on 10/15
						|| (this.country!=null&&aff.country!=null&&this.country.equals(aff.country) 
						&&this.city!=null &&aff.city!=null &&this.city.equals(aff.city)
						&&this.region!=null&&aff.region!=null&&this.region.equals(aff.region) 
						&& simOrg(aff.organizations)) 


						;
	}


	public boolean exactEquals(Affiliation aff, DetailedNLPer nlp) {
		return 
				//complete match
				this.mention.equalsIgnoreCase(aff.mention) ||
				(this.masterMainOrganizationId==aff.masterMainOrganizationId && this.masterMainOrganizationId>0)

				//same country and same city and (similar main organization or share  a main organization)
				//same country and same city and (exact normalized main organization or share  a main organization)
				|| (this.country!=null&&aff.country!=null&&this.country.equals(aff.country) 
				&&this.city!=null &&aff.city!=null &&this.city.equals(aff.city)
				&& this.mainInstitution!=null&&aff.mainInstitution!=null&&this.mainInstitution.length()>0&&aff.mainInstitution.length()>0&&  
				( nlp.removeStopWordsAndStem(this.mainInstitution.toLowerCase()).equals(nlp.removeStopWordsAndStem(aff.mainInstitution.toLowerCase()))
						|| this.organizations.contains(aff.mainInstitution)||aff.organizations.contains(this.mainInstitution)
						))
						;

	}

	public boolean exactEqualsOnlyMain(Affiliation aff, DetailedNLPer nlp) {
		return 
				//complete match
				this.mention.equalsIgnoreCase(aff.mention) ||
				(this.masterMainOrganizationId==aff.masterMainOrganizationId && this.masterMainOrganizationId>0)

				//same country and same city and (similar main organization or share  a main organization)
				//same country and same city and (exact normalized main organization or share  a main organization)
				|| (this.country!=null&&aff.country!=null&&this.country.equals(aff.country) 
				&&this.city!=null &&aff.city!=null &&this.city.equals(aff.city)
				&& this.mainInstitution!=null&&aff.mainInstitution!=null&&this.mainInstitution.length()>0&&aff.mainInstitution.length()>0&& 
				( nlp.removeStopWordsAndStem(this.mainInstitution.toLowerCase()).equals(nlp.removeStopWordsAndStem(aff.mainInstitution.toLowerCase()))
						//	|| this.organizations.contains(aff.mainInstituion)||aff.organizations.contains(this.mainInstituion)
						))
						;
	}





	public static final double SW_THRESHOLD = //0.70;
			0.75;//
	/*UNIVERSITY OF TEXAS HOUSTON
	|||||||||||||      ||||||||
	UNIVERSITY OF------ HOUSTON
	Score: 15
	0.7142857142857143*/
	private boolean simMainInstitution(String inst) {
		PairwiseAlignmentAlgorithm	algorithm = new SmithWaterman();
		algorithm.setScoringScheme(new BasicScoringScheme (1, -1, -1));
		algorithm.loadSequenceStrings(this.mainInstitution, inst);
		//double score2 = NeoBio.getScore(org, org2);
		try {
			double score=algorithm.getScore()*1.0/Math.min(this.mainInstitution.length(), inst.length());
			if(score>SW_THRESHOLD) 
				return true;
		} catch (IncompatibleScoringSchemeException e) {
			e.printStackTrace();
		}
		return false;
	}

	private boolean simOrg(ArrayList<String> orgs) {
		//Fletcher, Tony [LONDON SCHOOL OF HYGIENE AND TROPICAL MEDICINE, LONDON, UNITED KINGDOM] :1
		//Fletcher, Tony [LSHTM, , UNITED KINGDOM] :1
		for (String org : orgs) {
			String orgInit = org.replaceAll("[^A-Z]", "");
			for (String org2 : this.organizations) {
				if(org2.equals(orgInit)) 
					return true;
				else if(org.equals(org2.replaceAll("[^A-Z]", ""))) 
					return true;
			}
		}

		for (String org : orgs) {
			org=org.toUpperCase();
			if(org.contains("DEPARTMENT OF")||org.contains("DIVISION OF"))
				continue;
			for (String org2 : this.organizations) {
				org2=org2.toUpperCase();
				if(org2.contains("DEPARTMENT OF")||org2.contains("DIVISION OF"))
					continue;
				PairwiseAlignmentAlgorithm	algorithm = new SmithWaterman();
				algorithm.setScoringScheme(new BasicScoringScheme (1, -1, -1));
				algorithm.loadSequenceStrings(org, org2);
				//double score2 = NeoBio.getScore(org, org2);
				try {
					double score=algorithm.getScore()*1.0/Math.min(org.length(), org2.length());
					//12817847|2|2014|Medmeme|L.||Iacampo Leiva|Servicio de Neurología. Complejo Hospital Universitario Nuestra Sra de Candelaria
					//12818779|6|2014|Medmeme|L.||Iacampo Leiva|Servicio de Neurología. Hospital Nuestra Señora de Candelaria
					//both affs are similar, but the score is only 0.5
					if(score>SW_THRESHOLD) 
						return true;
					else if(NeoBio.getJaccardMinSimilarity(Arrays.asList(org.split("\\s+")), Arrays.asList(org2.split("\\s+")))>SW_THRESHOLD)
						return true;
				} catch (IncompatibleScoringSchemeException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}
		

	private boolean hasOrgs(ArrayList<String> orgs) {
		for (String org : orgs) {
			for (String org2 : this.organizations) {
				if(org2.equals(org)) 
					return true;
			}
		}
		return false;
	}
	public Affiliation(String aff, Dictionaries dictionaries) {
	}

	/**
	 * Instantiates a new affiliation.
	 *
	 * @param aff the aff
	 * @param dictionaries the dictionaries
	 */
	public Affiliation(String aff, Dictionaries dictionaries, String inputCity, String inputRegion, String inputCountry, AllAffiliations allAffs, DetailedNLPer nlp) {
		dict=dictionaries;

		this.origMention=aff;
		//Input error
		aff=aff.replaceAll("Not Listed,", ",");
		//will move this to parseOrgs()
		//aff=dict.replaceOrgAbbr(aff);
		this.mention=dict.cleanAff(parseMultipleAffs(aff).get(0));
		affPhrases= splitPhrasesNotLists(mention);
		parsedAffPhrases = new ArrayList<String>();

		this.email=this.mention.replaceAll(".*?([A-Z\\._]+@[A-Z\\.]+).*?", "$1");
		if(!this.email.contains("@"))
			this.email="";
		this.parseCountry();
		this.parseRegion();
		this.parseCity();
		//allowing to be copied from affiliations -- Mmc, Clayton, VIC, Australia
		String[] splits=this.origMention.split("\\s*,\\s*");
		if(splits.length>=3){
			String givenCity=splits[splits.length-3].toUpperCase();
			if(this.dict.allCities.contains(givenCity)){
				if(city==null) 
					city=givenCity;
				if(affPhrases.contains(givenCity)&&!parsedAffPhrases.contains(givenCity))
					parsedAffPhrases.add(givenCity);
			}
			else if(this.dict.allCities.contains(givenCity.replaceAll(" CITY$", ""))){
				if(city==null) 
					city=givenCity.replaceAll(" CITY$", "");
				if(affPhrases.contains(givenCity)&&!parsedAffPhrases.contains(givenCity))
					parsedAffPhrases.add(givenCity);
			}
		}


		//allowing region to be duplicated -- School of Nursing, University of Alabama, AL, USA.
		if(city==null && parsedAffPhrases.contains(region) && dict.allCities.contains(region) && region.length()>=3)
			city=region;

		//address
		this.parseAddress();
		//String tempCity=city;
		this.partialCountryRegionCityParsing();
		//city=tempCity;
		if ((city==null) || (city.compareTo("null")==0)){
                        if ((inputCity!=null)&&(inputCity.length()>0))
                        	city=inputCity;
                }
		//Institute of Cardiology named after Academician M D Tsinamdzgvrishvili, Scientific Research Laboratory Test House of Physicians, Centre of Angiology and Vascular Surgery named after N K Bokhua, Tbilisi, Georgia.	country=#GEORGIA#COMMONCITY	region=GEORGIA	city=TBILISI	address=null	organizations=[INSTITUTE OF CARDIOLOGY NAMED AFTER ACADEMICIAN M D TSINAMDZGVRISHVILI, SCIENTIFIC RESEARCH LABORATORY TEST HOUSE OF PHYSICIANS, CENTRE OF ANGIOLOGY AND VASCULAR SURGERY NAMED AFTER N K BOKHUA]	mainInstitution=INSTITUTE OF CARDIOLOGY NAMED AFTER ACADEMICIAN M D TSINAMDZGVRISHVILI	masterMainOrganization=null	masterMainOrganizationId=-1	masterBranchOrganization=	masterSchoolOrganization=	masterCollegeOrganization=	masterDepartmentOrganization=	masterServicesOrganization=	masterCentreOrganization=CENTRE OF ANGIOLOGY AND VASCULAR SURGERY NAMED AFTER N K BOKHUA
		if(this.country!=null&&this.country.startsWith("#"+this.region+"#")){
			this.country=this.region;
			this.region=null;
		}

		//Tbilisi State Medical University, Department of Pharmacognosy and Botany; L. Samkharauli National Forensics Bureau, Tbilisi, Georgia.	country=#GEORGIA#COMMONCITY	region=null	city=null	address=null	organizations=[TBILISI STATE MEDICAL UNIVERSITY, DEPARTMENT OF PHARMACOGNOSY AND BOTANY]	mainInstitution=TBILISI STATE MEDICAL UNIVERSITY	masterMainOrganization=Tbilisi State Medical University	masterMainOrganizationId=244566	masterBranchOrganization=	masterSchoolOrganization=	masterCollegeOrganization=	masterDepartmentOrganization=DEPARTMENT OF PHARMACOGNOSY AND BOTANY	masterServicesOrganization=	masterCentreOrganization=
		if(this.country!=null&&this.country.startsWith("#")){
			this.country=this.country.substring(1, this.country.lastIndexOf("#"));
		}

		this.region=normalizeUSregions(this.region, this.country);

		//organization using parsedAffs and keywords
		this.parseOrgs(allAffs, nlp);

		this.parseMainInstitution();
		if(dict.orgAbbreviations.containsKey(mainInstitution)&&this.origMention.contains(mainInstitution)//making sure it is present in Uppercase in the original mention as well
				)
			mainInstitution=dict.orgAbbreviations.get(mainInstitution);
		//clean numbers in front and remove abbreviations 1Universidade Federal do Pampa (UNIPAMPA), Campus Uruguaiana, RS, Brasil.
		///allowing 1st or 1ST
		while(this.mainInstitution.matches("[0-9\\*-]\\S{4,}.+"))
			this.mainInstitution=this.mainInstitution.substring(1).trim();
		this.mainInstitution=this.mainInstitution.replaceAll("\\([^\\)]+\\)", "").replaceAll("[\\(^\\)]", "").trim();
		this.mainInstitution = this.mainInstitution.replaceAll("UNIVERSITETSSYKEHUS", "UNIVERSITY HOSPITAL");//TODO: generic translations


		//358	Case Western Reserve University, Not Listed, OH, United States	United States	Ohio	United
		if((city!=null?city:"").equalsIgnoreCase("UNITED"))
			this.city=null;
	}


	public Affiliation(String mainOrg, String mainCity, String mainRegion,
			String mainCountry, String mainOrgId) {
		this.masterMainOrganization=mainOrg;
		this.city=mainCity;
		this.region=mainRegion;
		this.country=mainCountry;
		this.masterMainOrganizationId=Integer.parseInt(mainOrgId);
	}

	private String normalizeUSregions(String region, String country) {
		if(country!=null&&country.equals("UNITED STATES")&&this.dict.USAabbrev2FullState.containsKey(region)) 
			return this.dict.USAabbrev2FullState.get(region);
		return region;
	}

	Pattern p1=Pattern.compile("(.*HOSPITAL.*) OF (.*UNIVERSITY.*)");
	private void parseMainInstitution() {
		organizations.remove("AFFILIATED HOSPITAL");

		//1st Affiliated Hospital of Sun Yat sen University, Guangdong, Not Listed, China
		for (int i=0; i<organizations.size(); i++) {
			Matcher m1 = p1.matcher(organizations.get(i));
			if(m1.find()){
				organizations.set(i, m1.group(1));
				organizations.add(m1.group(2));
			}	
		}

		for (String org : organizations) {
			if(org.startsWith("UNIVERSITY OF")){
				mainInstitution=org;
				return;
			}	
		}
		for (String org : organizations) {
			if(//org.contains("DEPARTMENT") || org.contains("DIVISION") || //-USDA
					org.endsWith("BUILDING") || org.endsWith("CAMPUS")) continue;
			for (String tok : org.split("\\s+|-|'")) {
				if(dict.universityKeywords.contains(tok) || tok.contains("UNIVERSIT")){
					mainInstitution=org;
					return;
				}
			}
		}

		for (String org : organizations) {
			if(//org.contains("DEPARTMENT") || org.contains("DIVISION") || //-USDA
					org.endsWith("BUILDING") || org.endsWith("CAMPUS")) continue;
			if(org.contains("HEALTH SYSTEM")||org.contains("COLLEGE OF MEDICINE")){
				mainInstitution=org;
				return;
			}
			for (String tok : org.split("\\s+|-|'")) {
				if(dict.level2OrgKeywords.contains(tok) || tok.contains("HOSPIT")){
					mainInstitution=org;
					return;
				}
			}
		}

		//looking for back for this. starting from an org that doesn't have a number in it.
		int stopAT=-1;
		for (String org : organizations) {
			if(!org.matches(".*\\d+.*")) stopAT++;
			else break;
		}
		for(int i=stopAT;i>=0;i--){
			String org=organizations.get(i);
			if(org.contains("DEPARTMENT") || org.contains("DIVISION") || org.endsWith("BUILDING") || org.endsWith("CAMPUS")) continue;
			for (String tok : org.split("\\s+|-|'")) {
				if(dict.mainOrgKeywords.contains(tok)){
					mainInstitution=org;
					return;
				}
			}
		}

		/*@SuppressWarnings("unused")
		int doNothing=0;*/
		///this is getting a second place because entries like Faculty of medicine, RADIOLOGY are listed as main institute
		for (String org : organizations) {
			if(dict.orgNames.contains(org)){
				mainInstitution=org;
				return;
			}
		}

		//looking for back for this. starting from an org that doesn't have a number in it.
		stopAT=-1;
		for (String org : organizations) {
			if(!org.matches(".*\\d+.*")) stopAT++;
			else break;
		}
		for(int i=stopAT;i>=0;i--){
			String org=organizations.get(i);
			if(org.contains("DEPARTMENT") || org.contains("DIVISION") || org.endsWith("BUILDING") || org.endsWith("CAMPUS")) continue;
			for (String tok : org.split("\\s+|-|'")) {
				if(dict.orgKeywords.contains(tok)){
					mainInstitution=org;
					return;
				}
			}
		}

		//the same, but looking from beginning
		for (String org : organizations) {
			if(org.contains("DEPARTMENT") || org.contains("DIVISION") || org.endsWith("BUILDING") || org.endsWith("CAMPUS")) continue;
			for (String tok : org.split("\\s+|-|'")) {
				if(dict.orgKeywords.contains(tok)){
					mainInstitution=org;
					return;
				}
			}
		}
		//ignore department names and all english names and all abbreviations (present as capslock)
		for (String org : organizations) {
			if(!org.contains("DEPARTMENT OF") && !org.contains("DIVISION OF") && !dict.deptNames.contains(org)&&!dict.allEnglishWords(org)&&!org.matches(".*[0-9]+.*") &&!this.origMention.contains(org)){
				mainInstitution=org;
				return;
			}
		}

		//include abbreviations
		//some abbreviations are incorrect
		for (String org : organizations) {
			if(dict.orgAbbreviations.containsKey(org)&&this.origMention.contains(org)//making sure it is present in Uppercase in the original mention as well
					){
				mainInstitution=dict.orgAbbreviations.get(org);
				return;
			}
		}



		if(organizations.size()>0)//need to check if the last organization is the main organization
			mainInstitution=organizations.get(0);

		this.mainInstitution= this.mainInstitution==null?"":this.mainInstitution;
	}

	/**
	 * Parses the orgs. Only this method searches from the beginning
	 */
	private void parseOrgs(AllAffiliations allAffs, DetailedNLPer nlp) {
		HashMap<String, ArrayList<String>> mainOrgRowTable = allAffs.mainOrgRowTable;

		//Dr. Bernard Verbeeten Instituut, Tilburg, Not Listed, Netherlands	country=Netherlands	region=null	city=Tilburg	address=DR BERNARD VERBEETEN INSTITUUT 	organizations=[]	mainInstitution=	masterMainOrganization=null	masterMainOrganizationId=-1	masterBranchOrganization=	masterSchoolOrganization=	masterCollegeOrganization=	masterDepartmentOrganization=	masterServicesOrganization=	masterCentreOrganization=
		if(affPhrases.size()==parsedAffPhrases.size()&&parsedAffPhrases.size()>0){
			parsedAffPhrases.remove(parsedAffPhrases.size()-1);
		}

		//Cfsan, College Park, MD, United States
		for (String str : affPhrases) {
			if((country!=null?country.equals(str):false) || (region!=null?region.equals(str):false) || (city!=null?city.equals(str):false)){
				if(!parsedAffPhrases.contains(str))
					parsedAffPhrases.add(str);
			}
		}

		//expanding with abbreviations here
		for(int i=0;i<affPhrases.size();i++){
			String cur=affPhrases.get(i);
			if(!parsedAffPhrases.contains(cur)){
				String expanded_org = dict.replaceOrgAbbr(cur,this.origMention);
                                if (expandedMainOrgDictionaryEntry(expanded_org, mainOrgRowTable, allAffs, nlp)){
				affPhrases.set(i, dict.replaceOrgAbbr(cur,this.origMention));
				}
			}

		}

		//everything is an organization until a parsed phrase is encountered
		for(int i=0;i<affPhrases.size();i++){
			String cur=affPhrases.get(i);

			if(parsedAffPhrases.contains(cur)){
				break;
			}
			organizations.add(cur);
			parsedAffPhrases.add(cur);
		}

		//every unparsed phrase is an organization if it contains an orgkeword
		for(int i=0;i<affPhrases.size();i++){
			String cur=affPhrases.get(i);

			if(!parsedAffPhrases.contains(cur) && containsOrgKeyword(cur)){
				parsedAffPhrases.add(cur); 
				organizations.add(cur);
			}
		}

		//Centro Medico Nacional 20 De Noviembre, Mexico City, Not Listed, Mexico
		for(int i=0;i<affPhrases.size();i++){
			String cur=affPhrases.get(i);
			if(organizations.contains(cur) && cur.endsWith(" CITY")){
				organizations.remove(cur);
			}
		}
	}


	private boolean containsOrgKeyword(String cur) {
		for (String tok : cur.split("\\s+")) {
			if(dict.orgKeywords.contains(tok))
				return true;
		}
		return false;
	}

	/**
	 * Parses the address.
	 */
	private void parseAddress() {
		if(address!=null) return;
		else address="";
		for(int i=affPhrases.size()-1;i>=0;i--){
			String cur=affPhrases.get(i);
			String[] curs=cur.split("\\s+");

			//C.T.O. Hospital, Torino, Not Listed, Italy
			if(!parsedAffPhrases.contains(cur) && dict.orgKeywords.contains(curs[curs.length-1])){
				break;
			}

			if(!parsedAffPhrases.contains(cur) && dict.directionWords.contains(curs[curs.length-1])){
				parsedAffPhrases.add(cur);
				address=cur+" "+address;
				//break;
			}
			if(!parsedAffPhrases.contains(cur) && dict.addressWords.contains(curs[curs.length-1].replaceAll("\\.", ""))){
				if(curs[0].matches(".*\\d+.*"))
					parsedAffPhrases.add(cur); //NOTE: filtered as it can lead to some addresses not properly separated with organizations
				if(!containsOrgKeyword(cur))
					parsedAffPhrases.add(cur);
				address=cur+" "+address;
				//break;
			}
			//Service de néphrologie, groupe hospitalier Pitié-Salpêtrière, 83, boulevard de l'Hôpital, 75013 Paris, France. Electronic address: lucile.mercadal@psl.aphp.fr.
			if(!parsedAffPhrases.contains(cur) && dict.addressWords.contains(curs[0].replaceAll("\\.", ""))){
				if(curs.length>1 && curs[1].length()<=3)//the idea is the second word should be a preposition
					parsedAffPhrases.add(cur); //NOTE: filtered as it can lead to some addresses not properly separated with organizations
				if(!containsOrgKeyword(cur))
					parsedAffPhrases.add(cur);
				address=cur+" "+address;
				//break;
			}

			//9	Health and Environmental Impacts Division, Research Triangle Park, NC, United States	United States	North Carolina	United		Health and Environmental Impacts Division__Research Triangle Park	Research Triangle Park	Research Triangle Institute	230058				HEALTH AND ENVIRONMENTAL IMPACTS DIVISION												
			if(!parsedAffPhrases.contains(cur)){
				if(cur.equals("RESEARCH TRIANGLE PARK"))
					parsedAffPhrases.add(cur);
			}
		}
	}


	/**
	 * Partial country region city parsing.
	 */
	private void partialCountryRegionCityParsing() {
		for(int i=affPhrases.size()-1;i>=0;i--){
			if(parsedAffPhrases.contains(affPhrases.get(i)) || country!=null) continue;
			String cur=" "+affPhrases.get(i)+" ";
			for(String countryDict: dict.countryTable.keySet()){
				//need to remove abbreviations such as CA for Canada
				if(countryDict.length()<3) continue;
				if(cur.contains(" "+countryDict+" ")){
					country=dict.countryTable.get(countryDict);
					break;
				}
			}

		}

		for(int i=affPhrases.size()-1;i>=0;i--){
			if(parsedAffPhrases.contains(affPhrases.get(i)) || region!=null) continue;
			String cur=" "+affPhrases.get(i)+" ";
			for(String regionDict: dict.uniqueRegionTable.keySet()){
				//need to remove US region abbreviations such as IN
				if(regionDict.length()<3) continue;
				if(cur.contains(" "+regionDict+" ") && !dict.keywords.contains(regionDict) && (country==null||country.equals(dict.uniqueRegionTable.get(regionDict)))){
					country=dict.uniqueRegionTable.get(regionDict);
					region=regionDict;
					break;
				}
			}

		}


		for(int i=affPhrases.size()-1;i>=0;i--){
			//if(parsedAffPhrases.contains(affPhrases.get(i)) || city!=null) continue;//University of California-San Francisco, 94117, USA. daaronson@urology.ucsf.edu
			if(parsedAffPhrases.contains(affPhrases.get(i)) && city!=null) continue; 
			String cur=" "+affPhrases.get(i)+" ";
			if(containsOrgKeyword(cur)){
				if(cur.contains(" AT "))
					cur=cur.substring(cur.indexOf(" AT "));//Glomerular Disease Therapeutics Laboratory, Division of Nephrology, University of Alabama at Birmingham Birmingham, AL, USA.
				//else
				//not sure if this was necessary
				//	continue;//School of Nursing, University of Alabama, AL, USA.//
			}

			if ((city!=null) && (city.toUpperCase().compareTo("TORONTO") == 0)){
                                country="Canada";
                                break;
                        }

			for(String cityDict: dict.uniqueCityTable.keySet()){
				//need to remove abbreviations such as CA for Canada
				if(cityDict.length()<3) continue;
				if(cur.contains(" "+cityDict+" ") && !dict.keywords.contains(cityDict) 
						&& (country==null||country.equals(dict.uniqueCityTable.get(cityDict)))){
					country=dict.uniqueCityTable.get(cityDict);
					if (cityDict.compareTo("Toronto")==0)
						country="Canada";
					if(this.city==null)
						this.city=cityDict;
					break;
				}
			}

			if(this.city==null){
				//comma missing before city name
				for(String cityDict: dict.allCitiesSmall){
					//need to remove abbreviations such as CA for Canada
					//Glomerular Disease Therapeutics Laboratory, Division of Nephrology, University of Alabama at Birmingham Birmingham, AL, USA.
					if(cityDict.length()<3) continue;
					if(cur.endsWith(" "+cityDict+" ") && !dict.keywords.contains(cityDict) ){
						if(city!=null){
                                                        if (cur.compareTo(country)!=0) continue;
                                                                city=cityDict;
                                                                break;
                                                }
					}
				}
			}
		}

		//allowing region to be duplicated
		//School of Nursing, University of Alabama, AL, USA.
		//but this won't work well for few names
		//Neuroscience Graduate Program, University of Michigan, USA.
		//country=United States	region=Michigan	city=Michigan
		/*if(city==null && dict.allCities.contains(region) && region.length()>=3)
			city=region;*/

		//checking full orgs is better than partial city/regions
		//but not always INSTITUTE OF PUBLIC HEALTH
		organizations=new ArrayList<String>();

		//use main orgs and also use this for assigning country name
		for(int i=0;i<affPhrases.size();i++){
			String cur=affPhrases.get(i);
			if(!parsedAffPhrases.contains(cur) && dict.org2CountryTable.containsKey(cur)){
		//	if(!parsedAffPhrases.contains(cur) && dict.org2CountryTable.containsKey(cur) && dict.uniqueCityTable.containsKey(cur))
				organizations.add(cur);
				parsedAffPhrases.add(cur);
				if(country==null){
					country=dict.org2CountryTable.get(cur);
					break;
				}
		/*		if(city==null){
					city= dict.uniqueCityTable.get(cur);
					break;
				}*/
			}
		}
	}

	/**
	 * Parses the city.
	 */
	private void parseCity() {
		if(city!=null) return;
		for(int i=affPhrases.size()-1;i>=0;i--){
			String cur=affPhrases.get(i);
			//Bat-Yam
			List<String> splits=Arrays.asList(cur.replaceAll("-", " ").split("\\s+"));

			int end=splits.size();
			while(end>0&&splits.get(end-1).matches(".*[0-9]+.*"))
				end--;
			int beg=0;
			while(beg<splits.size()&&splits.get(beg).matches(".*[0-9]+.*"))
				beg++;
			String curNorm = beg<=end?StringUtils.join(splits.subList(beg, end), " "):"";
			curNorm=curNorm.replaceAll("CAMPUS ", "");//1Universidade Federal do Pampa (UNIPAMPA), Uruguaiana, RS, Brasil

			if(this.dict.countryTable.containsKey(curNorm))//1National Institute for Occupational Safety and Health (preferred mailing address)Division of Respiratory Disease Studies, Surveillance Branch 1095 Willowdale Road Morgantown , West Virginia, 26505 USA and West Virginia University School of Public Health PO Box 9190 Morgantown, WV, 26506 USA , Email: bdoney@cdc.gov , Fax: (304) 285-6111.
				curNorm="";

			//need to remove abbreviations such as CA//From the Departments of Cardiology (A.S., O.H., Y.A., M.K., A.F., S.B., A.H.), Internal Medicine (B.S., S.G.), and Neurology (N.M.B.), Tel Aviv Medical Center affiliated to the Sackler Faculty of Medicine, Tel Aviv University, Tel Aviv, Israel.
			if(cur.length()<3) continue;

			if(!parsedAffPhrases.contains(cur) && 
					(dict.allCities.contains(cur))){
				parsedAffPhrases.add(cur);
				city=cur;
				break;
			}
			else if(!parsedAffPhrases.contains(cur) && 
					(dict.allCities.contains(curNorm))){
				parsedAffPhrases.add(cur);
				city=curNorm;
				break;
			}
		}


	}

	/**
	 * Parses the region.
	 */
	private void parseRegion() {
		if(region!=null) return;
		for(int i=affPhrases.size()-1;i>=0;i--){
			String cur=affPhrases.get(i);
			List<String> splits=Arrays.asList(cur.split("\\s+"));
			int size=splits.size();
			while(size>0&&splits.get(size-1).matches(".*\\d+.*"))
				size--;
			String curNorm = StringUtils.join(splits.subList(0, size), " ");
			if(!parsedAffPhrases.contains(cur) && dict.allRegions.contains(curNorm)){
				parsedAffPhrases.add(cur);
				region=curNorm;
				break;
			}
		}

		if(region==null&&country!=null){
			String ccTLD=this.dict.country2ccTLDs.get(country);
			for(int i=affPhrases.size()-1;i>=0&&region==null;i--){
				String cur = affPhrases.get(i);
				if(this.dict.regionExpansions.containsKey(ccTLD+"_"+cur) ){
					parsedAffPhrases.add(cur);
					region=this.dict.regionExpansions.get(ccTLD+"_"+cur);
					break;
				}

			}
		}
	}



	/**
	 * Parses the country.
	 */
	private void parseCountry() {


		for(int i=affPhrases.size()-1;i>=0;i--){
			String cur=affPhrases.get(i);
			List<String> splits=Arrays.asList(cur.split("\\s+"));
			int size=splits.size();
			//also ignore email addresses
			while(size>0 && splits.get(size-1).matches(".*[\\d@].*"))
				size--;
			String curNorm = StringUtils.join(splits.subList(0, size), " ");
			if(!parsedAffPhrases.contains(cur) && dict.countryTable.containsKey(curNorm)){
				parsedAffPhrases.add(cur);
				country=dict.countryTable.get(curNorm);
				//Department of Clinical Pharmacology, Aerospace Central Hospital, Beijing 100049, China. Email: gwz501@hotmail.com.
				if(city==null && !country.equals(curNorm) && //only applies when talking about the capital. don't misuse when the country and capital are same as in Mexico city...
						dict.uniqueCityTable.containsKey(curNorm) && dict.uniqueCityTable.get(curNorm).equals(country) )
					city=curNorm;
				break;
			}
		}

		for (String ccTLD : dict.ccTLDs.keySet()) {
			if(mention.matches(".*@[A-Z0-9\\.-]+\\."+ccTLD+"\\.?(\\s|$).*")
					&& country==null){
				country=dict.ccTLDs.get(ccTLD);
				break;
			}	
		}

		for(int i=affPhrases.size()-1;i>=0;i--){
			String cur=affPhrases.get(i);
			if(!parsedAffPhrases.contains(cur) && dict.uniqueRegionTable.containsKey(cur) && (country==null||country.equals(dict.uniqueRegionTable.get(cur)))){
				parsedAffPhrases.add(cur);
				country=dict.uniqueRegionTable.get(cur);
				region=cur;
				break;
			}
			if(!parsedAffPhrases.contains(cur) && dict.USAabbrev2FullState.containsValue(cur) && (country==null||country.equals("UNITED STATES"))){
				parsedAffPhrases.add(cur);
				country="UNITED STATES";
				region=cur;
				break;
			}
		}

		for(int i=affPhrases.size()-1;i>=0;i--){
			String cur=affPhrases.get(i);
			if(!parsedAffPhrases.contains(cur) && dict.uniqueCityTable.containsKey(cur) && (country==null||country.equals(dict.uniqueCityTable.get(cur)))){
				if(!containsOrgKeyword(cur))//Auburn University Harrison School of Pharmacy, Auburn University, Alabama, USA.
					parsedAffPhrases.add(cur);
				country=dict.uniqueCityTable.get(cur);
				city=cur;
				break;
			}
		}

		if(country!=null&&country.length()>0) return;
		String commaMention=", "+this.mention;
		for (String cntry : dict.countryTable.keySet()) {
			if(commaMention.endsWith(", "+cntry)){
				country=dict.countryTable.get(cntry);
				for(int i=affPhrases.size()-1;i>=0;i--){
					String cur=affPhrases.get(i);
					if(cntry.contains(cur))
						parsedAffPhrases.add(cur);
				}
				break;	
			}
		}
	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void main(String[] args) throws IOException {
		Dictionaries dict=new Dictionaries();
		//HEALTH SYSTEMS, MANAGEMENT, POLICY, COLORADO SCHOOL OF PUBLIC HEALTH, 13001 E 17TH PLACE, AURORA, CO 80045	country=PHILIPPINES	region=AURORA	city=null

		DetailedNLPer nlp = new DetailedNLPer();
		AllAffiliations allAffs = new AllAffiliations(nlp);

		Affiliation anAff = new Affiliation("Zurich University of Applied Science, Winterthur, Not Listed, Switzerland",dict);
		anAff.findMainOrgDictionaryEntry(allAffs.normMainOrgRowTable,allAffs, allAffs.nlp);
		anAff.findOtherOrgDictionaryEntry(allAffs,allAffs.nlp);
		anAff.findInferredMainOrg(allAffs.normInferredOrgRowTable,allAffs.nlp);
		anAff.print();
		//System.out.println("anAff:" +anAff);

		/**
		 * SELECT *
FROM [CIS].[dbo].[tblAbstracts] ab
where ab.lngAbstractID in (  
SELECT top 1000 [lngAbstractID]      
  FROM [CIS].[dbo].[tblAbstracts] abs
  where abs.intSource=1
  AND abs.datCreatedDate >= CONVERT(datetime, '2004-01-01')
ORDER BY NEWID())"
		 */
		Scanner sc = new Scanner(System.in);
		while(sc.hasNextLine()){
			String affString = sc.nextLine();
			Affiliation anAffiliation = new Affiliation(affString,dict);
			anAffiliation.findMainOrgDictionaryEntry(allAffs.normMainOrgRowTable,allAffs, allAffs.nlp);
			anAffiliation.findOtherOrgDictionaryEntry(allAffs,allAffs.nlp);
			anAffiliation.findInferredMainOrg(allAffs.normInferredOrgRowTable,allAffs.nlp);
			anAffiliation.print();
			System.out.print("");

		}
		sc.close();
	}

	public void findInferredMainOrg(
			HashMap<String, ArrayList<String>> normInferredOrgRowTable,
			DetailedNLPer nlp) {
		if(normInferredOrgRowTable.size()>0 && this.masterMainOrganizationId==-1){
			String mainInstNorm = nlp.removeStopWordsAndStem(this.mainInstitution.toLowerCase());
			if(normInferredOrgRowTable.containsKey(mainInstNorm)){
				ArrayList<String> lines = normInferredOrgRowTable.get(mainInstNorm);
				for (String line : lines) {
					String[] splits=line.split("\"\\|\"");
					String country=splits[1],region=splits[2],city=splits[3];
					if(this.country!=null&&!country.equals("null")&&this.country.equals(country) && ((this.city!=null&&city.equals(this.city))||(this.region!=null&&region.equals(this.region)))){
						this.masterMainOrganization=splits[7];
						this.masterMainOrganizationId=Integer.parseInt(splits[8]);
					}

				}
			}

			//17186169	2	12/1/06	PubMed	Werner		Benzer	Department of Interventional Cardiology, Academic Hospital, Feldkirch, Austria. wbenzer@cable.vol.at					ACADEMIC HOSPITAL	FELDKIRCH		AUSTRIA	Academic Hospital, Feldkirch	Feldkirch		Austria	Need to mention the city with the organization such as University Hospital, University Medical Center, Academic Hospital and Centre University Hospital	  
			if(this.masterMainOrganizationId==-1 && dict.allEnglishWords(this.mainInstitution) && this.city!=null){
				this.mainInstitution+=", "+this.city;
			}
		}
		CustomAffiliation.process(this);
	}





	/**
	 * input - Department of Gastroenterology, Hepatology and Immunology, The Children's Memorial Health Institute, Warsaw, Poland
	 * output - Department of Gastroenterology, Blood, Lung, Hepatology and Immunology	
	 * The Children's Memorial Health Institute	
	 * Warsaw	
	 * Poland	.
	 *
	 * @param str the str
	 * @return the array list
	 */

	//TODO: this is a better way of finding email addresses
	//Pattern emailPattern =
			//Pattern.compile("([a-zA-Z0-9!#$%&''*+,/=?^_`{|}~-])+(\\.[a-zA-Z0-9!#$%&''*+/=?^_`{|}~-]+)*@([A-Za-z0-9]([A-Za-z0-9-]*[A-Za-z0-9])?\\.)+[A-Za-z0-9]([A-Za-z0-9-]*[A-Za-z0-9])?");

	private ArrayList<String> splitPhrasesNotLists(String str) {
		str=str.replaceAll(" & ", " AND " ).replaceAll(", AND ", " AND " ).replaceAll("\\. ", " ");//handle \\. from abbreviations later on

		//remove email address out
		/*Matcher m =emailPattern.matcher(str);
		while (m.find()) {
			str=str.substring(0,m.start())+str.substring(m.end()).trim();
			m.reset(str);
		}*/

		

		//TODO: consider using this before 
		List<String> toks =Arrays.asList(str.split("\\s+|-"));
		for (int i=0;i<toks.size();i++) {
			toks.set(i, dict.expandAbbr(toks.get(i)));
		}
		str = StringUtils.join(toks," ");

		ArrayList<String> tmp = new ArrayList<String>(Arrays.asList(str.split(",|\\.$|;")));
		//	(",|\\. |\\.$|;")));
		for (int i=0;i<tmp.size();i++) {
			tmp.set(i, tmp.get(i).trim());
		}

		//for handling lists
		for (int i=tmp.size()-1;i>=0;i--) {
			if(!tmp.get(i).contains(" AND ")) continue;
			String beforeAnd = tmp.get(i).substring(0,tmp.get(i).indexOf(" AND "));
			if(beforeAnd.contains(" ")) continue;//this means there is more than one word and less likely to be a list - this is a guess
			String afterAnd = tmp.get(i).substring(tmp.get(i).indexOf(" AND ")+" AND ".length());
			if(afterAnd.contains(" AND ")) continue;//this means there are more than one and's and unlikely this is a list. example - Brigham and Women's Hospital and Harvard Medical School

			for (int j=i-1;j>=0;j--) {
				String prev= tmp.get(j);
				tmp.set(i, prev+", "+tmp.get(i));
				tmp.set(j, "");
				if(prev.contains(" ")) break;
			}
		}

		//alternative
		/*		Pattern listAnd=Pattern.compile("(\\s|^)([A-Z]+, )+([A-Z]+ )AND ");

		m=listAnd.matcher(str);
		while(m.find()){
			str=str.substring(0,m.start())+m.group().replaceAll(",", "").trim()+" "+str.substring(m.end()).trim();
			m.reset(str);

		}*/

		ArrayList<String> affPhrases= new ArrayList<String>();
		for (int i=0;i<tmp.size();i++) {
			if(tmp.get(i).equals("")) continue;
			if(tmp.get(i).matches("\\d+")) continue; //57, chemin du Lavoir, 01630 Sergy Haut, France. Electronic address: dfedson@wanadoo.fr.
			if(tmp.get(i).startsWith("ST. ") || tmp.get(i).startsWith("ST "))
				affPhrases.add("SAINT"+tmp.get(i).substring(tmp.get(i).indexOf(" ")));
			else
				affPhrases.add(tmp.get(i));
			//System.out.println(tmp.get(i)+"\t");
		}



		return affPhrases;
	}

	//input - 1) Chemical Engineering, California Institute of Technology - Center for the Science and Engineering of Materials, 1200 E. California Blvd., Chemical Engineering Mail Code 210-41, Pasadena, CA 91125-4100; 2) Department of Chemistry, Texarkana College, 2500 N. Robison Road, Texarkana, TX 75599-3078; 3) School of Science, Penn State Erie, The Behrend College, 5091 Station Road, Erie, PA 16563-0203; 4) St. Jude Children's Research Hospital, 332 N. Lauderdale Street, Mail Stop 507, Memphis, TN 38105-2794
	//output - [Chemical Engineering, California Institute of Technology - Center for the Science and Engineering of Materials, 1200 E. California Blvd., Chemical Engineering Mail Code 210-41, Pasadena, CA 91125-4100, Department of Chemistry, Texarkana College, 2500 N. Robison Road, Texarkana, TX 75599-3078, School of Science, Penn State Erie, The Behrend College, 5091 Station Road, Erie, PA 16563-0203, St. Jude Children's Research Hospital, 332 N. Lauderdale Street, Mail Stop 507, Memphis, TN 38105-2794]

	/**
	 * Parses the multiple affs.
	 *
	 * @param affString the aff string
	 * @return the array list
	 */
	public static ArrayList<String> parseMultipleAffs(String affString) {
		ArrayList<String> ret = new ArrayList<String>();
		if(affString.contains(";")){
			String[] splits = affString.split(";",-1);
			for (String split : splits) {
				split=split.trim();
				if(split.matches("\\d+\\) .*")){
					ret.add(split.substring(split.indexOf(" ")+1));
				}
				else
					ret.add(split);
			}
			return ret;
		}

		else if(affString.startsWith("* ")&&affString.length()>2){

		}

		ret.add(affString);
		return ret;
	}

	public boolean findMainOrgDictionaryEntry(HashMap<String, ArrayList<String>> normorgRowTable, AllAffiliations allAffs, DetailedNLPer nlp) {
		HashMap<String, ArrayList<String>> mainOrgRowTable = allAffs.mainOrgRowTable;

		
		String mainInstNorm = nlp.removeStopWordsAndStem(this.mainInstitution.toLowerCase());
		if(normorgRowTable.containsKey(mainInstNorm)){
			String[] splits=matchGPE(normorgRowTable.get(mainInstNorm));
			if(splits!=null){ 
				applyMasterMain(splits);
				return true;
			}

		}

	/*	for(String key: normorgRowTable.keySet()){
            System.out.println(key  +" :: "+ normorgRowTable.get(key));}
		for(String key: mainOrgRowTable.keySet()){
            System.out.println(key  +" :: "+ mainOrgRowTable.get(key));
        }*/

		//UCLA, UC Davis
		mainInstNorm+=" "+nlp.removeStopWordsAndStem(this.city==null?"":this.city.toLowerCase());
		if(normorgRowTable.containsKey(mainInstNorm)){
			String[] splits=matchGPE(normorgRowTable.get(mainInstNorm));
			if(splits!=null){ 
				applyMasterMain(splits);
				return true;
			}
		}

		//break at punctuation symbols [!"#$%&'()*+,\-./:;<=>?@[\\\]^_`{|}~]
		for (String affPhrase : this.mainInstitution.split("[#$%\\(\\)*+-/\\:;<=>?@\\[\\]\\{\\}|]")) {
			String affNorm = nlp.removeStopWordsAndStem(affPhrase.toLowerCase());
			if(normorgRowTable.containsKey(affNorm)){
				String[] splits=matchGPE(normorgRowTable.get(affNorm));
				if(splits!=null){ 
					applyMasterMain(splits);
					return true;
				}
			}
		}

		//too inaccurate because of dictionaries containing entries such as Hospitalization
		/*for (String normOrg : normorgRowTable.keySet()) {
			if((" "+mainInstNorm+" ").contains((" "+normOrg+" "))){
				String[] splits=normorgRowTable.get(normOrg).split("\t");
				if(matchGPE(splits)) 
					return true;
			}
		}*/

		for (String org : this.organizations) {
			String affNorm = nlp.removeStopWordsAndStem(org.toLowerCase());
			if(normorgRowTable.containsKey(affNorm)){
				String[] splits=matchGPE(normorgRowTable.get(affNorm));
				if(splits!=null){ 
					applyMasterMain(splits);
					return true;
				}
			}

			//break at punctuation symbols [!"#$%&'()*+,\-./:;<=>?@[\\\]^_`{|}~]
			for (String affPhrase1 : org.split("[#$%\\(\\)*+-/\\:;<=>?@\\[\\]\\{\\}|]")) {
				String affNorm1 = nlp.removeStopWordsAndStem(affPhrase1.toLowerCase());
				if(normorgRowTable.containsKey(affNorm1)){
					String[] splits=matchGPE(normorgRowTable.get(affNorm1));
					if(splits!=null){ 
						applyMasterMain(splits);
						return true;
					}
				}
			}

			//TODO: very loose rule (Healthed). broadening it for now by ensuring that the master main organization has organization keyword 
			//commenting because we are anyways doing the below
			/*	for (String normOrg : normorgRowTable.keySet()) {
				if((" "+affNorm+" ").contains((" "+normOrg+" "))){
					String[] splits=matchGPE(normorgRowTable.get(normOrg));
					if(splits!=null && 
							this.containsOrgKeyword(splits[2].toUpperCase())){
						applyMasterMain(splits);
						return true;
					}
				}
			}*/

			String mainInstU=this.mainInstitution.toUpperCase();
			
			double maxScore=SW_THRESHOLD;
			String[] maxSplits = null;
			try {
				String lucQ = (mainInstU+" "+(this.city==null?"":this.city+" ")+(this.region==null?"":this.region+" ")+(this.country==null?"":this.country+" ")).replaceAll("[^a-zA-Z0-9 ]", "").replaceAll("\\s+", " ")//.replace('/', ' ')
						.toLowerCase().trim().replaceAll("~?\\s+", "~ ")+"~";
				if(lucQ.equals("~")) {
					this.masterMainOrganizationId=-1;
					return false;
				}
				Query q = new QueryParser("mainOrg", allAffs.analyzer).parse(lucQ);
				TopScoreDocCollector collector = TopScoreDocCollector.create(100, true);
				allAffs.searcher.search(q, collector);
			    ScoreDoc[] hits = collector.topDocs().scoreDocs;
			    if(hits.length==0)
			    	System.err.println(this.mention + ": lucene might have missed a hit for want of optimization");
			    for(int i=0;i<hits.length;++i) {
			        int docId = hits[i].doc;
			        Document d = allAffs.searcher.doc(docId);
			        String mainOrg = d.get("mainOrg").toUpperCase();
			        /*if(mainOrg.equalsIgnoreCase("University of California"))
			        	System.out.println(mainOrgRowTable.get(mainOrg));
			        else
			        	System.err.println(mainOrgRowTable.get(mainOrg));*/
			        //String[] splits= d.get("row").split("\t",-1);
			        String[] splits=matchGPE(mainOrgRowTable.get(mainOrg));
					if(splits!=null){ 
						double score=NeoBio.getSmithWatermanMatch(mainOrg, mainInstU);
						if(score>maxScore) {
							if(mainInstU.contains(mainOrg)//David Geffen School of Medicine at the University of California//not the other way 
									//(this.city!=null && (this.city.equalsIgnoreCase(splits[11])||this.mention.contains(splits[11].toUpperCase())) && score==1.0) //University Of, Bolu, Not Listed, Turkey and [233456, 1, University of the Republic, , Institution, Main, http://www.cumhuriyet.edu.tr, , Imaret Koyu Cumhuriyet Unv., , , Sivas, --, TR]
									//Nagsaki Univeristy Medical School and Nagasaki University --- relaxing to just first character as same
									||((mainOrg.charAt(0)==mainInstU.charAt(0)
											||(this.city!=null && (this.city.equalsIgnoreCase(splits[11])||this.mention.contains(splits[11].toUpperCase())) ))
										&& this.dict.matchingNonOrgWords(mainOrg, mainInstU,nlp.stopwordsU)>SW_THRESHOLD)){
								maxScore=score;
								maxSplits=splits;
							}
						}
					}
			        //System.out.println((i + 1) + ". " + d.get("mainOrg") + "\t" + hits[i].score);
			     }
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(maxScore>SW_THRESHOLD){
				applyMasterMain(maxSplits);
				return true;
			}
			

		    
			/*maxScore=SW_THRESHOLD;
			maxSplits = null;
			//approximate matching all main organizations here
			for (String mainOrg : mainOrgRowTable.keySet()) {
				//first checking GPE
				String[] splits=matchGPE(mainOrgRowTable.get(mainOrg));
				if(splits!=null){ 
					double score=NeoBio.getSmithWatermanMatch(mainOrg, mainInstU);
					if(score>maxScore) {
						if(mainInstU.contains(mainOrg)//David Geffen School of Medicine at the University of California//not the other way 
								//(this.city!=null && (this.city.equalsIgnoreCase(splits[11])||this.mention.contains(splits[11].toUpperCase())) && score==1.0) //University Of, Bolu, Not Listed, Turkey and [233456, 1, University of the Republic, , Institution, Main, http://www.cumhuriyet.edu.tr, , Imaret Koyu Cumhuriyet Unv., , , Sivas, --, TR]
								//Nagsaki Univeristy Medical School and Nagasaki University --- relaxing to just first character as same
								||((mainOrg.charAt(0)==mainInstU.charAt(0)
										||(this.city!=null && (this.city.equalsIgnoreCase(splits[11])||this.mention.contains(splits[11].toUpperCase())) ))
								&& this.dict.matchingNonOrgWords(mainOrg, mainInstU,nlp.stopwordsU)>SW_THRESHOLD)){
							maxScore=score;
							maxSplits=splits;
						}
					}
				}
			} 
			if(maxScore>SW_THRESHOLD){
				applyMasterMain(maxSplits);
				return true;
			}*/
	
			/*for (String mainOrg : mainOrgRowTable.keySet()) {
				//first checking GPE
				String[] splits=matchGPE(mainOrgRowTable.get(mainOrg));
				if(splits!=null){ 
					int ed = NeoBio.editDistance(mainOrg, mainInstU);
					if(ed<=2//allows for one or two small mistakes 
							&& (this.city!=null && this.city.equalsIgnoreCase(splits[11]) || mainOrg.charAt(0)==mainInstU.charAt(0))//TODO: check scope of ||
							&& (ed==0||this.dict.matchingNonOrgWords(mainOrg, mainInstU,nlp.stopwordsU)>SW_THRESHOLD)){
						applyMasterMain(splits);
						return true;
					}
				}
			}*/
		}

		this.masterMainOrganizationId=-1;
		return false;
	}


	public boolean expandedMainOrgDictionaryEntry(String expanded_org, HashMap<String, ArrayList<String>> normorgRowTable, AllAffiliations allAffs, DetailedNLPer nlp) {
                HashMap<String, ArrayList<String>> mainOrgRowTable = allAffs.mainOrgRowTable;


                for (String org : this.organizations) {
                        String affNorm = nlp.removeStopWordsAndStem(org.toLowerCase());
                        if(normorgRowTable.containsKey(affNorm)){
                                String[] splits=matchGPE(normorgRowTable.get(affNorm));
                                if(splits!=null){
                                	return true;
                                }
                        }


                        //break at punctuation symbols [!"#$%&'()*+,\-./:;<=>?@[\\\]^_`{|}~]
                        for (String affPhrase1 : org.split("[#$%\\(\\)*+-/\\:;<=>?@\\[\\]\\{\\}|]")) {
                                String affNorm1 = nlp.removeStopWordsAndStem(affPhrase1.toLowerCase());
                                if(normorgRowTable.containsKey(affNorm1)){
                                        String[] splits=matchGPE(normorgRowTable.get(affNorm1));
                                        if(splits!=null){
                                                return true;
                                        }
                                }
                        }
                }

                        String mainInstU=expanded_org.toUpperCase();

                        double maxScore=SW_THRESHOLD;
                        String[] maxSplits = null;
                        try {
                                String lucQ = (mainInstU+" "+(this.city==null?"":this.city+" ")+(this.region==null?"":this.region+" ")+(this.country==null?"":this.country+" ")).replaceAll("[^a-zA-Z0-9 ]", "").replaceAll("\\s+", " ")//.replace('/', ' ')
                                                .toLowerCase().trim().replaceAll("~?\\s+", "~ ")+"~";

                                if(lucQ.equals("~")) {
                                        return false;
                                }
                                Query q = new QueryParser("mainOrg", allAffs.analyzer).parse(lucQ);
                                TopScoreDocCollector collector = TopScoreDocCollector.create(100, true);
                                allAffs.searcher.search(q, collector);
                            ScoreDoc[] hits = collector.topDocs().scoreDocs;
                            if(hits.length==0)
                                System.err.println(this.mention + ": lucene might have missed a hit for want of optimization");
                            for(int i=0;i<hits.length;++i) {
                                int docId = hits[i].doc;
                                Document d = allAffs.searcher.doc(docId);
                                String mainOrg = d.get("mainOrg").toUpperCase();
                                String[] splits=matchGPE(mainOrgRowTable.get(mainOrg));
                                        if(splits!=null){
                                                double score=NeoBio.getSmithWatermanMatch(mainOrg, mainInstU);
                                                if(score>maxScore) {
                                                        if(mainInstU.contains(mainOrg) || ((mainOrg.charAt(0)==mainInstU.charAt(0)||(this.city!=null && (this.city.equalsIgnoreCase(splits[11]) || this.mention.contains(splits[11].toUpperCase())))) && this.dict.matchingNonOrgWords(mainOrg, mainInstU,nlp.stopwordsU)>SW_THRESHOLD)){
                                                                maxScore=score;
								maxSplits=splits;
                                                        }
                                                }
                                        }
                                //System.out.println((i + 1) + ". " + d.get("mainOrg") + "\t" + hits[i].score);
                             }
                        } catch (ParseException e) {
                                e.printStackTrace();
                        } catch (IOException e) {
                                e.printStackTrace();
                        }
                        if(maxScore>SW_THRESHOLD){
                                return true;
                        }

                return false;
        }	


	private void applyMasterMain(String[] splits) {
		this.masterMainOrganization=splits[2];
		this.masterMainOrganizationId=Integer.parseInt(splits[0]);

		String city=splits[11].toUpperCase();
		//Neuroscience Graduate Program, University of Michigan, USA.	country=United States	region=Michigan	city=Michigan	address=	organizations=[UNIVERSITY OF MICHIGAN, NEUROSCIENCE GRADUATE PROGRAM]	mainInstitution=University of Michigan	masterMainOrganization=University of Michigan	masterMainOrganizationId=29114	
		if((this.city==null || (this.region!=null && this.city.equals(this.region))) && !city.equals("NO CITY"))
			this.city=city;
	}

	private String[] matchGPE(ArrayList<String> masterOrgs) {
		for (String masterOrg : masterOrgs) {
			String[] splits=masterOrg.split("\t",-1);
			String masterOrgU = masterOrg.toUpperCase();

			/*if(!splits[2].equalsIgnoreCase("Catholic University of Daegu")) continue;
			System.err.println(masterOrg);*/

			String city=splits[11].toUpperCase();String state=splits[12].toUpperCase();String country=splits[13].toUpperCase();
			if(dict.countryTable.containsKey(country)) 
				country=dict.countryTable.get(country);
			else if(dict.ccTLDs.containsKey(country)) 
				country=dict.ccTLDs.get(country);
			else
				country="??";

			state=this.normalizeUSregions(state, country);
			state=this.dict.regionExpansions.getOrDefault(splits[13].toUpperCase()+"_"+state, state);
			/*			if((this.city==null||city.equals(this.city)||city.equals("NO CITY"))
					&& (this.region==null||state.equals(this.region)||state.equals("--")||state.length()<3)//last condition temporary. abbreviations such as CA, AZ, need to be expanded if necessary
			 */
			//Department of Community Medicine, Kasturba Medical College, Manipal - 576 104, Karnataka, India.
			//12953	2	Department of Community Medicine	2324	Institution	Dept.	http://afmc.nic.in/Departments/CommunityMedicine/homepage.html		No Address			Pune	MM	IN
			//MM has to be expanded to Maharashtra
			/*if((this.city==null||city.equals(this.city)||city.equals("NO CITY") || this.region==null||state.equals(this.region)||state.equals("--"))//||state.length()<3)//last condition temporary. abbreviations such as CA, AZ, need to be expanded if necessary
					//[12952, 2, Department of Community Medicine, 2689, Institution, Dept., http://www.pmc.edu.pk/community.htm, , No Address, , , No City, --, ??]
					&& (this.country==null||country.equals(this.country))){//||country.equals("??")||country.length()<3)){//last condition temporary. 
			 */		
			//feb 12 2015  -- making it stricter to make either city or region be the same
			//Catholic University of Medical Center, Seoul, Not Listed, South Korea	country=Korea, South	region=null	city=Seoul	address=	organizations=[Catholic University of Medical Center]	mainInstitution=Catholic University of Medical Center	masterMainOrganization=Catholic University of Daegu	masterMainOrganizationId=3137
			if((city.equals(this.city!=null?this.city:"NULL") || masterOrgU.contains(this.city!=null?this.city:"NULL")
					|| state.equals(this.region!=null?this.region:"NULL") || masterOrgU.contains(this.region!=null?this.region:"NULL"))
					&& (this.country==null||country.equals(this.country))){//||country.equals("??")||country.length()<3)){//last condition temporary. 
				return splits;
			}
		}

		return null;

	}

	private String[] matchGPELoose(ArrayList<String> masterOrgs) {
		String[] splits=matchGPE(masterOrgs);
		if(splits==null){ 
			splits=masterOrgs.get(0).split("\t");
		}
		return splits;
	}

	public void findOtherOrgDictionaryEntry(AllAffiliations allAffs,
			DetailedNLPer nlp) {
		for (String org : this.organizations) {
			String affNorm = nlp.removeStopWordsAndStem(org.toLowerCase());
			if(allAffs.normBranchOrgRowTable.containsKey(affNorm)){
				String[] splits=matchGPE(allAffs.normBranchOrgRowTable.get(affNorm));
				if(splits!=null){ 
					this.masterBranchOrganization=splits[2];this.masterBranchOrganizationId=Integer.parseInt(splits[0]);
					allAffs.fillRest(splits, this);
				}
			}
			else if(allAffs.normSchoolOrgRowTable.containsKey(affNorm)){
				String[] splits=matchGPE(allAffs.normSchoolOrgRowTable.get(affNorm));
				if(splits!=null){ 
					this.masterSchoolOrganization=splits[2];this.masterSchoolOrganizationId=Integer.parseInt(splits[0]);
					allAffs.fillRest(splits, this);
				}
			} 
			else if(allAffs.normCollegeOrgRowTable.containsKey(affNorm)){
				String[] splits=matchGPE(allAffs.normCollegeOrgRowTable.get(affNorm));
				if(splits!=null){ 
					this.masterCollegeOrganization=splits[2];this.masterCollegeOrganizationId=Integer.parseInt(splits[0]);
					allAffs.fillRest(splits, this);
				}
			} 
			else if(allAffs.normDepartmentOrgRowTable.containsKey(affNorm)){
				String[] splits=matchGPE(allAffs.normDepartmentOrgRowTable.get(affNorm));
				if(splits!=null){ 
					this.masterDepartmentOrganization=splits[2];this.masterDepartmentOrganizationId=Integer.parseInt(splits[0]);
					//Department of Community Medicine, Kasturba Medical College, Manipal - 576 104, Karnataka, India. 
					//-- Department of Community Medicine can be at multiple other places
					if(this.masterDepartmentOrganization.startsWith("Department of")) continue;
					allAffs.fillRest(splits, this);
				}
			} 
			else if(allAffs.normServicesOrgRowTable.containsKey(affNorm)){
				String[] splits=matchGPE(allAffs.normServicesOrgRowTable.get(affNorm));
				if(splits!=null){ 
					this.masterServicesOrganization=splits[2];this.masterServicesOrganizationId=Integer.parseInt(splits[0]);
					allAffs.fillRest(splits, this);
				}
			} 
			else if(allAffs.normCenterOrgRowTable.containsKey(affNorm)){
				String[] splits=matchGPE(allAffs.normCenterOrgRowTable.get(affNorm));
				if(splits!=null){ 
					this.masterCentreOrganization=splits[2];this.masterCentreOrganizationId=Integer.parseInt(splits[0]);
					allAffs.fillRest(splits, this);
				}
			} 
		}

		//now do dictionary matching
		if(this.masterBranchOrganization==null){
			for (String org : this.organizations){
				if(this.mainInstitution.equalsIgnoreCase(org)) continue;
				if(org.endsWith(" COLLEGE")||(" "+org+" ").contains(" CLINIC ")||(" "+org+" ").contains(" BRANCH ")||org.startsWith("FACULTY ")){
					this.masterBranchOrganization=org;
					break;
				}
			}
			if(this.masterBranchOrganization==null) this.masterBranchOrganization="";
		}

		if(this.masterSchoolOrganization==null){
			for (String org : this.organizations){
				if(this.mainInstitution.equalsIgnoreCase(org)||this.masterBranchOrganization.equalsIgnoreCase(org)) continue;
				if((" "+org+" ").contains(" SCHOOL ")){
					this.masterSchoolOrganization=org;
					break;
				}
			}
			if(this.masterSchoolOrganization==null) this.masterSchoolOrganization="";
		}

		if(this.masterCollegeOrganization==null){
			for (String org : this.organizations){
				if(this.mainInstitution.equalsIgnoreCase(org)||this.masterBranchOrganization.equalsIgnoreCase(org)||this.masterSchoolOrganization.equalsIgnoreCase(org)) continue;
				if(org.startsWith("COLLEGE OF")){
					this.masterCollegeOrganization=org;
					break;
				}
			}
			if(this.masterCollegeOrganization==null) this.masterCollegeOrganization="";
		}

		if(this.masterDepartmentOrganization==null){
			for (String org : this.organizations){
				if(this.mainInstitution.equalsIgnoreCase(org)||this.masterBranchOrganization.equalsIgnoreCase(org)||this.masterSchoolOrganization.equalsIgnoreCase(org)||this.masterCollegeOrganization.equalsIgnoreCase(org)) continue;
				if((" "+org+" ").contains(" DEPARTMENT ")||(" "+org+" ").contains(" DIVISION ")){
					this.masterDepartmentOrganization=org;
					break;
				}
			}
			if(this.masterDepartmentOrganization==null) this.masterDepartmentOrganization="";
		}

		if(this.masterServicesOrganization==null){
			for (String org : this.organizations){
				if(this.mainInstitution.equalsIgnoreCase(org)||this.masterBranchOrganization.equalsIgnoreCase(org)||this.masterSchoolOrganization.equalsIgnoreCase(org)||this.masterCollegeOrganization.equalsIgnoreCase(org)|this.masterDepartmentOrganization.equalsIgnoreCase(org)) continue;
				if((" "+org+" ").contains(" SERVICE ")||(" "+org+" ").contains(" UNIT ")){
					this.masterServicesOrganization=org;
					break;
				}
			}
			if(this.masterServicesOrganization==null) this.masterServicesOrganization="";
		}

		if(this.masterCentreOrganization==null){
			for (String org : this.organizations){
				if(this.mainInstitution.equalsIgnoreCase(org)||this.masterBranchOrganization.equalsIgnoreCase(org)||this.masterSchoolOrganization.equalsIgnoreCase(org)||this.masterCollegeOrganization.equalsIgnoreCase(org)|this.masterDepartmentOrganization.equalsIgnoreCase(org)||this.masterServicesOrganization.equals(org)) continue;
				if((" "+org+" ").contains(" CENTER ")||(" "+org+" ").contains(" CENTRE ")){
					this.masterCentreOrganization=org;
					break;
				}
			}
			if(this.masterCentreOrganization==null) this.masterCentreOrganization="";
		}
	}

	public boolean idEquals(Affiliation aff, DetailedNLPer nlp) {
		if(this.masterMainOrganizationId==-1)
			return false;
		return this.masterMainOrganizationId==aff.masterMainOrganizationId;
	}



}
