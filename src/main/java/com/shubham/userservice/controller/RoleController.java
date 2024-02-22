package com.shubham.userservice.controller;

import com.shubham.userservice.dto.CreateRoleRequestDto;
import com.shubham.userservice.model.Role;
import com.shubham.userservice.service.RoleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/roles")
public class RoleController {
    private RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }
    @PostMapping
    public ResponseEntity<Role> createRole(@RequestBody CreateRoleRequestDto requestDto){
        Role role = roleService.createRole(requestDto.getName());

        return new ResponseEntity<>(role, HttpStatus.OK);
    }
}
