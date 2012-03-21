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

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.Enumeration;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import org.cocktail.cannelle.serveur.components.Accueil;
import org.cocktail.fwkcktlajaxwebext.serveur.CocktailAjaxApplication;
import org.cocktail.fwkcktlreport.server.CktlReportParameters;
import org.cocktail.fwkcktlwebapp.common.CktlLog;
import org.cocktail.fwkcktlwebapp.common.util.DateCtrl;
import org.cocktail.fwkcktlwebapp.server.CktlMailBus;
import org.cocktail.fwkcktlwebapp.server.util.EOModelCtrl;
import org.cocktail.fwkcktlwebapp.server.version.A_CktlVersion;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WORequestHandler;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver.WOSession;
import com.webobjects.eoaccess.EODatabaseChannel;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.eocontrol.EOObjectStoreCoordinator;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNumberFormatter;
import com.webobjects.foundation.NSPropertyListSerialization;
import com.webobjects.foundation.NSTimeZone;
import com.webobjects.foundation.NSTimestampFormatter;

import er.ajax.AjaxUtils;
import er.extensions.appserver.ERXApplication;
import er.extensions.foundation.ERXProperties;

public class Application extends CocktailAjaxApplication {

	public static final String TYPE_APP_STR = "CANNELLE"; // ID de l'application dans JefyAdmin
	private static final String CONFIG_FILE_NAME = VersionMe.APPLICATIONINTERNALNAME + ".config";
	private static final String CONFIG_TABLE_NAME = "FwkCktlWebApp_GrhumParametres";
	private static final String MAIN_MODEL_NAME = org.cocktail.fwkcktlreport.server.Version.APPLICATIONINTERNALNAME;

	/**
	 * Liste des parametres obligatoires (dans fichier de config ou table grhum_parametres) pour que l'application se lance. Si un des parametre n'est
	 * pas initialise, il y a une erreur bloquante.
	 */
	public static final NSArray<String> MANDATORY_PARAMS = new NSArray(new String[] {
			"GRHUM_HOST_MAIL", "ADMIN_MAIL", CktlReportParameters.CKTLREPORT_COCKTAIL_REPORTS_LOCATION
	});

	/**
	 * Liste des parametres optionnels (dans fichier de config ou table grhum_parametres). Si un des parametre n'est pas initialise, il y a un
	 * warning.
	 */
	public static final String[] OPTIONAL_PARAMS = new String[] {};

	/**
	 * Mettre a true pour que votre application renvoie les informations de collecte au serveur de collecte de Cocktail.
	 */
	public static boolean APP_SHOULD_SEND_COLLECTE = false;

	/**
	 * boolean qui indique si on se trouve en mode developpement ou non. Permet de desactiver l'envoi de mail lors d'une exception par exemple
	 */
	public static boolean isModeDebug = false;

	private Version _appVersion;

	public static NSTimeZone ntz = null;
	/**
	 * Formatteur a deux decimales e utiliser pour les donnees numeriques non monetaires.
	 */
	public NSNumberFormatter app2DecimalesFormatter;
	/**
	 * Formatteur a 5 decimales a utiliser pour les pourcentages dans la repartition.
	 */
	public NSNumberFormatter app5DecimalesFormatter;

	/**
	 * Formatteur de dates.
	 */
	public NSTimestampFormatter appDateFormatter;

	/**
	 * Liste des emails des utilisateurs connectes.
	 */
	private static NSMutableArray utilisateurs; // Liste des emails des utilisateurs connectes

	public static void main(String[] argv) {
		ERXApplication.main(argv, Application.class);
	}

	public Application() {
		super();
		//setAllowsConcurrentRequestHandling(false);
		setDefaultRequestHandler(requestHandlerForKey(directActionRequestHandlerKey()));
		setPageRefreshOnBacktrackEnabled(true);
		//		WOMessage.setDefaultEncoding("UTF-8");
		//		WOMessage.setDefaultURLEncoding("UTF-8");
		//		ERXMessageEncoding.setDefaultEncoding("UTF8");
		//		ERXMessageEncoding.setDefaultEncodingForAllLanguages("UTF8");
		utilisateurs = new NSMutableArray();
		//    	setupDatabaseChannelCloserTimer();
		//		ERXEC.setDefaultFetchTimestampLag(2000);
	}

