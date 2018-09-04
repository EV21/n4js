package org.eclipse.n4js.ui.proposals.packagejson;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.n4js.json.JSON.JSONArray;
import org.eclipse.n4js.json.JSON.JSONDocument;
import org.eclipse.n4js.json.JSON.JSONObject;
import org.eclipse.n4js.json.JSON.NameValuePair;
import org.eclipse.n4js.json.model.utils.JSONModelUtils;
import org.eclipse.n4js.json.ui.contentassist.IJSONProposalProvider;
import org.eclipse.n4js.json.ui.contentassist.NameValuePairProposalFactory;
import org.eclipse.n4js.packagejson.PackageJsonProperties;
import org.eclipse.n4js.projectModel.IN4JSProject;
import org.eclipse.n4js.resource.XpectAwareFileExtensionCalculator;
import org.eclipse.n4js.utils.languages.N4LanguageUtils;
import org.eclipse.xtext.Keyword;
import org.eclipse.xtext.RuleCall;
import org.eclipse.xtext.ui.editor.contentassist.ContentAssistContext;
import org.eclipse.xtext.ui.editor.contentassist.ICompletionProposalAcceptor;

import com.google.inject.Inject;

/**
 * A factory for creating {@link ICompletionProposal}s that insert new name-value-pairs into a JSON Document.
 */
public class PackageJsonProposalProvider implements IJSONProposalProvider {

	@Inject
	private XpectAwareFileExtensionCalculator fileExtensionCalculator;

	@Override
	public boolean isResponsible(EObject eObject) {
		// this validator extension only applies to package.json files
		return fileExtensionCalculator.getFilenameWithoutXpectExtension(eObject.eResource().getURI())
				.equals(IN4JSProject.PACKAGE_JSON);
	}

	@Override
	public void complete_NameValuePair(EObject model, RuleCall ruleCall, ContentAssistContext context,
			ICompletionProposalAcceptor acceptor) {

		NameValuePairProposalFactory nvpFactory = N4LanguageUtils
				.getServiceForContext(JSONModelUtils.FILE_EXTENSION, NameValuePairProposalFactory.class).get();

		List<String> namePath = getJsonPathNames(model);
		Set<String> alreadyUsedNames = getAlreadyUsedNames(model);

		List<PackageJsonProperties> pathProps = PackageJsonProperties.valuesOfPath(namePath);
		for (PackageJsonProperties pathProp : pathProps) {
			String name = pathProp.name;
			if (alreadyUsedNames.contains(name)) {
				continue;
			}
			String value = "\"\"";
			if (pathProp.valueType == JSONArray.class) {
				value = "[]";
			}
			if (pathProp.valueType == JSONObject.class) {
				value = "{}";
			}
			String descr = pathProp.description;
			ICompletionProposal pairProposal = nvpFactory.createNameValuePairProposal(context, name, value, descr);
			acceptor.accept(pairProposal);
		}
	}

	private List<String> getJsonPathNames(EObject model) {
		List<String> namePath = new LinkedList<>();
		EObject eobj = model;
		while (!(eobj instanceof JSONDocument)) {

			if (eobj instanceof NameValuePair) {
				NameValuePair nvp = (NameValuePair) eobj;
				namePath.add(0, nvp.getName());
			}

			eobj = eobj.eContainer();
		}
		return namePath;
	}

	private Set<String> getAlreadyUsedNames(EObject model) {
		Set<String> alreadyUsedNames = new HashSet<>();
		if (model instanceof JSONObject) {
			JSONObject jsonObj = (JSONObject) model;
			for (NameValuePair nvp : jsonObj.getNameValuePairs()) {
				alreadyUsedNames.add(nvp.getName());
			}
		}
		return alreadyUsedNames;
	}

	@Override
	public void complete_JSONArray(EObject model, RuleCall ruleCall, ContentAssistContext context,
			ICompletionProposalAcceptor acceptor) {
		// empty
	}

	@Override
	public void complete_STRING(EObject model, RuleCall ruleCall, ContentAssistContext context,
			ICompletionProposalAcceptor acceptor) {
		// empty
	}

	@Override
	public void completeKeyword(Keyword keyword, ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
		// empty
	}

	@Override
	public void complete_JSONObject(EObject model, RuleCall ruleCall, ContentAssistContext context,
			ICompletionProposalAcceptor acceptor) {
		// empty
	}

}
