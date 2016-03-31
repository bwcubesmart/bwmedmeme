package production;

import java.io.*;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import neobio.alignment.BasicScoringScheme;
import neobio.alignment.IncompatibleScoringSchemeException;
import neobio.alignment.NeoBio;
import neobio.alignment.PairwiseAlignmentAlgorithm;
import neobio.alignment.SmithWaterman;
import normalization.AffiliationMainOrgNormalizer;
import opennlp.DetailedNLPer;
import parsing.Affiliation;
import parsing.Dictionaries;
import utils.MyDateFormatter;

public class AllAffiliations {

	public void printWrite(String detail,String input)
        {
                try {
                PrintWriter pw = new PrintWriter(new FileOutputStream( new File("../NLP Process/KOLs/Output/fillRest.txt"), true /* append = true */));
                                pw.println(detail + input);
                                        if (pw != null)
                                           pw.close();
                }
                catch (Exception ex)
                {
                }
        }
	
	public void printWrite1(String detail)
        {
                try {
                PrintWriter pw = new PrintWriter(new FileOutputStream( new File("../NLP Process/KOLs/Output/normMainOrgRowTable.txt"), true /* append = true */));
                                pw.println(detail);
                                        if (pw != null)
                                           pw.close();
                }
                catch (Exception ex)
                {
                }
        }
	public void printWrite2(String detail)
        {
                try {
                PrintWriter pw = new PrintWriter(new FileOutputStream( new File("../NLP Process/KOLs/Output/normBranchOrgRowTable.txt"), true /* append = true */));
                                pw.println(detail);
                                        if (pw != null)
                                           pw.close();
                }
                catch (Exception ex)
                {
                }
        }
	public void printWrite3(String detail)
        {
                try {
                PrintWriter pw = new PrintWriter(new FileOutputStream( new File("../NLP Process/KOLs/Output/normSchoolOrgRowTable.txt"), true /* append = true */));
                                pw.println(detail);
                                        if (pw != null)
                                           pw.close();
                }
                catch (Exception ex)
                {
                }
        }
	public void printWrite4(String detail)
        {
                try {
                PrintWriter pw = new PrintWriter(new FileOutputStream( new File("../NLP Process/KOLs/Output/normCollegeOrgRowTable.txt"), true /* append = true */));
                                pw.println(detail);
                                        if (pw != null)
                                           pw.close();
                }
                catch (Exception ex)
                {
                }
        }
	public void printWrite5(String detail)
        {
                try {
                PrintWriter pw = new PrintWriter(new FileOutputStream( new File("../NLP Process/KOLs/Output/normDepartmentOrgRowTable.txt"), true /* append = true */));
                                pw.println(detail);
                                        if (pw != null)
                                           pw.close();
                }
                catch (Exception ex)
                {
                }
        }
	public void printWrite6(String detail)
        {
                try {
                PrintWriter pw = new PrintWriter(new FileOutputStream( new File("../NLP Process/KOLs/Output/normServiceOrgRowTable.txt"), true /* append = true */));
                                pw.println(detail);
                                        if (pw != null)
                                           pw.close();
                }
                catch (Exception ex)
                {
                }
        }
	public void printWrite7(String detail)
        {
                try {
                PrintWriter pw = new PrintWriter(new FileOutputStream( new File("../NLP Process/KOLs/Output/normCenterOrgRowTable.txt"), true /* append = true */));
                                pw.println(detail);
                                        if (pw != null)
                                           pw.close();
                }
                catch (Exception ex)
                {
                }
        }

	public void printWriteArray(String detail,String[] splits)
        {
                try {
                StringBuffer buf=new StringBuffer();
                PrintWriter pw = new PrintWriter(new FileOutputStream( new File("../NLP Process/KOLs/Output/fillRest.txt"), true /* append = true */));
                                int len=splits.length;
                                for (int j=0;j< len;j++)
                                {
                                    buf.append(splits[j]+ "|");
                                }
                                pw.println(detail + buf.toString() +"||" + len);
                                        if (pw != null)
                                           pw.close();
                }
                catch (Exception ex)
                {
                }
}

	public HashMap<String, String> id2LineTable;
	
