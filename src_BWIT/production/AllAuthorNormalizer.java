package production;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;

import normalization.TopInstitutions;

import org.apache.commons.lang3.StringUtils;


import parsing.Affiliation;
import parsing.Author;
import parsing.AuthorVector;
import parsing.Dictionaries;
import utils.MyDateFormatter;
import utils.WordFrequencyCounter;


public class AllAuthorNormalizer {
	
	static final boolean relatedAuthors = false;
	static final boolean searchOnlyInVicinityForCoAuthors = true;
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		Scanner sc = new Scanner(new File("tmp/test.txt"));
		HashSet<String> presentationIdsLimited=new HashSet<String>();
		while(sc.hasNextLine()) presentationIdsLimited.add(sc.nextLine());
		sc.close();
		
		ArrayList<AuthorVector> authorVectors = new ArrayList<AuthorVector>();
		
		Dictionaries dict=new Dictionaries();
		
		int c=0;
		BufferedReader br = new BufferedReader(new FileReader("../NLP Process/KOLs/Input/KOL_Sample data_InsightMeme.txt"));
		br.readLine(); //header 
		while(br.ready()){
			String row = br.readLine();
			String[] splits = row.split("\\|",-1);
			String lngPresentationID = splits[0]; if(!presentationIdsLimited.contains(lngPresentationID)) continue;
			int intAuthorSeq = Integer.parseInt(splits[1]);
			
			String txtFirstname = splits[4];
			String txtLastName = splits[6].trim();//example row - 10161682|1|2012|Insigtmeme|M.|| Kryger|Pulmonology and Critical Care Medicine, Yale School of Medical|New Haven, CT||||Strohl_K.:Maurer_J.:Woodson_BT.:De Backer_W.
			
			if(splits.length<11
					//||txtFirstname.length()==0
					||txtLastName.length()==0){
				System.err.println("skipping row: "+row);
				continue;
			}
			
			String txtAffiliation = ((splits.length>7?splits[7]:"")+", "+ (splits.length>8?splits[8]:"")+", "+(splits.length>9?splits[9]:"")+", "+(splits.length>10?splits[10]:"")).replaceAll("\"", "");
			txtAffiliation=txtAffiliation.replaceAll(", ,",  ",").trim();
			if(txtAffiliation.endsWith(",")) txtAffiliation=txtAffiliation.substring(0, txtAffiliation.length()-1);
			
			Affiliation aff = new Affiliation(txtAffiliation,dict);
			c++;
			if(c%10000==0) 
				System.out.println("read lines: "+c);

			AuthorVector av = new AuthorVector(txtFirstname, txtLastName, intAuthorSeq, aff, lngPresentationID, row);
			authorVectors.add(av);
			
		}
		br.close();