	public void initApplication() {
		System.out.println("Lancement de l'application serveur " + this.name() + "...");
		super.initApplication();

		//Afficher les infos de connexion des modeles de donnees
		rawLogModelInfos();
		//Verifier la coherence des dictionnaires de connexion des modeles de donnees
		boolean isInitialisationErreur = !checkModel();

		Application.isModeDebug = (Application.isDevelopmentModeSafe() || config().booleanForKey("MODE_DEBUG"));
		Application.APP_SHOULD_SEND_COLLECTE = !Application.isDevelopmentModeSafe();
	}

	/**
	 * Execute les operations au demarrage de l'application, juste apres l'initialisation standard de l'application.
	 */
	public void startRunning() {
		//		EODatabaseContext.setDefaultDelegate(this); // A activer si l'application présente des pbs de synchronisation entre snapshots et base de donnees
		initFormatters();
		initTimeZones();
		this.appDateFormatter = new NSTimestampFormatter();
		this.appDateFormatter.setPattern("%d/%m/%Y");

		// Prefetch dans le sharedEditingContext des nomenclatures communes a toute l'appli
		/**
		 * Prefetch dans le sharedEditingContext des nomenclatures communes a toute l'appli Il est necessaire de declarer dans l'eomodel, l'entite a
		 * prefetecher via l'inspecteur: 'Share all objects' --> creation d'un fetchspecificationnamed 'FetchAll' Il est indispensable d'utiliser
		 * l'api 'bindObjectsWithFetchSpecification'
		 */

		// EOSharedEditingContext sedc = EOSharedEditingContext.defaultSharedEditingContext();
		// EOFetchSpecification fetchSpec = EOFetchSpecification.fetchSpecificationNamed("FetchAll", TypeClassificationContrat.ENTITY_NAME);
		// sedc.bindObjectsWithFetchSpecification(fetchSpec, "FetchAll");
	}

	@Override
	public WOResponse dispatchRequest(WORequest aRequest) {
		return super.dispatchRequest(aRequest);
	}

	@Override
	public WORequestHandler handlerForRequest(WORequest aRequest) {
		return super.handlerForRequest(aRequest);
	}

	@Override
	public WOContext createContextForRequest(WORequest aRequest) {
		return super.createContextForRequest(aRequest);
	}

	@Override
	public void awake() {
		super.awake();
	}

	@Override
	public WOSession restoreSessionWithID(String aSessionID, WOContext aContext) {
		return super.restoreSessionWithID(aSessionID, aContext);
	}

	@Override
	public WOResponse createResponseInContext(WOContext aContext) {
		return super.createResponseInContext(aContext);
	}

	@Override
	public void takeValuesFromRequest(WORequest aRequest, WOContext aContext) {
		super.takeValuesFromRequest(aRequest, aContext);
	}

	@Override
	public WOActionResults invokeAction(WORequest request, WOContext context) {
		return super.invokeAction(request, context);
	}

	@Override
	public void appendToResponse(WOResponse aResponse, WOContext aContext) {
		super.appendToResponse(aResponse, aContext);
	}

	@Override
	public void saveSessionForContext(WOContext aContext) {
		super.saveSessionForContext(aContext);
	}

	@Override
	public void sleep() {
		super.sleep();
	}

	public String configFileName() {
		return CONFIG_FILE_NAME;
	}

	public String configTableName() {
		return CONFIG_TABLE_NAME;
	}

	public String[] configMandatoryKeys() {
		return MANDATORY_PARAMS.toArray(new String[] {});
	}

	public String[] configOptionalKeys() {
		return OPTIONAL_PARAMS;
	}

	public boolean appShouldSendCollecte() {
		return APP_SHOULD_SEND_COLLECTE;
	}

	public String copyright() {
		return appVersion().copyright();
	}

