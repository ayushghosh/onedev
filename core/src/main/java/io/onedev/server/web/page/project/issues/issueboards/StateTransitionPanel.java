package io.onedev.server.web.page.project.issues.issueboards;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;

import io.onedev.server.OneDev;
import io.onedev.server.manager.IssueActionManager;
import io.onedev.server.model.Issue;
import io.onedev.server.model.support.issue.workflow.TransitionSpec;
import io.onedev.server.model.support.issue.workflow.transitiontrigger.PressButtonTrigger;
import io.onedev.server.util.IssueUtils;
import io.onedev.server.util.inputspec.InputContext;
import io.onedev.server.util.inputspec.InputSpec;
import io.onedev.server.web.editable.BeanContext;
import io.onedev.server.web.editable.BeanEditor;

@SuppressWarnings("serial")
abstract class StateTransitionPanel extends Panel implements InputContext {

	public StateTransitionPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		Class<?> fieldBeanClass = IssueUtils.defineBeanClass(getIssue().getProject());
		Serializable fieldBean = getIssue().getFieldBean(fieldBeanClass, true);

		Form<?> form = new Form<Void>("form");
		form.setOutputMarkupId(true);
		add(form);
		
		form.add(new Label("state", getTransition().getToState()));
		
		PressButtonTrigger trigger = (PressButtonTrigger) getTransition().getTrigger();
		
		BeanEditor editor = BeanContext.editBean("editor", fieldBean, 
				IssueUtils.getPropertyNames(fieldBeanClass, trigger.getPromptFields()), false); 
		form.add(editor);
		
		form.add(new AjaxButton("ok") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				
				getIssue().removeFields(getTransition().getRemoveFields());
				Map<String, Object> fieldValues = IssueUtils.getFieldValues(fieldBean, trigger.getPromptFields());
				OneDev.getInstance(IssueActionManager.class).changeState(getIssue(), getTransition().getToState(), fieldValues, null);
				onSaved(target);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				super.onError(target, form);
				target.add(form);
			}

		});
		
		form.add(new AjaxLink<Void>("close") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancelled(target);
			}
			
		});
		
		form.add(new AjaxLink<Void>("cancel") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancelled(target);
			}
			
		});
		
	}

	@Override
	public List<String> getInputNames() {
		throw new UnsupportedOperationException();
	}

	@Override
	public InputSpec getInputSpec(String inputName) {
		return getIssue().getProject().getIssueWorkflow().getFieldSpec(inputName);
	}
	
	@Override
	public boolean isReservedName(String inputName) {
		throw new UnsupportedOperationException();
	}
	
	protected abstract Issue getIssue();
	
	protected abstract void onSaved(AjaxRequestTarget target);
	
	protected abstract void onCancelled(AjaxRequestTarget target);
	
	protected abstract TransitionSpec getTransition();
}
