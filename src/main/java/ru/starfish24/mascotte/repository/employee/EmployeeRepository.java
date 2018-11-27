package ru.starfish24.mascotte.repository.employee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import ru.starfish24.starfish24model.Employee;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Employee findByLogin(@Param("login") String login);

    Employee findById(@Param("id") Long id);

    List<Employee> findByGroupCode(@Param("accountId") Long accountId, @Param("code") String code);

    List<Employee> findByGroupShop(@Param("accountId") Long accountId, @Param("shopId") Long shopId, @Param("code") String code);
}
