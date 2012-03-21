package org.cocktail.cannelle.serveur.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;

public class Administrer extends MyWOComponent {
	private Boolean isOpenFenetreException = Boolean.FALSE;
	public WODisplayGroup rapportsDg = new WODisplayGroup();

	public Administrer(WOContext context) {
		super(context);
	}

	/**
	 * @return the isOpenFenetreException
	 */
	public Boolean isOpenFenetreException() {
		return isOpenFenetreException;
	}

	/**
	 * @param isOpenFenetreException the isOpenFenetreException to set
	 */
	public void setIsOpenFenetreException(Boolean isOpenFenetreException) {
		this.isOpenFenetreException = isOpenFenetreException;
	}

	public String getFormId() {
		return "adminForm";
	}
}
