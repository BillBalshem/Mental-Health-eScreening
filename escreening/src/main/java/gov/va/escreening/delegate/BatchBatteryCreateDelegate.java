package gov.va.escreening.delegate;

import gov.va.escreening.domain.VeteranWithClinicalReminderFlag;
import gov.va.escreening.dto.BatchBatteryCreateResult;
import gov.va.escreening.security.EscreenUser;
import gov.va.escreening.vista.dto.VistaClinicAppointment;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface BatchBatteryCreateDelegate {
	List<VistaClinicAppointment> searchVeteranByAppointments(EscreenUser user, String clinicIen, 
			String startdate, String enddate);
	
	/**
	 * Returns veteranDTOs from the local DB, if veteran does not exist, import first.
	 * @param veteranIens
	 * @return
	 */
	List<VeteranWithClinicalReminderFlag> getVeteranDetails(String[] veteranIens, EscreenUser user, List<VistaClinicAppointment> appList);

	List<VistaClinicAppointment> searchVeteranByAppointments(EscreenUser user,
			String clinicIen, Date start, Date end);


	List<BatchBatteryCreateResult> batchCreate(
			List<VeteranWithClinicalReminderFlag> vets, int programId,
			int clinicId, int clinicianId, int noteTitleId, int batteryId,
			Map<Integer, Set<Integer>> surveyMap, List<Integer> selectedSurvey,
			EscreenUser escreenUser);


}
