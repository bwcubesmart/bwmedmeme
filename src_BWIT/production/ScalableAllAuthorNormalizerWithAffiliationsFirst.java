package production;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import opennlp.DetailedNLPer;

import org.apache.commons.lang3.StringUtils;

import parsing.Affiliation;
import parsing.AuthorVector;
import parsing.Dictionaries;
import utils.Flags;
import utils.MyDateFormatter;
import utils.StaticStringOps;
import utils.WordFrequencyCounter;

public class ScalableAllAuthorNormalizerWithAffiliationsFirst {

	static final boolean relatedAuthors = false;
	static final boolean searchOnlyInVicinityForCoAuthors = true;

	static final boolean printConsoleOutput = true;
	
	static int NLPid=0;
	static String NLPidPrefix="";
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		long begTime = System.currentTimeMillis();

		String inputFile=
				//"Cardiology_export_Co_Author_withunderscore_NLP_sorted.txt"
				//"ImpactMeme_Impactmeme Final Part II_v2_NLP_sorted.txt"
				"NLP Cardiology Pubmed with Affiliation Input_v1_sorted.txt"
				;

		String inputDir="../NLP Process/KOLs/Input/";
		//comment the below if you want to use the input file name specified above
		/*if(args.length>0){
			inputDir=args[0];
		}
		if(args.length>2)
			NLPidPrefix=args[2];
		if(args.length>1){
			inputFile=args[1];
		}*/
		if(args.length>0){
			try {
				args = Flags.parseCommandLineFlags(args);
			} catch (IllegalArgumentException e) {
				//usage();
				throw e;
			}
			inputDir=Flags.inputdir;
			inputFile=Flags.inputfile;
			NLPidPrefix=Flags.nlpidprefix;
		}
		else{
			File f = new File(inputDir);
			String[] ls = f.list();
			for (String fileName : ls) {
				if(fileName.endsWith("_WAffssorted.txt"))//this first needs to be sorted first and affiliations added
					inputFile=fileName;
			}
		}
		//