	public A_CktlVersion appCktlVersion() {
		return appVersion();
	}

	public Version appVersion() {
		if (_appVersion == null) {
			_appVersion = new Version();
		}
		return _appVersion;
	}

	public String mainModelName() {
		return MAIN_MODEL_NAME;
	}

	public void initFormatters() {
		this.app2DecimalesFormatter = new NSNumberFormatter();
		this.app2DecimalesFormatter.setDecimalSeparator(",");
		this.app2DecimalesFormatter.setThousandSeparator(" ");

		this.app2DecimalesFormatter.setHasThousandSeparators(true);
		this.app2DecimalesFormatter.setPattern("#,##0.00;0,00;-#,##0.00");

		this.app5DecimalesFormatter = new NSNumberFormatter();
		this.app5DecimalesFormatter.setDecimalSeparator(",");
		this.app5DecimalesFormatter.setThousandSeparator(" ");

		this.app5DecimalesFormatter.setHasThousandSeparators(true);
		this.app5DecimalesFormatter.setPattern("##0.00000;0,00000;-##0.00000");
	}

	public NSNumberFormatter app2DecimalesFormatter() {
		return this.app2DecimalesFormatter;
	}

	public NSNumberFormatter getApp5DecimalesFormatter() {
		return this.app5DecimalesFormatter;
	}

	/**
	 * Initialise le TimeZone a utiliser pour l'application.
	 */
	protected void initTimeZones() {
		CktlLog.log("Initialisation du NSTimeZone");
		String tz = config().stringForKey("DEFAULT_NS_TIMEZONE");
		if (tz == null || tz.equals("")) {
			CktlLog.log("Le parametre DEFAULT_NS_TIMEZONE n'est pas defini dans le fichier .config.");
			TimeZone.setDefault(TimeZone.getTimeZone("Europe/Paris"));
			NSTimeZone.setDefaultTimeZone(NSTimeZone.timeZoneWithName("Europe/Paris", false));
		}
		else {
			ntz = NSTimeZone.timeZoneWithName(tz, false);
			if (ntz == null) {
				CktlLog.log("Le parametre DEFAULT_NS_TIMEZONE defini dans le fichier .config n'est pas valide (" + tz + ")");
				TimeZone.setDefault(TimeZone.getTimeZone("Europe/Paris"));
				NSTimeZone.setDefaultTimeZone(NSTimeZone.timeZoneWithName("Europe/Paris", false));
			}
			else {
				TimeZone.setDefault(ntz);
				NSTimeZone.setDefaultTimeZone(ntz);
			}
		}
		ntz = NSTimeZone.defaultTimeZone();
		CktlLog.log("NSTimeZone par defaut utilise dans l'application:" + NSTimeZone.defaultTimeZone());
		NSTimestampFormatter ntf = new NSTimestampFormatter();
		CktlLog.log("Les NSTimestampFormatter analyseront les dates avec le NSTimeZone: " + ntf.defaultParseTimeZone());
		CktlLog.log("Les NSTimestampFormatter afficheront les dates avec le NSTimeZone: " + ntf.defaultFormatTimeZone());
	}

	/**
	 * Retourne le mot de passe du super-administrateur. Il permet de se connecter a l'application avec le nom d'un autre utilisateur
	 * (l'authentification local et non celle CAS doit etre activee dans ce cas).
	 */
	public String getRootPassword() {
		// passpar2
		//return "HO4LI8hKZb81k";
		return super.getRootPassword();
	}

