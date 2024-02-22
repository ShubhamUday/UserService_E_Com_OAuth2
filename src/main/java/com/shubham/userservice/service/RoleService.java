package com.shubham.userservice.service;

import com.shubham.userservice.repository.RoleRepository;
import org.springframework.stereotype.Service;
import com.shubham.userservice.model.Role;

@Service
public class RoleService {
    private RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }
    public Role createRole(String name){
        Role role = new Role();
        role.setRole(name);

        return roleRepository.save(role);
    }
}
