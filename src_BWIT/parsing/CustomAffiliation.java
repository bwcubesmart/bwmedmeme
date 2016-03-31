package parsing;

import org.apache.commons.lang.WordUtils;

import normalization.AffiliationMainOrgNormalizer;

public class CustomAffiliation {

	public static void process(Affiliation aff) {
		if(aff.masterMainOrganization==null){
			if(aff.mainInstitution.equalsIgnoreCase("GASTROINTESTINAL RESEARCH UNIT UNIVERSITY MEDICAL CENTER")&&aff.city.equalsIgnoreCase("UTRECHT")){
				aff.masterMainOrganization="University Medical Center Utrecht";
			}
			if(aff.mainInstitution.equalsIgnoreCase("VETERANS AFFAIRS DENVER MEDICAL CENTER VISN 19 MIRECC"))
				aff.masterMainOrganization="Denver Veterans Affairs Medical Center";
		}
		
		if(aff.city==null && aff.region!=null && aff.region.equalsIgnoreCase("District Of Columbia"))
			aff.city="Washington";
		if(aff.country!=null && aff.country.equalsIgnoreCase("FRANCE") && aff.region!=null && aff.region.equalsIgnoreCase("BP"))
			aff.region=null;
		//if(aff.city==null) aff.city="";
		//if(aff.region==null) aff.region="";
		//if(aff.country==null) aff.country="";
		
		CustomAffiliation.properCases(aff);
	}

	static void properCases(Affiliation aff) {
		if(aff.masterMainOrganizationId>AffiliationMainOrgNormalizer.seed||aff.masterMainOrganizationId<0){
			aff.masterMainOrganization=casify(aff.masterMainOrganization,aff.origMention);
		}
		aff.mainInstitution=casify(aff.mainInstitution,aff.origMention);
		aff.city=casify(aff.city,aff.origMention);
		aff.region=casify(aff.region,aff.origMention);
		aff.country=casify(aff.country,aff.origMention);
		for (int i=0;i<aff.organizations.size();i++) {
			aff.organizations.set(i, casify(aff.organizations.get(i), aff.origMention));
		}
	}

	private static String casify(String item, String mention) {
		if(item==null) return item;
		
		String mentionU=mention.toUpperCase();
		boolean upperEqual=mentionU.length()==mention.length();
		String ret="";
		for (String tok : item.split("\\s+")) {
			if(mentionU.contains(tok)&&upperEqual)
				ret+=mention.substring(mentionU.indexOf(tok)).substring(0, tok.length()) + " ";
			else
				ret+=WordUtils.capitalizeFully(tok)+" ";
		}
		return ret.trim();
	}

}