	public HashMap<String, ArrayList<String>> normMainOrgRowTable;
	public HashMap<String, ArrayList<String>> mainOrgRowTable;//feb 3 2015
	//public HashMap<String, ArrayList<String>> normMainAbbrvRowTable;
	public Directory mainOrgIndex;
	public StandardAnalyzer analyzer;
	
	public HashMap<String, ArrayList<String>> normInferredOrgRowTable;
	
	public HashMap<String, ArrayList<String>> normBranchOrgRowTable;
	public HashMap<String, ArrayList<String>> normSchoolOrgRowTable;
	public HashMap<String, ArrayList<String>> normCollegeOrgRowTable;
	public HashMap<String, ArrayList<String>> normDepartmentOrgRowTable;
	public HashMap<String, ArrayList<String>> normServicesOrgRowTable;
	public HashMap<String, ArrayList<String>> normCenterOrgRowTable;
	public DetailedNLPer nlp;
	
	private int clusterSeed=9000000;
	
	IndexReader reader;
    public IndexSearcher searcher;
	public AllAffiliations(DetailedNLPer nlp) {
		this.nlp=nlp;
		
		id2LineTable=new HashMap<String, String>();		
		normMainOrgRowTable=new HashMap<String, ArrayList<String>>();
		mainOrgRowTable=new HashMap<String, ArrayList<String>>();
		mainOrgIndex = new RAMDirectory();
		analyzer = new StandardAnalyzer();
	    IndexWriterConfig config = new IndexWriterConfig(Version.LATEST, analyzer);
	    IndexWriter w = null ;
	    try {
			w = new IndexWriter(mainOrgIndex, config);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		//normMainAbbrvRowTable=new HashMap<String, ArrayList<String>>();
		
		normInferredOrgRowTable = new HashMap<String, ArrayList<String>>();
		
		normBranchOrgRowTable=new HashMap<String, ArrayList<String>>();
		normSchoolOrgRowTable=new HashMap<String, ArrayList<String>>();
		normCollegeOrgRowTable=new HashMap<String, ArrayList<String>>();
		normDepartmentOrgRowTable=new HashMap<String, ArrayList<String>>();
		normServicesOrgRowTable=new HashMap<String, ArrayList<String>>();
		normCenterOrgRowTable=new HashMap<String, ArrayList<String>>();
		try {
			BufferedReader br = new BufferedReader(new FileReader("../NLP Process/Organization Master.tsv.txt"));
			
			br.readLine();
			//int c=0;
			while(br.ready()){
				String line=br.readLine();
				//c++; if(c%1000==0) System.err.println("read orgs---"+c);
				String[] splits=line.split("\t");
				//if(!splits[0].equals("29167")) continue;
				 
				id2LineTable.put(splits[0], line);
				String org=splits[2];
				//System.out.println("Org:" +org);
				//if(!org.equals("Titu Maiorescu University")) continue;
				String normorg=nlp.removeStopWordsAndStem(org.toLowerCase());
				if(splits[5].equals("Main")){
					if(splits.length<14
							|| splits[11].equalsIgnoreCase("NO CITY")
							//|| splits[12].equalsIgnoreCase("--") //compromising here
							|| splits[13].equalsIgnoreCase("??")
							) continue;
					
					String orgU=org.toUpperCase();
					if(!mainOrgRowTable.containsKey(orgU)) mainOrgRowTable.put(orgU, new ArrayList<String>());
					mainOrgRowTable.get(orgU).add(line);
					addOrgToLucene(w, orgU, line);
					
					if(!normMainOrgRowTable.containsKey(normorg)) normMainOrgRowTable.put(normorg, new ArrayList<String>());
					normMainOrgRowTable.get(normorg).add(line);
					
					//abbreviations
					if(!splits[7].trim().equals("")){
						if(!normMainOrgRowTable.containsKey(splits[7])) normMainOrgRowTable.put(splits[7], new ArrayList<String>());
						normMainOrgRowTable.get(splits[7]).add(line);
					}
					
				}
				else if(splits[5].equals("Branch")){
					if(!normBranchOrgRowTable.containsKey(normorg)) normBranchOrgRowTable.put(normorg, new ArrayList<String>());
					normBranchOrgRowTable.get(normorg).add(line);
				}
				else if(splits[5].equals("School")){
					if(!normSchoolOrgRowTable.containsKey(normorg)) normSchoolOrgRowTable.put(normorg, new ArrayList<String>());
					normSchoolOrgRowTable.get(normorg).add(line);
				}
				else if(splits[5].equals("College")){
					if(!normCollegeOrgRowTable.containsKey(normorg)) normCollegeOrgRowTable.put(normorg, new ArrayList<String>());
					normCollegeOrgRowTable.get(normorg).add(line);
				}
				else if(splits[5].equals("Dept.")){
					if(!normDepartmentOrgRowTable.containsKey(normorg)) normDepartmentOrgRowTable.put(normorg, new ArrayList<String>());
					normDepartmentOrgRowTable.get(normorg).add(line);
				}
				else if(splits[5].equals("Service")){
					if(!normServicesOrgRowTable.containsKey(normorg)) normServicesOrgRowTable.put(normorg, new ArrayList<String>());
					normServicesOrgRowTable.get(normorg).add(line);
				}
				else if(splits[5].equals("Centre")){
					if(!normCenterOrgRowTable.containsKey(normorg)) normCenterOrgRowTable.put(normorg, new ArrayList<String>());
					normCenterOrgRowTable.get(normorg).add(line);
				}

			}
			br.close();
			
			for(String key1 :normMainOrgRowTable.keySet()){
                		printWrite1(key1  +" :: "+ normMainOrgRowTable.get(key1));
                	}
			for(String key2 :normBranchOrgRowTable.keySet()){
                		printWrite2(key2  +" :: "+ normBranchOrgRowTable.get(key2));
                	}
			for(String key3 :normSchoolOrgRowTable.keySet()){
                		printWrite3(key3  +" :: "+ normSchoolOrgRowTable.get(key3));
                	}
			for(String key4 :normCollegeOrgRowTable.keySet()){
                		printWrite4(key4  +" :: "+ normCollegeOrgRowTable.get(key4));
                	}
			for(String key5 :normDepartmentOrgRowTable.keySet()){
                		printWrite5(key5  +" :: "+ normDepartmentOrgRowTable.get(key5));
                	}
			for(String key6 :normServicesOrgRowTable.keySet()){
                		printWrite6(key6  +" :: "+ normServicesOrgRowTable.get(key6));
                	}
			for(String key7 :normCenterOrgRowTable.keySet()){
                		printWrite7(key7  +" :: "+ normCenterOrgRowTable.get(key7));
                	}

		    w.close();
		    reader = DirectoryReader.open(this.mainOrgIndex);
		    searcher = new IndexSearcher(reader);
		    
			System.err.println("doing clustering directly without using precomputed clusters");
			/*br = new BufferedReader(new FileReader("../NLP Process/randomized_200000_10032014161533Pubmed_Affliation_export_10012014_out_clusteredMainOrgs_USED_AS_DICTIONARY.txt"));
			
			br.readLine();
			while(br.ready()){
				String line = br.readLine();
				String[] splits=line.split("\"\\|\"");
				String org=splits[7];
				int orgID=Integer.parseInt(splits[8]);
				if(orgID<AffiliationMainOrgNormalizer.seed) continue;
				
				String normorg=nlp.removeStopWordsAndStem(org.toLowerCase());
				if(!normInferredOrgRowTable.containsKey(normorg)) normInferredOrgRowTable.put(normorg, new ArrayList<String>());
				normInferredOrgRowTable.get(normorg).add(line);
			}
			br.close();*/
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private void addOrgToLucene(IndexWriter w, String orgU, String line) {
		Document doc = new Document();
	    doc.add(new TextField("mainOrg", orgU.toLowerCase(), Field.Store.YES));

	    // use a string field for isbn because we don't want it tokenized
	    //doc.add(new TextField("row", "", Field.Store.YES));
	    try {
			w.addDocument(doc, this.analyzer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	public static void process(String fileName, Dictionaries dict, AllAffiliations allAffs){
		ArrayList<Affiliation> affs = new ArrayList<Affiliation>();

		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(
					fileName
					), "UTF-8"));
			while(br.ready()){
				String row = br.readLine();
				String[] splits = row.split("\\|",-1);
				String txtAffiliation = ((splits.length>7?splits[7]:"")+", "+ (splits.length>8?splits[8]:"")+", "+(splits.length>9?splits[9]:"")+", "+(splits.length>10?splits[10]:"")).replaceAll("\"", "");
				txtAffiliation=txtAffiliation.replaceAll(", ,",  ",").trim();
				if(txtAffiliation.endsWith(",")) txtAffiliation=txtAffiliation.substring(0, txtAffiliation.length()-1);
				System.out.println("txtAffiliation:" +txtAffiliation);

				Affiliation aff = new Affiliation(txtAffiliation,dict);
				aff.findMainOrgDictionaryEntry(allAffs.normMainOrgRowTable,allAffs, allAffs.nlp);
				aff.findOtherOrgDictionaryEntry(allAffs,allAffs.nlp);
				aff.findInferredMainOrg(allAffs.normInferredOrgRowTable,allAffs.nlp);
				
				if(aff.masterMainOrganizationId!=-1){
					//aff.print(pwr);
				}
				else{
					allAffs.matchTheUnmatched(aff, affs);
					//aff.print(pwr);
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	*/
	
	public static void main(String[] args) throws IOException {
		
		String inputFile=
				//"Pubmed_Affliation_export_10012014.txt"
				//"univ_penn_affs.txt"
				"gold_affs.txt"
				;
		
		//String inputDir="/Users/Shared/TechSmith/";//Impactmeme_Affiliations Input_for Sid/";//GOLD/
		String inputDir="/home/solr/ConfParser/tmp/";//Impactmeme_Affiliations Input_for Sid/";//GOLD/
		//comment the below if you want to use the input file name specified above
		File f = new File(inputDir);
		String[] ls = f.list();
		for (String fileName : ls) {
			if(fileName.endsWith(".txt"))
				inputFile=fileName;
		}
		
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(
				inputDir+inputFile
				), "UTF-8"));
		
		String outputDir="../NLP Process/Affiliation/Output/";
		PrintWriter pwr = new PrintWriter(outputDir+inputFile.substring(0, inputFile.lastIndexOf(".txt"))+"_out.txt", "UTF-8");
		pwr.print("\"mention\"");
		pwr.print("|\"country\"");
		pwr.print("|\"region\"");
		pwr.print("|\"city\"");
		pwr.print("|\"address\"");
		pwr.print("|\"All organizations\"");
		pwr.print("|\"mainInstitution\"");
		pwr.print("|\"masterMainOrganization\"");
		pwr.print("|\"masterMainOrganizationId\"");
		pwr.print("|\"masterBranchOrganization\"");
		pwr.print("|\"masterSchoolOrganization\"");
		pwr.print("|\"masterCollegeOrganization\"");
		pwr.print("|\"masterDepartmentOrganization\"");
		pwr.print("|\"masterServicesOrganization\"");
		pwr.print("|\"masterCentreOrganization\"");
		pwr.println();
		
		
		//
		ArrayList<Affiliation> affs = new ArrayList<Affiliation>();
		DetailedNLPer nlp = new DetailedNLPer();
		AllAffiliations allAffs = new AllAffiliations(nlp);
		Dictionaries dict=new Dictionaries();
		long begTime = System.currentTimeMillis();

		int c=0;
		while(br.ready()){
			String txtAffiliation = br.readLine();
			txtAffiliation=txtAffiliation.replaceAll(", ,",  ",").trim();
			if(txtAffiliation.endsWith(",")) txtAffiliation=txtAffiliation.substring(0, txtAffiliation.length()-1);
			if(txtAffiliation.startsWith(",")) txtAffiliation=txtAffiliation.substring(1);
			txtAffiliation=txtAffiliation.trim();
			
			Affiliation aff = new Affiliation(txtAffiliation,dict);
			aff.findMainOrgDictionaryEntry(allAffs.normMainOrgRowTable,allAffs, allAffs.nlp);
			aff.findOtherOrgDictionaryEntry(allAffs,allAffs.nlp);
			aff.findInferredMainOrg(allAffs.normInferredOrgRowTable,allAffs.nlp);

			if(aff.masterMainOrganizationId!=-1){
				aff.print(pwr);
			}
			else{
				allAffs.matchTheUnmatched(aff, affs);
				aff.print(pwr);
			}
			c++;
			if(c%1000==0) {System.err.println("processed "+c+" lines");}
		}
		br.close();
		
		
		pwr.close();
		System.out.println("Total time taken in ms: "+(System.currentTimeMillis()-begTime));

	}

	public boolean matchTheUnmatched(Affiliation aff, ArrayList<Affiliation> affs) {

		//////
		String mainInstU=aff.mainInstitution.toUpperCase();
		for(Affiliation aff2: affs){
			String mainOrgU = aff2.masterMainOrganization.toUpperCase();
			if(((aff2.country==null||aff.country==null||aff2.country.equalsIgnoreCase(aff.country))
					//&&this.city!=null &&aff.city!=null &&this.city.equals(aff.city)
					&&(aff2.city==null ||aff.city==null || aff2.city.equalsIgnoreCase(aff.city) 
							//|| (this.region==null || aff.region==null || this.region.equalsIgnoreCase(aff.region))) //sid added on Feb 3 2015
							|| (aff2.region!=null && aff.region!=null && aff2.region.equalsIgnoreCase(aff.region))))){//allows for one or two small mistakes 
				int ed = NeoBio.editDistance(mainOrgU, mainInstU);
				if(ed<=2//allows for one or two small mistakes 
						&& (((aff2.city!=null && aff.city!=null && aff2.city.equalsIgnoreCase(aff.city)) || (aff2.region!=null && aff.region!=null && aff2.region.equalsIgnoreCase(aff.region))) 
								&& mainOrgU.length()>0 && mainInstU.length()>0 && mainOrgU.charAt(0)==mainInstU.charAt(0)
						&& (ed==0||aff.dict.matchingNonOrgWords(mainOrgU, mainInstU,nlp.stopwordsU)>Affiliation.SW_THRESHOLD))){
					aff.masterMainOrganization=aff2.masterMainOrganization;
					aff.masterMainOrganizationId=aff2.masterMainOrganizationId;
					return true;
				}
			}
		}
		
		double maxScore=Affiliation.SW_THRESHOLD;
		Affiliation maxAff = null;
		
		
		for(Affiliation aff2: affs){
			String mainOrgU = aff2.masterMainOrganization.toUpperCase();
			if((aff2.country==null||aff.country==null||aff2.country.equalsIgnoreCase(aff.country))
								//&&this.city!=null &&aff.city!=null &&this.city.equals(aff.city)
								&&(aff2.city!=null && aff.city!=null && aff2.city.equalsIgnoreCase(aff.city)
										//|| (this.region==null || aff.region==null || this.region.equalsIgnoreCase(aff.region))) //sid added on Feb 3 2015
										|| (aff2.region!=null && aff.region!=null && aff2.region.equalsIgnoreCase(aff.region)))){
				double score=NeoBio.getSmithWatermanMatch(mainOrgU, mainInstU);
				if(score>maxScore) {
					if(//(this.city!=null&&this.city.equalsIgnoreCase(splits[11])) || - sometimes even the cities are the same: Zuyd University, Maastricht, Not Listed, Netherlands mapped to Maastricht University
							aff.dict.matchingNonOrgWords(mainOrgU, mainInstU,nlp.stopwordsU)>Affiliation.SW_THRESHOLD){
						maxScore=score;
						maxAff=aff2;
					}
				}
				
				/*double score=NeoBio.getSmithWatermanMatch(mainOrgU, mainInstU);
				if(score>maxScore) {
					if((this.city!=null && (this.city.equalsIgnoreCase(splits[11])||this.mention.contains(splits[11].toUpperCase())) && score==1.0) //University Of, Bolu, Not Listed, Turkey and [233456, 1, University of the Republic, , Institution, Main, http://www.cumhuriyet.edu.tr, , Imaret Koyu Cumhuriyet Unv., , , Sivas, --, TR]
							//Nagsaki Univeristy Medical School and Nagasaki University --- relaxing to just first character as same
							||((mainOrg.charAt(0)==mainInstU.charAt(0)
							||(this.city!=null && (this.city.equalsIgnoreCase(splits[11])||this.mention.contains(splits[11].toUpperCase())) ))
							&& this.dict.matchingNonOrgWords(mainOrg, mainInstU,nlp.stopwordsU)>SW_THRESHOLD)){
						maxScore=score;
						maxSplits=splits;
					}
				}*/
			}
		}
		if(maxScore>Affiliation.SW_THRESHOLD){
			aff.masterMainOrganization=maxAff.masterMainOrganization;
			aff.masterMainOrganizationId=maxAff.masterMainOrganizationId;
			return true;
		}
		
		affs.add(aff);
		aff.masterMainOrganization=aff.mainInstitution;
		aff.masterMainOrganizationId=++this.clusterSeed;
		return false;
		
	}
	
	public void fillRest(String[] splits, Affiliation aff) {
		// splits example:2464	2	Harvard Medical School	20250	Institution	Branch	http://hms.harvard.edu	HMS	25 Shattuck Street			Boston	MA	US
		if(splits[1].equals("1")||splits[3].equals("")) return;
		ArrayList<String> orgU1List= new ArrayList<String>(Arrays.asList(splits[2].toUpperCase().split("\\s+|'|-")));
		orgU1List.removeAll(aff.dict.orgKeywords);
		orgU1List.removeAll(this.nlp.stopwordsU);
		orgU1List.removeAll(aff.dict.englishWords);//this is riskly, but otherwise words like biomedical and sciences are not disappearing
		if(orgU1List.size()==0) 
			return;//likely to be a generic name such as "School of Biomedical Sciences"
		
		String city=splits[11].toUpperCase();String state=splits[12].toUpperCase();String country=splits[13].toUpperCase();
		if(city.equals("NO CITY") || state.equals("--") ||country.equals("??"))
			return; //don't do anything with incomplete information
		String temp=splits[3];
		String temp1=this.id2LineTable.get(splits[3]);
		
		printWrite("temp from fillRest:",temp);
		printWrite("temp1 from fillRest:",temp1);

		
		splits=this.id2LineTable.get(splits[3]).split("\t",-1);
		printWriteArray("splits from fillRest:",splits);
		if(splits[5].equals("Main")){
			if(aff.masterMainOrganizationId!=-1) return;
			aff.masterMainOrganization=splits[2];aff.masterMainOrganizationId=Integer.parseInt(splits[0]);
		}
		else if(splits[5].equals("Branch")){
			if(aff.masterBranchOrganizationId!=-1) return;
			aff.masterBranchOrganization=splits[2];aff.masterBranchOrganizationId=Integer.parseInt(splits[0]);
			this.fillRest(splits, aff);
		}
		else if(splits[5].equals("School")){
			if(aff.masterSchoolOrganizationId!=-1) return;
			aff.masterSchoolOrganization=splits[2];aff.masterSchoolOrganizationId=Integer.parseInt(splits[0]);
			this.fillRest(splits, aff);
		}
		else if(splits[5].equals("College")){
			if(aff.masterCollegeOrganizationId!=-1) return;
			aff.masterCollegeOrganization=splits[2];aff.masterCollegeOrganizationId=Integer.parseInt(splits[0]);
			this.fillRest(splits, aff);
		}
		else if(splits[5].equals("Dept.")){
			if(aff.masterDepartmentOrganizationId!=-1) return;
			aff.masterDepartmentOrganization=splits[2];aff.masterDepartmentOrganizationId=Integer.parseInt(splits[0]);
			this.fillRest(splits, aff);
		}
		else if(splits[5].equals("Service")){
			if(aff.masterServicesOrganizationId!=-1) return;
			aff.masterServicesOrganization=splits[2];aff.masterServicesOrganizationId=Integer.parseInt(splits[0]);
			this.fillRest(splits, aff);
		}
		else if(splits[5].equals("Centre")){
			if(aff.masterCentreOrganizationId!=-1) return;
			printWrite("masterCentreOrganization from fillRest:",splits[2]);
			aff.masterCentreOrganization=splits[2];aff.masterCentreOrganizationId=Integer.parseInt(splits[0]);
			this.fillRest(splits, aff);
		}
	}
}
