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

import java.util.GregorianCalendar;

import org.cocktail.cannelle.serveur.Application;
import org.cocktail.cannelle.serveur.CannelleHelpers;
import org.cocktail.cannelle.serveur.VersionMe;
import org.cocktail.cannelle.serveur.components.controlers.WrapperCtrl;
import org.cocktail.fwkcktlwebapp.common.util.DateCtrl;
import org.cocktail.fwkcktlwebapp.common.util.StringCtrl;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

import er.ajax.AjaxValue;
import er.ajax.CktlAjaxUtils;

public class Wrapper extends MyWOComponent {

	private WrapperCtrl ctrl;

	private String onloadJS;
	private String erreurScript;

	public Wrapper(WOContext context) {
		super(context);
		ctrl = new WrapperCtrl(this);
	}

	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		super.appendToResponse(response, context);
		CannelleHelpers.insertStylesSheet(context, response);

		CktlAjaxUtils.addScriptResourceInHead(context, response, "prototype.js");
		CktlAjaxUtils.addScriptResourceInHead(context, response, "FwkCktlThemes.framework", "scripts/window.js");
		CktlAjaxUtils.addScriptResourceInHead(context, response, "app", "scripts/strings.js");
		CktlAjaxUtils.addScriptResourceInHead(context, response, "app", "scripts/formatteurs.js");
		CktlAjaxUtils.addScriptResourceInHead(context, response, "app", "scripts/cannelle.js");

		session.removeObjectForKey("MessageErreur");
	}

	public Erreur pageErreur() {
		Erreur nextPage = (Erreur) pageWithName(Erreur.class.getName());
		return nextPage;
	}

	/**
	 * @return the ctrl
	 */
	public WrapperCtrl ctrl() {
		return ctrl;
	}

	/**
	 * @param ctrl the ctrl to set
	 */
	public void setCtrl(WrapperCtrl ctrl) {
		this.ctrl = ctrl;
	}

	/**
	 * @return the onloadJS
	 */
	public String onloadJS() {
		return onloadJS;
	}

	/**
	 * @param onloadJS the onloadJS to set
	 */
	public void setOnloadJS(String onloadJS) {
		this.onloadJS = onloadJS;
	}

	/**
	 * @return the erreurScript
	 */
	public String erreurScript() {
		String messageErreur = session().messageErreur();
		if (!StringCtrl.isEmpty(messageErreur) && !messageErreur.startsWith("[CANNELLE]:Exception")) {
			if (messageErreur.indexOf("ORA-") > -1) {
				messageErreur = messageErreur.substring(messageErreur.indexOf("ORA-") + 10);
			}
			erreurScript = "alert(" + AjaxValue.javaScriptEscaped(messageErreur) + ");";
			session().setMessageErreur(null);
		}
		else {
			erreurScript = null;
		}

		return erreurScript;
	}

	/**
	 * @param erreurScript the erreurScript to set
	 */
	public void setErreurScript(String erreurScript) {
		this.erreurScript = erreurScript;
	}

	/**
	 * @return the titre
	 */
	public String titre() {
		return VersionMe.APPLICATIONFINALNAME;
	}

	public String copyright() {
		return "(c) " + DateCtrl.nowDay().get(GregorianCalendar.YEAR) + " Cocktail";
	}

	public String version() {
		return VersionMe.htmlAppliVersion();
	}

	public String serverId() {
		return Application.serverBDId();
	}

	public WOActionResults reinitialiserMessageErreur() {
		session.removeObjectForKey("MessageErreur");
		return null;
	}

}
