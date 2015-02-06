package gov.va.escreening.delegate;

import gov.va.escreening.security.EscreenUser;
import gov.va.escreening.vista.dto.VistaClinicAppointment;

import java.util.List;

public interface BatchBatteryCreateDelegate {
	List<VistaClinicAppointment> searchVeteranByAppointments(EscreenUser user, int clinicID, 
			String startdate, String enddate);

}
