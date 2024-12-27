package pl.lodz.p.it.eduvirt.controller;

import lombok.RequiredArgsConstructor;
import org.ovirt.engine.sdk4.types.User;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.lodz.p.it.eduvirt.aspect.logging.LoggerInterceptor;
import pl.lodz.p.it.eduvirt.dto.user.OVirtUserWithPermissionsDto;


import pl.lodz.p.it.eduvirt.dto.user.OvirtUserDto;
import pl.lodz.p.it.eduvirt.mappers.UserMapper;
import pl.lodz.p.it.eduvirt.service.OVirtUserService;

import java.util.List;
import java.util.UUID;

@RestController
@LoggerInterceptor
@RequestMapping("/resources/users")
@RequiredArgsConstructor
public class UserController {

    private final OVirtUserService ovirtUserService;
    private final UserMapper userMapper;

    @GetMapping(path="/permissions",  produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllUsersWithPermissions() {
        List<User> foundUsers = ovirtUserService.getAllUsersWithPermissions();
        List<OVirtUserWithPermissionsDto> userDtos = foundUsers.stream()
                .map(userMapper::ovirtUserWithPermissionsToUserDto)
                .toList();

        if (userDtos.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(userDtos);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllUsers() {
        List<User> foundUsers = ovirtUserService.getAllUsers();
        List<OvirtUserDto> userDtos = foundUsers.stream()
                .map(userMapper::ovirtUserToUserDto)
                .toList();

        if (userDtos.isEmpty()) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(userDtos);
    }

    @GetMapping(path = "/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getUserById(@PathVariable("userId") UUID userId) {
        User foundUser = ovirtUserService.getUserById(userId);
        OVirtUserWithPermissionsDto userDto = userMapper.ovirtUserWithPermissionsToUserDto(foundUser);

        return ResponseEntity.ok(userDto);
    }

}