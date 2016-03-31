package production;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import normalization.TopInstitutions;

import opennlp.DetailedNLPer;

import org.apache.commons.lang3.StringUtils;

import parsing.Affiliation;
import utils.WordFrequencyCounter;

public class AllAffiliationsNormalizer {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws ClassNotFoundException, IOException {
		ObjectInput input = new ObjectInputStream (new BufferedInputStream(new FileInputStream("../NLP Process/Affiliation/Output/Affiliation_InsightMeme_New_100000random_out.txt.ser")));
		ArrayList<Affiliation> affs=(ArrayList<Affiliation>) input.readObject();
		input.close();
		DetailedNLPer nlp = new DetailedNLPer();
		
		PrintWriter pwr = new PrintWriter("../NLP Process/Affiliation/Output/Affiliation_InsightMeme_New_random_out_topInstituions.tsv", "UTF-8");
		
		ArrayList<ArrayList<Affiliation>> affClusters = new ArrayList<ArrayList<Affiliation>>();
		
		int MAX=20000;
		int c=0;
		for (Affiliation aff : affs) {
			boolean found=false;
			c++; if(c%1000==0) System.err.println("processed "+c+" orgs out of"+affs.size());if(c==MAX) break;
			for (ArrayList<Affiliation> affCluster : affClusters) {
				for (Affiliation aff2 : affCluster) {
					/*if(aff2.mention.equalsIgnoreCase(aff.mention) ||
				aff2.masterMainOrganizationId==aff.masterMainOrganizationId || aff2.mainInstituion.equals(aff.mainInstituion)
				|| aff2.organizations.contains(aff.mainInstituion)||aff.organizations.contains(aff2.mainInstituion)) continue;*/ //used only for testing norm match
					
					if(aff.exactEqualsOnlyMain(aff2, nlp)){
						found=true;affCluster.add(aff); break;
					}
				}
				//if(found) break;
			}

			if(!found) {
				ArrayList<Affiliation> cluster = new ArrayList<Affiliation>();
				cluster.add(aff);affClusters.add(cluster);
			}
		}
		
		affClusters=TopInstitutions.mergeClusters(affClusters);


		Collections.sort(affClusters, new Comparator<ArrayList>() {

			public int compare(ArrayList arg0, ArrayList arg1) {		
				return arg1.size()-arg0.size();
			}
		});
		//int maxAffs=0;

		System.out.println("Affiliation"+"\t"+"ID"+"\t"+"Country"+"\t"+"Cluster" +"\t"+"Frequency");
		pwr.println("Affiliation"+"\t"+"ID"+"\t"+"Country"+"\t"+"Cluster" +"\t"+"Frequency");

		for (ArrayList<Affiliation> affCluster : affClusters) {
			ArrayList<String> affilaitions=new ArrayList<String>();
			for (Affiliation aff : affCluster) {
				if(!affilaitions.contains(aff.mention)) affilaitions.add(aff.mention);
			}
			//maxAffs=Math.max(maxAffs, affilaitions.size());
			Affiliation mostAff = WordFrequencyCounter.mostCommon(affCluster);
			System.out.println(mostAff.mention+"\t"+mostAff.masterMainOrganizationId+"\t"+mostAff.country+"\t"+StringUtils.join(affilaitions,"|||") +"\t"+affCluster.size());
			
			pwr.println(mostAff.mention+"\t"+mostAff.masterMainOrganizationId+"\t"+mostAff.country+"\t"+StringUtils.join(affilaitions,"|||") +"\t"+affCluster.size());
		}
		
		pwr.close();
	}

}
