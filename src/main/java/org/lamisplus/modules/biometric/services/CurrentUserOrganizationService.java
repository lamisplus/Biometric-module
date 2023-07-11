package org.lamisplus.modules.biometric.services;

import lombok.RequiredArgsConstructor;
import org.lamisplus.modules.base.domain.entities.User;
import org.lamisplus.modules.base.service.UserService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CurrentUserOrganizationService {
    private  final UserService userService;
    public Long getCurrentUserOrganization() {
        Optional<User> userWithRoles = userService.getUserWithRoles ();
        return userWithRoles.map (User::getCurrentOrganisationUnitId).orElse (null);
    }

    /*public String getCurrentUserOrganizationDatimId() {
        Optional<User> userWithRoles = userService.getUserWithRoles ();

        return userWithRoles.map (User::).orElse (null);
    }*/
}
