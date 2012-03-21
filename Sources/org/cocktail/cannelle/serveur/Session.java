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

package org.cocktail.cannelle.serveur;

import org.cocktail.fwkcktlajaxwebext.serveur.CocktailAjaxSession;
import org.cocktail.fwkcktlwebapp.common.util.StringCtrl;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.eoaccess.EODatabaseChannel;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;

public class Session extends CocktailAjaxSession {

	private static final long serialVersionUID = 1L;
	private static final int DatabaseChannelCountMax = 3;

	private NSDictionary exceptionInfos;
	private String messageErreur;

	private CannelleApplicationUser applicationUser = null;

	public Session() {
		super();
		NSNotificationCenter.defaultCenter().addObserver(this,
					new NSSelector("registerNewDatabaseChannel", new Class[] {
							NSNotification.class
					}),
					EODatabaseContext.DatabaseChannelNeededNotification, null);
		// Initialisation du theme applique a toutes les fenetres gerees via CktlAjaxModalDialog
		// setWindowsClassName(CktlAjaxModalDialog.WINDOWS_CLASS_NAME_BLUELIGHTING);
	}

	public void registerNewDatabaseChannel(NSNotification notification) {
		EODatabaseContext databaseContext = (EODatabaseContext) notification.object();
		if (databaseContext.registeredChannels().count() < DatabaseChannelCountMax) {
			EODatabaseChannel channel = new EODatabaseChannel(databaseContext);
			databaseContext.registerChannel(channel);
		}
	}

	public CannelleApplicationUser applicationUser() {
		return applicationUser;
	}

	public void setApplicationUser(CannelleApplicationUser appUser) {
		this.applicationUser = appUser;
	}

	public WOActionResults onQuitter() {
		return logout();
	}

	public void reset() {
		if (defaultEditingContext() != null) {
			defaultEditingContext().revert();
		}
		exceptionInfos = null;
		messageErreur = null;
	}

	public NSDictionary exceptionInfos() {
		return null;
	}

	/**
	 * @param exceptionInfos the exceptionInfos to set
	 */
	public void setExceptionInfos(NSDictionary exceptionInfos) {
		this.exceptionInfos = exceptionInfos;
	}

	/**
	 * @return the messageErreur
	 */
	public String messageErreur() {
		if (messageErreur != null) {
			messageErreur = StringCtrl.trimText(messageErreur);
		}
		return messageErreur;
	}

	/**
	 * @param messageErreur the messageErreur to set
	 */
	public void setMessageErreur(String messageErreur) {
		this.messageErreur = messageErreur;
	}

}
