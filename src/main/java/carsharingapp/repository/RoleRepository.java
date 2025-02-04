package carsharingapp.repository;

import carsharingapp.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RoleRepository extends JpaRepository<Role, Long> {
    @Query("FROM Role r WHERE r.name = :name")
    Role findByRoleName(Role.RoleName name);
}
