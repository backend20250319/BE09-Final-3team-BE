package org.example.petservice.admin.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="user-service",url = "${user-service.url:http://localhost:8000}")
public interface UserClient {
    @GetMapping("/api/v1/user-service/admin/users/{id}")
    UserResponse getUserById(@PathVariable("id") Long userId);
}
