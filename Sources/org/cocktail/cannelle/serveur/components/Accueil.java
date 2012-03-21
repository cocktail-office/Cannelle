/*
 * Copyright COCKTAIL (www.cocktail.org), 1995, 2010 This software 
 * is governed by the CeCILL license under French law and abiding by the
 * rules of distribution of free software. You can use, modify and/or 
 * redistribute the software under the terms of the CeCILL license as 
 * circulated by CEA, CNRS and INRIA at the following URL 
 * "http://www.cecill.info". 
 * As a counterpart to the access to the source code and rights to copy, modify 
 * and redistribute granted by the license, users are provided only with a 
 * limited warranty and the software's author, the holder of the economic 
 * rights, and the successive licensors have only limited liability. In this 
 * respect, the user's attention is drawn to the risks associated with loading,
 * using, modifying and/or developing or reproducing the software by the user 
 * in light of its specific status of free software, that may mean that it
 * is complicated to manipulate, and that also therefore means that it is 
 * reserved for developers and experienced professionals having in-depth
 * computer knowledge. Users are therefore encouraged to load and test the 
 * software's suitability as regards their requirements in conditions enabling
 * the security of their systems and/or data to be ensured and, more generally, 
 * to use and operate it in the same conditions as regards security. The
 * fact that you are presently reading this means that you have had knowledge 
 * of the CeCILL license and that you accept its terms.
 */
package org.cocktail.cannelle.serveur.components;

import org.cocktail.cannelle.serveur.components.controlers.AccueilCtrl;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;

public class Accueil extends MyWOComponent {

	private AccueilCtrl ctrl;

	private Integer numero;

	private Boolean isOpenFenetreException = Boolean.FALSE;

	//	private Boolean refreshDataRapportList = Boolean.FALSE;
	public WODisplayGroup rapportsDg = new WODisplayGroup();
	public WODisplayGroup rapportsPretsDg = new WODisplayGroup();
	public Boolean refreshData;

	public Accueil(WOContext context) {
		super(context);
		ctrl = new AccueilCtrl(this);
		//setCtrl(ctrl);

		//	setRefreshDataRapportList(Boolean.TRUE);
	}

	public boolean isCreerDisabled() {
		boolean isCreerDisabled = false;
		// TODO Ajouter le code correspondant

		return isCreerDisabled;
	}

	public WOActionResults rechercheAvancee() {
		// TODO Ajouter le code pour renvoyer la page de recherche avancee
		return null;
	}

	/**
	 * @return the ctrl
	 */
	public AccueilCtrl ctrl() {
		return ctrl;
	}

	/**
	 * @param ctrl the ctrl to set
	 */
	public void setCtrl(AccueilCtrl ctrl) {
		this.ctrl = ctrl;
	}

	/**
	 * @return the numero
	 */
	public Integer numero() {
		return numero;
	}

	/**
	 * @param numero the numero to set
	 */
	public void setNumero(Integer numero) {
		this.numero = numero;
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

	public String containerCriteresId() {
		return getComponentId() + "_crits";
	}

}