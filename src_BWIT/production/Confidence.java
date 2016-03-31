package production;

import neobio.alignment.BasicScoringScheme;
import neobio.alignment.IncompatibleScoringSchemeException;
import neobio.alignment.PairwiseAlignmentAlgorithm;
import neobio.alignment.SmithWaterman;
import parsing.AuthorVector;

public class Confidence {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public static double getAuthorConfidence(AuthorVector av,
			String normFirstName, String normLastName,
			String normAffMainOrganization, String normCity,
			String normRegion, String normCountry) {
		double ret=1;
		ret*=similarityFirstName(av.authorFirstname,normFirstName);
		ret*=similarityLastName(av.authorLastName,normLastName);
		ret*=similarityWaterman(av.aff.masterMainOrganization,normAffMainOrganization);
		ret*=Math.sqrt(similarityWaterman(av.aff.city,normCity));
		ret*=Math.sqrt(similarityWaterman(av.aff.region,normRegion));
		ret*=similarityWaterman(av.aff.country,normCountry);

		return Math.round(ret*100)*1.0/100;
	}

	private static double similarityWaterman(String str1,
			String str2) {
		if(str1!=null && str2!=null){
			str1=str1.toUpperCase();str2=str2.toUpperCase();
			
			PairwiseAlignmentAlgorithm	algorithm = new SmithWaterman();
			algorithm.setScoringScheme(new BasicScoringScheme (1, -1, -1));
			algorithm.loadSequenceStrings(str1, str2);
			//double score2 = NeoBio.getScore(org, org2);
			try {
				double score=algorithm.getScore()*1.0/Math.min(str1.length(), str2.length());
				if(score>0.8) 
					return score;
			} catch (IncompatibleScoringSchemeException e) {
				e.printStackTrace();
			}
		}

		return 0.8;
	}

	private static double similarityLastName(String str1,
			String str2) {
		if(str1.equalsIgnoreCase(str2))
			return 1;
		return 0.8;
	}

	private static double similarityFirstName(String str1,
			String str2) {
		if(str1.equalsIgnoreCase(str2))
			return 1;
		if(str2.startsWith(str1.replaceAll("\\.", "")))
			return 0.9;
		return 0.8;
	}

}