		//add coauthors to author network
		for (int c1=0;c1<authorVectors.size();c1++) {
			if(c1%1000==0) 
				System.out.println("read vectors: "+c1);
			AuthorVector authorVector = authorVectors.get(c1);
			
			for (int c2=c1+1;c2<authorVectors.size();c2++) {
				AuthorVector authorVector2 = authorVectors.get(c2);
				if(!authorVector.presentationId.equals(authorVector2.presentationId))	break;
				if(authorVector.authorSeq==authorVector2.authorSeq) continue;

				//authorVector.coauthorFirstnames.add(authorVector2.authorFirstname);
				
				authorVector.coauthorLastNameFirstInitials.add(authorVector2.authorLastName+"_"+(authorVector2.authorFirstname+" ").substring(0,1));
				authorVector2.coauthorLastNameFirstInitials.add(authorVector.authorLastName+"_"+(authorVector.authorFirstname+" ").substring(0,1));
			}
		}
		
		
		ObjectOutput output = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream("authorVectors.ser")));
		output.writeObject(authorVectors);
		output.close();
		
		
		
		/*ObjectInput input = new ObjectInputStream (new BufferedInputStream(new FileInputStream("authorVectors.ser")));
		ArrayList<AuthorVector> authorVectors=(ArrayList<AuthorVector>) input.readObject();
		input.close();*/
		
		ArrayList<ArrayList<AuthorVector>> authorClusters = new ArrayList<ArrayList<AuthorVector>>();
		int c2=0;
		for (AuthorVector authorVector : authorVectors) {
			c2++;
			if(c2%1000==0) 
				System.out.println("processed vectors: "+c2);
			
			/*boolean found=false;
			
			for (ArrayList<AuthorVector> authorCluster : authorClusters) {
				for (AuthorVector authorVector2 : authorCluster) {
					boolean sim = isSameAuthorname_SameAffiliationOrSimCollabs(authorVector,authorVector2);
					if(sim){
						found=sim;authorCluster.add(authorVector); break;
					}
				}
			}
			
			if(!found) {
				ArrayList<AuthorVector> cluster = new ArrayList<AuthorVector>();
				cluster.add(authorVector);authorClusters.add(cluster);
			}*/
			
			boolean found=false;
			for (ArrayList<AuthorVector> authorCluster : authorClusters) {
				found=true;
				for (AuthorVector authorVector2 : authorCluster) {
					boolean sim = isSameAuthorname(authorVector,authorVector2);//all author names in the cluster should be consistent
					if(!sim) {found=false; break;}
				}
				if(found){
					found=false;
					for (AuthorVector authorVector2 : authorCluster) {
						boolean sim = (authorVector.aff.strictEquals(authorVector2.aff)//other -older- option is equals
										|| isSimCollabs(authorVector,authorVector2)
										);
						if(sim) {//at least one affiliation should be consistent or have similar collaborations
							found=true;
						}
					}
				}
				if(found){
					authorCluster.add(authorVector);
					break;//no break statement here means something can fall in multiple clusters
				}
			}
			
			if(!found) {
				ArrayList<AuthorVector> cluster = new ArrayList<AuthorVector>();
				cluster.add(authorVector);authorClusters.add(cluster);
			}
		}
		
		authorClusters=mergeAuthorClusters(authorClusters);
		
		Collections.sort(authorClusters, new Comparator<ArrayList>() {

			public int compare(ArrayList arg0, ArrayList arg1) {		
				return arg1.size()-arg0.size();
			}
		});
		
		String formalExtractedOutputFile="../NLP Process/KOLs/Output/PROFILE_MAP_"+
		MyDateFormatter.getDate()+
		".txt";
		PrintWriter formalExtractedPwr = new PrintWriter(formalExtractedOutputFile);
		
		int NLPid=0;
		for (ArrayList<AuthorVector> authorCluster : authorClusters) {
			NLPid++;
			HashSet<String> names=new HashSet<String>();
			HashSet<String> affilaitions=new HashSet<String>();
			for (AuthorVector authorVector : authorCluster) {
				names.add(authorVector.authorLastName+", "+authorVector.authorFirstname);
				affilaitions.add(authorVector.aff.mention);
				formalExtractedPwr.println(authorVector.row+"|"+NLPid);
			}
			System.out.println(StringUtils.join(names,"|||")+" ["+StringUtils.join(affilaitions,"|||") +"] :"+authorCluster.size());
			
		}
		formalExtractedPwr.close();
		
		String normOutputFile="../NLP Process/KOLs/Output/PROFILE_NORMALIZED_"+
		MyDateFormatter.getDate()+
		".txt";
		PrintWriter normPwr = new PrintWriter(normOutputFile);
		
		for (int count=0; count<authorClusters.size(); count++) {
			ArrayList<AuthorVector> authorCluster = authorClusters.get(count);
			HashMap<String, Affiliation> affs=new HashMap<String, Affiliation>();
			ArrayList<String> affMentions=new ArrayList<String>();
			ArrayList<String> lastNames=new ArrayList<String>();
			String longestFirstName="";
			
			for (AuthorVector authorVector : authorCluster) {
				lastNames.add(authorVector.authorLastName);
				if(authorVector.authorFirstname.length()>longestFirstName.length())
					longestFirstName=authorVector.authorFirstname;
				affs.put(authorVector.aff.mention,authorVector.aff);
				affMentions.add(authorVector.aff.mention);
			}
			
			String mostCommonLastName = WordFrequencyCounter.mostCommon(lastNames);
			String mostCommonAffMention = WordFrequencyCounter.mostCommon(affMentions);
			Affiliation mostCommonAff = affs.get(mostCommonAffMention);
			
			int nlpId = count+1;
			normPwr.println(nlpId+"|"+longestFirstName+" "+mostCommonLastName+"|"+longestFirstName+"| |"+mostCommonLastName+"|"+mostCommonAff.mainInstitution+"|"+mostCommonAff.city+"|"+mostCommonAff.region+"|"+mostCommonAff.country+"| |");

		}
		
		normPwr.close();
		
	}

	private static ArrayList<ArrayList<AuthorVector>> mergeAuthorClusters(
			ArrayList<ArrayList<AuthorVector>> clusters) {
		for (int count=0; count<clusters.size(); count++) {
			if(count%1000==0) System.out.println("finished processing "+count+" cluster out of "+clusters.size());
			
			ArrayList<AuthorVector> aCluster = clusters.get(count); if(aCluster==null) continue;

			for (int count1 = count + 1; count1 < clusters.size(); count1++) {
				ArrayList<AuthorVector> bCluster = clusters.get(count1); if(bCluster==null) continue;
				//ArrayList<AuthorVector> intersection = new ArrayList<AuthorVector>(bCluster);intersection.retainAll(aCluster);
				ArrayList<AuthorVector> minus = new ArrayList<AuthorVector>(bCluster);bCluster.removeAll(aCluster);
				boolean perfectMatch = true;
				for (AuthorVector extraVector : minus) {
					for (AuthorVector a : aCluster) {
						boolean sim = isSameAuthorname(a,extraVector);//all author names in both the clusters should be consistent
						if(!sim) {perfectMatch=false; break;}
					}
					if(!perfectMatch) break;
				}
				//TODO:I think it is not needed to check for organization names since there is already one common element
				
				if(perfectMatch){
					aCluster.addAll(minus);
					clusters.set(count1, null);
				}
				
				else
				//TODO: for now giving priority ot first cluster. in the future need to break on more similarity
					clusters.set(count1, minus);
			}
			
			
		}
		clusters.removeAll(Collections.singleton(null));

		return clusters;
	}

	static boolean isSameAuthorname_SameAffiliationOrSimCollabs(
			AuthorVector v1, AuthorVector v2) {
		if(v1.presentationId.equals(v2.presentationId)) {
			if(v1.authorSeq!=v2.authorSeq)
				return false;
			else
				return true;//same author, multiple affiliations
		}
		
		return 
				v1.authorLastName.equals(v2.authorLastName)
				&& isSameFirstAuthorName(v1.authorFirstname, v2.authorFirstname)
				&& 
				(v1.aff.strictEquals(v2.aff)//other -older- option is equals
				|| isSimCollabs(v1,v2)
				);
	}

	static boolean isSameAuthorname(
			AuthorVector v1, AuthorVector v2) {
		if(v1.presentationId.equals(v2.presentationId)) {
			if(v1.authorSeq!=v2.authorSeq)
				return false;
			else
				return true;//same author, multiple affiliations
		}
		
		return 
				v1.authorLastName.equals(v2.authorLastName)
				&& isSameFirstAuthorName(v1.authorFirstname, v2.authorFirstname)
				;
	}
	
	 static boolean isSimCollabs(AuthorVector v1, AuthorVector v2) {
		//must have at least one coauthor in both papers
		//TODO parameterize 1
		if(v1.coauthorLastNameFirstInitials.size()<1||v2.coauthorLastNameFirstInitials.size()<1) return false;
		
		ArrayList<String> tmp=new ArrayList<String>(v1.coauthorLastNameFirstInitials);
		tmp.retainAll(v2.coauthorLastNameFirstInitials);
		if(tmp.size()*1.0/Math.min(v1.coauthorLastNameFirstInitials.size(), v2.coauthorLastNameFirstInitials.size())>0.8)//TODO parameterize 0.8
			return true;
		return false;
	}

	 static boolean isSameFirstAuthorName(String name1,
			String name2) {
		if(name1.equalsIgnoreCase(name2)) 
			return true;
		
		if(name1.length()==0 || name2.length()==0) return false;
		
		String initialPattern = "([A-Z]\\.?\\s*)+";
		if(name1.matches(initialPattern)||name2.matches(initialPattern)){
			String name1Init=getInitials(name1);String name2Init=getInitials(name2);
			return name1Init.startsWith(name2Init) || name2Init.startsWith(name1Init);
		}
		
		return false;
		/*too broad - making it strict by imposing strict equals
		 * if(name1Init.startsWith(name2Init)) 
			return true;
		if(name2Init.startsWith(name1Init)) 
			return true;
		if(name1Init.equals(name2)) 
			return true;
		if(name2Init.equals(name1)) 
			return true;*/
		
		
		/*
		 * same thing repeated above
		 * String initialPattern = "([A-Z]\\.?\\s*)+";
		if(name1.matches(initialPattern)||name2.matches(initialPattern)){
			String[] splits1 = name1.split("\\s+");
			String tmp1="";
			for (String split : splits1) {
				tmp1+=split.charAt(0);
			}
			String[] splits2 = name2.split("\\s+");
			String tmp2="";
			for (String split : splits2) {
				tmp2+=split.charAt(0);
			}
			if(
					//just for debugging - (name1.length()>8&&!name1.contains(".")||name2.length()>8&&!name2.contains("."))&&
					(tmp1.startsWith(tmp2)||tmp2.startsWith(tmp1)))
				return true;
		}*/
		
		
		
	}

	private static String getInitials(String name) {
		String ret="";
		for (int i=0; i<name.length(); i++) {
			if(Character.isUpperCase(name.charAt(i)))
				ret+=name.charAt(i);
		}
		if(!ret.equals("")) return ret;
		return name;
	}

	

}