	public WOResponse handleExceptionOld(Exception anException, WOContext context) {
		WOResponse response = null;
		if (context != null && context.hasSession()) {
			Session session = (Session) context.session();
			try {
				NSDictionary extraInfo = extraInformationForExceptionInContext(anException, context);
				CktlMailBus cmb = session.mailBus();
				String smtpServeur = config().stringForKey("GRHUM_HOST_MAIL");
				String destinataires = config().stringForKey("ADMIN_MAIL");

				if (cmb != null && smtpServeur != null && smtpServeur.equals("") == false && destinataires != null
						&& destinataires.equals("") == false) {
					String objet = "[CANNELLE]:Exception:[";
					objet += VersionMe.txtAppliVersion() + "]";
					String contenu = "Date : " + DateCtrl.dateToString(DateCtrl.now(), "%d/%m/%Y %H:%M") + "\n";
					contenu += "OS: " + System.getProperty("os.name") + "\n";
					contenu += "Java vm version: " + System.getProperty("java.vm.version") + "\n";
					contenu += "WO version: " + ERXProperties.webObjectsVersion() + "\n\n";
					contenu += "User agent: " + context.request().headerForKey("user-agent") + "\n\n";
					contenu += "Utilisateur(Numero individu): " + session.applicationUser().getUtilisateur().getPrenomAndNomAndNomPatronymique() + "(" + session.applicationUser().getNoIndividu() + ")" + "\n";

					contenu += "\n\nException : " + "\n";
					if (anException instanceof InvocationTargetException) {
						contenu += getMessage(anException, extraInfo) + "\n";
						anException = (Exception) anException.getCause();
					}
					contenu += getMessage(anException, extraInfo) + "\n";
					contenu += "\n\n";

					session.setMessageErreur(contenu);
					boolean retour = false;
					if (isModeDebug) {
						CktlLog.log("!!!!!!!!!!!!!!!!!!!!!!!! MODE DEVELOPPEMENT : pas de mail !!!!!!!!!!!!!!!!");
						retour = false;
					}
					else {
						retour = cmb.sendMail(destinataires, destinataires, null, objet, contenu);
					}
					if (!retour) {
						CktlLog.log("!!!!!!!!!!!!!!!!!!!!!!!! IMPOSSIBLE d'ENVOYER le mail d'exception !!!!!!!!!!!!!!!!");
						CktlLog.log("\nMail:\n\n" + contenu);

					}

				}
				else {
					CktlLog.log("!!!!!!!!!!!!!!!!!!!!!!!! IMPOSSIBLE d'ENVOYER le mail d'exception !!!!!!!!!!!!!!!!");
					CktlLog.log("Veuillez verifier que les parametres HOST_MAIL et ADMIN_MAIL sont bien renseignes");
					CktlLog.log("GRHUM_HOST_MAIL = " + smtpServeur);
					CktlLog.log("ADMIN_MAIL = " + destinataires);
					CktlLog.log("cmb = " + cmb);
					CktlLog.log("\n\n\n");
				}

				if (AjaxUtils.isAjaxRequest(context.request())) {
					// Create redirect to return to break out of Ajax
					Accueil errorPage = (Accueil) pageWithName(Accueil.class.getName(), context);
					errorPage.setIsOpenFenetreException(true);
					AjaxUtils.redirectTo(errorPage);
					WOResponse redirect = errorPage.context().response();

					// Force errorPage into the page cache, don't do this earlier!
					context.session().savePage(errorPage);
					return redirect;
				}
				else {
					response = createResponseInContext(context);
					NSMutableDictionary formValues = new NSMutableDictionary();
					formValues.setObjectForKey(session.sessionID(), "wosid");
					String applicationExceptionUrl = context.directActionURLForActionNamed("applicationException", formValues);
					response.appendContentString("<script>document.location.href='" + applicationExceptionUrl + "';</script>");

					return response;
				}
			} catch (Exception e) {
				CktlLog.log("\n\n\n");
				CktlLog.log("!!!!!!!!!!!!!!!!!!!!!!!! Exception durant le traitement d'une autre exception !!!!!!!!!!!!!!!!");
				CktlLog.log("Message Exception dans exception: "
						+ e.getMessage());
				CktlLog.log("Stack Exception dans exception: "
						+ e.getStackTrace());
				super.handleException(e, context);
				CktlLog.log("\n");
				CktlLog.log("Message Exception originale: "
						+ anException.getMessage());
				CktlLog.log("Stack Exception dans exception: "
						+ anException.getStackTrace());
				return super.handleException(anException, context);
			}
		}
		else {
			return super.handleException(anException, context);
		}
	}