		int c=0;
	
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(
				inputDir +
				inputFile
				), "UTF-8"));
		//br.readLine(); //header 
		String formalExtractedOutputFile="../NLP Process/KOLs/Output/PROFILE_MAP_"+
				inputFile.substring(0, inputFile.lastIndexOf(".txt"))+//"_"+MyDateFormatter.getDate()+
				".txt";

		PrintWriter formalExtractedPwr = new PrintWriter(formalExtractedOutputFile, "UTF-8");



		String normOutputFile="../NLP Process/KOLs/Output/PROFILE_NORMALIZED_"+
				inputFile.substring(0, inputFile.lastIndexOf(".txt"))+//"_"+MyDateFormatter.getDate()+
				".txt";
		PrintWriter normPwr = new PrintWriter(normOutputFile, "UTF-8");

		/*Scanner sc = new Scanner(new File("tmp/test.txt"));
		HashSet<String> presentationIdsLimited=new HashSet<String>();
		while(sc.hasNextLine()) presentationIdsLimited.add(sc.nextLine());
		sc.close();*/

		//ArrayList<AuthorVector> authorVectors = new ArrayList<AuthorVector>();

		DetailedNLPer nlp = new DetailedNLPer();
		AllAffiliations allAffs = new AllAffiliations(nlp);

		Dictionaries dict=new Dictionaries();


		ArrayList<ArrayList<AuthorVector>> authorClusters = new ArrayList<ArrayList<AuthorVector>>();
		String clusterLastName="";
		while(br.ready()){
			String row = br.readLine();
			String[] splits = row.split("\\|",-1);
			String lngPresentationID = splits[0];// if(!presentationIdsLimited.contains(lngPresentationID)) continue;
			
			/*if(lngPresentationID.equals("8034836"))
				System.err.println(splits);*/
			
			if(!lngPresentationID.matches("\\d+")) continue;//contains header
			int intAuthorSeq = splits[1].length()==0?1:Integer.parseInt(splits[1]);//blank in author's sequence means 1 as per Gopal

			String txtFirstname = splits[4];
			String txtLastName = splits[6].trim();//example row - 10161682|1|2012|Insigtmeme|M.|| Kryger|Pulmonology and Critical Care Medicine, Yale School of Medical|New Haven, CT||||Strohl_K.:Maurer_J.:Woodson_BT.:De Backer_W.

			/*if(!txtLastName.equalsIgnoreCase("von K?nel")
					&&!txtLastName.equalsIgnoreCase("Volpe")
					&& authorClusters.size()==0) 
				continue;*/

			String mainOrg=splits.length>13?splits[13]:"";
			String mainCity=splits.length>14?splits[14]:"";
			String mainRegion=splits.length>15?splits[15]:"";
			String mainCountry=splits.length>13?splits[16]:"";
			String mainOrgId=splits.length>13?splits[17]:"-1";
			Affiliation aff = new Affiliation(mainOrg, mainCity, mainRegion, mainCountry, mainOrgId);
			/*String txtAffiliation = ((splits.length>7?splits[7]:"")+", "+ (splits.length>8?splits[8]:"")+", "+(splits.length>9?splits[9]:"")+", "+(splits.length>10?splits[10]:"")).replaceAll("\"", "");
			txtAffiliation=txtAffiliation.replaceAll(", ,",  ",").trim();
			if(txtAffiliation.endsWith(",")) txtAffiliation=txtAffiliation.substring(0, txtAffiliation.length()-1);

			Affiliation aff = new Affiliation(txtAffiliation,dict);
			aff.findMainOrgDictionaryEntry(allAffs.normMainOrgRowTable,allAffs, allAffs.nlp);
			aff.findOtherOrgDictionaryEntry(allAffs,allAffs.nlp);
			aff.findInferredMainOrg(allAffs.normInferredOrgRowTable,allAffs.nlp);*/			

			c++;
			if(c%10000==0) 
				System.out.println("read lines: "+c);

			
			AuthorVector authorVector = new AuthorVector(txtFirstname, txtLastName, intAuthorSeq, aff, lngPresentationID, row);
			//authorVectors.add(authorVector);
			if(splits.length>12){//providing flexibility for coauthors to not be mentioned
				for (String coAuth : splits[12].split(":")) {
					coAuth=(coAuth+" ").substring(0, coAuth.indexOf("_")+2);
					authorVector.coauthorLastNameFirstInitials.add(StaticStringOps.deAccent(coAuth).toUpperCase());
				}
			}

			//System.out.println("vector: "+authorVector.row);

			if(authorVector.authorLastName.equalsIgnoreCase(clusterLastName)){
				numberOfInputs++;
				boolean added=false;
				for (ArrayList<AuthorVector> authorCluster : authorClusters) {
					boolean found=true;
					for (AuthorVector authorVector2 : authorCluster) {
						boolean sim = isSameAuthorname(authorVector,authorVector2);//all author names in the cluster should be consistent
						if(!sim) {
							found=false; break;
						}
					}
					if(found){
						found=false;
						for (AuthorVector authorVector2 : authorCluster) {
							boolean sim = (authorVector.aff.idEquals(authorVector2.aff, nlp)//authorVector.aff.equals(authorVector2.aff, nlp)//other -older- option is equals
									|| isSimCollabs(authorVector,authorVector2)
									);
							if(sim) {//at least one affiliation should be consistent or have similar collaborations
								/*if(authorVector.row.contains("New York Hospital Queens") && !authorVector2.row.contains("New York Hospital Queens") && authorVector.row.contains("Medmeme|D.|"))
									System.err.println(authorVector.row+"\ntest\n"+authorVector2.row);*/
								found=true;
								//System.out.println("merging with the vector: "+authorVector2.row);
								break;
								
							}
						}
					}
					if(found){
						authorCluster.add(authorVector);added=true;

						//break;//no break statement here means something can fall in multiple clusters
					}
				}

				if(!added) {
					//System.out.println("\t\tnew cluster");
					ArrayList<AuthorVector> cluster = new ArrayList<AuthorVector>();
					cluster.add(authorVector);authorClusters.add(cluster);
				}
			}
			else{
				mergeAndPrint(authorClusters, formalExtractedPwr, normPwr, dict);
				//reset
				authorClusters = new ArrayList<ArrayList<AuthorVector>>();
				ArrayList<AuthorVector> cluster = new ArrayList<AuthorVector>();
				cluster.add(authorVector);authorClusters.add(cluster);
				clusterLastName=authorVector.authorLastName;
				numberOfInputs=1;
			}
		}

		br.close();
		//there can be something left
		mergeAndPrint(authorClusters, formalExtractedPwr, normPwr, dict);
		formalExtractedPwr.close();
		normPwr.close();
		
		System.out.println("Total time taken in ms: "+(System.currentTimeMillis()-begTime));

	}

	private static int numberOfInputs=0;

	private static void mergeAndPrint(
			ArrayList<ArrayList<AuthorVector>> authorClusters, PrintWriter formalExtractedPwr, PrintWriter normPwr, Dictionaries dict) {
		Collections.sort(authorClusters, new Comparator<ArrayList<?>>() {
			public int compare(ArrayList<?> arg0, ArrayList<?> arg1) {		
				return arg1.size()-arg0.size();
			}
		});

		authorClusters=mergeAuthorClusters(authorClusters);

		Collections.sort(authorClusters, new Comparator<ArrayList<?>>() {
			public int compare(ArrayList<?> arg0, ArrayList<?> arg1) {		
				return arg1.size()-arg0.size();
			}
		});
		authorClusters=removeDuplicates(authorClusters);

		int numberOfOutputs=0;
		//printing map
		/*--moving this to inside norm
		 * for (ArrayList<AuthorVector> authorCluster : authorClusters) {
			NLPid++;
			HashSet<String> names=new HashSet<String>();
			HashSet<String> affiliations=new HashSet<String>();
			for (AuthorVector av : authorCluster) {
				names.add(av.authorLastName+", "+av.authorFirstname);
				affiliations.add(av.aff.mention);
				formalExtractedPwr.println(av.row+"|"+NLPid+"|"+av.aff.masterMainOrganizationId);
				numberOfOutputs++;
			}
			System.out.print(StringUtils.join(names,"|||")+" ["+StringUtils.join(affiliations,"|||") +"] :"+authorCluster.size());
			System.out.println();
		}
		if(numberOfOutputs!=numberOfInputs)
			System.err.println(numberOfOutputs+"\t"+numberOfInputs);*/

		//printing norm
		for (int count=0; count<authorClusters.size(); count++) {
			ArrayList<AuthorVector> authorCluster = authorClusters.get(count);
			//HashMap<String, Affiliation> affs=new HashMap<String, Affiliation>();
			ArrayList<String> affMasterOrg=new ArrayList<String>();
			ArrayList<String> cities=new ArrayList<String>();
			ArrayList<String> regions=new ArrayList<String>();
			ArrayList<String> countries=new ArrayList<String>();
			ArrayList<String> lastNames=new ArrayList<String>();
			String longestFirstName="";

			for (AuthorVector av : authorCluster) {
				lastNames.add(av.authorLastName);
				if(av.authorFirstname.length()>longestFirstName.length())
					longestFirstName=av.authorFirstname;
				if(av.aff.masterMainOrganization==null)
					av.aff.masterMainOrganization=av.aff.mainInstitution;
				//affs.put(av.aff.masterMainOrganization,av.aff);
				affMasterOrg.add(av.aff.masterMainOrganization);
			}

			String mostCommonLastName = WordFrequencyCounter.mostCommon(lastNames);
			ArrayList<String> selectedMainOrganizations = new ArrayList<String>();
			for (String org : affMasterOrg) {
				String orgU=org.toUpperCase();
				boolean found =false;
				if(//org.contains("DEPARTMENT") || org.contains("DIVISION") || //-USDA
						orgU.endsWith("BUILDING") || orgU.endsWith("CAMPUS")) continue;
				for (String tok : orgU.split("\\s+|-|'")) {
					if(dict.universityKeywords.contains(tok) || tok.contains("UNIVERSIT")
							|| dict.level2OrgKeywords.contains(tok) || tok.contains("HOSPIT")
							|| orgU.contains("HEALTH SYSTEM")||orgU.contains("COLLEGE OF MEDICINE")){
						found=true;
						continue;
					}
				}
				if(found)
					selectedMainOrganizations.add(org);
			}
			if(selectedMainOrganizations.size()==0)
				selectedMainOrganizations=affMasterOrg;
			String mostCommonAffMainOrganization = WordFrequencyCounter.mostCommon(selectedMainOrganizations);
			
			for (AuthorVector av : authorCluster) {
				if(av.aff.masterMainOrganization.equals(mostCommonAffMainOrganization)){
					if(av.aff.city!=null)
						cities.add(av.aff.city);
					if(av.aff.region!=null)
						regions.add(av.aff.region);
					if(av.aff.country!=null)
						countries.add(av.aff.country);
				}
				
			}
			//Affiliation mostCommonAff = affs.get(mostCommonAffMainOrganization);
			String mostCommonCity=WordFrequencyCounter.mostCommon(cities);
			String mostCommonRegion=WordFrequencyCounter.mostCommon(regions);
			String mostCommonCountry=WordFrequencyCounter.mostCommon(countries);

			int nlpId;
			NLPid++;
			nlpId = NLPid;//+count+1-authorClusters.size();
			HashSet<String> names=new HashSet<String>();
			HashSet<String> affiliations=new HashSet<String>();
			for (AuthorVector av : authorCluster) {
				names.add(av.authorLastName+", "+av.authorFirstname);
				affiliations.add(av.aff.masterMainOrganization+"_"+av.aff.city);
				double confidence = Confidence.getAuthorConfidence(av,longestFirstName,mostCommonLastName,mostCommonAffMainOrganization,mostCommonCity,mostCommonRegion,mostCommonCountry);
				formalExtractedPwr.println(av.row+"|"+NLPidPrefix+NLPid
						+"|"+av.aff.masterMainOrganizationId
						+"|"+confidence);
				numberOfOutputs++;
			}
			if(ScalableAllAuthorNormalizerWithAffiliationsFirst.printConsoleOutput){
				System.out.print(StringUtils.join(names,"|||")+" ["+StringUtils.join(affiliations,"|||") +"] :"+authorCluster.size());
				System.out.println();
			}
			

			normPwr.println(NLPidPrefix+nlpId+"|"+longestFirstName+" "+mostCommonLastName+"|"+longestFirstName+"| |"+mostCommonLastName+"|"+mostCommonAffMainOrganization+"|"+mostCommonCity+"|"+mostCommonRegion+"|"+mostCommonCountry+"| |");


		}
		if(numberOfOutputs!=numberOfInputs)
			System.err.println(numberOfOutputs+"\t"+numberOfInputs);
	}

	public static ArrayList<ArrayList<AuthorVector>> removeDuplicates(
			ArrayList<ArrayList<AuthorVector>> clusters) {
		HashSet<String> existingRows = new HashSet<String>();
		for (int count=0; count<clusters.size(); count++) {
			ArrayList<AuthorVector> aCluster = clusters.get(count); if(aCluster==null) continue;
			HashSet<AuthorVector> duplicateRows = new HashSet<AuthorVector>();
			for (AuthorVector authorVector : aCluster) {
				if(existingRows.contains(authorVector.row))
					duplicateRows.add(authorVector);
				else
					existingRows.add(authorVector.row);
			}
			aCluster.removeAll(duplicateRows);
		}
		return clusters;
	}

	public static ArrayList<ArrayList<AuthorVector>> mergeAuthorClusters(
			ArrayList<ArrayList<AuthorVector>> clusters) {

		for (int count=0; count<clusters.size(); count++) {
			if(count>=1000&&count%1000==0) System.out.println("finished processing "+count+" cluster out of "+clusters.size());

			ArrayList<AuthorVector> aCluster = clusters.get(count); if(aCluster==null) continue;

			for (int count1 = count + 1; count1 < clusters.size(); count1++) {
				ArrayList<AuthorVector> bCluster = clusters.get(count1); if(bCluster==null) continue;
				ArrayList<AuthorVector> intersection = new ArrayList<AuthorVector>(bCluster);intersection.retainAll(aCluster);
				if(intersection.size()==0) 
					continue;//only focus on clusters with an intersection

				ArrayList<AuthorVector> minus = new ArrayList<AuthorVector>(bCluster);minus.removeAll(aCluster);
				boolean perfectMatch = true;
				for (AuthorVector extraVector : minus) {
					if(extraVector.row.contains("Duke University"))
						System.err.println("check");
					for (AuthorVector a : aCluster) {
						boolean sim = isSameAuthorname(a,extraVector);//all author names in both the clusters should be consistent
						if(!sim) {
							perfectMatch=false; break;
						}
					}
					if(!perfectMatch) break;
				}
				//TODO:I think it is not needed to check for organization names since there is already one common element

				if(perfectMatch){
					aCluster.addAll(minus);
					clusters.set(count1, null);
				}

				else
					//TODO: for now giving priority to first cluster. in the future need to break on more similarity
					clusters.set(count1, minus);
			}


		}
		clusters.removeAll(Collections.singleton(null));

		return clusters;
	}

	/*	static boolean isSameAuthorname_SameAffiliationOrSimCollabs(
			AuthorVector v1, AuthorVector v2) {
		if(v1.presentationId.equals(v2.presentationId)) {
			if(v1.authorSeq!=v2.authorSeq)
				return false;
			else
				return true;//same author, multiple affiliations
		}

		return 
				isSameAuthorname(v1, v2)
				&& 
				(v1.aff.strictEquals(v2.aff)//other -older- option is equals
						|| isSimCollabs(v1,v2)
						);
	}*/

	public static boolean isSameAuthorname(
			AuthorVector v1, AuthorVector v2) {
		if(v1.presentationId.equals(v2.presentationId)) {
			if(v1.authorSeq!=v2.authorSeq)
				return false;
			else
				return true;//same author, multiple affiliations
		}

		return 
				v1.authorLastName.equalsIgnoreCase(v2.authorLastName)
				&& isSameFirstAuthorName(v1.authorFirstname, v2.authorFirstname)
				;
	}

	public static boolean isSimCollabs(AuthorVector v1, AuthorVector v2) {
		//must have at least one or two?? coauthor in both papers
		//TODO parameterize the number of coauthors
		if(v1.coauthorLastNameFirstInitials.size()<2||v2.coauthorLastNameFirstInitials.size()<2) return false;

		ArrayList<String> tmp=new ArrayList<String>(v1.coauthorLastNameFirstInitials);
		tmp.retainAll(v2.coauthorLastNameFirstInitials);
		double score=tmp.size()*1.0/Math.min(v1.coauthorLastNameFirstInitials.size(), v2.coauthorLastNameFirstInitials.size());
		if(score>=0.6||tmp.size()>=3)
			//TODO parameterize 0.6
			//tmp.size() because of these two examples-
			//8344929|1|2012|Medmeme|G.||Miltenberger-Miltenyi|Instituto de Medicina Molecular, Faculdade de Medicina, Universidade de Lisboa|Lisbon||Portugal||Calado_J.:Carvalho_M.:Viana_H.:Pereira_S. V.:Teixeira_C.:Jorge_S.:Brinca_A.:Ars_E.:Almeida_E.
			//9902508|1|2013|Medmeme|Gabriel||Miltenberger-Miltenyi||Lisbon||Portugal||Carvalho_F.:Viana_H.:Santos_A.R.:Ferreira_C.:Ejarque_L.:Ars_E.:Almeida_E.:Calado_J.
			return true;
		return false;
	}

	static boolean strictFirstAuthors=true;
	public static boolean isSameFirstAuthorName(String name1,
			String name2) {
		/*if(name1.equals("D. T.") && name2.equals("Daniel"))
			System.err.println("check");
		else if(name2.equals("Daniel") && name1.equals("D. T."))
			System.err.println("check");*/
		
		if(name1==null|name2==null) return false;

		if(name1.equalsIgnoreCase(name2)) 
			return true;

		if(name1.length()==0 || name2.length()==0) return false;
		String name1Init=getInitials(name1);String name2Init=getInitials(name2);
		//works if one form is initials and the other is full
		//Doesn't capture Daniel and D. T.
		/*if(name1.replaceAll("[\\. ]+", "").equals(name2Init) ||
				name2.replaceAll("[\\. ]+", "").equals(name1Init))
			return true;*/
		if((name1.matches("[\\. A-Z]+") && name1Init.startsWith(name2Init)) ||
				(name2.matches("[\\. A-Z]+") && name2Init.startsWith(name1Init)))
			return true;

		//Esther N and Esther Natalie 
		//Doesn't work for Daniel and Daniela
		/*if(name1Init.equals(name2Init) && (name1.startsWith(name2) || name2.startsWith(name1)))
			return true;*/
		if(name1Init.equals(name2Init) && name1Init.length()>1 && (name1.startsWith(name2) || name2.startsWith(name1)))
			return true;

		//Anne K H and Anne Kari Hersvik
		if(name1Init.equals(name2Init) && name1Init.length()>1 ){
			String lowerCases1=name1.replaceAll("[^a-z]", "");
			String lowerCases2=name2.replaceAll("[^a-z]", "");
			if(lowerCases1.startsWith(lowerCases2) || lowerCases2.startsWith(lowerCases1))
				return true;
		}

		//Aarts, M -A [UNIVERSITY OF TORONTO, TORONTO, ON, CANADA] :1
		//Aarts, Mary-Anne W [UNIVERSITY OF TORONTO, TORONTO, ON, CANADA] :1
		if((name1Init.startsWith(name2Init) || name2Init.startsWith(name1Init)) && name1Init.length()>1 && name2Init.length()>1 ){
			String lowerCases1=name1.replaceAll("[^a-z]", "");
			String lowerCases2=name2.replaceAll("[^a-z]", "");
			if(lowerCases1.startsWith(lowerCases2) || lowerCases2.startsWith(lowerCases1))
				return true;
		}

		//David S and David
		if((name1Init.startsWith(name2Init) || name2Init.startsWith(name1Init)) && (name1.startsWith(name2+" ") || name2.startsWith(name1+" ")))
			return true;

		//ANTONIO and A. or ZAIGHAM and Z
		if(name2.matches("[A-Z]\\.?") && name1.startsWith(name2Init)) 
			return true;
		if(name1.matches("[A-Z]\\.?") && name2.startsWith(name1Init)) 
			return true;

		//Ehab and E.A.
		//this is turning out to be a bad rule: D.C. and David T.
		/*if(name2.matches("[A-Z]\\.[A-Z]\\.?") && name1.startsWith(name2.substring(0, 1))) 
			return true;
		if(name1.matches("[A-Z]\\.[A-Z]\\.?") && name2.startsWith(name1.substring(0, 1))) 
			return true;*/
		//better to do it like this
		if(name2.matches("[A-Z]\\.[A-Z]\\.?") && name1.matches("[A-Za-z]+") && name1.startsWith(name2.substring(0, 1))) 
			return true;
		if(name1.matches("[A-Z]\\.[A-Z]\\.?") && name2.matches("[A-Za-z]+") && name2.startsWith(name1.substring(0, 1))) 
			return true;

		return !strictFirstAuthors && (name1Init.startsWith(name2Init) || name2Init.startsWith(name1Init));

	}

	
	public static String getInitials(String name) {
		String ret="";
		//DANIEL C.'s initial should be DC
		if(name.matches("[A-Z][A-Z]+ [A-Z][A-Z\\. ]+")){
			String[] splits=name.split("\\s+");
			for (String split : splits) {
				ret+=split.charAt(0);
			}
			return ret;
		}
		for (int i=0; i<name.length(); i++) {
			if(Character.isUpperCase(name.charAt(i)))
				ret+=name.charAt(i);
		}
		if(!ret.equals("")) return ret;
		return name;
	}



}
