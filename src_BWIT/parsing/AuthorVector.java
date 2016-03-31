package parsing;

import java.io.Serializable;
import java.util.ArrayList;

import utils.StaticStringOps;

public class AuthorVector implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3212810327844719204L;
	public AuthorVector(String authorFirstname, String authorLastName,
			int authorSeq, Affiliation aff, String lngPresentationID) {
		this.authorFirstname = authorFirstname;
		this.authorLastName = authorLastName;
		this.authorSeq = authorSeq;
		this.aff = aff;
		this.presentationId = lngPresentationID;
		coauthorLastNameFirstInitials = new ArrayList<String>();
	}
	
	public AuthorVector(String txtFirstname, String txtLastName,
			int intAuthorSeq, Affiliation aff, String lngPresentationID,
			String row) {
		this(StaticStringOps.deAccent(txtFirstname), StaticStringOps.deAccent(txtLastName), intAuthorSeq, aff, lngPresentationID);
		this.row=row;		
	}

	public String row;
	public String authorFirstname;
	public String authorLastName;
	public int authorSeq;
	public Affiliation aff;
	public String presentationId;
	public ArrayList<String> coauthorLastNameFirstInitials;
}