	protected String getMessage(Throwable e, NSDictionary extraInfo) {
		String message = "";
		if (e != null) {
			message = stackTraceToString(e, false) + "\n\n";
			message += "Info extra :\n";
			if (extraInfo != null) {
				message += NSPropertyListSerialization.stringFromPropertyList(extraInfo) + "\n\n";
			}
		}
		return message;
	}

	/**
	 * permet de recuperer la trace d'une exception au format string message + trace
	 * 
	 * @param e
	 * @return
	 */
	public static String stackTraceToString(Throwable e, boolean useHtml) {
		String tagCR = "\n";
		if (useHtml) {
			tagCR = "<br>";
		}
		String stackStr = e + tagCR + tagCR;
		StackTraceElement[] stack = e.getStackTrace();
		for (int i = 0; i < stack.length; i++) {
			stackStr += (stack[i]).toString() + tagCR;
		}
		return stackStr;
	}

	public NSDictionary databaseContextShouldUpdateCurrentSnapshot(EODatabaseContext dbCtxt, NSDictionary dic, NSDictionary dic2, EOGlobalID gid,
			EODatabaseChannel dbChannel) {
		return dic2;
	}

	public boolean _isSupportedDevelopmentPlatform() {
		return (super._isSupportedDevelopmentPlatform() || System.getProperty("os.name").startsWith("Win"));
	}

	@Override
	public WOResponse handleSessionRestorationErrorInContext(WOContext context) {
		WOResponse response;
		response = createResponseInContext(context);
		String sessionExpiredUrl = context.directActionURLForActionNamed("sessionExpired", null);
		response.appendContentString("<script>document.location.href='" + sessionExpiredUrl + "';</script>");

		return response;
	}

	public NSMutableArray utilisateurs() {
		return utilisateurs;
	}

	public void setupDatabaseChannelCloserTimer() {
		Timer timer = new Timer(true);
		//Close open database connections every four hours.
		timer.scheduleAtFixedRate(new DBChannelCloserTask(), new Date(), 14400000);
	}

	public static String serverBDId() {
		NSMutableArray<String> serverDBIds = new NSMutableArray<String>();
		final NSMutableDictionary mdlsDico = EOModelCtrl.getModelsDico();
		final Enumeration mdls = mdlsDico.keyEnumerator();
		while (mdls.hasMoreElements()) {
			final String mdlName = (String) mdls.nextElement();
			String serverDBId = EOModelCtrl.bdConnexionServerId((EOModel) mdlsDico.objectForKey(mdlName));
			if (serverDBId != null && !serverDBIds.containsObject(serverDBId)) {
				serverDBIds.addObject(serverDBId);
			}
		}
		return serverDBIds.componentsJoinedByString(",");
	}

	class DBChannelCloserTask extends TimerTask {
		public DBChannelCloserTask() {
			super();
		}

		public void run() {
			closeDatabaseChannels();
			NSLog.out.appendln("running timer");
		}

		public void closeDatabaseChannels() {
			int i, contextCount, j, channelCount;
			NSArray databaseContexts;
			EOObjectStoreCoordinator coordinator;
			coordinator = (EOObjectStoreCoordinator) EOObjectStoreCoordinator.defaultCoordinator();
			databaseContexts = coordinator.cooperatingObjectStores();
			contextCount = databaseContexts.count();
			//Iterate through all an app's cooperating object stores (database contexts).
			for (i = 0; i < contextCount; i++) {
				NSArray channels = ((EODatabaseContext) databaseContexts.objectAtIndex(i)).registeredChannels();
				channelCount = channels.count();
				for (j = 0; j < channelCount; j++) {
					//Make sure the channel you're trying to close isn't performing a transaction.
					if (!((EODatabaseChannel) channels.objectAtIndex(j)).adaptorChannel().adaptorContext().hasOpenTransaction()) {
						((EODatabaseChannel) channels.objectAtIndex(j)).adaptorChannel().closeChannel();
					}
				}
			}
		}
	}

}
