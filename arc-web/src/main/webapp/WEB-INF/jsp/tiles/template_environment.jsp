<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@taglib prefix="c" uri="jakarta.tags.core"%>

<div class="row" id="viewEnv">
	<div class="col-3"></div>
	<div class="col-6 d-flex justify-content-center align-self-baseline">
			<h2 class="badge arc-page-title">${envMap[bacASable]}</h2>
	</div>
	<div class="col-3 d-flex justify-content-right">
				<div class="col">
					<textarea name="viewPilotageBAS.customValues['envDescription']" rows="1"
						placeholder="<spring:message code="gui.textarea.envDescription.placeholder"/>"
						class="env-description-field col border-light p-1">${viewPilotageBAS.customValues['envDescription']}</textarea>
				</div>
				<div>
					<button class="btn btn-primary btn-sm m-0" type="submit"
						doAction="updateEnvDescription" scope="viewEnv;">
						<span class="fa fa-save">&nbsp;</span>
						<spring:message code="gui.button.update" />
					</button>
				</div>
	</div>
</div>
<hr class="mt-2 mb-2" />
